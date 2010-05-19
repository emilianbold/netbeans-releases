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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.model;

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

import org.openide.util.NbBundle;

/**
 * Extracts database metadata information (table names and constraints, their
 * associated columns, etc.)
 *
 * @author Ahimanikya Satapathy
 */
public class VirtualDBMetaDataFactory {

    public static final int NAME = 0;
    public static final int CATALOG = 1;
    public static final int SCHEMA = 2;
    public static final int TYPE = 3;
    
    private static final String SYSTEM_TABLE = "SYSTEM TABLE"; // NOI18N
    private static final String TABLE = "TABLE"; // NOI18N
    private static final String VIEW = "VIEW"; // NOI18N
    
    private Connection dbconn;
    private DatabaseMetaData dbmeta;
    private static Logger mLogger = Logger.getLogger("DM.VDB" + VirtualDBMetaDataFactory.class.getName());

    public void connectDB(Connection conn) throws Exception {
        if (conn == null) {
            throw new NullPointerException(NbBundle.getMessage(VirtualDBMetaDataFactory.class, "MSG_NullConnection"));
        }
        dbconn = conn;
        getDBMetaData();
    }

    public void disconnectDB() {
        try {
            if ((dbconn != null) && (!dbconn.isClosed())) {
                DBExplorerUtil.closeIfLocalConnection(dbconn);
            }
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "disconnectDB", e);
        }
    }
    
    public String[][] getTablesAndViews(String catalog, String schemaPattern, String tablePattern, boolean includeSystemTables) throws Exception {
        if (includeSystemTables) {
            return getTables(catalog, schemaPattern, tablePattern, new String[]{TABLE, VIEW, SYSTEM_TABLE});
        } else {
            return getTables(catalog, schemaPattern, tablePattern, new String[]{TABLE, VIEW});
        }
    }

    public String[][] getTables(String catalog, String schemaPattern, String tablePattern, String[] tableTypes) throws Exception {
        ResultSet rs = null;
        try {
            catalog = setToNullIfEmpty(catalog);
            schemaPattern = setToNullIfEmpty(schemaPattern);
            tablePattern = setToNullIfEmpty(tablePattern);

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

    public void populateColumns(VirtualDBTable table) throws Exception {
        ResultSet rs = null;
        try {
            // get table column information
            rs = dbmeta.getColumns(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName(), "%");
            while (rs.next()) {
                String defaultValue = rs.getString("COLUMN_DEF");
                int sqlTypeCode = rs.getInt("DATA_TYPE");
                String colName = rs.getString("COLUMN_NAME");
                int position = rs.getInt("ORDINAL_POSITION");
                int scale = rs.getInt("DECIMAL_DIGITS");
                int precision = rs.getInt("COLUMN_SIZE");

                boolean isNullable = rs.getString("IS_NULLABLE").equals("YES") ? true : false;

                // create a table column and add it to the vector
                VirtualDBColumn col = new VirtualDBColumn();
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

            if (table instanceof VirtualDBTable) {
                checkPrimaryKeys((VirtualDBTable) table);
                checkForeignKeys((VirtualDBTable) table);
            }


        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getTableMetaData", e);
            throw e;
        } finally {
            closeResultSet(rs);
        }
    }

    private PrimaryKey getPrimaryKeys(String tcatalog, String tschema, String tname) throws Exception {
        ResultSet rs = null;
        try {
            rs = dbmeta.getPrimaryKeys(setToNullIfEmpty(tcatalog), setToNullIfEmpty(tschema), tname);
            return new PrimaryKey(rs);
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getPrimaryKeys", e);
        } finally {
            closeResultSet(rs);
        }
        return null;
    }

    private Map<String, ForeignKey> getForeignKeys(VirtualDBTable table) throws Exception {
        Map<String, ForeignKey> fkList = Collections.emptyMap();
        ResultSet rs = null;
        try {
            rs = dbmeta.getImportedKeys(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName());
            fkList = ForeignKey.createForeignKeyColumnMap(table, rs);
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "getForeignKeys", e);
        } finally {
            closeResultSet(rs);
        }
        return fkList;
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
        try {
            dbmeta = dbconn.getMetaData();
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "getDBMetaData", e);
            throw e;
        }
    }

    private void checkPrimaryKeys(VirtualDBTable newTable) throws Exception {
        try {
            PrimaryKey keys = getPrimaryKeys(newTable.getCatalog(), newTable.getSchema(), newTable.getName());
            if (keys != null && keys.getColumnCount() != 0) {
                newTable.setPrimaryKey(keys);

                // now loop through all the columns flagging the primary keys
                List columns = newTable.getColumnList();
                if (columns != null) {
                    for (int i = 0; i < columns.size(); i++) {
                        VirtualDBColumn col = (VirtualDBColumn) columns.get(i);
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

    private void checkForeignKeys(VirtualDBTable newTable) throws Exception {
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
                        VirtualDBColumn col = (VirtualDBColumn) columns.get(i);
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

    private String setToNullIfEmpty(String source) {
        if (source != null && source.equals("")) {
            source = null;
        }
        return source;
    }
}
