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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.test.stub.api.Stub;

/**
 *
 * @author Andrei Badea
 */
public final class JDBCStubUtil {
    
    // TODO need methods for returning the resultset columns, not just the resultsets
    
    private JDBCStubUtil() {
    }
    
    public static ResultSet createResultSet(List/*<List<Object>>*/ columns) {
        return (ResultSet)Stub.create(ResultSet.class, new ResultSetImpl(columns));
    }
    
    public static ResultSet singleColumnResultSet(String columnName, String[] values) {
        List column = new ArrayList();
        column.add(columnName);
        column.addAll(Arrays.asList(values));
        
        List columns = Collections.singletonList(column);
        return createResultSet(columns);
    }
    
    public static ResultSet catalogsResultSet(String[] catalogNames) {
        return singleColumnResultSet("TABLE_CAT", catalogNames);
    }
    
    public static ResultSet schemasResultSet(String[] schemaNames) {
        return singleColumnResultSet("TABLE_SCHEM", schemaNames);
    }
    
    /**
     * @param tablesBySchema something like { { "schema1", "table1", "table2" }, { "schema2", "table1" }, ... }
     */
    public static ResultSet tablesResultSet(String[][] tableNamesBySchema) {
        List tableSchemCol = new ArrayList();
        tableSchemCol.add("TABLE_SCHEM");
        List tableNameCol = new ArrayList();
        tableNameCol.add("TABLE_NAME");
        
        for (int i = 0; i < tableNamesBySchema.length; i++) {
            String[] tables = tableNamesBySchema[i];
            assert tables.length > 1;
            
            for (int j = 1; j < tables.length; j++) {
                tableSchemCol.add(tables[0]);
                tableNameCol.add(tables[j]);
            }
        }
        
        List columns = new ArrayList();
        columns.add(tableSchemCol);
        columns.add(tableNameCol);
        return createResultSet(columns);
    }
    
    public static ResultSet columnsResultSet(String[] columnNames, String[] typeNames, int[] sqlTypes, int[] columnSizes, int[] decimalDigits, int[] nullables) {
        List columnNameColumn = writeableSingletonList("COLUMN_NAME");
        columnNameColumn.addAll(Arrays.asList(columnNames));
        
        List typeNameColumn = writeableSingletonList("TYPE_NAME");
        typeNameColumn.addAll(Arrays.asList(typeNames));
        
        List sqlTypeColumn = writeableSingletonList("DATA_TYPE");
        addAllAsReferenceType(sqlTypeColumn, sqlTypes);
        
        List columnSizeColumn = writeableSingletonList("COLUMN_SIZE");
        addAllAsReferenceType(columnSizeColumn, columnSizes);
        
        List decimalDigitsColumn = writeableSingletonList("DECIMAL_DIGITS");
        addAllAsReferenceType(decimalDigitsColumn, decimalDigits);
        
        List nullablesColumn = writeableSingletonList("NULLABLE");
        addAllAsReferenceType(nullablesColumn, nullables);
        
        List columns = new ArrayList();
        columns.add(columnNameColumn);
        columns.add(typeNameColumn);
        columns.add(sqlTypeColumn);
        columns.add(columnSizeColumn);
        columns.add(decimalDigitsColumn);
        columns.add(nullablesColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet indexesResultSet(String[] indexNames, String[] columnNames, boolean[] nonUniques) {
        List indexNameColumn = writeableSingletonList("INDEX_NAME");
        indexNameColumn.addAll(Arrays.asList(indexNames));
        
        List columnNameColumn = writeableSingletonList("COLUMN_NAME");
        columnNameColumn.addAll(Arrays.asList(columnNames));
        
        List nonUniqueColumn = writeableSingletonList("NON_UNIQUE");
        addAllAsReferenceType(nonUniqueColumn, nonUniques);
        
        List columns = new ArrayList();
        columns.add(indexNameColumn);
        columns.add(columnNameColumn);
        columns.add(nonUniqueColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet primaryKeysResultSet(String[] pkNames, String[] columnNames, short[] keySeqs) {
        List pkNameColumn = writeableSingletonList("PK_NAME");
        pkNameColumn.addAll(Arrays.asList(pkNames));
        
        List columnNameColumn = writeableSingletonList("COLUMN_NAME");
        columnNameColumn.addAll(Arrays.asList(columnNames));
        
        List keySeqColumn = writeableSingletonList("KEY_SEQ");
        addAllAsReferenceType(keySeqColumn, keySeqs);
        
        List columns = new ArrayList();
        columns.add(pkNameColumn);
        columns.add(columnNameColumn);
        columns.add(keySeqColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet importedKeysResultSet(
            String[] fkNames,
            String[] pkTableCats, String[] pkTableSchemas, String[] pkTableNames, String[] pkColumnNames,
            String[] fkTableCats, String[] fkTableSchemas, String[] fkTableNames, String[] fkColumnNames) {
        
        List pkTableCatColumn = writeableSingletonList("PKTABLE_CAT");
        pkTableCatColumn.addAll(Arrays.asList(pkTableCats));
        
        List pkTableSchemaColumn = writeableSingletonList("PKTABLE_SCHEM");
        pkTableSchemaColumn.addAll(Arrays.asList(pkTableSchemas));
        
        List pkTableNameColumn = writeableSingletonList("PKTABLE_NAME");
        pkTableNameColumn.addAll(Arrays.asList(pkTableNames));
        
        List pkColumnNameColumn = writeableSingletonList("PKCOLUMN_NAME");
        pkColumnNameColumn.addAll(Arrays.asList(pkColumnNames));
        
        List fkTableCatColumn = writeableSingletonList("FKTABLE_CAT");
        fkTableCatColumn.addAll(Arrays.asList(fkTableCats));
        
        List fkTableSchemaColumn = writeableSingletonList("FKTABLE_SCHEM");
        fkTableSchemaColumn.addAll(Arrays.asList(fkTableSchemas));
        
        List fkTableNameColumn = writeableSingletonList("FKTABLE_NAME");
        fkTableNameColumn.addAll(Arrays.asList(fkTableNames));
        
        List fkColumnNameColumn = writeableSingletonList("FKCOLUMN_NAME");
        fkColumnNameColumn.addAll(Arrays.asList(fkColumnNames));
        
        List fkNameColumn = writeableSingletonList("FK_NAME");
        fkNameColumn.addAll(Arrays.asList(fkNames));
        
        List columns = new ArrayList();
        columns.add(pkTableCatColumn);
        columns.add(pkTableSchemaColumn);
        columns.add(pkTableNameColumn);
        columns.add(pkColumnNameColumn);
        columns.add(fkTableCatColumn);
        columns.add(fkTableSchemaColumn);
        columns.add(fkTableNameColumn);
        columns.add(fkColumnNameColumn);
        columns.add(fkNameColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet emptyResultSet() {
        return createResultSet(Collections.EMPTY_LIST);
    }
    
    private static List writeableSingletonList(Object value) {
        List result = new ArrayList();
        result.add(value);
        return result;
    }
    
    private static void addAllAsReferenceType(List list, int[] values) {
        for (int i = 0; i < values.length; i++) {
            list.add(new Integer(values[i]));
        }
    }
    
    private static void addAllAsReferenceType(List list, short[] values) {
        for (int i = 0; i < values.length; i++) {
            list.add(new Short(values[i]));
        }
    }
    
    private static void addAllAsReferenceType(List list, boolean[] values) {
        for (int i = 0; i < values.length; i++) {
            list.add(Boolean.valueOf(values[i]));
        }
    }
}
