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

import java.sql.*;
import java.util.*;

import org.netbeans.lib.ddl.impl.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class ViewListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =2854540580610981370L;
    
    public void initChildren(Vector children) throws DatabaseException {
        try {
            String[] types = new String[] {"VIEW"}; // NOI18N

            DriverSpecification drvSpec = getDriverSpecification();
            if (drvSpec.areViewsSupported()) {
                drvSpec.getTables("%", types);
                ResultSet rs = drvSpec.getResultSet();
                if (rs != null) {
                    HashMap rset = new HashMap();
                    DatabaseNodeInfo info;
                    while (rs.next()) {
                        rset = drvSpec.getRow();
                        info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, rset);
                        if (info != null) {
                            info.put(DatabaseNode.VIEW, info.getName());
                            children.add(info);
                        } else
                            throw new Exception(bundle.getString("EXC_UnableToCreateNodeInformationForView")); // NOI18N
                        rset.clear();
                    }
                    rs.close();
                }
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /** Adds view into list
    * Adds view named name into children list. View should exist.
    * @param name Name of existing view
    */
    public void addView(String name) throws DatabaseException {
        try {
            String[] types = new String[] {"VIEW"}; // NOI18N

            DriverSpecification drvSpec = getDriverSpecification();
            if (drvSpec.areViewsSupported()) {
                drvSpec.getTables(name, types);
                ResultSet rs = drvSpec.getResultSet();
                if (rs != null) {
                    HashMap rset = new HashMap();
                    rs.next();
                    rset = drvSpec.getRow();
                    DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, rset);
                    rset.clear();
                    rs.close();
                    if (info != null)
                        ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
                    else
                        throw new Exception(bundle.getString("EXC_UnableToCreateNodeInformationForView")); // NOI18N
                }
                // refersh list of views
                refreshChildren();

            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void refreshChildren() throws DatabaseException {
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
}
