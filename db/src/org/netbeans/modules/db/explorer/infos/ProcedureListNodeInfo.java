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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ProcedureListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-7911927402768472443L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getProcedures("%");
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    
                    //workaround for issue #21409 (http://db.netbeans.org/issues/show_bug.cgi?id=21409)
                    if (drvSpec.getDBName().indexOf("Oracle") != -1) {
                        String pac = (String) rset.get(new Integer(1));
                        if (pac != null)
                            rset.put(new Integer(3), pac + "." + rset.get(new Integer(3)));
                    }

                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE, rset);
                    if (info != null) {
                        info.put(DatabaseNode.PROCEDURE, info.getName());
                        children.add(info);
                    } else
                        throw new Exception(bundle().getString("EXC_UnableToCreateProcedureNodeInfo")); //NOI18N
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

}
