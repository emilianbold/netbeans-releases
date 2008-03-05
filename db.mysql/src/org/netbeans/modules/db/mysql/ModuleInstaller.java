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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.mysql.DatabaseUtils.ConnectStatus;
import org.netbeans.modules.db.mysql.DatabaseUtils.URLParser;
import org.openide.modules.ModuleInstall;
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
public class ModuleInstaller extends ModuleInstall {

    private static final Logger LOGGER = Logger.getLogger(ModuleInstaller.class.getName());
    private static final MySQLOptions options = MySQLOptions.getDefault();

    @Override
    public void restored() {
        // If MySQL was already registered once for this user, don't
        // do it again
        if ( options.isConnectionRegistered() && options.isProviderRegistered()) {
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
                    NbBundle.getMessage(ModuleInstaller.class, "MSG_RegisterMySQL"));
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
            if ( (jdbcDriver = DatabaseUtils.getJDBCDriver()) == null ) {
                // Driver not registered, that's OK, the user may 
                // have deleted it, but nothing to do here.
                return;
            }
                        
            findAndRegisterInstallation();            
            findAndRegisterRunningServer();
        }
        
        private void findAndRegisterRunningServer() {
            String host = MySQLOptions.getDefaultHost();
            
            // port may have been set through installation detection
            String port = options.getPort();
            if ( port == null || port.length() == 0 ) {
                port = MySQLOptions.getDefaultPort();
            }
            String user = MySQLOptions.getDefaultAdminUser();
            String password = MySQLOptions.getDefaultAdminPassword();
            
            DatabaseConnection dbconn = findDatabaseConnection();
            if ( dbconn != null ) {
                // The user has a registered connection for MySQL, 
                // so let's use its settings to register the MySQL node
                options.setAdminUser(user);
                if ( MySQLOptions.getDefaultAdminUser().equals(user)) {
                    options.setAdminPassword(password);
                }

                URLParser urlParser = new URLParser(dbconn.getDatabaseURL());
                options.setHost(urlParser.getHost());
                options.setPort(urlParser.getPort());
                options.setConnectionRegistered(true);
                registerProvider(true);
                return;
            }

            // All right, now let's try auto-detection...
            String url = DatabaseUtils.getURL(host, port);
            ConnectStatus status = DatabaseUtils.testConnection(url, user, 
                    password);
            
            if ( status == ConnectStatus.CONNECT_SUCCEEDED  ||
                 status == ConnectStatus.SERVER_RUNNING ) {
                options.setHost(host);
                options.setPort(port);
                options.setAdminUser(user);
                
                registerConnection(host, port, user);
                registerProvider(true);
            }

        }
        
        private void findAndRegisterInstallation() {
            Installation installation = InstallationSupport.detectInstallation();
            if ( installation == null ) {
                return;
            }
            
            String[] command = installation.getAdminCommand();
            if ( Utils.isValidExecutable(command[0], true /*emptyOK*/) ||
                 Utils.isValidURL(command[0], true /*emptyOK*/ )) {
                options.setAdminPath(command[0]);
                options.setAdminArgs(command[1]);
            }
            
            command = installation.getStartCommand();
            if ( Utils.isValidExecutable(command[0], true)) {
                options.setStartPath(command[0]);
                options.setStartArgs(command[1]);
            }
            
            command = installation.getStopCommand();
            if ( Utils.isValidExecutable(command[0], true)) {
                options.setStopPath(command[0]);
                options.setStopArgs(command[1]);
            }
            
            options.setPort(installation.getDefaultPort());
            
            options.setProviderRegistered(true);
        }

        private DatabaseConnection findDatabaseConnection() {
            DatabaseConnection[] connections = 
                    ConnectionManager.getDefault().getConnections();
            
            for ( DatabaseConnection conn : connections ) {
                if ( conn.getDriverClass().equals(MySQLOptions.getDriverClass())) {
                    return conn;
                }
            }
            
            return null;
        }

        private void registerConnection(String host, String port, String user) {
            if ( options.isConnectionRegistered() ) {
                return;
            }
            
            String url = DatabaseUtils.getURL(host, port);                        
            DatabaseConnection dbconn = DatabaseConnection.create(jdbcDriver, url, user, 
                 null, null, false);

            try {
             ConnectionManager.getDefault().addConnection(dbconn);
             options.setConnectionRegistered(true);
            } catch ( DatabaseException e ) {
             LOGGER.log(Level.INFO, 
                 "Unable to register default connection for MySQL", e);
            }
        }

        private void registerProvider(boolean value) {
            ServerNodeProvider.getDefault().setRegistered(value);
        }

    }   
}
