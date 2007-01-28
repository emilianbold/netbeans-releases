/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.faces.data;

import javax.sql.RowSet;

public class RowSetColumnBinding extends ColumnBinding {

    public RowSetColumnBinding() {}

    public RowSetColumnBinding(RowSet rowSet, String tableName, String columnName) {
        super(tableName, columnName);
        this.rowSet = rowSet;
    }

    protected RowSet rowSet;
    public void setRowSet(RowSet rowSet) {
        this.rowSet = rowSet;
    }

    public RowSet getRowSet() {
        return rowSet;
    }
}
