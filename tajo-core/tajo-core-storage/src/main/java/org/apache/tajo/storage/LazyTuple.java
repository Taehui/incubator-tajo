/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tajo.storage;

import com.google.protobuf.Message;
import org.apache.commons.codec.binary.Base64;
import org.apache.tajo.catalog.Schema;
import org.apache.tajo.common.TajoDataTypes;
import org.apache.tajo.datum.*;
import org.apache.tajo.datum.exception.InvalidCastException;
import org.apache.tajo.datum.protobuf.ProtobufJsonFormat;
import org.apache.tajo.util.Bytes;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

public class LazyTuple implements Tuple {
  private long offset;
  private Datum[] values;
  private byte[][] textBytes;
  private Schema schema;
  private byte[] nullBytes;

  public LazyTuple(Schema schema, byte[][] textBytes, long offset) {
    this(schema, textBytes, offset, NullDatum.get().asTextBytes());
  }

  public LazyTuple(Schema schema, byte[][] textBytes, long offset, byte[] nullBytes) {
    this.schema = schema;
    this.textBytes = textBytes;
    this.values = new Datum[schema.getColumnNum()];
    this.offset = offset;
    this.nullBytes = nullBytes;
  }

  public LazyTuple(LazyTuple tuple) {
    this.values = new Datum[tuple.size()];
    System.arraycopy(tuple.values, 0, values, 0, tuple.size());
    this.offset = tuple.offset;
    this.schema = tuple.schema;
    this.textBytes = tuple.textBytes.clone();
    this.nullBytes = tuple.nullBytes;
  }

  @Override
  public int size() {
    return values.length;
  }

  @Override
  public boolean contains(int fieldid) {
    return textBytes[fieldid] != null || values[fieldid] != null;
  }

  @Override
  public boolean isNull(int fieldid) {
    return get(fieldid) instanceof NullDatum;
  }

  @Override
  public void clear() {
    for (int i = 0; i < values.length; i++) {
      values[i] = null;
      textBytes[i] = null;
    }
  }

  //////////////////////////////////////////////////////
  // Setter
  //////////////////////////////////////////////////////
  @Override
  public void put(int fieldId, Datum value) {
    values[fieldId] = value;
    textBytes[fieldId] = null;
  }

  @Override
  public void put(int fieldId, Datum[] values) {
    for (int i = fieldId, j = 0; j < values.length; i++, j++) {
      this.values[i] = values[j];
    }
    this.textBytes = new byte[values.length][];
  }

  @Override
  public void put(int fieldId, Tuple tuple) {
    for (int i = fieldId, j = 0; j < tuple.size(); i++, j++) {
      values[i] = tuple.get(j);
      textBytes[i] = null;
    }
  }

  @Override
  public void put(Datum[] values) {
    System.arraycopy(values, 0, this.values, 0, size());
    this.textBytes = new byte[values.length][];
  }

  //////////////////////////////////////////////////////
  // Getter
  //////////////////////////////////////////////////////
  @Override
  public Datum get(int fieldId) {
    if (values[fieldId] != null)
      return values[fieldId];
    else if (textBytes.length <= fieldId) {
      values[fieldId] = NullDatum.get();  // split error. (col : 3, separator: ',', row text: "a,")
    } else if (textBytes[fieldId] != null) {
      values[fieldId] = createByTextBytes(schema.getColumn(fieldId).getDataType(), textBytes[fieldId]);
      textBytes[fieldId] = null;
    } else {
      //non-projection
    }
    return values[fieldId];
  }

  @Override
  public void setOffset(long offset) {
    this.offset = offset;
  }

  @Override
  public long getOffset() {
    return this.offset;
  }

  @Override
  public BooleanDatum getBoolean(int fieldId) {
    return (BooleanDatum) get(fieldId);
  }

  @Override
  public BitDatum getByte(int fieldId) {
    return (BitDatum) get(fieldId);
  }

  @Override
  public CharDatum getChar(int fieldId) {
    return (CharDatum) get(fieldId);
  }

  @Override
  public BlobDatum getBytes(int fieldId) {
    return (BlobDatum) get(fieldId);
  }

  @Override
  public Int2Datum getShort(int fieldId) {
    return (Int2Datum) get(fieldId);
  }

  @Override
  public Int4Datum getInt(int fieldId) {
    return (Int4Datum) get(fieldId);
  }

  @Override
  public Int8Datum getLong(int fieldId) {
    return (Int8Datum) get(fieldId);
  }

  @Override
  public Float4Datum getFloat(int fieldId) {
    return (Float4Datum) get(fieldId);
  }

  @Override
  public Float8Datum getDouble(int fieldId) {
    return (Float8Datum) get(fieldId);
  }

  @Override
  public Inet4Datum getIPv4(int fieldId) {
    return (Inet4Datum) get(fieldId);
  }

  @Override
  public byte[] getIPv4Bytes(int fieldId) {
    return get(fieldId).asByteArray();
  }

  @Override
  public InetAddress getIPv6(int fieldId) {
    throw new InvalidCastException("IPv6 is unsupported yet");
  }

  @Override
  public byte[] getIPv6Bytes(int fieldId) {
    throw new InvalidCastException("IPv6 is unsupported yet");
  }

  @Override
  public TextDatum getString(int fieldId) {
    return (TextDatum) get(fieldId);
  }

  @Override
  public TextDatum getText(int fieldId) {
    return (TextDatum) get(fieldId);
  }

  public byte[] getTextBytes(int fieldId) {
    if(textBytes[fieldId] != null)
      return textBytes[fieldId];
    else {
      return get(fieldId).asTextBytes();
    }
  }

  public String toString() {
    boolean first = true;
    StringBuilder str = new StringBuilder();
    str.append("(");
    Datum d;
    for (int i = 0; i < values.length; i++) {
      d = get(i);
      if (d != null) {
        if (first) {
          first = false;
        } else {
          str.append(", ");
        }
        str.append(i)
            .append("=>")
            .append(d);
      }
    }
    str.append(")");
    return str.toString();
  }

  @Override
  public int hashCode() {
    int hashCode = 37;
    for (int i = 0; i < values.length; i++) {
      Datum d = get(i);
      if (d != null) {
        hashCode ^= (d.hashCode() * 41);
      } else {
        hashCode = hashCode ^ (i + 17);
      }
    }

    return hashCode;
  }

  public Datum[] toArray() {
    Datum[] datums = new Datum[values.length];
    for (int i = 0; i < values.length; i++) {
      datums[i] = get(i);
    }
    return datums;
  }

  @Override
  public Tuple clone() throws CloneNotSupportedException {
    return new LazyTuple(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LazyTuple) {
      LazyTuple other = (LazyTuple) obj;
      return Arrays.equals(toArray(), other.toArray());
    } else if (obj instanceof VTuple) {
      VTuple other = (VTuple) obj;
      return Arrays.equals(toArray(), other.values);
    }
    return false;
  }


  public  boolean isNull(byte[] val){
    return val == null || val.length == 0 || ((val.length == nullBytes.length) && Bytes.equals(val, nullBytes));
  }

  public  boolean isNullText(byte[] val){
    return val == null || (val.length > 0 && val.length == nullBytes.length && Bytes.equals(val, nullBytes));
  }

  public boolean isNotNull(byte[] val){
    return !isNull(val);
  }

  public boolean isNotNullText(byte[] val){
    return !isNullText((val));
  }

  private  Datum createByTextBytes(TajoDataTypes.DataType type, byte [] val) {
    switch (type.getType()) {
      case BOOLEAN:
        return isNotNull(val) ? DatumFactory.createBool(val[0] == 't' || val[0] == 'T') : NullDatum.get();
      case INT2:
        return isNotNull(val) ? DatumFactory.createInt2(new String(val)) : NullDatum.get();
      case INT4:
        return isNotNull(val) ? DatumFactory.createInt4(new String(val)) : NullDatum.get();
      case INT8:
        return isNotNull(val) ? DatumFactory.createInt8(new String(val)) : NullDatum.get();
      case FLOAT4:
        return isNotNull(val) ? DatumFactory.createFloat4(new String(val)) : NullDatum.get();
      case FLOAT8:
        return isNotNull(val) ? DatumFactory.createFloat8(new String(val)) : NullDatum.get();
      case CHAR:
        return isNotNullText(val) ? DatumFactory.createChar(new String(val).trim()) : NullDatum.get();
      case TEXT:
        return isNotNullText(val) ? DatumFactory.createText(val) : NullDatum.get();
      case BIT:
        return DatumFactory.createBit(Byte.parseByte(new String(val)));
      case BLOB:
        return DatumFactory.createBlob(Base64.decodeBase64(val));
      case INET4:
        return isNotNull(val) ? DatumFactory.createInet4(new String(val)) : NullDatum.get();
      case PROTOBUF: {
        if (isNotNull(val)) {
          ProtobufDatumFactory factory = ProtobufDatumFactory.get(type);
          Message.Builder builder = factory.newBuilder();
          try {
            ProtobufJsonFormat.getInstance().merge(val, builder);
            return factory.createDatum(builder.build());
          } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        } else {
          return NullDatum.get();
        }
      }
      case NULL:
        return NullDatum.get();
      default:
        throw new UnsupportedOperationException(type.toString());
    }
  }
}
