/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.util.Vector;
import org.netbeans.modules.db.DatabaseException;

public class AdaptorListNodeInfo extends DatabaseNodeInfo
{
    static final long serialVersionUID =1895162778653251095L;
    protected void initChildren(Vector children)
    throws DatabaseException
    {
        /*		Vector cons = RootNode.getOption().getAvailableDrivers();
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
        */	}
}
