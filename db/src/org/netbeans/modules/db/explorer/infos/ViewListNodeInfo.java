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

import java.sql.*;
import java.util.*;
import com.netbeans.ddl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class ViewListNodeInfo extends DatabaseNodeInfo
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String[] filter = new String[] {"VIEW"};
			String catalog = (String)get(DatabaseNode.CATALOG);
			ResultSet rs = dmd.getTables(catalog, getUser(), null, filter);
			while (rs.next()) {
				DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, rs);
				if (info != null) {
					info.put(DatabaseNode.TABLE, info.getName());
					children.add(info);
				} else throw new Exception("unable to create node info for table");
			}
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	/** Adds view into list
	* Adds view named name into children list. View should exist.
	* @param name Name of existing view
	*/
	public void addView(String name)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String[] filter = new String[] {"VIEW"};
			String catalog = (String)get(DatabaseNode.CATALOG);
			boolean uc = dmd.storesUpperCaseIdentifiers();
			String cname = (uc ? name.toUpperCase() : name.toLowerCase());
			ResultSet rs = dmd.getTables(catalog, getUser(), cname, filter);
			rs.next();
			DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, rs);
			if (info != null) ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
			else throw new Exception("unable to create node info for view");
			rs.close();
 		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
}
