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

import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class IndexNodeInfo extends TableNodeInfo {
    static final long serialVersionUID =-8633867970381524742L;
    
    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(table, false, false);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                Hashtable ixmap = new Hashtable();
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                Object value;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    String ixname = (String)get("index"); //NOI18N
                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXCOLUMN, rset);
                    String newixname = (String)info.get("ixname"); //NOI18N
                    if (ixname != null && newixname != null && newixname.equals(ixname)) {
                        String way;
                        if (info.get("ord") instanceof java.lang.Boolean)  //NOI18N //HACK for PointBase
                            way = "A"; //NOI18N
                        else
                            way = (String) info.get("ord"); //NOI18N
                        if (way == null) way = "A"; //NOI18N
                        info.put(DatabaseNodeInfo.ICONBASE, info.get(DatabaseNodeInfo.ICONBASE+way));
                        if (info != null)
                            children.add(info);
                        else {
                            rs.close();
                            throw new Exception(bundle.getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
                        }
                    }
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void refreshChildren() throws DatabaseException
    {
        // create list (infos)
        Vector charr = new Vector();
        put(DatabaseNodeInfo.CHILDREN, charr);
        initChildren(charr);
        
        // create sub-tree (by infos)
        try {

            Node[] subTreeNodes = new Node[charr.size()];

            // current sub-tree
            DatabaseNodeChildren children = (DatabaseNodeChildren)getNode().getChildren();

            // remove current sub-tree
            children.remove(children.getNodes());

            // build refreshed sub-tree
            for(int i=0; i<charr.size(); i++)
                subTreeNodes[i] = children.createNode((DatabaseNodeInfo)charr.elementAt(i));

            // add built sub-tree
            children.add(subTreeNodes);

        } catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                ex.printStackTrace();
        }
    }

    public void delete() throws IOException {
        try {
            String code = getCode();
            String table = (String)get(DatabaseNode.TABLE);
            Specification spec = (Specification)getSpecification();
            DropIndex cmd = (DropIndex)spec.createCommandDropIndex(getName());
            cmd.setTableName(table);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            //refresh list of columns due to the column's icons
            getParent(DatabaseNode.TABLE).refreshChildren();
        } catch (DDLException e) {
            
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
