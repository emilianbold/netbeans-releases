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

import java.util.Map;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import java.util.Arrays;
import java.util.TreeSet;
import java.sql.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabaseNodeChildren extends Children.Array 
{		
	protected Collection initCollection()
	{
		DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
		java.util.Map nodeord = (java.util.Map)nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);
		TreeSet children = new TreeSet(new NodeComparator(nodeord));
		
		try {
			Vector chlist = nodeinfo.getChildren();
			for (int i=0;i<chlist.size();i++) {
				Node snode = null;
				Object sinfo = chlist.elementAt(i);
				
				if (sinfo instanceof DatabaseNodeInfo) {
				  DatabaseNodeInfo dni = (DatabaseNodeInfo) sinfo;
				  if (dni.getName().equals("Connection"))
            dni.setName(dni.getName() + " " + dni.getDatabase());
				  snode = createNode(dni);
				}
				else
				  if (sinfo instanceof Node)
				    snode = (Node)sinfo;
				if (snode != null)
				  children.add(snode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			children.clear();
		}
		
		return children;
	}
/*
	protected Node[] createNodes()
	{
		Node[] nodeorg = super.createNodes();
		DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
		java.util.Map nodeord = (java.util.Map)nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);
		if (nodeord != null) Arrays.sort(nodeorg, new NodeComparator(nodeord));
		return nodeorg;
	}
*/
	class NodeComparator implements Comparator 
	{
		private java.util.Map map = null;
		
		public NodeComparator(java.util.Map map) 
		{
			this.map = map;
		}
		
		public int compare(Object o1, Object o2) 
		{
			int o1val, o2val, diff;
			Integer o1i = (Integer)map.get(o1.getClass().getName());
			if (o1i != null) o1val = o1i.intValue();
			else o1val = Integer.MAX_VALUE;
			Integer o2i = (Integer)map.get(o2.getClass().getName());
			if (o2i != null) o2val = o2i.intValue();
			else o2val = Integer.MAX_VALUE;
			
			diff = o1val-o2val;
			if (diff == 0) return ((DatabaseNode)o1).getInfo().getName().compareTo(((DatabaseNode)o2).getInfo().getName());
			return diff;			
		}
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
			e.printStackTrace();
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

/*
 * <<Log>>
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/12/99 Radko Najman    bug 4318 fixed
 *  8    Gandalf   1.7         9/27/99  Slavek Psenicka Support for 
 *       non-DatabaseNodeInfo nodes in initCollection
 *  7    Gandalf   1.6         9/8/99   Slavek Psenicka adaptor changes
 *  6    Gandalf   1.5         9/2/99   Slavek Psenicka CreateNodes removed
 *  5    Gandalf   1.4         8/27/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
