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

package com.netbeans.enterprise.modules.db.explorer.nodes;

import java.util.Vector;
import java.util.Enumeration;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;

public class DriverListNode extends DatabaseNode
{		
	public void setInfo(DatabaseNodeInfo info)
	{
		super.setInfo(info);
		DatabaseNodeInfo nfo = getInfo();
		Vector cons = RootNode.getOption().getAvailableDrivers();
		System.out.println("restoring drivers "+cons);
		if (cons != null) {
			try {
				Vector children = nfo.getChildren();
				Enumeration cons_e = cons.elements();
				while (cons_e.hasMoreElements()) {
					DatabaseDriver drv = (DatabaseDriver)cons_e.nextElement();
					DriverNodeInfo chinfo = (DriverNodeInfo)DatabaseNodeInfo.createNodeInfo(nfo, DatabaseNode.DRIVER);
					if (chinfo != null && drv != null) {
						chinfo.setDatabaseDriver(drv);
						children.add(chinfo);
					} else throw new Exception("driver "+drv);
				}
			} catch (Exception e) {
				System.out.println("can't restore all drivers; "+e);
			}
		}
	}
}
