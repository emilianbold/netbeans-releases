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
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.builder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;

/**
 * Extracts database metadata information (table names and constraints, their associated columns,
 * etc.)
 * 
 * @author
 */
class DriverShim implements Driver {

    private final Driver driver;

    DriverShim(final Driver d) {
        this.driver = d;
    }

    public boolean acceptsURL(final String u) throws SQLException {
        return this.driver.acceptsURL(u);
    }

    public Connection connect(final String u, final Properties p) throws SQLException {
        return this.driver.connect(u, p);
    }

    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    public DriverPropertyInfo[] getPropertyInfo(final String u, final Properties p) throws SQLException {
        return this.driver.getPropertyInfo(u, p);
    }

    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }
}

public final class DBMetaData {
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

    /** Database OTD type for JDBC */
    public static final String JDBC = "JDBC"; // NOI18N

    /** Database OTD type for VSAM */
    public static final String VSAM_ADABAS_IAM = "LEGACY"; // NOI18N

    /** Database OTD type for JDBC-ODBC */
    public static final String JDBC_ODBC = "JDBC"; // NOI18N

    /** Database type display description for DB2 */
    public static final String DB2_TEXT = "DB2"; // NOI18N

    /** Database type display description for Oracle */
    public static final String ORACLE_TEXT = "ORACLE"; // NOI18N

    /** Database type display description for Derby */
    public static final String DERBY = "DERBY"; // NOI18N

    /** Database type display description for SQL Server */
    public static final String SQLSERVER_TEXT = "SQL SERVER"; // NOI18N

    /** Database type display description for MySQL Server */
    public static final String MYSQL_TEXT = "MySQL"; // NOI18N

    public static final String MYSQL = "MYSQL"; // NOI18N

    /** Database type display description for JDBC */
    // public static final String JDBC_TEXT = "JDBC"; // NOI18N
    /** Database type display description for VSAM/ADABAS/IAM */
    public static final String VSAM_ADABAS_IAM_TEXT = "VSAM/ADABAS/IAM"; // NOI18N

    /** Database type display description for JDBC-ODBC */
    public static final String JDBC_TEXT = "JDBC-ODBC"; // NOI18N
    
    /** Database type display description for SYBASE */
    public static final String SYBASE = "SYBASE"; // NOI18N

    /** List of database type display descriptions */
    public static final String[] DBTYPES = {DBMetaData.DB2_TEXT, DBMetaData.ORACLE_TEXT, DBMetaData.SQLSERVER_TEXT, DBMetaData.JDBC_TEXT, DBMetaData.VSAM_ADABAS_IAM_TEXT, DBMetaData.JDBC_TEXT, DBMetaData.MYSQL_TEXT, DBMetaData.SYBASE};
    /** List of Java types */
    public static final String[] JAVATYPES = {"boolean", "byte", "byte[]", "double", "float", "int",
        "java.lang.String", "java.lang.Object", "java.math.BigDecimal", "java.net.URL", "java.sql.Array",
        "java.sql.Blob", "java.sql.Clob", "java.sql.Date", "java.sql.Ref", "java.sql.Struct", "java.sql.Time",
        "java.sql.Timestamp", "long", "short"
    };
    /** List of JDBC SQL types */
    public static final String[] SQLTYPES = {"ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "BOOLEAN", "CHAR", "CLOB",
        "DATALINK", "DATE", "DECIMAL", "DISTINCT", "DOUBLE", "FLOAT", "INTEGER", "JAVA_OBJECT", "LONGVARBINARY",
        "LONGVARCHAR", "NULL", "NUMERIC", "OTHER", "REAL", "REF", "SMALLINT", "STRUCT", "TIME", "TIMESTAMP",
        "TINYINT", "VARBINARY", "VARCHAR"
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
        70, // case java.sql.Types.DATALINK,
        java.sql.Types.DATE, java.sql.Types.DECIMAL, java.sql.Types.DISTINCT, java.sql.Types.DOUBLE,
        java.sql.Types.FLOAT, java.sql.Types.INTEGER, java.sql.Types.JAVA_OBJECT, java.sql.Types.LONGVARBINARY,
        java.sql.Types.LONGVARCHAR, java.sql.Types.NULL, java.sql.Types.NUMERIC, java.sql.Types.OTHER,
        java.sql.Types.REAL, java.sql.Types.REF, java.sql.Types.SMALLINT, java.sql.Types.STRUCT,
        java.sql.Types.TIME, java.sql.Types.TIMESTAMP, java.sql.Types.TINYINT, java.sql.Types.VARBINARY,
        java.sql.Types.VARCHAR
    };
    /** Map SQL type to Java type */
    public static final HashMap SQLTOJAVATYPES = new HashMap();
    

    static {
        DBMetaData.SQLTOJAVATYPES.put("ARRAY", "java.sql.Array"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("BIGINT", "long"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("BINARY", "byte[]"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("BIT", "boolean"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("BLOB", "java.sql.Blob"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("BOOLEAN", "boolean"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("CHAR", "java.lang.String"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("CLOB", "java.sql.Clob"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("DATALINK", "java.net.URL"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("DATE", "java.sql.Date"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("DECIMAL", "java.math.BigDecimal"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("DISTINCT", "java.lang.String"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("DOUBLE", "double"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("FLOAT", "double"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("INTEGER", "int"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("JAVA_OBJECT", "java.lang.Object"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("LONGVARBINARY", "java.sql.Blob"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("LONGVARCHAR", "java.sql.Clob"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("NULL", "java.lang.String"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("NUMERIC", "java.math.BigDecimal"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("OTHER", "java.lang.String"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("REAL", "float"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("REF", "java.sql.Ref"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("SMALLINT", "short"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("STRUCT", "java.sql.Struct"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("TIME", "java.sql.Time"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("TIMESTAMP", "java.sql.Timestamp"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("TINYINT", "byte"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("VARBINARY", "byte[]"); // NOI18N

        DBMetaData.SQLTOJAVATYPES.put("VARCHAR", "java.lang.String"); // NOI18N
        // added abey for Procedure ResultSets

        DBMetaData.SQLTOJAVATYPES.put("RESULTSET", "java.sql.ResultSet"); // NOI18N

    }

    // String used in java.sql.DatabaseMetaData to indicate system tables.
    private static final String SYSTEM_TABLE = "SYSTEM TABLE"; // NOI18N

    // String used in java.sql.DatabaseMetaData to indicate system tables.
    private static final String TABLE = "TABLE"; // NOI18N

    // String used in java.sql.DatabaseMetaData to indicate system tables.
    private static final String VIEW = "VIEW"; // NOI18N

    //private Connection dbconn; // db connection

    //private DatabaseMetaData dbmeta; // db metadata

    //private String errMsg; // error message

    //private boolean checkPrepStmtMetaData = true; // indicates driver does not

    // fully support finding prepared
    // statement metadata
    //private boolean errPrepStmtParameters = false; // error getting prep. stmt. parameters

    //private boolean errPrepStmtResultSetColumns = false; // error getting prep. stmt. resultset
    private String sqlText;
    // columns

    /**
     * Gets the primary keys for a table.
     * 
     * @param newTable Table to get the primary key(s) for
     * @throws Exception DOCUMENT ME!
     */
    public static final void checkPrimaryKeys(final Table newTable, final Connection connection) throws Exception {
        //this.errMsg = "";
        try {
            // get the primary keys
            final List primaryKeys = getPrimaryKeys(newTable.getCatalog(), newTable.getSchema(), newTable.getName(), connection);

            if (primaryKeys.size() != 0) {
                newTable.setPrimaryKeyColumnList(primaryKeys);

                // create a hash set of the keys
                final java.util.Set primaryKeysSet = new java.util.HashSet();
                for (int i = 0; i < primaryKeys.size(); i++) {
                    final KeyColumn key = (KeyColumn) primaryKeys.get(i);
                    primaryKeysSet.add(key.getColumnName());
                }

                // now loop through all the columns flagging the primary keys
                final TableColumn[] columns = newTable.getColumns();
                if (columns != null) {
                    for (int i = 0; i < columns.length; i++) {
                        if (primaryKeysSet.contains(columns[i].getName())) {
                            columns[i].setIsPrimaryKey(true);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        }
    }

    /**
     * Gets the foreign keys for a table.
     * 
     * @param newTable Table to get the foreign key(s) for
     * @throws Exception DOCUMENT ME!
     */
    public static void checkForeignKeys(final Table newTable, final Connection connection) throws Exception {
        //this.errMsg = "";
        try {
            // get the foreing keys
            final List foreignKeys = getForeignKeys(newTable.getCatalog(), newTable.getSchema(), newTable.getName(), connection);
            if (foreignKeys != null) {
                newTable.setForeignKeyColumnList(foreignKeys);

                // create a hash set of the keys
                final java.util.Set foreignKeysSet = new java.util.HashSet();
                for (int i = 0; i < foreignKeys.size(); i++) {
                    final ForeignKeyColumn key = (ForeignKeyColumn) foreignKeys.get(i);
                    foreignKeysSet.add(key.getColumnName());
                }

                // now loop through all the columns flagging the foreign keys
                final TableColumn[] columns = newTable.getColumns();
                if (columns != null) {
                    for (int i = 0; i < columns.length; i++) {
                        if (foreignKeysSet.contains(columns[i].getName())) {
                            columns[i].setIsForeignKey(true);
                        }
                    }
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        }
    }
    private static final Logger mLogger = Logger.getLogger(DBMetaData.class.getName());

    /**
     * Establishes a connection to the database.
     * 
     * @param conn JDBC connection
     * @throws Exception DOCUMENT ME!
     */
    /*public void connectDB(final Connection conn) throws Exception {
    this.errMsg = "";
    if (conn == null) {
    throw new IllegalArgumentException("Connection can't be null.");
    }
    
    this.dbconn = conn;
    this.getDBMetaData();
    }*/
    /**
     * Disconnects from the database.
     * 
     * @throws Exception DOCUMENT ME!
     */
    /*public void disconnectDB() throws Exception {
    this.errMsg = "";
    // close connection to database
    try {
    if (this.dbconn != null && !this.dbconn.isClosed()) {
    this.dbconn.close();
    this.dbconn = null;
    }
    } catch (final SQLException e) {
    e.printStackTrace();
    this.errMsg = e.getLocalizedMessage();
    throw e;
    }
    }*/

    /*private void getDBMetaData() throws Exception {
    this.errMsg = "";
    // get the metadata
    try {
    this.dbmeta = this.dbconn.getMetaData();
    } catch (final SQLException e) {
    e.printStackTrace();
    this.errMsg = e.getLocalizedMessage();
    throw e;
    }
    }*/
    /**
     * Returns the database product name
     * 
     * @return String database product name
     * @throws Exception DOCUMENT ME!
     */
    /*public String getDBName() throws Exception {
    String dbname = "";
    
    this.errMsg = "";
    // get the database product name
    try {
    dbname = this.dbmeta.getDatabaseProductName();
    } catch (final SQLException e) {
    e.printStackTrace();
    this.errMsg = e.getLocalizedMessage();
    throw e;
    }
    return dbname;
    }*/
    /**
     * Returns the database OTD type.
     * 
     * @return String Database OTD type
     * @throws Exception DOCUMENT ME!
     */
    public static String getDBType(final Connection conn) throws Exception {
        String dbtype = "";

        // get the database type based on the product name converted to lowercase
        final String dbname = conn.getMetaData().getDatabaseProductName().toLowerCase();
        if (dbname.equals("microsoft sql server")) {
            // Microsoft SQL Server
            dbtype = DBMetaData.SQLSERVER;
        } else if (dbname.equals("mysql")) {
            // Microsoft SQL Server
            dbtype = DBMetaData.MYSQL;
        } else if (dbname.equals("sql server") || dbname.indexOf("jdbc") > -1) {
            // JDBC
            dbtype = DBMetaData.JDBC;
        } else if (dbname.indexOf("db2") > -1 || dbname.equals("as")) {
            // DB2
            dbtype = DBMetaData.DB2;
        } else if (dbname.equals("exadas") || dbname.equals("attunity connect driver")) {
            // VSAM
            dbtype = DBMetaData.VSAM_ADABAS_IAM;
        } else if (dbname.indexOf("orac") > -1) {
            // Oracle
            dbtype = DBMetaData.ORACLE;
        } else if (dbname.indexOf("derby") > -1) {
            // derby
            dbtype = DBMetaData.DERBY;
        } else if (dbname.toUpperCase().contains("ADAPTIVE SERVER")){
        	//Sybase
        	dbtype = DBMetaData.SYBASE;
        } else {
            // other type, default to JDBC-ODBC
            dbtype = DBMetaData.JDBC_ODBC;
        }

        return dbtype;
    }

    public static int getDatabaseMajorVersion(final Connection conn) throws Exception {
        final DatabaseMetaData dbmeta = conn.getMetaData();
        return dbmeta.getDatabaseMajorVersion();
    }

    public static List getOracleRecycleBinTables(final Connection conn) {
        List result = new ArrayList();
        try {
            Statement stmt = conn.createStatement();
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
        } catch (SQLException exc) {
            result = Collections.EMPTY_LIST;
        }
        return result;
    }

    private static final String getJDBCSearchPattern(final String guiPattern, final Connection connection) throws Exception {
        //this.errMsg = "";

        // Converts the passed in GUI pattern to one understood by the
        // JDBC driver:
        // change _ to <escape char>_
        // change % to <escape char>%
        // change * to % = GUI uses * to represent 0 or more characters
        // change ? to _ = GUI uses ? to represent any single character
        try {
            String jdbcPattern = guiPattern;
            final String escapeChar = connection.getMetaData().getSearchStringEscape();

            // change _ to <escape char>_
            // PP:See bug 10718. Disabling the escape character for _
            // jdbcPattern = replaceAllChars(jdbcPattern, '_', escapeChar + "_");

            // change % to <escape char>%
            jdbcPattern = replaceAllChars(jdbcPattern, '%', escapeChar + "%");

            // change * to %
            jdbcPattern = jdbcPattern.replace('*', '%');

            // change ? to _
            jdbcPattern = jdbcPattern.replace('?', '_');

            return jdbcPattern;
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        }
    }

    /**
     * Returns a list of schemas in the database.
     * 
     * @return String[] List of schema names
     * @throws Exception DOCUMENT ME!
     */
    public static final String[] getSchemas(final Connection connection) throws Exception {
        //this.errMsg = "";
        // get all schemas
        try {
            final ResultSet rs = connection.getMetaData().getSchemas();
            final Vector v = new Vector();
            String[] schemaNames = null;

            while (rs.next()) {
                final String schema = rs.getString("TABLE_SCHEM");
                v.add(schema);
            }
            if (v.size() > 0) {
                // copy into array to return
                schemaNames = new String[v.size()];
                v.copyInto(schemaNames);
            }
            rs.close();
            return schemaNames;
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
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
    public static final String[][] getTablesOnly(final String catalog,
            final String schemaPattern,
            final String tablePattern,
            final boolean includeSystemTables, final Connection connection) throws Exception {
        String[] tableTypes;

        if (includeSystemTables) {
            final String[] types = {DBMetaData.TABLE, DBMetaData.SYSTEM_TABLE};
            tableTypes = types;
        } else {
            final String[] types = {DBMetaData.TABLE};
            tableTypes = types;
        }

        return getTables(catalog, schemaPattern, tablePattern, tableTypes, connection);
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
    public static final String[][] getViewsOnly(final String catalog, final String schemaPattern, final String viewPattern, final boolean includeSystemTables, final Connection connection)
            throws Exception {
        String[] tableTypes;

        if (includeSystemTables) {
            final String[] types = {DBMetaData.VIEW, DBMetaData.SYSTEM_TABLE};
            tableTypes = types;
        } else {
            final String[] types = {DBMetaData.VIEW};
            tableTypes = types;
        }

        return getTables(catalog, schemaPattern, viewPattern, tableTypes, connection);
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
    public static final String[][] getTablesAndViews(final String catalog,
            final String schemaPattern,
            final String tablePattern,
            final boolean includeSystemTables, final Connection connection) throws Exception {
        String[] tableTypes;

        if (includeSystemTables) {
            final String[] types = {DBMetaData.TABLE, DBMetaData.VIEW, DBMetaData.SYSTEM_TABLE};
            tableTypes = types;
        } else {
            final String[] types = {DBMetaData.TABLE, DBMetaData.VIEW};
            tableTypes = types;
        }

        return getTables(catalog, schemaPattern, tablePattern, tableTypes, connection);
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
    public static final String[][] getTables(String catalog, String schemaPattern, String tablePattern, final String[] tableTypes, final Connection connection)
            throws Exception {
        //this.errMsg = "";
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
                tablePattern = getJDBCSearchPattern(tablePattern, connection);
            }

            final ResultSet rs = connection.getMetaData().getTables(catalog, schemaPattern, tablePattern, tableTypes);

            final Vector v = new Vector();
            String[][] tables = null; // array of table structures: Name, Catalog, Schema

            while (rs.next()) {
                String tableCatalog = rs.getString("TABLE_CAT");
                String tableSchema = rs.getString("TABLE_SCHEM");
                final String tableName = rs.getString("TABLE_NAME");
                final String tableType = rs.getString("TABLE_TYPE");

                if (tableCatalog == null) {
                    tableCatalog = "";
                }
                if (tableSchema == null) {
                    tableSchema = "";
                }

                // fill in table info
                final String[] tableItem = new String[4]; // hold info for each table

                tableItem[DBMetaData.NAME] = tableName;
                tableItem[DBMetaData.CATALOG] = tableCatalog;
                tableItem[DBMetaData.SCHEMA] = tableSchema;
                tableItem[DBMetaData.TYPE] = tableType;

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
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        }
    }

    public PrepStmt getPrepStmtMetaData() throws Exception {
        PrepStmt newPrepStmt = null;
        try {
            newPrepStmt = getPrepStmtMetaData(null, null, null, sqlText, null);
        } catch (Exception e) {
            //errMsg = e.getLocalizedMessage();
            //throw e;
        }
        return newPrepStmt;
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
    public static final PrepStmt getPrepStmtMetaData(final String catalog, final String schema, final String name, final String sqlText, final Connection connection) throws Exception {

        //this.errMsg = "";
        //this.checkPrepStmtMetaData = false;

        try {
            PrepStmt newPrepStmt = null;

            // make sure there is some sql text for the prepared statement
            if (sqlText == null || sqlText.equals("")) {
                return null;
            }

            // fill in name and sql text
            newPrepStmt = new PrepStmt(name, catalog, schema, sqlText);

            // prepare the statement
            final PreparedStatement pstmt = connection.prepareStatement(sqlText);

            // Parameter metadata only available through JDBC 3.0, JDK 1.4
            // get parameter meta data of the prepared statment from the DB connection
            Parameter[] parameters = null;

            // pass sqlText to getPrepStmtParameters(...) so that in case
            // the driver does not support java.sql.ParameterMetaData we
            // can construct a paramters array using default values. see
            // details inside getPrepStmtParameters(...)
            parameters = getPrepStmtParameters(pstmt, sqlText, connection);

            newPrepStmt.setParameters(parameters);

            ResultSetColumn[] cols = null;

            // get the resultset metadata
            // of the prepared statment from the DB connection
            cols = getPrepStmtResultSetColumns(pstmt);

            // set the prepared statement's resultset columns
            newPrepStmt.setResultSetColumns(cols);

            //this.checkPrepStmtMetaData = this.errPrepStmtParameters && this.errPrepStmtResultSetColumns;

            pstmt.close();

            return newPrepStmt;
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        }
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
    public static final String[][] getProcedures(String catalog, String schemaPattern, String procedurePattern, final Connection connection) throws Exception {
        //this.errMsg = "";
        try {
            if ((procedurePattern != null) && (procedurePattern.equals(""))) {
                procedurePattern = null;
            }

            if ((schemaPattern != null) && (schemaPattern.equals(""))) {
                schemaPattern = null;
            }
            
            final Vector v = new Vector();
            String[][] procedures = null; // array of procedure structures: Name, Catalog, Schema,
            // Type

            final ResultSet rs = connection.getMetaData().getProcedures(catalog, schemaPattern, procedurePattern);
            while (rs.next()) {
                String procedureCatalog = rs.getString("PROCEDURE_CAT");
                String procedureSchema = rs.getString("PROCEDURE_SCHEM");
                String procedureName = rs.getString("PROCEDURE_NAME");
                if(procedureName.endsWith(";1")){
                	procedureName = procedureName.substring(0, procedureName.length()-2);
                }
                final String procedureType = getProcedureTypeDescription(rs.getShort("PROCEDURE_TYPE"));

                if (procedureCatalog == null) {
                    procedureCatalog = "";
                }
                if (procedureSchema == null) {
                    procedureSchema = "";
                }
                // fill in procedure info
                final String[] procedureItem = new String[4]; // hold info for each procedure

                procedureItem[DBMetaData.NAME] = procedureName;
                procedureItem[DBMetaData.CATALOG] = procedureCatalog;
                procedureItem[DBMetaData.SCHEMA] = procedureSchema;
                procedureItem[DBMetaData.TYPE] = procedureType;

                // add procedure to Vector
                v.add(procedureItem);
            }

            // now copy Vector to array to return back
            if (v.size() > 0) {
                procedures = new String[v.size()][4];
                v.copyInto(procedures);
            }
            rs.close();
            return procedures;
        } catch (final Exception ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            //this.errMsg = e.getLocalizedMessage();
            throw ex;
        }
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
    public static final List getPrimaryKeys(String tcatalog, String tschema, final String tname, final Connection v) throws Exception {
        List pkList = Collections.EMPTY_LIST;
        ResultSet rs = null;

        //this.errMsg = "";
        try {
            if (tcatalog.equals("")) {
                tcatalog = null;
            }
            if (tschema.equals("")) {
                tschema = null;
            }

            rs = v.getMetaData().getPrimaryKeys(tcatalog, tschema, tname);
            if (v.getMetaData().getDriverName().startsWith("JDBC-ODBC")) {
                pkList = KeyColumn.createPrimaryKeyColumnList(rs, true);
            } else {
                pkList = KeyColumn.createPrimaryKeyColumnList(rs, false);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    /* Ignore */ ;
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
    public static final List getForeignKeys(String tcatalog, String tschema, final String tname, final Connection connection) throws Exception {
        //this.errMsg = "";
        List fkList = Collections.EMPTY_LIST;
        ResultSet rs = null;

        try {
            if (tcatalog.equals("")) {
                tcatalog = null;
            }
            if (tschema.equals("")) {
                tschema = null;
            }
            try {
                rs = connection.getMetaData().getImportedKeys(tcatalog, tschema, tname);
                fkList = ForeignKeyColumn.createForeignKeyColumnList(rs);
            } catch (final Exception e) {
                e.printStackTrace();
                DBMetaData.mLogger.warning("JDBC driver does not support java.sql.ParameterMetaData " + e.getMessage());
            //this.errMsg = e.getLocalizedMessage();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    /* Ignore */ ;
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
    public static final Procedure getProcedureMetaData(String pcatalog, String pschema, final String pname, final String ptype, final Connection connection) throws Exception {
        //this.errMsg = "";
        try {
            // create a new procedure object
            final Procedure newProcedure = new Procedure(pname, pcatalog, pschema, ptype);
            final Vector v = new Vector();

            if ((pcatalog != null) && pcatalog.equals("")) {
                pcatalog = null;
            }
            if ((pschema != null) && pschema.equals("")) {
                pschema = null;
            }

            // get procedure parameter information
            final ResultSet rs = connection.getMetaData().getProcedureColumns(pcatalog, pschema, pname, "%");

            Parameter[] parameters = null;
            int pos = 0;
            boolean hasReturn = false;

            while (rs.next()) {
                pos++;
                String parmName = rs.getString("COLUMN_NAME");
                if (parmName != null) {
                    // strip off "@" in front of parameter name
                    if (parmName.charAt(0) == '@') {
                        parmName = parmName.substring(1);
                    }
                } else {
                    // parameter name is not return - call it "param<pos>"
                    parmName = "param" + String.valueOf(pos);
                }
                String sqlType = DBMetaData.getSQLTypeDescription(rs.getInt("DATA_TYPE"));
                String javaType = getJavaFromSQLTypeDescription(sqlType);
                // added abey for Procedure ResultSet
                final int dataType = rs.getInt("DATA_TYPE");
                if (dataType == java.sql.Types.OTHER && rs.getString("TYPE_NAME").equalsIgnoreCase("REF CURSOR")) {
                    sqlType = "RESULTSET";
                    javaType = "java.sql.ResultSet";
                }
                final String paramType = getParamTypeDescription(rs.getShort("COLUMN_TYPE"));
                final int nullable = rs.getShort("NULLABLE");
                final int numericPrecision = rs.getInt("PRECISION");
                final short numericScale = rs.getShort("SCALE");

                // create a parameter and add it to the vector
                final Parameter parm = new Parameter(parmName, javaType);
                boolean isNullable = false;
                if (nullable == DatabaseMetaData.procedureNullable) {
                    isNullable = true;
                }
                parm.setJavaType(javaType);
                parm.setSqlType(sqlType);
                parm.setParamType(paramType);
                parm.setOrdinalPosition(pos);
                parm.setNumericPrecision(numericPrecision);
                parm.setNumericScale(numericScale);
                parm.setIsNullable(isNullable);

                if (paramType.equals("RETURN")) {
                    hasReturn = true;
                }

                // add to vector
                v.add(parm);
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

            return newProcedure;
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        }
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
    public static final Table getTableMetaData(String tcatalog, String tschema, final String tname, final String ttype, final Connection connection) throws Exception {
        //this.errMsg = "";
        ResultSet rs = null;

        try {
            // create a new Table object
            final Table newTable = new Table(tname, tcatalog, tschema, ttype);
            final Vector v = new Vector();

            if (tcatalog.equals("")) {
                tcatalog = null;
            }

            if (tschema.equals("")) {
                tschema = null;
            }

            // get table column information
            rs = connection.getMetaData().getColumns(tcatalog, tschema, tname, "%");

            TableColumn[] columns = null;

            while (rs.next()) {
                final String defaultValue = rs.getString("COLUMN_DEF");

                final int sqlTypeCode = rs.getInt("DATA_TYPE");

                final String colName = rs.getString("COLUMN_NAME");
                final String sqlType = DBMetaData.getSQLTypeDescription(sqlTypeCode);
                final String javaType = getJavaFromSQLTypeDescription(sqlType);

                final int position = rs.getInt("ORDINAL_POSITION");

                final int scale = rs.getInt("DECIMAL_DIGITS");
                final int precision = rs.getInt("COLUMN_SIZE");
                final int radix = rs.getInt("NUM_PREC_RADIX");

                // create a table column and add it to the vector
                final TableColumn col = new TableColumn(colName, javaType);
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
            checkPrimaryKeys(newTable, connection);

            // now check the columns that are foreign keys
            checkForeignKeys(newTable, connection);

            // catch exceptions for this as index only makes sense for
            // tables and not views (can't check the table type because it's dependent on driver)
            try {
                // get index info for this table
                rs = connection.getMetaData().getIndexInfo(tcatalog, tschema, tname, false, true);
                newTable.setIndexList(IndexColumn.createIndexList(rs));
            } catch (final Exception e) {
                // ignore and continue
                //this.errMsg = e.getLocalizedMessage();
            }

            return newTable;
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    /* Ignore... */ ;
                }
            }
        }
    }

    public static final Table getTableMetaDataForODBCDriver(String tcatalog, String tschema, final String tname, final String ttype, final Connection connection) throws Exception {
        //this.errMsg = "";
        ResultSet rs = null;

        try {
            // create a new Table object
            final Table newTable = new Table(tname, tcatalog, tschema, ttype);
            final Vector v = new Vector();

            if (tcatalog.equals("")) {
                tcatalog = null;
            }

            if (tschema.equals("")) {
                tschema = null;
            }

            // get table column information
            rs = connection.getMetaData().getColumns(tcatalog, tschema, tname, "%");

            TableColumn[] columns = null;

            while (rs.next()) {
                // {13=COLUMN_DEF, 12=REMARKS, 11=NULLABLE, 10=NUM_PREC_RADIX,
                // 9=DECIMAL_DIGITS, 8=BUFFER_LENGTH, 7=COLUMN_SIZE,
                // 6=TYPE_NAME,
                // 5=DATA_TYPE, 4=COLUMN_NAME, 3=TABLE_NAME, 2=TABLE_SCHEM,
                // 1=TABLE_CAT}
                String tablecat = rs.getString(1);
                String tablesch = rs.getString(2);
                String tablename = rs.getString(3);
                String colName = rs.getString(4);
                int sqlTypeCode = rs.getInt(5);
                String typename = rs.getString(6);
                int precision = rs.getInt(7);
                int bufflen = rs.getInt(8);
                int scale = rs.getInt(9);
                int radix = rs.getInt(10);
                boolean nullable = rs.getBoolean(11);
                String remarks = rs.getString(12);
                String defaultValue = rs.getString(13);

                final String sqlType = DBMetaData.getSQLTypeDescription(sqlTypeCode);
                final String javaType = getJavaFromSQLTypeDescription(sqlType);

                // create a table column and add it to the vector
                final TableColumn col = new TableColumn(colName, javaType);
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
                //col.setOrdinalPosition(position);
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
            checkPrimaryKeys(newTable, connection);

            // now check the columns that are foreign keys
            checkForeignKeys(newTable, connection);

            // catch exceptions for this as index only makes sense for
            // tables and not views (can't check the table type because it's dependent on driver)
            try {
                // get index info for this table
                rs = connection.getMetaData().getIndexInfo(tcatalog, tschema, tname, false, true);
                newTable.setIndexList(IndexColumn.createIndexList(rs));
            } catch (final Exception e) {
                // ignore and continue
                //this.errMsg = e.getLocalizedMessage();
            }

            return newTable;
        } catch (final Exception e) {
            e.printStackTrace();
            //this.errMsg = e.getLocalizedMessage();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    /* Ignore... */ ;
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
    public static final String getJavaFromSQLTypeDescription(final String sqlType) {
        Object t;
        String javaType = "java.lang.String"; // default value

        t = DBMetaData.SQLTOJAVATYPES.get(sqlType);

        if (t != null) {
            javaType = t.toString();
        }

        return javaType;
    }

    /**
     * Converts the numeric value of a JDBC SQL type to a display string.
     * 
     * @param type JDBC numeric SQL type value
     * @return JDBC SQL type string
     */
    public static final String getSQLTypeDescription(final int type) {
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
                // case java.sql.Types.BOOLEAN:
                return "BOOLEAN";
            case java.sql.Types.CHAR:
                return "CHAR";
            case java.sql.Types.CLOB:
                return "CLOB";
            case 70:
                // case java.sql.Types.DATALINK:
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
     * Converts a text representation of a JDBC SQL type to a display string.
     * 
     * @param sqlText JDBC SQL type string
     * @return JDBC numeric SQL type value
     */
    public static final int getSQLTypeCode(String sqlText) {
        if (sqlText == null) {
            throw new IllegalArgumentException("Must supply non-null String value for sqlText.");
        }

        sqlText = sqlText.trim().toUpperCase();
        for (int i = 0; i < DBMetaData.SQLTYPES.length; i++) {
            if (DBMetaData.SQLTYPES[i].equals(sqlText)) {
                return DBMetaData.SQLTYPE_CODES[i];
            }
        }

        return java.sql.Types.OTHER;
    }

    private static final String getJavaTypeDescription(final int type) {
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
            // case java.sql.Types.BOOLEAN:
            // javaType = "boolean";
            case java.sql.Types.CHAR:
                javaType = "java.lang.String";
            case java.sql.Types.CLOB:
                javaType = "java.sql.Clob";
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
                javaType = "java.sql.Blob";
            case java.sql.Types.LONGVARCHAR:
                javaType = "java.sql.Clob";
            case java.sql.Types.NUMERIC:
                javaType = "java.math.BigDecimal";
            // case java.sql.Types.OTHER:
            // javaType = "java.sql.Blob";
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

    private static final String getParamTypeDescription(final int type) {
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

    private static final String getProcedureTypeDescription(final int type) {
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

    private static final String getPrepStmtParamTypeDescription(final int type) {
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

    private static final String replaceAllChars(final String orig, final char oldChar, final String replStr) {
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

    /**
     * Get String representing current error message, if any.
     * 
     * @return error message
     */
    /*public String getErrString() {
    return this.errMsg;
    }*/
    private static final Parameter[] getPrepStmtParameters(final PreparedStatement pstmt, final String sqlText, final Connection connection) {
        String errMsg = "";
        //errPrepStmtParameters = false;
        Parameter[] parameters = null;

        try {
            ParameterMetaData pmeta = null;
            try {
                pmeta = pstmt.getParameterMetaData();
            } // just catch all exception since
            // attunity throws java.lang.AbstractMethodError
            // SequeLink throws SQLException
            catch (final AbstractMethodError absE) {
                DBMetaData.mLogger.log(Level.INFO, "JDBC driver does not support java.sql.ParameterMetaData " + absE.getMessage());
                return handleUnsupportParameterMetaData(sqlText);
            } catch (final SQLException sqlE) {
                DBMetaData.mLogger.log(Level.INFO, "JDBC driver does not support java.sql.ParameterMetaData " + sqlE.getMessage());
                return handleUnsupportParameterMetaData(sqlText);
            } catch (final Exception e) {
                DBMetaData.mLogger.log(Level.INFO, "JDBC driver does not support java.sql.ParameterMetaData " + e.getMessage());
                return handleUnsupportParameterMetaData(sqlText);
            }

            if (pmeta != null) {
                final int numParams = pmeta.getParameterCount();
                if (numParams > 0) {
                    parameters = new Parameter[numParams];
                    // get info for each parameter
                    for (int i = 1; i <= numParams; i++) {
                        final Parameter currParam = new Parameter();
                        final String paramname = "param" + String.valueOf(i);
                        currParam.setName(paramname);

                        // try to get the sql type info - default to VARCHAR
                        String sqltype = "VARCHAR";
                        try {
                            sqltype = DBMetaData.getSQLTypeDescription(pmeta.getParameterType(i));
                        } catch (final SQLException e) {
                            // default to VARCHAR if we can't get the type
                            //this.errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                        }

                        // try to get the java type info - default to String
                        /**
                         * Changing it to not use metadata class name and instead use the HashMap
                         * SQLTOJAVATYPES. Without the change the parameter datatypes
                         * java.lang.Double and WSDLGenerator look up list exepects native type
                         * double, float, short etc.
                         */
                        String javatype = "java.lang.String";
                        javatype = getJavaFromSQLTypeDescription(sqltype);

                        // try to get the numeric precision, default to 0
                        int precision = 0;
                        try {
                            precision = pmeta.getPrecision(i);
                        } catch (final SQLException e) {
                            //this.errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                        }

                        // try to get the numeric scale, default to 0
                        int scale = 0;
                        try {
                            scale = pmeta.getScale(i);
                        } catch (final SQLException e) {
                            //this.errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                        }

                        // try to get the param type, default to IN
                        String paramType = "IN";
                        try {
                            paramType = getPrepStmtParamTypeDescription(pmeta.getParameterMode(i));
                        } catch (final SQLException e) {
                            //this.errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
                        }

                        // try to get is nullable, default to TRUE
                        boolean isNullable = true;
                        try {
                            if (pmeta.isNullable(i) == java.sql.ParameterMetaData.parameterNullable) {
                                isNullable = true;
                            } else {
                                isNullable = false;
                            }
                        } catch (final SQLException e) {
                            //this.errPrepStmtParameters = true;
                            e.printStackTrace();
                            errMsg = e.getLocalizedMessage();
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
        } catch (final Exception e) {
            // parameter metadata not supported
            parameters = null;
            //this.errPrepStmtParameters = true;
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }

        return parameters;
    }

    private static final ResultSetColumn[] getPrepStmtResultSetColumns(final PreparedStatement pstmt) {
        String errMsg = "";
        //this.errPrepStmtResultSetColumns = false;
        ResultSetColumn[] cols = null;
		try {
			if (getDBType(pstmt.getConnection()).equals(DBMetaData.ORACLE)){
				int i=0;
				try {
					while(true)
						pstmt.setNull(++i, java.sql.Types.NULL);
				}
				
				catch (SQLException sqe)
				{
					// no more parameters to bind
				}
				pstmt.execute();
			}
        }
        catch (Exception e) {}
		
        try {
            final ResultSetMetaData rsmd = pstmt.getMetaData();
            int count = 0;
            if (rsmd != null) {
                count = rsmd.getColumnCount();
            } else {
                //this.errPrepStmtResultSetColumns = true;
            }
            if (count > 0) {
                // scroll through the resultset column information
                cols = new ResultSetColumn[count];
                for (int i = 1; i <= count; i++) {
                    final ResultSetColumn currCol = new ResultSetColumn();
                    currCol.setName(rsmd.getColumnName(i));
                    currCol.setSqlType(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i)));
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

        } catch (final Exception e) {
            // resultset column metadata not supported
            //this.errPrepStmtResultSetColumns = true;
            cols = null;
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }

        return cols;
    }

    /**
     * check all the used APIs to see if they are supported.
     * 
     * @param type none
     * @return boolean
     */
    /*public boolean checkAPIsForSupport() {
    boolean support = true;
    
    try {
    this.dbmeta.supportsBatchUpdates();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsBatchUpdates() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsBatchUpdates() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsCatalogsInDataManipulation();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInDataManipulation() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInDataManipulation() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsCatalogsInProcedureCalls();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInProcedureCalls() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInProcedureCalls() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsCatalogsInTableDefinitions();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInTableDefinitions() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInTableDefinitions() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsCatalogsInIndexDefinitions();
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInIndexDefinitions() failed - " + errE.getMessage());
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInIndexDefinitions() failed - " + e.getMessage());
    }
    try {
    this.dbmeta.supportsCatalogsInPrivilegeDefinitions();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInPrivilegeDefinitions() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsCatalogsInPrivilegeDefinitions() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsConvert();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsConvert() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsConvert() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsExpressionsInOrderBy();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsExpressionsInOrderBy() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsExpressionsInOrderBy() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsLikeEscapeClause();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsLikeEscapeClause() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsLikeEscapeClause() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsNamedParameters();
    } catch (final Exception e) {
    support = false;
    DBMetaData.mLogger.warning("supportsNamedParameters() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsNamedParameters() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsNonNullableColumns();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsNonNullableColumns() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsNonNullableColumns() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsOuterJoins();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsOuterJoins() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsOuterJoins() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsPositionedDelete();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsPositionedDelete() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsPositionedDelete() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsPositionedUpdate();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsPositionedUpdate() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsPositionedUpdate() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY ) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY ) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY ) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY ) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY) failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY) failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE) failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE) failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE) failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE) failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSchemasInDataManipulation();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInDataManipulation() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInDataManipulation() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSchemasInIndexDefinitions();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInIndexDefinitions() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInIndexDefinitions() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSchemasInPrivilegeDefinitions();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInPrivilegeDefinitions() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInPrivilegeDefinitions() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSchemasInProcedureCalls();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInProcedureCalls() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInProcedureCalls() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSchemasInTableDefinitions();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInTableDefinitions() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSchemasInTableDefinitions() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSelectForUpdate();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSelectForUpdate() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSelectForUpdate() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsStoredProcedures();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsStoredProcedures() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsStoredProcedures() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSubqueriesInComparisons();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInComparisons() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInComparisons() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSubqueriesInExists();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInExists() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInExists() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSubqueriesInIns();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInIns() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInIns() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsSubqueriesInQuantifieds();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInQuantifieds() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsSubqueriesInQuantifieds() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTableCorrelationNames();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTableCorrelationNames() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTableCorrelationNames() failed - " + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE) failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE);
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE) failed - "
    + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE) failed - "
    + errE.getMessage());
    }
    try {
    this.dbmeta.supportsTransactions();
    } catch (final SQLException e) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactions() failed - " + e.getMessage());
    } catch (final AbstractMethodError errE) {
    support = false;
    DBMetaData.mLogger.warning("supportsTransactions() failed - " + errE.getMessage());
    }
    
    return support;
    }*/
    private static final Parameter[] handleUnsupportParameterMetaData(final String sqlText) {
        int numParams = 0;
        for (int i = 0; i < sqlText.length(); i++) {
            if (sqlText.charAt(i) == '?') {
                numParams++;
            }
        }
        final Parameter[] parameters = new Parameter[numParams];
        for (int i = 1; i <= numParams; i++) {
            final Parameter currParam = new Parameter();
            final String paramname = "param" + String.valueOf(i);
            currParam.setName(paramname);
            final String javatype = "java.lang.String";
            final String sqltype = "VARCHAR";
            final int precision = 0;
            final int scale = 0;
            final String paramType = "IN";
            final boolean isNullable = true;

            currParam.setJavaType(javatype);
            currParam.setSqlType(sqltype);
            currParam.setNumericPrecision(precision);
            currParam.setNumericScale(scale);
            currParam.setOrdinalPosition(i);
            currParam.setParamType(paramType);
            currParam.setIsNullable(isNullable);

            parameters[i - 1] = currParam;
        }
        return parameters;
    }

    /**
     * added by Bobby to retrieve the resultset metadata of a procedure
     * 
     * @param pcatalog Catalog (package) name of the procedure
     * @param pschema Schema name of the procdure
     * @param pname Name of the procedure
     * @param columnName Name of the column
     * @return Procedure resultset encapsulated in a Procedure object
     * @throws SQLException, NullPointerException
     */
    public static final Procedure getProcResultSetColumns(final String pcatalog,
                                                            final String pschema,
                                                            final String pname,
                                                            final String columnName,
                                                            final Connection connection) {

        String errMsg = "";
        String cstmtString = "";
        int colCount = 0;
        boolean isFunction = false;
        boolean hasParameters = true;
        // indicates if the procedure is within a package or standalone
        boolean isPackaged = true;

        final Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
        final ResultSetColumn resultCol = new ResultSetColumn();
        final ArrayList paramIndices = new ArrayList(); // arraylist to hold the indices of the
        // paramters that return resultsets

        final ArrayList result = new ArrayList(); // arraylist to hold ResultSetColumns objects

        // check if the procedure is within a package or not
        if (pcatalog.trim().equalsIgnoreCase("") || pcatalog == null) {
            isPackaged = false;
        }
        try {
            final DatabaseMetaData dbmeta = connection.getMetaData();
            ResultSet rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // loop to identify if the procedure is actually a function
            while (rs.next()) {
                if (rs.getShort("COLUMN_TYPE") == DatabaseMetaData.procedureColumnReturn) {
                    // this is a function, so set the flag to true
                    isFunction = true;
                }
            }

            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // get the count of the parameters
            while (rs.next()) {
                colCount++;
            }

            // check if the procedure has parameters or not
            if (colCount == 0) {
                hasParameters = false;
            }

            // construct the procedure execution command string
            if (isFunction == true) {
                cstmtString = "{ ? = call ";
                // use the package name to qualify the procedure name if the procedure is within a
                // package
                if (isPackaged) {
                    cstmtString += pcatalog + "." + pname + "(";
                } else {
                    cstmtString += pname + "(";
                }

                for (int j = 1; j < colCount; j++) {
                    cstmtString += "?,";
                }

                // trim the last comma only if the procedure has any parameters
                if (hasParameters) {
                    cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
                }
                cstmtString += ") }";
            } else {
                cstmtString = "call ";
                // use the package name to qualify the procedure name if the procedure is within a
                // package
                if (isPackaged) {
                    cstmtString += pcatalog + "." + pname + "(";
                } else {
                    cstmtString += pname + "(";
                }

                for (int j = 0; j < colCount; j++) {
                    cstmtString += "?,";
                }

                // trim the last comma only if the procedure has any parameters
                if (hasParameters) {
                    cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
                }
                cstmtString += ")";
            }

            final CallableStatement cstmt = connection.prepareCall(cstmtString);

            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
            int paramIndex = 0;

            // loop through the list of parameters and register them
            for (int j = 0; j < colCount; j++) {
                rs.next();
                paramIndex++;
                final String parameterName = rs.getString("COLUMN_NAME");
                int targetSqlType = rs.getInt("DATA_TYPE");
                final int colType = rs.getShort("COLUMN_TYPE");
                final String type_Name = rs.getString("TYPE_NAME");
                try{
                	cstmt.setNull(paramIndex, targetSqlType);
                }catch(SQLException e){
                	e.printStackTrace();
                }

                if (colType == DatabaseMetaData.procedureColumnInOut || colType == DatabaseMetaData.procedureColumnOut) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if (targetSqlType == 1111 && type_Name.equals("OTHER")) {
                            targetSqlType = java.sql.Types.OTHER;
                            paramIndices.add(Integer.valueOf(String.valueOf(paramIndex)));
                        }
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    }
                }

                // check if the parameter is RETURN type (i.e. it is a function)
                if (colType == DatabaseMetaData.procedureColumnReturn) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if (targetSqlType == 1111 && type_Name.equals("OTHER")) {
                            targetSqlType = java.sql.Types.OTHER;
                            paramIndices.add(Integer.valueOf(String.valueOf(paramIndex)));
                        }
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            // execute the stored procedure
            final boolean resultsAvailable = cstmt.execute();
            int count = -1;
            final int numResults = paramIndices.size();

            final Iterator paramIdxIter = paramIndices.iterator();

            // iterate through the resultsets returned, whose indices are stored in the arraylist
            while (paramIdxIter.hasNext()) {
                final ArrayList resultArray = new ArrayList(); // arraylist to hold the objects of
                // ResultSetColumn

                count += 1;
                // get the index (from the arraylist) of the parameter which is a resultset
                final int index = ((Integer) paramIdxIter.next()).intValue();
                ResultSet paramRS;
                ResultSetMetaData rsmd;
                // if the resultset returns nothing, set the metadata object to null
                try {
                    paramRS = (ResultSet) cstmt.getObject(index);
                    rsmd = paramRS.getMetaData();
                } catch (final SQLException e) {
                    rsmd = null;
                }

                int rsmdColCount = 0;
                if (rsmd != null) {
                    rsmdColCount = rsmd.getColumnCount();
                }
                // scroll through the resultset column information
                for (int i = 1; i <= rsmdColCount; i++) {
                    final ResultSetColumn currCol = new ResultSetColumn();
                    currCol.setOrdinalPosition(i);
                    currCol.setName(rsmd.getColumnName(i));
                    currCol.setLabel(rsmd.getColumnLabel(i));
                    currCol.setSqlType(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i)));
                    currCol.setJavaType((String) DBMetaData.SQLTOJAVATYPES.get(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i))));

                    if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
                        currCol.setIsNullable(true);
                    } else {
                        currCol.setIsNullable(false);
                    }
                    // add ResultSetColumn object to the arraylist
                    final boolean addToArray = resultArray.add(currCol);
                }

                // add the arraylist having ResultSetColumn objects to the ResultSetColumns object
                // now add this ResultSetColumns object to the arraylist object (result)
                if (resultArray.size() > 0) {
                    final ResultSetColumns rsColbj = new ResultSetColumns();
                    rsColbj.setColumns(resultArray);
                    rsColbj.setName(pname + "_" + count);
                    result.add(rsColbj);
                }
            }
        } catch (final SQLException e) {
            // resultset column metadata not supported
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        } catch (final NullPointerException npe) {
            npe.printStackTrace();
            errMsg = npe.getLocalizedMessage();
        } catch (final Exception e) {
            // resultset column metadata not supported
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }

        // add the arraylist object to the Procedure object
        if ((result != null) && (result.size() > 0)) {
            procResult.setResultSetColumns(result);
        }
        procResult.setCallableStmtString(cstmtString);
        return procResult;
    }

     /**
     * Modified getProcResultSetColumns to work with DB2
     * @param pcatalog Catalog (package) name of the procedure
     * @param pschema Schema name of the procdure
     * @param pname Name of the procedure
     * @param columnName Name of the column
     * @return Procedure resultset encapsulated in a Procedure object
     * @throws SQLException, NullPointerException
     */
    public static final Procedure getDB2ProcResultSetColumns(final String pcatalog,
                                                            final String pschema,
                                                            final String pname,
                                                            final String columnName,
                                                            final Connection connection) {

        String errMsg = "";
        String cstmtString = "";
        int colCount = 0;
        boolean isFunction = false;
        boolean hasParameters = true;
        // indicates if the procedure is within a package or standalone
        boolean isPackaged = true;

        final Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
        final ResultSetColumn resultCol = new ResultSetColumn();
        final ArrayList paramIndices = new ArrayList(); // arraylist to hold the indices of the
        // paramters that return resultsets

        final ArrayList result = new ArrayList(); // arraylist to hold ResultSetColumns objects

        // check if the procedure is within a package or not
        if (pcatalog.trim().equalsIgnoreCase("") || pcatalog == null) {
            isPackaged = false;
        }
        try {
            final DatabaseMetaData dbmeta = connection.getMetaData();
            ResultSet rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // loop to identify if the procedure is actually a function
            while (rs.next()) {
                if (rs.getShort("COLUMN_TYPE") == DatabaseMetaData.procedureColumnReturn) {
                    // this is a function, so set the flag to true
                    isFunction = true;
                }
            }

            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // get the count of the parameters
            while (rs.next()) {
                colCount++;
            }

            // check if the procedure has parameters or not
            if (colCount == 0) {
                hasParameters = false;
            }

            // construct the procedure execution command string
            if (isFunction == true) {
                cstmtString = "{ ? = call ";
                // use the package name to qualify the procedure name if the procedure is within a
                // package
                if (isPackaged) {
                    cstmtString += pschema + "." + pname + "(";
                } else {
                    cstmtString += pname + "(";
                }

                for (int j = 1; j < colCount; j++) {
                    cstmtString += "?,";
                }

                // trim the last comma only if the procedure has any parameters
                if (hasParameters) {
                    cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
                }
                cstmtString += ") }";
            } else {
                cstmtString = "call ";
                // use the package name to qualify the procedure name if the procedure is within a
                // package
                if (isPackaged) {
                    cstmtString += pschema + "." + pname + "(";
                } else {
                    cstmtString += pname + "(";
                }

                for (int j = 0; j < colCount; j++) {
                    cstmtString += "?,";
                }

                // trim the last comma only if the procedure has any parameters
                if (hasParameters) {
                    cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
                }
                cstmtString += ")";
            }

            final CallableStatement cstmt = connection.prepareCall(cstmtString);

            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
            int paramIndex = 0;

            // loop through the list of parameters and register them
            for (int j = 0; j < colCount; j++) {
                rs.next();
                paramIndex++;
                final String parameterName = rs.getString("COLUMN_NAME");
                int targetSqlType = rs.getInt("DATA_TYPE");
                final int colType = rs.getShort("COLUMN_TYPE");
                final String type_Name = rs.getString("TYPE_NAME");
                if(colType==DatabaseMetaData.procedureColumnIn){
                    //Lets set a String object with "0" value.
                    cstmt.setObject(paramIndex, new String("0"), targetSqlType);
                }

                if (colType == DatabaseMetaData.procedureColumnInOut || colType == DatabaseMetaData.procedureColumnOut) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if (targetSqlType == 1111 && type_Name.equals("OTHER")) {
                            targetSqlType = java.sql.Types.OTHER;
                            paramIndices.add(Integer.valueOf(String.valueOf(paramIndex)));
                        }
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    }
                }

                // check if the parameter is RETURN type (i.e. it is a function)
                if (colType == DatabaseMetaData.procedureColumnReturn) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if (targetSqlType == 1111 && type_Name.equals("OTHER")) {
                            targetSqlType = java.sql.Types.OTHER;
                            paramIndices.add(Integer.valueOf(String.valueOf(paramIndex)));
                        }
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            // execute the stored procedure
            final boolean resultsAvailable = cstmt.execute();
            int count = -1;
            final int numResults = paramIndices.size();

            final Iterator paramIdxIter = paramIndices.iterator();

            
           do{
                ArrayList resultArray = new ArrayList(); // arraylist to hold the objects of
                // ResultSetColumn

                count += 1;
                
                ResultSet paramRS;
                ResultSetMetaData rsmd=null;
                // if the resultset returns nothing, set the metadata object to null
                try {
				    paramRS = (ResultSet) cstmt.getResultSet();
					if(paramRS!=null){
						rsmd = paramRS.getMetaData();
					}
                } catch (final SQLException e) {
                    rsmd = null;
                }

                int rsmdColCount = 0;
                if (rsmd != null) {
                    rsmdColCount = rsmd.getColumnCount();
                }
                // scroll through the resultset column information
                for (int i = 1; i <= rsmdColCount; i++) {
                    final ResultSetColumn currCol = new ResultSetColumn();
                    currCol.setOrdinalPosition(i);
                    currCol.setName(rsmd.getColumnName(i));
                    currCol.setLabel(rsmd.getColumnLabel(i));
                    currCol.setSqlType(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i)));
                    currCol.setJavaType((String) DBMetaData.SQLTOJAVATYPES.get(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i))));

                    if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
                        currCol.setIsNullable(true);
                    } else {
                        currCol.setIsNullable(false);
                    }
                    // add ResultSetColumn object to the arraylist
                    final boolean addToArray = resultArray.add(currCol);
                }

                // add the arraylist having ResultSetColumn objects to the ResultSetColumns object
                // now add this ResultSetColumns object to the arraylist object (result)
                if (resultArray.size() > 0) {
                    final ResultSetColumns rsColbj = new ResultSetColumns();
                    rsColbj.setColumns(resultArray);
                    rsColbj.setName(pname + "_" + count);
                    result.add(rsColbj);
                }
           }while(cstmt.getMoreResults());
        } catch (final SQLException e) {
            // resultset column metadata not supported
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        } catch (final NullPointerException npe) {
            npe.printStackTrace();
            errMsg = npe.getLocalizedMessage();
        } catch (final Exception e) {
            // resultset column metadata not supported
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }

        // add the arraylist object to the Procedure object
        if ((result != null) && (result.size() > 0)) {
            procResult.setResultSetColumns(result);
        }
        procResult.setCallableStmtString(cstmtString);
        return procResult;
    }
    //Only works for Oracle.
    public Procedure getOracleProcResultSetColumns(Procedure storedProc,
                                                    String columnName,
                                                    Connection dbconn) {

        String errMsg = "";
        String cstmtString = "";
        int colCount = 0;
        boolean isFunction = false;
        boolean hasParameters = true;
        // indicates if the procedure is within a package or standalone
        boolean isPackaged = true;

        String pname = storedProc.getName();
        String pcatalog = storedProc.getCatalog();
        String pschema = storedProc.getSchema();
        Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
        ResultSetColumn resultCol = new ResultSetColumn();
        ArrayList paramIndices = new ArrayList();   // arraylist to hold the indices of the paramters that return resultsets

        ArrayList result = new ArrayList();     // arraylist to hold ResultSetColumns objects

        // check if the procedure is within a package or not
        if (pcatalog.trim().equalsIgnoreCase("") || pcatalog == null) {
            isPackaged = false;
        }
        try {
            DatabaseMetaData dbmeta = dbconn.getMetaData();
            ResultSet rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // loop to identify if the procedure is actually a function
            while (rs.next()) {
                if (rs.getShort("COLUMN_TYPE") == DatabaseMetaData.procedureColumnReturn) {
                    // this is a function, so set the flag to true
                    isFunction = true;
                }
            }

            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // get the count of the parameters
            while (rs.next()) {
                colCount++;
            }

            // check if the procedure has parameters or not
            if (colCount == 0) {
                hasParameters = false;
            }

            // construct the procedure execution command string
            if (isFunction == true) {
                cstmtString = "{ ? = call ";
                // use the package name to qualify the procedure name if the procedure is within a package
                if (isPackaged) {
                    cstmtString += pcatalog + "." + pname + "(";
                } else {
                    cstmtString += pname + "(";
                }

                for (int j = 1; j < colCount; j++) {
                    cstmtString += "?,";
                }

                // trim the last comma only if the procedure has any parameters
                if (hasParameters) {
                    cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
                }
                cstmtString += ") }";
            } else {
                cstmtString = "call ";
                // use the package name to qualify the procedure name if the procedure is within a package
                if (isPackaged) {
                    cstmtString += pcatalog + "." + pname + "(";
                } else {
                    cstmtString += pname + "(";
                }

                for (int j = 0; j < colCount; j++) {
                    cstmtString += "?,";
                }

                // trim the last comma only if the procedure has any parameters
                if (hasParameters) {
                    cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
                }
                cstmtString += ")";
            }

            CallableStatement cstmt = dbconn.prepareCall(cstmtString);

            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
            int paramIndex = 0;

            // loop through the list of parameters and register them
            for (int j = 0; j < colCount; j++) {
                rs.next();
                paramIndex++;
                String parameterName = rs.getString("COLUMN_NAME");
                int targetSqlType = rs.getInt("DATA_TYPE");
                int colType = rs.getShort("COLUMN_TYPE");
                String type_Name = rs.getString("TYPE_NAME");

                if (colType == DatabaseMetaData.procedureColumnIn) {
                    if ((targetSqlType == 1111) && (type_Name.equals("PL/SQL TABLE"))) {
                        targetSqlType = -14; //OracleTypes.PLSQL_INDEX_TABLE;
                    }

                    if ((targetSqlType == 1111) && (type_Name.equals("PL/SQL RECORD"))) {
                        targetSqlType = -14; //OracleTypes.PLSQL_INDEX_TABLE;
                    }
					if ((targetSqlType == 1111) && (type_Name.equals("NVARCHAR2"))) {
                        targetSqlType = 12;
                    }
                    cstmt.setNull(paramIndex, targetSqlType);
                }

                if (colType == DatabaseMetaData.procedureColumnInOut || colType == DatabaseMetaData.procedureColumnOut) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if ((targetSqlType == 1111) && (type_Name.equals("REF CURSOR"))) {
                            targetSqlType = -10; //OracleTypes.CURSOR;
                            paramIndices.add(new Integer(paramIndex));
                        }
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                }

                // check if the parameter is RETURN type (i.e. it is a function)
                if (colType == DatabaseMetaData.procedureColumnReturn) {
                    try {
                        // if the parameter is a cursor type, add its index to the arraylist
                        if ((targetSqlType == 1111) && (type_Name.equals("REF CURSOR"))) {
                            targetSqlType = -10; //OracleTypes.CURSOR;
                            paramIndices.add(new Integer(paramIndex));
                        }
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        throw e;
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
                int index = ((Integer) paramIdxIter.next()).intValue();
                ResultSet paramRS;
                ResultSetMetaData rsmd;
                // if the resultset returns nothing, set the metadata object to null
                try {
                    paramRS = (ResultSet) cstmt.getObject(index);
                    rsmd = paramRS.getMetaData();
                } catch (SQLException e) {
                    rsmd = null;
                }

                int rsmdColCount = 0;
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
                    currCol.setJavaType((String) SQLTOJAVATYPES.get(getSQLTypeDescription(rsmd.getColumnType(i))));

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
                if (resultArray.size() > 0) {
                    ResultSetColumns rsColbj = new ResultSetColumns();
                    rsColbj.setColumns(resultArray);
                    Parameter[] params = storedProc.getParameters();
                    
                    //rsColbj.setName(pname + "_" + count);
                    rsColbj.setName(params[index-1].getName());
                    result.add(rsColbj);
                }
            }
        } catch (SQLException e) {
            // resultset column metadata not supported
            System.out.println("\nException occurred: " + e.getClass().getName() + ", " + e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        } catch (NullPointerException npe) {
            System.out.println("\nException occurred: " + npe.getClass().getName() + ", " + npe.getMessage());
            npe.printStackTrace();
            errMsg = npe.getLocalizedMessage();
        } catch (Exception e) {
            // resultset column metadata not supported
            System.out.println("\nException occurred: " + e.getClass().getName() + ", " + e.getMessage());
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }

        // add the arraylist object to the Procedure object
        if ((result != null) && (result.size() > 0)) {
            procResult.setResultSetColumns(result);
        }
        procResult.setCallableStmtString(cstmtString);

        return procResult;
    }

    public Procedure getSQLServerProcResultSetColumns(String pcatalog,
                                                        String pschema,
                                                        String pname,
                                                        String columnName,
                                                        Connection dbconn) {

        String errMsg = "";
        String cstmtString = "";
        int colCount = 0;
        boolean hasParameters = true;
        Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
        ResultSetColumn resultCol = new ResultSetColumn();
        ArrayList result = new ArrayList();     // arraylist to hold ResultSetColumns objects
        ResultSet rs = null;
        try {
            DatabaseMetaData dbmeta = dbconn.getMetaData();
            if((dbconn.getMetaData().getDatabaseProductName().toUpperCase().contains("ADAPTIVE SERVER")) || (dbconn.getMetaData().getDatabaseProductVersion().startsWith("8")) || (dbconn.getMetaData().getDatabaseProductVersion().startsWith("10"))){
            	cstmtString = generateSybaseProcQuery(dbmeta, pcatalog, pschema, pname, columnName);
            	rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
                // get the count of the procedure parameters
                while (rs.next()) {
                    colCount++;
                }
            }else{
            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

            // get the count of the procedure parameters
            while (rs.next()) {
                colCount++;
            }

            // construct the procedure execution command string
            cstmtString = "{ call ";
            cstmtString += pname + "(";


            hasParameters = false;

            for (int j = 0; j < colCount; j++) {
                cstmtString += "?,";
                hasParameters = true;
            }
            /*
            if(colCount == 0) {
            hasParameters = false;
            }
             */
            if (hasParameters) {
                // to trim the last comma in the command string
                cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
            }
            cstmtString += ") }";
            }
            
            Logger.getAnonymousLogger().fine("\nCallable statement is: " + cstmtString);
            
            CallableStatement cstmt = dbconn.prepareCall(cstmtString);
            rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);
            int paramIndex = 0;
            // loop through the list of parameters and register them
            for (int j = 0; j < colCount; j++) {
                rs.next();
                paramIndex++;
                String parameterName = rs.getString("COLUMN_NAME");
                int targetSqlType = rs.getInt("DATA_TYPE");
                int colType = rs.getShort("COLUMN_TYPE");
                String type_Name = rs.getString("TYPE_NAME");

                // set the IN parameters to null
                if (colType == DatabaseMetaData.procedureColumnIn) {
                    cstmt.setNull(paramIndex, targetSqlType);
                }

                // register the INOUT and OUT paramters with obtained SQL datatype
                if (colType == DatabaseMetaData.procedureColumnInOut || colType == DatabaseMetaData.procedureColumnOut) {
                    try {
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (SQLException e) {
                        Logger.getAnonymousLogger().severe(e.getMessage());
                        Logger.getAnonymousLogger().severe(e.toString());
                        throw e;
                    }
                }
             // register the RETURN with obtained SQL datatype
                if (colType == DatabaseMetaData.procedureColumnReturn) {
                    try {
                        cstmt.registerOutParameter(paramIndex, targetSqlType);
                    } catch (SQLException e) {
                        Logger.getAnonymousLogger().severe(e.getMessage());
                        Logger.getAnonymousLogger().severe(e.toString());
                        throw e;
                    }
                }
            }

            // execute the stored procedure
            boolean resultsAvailable = cstmt.execute();
            int count = -1;

            ResultSet rsProc;
            // iterate through the resultsets returned after execution
            do {
                ArrayList resultArray = new ArrayList();    // arraylist to hold the objects of ResultSetColumn

                count += 1;

                ResultSetMetaData rsmd;
                // if the resultset returns nothing, set the metadata object to null
                try {
                    rsProc = cstmt.getResultSet();
                    rsmd = rsProc.getMetaData();
                } catch (SQLException e) {
                    rsmd = null;
                }

                // get the count of the columns returned by the resultset
                int rsmdColCount = 0;
                if (rsmd != null) {
                    rsmdColCount = rsmd.getColumnCount();
                }
                // scroll through the resultset column information (metadata)
                for (int i = 1; i <= rsmdColCount; i++) {
                    ResultSetColumn currCol = new ResultSetColumn();
                    currCol.setOrdinalPosition(i);
                    currCol.setName(rsmd.getColumnName(i));
                    currCol.setLabel(rsmd.getColumnLabel(i));
                    currCol.setSqlType(getSQLTypeDescription(rsmd.getColumnType(i)));
                    currCol.setJavaType((String) SQLTOJAVATYPES.get(getSQLTypeDescription(rsmd.getColumnType(i))));

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
                if (resultArray.size() > 0) {
                    ResultSetColumns rsColbj = new ResultSetColumns();
                    rsColbj.setColumns(resultArray);
                    rsColbj.setName(pname + "_" + count);
                    result.add(rsColbj);
                }
            } while (cstmt.getMoreResults());
        } catch (SQLException e) {
            // resultset column metadata not supported
            Logger.getAnonymousLogger().severe("\nException occurred: " + e.getClass().getName() + "\n" + e.getMessage());
            Logger.getAnonymousLogger().severe(e.toString());
            errMsg = e.getLocalizedMessage();
        } catch (NullPointerException npe) {
            Logger.getAnonymousLogger().severe("\nException occurred: " + npe.getClass().getName() + "\n" + npe.getMessage());
            Logger.getAnonymousLogger().severe(npe.toString());
            errMsg = npe.getLocalizedMessage();
        } catch (Exception e) {
            // resultset column metadata not supported
            Logger.getAnonymousLogger().severe("\nException occurred: " + e.getClass().getName() + ", " + e.getMessage());
            Logger.getAnonymousLogger().severe(e.toString());
            errMsg = e.getLocalizedMessage();
        }

        // add the arraylist object to the Procedure object
        if ((result != null) && (result.size() > 0)) {
            procResult.setResultSetColumns(result);
        }

        procResult.setCallableStmtString(cstmtString);
        return procResult;
    }

    /**
     * added by Bobby to retrieve the resultset metadata of an SQL query
     * 
     * @param pcatalog Catalog (package) name of the procedure
     * @param pschema Schema name of the procdure
     * @param pname Name of the procedure
     * @param sqlText Text of the procedure/function
     * @return Procedure resultset encapsulated in a Procedure object
     * @throws SQLException, NullPointerException
     */
    public static final Procedure getQueryResultSet(final String pcatalog, final String pschema, final String pname, final String sqlText, final Connection connection)
            throws SQLException, NullPointerException {
        String errMsg = "";
        final Procedure procResult = new Procedure(pname, pcatalog, pschema, new String("PROCEDURE"));
        ResultSetColumns[] result = null;
        final ArrayList resultList = new ArrayList();

        try {
            final DatabaseMetaData dbmeta = connection.getMetaData();
            final Statement stmt = connection.createStatement();

            // retrieve the names of the fields in the select query
            // required if the query contains calculated fields
            final String[] queryFields = getQueryFields(sqlText);

            // execute the SQL query and retrieve the resultset
            final ResultSet rs = stmt.executeQuery(sqlText);
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int numColumns = rsmd.getColumnCount();

            for (int i = 1; i <= numColumns; i++) {
                final ResultSetColumn resultCol = new ResultSetColumn();
                resultCol.setOrdinalPosition(i);
                final String colName = rsmd.getColumnName(i).trim();
                final String colLabel = rsmd.getColumnLabel(i).trim();

                // check if the column names/labels are returned as null
                // (this happens in the case of derived/calculated fields and no aliases are
                // provided)
                if (colName.equalsIgnoreCase("") || colName == null) {
                    // parse the query string to extract derived field names
                    final String strFieldName = queryFields[i - 1];
                    resultCol.setName(strFieldName);
                } else {
                    resultCol.setName(colName);
                }

                if (colLabel.equalsIgnoreCase("") || colLabel == null) {
                    // parse the query string to extract derived field names
                    final String strFieldName = queryFields[i - 1];
                    resultCol.setLabel(strFieldName);
                } else {
                    resultCol.setLabel(colLabel);
                }

                resultCol.setSqlType(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i)));
                resultCol.setJavaType((String) DBMetaData.SQLTOJAVATYPES.get(DBMetaData.getSQLTypeDescription(rsmd.getColumnType(i))));

                if (rsmd.isNullable(i) == DatabaseMetaData.columnNullable) {
                    resultCol.setIsNullable(true);
                } else {
                    resultCol.setIsNullable(false);
                }

                // add ResultSetColumn object to the arraylist
                final boolean addToArray = resultList.add(resultCol);
            }

            result = new ResultSetColumns[1];
            result[0] = new ResultSetColumns();
            // add the arraylist to the ResultSetColumns object
            result[0].setColumns(resultList);
            result[0].setName(pname + "_0");
        } catch (final SQLException e) {
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
            throw e;
        } catch (final NullPointerException npe) {
            npe.printStackTrace();
            errMsg = npe.getLocalizedMessage();
            throw npe;
        } catch (final Exception e) {
            // resultset column metadata not supported
            e.printStackTrace();
            errMsg = e.getLocalizedMessage();
        }

        // add the ResultSetColumns array to the Procedure object
        procResult.setResultSetColumns(result);
        return procResult;
    }

    public String generateSybaseProcQuery(DatabaseMetaData dbmeta,String pcatalog, String pschema, String pname, String columnName)throws SQLException{
    	String cstmtString = "";
        int colCount = 0;
        boolean isFunction = false;
        boolean hasParameters = true;
    	ResultSet rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

        // loop to identify if the procedure is actually a function
        while (rs.next()) {
            if (rs.getShort("COLUMN_TYPE") == DatabaseMetaData.procedureColumnReturn) {
                // this is a function, so set the flag to true
                isFunction = true;
            }
        }

        rs = dbmeta.getProcedureColumns(pcatalog, pschema, pname, columnName);

        // get the count of the parameters
        while (rs.next()) {
            colCount++;
        }

        // check if the procedure has parameters or not
        if (colCount == 0) {
            hasParameters = false;
        }

        // construct the procedure execution command string
        if (isFunction == true) {
            cstmtString = "{ ? = call ";
            // use the package name to qualify the procedure name if the procedure is within a
            // package
            cstmtString += pname + "(";
            
            for (int j = 1; j < colCount; j++) {
                cstmtString += "?,";
            }

            // trim the last comma only if the procedure has any parameters
            if (hasParameters) {
                cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
            }
            cstmtString += ") }";
        } else {
            cstmtString = "call ";
            cstmtString += pname + "(";
            
            for (int j = 0; j < colCount; j++) {
                cstmtString += "?,";
            }

            // trim the last comma only if the procedure has any parameters
            if (hasParameters) {
                cstmtString = cstmtString.substring(0, cstmtString.length() - 1);
            }
            cstmtString += ")";
        }
    	
        return cstmtString;
    }
    
    /**
     * added by Bobby to retrieve the text of a procedure/function
     * 
     * @param Procedure Procedure object representing a procedure or function
     * @return String Text of the procedure or function
     */
    public static final String getProcedureText(final Procedure proc, final Connection connection) {
        String procText = "";
        String stmtString = "";
        final String procName = proc.getName();
        final String packageName = proc.getCatalog();

        // construct the SQL select query depending on whether
        // the procedure or function is part of a package or not
        if (packageName.equals("") || packageName == null) {
            stmtString = "select text from user_source where name = '" + procName + "'";
        } else {
            stmtString = "select text from user_source where name = '" + packageName + "'";
        }

        try {
            final Statement stmt = connection.createStatement();
            final ResultSet rsProcText = stmt.executeQuery(stmtString);

            while (rsProcText.next()) {
                procText += rsProcText.getString(1);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        //this.errMsg = e.getLocalizedMessage();
        }

        return procText;
    }

    /**
     * added by Bobby to parse an SQL query string and return a String array containing the names of
     * the select fields
     * 
     * @param sqlQuery the SQL query string to be parsed
     * @return String array containing the list of derived field names
     */
    private static final String[] getQueryFields(final String sqlQuery) {
        String[] strFieldNames = null;

        final String queryString = sqlQuery.toUpperCase().trim();
        final int fromIndex = queryString.indexOf("FROM");

        // extract the part of the query between the SELECT and the FROM keywords
        final String searchString = sqlQuery.substring(7, fromIndex);

        final StringTokenizer stFields = new StringTokenizer(searchString, ",");
        final int noTokens = stFields.countTokens();
        strFieldNames = new String[noTokens];

        int tokenNo = 0;
        // extract the string tokens fom the query (the derived columns)
        while (stFields.hasMoreTokens()) {
            strFieldNames[tokenNo] = stFields.nextToken().trim();
            tokenNo++;
        }

        return strFieldNames;
    }
}
