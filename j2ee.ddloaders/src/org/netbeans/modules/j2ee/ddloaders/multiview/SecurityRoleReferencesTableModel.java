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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 * @author pfiala
 */
public class SecurityRoleReferencesTableModel extends InnerTableModel {

    private EntityAndSession ejb;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_ReferenceName"),
                                                  Utils.getBundleMessage("LBL_LinkedRole"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 150, 100};

    public SecurityRoleReferencesTableModel(XmlMultiViewDataSynchronizer synchronizer, EntityAndSession ejb) {
        super(synchronizer, COLUMN_NAMES, COLUMN_WIDTHS);
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
        modelUpdatedFromUI();
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
        modelUpdatedFromUI();
        return getRowCount() - 1;
    }

    public void removeRow(int row) {
        ejb.removeSecurityRoleRef(ejb.getSecurityRoleRef(row));
        modelUpdatedFromUI();
    }
}
