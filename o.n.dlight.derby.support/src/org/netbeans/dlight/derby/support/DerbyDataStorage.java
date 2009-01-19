/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.dlight.derby.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.dlight.core.stack.model.FunctionCall;
import org.netbeans.dlight.core.stack.model.FunctionMetric;
import org.netbeans.dlight.core.stack.storage.SQLStackStorage;
import org.netbeans.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.storage.support.SQLDataStorage;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;

/**
 *
 * @author mt154047
 */
public class DerbyDataStorage extends SQLDataStorage implements StackDataStorage {

  private static final String DERBY_DATA_STORAGE_TYPE = "db:sql:derby";
  private static final String SQL_QUERY_DELIMETER = "";
  static private int dbIndex = 1;
  private static final Logger logger = DLightLogger.getLogger(DerbyDataStorage.class);
  private static boolean driverLoaded = false;
  private SQLStackStorage stackStorage;
  private final List<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();;


  static {
    Util.deleteLocalDirectory(new File("/tmp/derby_dlight"));

  }

  /**
   * Empty constructor, used by Lookup
   */
  public DerbyDataStorage() {
    initStorageTypes();
  
  }

  private void initStorageTypes(){
    supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(DERBY_DATA_STORAGE_TYPE));
    supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    supportedStorageTypes.addAll(super.getStorageTypes());

  }

  private DerbyDataStorage(String url) {
    super(url);
    try {
      initStorageTypes();
      initTables();
      stackStorage = new SQLStackStorage(this);

    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  @Override
  protected String classToType(Class clazz) {
    if (clazz == Integer.class){
      return "integer";
    }
    return super.classToType(clazz);
  }



  private void initTables() throws SQLException, IOException {
    InputStream is = DerbyDataStorage.class.getClassLoader().getResourceAsStream("org/netbeans/dlight/derby/support/schema.sql");
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    executeSQL(reader);
    reader.close();
  }

  private void executeSQL(BufferedReader reader) throws SQLException, IOException {
    String line;
    StringBuilder buf = new StringBuilder();
    Statement s = connection.createStatement();
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("-- ")) {
        continue;
      }
      buf.append(line);
      if (line.endsWith(";")) {
        String sql = buf.toString();
        buf.setLength(0);
        s.execute(sql.substring(0, sql.length() - 1));
      }
    }
    s.close();
  }

  @Override
  public String getID() {
    return "DerbyDataStorage";
  }

  @Override
  protected void connect(String dburl) {
    try {
      connection = DriverManager.getConnection(dburl);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
  //super.connect(dburl);
  }

  @Override
  public DataStorage newInstance() {
    if (!driverLoaded) {
      try {
//        Class driver = Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        //Class driver =  Class.forName("org.apache.derby.jdbc.ClientDriver");
        /// Decide on the db system directory: <userhome>/.addressbook/
        String userHomeDir = System.getProperty("user.home", ".");
        //  String systemDir = userHomeDir + "/.dlight";
        String systemDir = "/tmp/derby_dlight";

        // Set the db system directory.
        System.setProperty("derby.system.home", systemDir);
        Class driver = Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        driverLoaded = true;
        logger.info("Driver for Derby(JavaDB) (" + driver.getName() + ") Loaded ");
      } catch (Exception ex) {
        logger.log(Level.SEVERE, null, ex);
      }
    }

    return new DerbyDataStorage("jdbc:derby:DerbyDlight" + (dbIndex++) + ";create=true;user=dbuser;password=dbuserpswd");
  }

  @Override
  public List<DataStorageType> getStorageTypes() {
    return supportedStorageTypes;
  }

  @Override
  public boolean createTablesImpl(List<? extends DataTableMetadata> tableMetadatas) {
    for (DataTableMetadata tdmd : tableMetadatas) {
      if (tdmd.getName().equals(STACK_METADATA_VIEW_NAME)) {
        if (!tables.containsKey(STACK_METADATA_VIEW_NAME)) {
          tables.put(STACK_METADATA_VIEW_NAME, tdmd);
        }
        continue;
      }
      if (!createTable(tdmd)) {
        return false;
      }
    }

    return true;
  }

  public void putStack(List<CharSequence> stack, int cpuId, int threadId, long sampleTimestamp, long sampleDuration) {
    stackStorage.putStack(stack, cpuId, threadId, sampleTimestamp, sampleDuration);
  }

  public int getStackId(List<CharSequence> stack, int cpuId, int threadId, long timestamp) {
    return stackStorage.getStackId(stack, cpuId, threadId, timestamp);
  }

  public List<Long> getPeriodicStacks(long startTime, long endTime, long interval) {
    try {
      return stackStorage.getPeriodicStacks(startTime, endTime, interval);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public List<FunctionMetric> getMetricsList() {
    return stackStorage.getMetricsList();
  }

  public List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate) {
    try {
      return stackStorage.getCallers(path, aggregate);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return new ArrayList<FunctionCall>();
    }
  }

  public List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate) {
    try {
      return stackStorage.getCallees(path, aggregate);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return new ArrayList<FunctionCall>();
    }
  }

  public List<FunctionCall> getHotSpotFunctions(FunctionMetric metric, int limit) {
    try {
      return stackStorage.getHotSpotFunctions(metric, limit);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return new ArrayList<FunctionCall>();
    }
  }

  @Override
  protected String getSQLQueriesDelimeter() {
    return SQL_QUERY_DELIMETER;
  }
}
