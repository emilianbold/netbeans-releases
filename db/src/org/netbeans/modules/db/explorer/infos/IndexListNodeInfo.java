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

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class IndexListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =5809643799834921044L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(table, false, false);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                Set ixmap = new HashSet();
                HashMap rset = new HashMap();
                IndexNodeInfo info;
                Object value;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    if (rset.get(new Integer(6)) != null) {
                        info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, rset);
                        if (info != null) {
                            if (!ixmap.contains(info.getName())) {
                                ixmap.add(info.getName());
                                info.put("index", info.getName()); //NOI18N
                                children.add(info);
                            }
                        } else
                            throw new Exception(bundle.getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
                    }
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void addIndex(String name) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(table, false, false);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                IndexNodeInfo info = null;
                String findex;
                Object value;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    findex = (String) rset.get(new Integer(6));
                    if (findex != null)
                        if(findex.equalsIgnoreCase(name))
                            info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, rset);
                    rset.clear();
                }
                rs.close();

                if (info != null) ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
                // refersh list of indexes
                refreshChildren();
                //refresh list of columns due to the column's icons
                getParent().refreshChildren();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }

    public void refreshChildren() throws DatabaseException {
        Vector charr = new Vector();
        DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();

        // it is unnecessary ?????
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
