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

package org.netbeans.modules.db.explorer.infos;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.lib.ddl.DatabaseSpecificationFactory;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.DbMetaDataListenerSupport;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.util.UIUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;

public class DatabaseNodeInfo extends ConcurrentHashMap<String, Object>
        implements Node.Cookie, Comparable {
    public static final String SPECIFICATION_FACTORY = "specfactory"; //NOI18N
    public static final String SPECIFICATION = "spec"; //NOI18N
    public static final String DRIVER_SPECIFICATION = "drvspec"; //NOI18N
    public static final String DBPRODUCT = "dbproduct"; //NOI18N
    public static final String DBVERSION = "dbversion"; //NOI18N
    public static final String SUPPORTED_DBS = "suppdbs"; //NOI18N
    public static final String DRIVER = "driver"; //NOI18N
    public static final String DBDRIVER = "dbdriver"; //NOI18N
    public static final String DATABASE = "db"; //NOI18N
    public static final String URL = "url"; //NOI18N
    public static final String PREFIX = "prefix"; //NOI18N
    public static final String DATABASE_CONNECTION = "database_connection"; //NOI18N
    public static final String CONNECTION = "connection"; //NOI18N
    public static final String CODE = "code"; //NOI18N
    public static final String NODE = "node"; //NOI18N
    public static final String CLASS = "class"; //NOI18N
    public static final String INFOCLASS = "infoclass"; //NOI18N
    public static final String NAME = "name"; //NOI18N
    public static final String USER = "user"; //NOI18N
    public static final String SCHEMA = "schema"; //NOI18N
    public static final String PASSWORD = "password"; //NOI18N
    public static final String CHILDREN = "children"; //NOI18N
    public static final String ACTIONS = "actions"; //NOI18N
    public static final String ICONBASE = "iconbase"; //NOI18N
    public static final String PROPERTIES = "properties"; //NOI18N
    public static final String RESULTSET = "resultset"; //NOI18N
    public static final String REMEMBER_PWD = "rememberpwd"; //NOI18N
    public static final String WRITABLE = "writable"; //NOI18N
    public static final String DELETABLE = "deletable"; //NOI18N
    public static final String DESCRIPTION = "description"; //NOI18N
    public static final String READONLYDB = "readonlydatabase"; //NOI18N
    public static final String GROUPSUP = "groupbysupport"; //NOI18N
    public static final String OJOINSUP = "outerjoinsupport"; //NOI18N
    public static final String UNIONSUP = "unionsupport"; //NOI18N
    public static final String SYSTEM_ACTION = "system"; //NOI18N
    public static final String CHILDREN_ORDERING = "children_ordering"; //NOI18N
    public static final String READONLY = "readOnly"; //NOI18N
    public static final String PERM = "perm"; //NOI18N
    public static final String ADAPTOR = "adaptor"; //NOI18N
    public static final String ADAPTOR_CLASSNAME = "adaptorClass"; //NOI18N
    public static final String REGISTERED_NODE = "registered"; // NOI18N

    // Multi-operation changes are synchronized on this
    private static Map gtab = null;
    static final String gtabfile = "org/netbeans/modules/db/resources/explorer.plist"; //NOI18N

    // Sychronized on this
    private boolean connected = false;

    // Thread-safe, no synchronization
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public synchronized static Map getGlobalNodeInfo() {
        if (gtab == null)
            gtab = readInfo();
        
        return gtab;
    }

    private final UUID uuid =   UUID.randomUUID();
    private static final Logger LOGGER = Logger.getLogger(DatabaseNodeInfo.class.getName());
    
    public static Map readInfo() {
        Map data;
        try {
            ClassLoader cl = DatabaseNodeInfo.class.getClassLoader();
            InputStream stream = cl.getResourceAsStream(gtabfile);
            if (stream == null) {
                String message = MessageFormat.format(bundle().getString("EXC_UnableToOpenStream"),gtabfile); // NOI18N
                throw new Exception(message);
            }
            PListReader reader = new PListReader(stream);
            data = reader.getData();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            data = null;
        }
        
        return data;
    }

    public static Object getGlobalNodeInfo(String key) {
        return getGlobalNodeInfo().get(key);
    }

    public static DatabaseNodeInfo createNodeInfo(DatabaseNodeInfo parent, 
            String nodecode) throws DatabaseException {
        DatabaseNodeInfo e_ni = null;
        
        String nodec = (String)((Map)DatabaseNodeInfo.getGlobalNodeInfo().get(nodecode)).get(INFOCLASS);
        if (nodec != null) {
            try {
                e_ni = (DatabaseNodeInfo)Class.forName(nodec).newInstance();
            } catch ( Exception e ) {
                throw new DatabaseException(e);
            }
        } else {
            String message = MessageFormat.format(bundle().
                    getString("EXC_UnableToFindClassInfo"), 
                    nodecode); // NOI18N
            throw new DatabaseException(message);
        }

        if (e_ni != null) {
            e_ni.setParentInfo(parent, nodecode);
        } else {
            String message = MessageFormat.format(bundle().getString("EXC_UnableToCreateNodeInfo"), nodecode); // NOI18N
            throw new DatabaseException(message);
        }
        return e_ni;
    }
    
    public static DatabaseNodeInfo createNodeInfo(DatabaseNodeInfo parent, String nodecode, HashMap rset) throws DatabaseException {
        int colidx = 1;
        String key = null;
        DatabaseNodeInfo nfo = createNodeInfo(parent, nodecode);
        Vector rsnames = (Vector)nfo.get(DatabaseNodeInfo.RESULTSET);
        Iterator rsnames_i = rsnames.iterator();
        Hashtable data = new Hashtable();
        while (rsnames_i.hasNext()) {
            key = (String)rsnames_i.next();
            if (!key.equals("unused")) { //NOI18N
                Object value = rset.get(new Integer(colidx));
                if (value != null) data.put(key, value);
            }
            colidx++;
        }

        nfo.putAll(data);
        nfo.put(nodecode, nfo.getName());
        if (parent != null && parent.isReadOnly()) nfo.setReadOnly(true);
        return nfo;
    }

    /* Parent of info in node hierarchy */
    private DatabaseNodeInfo parent = null;

    private PropertyChangeSupport pcs = null;
    private Set connectionpcsKeys = null;

    private PropertyChangeSupport driverpcs = null;
    private Set driverpcsKeys = null;

    static final long serialVersionUID =1176243907461868244L;
    public DatabaseNodeInfo()
    {
        super();
    }

    public DatabaseNodeInfo(DatabaseNodeInfo parent, String sname)
    throws DatabaseException
    {
        DatabaseNodeInfo nfo = new DatabaseNodeInfo();
        nfo.setParentInfo(parent, sname);
    }

    public void setParentInfo(DatabaseNodeInfo parent, String sname)
    throws DatabaseException
    {
        if (parent != null) {
            putAll(parent);
            this.parent = parent;
        }
        Map ltab = (Map)getGlobalNodeInfo(sname);
        if (ltab != null) {
            putAll(ltab);
        }
        put(CODE, sname);
        if (parent != null && parent.isReadOnly()) setReadOnly(true);
    }

    public DatabaseNodeInfo getParent()
    {
        return parent;
    }

    /** Returns parent of nodeinfo defined by <code>parent</code> variable.
    * If no info was found, it returns null.
    */
    public DatabaseNodeInfo getParent(String code)
    {
        DatabaseNodeInfo iinfo = this;
        if (code != null) {
            while (iinfo != null) {
                String iicode = iinfo.getCode();
                if (iicode.equals(code)) return iinfo;
                else iinfo = iinfo.getParent();
            }
        }

        return iinfo;
    }

    public boolean canAdd(Map propmap, String propname)
    {
        return true;
    }

    public boolean canWrite(Map propmap, String propname, boolean defa)
    {
        if (isReadOnly()) return false;
        String wflag = (String)propmap.get(DatabaseNodeInfo.WRITABLE);
        if (wflag != null) return wflag.toUpperCase().equals("YES"); //NOI18N
        return defa;
    }

    private  PropertyChangeSupport getConnectionPCS()
    {
        if (pcs == null)
            pcs = new PropertyChangeSupport(this);

        return pcs;
    }

    /** Returns PropertyChangeSupport used for driver change monitoring */
    private PropertyChangeSupport getDriverPCS()
    {
        if (driverpcs == null)
            driverpcs = new PropertyChangeSupport(this);

        return driverpcs;
    }

    /** Returns PropertyChangeSupport used for driver change monitoring */
    private Set getDriverPCSKeys()
    {
        if (driverpcsKeys == null) {
            driverpcsKeys = new HashSet();
            driverpcsKeys.add(NAME);
            driverpcsKeys.add(URL);
            driverpcsKeys.add(PREFIX);
            driverpcsKeys.add(ADAPTOR_CLASSNAME);
        }

        return driverpcsKeys;
    }

    /** Returns PropertyChangeSupport used for connection change monitoring */
    private  Set getConnectionPCSKeys()
    {
        if (connectionpcsKeys == null) {
            connectionpcsKeys = new HashSet();
            connectionpcsKeys.add(SCHEMA);
            connectionpcsKeys.add(USER);
            connectionpcsKeys.add(DATABASE);

        }

        return connectionpcsKeys;
    }
    
    @Override
    public Object put(String key, Object obj)
    {
        Object old = get(key);
        
        if (key == null)
            throw new NullPointerException();

        if (obj != null)
            super.put(key, obj);
        else
            remove(key);

        if (getDriverPCSKeys().contains(key)){
            getDriverPCS().firePropertyChange((String)key, old, obj);
        }
        if (getConnectionPCSKeys().contains(key))
            getConnectionPCS().firePropertyChange((String)key, null, obj);

        return old;
    }

    public void delete() throws IOException
    {
    }

    public void refreshChildren() throws DatabaseException {
        // create list (infos)
        Vector children = loadChildren(new Vector());

        put(DatabaseNodeInfo.CHILDREN, children);
        
        notifyChange();
    }
    
    public String getShortDescription() {
        return ""; // NOI18N
    }
    

    protected void fireRefresh() {
        boolean allTables = true;

        if (!(this instanceof TableListNodeInfo)) {
            allTables = false;
            if (!(this instanceof TableNodeInfo)) {
                return;
            }
        }

        ConnectionNodeInfo cnnfo = (ConnectionNodeInfo)getParent(DatabaseNode.CONNECTION);
        if (cnnfo == null) {
            return;
        }

        DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cnnfo.getDatabaseConnection());
        if (dbconn != null) {
            if (allTables) {
                DbMetaDataListenerSupport.fireTablesChanged(dbconn.getDatabaseConnection());
            } else {
                String tableName = (String)get(DatabaseNode.TABLE);
                DbMetaDataListenerSupport.fireTableChanged(dbconn.getDatabaseConnection(), tableName);
            }
        }
    }

    /** Called by property editor */
    public Object getProperty(String key)
    {
        return get(key);
    }

    /** Called by property editor */
    public void setProperty(String key, Object obj)
    {
        put(key, obj);
        notifyChange();
    }

    /** Add property change listener */
    public void addConnectionListener(PropertyChangeListener l)
    {
        getConnectionPCS().addPropertyChangeListener(l);
    }

    /** Remove property change listener */
    public void removeConnectionListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    public void addDriverListener(PropertyChangeListener l)
    {
        getDriverPCS().addPropertyChangeListener(l);
    }

    public void removeDriverListener(PropertyChangeListener l)
    {
        getDriverPCS().removePropertyChangeListener(l);
    }

    public DatabaseSpecificationFactory getSpecificationFactory()
    {
        return (DatabaseSpecificationFactory)get(SPECIFICATION_FACTORY);
    }

    public void setSpecificationFactory(DatabaseSpecificationFactory fac)
    {
        put(SPECIFICATION_FACTORY, fac);
        put(SUPPORTED_DBS, fac.supportedDatabases());
    }

    protected String getDatabaseAdaptorClassName() {
        return "org.netbeans.lib.ddl.adaptors.DefaultAdaptor"; //NOI18N
    }

    public DatabaseSpecification getSpecification()
    {
        DatabaseSpecification spec = (DatabaseSpecification)getParent(CONNECTION).get(SPECIFICATION);
        if (spec == null) return spec;
        String adaname = getDatabaseAdaptorClassName();
        if (!spec.getMetaDataAdaptorClassName().equals(adaname)) {
            spec.setMetaDataAdaptorClassName(adaname);
        }

        return spec;
    }

    public void setSpecification(DatabaseSpecification spec)
    {
        getParent(CONNECTION).put(SPECIFICATION, spec);
    }

    public String getDriver()
    {
        return (String)get(DRIVER);
    }

    public void setDriver(String drv)
    {
        put(DRIVER, drv);
        notifyChange();
    }

    public Connection getConnection()
    {
        return (Connection)getParent(CONNECTION).get(CONNECTION);
    }

    public void setConnection(Connection con) throws DatabaseException
    {
        Connection oldval = getConnection();
        if (con != null) {
            if (oldval != null && oldval.equals(con)) return;
            getParent(CONNECTION).put(CONNECTION, con);
            setConnected(true);
        } else {
            getParent(CONNECTION).remove(CONNECTION);
            setConnected(false);
        }

        // Check if node is readonly or not.

        if (con != null && isReadOnly()) {
            Enumeration enu = getChildren().elements();
            while(enu.hasMoreElements()) {
                DatabaseNodeInfo ninfo = (DatabaseNodeInfo)enu.nextElement();
                ninfo.setReadOnly(true);
            }
        }

        getConnectionPCS().firePropertyChange(CONNECTION, oldval, con);
        notifyChange();
    }

    private synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public synchronized boolean isConnected() {
        if (this instanceof ConnectionNodeInfo) {
            return this.connected;
        } else {
            ConnectionNodeInfo cinfo = (ConnectionNodeInfo)getParent(CONNECTION);
            return cinfo.isConnected();
        }
    }
    /**
     * Make sure this is a valid connection, and if it's not, try
     * to reconnect.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException if we're
     *
     */
    public boolean ensureConnected() throws DatabaseException {
        final org.netbeans.api.db.explorer.DatabaseConnection dbconn = getDatabaseConnection().getDatabaseConnection();

        assert(dbconn != null);

        Connection conn = dbconn.getJDBCConnection(true);

        if (conn == null) {
            Mutex.EVENT.readAccess(new Action() {
                public Object run() {
                    boolean connect = UIUtils.displayYesNoDialog(NbBundle.getMessage(ConnectionNodeInfo.class, "MSG_ConnectionLost"));
                    if (connect) {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    }
                    return null;
                }
            });
        }

        conn = dbconn.getJDBCConnection();

        return (conn != null);
    }

    public DatabaseConnection getDatabaseConnection()
    {
        return (DatabaseConnection) get(DATABASE_CONNECTION);
    }

    public DatabaseDriver getDatabaseDriver()
    {
        return (DatabaseDriver)get(DBDRIVER);
    }

    public void setDatabaseConnection(DatabaseConnection cinfo)
    {
        put(DATABASE_CONNECTION, cinfo);
        String pwd = cinfo.getPassword();
        put(DRIVER, cinfo.getDriver());
        put(DATABASE, cinfo.getDatabase());
        put(USER, cinfo.getUser());
        put(SCHEMA, cinfo.getSchema());
        if (pwd != null)
            put(PASSWORD, pwd);
        put(REMEMBER_PWD, (Boolean.valueOf(cinfo.rememberPassword())));
        put("drivername", cinfo.getDriverName());
        notifyChange();
    }

    public String getCode()
    {
        return (String)get(CODE);
    }

    public void setCode(String nam)
    {
        put(CODE, nam);
    }

    public String getName()
    {
        return (String)get(NAME);
    }

    public void setName(String nam)
    {
        put(NAME, nam);
        notifyChange();
    }

    public String getUser()
    {
        return (String)get(USER);
    }

    public void setUser(String usr)
    {
        put(USER, usr);
    }
    
    public String getSchema() {
        return (String) get(SCHEMA);
    }

    public void setSchema(String schema) {
        put(SCHEMA, schema);
        notifyChange();
    }

    public String getDatabase()
    {
        return (String)get(DATABASE);
    }

    public void setDatabase(String db)
    {
        put(DATABASE, db);
        notifyChange();
    }

    public String getPassword()
    {
        return (String)get(PASSWORD);
    }

    public void setPassword(String pwd)
    {
        put(PASSWORD, pwd);
        notifyChange();
    }

    public String getTable()
    {
        return (String)get("table"); //NOI18N
    }

    public String getView()
    {
        return (String)get("view"); //NOI18N
    }

    public void setTable(String nam)
    {
        put("table", nam); //NOI18N
        notifyChange();
    }

    public String getIconBase() {
        return (String) get("iconbase"); //NOI18N
    }

    public void setIconBase(String base) {
        put("iconbase", base); //NOI18N
        notifyChange();
    }

    public String getDisplayName()
    {
        String dname = (String)get("displayname");
        if ( dname == null || "{name}".equals(dname)) {
            return getName();
        } else {
            return dname;
        }
    }

    public void setDisplayName(String name)
    {
        put("displayname", name); //NOI18N
        notifyChange();
    }

    public String getURL()
    {
        return (String)get(URL);
    }

    public void setURL(String url)
    {
        put(URL, url);
        notifyChange();
    }

    /** Returns connection properties (login name and password)
    * Returns null if name or password isn't specified yet.
    */
    public Properties getConnectionProperties()
    {
        Properties props = new Properties();
        try {
            props.put("user", getUser()); //NOI18N
            props.put("password", getPassword()); //NOI18N
            props.put("schema", getSchema()); //NOI18N
        } catch (Exception e) { props = null; }

        return props;
    }

    protected void initChildren(Vector children)
    throws DatabaseException
    {
    }
    
    public void resetChildren() throws DatabaseException {
        Vector children = new Vector();
        initChildren(children);
        put(CHILDREN, children);
    }
    
    public boolean isChildrenInitialized() {
        Vector children = (Vector)get(CHILDREN);

        return children.size() > 0 && 
                children.elementAt(0) instanceof DatabaseNodeInfo;        
    }

    public Vector getChildren()
    throws DatabaseException
    {
        Vector children = (Vector)get(CHILDREN);
        
        if ( isChildrenInitialized() ) {
            return children;
        }

        Vector chalt = loadChildren(children);

        put(CHILDREN, chalt);

        // Do NOT notify change here, as this is called by the stateChanged()
        // method in the Node, we'd end up in an endless loop
        
        return chalt;
    }
    
    public Vector loadChildren(Vector initialList) throws DatabaseException {
        Vector chalt = new Vector();
        initChildren(chalt);
        chalt.addAll(initialList);

        for (int i=0; i<chalt.size();i++) {
            Object e_child = chalt.elementAt(i);
            if (e_child instanceof String) {
                DatabaseNodeInfo e_ni = createNodeInfo(this, (String)e_child);
                chalt.setElementAt(e_ni,i);
            }
        }

        return chalt;
    }
    
    // For debugging
    public static void printChildren(String message, Vector children) {
        System.out.println("");
        System.out.println(message);
        for ( Object child : children ) {
            StringBuffer childstr = new StringBuffer(child.getClass().getName());
            
            if ( child instanceof DatabaseNodeInfo ) {
                childstr.append(": " + ((DatabaseNodeInfo)child).getDisplayName());
            } else {
                childstr.append(": " + child.toString());
            }

            childstr.append(": " + child.hashCode());
            
            System.out.println(childstr.toString());            
        }
        
    }
    
    public void addChild(DatabaseNodeInfo child) throws DatabaseException {
        addChild(child, true);
    }

    public void addChild(DatabaseNodeInfo child, boolean notify)
            throws DatabaseException {
        getChildren().add(child);

        if ( notify ) {
            notifyChange();
        }
    }
        
    public void removeChild(DatabaseNodeInfo child) throws DatabaseException {
        removeChild(child, true);
    }

    public synchronized void removeChild(DatabaseNodeInfo child, boolean notify)
            throws DatabaseException {
        getChildren().remove(child);

        if ( notify ) {
            notifyChange();
        }
    }

    public Vector getActions()
    {
        Vector actions = (Vector)get(ACTIONS);
        actions = (Vector)actions.clone();
        if (actions == null) {
            actions = new Vector();
            put(ACTIONS, actions);
        }

        if (actions.size() == 0) return actions;
        boolean ro = isReadOnly();
        for (int i=0; i<actions.size();i++) {

            Object e_act = actions.elementAt(i);
            SystemAction action = null;
            if (e_act instanceof Map) {
                Map e_action = (Map)e_act;
                try {

                    // Try permissions

                    String perm = (String)e_action.get(PERM);
                    if (ro && perm != null && perm instanceof String && perm.indexOf("write") != -1) { //NOI18N
                        actions.setElementAt(null, i);
                        continue;
                    }

                    boolean systemact = false;
                    String sysactstr = (String)e_action.get(SYSTEM_ACTION);
                    if (sysactstr != null) systemact = sysactstr.toUpperCase().equals("YES"); //NOI18N
                    String actnode = (String)e_action.get(NODE);
                    String actcn = (String)e_action.get(CLASS);

                    if (!systemact) {
                        String locname, xname = (String)e_action.get(NAME);
                        try {
                            locname = bundle().getString(xname);
                        } catch (MissingResourceException e) {
                            locname = xname;
                            
                            String message = MessageFormat.format(bundle().getString("ERR_UnableToLocateLocalizedMenuItem"), xname); // NOI18N
                            System.out.println(message);
                        }

                        //action = (SystemAction)Class.forName(actcn).newInstance();
                        //action = (SystemAction)SharedClassObject.findObject(Class.forName(actcn), true);
                        action = SystemAction.get(Class.forName(actcn).asSubclass(SystemAction.class));
                        ((DatabaseAction)action).setName(locname);
                        ((DatabaseAction)action).setNode(actnode);
                    } else {
                        ClassLoader l = (ClassLoader)org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                        if (l == null) {
                            l = getClass().getClassLoader();
                        }
                        action = SystemAction.get(Class.forName(actcn, true, l).asSubclass(SystemAction.class));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            actions.setElementAt(action, i);
        }

        return actions;
    }
    
    @Override
    public String toString()
    {
        String result = ""; //NOI18N
        Enumeration keys = keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            result = result + key+": "+get(key)+ "\n"; //NOI18N
        }

        return result;
    }

    public boolean isDebugMode()
    {
        return RootNodeInfo.getOption().getDebugMode();
    }

    public void setDebugMode(boolean mode)
    {
        RootNodeInfo.getOption().setDebugMode(mode);
    }

    public boolean isReadOnly()
    {
        Boolean roobj = (Boolean)get(READONLY);
        if (roobj != null) return roobj.booleanValue();
        return false;
    }

    public void setReadOnly(boolean flag)
    {
        put(READONLY, flag ? Boolean.TRUE : Boolean.FALSE);
        notifyChange();
    }
    
    /** Getter for property driverSpecification.
     *@return Value of property driverSpecification.
     */
    public DriverSpecification getDriverSpecification() {
        return (DriverSpecification) getParent(CONNECTION).get(DRIVER_SPECIFICATION);
    }

    /** Setter for property driverSpecification.
     *@param driverSpecification New value of property driverSpecification.
     */
    public void setDriverSpecification(DriverSpecification driverSpecification) {
        getParent(CONNECTION).put(DRIVER_SPECIFICATION, driverSpecification);
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    public void notifyChange() {
        changeSupport.fireChange();
    }
    
    protected static ResourceBundle bundle() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); // NOI18N

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DatabaseNodeInfo other = (DatabaseNodeInfo) obj;
        if (this.uuid != other.uuid && (this.uuid == null || !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (uuid != null ? uuid.hashCode() : 0);
        return hash;
    }


    
    public int compareTo(Object o2) {
        // It's an exception if this isn't a DatabaseNodeInfo...
        DatabaseNodeInfo info2 = (DatabaseNodeInfo)o2;
        
        if ( this.equals(info2)) {
            return 0;
        }        

        java.util.Map map = null;
        if ( parent != null ) {
            map = (java.util.Map)parent.get(DatabaseNodeInfo.CHILDREN_ORDERING);
        }
        
        if ( map != null ) {
            boolean sort = 
                ((! (this instanceof TableNodeInfo)) && 
                (! (this instanceof ViewNodeInfo)) && 
                (! (this instanceof ProcedureNodeInfo)) ); //NOI18N

            if (sort)
            {
                // Ordering is based on the node class for this info class
                // See if the node class is in the ordering map, and if it
                // is, use that for comparison.
                int o1val, o2val, diff;
                Integer o1i = (Integer)map.get(get(CLASS));
                if (o1i != null)
                    o1val = o1i.intValue();
                else
                    o1val = Integer.MAX_VALUE;

                Integer o2i = null;
                o2i = (Integer)map.get(info2.get(CLASS));

                if (o2i != null)
                    o2val = o2i.intValue();
                else
                    o2val = Integer.MAX_VALUE;

                diff = o1val-o2val;

                // If they're the same class, then sort using display name
                // below...
                if (diff != 0) {
                    return diff;
                }
            }
        }
        
        return getDisplayName() != null ?
            getDisplayName().compareTo(info2.getDisplayName()) :
            1;
    }

    

}
