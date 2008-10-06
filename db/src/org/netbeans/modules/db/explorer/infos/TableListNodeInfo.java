/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    public static final Logger LOGGER = 
            Logger.getLogger(TableListNodeInfo.class.getName());

    @Override
    protected void initChildren(Vector children) throws DatabaseException {
        if (! isConnected()) {
            return;
        }
        try {
            if (!ensureConnected()) {
                return;
            }
            String[] types = new String[] {"TABLE"}; // NOI18N
            List recycleBinTables;
            
            DriverSpecification drvSpec = getDriverSpecification();
            
            // issue 76953: do not display tables from the Recycle Bin on Oracle 10 and higher
            DatabaseMetaData dmd = drvSpec.getMetaData();
            try { 
                if ("Oracle".equals(dmd.getDatabaseProductName())) {  // NOI18N
                    if (dmd.getDatabaseMajorVersion() >= 10) {
                        recycleBinTables = getOracleRecycleBinTables(dmd);
                    } else {
                        recycleBinTables = Collections.EMPTY_LIST;
                    }
                } else {
                    recycleBinTables = Collections.EMPTY_LIST;
                }
            } catch ( Throwable t ) {
                LOGGER.log(Level.INFO, null, t);
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
    
    @Override
    public String getDisplayName() {
        return bundle().getString("NDN_Tables"); //NOI18N
    }

    @Override
    public String getShortDescription() {
        return bundle().getString("ND_TableList"); //NOI18N
    }
    
    @Override
    public void notifyChange() {
        super.notifyChange();
        fireRefresh();        
    }
}
