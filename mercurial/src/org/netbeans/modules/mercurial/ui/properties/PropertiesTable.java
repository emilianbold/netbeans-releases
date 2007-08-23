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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.properties;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.versioning.util.TableSorter;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Pis
 */
public class PropertiesTable implements AncestorListener, TableModelListener {
    
    static public final String[] PROPERTIES_COLUMNS = new String[] {PropertiesTableModel.COLUMN_NAME_NAME, PropertiesTableModel.COLUMN_NAME_VALUE};
            
    private PropertiesTableModel tableModel;
    private JTable table;
    private TableSorter sorter;
    private JComponent component;
    private String[] columns;
    private String[] sortByColumns;
    
    /** Creates a new instance of PropertiesTable */
    public PropertiesTable(String[] columns, String[] sortByColumns) {
        init(columns, null);
        this.sortByColumns = sortByColumns;
        setSortingStatus();
    }
    
    public PropertiesTable(String[] columns, TableSorter sorter) {
        init(columns, sorter);
    } 
    
    private void init(String[] columns, TableSorter sorter) {
        tableModel = new PropertiesTableModel(columns);
        tableModel.addTableModelListener(this);
        if(sorter == null) {
            sorter = new TableSorter(tableModel);
        } 
        this.sorter = sorter;   
        table = new JTable(this.sorter);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(String.class, new PropertiesTableCellRenderer());
        //table.setDefaultEditor(CommitOptions.class, new CommitOptionsCellEditor());
        table.getTableHeader().setReorderingAllowed(true);
        this.sorter.setTableHeader(table.getTableHeader());
        table.setRowHeight(table.getRowHeight());
        table.addAncestorListener(this);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        component.setPreferredSize(new Dimension(340, 150));
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PropertiesTable.class, "ACSD_PropertiesTable")); // NOI18N        
        setColumns(columns);
    }
    
    public void setColumns(String[] clmns) {
        if (Arrays.equals(columns, clmns))
            return;
        columns = clmns;
        tableModel.setColumns(clmns);
        setDefaultColumnSize();
    }
    
    public JTable getTable() {
        return table;
    }
    
    private void setDefaultColumnSize() {
        int width = table.getWidth();
        TableColumnModel columnModel = table.getColumnModel();
        if (columns == null || columnModel == null)
            return;
        if (columnModel.getColumnCount() != columns.length)
            return;
        for (int i = 0; i < columns.length; i++) {
            String col = columns[i];                                
            sorter.setColumnComparator(i, null);                    
            if (col.equals(PropertiesTableModel.COLUMN_NAME_NAME)) {
                columnModel.getColumn(i).setPreferredWidth(width * 20 / 100);
            } else if (col.equals(PropertiesTableModel.COLUMN_NAME_VALUE)) {
                columnModel.getColumn(i).setPreferredWidth(width * 40 / 100);
            }
        }
    }
    
    private void setSortingStatus() {
        for (int i = 0; i < sortByColumns.length; i++) {
            String sortByColumn = sortByColumns[i];        
            for (int j = 0; j < columns.length; j++) {
                String column = columns[j];
                if(column.equals(sortByColumn)) {
                    sorter.setSortingStatus(j, column.equals(sortByColumn) ? TableSorter.ASCENDING : TableSorter.NOT_SORTED);                       
                    break;
                }                    
            }                        
        }        
    }
    
    TableModel getTableModel() {
        return tableModel;
    }
    
    void dataChanged() {
        int idx = table.getSelectedRow();
        tableModel.fireTableDataChanged();
        if (idx != -1) {
            table.getSelectionModel().addSelectionInterval(idx, idx);
        }    
    }
    
    public int getModelIndex(int viewIndex) {
        return sorter.modelIndex(viewIndex);
    }
    
    public int[] getSelectedItems() {
        return table.getSelectedRows();
    }
     
    public HgPropertiesNode[] getNodes() {
        return tableModel.getNodes();
    }
    
    public void setNodes(HgPropertiesNode[] nodes) {
        tableModel.setNodes(nodes);
    }
    
    public JComponent getComponent() {
        return component;
    }
    
    public void ancestorAdded(AncestorEvent arg0) {
        setDefaultColumnSize();
    }

    public void ancestorRemoved(AncestorEvent arg0) {
    }

    public void ancestorMoved(AncestorEvent arg0) {
    }

    public void tableChanged(TableModelEvent event) {
        table.repaint();
    }

    public class PropertiesTableCellRenderer extends DefaultTableCellRenderer {
           
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            Component renderer =  super.getTableCellRendererComponent(table, value, hasFocus, hasFocus, rowIndex, columnIndex);
            if (renderer instanceof JComponent) {
                String strValue = tableModel.getNode(sorter.modelIndex(rowIndex)).getValue(); 
                ((JComponent) renderer).setToolTipText(strValue);
            }
            setToolTipText(value.toString());
            return renderer;
        }
    }
    
    
}
