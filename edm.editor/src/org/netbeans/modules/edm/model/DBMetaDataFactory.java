/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.edm.model;

import org.netbeans.modules.edm.model.EDMException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.edm.editor.utils.DBExplorerUtil;
import org.netbeans.modules.edm.model.impl.AbstractDBColumn;
import org.netbeans.modules.edm.model.impl.AbstractDBTable;
import org.netbeans.modules.edm.model.impl.ForeignKeyImpl;
import org.netbeans.modules.edm.model.impl.PrimaryKeyImpl;
import org.netbeans.modules.edm.model.impl.SourceColumnImpl;
import org.netbeans.modules.edm.editor.utils.DBConnectionFactory;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Extracts database metadata information (table names and constraints, their
 * associated columns, etc.)
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DBMetaDataFactory {

    /** Index to the name field for results of table/view/procedure searches */
    public static final int NAME = 0;
    /** Index to the catalog field for results of table/view/procedure searches */
    public static final int CATALOG = 1;
    /** Index to the schema field for results of table/view/procedure searches */
    public static final int SCHEMA = 2;
    /** Index to the type field for results of table/view/procedure searches */
    public static final int TYPE = 3;
    public static final String DB2 = "DB2"; // NOI18N
    public static final String ORACLE = "ORACLE"; // NOI18N
    public static final String AXION = "AXION"; // NOI18N
    public static final String DERBY = "DERBY"; // NOI18N
    public static final String PostgreSQL = "PostgreSQL"; // NOI18N
    public static final String MYSQL = "MYSQL"; // NOI18N
    public static final String SQLSERVER = "SQLSERVER"; // NOI18N
    public static final String SYBASE = "SYBASE"; // NOI18N
    public static final String JDBC = "JDBC"; // NOI18N
    public static final String VSAM_ADABAS_IAM = "LEGACY"; // NOI18N
    public static final String JDBC_ODBC = "JDBC"; // NOI18N
    public static final String DB2_TEXT = "DB2"; // NOI18N
    public static final String ORACLE_TEXT = "ORACLE"; // NOI18N
    public static final String AXION_TEXT = "AXION"; // NOI18N
    public static final String DERBY_TEXT = "DERBY"; // NOI18N
    public static final String MYSQL_TEXT = "MYSQL"; // NOI18N
    public static final String PostgreSQL_TEXT = "PostgreSQL"; // NOI18N
    public static final String SQLSERVER_TEXT = "SQL SERVER"; // NOI18N
    public static final String JDBC_TEXT = "JDBC"; // NOI18N
    public static final String VSAM_ADABAS_IAM_TEXT = "VSAM/ADABAS/IAM"; // NOI18N
    /** List of database type display descriptions */
    public static final String[] DBTYPES = {DB2_TEXT, ORACLE_TEXT, SQLSERVER_TEXT, JDBC_TEXT, VSAM_ADABAS_IAM_TEXT, PostgreSQL_TEXT, MYSQL_TEXT, DERBY_TEXT, MYSQL_TEXT, AXION_TEXT};
    private static final String SYSTEM_TABLE = "SYSTEM TABLE"; // NOI18N
    private static final String TABLE = "TABLE"; // NOI18N
    private static final String VIEW = "VIEW"; // NOI18N
    private Connection dbconn; // db connection
    private DatabaseMetaData dbmeta; // db metadata

    /**
     * Gets the primary keys for a table.
     *
     * @param newTable Table to get the primary key(s) for
     * @throws Exception 
     */
    private void checkPrimaryKeys(AbstractDBTable newTable) throws Exception {
        try {
            PrimaryKeyImpl keys = getPrimaryKeys(newTable.getCatalog(), newTable.getSchema(), newTable.getName());
            if (keys != null && keys.getColumnCount() != 0) {
                newTable.setPrimaryKey(keys);

                // now loop through all the columns flagging the primary keys
                List columns = newTable.getColumnList();
                if (columns != null) {
                    for (int i = 0; i < columns.size(); i++) {
                        SQLDBColumn col = (SQLDBColumn) columns.get(i);
                        if (keys.contains(col.getName())) {
                            col.setPrimaryKey(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "checkPrimaryKeys", e);
        }
    }

    /**
     * Gets the foreign keys for a table.
     *
     * @param newTable Table to get the foreign key(s) for
     * @throws Exception 
     */
    private void checkForeignKeys(AbstractDBTable newTable) throws Exception {
        try {
            // get the foreing keys
            Map<String, ForeignKey> foreignKeys = getForeignKeys(newTable);
            if (foreignKeys != null && foreignKeys.size() != 0) {
                newTable.setForeignKeyMap(foreignKeys);

                // create a hash set of the keys
                Set<String> foreignKeysSet = new HashSet<String>();
                Iterator<ForeignKey> it = foreignKeys.values().iterator();
                while (it.hasNext()) {
                    ForeignKey key = it.next();
                    if (key != null) {
                        foreignKeysSet.addAll(key.getColumnNames());
                    }
                }

                // now loop through all the columns flagging the foreign keys
                List columns = newTable.getColumnList();
                if (columns != null) {
                    for (int i = 0; i < columns.size(); i++) {
                        SQLDBColumn col = (SQLDBColumn) columns.get(i);
                        if (foreignKeysSet.contains(col.getName())) {
                            col.setForeignKey(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "checkForeignKeys", e);
        }
    }
    private static Logger mLogger = Logger.getLogger("DM.DI" + DBMetaDataFactory.class.getName());

    /**
     * Establishes a connection to the database.
     *
     * @param conn JDBC connection
     * @throws Exception 
     */
    public void connectDB(Connection conn) throws Exception {
        if (conn == null) {
            throw new NullPointerException(NbBundle.getMessage(DBMetaDataFactory.class, "ERROR_Connection_can't_be_null"));
        }
        dbconn = conn;
        getDBMetaData();
    }
    
    public Connection showConnectionDialog(final DatabaseConnection dbConn) {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {

            public Void run() {
                ConnectionManager.getDefault().showConnectionDialog(dbConn);
                return null;
            }
        });

        synchronized (DBConnectionFactory.class) {
            if (dbConn != null) {
                return dbConn.getJDBCConnection();
            }
        }
        return null;
    }

    /**
     * Disconnects from the database.
     *
     * @throws Exception 
     */
    public void disconnectDB() {
        // close connection to database
        try {
            if ((dbconn != null) && (!dbconn.isClosed())) {
                DBExplorerUtil.closeIfLocalConnection(dbconn);
            }
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "disconnectDB", e);
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            /* Ignore... */
            }
        }
    }

    private void getDBMetaData() throws Exception {
        // get the metadata
        try {
            dbmeta = dbconn.getMetaData();
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "getDBMetaData", e);
            throw e;
        }
    }

    /**
     * Returns the database product name
     *
     * @return String database product name
     * @throws Exception 
     */
    public String getDBName() throws Exception {
        String dbname = "";
        // get the database product name
        try {
            dbname = dbmeta.getDatabaseProductName();
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "getDBName", e);
            throw e;
        }
        return dbname;
    }

    /**
     * Returns the database Database type.
     *
     * @return String Database Database type
     * @throws Exception 
     */
    public String getDBType() throws Exception {
        // get the database type based on the product name converted to lowercase
        if(dbmeta.getURL() != null) {
            return getDBTypeFromURL(dbmeta.getURL());
        }
        return getDBTypeFromURL(getDBName());
    }

    public static String getDBTypeFromURL(String url) throws Exception {
        String dbtype = "";

        // get the database type based on the product name converted to lowercase
        url = url.toLowerCase();
        if (url.indexOf("sybase") > -1) {
            // sybase
            dbtype = SYBASE;
        }  else if (url.equals("microsoft sql server") || (url.equals("sql server"))) {
            // Microsoft SQL Server
            dbtype = SQLSERVER;
        } else if ((url.equals("exadas")) || (url.equals("attunity connect driver"))) {
            // VSAM
            dbtype = VSAM_ADABAS_IAM;
        } else if (url.indexOf("orac") > -1) {
            // Oracle
            dbtype = ORACLE;
        } else if (url.indexOf("axion") > -1) {
            // Axion
            dbtype = AXION;
        } else if (url.indexOf("derby") > -1) {
            // Derby
            dbtype = DERBY;
        } else if (url.indexOf("postgre") > -1) {
            // PostgreSQL
            dbtype = PostgreSQL;
        } else if (url.indexOf("mysql") > -1) {
            // MySQL
            dbtype = MYSQL;
        } else if ((url.indexOf("db2") > -1) || (url.equals("as"))) {
            // DB2
            dbtype = DB2;
        } else {
            // other type, default to JDBC-ODBC
            dbtype = JDBC;
        }

        return dbtype;
    }

    private String getJDBCSearchPattern(String guiPattern) throws Exception {
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
            mLogger.log(Level.SEVERE, "getJDBCSearchPattern", e);
            throw e;
        }
    }

    /**
     * Returns a list of schemas in the database.
     *
     * @return String[] List of schema names
     * @throws Exception 
     */
    public String[] getSchemas() throws Exception {
        ResultSet rs = null;
        try {
            rs = dbmeta.getSchemas();
            Vector<String> v = new Vector<String>();
            String[] schemaNames = null;

            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                v.add(schema);
            }

            if (v.size() > 0) {
                schemaNames = new String[v.size()];
                v.copyInto(schemaNames);
            }
            return schemaNames;
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getSchemas", e);
            throw e;
        } finally {
            closeResultSet(rs);
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
     * @throws Exception 
     */
    public String[][] getTablesOnly(String catalog, String schemaPattern, String tablePattern, boolean includeSystemTables) throws Exception {
        if (includeSystemTables) {
            return getTables(catalog, schemaPattern, tablePattern, new String[]{TABLE, SYSTEM_TABLE});
        } else {
            return getTables(catalog, schemaPattern, tablePattern, new String[]{TABLE});
        }
    }

    /**
     * Returns a list of views matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param viewPattern View name pattern
     * @param includeSystemTables Indicate whether to include system tables in search
     * @return String[][] List of views matching search filters
     * @throws Exception 
     */
    public String[][] getViewsOnly(String catalog, String schemaPattern, String viewPattern, boolean includeSystemTables) throws Exception {
        if (includeSystemTables) {
            return getTables(catalog, schemaPattern, viewPattern, new String[]{VIEW, SYSTEM_TABLE});
        } else {
            return getTables(catalog, schemaPattern, viewPattern, new String[]{VIEW});
        }
    }

    /**
     * Returns a list of tables and views matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param tablePattern Table/View name pattern
     * @param includeSystemTables Indicate whether to include system tables in search
     * @return String[][] List of tables and views matching search filters
     * @throws Exception 
     */
    public String[][] getTablesAndViews(String catalog, String schemaPattern, String tablePattern, boolean includeSystemTables) throws Exception {
        if (includeSystemTables) {
            return getTables(catalog, schemaPattern, tablePattern, new String[]{TABLE, VIEW, SYSTEM_TABLE});
        } else {
            return getTables(catalog, schemaPattern, tablePattern, new String[]{TABLE, VIEW});
        }
    }

    /**
     * Returns a list of tables/views matching in the passed in filters.
     *
     * @param catalog Catalog name
     * @param schemaPattern Schema pattern
     * @param tablePattern Table/View name pattern
     * @param tableTypes List of table types to include (ex. TABLE, VIEW)
     * @return String[][] List of tables matching search filters
     * @throws Exception 
     */
    public String[][] getTables(String catalog, String schemaPattern, String tablePattern, String[] tableTypes) throws Exception {
        ResultSet rs = null;
        try {
            catalog = setToNullIfEmpty(catalog);
            schemaPattern = setToNullIfEmpty(schemaPattern);
            tablePattern = setToNullIfEmpty(tablePattern);

            if (tablePattern != null && !AXION.equals(getDBType())) {
                tablePattern = getJDBCSearchPattern(tablePattern);
            }

            rs = dbmeta.getTables(catalog, schemaPattern, tablePattern, tableTypes);

            Vector<String[]> v = new Vector<String[]>();
            String[][] tables = null; // array of table structures: Name, Catalog, Schema
            while (rs.next()) {
                String tableCatalog = rs.getString("TABLE_CAT");
                String tableSchema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String tableType = rs.getString("TABLE_TYPE");

                // fill in table info
                String[] tableItem = new String[4]; // hold info for each table
                tableItem[NAME] = tableName;
                tableItem[CATALOG] = (tableCatalog == null ? "" : tableCatalog);
                tableItem[SCHEMA] = (tableSchema == null ? "" : tableSchema);
                tableItem[TYPE] = tableType;

                // add table to Vector
                v.add(tableItem);
            }

            // now copy Vector to array to return back
            if (v.size() > 0) {
                tables = new String[v.size()][4];
                v.copyInto(tables);
            }
            return tables;
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getTables", e);
            throw e;
        } finally {
            closeResultSet(rs);
        }
    }

    /**
     * Returns a list of primary keys for a table.
     *
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param tname Table name
     * @return List List of primary keys
     * @throws Exception 
     */
    private PrimaryKeyImpl getPrimaryKeys(String tcatalog, String tschema, String tname) throws Exception {
        ResultSet rs = null;
        try {
            rs = dbmeta.getPrimaryKeys(setToNullIfEmpty(tcatalog), setToNullIfEmpty(tschema), tname);
            return new PrimaryKeyImpl(rs);
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getPrimaryKeys", e);            
        } finally {
            closeResultSet(rs);
        }
        return null;
    }

    /**
     * Returns a list of foreign keys for a table.
     *
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param tname Table name
     * @return List List of foreign keys
     * @throws Exception 
     */
    private Map<String, ForeignKey> getForeignKeys(DBTable table) throws Exception {
        Map<String, ForeignKey> fkList = Collections.emptyMap();
        ResultSet rs = null;
        try {
            rs = dbmeta.getImportedKeys(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName());
            fkList = ForeignKeyImpl.createForeignKeyColumnMap(table, rs);
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getForeignKeys", e);
            mLogger.warning(NbBundle.getMessage(DBMetaDataFactory.class, "LOG_JDBC_driver_does_not_support") + e.getMessage());
        } finally {
            closeResultSet(rs);
        }
        return fkList;
    }

    public boolean isTableOrViewExist(String tcatalog, String tschema, String tname) throws Exception {
        String[][] tables = getTablesAndViews(tcatalog, tschema, tname, false);
        if (tables == null || tables[0] == null || !tname.equalsIgnoreCase(tables[0][0])) {
            return false;
        }
        return true;
    }

    public void populateColumns(SQLDBTable table) throws Exception {
        ResultSet rs = null;
        try {
            // get table column information
            rs = dbmeta.getColumns(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName(), "%");
            while (rs.next()) {
                String defaultValue = rs.getString("COLUMN_DEF");
                int sqlTypeCode = rs.getInt("DATA_TYPE");
                if (sqlTypeCode == java.sql.Types.OTHER && getDBType().equals(ORACLE)) {
                    String sqlTypeStr = rs.getString("TYPE_NAME");
                    if (sqlTypeStr.startsWith("TIMESTAMP")) {
                        sqlTypeCode = java.sql.Types.TIMESTAMP;
                    } else if (sqlTypeStr.startsWith("FLOAT")) {
                        sqlTypeCode = java.sql.Types.FLOAT;
                    } else if (sqlTypeStr.startsWith("REAL")) {
                        sqlTypeCode = java.sql.Types.REAL;
                    } else if (sqlTypeStr.startsWith("BLOB")) {
                        sqlTypeCode = java.sql.Types.BLOB;
                    } else if (sqlTypeStr.startsWith("CLOB")) {
                        sqlTypeCode = java.sql.Types.CLOB;
                    }
                }
                String colName = rs.getString("COLUMN_NAME");
                int position = rs.getInt("ORDINAL_POSITION");
                int scale = rs.getInt("DECIMAL_DIGITS");
                int precision = rs.getInt("COLUMN_SIZE");

                boolean isNullable = rs.getString("IS_NULLABLE").equals("YES") ? true : false;

                // create a table column and add it to the vector

                SQLDBColumn col = createColumn(table);
                col.setName(colName);
                col.setJdbcType(sqlTypeCode);
                col.setNullable(isNullable);
                col.setPrimaryKey(false);
                col.setForeignKey(false);
                col.setOrdinalPosition(position);
                col.setPrecision(precision);
                col.setScale(scale);

                if (defaultValue != null) {
                    col.setDefaultValue(defaultValue.trim());
                }
                table.addColumn(col);
            }

            if (table instanceof AbstractDBTable) {
                checkPrimaryKeys((AbstractDBTable) table);
                checkForeignKeys((AbstractDBTable) table);

            //    try {
            //        // get index info for this table
            //        rs = dbmeta.getIndexInfo(tcatalog, tschema, tname, false, true);
            //        Iterator<IndexImpl> it = IndexImpl.createIndexList(rs).iterator();
            //        while (it.hasNext()) {
            //            ((AbstractDBTable) table).addIndex(it.next());
            //        }
            //    } catch (Exception e) {
            //        // ignore and continue
            //    }
            }


        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getTableMetaData", e);
            throw e;
        } finally {
            closeResultSet(rs);
        }
    }

    private SQLDBColumn createColumn(SQLDBTable table) {
        if (table.getObjectType() == SQLConstants.SOURCE_TABLE) {
            return new SourceColumnImpl();
        } 
        else {
            return new AbstractDBColumn() {

                public String toXMLString(String prefix) throws EDMException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                protected String getElementTagName() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
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

    private String setToNullIfEmpty(String source) {
        if (source != null && source.equals("")) {
            source = null;
        }
        return source;
    }
}
