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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class TableListNodeInfo extends DatabaseNodeInfo implements TableOwnerOperations {
    static final long serialVersionUID =-6156362126513404875L;

    protected void initChildren(Vector children) throws DatabaseException {
        try {
            String[] types = new String[] {"TABLE"}; // NOI18N
            List recycleBinTables;
            
            DriverSpecification drvSpec = getDriverSpecification();
            
            // issue 76953: do not display tables from the Recycle Bin on Oracle 10 and higher
            DatabaseMetaData dmd = drvSpec.getMetaData();
            if ("Oracle".equals(dmd.getDatabaseProductName())) {
                try {
                    if (dmd.getDatabaseMajorVersion() >= 10) {
                        recycleBinTables = getOracleRecycleBinTables(dmd);
                    } else {
                        recycleBinTables = Collections.EMPTY_LIST;
                    }
                } catch (SQLException e) {
                    // Some older versions of Oracle driver throw an exception on getDatabaseMajorVersion()
                    recycleBinTables = Collections.EMPTY_LIST;
                }

            } else {
                recycleBinTables = Collections.EMPTY_LIST;
            }
                
            drvSpec.getTables("%", types);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.TABLE, rset);
                    if (info != null) {
                        if (!recycleBinTables.contains(info.getName())) {
                            info.put(DatabaseNode.TABLE, info.getName());
                            children.add(info);
                        }
                    } else
                        throw new Exception(bundle().getString("EXC_UnableToCreateNodeInformationForTable")); // NOI18N
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
    }

    /** Adds driver specified in drv into list.
    * Creates new node info and adds node into node children.
    */
    public void addTable(String tname) throws DatabaseException {
        refreshChildren();
    }

    /** Returns tablenodeinfo specified by info
    * Compares code and name only.
    */
    public TableNodeInfo getChildrenTableInfo(TableNodeInfo info) {
        String scode = info.getCode();
        String sname = info.getName();

        try {
            Enumeration enu = getChildren().elements();
            while (enu.hasMoreElements()) {
                TableNodeInfo elem = (TableNodeInfo)enu.nextElement();
                if (elem.getCode().equals(scode) && elem.getName().equals(sname))
                    return elem;
            }
        } catch (Exception e) {
            //PENDING
        }
        
        return null;
    }
    
    private List getOracleRecycleBinTables(DatabaseMetaData dmd) {
        List result = new ArrayList();
        try {
            Statement stmt = dmd.getConnection().createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT OBJECT_NAME FROM RECYCLEBIN WHERE TYPE = 'TABLE'"); // NOI18N
                try {
                    while (rs.next()) {
                        result.add(rs.getString("OBJECT_NAME")); // NOI18N
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            // not critical, logging is enough
            Logger.getLogger("global").log(Level.INFO, null, e);
            result = Collections.EMPTY_LIST;
        }
        return result;
    }
    
/*
    public void dropIndex(DatabaseNodeInfo tinfo) throws DatabaseException {
        DatabaseNode node = (DatabaseNode)tinfo.getNode();
        DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
        try {
            String tname = tinfo.getName();
            Specification spec = (Specification)getSpecification();
            AbstractCommand cmd = spec.createCommandDropIndex(tname);
            cmd.execute();
            getNode().getChildren().remove(new Node[]{node});
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }           
*/
}
