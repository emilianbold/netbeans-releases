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
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.CmpFieldHelper;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityHelper;
import org.openide.filesystems.FileObject;

import java.util.HashMap;

/**
 * @author pfiala
 */
class CmpFieldsTableModel extends InnerTableModel {
    private final FileObject ejbJarFile;
    private Entity entity;
    private HashMap cmpFieldHelperMap = new HashMap();
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_FieldName"),
                                                  Utils.getBundleMessage("LBL_Type"),
                                                  Utils.getBundleMessage("LBL_LocalGetter"),
                                                  Utils.getBundleMessage("LBL_LocalSetter"),
                                                  Utils.getBundleMessage("LBL_RemoteGetter"),
                                                  Utils.getBundleMessage("LBL_RemoteSetter"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{120, 160, 70, 70, 70, 70, 220};

    public CmpFieldsTableModel(FileObject ejbJarFile, Entity entity) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejbJarFile = ejbJarFile;
        this.entity = entity;
    }

    public int addRow() {
        new EntityHelper(ejbJarFile, entity).addCmpField();
        fireTableDataChanged();
        return getRowCount() - 1;
// TODO: remove following?
//        ClassElement beanClass = Utils.getBeanClass(ejbJarFile, entity);
//        if (new AddCmpFieldAction().addCmpField(beanClass, ejbJarFile)) {
//            int n = entity.getCmpField().length - 1;
//            fireTableRowsInserted(n, n);
//            return n;
//        } else {
//            return -1;
//        }
    }

    public void removeRow(int row) {
        CmpField cmpField = entity.getCmpField()[row];
        if (getCmpFieldHelper(cmpField).deleteCmpField()) {
            fireTableRowsDeleted(row, row);
        }
    }

    public int getRowCount() {
        return entity.getCmpField().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CmpField field = entity.getCmpField(rowIndex);
        CmpFieldHelper helper = getCmpFieldHelper(field);
        switch (columnIndex) {
            case 0:
                return field.getFieldName();
            case 1:
                return helper.getType();
            case 2:
                return new Boolean(helper.hasLocalGetter());
            case 3:
                return new Boolean(helper.hasLocalSetter());
            case 4:
                return new Boolean(helper.hasRemoteGetter());
            case 5:
                return new Boolean(helper.hasRemoteSetter());
            case 6:
                return field.getDefaultDescription();
        }
        return null;
    }

    private CmpFieldHelper getCmpFieldHelper(CmpField field) {
        CmpFieldHelper cmpFieldHelper = (CmpFieldHelper) cmpFieldHelperMap.get(field);
        if (cmpFieldHelper == null) {
            cmpFieldHelper = new CmpFieldHelper(ejbJarFile, entity, field);
            cmpFieldHelperMap.put(field, cmpFieldHelper);
        }
        return cmpFieldHelper;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
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
