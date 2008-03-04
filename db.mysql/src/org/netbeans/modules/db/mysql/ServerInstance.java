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

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.mysql.DatabaseUtils.ConnectStatus;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Model for a server.  Currently just uses MySQLOptions since we only
 * support one server, but this can be migrated to use an approach that
 * supports more than one server
 * 
 * @author David Van Couvering
 */
public class ServerInstance implements Node.Cookie {

    /**
     *  Enumeration of valid sample database names
     */
    public enum SampleName {
        sample, vir, travel
    };

    private static final Logger LOGGER = Logger.getLogger(ServerInstance.class.getName());
        
    private static ServerInstance DEFAULT;;

    private static final MySQLOptions OPTIONS = MySQLOptions.getDefault();
    
    // SQL commands
    private static final String GET_DATABASES_SQL = "SHOW DATABASES"; // NOI18N
    private static final String GET_USERS_SQL = 
            "SELECT DISTINCT user, host FROM mysql.user"; // NOI18N
    private static final String CREATE_DATABASE_SQL = "CREATE DATABASE "; // NOI18N
    private static final String DROP_DATABASE_SQL = "DROP DATABASE "; // NOI18N
    
    // This is in two parts because the database name is an identifier and can't
    // be parameterized (it gets quoted and it is a syntax error to quote it).
    private static final String GRANT_ALL_SQL_1 = "GRANT ALL ON "; // NOI18N
    private static final String GRANT_ALL_SQL_2 = ".* TO ?@?"; // NOI8N
    
    // Other static finals
    private static final String MODULE_JAR_FILE = 
            "modules/org-netbeans-modules-db-mysql.jar";
    private static final String RESOURCE_DIR_PATH =
            "org/netbeans/modules/db/mysql/resources";
    
    final AdminConnection adminConn = new AdminConnection();
    final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    // Cache this in cases where it is not being saved to disk
    private String adminPassword;
    
    // Cache list of databases, refresh only if connection is changed
    // or an explicit refresh is requested
    ArrayList<DatabaseModel> databases;
    
    static synchronized ServerInstance getDefault() {
        if ( DEFAULT == null ) {
            DEFAULT = new ServerInstance();
        }
        
        return DEFAULT;
    }
    private String displayName;
    
    private ServerInstance() {  
        // Try and set up an initial connection to the server
        try {
            adminConn.reconnect();
        } catch ( DatabaseException e ) {
            LOGGER.log(Level.FINE, null, e);
        }
        
        updateDisplayName();
    }
    
    public static boolean isSampleName(String name) {
        SampleName[] samples = SampleName.values();
        for ( SampleName sample : samples ) {
            if (sample.toString().equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void createSample(String sampleName, DatabaseConnection dbconn) 
            throws DatabaseException {     
        if ( ! isSampleName(sampleName)) {
            throw new DatabaseException(NbBundle.getMessage(
                    ServerInstance.class, 
                    "MSG_NoSuchSample", sampleName));
        }
             
        DataObject sqlDO = getSQLDataObject(sampleName);
        
        try {
            if ( ! DatabaseUtils.ensureConnected(dbconn) ) {
                return;
            } 
            
            OpenCookie openCookie = (OpenCookie)sqlDO.getCookie(OpenCookie.class);
            openCookie.open();

            SQLExecuteCookie sqlCookie = (SQLExecuteCookie)sqlDO.getCookie(
                    SQLExecuteCookie.class);

            sqlCookie.setDatabaseConnection(dbconn);
            sqlCookie.execute();
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(
                    NbBundle.getMessage(ServerInstance.class,
                        "MSG_ErrorExecutingSampleSQL", sampleName, 
                        e.getMessage()));
            dbe.initCause(e);
            throw dbe;
        } finally {
            if ( sqlDO != null ) {
                CloseCookie closeCookie = 
                        (CloseCookie)sqlDO.getCookie(CloseCookie.class);
                
                if ( closeCookie != null ) {
                    closeCookie.close();
                }
            }
        }

    }
        
    private DataObject getSQLDataObject(String sampleName) 
            throws DatabaseException {
        SQLExecuteCookie sqlCookie = null;
        
        try {
            File jarfile = InstalledFileLocator.getDefault().locate(
                MODULE_JAR_FILE, null, false); // NOI18N
    
            JarFileSystem jarfs = new JarFileSystem();

            jarfs.setJarFile(jarfile);

            String filename = "/create-" + sampleName + ".sql";
            FileObject sqlFO = jarfs.findResource(RESOURCE_DIR_PATH + filename);

            return DataObject.find(sqlFO);
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(
                    NbBundle.getMessage(ServerInstance.class,
                        "MSG_ErrorLoadingSampleSQL", sampleName, 
                        e.getMessage()));
            dbe.initCause(e);
            throw dbe;
        }
    }


    public String getHost() {
        return OPTIONS.getHost();
    }

    public void setHost(String host) {
        OPTIONS.setHost(host);
    }
 
    public String getPort() {
        return OPTIONS.getPort();
    }

    public void setPort(String port) {
        OPTIONS.setPort(port);
    }

    public String getUser() {
        return OPTIONS.getAdminUser();
    }

    public void setUser(String adminUser) {
        OPTIONS.setAdminUser(adminUser);
    }

    public String getPassword() {
        if ( adminPassword != null ) {
            return adminPassword;
        } else{
            return OPTIONS.getAdminPassword();
        }
    }

    public void setPassword(String adminPassword) {
        this.adminPassword = adminPassword == null ? "" : adminPassword;

        if ( isSavePassword() ) {
            OPTIONS.setAdminPassword(adminPassword);
        } 
    }
    
    public boolean isSavePassword() {
        return OPTIONS.isSavePassword();
    }

    public void setSavePassword(boolean savePassword) {
        OPTIONS.setSavePassword(savePassword);
        
        // Save the password in case it was already set...
        OPTIONS.setAdminPassword(getPassword());
    }
    
    public String getAdminPath() {
        return OPTIONS.getAdminPath();
    }
    
    public void setAdminPath(String path) {
        OPTIONS.setAdminPath(path);
    }
    
    public String getStartPath() {
        return OPTIONS.getStartPath();
    }
    
    public void setStartPath(String path) {
        OPTIONS.setStartPath(path);
    }
    
    public String getStopPath() {
        return OPTIONS.getStopPath();
    }
    
    public void setStopPath(String path) {
        OPTIONS.setStopPath(path);
    }
    
    public String getStopArgs() {
        return OPTIONS.getStopArgs();
    }
    
    public void setStopArgs(String args) {
        OPTIONS.setStopArgs(args);
    }
    public String getStartArgs() {
        return OPTIONS.getStartArgs();
    }
    
    public void setStartArgs(String args) {
        OPTIONS.setStartArgs(args);
    }
    public String getAdminArgs() {
        return OPTIONS.getAdminArgs();
    }
    
    public void setAdminArgs(String args) {
        OPTIONS.setAdminArgs(args);
    }
    
    public boolean isAdminCommandsConfirmed() {
        return OPTIONS.isAdminCommandsConfirmed();
    }
    
    public void setAdminCommandsConfirmed(boolean confirmed) {
        OPTIONS.setAdminCommandsConfirmed(confirmed);
    }
    
    public boolean isConnected() {
        return adminConn.conn != null;
    }
    
    public boolean isRunning() {
        if ( isConnected() ) {
            return true;
        }
        
        // Test to see if the server is up but we can't authenticate,
        // or if it's just not there. 
        ConnectStatus status = DatabaseUtils.testConnection(
                getURL(), getHost(), getPort());
        
        return ( status == ConnectStatus.CONNECT_SUCCEEDED || 
                 status == ConnectStatus.SERVER_RUNNING);

    }

    public String getDisplayName() {
        return displayName;
    }
    
    private void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    private void updateDisplayName() {
        if ( isConnected() ) {
            setDisplayName(NbBundle.getMessage(ServerInstance.class,
                    "LBL_ServerDisplayName"));
        } else if ( isRunning() ) {
            setDisplayName(NbBundle.getMessage(ServerInstance.class,
                    "LBL_ServerNotConnectedDisplayName"));
        } else {
            setDisplayName(NbBundle.getMessage(ServerInstance.class,
                    "LBL_ServerNotRunningDisplayName"));
        }
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(ServerInstance.class,
                "LBL_ServerDisplayName");
    }
    
    public String getURL() {
        return DatabaseUtils.getURL(getHost(), getPort());
    }
    
    public String getURL(String databaseName) {
        return DatabaseUtils.getURL(getHost(), getPort(), databaseName);
    }

    
    private void notifyChange() {
        ChangeEvent evt = new ChangeEvent(this);
        
        for ( ChangeListener listener : listeners ) {
            listener.stateChanged(evt);
        }
    }
    
    public void refreshDatabaseList() throws DatabaseException { 
        databases = new ArrayList<DatabaseModel>();
        
        try {
            if ( isConnected() ) {        
                ResultSet rs = adminConn.getConnection()
                        .prepareStatement(GET_DATABASES_SQL).
                        executeQuery();

                while ( rs.next() ) {
                    databases.add(new DatabaseModel(this, rs.getString(1)));
                }
            }
        } catch ( SQLException ex ) {
            throw new DatabaseException(ex);
        } finally {
            notifyChange();
        }
    }

    /**
     * Get the list of databases.  NOTE that the list is retrieved from
     * a cache to improve performance.  If you want to ensure that the
     * list is up-to-date, call <i>refreshDatabaseList</i>
     * 
     * @return
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    public Collection<DatabaseModel> getDatabases() throws DatabaseException {
        if ( databases == null ) {
            refreshDatabaseList();
        }
        
        return databases;
    }
        
    public void connect() throws DatabaseException {
        try {
            adminConn.reconnect();
        } finally {
            updateDisplayName();
            refreshDatabaseList();
        }
    }
    
    public void disconnect() {
        adminConn.disconnect();
        updateDisplayName();
        try {
            this.refreshDatabaseList();
        } catch ( DatabaseException dbe ) {
            LOGGER.log(Level.FINE, null, dbe);
        }
    }
    
    public void reconnect() throws DatabaseException {
        adminConn.disconnect();
        connect();
    }

    
    public boolean databaseExists(String dbname)  throws DatabaseException {
        refreshDatabaseList();
        
        return getDatabases().contains(dbname);
    }
    
    public void createDatabase(String dbname) throws DatabaseException {
        try { 
            adminConn.getConnection()
                    .prepareStatement(CREATE_DATABASE_SQL + dbname)
                    .executeUpdate();
            
            refreshDatabaseList();
        } catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }
    
    public void dropDatabase(String dbname) throws DatabaseException {
        try {
            adminConn.getConnection()
                    .prepareStatement(DROP_DATABASE_SQL + dbname)
                    .executeUpdate();
            
            deleteConnections(dbname);
        } catch ( SQLException sqle ) {
            throw new DatabaseException(sqle);
        } finally {
            refreshDatabaseList();            
        }
        
    }
    
    private void deleteConnections(String dbname) throws DatabaseException {
        List<DatabaseConnection> conns = 
                DatabaseUtils.findDatabaseConnections(getURL(dbname));
        
        for ( DatabaseConnection conn : conns ) {
            ConnectionManager.getDefault().removeConnection(conn);
        }
    }

    
    /**
     * Get the list of users defined for this server
     * 
     * @return the list of users
     * 
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *      if some problem occurred
     */
    public List<DatabaseUser> getUsers() throws DatabaseException {
        ArrayList<DatabaseUser> users = new ArrayList<DatabaseUser>();
        Connection conn = adminConn.getConnection();
        
        if ( conn == null ) {
            return users;
        }
        
        try {
            ResultSet rs = conn.prepareStatement(GET_USERS_SQL).executeQuery();

            while ( rs.next() ) {
                String user = rs.getString(1).trim();
                String host = rs.getString(2).trim();
                users.add(new DatabaseUser(user, host));
            }
        } catch ( SQLException ex ) {
            throw new DatabaseException(ex);
        }
        
        return users;
    }

    /**
     * Grant full rights to the database to the specified user
     * 
     * @param dbname the database whose rights we are granting
     * @param grantUser the name of the user to grant the rights to
     * 
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *      if some error occurs
     */
    public void grantFullDatabaseRights(String dbname, DatabaseUser grantUser) 
        throws DatabaseException {
        try {
            PreparedStatement ps = adminConn.getConnection()
                    .prepareStatement(GRANT_ALL_SQL_1 + dbname +
                        GRANT_ALL_SQL_2);
            ps.setString(1, grantUser.getUser());
            ps.setString(2, grantUser.getHost());
            
            ps.executeUpdate();
        } catch ( SQLException sqle ) {
            throw new DatabaseException(sqle);
        }
    }
    
    /**
     * Run the start command.  Display stdout and stderr to an output
     * window.  Wait the configured wait time, attempt to connect, and
     * then return.
     * 
     * @return true if the server is definitely started, false otherwise (the server is
     *  not started or the status is unknown).
     * 
     * @throws org.netbeans.api.db.explorer.DatabaseException
     * 
     * @see #getStartWaitTime()
     */
    public void start() throws DatabaseException {        
        if ( !Utils.isValidExecutable(getStopPath(), false)) {
            throw new DatabaseException(NbBundle.getMessage(ServerInstance.class,
                    "MSG_InvalidStartCommand"));
        }
        
        try {
            runProcess(getStartPath(), getStartArgs(),
                    true, NbBundle.getMessage(ServerInstance.class, 
                        "LBL_StartOutputTab"));
            
            // Spawn off a thread to poll the server and attempt to 
            // reconnect.  Give up after 5 minutes
            new Thread() {
                @Override
                public void run() {
                    long fiveMinutes = 1000 * 60 * 5;
                    long runTime = 0;
                    
                    while ( runTime < fiveMinutes ) {
                        try {
                            sleep(1000);
                        } catch ( InterruptedException e ) {
                            return;
                        }
                        
                        try {
                            reconnect();
                            return;
                        } catch ( DatabaseException e ) {
                        }

                        runTime += 1000;
                    }
                }
            }.start();

        } catch ( Exception e ) {
            throw new DatabaseException(e);
        }

    }
    
    public void stop() throws DatabaseException {
        if ( !Utils.isValidExecutable(getStopPath(), false)) {
            throw new DatabaseException(NbBundle.getMessage(ServerInstance.class,
                    "MSG_InvalidStopCommand"));
        }
        
        try {
            runProcess(getStopPath(), getStopArgs(), 
                    true, NbBundle.getMessage(ServerInstance.class, 
                        "LBL_AdminOutputTab"));
        } catch ( Exception e ) {
            throw new DatabaseException(e);
        }
                
        // Mark ourselves as disconnected (this includes notifying listeners)
        disconnect();
    }
    
    /**
     * Launch the admin tool.  If the specified admin path is a URL,
     * a browser is launched with the URL.  If the specified admin path
     * is a file, the file path is executed.
     * 
     * @return a process object for the executed command if the admin
     *   path was a file.  Returns null if the browser was launched.
     * 
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    public void startAdmin() throws DatabaseException {
        String adminCommand = getAdminPath();
        
        if ( adminCommand == null || adminCommand.length() == 0) {
            throw new DatabaseException(NbBundle.getMessage(
                    ServerInstance.class,
                    "MSG_AdminCommandNotSet"));
        }
        
        if ( Utils.isValidURL(adminCommand, false)) {
            launchBrowser(adminCommand);
        } else if ( Utils.isValidExecutable(adminCommand, false)) {
            runProcess(adminCommand, getAdminArgs(),
                    true, NbBundle.getMessage(ServerInstance.class, 
                        "LBL_AdminOutputTab"));
        } else {
            throw new DatabaseException(NbBundle.getMessage(
                    ServerInstance.class,
                    "MSG_InvalidAdminCommand", adminCommand));
        }
        
    }
    
    private void runProcess(String command, String args, boolean displayOutput,
            String outputLabel) throws DatabaseException {
        
        if ( Utilities.isMac() && command.endsWith(".app") ) {  // NOI18N
            // TODO - find a way to run .app files.  This feels like a hack
            String[] pieces = command.split("/"); // NOI18N
            String base = pieces[pieces.length - 1];
            base = base.replace(".app", ""); // NOI18N
            command = command + "/Contents/MacOS/" + base; // NOI18N
        }
        try {
            NbProcessDescriptor desc = new NbProcessDescriptor(command, args);
            Process proc = desc.exec();
            
            if ( displayOutput ) {
                new ExecSupport().displayProcessOutputs(proc, outputLabel);
            }
        } catch ( Exception e ) {
            throw new DatabaseException(e);
        }
        
    }

    private void launchBrowser(String adminCommand)  throws DatabaseException {
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(adminCommand));
        } catch ( Exception e ) {
            throw new DatabaseException(e);
        }
    }
    
    void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    } 
    
    void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

      
    /**
     * Provides a useful abstraction around the database connection
     * for this server.  In particular it invalidates the connection if
     * a connection property gets changed.
     */
    class AdminConnection {
        private Connection conn;
        
        private AdminConnection() {
        }
        
        synchronized Connection getConnection() throws DatabaseException {
            try {
                if ( conn == null || conn.isClosed() ) {
                    reconnect();
                }
            } catch ( SQLException sqle ) {
                throw new DatabaseException(sqle);
            }
            return conn;
        }
        
        synchronized void disconnect() {
            DatabaseUtils.closeConnection(conn);
            conn = null;
        }
            
        synchronized void reconnect() throws DatabaseException {
            conn = null;

            // I would love to use a DatabaseConnection, but this
            // causes deadlocks because DatabaseConnection.showDialog
            // relys on DatabaseNodeInfo, which scans the node tree
            // at the same time this method is being used to update
            // the node tree (e.g. to get the list of databases).

            conn = DatabaseUtils.connect(getURL(), getUser(), 
                    getPassword());
        }
    } 
    
}
