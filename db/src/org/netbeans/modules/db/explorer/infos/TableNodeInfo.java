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
import org.openide.nodes.Node;
import com.netbeans.ddl.adaptors.*;
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
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
//			String user = getUser();

//je to BARBARSTVI, po beta 6 rozumne prepsat
String user;
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	user = null;
else
	user = dmd.getUserName();

			String table = (String)get(DatabaseNode.TABLE);
        
			// Primary keys
				
//BARBARSTVI podruhe
			Hashtable ihash = new Hashtable(); 		
if (! dmd.getDatabaseProductName().trim().equals("ACCESS")) {
			rs = dmd.getPrimaryKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PRIMARY_KEY, rs);
				String iname = (String)iinfo.get("name");
				ihash.put(iname,iinfo);
			}
			rs.close();
}

			// Indexes
				
			Hashtable ixhash = new Hashtable(); 		
			rs = dmd.getIndexInfo(catalog,user,table, true, false);
			while (rs.next()) {
				DatabaseNodeInfo iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXED_COLUMN, rs);
				String iname = (String)iinfo.get("name");
				ixhash.put(iname,iinfo);
			}
			rs.close();
        
			// Foreign keys
/*        
			Hashtable fhash = new Hashtable(); 	
			rs = dmd.getImportedKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo finfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_KEY, rs);
				String iname = (String)finfo.get("name");
				fhash.put(iname,finfo);
			}
			rs.close();
*/        
			// Columns

			rs = dmd.getColumns(catalog, user, table, columnname);
			while (rs.next()) {
				DatabaseNodeInfo nfo;
				
//BARBARSTVI potreti - nemuzu zavolat 2x getObject na stejny index (druhe volani je v createNodeInfo())
if (! dmd.getDatabaseProductName().trim().equals("ACCESS")) {
				String cname = (String)rs.getObject(4);

  			if (ihash.containsKey(cname))
				  nfo = (DatabaseNodeInfo)ihash.get(cname);
			  else
  			  if (ixhash.containsKey(cname))
				    nfo = (DatabaseNodeInfo)ixhash.get(cname);
//				} else if (fhash.containsKey(cname)) {
//					nfo = (DatabaseNodeInfo)fhash.get(cname);
			    else
  			    nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rs);
} else
  nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rs);
  			    
			  children.add(nfo);

			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setProperty(String key, Object obj)
	{
		try {
			if (key.equals("remarks")) setRemarks((String)obj);		
			put(key, obj);
		} catch (Exception ex) {
			ex.printStackTrace();
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
//			DatabaseMetaData dmd = getConnection().getMetaData();
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
//			String user = getUser();

//je to BARBARSTVI, po beta 6 rozumne prepsat
String user;
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	user = null;
else
	user = dmd.getUserName();

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
/*
 * <<Log>>
 *  13   Gandalf   1.12        11/15/99 Radko Najman    MS ACCESS
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  10   Gandalf   1.9         9/15/99  Slavek Psenicka 
 *  9    Gandalf   1.8         9/13/99  Slavek Psenicka 
 *  8    Gandalf   1.7         9/13/99  Slavek Psenicka 
 *  7    Gandalf   1.6         9/8/99   Slavek Psenicka adaptor changes
 *  6    Gandalf   1.5         7/21/99  Slavek Psenicka 
 *  5    Gandalf   1.4         6/15/99  Slavek Psenicka debug prints
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
