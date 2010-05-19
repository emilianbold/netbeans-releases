/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.model;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dm.virtual.db.ui.wizard.CommonUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Ahimanikya Satapathy
 * 
 */
public class DBExplorerUtil {

    private static final String LOG_CATEGORY = DBExplorerUtil.class.getName();
    private static List localConnectionList = new ArrayList();
    private static transient final Logger mLogger = Logger.getLogger(DBExplorerUtil.class.getName());

    private static Properties loadAxionProperties() {
        File conf = AxionDBConfiguration.getConfigFile();
        Properties prop = new Properties();
        try {
            FileInputStream in = new FileInputStream(conf);
            prop.load(in);
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }
        return prop;
    }

    private static JDBCDriver registerDriver(String driverName) throws Exception {
        JDBCDriver drv;

        if (driverName.equals(VirtualDBConnectionDefinition.AXION_DRIVER)) {
            drv = registerAxionDriverInstance();
        } else {
            drv = registerDriverInstance(driverName);
        }

        return drv;
    }

    public DBExplorerUtil() {
    }

    public static String[] parseConnUrl(String url) {
        String name, workDir;
        String prefixStripped = url.substring(VirtualDBConnectionFactory.VIRTUAL_DB_URL_PREFIX.length());
        int colon = prefixStripped.indexOf(":");
        if (colon == -1 || (prefixStripped.length() - 1 == colon)) {
            name = prefixStripped;
            workDir = name;
        } else {
            name = prefixStripped.substring(0, colon);
            workDir = prefixStripped.substring(colon + 1);
        }

        String[] connStr = new String[2];
        connStr[0] = name;
        connStr[1] = workDir;
        return connStr;
    }
    
    public static Connection createConnection(DatabaseConnection dbConn) throws SQLException {
        Connection conn = null;
        if (dbConn != null) {
            conn = createConnection(dbConn.getDriverClass(), dbConn.getDatabaseURL(), dbConn.getUser(), dbConn.getPassword());
        }
        return conn;
    }
    
    public static Connection createConnection(Properties connProps) throws SQLException {
        String driver = connProps.getProperty(VirtualDBConnectionFactory.PROP_DRIVERCLASS);
        String username = connProps.getProperty(VirtualDBConnectionFactory.PROP_USERNAME);
        String password = connProps.getProperty(VirtualDBConnectionFactory.PROP_PASSWORD);
        String url = connProps.getProperty(VirtualDBConnectionFactory.PROP_URL);
        if (!(url.contains(VirtualDBConnectionFactory.VIRTUAL_DB_URL_PREFIX))) {
            if (VirtualDBUtil.isNullString(username) || VirtualDBUtil.isNullString(password)) {
                JOptionPane.showMessageDialog(new JFrame(), "UserName/Password is empty.Please fill in the credentials ", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return DBExplorerUtil.createConnection(driver, url, username, password, false);
    }
    
    public static Connection createConnection(String driverName, String url, String username, String password) throws SQLException {
        return createConnection(driverName, url, username, password, true);
    }

    public static Connection createConnection(String driverName, String url, String username, String password, boolean addToExplorer) throws SQLException {
        // Try to get the connection directly. Dont go through DB Explorer.
        // It may pop up a window asking for password.
        JDBCDriver drv = null;
        Connection conn = null;
        try {
            drv = registerDriver(driverName);

            conn = getConnection(drv, driverName, url, username, password);
            if (conn == null) { // get from db explorer

                DatabaseConnection dbConn = createDatabaseConnection(driverName, url, username, password, addToExplorer);
                try {
                    if (dbConn != null) {
                        conn = dbConn.getJDBCConnection();
                        if (conn == null) { // make a final try

                            ConnectionManager.getDefault().showConnectionDialog(dbConn);
                            Thread.sleep(5000);
                            conn = dbConn.getJDBCConnection();
                        }
                    }
                } catch (Exception ex) {
                    // ignore
                }
                // If connection is still nul throw exception
                if (conn == null) {
                    throw new SQLException(NbBundle.getMessage(DBExplorerUtil.class, "MSG_DBserverError"));
                }
            } else {
                synchronized (localConnectionList) {
                    localConnectionList.add(conn);
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(DBExplorerUtil.class.getName()).log(Level.SEVERE, "Specified JDBC Driver not found in DB Explorer." + ex);
        }
        return conn;
    }

    public static void closeIfLocalConnection(Connection conn) {
        if (localConnectionList.contains(conn)) {
            try {
                localConnectionList.remove(conn);
                conn.close();
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static DatabaseConnection createDatabaseConnection(String driverName, String url, String username, String password) throws VirtualDBException {
        return createDatabaseConnection(driverName, url, username, password, true);
    }

    public static DatabaseConnection createDatabaseConnection(String driverName, String url, String username, String password, boolean addToDBExplorer) throws VirtualDBException {
        DatabaseConnection dbconn = null;
        JDBCDriver drv = null;
        String schema = null;
        try {
            drv = registerDriver(driverName);

            // check if connection exists in DB Explorer. Else add the connection to DB Explorer.
            DatabaseConnection[] dbconns = ConnectionManager.getDefault().getConnections();
            for (int i = 0; i < dbconns.length; i++) {
                if (dbconns[i].getDriverClass().equals(driverName) && dbconns[i].getDatabaseURL().equals(url) && dbconns[i].getUser().equals(username)) {
                    dbconn = dbconns[i];
                    break;
                }
            }

            // dont add instance db and monitor db and local dbs connections to db explorer.
            if (dbconn == null) {
                if (url.startsWith(VirtualDBConnectionFactory.VIRTUAL_DB_URL_PREFIX)) {
                    schema = "";
                } else {
                    schema = username.toUpperCase();
                }
                dbconn = DatabaseConnection.create(drv, url, username, schema, password, true);


                if (url.indexOf("InstanceDB") == -1 && url.indexOf("MonitorDB") == -1 && addToDBExplorer && 
                        !(CommonUtils.IS_PROJECT_CALL)) { //Otherwise it adds to the services tab when we add tables from the project.
                    ConnectionManager.getDefault().addConnection(dbconn);
                }
            }

            return dbconn;

        } catch (Exception e) {
            throw new VirtualDBException(NbBundle.getMessage(DBExplorerUtil.class, "MSG_ConncetionError") + e.getMessage());
        }
    }

    private static JDBCDriver registerDriverInstance(final String driverName) throws Exception {
        JDBCDriver driver = null;
        JDBCDriver[] drivers;
        try {
            drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        } catch (Exception ex) {
            throw new Exception(NbBundle.getMessage(DBExplorerUtil.class, "MSG_InvalidDriver"));
        }
        if (driverName.equals(VirtualDBConnectionDefinition.AXION_DRIVER)) {
            driver = registerAxionDriverInstance();
        } else {
            if (drivers.length == 0) {
                throw new Exception(NbBundle.getMessage(DBExplorerUtil.class, "MSG_JDBC_notFound"));
            } else {
                driver = drivers[0];
            }
        }
        return driver;
    }

    private static JDBCDriver registerAxionDriverInstance() throws Exception {
        JDBCDriver driver = null;
        String driverName = VirtualDBConnectionDefinition.AXION_DRIVER;
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        if (drivers.length == 0) {
            // if axion db driver not available in db explorer, add it.
            URL[] url = new URL[1];
            String workDir = getDefaultAxionDriverLoc();
            if (workDir == null || workDir.trim().equals("")) {
                throw new Exception(NbBundle.getMessage(DBExplorerUtil.class, "MSG_Axion_notFound"));
            }
            url[0] = new File(workDir).toURL();
            driver = JDBCDriver.create(driverName, NbBundle.getMessage(DBExplorerUtil.class, "LBL_VirtualDB"), driverName, url);
            JDBCDriverManager.getDefault().addDriver(driver);
        }
        if (driver == null) {
            for (int i = 0; i < drivers.length; i++) {
                if (drivers[i].getClassName().equals(driverName)) {
                    driver = drivers[i];
                    break;
                }
            }
        }
        return driver;
    }

    private static Connection getConnection(JDBCDriver drv, String driverName, String url, String username, String password) {
        Connection conn = null;
        Driver newDriverClass = null;
        try {
            // get the driver jar files and load them manually
            URL[] urls = drv.getURLs();
            ClassLoader cl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            newDriverClass = (Driver) cl.loadClass(driverName).newInstance();
            Properties prop = new Properties();
            prop.setProperty("user", username);
            prop.setProperty("password", password);
            conn = newDriverClass.connect(url, prop);
        } catch (Exception ex) {
            mLogger.log(Level.INFO, NbBundle.getMessage(DBExplorerUtil.class, "LOG_Driver_notFound", LOG_CATEGORY));
        }
        return conn;
    }

    private static String getDefaultAxionWorkingFolder() {
        Properties prop = loadAxionProperties();
        return prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
    }

    private static String getDefaultAxionDriverLoc() {
        Properties prop = loadAxionProperties();
        return prop.getProperty(AxionDBConfiguration.PROP_DRIVER_LOC);
    }
    private static String databaseName;

    private static boolean checkFileNameFilter(String path) {
        File[] files = new File(path).listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.toUpperCase().trim().endsWith(".VER")) {  // NOI18N
                    databaseName = name.replace(".VER", "");
                    return true;
                } else {
                    return false;
                }
            }
        });
        return files.length == 0 ? false : true;
    }

    public static List<DatabaseConnection> getDatabasesForCurrentProject() {
        List<DatabaseConnection> dbConns = new ArrayList<DatabaseConnection>();
        String workDir = CommonUtils.PRJ_PATH + File.separator + "nbproject" + File.separator +
                "private" + File.separator + "databases" + File.separator;
        File f = new File(workDir);
        File[] db = null;
        if (f.exists()) {
            db = f.listFiles();
            for (int i = 0; i < db.length; i++) {                
                try {
                    boolean fileNameFilter = checkFileNameFilter(db[i].getCanonicalPath());
                    if (fileNameFilter) {                        
                        String url = VirtualDBConnectionFactory.VIRTUAL_DB_URL_PREFIX + databaseName + ":" + workDir + db[i].getName(); // NOI18N
                        DatabaseConnection dbConn = DBExplorerUtil.createDatabaseConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa"); // NOI18N
                        dbConns.add(dbConn);
                    }
                } catch (Exception ex) {                    
                }
            }
        }
        return dbConns;
    }

    public static void recreateMissingVirtualDBConnectionInDBExplorer() {
        // add virtual databases to db explorer.
        String workDir = getDefaultAxionWorkingFolder();
        File f = new File(workDir);
        File[] db = null;
        if (f.exists()) {
            db = f.listFiles();
            for (int i = 0; i < db.length; i++) {
                try {

                    boolean fileNameFilter = checkFileNameFilter(db[i].getCanonicalPath());
                    if (fileNameFilter) {
                        String url = VirtualDBConnectionFactory.VIRTUAL_DB_URL_PREFIX + databaseName + ":" + workDir + db[i].getName();
                        DatabaseConnection con = ConnectionManager.getDefault().getConnection(url);
                        if (con == null) {
                            DBExplorerUtil.createDatabaseConnection(VirtualDBConnectionDefinition.AXION_DRIVER, url, "sa", "sa", true); // NOI18N
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }
}
