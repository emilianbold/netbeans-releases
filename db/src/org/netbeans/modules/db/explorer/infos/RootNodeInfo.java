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

import java.util.*;
import com.netbeans.ddl.*;
import com.netbeans.ide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class RootNodeInfo extends DatabaseNodeInfo
implements ConnectionOwnerOperations
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			Vector cons = RootNode.getOption().getConnections();
			if (cons != null) {
				Enumeration en = cons.elements();
				while(en.hasMoreElements()) {
					DBConnection cinfo = (DBConnection)en.nextElement();
					ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
					ninfo.setUser(cinfo.getUser());
					ninfo.setDatabase(cinfo.getDatabase());
					ninfo.setDatabaseConnection(cinfo);
					if (cinfo.rememberPassword()) ninfo.put(DatabaseNodeInfo.REMEMBER_PWD, Boolean.TRUE);
					children.add(ninfo);
				}
			}
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
		
	
	public void addConnection(DBConnection cinfo)
	throws DatabaseException
	{
		Vector cons = RootNode.getOption().getConnections();
		if (cons.contains(cinfo)) throw new DatabaseException("connection already exists");
		try {
			DatabaseNode node = getNode();
			DatabaseNodeChildren children = (DatabaseNodeChildren)node.getChildren();
			ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
			ninfo.setUser(cinfo.getUser());
			ninfo.setDatabase(cinfo.getDatabase());
			ninfo.setDatabaseConnection(cinfo);
			cons.add(cinfo);
			DatabaseNode cnode = children.createSubnode(ninfo, true);
			if (cinfo.getPassword() != null) ((ConnectionNodeInfo)cnode.getInfo()).connect();
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
/*
	public void removeConnection(DBConnection cinfo, DatabaseNode xnode)
	throws DatabaseException
	{
		DatabaseNode node = getNode();
		Vector cons = RootNode.getOption().getConnections();
		if (!cons.contains(cinfo)) throw new DatabaseException("connection does not exist");
		if (xnode == null) throw new DatabaseException("finding node not implemented yet");	
		try {
			DatabaseNodeChildren chld = (DatabaseNodeChildren)node.getChildren();
			cons.remove(cinfo);
			chld.remove(new Node[]{xnode});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
*/
}