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

package org.netbeans.modules.db.explorer.infos;

import java.io.InputStream;
import java.util.*;
import java.sql.*;
import org.netbeans.lib.ddl.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Node;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.nodes.RootNode;

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
/*
 * <<Log>>
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Slavek Psenicka 
 * $
 */
