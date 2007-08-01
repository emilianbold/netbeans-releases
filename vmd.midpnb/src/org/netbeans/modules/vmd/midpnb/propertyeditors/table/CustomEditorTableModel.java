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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Anton Chechel
 */
class CustomEditorTableModel extends DefaultTableModel {

    private Vector<String> header = new Vector<String>();
    private boolean hasHeader;

    public void removeLastColumn() {
        if (header.size() > 0) {
            header.remove(header.size() - 1);
        }

        int columnCount = getColumnCount();
        if (columnCount > 0) {
            columnIdentifiers.remove(columnCount - 1);
            int size = dataVector.size();
            for (int i = 0; i < size; i++) {
                Vector row = (Vector) dataVector.elementAt(i);
                row.remove(columnCount - 1);
            }
        }
        fireTableStructureChanged();
    }

    @Override
    public void removeRow(int row) {
        if (dataVector.size() > 0) {
            if (row > 0 && hasHeader) {
                row--;
            }
            super.removeRow(row);
        }
    }

    @Override
    public void addRow(Object[] rowData) {
        dataVector.addElement(convertToVector(rowData));
        fireTableStructureChanged();
    }

    public void addColumn(String columnName) {
        header.addElement(columnName);
        columnIdentifiers.addElement(columnName);
        for (int i = 0; i < dataVector.size(); i++) {
            Vector row = (Vector) dataVector.elementAt(i);
            row.addElement(columnName);
        }

        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return dataVector.size() + (hasHeader ? 1 : 0);
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object value;
        if (hasHeader) {
            if (row == 0) {
                value = header.elementAt(column);
            } else {
                value = super.getValueAt(row - 1, column);
            }
        } else {
            value = super.getValueAt(row, column);
        }
        return value;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (hasHeader) {
            if (row == 0) {
                header.setElementAt((String) aValue, column);
            } else {
                super.setValueAt(aValue, row - 1, column);
            }
        } else {
            super.setValueAt(aValue, row, column);
        }
    }

    @Override
    public void setDataVector(Object[][] dataArrays, Object[] columnArray) {
        if (hasHeader) {
            header.clear();
            for (int i = 0; i < columnArray.length; i++) {
                header.addElement((String) columnArray[i]);
            }
        }
        super.dataVector = nonNullVector(convertToVector(dataArrays));
        super.columnIdentifiers = nonNullVector(convertToVector(columnArray));
        fireTableStructureChanged();
    }

    private static Vector nonNullVector(Vector v) { 
	return (v != null) ? v : new Vector(); 
    } 

    public void clear() {
        header.clear();
        columnIdentifiers.clear();
        dataVector.clear();
        fireTableStructureChanged();
    }

    public void setUseHeader(boolean useHeader) {
        this.hasHeader = useHeader;
        fireTableStructureChanged();
    }

    public boolean hasHeader() {
        return hasHeader;
    }
}
