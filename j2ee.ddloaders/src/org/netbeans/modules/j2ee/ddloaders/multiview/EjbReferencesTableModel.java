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
import org.netbeans.modules.j2ee.dd.api.ejb.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRef;
import org.openide.util.NbBundle;

/**
 * @author pfiala
 */
class EjbReferencesTableModel extends InnerTableModel {
    Ejb ejb;
    private static final String[] COLUMN_NAMES = {
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_ReferenceName"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_LinkedEjb"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Interface"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{170, 260, 70, 250};

    public EjbReferencesTableModel(Ejb ejb) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public int getRowCount() {
        return ejb.getEjbLocalRef().length + ejb.getEjbRef().length;
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int addRow() {
        return -1;
    }

    public void removeRow(int selectedRow) {
        // TODO: implement removal of reference
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        int n = ejb.getEjbLocalRef().length;
        if (rowIndex < n) {
            EjbLocalRef ref = ejb.getEjbLocalRef(rowIndex);
            switch (columnIndex) {
                case 0:
                    return ref.getEjbRefName();
                case 1:
                    return ref.getEjbLink();
                case 2:
                    return "local"; //NOI18N
                case 3:
                    return ref.getDefaultDescription();
            }
        } else {
            EjbRef ref = ejb.getEjbRef(rowIndex - n);
            switch (columnIndex) {
                case 0:
                    return ref.getEjbRefName();
                case 1:
                    return ref.getEjbLink();
                case 2:
                    return "remote"; //NOI18N
                case 3:
                    return ref.getDefaultDescription();
            }
        }
        return null;
    }
}
