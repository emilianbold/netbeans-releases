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

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import org.openide.NotifyDescriptor;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ProcedureNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-5984072379104199563L;

    @Override
    public void initChildren(Vector children) throws DatabaseException {
        try {
            if (!ensureConnected()) {
                return;
            }
            String name = (String)get(DatabaseNode.PROCEDURE);
            
            DriverSpecification drvSpec = getDriverSpecification();
            
            //workaround for issue #21409 (http://db.netbeans.org/issues/show_bug.cgi?id=21409)
            String pac = null;
            if (drvSpec.getDBName().indexOf("Oracle") != -1) {
                int pos = name.indexOf(".");
                if (pos != -1) {
                    pac = name.substring(0, pos);
                    name = name.substring(pos + 1);
                }
            }

            drvSpec.getProcedureColumns(name, "%");
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    
                    if (rset.get(new Integer(4)) == null)
                        continue;
                    
                    //workaround for issue #21409 (http://db.netbeans.org/issues/show_bug.cgi?id=21409)
                    if (drvSpec.getDBName().indexOf("Oracle") != -1) {
                        String pac1 = (String) rset.get(new Integer(1));
                        if ((pac == null && pac1 != null) || (pac != null && pac1 == null) || (pac != null && pac1 != null && ! pac1.equals(pac)))
                            continue;
                    }
                    
                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE_COLUMN, rset);
                    if (info != null) {
                        Object ibase = null;
                        String itype = "unknown"; //NOI18N
                        
//                        int type = ((Number)info.get("type")).intValue(); //NOI18N
                        
//cannot use previous line because of MSSQL ODBC problems - see DriverSpecification.getRow() for more info
                        int type;       
                        try {
                            type = (new Integer(info.get("type").toString())).intValue(); //NOI18N
                        } catch (NumberFormatException exc) {
                            throw new IllegalArgumentException(exc.getMessage());
                        }
//end of MSSQL hack
                        
                        switch (type) {
                        case DatabaseMetaData.procedureColumnIn:
                            ibase = info.get("iconbase_in"); //NOI18N
                            itype = "in"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnOut:
                            ibase = info.get("iconbase_out"); //NOI18N
                            itype = "out"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnInOut:
                            ibase = info.get("iconbase_inout"); //NOI18N
                            itype = "in/out"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnReturn:
                            ibase = info.get("iconbase_return"); //NOI18N
                            itype = "return"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnResult:
                            ibase = info.get("iconbase_result"); //NOI18N
                            itype = "result"; //NOI18N
                            break;
                        }
                        if (ibase != null)
                            info.put("iconbase", ibase); //NOI18N
                        info.put("type", itype); //NOI18N
                        children.add(info);
                    } else
                        throw new Exception(bundle().getString("EXC_UnableToCreateProcedureColumnNodeInfo"));
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /* delete procedure from list of procedures and drop procedure in the database */	
    public void delete() throws IOException {
        try {
            Specification spec = (Specification) getSpecification();
            AbstractCommand cmd = spec.createCommandDropProcedure((String) get(DatabaseNode.PROCEDURE));
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            
            getParent().removeChild(this);
        } catch (Exception e) {
            org.openide.DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    @Override
    public String getShortDescription() {
        return bundle().getString("ND_Procedure"); //NOI18N
    }

}
