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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.*;
import java.sql.*;
import java.text.MessageFormat;

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.nodes.RootNode;

public class DriverListNodeInfo extends DatabaseNodeInfo implements DriverOperations {
    static final long serialVersionUID =-7948529055260667590L;
    
    protected void initChildren(Vector children) throws DatabaseException {
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
                    } else {
                        String message = MessageFormat.format(bundle.getString("EXC_Driver"), new String[] {drv.toString()}); // NOI18N
                        throw new Exception(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Refresh the list of drivers. */
    public void refreshChildren() throws DatabaseException
    {
    
        Vector charr = new Vector();
        DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();
    
        put(DatabaseNodeInfo.CHILDREN, charr);
        chil.remove(chil.getNodes());
        initChildren(charr);
        Enumeration en = charr.elements();
        while(en.hasMoreElements()) {
            DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
            chil.add(new Node[] {subnode});
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
        if (!drvs.contains(drv))
            drvs.add(drv);
        else {
            String message = MessageFormat.format(bundle.getString("EXC_DriverAlreadyExists"), new String[] {drv.toString()}); // NOI18N
            throw new DatabaseException(message);
        }

        DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
        DriverNodeInfo ninfo = (DriverNodeInfo)createNodeInfo(this, DatabaseNodeInfo.DRIVER);
        ninfo.setDatabaseDriver(drv);
        chld.createSubnode(ninfo, true);
    }
}
