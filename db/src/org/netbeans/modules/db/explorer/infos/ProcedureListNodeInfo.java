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

public class ProcedureListNodeInfo extends DatabaseNodeInfo
implements ProcedureOwnerOperations
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			ResultSet rs = dmd.getProcedures(catalog, getUser(), null);
			while (rs.next()) {
				DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE, rs);
				info.put(DatabaseNode.PROCEDURE, info.getName());
				if (info != null) children.add(info);
				else throw new Exception("unable to create node info for view");
			}
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}	

	public void dropProcedure(DatabaseNodeInfo tinfo) 
	throws DatabaseException
	{
		DatabaseNode node = (DatabaseNode)tinfo.getNode();
		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
		try {
			String tname = tinfo.getName();
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropProcedure(tname);
			System.out.println(cmd.getCommand()); // execute();
			getNode().getChildren().remove(new Node[]{node});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}		
	}
}