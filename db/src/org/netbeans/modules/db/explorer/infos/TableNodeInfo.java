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
import com.netbeans.ide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class TableNodeInfo extends DatabaseNodeInfo
implements TableOperations
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
 			
			ResultSet rs;
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String user = getUser();
			String table = (String)get(DatabaseNode.TABLE);
        
			// Indexes
				
			Hashtable ihash = new Hashtable(); 		
			rs = dmd.getPrimaryKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PRIMARY_KEY, rs);
				String iname = (String)iinfo.get("name");
				ihash.put(iname,iinfo);
			}
			rs.close();
        
			// Foreign keys
        
			Hashtable fhash = new Hashtable(); 	
			rs = dmd.getImportedKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo finfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_KEY, rs);
				String iname = (String)finfo.get("name");
				fhash.put(iname,finfo);
			}
			rs.close();
        
			// Columns

			rs = dmd.getColumns(catalog,user,table,null);
			while (rs.next()) {
				DatabaseNodeInfo nfo;
				String cname = (String)rs.getObject(4);
				if (ihash.containsKey(cname)) {
					nfo = (DatabaseNodeInfo)ihash.get(cname);
				} else if (fhash.containsKey(cname)) {
					nfo = (DatabaseNodeInfo)fhash.get(cname);
				} else nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rs);
				children.add(nfo);
			}
			rs.close();

		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setProperty(String key, Object obj)
	{
		try {
			if (key.equals("remarks")) setRemarks((String)obj);		
			put(key, obj);
		} catch (Exception e) {
			System.out.println("unable to set "+key+" = "+obj);
		}
	}

	public void setRemarks(String rem)
	throws DatabaseException
	{
		String tablename = (String)get(DatabaseNode.TABLE);
		Specification spec = (Specification)getSpecification();
		try {
			AbstractCommand cmd = spec.createCommandCommentTable(tablename, rem);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void removeColumn(DatabaseNodeInfo tinfo) 
	throws DatabaseException
	{
		DatabaseNode node = (DatabaseNode)tinfo.getNode();
		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
		try {
			String cname = tinfo.getName();
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandRemoveColumn(cname);
			cmd.execute();
			getNode().getChildren().remove(new Node[]{node});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	public void dropIndex(DatabaseNodeInfo tinfo) 
	throws DatabaseException
	{
		DatabaseNode node = (DatabaseNode)tinfo.getNode();
		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
		try {
			String cname = tinfo.getName();
			Specification spec = (Specification)getSpecification();

			// Add
			
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}