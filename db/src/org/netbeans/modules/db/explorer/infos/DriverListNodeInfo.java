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
import org.openide.nodes.Node;
import com.netbeans.ddl.util.PListReader;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.DatabaseNode;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;

public class DriverListNodeInfo extends DatabaseNodeInfo 
implements DriverOperations
{	
	protected void initChildren(Vector children)
	throws DatabaseException
	{
		Vector cons = RootNode.getOption().getAvailableDrivers();
		if (cons != null) {
			try {
				Enumeration cons_e = cons.elements();
				while (cons_e.hasMoreElements()) {
					DatabaseDriver drv = (DatabaseDriver)cons_e.nextElement();
					DriverNodeInfo chinfo = (DriverNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.DRIVER);
					if (chinfo != null && drv != null) {
						chinfo.setDatabaseDriver(drv);
						children.add(chinfo);
					} else throw new Exception("driver "+drv);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
}
/*
 * <<Log>>
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/8/99   Slavek Psenicka adaptor changes
 *  6    Gandalf   1.5         8/19/99  Slavek Psenicka English
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/21/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/26/99  Slavek Psenicka Default driver list 
 *       added.
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
