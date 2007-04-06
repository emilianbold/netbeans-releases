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

package org.netbeans.modules.swingapp.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A custom TableModel which can filter the rows based on a search string
 *
 * @author joshua.marinacci@sun.com
 */
public class FilteredTableModel extends AbstractTableModel {
    private TableModel model;
    private String filterString;
    List<List<Object>> rows;
    
    public FilteredTableModel(TableModel model) {
        this.rows = new ArrayList<List<Object>>();
        this.model = model;
        rebuildRows();
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                rebuildRows();
            }
        });
    }
    
    public void setFilterString(String filterString) {
        this.filterString = filterString;
        rebuildRows();
    }
    
    public int getRowCount() {
        return rows.size();
    }
    
    public int getColumnCount() {
        return model.getColumnCount();
    }
    
    public String getColumnName(int columnIndex) {
        return model.getColumnName(columnIndex);
    }
    
    public Class<?> getColumnClass(int columnIndex) {
        return model.getColumnClass(columnIndex);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // do nothing, editing not supported
        return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // do nothing, editing not supported
    }
    
    public TableModel getTableModel() {
        return model;
    }
    
    private void rebuildRows() {
        rows = new ArrayList<List<Object>>();
        for(int r=0; r<model.getRowCount(); r++) {
            List<Object> row = new ArrayList<Object>();
            boolean passesFilter = false;
            for(int c=0; c<model.getColumnCount(); c++) {
                Object o = model.getValueAt(r,c);
                row.add(o);
                if(o instanceof String) {
                    if(filter((String)o)) {
                        passesFilter = true;
                    }
                }
            }
            if(passesFilter) {
                rows.add(row);
            }
        }
        TableModelEvent evt = new TableModelEvent(this);
        fireTableChanged(evt);
    }
    
    private boolean filter(String string) {
        //if the filter is empty then let it pass
        if(filterString == null || "".equals(filterString)) {
            return true;
        }
        
        if(string == null) {
            return false;
        }
        
        if(string.toLowerCase().contains(filterString.toLowerCase())) {
            return true;
        }
        
        return false;
    }
    
}