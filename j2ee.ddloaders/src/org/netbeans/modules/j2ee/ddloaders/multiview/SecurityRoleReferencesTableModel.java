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

import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;

/**
 * @author pfiala
 */
class SecurityRoleReferencesTableModel extends InnerTableModel {
    private EntityAndSession ejb;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_ReferenceName"),
                                                  Utils.getBundleMessage("LBL_LinkedRole"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 150, 100};

    public SecurityRoleReferencesTableModel(EntityAndSession ejb) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        SecurityRoleRef securityRoleRef = ejb.getSecurityRoleRef(rowIndex);
        switch (columnIndex) {
            case 0:
                securityRoleRef.setRoleName((String) value);
                break;
            case 1:
                securityRoleRef.setRoleLink((String) value);
                break;
            case 2:
                securityRoleRef.setDescription((String) value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getSecurityRoleRef().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        SecurityRoleRef securityRoleRef = ejb.getSecurityRoleRef(rowIndex);
        switch (columnIndex) {
            case 0:
                return securityRoleRef.getRoleName();
            case 1:
                return securityRoleRef.getRoleLink();
            case 2:
                return securityRoleRef.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        SecurityRoleRef securityRoleRef = ejb.newSecurityRoleRef();
        ejb.addSecurityRoleRef(securityRoleRef);
        int row = getRowCount() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    public void removeRow(int row) {
        fireTableStructureChanged(); // cancel editing
        ejb.removeSecurityRoleRef(ejb.getSecurityRoleRef(row));
        fireTableRowsDeleted(row, row);
    }
}
