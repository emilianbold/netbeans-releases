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

package org.netbeans.modules.db.mysql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author David
 */
public class DatabaseUtils {
    // MySQL's SQL State for a communication error.  
    public static final String SQLSTATE_COMM_ERROR = "08S01";
    // The SQL State prefix (class) used for client-side exceptions
    private static final String SQLSTATE_CLIENT_PREFIX = "20";
    
    private static final Logger LOGGER = 
            Logger.getLogger(DatabaseUtils.class.getName());
    
    // A cache of the driver class so we don't have to load it each time
    private static Driver driver;
    
    /**
     * An enumeration indicating the status after attempting to connect to the
     * server
     * 
     * @author David Van Couvering
     */
    public enum ConnectStatus {
        /** The server was not detected at the given host/port */
        NO_SERVER, 

        /** We could establish a connection, but authentication failed with
         * the given user and password
         */
        SERVER_RUNNING, 

        /** We were able to connect and authenticate */
        CONNECT_SUCCEEDED

    }

    /**
     * Connect to the MySQL server on a task thread, showing a progress bar
     * and displaying a dialog if an error occurred
     * 
     * @param instance the server instance to connect with
     */
    public static void connectToServerAsync(final ServerInstance instance) {
         connectToServerAsync(instance, false);
    }
     
    /**
     * Connect to the server asynchronously, with the option not to display
     * a dialog but just write to the log if an error occurs
     * @param instance the instance to connect to
     * @param quiet true if you don't want this to happen without any dialogs
     *   being displayed in case of error or to get more information.
     */
    static void connectToServerAsync(final ServerInstance instance, 
            final boolean quiet) {
        
         if ( instance == null ) {
                 throw new NullPointerException();
         }
         
         if ( isEmpty(instance.getHost()) || isEmpty(instance.getUser()) ||
                 (isEmpty(instance.getPassword()) && !instance.isSavePassword()) ) {
             if ( ! quiet ) {
                 Utils.displayErrorMessage("MSG_UnableToConnect");
             }
             return;
         }
         
         final ProgressHandle progress = ProgressHandleFactory.createHandle(
                 NbBundle.getMessage(DatabaseUtils.class, "MSG_ConnectingToServer"));
         progress.start();
         progress.switchToIndeterminate();
         
         RequestProcessor.getDefault().post(new Runnable() {
             public void run() {
                 try { 
                     instance.connect();
                 } catch ( DatabaseException dbe ) {
                     LOGGER.log(Level.INFO, null, dbe);
                     if ( ! quiet ) {
                         Utils.displayError(NbBundle.getMessage(DatabaseUtils.class,
                                     "MSG_UnableToConnect"), 
                                 dbe);
                     }
                 } finally {
                     progress.finish();
                 }
             }
         });
        
    }
     
    public static boolean isEmpty(String val) {
        return (val == null || val.length() == 0);
    }
     
    public static JDBCDriver getJDBCDriver() {
        JDBCDriver[]  drivers = JDBCDriverManager.getDefault().
                getDrivers(MySQLOptions.getDriverClass());

        if ( drivers.length == 0 ) {
            return null;
        }

        return drivers[0];
    }
     
    /**
     * Load and return the JDBC Driver for MySQL.  This method
     * gets the search path for the MySQL driver, creates a classloader
     * that uses this search path, and then instantiates the driver
     * class from this classloader
     * 
     * @return an instance of the MySQL driver.
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *      If an error occured while trying to load the driver
     */
    public static Driver getDriver() throws DatabaseException {
        if ( driver != null ) {
            return driver;
        }
        JDBCDriver jdbcDriver = getJDBCDriver();
        
        if ( jdbcDriver == null ) {
            throw new DatabaseException(
                    NbBundle.getMessage(DatabaseUtils.class, 
                    "MSG_JDBCDriverNotRegistered"));
        }
                
        try {
            ClassLoader driverLoader = new DriverClassLoader(jdbcDriver);
            driver = (Driver)Class.forName(jdbcDriver.getClassName(), 
                        true, driverLoader).newInstance();
        } catch ( Exception e ) {
            DatabaseException dbe = new DatabaseException(
                    NbBundle.getMessage(DatabaseUtils.class,
                        "MSG_UnableToLoadJDBCDriver") + e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
        
        return driver;
    }
    
    /**
     * Get a JDBC connection from a DatabaseConnection, bringing up a dialog
     * to connect if necessary
     * 
     * @param dbconn
     * @return the resulting JDBC connection
     * @throws java.sql.SQLException if there was an error connecting
     */
    public static Connection getConnection(DatabaseConnection dbconn) throws SQLException {
        Connection conn = dbconn.getJDBCConnection();
        
        if ( conn == null || conn.isClosed()) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
            conn = dbconn.getJDBCConnection();
        }    
        
        return conn;
    }
    
    /** 
     * Open a JDBC connection directly from the MySQL driver
     * 
     * @ return a live JDBC connection
     * @throws SQLException if there was a problem connecting
     * @throws DatabaseException if there were issues getting the MySQL driver
     */
    public static Connection connect(String url, String user, String password)
            throws DatabaseException {
        Driver theDriver = getDriver();
        Properties props = new Properties();
        props.put("user", user == null ? "" : user);                
        props.put("password", password == null ? "" : password);
        
        props.put("connectTimeout", 
                MySQLOptions.getDefault().getConnectTimeout()); 
        
        try {
            return theDriver.connect(url, props);
        } catch ( SQLException sqle ) {
            if ( DatabaseUtils.SQLSTATE_COMM_ERROR.equals(sqle.getSQLState())) {
                // On a communications failure (e.g. the server's not running)
                // the message horribly includes the entire stack trace of the
                // exception chain.  We don't want to display this to our users,
                // so let's provide our own message...
                //
                // If other MySQL exceptions exhibit this behavior we'll have to
                // address this in a more general way...
                String msg = NbBundle.getMessage(DatabaseUtils.class,
                        "ERR_MySQLCommunicationFailure");

                DatabaseException dbe = new DatabaseException(msg);
                dbe.initCause(sqle);
                throw dbe;
            } else {
                throw new DatabaseException(sqle);
            }
        }
    }

    /**
     * Handles all that annoying try/catch stuff around closing a connection
     * 
     * @param conn the connection to close
     */
    static void closeConnection(Connection conn) {
        try {
            if ( conn != null ) {
                conn.close();
            } 
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, null, e);
        }
    }
        
    /**
     * Find a registered database connection.
     * 
     * @param the database URL to use
     * @param user the user name for the connection
     * @param password the password for the connection; can be null
     * 
     * @return the database connection
     */
    public static DatabaseConnection findDatabaseConnection(String url, String user) {
        List<DatabaseConnection> conns =
                findDatabaseConnections(url);
        
        for ( DatabaseConnection conn : conns ) {
            if ( conn.getUser().equals(user)) {
                return conn;
            }
        }
        
        return null;
    }
    
    /** 
     * Find all registered database connections that match the given URL
     * (there could be multiple ones, for different users
     * 
     * @param host
     * @param port
     * @return
     */
    public static List<DatabaseConnection> findDatabaseConnections(String url) {
        ArrayList<DatabaseConnection> result =
                new ArrayList<DatabaseConnection>();
                
        DatabaseConnection[] connections = 
            ConnectionManager.getDefault().getConnections();

        for ( DatabaseConnection conn : connections ) {
            // If there's already a connection registered, we're done
            if ( conn.getDriverClass().equals(MySQLOptions.getDriverClass()) &&
                 conn.getDatabaseURL().equals(url) ) {
                result.add(conn);
            }
        }
        
        return result;
    }
    
    public static String getURL(String host, String port) {
        return getURL(host, port, null);
    }
    
    public static String getURL(String host, String port, String database) {
        // Format is jdbc:mysql://<HOST>:<PORT>
        // No database is specified for an admin connection.
        StringBuffer url = new StringBuffer("jdbc:mysql://"); // NOI18N
        url.append( host == null || host.equals("") ? "localhost" : host); // NO18N
        if ( port != null && (! port.equals("")) ) {
            url.append(":" + port);
        }
        if ( database != null && (! database.equals(""))) {
            url.append("/" + database); // NOI18N
        }
        
        return url.toString();
    }

    static boolean ensureConnected(DatabaseConnection dbconn) {
        try {
            Connection conn = dbconn.getJDBCConnection();
            if ( conn == null || conn.isClosed() ) {
                ConnectionManager.getDefault().showConnectionDialog(dbconn);
            }

            conn = dbconn.getJDBCConnection();
            
            if ( conn == null || conn.isClosed() ) {
                return false;
            }

            return true;
        } catch ( SQLException e ) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }
    
    public static boolean isServerException(SQLException e) {
        //
        // See http://dev.mysql.com/doc/refman/5.0/en/error-handling.html
        // for info on MySQL errors and sql states.
        //
        String sqlstate = e.getSQLState();
        SQLException nexte = e.getNextException();

        if ( SQLSTATE_COMM_ERROR.equals(sqlstate)) { // Communications exception
            return false;
        }

        if ( sqlstate.startsWith(SQLSTATE_CLIENT_PREFIX))  {
            // An exception whose SQL state starts with this prefix is
            // client side-only.  So any SQL state that *doesn't*
            // start with this prefix must have come from a live server
            return false;
        }

        if ( nexte != null ) {
            return ( isServerException(nexte) );
        } 

        return true;
    }
    
    public static ConnectStatus testConnection(String url, String user, 
            String password) {
        Connection conn;
        try {
            conn = connect(url, user, password);
        } catch ( DatabaseException e ) {
            if ( e.getCause() instanceof SQLException ) {
                LOGGER.log(Level.FINE, null, e);
                if ( DatabaseUtils.isServerException(
                        (SQLException)e.getCause()) ) {
                    return ConnectStatus.SERVER_RUNNING;
                } else {
                    return ConnectStatus.NO_SERVER;
                }
            } else {
                Exceptions.printStackTrace(e);
                return ConnectStatus.NO_SERVER;
            }
        }

        // Issue 127994 - driver sometimes silently returns null (??)
        if ( conn == null ) {
            return ConnectStatus.NO_SERVER;
        }

        try { 
            conn.close();
        } catch (SQLException sqle) {
            LOGGER.log(Level.FINE, null, sqle);
        }

        return ConnectStatus.CONNECT_SUCCEEDED;            
    }

    
    public static class URLParser {
        private static final String MYSQL_PROTOCOL = "jdbc:mysql://";
        private String host;
        private String port;
                
        private final String url;
        public URLParser(String url) {
            assert(url != null && url.startsWith(MYSQL_PROTOCOL));
            
            this.url = url.replaceFirst(MYSQL_PROTOCOL, "");
        }
        
        public String getHost() {
            if ( host == null ) {
                if ( url.indexOf(":") >= 0 ) {
                    host = url.split(":")[0];
                } else {
                   host = url.split("/")[0]; 
                }
            }
            
            return host;
        }
        
        public String getPort() {
            if ( port == null ) {
                if ( url.indexOf(":") >= 0 ) {
                    port = url.split(":")[1];
                    if ( url.indexOf("/") >= 0) {
                        port = url.split("/")[0];
                    }
                } else {
                   port = "";
                }
            }
            
            return port;            
        }
    }
}
