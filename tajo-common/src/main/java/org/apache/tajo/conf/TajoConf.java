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

package org.apache.tajo.conf;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.tajo.TajoConstants;
import org.apache.tajo.util.NetUtils;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class TajoConf extends YarnConfiguration {

  static {
    Configuration.addDefaultResource("catalog-default.xml");
    Configuration.addDefaultResource("catalog-site.xml");
    Configuration.addDefaultResource("storage-default.xml");
    Configuration.addDefaultResource("storage-site.xml");
    Configuration.addDefaultResource("tajo-default.xml");
    Configuration.addDefaultResource("tajo-site.xml");
  }

  private static final String EMPTY_VALUE = "";

  private static final Map<String, ConfVars> vars = Maps.newHashMap();

  public TajoConf() {
    super();
  }

  public TajoConf(Configuration conf) {
    super(conf);
  }

  public TajoConf(Path path) {
    super();
    addResource(path);
  }

  public static enum ConfVars {

    //////////////////////////////////
    // Tajo System Configuration
    //////////////////////////////////

    // a username for a running Tajo cluster
    ROOT_DIR("tajo.rootdir", "file:///tmp/tajo-${user.name}/"),
    USERNAME("tajo.username", "${user.name}"),

    // Configurable System Directories
    WAREHOUSE_DIR("tajo.warehouse.directory", EMPTY_VALUE),
    STAGING_ROOT_DIR("tajo.staging.directory", "/tmp/tajo-${user.name}/staging"),

    SYSTEM_CONF_PATH("tajo.system-conf.path", EMPTY_VALUE),
    SYSTEM_CONF_REPLICA_COUNT("tajo.system-conf.replica-count", 20),

    // Tajo Master Service Addresses
    TAJO_MASTER_UMBILICAL_RPC_ADDRESS("tajo.master.umbilical-rpc.address", "localhost:26001"),
    TAJO_MASTER_CLIENT_RPC_ADDRESS("tajo.master.client-rpc.address", "localhost:26002"),
    TAJO_MASTER_INFO_ADDRESS("tajo.master.info-http.address", "0.0.0.0:26080"),

    // Tajo Worker Service Addresses
    WORKER_INFO_ADDRESS("tajo.worker.info-http.address", "0.0.0.0:28080"),
    WORKER_QM_INFO_ADDRESS("tajo.worker.qm-info-http.address", "0.0.0.0:28081"),
    WORKER_PEER_RPC_ADDRESS("tajo.worker.peer-rpc.address", "0.0.0.0:28091"),
    WORKER_CLIENT_RPC_ADDRESS("tajo.worker.client-rpc.address", "0.0.0.0:28092"),
    WORKER_QM_RPC_ADDRESS("tajo.worker.qm-rpc.address", "0.0.0.0:28093"),

    // Tajo Worker Temporal Directories
    WORKER_TEMPORAL_DIR("tajo.worker.tmpdir.locations", "/tmp/tajo-${user.name}/tmpdir"),

    // Tajo Worker Resources
    WORKER_RESOURCE_AVAILABLE_CPU_CORES("tajo.worker.resource.cpu-cores", 1),
    WORKER_RESOURCE_AVAILABLE_MEMORY_MB("tajo.worker.resource.memory-mb", 1024),
    WORKER_RESOURCE_AVAILABLE_DISKS("tajo.worker.resource.disks", 1),
    WORKER_EXECUTION_MAX_SLOTS("tajo.worker.parallel-execution.max-num", 2),

    // Tajo Worker Dedicated Resources
    WORKER_RESOURCE_DEDICATED("tajo.worker.resource.dedicated", false),
    WORKER_RESOURCE_DEDICATED_MEMORY_RATIO("tajo.worker.resource.dedicated-memory-ratio", 0.8f),

    // Tajo Worker History
    WORKER_HISTORY_EXPIRE_PERIOD("tajo.worker.history.expire-interval-minutes", 12 * 60), // 12 hours

    WORKER_HEARTBEAT_TIMEOUT("tajo.worker.heartbeat.timeout", 120 * 1000),  //120 sec

    // Resource Manager
    RESOURCE_MANAGER_CLASS("tajo.resource.manager", "org.apache.tajo.master.rm.TajoWorkerResourceManager"),

    // Catalog
    CATALOG_ADDRESS("tajo.catalog.client-rpc.address", "localhost:26005"),

    //////////////////////////////////
    // for Yarn Resource Manager
    //////////////////////////////////
    /** how many launching TaskRunners in parallel */
    YARN_RM_QUERY_MASTER_MEMORY_MB("tajo.querymaster.memory-mb", 512),
    YARN_RM_QUERY_MASTER_DISKS("tajo.yarn-rm.querymaster.disks", 1),
    YARN_RM_TASKRUNNER_LAUNCH_PARALLEL_NUM("tajo.yarn-rm.parallel-task-runner-launcher-num", 16),
    YARN_RM_WORKER_NUMBER_PER_NODE("tajo.yarn-rm.max-worker-num-per-node", 8),

    //////////////////////////////////
    // Query Configuration
    //////////////////////////////////
    QUERY_SESSION_TIMEOUT("tajo.query.session.timeout-sec", 60),

    //////////////////////////////////
    // Shuffle Configuration
    //////////////////////////////////
    PULLSERVER_PORT("tajo.pullserver.port", 0),
    SHUFFLE_SSL_ENABLED_KEY("tajo.pullserver.ssl.enabled", false),

    //////////////////////////////////
    // Storage Configuration
    //////////////////////////////////
    RAWFILE_SYNC_INTERVAL("rawfile.sync.interval", null),
    // for RCFile
    HIVEUSEEXPLICITRCFILEHEADER("tajo.exec.rcfile.use.explicit.header", true),

    // for Storage Manager v2
    STORAGE_MANAGER_VERSION_2("tajo.storage-manager.v2", false),
    STORAGE_MANAGER_DISK_SCHEDULER_MAX_READ_BYTES_PER_SLOT("tajo.storage-manager.max-read-bytes", 8 * 1024 * 1024),
    STORAGE_MANAGER_DISK_SCHEDULER_REPORT_INTERVAL("tajo.storage-manager.disk-scheduler.report-interval", 60 * 1000),
    STORAGE_MANAGER_CONCURRENCY_PER_DISK("tajo.storage-manager.disk-scheduler.per-disk-concurrency", 2),

    //////////////////////////////////////////
    // Distributed Query Execution Parameters
    //////////////////////////////////////////
    DIST_QUERY_BROADCAST_JOIN_THRESHOLD("tajo.dist-query.join.broadcast.threshold-bytes", (long)5 * 1048576),

    DIST_QUERY_JOIN_TASK_VOLUME("tajo.dist-query.join.task-volume-mb", 128),
    DIST_QUERY_SORT_TASK_VOLUME("tajo.dist-query.sort.task-volume-mb", 128),
    DIST_QUERY_GROUPBY_TASK_VOLUME("tajo.dist-query.groupby.task-volume-mb", 128),

    DIST_QUERY_JOIN_PARTITION_VOLUME("tajo.dist-query.join.partition-volume-mb", 128),
    DIST_QUERY_SORT_PARTITION_VOLUME("tajo.dist-query.sort.partition-volume-mb", 256),
    DIST_QUERY_GROUPBY_PARTITION_VOLUME("tajo.dist-query.groupby.partition-volume-mb", 256),

    //////////////////////////////////
    // Physical Executors
    //////////////////////////////////
    EXECUTOR_SORT_EXTENAL_BUFFER_SIZE("tajo.executor.sort.external-buffer-num", 1000000),
    EXECUTOR_INNER_JOIN_INMEMORY_HASH_TABLE_SIZE("tajo.executor.join.inner.in-memory-table-num", (long)1000000),
    EXECUTOR_INNER_JOIN_INMEMORY_HASH_THRESHOLD("tajo.executor.join.inner.in-memory-hash-threshold-bytes",
        (long)256 * 1048576),
    EXECUTOR_OUTER_JOIN_INMEMORY_HASH_THRESHOLD("tajo.eecutor.join.outer.in-memory-hash-threshold-bytes",
        (long)256 * 1048576),
    EXECUTOR_GROUPBY_INMEMORY_HASH_THRESHOLD("tajo.executor.groupby.in-memory-hash-threshold-bytes",
        (long)256 * 1048576),

    //////////////////////////////////
    // RPC
    //////////////////////////////////
    RPC_POOL_MAX_IDLE("tajo.rpc.pool.idle.max", 10),

    //////////////////////////////////
    // The Below is reserved
    //////////////////////////////////

    // GeoIP
    GEOIP_DATA("tajo.geoip.data", ""),

    //////////////////////////////////
    // Hive Configuration
    //////////////////////////////////
    HIVE_QUERY_MODE("tajo.hive.query.mode", false),
    ;

    public final String varname;
    public final String defaultVal;
    public final int defaultIntVal;
    public final long defaultLongVal;
    public final float defaultFloatVal;
    public final Class<?> valClass;
    public final boolean defaultBoolVal;

    private final VarType type;

    ConfVars(String varname, String defaultVal) {
      this.varname = varname;
      this.valClass = String.class;
      this.defaultVal = defaultVal;
      this.defaultIntVal = -1;
      this.defaultLongVal = -1;
      this.defaultFloatVal = -1;
      this.defaultBoolVal = false;
      this.type = VarType.STRING;
    }

    ConfVars(String varname, int defaultIntVal) {
      this.varname = varname;
      this.valClass = Integer.class;
      this.defaultVal = Integer.toString(defaultIntVal);
      this.defaultIntVal = defaultIntVal;
      this.defaultLongVal = -1;
      this.defaultFloatVal = -1;
      this.defaultBoolVal = false;
      this.type = VarType.INT;
    }

    ConfVars(String varname, long defaultLongVal) {
      this.varname = varname;
      this.valClass = Long.class;
      this.defaultVal = Long.toString(defaultLongVal);
      this.defaultIntVal = -1;
      this.defaultLongVal = defaultLongVal;
      this.defaultFloatVal = -1;
      this.defaultBoolVal = false;
      this.type = VarType.LONG;
    }

    ConfVars(String varname, float defaultFloatVal) {
      this.varname = varname;
      this.valClass = Float.class;
      this.defaultVal = Float.toString(defaultFloatVal);
      this.defaultIntVal = -1;
      this.defaultLongVal = -1;
      this.defaultFloatVal = defaultFloatVal;
      this.defaultBoolVal = false;
      this.type = VarType.FLOAT;
    }

    ConfVars(String varname, boolean defaultBoolVal) {
      this.varname = varname;
      this.valClass = Boolean.class;
      this.defaultVal = Boolean.toString(defaultBoolVal);
      this.defaultIntVal = -1;
      this.defaultLongVal = -1;
      this.defaultFloatVal = -1;
      this.defaultBoolVal = defaultBoolVal;
      this.type = VarType.BOOLEAN;
    }

    enum VarType {
      STRING { void checkType(String value) throws Exception { } },
      INT { void checkType(String value) throws Exception { Integer.valueOf(value); } },
      LONG { void checkType(String value) throws Exception { Long.valueOf(value); } },
      FLOAT { void checkType(String value) throws Exception { Float.valueOf(value); } },
      BOOLEAN { void checkType(String value) throws Exception { Boolean.valueOf(value); } };

      boolean isType(String value) {
        try { checkType(value); } catch (Exception e) { return false; }
        return true;
      }
      String typeString() { return name().toUpperCase();}
      abstract void checkType(String value) throws Exception;
    }
  }

  public static int getIntVar(Configuration conf, ConfVars var) {
    assert (var.valClass == Integer.class);
    return conf.getInt(var.varname, var.defaultIntVal);
  }

  public static void setIntVar(Configuration conf, ConfVars var, int val) {
    assert (var.valClass == Integer.class);
    conf.setInt(var.varname, val);
  }

  public int getIntVar(ConfVars var) {
    return getIntVar(this, var);
  }

  public void setIntVar(ConfVars var, int val) {
    setIntVar(this, var, val);
  }

  public static long getLongVar(Configuration conf, ConfVars var) {
    assert (var.valClass == Long.class);
    return conf.getLong(var.varname, var.defaultLongVal);
  }

  public static long getLongVar(Configuration conf, ConfVars var, long defaultVal) {
    return conf.getLong(var.varname, defaultVal);
  }

  public static void setLongVar(Configuration conf, ConfVars var, long val) {
    assert (var.valClass == Long.class);
    conf.setLong(var.varname, val);
  }

  public long getLongVar(ConfVars var) {
    return getLongVar(this, var);
  }

  public void setLongVar(ConfVars var, long val) {
    setLongVar(this, var, val);
  }

  public static float getFloatVar(Configuration conf, ConfVars var) {
    assert (var.valClass == Float.class);
    return conf.getFloat(var.varname, var.defaultFloatVal);
  }

  public static float getFloatVar(Configuration conf, ConfVars var, float defaultVal) {
    return conf.getFloat(var.varname, defaultVal);
  }

  public static void setFloatVar(Configuration conf, ConfVars var, float val) {
    assert (var.valClass == Float.class);
    conf.setFloat(var.varname, val);
  }

  public float getFloatVar(ConfVars var) {
    return getFloatVar(this, var);
  }

  public void setFloatVar(ConfVars var, float val) {
    setFloatVar(this, var, val);
  }

  public static boolean getBoolVar(Configuration conf, ConfVars var) {
    assert (var.valClass == Boolean.class);
    return conf.getBoolean(var.varname, var.defaultBoolVal);
  }

  public static boolean getBoolVar(Configuration conf, ConfVars var, boolean defaultVal) {
    return conf.getBoolean(var.varname, defaultVal);
  }

  public static void setBoolVar(Configuration conf, ConfVars var, boolean val) {
    assert (var.valClass == Boolean.class);
    conf.setBoolean(var.varname, val);
  }

  public boolean getBoolVar(ConfVars var) {
    return getBoolVar(this, var);
  }

  public void setBoolVar(ConfVars var, boolean val) {
    setBoolVar(this, var, val);
  }

  public static String getVar(Configuration conf, ConfVars var) {
    assert (var.valClass == String.class);
    return conf.get(var.varname, var.defaultVal);
  }

  public static String getVar(Configuration conf, ConfVars var, String defaultVal) {
    return conf.get(var.varname, defaultVal);
  }

  public static void setVar(Configuration conf, ConfVars var, String val) {
    assert (var.valClass == String.class);
    conf.set(var.varname, val);
  }

  public static ConfVars getConfVars(String name) {
    return vars.get(name);
  }

  public String getVar(ConfVars var) {
    return getVar(this, var);
  }

  public void setVar(ConfVars var, String val) {
    setVar(this, var, val);
  }

  public void logVars(PrintStream ps) {
    for (ConfVars one : ConfVars.values()) {
      ps.println(one.varname + "=" + ((get(one.varname) != null) ? get(one.varname) : ""));
    }
  }

  public InetSocketAddress getSocketAddrVar(ConfVars var) {
    final String address = getVar(var);
    return NetUtils.createSocketAddr(address);
  }

  /////////////////////////////////////////////////////////////////////////////
  // Tajo System Specific Methods
  /////////////////////////////////////////////////////////////////////////////

  public static Path getTajoRootDir(TajoConf conf) {
    String rootPath = conf.getVar(ConfVars.ROOT_DIR);
    Preconditions.checkNotNull(rootPath,
          ConfVars.ROOT_DIR.varname + " must be set before a Tajo Cluster starts up");
    return new Path(rootPath);
  }

  public static Path getWarehouseDir(TajoConf conf) {
    String warehousePath = conf.getVar(ConfVars.WAREHOUSE_DIR);
    if (warehousePath == null || warehousePath.equals("")) {
      Path rootDir = getTajoRootDir(conf);
      warehousePath = new Path(rootDir, TajoConstants.WAREHOUSE_DIR_NAME).toUri().toString();
      conf.setVar(ConfVars.WAREHOUSE_DIR, warehousePath);
      return new Path(warehousePath);
    } else {
      return new Path(warehousePath);
    }
  }

  public static Path getSystemDir(TajoConf conf) {
    Path rootPath = getTajoRootDir(conf);
    return new Path(rootPath, TajoConstants.SYSTEM_DIR_NAME);
  }

  public static Path getSystemResourceDir(TajoConf conf) {
    return new Path(getSystemDir(conf), TajoConstants.SYSTEM_RESOURCE_DIR_NAME);
  }

  private static boolean hasScheme(String path) {
    return path.indexOf("file:/") == 0 || path.indexOf("hdfs:/") == 0;
  }

  public static Path getStagingDir(TajoConf conf) {
    String stagingDirString = conf.getVar(ConfVars.STAGING_ROOT_DIR);
    if (!hasScheme(stagingDirString)) {
      Path warehousePath = getWarehouseDir(conf);
      FileSystem fs;
      try {
        fs = warehousePath.getFileSystem(conf);
      } catch (Throwable e) {
        throw null;
      }
      Path path = new Path(fs.getUri().toString(), stagingDirString);
      conf.setVar(ConfVars.STAGING_ROOT_DIR, path.toString());
      return path;
    }
    return new Path(stagingDirString);
  }

  public static Path getSystemConfPath(TajoConf conf) {
    String systemConfPathStr = conf.getVar(ConfVars.SYSTEM_CONF_PATH);
    if (systemConfPathStr == null || systemConfPathStr.equals("")) {
      Path systemResourcePath = getSystemResourceDir(conf);
      Path systemConfPath = new Path(systemResourcePath, TajoConstants.SYSTEM_CONF_FILENAME);
      conf.setVar(ConfVars.SYSTEM_CONF_PATH, systemConfPath.toString());
      return systemConfPath;
    } else {
      return new Path(systemConfPathStr);
    }
  }
}
