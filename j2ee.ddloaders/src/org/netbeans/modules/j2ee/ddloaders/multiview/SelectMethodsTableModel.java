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

import org.netbeans.modules.j2ee.dd.api.ejb.Query;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public class SelectMethodsTableModel extends QueryMethodsTableModel {
    
    protected static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Method"),
    Utils.getBundleMessage("LBL_ReturnType"),
    Utils.getBundleMessage("LBL_Query"),
    Utils.getBundleMessage("LBL_Description")};
    protected static final int[] COLUMN_WIDTHS = new int[]{200, 100, 200, 100};
//    private JComboBox returnMethodComboBox = new JComboBox(FieldCustomizer.COMMON_TYPES);
//    private TableCellEditor returnMethodEditor = new DefaultCellEditor(returnMethodComboBox);
    
    public SelectMethodsTableModel(EntityHelper.Queries queries) {
        super(COLUMN_NAMES, COLUMN_WIDTHS, queries);
    }
    
    public int addRow() {
//        queries.addSelectMethod();
        return getRowCount() - 1;
    }
    
    
    public boolean editRow(int row) {
        QueryMethodHelper helper = getQueryMethodHelper(row);
//        QueryCustomizer customizer = new QueryCustomizer();
//        Method method = helper.getPrototypeMethod();
//        if (method == null || method.getTypeName() == null){
//            return false;
//        }
//        method.setType(JMIUtils.resolveType(method.getTypeName().getName()));
//        Query aQuery = (Query) helper.query.clone();
//        boolean result = customizer.showSelectCustomizer(method, aQuery);
//        if (result) {
//            helper.updateSelectMethod(method, aQuery);
//        }
        return true;
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
    
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Query query = (Query) queries.getSelecMethod(rowIndex).clone();
        if (columnIndex == 3) {
            query.setDescription((String) value);
        }
        QueryMethodHelper helper = getQueryMethodHelper(rowIndex);
//        Method method = helper.getPrototypeMethod();
//        helper.updateSelectMethod(method, query);
    }
    
    public TableCellEditor getCellEditor(int columnIndex) {
//        return columnIndex == 1 ? returnMethodEditor : super.getCellEditor(columnIndex);
        return null;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3) {
            return true;
        } else {
            return super.isCellEditable(rowIndex, columnIndex);
        }
    }
}
