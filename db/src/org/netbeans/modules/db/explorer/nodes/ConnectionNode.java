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

package com.netbeans.enterprise.modules.db.explorer.nodes;

import java.util.*;
import java.beans.*;
import java.sql.*;
import java.io.IOException;
import java.awt.datatransfer.Transferable;

import org.openide.cookies.InstanceCookie;
import org.openide.util.MapFormat;
import org.openide.nodes.*;
import org.openide.util.datatransfer.*;

import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.SpecificationFactory;
import com.netbeans.ddl.impl.Specification;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.DatabaseConnection;
import com.netbeans.enterprise.modules.db.explorer.dlg.ConnectDialog;
import com.netbeans.developer.modules.loaders.form.RADComponentNode;
import com.netbeans.developer.modules.loaders.form.RADComponent;

/** 
* Node representing open or closed connection to database.
*/

public class ConnectionNode extends DatabaseNode implements InstanceCookie
{
	public void setInfo(DatabaseNodeInfo nodeinfo)
	{
		super.setInfo(nodeinfo);
		DatabaseNodeInfo info = getInfo();
		displayFormat = new java.text.MessageFormat((String)info.get("displayname"));
		setName(info.getName());
		getInfo().addConnectionListener(new PropertyChangeListener() {
      		public void propertyChange(PropertyChangeEvent evt) {
      			if (evt.getPropertyName().equals(DatabaseNodeInfo.CONNECTION)) {
      				update((Connection)evt.getNewValue());
      			}
      		}
    	});
	    getCookieSet().add(this);
	}

	public String instanceName() 
	{
		return "com.netbeans.sql.ConnectionSource";
    }    

	public Class instanceClass() throws IOException, ClassNotFoundException
	{
		return Class.forName("com.netbeans.sql.ConnectionSource");
	}
	
	public Object instanceCreate()
	{
		try {
			Object obj = Beans.instantiate(null, instanceName());
			return obj; 				
		} catch (Exception ex) {
			return null;
		}
	}

	private void update(Connection connection)
	{
		boolean connecting = (connection != null);
		DatabaseNodeChildren children = (DatabaseNodeChildren)getChildren();
		DatabaseNodeInfo info = getInfo();
		setIconBase((String)info.get(connecting ? "activeiconbase" : "iconbase"));
		String dkey = (connecting ? "activedisplayname" : "displayname");
		String fmt = (String)info.get(dkey);
		if (fmt != null) {
//			String dname = MapFormat.format(fmt, info);
//			if (dname != null) {
//				info.setName(dname);
//				setName(dname);
//				setDisplayName(dname);
				displayFormat = new java.text.MessageFormat(fmt);
				setName((String)info.get(DatabaseNodeInfo.DATABASE));
//			}
		}
		
		Sheet.Set set = getSheet().get(Sheet.PROPERTIES);
		Node.Property dbprop = set.get(DatabaseNodeInfo.DATABASE);
		Node.Property drvprop = set.get(DatabaseNodeInfo.DRIVER);
		Node.Property usrprop = set.get(DatabaseNodeInfo.USER);
		Node.Property rememberprop = set.get(DatabaseNodeInfo.REMEMBER_PWD);
		
		if (!connecting) {
			children.remove(children.getNodes());
		} else try {
			DatabaseMetaData dmd = connection.getMetaData();
			info.put(DatabaseNodeInfo.DBPRODUCT, dmd.getDatabaseProductName());
			info.put(DatabaseNodeInfo.DBVERSION, dmd.getDatabaseProductVersion());
			info.put(DatabaseNodeInfo.READONLYDB, new Boolean(dmd.isReadOnly()));
			info.put(DatabaseNodeInfo.GROUPSUP, new Boolean(dmd.supportsGroupBy()));
			info.put(DatabaseNodeInfo.OJOINSUP, new Boolean(dmd.supportsOuterJoins()));
			info.put(DatabaseNodeInfo.UNIONSUP, new Boolean(dmd.supportsFullOuterJoins()));
			
			// Create subnodes
			
			DatabaseNodeInfo innernfo;
			innernfo = DatabaseNodeInfo.createNodeInfo(info, DatabaseNode.TABLELIST);
			children.createSubnode(innernfo, true);
			innernfo = DatabaseNodeInfo.createNodeInfo(info, DatabaseNode.VIEWLIST);
			children.createSubnode(innernfo, true);
			innernfo = DatabaseNodeInfo.createNodeInfo(info, DatabaseNode.PROCEDURELIST);
			children.createSubnode(innernfo, true);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		PropertySupport newdbprop = createPropertySupport(dbprop.getName(), dbprop.getValueType(), dbprop.getDisplayName(), dbprop.getShortDescription(), info, !connecting);
		set.put(newdbprop);
		firePropertyChange("db",dbprop,newdbprop);
		PropertySupport newdrvprop = createPropertySupport(drvprop.getName(), drvprop.getValueType(), drvprop.getDisplayName(), drvprop.getShortDescription(), info, !connecting);
		set.put(newdrvprop);
		firePropertyChange("driver",drvprop,newdrvprop);
		PropertySupport newusrprop = createPropertySupport(usrprop.getName(), usrprop.getValueType(), usrprop.getDisplayName(), usrprop.getShortDescription(), info, !connecting);
		set.put(newusrprop);
		firePropertyChange("user",usrprop,newusrprop);
		PropertySupport newrememberprop = createPropertySupport(rememberprop.getName(), rememberprop.getValueType(), rememberprop.getDisplayName(), rememberprop.getShortDescription(), info, connecting);
		set.put(newrememberprop);
		firePropertyChange("rememberpassword",rememberprop,newrememberprop);
	}
}