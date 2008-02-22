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
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

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

    private static final MySQLOptions options = MySQLOptions.getDefault();
    
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
        return options.getHost();
    }

    public void setHost(String host) {
        options.setHost(host);
    }
 
    public String getPort() {
        return options.getPort();
    }

    public void setPort(String port) {
        options.setPort(port);
    }

    public String getUser() {
        return options.getAdminUser();
    }

    public void setUser(String adminUser) {
        options.setAdminUser(adminUser);
    }

    public String getPassword() {
        if ( adminPassword != null ) {
            return adminPassword;
        } else{
            return options.getAdminPassword();
        }
    }

    public void setPassword(String adminPassword) {
        this.adminPassword = adminPassword == null ? "" : adminPassword;

        if ( isSavePassword() ) {
            options.setAdminPassword(adminPassword);
        } 
    }
    
    public boolean isSavePassword() {
        return options.isSavePassword();
    }

    public void setSavePassword(boolean savePassword) {
        options.setSavePassword(savePassword);
    }
    
    public boolean isConnected() {
        return adminConn.conn != null;
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
        } else {
            setDisplayName(NbBundle.getMessage(ServerInstance.class,
                    "LBL_ServerNotConnectedDisplayName"));
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
            ResultSet rs = adminConn.getConnection()
                    .prepareStatement(GET_DATABASES_SQL).
                    executeQuery();

            while ( rs.next() ) {
                databases.add(new DatabaseModel(this, rs.getString(1)));
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
            
            refreshDatabaseList();
        } catch ( SQLException sqle ) {
            throw new DatabaseException(sqle);
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
            
        synchronized void reconnect() throws DatabaseException {
            conn = null;
            
            try {
                // I would love to use a DatabaseConnection, but this
                // causes deadlocks because DatabaseConnection.showDialog
                // relys on DatabaseNodeInfo, which scans the node tree
                // at the same time this method is being used to update
                // the node tree (e.g. to get the list of databases).

                conn = DatabaseUtils.connect(getURL(), getUser(), 
                        getPassword());
            } catch ( SQLException sqle ) {
                throw new DatabaseException(sqle);
            }

        }
    } 
    
}
