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

package org.netbeans.lib.ddl.impl;

import java.beans.*;
import java.sql.*;
import java.util.*;

import org.openide.*;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.lib.ddl.impl.*;

public class DriverSpecification {

    /** Used DBConnection */
    private HashMap desc;

    private String catalog, schema;

    private DatabaseMetaData dmd;

    private ResultSet rs;

    private String quoteString;
    
    /** Owned factory */
    SpecificationFactory factory;

    /** Constructor */
    public DriverSpecification(HashMap description) {
        desc = description;
        quoteString = null;
    }

    public DriverSpecificationFactory getDriverSpecificationFactory() {
        return factory;
    }

    public void setDriverSpecificationFactory(DriverSpecificationFactory fac) {
        factory = (SpecificationFactory) fac;
    }

    public void setCatalog(String catalog) {
        if (catalog == null || dmd == null) {
            this.catalog = catalog;
            return;
        } else
            catalog.trim();

        ResultSet rs;
        LinkedList list = new LinkedList();

        try {
            rs = dmd.getCatalogs();
            while (rs.next())
                list.add(rs.getString(1).trim());
            rs.close();
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
//            this.catalog = catalog;
            this.catalog = null;  //hack for IBM ODBC driver
            rs = null;
            return;
        }

        if (list.contains(catalog))
            this.catalog = catalog;
        else
            this.catalog = null; //hack for Sybase ODBC driver
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setMetaData(DatabaseMetaData dmd) {
        this.dmd = dmd;
    }

    public void getTables(String tableNamePattern, String[] types) {
        try {
            tableNamePattern = quoteString(tableNamePattern);
            rs = dmd.getTables(catalog, schema, tableNamePattern, types);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getProcedures(String procedureNamePattern) {
        try {
            procedureNamePattern = quoteString(procedureNamePattern);
            rs = dmd.getProcedures(catalog, schema, procedureNamePattern);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getPrimaryKeys(String table) {
        try {
            table = quoteString(table);
            rs = dmd.getPrimaryKeys(catalog, schema, table);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getIndexInfo(String table, boolean unique, boolean approximate) {
        try {
            table = quoteString(table);
            rs = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getColumns(String tableNamePattern, String columnNamePattern) {
        try {
            tableNamePattern = quoteString(tableNamePattern);
            columnNamePattern = quoteString(columnNamePattern);
            rs = dmd.getColumns(catalog, schema, tableNamePattern, columnNamePattern);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getProcedureColumns(String procedureNamePattern, String columnNamePattern) {
        try {
            procedureNamePattern = quoteString(procedureNamePattern);
            columnNamePattern = quoteString(columnNamePattern);
            rs = dmd.getProcedureColumns(catalog, schema, procedureNamePattern, columnNamePattern);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getExportedKeys(String table) {
        try {
            table = quoteString(table);
            rs = dmd.getExportedKeys(catalog, schema, table);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public void getImportedKeys(String table) {
        try {
            table = quoteString(table);
            rs = dmd.getImportedKeys(catalog, schema, table);
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();
            rs = null;
        }
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public HashMap getRow() {
        HashMap rset = new HashMap();
        Object value;

        try {
            int count = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= count; i++) {
                value = null;
                try {
                    value = rs.getString(i);
//                    value = rs.getObject(i); //cannot use getObject() because of problems with MSSQL ODBC driver
                }  catch (SQLException exc) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        exc.printStackTrace();

                    rset = null;
                    break;
                }
                rset.put(new Integer(i), value);
            }
        } catch (SQLException exc) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                exc.printStackTrace();

            rset = null;
        }

        return rset;
    }

    //another patches

    public boolean areViewsSupported() {
        LinkedList list = new LinkedList();

        list.add("PointBase"); // NOI18N
        list.add("MySQL"); // NOI18N
        list.add("HypersonicSQL"); // NOI18N
//        list.add("InstantDB"); // NOI18N - isn't necessary in the list - getTables() returns empty result set for views

        try {
            if (list.contains(dmd.getDatabaseProductName().trim()))
                return false;
            else
                return true;
        } catch(SQLException exc) {
            //PENDING
            return true;
        }
    }

    private String getQuoteString() {
        if (quoteString == null) {
            try {
                quoteString = dmd.getIdentifierQuoteString();
                if (quoteString == null || quoteString.equals(" ")) //NOI18N
                    quoteString = ""; //NOI18N
                else
                    quoteString.trim();
            } catch (SQLException exc) {
                quoteString = ""; //NOI18N
            }
        }
        
        return quoteString;
    }
    
    private String quoteString(String str) {
        try {
            if (dmd.getDatabaseProductName().trim().equals("PointBase")) { //NOI18N
                //hack for PointBase - DatabaseMetaData methods require quoted arguments for case sensitive identifiers
                String quoteStr = getQuoteString();
                if (str != null && !str.equals("%") && !quoteStr.equals("")) //NOI18N
                    str = quoteStr + str + quoteStr;
            }
        } catch (SQLException exc) {
            //PENDING
        }
        
        return str;
    }
}
