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
import org.openide.nodes.Node;
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
		getChildren(); // force restore
		Vector cons = RootNode.getOption().getConnections();
		String usr = cinfo.getUser();
		String pwd = cinfo.getPassword();
		if (cons.contains(cinfo)) throw new DatabaseException("connection already exists");
		try {
			DatabaseNode node = getNode();
			DatabaseNodeChildren children = (DatabaseNodeChildren)node.getChildren();
			ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
			ninfo.setName(cinfo.getDatabase());
			ninfo.setUser(cinfo.getUser());
			ninfo.setDatabase(cinfo.getDatabase());
			ninfo.setDatabaseConnection(cinfo);
			cons.add(cinfo);
			DatabaseNode cnode = children.createSubnode(ninfo, true);
			if (usr != null && usr.length() > 0 && pwd != null && pwd.length() > 0) {
				((ConnectionNodeInfo)cnode.getInfo()).connect();
			}
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}