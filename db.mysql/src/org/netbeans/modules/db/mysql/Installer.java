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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.mysql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Class that is run when the module is being restored.  This class in 
 * particular is responsible for registering the MySQL features into
 * the Database Explorer
 * 
 * @author David Van Couvering
 */
public class Installer extends ModuleInstall {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
    private static final MySQLOptions options = MySQLOptions.getDefault();

    @Override
    public void restored() {
        // If MySQL was already registered once for this user, don't
        // do it again
        if ( options.getConnectionRegistered() ) {
            return;
        }
            
        WindowManager.getDefault().invokeWhenUIReady(new RegisterMySQL());
    }

    private static final class RegisterMySQL implements Runnable {
        private JDBCDriver jdbcDriver;
        
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                RequestProcessor.getDefault().post(this);
                return;
            }


            ProgressHandle handle = ProgressHandleFactory.createSystemHandle(
                    NbBundle.getMessage(Installer.class, "MSG_RegisterMySQL"));
            handle.start();
            try {
                findAndRegisterMySQL();
            } finally {
                handle.finish();
            }
        }

        /**
         * Try to find MySQL on the local machine, and if it can be found,
         * register a connection and the MySQL server node in the Database
         * Explorer.
         */
        private void findAndRegisterMySQL() {            
            if ( (jdbcDriver = getJDBCDriver()) == null ) {
                // Driver not registered, that's OK, the user may 
                // have deleted it, but nothing to do here.
                return;
            }
                        
            // TODO - Search for install path and set location if found
            
            String host = MySQLOptions.getDefaultHost();
            String port = MySQLOptions.getDefaultPort();
            String user = MySQLOptions.getDefaultAdminUser();
            String password = MySQLOptions.getDefaultAdminPassword();
            
            String url = MySQLOptions.getURL(host, port);
            ConnectStatus status = testConnection(url, user, password);
            
            if ( status == ConnectStatus.CONNECT_SUCCEEDED  ||
                 status == ConnectStatus.SERVER_RUNNING ) {
                options.setHost(host);
                options.setPort(port);
                options.setAdminUser(user);
                
                registerConnection(host, port, user);
            }
        }

        private void registerConnection(String host, String port, String user) {
            if ( options.getConnectionRegistered() ) {
                return;
            }
            
            String url = MySQLOptions.getURL(host, port);
            DatabaseConnection[] connections = 
                    ConnectionManager.getDefault().getConnections();
            
            for ( DatabaseConnection conn : connections ) {
                // If there's already a connection registered, we're done
                if ( conn.getDriverClass().equals(MySQLOptions.getDriverClass()) &&
                     conn.getDatabaseURL().equals(url) && 
                     conn.getUser().equals(user)) {
                    options.setConnectionRegistered(true);
                    return;
                }
            }
            
            DatabaseConnection dbconn = 
                    DatabaseConnection.create(jdbcDriver, url, user, null, null, false);
            
            try {
                ConnectionManager.getDefault().addConnection(dbconn);
                options.setConnectionRegistered(true);
            } catch ( DatabaseException e ) {
                LOGGER.log(Level.INFO, 
                    "Unable to register default connection for MySQL", e);
            }            
        }

        private ConnectStatus testConnection(String url, String user, 
                String password) {            
            Driver driver = getDriver();
            Properties dbprops = new Properties();
            Connection conn;

            if ( driver == null ) {
                return ConnectStatus.NO_SERVER;
            } 
            
            if ((user != null) && (user.length() > 0)) {
                dbprops.put("user", user); //NOI18N
                dbprops.put("password", password); //NOI18N
            }
           
            try {
                conn = driver.connect(url, dbprops);
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, null, e);
                if ( isServerException(e) ) {
                    return ConnectStatus.SERVER_RUNNING;
                } else {
                    return ConnectStatus.NO_SERVER;
                }
            }
            
            try { 
                conn.close();
            } catch (SQLException sqle) {
                LOGGER.log(Level.FINE, null, sqle);
            }

            return ConnectStatus.CONNECT_SUCCEEDED;            
        }
        
        private JDBCDriver getJDBCDriver() {
            JDBCDriver[]  drivers = JDBCDriverManager.getDefault().
                    getDrivers(MySQLOptions.getDriverClass());

            if ( drivers.length == 0 ) {
                return null;
            }

            return drivers[0];
        }
        
        private Driver getDriver() {
            Driver driver;
            ClassLoader driverLoader = new DriverClassLoader(jdbcDriver);
            
            try {
                driver = (Driver)Class.forName(jdbcDriver.getClassName(), 
                        true, driverLoader).newInstance();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            
            return driver;

        }

        private boolean isServerException(SQLException e) {
            // An exception whose SQL state starts with '20' is
            // client side-only.  So any SQL state that *doesn't*
            // start with '20' must have come from a live server
            //
            // See http://dev.mysql.com/doc/refman/5.0/en/error-handling.html
            //
            if ( e.getSQLState().startsWith("20"))  { // NOI18N
                if ( e.getNextException() != null ) {
                    // Maybe the next exception is a server exception...
                    return ( isServerException(e.getNextException()) );
                } else {
                    return false;
                }
            }
            
            return true;
        }

    }
}
