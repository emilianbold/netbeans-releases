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
  static final long serialVersionUID =2854540580610981370L;
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
//			DatabaseMetaData dmd = getConnection().getMetaData();
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String[] filter = new String[] {"VIEW"};
			String catalog = (String)get(DatabaseNode.CATALOG);
//			ResultSet rs = dmd.getTables(catalog, getUser(), null, filter);

//je to BARBARSTVI, po beta 6 rozumne prepsat
ResultSet rs;
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	rs = dmd.getTables(catalog, null, null, filter);
else
	rs = dmd.getTables(catalog, dmd.getUserName(), null, filter);
	
			while (rs.next()) {
				DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, rs);
				if (info != null) {
					info.put(DatabaseNode.TABLE, info.getName());
					children.add(info);
				} else throw new Exception("unable to create node information for table");
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
//			DatabaseMetaData dmd = getConnection().getMetaData();
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String[] filter = new String[] {"VIEW"};
			String catalog = (String)get(DatabaseNode.CATALOG);
			boolean uc = dmd.storesUpperCaseIdentifiers();
			String cname = (uc ? name.toUpperCase() : name.toLowerCase());
//			ResultSet rs = dmd.getTables(catalog, getUser(), cname, filter);

//je to BARBARSTVI, po beta 6 rozumne prepsat
ResultSet rs;
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	rs = dmd.getTables(catalog, null, cname, filter);
else
	rs = dmd.getTables(catalog, dmd.getUserName(), cname, filter);
			
			rs.next();
			DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, rs);
			if (info != null) ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
			else throw new Exception("unable to create node information for view");
			rs.close();
 		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
}

/*
 * <<Log>>
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         11/15/99 Radko Najman    MS ACCESS
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         8/19/99  Slavek Psenicka English
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
