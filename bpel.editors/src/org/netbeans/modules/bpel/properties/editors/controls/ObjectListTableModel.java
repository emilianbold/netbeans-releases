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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * The base class for table models based on the list of single-type objects.
 * Type has to be specified with Generics approach.
 *
 * @author nk160297
 */
public abstract class ObjectListTableModel<T> extends AbstractTableModel {
    
    static final long serialVersionUID = 1L;
    
    private TableColumnModel columnModel;
    private ArrayList<T> myList;
    
    public ObjectListTableModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
        this.myList = new ArrayList<T>();
    }
    
    public abstract void reload();
    
    public int getRowCount() {
        return myList.size();
    }
    
    public int getColumnCount() {
        return columnModel.getColumnCount();
    }
    
    public T getRowObject(int rowIndex) {
        return myList.get(rowIndex);
    }
    
    public Class<? extends Object> getColumnClass(int columnIndex) {
        return String.class;
    }
    
    public void addRow(T row) {
        insertRow(myList.size(), row);
    }
    
    public void insertRow(int index, T row) {
        if (index < 0 || index > myList.size()) {
            return; // index out of scope
        }
        //
        myList.add(index, row);
        fireTableRowsInserted(index, index);
    }
    
    public void deleteRow(T row) {
        int index = myList.indexOf(row);
        if (index != -1) {
            myList.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    public void deleteRow(int index) {
        if (index < 0 || index >= myList.size()) {
            return; // index out of scope
        }
        //
        myList.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public void updateRow(int index) {
        if (index < 0 || index >= myList.size()) {
            return; // index out of scope
        }
        fireTableRowsUpdated(index, index);
    }
    
    public Iterator<T> getRowIterator() {
        return myList.iterator();
    }
    
    public int indexOf(T cp) {
        return myList.indexOf(cp);
    }
    
    public List<T> getRowsList() {
        return myList;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        TableColumn column = columnModel.getColumn(columnIndex);
        if (column != null) {
            TableCellEditor editor = column.getCellEditor();
            if (editor != null) {
                return true;
            }
        }
        return false;
    }
}

