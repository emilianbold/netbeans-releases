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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class IndexListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =5809643799834921044L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String) get(DatabaseNode.TABLE);
            DriverSpecification drvSpec = getDriverSpecification();
            Connection con = getConnection();
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rs = dmd.getIndexInfo(drvSpec.getCatalog(), drvSpec.getSchema(), table, false, true);
            if (rs != null) {
                Set ixmap = new HashSet();
                IndexNodeInfo info;
                while (rs.next()) {
                    HashMap rset = getRow(rs);
                    if (rset == null)
                        continue;
                    if (rset.get(new Integer(6)) != null) {
                        info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, rset);
                        if (info != null) {
                            if (!ixmap.contains(info.getName())) {
                                ixmap.add(info.getName());
                                info.put("index", info.getName()); //NOI18N
                                children.add(info);
                            }
                        } else
                            throw new Exception(bundle().getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
                    }
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public HashMap getRow(ResultSet rs) {
        HashMap rset = new HashMap();
        Object value;

        try {
            int count = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= count; i++) {
                value = null;
                try {
                    value = rs.getString(i);
                } catch (SQLException exc) {
                    Logger.getLogger("global").log(Level.INFO, null, exc);
                    rset = null;
                    break;
                }
                rset.put(new Integer(i), value);
            }
        } catch (SQLException exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);
            rset = null;
        }

        return rset;
    }
    
    public void addIndex(String name) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(table, false, true);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                IndexNodeInfo info = null;
                String findex;
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
                //refresh list of columns due to the column's icons
                getParent().refreshChildren();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }

}
