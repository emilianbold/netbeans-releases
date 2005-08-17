/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute;

/**
 * Describes a column in the TableModel.
 * A ResultSetTableModel is composed of a list of ColumnDefs and the data.
 *
 * @author Andrei Badea
 */
public class ColumnDef {

    /**
     * The column name.
     */
    private String name;

    /**
     * Whether we can write to this column.
     * A column is writable if its ColumnTypeDef says so and the
     * ResultSet is updateable.
     */
    private boolean writable;

    /**
     * The class used to display this column in the table.
     */
    private Class clazz;

    public ColumnDef(String name, boolean writable, Class clazz) {
        this.name = name;
        this.writable = writable;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public boolean isWritable() {
        return writable;
    }

    public Class getDisplayClass() {
        return clazz;
    }
}
