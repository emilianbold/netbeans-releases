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

import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.QueryCustomizer;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.FieldCustomizer;
import org.openide.src.MethodElement;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
class SelectMethodsTableModel extends QueryMethodsTableModel {

    protected static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Method"),
                                                    Utils.getBundleMessage("LBL_ReturnType"),
                                                    Utils.getBundleMessage("LBL_Query"),
                                                    Utils.getBundleMessage("LBL_Description")};
    protected static final int[] COLUMN_WIDTHS = new int[]{200, 100, 200, 100};
    private JComboBox returnMethodComboBox = new JComboBox(FieldCustomizer.COMMON_TYPES);
    private TableCellEditor returnMethodEditor = new DefaultCellEditor(returnMethodComboBox);

    public SelectMethodsTableModel(EntityHelper.Queries queries) {
        super(COLUMN_NAMES, COLUMN_WIDTHS, queries);
    }

    public int addRow() {
        queries.addSelectMethod();
        return getRowCount() - 1;
    }


    public void editRow(int row) {
        QueryMethodHelper helper = getQueryMethodHelper(row);
        QueryCustomizer customizer = new QueryCustomizer();
        MethodElement methodElement = (MethodElement) helper.getPrototypeMethod().clone();
        Query aQuery = (Query) helper.query.clone();
        boolean result = customizer.showSelectCustomizer(methodElement, aQuery);
        if (result) {
            helper.updateSelectMethod(methodElement, aQuery);
        }
    }

    public QueryMethodHelper getQueryMethodHelper(int row) {
        return queries.getSelectMethodHelper(row);
    }

    public int getRowCount() {
        return queries.getSelectMethodCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        QueryMethodHelper queryMethodHelper = getQueryMethodHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                return queryMethodHelper.getQueryMethod().getMethodName();
            case 1:
                return queryMethodHelper.getReturnType();
            case 2:
                return queryMethodHelper.getEjbQl();
            case 3:
                return queryMethodHelper.getDefaultDescription();
            }
        return null;
    }

    public TableCellEditor getCellEditor(int columnIndex) {
        return columnIndex == 1 ? returnMethodEditor : super.getCellEditor(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return super.isCellEditable(rowIndex, columnIndex);
    }
}
