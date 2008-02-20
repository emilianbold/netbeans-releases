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
import org.openide.util.NbBundle;

/**
 *
 * @author David
 */
public class DatabaseUtils {   
    private static final Logger LOGGER = 
            Logger.getLogger(DatabaseUtils.class.getName());
    
    // A cache of the driver class so we don't have to load it each time
    private static Driver driver;
    
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
            throws SQLException, DatabaseException {
        Driver driver = getDriver();
        Properties props = new Properties();
        props.put("user", user == null ? "" : user);                
        props.put("password", password == null ? "" : password);

        return driver.connect(url, props);        
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
}
