/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db.explorer.infos;

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.sql.*;
import com.netbeans.ddl.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import com.netbeans.ddl.util.PListReader;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

public class DatabaseNodeInfo extends Hashtable implements Node.Cookie
{
	public static final String SPECIFICATION_FACTORY = "specfactory";
	public static final String SPECIFICATION = "spec";
	public static final String DBPRODUCT = "dbproduct";
	public static final String DBVERSION = "dbversion";	
	public static final String SUPPORTED_DBS = "suppdbs";
	public static final String DRIVER = "driver";
	public static final String DBDRIVER = "dbdriver";
	public static final String DATABASE = "db";
	public static final String URL = "url";
	public static final String CONNECTION = "connection";
	public static final String CODE = "code";
	public static final String NODE = "node";
	public static final String CLASS = "class";
	public static final String INFOCLASS = "infoclass";
	public static final String NAME = "name";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String CHILDREN = "children";
	public static final String ACTIONS = "actions";
	public static final String ICONBASE = "iconbase";
	public static final String PROPERTIES = "properties";
	public static final String RESULTSET = "resultset";
	public static final String REMEMBER_PWD = "rememberpwd";
	public static final String WRITABLE = "writable";
	public static final String DELETABLE = "deletable";
	public static final String DESCRIPTION = "description";
	public static final String READONLYDB = "readonlydatabase";
	public static final String GROUPSUP = "groupbysupport";
	public static final String OJOINSUP = "outerjoinsupport";
	public static final String UNIONSUP = "unionsupport";
	public static final String SYSTEM_ACTION = "system";
	public static final String CHILDREN_ORDERING = "children_ordering";
	
	private static Map gtab = null;
	private static final String gtabfile = "com/netbeans/enterprise/modules/db/resources/explorer.plist";
	
	public static Map getGlobalNodeInfo()
	{
		if (gtab == null) try {
			ClassLoader cl = DatabaseNodeInfo.class.getClassLoader();
			InputStream stream = cl.getResourceAsStream(gtabfile);
			if (stream == null) throw new Exception("unable to open stream "+gtabfile);
			PListReader reader = new PListReader(stream);
			gtab = reader.getData();
			stream.close();		
		} catch (Exception e) {
			System.out.println(e); 
			gtab = null;
		}
		return gtab;
	}

	public static Object getGlobalNodeInfo(String key)
	{
		return getGlobalNodeInfo().get(key);
	}

	public static DatabaseNodeInfo createNodeInfo(DatabaseNodeInfo parent, String nodecode)
	throws DatabaseException
	{
		DatabaseNodeInfo e_ni = null;
		try {
			String nodec = (String)((Map)DatabaseNodeInfo.getGlobalNodeInfo().get(nodecode)).get(INFOCLASS);
			if (nodec != null) e_ni = (DatabaseNodeInfo)Class.forName(nodec).newInstance();
			else throw new Exception("unable to find class info for "+nodecode);
		} catch (Exception e) { 
			System.out.println(e);
			throw new DatabaseException(e.getMessage()); 
		}
		
		if (e_ni != null) e_ni.setParentInfo(parent, nodecode);	
		else throw new DatabaseException("unable to create node info "+nodecode);
		return e_ni;
	}

	public static DatabaseNodeInfo createNodeInfo(DatabaseNodeInfo parent, String nodecode, ResultSet rset)
	throws DatabaseException
	{
		int colidx = 1;
		String key = null;
		DatabaseNodeInfo nfo = createNodeInfo(parent, nodecode);
		Vector rsnames = (Vector)nfo.get(DatabaseNodeInfo.RESULTSET);
		Iterator rsnames_i = rsnames.iterator();
		try {
			Hashtable data = new Hashtable();
			while (rsnames_i.hasNext()) {
				key = (String)rsnames_i.next();
				if (!key.equals("unused")) {
					Object value = rset.getObject(colidx);
					if (value != null) data.put(key, value);
				}
				colidx++;
			}
			nfo.putAll(data);
			nfo.put(nodecode, nfo.getName());
		} catch (Exception e) {
			throw new DatabaseException("unable to read key "+key+" from result set, "+e.getMessage());
		}
			
		return nfo;
	}		

	/* Parent of info in node hierarchy */
	private DatabaseNodeInfo parent = null;
	
	/* Owning node */
	WeakReference nodewr = null;
	private PropertyChangeSupport pcs = null;
		
	private PropertyChangeSupport driverpcs = null;
	private Set driverpcsKeys = null;

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
		if (ltab != null) putAll(ltab);
		else throw new DatabaseException("unable to read nfo for "+sname);
		put(CODE, sname);
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
		String wflag = (String)propmap.get(DatabaseNodeInfo.WRITABLE);
		if (wflag != null) return wflag.toUpperCase().equals("YES");
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

	private PropertyChangeSupport getConnectionPCS()
	{
		if (pcs == null) {
			pcs = new PropertyChangeSupport(this);
		}
		
		return pcs;
	}

	/** Returns PropertyChangeSupport used for driver change monitoring */
	private PropertyChangeSupport getDriverPCS()
	{
		if (driverpcs == null) {
			driverpcs = new PropertyChangeSupport(this);
		}
		
		return driverpcs;
	}

	/** Returns PropertyChangeSupport used for driver change monitoring */
	private Set getDriverPCSKeys()
	{
		if (driverpcsKeys == null) {
			driverpcsKeys = new HashSet();
			driverpcsKeys.add(NAME);
			driverpcsKeys.add(URL);
		}
		
		return driverpcsKeys;
	}
		
	public Object put(Object key, Object obj)
	{
		Object old = get(key);
		super.put(key, obj);
		if (getDriverPCSKeys().contains(key)) getDriverPCS().firePropertyChange((String)key, old, obj);		
		return old;
	}

	public void delete() throws IOException
	{
	}

	public void refreshChildren() throws DatabaseException
	{
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

	public DBSpecFactory getSpecificationFactory()
	{
		return (DBSpecFactory)get(SPECIFICATION_FACTORY);
	}
	
	public void setSpecificationFactory(DBSpecFactory fac)
	{
		put(SPECIFICATION_FACTORY, fac);
		put(SUPPORTED_DBS, fac.supportedDatabases());
	}

	public DBSpec getSpecification()
	{
		return (DBSpec)get(SPECIFICATION);
	}
	
	public void setSpecification(DBSpec spec)
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

	public void setConnection(Connection con)
	{
		Connection oldval = getConnection();
		if (con != null) {
			if (oldval != null && oldval.equals(con)) return;
			put(CONNECTION, con);
		} else remove(CONNECTION);
		getConnectionPCS().firePropertyChange(CONNECTION, oldval, con);
	}

	public DBConnection getDatabaseConnection()
	{
		DatabaseConnection con = new DatabaseConnection(getDriver(), getDatabase(), getUser(), getPassword());		
		if (get(REMEMBER_PWD) != null) con.setRememberPassword(true);
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
		if (pwd != null) put(PASSWORD, pwd);
		if (cinfo.rememberPassword()) put(REMEMBER_PWD, new Boolean(true));
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
		return (String)get("table");
	}

	public void setTable(String nam)
	{
		put("table", nam);
	}

	public String getIconBase()
	{
		return (String)get("iconbase");
	}
	
	public void setIconBase(String base)
	{
		put("iconbase", base);
	}

	public String getDisplayname()
	{
		return (String)get("displayname");
	}

	public void setDisplayname(String name)
	{
		put("displayname", name);
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
			props.put("user", getUser());
			props.put("password", getPassword());
		} catch (Exception e) { props = null; }
 		
 		return props;
	}

	protected void initChildren(Vector children)
	throws DatabaseException
	{
	}

	public Vector getChildren()
	throws DatabaseException
	{
		Vector children = (Vector)get(CHILDREN);		
		if (children.size() > 0 && children.elementAt(0) instanceof DatabaseNodeInfo) return children;

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
			
		} catch (Exception e) {
			throw new DatabaseException("unable to create children, "+e.getMessage());
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
		if (actions == null) {
			actions = new Vector();
			put(ACTIONS, actions);
		}
		
		if (actions.size() == 0) return actions;
		ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");		
		Object xaction = actions.elementAt(0);
		if (xaction != null && xaction instanceof DatabaseAction) return actions;
		for (int i=0; i<actions.size();i++) {
			
			Object e_act = actions.elementAt(i);
			SystemAction action = null;
			if (e_act instanceof Map) {
				Map e_action = (Map)e_act;
				try {
					boolean systemact = false;
					String sysactstr = (String)e_action.get(SYSTEM_ACTION);
					if (sysactstr != null) systemact = sysactstr.toUpperCase().equals("YES");
					String actnode = (String)e_action.get(NODE);
					String actcn = (String)e_action.get(CLASS);
					
					if (!systemact) {
						String locname, xname = (String)e_action.get(NAME);
						try {
							locname = bundle.getString(xname);
						} catch (MissingResourceException e) {
							locname = xname;
							System.out.println("unable to locate localized menu item "+xname);
						}
						
						action = (SystemAction)Class.forName(actcn).newInstance();
						((DatabaseAction)action).setName(locname);		
						((DatabaseAction)action).setNode(actnode); 	
					} else action = SystemAction.get(Class.forName(actcn));
					
				} catch (Exception e) {
					System.out.println("unable to create action \""+e_action.get(NAME)+"\", "+e.getMessage());
				}
			}
			
			actions.setElementAt(action, i);
		}
		
		return actions;
	}
	
	public String toString()
	{
		String result = "";
		Enumeration keys = keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			result = result + key+": "+get(key)+ "\n";
		}
		
		return result;
	}
	
	public boolean isDebugMode()
	{
		DBSpecFactory fac = (DBSpecFactory)get(SPECIFICATION_FACTORY);
		return fac.isDebugMode();
	}
	
	public void setDebugMode(boolean mode)
	{
		DBSpecFactory fac = (DBSpecFactory)get(SPECIFICATION_FACTORY);
		fac.setDebugMode(mode);
		System.out.println("Debugging is "+(mode ? "on" : "off"));
	}
}