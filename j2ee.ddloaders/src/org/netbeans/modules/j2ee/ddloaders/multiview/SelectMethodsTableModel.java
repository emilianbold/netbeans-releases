/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;

/**
 * @author pfiala
 */
class SelectMethodsTableModel extends InnerTableModel {
    Entity entity;
    private static final String[] COLUMN_NAMES = {"Method", "Returns Type", "Result Interface", "Query",
                                                  "Description"};
    private static final int[] COLUMN_WIDTHS = new int[]{200, 100, 120, 200, 100};

    public SelectMethodsTableModel(Entity entity) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.entity = entity;
    }

    public int addRow() {
        return -1;
    }

    public void removeRow(int selectedRow) {
        // TODO: implement Select Method removal
    }

    public int getRowCount() {
        return entity.getQuery().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Query query = entity.getQuery(rowIndex);
        switch (columnIndex) {
            case 0:
                return query.getQueryMethod().getMethodName();
            case 1:
                return "-";
            case 2:
                return query.getResultTypeMapping();
            case 3:
                return query.getEjbQl();
            case 4:
                return query.getDefaultDescription();
        }
        return null;
    }
}
