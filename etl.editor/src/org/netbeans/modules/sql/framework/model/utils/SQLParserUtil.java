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
package org.netbeans.modules.sql.framework.model.utils;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class SQLParserUtil {

    Pattern p1;
    Pattern p2;
    Pattern p3;
    Pattern p4;
    private SQLDefinition def;

    public SQLParserUtil() {
        p1 = Pattern.compile(".*\\..*");
        p2 = Pattern.compile(".*\\..*\\..*");
        p3 = Pattern.compile("\\.");
        p4 = Pattern.compile(".*\\..*\\..*\\..*");
    }

    /** Creates a new instance of SQLParserUtil */
    public SQLParserUtil(SQLDefinition sqlDef) {
        this();
        this.def = sqlDef;
    }

    /**
     * this method is called when column name is specified as TABLE_NAME.COLUMN_NAME, this
     * method first column it finds from source or target tables. For accurate result user
     * should type fully qualified column name.
     */
    public SQLObject getColumn(String fullName) throws BaseException {
        SQLDBColumn column = null;
        ColumnRef columnRef = null;
        String tableName = getTableName(fullName);
        String columnName = getColumnName(fullName);

        if (tableName != null && columnName != null) {
            SQLDBTable table = getTable(def.getSourceTables(), tableName);
            if (table != null) {
                column = getColumn(table.getColumnList(), columnName);
            }

            // if column is still null check in target tables
            if (column == null) {
                table = getTable(def.getTargetTables(), tableName);
                if (table != null) {
                    column = getColumn(table.getColumnList(), columnName);
                }
            }
        }

        // if column is not null create a ColumnRefImpl and return
        if (column != null) {
            columnRef = SQLModelObjectFactory.getInstance().createColumnRef(column);
        } else {
            throw new BaseException("Column \"" + fullName + "\"is not available in collaboration \"" + def.getDisplayName() + "\"");
        }

        return columnRef;
    }

    /**
     * This method will return a column when user types a fully qualified column name. for
     * oracle fully qualified name is SCHEMA_NAME.TABLE.NAME.COLUMN.NAME for sql server it
     * is CATALOG_NAME.SCHEMA_NAME.TABLE.NAME.COLUMN.NAME
     */
    public SQLObject getColumnForFullyQualifiedName(String fullName) throws BaseException {
        SQLDBColumn column = null;
        ColumnRef columnRef = null;

        String tableName = getFullyQualifiedTableName(fullName);
        String columnName = getColumnName(fullName);

        if (tableName != null && columnName != null) {
            SQLDBTable table = getTableForFullyQualifiedName(def.getSourceTables(), tableName);
            if (table != null) {
                column = getColumn(table.getColumnList(), columnName);
            }

            // if column is still null check in target tables
            if (column == null) {
                table = getTableForFullyQualifiedName(def.getTargetTables(), tableName);
                if (table != null) {
                    column = getColumn(table.getColumnList(), columnName);
                }
            }
        }

        // if column is not null create a ColumnRefImpl and return
        if (column != null) {
            columnRef = SQLModelObjectFactory.getInstance().createColumnRef(column);
        } else {
            throw new BaseException("Column \"" + fullName + "\"is not available in collaboration \"" + def.getDisplayName() + "\"");
        }

        return columnRef;

    }

    public SQLObject getRuntimeInput(String argName) throws BaseException {
        SQLDBColumn column = null;
        ColumnRef columnRef = null;
        String argumentName = argName;

        RuntimeDatabaseModel runDb = def.getRuntimeDbModel();
        if (runDb != null) {
            RuntimeInput rInput = runDb.getRuntimeInput();

            if (rInput != null) {
                int idx = argName.indexOf('$');
                if (idx != -1 && idx + 1 < argName.length()) {
                    argumentName = argName.substring(idx + 1);
                    column = (SQLDBColumn) rInput.getColumn(argumentName);
                }
            }
        }

        if (column != null) {
            columnRef = SQLModelObjectFactory.getInstance().createColumnRef(column);
        } else {
            throw new BaseException("Runtime input argument \"" + argumentName + "\"is not defined in collaboration \"" + def.getDisplayName() + "\"");
        }

        return columnRef;
    }

    private SQLDBColumn getColumn(List columns, String name) {
        Iterator it = columns.iterator();
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            if (column.getName().equalsIgnoreCase(name)) {
                return column;
            }
        }
        return null;
    }

    private String getColumnName(String fullName) {
        // last part will be column name
        int dotIdx = fullName.lastIndexOf('.');
        if (dotIdx != -1) {
            return fullName.substring(dotIdx + 1, fullName.length());
        }

        return null;
    }

    private String getFullyQualifiedTableName(String fullName) {
        // last part will be column name
        // and last -1 will be table name
        int dotIdx = fullName.lastIndexOf('.');
        if (dotIdx != -1) {
            return fullName.substring(0, dotIdx);
        }

        return null;
    }

    private SQLDBTable getTable(List tables, String name) {
        Iterator it = tables.iterator();

        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            if (table.getName().equalsIgnoreCase(name) || (table.getAliasName() != null && table.getAliasName().equalsIgnoreCase(name))) {
                return table;
            }
        }

        return null;
    }

    private SQLDBTable getTableForFullyQualifiedName(List tables, String fullName) {
        Iterator it = tables.iterator();

        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            DatabaseModel dbModel = table.getParent();
            if (dbModel != null) {
                String tableFName = table.getFullyQualifiedName();
                if (tableFName != null && tableFName.equalsIgnoreCase(fullName)) {
                    return table;
                }
            }
        }

        return null;
    }

    private String getTableName(String fullName) {
        // last part will be column name
        // and last -1 will be table name
        int dotIdx = fullName.lastIndexOf('.');
        if (dotIdx != -1) {
            return fullName.substring(0, dotIdx);
        }

        return null;
    }

}

