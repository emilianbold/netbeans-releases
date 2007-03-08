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
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Just in time table meta data.  Cached after retreived.
 *
 * @author John Kline
 */
public class TableMetaData {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());

    public static final int TABLE_CAT = 0;
    public static final int TABLE_SCHEM = 1;
    public static final int TABLE_NAME = 2;
    public static final int TABLE_TYPE = 3;
    public static final int REMARKS = 4;
    public static final int TYPE_CAT = 5;
    public static final int TYPE_SCHEM = 6;
    public static final int TYPE_NAME = 7;
    public static final int SELF_REFERENCING_COL_NAME = 8;
    public static final int REF_GENERATION = 9;
    private static final String[] metaNames = {
        "TABLE_CAT", // NOI18N
        "TABLE_SCHEM", // NOI18N
        "TABLE_NAME", // NOI18N
        "TABLE_TYPE", // NOI18N
        "REMARKS", // NOI18N
        "TYPE_CAT", // NOI18N
        "TYPE_SCHEM", // NOI18N
        "TYPE_NAME", // NOI18N
        "SELF_REFERENCING_COL_NAME", // NOI18N
        "REF_GENERATION" // NOI18N
    };
    private String[] metaValues;
    private ColumnMetaData[] columnMetaData;
    private DatabaseMetaData dbmd;

    TableMetaData(ResultSet resultSet, DatabaseMetaData dbmd) throws SQLException {
        this.dbmd = dbmd;
        int exceptionCount = 0;
        SQLException firstException = null;
        metaValues = new String[metaNames.length];
        for (int i = 0; i < metaNames.length; i++) {
            try {
                metaValues[i] = resultSet.getString(metaNames[i]);
            } catch (SQLException e) {
                metaValues[i] = null;
                exceptionCount++;
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        if (exceptionCount == metaNames.length) {
            throw firstException;
        }
        // Delay getting column metadata until it is needed
        columnMetaData = null;
    }

    public String getMetaInfo(String name) throws SQLException {
        for (int i = 0; i < metaNames.length; i++) {
            if (name.equals(metaNames[i])) {
                return metaValues[i];
            }
        }
        throw new SQLException(rb.getString("NAME_NOT_FOUND") + ": " + name); // NOI18N
    }

    public String getMetaInfo(int index) throws SQLException {
        if (index < 0 || index > metaValues.length) {
            throw new SQLException(rb.getString("NO_SUCH_INDEX") + ": " + index); // NOI18N
        }
        return metaValues[index];
    }

    public ColumnMetaData[] getColumnMetaData() throws SQLException {
        if (columnMetaData == null) {
            ArrayList list = new ArrayList();
            ResultSet colRs = dbmd.getColumns(null, metaValues[TABLE_SCHEM], metaValues[TABLE_NAME], "%"); // NOI18N
            while (colRs.next()) {
                list.add(new ColumnMetaData(colRs));
            }
            colRs.close();
            columnMetaData = (ColumnMetaData[])list.toArray(new ColumnMetaData[0]);
        }
        return columnMetaData;
    }

    public ColumnMetaData getColumnMetaData(String columnName) throws SQLException {
        for (int i = 0; i < getColumnMetaData().length; i++) {
            if (getColumnMetaData()[i].getMetaInfo(ColumnMetaData.COLUMN_NAME).equals(columnName)) {
                return getColumnMetaData()[i];
            }
        }
        throw new SQLException(rb.getString("COLUMN_NOT_FOUND") + ": " + columnName); // NOI18N
    }

    public String[] getColumns() throws SQLException {
        ArrayList list = new ArrayList();
        for (int i = 0; i < getColumnMetaData().length; i++) {
            list.add(getColumnMetaData()[i].getMetaInfoAsString(ColumnMetaData.COLUMN_NAME));
        }
        return (String[])list.toArray(new String[0]);
    }
}
