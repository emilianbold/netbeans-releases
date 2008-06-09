/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.iep.editor.wizard.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.SchemaAttribute;

/**
 *
 * @author radval
 */
public class DatabaseMetaDataHelper {

    private static Map<Integer, String> mSqlCodeToString = new HashMap<Integer, String>();
    
    private static Map<String, Integer> mStringToSqlCode = new HashMap<String, Integer>();
    
    /** List of JDBC SQL types */
    public static final String[] SQLTYPES = { 
            "ARRAY", 
            "BIGINT", 
            "BINARY", 
            "BIT", 
            "BLOB", 
            "BOOLEAN", 
            "CHAR", 
            "CLOB",
            "DATALINK", 
            "DATE", 
            "DECIMAL", 
            "DISTINCT", 
            "DOUBLE", 
            "FLOAT", 
            "INTEGER", 
            "JAVA_OBJECT", 
            "LONGVARBINARY",
            "LONGVARCHAR", 
            "NULL", 
            "NUMERIC", 
            "OTHER", 
            "REAL", 
            "REF", 
            "SMALLINT", 
            "STRUCT", 
            "TIME", 
            "TIMESTAMP",
            "TINYINT", 
            "VARBINARY", 
            "VARCHAR" };

    public static final int[] SQLTYPE_CODES = {
            java.sql.Types.ARRAY,          //1
            java.sql.Types.BIGINT,         //2 
            java.sql.Types.BINARY,         //3
            java.sql.Types.BIT,            //4  
            java.sql.Types.BLOB,           //5 
            java.sql.Types.BOOLEAN,        //6 
            java.sql.Types.CHAR,           //7 
            java.sql.Types.CLOB,           //8
            java.sql.Types.DATALINK,       //9 
            java.sql.Types.DATE,           //10
            java.sql.Types.DECIMAL,        //11 
            java.sql.Types.DISTINCT,       //12
            java.sql.Types.DOUBLE,         //13
            java.sql.Types.FLOAT,          //14
            java.sql.Types.INTEGER,        //15
            java.sql.Types.JAVA_OBJECT,    //16
            java.sql.Types.LONGVARBINARY,  //17
            java.sql.Types.LONGVARCHAR,    //18
            java.sql.Types.NULL,           //19
            java.sql.Types.NUMERIC,        //20
            java.sql.Types.OTHER,          //21
            java.sql.Types.REAL,           //22
            java.sql.Types.REF,            //23
            java.sql.Types.SMALLINT,       //24
            java.sql.Types.STRUCT,         //25
            java.sql.Types.TIME,           //26
            java.sql.Types.TIMESTAMP,      //27
            java.sql.Types.TINYINT,        //28
            java.sql.Types.VARBINARY,      //29
            java.sql.Types.VARCHAR };      //30

    
    static {
        for(int i = 0; i < SQLTYPE_CODES.length; i++) {
            mSqlCodeToString.put(SQLTYPE_CODES[i], SQLTYPES[i]);
            mStringToSqlCode.put(SQLTYPES[i], SQLTYPE_CODES[i]);
        }
    }
    
    public static String getSQLTypeString(int sqlCode) {
        return mSqlCodeToString.get(sqlCode);
    }
    
    public static int getSQLCode(String sqlTypeString) {
        return mStringToSqlCode.get(sqlTypeString);
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
    public static List<TableInfo> getTablesAndViews(final String catalog,
                                        final String schemaPattern,
                                        final String tablePattern,
                                        final boolean includeSystemTables,final Connection connection) throws Exception {
        String[] tableTypes;

        if (includeSystemTables) {
            final String[] types = { TableInfo.TABLE, TableInfo.VIEW, TableInfo.SYSTEM_TABLE };
            tableTypes = types;
        } else {
            final String[] types = { TableInfo.TABLE, TableInfo.VIEW };
            tableTypes = types;
        }

        return getTables(catalog, schemaPattern, tablePattern, tableTypes,connection);
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
    public static List<TableInfo> getTables(String catalog, 
                                             String schemaPattern, 
                                             String tablePattern, 
                                             final String[] tableTypes, 
                                             final Connection connection)
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

//            if (tablePattern != null) {
//                tablePattern = getJDBCSearchPattern(tablePattern, connection);
//            }

            ResultSet rs = connection.getMetaData().getTables(catalog, schemaPattern, tablePattern, tableTypes);

            List<TableInfo> tables = new ArrayList<TableInfo>();
           
            while (rs.next()) {
                String tableCatalog = rs.getString("TABLE_CAT");
                String tableSchema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String tableType = rs.getString("TABLE_TYPE");
                
//                if(tableCatalog != null && tableCatalog.equals("")) {
//                    tableCatalog = null;
//                }
//                
//                if(tableSchema != null && tableSchema.equals("")) {
//                    tableSchema = null;
//                }
                
                
                TableInfo table = new TableInfo(tableCatalog, tableSchema, tableName, tableType);
                
                // add table to Vector
                tables.add(table);
            }

            rs.close();
            return tables;
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
    public static void populateTableColumns(TableInfo table, Connection connection) throws Exception {
        ResultSet rs = null;

        try {
            String catalogName = table.getCatalogName();
            String schemaName = table.getSchemaName();
            String tableName = table.getTableName();
            
            // get table column information
            rs = connection.getMetaData().getColumns(catalogName, schemaName, tableName, "%");

            
            while (rs.next()) {
                //String defaultValue = rs.getString("COLUMN_DEF");
                int sqlTypeCode = rs.getInt("DATA_TYPE");
                String colName = rs.getString("COLUMN_NAME");
                String sqlType = getSQLTypeString(sqlTypeCode);
                int scale = rs.getInt("DECIMAL_DIGITS");
                int precision = rs.getInt("COLUMN_SIZE");
                
                ColumnInfo column = new ColumnInfo(colName, sqlType, precision, scale);
                table.addColumn(column);
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
                    /* Ignore... */;
                }
            }
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
    public static  void populatePrimaryKeys(TableInfo table,
                                       Connection v) throws Exception {
        ResultSet rs = null;

        try {
            String tcatalog = table.getCatalogName();
            String tschema = table.getSchemaName();
            String tname = table.getTableName();
            rs = v.getMetaData().getPrimaryKeys(tcatalog, tschema, tname);
            while(rs.next()) {
//                String tablecatalog = rs.getString("TABLE_CAT");
//                String tableschema = rs.getString("TABLE_SCHEM");
//                String tablename = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String keyName = rs.getString("PK_NAME");
                if(columnName != null) {
                    ColumnInfo column = table.findColumn(columnName);
                    if(column != null) {
                        PrimaryKeyInfo pk = new PrimaryKeyInfo(keyName, column);        
                        table.addPrimaryKey(pk);
                    }
                }
            }
            
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    /* Ignore */;
                }
            }
        }

        
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
    public static void populateForeignKeys(TableInfo table, Connection connection) throws Exception {
        ResultSet rs = null;

        try {
            String tcatalog = table.getCatalogName();
            String tschema = table.getSchemaName();
            String tname = table.getTableName();
                                           
            try {
                rs = connection.getMetaData().getImportedKeys(tcatalog, tschema, tname);
//                PKTABLE_CAT String => primary key table catalog being imported (may be null) 
//                PKTABLE_SCHEM String => primary key table schema being imported (may be null) 
//                PKTABLE_NAME String => primary key table name being imported 
//                PKCOLUMN_NAME String => primary key column name being imported 
//                FKTABLE_CAT String => foreign key table catalog (may be null) 
//                FKTABLE_SCHEM String => foreign key table schema (may be null) 
//                FKTABLE_NAME String => foreign key table name 
//                FKCOLUMN_NAME 
//                  FK_NAME String => foreign key name (may be null) 
//                  PK_NAME String => primary key name (may be null) 

                while(rs.next()) {
                    String fkName = rs.getString("FK_NAME");
                    String fkColumnName = rs.getString("FKCOLUMN_NAME");        
                    String fkTableName = rs.getString("FKTABLE_NAME");
                    String fkSchemaName = rs.getString("FKTABLE_SCHEM");
                    String fkCatalogName = rs.getString("FKTABLE_CAT");        
                            
                    String pkName = rs.getString("PK_NAME");
                    String pkTableName = rs.getString("PKTABLE_NAME");
                    String pkSchemaName = rs.getString("PKTABLE_SCHEM");
                    String pkCatalogName = rs.getString("PKTABLE_CAT");        
                    
                    if(fkColumnName != null) {
                        ColumnInfo column = table.findColumn(fkColumnName);
                        if(column != null) {
                            ForeignKeyInfo fk = new ForeignKeyInfo(fkName, 
                                                                    column, 
                                                                    pkName, 
                                                                    pkTableName, 
                                                                    pkSchemaName, 
                                                                    pkCatalogName);
                            table.addForeignKey(fk);
                        }
                    }
                            
                }
                
               
            } catch (final Exception e) {
                e.printStackTrace();
                throw e;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    /* Ignore */;
                }
            }
        }
      
    }

    
    public static String findJoinCondition(List<TableInfo> tables) {
    	StringBuffer joinCondition = new StringBuffer();
    	
    	Iterator<TableInfo> it = tables.iterator();
    	while(it.hasNext()) {
    		TableInfo table = it.next();
    		List<ForeignKeyInfo> fkList =  table.getForeignKeys();
    		processForeignKeys(fkList, tables, joinCondition);
    		
    	}
    	
    	return joinCondition.toString();
    }
    
    private static void processForeignKeys(List<ForeignKeyInfo> fks, 
    									   List<TableInfo> tables,
    									   StringBuffer joinCondition) {
    	
    	boolean foundSomeCondition = false;
    	
    	Iterator<ForeignKeyInfo> it = fks.iterator();
    	while(it.hasNext()) {
    		ForeignKeyInfo fk = it.next();
    		//if reference table is in our selection
    		//we can create join condition
    		TableInfo table = findReferncedTable(fk, tables);
    		if(table != null) {
    			//this column is current table which is a reference
    			//to column in another table
    			ColumnInfo fkColumn = fk.getForeignKeyColumn();
    			
    			String primaryKeyName = fk.getPrimaryKeyName();
    			//now find refered primary key from referenced table
    			PrimaryKeyInfo pk = table.findPrimaryKey(primaryKeyName);
    			if(pk != null) {
    				ColumnInfo pkColumn = pk.getColumn();
    				if(foundSomeCondition) {
    					joinCondition.append(" AND ");
    				}
    				joinCondition.append(fkColumn.getQualifiedName());
    				joinCondition.append("=");
    				joinCondition.append(pkColumn.getQualifiedName());
    				
    				foundSomeCondition = true;
    			}
    			
    		}
    	}
    }
    
    private static TableInfo findReferncedTable(ForeignKeyInfo fk, List<TableInfo> tables) {
    	TableInfo table = null;
    	String catalogName = fk.getPrimaryKeyCatalogName();
    	String schemaName = fk.getPrimaryKeySchemaName();
    	String tableName = fk.getPrimaryKeyTableName();
    	
    	table = findMatchingTable(catalogName, schemaName, tableName, tables);
    	
    	return table;
    }
    
    private static TableInfo findMatchingTable(String catalogName, 
    									String schemaName, 
    									String tableName,
    									List<TableInfo> tables) {
    	TableInfo table = null;
    	
    	Iterator<TableInfo> it = tables.iterator();
    	while(it.hasNext()) {
    		TableInfo t = it.next();
    		
    		String cName = t.getCatalogName();
    		String sName = t.getSchemaName();
    		String tName = t.getTableName();
    		
    		boolean foundTable = true;
    		
    		foundTable &= (catalogName != null) ? catalogName.equals(cName) : cName == null;
    		foundTable &= (schemaName != null) ? schemaName.equals(sName) : sName == null;
    		foundTable &= (tableName != null) ? tableName.equals(tName) : tName == null;
    		
    		if(foundTable) {
    			table = t;
    			break;
    		}
    	}
    	
    	return table;
    }
    
    public static SchemaAttribute createSchemaAttributeFromColumnInfo(ColumnInfo column, 
                                                               String attrName,
                                                               IEPModel model) {
            IEPComponentFactory factory = model.getFactory();
            SchemaAttribute sa = factory.createSchemaAttribute(model);
            
            sa.setAttributeName(attrName);
            String dataType = column.getColumnDataType();
            sa.setAttributeType(dataType);

            int precision = column.getPrecision();
            int scale = column.getScale();
            sa.setAttributeSize("");
            sa.setAttributeScale("");

            if(dataType.equalsIgnoreCase("CHAR")
               || dataType.equalsIgnoreCase("VARCHAR")
               || dataType.equalsIgnoreCase("DECIMAL") 
               || dataType.equalsIgnoreCase("FLOAT")
               ) {
                if(precision != 0) {
                    sa.setAttributeSize(""+column.getPrecision());
                } 
            }

            if(dataType.equalsIgnoreCase("DECIMAL")) {
                if(scale != 0) {
                    sa.setAttributeScale(""+column.getScale());
                } 
            }
            
            return sa;
        }
}
