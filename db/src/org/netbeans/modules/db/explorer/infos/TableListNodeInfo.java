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
	
	/** Add driver operation 
	* @param drv Driver to add
	*/
	public void createTable(DatabaseNodeInfo tinfo) 
	throws DatabaseException
	{
	}	

	/** Remove driver operation 
	* @param drv Driver to remove
	* @param node Owning node info
	*/
	public void dropTable(DatabaseNodeInfo tinfo) 
	throws DatabaseException
	{
		DatabaseNode node = (DatabaseNode)tinfo.getNode();
		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
		try {
			String tname = tinfo.getName();
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropTable(tname);
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
			String tname = tinfo.getName();
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropIndex(tname);
			cmd.execute();
			getNode().getChildren().remove(new Node[]{node});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}		
}