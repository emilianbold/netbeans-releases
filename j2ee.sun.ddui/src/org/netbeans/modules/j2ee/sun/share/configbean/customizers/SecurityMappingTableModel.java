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
/*
 * GroupTableModel.java
 *
 * Created on April 14, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;


/**
 *
 * @author Peter Williams
 */
public abstract class SecurityMappingTableModel extends AbstractTableModel {
    
    /** Number of columns in table.
     */
    private final int numColumns;
    
    /** List of some type of Object.
     */
    private final List itemList;
    
    protected SecurityMappingTableModel(List p, int columns) {
        assert p != null;
        
        itemList = p;
        numColumns = columns;
    }

    /** Model manipulation
     */
    protected int addElement(Object entry) {
        itemList.add(entry);
        int index = itemList.size();
        fireTableRowsInserted(index, index);
        return index;
    }
    
    protected int replaceElement(Object oldEntry, Object newEntry) {
        int index = itemList.indexOf(oldEntry);
        if(index != -1) {
            itemList.set(index, newEntry);
            fireTableRowsUpdated(index, index);
        }
        return index;
    }
    
    protected int removeElement(Object entry) {
        int index = itemList.indexOf(entry);
        if(index != -1) {
            itemList.remove(index);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
        }
        return index;
    }
    
	public void removeElementAt(int index) {
		if(index >= 0 || index < itemList.size())  {
			itemList.remove(index);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
		}
	}
	
	public void removeElements(int[] indices) {
        // !PW FIXME this method has an unwritten requirement that the
        // list of indices passed in is ordered in ascending numerical order.
		if(indices.length > 0) {
			for(int i = indices.length-1; i >= 0; i--) {
				if(indices[i] >= 0 || indices[i] < itemList.size())  {
					itemList.remove(indices[i]);
				}
			}
//            fireTableRowsUpdated(indices[0], indices[indices.length-1]);
            fireTableDataChanged();
		}
	}
    
    protected boolean contains(Object entry) {
        return itemList.contains(entry);
    }
    
    protected Object getRowElement(int rowIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < itemList.size()) {
            result = itemList.get(rowIndex);
        }
        return result;
    }
    
    /** TableModel interface methods
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < itemList.size() && columnIndex >= 0 && columnIndex < numColumns) {
            result = getColumnValueFromRow(itemList.get(rowIndex), columnIndex);
        } 
        return result;
    }
    
    public int getRowCount() {
        return itemList.size();
    }

    public int getColumnCount() {
        return numColumns;
    }

    public abstract String getColumnName(int column);

    /** SecurityMappingTableModel methods
     */
    /** This method is passed the entry for a given row and returns the value
     *  of the specified colum element within that row.  Used by superclass to
     *  implement getValueAt().  columnIndex has already been range checked, but
     *  rowEntry could be null if this table allows null values.
     */
    protected abstract Object getColumnValueFromRow(Object rowEntry, int columnIndex);

}
