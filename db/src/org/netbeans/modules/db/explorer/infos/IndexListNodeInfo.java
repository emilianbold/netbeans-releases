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
import com.netbeans.ide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class IndexListNodeInfo extends DatabaseNodeInfo
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String table = (String)get(DatabaseNode.TABLE);
			ResultSet rs = dmd.getIndexInfo(catalog,getUser(),table, true, false);
			Set ixmap = new HashSet();
			while (rs.next()) {
				IndexNodeInfo info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, rs);
				if (info != null) {
					if (!ixmap.contains(info.getName())) {
						ixmap.add(info.getName());
						info.put("index", info.getName());
						children.add(info);
					}
				} else throw new Exception("unable to create node info for index");
			}
			rs.close();
 		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
	
	public void addIndex(String name)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String table = (String)get(DatabaseNode.TABLE);
			ResultSet rs = dmd.getIndexInfo(catalog,getUser(),table, true, false);
			while (rs.next()) {
				String findex = rs.getString("INDEX_NAME");
				if (findex.equals(name)) {
					IndexNodeInfo info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, rs);
					if (info != null) ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
				} else throw new Exception("unable to create node info for index");
			}
			rs.close();
 		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
}
