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
import com.netbeans.ddl.impl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class TableListNodeInfo extends DatabaseNodeInfo
implements TableOwnerOperations
{
	protected void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String[] filter = new String[] {"TABLE","BASE"};
			String catalog = (String)get(DatabaseNode.CATALOG);
			ResultSet rs = dmd.getTables(catalog, getUser(), null, filter);
			while (rs.next()) {
				DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.TABLE, rs);
				if (info != null) {
					info.put(DatabaseNode.TABLE, info.getName());
					children.add(info);
				} else throw new Exception("unable to create node info for table");
			}
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
	
	/** Adds driver specified in drv into list.
	* Creates new node info and adds node into node children.
	*/
	public void addTable(String tname)
	throws DatabaseException
	{
		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String[] filter = new String[] {"TABLE","BASE"};
			String catalog = (String)get(DatabaseNode.CATALOG);
			boolean uc = dmd.storesUpperCaseIdentifiers();
			String cname = (uc ? tname.toUpperCase() : tname.toLowerCase());
			ResultSet rs = dmd.getTables(catalog, getUser(), cname, filter);
			rs.next();
			DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.TABLE, rs);
			rs.close();
			if (info != null) info.put(DatabaseNode.TABLE, info.getName());
			else throw new Exception("unable to create node info for table");
			DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();		
			chld.createSubnode(info, true);
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	public void refreshChildren() throws DatabaseException
	{
		Vector charr = new Vector();
		DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();

		put(DatabaseNodeInfo.CHILDREN, charr);
		chil.remove(chil.getNodes());		
		initChildren(charr);
		Enumeration en = charr.elements();
		while(en.hasMoreElements()) {
			DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
			chil.add(new Node[] {subnode});
		}
	}

	/** Returns tablenodeinfo specified by info
	* Compares code and name only.
	*/
	public TableNodeInfo getChildrenTableInfo(TableNodeInfo info)
	{
		String scode = info.getCode();
		String sname = info.getName();

		try {		
			Enumeration enu = getChildren().elements();
			while (enu.hasMoreElements()) {
				TableNodeInfo elem = (TableNodeInfo)enu.nextElement();
				if (elem.getCode().equals(scode) && elem.getName().equals(sname)) {
					return elem;
				}
			}
		} catch (Exception e) {}
		return null;
	}
/*
	public void dropIndex(DatabaseNodeInfo tinfo) 
	throws DatabaseException
	{
		DatabaseNode node = (DatabaseNode)tinfo.getNode();
		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
		try {
			String tname = tinfo.getName();
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropIndex(tname);
			cmd.execute();
			getNode().getChildren().remove(new Node[]{node});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}		
*/
}