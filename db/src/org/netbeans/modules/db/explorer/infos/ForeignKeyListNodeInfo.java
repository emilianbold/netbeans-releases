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

import org.openide.nodes.*;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.ForeignKeyNodeInfo;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

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
                Object value;
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
                            throw new Exception(bundle.getString("EXC_UnableToCreateForeignNodeInfo")); //NOI18N
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
