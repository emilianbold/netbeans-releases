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
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ForeignKeyListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =5809643799834921044L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getImportedKeys(table);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                Set fkmap = new HashSet();
                HashMap rset = new HashMap();
                ForeignKeyNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    if (rset.get(new Integer(12)) != null) {
                        info = (ForeignKeyNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.IMPORTED_KEY, rset);
                        if (info != null) {
                            String fkName = info.getName();
                            if (fkName == null || fkName.trim().equals("")) {// NOI18N 
                                String refName = (String) rset.get(new Integer(3));
                                info.setName("GENERATED_FK_" + refName); // NOI18N
                            }
                            if (!fkmap.contains(info.getName())) {
                                fkmap.add(info.getName());
                                info.put(DatabaseNode.IMPORTED_KEY, info.getName());
                                children.add(info);
                            }
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

}
