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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.sql.ParameterMetaData;
import java.sql.SQLException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * Extracts database metadata information (table names and constraints, their
 * associated columns, etc.)
 *
 * @author Susan Chen
 * @version 
 */
public class DBMetaData {
    // constants
    
    /** Index to the name field for results of table/view/procedure searches */
    public static final int NAME = 0;
    
    /** Index to the catalog field for results of table/view/procedure searches */
    public static final int CATALOG = 1;
    
    /** Index to the schema field for results of table/view/procedure searches */
    public static final int SCHEMA = 2;
    
    /** Index to the type field for results of table/view/procedure searches */
    public static final int TYPE = 3;
    
    /** Database OTD type for DB2 */
    public static final String DB2 = "DB2"; // NOI18N
    
    /** Database OTD type for Oracle */
    public static final String ORACLE = "ORACLE"; // NOI18N
    
    /** Database OTD type for SQL Server */
    public static final String SQLSERVER = "SQLSERVER"; // NOI18N
    
    /** Database OTD type for Sybase */
    public static final String SYBASE = "SYBASE"; // NOI18N
    
    /** Database OTD type for VSAM */
    public static final String VSAM_ADABAS_IAM = "LEGACY"; // NOI18N
    
    /** Database OTD type for JDBC-ODBC */
    public static final String JDBC_ODBC = "JDBCODBC"; // NOI18N
    
    /** Database type display description for DB2 */
    public static final String DB2_TEXT = "DB2"; // NOI18N
    
    /** Database type display description for Oracle */
    public static final String ORACLE_TEXT = "ORACLE"; // NOI18N
    
    /** Database type display description for SQL Server */
    public static final String SQLSERVER_TEXT = "SQL SERVER"; // NOI18N
    
    /** Database type display description for Sybase */
    public static final String SYBASE_TEXT = "SYBASE"; // NOI18N
    
    /** Database type display description for VSAM/ADABAS/IAM */
    public static final String VSAM_ADABAS_IAM_TEXT = "VSAM/ADABAS/IAM"; // NOI18N
    
    /** Database type display description for JDBC-ODBC */
    public static final String JDBCODBC_TEXT = "JDBC-ODBC"; // NOI18N
    
    /** List of database type display descriptions */
    public static final String[] DBTYPES = {
        DB2_TEXT, ORACLE_TEXT, SQLSERVER_TEXT, SYBASE_TEXT,
        VSAM_ADABAS_IAM_TEXT, JDBCODBC_TEXT
    };
    
    /** List of Java types */
    public static final String[] JAVATYPES = {
        "boolean", "byte", "byte[]", "double", "float", "int",
        "java.lang.String", "java.lang.Object", "java.math.BigDecimal",
        "java.net.URL", "java.sql.Array",
        "java.sql.Blob", "java.sql.Clob", "java.sql.Date", "java.sql.Ref",
        "java.sql.Struct", "java.sql.Time", "java.sql.Timestamp",
        "long", "short"
    };
    
    /** List of JDBC SQL types */
    public static final String[] SQLTYPES = {
        "ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "BOOLEAN",
        "CHAR", "CLOB", "DATALINK", "DATE", "DECIMAL", "DISTINCT",
        "DOUBLE", "FLOAT", "INTEGER", "JAVA_OBJECT", "LONGVARBINARY",
        "LONGVARCHAR", "NULL", "NUMERIC", "OTHER", "REAL", "REF", "SMALLINT",
        "STRUCT", "TIME", "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR",
        //added abey for Procedure ResultSet
        "RESULTSET"
    };
    
    public static final int[] SQLTYPE_CODES = {
        java.sql.Types.ARRAY,
        java.sql.Types.BIGINT,
        java.sql.Types.BINARY,
        java.sql.Types.BIT,
        java.sql.Types.BLOB,
        16, // java.sql.Types.BOOLEAN,
        java.sql.Types.CHAR,
        java.sql.Types.CLOB,
        70, //case java.sql.Types.DATALINK,
        java.sql.Types.DATE,
        java.sql.Types.DECIMAL,
        java.sql.Types.DISTINCT,
        java.sql.Types.DOUBLE,
        java.sql.Types.FLOAT,
        java.sql.Types.INTEGER,
        java.sql.Types.JAVA_OBJECT,
        java.sql.Types.LONGVARBINARY,
        java.sql.Types.LONGVARCHAR,
        java.sql.Types.NULL,
        java.sql.Types.NUMERIC,
        java.sql.Types.OTHER,
        java.sql.Types.REAL,
        java.sql.Types.REF,
        java.sql.Types.SMALLINT,
        java.sql.Types.STRUCT,
        java.sql.Types.TIME,
        java.sql.Types.TIMESTAMP,
        java.sql.Types.TINYINT,
        java.sql.Types.VARBINARY,
        java.sql.Types.VARCHAR
    };
    
    
    /** Map SQL type to Java type */
    public static final HashMap SQLTOJAVATYPES = new HashMap();
    static {
        SQLTOJAVATYPES.put("ARRAY", "java.sql.Array"); // NOI18N
        SQLTOJAVATYPES.put("BIGINT", "long"); // NOI18N
        SQLTOJAVATYPES.put("BINARY", "byte[]"); // NOI18N
        SQLTOJAVATYPES.put("BIT", "boolean"); // NOI18N
        SQLTOJAVATYPES.put("BLOB", "java.sql.Blob"); // NOI18N
        SQLTOJAVATYPES.put("BOOLEAN", "boolean"); // NOI18N
        SQLTOJAVATYPES.put("CHAR", "java.lang.String"); // NOI18N
        SQLTOJAVATYPES.put("CLOB", "java.sql.Clob"); // NOI18N
        SQLTOJAVATYPES.put("DATALINK", "java.net.URL"); // NOI18N
        SQLTOJAVATYPES.put("DATE", "java.sql.Date"); // NOI18N
        SQLTOJAVATYPES.put("DECIMAL", "java.math.BigDecimal"); // NOI18N
        SQLTOJAVATYPES.put("DISTINCT", "java.lang.String"); // NOI18N
        SQLTOJAVATYPES.put("DOUBLE", "double"); // NOI18N
        SQLTOJAVATYPES.put("FLOAT", "double"); // NOI18N
        SQLTOJAVATYPES.put("INTEGER", "int"); // NOI18N
        SQLTOJAVATYPES.put("JAVA_OBJECT", "java.lang.Object"); // NOI18N
        SQLTOJAVATYPES.put("LONGVARBINARY", "byte[]"); // NOI18N
        SQLTOJAVATYPES.put("LONGVARCHAR", "java.lang.String"); // NOI18N
        SQLTOJAVATYPES.put("NULL", "java.lang.String"); // NOI18N
        SQLTOJAVATYPES.put("NUMERIC", "java.math.BigDecimal"); // NOI18N
        SQLTOJAVATYPES.put("OTHER", "java.lang.String"); // NOI18N
        SQLTOJAVATYPES.put("REAL", "float"); // NOI18N
        SQLTOJAVATYPES.put("REF", "java.sql.Ref"); // NOI18N
        SQLTOJAVATYPES.put("SMALLINT", "short"); // NOI18N
        SQLTOJAVATYPES.put("STRUCT", "java.sql.Struct"); // NOI18N
        SQLTOJAVATYPES.put("TIME", "java.sql.Time"); // NOI18N
        SQLTOJAVATYPES.put("TIMESTAMP", "java.sql.Timestamp"); // NOI18N
        SQLTOJAVATYPES.put("TINYINT", "byte"); // NOI18N
        SQLTOJAVATYPES.put("VARBINARY", "byte[]"); // NOI18N
        SQLTOJAVATYPES.put("VARCHAR", "java.lang.String"); // NOI18N
        //added abey for Procedure ResultSets
        SQLTOJAVATYPES.put("RESULTSET", "java.sql.ResultSet"); // NOI18N
    }
    
    // String used in java.sql.DatabaseMetaData to indicate system tables.
    private static final String SYSTEM_TABLE = "SYSTEM TABLE"; // NOI18N
    
    // String used in java.sql.DatabaseMetaData to indicate system tables.
    private static final String TABLE = "TABLE"; // NOI18N
    
    // String used in java.sql.DatabaseMetaData to indicate system tables.
    private static final String VIEW = "VIEW"; // NOI18N
    
    // String used in java.sql.DatabaseMetaData to indicate aliases.
    private static final String ALIAS = "ALIAS"; // NOI18N
    
    // String used in java.sql.DatabaseMetaData to indicate synonyms.
    private static final String SYNONYM = "SYNONYM"; // NOI18N
    
    private Connection dbconn;           // db connection
    private DatabaseMetaData dbmeta;     // db metadata
    private String errMsg;               // error message
    private boolean checkPrepStmtMetaData = true;    // indicates driver does not
    // fully support finding prepared
    // statement metadata
    private boolean checkProcMetaData = false;    // indicates driver does not
    // fully support finding prepared
    // statement metadata
    private boolean errPrepStmtParameters = false;   // error getting prep. stmt. parameters
    private boolean errPrepStmtResultSetColumns = false; // error getting prep. stmt. resultset columns
    
	private String sqlText;
    
    /**
     * Gets the primary keys for a table.
     *
     * @param newTable Table to get the primary key(s) for
     * @throws Exception DOCUMENT ME!
     */
    public void checkPrimaryKeys(Table newTable) throws Exception {
        errMsg = "";
        try {
            // get the primary keys
            List primaryKeys = getPrimaryKeys(newTable.getCatalog(),
                    newTable.getSchema(),
                    newTable.getName());
            
            if (primaryKeys.size() != 0) {
                newTable.setPrimaryKeyColumnList(primaryKeys);
                
                // create a hash set of the keys
                java.util.Set primaryKeysSet = new java.util.HashSet();
                for (int i = 0; i < primaryKeys.size(); i++) {
                    KeyColumn key = (KeyColumn) primaryKeys.get(i);
                    primaryKeysSet.add(key.getColumnName());
                }
                
                // now loop through all the columns flagging the primary keys
                TableColumn[] columns = newTable.getColumns();
                if (columns != null) {
                    for (int i = 0; i < columns.length; i++) {
                        if (primaryKeysSet.contains(columns[i].getName())) {
                            columns[i].setIsPrimaryKey(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    /**
     * Gets the foreign keys for a table.
     *
     * @param newTable Table to get the foreign key(s) for
     * @throws Exception DOCUMENT ME!
     */
    public void checkForeignKeys(Table newTable) throws Exception {
        errMsg = "";
        try {
            // get the foreing keys
            List foreignKeys = getForeignKeys(newTable.getCatalog(),
                    newTable.getSchema(),
                    newTable.getName());
            if (foreignKeys != null) {
                newTable.setForeignKeyColumnList(foreignKeys);
                
                // create a hash set of the keys
                java.util.Set foreignKeysSet = new java.util.HashSet();
                for (int i = 0; i < foreignKeys.size(); i++) {
                    ForeignKeyColumn key = (ForeignKeyColumn) foreignKeys.get(i);
                    foreignKeysSet.add(key.getColumnName());
                }
                
                // now loop through all the columns flagging the foreign keys
                TableColumn[] columns = newTable.getColumns();
                if (columns != null) {
                    for (int i = 0; i < columns.length; i++) {
                        if (foreignKeysSet.contains(columns[i].getName())) {
                            columns[i].setIsForeignKey(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    /**
     * Establishes a connection to the database.
     *
     * @param driver Driver class
     * @param url JDBC connection URL
     * @param userName User name
     * @param passWord Password
     * @throws Exception DOCUMENT ME!
     */
    public void connectDB(String driver, String url, String userName, String passWord) throws Exception {
        errMsg = "";
        boolean isNativeDriver = false;
        // connect to the database
        try {
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            //Class.forName("oracle.jdbc.OracleDriver");
            //String url = "jdbc:odbc:" + dataBase;
            Class.forName(driver);
            //PP: Oracle specific change for date type columns to be
            //compatible with 5.0.x versions of the product. See QAI 90533.
            java.util.Properties props = new java.util.Properties();
            props.put("user", userName);
            props.put("password", passWord);
            if(url.startsWith("jdbc:oracle:")){
                isNativeDriver = true;
                props.put("oracle.jdbc.V8Compatible", "true");
            }
            dbconn = DriverManager.getConnection(url, props);
            
            // once we've connected, get the metadata
            getDBMetaData();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            errMsg = "Could not locate JDBC driver: " + e.getLocalizedMessage();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    /**
     * Disconnects from the database.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void disconnectDB() throws Exception {
        errMsg = "";
        // close connection to database
        try {
            if ((dbconn != null) && (!dbconn.isClosed())) {
                dbconn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    private void getDBMetaData() throws Exception {
        errMsg = "";
        // get the metadata
        try {
            dbmeta = dbconn.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            //throw e;
        }
    }
    
    /**
     * Returns the database product name
     *
     * @return String database product name
     * @throws Exception DOCUMENT ME!
     */
    public String getDBName() throws Exception {
        String dbname = "";
        
        errMsg = "";
        // get the database product name
        try {
            dbname = dbmeta.getDatabaseProductName();
        } catch (SQLException e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
        return dbname;
    }
    
    /**
     * Returns the database OTD type.
     *
     * @return String Database OTD type
     * @throws Exception DOCUMENT ME!
     */
    public String getDBType() throws Exception {
        String dbtype = "";
        
        // get the database type based on the product name converted to lowercase
        String dbname = getDBName().toLowerCase();
        if (dbname.equals("microsoft sql server")) {
            // Microsoft SQL Server
            dbtype = SQLSERVER;
        } else if ((dbname.equals("sql server")) || (dbname.indexOf("sybase") > -1)) {
            // SYBASE
            dbtype = SYBASE;
        } else if ((dbname.indexOf("db2") > -1) || (dbname.equals("as"))) {
            // DB2
            dbtype = DB2;
        } else if ((dbname.equals("exadas")) || (dbname.equals("attunity connect driver"))) {
            // VSAM
            dbtype = VSAM_ADABAS_IAM;
        } else if (dbname.indexOf("orac") > -1) {
            // Oracle
            dbtype = ORACLE;
        } else {
            // other type, default to JDBC-ODBC
            dbtype = JDBC_ODBC;
        }
        
        return dbtype;
    }
    
    
    private String getJDBCSearchPattern(String guiPattern) throws Exception {
        errMsg = "";
        
        // Converts the passed in GUI pattern to one understood by the
        // JDBC driver:
        //   change _ to <escape char>_
        //   change % to <escape char>%
        //   change * to % = GUI uses * to represent 0 or more characters
        //   change ? to _ = GUI uses ? to represent any single character
        try {
            String jdbcPattern = guiPattern;
            String escapeChar = dbmeta.getSearchStringEscape();
            
            // change _ to <escape char>_
            //PP:See bug 10718. Disabling the escape character for _
            //jdbcPattern = replaceAllChars(jdbcPattern, '_', escapeChar + "_");
            
            // change % to <escape char>%
            jdbcPattern = replaceAllChars(jdbcPattern, '%', escapeChar + "%");
            
            // change * to %
            jdbcPattern = jdbcPattern.replace('*', '%');
            
            // change ? to _
            jdbcPattern = jdbcPattern.replace('?', '_');
            
            return jdbcPattern;
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    /**
     * Returns a list of schemas in the database.
     *
     * @return String[] List of schema names
     * @throws Exception DOCUMENT ME!
     */
    public String[] getSchemas() throws Exception {
        errMsg = "";
        // get all schemas
        try {
            ResultSet rs = dbmeta.getSchemas();
            Vector v = new Vector();
            String[] schemaNames = null;
            
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                v.add(schema);
            }
            if (v.size() > 0) {
                // copy into array to return
                schemaNames = new String[v.size()];
                v.copyInto(schemaNames);
            }
            rs.close();
            return schemaNames;
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    /**
     * Returns a list of tables matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param tablePattern Table name pattern
     * @param includeSystemTables Indicate whether to include system tables in search
     * @return String[][] List of tables matching search filters
     * @throws Exception DOCUMENT ME!
     */
    public String[][] getTablesOnly(String catalog, String schemaPattern,
            String tablePattern, boolean includeSystemTables)
            throws Exception {
        String[] tableTypes;
        
        if (includeSystemTables) {
            String[] types = {TABLE, ALIAS, SYNONYM, SYSTEM_TABLE};
            tableTypes = types;
        } else {
            String[] types = {TABLE, ALIAS, SYNONYM};
            tableTypes = types;
        }
        
        return getTables(catalog, schemaPattern, tablePattern, tableTypes);
    }
    
    /**
     * Returns a list of views matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param viewPattern View name pattern
     * @param includeSystemTables Indicate whether to include system tables in search
     * @return String[][] List of views matching search filters
     * @throws Exception DOCUMENT ME!
     */
    public String[][] getViewsOnly(String catalog, String schemaPattern,
            String viewPattern, boolean includeSystemTables)
            throws Exception {
        String[] tableTypes;
        
        if (includeSystemTables) {
            String[] types = {VIEW, ALIAS, SYNONYM, SYSTEM_TABLE};
            tableTypes = types;
        } else {
            String[] types = {VIEW, ALIAS, SYNONYM};
            tableTypes = types;
        }
        
        return getTables(catalog, schemaPattern, viewPattern, tableTypes);
    }
    
    /**
     * Returns a list of tables and views matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param tablePattern Table/View name pattern
     * @param includeSystemTables Indicate whether to include system tables in search
     * @return String[][] List of tables and views matching search filters
     * @throws Exception DOCUMENT ME!
     */
    public String[][] getTablesAndViews(String catalog, String schemaPattern,
            String tablePattern,
            boolean includeSystemTables)
            throws Exception {
        String[] tableTypes;
        
        if (includeSystemTables) {
            String[] types = {TABLE, VIEW, ALIAS, SYNONYM, SYSTEM_TABLE};
            tableTypes = types;
        } else {
            String[] types = {TABLE, VIEW, ALIAS, SYNONYM};
            tableTypes = types;
        }
        
        return getTables(catalog, schemaPattern, tablePattern, tableTypes);
    }
    
    /**
     * Returns a list of tables/views matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param tablePattern Table/View name pattern
     * @param tableTypes List of table types to include (ex. TABLE, VIEW)
     * @return String[][] List of tables matching search filters
     * @throws Exception DOCUMENT ME!
     */
    public String[][] getTables(String catalog, String schemaPattern,
            String tablePattern, String[] tableTypes)
            throws Exception {
        errMsg = "";
        try {
            if (catalog.equals("")) {
                catalog = null;
            }
            if (schemaPattern.equals("")) {
                schemaPattern = null;
            }
            if (tablePattern.equals("")) {
                tablePattern = null;
            }
            
            if (tablePattern != null) {
                tablePattern = getJDBCSearchPattern(tablePattern);
            }
            
            ResultSet rs = dbmeta.getTables(catalog, schemaPattern, tablePattern, tableTypes);
            
            Vector v = new Vector();
            String[][] tables = null; // array of table structures: Name, Catalog, Schema
            
            while (rs.next()) {
                String tableCatalog = rs.getString("TABLE_CAT");
                String tableSchema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String tableType = rs.getString("TABLE_TYPE");
                
                if (tableCatalog == null) {
                    tableCatalog = "";
                }
                if (tableSchema == null) {
                    tableSchema = "";
                }
                
                // fill in table info
                String[] tableItem = new String[4];    // hold info for each table
                tableItem[NAME] = tableName;
                tableItem[CATALOG] = tableCatalog;
                tableItem[SCHEMA] = tableSchema;
                tableItem[TYPE] = tableType;
                
                // add table to Vector
                v.add(tableItem);
            }
            
            // now copy Vector to array to return back
            if (v.size() > 0) {
                tables = new String[v.size()][4];
                v.copyInto(tables);
            }
            rs.close();
            return tables;
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    
    /**
     * Gets the prepared statement metadata (parameters, resultsets).
     *
     * @param catalog Catalog name
     * @param schema Schema name
     * @param name Prepared statement name
     * @param sqlText SQL text of prepared statement
     * @return PrepStmt Prepared statement object
     * @throws Exception DOCUMENT ME!
     */
    public PrepStmt getPrepStmtMetaData(String catalog, String schema,
            String name, String sqlText)
            throws Exception {
        
        errMsg = "";
        checkPrepStmtMetaData = false;
        PrepStmt newPrepStmt = null;
        try {            
            
            // make sure there is some sql text for the prepared statement
            if ((sqlText == null) || (sqlText.equals(""))) {
                return null;
            }
            
            // fill in name and sql text
            newPrepStmt = new PrepStmt(name, catalog, schema, sqlText);
            
            // prepare the statement
            PreparedStatement pstmt = dbconn.prepareStatement(sqlText);
            
            // Parameter metadata only available through JDBC 3.0, JDK 1.4
            // get parameter meta data of the prepared statment from the DB connection
            Parameter[] parameters = null;
            parameters = getPrepStmtParameters(pstmt);
            newPrepStmt.setParameters(parameters);
            
            ResultSetColumn[] cols = null;
            
            // get the resultset metadata
            // of the prepared statment from the DB connection
            StringTokenizer tok = new StringTokenizer(sqlText);
            if (tok.hasMoreElements()) {
                String firstTok = (String) tok.nextElement();
                if(firstTok.equalsIgnoreCase("select")){
                    cols = getPrepStmtResultSetColumns(pstmt);
                }
            } else {
                cols=null;
            }
            
            
            // set the prepared statement's resultset columns
            newPrepStmt.setResultSetColumns(cols);
            
            checkPrepStmtMetaData = errPrepStmtParameters && errPrepStmtResultSetColumns;
            
            pstmt.close();
                        
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            checkPrepStmtMetaData = true;
            //throw e;
        }
		return newPrepStmt;
    }
    
    public PrepStmt getPrepStmtMetaData() throws Exception{
        PrepStmt newPrepStmt = null;
		try {
        newPrepStmt = getPrepStmtMetaData(null,null,null,sqlText);
        } catch(Exception e) {
            errMsg = e.getLocalizedMessage();
            //throw e;
        }
        return newPrepStmt;        
    }
    
    /**
     * Returns a list of procedures matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param procedurePattern Procedure name pattern
     * @return String[][] List of procedures matching search filters
     * @throws Exception DOCUMENT ME!
     */
    public String[][] getProcedures(String catalog, String schemaPattern,
            String procedurePattern)
            throws Exception {
        errMsg = "";
        String[][] procedures = null; // array of procedure structures: Name, Catalog, Schema, Type
        try {
            if (catalog.equals("")) {
                catalog = null;
            }
            if (schemaPattern.equals("")) {
                schemaPattern = null;
            }
            if (procedurePattern.equals("")) {
                procedurePattern = null;
            }
            
            if (procedurePattern != null) {
                procedurePattern = getJDBCSearchPattern(procedurePattern);
            }
            
            Vector v = new Vector();
            
            ResultSet rs = dbmeta.getProcedures(catalog, schemaPattern, procedurePattern);
            while (rs.next()) {
                String procedureCatalog = rs.getString("PROCEDURE_CAT");
                String procedureSchema = rs.getString("PROCEDURE_SCHEM");
                String procedureName = rs.getString("PROCEDURE_NAME");
                String procedureType = getProcedureTypeDescription(rs.getShort("PROCEDURE_TYPE"));
                
                if (procedureCatalog == null) {
                    procedureCatalog = "";
                }
                if (procedureSchema == null) {
                    procedureSchema = "";
                }
                
                // fill in procedure info
                String[] procedureItem = new String[4];    // hold info for each procedure
                procedureItem[NAME] = procedureName;
                procedureItem[CATALOG] = procedureCatalog;
                procedureItem[SCHEMA] = procedureSchema;
                procedureItem[TYPE] = procedureType;
                
                // add procedure to Vector
                v.add(procedureItem);
            }
            
            // now copy Vector to array to return back
            if (v.size() > 0) {
                procedures = new String[v.size()][4];
                v.copyInto(procedures);
            }
            rs.close();
            } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            //throw e;
        }
		return procedures;
    }
    
    /**
     * Returns a list of primary keys for a table.
     *
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param tname Table name
     * @return List List of primary keys
     * @throws Exception DOCUMENT ME!
     */
    public List getPrimaryKeys(String tcatalog, String tschema, String tname)
    throws Exception {
        List pkList = Collections.EMPTY_LIST;
        ResultSet rs = null;
        
        errMsg = "";
        try {
            if (tcatalog.equals("")) {
                tcatalog = null;
            }
            if (tschema.equals("")) {
                tschema = null;
            }
            
            rs = dbmeta.getPrimaryKeys(tcatalog, tschema, tname);
            pkList = KeyColumn.createPrimaryKeyColumnList(rs);
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* Ignore */;
                }
            }
        }
        
        return pkList;
    }
    
    /**
     * Returns a list of foreign keys for a table.
     *
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param tname Table name
     * @return List List of foreign keys
     * @throws Exception DOCUMENT ME!
     */
    public List getForeignKeys(String tcatalog, String tschema, String tname)
    throws Exception {
        errMsg = "";
        List fkList = Collections.EMPTY_LIST;
        ResultSet rs = null;
        
        try {
            if (tcatalog.equals("")) {
                tcatalog = null;
            }
            if (tschema.equals("")) {
                tschema = null;
            }
            rs = dbmeta.getImportedKeys(tcatalog, tschema, tname);
            fkList = ForeignKeyColumn.createForeignKeyColumnList(rs);
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* Ignore */;
                }
            }
        }
        
        return fkList;
    }
    
    
    /**
     * Gets the procedure metadata (parameters).
     *
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param pname Procedure name
     * @param ptype Procedure type
     * @return Procedure object
     * @throws Exception DOCUMENT ME!
     */
    public Procedure getProcedureMetaData()
            throws Exception {
    	return getProcedureMetaData(null, null, null, sqlText);
    	}
    
    /**
     * Gets the procedure metadata (parameters).
     *
     * @param pcatalog Catalog name
     * @param pschema Schema name
     * @param pname Procedure name
     * @param ptype Procedure type
     * @return Procedure object
     * @throws Exception DOCUMENT ME!
     */
    public Procedure getProcedureMetaData(String pcatalog, String pschema, 
            String pname, String ptype) 
	throws Exception {
    	Procedure newProcedure = new Procedure(pname, pcatalog, pschema, ptype);
		
    	try {
		// create a new procedure object
		Vector v = new Vector();
		
		if (pcatalog.equals("")) {
		pcatalog = null;
		}
		if (pschema.equals("")) {
		pschema = null;
		}
		
		int colCount = 0;
		boolean isFunction = false;
		boolean hasParameters = true;
		// indicates if the procedure is within a package or standalone
		boolean isPackaged = true;
		
		ResultSetColumn resultCol = new ResultSetColumn();
		ArrayList paramIndices = new ArrayList();   // arraylist to hold the indices of the paramters that return resultsets
		ArrayList result = new ArrayList();     // arraylist to hold ResultSetColumns objects
		
		// check if the procedure is within a package or not
		if(pcatalog == null || pcatalog.trim().equalsIgnoreCase("")) {
		isPackaged = false;
		}
		
		
		
		dbmeta=dbconn.getMetaData();
		// get procedure parameter information
		ResultSet rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, "%");
		
		Parameter[] parameters = null;
		int pos = 0;
		int paramIndex=0;
		boolean hasReturn = false;
		CallableStatement cstmt = dbconn.prepareCall(sqlText);
		while (rs.next()) {
		pos++;
		String parmName = rs.getString("COLUMN_NAME");
		if(rs.getShort("COLUMN_TYPE") == DatabaseMetaData.procedureColumnReturn){
			// this is a function, so set the flag to true
			isFunction = true;
		}
		colCount++;
		if (parmName != null) {
		// strip off "@" in front of parameter name
		if (parmName.charAt(0) == '@') {
		parmName = parmName.substring(1);
		}
		} else {
		// parameter name is not return - call it "param<pos>"
		parmName = "param" + String.valueOf(pos);
		}
		String sqlType = getSQLTypeDescription(rs.getInt("DATA_TYPE"));
		String javaType = getJavaFromSQLTypeDescription(sqlType);
		//added abey for Procedure ResultSet
		int dataType = rs.getInt("DATA_TYPE");
		if((dataType==java.sql.Types.OTHER)&&(rs.getString("TYPE_NAME").equalsIgnoreCase("REF CURSOR"))){
		sqlType = "RESULTSET";
		javaType = "java.sql.ResultSet";
		}
		String paramType = getParamTypeDescription(rs.getShort("COLUMN_TYPE"));
		
		if (paramType.equals("RETURN") || paramType.equals("OUT") || paramType.equals("INOUT")) {
			hasReturn = true;
		}
		// create a parameter and add it to the vector
		Parameter parm = new Parameter(parmName, javaType);
		
		parm.setJavaType(javaType);
		parm.setSqlType(sqlType);
		parm.setParamType(paramType);
				
		paramIndex++;
		String parameterName = rs.getString("COLUMN_NAME");
		int targetSqlType = rs.getInt("DATA_TYPE");
		int colType = rs.getShort("COLUMN_TYPE");
		String type_Name = rs.getString("TYPE_NAME");
		
		if ( colType == DatabaseMetaData.procedureColumnIn) {
			if ((targetSqlType == 1111) && (type_Name.equals("PL/SQL TABLE"))) {
			targetSqlType = -14;
			}
			
			if ((targetSqlType == 1111) && (type_Name.equals("PL/SQL RECORD"))) {
			targetSqlType = -14;
			}
			cstmt.setNull(paramIndex, targetSqlType);
			}
			
			if (colType == DatabaseMetaData.procedureColumnInOut || colType == DatabaseMetaData.procedureColumnOut) {
			try {
			// if the parameter is a cursor type, add its index to the arraylist
			if ((targetSqlType == 1111) && (type_Name.equals("REF CURSOR"))) {
			targetSqlType = -10;
			paramIndices.add(new Integer(paramIndex));
			}
			cstmt.registerOutParameter(paramIndex, targetSqlType);
			} catch(SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			checkProcMetaData = true;
			//throw e;
			}
			}
			
			// check if the parameter is RETURN type (i.e. it is a function)
			if (colType == DatabaseMetaData.procedureColumnReturn) {
			try {
			// if the parameter is a cursor type, add its index to the arraylist
			if ((targetSqlType == 1111) && (type_Name.equals("REF CURSOR"))) {
			targetSqlType = -10;
			paramIndices.add(new Integer(paramIndex));
			}
			cstmt.registerOutParameter(paramIndex, targetSqlType);
			} catch(SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			checkProcMetaData = true;
			//throw e;
			}
			}
		
		// add to vector
			if(sqlType != "RESULTSET"){
				v.add(parm);
			}
		}
		
		rs.close();
		
		// now copy Vector to array
		if (v.size() > 0) {
		parameters = new Parameter[v.size()];
		v.copyInto(parameters);
		}
		
		// now set up parameters in the procedure to return
		newProcedure.setParameters(parameters);
		newProcedure.setHasReturn(hasReturn);
		///////////////////////////////////////////////////
		try {
		if(hasReturn) {
			boolean resultsAvailable = cstmt.execute();
			int count = -1;
			int numResults = paramIndices.size();
			
			Iterator paramIdxIter = paramIndices.iterator();
			
			// iterate through the resultsets returned, whose indices are stored in the arraylist
			while (paramIdxIter.hasNext()) {
			ArrayList resultArray = new ArrayList();    // arraylist to hold the objects of ResultSetColumn
			count += 1;
			// get the index (from the arraylist) of the parameter which is a resultset
			int index = ((Integer)paramIdxIter.next()).intValue();
			ResultSet paramRS ;
			ResultSetMetaData rsmd;
			// if the resultset returns nothing, set the metadata object to null
			try {
			paramRS = (ResultSet)cstmt.getObject(index);
			rsmd = paramRS.getMetaData();
			} catch(SQLException e) {
			rsmd = null;
			checkProcMetaData = true;
			}
			
			int rsmdColCount=0;
			if (rsmd != null) {
			rsmdColCount = rsmd.getColumnCount();
			}
			// scroll through the resultset column information
			for (int i = 1; i <= rsmdColCount; i++) {
			ResultSetColumn currCol = new ResultSetColumn();
			currCol.setName(rsmd.getColumnName(i));
			currCol.setSqlType(getSQLTypeDescription(rsmd.getColumnType(i)));
			currCol.setJavaType((String)SQLTOJAVATYPES.get(getSQLTypeDescription(rsmd.getColumnType(i))));
			
			// add ResultSetColumn object to the arraylist
			boolean addToArray = resultArray.add(currCol);
			}
			
			// add the arraylist having ResultSetColumn objects to the ResultSetColumns object
			// now add this ResultSetColumns object to the arraylist object (result)
			if(resultArray.size() > 0){
			ResultSetColumns rsColbj = new ResultSetColumns();
			rsColbj.setColumns(resultArray);
			rsColbj.setName(pname + "_" + count);
			result.add(rsColbj);
			}
			}
		}
		} catch (SQLException e) {
		// resultset column metadata not supported
		System.out.println("\nException occurred: " + e.getClass().getName()+ ", "+ e.getMessage());
		e.printStackTrace();
		errMsg = e.getLocalizedMessage();
		checkProcMetaData = true;
		//throw e;
		}
		catch (NullPointerException npe) {
		System.out.println("\nException occurred: " + npe.getClass().getName()+ ", " + npe.getMessage());
		npe.printStackTrace();
		errMsg = npe.getLocalizedMessage();
		checkProcMetaData = true;
		//throw npe;
		}
		catch (Exception e) {
		// resultset column metadata not supported
		System.out.println("\nException occurred: " + e.getClass().getName()+ ", " + e.getMessage());
		e.printStackTrace();
		errMsg = e.getLocalizedMessage();
		checkProcMetaData = true;
		}
		
		// add the arraylist object to the Procedure object
		newProcedure.setResultSetColumns(result);
				
		
		///////////////////////////////////////////////////
		} catch (Exception e) {
		e.printStackTrace();
		checkProcMetaData = true;
		//throw e;
		}
		return newProcedure;
		
	}
    
    /**
     * Gets the table metadata (columns).
     *
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param tname Table name
     * @param ttype Table type
     * @return Table object
     * @throws Exception DOCUMENT ME!
     */
    public Table getTableMetaData(String tcatalog, String tschema, String tname, String ttype)
    throws Exception {
        errMsg = "";
        ResultSet rs = null;
        
        try {
            // create a new Table object
            Table newTable = new Table(tname, tcatalog, tschema, ttype);
            Vector v = new Vector();
            
            if (tcatalog.equals("")) {
                tcatalog = null;
            }
            
            if (tschema.equals("")) {
                tschema = null;
            }
            
            // get table column information
            rs = dbmeta.getColumns(tcatalog, tschema, tname, "%");
            
            TableColumn[] columns = null;
            
            while (rs.next()) {
                String defaultValue = rs.getString("COLUMN_DEF");
                
                int sqlTypeCode = rs.getInt("DATA_TYPE");
                
                String colName = rs.getString("COLUMN_NAME");
                String sqlType = getSQLTypeDescription(sqlTypeCode);
                String javaType = getJavaFromSQLTypeDescription(sqlType);
                
                int position = rs.getInt("ORDINAL_POSITION");
                
                int scale = rs.getInt("DECIMAL_DIGITS");
                int precision = rs.getInt("COLUMN_SIZE");
                int radix = rs.getInt("NUM_PREC_RADIX");
                
                // create a table column and add it to the vector
                TableColumn col = new TableColumn(colName, javaType);
                boolean isNullable = false;
                if (rs.getString("IS_NULLABLE").equals("YES")) {
                    isNullable = true;
                }
                col.setJavaType(javaType);
                col.setSqlType(sqlType);
                col.setIsNullable(isNullable);
                col.setIsSelected(true);
                col.setIsPrimaryKey(false);
                col.setIsForeignKey(false);
                col.setSqlTypeCode(sqlTypeCode);
                
                col.setOrdinalPosition(position);
                col.setNumericPrecision(precision);
                col.setNumericScale(scale);
                col.setNumericRadix(radix);
                
                if (defaultValue != null) {
                    col.setDefaultValue(defaultValue.trim());
                }
                
                // add to vector
                v.add(col);
            }
            
            // now copy Vector to array
            if (v.size() > 0) {
                columns = new TableColumn[v.size()];
                v.copyInto(columns);
            }
            
            // now set up columns in the table to return
            newTable.setColumns(columns);
            
            // now check the columns that are primary keys
            checkPrimaryKeys(newTable);
            
            // now check the columns that are foreign keys
            checkForeignKeys(newTable);
            
            // catch exceptions for this as index only makes sense for
            // tables and not views (can't check the table type because it's dependent on driver)
            try {
                // get index info for this table
                rs = dbmeta.getIndexInfo(tcatalog, tschema, tname, false, true);
                newTable.setIndexList(IndexColumn.createIndexList(rs));
            } catch (Exception e) {
                // ignore and continue
                errMsg = e.getLocalizedMessage();
            }
            
            return newTable;
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* Ignore... */;
                }
            }
        }
    }
    
    /**
     * Converts a JDBC SQL Type to a Java Type.
     *
     * @param sqlType JDBC SQL Type
     * @return Java Type
     */
    public String getJavaFromSQLTypeDescription(String sqlType) {
        Object t;
        String javaType = "java.lang.String";    // default value
        t = SQLTOJAVATYPES.get(sqlType);
        
        if (t != null) {
            javaType = t.toString();
        }
        
        return javaType;
    }
    
    /**
     * Converts the numeric value of a JDBC SQL type to
     * a display string.
     *
     * @param type JDBC numeric SQL type value
     * @return JDBC SQL type string
     */
    public static String getSQLTypeDescription(int type) {
        // returns a String representing the passed in numeric
        // SQL type
        switch (type) {
            case java.sql.Types.ARRAY:
                return "ARRAY";
            case java.sql.Types.BIGINT:
                return "BIGINT";
            case java.sql.Types.BINARY:
                return "BINARY";
            case java.sql.Types.BIT:
                return "BIT";
            case java.sql.Types.BLOB:
                return "BLOB";
            case 16:
                //case java.sql.Types.BOOLEAN:
                return "BOOLEAN";
            case java.sql.Types.CHAR:
                return "CHAR";
            case java.sql.Types.CLOB:
                return "CLOB";
            case 70:
                //case java.sql.Types.DATALINK:
                return "DATALINK";
            case java.sql.Types.DATE:
                return "DATE";
            case java.sql.Types.DECIMAL:
                return "DECIMAL";
            case java.sql.Types.DOUBLE:
                return "DOUBLE";
            case java.sql.Types.FLOAT:
                return "FLOAT";
            case java.sql.Types.INTEGER:
                return "INTEGER";
            case java.sql.Types.JAVA_OBJECT:
                return "JAVA_OBJECT";
            case java.sql.Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case java.sql.Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case java.sql.Types.NULL:
                return "NULL";
            case java.sql.Types.NUMERIC:
                return "NUMERIC";
            case java.sql.Types.OTHER:
                return "OTHER";
            case java.sql.Types.REAL:
                return "REAL";
            case java.sql.Types.REF:
                return "REF";
            case java.sql.Types.SMALLINT:
                return "SMALLINT";
            case java.sql.Types.STRUCT:
                return "STRUCT";
            case java.sql.Types.TIME:
                return "TIME";
            case java.sql.Types.TIMESTAMP:
                return "TIMESTAMP";
            case java.sql.Types.TINYINT:
                return "TINYINT";
            case java.sql.Types.VARBINARY:
                return "VARBINARY";
            case java.sql.Types.VARCHAR:
                return "VARCHAR";
        }
        // all others default to OTHER
        return "OTHER";
    }
    
    /**
     * Converts a text representation of a JDBC SQL type to
     * a display string.
     *
     * @param sqlText JDBC SQL type string
     * @return JDBC numeric SQL type value
     */
    public static int getSQLTypeCode(String sqlText) {
        if (sqlText == null) {
            throw new IllegalArgumentException(
                    "Must supply non-null String value for sqlText.");
        }
        
        sqlText = sqlText.trim().toUpperCase();
        for (int i = 0; i < SQLTYPES.length; i++) {
            if (SQLTYPES[i].equals(sqlText)) {
                return SQLTYPE_CODES[i];
            }
        }
        
        return java.sql.Types.OTHER;
    }
    
    private String getJavaTypeDescription(int type) {
        // converts a numeric SQL type to a Java type
        String javaType = "java.lang.String";
        
        switch (type) {
            case java.sql.Types.ARRAY:
                javaType = "java.sql.ARRAY";
            case java.sql.Types.BIGINT:
                javaType = "long";
            case java.sql.Types.BINARY:
                javaType = "byte[]";
            case java.sql.Types.BIT:
                javaType = "boolean";
            case java.sql.Types.BLOB:
                javaType = "java.sql.Blob";
                //case java.sql.Types.BOOLEAN:
                //    javaType = "boolean";
            case java.sql.Types.CHAR:
                javaType = "java.lang.String";
            case java.sql.Types.CLOB:
                javaType =  "java.sql.Clob";
            case java.sql.Types.DATE:
                javaType = "java.sql.Date";
            case java.sql.Types.DECIMAL:
                javaType = "java.math.BigDecimal";
            case java.sql.Types.DOUBLE:
                javaType = "double";
            case java.sql.Types.FLOAT:
                javaType = "double";
            case java.sql.Types.INTEGER:
                javaType = "int";
            case java.sql.Types.LONGVARBINARY:
                javaType = "byte[]";
            case java.sql.Types.LONGVARCHAR:
                javaType = "java.lang.String";
            case java.sql.Types.NUMERIC:
                javaType = "java.math.BigDecimal";
                //case java.sql.Types.OTHER:
                //    javaType = "java.sql.Blob";
            case java.sql.Types.REAL:
                javaType = "float";
            case java.sql.Types.REF:
                javaType = "java.sql.Ref";
            case java.sql.Types.SMALLINT:
                javaType = "short";
            case java.sql.Types.STRUCT:
                javaType = "java.sql.Struct";
            case java.sql.Types.TIME:
                javaType = "java.sql.Time";
            case java.sql.Types.TIMESTAMP:
                javaType = "java.sql.Timestamp";
            case java.sql.Types.TINYINT:
                javaType = "byte";
            case java.sql.Types.VARBINARY:
                javaType = "byte[]";
            case java.sql.Types.VARCHAR:
                javaType = "java.lang.String";
        }
        return javaType;
    }
    
    private String getParamTypeDescription(int type) {
        String descr = "";
        
        if (type == DatabaseMetaData.procedureColumnIn) {
            descr = "IN";
        } else if (type == DatabaseMetaData.procedureColumnInOut) {
            descr = "INOUT";
        } else if (type == DatabaseMetaData.procedureColumnOut) {
            descr = "OUT";
        } else if (type == DatabaseMetaData.procedureColumnReturn) {
            descr = "RETURN";
        } else if (type == DatabaseMetaData.procedureColumnResult) {
            descr = "RESULT";
        } else {
            descr = "UNKNOWN";
        }
        
        return descr;
    }
    
    private String getProcedureTypeDescription(int type) {
        // converts the numeric procedure type code to a string description
        String descr = "";
        if (type == DatabaseMetaData.procedureNoResult) {
            descr = Procedure.PROCEDURE;
        } else if (type == DatabaseMetaData.procedureReturnsResult) {
            descr = Procedure.FUNCTION;
        } else if (type == DatabaseMetaData.procedureResultUnknown) {
            descr = Procedure.UNKNOWN;
        } else {
            descr = Procedure.UNKNOWN;
        }
        
        return descr;
    }
    
    private String getPrepStmtParamTypeDescription(int type) {
        String descr = "";
        
        if (type == ParameterMetaData.parameterModeIn) {
            descr = "IN";
        } else if (type == ParameterMetaData.parameterModeInOut) {
            descr = "INOUT";
        } else if (type == ParameterMetaData.parameterModeOut) {
            descr = "OUT";
        } else if (type == ParameterMetaData.parameterModeUnknown) {
            descr = "UNKNOWN";
        } else {
            descr = "UNKNOWN";
        }
        
        return descr;
    }
    
    private String replaceAllChars(String orig, char oldChar, String replStr) {
        String newString = "";
        
        for (int i = 0; i < orig.length(); i++) {
            if (orig.charAt(i) == oldChar) {
                newString = newString + replStr;
            } else {
                newString = newString + orig.charAt(i);
            }
        }
        return newString;
    }
    
    /** Main routine
     *
     * @param args command line arguments
     */
    public static void main(String args[]) {
        DBMetaData myDB = new DBMetaData();
        
        String driver = "";
        String url = "";
        String user = "";
        String pwd = "";
        
        // connect and get metadata
        
        // Oracle Native
        //
        driver = "oracle.jdbc.OracleDriver";
        url = "jdbc:oracle:thin:@jlongbgx:1521:jlongbgx";
        user = "dgdb";
        pwd = "dgdb";
        //
        
        // Sybase Native
      /*
      driver = "com.sybase.jdbc2.jdbc.SybDriver";
      url = "jdbc:sybase:Tds:Atlas:4100?DYNAMIC_PREPARE=true";
      user = "dgdb";
      pwd = "dgdbdgdb";
       */
        
        //DataDirect Oracle branded
      /*
      driver = "com.SeeBeyond.jdbc.oracle.OracleDriver";
      url = "jdbc:SeeBeyond:oracle://jlongbgx:1521;SID=jlongbgx";
      user = "dgdb";
      pwd = "dgdb";
       */
        
        //Merant Sybase
      /*
      driver = "com.SeeBeyond.jdbc.sybase.SybaseDriver";
      url = "jdbc:SeeBeyond:sybase://Atlas:4100";
      user = "dgdb";
      pwd = "dgdbdgdb";
       */
        
        //Merant SQL Server
      /*
      driver = "com.ddtek.jdbc.sqlserver.SQLServerDriver";
      url = "jdbc:datadirect:sqlserver://rpoon";
      user = "dgdb";
      pwd = "dgdbdgdb";
       */
        
        //Merant DB2
      /*
      driver = "com.ddtek.jdbc.db2.DB2Driver";
      url = "jdbc:datadirect:db2://testdb2:50000;DatabaseName=SAMPLE;"
          + "CollectionId=DEFAULT;PackageName=SUSAN";
      user = "db2admin";
      pwd = "db2admin";
       */
        
        try {
            myDB.connectDB(driver, url, user, pwd);
            System.out.println("Successfully connected to " + url + ".");
            
            // get DB name
            System.out.println("Database product name: " + myDB.getDBName());
            
            // get DB type
            System.out.println("Database Type: " + myDB.getDBType());
            
            // get the schema names
            String[] schemaList = myDB.getSchemas();
            // iterate and print schemas
            if (schemaList != null) {
                for (int i = 0; i < schemaList.length; i++) {
                    System.out.println(schemaList[i]);
                }
            }
            System.out.println();
            
            // get tables - pattern matching
            System.out.println("TABLES:");
            String[][] tableList = myDB.getTablesAndViews("", "", "DB*", false);
            // iterate and print tables
            if (tableList != null) {
                for (int i = 0; i < tableList.length; i++) {
                    String[] currTable = tableList[i];
                    System.out.println(currTable[myDB.CATALOG] + "."
                            + currTable[myDB.SCHEMA] + "." + currTable[myDB.NAME]
                            + ":Type=" + currTable[myDB.TYPE]);
                }
                System.out.println("Table Count = " + tableList.length);
            }
            
            System.out.println();
            
            // table columns - with datatypes
            System.out.println("TABLE COLUMNS:");
            //      Table mytable = myDB.getTableMetaData("dgdb","dbo","db_employee");
            Table mytable = myDB.getTableMetaData("", "DGDB", "DB_EMPLOYEE", TABLE);
            if (mytable != null) {
                TableColumn[] tableColumns = mytable.getColumns();
                // iterate and print table columns
                if (tableColumns != null) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        TableColumn currColumn = tableColumns[i];
                        System.out.println("Column " + (i + 1) + ":"
                                + currColumn.getName() + "("
                                + currColumn.getJavaType()
                                + "):IsNullable=" + currColumn.getIsNullable()
                                + ":IsSelected=" + currColumn.getIsSelected()
                                + ":IsPrimaryKey=" + currColumn.getIsPrimaryKey()
                                + ":IsForeignKey=" + currColumn.getIsForeignKey()
                                + ":Precision=" + currColumn.getNumericPrecision()
                                + ":Scale=" + currColumn.getNumericScale());
                    }
                }
            }
            
            System.out.println();
            
            // get procedures - pattern matching
            System.out.println("PROCEDURES:");
            String[][] procList = myDB.getProcedures("", "", "P*");
            // iterate and print procedures
            if (procList != null) {
                for (int i = 0; i < procList.length; i++) {
                    String[] currProc = procList[i];
                    System.out.println(currProc[myDB.CATALOG] + "."
                            + currProc[myDB.SCHEMA] + "." + currProc[myDB.NAME]
                            + ":" + currProc[myDB.TYPE]);
                }
                System.out.println("Procedure Count = " + procList.length);
            }
            
            System.out.println();
            
            // procedure parameters - with datatypes
            System.out.println("PROCEDURE PARAMETERS:");
            Procedure myproc = myDB.getProcedureMetaData("", "DGDB", "PLUSONE", "PROCEDURE");
            if (myproc != null) {
                Parameter[] procParams = myproc.getParameters();
                // iterate and print procedure parameters
                if (procParams != null) {
                    for (int i = 0; i < procParams.length; i++) {
                        Parameter currParam = procParams[i];
                        System.out.println("Parameter "
                                + currParam.getOrdinalPosition() + ":"
                                + currParam.getName() + "(" + currParam.getJavaType()
                                + "):ParamType=" + currParam.getParamType()
                                + ":NumericPrecision=" + currParam.getNumericPrecision()
                                + ":NumericScale=" + currParam.getNumericScale()
                                + ":IsNullable=" + currParam.getIsNullable());
                    }
                }
            }
            
            System.out.println();
            
            // get prepared statement metadata and iterate through resultset columns
            PrepStmt myPrep = myDB.getPrepStmtMetaData("", "", "prep1",
                    "select * from db_employee where RATE=?");
            if (myPrep != null) {
                System.out.println("PREPARED STATMENT:" + myPrep.getName());
                ResultSetColumn[] rsCols = myPrep.getResultSetColumns();
                // iterate and print procedure parameters
                if (rsCols != null) {
                    for (int i = 0; i < rsCols.length; i++) {
                        ResultSetColumn currCol = rsCols[i];
                        System.out.println("RS Col "
                                + currCol.getOrdinalPosition() + ":"
                                + currCol.getName() + "(" +  currCol.getJavaType()
                                + "):NumericPrecision="
                                + currCol.getNumericPrecision()
                                + ":NumericScale=" + currCol.getNumericScale()
                                + ":IsNullable=" + currCol.getIsNullable());
                    }
                }
            }
            
            System.out.println();
            
            myDB.disconnectDB();
            System.out.println("Successfully disconnected from " + url + ".");
            
        } catch (Exception e) {
            // get error msg to display
            String errMsg = myDB.getErrString();
            System.out.println("Exception: " + errMsg);
        }
    }
    
    /**
     * Get String representing current error message, if any.
     *
     * @return error message
     */
    public String getErrString() {
        return errMsg;
    }
    
    private Parameter[] getPrepStmtParameters(PreparedStatement pstmt)throws Exception {
        String errMsg = "";
        errPrepStmtParameters = false;
        Parameter[] parameters = null;
        
        try {
            
            ParameterMetaData pmeta = pstmt.getParameterMetaData();
            if (pmeta != null) {
                int numParams = pmeta.getParameterCount();
                if (numParams > 0) {
                    parameters = new Parameter[numParams];
                    // get info for each parameter
                    for (int i = 1; i <= numParams; i++) {
                        Parameter currParam = new Parameter();
                        String paramname = "param" + String.valueOf(i);
                        currParam.setName(paramname);
                        
                        // try to get the sql type info - default to VARCHAR
                        String sqltype = "VARCHAR";
                        try {
                            sqltype = getSQLTypeDescription(pmeta.getParameterType(i));
                        } catch (SQLException e) {
                            // default to VARCHAR if we can't get the type
                            errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                            //throw e;
                        }
                        
                        // try to get the java type info - default to String
                        /**
                         * Changing it to not use metadata class name and instead use the HashMap SQLTOJAVATYPES.
                         * Without the change the parameter datatypes java.lang.Double and WSDLGenerator look up list
                         * exepects native type double, float, short etc.
                         **/
                        String javatype = "java.lang.String";
                        javatype = getJavaFromSQLTypeDescription(sqltype);
                        
                        // try to get the numeric precision, default to 0
                        int precision = 0;
                        try {
                            precision = pmeta.getPrecision(i);
                        } catch (SQLException e) {
                            errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                            //throw e;
                        }
                        
                        // try to get the numeric scale, default to 0
                        int scale = 0;
                        try {
                            scale = pmeta.getScale(i);
                        } catch (SQLException e) {
                            errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                            //throw e;
                        }
                        
                        // try to get the param type, default to IN
                        // always default it since getParameterMode() in data direct 3.3 throws exception
                        // and 3.4 return UNKNOWN type
                        String paramType = "IN";
                        /*
                        try {
                            paramType = getPrepStmtParamTypeDescription(pmeta.getParameterMode((i)));
                        } catch (SQLException e) {
                            errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                        }
                         */
                        
                        // try to get is nullable, default to TRUE
                        boolean isNullable = true;
                        try {
                            if (pmeta.isNullable(i) == java.sql.ParameterMetaData.parameterNullable) {
                                isNullable = true;
                            } else {
                                isNullable = false;
                            }
                        } catch (SQLException e) {
                            errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                            //throw e;
                        }
                        
                        currParam.setJavaType(javatype);
                        currParam.setSqlType(sqltype);
                        currParam.setNumericPrecision(precision);
                        currParam.setNumericScale(scale);
                        currParam.setOrdinalPosition(i);
                        currParam.setParamType(paramType);
                        currParam.setIsNullable(isNullable);
                        
                        parameters[i - 1] = currParam;
                    }
                }
            }
        } catch (Exception e) {
            // parameter metadata not supported
            parameters = null;
            errPrepStmtParameters = true;
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            //throw e;
        }
        
        return parameters;
    }
    
    private ResultSetColumn[] getPrepStmtResultSetColumns(PreparedStatement pstmt) throws SQLException{
        String errMsg = "";
        errPrepStmtResultSetColumns = false;
        ResultSetColumn[] cols = null;
        try {
            ResultSetMetaData rsmd = pstmt.getMetaData();
            int count = 0;
            if (rsmd != null) {
                count = rsmd.getColumnCount();
            } else {
                errPrepStmtResultSetColumns = true;
            }
            if (count > 0) {
                // scroll through the resultset column information
                cols = new ResultSetColumn[count];
                for (int i = 1; i <= count; i++) {
                    ResultSetColumn currCol = new ResultSetColumn();
                    currCol.setName(rsmd.getColumnName(i));
                    currCol.setSqlType(getSQLTypeDescription(rsmd.getColumnType(i)));
                    currCol.setJavaType(getJavaFromSQLTypeDescription(currCol.getSqlType()));
                    currCol.setOrdinalPosition(i);
                    currCol.setNumericPrecision(rsmd.getPrecision(i));
                    currCol.setNumericScale(rsmd.getScale(i));
                    
                    if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
                        currCol.setIsNullable(true);
                    } else {
                        currCol.setIsNullable(false);
                    }
                    
                    cols[i - 1] = currCol;
                }
            }
            
        } catch (SQLException e) {
            // resultset column metadata not supported
            errPrepStmtResultSetColumns = true;
            cols = null;
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        }
        
        return cols;
    }
    
    
    
    public void getProcResultSetColumns(CallableStatement cstmt, Procedure proc)
            throws SQLException, NullPointerException {
    	    String errMsg = "";
	        int colCount = 0;
	        boolean isFunction = false;
			boolean hasReturn = false;
	        boolean hasParameters = true;
	        // indicates if the procedure is within a package or standalone
	        boolean isPackaged = true;
	        cstmt = dbconn.prepareCall(sqlText);
	        ArrayList paramIndices = new ArrayList();
	        ArrayList result = new ArrayList();
            int paramIndex=0;
            try {
            Parameter[] parameters = proc.getParameters();
            colCount = proc.getNumParameters();
            // loop through the list of parameters and register them
            if(colCount > 0) {
            for (int j = 0; j < colCount; j++) {
            	paramIndex++;
            	Parameter param = parameters[j];
                String parameterName = param.getName();
                String sqlType = param.getSqlType();
                int sqlTypeCode = getSQLTypeCode(sqlType);
                String colType = param.getParamType();
                cstmt.setNull(paramIndex, sqlTypeCode);
                
                if (colType.equalsIgnoreCase("INOUT") || colType.equalsIgnoreCase("OUT")) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if ((sqlTypeCode == 1111) && (colType.equals("OTHER"))) {
                            sqlTypeCode = java.sql.Types.OTHER;
                            paramIndices.add(new Integer(paramIndex));
                        }
                        cstmt.registerOutParameter(paramIndex, sqlTypeCode);
                    } catch(SQLException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                }
                
                // check if the parameter is RETURN type (i.e. it is a function)
                if (colType == "RETURN") {
                    try {

                        // if the parameter is a cursor type, add its index to the arraylist
                        if ((sqlTypeCode == 1111) && (colType.equals("OTHER"))) {
                            sqlTypeCode = java.sql.Types.OTHER;
                            paramIndices.add(new Integer(paramIndex));
                        }
                        hasReturn = true;
						cstmt.registerOutParameter(paramIndex, sqlTypeCode);
                    } catch(SQLException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
            }
            // execute the stored procedure
			if(hasReturn) {
            boolean resultsAvailable = cstmt.execute();
			
            int count = -1;
            int numResults = paramIndices.size();
            
            Iterator paramIdxIter = paramIndices.iterator();
            
            // iterate through the resultsets returned, whose indices are stored in the arraylist
            while (paramIdxIter.hasNext()) {
                ArrayList resultArray = new ArrayList();    // arraylist to hold the objects of ResultSetColumn
                count += 1;
                // get the index (from the arraylist) of the parameter which is a resultset
                int index = ((Integer)paramIdxIter.next()).intValue();
                ResultSet paramRS ;
                ResultSetMetaData rsmd;
                // if the resultset returns nothing, set the metadata object to null
                try {
                    paramRS = (ResultSet)cstmt.getObject(index);
                    rsmd = paramRS.getMetaData();
                } catch(SQLException e) {
                    rsmd = null;
                }
                
                int rsmdColCount=0;
                if (rsmd != null) {
                    rsmdColCount = rsmd.getColumnCount();
                }
                // scroll through the resultset column information
                for (int i = 1; i <= rsmdColCount; i++) {
                    ResultSetColumn currCol = new ResultSetColumn();
                    currCol.setOrdinalPosition(i);
                    currCol.setName(rsmd.getColumnName(i));
                    currCol.setLabel(rsmd.getColumnLabel(i));
                    currCol.setSqlType(getSQLTypeDescription(rsmd.getColumnType(i)));
                    currCol.setJavaType((String)SQLTOJAVATYPES.get(getSQLTypeDescription(rsmd.getColumnType(i))));
                    
                    if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
                        currCol.setIsNullable(true);
                    } else {
                        currCol.setIsNullable(false);
                    }
                    // add ResultSetColumn object to the arraylist
                    boolean addToArray = resultArray.add(currCol);
                }
                
                // add the arraylist having ResultSetColumn objects to the ResultSetColumns object
                // now add this ResultSetColumns object to the arraylist object (result)
                if(resultArray.size() > 0){
                    ResultSetColumns rsColbj = new ResultSetColumns();
                    rsColbj.setColumns(resultArray);
                    rsColbj.setName("proc_" + count);
                    result.add(rsColbj);
                }
            }
		   }
        } catch (SQLException e) {
            // resultset column metadata not supported
            System.out.println("\nException occurred: " + e.getClass().getName()+ ", "+ e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        } catch (NullPointerException npe) {
            System.out.println("\nException occurred: " + npe.getClass().getName()+ ", " + npe.getMessage());
            npe.printStackTrace();
            errMsg = npe.getLocalizedMessage();
            throw npe;
        } catch (Exception e) {
            // resultset column metadata not supported
            System.out.println("\nException occurred: " + e.getClass().getName()+ ", " + e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }
        
        // add the arraylist object to the Procedure object
        proc.setResultSetColumns(result);
        
    }
    
    
    /**
     * added by Bobby to retrieve the resultset metadata of an SQL query
     *
     * @param pcatalog Catalog (package) name of the procedure
     * @param pschema Schema name of the procdure
     * @param pname Name of the procedure
     * @param sqlText Text of the procedure/function
     *
     * @return Procedure resultset encapsulated in a Procedure object
     *
     * @throws SQLException, NullPointerException
     */
    public Procedure getQueryResultSet(String pcatalog,
            String pschema,
            String pname,
            String sqlText)
            throws SQLException, NullPointerException {
        String errMsg = "";
        Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
        ResultSetColumns[] result = null;
        ArrayList resultList = new ArrayList();
        
        try {
            DatabaseMetaData dbmeta = dbconn.getMetaData();
            Statement stmt = dbconn.createStatement();
            
            // retrieve the names of the fields in the select query
            // required if the query contains calculated fields
            String[] queryFields = getQueryFields(sqlText);
            
            // execute the SQL query and retrieve the resultset
            ResultSet rs = stmt.executeQuery(sqlText);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            
            for (int i = 1; i <= numColumns; i++) {
                ResultSetColumn resultCol = new ResultSetColumn();
                resultCol.setOrdinalPosition(i);
                String colName = rsmd.getColumnName(i).trim();
                String colLabel = rsmd.getColumnLabel(i).trim();
                
                // check if the column names/labels are returned as null
                // (this happens in the case of derived/calculated fields and no aliases are provided)
                if (colName.equalsIgnoreCase("") || colName == null) {
                    // parse the query string to extract derived field names
                    String strFieldName = queryFields[i-1];
                    resultCol.setName(strFieldName);
                } else {
                    resultCol.setName(colName);
                }
                
                if (colLabel.equalsIgnoreCase("") || colLabel == null) {
                    // parse the query string to extract derived field names
                    String strFieldName = queryFields[i-1];
                    resultCol.setLabel(strFieldName);
                } else {
                    resultCol.setLabel(colLabel);
                }
                
                resultCol.setSqlType(getSQLTypeDescription(rsmd.getColumnType(i)));
                resultCol.setJavaType((String)SQLTOJAVATYPES.get(getSQLTypeDescription(rsmd.getColumnType(i))));
                
                
                if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
                    resultCol.setIsNullable(true);
                } else {
                    resultCol.setIsNullable(false);
                }
                
                // add ResultSetColumn object to the arraylist
                boolean addToArray = resultList.add(resultCol);
            }
            
            result = new ResultSetColumns[1];
            result[0] = new ResultSetColumns();
            // add the arraylist to the ResultSetColumns object
            result[0].setColumns(resultList);
            result[0].setName(pname + "_0");
        } catch (SQLException e) {
            System.out.println("\nException occurred: " + e.getClass().getName()+ ", " + e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        } catch (NullPointerException npe) {
            System.out.println("\nException occurred: " + npe.getClass().getName()+ ", " + npe.getMessage());
            npe.printStackTrace();
            errMsg = npe.getLocalizedMessage();
            throw npe;
        } catch (Exception e) {
            // resultset column metadata not supported
            System.out.println("\nException occurred: " + e.getClass().getName()+ ", " + e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }
        
        // add the ResultSetColumns array to the Procedure object
        procResult.setResultSetColumns(result);
        return procResult;
    }
    
    /**
     * added by Bobby to retrieve the text of a procedure/function
     *
     * @param Procedure Procedure object representing a procedure or function
     *
     * @return String Text of the procedure or function
     */
    public String getProcedureText(Procedure proc) {
        String procText = "";
        String stmtString = "";
        String procName = proc.getName();
        String packageName = proc.getCatalog();
        
        // construct the SQL select query depending on whether
        // the procedure or function is part of a package or not
        if (packageName.equals("") || packageName == null ) {
            stmtString = "select text from user_source where name = '" + procName + "'";
        } else {
            stmtString = "select text from user_source where name = '" + packageName + "'";
        }
        
        try {
            Statement stmt = dbconn.createStatement();
            ResultSet rsProcText = stmt.executeQuery(stmtString);
            
            while (rsProcText.next()) {
                procText += rsProcText.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("\nException occurred: " + e.getClass().getName()+ ", " + e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }
        
        return procText;
    }
    
    /**
     * added by Bobby to parse an SQL query string
     * and return a String array containing the names of the select fields
     *
     * @param sqlQuery the SQL query string to be parsed
     *
     * @return String array containing the list of derived field names
     */
    private String[] getQueryFields(String sqlQuery) {
        String[] strFieldNames = null;
        
        String queryString = sqlQuery.toUpperCase().trim();
        int fromIndex = queryString.indexOf("FROM");
        
        // extract the part of the query between the SELECT and the FROM keywords
        String searchString = sqlQuery.substring(7, fromIndex);
        
        StringTokenizer stFields = new StringTokenizer(searchString, ",");
        int noTokens = stFields.countTokens();
        strFieldNames = new String[noTokens];
        
        int tokenNo = 0;
        // extract the string tokens fom the query (the derived columns)
        while (stFields.hasMoreTokens()) {
            strFieldNames[tokenNo] = stFields.nextToken().trim();
            tokenNo++;
        }
        
        return strFieldNames;
    }
    
    public DBMetaData(Connection conn, String sqlText) {
        this.dbconn = conn;
        this.sqlText = sqlText;
    }
    
    public DBMetaData() {        
    }
    
    public String getSQLText(){
        return this.sqlText;
    }
    
    
	
	
	public Procedure getProcResultSetColumns(String pcatalog,
            String pschema,
            String pname,
            String columnName,
            Procedure procResult)
		throws SQLException, NullPointerException {
		
		checkProcMetaData = false;
		int colCount = 0;
		boolean isFunction = false;
		boolean hasParameters = true;
		// indicates if the procedure is within a package or standalone
		boolean isPackaged = true;
		
		//Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
		ResultSetColumn resultCol = new ResultSetColumn();
		ArrayList paramIndices = new ArrayList();   // arraylist to hold the indices of the paramters that return resultsets
		ArrayList result = new ArrayList();     // arraylist to hold ResultSetColumns objects
		
		// check if the procedure is within a package or not
		if(pcatalog.trim().equalsIgnoreCase("") || pcatalog == null) {
		isPackaged = false;
		}
		try {
		ResultSet rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
		
		// loop to identify if the procedure is actually a function
		while(rs.next()) {
		if(rs.getShort("COLUMN_TYPE") == DatabaseMetaData.procedureColumnReturn){
		// this is a function, so set the flag to true
		isFunction = true;
		}
		}
		
		rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
		
		// get the count of the parameters
		while(rs.next()) {
		colCount++;
		}
		
		// check if the procedure has parameters or not
		if(colCount == 0) {
		hasParameters = false;
		}
		
		// construct the procedure execution command string
		if (isFunction == true) {} else {}
		
		CallableStatement cstmt = dbconn.prepareCall(sqlText);
		
		rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
		int paramIndex=0;
		
		// loop through the list of parameters and register them
		for (int j = 0; j < colCount; j++)
		{
		rs.next();
		paramIndex++;
		String parameterName = rs.getString("COLUMN_NAME");
		int targetSqlType = rs.getInt("DATA_TYPE");
		int colType = rs.getShort("COLUMN_TYPE");
		String type_Name = rs.getString("TYPE_NAME");
		
		if ( colType == DatabaseMetaData.procedureColumnIn) {
		if ((targetSqlType == 1111) && (type_Name.equals("PL/SQL TABLE"))) {
		targetSqlType = -14;
		}
		
		if ((targetSqlType == 1111) && (type_Name.equals("PL/SQL RECORD"))) {
		targetSqlType = -14;
		}
		cstmt.setNull(paramIndex, targetSqlType);
		}
		
		if (colType == DatabaseMetaData.procedureColumnInOut || colType == DatabaseMetaData.procedureColumnOut) {
		try {
		// if the parameter is a cursor type, add its index to the arraylist
		if ((targetSqlType == 1111) && (type_Name.equals("REF CURSOR"))) {
		targetSqlType = -10;
		paramIndices.add(new Integer(paramIndex));
		}
		cstmt.registerOutParameter(paramIndex, targetSqlType);
		} catch(SQLException e) {
		System.out.println(e.getMessage());
		e.printStackTrace();
		checkProcMetaData = true;
		//throw e;
		}
		}
		
		// check if the parameter is RETURN type (i.e. it is a function)
		if (colType == DatabaseMetaData.procedureColumnReturn) {
		try {
		// if the parameter is a cursor type, add its index to the arraylist
		if ((targetSqlType == 1111) && (type_Name.equals("REF CURSOR"))) {
		targetSqlType = -10;
		paramIndices.add(new Integer(paramIndex));
		}
		cstmt.registerOutParameter(paramIndex, targetSqlType);
		} catch(SQLException e) {
		System.out.println(e.getMessage());
		e.printStackTrace();
		//throw e;
		checkProcMetaData = true;
		}
		}
		}
		
		// execute the stored procedure
		boolean resultsAvailable = cstmt.execute();
		int count = -1;
		int numResults = paramIndices.size();
		
		Iterator paramIdxIter = paramIndices.iterator();
		
		// iterate through the resultsets returned, whose indices are stored in the arraylist
		while (paramIdxIter.hasNext()) {
		ArrayList resultArray = new ArrayList();    // arraylist to hold the objects of ResultSetColumn
		count += 1;
		// get the index (from the arraylist) of the parameter which is a resultset
		int index = ((Integer)paramIdxIter.next()).intValue();
		ResultSet paramRS ;
		ResultSetMetaData rsmd;
		// if the resultset returns nothing, set the metadata object to null
		try {
		paramRS = (ResultSet)cstmt.getObject(index);
		rsmd = paramRS.getMetaData();
		} catch(SQLException e) {
		rsmd = null;
		}
		
		int rsmdColCount=0;
		if (rsmd != null) {
		rsmdColCount = rsmd.getColumnCount();
		}
		// scroll through the resultset column information
		for (int i = 1; i <= rsmdColCount; i++) {
		ResultSetColumn currCol = new ResultSetColumn();
		currCol.setOrdinalPosition(i);
		currCol.setName(rsmd.getColumnName(i));
		currCol.setLabel(rsmd.getColumnLabel(i));
		currCol.setSqlType(getSQLTypeDescription(rsmd.getColumnType(i)));
		currCol.setJavaType((String)SQLTOJAVATYPES.get(getSQLTypeDescription(rsmd.getColumnType(i))));
		
		if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
		currCol.setIsNullable(true);
		} else {
		currCol.setIsNullable(false);
		}
		// add ResultSetColumn object to the arraylist
		boolean addToArray = resultArray.add(currCol);
		}
		
		// add the arraylist having ResultSetColumn objects to the ResultSetColumns object
		// now add this ResultSetColumns object to the arraylist object (result)
		if(resultArray.size() > 0){
		ResultSetColumns rsColbj = new ResultSetColumns();
		rsColbj.setColumns(resultArray);
		rsColbj.setName(pname + "_" + count);
		result.add(rsColbj);
		}
		}
		} catch (SQLException e) {
		// resultset column metadata not supported
		System.out.println("\nException occurred: " + e.getClass().getName()+ ", "+ e.getMessage());
		e.printStackTrace();
		errMsg = e.getLocalizedMessage();
		checkProcMetaData = true;
		//throw e;
		}
		catch (NullPointerException npe) {
		System.out.println("\nException occurred: " + npe.getClass().getName()+ ", " + npe.getMessage());
		npe.printStackTrace();
		errMsg = npe.getLocalizedMessage();
		checkProcMetaData = true;
		//throw npe;
		}
		catch (Exception e) {
		// resultset column metadata not supported
		System.out.println("\nException occurred: " + e.getClass().getName()+ ", " + e.getMessage());
		e.printStackTrace();
		errMsg = e.getLocalizedMessage();
		checkProcMetaData = true;
		}
		
		// add the arraylist object to the Procedure object
		procResult.setResultSetColumns(result);
		return procResult;
	}
	
	public boolean getErrPrepStmtMetaData() {
		return this.checkPrepStmtMetaData;		
	}
	public boolean getErrProcMetaData() {
		return this.checkProcMetaData;		
	}


    
}
