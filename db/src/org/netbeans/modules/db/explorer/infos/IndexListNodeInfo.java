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

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class IndexListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =5809643799834921044L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            DatabaseMetaData dmd = getSpecification().getMetaData();
            String catalog = (String)get(DatabaseNode.CATALOG);
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(catalog, dmd, table, true, false);

            //      boolean jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);
            boolean jdbcOdbcBridge = (((((String)get(DatabaseNode.DRIVER)).trim().equals("sun.jdbc.odbc.JdbcOdbcDriver")) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false); //NOI18N

            if (drvSpec.rs != null) {
                Set ixmap = new HashSet();
                while (drvSpec.rs.next()) {
                    if (jdbcOdbcBridge)
                        drvSpec.rsTemp.next();
                    if (drvSpec.rs.getString("INDEX_NAME") != null) { //NOI18N
                        IndexNodeInfo info;
                        if (jdbcOdbcBridge)
                            info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rsTemp);
                        else
                            info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rs);

                        if (info != null) {
                            if (!ixmap.contains(info.getName())) {
                                ixmap.add(info.getName());
                                info.put("index", info.getName()); //NOI18N
                                children.add(info);
                            }
                        } else
                            throw new Exception(bundle.getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
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

    public void addIndex(String name)
    throws DatabaseException
    {
        try {
            DatabaseMetaData dmd = getSpecification().getMetaData();
            String catalog = (String)get(DatabaseNode.CATALOG);
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(catalog, dmd, table, true, false);
            boolean jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false); //NOI18N

            if (drvSpec.rs != null) {
                while (drvSpec.rs.next()) {
                    if (jdbcOdbcBridge) drvSpec.rsTemp.next();
                    String findex = drvSpec.rs.getString("INDEX_NAME"); //NOI18N
                    if (findex != null) {
                        if (findex.equals(name)) {
                            IndexNodeInfo info;
                            if (jdbcOdbcBridge)
                                info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rsTemp);
                            else
                                info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rs);

                            if (info != null) ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
                        }
                    }
                }
                drvSpec.rs.close();
                if (jdbcOdbcBridge) drvSpec.rsTemp.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }
}
