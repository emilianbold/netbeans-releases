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

public class ForeignKeyNodeInfo extends TableNodeInfo {
    static final long serialVersionUID =-8633867970381524742L;
    
    public void initChildren(Vector children) throws DatabaseException {
        try {

            DatabaseMetaData dmd = getSpecification().getMetaData();
            String catalog = (String)get(DatabaseNode.CATALOG);
            String table = (String)get(DatabaseNode.TABLE);
            String fk_name = (String)get(DatabaseNode.IMPORTED_KEY);
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getImportedKeys(catalog, dmd, table);

            //      boolean jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);
            boolean jdbcOdbcBridge = (((((String)get(DatabaseNode.DRIVER)).trim().equals("sun.jdbc.odbc.JdbcOdbcDriver")) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false); //NOI18N

            if (drvSpec.rs != null) {
                while (drvSpec.rs.next()) {
                    if (jdbcOdbcBridge)
                        drvSpec.rsTemp.next();
                    if (drvSpec.rs.getString("FKCOLUMN_NAME") != null) { //NOI18N
                        if (drvSpec.rs.getString("FK_NAME").startsWith(fk_name)) { // NOI18N
                            ColumnNodeInfo info;
                            if (jdbcOdbcBridge)
                                info = (ColumnNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_COLUMN, drvSpec.rsTemp);
                            else
                                info = (ColumnNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_COLUMN, drvSpec.rs);

                            if (info != null) {
                                String tempTName = drvSpec.rs.getString("PKTABLE_NAME"); // NOI18N
                                tempTName = (tempTName=="")?"":tempTName+"."; // NOI18N
                                info.setName(info.getName()+" -> " + tempTName + // NOI18N
                                    drvSpec.rs.getString("PKCOLUMN_NAME")); // NOI18N
                                children.add(info);
                            }
                            else
                                throw new Exception(bundle.getString("EXC_UnableToCreateForeignNodeInfo")); //NOI18N
                        }
                    }
                }
                drvSpec.rs.close();
                if (jdbcOdbcBridge)
                    drvSpec.rsTemp.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void refreshChildren() throws DatabaseException
    {
        Vector charr = new Vector();
        DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();

        // it is unnecessary
        // put(DatabaseNodeInfo.CHILDREN, charr);
        chil.remove(chil.getNodes());
        initChildren(charr);
        Enumeration en = charr.elements();
        while(en.hasMoreElements()) {
            DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
            chil.add(new Node[] {subnode});
        }
    }

}
