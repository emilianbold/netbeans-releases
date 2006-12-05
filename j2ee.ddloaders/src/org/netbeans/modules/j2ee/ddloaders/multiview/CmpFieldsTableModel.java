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

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public class CmpFieldsTableModel extends InnerTableModel {

    private EntityHelper.CmpFields cmpFields;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_FieldName"),
                                                  Utils.getBundleMessage("LBL_Type"),
                                                  Utils.getBundleMessage("LBL_LocalGetter"),
                                                  Utils.getBundleMessage("LBL_LocalSetter"),
                                                  Utils.getBundleMessage("LBL_RemoteGetter"),
                                                  Utils.getBundleMessage("LBL_RemoteSetter"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{120, 160, 70, 70, 70, 70, 220};

    public CmpFieldsTableModel(EntityHelper.CmpFields cmpFields) {
        super(null, COLUMN_NAMES, COLUMN_WIDTHS);
        this.cmpFields = cmpFields;
        cmpFields.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt != null && evt.getSource() instanceof Entity &&
                        (evt.getOldValue() instanceof CmpField || evt.getNewValue() instanceof CmpField)) {
                    tableChanged();
                }
            }
        });
    }

    public int addRow() {
        cmpFields.addCmpField();
        int row = getRowCount() - 1;
        //fireTableRowsInserted(row, row);
        return row;
    }

    public void editRow(int row) {
        if (cmpFields.getCmpFieldHelper(row).edit()) {
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeRow(int row) {
        cmpFields.getCmpFieldHelper(row).deleteCmpField();
    }

    public void refreshView() {
        super.refreshView();
    }

    public int getRowCount() {
        return cmpFields.getCmpFieldCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CmpFieldHelper helper = cmpFields.getCmpFieldHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                return helper.getFieldName();
//            case 1:
//                return helper.getTypeString();
//            case 2:
//                return new Boolean(helper.hasLocalGetter());
//            case 3:
//                return new Boolean(helper.hasLocalSetter());
//            case 4:
//                return new Boolean(helper.hasRemoteGetter());
//            case 5:
//                return new Boolean(helper.hasRemoteSetter());
            case 6:
                return helper.getDefaultDescription();
        }
        return null;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        CmpFieldHelper helper = cmpFields.getCmpFieldHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                helper.setFieldName((String) value);
                break;
//            case 1:
//                helper.setType((String) value);
//                break;
            case 2:
                helper.setLocalGetter(((Boolean) value).booleanValue());
                break;
            case 3:
                helper.setLocalSetter(((Boolean) value).booleanValue());
                break;
            case 4:
                helper.setRemoteGetter(((Boolean) value).booleanValue());
                break;
            case 5:
                helper.setRemoteSetter(((Boolean) value).booleanValue());
                break;
            case 6:
                helper.setDescription((String) value);
                break;
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Boolean.class;
            case 3:
                return Boolean.class;
            case 4:
                return Boolean.class;
            case 5:
                return Boolean.class;
            case 6:
                return String.class;
        }
        return super.getColumnClass(columnIndex);
    }
}
