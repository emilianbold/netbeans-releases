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

package org.netbeans.modules.db.explorer;

import java.awt.Component;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.ObjectStreamException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.explorer.actions.ConnectAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task ;

import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;

import org.netbeans.modules.db.ExceptionListener;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.nodes.RootNode;

import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;


/**
 * Connection information
 * This class encapsulates all information needed for connection to database
 * (database and driver url, login name, password and schema name). It can create JDBC
 * connection and feels to be a bean (has propertychange support and customizer).
 * Instances of this class uses explorer option to store information about
 * open connection.
 */
public class DatabaseConnection implements DBConnection {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    static final long serialVersionUID =4554639187416958735L;

    private Set exceptionListeners = Collections.synchronizedSet(new HashSet());
    private Connection con;

    /** Driver URL and name */
    private String drv, drvname;

    /** Database URL */
    private String db;

    /** User login name */
    private String usr;

    /** Schema name */
    private String schema;

    /** User password */
    private String pwd = ""; //NOI18N

    /** Remembers password */
    private Boolean rpwd = Boolean.FALSE;

    /** The support for firing property changes */
    private PropertyChangeSupport propertySupport;

    /** Connection name */
    private String name;
    
    /**
     * The API DatabaseConnection (delegates to this instance)
     */
    private transient org.netbeans.api.db.explorer.DatabaseConnection dbconn;

    private static final String SUPPORT = "_schema_support"; //NOI18N
    public static final String PROP_DRIVER = "driver"; //NOI18N
    public static final String PROP_DATABASE = "database"; //NOI18N
    public static final String PROP_USER = "user"; //NOI18N
    public static final String PROP_PASSWORD = "password"; //NOI18N
    public static final String PROP_REMEMBER_PASSWORD = "rememberpwd";
    public static final String PROP_SCHEMA = "schema"; //NOI18N
    public static final String PROP_DRIVERNAME = "drivername"; //NOI18N
    public static final String PROP_NAME = "name"; //NOI18N

    private OpenConnectionInterface openConnection = null;

    static private final Lookup.Result openConnectionLookupResult;
    static private Collection openConnectionServices = null;
    static {
        openConnectionLookupResult = Lookup.getDefault().lookup(new Lookup.Template(OpenConnectionInterface.class));
        openConnectionLookupResult.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                synchronized (DatabaseConnection.class) {
                    openConnectionServices = null;
                }
            }
        });            
    }

    /** Default constructor */
    public DatabaseConnection() {
        dbconn = DatabaseConnectionAccessor.DEFAULT.createDatabaseConnection(this);
        propertySupport = new PropertyChangeSupport(this);
    }
    
    /** Advanced constructor
     * Allows to specify all needed information.
     * @param driver Driver URL
     * @param database Database URL
     * @param user User login name
     * @param password User password
     */
    public DatabaseConnection(String driver, String database, String user, String password) {
        this(driver, null, database, null, user, password, false);
    }
    
    public DatabaseConnection(String driver, String driverName, String database,
            String theschema, String user, String password) {
        this(driver, driverName, database, theschema, user, password, false);
    }
    
    public DatabaseConnection(String driver, String driverName, String database, 
            String theschema, String user, String password,
            boolean rememberPassword) {
        this();
        drv = driver;
        drvname = driverName;
        db = database;
        usr = user;
        schema = theschema;
        pwd = password;
        name = getName();
        rpwd = Boolean.valueOf(rememberPassword);
    }
    
    public JDBCDriver findJDBCDriver() {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers(drv);
        if (drvs.length <= 0) {
            return null;
        }

        JDBCDriver useDriver = drvs[0];
        for (int i = 0; i < drvs.length; i++) {
            if (drvs[i].getName().equals(getDriverName())) {
                useDriver = drvs[i];
                break;
            }
        }
        return useDriver;
    }

     private Collection getOpenConnections() {
         if (openConnectionServices == null) {
             openConnectionServices = openConnectionLookupResult.allInstances();
         }
         return openConnectionServices;
     }

     private OpenConnectionInterface getOpenConnection() {
         if (openConnection != null)
             return openConnection;
         
         openConnection = new OpenConnection();
         String driver = getDriver();
         if (driver == null) {
             return openConnection;
         }
         
         // For Java Studio Enterprise. Create instanceof OpenConnection
         try {
             Collection c = getOpenConnections();
             for (Iterator i=c.iterator(); driver != null && i.hasNext();) {
                 OpenConnectionInterface oci = (OpenConnectionInterface) i.next();
                 if (oci.isFor(driver)) {
                     openConnection = oci;
                     break;
                 }
             }
         } catch(Exception ex) {
             Logger.getLogger("global").log(Level.INFO, null, ex);
         }
         return openConnection;
     }
     
    /** Returns driver class */
    public String getDriver() {
        return drv;
    }

    /** Sets driver class
     * Fires propertychange event.
     * @param driver DNew driver URL
     */
    public void setDriver(String driver) {
        if (driver == null || driver.equals(drv))
            return;

        String olddrv = drv;
        drv = driver;
        propertySupport.firePropertyChange(PROP_DRIVER, olddrv, drv);
        openConnection = null;
    }

    public String getDriverName() {
        return drvname;
    }

    public void setDriverName(String name) {
        if (name == null || name.equals(drvname))
            return;

        String olddrv = drvname;
        drvname = name;
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_DRIVERNAME, olddrv, drvname);
    }

    /** Returns database URL */
    public String getDatabase() {
        if (db == null)
            db = "";

        return db;
    }

    /** Sets database URL
     * Fires propertychange event.
     * @param database New database URL
     */
    public void setDatabase(String database) {
        if (database == null || database.equals(db))
            return;

        String olddb = db;
        db = database;
        name = null;
        name = getName();
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_DATABASE, olddb, db);
    }

    /** Returns user login name */
    public String getUser() {
        if (usr == null)
            usr = "";

        return usr;
    }

    /** Sets user login name
     * Fires propertychange event.
     * @param user New login name
     */
    public void setUser(String user) {
        if (user == null || user.equals(usr))
            return;

        String oldusr = usr;
        usr = user;
        name = null;
        name = getName();
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_USER, oldusr, usr);
    }

    /** Returns name of the connection */
    public String getName() {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
        if(name == null)
            if((getSchema()==null)||(getSchema().length()==0))
                name = MessageFormat.format(bundle.getString("ConnectionNodeUniqueName"), new String[] {getDatabase(), getUser(), bundle.getString("SchemaIsNotSet")}); //NOI18N
            else
                name = MessageFormat.format(bundle.getString("ConnectionNodeUniqueName"), new String[] {getDatabase(), getUser(), getSchema()}); //NOI18N
                return name;
    }

    /** Sets user name of the connection
     * Fires propertychange event.
     * @param value New connection name
     */
    public void setName(String value) {
        if (name == null || name.equals(value))
            return;

        String old = name;
        name = value;
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_NAME, old, name);
    }

    /** Returns user schema name */
    public String getSchema() {
        if (schema == null)
            schema = "";

        return schema;
    }

    /** Sets user schema name
     * Fires propertychange event.
     * @param schema_name New login name
     */
    public void setSchema(String schema_name) {
        if (schema_name == null || schema_name.equals(schema))
            return;

        String oldschema = schema;
        schema = schema_name;
        name = null;
        name = getName();
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_SCHEMA, oldschema, schema);
    }

    /** Returns if password should be remembered */
    public boolean rememberPassword() {
        return rpwd.booleanValue();
    }

    /** Sets password should be remembered
     * @param flag New flag
     */
    public void setRememberPassword(boolean flag) {
        Boolean oldrpwd = rpwd;
        rpwd = Boolean.valueOf(flag);
        if(propertySupport!=null)
            propertySupport.firePropertyChange(
                    PROP_REMEMBER_PASSWORD, oldrpwd, rpwd);
    }

    /** Returns password */
    public String getPassword() {
        return pwd;
    }

    /** Sets password
     * Fires propertychange event.
     * @param password New password
     */
    public void setPassword(String password) {
        if (password == null || password.equals(pwd))
            return;

        String oldpwd = pwd;
        if ( password.length() == 0 ) {
            password = null;
        }
        pwd = password;
        if(propertySupport!=null)
            propertySupport.firePropertyChange(PROP_PASSWORD, oldpwd, pwd);
    }

    /** Creates JDBC connection
     * Uses DriverManager to create connection to specified database. Throws
     * DDLException if none of driver/database/user/password is set or if
     * driver or database does not exist or is inaccessible.
     */
    public Connection createJDBCConnection() throws DDLException {
        if (LOG) {
            LOGGER.log(Level.FINE, "createJDBCConnection()");
        }
        
        if (drv == null || db == null || usr == null )
            throw new DDLException(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_InsufficientConnInfo"));

        Properties dbprops = new Properties();
        if ((usr != null) && (usr.length() > 0)) {
            dbprops.put("user", usr); //NOI18N
            dbprops.put("password", pwd); //NOI18N
        }

        try {
            propertySupport.firePropertyChange("connecting", null, null);

            // For Java Studio Enterprise.
            getOpenConnection().enable();
            startRuntimes();
            
            // hack for Derby
            DerbyConectionEventListener.getDefault().beforeConnect(this);
            
            JDBCDriver useDriver = findJDBCDriver();
            if (useDriver == null) {
                // will be loaded through DriverManager, make sure it is loaded
                Class.forName(drv);
            }

            Connection connection = DbDriverManager.getDefault().getConnection(db, dbprops, useDriver);
            setConnection(connection);
            
            DatabaseUILogger.logConnection(drv);
            
            propertySupport.firePropertyChange("connected", null, null);
            
            // For Java Studio Enterprise.
            getOpenConnection().disable();

            return connection;
        } catch (SQLException e) {
            String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_CannotEstablishConnection"), new String[] {db, drv, e.getMessage()}); // NOI18N

            //commented out for 3.6 release, need to solve for next Studio release
            // hack for Pointbase Network Server
            //            if(drv.equals(PointbasePlus.DRIVER))
            //                if(e.getErrorCode()==PointbasePlus.ERR_SERVER_REJECTED)
            //                    message = MessageFormat.format(bundle.getString("EXC_PointbaseServerRejected"), new String[] {message, db}); // NOI18N

            propertySupport.firePropertyChange("failed", null, null);
            
            // For Java Studio Enterprise.
            getOpenConnection().disable();

            initSQLException(e);
            DDLException ddle = new DDLException(message);
            ddle.initCause(e);
            throw ddle;
        } catch (Exception exc) {
            String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_CannotEstablishConnection"), new String[] {db, drv, exc.getMessage()}); // NOI18N

            propertySupport.firePropertyChange("failed", null, null);

            // For Java Studio Enterprise.
            getOpenConnection().disable();

            DDLException ddle = new DDLException(message);
            ddle.initCause(exc);
            throw ddle;
        }
    }

    public void connect() {
        if (LOG) {
            LOGGER.log(Level.FINE, "connect()");
        }
        
        createConnectTask() ;
    }

    public Task createConnectTask() {
        return RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (drv == null || db == null || usr == null )
                    sendException(new DDLException(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_InsufficientConnInfo")));

                Properties dbprops = new Properties();
                if ( usr.length() > 0 ) {
                    dbprops.put("user", usr); //NOI18N
                }
                if ((pwd != null && pwd.length() > 0)) {
                    dbprops.put("password", pwd); //NOI18N
                }
                
                Connection conn = null;
                try {
                    propertySupport.firePropertyChange("connecting", null, null);
                    
                    // For Java Studio Enterprise.
                    getOpenConnection().enable();

                    // For Java Studio Enterprise.
                    getOpenConnection().enable();
                    startRuntimes();
                    
                    // hack for Derby
                    DerbyConectionEventListener.getDefault().beforeConnect(DatabaseConnection.this);
                    
                    JDBCDriver useDriver = findJDBCDriver();
                    if (useDriver == null) {
                        // will be loaded through DriverManager, make sure it is loaded
                        Class.forName(drv);
                    }
                    
                    conn = DbDriverManager.getDefault().getConnection(db, dbprops, useDriver);
                    setConnection(conn);
                    
                    DatabaseUILogger.logConnection(drv);
                        
                    propertySupport.firePropertyChange("connected", null, null);

                    // For Java Studio Enterprise.
                    getOpenConnection().disable();

                } catch (SQLException e) {
                    String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_CannotEstablishConnection"), new String[] {db, drv, e.getMessage()}); // NOI18N

                    //commented out for 3.6 release, need to solve for next Studio release
                    // hack for Pointbase Network Server
                    //                    if (drv.equals(PointbasePlus.DRIVER))
                    //                        if (e.getErrorCode() == PointbasePlus.ERR_SERVER_REJECTED)
                    //                            message = MessageFormat.format(bundle.getString("EXC_PointbaseServerRejected"), new String[] {message, db}); // NOI18N

                    propertySupport.firePropertyChange("failed", null, null);

                    // For Java Studio Enterprise.
                    getOpenConnection().disable();

                    initSQLException(e);
                    DDLException ddle = new DDLException(message);
                    ddle.initCause(e);
                    sendException(ddle);
                    
                    if (conn != null) {
                        setConnection(null);
                        try {
                            conn.close();
                        } catch (SQLException sqle) {
                            Logger.getLogger("global").log(Level.WARNING, null, sqle); // NOI18N
                        }
                    }
                } catch (Exception exc) {
                    propertySupport.firePropertyChange("failed", null, null);

                    // For Java Studio Enterprise.
                    getOpenConnection().disable();

                    sendException(exc);
                    
                    setConnection(null);
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException sqle) {
                            Logger.getLogger("global").log(Level.WARNING, null, sqle); // NOI18N
                        }
                    }
                }
            }
        }, 0);
    }
    
    /** Calls the initCause() for SQLException with the value
      * of getNextException() so this exception's stack trace contains 
      * the complete data.
      */
    private void initSQLException(SQLException e) {
        SQLException next = e.getNextException();
        while (next != null) {
            try {
                e.initCause(next);
            }
            catch (IllegalStateException e2) {
                // do nothing, already initialized
            }
            e = next;
            next = e.getNextException();
        }
    }

    private void startRuntimes() {
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes(drv);
        
        for (int i = 0; i < runtimes.length; i++) {
            DatabaseRuntime runtime = runtimes[i];
            if (runtime.isRunning()) {
                continue;
            }
            if (runtime.canStart() && runtime.acceptsDatabaseURL(db)) {
                runtime.start();
            }
        }
    }

    public void addExceptionListener(ExceptionListener l) {
        if (l != null)
            exceptionListeners.add(l);
    }

    public void removeExceptionListener(ExceptionListener l) {
        exceptionListeners.remove(l);
    }

    private void sendException(Exception exc) {
        synchronized (exceptionListeners) {
            Iterator it = exceptionListeners.iterator();
            while (it.hasNext()) {
                ExceptionListener l = (ExceptionListener) it.next();
                l.exceptionOccurred(exc);
            }
        }
    }

    public void setConnection(Connection c) {
        con = c;
    }

    public Connection getConnection() {
        return con;
    }

    /** Add property change listener
     * Registers a listener for the PropertyChange event. The connection object
     * should fire a PropertyChange event whenever somebody changes driver, database,
     * login name or password.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove property change listener
     * Remove a listener for the PropertyChange event.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener(l);
    }

    public int hashCode() {
        return drv.hashCode() + db.hashCode() + usr.hashCode();
    }

    /** Compares two connections.
     * Returns true if driver, database and login name equals.
     */
    public boolean equals(Object obj) {
        if (obj instanceof DBConnection) {
            DBConnection con = (DBConnection) obj;
            return toString().equals(con.toString());
        }

        return false;
    }

    /** Reads object from stream */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        drv = (String) in.readObject();
        db = (String) in.readObject();
        usr = (String) in.readObject();
        schema = (String) in.readObject();
        rpwd = Boolean.FALSE;
        name = (String) in.readObject();

        try {
            drvname = (String) in.readObject();
        } catch (Exception exc) {
            //IGNORE - not stored in 3.6 and earlier
        }
        
        // boston setting/pilsen setting?
        if ((name != null) && (name.equals(DatabaseConnection.SUPPORT))) {
            // pilsen
        } else {
            // boston
            schema = null;
        }
        name = null;
        name = getName();
        
        dbconn = DatabaseConnectionAccessor.DEFAULT.createDatabaseConnection(this);
    }

    /** Writes object to stream */
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.writeObject(drv);
        out.writeObject(db);
        out.writeObject(usr);
        out.writeObject(schema);
        out.writeObject(DatabaseConnection.SUPPORT);        
        out.writeObject(drvname);
    }

    public String toString() {
        return "Driver:" + getDriver() + "Database:" + getDatabase().toLowerCase() + "User:" + getUser().toLowerCase() + "Schema:" + getSchema().toLowerCase(); // NOI18N
    }
    
    /**
     * Gets the API DatabaseConnection which corresponds to this connection.
     */
    public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }
    
    public void selectInExplorer() {
        String nodeName = null;
        try {
            nodeName = findConnectionNodeInfo(getName()).getNode().getName();
        } catch (DatabaseException e) {
            Exceptions.printStackTrace(e);
            return;
        }
        
        // find the Runtime panel top component
        // quite hacky, but it will be replaced by the Server Navigator
        
        TopComponent runtimePanel = null;
        ExplorerManager runtimeExplorer = null;
        Node runtimeNode = null;
        
        for (Iterator i = TopComponent.getRegistry().getOpened().iterator(); i.hasNext();) {
            TopComponent component = (TopComponent)i.next();
            Component[] children = component.getComponents();
            if (children.length > 0) {
                ExplorerManager explorer = ExplorerManager.find(children[0]);
                if ("Runtime".equals(explorer.getRootContext().getName())) { // NOI18N
                    runtimePanel = component;
                    runtimeExplorer = explorer;
                    runtimeNode = explorer.getRootContext();
                }
            }
        }
        
        if (runtimePanel == null) {
            return;
        }
            
        Node node = null;
        try {
            node = NodeOp.findPath(runtimeNode, new String[] { "Databases", nodeName }); // NOI18N
        } catch (NodeNotFoundException e) {
            Exceptions.printStackTrace(e);
            return;
        }
        
        try {
            runtimeExplorer.setSelectedNodes(new Node[] { node });
        } catch (PropertyVetoException e) {
            Exceptions.printStackTrace(e);
            return;
        }
    
        runtimePanel.requestActive();
    }
    
    public void showConnectionDialog() {
        try {
            final ConnectionNodeInfo cni = findConnectionNodeInfo(getName());
            if (cni != null && cni.getConnection() == null) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        new ConnectAction.ConnectionDialogDisplayer().showDialog(cni, false);
                    }
                });
            }
        } catch (DatabaseException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public Connection getJDBCConnection() {
        try {
            ConnectionNodeInfo cni = findConnectionNodeInfo(getName());
            if (cni != null && cni.getConnection() != null) {
                return cni.getConnection();
            }
        } catch (DatabaseException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    public void disconnect() throws DatabaseException {
        ConnectionNodeInfo cni = findConnectionNodeInfo(getName());
        if (cni != null && cni.getConnection() != null) {
            cni.disconnect();
        }
    }
    
    private ConnectionNodeInfo findConnectionNodeInfo(String connection) throws DatabaseException {
        assert connection != null;
        
        // Got to retrieve the conn nodes and wait for the "Please wait" node to dissapear.
        // We can't use the info classes here since surprisingly 
        // the CNIs found in RootNode.getInstance().getInfo are different than
        // the ones the ConnectionNodes in the Databases tree listen to
        
        Node[] nodes;
        String waitNode = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("WaitNode"); // NOI18N
        
        for (;;) {
            nodes = RootNode.getInstance().getChildren().getNodes();
            if (nodes.length == 1 && waitNode.equals(nodes[0].getName())) {
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) { 
                    // PENDING
                }
            } else {
                break;
            }
        }
                
        for (int i = 0; i < nodes.length; i++) {
            // Skip nodes registered by node providers
            if ( ! (nodes[i] instanceof DatabaseNode) ) {
                continue;
            }
            
            DatabaseNodeInfo info = (DatabaseNodeInfo)nodes[i].getCookie(DatabaseNodeInfo.class);
            if (info == null) {
                continue;
            }
            ConnectionNodeInfo nfo = (ConnectionNodeInfo)info.getParent(DatabaseNode.CONNECTION);
            if (nfo == null) {
                continue;
            }
            if (connection.equals(nfo.getDatabaseConnection().getName())) {
                return nfo;
            }
        }
        return null;
    }
    
    private Object readResolve() throws ObjectStreamException {
        // sometimes deserialized objects have a null propertySuppport, not sure why
        if (propertySupport == null) {
            propertySupport = new PropertyChangeSupport(this);
        }
        return this;
    }
}
