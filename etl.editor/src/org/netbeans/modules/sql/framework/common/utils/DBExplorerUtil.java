/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.sql.framework.common.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.mashup.db.ui.AxionDBConfiguration;
import com.sun.sql.framework.exception.DBSQLException;
import com.sun.sql.framework.utils.ScEncrypt;
import com.sun.sql.framework.jdbc.DBConnectionFactory;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.engine.Databases;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author radval
 * 
 */
public class DBExplorerUtil {

    public static final String AXION_DRIVER = "org.axiondb.jdbc.AxionDriver";
    private static final String LOG_CATEGORY = DBExplorerUtil.class.getName();
    private static List localConnectionList = new ArrayList();
    private static transient final Logger mLogger = Logger.getLogger(DBExplorerUtil.class.getName());
    //private static transient final Localizer mLoc = Localizer.get();

    public static String adjustDatabaseURL(String url) {
        if (url.indexOf(AXION_URL_PREFIX) != -1) {
            String[] urlParts = parseConnUrl(url);
            String relativePath = "\\nbproject\\private\\databases\\";
            if (urlParts[1].startsWith(ETLEditorSupport.PRJ_PATH)) {
                urlParts[0] =  urlParts[0].toUpperCase();
                String adjustedName = urlParts[0].contains(ETLEditorSupport.PRJ_NAME.toUpperCase()) ? urlParts[0] : ETLEditorSupport.PRJ_NAME.toUpperCase() + "_" + urlParts[0];
                url = AXION_URL_PREFIX + adjustedName + ":" + urlParts[1];
            }else if (urlParts[1].startsWith(relativePath)) {
                url = AXION_URL_PREFIX + urlParts[0] + ":" + ETLEditorSupport.PRJ_PATH + urlParts[1];
            }
        }

        return url;
    }

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

        if (driverName.equals(AXION_DRIVER)) {
            drv = registerAxionDriverInstance();
        } else {
            drv = registerDriverInstance(driverName);
        }

        return drv;
    }

    /** Creates a new instance of DBExplorerUtil */
    public DBExplorerUtil() {
    }

    public static Connection createConnection(Properties connProps) throws DBSQLException {
        String driver = connProps.getProperty(DBConnectionFactory.PROP_DRIVERCLASS);
        String username = connProps.getProperty(DBConnectionFactory.PROP_USERNAME);
        String password = connProps.getProperty(DBConnectionFactory.PROP_PASSWORD);
        String url = connProps.getProperty(DBConnectionFactory.PROP_URL);
        if (!(url.contains(AXION_URL_PREFIX))) {
            if (StringUtil.isNullString(username) || StringUtil.isNullString(password)) {
                JOptionPane.showMessageDialog(new JFrame(), "UserName/Password is empty.Please fill in the credentials ", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return createConnection(driver, url, username, password);
    }
    public static final String AXION_URL_PREFIX = "jdbc:axiondb:";

    public static String[] parseConnUrl(String url) {
        String name, workDir;
        String prefixStripped = url.substring(AXION_URL_PREFIX.length());
        int colon = prefixStripped.indexOf(":");
        if (colon == -1 || (prefixStripped.length() - 1 == colon)) {
            name = prefixStripped;
            workDir = name;
        } else {
            name = prefixStripped.substring(0, colon);
            workDir = unifyPath(prefixStripped.substring(colon + 1));
        }

        String[] connStr = new String[2];
        connStr[0] = name;
        connStr[1] = workDir;
        return connStr;
    }

    public static Connection createConnection(DatabaseConnection dbConn) throws DBSQLException {
        Connection conn = null;
        if (dbConn != null) {
            conn = createConnection(dbConn.getDriverClass(), dbConn.getDatabaseURL(), dbConn.getUser(), dbConn.getPassword());
        }
        return conn;
    }


    public static Connection createConnection(String driverName, String url, String username, String password) throws DBSQLException {
        // Try to get the connection directly. Dont go through DB Explorer.
        // It may pop up a window asking for password.
        JDBCDriver drv = null;
        Connection conn = null;
        try {
           
                url = adjustDatabaseURL(url);
            drv = registerDriver(driverName);

            conn = getConnection(drv, driverName, url, username, password);
            if (conn == null) { // get from db explorer

                DatabaseConnection dbConn = createDatabaseConnection(driverName, url, username, password);
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
                    throw new DBSQLException("Connection could not be established. Please check the Database Server.");
                }
            } else {
                synchronized (localConnectionList) {
                    localConnectionList.add(conn);
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
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

    public static DatabaseConnection createDatabaseConnection(String driverName, String url, String username, String password) throws DBSQLException {
        DatabaseConnection dbconn = null;
        JDBCDriver drv = null;
        String schema = null;
        try {
            
                url = adjustDatabaseURL(url);
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
                if (url.startsWith(AXION_URL_PREFIX)) {
                    schema = "";
                } else {
                    schema = username.toUpperCase();
                }
                dbconn = DatabaseConnection.create(drv, url, username, schema, password, true);

                if (url.indexOf("InstanceDB") == -1 && url.indexOf("MonitorDB") == -1 && url.indexOf(ETLEditorSupport.PRJ_PATH) == -1) {
                    ConnectionManager.getDefault().addConnection(dbconn);
                }
            }

            return dbconn;

        } catch (Exception e) {
            throw new DBSQLException("Connection could not be established.", e);
        }
    }

    /**
     * Registers an instance of Driver associated with the given driver class name. Does
     * nothing if an instance has already been registered with the JDBC DriverManager.
     *
     * @param driverName class name of driver to be created
     * @return Driver instance associated with <code>driverName</code>
     * @throws Exception if error occurs while creating or looking up the desired driver
     *         instance
     */
    public static JDBCDriver registerDriverInstance(final String driverName) throws Exception {
        JDBCDriver driver = null;
        JDBCDriver[] drivers;
        try {
            drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        } catch (Exception ex) {
            throw new Exception("Invalid driver name specified.");
        }
        if (driverName.equals(AXION_DRIVER)) {
            driver = registerAxionDriverInstance();
        } else {
            if (drivers.length == 0) {
                throw new Exception("Specified JDBC Driver not found in DB Explorer.");
            } else {
                driver = drivers[0];
            }
        }
        return driver;
    }

    private static JDBCDriver registerAxionDriverInstance() throws Exception {
        JDBCDriver driver = null;
        String driverName = AXION_DRIVER;
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        if (drivers.length == 0) {
            // if axion db driver not available in db explorer, add it.
            URL[] url = new URL[1];
            String workDir = getDefaultAxionDriverLoc();
            if (StringUtil.isNullString(workDir)) {
                throw new Exception("Axion Driver could not be located");
            }
            url[0] = new URL("file:/" + workDir);
            driver = JDBCDriver.create(driverName, "Mashup DB", driverName, url);
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

    /**
     * Manually load the driver class and get the connection for the specified properties.
     *
     * @return connection for the corresponding db url and properties.
     */
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
        } catch (SQLException ex) {
            try {
                // may be some class forgot to decrypt the password. Check if this one works
                Properties prop = new Properties();
                prop.setProperty("user", username);
                // if its not an encrypted password, decrypt operation will fail. Caught as general Exception
                password = ScEncrypt.decrypt(username, password);
                prop.setProperty("password", password);
                conn = newDriverClass.connect(url, prop);
            } catch (SQLException e) {
                //mLogger.infoNoloc(mLoc.t("EDIT098: Unable to get the specified connection directly.{0}", LOG_CATEGORY));
                mLogger.infoNoloc("Unable to get the specified connection directly.{0}");
            } catch (Exception numex) {
                //mLogger.infoNoloc(mLoc.t("EDIT098: Unable to get the specified connection directly.{0}", LOG_CATEGORY));
                mLogger.infoNoloc("Unable to get the specified connection directly.{0}");
            }
        } catch (Exception ex) {
            //mLogger.infoNoloc(mLoc.t("EDIT100: Unable to find the driver class in the specified jar file{0}", LOG_CATEGORY));
            mLogger.infoNoloc("Unable to find the driver class in the specified jar file{0}");
        }
        return conn;
    }

    /*
     * Returns the default location of axiondb.jar along with the dependencies.
     *
     */
    public static String getDefaultAxionWorkingFolder() {
        Properties prop = loadAxionProperties();
        return prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
    }

    public static String getDefaultAxionDriverLoc() {
        Properties prop = loadAxionProperties();
        return prop.getProperty(AxionDBConfiguration.PROP_DRIVER_LOC);
    }

    public static void recreateMissingFlatfileConnectionInDBExplorer() {
        // add flatfile databases to db explorer.
        String workDir = getDefaultAxionWorkingFolder();
        File f = new File(workDir);
        File[] db = null;
        if (f.exists()) {
            db = f.listFiles();
            for (int i = 0; i < db.length; i++) {
                String ver = null;
                try {
                    ver = db[i].getCanonicalPath() + "\\" + db[i].getName().toUpperCase() + ".VER";
                    File version = new File(ver);
                    if (version.exists()) {
                        String url = AXION_URL_PREFIX + db[i].getName() + ":" + workDir + db[i].getName();
                        url = unifyPath(url);
                        DatabaseConnection con = ConnectionManager.getDefault().getConnection(url);
                        if (con == null) {
                            DBExplorerUtil.createDatabaseConnection(AXION_DRIVER, url, "sa", "sa");
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }

    public static String unifyPath(String workDir) {
        return workDir.replace('/', '\\');
    }

    public static List<DatabaseConnection> getDatabasesForCurrentProject() {
        List<DatabaseConnection> dbConns = new ArrayList<DatabaseConnection>();
        String workDir = ETLEditorSupport.PRJ_PATH + "\\nbproject\\private\\databases\\";
        File f = new File(workDir);
        File[] db = null;
        if (f.exists()) {
            db = f.listFiles();
            for (int i = 0; i < db.length; i++) {
                String ver = null;
                try {
                    ver = db[i].getCanonicalPath() + "\\" + (ETLEditorSupport.PRJ_NAME + "_" + db[i].getName()).toUpperCase() + ".VER";
                    File version = new File(ver);
                    if (version.exists()) {
                        String url = DBExplorerUtil.AXION_URL_PREFIX + db[i].getName() + ":" + workDir + db[i].getName();
                        url = unifyPath(url);
                        DatabaseConnection dbConn = DBExplorerUtil.createDatabaseConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa");
                        dbConns.add(dbConn);
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return dbConns;
    }

    public static String getDisplayName(DatabaseConnection dbConn) {
        if (dbConn.toString() == null) {
            return "<None>";
        }

        String connName = dbConn.getName();
        if (connName.startsWith(DBExplorerUtil.AXION_URL_PREFIX)) {
            String[] parts = DBExplorerUtil.parseConnUrl(connName);
            connName = parts[0] + " [MASHUP DB]";
        } else if (connName.startsWith("jdbc:derby://")) {
            int start = "jdbc:derby://".length();
            String host = connName.substring(start);
            host = host.substring(0, host.indexOf(":"));
            String dbName = connName.substring(start + host.length());
            start = dbName.indexOf("/") + 1;
            dbName = dbName.substring(start, dbName.indexOf("["));
            connName = dbName.trim() + "@" + host + " [DERBY]";
        }
        return connName;
    }

    public static Database getAxionDBFromURL(String url) throws AxionException {
        int initialDBIndex = url.indexOf("axiondb") + 8;
        int endDBIndex = url.indexOf(":", initialDBIndex);
        String dbName = url.substring(initialDBIndex, endDBIndex);
        String dbLoc = url.substring(endDBIndex + 1);
        return (Databases.getOrCreateDatabase(dbName, new File(dbLoc)));
    }
}
