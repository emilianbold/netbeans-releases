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

import java.io.InputStream;
import java.util.*;
import java.sql.*;
import com.netbeans.ddl.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ddl.util.PListReader;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.DatabaseNode;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;

public class DriverListNodeInfo extends RootNodeInfo 
implements DriverOperations
{	
	public void completeChildren(Vector children)
	throws DatabaseException
	{
	}

	/** Adds driver specified in drv into list.
	* Creates new node info and adds node into node children.
	*/
	public void addDriver(DatabaseDriver drv)
	throws DatabaseException
	{
		DatabaseOption option = RootNode.getOption();
		Vector drvs = option.getAvailableDrivers();
		if (!drvs.contains(drv)) drvs.add(drv);
		else throw new DatabaseException("driver "+drv+" already exists in list");

		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();		
		DriverNodeInfo ninfo = (DriverNodeInfo)createNodeInfo(this, DatabaseNodeInfo.DRIVER);
		ninfo.setDatabaseDriver(drv);
		chld.createSubnode(ninfo, true);
	}
	
	/** Removes node from list and node list.
	*/
	public void removeDriver(DatabaseDriver drv)
	throws DatabaseException
	{
		DatabaseNode node = (DatabaseNode)getNode();
		Vector drvs = RootNode.getOption().getAvailableDrivers();
		int idx = drvs.indexOf(drv);
		if (idx == -1) throw new DatabaseException("driver "+drv+" was not found");
		DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
		Node rnode = chld.getNodes()[idx];
		System.out.println("removing node "+rnode);
		if (rnode != null) chld.remove(new Node[]{rnode});
		else throw new DatabaseException("driver node "+drv+" was not found");
		drvs.removeElementAt(idx);
	}	
}