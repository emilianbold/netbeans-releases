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
import com.netbeans.ide.util.MapFormat;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.SpecificationFactory;
import com.netbeans.ddl.impl.Specification;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.DatabaseConnection;
import com.netbeans.enterprise.modules.db.explorer.dlg.ConnectDialog;

public class ConnectionNode extends DatabaseNode
{
	public void setInfo(DatabaseNodeInfo nodeinfo)
	{
		super.setInfo(nodeinfo);
		getInfo().addConnectionListener(new PropertyChangeListener() {
      		public void propertyChange(PropertyChangeEvent evt) {
      			if (evt.getPropertyName().equals(DatabaseNodeInfo.CONNECTION)) {
      				System.out.println("connection changed...");
      				update((Connection)evt.getNewValue());
      			}
      		}
    	});
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
			String dname = MapFormat.format(fmt, info);
			if (dname != null) setDisplayName(dname);
		}
		
		if (!connecting) {
			children.remove(children.getNodes());
		} else try {
			DatabaseMetaData dmd = connection.getMetaData();
			
			// Setup properties
			
			info.put(DatabaseNodeInfo.DBPRODUCT, dmd.getDatabaseProductName());
			info.put(DatabaseNodeInfo.DBVERSION, dmd.getDatabaseProductVersion());
			info.put(DatabaseNodeInfo.READONLYDB, new Boolean(dmd.isReadOnly()));
			info.put(DatabaseNodeInfo.GROUPSUP, new Boolean(dmd.supportsGroupBy()));
			info.put(DatabaseNodeInfo.OJOINSUP, new Boolean(dmd.supportsOuterJoins()));
			info.put(DatabaseNodeInfo.UNIONSUP, new Boolean(dmd.supportsFullOuterJoins()));
			
			boolean catalogexist = false;
			ResultSet rs = dmd.getCatalogs();
			while (rs.next()) {
				DatabaseNodeInfo nfo = DatabaseNodeInfo.createNodeInfo(getInfo(), DatabaseNode.CATALOG, rs);
				children.createSubnode(nfo, true);
				catalogexist = true;
			}
			rs.close();
			
			if (!catalogexist) {
				DatabaseNodeInfo nfo = DatabaseNodeInfo.createNodeInfo(getInfo(), DatabaseNode.CATALOG);	
				nfo.put("displayname", "Default catalog");		
				children.createSubnode(nfo, true);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}


	protected Map createProperty(String name)
	{
/*		try {
			Connection connection = (Connection)getInfo().getConnection();
			DatabaseMetaData dmd = connection.getMetaData();
		
			if (name.equals("readonlydatabase")) return new Boolean(dmd.isReadOnly());
			if (name.equals("groupbysupport")) return new Boolean(dmd.supportsGroupBy());		
			if (name.equals("outerjoinsupport")) return new Boolean(dmd.supportsOuterJoins());		
			if (name.equals("unionsupport")) return new Boolean(dmd.supportsUnion());		
			if (name.equals("databasename")) return dmd.getDatabaseProductName();
			if (name.equals("databasever")) return dmd.getDatabaseProductVersion();
		} catch (Exception e) {}
*/
		return super.createProperty(name);
	}
}