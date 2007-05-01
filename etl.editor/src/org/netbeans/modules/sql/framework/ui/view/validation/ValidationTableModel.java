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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view.validation;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.sql.framework.model.ValidationInfo;


/**
 * @author Ritesh Adval
 */
public class ValidationTableModel extends AbstractTableModel {
    private List dataList;

    private static final String[] columnNames = { "", "Description"};

    public ValidationTableModel(List vInfos) {
        this.dataList = vInfos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return dataList.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        ValidationInfo vInfo = (ValidationInfo) dataList.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return new Integer(vInfo.getValidationType());
            case 1:
                return vInfo.getDescription();

        }

        return "";
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Integer.class;
        }

        return String.class;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public ValidationInfo getValidationInfo(int row) {
        return (ValidationInfo) this.dataList.get(row);
    }
}

