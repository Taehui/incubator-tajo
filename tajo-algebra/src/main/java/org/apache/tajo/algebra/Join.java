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

package org.apache.tajo.algebra;

import org.apache.tajo.util.TUtil;

import java.util.Arrays;

public class Join extends BinaryOperator {
  private JoinType joinType;
  private Expr joinQual;
  private ColumnReferenceExpr[] joinColumns;
  private boolean natural = false;

  public Join(JoinType joinType) {
    super(OpType.Join);
    this.joinType = joinType;
  }

  public JoinType getJoinType() {
    return  this.joinType;
  }

  public boolean hasQual() {
    return this.joinQual != null;
  }

  public Expr getQual() {
    return this.joinQual;
  }

  public void setQual(Expr expr) {
    this.joinQual = expr;
  }

  public boolean hasJoinColumns() {
    return joinColumns != null;
  }

  public ColumnReferenceExpr[] getJoinColumns() {
    return joinColumns;
  }

  public void setJoinColumns(ColumnReferenceExpr[] columns) {
    joinColumns = columns;
  }

  public void setNatural() {
    natural = true;
  }

  public boolean isNatural() {
    return natural;
  }

  boolean equalsTo(Expr expr) {
    Join another = (Join) expr;
    return joinType.equals(another.joinType) &&
        TUtil.checkEquals(joinQual, another.joinQual) &&
        TUtil.checkEquals(joinColumns, another.joinColumns) &&
        natural == another.natural;
  }

  @Override
  public String toJson() {
    return JsonHelper.toJson(this);
  }

  @Override
  public int hashCode() {
    int result = getType().hashCode();
    result = 31 * result + joinType.hashCode();
    result = 31 * result + (joinQual != null ? joinQual.hashCode() : 0);
    result = 31 * result + (joinColumns != null ? Arrays.hashCode(joinColumns) : 0);
    result = 31 * result + (natural ? 1 : 0);
    return result;
  }
}
