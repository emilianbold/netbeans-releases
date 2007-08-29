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

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class IndexNodeInfo extends TableNodeInfo {
    static final long serialVersionUID =-8633867970381524742L;
    static final Logger LOGGER = Logger.getLogger(IndexNodeInfo.class.getName());
    
    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(table, false, true);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
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
                            throw new Exception(bundle().getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
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

    public void refreshChildren() throws DatabaseException {
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
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
    }

    public void delete() throws IOException {
        try {
            String table = (String)get(DatabaseNode.TABLE);
            Specification spec = (Specification)getSpecification();
            DDLHelper.deleteIndex(spec, (String)get(DatabaseNodeInfo.SCHEMA), 
                    table, getName());

            //refresh list of columns due to the column's icons
            getParent(DatabaseNode.TABLE).refreshChildren();
        } catch (DDLException e) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e.toString());
        }
    }
}
