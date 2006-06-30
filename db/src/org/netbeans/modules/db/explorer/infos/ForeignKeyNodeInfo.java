/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.*;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;

public class ForeignKeyNodeInfo extends TableNodeInfo {
    static final long serialVersionUID =-8633867970381524742L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);
            String fk_name = (String)get(DatabaseNode.IMPORTED_KEY);
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getImportedKeys(table);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                ColumnNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    if (rset.get(new Integer(8)) != null)
                        if (((String) rset.get(new Integer(12))).startsWith(fk_name)) {
                            info = (ColumnNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_COLUMN, rset);

                            if (info != null) {
                                String tempTName = (String) rset.get(new Integer(3));
                                tempTName = (tempTName == "") ? "" : tempTName + "."; // NOI18N
                                info.setName(info.getName() + " -> " + tempTName + ((String) rset.get(new Integer(4)))); // NOI18N
                                children.add(info);
                            } else
                                throw new Exception(bundle().getString("EXC_UnableToCreateForeignNodeInfo")); //NOI18N
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
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

}
