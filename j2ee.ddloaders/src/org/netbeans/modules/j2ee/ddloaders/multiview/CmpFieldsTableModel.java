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

import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;

/**
 * @author pfiala
 */
class CmpFieldsTableModel extends InnerTableModel {
    Entity entity;
    private static final String[] COLUMN_NAMES = {"Field Name", "Type", "Local Getter", "Local Setter", "Remote Getter",
                                                  "Remote Setter", "Description"};
    private static final int[] COLUMN_WIDTHS = new int[]{120, 100, 120, 120, 120, 120, 120};

    public CmpFieldsTableModel(Entity entity) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.entity = entity;
    }

    public int addRow() {
        return -1;
    }

    public void removeRow(int selectedRow) {
        // TODO: implement field removal
    }

    public int getRowCount() {
        return entity.getCmpField().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CmpField field = entity.getCmpField(rowIndex);
        switch (columnIndex) {
            case 0:
                return field.getFieldName();
            case 1:
                return "";
            case 2:
                return Boolean.FALSE;
            case 3:
                return Boolean.FALSE;
            case 4:
                return Boolean.FALSE;
            case 5:
                return Boolean.FALSE;
            case 6:
                return field.getDefaultDescription();
        }
        return null;
    }
}
