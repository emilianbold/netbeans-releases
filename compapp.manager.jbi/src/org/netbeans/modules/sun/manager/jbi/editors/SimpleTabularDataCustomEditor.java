/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.NbBundle;

/**
 * An editor for simple TabularData.
 * 
 * @author  jqian
 */
public class SimpleTabularDataCustomEditor extends javax.swing.JPanel
        implements EnhancedCustomPropertyEditor, ActionListener {

    private TableCellRenderer headerCellRenderer;
    /** Number of index columns in the tabular data type. */
    protected int indexColumnCount;    // all the keys are at the beginning of the array
    protected String[] columnNames;
    private TabularType tabularType;
    private TabularData tabularData;
    private String tableLabelText;
    private String tableLabelDescription;
    protected JBIComponentConfigurationDescriptor descriptor;
    protected boolean isWritable = true;

    private static int MIN_COLUMN_COUNT_FOR_MULTILINE_HEADER = 4;

    public SimpleTabularDataCustomEditor(SimpleTabularDataEditor editor,
            String tableLabelText,
            String tableLabelDescription,
            JBIComponentConfigurationDescriptor descriptor,
            boolean isWritable) {
        this.tableLabelText = tableLabelText;
        this.tableLabelDescription = tableLabelDescription;
        this.tabularData = (TabularData) editor.getValue();
        this.tabularType = editor.getTabluarType();
        this.descriptor = descriptor;
        this.isWritable = isWritable;
        init();
    }

    private void init() {
        initComponents();

        headerCellRenderer = createTableHeaderRenderer();
        
        initColumnNames();
        
        DefaultTableModel tableModel = createTableModel(tabularData);
        table = createTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(table);

        table.setName(tableLabelText);
        table.setToolTipText(tableLabelDescription);
        label.setLabelFor(table);

        configureTableColumns(table);
        initTableColumnWidths();

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int minSelectionIndex = table.getSelectionModel().getMinSelectionIndex();
                    deleteButton.setEnabled(isWritable && minSelectionIndex != -1);
                }
            }
        });

        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        deleteAllButton.addActionListener(this);
        addButton.setEnabled(isWritable);
        deleteButton.setEnabled(false);
        deleteAllButton.setEnabled(isWritable && tableModel.getRowCount() > 0);
    }
    
    protected TabularType getTabularType() {
        return tabularType;
    }

    protected TableCellRenderer createTableHeaderRenderer() {
        return new TabularDataTableHeaderRenderer();
    }

    public TabularData getPropertyValue() throws IllegalStateException {

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        TabularData ret = null;

        try {
            ret = getTabularData();
        } catch (KeyAlreadyExistsException e) {
            List<String> indexColumns =
                    Arrays.asList(columnNames).subList(0, indexColumnCount);
            String msg = NbBundle.getMessage(SimpleTabularDataCustomEditor.class,
                    "MSG_TABULAR_DATA_KEY_ALREADY_EXISTS", indexColumns);
            throw new RuntimeException(msg);
//        } catch (InvalidKeyException e) {
//            throw new RuntimeException(e.getMessage());
        } catch (OpenDataException e) {
            throw new RuntimeException(e.getMessage());
        }

        return ret;
    }

    protected TabularData getTabularData() throws OpenDataException {

        TabularData ret = null;

        ret = new TabularDataSupport(tabularType);
        CompositeType rowType = tabularType.getRowType();

        int rowIndex = 0;
        for (Vector rowVector : getDataVector()) {
            Object[] itemValues = rowVector.toArray();

            // Check isRequired field based on the tabular data descriptor.
            if (descriptor != null) {
                List<String> incompleteRequiredColumns = new ArrayList<String>();
                for (int col = 0; col < itemValues.length; col++) {
                    Object itemValue = itemValues[col];
                    String columnName = columnNames[col];
                    JBIComponentConfigurationDescriptor childDescriptor =
                            descriptor.getChild(columnName);
                    if (childDescriptor != null && childDescriptor.isRequired()) {
                        if (itemValue == null ||
                                ((itemValue instanceof String) &&
                                (((String) itemValue).length() == 0))) {
                            incompleteRequiredColumns.add(
                                    childDescriptor.getDisplayName());
                        }
                    }
                }

                if (incompleteRequiredColumns.size() > 0) {
                    String msg = NbBundle.getMessage(
                            SimpleTabularDataCustomEditor.class,
                            "MSG_REQUIRED_FIELD_IS_MISSING", // NOI18N
                            rowIndex + 1,
                            incompleteRequiredColumns);
                    throw new RuntimeException(msg);
                }
            }

            // Check undefined index columns in the tabular data.
            List<String> undefinedIndexColumns = new ArrayList<String>();
            for (int col = 0; col < indexColumnCount; col++) {
                Object itemValue = itemValues[col];
                if (itemValues == null ||
                        ((itemValue instanceof String) &&
                        (((String) itemValue).length() == 0))) {
                    undefinedIndexColumns.add(columnNames[col]);
                }
            }

            if (undefinedIndexColumns.size() > 0) {
                String msg = NbBundle.getMessage(
                        SimpleTabularDataCustomEditor.class,
                        "MSG_INDEX_FIELD_IS_MISSING", // NOI18N
                        rowIndex + 1,
                        undefinedIndexColumns);
                throw new RuntimeException(msg);
            }

            CompositeData rowData = new CompositeDataSupport(
                    rowType, columnNames, itemValues);
            ret.put(rowData);

            rowIndex++;
        }

        return ret;
    }

    @Override
    public Dimension getPreferredSize() {
        // The preferred panel width is determined by all table columns'
        // perferred widths.
        int width = 5 * columnNames.length;  // some initial padding
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = null;
            try {
                column = table.getColumn(columnNames[i]);
            } catch (IllegalArgumentException e) {
                // this column is hidden
                continue;
            }
            width += column.getPreferredWidth();
        }

        width = Math.max(width, 450);
        width = Math.min(width, 1000);

        return new Dimension(width, 250);
    }

    protected JTable createTable(DefaultTableModel tableModel) {
        JTable ret = new JTable(tableModel) {

            @Override
            public Class getColumnClass(int column) {
                CompositeType rowType = tabularType.getRowType();                                
                OpenType columnType = //columnTypes[column];
                    rowType.getType(columnNames[column]);
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

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (!isWritable) {
                    return null;
                }

                Class clazz = getColumnClass(column);
                return TabularDataCellEditorFactory.getEditor(clazz,
                        descriptor == null ? null : descriptor.getChild(columnNames[column]));
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                Class clazz = getColumnClass(column);
                return TabularDataCellRendererFactory.getRenderer(clazz,
                        descriptor == null ? null : descriptor.getChild(columnNames[column]));
            }
        };

        return ret;
    }

    protected void configureTableColumns(JTable table) {
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setHeaderRenderer(headerCellRenderer);
        }
    }

    private void initTableColumnWidths() {
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = null;
            try {
                column = table.getColumn(columnNames[i]);
            } catch (IllegalArgumentException e) {
                // this column is hidden
                continue;
            }

            TableCellRenderer renderer = column.getHeaderRenderer();
            Component comp = renderer.getTableCellRendererComponent(
                    table, column.getHeaderValue(), false, false, 0, 0);
            int width = comp.getPreferredSize().width;
            column.setPreferredWidth(width);
        }
    }
    
    protected void initColumnNames() {
           
        CompositeType rowType = tabularType.getRowType();      
        
        // Construct reordered column names to make sure all the keys are
        // at the beginning of the list.
        @SuppressWarnings("unchecked")
        List<String> indexNames = tabularType.getIndexNames();
        indexColumnCount = indexNames.size();

        List<String> columnNameList = new ArrayList<String>();
        columnNameList.addAll(indexNames);

        for (Object columnName : rowType.keySet()) {
            if (!indexNames.contains(columnName)) {
                columnNameList.add((String) columnName);
            }
        }
        columnNames = columnNameList.toArray(new String[]{});
    }

    protected Vector<Vector> getDataVector(TabularData tabularData) {
        
        Vector<Vector> dataVector = new Vector<Vector>();
        for (Object rowDataObj : tabularData.values()) {
            CompositeData rowComposite = (CompositeData) rowDataObj;
            Vector<Object> rowVector = new Vector<Object>();
            for (String columnName : columnNames) {
                rowVector.add(rowComposite.get(columnName));
            }
            dataVector.add(rowVector);
        }
        
        return dataVector;
    }
    
    private DefaultTableModel createTableModel(TabularData tabularData) {
     
        Vector<Vector> dataVector = getDataVector(tabularData);

        Vector<String> columnIdentifiers = new Vector<String>();
        for (int i = 0; i < columnNames.length; i++) {
            columnIdentifiers.addElement(columnNames[i]);
        }

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setDataVector(dataVector, columnIdentifiers);

        return tableModel;
    }

    /**
     * Creates a new row for the tabular data.
     * 
     * @return  a Vector of objects for the new row in the tabular data. 
     *          The order of the objects in the Vector is determined by 
     *          the table columns.
     */
    @SuppressWarnings("unchecked")
    protected Vector createRow() {
        Vector row = new Vector();

        CompositeType rowType = tabularType.getRowType();

        for (int col = 0; col < columnNames.length; col++) {
            String headerName = columnNames[col];
            OpenType openType = rowType.getType(headerName);
            if (openType.equals(SimpleType.STRING)) {
                row.add("");
            } else if (openType.equals(SimpleType.BOOLEAN)) {
                row.add(Boolean.FALSE);
            } else if (openType.equals(SimpleType.INTEGER)) {  // ?
                row.add(0);
            } else if (openType instanceof TabularType) {
                row.add(new TabularDataSupport((TabularType) openType));
            } else {
                System.out.println("Unknown type: " + openType);
                row.add("");
            }
        }

        return row;
    }

    @SuppressWarnings("unchecked")
    protected Vector<Vector> getDataVector() {
        return ((DefaultTableModel) table.getModel()).getDataVector();
    }

    public void actionPerformed(ActionEvent event) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        Vector<Vector> dataVector = getDataVector();

        JButton source = (JButton) event.getSource();

        if (source == addButton) {
            Vector row = createRow();
            if (row != null) {
                dataVector.addElement(row);
                table.addNotify();

                int newRowIndex = dataVector.size() - 1;
                table.getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
            }
        } else {
            if (source == deleteButton) {
                int[] rowIndices = table.getSelectedRows(); // guaranteed to be non-null
                for (int i = rowIndices.length - 1; i >= 0; i--) {
                    dataVector.removeElementAt(rowIndices[i]);
                }
            } else { // source == deleteAllButton                
                dataVector.clear();
            }

            table.addNotify();
            table.getSelectionModel().clearSelection();
        }

        deleteAllButton.setEnabled(isWritable && !dataVector.isEmpty());
    }

    class TabularDataTableHeaderRenderer extends DefaultTableCellRenderer {
        // This method is called each time a column header
        // using this renderer needs to be rendered.
        @Override
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

            CompositeType rowType = tabularType.getRowType();
            String columnTitle = value.toString().toUpperCase();
            String columnDescription = //columnDescriptions[getColumnIndex(colIndex)];            
                rowType.getDescription(columnTitle);

            if (descriptor != null) {
                // Display name and description in the composite type 
                // definition will take precedence if there are any 
                // inconsistencies.
                JBIComponentConfigurationDescriptor childDescriptor =
                        descriptor.getChild(value.toString());
                if (childDescriptor != null) {
                    columnTitle = childDescriptor.getDisplayName();
                    columnDescription = childDescriptor.getDescription();
                }
            }

            if (columnNames.length >= MIN_COLUMN_COUNT_FOR_MULTILINE_HEADER) { // make the table header multi-line
                columnTitle = columnTitle.replace(" ", "<br>");
            }

            String myValue = colIndex < indexColumnCount ? "<html><body><b><i>" + columnTitle + "</i></b></body></html>" : // NOI18N
                    "<html><body><b>" + columnTitle + "</b></body></html>"; // NOI18N
            setText(myValue);
            setToolTipText(columnDescription);
            setBorder(UIManager.getBorder("TableHeader.cellBorder")); // NOI18N
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }

        protected int getColumnIndex(int column) {
            return column;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        deleteAllButton = new javax.swing.JButton();
        label = new javax.swing.JLabel();

        jScrollPane1.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "LBL_ADD")); // NOI18N
        buttonPanel.add(addButton);
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "ACS_ADD")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "LBL_DELETE")); // NOI18N
        buttonPanel.add(deleteButton);
        deleteButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "ACS_DELETE")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deleteAllButton, org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "LBL_DELETE_ALL")); // NOI18N
        buttonPanel.add(deleteAllButton);
        deleteAllButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "ACS_DELETE_ALL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(label, tableLabelText);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(label, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 364, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(label)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        label.getAccessibleContext().setAccessibleDescription(tableLabelDescription);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteAllButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel label;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
