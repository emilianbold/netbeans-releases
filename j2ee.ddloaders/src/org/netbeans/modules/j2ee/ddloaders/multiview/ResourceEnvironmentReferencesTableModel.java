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

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;

/**
 * @author pfiala
 */
class ResourceEnvironmentReferencesTableModel extends InnerTableModel {

    private Ejb ejb;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Name"),
                                                  Utils.getBundleMessage("LBL_ResourceType"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{80, 150, 100};

    public ResourceEnvironmentReferencesTableModel(Ejb ejb) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ResourceEnvRef resourceEnvRef = ejb.getResourceEnvRef(rowIndex);
        switch (columnIndex) {
            case 0:
                resourceEnvRef.setResourceEnvRefName((String) value);
                break;
            case 1:
                resourceEnvRef.setResourceEnvRefType((String) value);
                break;
            case 2:
                resourceEnvRef.setDescription((String) value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getResourceEnvRef().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ResourceEnvRef resourceEnvRef = ejb.getResourceEnvRef(rowIndex);
        switch (columnIndex) {
            case 0:
                return resourceEnvRef.getResourceEnvRefName();
            case 1:
                return resourceEnvRef.getResourceEnvRefType();
            case 2:
                return resourceEnvRef.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        ResourceEnvRef resourceEnvRef = ejb.newResourceEnvRef();
        ejb.addResourceEnvRef(resourceEnvRef);
        int row = getRowCount() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    public void removeRow(int row) {
        fireTableStructureChanged(); // cancel editing
        ejb.removeResourceEnvRef(ejb.getResourceEnvRef(row));
        fireTableRowsDeleted(row, row);
    }
}
