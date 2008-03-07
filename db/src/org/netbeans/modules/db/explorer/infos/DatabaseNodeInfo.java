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
import java.lang.ref.WeakReference;
import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.DbMetaDataListenerSupport;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;

public class DatabaseNodeInfo extends Hashtable implements Node.Cookie {
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

    // Multi-operation changes are synchronized on this
    private static Map gtab = null;
    static final String gtabfile = "org/netbeans/modules/db/resources/explorer.plist"; //NOI18N

    // Sychronized on this
    private boolean connected = false;

    protected static ResourceBundle bundle() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
    }

    public synchronized static Map getGlobalNodeInfo() {
        if (gtab == null)
            gtab = readInfo();
        
        return gtab;
    }
    
    public static Map readInfo() {
        Map data;
        try {
            ClassLoader cl = DatabaseNodeInfo.class.getClassLoader();
            InputStream stream = cl.getResourceAsStream(gtabfile);
            if (stream == null) {
                String message = MessageFormat.format(bundle().getString("EXC_UnableToOpenStream"), new String[] {gtabfile}); // NOI18N
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

    public static DatabaseNodeInfo createNodeInfo(DatabaseNodeInfo parent, String nodecode) throws DatabaseException {
        DatabaseNodeInfo e_ni = null;
        try {
            String nodec = (String)((Map)DatabaseNodeInfo.getGlobalNodeInfo().get(nodecode)).get(INFOCLASS);
            if (nodec != null)
                e_ni = (DatabaseNodeInfo)Class.forName(nodec).newInstance();
            else {
                String message = MessageFormat.format(bundle().getString("EXC_UnableToFindClassInfo"), new String[] {nodecode}); // NOI18N
                throw new Exception(message);
            }
        } catch (Exception exc) {
            throw new DatabaseException(exc.getMessage());
        }

        if (e_ni != null)
            e_ni.setParentInfo(parent, nodecode);
        else {
            String message = MessageFormat.format(bundle().getString("EXC_UnableToCreateNodeInfo"), new String[] {nodecode}); // NOI18N
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

    /* Owning node */
    WeakReference nodewr = null;
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
        if (ltab != null)
            putAll(ltab);
        else {
            String message = MessageFormat.format(bundle().getString("EXC_UnableToReadInfo"), new String[] {sname}); // NOI18N
            throw new DatabaseException(message);
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

    public DatabaseNode getNode()
    {
        if (nodewr != null) return (DatabaseNode)nodewr.get();
        return null;
    }

    public void setNode(DatabaseNode node)
    {
        nodewr = new WeakReference(node);
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

    public Object put(Object key, Object obj)
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
        Vector charr = new Vector();
        put(DatabaseNodeInfo.CHILDREN, charr);
        initChildren(charr);
        
        refreshNodes(charr);        
    }
    
    protected void refreshNodes(Vector charr) {
                // create sub-tree (by infos)
        try {
            final Node[] subTreeNodes = new Node[charr.size()];
            
            // current sub-tree
            final DatabaseNodeChildren children = (DatabaseNodeChildren) getNode().getChildren();
            
            // build refreshed sub-tree
            for(int i = 0; i < charr.size(); i++) {
                Object child = charr.elementAt(i);
                if ( child instanceof DatabaseNodeInfo ) {
                    subTreeNodes[i] = 
                            children.createNode((DatabaseNodeInfo)child);
                } else if ( child instanceof Node ) {
                    subTreeNodes[i] = (Node)child;
                } else {
                    throw new ClassCastException(child.getClass().getName());
                }
            }

            children.replaceNodes(subTreeNodes);
            
            fireRefresh();
        } catch (ClassCastException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }

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
        DatabaseSpecification spec = (DatabaseSpecification)get(SPECIFICATION);
        if (spec == null) return spec;
        String adaname = getDatabaseAdaptorClassName();
        if (!spec.getMetaDataAdaptorClassName().equals(adaname)) {
            spec.setMetaDataAdaptorClassName(adaname);
        }

        return spec;
    }

    public void setSpecification(DatabaseSpecification spec)
    {
        put(SPECIFICATION, spec);
    }

    public String getDriver()
    {
        return (String)get(DRIVER);
    }

    public void setDriver(String drv)
    {
        put(DRIVER, drv);
    }

    public Connection getConnection()
    {
        return (Connection)get(CONNECTION);
    }

    public void setConnection(Connection con) throws DatabaseException
    {
        Connection oldval = getConnection();
        if (con != null) {
            if (oldval != null && oldval.equals(con)) return;
            put(CONNECTION, con);
            setConnected(true);
        } else {
            remove(CONNECTION);   
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

    }

    public synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public synchronized boolean isConnected() {
        return connected;
    }

    public DatabaseConnection getDatabaseConnection()
    {
        DatabaseConnection con = new DatabaseConnection(getDriver(), getDatabase(), getUser(), getPassword());
        if(get(REMEMBER_PWD)!=null) {
            con.setRememberPassword(((Boolean)get(REMEMBER_PWD)).booleanValue());
        }
        else
            con.setRememberPassword(Boolean.FALSE.booleanValue());
        con.setSchema(getSchema());
        con.setDriverName((String)get("drivername"));
        return con;
    }

    public DatabaseDriver getDatabaseDriver()
    {
        return (DatabaseDriver)get(DBDRIVER);
    }

    public void setDatabaseConnection(DBConnection cinfo)
    {
        String pwd = cinfo.getPassword();
        put(DRIVER, cinfo.getDriver());
        put(DATABASE, cinfo.getDatabase());
        put(USER, cinfo.getUser());
        put(SCHEMA, cinfo.getSchema());
        if (pwd != null)
            put(PASSWORD, pwd);
        put(REMEMBER_PWD, (Boolean.valueOf(cinfo.rememberPassword())));
        put("drivername", cinfo.getDriverName());
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
    }

    public String getDatabase()
    {
        return (String)get(DATABASE);
    }

    public void setDatabase(String db)
    {
        put(DATABASE, db);
    }

    public String getPassword()
    {
        return (String)get(PASSWORD);
    }

    public void setPassword(String pwd)
    {
        put(PASSWORD, pwd);
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
    }

    public String getIconBase() {
        return (String) get("iconbase"); //NOI18N
    }

    public void setIconBase(String base) {
        put("iconbase", base); //NOI18N
    }

    public String getDisplayname()
    {
        return (String)get("displayname"); //NOI18N
    }

    public void setDisplayname(String name)
    {
        put("displayname", name); //NOI18N
    }

    public String getURL()
    {
        return (String)get(URL);
    }

    public void setURL(String url)
    {
        put(URL, url);
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

    public synchronized Vector getChildren()
    throws DatabaseException
    {
        Vector children = (Vector)get(CHILDREN);
        if (children.size() > 0 && 
                (children.elementAt(0) instanceof DatabaseNodeInfo ||
                 children.elementAt(0) instanceof Node) ) {
            return children;
        }

        Vector chalt = new Vector();
        initChildren(chalt);
        chalt.addAll(children);

        try {
            for (int i=0; i<chalt.size();i++) {
                Object e_child = chalt.elementAt(i);
                if (e_child instanceof String) {
                    DatabaseNodeInfo e_ni = createNodeInfo(this, (String)e_child);
                    chalt.setElementAt(e_ni,i);
                }
            }

            children = chalt;
            put(CHILDREN, children);

        } catch (Exception exc) {
            String message = MessageFormat.format(bundle().getString("EXC_UnableToCreateChildren"), new String[] {exc.getMessage()}); // NOI18N
            throw new DatabaseException(message);
        }

        return children;
    }

    public void setChildren(Vector chvec)
    {
        put(CHILDREN, chvec);
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
                            
                            String message = MessageFormat.format(bundle().getString("ERR_UnableToLocateLocalizedMenuItem"), new String[] {xname}); // NOI18N
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
        return RootNode.getOption().getDebugMode();
    }

    public void setDebugMode(boolean mode)
    {
        RootNode.getOption().setDebugMode(mode);
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
    }

    /** Getter for property driverSpecification.
     *@return Value of property driverSpecification.
     */
    public DriverSpecification getDriverSpecification() {
        return (DriverSpecification) get(DRIVER_SPECIFICATION);
    }

    /** Setter for property driverSpecification.
     *@param driverSpecification New value of property driverSpecification.
     */
    public void setDriverSpecification(DriverSpecification driverSpecification) {
        put(DRIVER_SPECIFICATION, driverSpecification);
    }

}
