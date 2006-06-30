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
