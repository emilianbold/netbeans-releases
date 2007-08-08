/*
 * MyNewJPanel.java
 *
 * Created on August 7, 2007, 5:28 PM
 */

package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.NbBundle;

/**
 *
 * @author  jqian
 */
public class SimpleTabularDataCustomEditor extends javax.swing.JPanel 
    implements EnhancedCustomPropertyEditor, ActionListener {    
     
    private TableCellRenderer headerCellRenderer;
    
    /** Number of index columns in the tabular data type. */
    private int indexColumnCount;
    
    // all the keys are at the beginning of the array
    private String[] columnNames;
    
    private String[] columnDescriptions;
    private OpenType[] columnTypes;
    
    private TabularType tabularType;
    private TabularData tabularData;
    
    
    /** Creates new form MyNewJPanel */
    public SimpleTabularDataCustomEditor(SimpleTabularDataEditor editor) {
        
        initComponents();
        
        headerCellRenderer = createTableHeaderRenderer(); 
        
        tabularData = (TabularData) editor.getValue();        
        DefaultTableModel tableModel = initTableModel(tabularData);
        table = createTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(table);
        
        configureTableColumns(table);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int minSelectionIndex = table.getSelectionModel().getMinSelectionIndex();
                    deleteButton.setEnabled(minSelectionIndex != -1);
                }
            }
        });
        
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        deleteAllButton.addActionListener(this);
        deleteButton.setEnabled(false);
    }
    
    protected TableCellRenderer createTableHeaderRenderer() {
        return new TabularDataTableHeaderRenderer();   
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
                for (int i = 0; i < indexColumnCount; i++) {
                    if (itemValues[i] == null ||
                            // the following check is not really required by tabular data
                            ((String)itemValues[i]).trim().length() == 0) {
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
        } catch (KeyAlreadyExistsException e) {
            e.printStackTrace();
            
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            
            ret = tabularData;  // if anything wrong, just return the old value
        } catch (OpenDataException e) {
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
    
    protected JTable createTable(DefaultTableModel tableModel) {
        JTable ret = new JTable(tableModel) {
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
        
        return ret;
    }
        
    protected void configureTableColumns(JTable table) {
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setHeaderRenderer(headerCellRenderer);
        }
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
        columnNames = columnNameList.toArray(new String[]{});
        
        columnDescriptions = new String[columnNames.length];
        columnTypes = new OpenType[columnNames.length];
        
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            columnDescriptions[i] = rowType.getDescription(columnName);
            columnTypes[i] = rowType.getType(columnName);
        }
        
        Vector<Vector> dataVector = new Vector<Vector>();
        for (Object rowDataObj : tabularData.values()) {
            CompositeData rowData = (CompositeData) rowDataObj;
            Vector<Object> row = new Vector<Object>();
            for (String columnName : columnNames) {
                row.add(rowData.get(columnName));
            }
            dataVector.add(row);
        }
        
        Vector<String> columnIdentifiers = new Vector<String>();
        for (int i = 0; i < columnNames.length; i++) {
            columnIdentifiers.addElement(columnNames[i]);
        }
        
        DefaultTableModel tableModel = createTableModel();
        tableModel.setDataVector(dataVector, columnIdentifiers);
        
        return tableModel;
    }
    
    protected DefaultTableModel createTableModel() {
        return new DefaultTableModel();
    }
    
    protected Vector<String> createRow() {
        Vector<String> row = new Vector<String>();
        for (int i = 0; i < columnNames.length; i++) {
            row.addElement(null);
        }
        return row;
    }
    
    private Vector<Vector> getDataVector() {
        return ((DefaultTableModel)table.getModel()).getDataVector();
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
    }    
    
    class TabularDataTableHeaderRenderer extends DefaultTableCellRenderer {
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
            String myValue = (value == null) ? "" : // NOI18N
                "<html><body><b>" + value.toString().toUpperCase() + "</b></body></html>"; // NOI18N
            setText(myValue);
            setToolTipText(columnDescriptions[getColumnIndex(colIndex)]); 
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        label.setLabelFor(table);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "LBL_TABLE")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(label, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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

        label.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SimpleTabularDataCustomEditor.class, "ACS_TABLE_LABEL")); // NOI18N
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
