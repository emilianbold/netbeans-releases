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

package com.netbeans.enterprise.modules.db.explorer;

import java.util.*;
import java.sql.*;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.nodes.Children;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabaseNodeChildren extends Children.Array 
{		
	protected Collection initCollection()
	{
		Vector children = new Vector();
		DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
		
		try {		
			Vector chlist = nodeinfo.getChildren();
			for (int i=0;i<chlist.size();i++) {
				DatabaseNodeInfo sinfo = (DatabaseNodeInfo)chlist.elementAt(i);
				DatabaseNode snode = createNode(sinfo);
				if (snode != null) children.add(snode);
				else throw new Exception("unable to create node for "+sinfo.getCode());
			}
		} catch (Exception e) {
			System.out.println("unable to create nodes for "+nodeinfo.getCode()+": "+e);
			children.removeAllElements();
		}
		
		return children;
	}

	public DatabaseNode createNode(DatabaseNodeInfo info)
	{
		String ncode = (String)info.get(DatabaseNodeInfo.CODE);
		String nclass = (String)info.get(DatabaseNodeInfo.CLASS);
		DatabaseNode node = null;
		
		try {
			node = (DatabaseNode)Class.forName(nclass).newInstance();
			node.setInfo(info); /* makes a copy of info, use node.getInfo() to access it */
			node.getInfo().setNode(node); /* this is a weak, be cool, baby ;) */
		} catch (Exception e) {
			System.out.println("unable to create node "+ncode+"("+nclass+")"+", "+e.getMessage());
		}

		return node;
	}
	
	public DatabaseNode createSubnode(DatabaseNodeInfo info, boolean addToChildrenFlag)
	throws DatabaseException
	{
		DatabaseNode subnode = createNode(info);
		if (subnode != null && addToChildrenFlag) {
			DatabaseNodeInfo ninfo = ((DatabaseNode)getNode()).getInfo();
			ninfo.getChildren().add(info);
			if (isInitialized()) add(new Node[] {subnode});
		}
		
		return subnode;
	}
}
