/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.db.dataview.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 * Extracts database metadata information (table names and constraints, their
 * associated columns, etc.)
 *
 * @author Ahimanikya Satapathy
 */
public final class DBMetaDataFactory {

    public static final int NAME = 0;
    public static final int CATALOG = 1;
    public static final int SCHEMA = 2;
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
    public static final String[] DBTYPES = {
        DB2_TEXT, ORACLE_TEXT, SQLSERVER_TEXT,
        JDBC_TEXT, VSAM_ADABAS_IAM_TEXT, PostgreSQL_TEXT,
        MYSQL_TEXT, DERBY_TEXT, MYSQL_TEXT, AXION_TEXT
    };

    private Connection dbconn; // db connection
    private int dbType;
    private DatabaseMetaData dbmeta; // db metadata
    private Logger mLogger = Logger.getLogger(DBMetaDataFactory.class.getName());

    public DBMetaDataFactory(Connection conn) throws DBException {
        if (conn == null) {
            throw new NullPointerException("Connection can't be null.");
        }
        dbconn = conn;

        // get the metadata
        try {
            dbmeta = dbconn.getMetaData();
            dbType = getDBTypeCode(getDBName());
        } catch (Exception e) {
            mLogger.severe(DBException.getMessage(e));
            throw new DBException(e);
        }
    }
    
    public String getEscapeString() throws SQLException {
        return dbmeta.getIdentifierQuoteString();
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
            mLogger.severe(DBException.getMessage(e));
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
        if (dbmeta.getURL() != null) {
            return getDBTypeFromURL(dbmeta.getURL());
        }
        return getDBTypeFromURL(getDBName());
    }

    public static String getDBTypeFromURL(String url) {
        String dbtype = "";

        // get the database type based on the product name converted to lowercase
        url = url.toLowerCase();
        if (url.indexOf("sybase") > -1) {
            // sybase
            dbtype = SYBASE;
        } else if (url.equals("microsoft sql server") || (url.equals("sql server"))) {
            // Microsoft SQL Server
            dbtype = SQLSERVER;
        } else if ((url.indexOf("db2") > -1) || (url.equals("as"))) {
            // DB2
            dbtype = DB2;
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
        } else {
            // other type, default to JDBC-ODBC
            dbtype = JDBC;
        }

        return dbtype;
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
    private DBPrimaryKey getPrimaryKeys(String tcatalog, String tschema, String tname) throws Exception {
        ResultSet rs = null;
        try {
            rs = dbmeta.getPrimaryKeys(setToNullIfEmpty(tcatalog), setToNullIfEmpty(tschema), tname);
            return new DBPrimaryKey(rs);
        } catch (Exception e) {
            mLogger.severe(DBException.getMessage(e));
            throw e;
        } finally {
            DataViewUtils.closeResources(rs);
        }
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
    public Map<String, DBForeignKey> getForeignKeys(DBTable table) throws Exception {
        Map<String, DBForeignKey> fkList = Collections.emptyMap();
        ResultSet rs = null;
        try {
            rs = dbmeta.getImportedKeys(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName());
            fkList = DBForeignKey.createForeignKeyColumnMap(table, rs);
        } catch (Exception e) {
            mLogger.severe(DBException.getMessage(e));
            mLogger.warning("JDBC driver does not support java.sql.ParameterMetaData " + e.getMessage());
            throw e;
        } finally {
            DataViewUtils.closeResources(rs);
        }
        return fkList;
    }

    public synchronized  Collection<DBTable> generateDBTables(ResultSet rs) throws DBException {
        Map<String, DBTable> tables = new HashMap<String, DBTable>();
        String noTableName = "UNKNOWN";
        try {
            // get table column information
            ResultSetMetaData rsMeta = rs.getMetaData();
            for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
                String tableName = rsMeta.getTableName(i);
                String schemaName = rsMeta.getSchemaName(i);
                String catalogName = rsMeta.getCatalogName(i);
                String key = catalogName+schemaName+tableName;
                if(key.equals("")){
                    key = noTableName;
                }
                DBTable table = tables.get(key);
                if(table == null){
                    table = new DBTable(tableName, schemaName, catalogName);
                    tables.put(key, table);
                }
                        
                int sqlTypeCode = rsMeta.getColumnType(i);
                if (sqlTypeCode == java.sql.Types.OTHER && dbType == 1) {
                    String sqlTypeStr = rsMeta.getColumnTypeName(i);
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
                
                String colName = rsMeta.getColumnName(i);
                int position = i;
                int scale = rsMeta.getScale(i);
                int precision = rsMeta.getPrecision(i);

                boolean isNullable = (rsMeta.isNullable(i) == rsMeta.columnNullable);
                String displayName = rsMeta.getColumnLabel(i);
                int displaySize = rsMeta.getColumnDisplaySize(i);
                boolean autoIncrement = rsMeta.isAutoIncrement(i);

                // create a table column and add it to the vector
                DBColumn col = new DBColumn(colName, sqlTypeCode, scale, precision, isNullable, autoIncrement);
                col.setParent(table);
                col.setOrdinalPosition(position);
                col.setDisplayName(displayName);
                col.setEditable(!tableName.equals("") && !autoIncrement);
                col.setDisplaySize(displaySize);

                table.addColumn(col);
                table.setEscapeString(getEscapeString());
            }
            
            for(DBTable table: tables.values()){
                checkPrimaryKeys(table);
                checkForeignKeys(table);
            }
     
        } catch (Exception e) {
            mLogger.severe(DBException.getMessage(e));
            throw new DBException(e);
        } 
        return tables.values();
    }
    
    
    /**
     * Gets the primary keys for a table.
     *
     * @param newTable Table to get the primary key(s) for
     * @throws Exception 
     */
    private void checkPrimaryKeys(DBTable newTable) throws Exception {
        try {
            DBPrimaryKey keys = getPrimaryKeys(newTable.getCatalog(), newTable.getSchema(), newTable.getName());
            if (keys.getColumnCount() != 0) {
                newTable.setPrimaryKey(keys);

                // now loop through all the columns flagging the primary keys
                List columns = newTable.getColumnList();
                if (columns != null) {
                    for (int i = 0; i < columns.size(); i++) {
                        DBColumn col = (DBColumn) columns.get(i);
                        if (keys.contains(col.getName())) {
                            col.setPrimaryKey(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.severe(DBException.getMessage(e));
            throw e;
        }
    }

    /**
     * Gets the foreign keys for a table.
     *
     * @param newTable Table to get the foreign key(s) for
     * @throws Exception 
     */
    private void checkForeignKeys(DBTable newTable) throws Exception {
        try {
            // get the foreing keys
            Map<String, DBForeignKey> foreignKeys = getForeignKeys(newTable);
            if (foreignKeys != null) {
                newTable.setForeignKeyMap(foreignKeys);

                // create a hash set of the keys
                Set<String> foreignKeysSet = new HashSet<String>();
                Iterator<DBForeignKey> it = foreignKeys.values().iterator();
                while (it.hasNext()) {
                    DBForeignKey key = it.next();
                    if (key != null) {
                        foreignKeysSet.addAll(key.getColumnNames());
                    }
                }

                // now loop through all the columns flagging the foreign keys
                List columns = newTable.getColumnList();
                if (columns != null) {
                    for (int i = 0; i < columns.size(); i++) {
                        DBColumn col = (DBColumn) columns.get(i);
                        if (foreignKeysSet.contains(col.getName())) {
                            col.setForeignKey(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.severe(DBException.getMessage(e));
            throw e;
        }
    }
    
    private int getDBTypeCode(String name){
        for (int i = 0; i < DBTYPES.length; i++) {
            String dbName = DBTYPES[i];
            if(dbName.equals(name)){
                return i;
            }
        }
        return -1;
    }

    private String setToNullIfEmpty(String source) {
        if (source != null && source.equals("")) {
            source = null;
        }
        return source;
    }
}
