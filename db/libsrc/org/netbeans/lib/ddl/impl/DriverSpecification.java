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
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.lib.ddl.impl.*;

import org.openide.*;

/**
* @author Radko Najman
*/
public class DriverSpecification {

    /** Used DBConnection */
    private HashMap desc;

    public ResultSet rs, rsTemp;

    /** Owned factory */
    SpecificationFactory factory;

    /** Constructor */
    public DriverSpecification(HashMap description)
    {
        desc = description;
    }

    public DriverSpecificationFactory getDriverSpecificationFactory()
    {
        return factory;
    }

    public void setDriverSpecificationFactory(DriverSpecificationFactory fac)
    {
        factory = (SpecificationFactory) fac;
    }

    private String getCatalog() {
        return (String) desc.get("get_Catalog"); // NOI18N
    }

    private String getSchema() {
        return (String) desc.get("get_Schema"); // NOI18N
    }

    public void getTables(String catalog, DatabaseMetaData dmd, String tableNamePattern, String[] types) {
        String schemaPattern = null;
        boolean caseindentifiers;

        if (tableNamePattern != null)
            try {
                caseindentifiers = dmd.storesMixedCaseIdentifiers();
                if (!caseindentifiers) {
                    caseindentifiers = dmd.storesUpperCaseIdentifiers();
                    tableNamePattern = (caseindentifiers ? tableNamePattern.toUpperCase() : tableNamePattern.toLowerCase());
                }
            } catch (SQLException ex) {
                //        System.out.println("Mixed identifiers: " + ex);
            }

        try {
            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schemaPattern = "dbo"; // NOI18N
                else
                    schemaPattern = dmd.getUserName().trim();
            if ((tableNamePattern == null) && (!desc.get("getTables_TableNamePattern").equals("null"))) // NOI18N
                tableNamePattern = (String) desc.get("getTables_TableNamePattern"); // NOI18N
            if (desc.get("getTables_Types").equals("null")) // NOI18N
                types = null;

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getTables(catalog, schemaPattern, tableNamePattern, types);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getTablesAdaptor: CANNOT OBTAIN TABLES: " + ex);
            rs = null;
        }

        try {
            //      System.out.println("1. PASS - Tables: " + dmd.getDriverName());
            rs = dmd.getTables(catalog, schemaPattern, null, types);
            checkResultSet();
            rs.close();
            rs = dmd.getTables(catalog, schemaPattern, null, types);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - Tables: " + dmd.getDriverName());
                rs = dmd.getTables(null, schemaPattern, null, types);
                checkResultSet();
                rs.close();
                rs = dmd.getTables(null, schemaPattern, null, types);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - Tables: " + dmd.getDriverName());
                    rs = dmd.getTables(catalog, null, null, types);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getTables(catalog, null, null, types);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - Tables: " + dmd.getDriverName());
                        rs = dmd.getTables(null, null, null, null);
                    } catch (Exception ex3) {
                        //            System.out.println("NO TABLES: " + ex3);
                        rs = null;
                    }
                }
            }
        }
    }

    public void getProcedures(String catalog, DatabaseMetaData dmd, String procedureNamePattern) {
        String schemaPattern = null;

        try {
            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schemaPattern = "dbo"; // NOI18N
                else
                    schemaPattern = dmd.getUserName().trim();
            if ((procedureNamePattern == null) && (!desc.get("getProcedures_ProcedureNamePattern").equals("null"))) // NOI18N
                procedureNamePattern = (String) desc.get("getProcedures_ProcedureNamePattern"); // NOI18N

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getProcedures(catalog, schemaPattern, procedureNamePattern);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getProceduresAdaptor: CANNOT OBTAIN PROCEDURES");
            rs = null;
        }

        try {
            //      System.out.println("1. PASS - Procedures: " + dmd.getDriverName());
            rs = dmd.getProcedures(catalog, schemaPattern, null);
            checkResultSet();
            rs.close();
            rs = dmd.getProcedures(catalog, schemaPattern, null);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - Procedures: " + dmd.getDriverName());
                rs = dmd.getProcedures(null, schemaPattern, null);
                checkResultSet();
                rs.close();
                rs = dmd.getProcedures(null, schemaPattern, null);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - Procedures: " + dmd.getDriverName());
                    rs = dmd.getProcedures(catalog, null, null);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getProcedures(catalog, null, null);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - Procedures: " + dmd.getDriverName());
                        rs = dmd.getProcedures(null, null, null);
                    } catch (Exception ex3) {
                        System.out.println("NO PROCEDURES: " + ex3);
                        rs = null;
                    }
                }
            }
        }
    }

    public void getPrimaryKeys(String catalog, DatabaseMetaData dmd, String table) {
        String schema = null;
        boolean caseindentifiers;

        try {
            caseindentifiers = dmd.storesMixedCaseIdentifiers();
            if (!caseindentifiers) {
                caseindentifiers = dmd.storesUpperCaseIdentifiers();
                table = (caseindentifiers ? table.toUpperCase() : table.toLowerCase());
            }
        } catch (SQLException ex) {
            //      System.out.println("DrvSpecification: " + ex);
        }

        try {
            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schema = "dbo"; // NOI18N
                else
                    schema = dmd.getUserName().trim();

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getPrimaryKeys(catalog, schema, table);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getPrimaryKeysAdaptor: CANNOT OBTAIN PRIMARYKEYS");
            rs = null;
        }

        try {
            //      System.out.println("1. PASS - PrimaryKeys: " + dmd.getDriverName());
            rs = dmd.getPrimaryKeys(catalog, schema, table);
            checkResultSet();
            rs.close();
            rs = dmd.getPrimaryKeys(catalog, schema, table);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - PrimaryKeys: " + dmd.getDriverName());
                rs = dmd.getPrimaryKeys(null, schema, table);
                checkResultSet();
                rs.close();
                rs = dmd.getPrimaryKeys(null, schema, table);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - PrimaryKeys: " + dmd.getDriverName());
                    rs = dmd.getPrimaryKeys(catalog, null, table);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getPrimaryKeys(catalog, null, table);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - PrimaryKeys: " + dmd.getDriverName());
                        rs = dmd.getPrimaryKeys(null, null, table);
                    } catch (Exception ex3) {
                        //            System.out.println("NO PRIMARYKEYS: " + ex3);
                        rs = null;
                    }
                }
            }
        }

    }

    public void getIndexInfo(String catalog, DatabaseMetaData dmd, String table, boolean unique, boolean approximate) {
        String schema = null;
        boolean caseindentifiers;
        boolean jdbcOdbcBridge = false;

        try {
            caseindentifiers = dmd.storesMixedCaseIdentifiers();
            if (!caseindentifiers) {
                caseindentifiers = dmd.storesUpperCaseIdentifiers();
                table = (caseindentifiers ? table.toUpperCase() : table.toLowerCase());
            }
        } catch (SQLException ex) {
            //      System.out.println("DrvSpecification: " + ex);
        }

        try {
            jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);

            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schema = "dbo"; // NOI18N
                else
                    schema = dmd.getUserName().trim();
            if (desc.get("getIndexInfo_Unique").equals("false")) // NOI18N
                unique = false;
            if (desc.get("getIndexInfo_Approximate").equals("true")) // NOI18N
                approximate = true;

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
                if (jdbcOdbcBridge) rsTemp = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getIndexInfoAdaptor: CANNOT OBTAIN INDEXINFO");
            rs = null;
            rsTemp = null;
        }

        try {
            //      System.out.println("1. PASS - IndexInfo: " + dmd.getDriverName());
            rs = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
            checkResultSet();
            rs.close();
            rs = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
            if (jdbcOdbcBridge) rsTemp = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - IndexInfo: " + dmd.getDriverName());
                rs = dmd.getIndexInfo(null, schema, table, unique, approximate);
                checkResultSet();
                rs.close();
                rs = dmd.getIndexInfo(null, schema, table, unique, approximate);
                if (jdbcOdbcBridge) rsTemp = dmd.getIndexInfo(null, schema, table, unique, approximate);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - IndexInfo: " + dmd.getDriverName());
                    rs = dmd.getIndexInfo(catalog, null, table, unique, approximate);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getIndexInfo(catalog, null, table, unique, approximate);
                    if (jdbcOdbcBridge) rsTemp = dmd.getIndexInfo(catalog, null, table, unique, approximate);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - IndexInfo: " + dmd.getDriverName());
                        rs = dmd.getIndexInfo(null, null, table, unique, approximate);
                        if (jdbcOdbcBridge) rsTemp = dmd.getIndexInfo(null, null, table, unique, approximate);
                    } catch (Exception ex3) {
                        //            System.out.println("NO INDEXINFO: " + ex3);
                        rs = null;
                        rsTemp = null;
                    }
                }
            }
        }
    }

    public void getColumns(String catalog, DatabaseMetaData dmd, String tableNamePattern, String columnNamePattern) {
        String schemaPattern = null;
        boolean caseindentifiers;
        boolean jdbcOdbcBridge = false;

        try {
            caseindentifiers = dmd.storesMixedCaseIdentifiers();
            if (!caseindentifiers) {
                caseindentifiers = dmd.storesUpperCaseIdentifiers();
                tableNamePattern = (caseindentifiers ? tableNamePattern.toUpperCase() : tableNamePattern.toLowerCase());
            }
        } catch (SQLException ex) {
            //      System.out.println("DrvSpecification: " + ex);
        }

        try {
            jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);

            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schemaPattern = "dbo"; // NOI18N
                else
                    schemaPattern = dmd.getUserName().trim();

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
                if (jdbcOdbcBridge) rsTemp = dmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getColumnsAdaptor: CANNOT OBTAIN COLUMNS");
            rs = null;
            rsTemp = null;
        }

        try {
            //      System.out.println("1. PASS - Columns: " + dmd.getDriverName());
            rs = dmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            checkResultSet();
            rs.close();
            rs = dmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            if (jdbcOdbcBridge) rsTemp = dmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - Columns: " + dmd.getDriverName());
                rs = dmd.getColumns(null, schemaPattern, tableNamePattern, columnNamePattern);
                checkResultSet();
                rs.close();
                rs = dmd.getColumns(null, schemaPattern, tableNamePattern, columnNamePattern);
                if (jdbcOdbcBridge) rsTemp = dmd.getColumns(null, schemaPattern, tableNamePattern, columnNamePattern);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - Columns: " + dmd.getDriverName());
                    rs = dmd.getColumns(catalog, null, tableNamePattern, columnNamePattern);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getColumns(catalog, null, tableNamePattern, columnNamePattern);
                    if (jdbcOdbcBridge) rsTemp = dmd.getColumns(catalog, null, tableNamePattern, columnNamePattern);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - Columns: " + dmd.getDriverName());
                        rs = dmd.getColumns(null, null, tableNamePattern, columnNamePattern);
                        if (jdbcOdbcBridge) rsTemp = dmd.getColumns(null, null, tableNamePattern, columnNamePattern);
                    } catch (Exception ex3) {
                        //            System.out.println("NO COLUMNS: " + ex3);
                        rs = null;
                        rsTemp = null;
                    }
                }
            }
        }
    }

    public void getProcedureColumns(String catalog, DatabaseMetaData dmd, String procedureNamePattern, String columnNamePattern) {
        String schemaPattern = null;

        try {
            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schemaPattern = "dbo"; // NOI18N
                else
                    schemaPattern = dmd.getUserName().trim();
            if ((columnNamePattern == null) && (!desc.get("getProcedureColumns_ColumnNamePattern").equals("null"))) // NOI18N
                columnNamePattern = (String) desc.get("getProcedureColumns_ColumnNamePattern"); // NOI18N

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getProcedureColumnsAdaptor: CANNOT OBTAIN PROCEDURECOLUMNS");
            rs = null;
        }

        try {
            //      System.out.println("1. PASS - ProcedureColumns: " + dmd.getDriverName());
            rs = dmd.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
            checkResultSet();
            rs.close();
            rs = dmd.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - ProcedureColumns: " + dmd.getDriverName());
                rs = dmd.getProcedureColumns(null, schemaPattern, procedureNamePattern, columnNamePattern);
                checkResultSet();
                rs.close();
                rs = dmd.getProcedureColumns(null, schemaPattern, procedureNamePattern, columnNamePattern);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - ProcedureColumns: " + dmd.getDriverName());
                    rs = dmd.getProcedureColumns(catalog, null, procedureNamePattern, columnNamePattern);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getProcedureColumns(catalog, null, procedureNamePattern, columnNamePattern);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - ProcedureColumns: " + dmd.getDriverName());
                        rs = dmd.getProcedureColumns(null, null, procedureNamePattern, columnNamePattern);
                    } catch (Exception ex3) {
                        //            System.out.println("NO PROCEDURECOLUMNS: " + ex3);
                        rs = null;
                    }
                }
            }
        }
    }

    public void getExportedKeys(String catalog, DatabaseMetaData dmd, String table) {
        String schema = null;
        boolean caseindentifiers;

        try {
            caseindentifiers = dmd.storesMixedCaseIdentifiers();
            if (!caseindentifiers) {
                caseindentifiers = dmd.storesUpperCaseIdentifiers();
                table = (caseindentifiers ? table.toUpperCase() : table.toLowerCase());
            }
        } catch (SQLException ex) {
            //      System.out.println("DrvSpecification: " + ex);
        }

        try {
            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schema = "dbo"; // NOI18N
                else
                    schema = dmd.getUserName().trim();

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getExportedKeys(catalog, schema, table);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getExportedKeysAdaptor: CANNOT OBTAIN EXPORTEDKEYS");
            rs = null;
        }

        try {
            //      System.out.println("1. PASS - ExportedKeys: " + dmd.getDriverName());
            rs = dmd.getExportedKeys(catalog, schema, table);
            checkResultSet();
            rs.close();
            rs = dmd.getExportedKeys(catalog, schema, table);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - ExportedKeys: " + dmd.getDriverName());
                rs = dmd.getExportedKeys(null, schema, table);
                checkResultSet();
                rs.close();
                rs = dmd.getExportedKeys(null, schema, table);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - ExportedKeys: " + dmd.getDriverName());
                    rs = dmd.getExportedKeys(catalog, null, table);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getExportedKeys(catalog, null, table);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - ExportedKeys: " + dmd.getDriverName());
                        rs = dmd.getExportedKeys(null, null, table);
                    } catch (Exception ex3) {
                        //            System.out.println("NO EXPORTEDKEYS: " + ex3);
                        rs = null;
                    }
                }
            }
        }
    }

    public void getImportedKeys(String catalog, DatabaseMetaData dmd, String table) {
        String schema = null;
        boolean caseindentifiers;

        try {
            caseindentifiers = dmd.storesMixedCaseIdentifiers();
            if (!caseindentifiers) {
                caseindentifiers = dmd.storesUpperCaseIdentifiers();
                table = (caseindentifiers ? table.toUpperCase() : table.toLowerCase());
            }
        } catch (SQLException ex) {
            //      System.out.println("DrvSpecification: " + ex);
        }

        try {
            if (!getCatalog().equals("true")) // NOI18N
                catalog = null;
            if (getSchema().equals("true")) // NOI18N
                if (isMSSQLSaUser(dmd))
                    schema = "dbo"; // NOI18N
                else
                    schema = dmd.getUserName().trim();

            if (!desc.get("DriverName").equals("DefaultDriver")) { // NOI18N
                rs = dmd.getImportedKeys(catalog, schema, table);
                return;
            }
        } catch (SQLException ex) {
            //      System.out.println("getImportedKeys: CANNOT OBTAIN IMPORTEDKEYS");
            rs = null;
        }

        try {
            //      System.out.println("1. PASS - ImportedKeys: " + dmd.getDriverName());
            rs = dmd.getImportedKeys(catalog, schema, table);
            checkResultSet();
            rs.close();
            rs = dmd.getImportedKeys(catalog, schema, table);
        } catch (Exception ex) {
            try {
                //        System.out.println("2. PASS - ImportedKeys: " + dmd.getDriverName());
                rs = dmd.getImportedKeys(null, schema, table);
                checkResultSet();
                rs.close();
                rs = dmd.getImportedKeys(null, schema, table);
            } catch (Exception ex1) {
                try {
                    //          System.out.println("3. PASS - ImportedKeys: " + dmd.getDriverName());
                    rs = dmd.getImportedKeys(catalog, null, table);
                    checkResultSet();
                    rs.close();
                    rs = dmd.getImportedKeys(catalog, null, table);
                } catch (Exception ex2) {
                    try {
                        //            System.out.println("4. PASS - ImportedKeys: " + dmd.getDriverName());
                        rs = dmd.getImportedKeys(null, null, table);
                    } catch (Exception ex3) {
                        //            System.out.println("NO IMPORTEDKEYS: " + ex3);
                        rs = null;
                    }
                }
            }
        }
    }

    private void checkResultSet() throws Exception {
        if (!rs.next()) {
            rs.close();
            throw new Exception();
        }
    }

    //another patches
    
    public boolean areViewsSupported(DatabaseMetaData dmd) {
        try {
            if (dmd.getDatabaseProductName().trim().equals("PointBase")) // NOI18N
                return false;
            else
                return true;
        } catch(SQLException exc) {
            //PENDING
            return true;
        }
    }
    
    public boolean isMSSQLSaUser(DatabaseMetaData dmd) {
        try {
            String user = dmd.getUserName().trim();
            
            if (dmd.getDatabaseProductName().trim().equals("Microsoft SQL Server") && user.equals("sa")) { // NOI18N
                LinkedList schemas = new LinkedList();
                ResultSet rs = dmd.getSchemas();
                
                while (rs.next())
                    schemas.add(rs.getString("TABLE_SCHEM").trim()); // NOI18N
                
                if (schemas.contains(user))
                    return false;
                
                if (schemas.contains("dbo")) // NOI18N
                    return true;
            }
            
            return false;
        } catch(SQLException exc) {
            //PENDING
            return false;
        }
    }
    
}
