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

import java.io.IOException;
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
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
		initChildren(children, null);
	}
	
	private void initChildren(Vector children, String columnname)
	throws DatabaseException
	{				
 		try {
 			
			ResultSet rs;
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String user = getUser();
			String table = (String)get(DatabaseNode.TABLE);
        
			// Primary keys
				
			System.out.println("Primary keys");
			Hashtable ihash = new Hashtable(); 		
			rs = dmd.getPrimaryKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PRIMARY_KEY, rs);
				String iname = (String)iinfo.get("name");
				System.out.println("\t"+iname);
				ihash.put(iname,iinfo);
			}
			rs.close();

			// Indexes
				
			System.out.println("Indexes");
			Hashtable ixhash = new Hashtable(); 		
			rs = dmd.getIndexInfo(catalog,user,table, true, false);
			while (rs.next()) {
				DatabaseNodeInfo iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXED_COLUMN, rs);
				String iname = (String)iinfo.get("colname");
				System.out.println("\t"+iname);
				ixhash.put(iname,iinfo);
			}
			rs.close();
        
			// Foreign keys
/*        
			System.out.println("Foreign keys");
			Hashtable fhash = new Hashtable(); 	
			rs = dmd.getImportedKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo finfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_KEY, rs);
				String iname = (String)finfo.get("name");
				System.out.println("\t"+iname);
				fhash.put(iname,finfo);
			}
			rs.close();
*/        
			// Columns

			rs = dmd.getColumns(catalog,user,table,columnname);
			while (rs.next()) {
				DatabaseNodeInfo nfo;
				String cname = (String)rs.getObject(4);
				if (ihash.containsKey(cname)) {
					nfo = (DatabaseNodeInfo)ihash.get(cname);
				} else if (ixhash.containsKey(cname)) {
					nfo = (DatabaseNodeInfo)ixhash.get(cname);
//				} else if (fhash.containsKey(cname)) {
//					nfo = (DatabaseNodeInfo)fhash.get(cname);
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
	
	public void delete()
	throws IOException
	{
		try {
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropTable(getTable());
			cmd.execute();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	
	/** Returns ColumnNodeInfo specified by info
	* Compares code and name only.
	*/
	public ColumnNodeInfo getChildrenColumnInfo(ColumnNodeInfo info)
	{
		String scode = info.getCode();
		String sname = info.getName();

		try {		
			Enumeration enu = getChildren().elements();
			while (enu.hasMoreElements()) {
				ColumnNodeInfo elem = (ColumnNodeInfo)enu.nextElement();
				if (elem.getCode().equals(scode) && elem.getName().equals(sname)) {
					return elem;
				}
			}
		} catch (Exception e) {}
		return null;
	}

	public void addColumn(String tname)
	throws DatabaseException
	{
		try {
			Vector chvec = new Vector(1);
			ResultSet rs;
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String user = getUser();
			String table = (String)get(DatabaseNode.TABLE);
			
			initChildren(chvec, tname);
			if (chvec.size() == 1) {
				DatabaseNodeInfo nfo = (DatabaseNodeInfo)chvec.elementAt(0); 
				DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
				DatabaseNode dnode = chld.createSubnode(nfo, true);
			}
			
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}