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

package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.awt.*;
import java.util.Vector;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.DialogDisplayer;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;

/**
 * A custom editor for basic TabularData.
 *
 * @author jqian
 */
public class SimpleTabularDataCustomEditor extends JPanel
        implements EnhancedCustomPropertyEditor, ActionListener {
    
    private JTable table;
    
    /** Number of index columns in the tabular data type. */
    private int indexColumnCount;
    
    // all the keys are at the beginning of the array
    private String[] columnNames;
    
    private String[] columnDescriptions;
    private OpenType[] columnTypes;
    
    private TabularType tabularType;
    private TabularData tabularData;
    
    private JButton addRowButton, deleteRowButton;
    
    private static String ADD_ROW = NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "AddRowLabel");
    private static String DELETE_ROW = NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "DeleteRowLabel");
    
    
    public SimpleTabularDataCustomEditor(SimpleTabularDataEditor editor) {
        
        tabularData = (TabularData) editor.getValue();
        
        initComponents(tabularData);
        
//        HelpCtx.setHelpIDString(this, SimpleTabularDataCustomEditor.class.getName());
//        getAccessibleContext().setAccessibleDescription(
//            NbBundle.getBundle(SimpleTabularDataCustomEditor.class).getString("ACSD_SimpleTabularDataCustomEditor"));
    }
    
    public Object getPropertyValue() throws IllegalStateException {
        
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        
        TabularData ret = null;
        
        try {
            ret = new TabularDataSupport(tabularType);
            CompositeType rowType = tabularType.getRowType();
            
            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
            Vector dataVectors = tableModel.getDataVector();
            for (Object rowDataObj : dataVectors) {
                Object[] itemValues = ((Vector) rowDataObj).toArray();
                
                // Ignore rows with null keys
                boolean nullKeyRow = false;
                System.out.println("indexCoumnCount is " + indexColumnCount + " first value is " + itemValues[0]);
                for (int i = 0; i < indexColumnCount; i++) {
                    if (itemValues[i] == null) {
                        nullKeyRow = true;
                        break;
                    }
                }                
                if (!nullKeyRow) {
                    CompositeData rowData =
                            new CompositeDataSupport(rowType, columnNames, itemValues);
                    ret.put(rowData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            
            ret = tabularData;  // if anything wrong, just return the old value
        }
        
        return ret;
    }
    
    /** Returns preferredSize as the preferred height and the width of the panel */
    public Dimension getPreferredSize() {
        return new Dimension(400, 250);
    }
    
    private void initComponents(TabularData tabularData) {
        DefaultTableModel tableModel = initTableModel(tabularData);
        
        table = new JTable(tableModel) {
            // TMP
            public TableCellRenderer getCellRenderer(int row, int column) {
                TableCellRenderer renderer = new DefaultTableCellRenderer() {
                    public Component getTableCellRendererComponent(JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row, int column) {
                        // Highlight key columns
                        if (column < indexColumnCount && value instanceof String) {
                            value = "<html><body><b>" + value + "</b></body></html>";
                        }
                        return super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);
                    }
                };
                return renderer;
            }
            
            public Class getColumnClass(int column) {
                OpenType columnType = columnTypes[column];
                String className = columnType.getClassName();
                Class clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    clazz = String.class;
                }
                
                return clazz;
            }
        };
        
        table.getTableHeader().setReorderingAllowed(false);
        
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setHeaderRenderer(new MyTableHeaderRenderer());
        }
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int minSelectionIndex = table.getSelectionModel().getMinSelectionIndex();
                    deleteRowButton.setEnabled(minSelectionIndex != -1);
                }
            }
        });
        
        JScrollPane scrollPane= new JScrollPane(table);
        JPanel buttonPanel = new JPanel();
        addRowButton = new JButton(ADD_ROW);
        addRowButton.setActionCommand(ADD_ROW);
        deleteRowButton = new JButton(DELETE_ROW);
        deleteRowButton.setActionCommand(DELETE_ROW);
        
        buttonPanel.add(addRowButton);
        buttonPanel.add(deleteRowButton);
        
        addRowButton.addActionListener(this);
        deleteRowButton.addActionListener(this);
        deleteRowButton.setEnabled(false);
        
        setLayout(new BorderLayout());
        add("Center", scrollPane);
        add("South", buttonPanel);
        setBackground(Color.white);
        buttonPanel.setBackground(Color.white);
        table.getParent().setBackground(Color.white);
    }
    
    private DefaultTableModel initTableModel(TabularData tabularData) {
        
        tabularType = tabularData.getTabularType();
        CompositeType rowType = tabularType.getRowType();
        
        // Construct reordered column names to make sure all the keys are
        // at the beginning of the list.
        
        List<String> indexNames = tabularType.getIndexNames();
        indexColumnCount = indexNames.size();
        
        List<String> columnNameList = new ArrayList<String>();
        columnNameList.addAll(indexNames);
        
        for (Object columnName : rowType.keySet()) {
            if (!indexNames.contains(columnName)) {
                columnNameList.add((String)columnName);
            }
        }
        columnNames = (String[]) columnNameList.toArray(new String[]{});
        
        columnDescriptions = new String[columnNames.length];
        columnTypes = new OpenType[columnNames.length];
        
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            columnDescriptions[i] = rowType.getDescription(columnName);
            columnTypes[i] = rowType.getType(columnName);
        }
        
        Vector dataVector = new Vector();
        for (Object rowDataObj : tabularData.values()) {
            CompositeData rowData = (CompositeData) rowDataObj;
            Vector row = new Vector();
            for (Object columnName : columnNames) {
                row.add(rowData.get((String)columnName));
            }
            dataVector.add(row);
        }
        
        Vector columnIdentifiers = new Vector();
        for (int i = 0; i < columnNames.length; i++) {
            columnIdentifiers.addElement(columnNames[i]);
        }
        
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setDataVector(dataVector, columnIdentifiers);
        
        return tableModel;
    }
    
    private void addNewRow() {
        Vector row = new Vector();
        for (int i = 0; i < columnNames.length; i++) {
            row.addElement(null);
        }
        
        Vector dataVector = getDataVector();
        dataVector.addElement(row);
        table.addNotify();
        
        int newRowIndex = dataVector.size() - 1;
        table.getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
    }
    
    private void deleteSelectedRows() {
        Vector dataVector = getDataVector();
        int[] rowIndices = table.getSelectedRows();  // guaranteed to be non-null
        
        for (int i = rowIndices.length - 1; i >= 0; i--) {
            dataVector.removeElementAt(rowIndices[i]);
        }
        table.addNotify();
        
        table.getSelectionModel().clearSelection();
//        if (rowIndices.length > 0) {
//            int newSelectedRowIndex = rowIndices[0] - 1;
//            table.getSelectionModel().setSelectionInterval(newSelectedRowIndex, newSelectedRowIndex);
//        }
    }
    
    private Vector getDataVector() {
        return ((DefaultTableModel)table.getModel()).getDataVector();
    }
    
    public void actionPerformed(ActionEvent event) {
        JButton source = (JButton) event.getSource();
        String actionCommand = source.getActionCommand();
        
        if (actionCommand.equals(ADD_ROW)) {
            addNewRow();
        } else if (actionCommand.equals(DELETE_ROW)) {
            deleteSelectedRows();
        }
    }
    
    public class MyTableHeaderRenderer extends DefaultTableCellRenderer {
        // This method is called each time a column header
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }
            String myValue = (value == null) ? "" : ("<html><body><b>" + value.toString() + "</b></body></html>");
            setText(myValue);
            setToolTipText(columnDescriptions[colIndex]);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }
}
