/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.editor;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.openide.util.WeakListener;

import org.netbeans.tax.TreeNamedObjectMap;
import org.netbeans.tax.TreeAttribute;
import org.netbeans.tax.TreeName;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.beans.TreeObjectListProxyListener;
import org.netbeans.modules.xml.tax.beans.Lib;

/**
 * Table oriented customizer of TreeElement attribute list.
 *
 * @author  Petr Kuzel
 * @author  Vladimir Zboril
 * @author  Libor Kramolis
 * @version 1.0
 */
public class TreeElementAttributeListCustomizer extends JPanel implements Customizer, PropertyChangeListener {
    private static final boolean DEBUG = false;

    
    /** Serial Version UID */
    private static final long serialVersionUID = 1071471854210683733L;
    
    private String headerToolTip;
    
    private final int COL_NAME  = 0;
    private final int COL_VALUE = 1;
    private final int COL_COUNT = 2;
    
    //
    // init
    //
    
    /** Creates new form TreeElementAttributeListCustomizer */
    
    public TreeElementAttributeListCustomizer() {
        
        headerToolTip = Util.getString("PROP_headerTooltip");
        initComponents ();
        //mnemonics
        upButton.setMnemonic(Util.getChar("MNE_element_attributelist_up")); // NOI18N
        downButton.setMnemonic(Util.getChar("MNE_element_attributelist_down")); // NOI18N
        removeButton.setMnemonic(Util.getChar("MNE_element_attributelist_remove")); // NOI18N
        addButton.setMnemonic(Util.getChar("MNE_element_attributelist_add")); // NOI18N
        
        // Add header renderer
        attrTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        
        // Set ListSelectionModel
        attrTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowsm = attrTable.getSelectionModel();
        rowsm.addListSelectionListener (new ListSelectionListener(){
                public void valueChanged (ListSelectionEvent e) {
                    if ( DEBUG ) {
                        Util.debug ("\n#=- TreeElementAttributeListCustomizer::ListSelectionListener.valueChanged: event = " + e);
                        Util.debug ("#=-     event.getValueIsAdjusting() = " + e.getValueIsAdjusting());
                        Util.debug ("#=-     event.getFirstIndex()       = " + e.getFirstIndex());
                        Util.debug ("#=-     event.getLastIndex()        = " + e.getLastIndex());
                    }

                    if (e.getValueIsAdjusting())
                        return;
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();

                    if ( DEBUG ) {
                        Util.debug ("#=-     event.getSource()                 = " + lsm);
                        Util.debug ("#=-     selectionModel.isSelectionEmpty() = " + lsm.isSelectionEmpty());
                        Util.debug ("#=-     attrTable.getSelectedRow()        = " + attrTable.getSelectedRow());
                        Util.debug ("#=-     attrTable.getRowCount()           = " + attrTable.getRowCount());
                    }

                    if ( lsm.isSelectionEmpty() ||
                         ( attrTable.getRowCount() == 0 ) ) {
                        upButton.setEnabled (false);
                        downButton.setEnabled (false);
                        removeButton.setEnabled (false);
                    }else {
                        upButton.setEnabled (attrTable.getSelectedRow() > 0);
                        downButton.setEnabled (attrTable.getSelectedRow() < (numRows() - 1));
                        removeButton.setEnabled (true);
                    }
                }
            });
        
        //Click to header - sorting
        attrTable.getTableHeader().addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    TableColumnModel colModel = attrTable.getColumnModel();
                    int columnIndex = colModel.getColumnIndexAtX(e.getX());
                    // No column was clicked.
                    if(columnIndex < 0) return;
                    int modelIndex = colModel.getColumn(columnIndex).getModelIndex();
                    // not detected column
                    if (modelIndex < 0) return;
                    // Do sort !
                    AttlistTableModel tm = (AttlistTableModel) attrTable.getModel();
                    tm.sortByColumn(columnIndex);
                }
            });
        
    }
    
    
    // Get count of rows
    private int numRows(){
        return attrTable.getModel().getRowCount();
    }
    
    //Get count of columns
    private int numCols(){
        return attrTable.getModel().getColumnCount();
    }
    
    //
    // itself
    //
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        tableScrollPane = new javax.swing.JScrollPane();
        attrTable = new javax.swing.JTable();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setPreferredSize(new java.awt.Dimension(450, 250));
        setMinimumSize(new java.awt.Dimension(450, 250));
        tableScrollPane.setViewportView(attrTable);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(tableScrollPane, gridBagConstraints1);
        
        upButton.setText(Util.getString("TEXT_element_attributelist_up"));
        upButton.setEnabled(false);
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        add(upButton, gridBagConstraints1);
        
        downButton.setText(Util.getString("TEXT_element_attributelist_down"));
        downButton.setEnabled(false);
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(5, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        add(downButton, gridBagConstraints1);
        
        addButton.setText(Util.getString("TEXT_element_attributelist_add"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(5, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        add(addButton, gridBagConstraints1);
        
        removeButton.setText(Util.getString("TEXT_element_attributelist_remove"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(5, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        add(removeButton, gridBagConstraints1);
        
    }//GEN-END:initComponents
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        AttlistTableModel tm = (AttlistTableModel) attrTable.getModel();
        tm.addRow();
        int actualIndex = numRows() - 1;
        attrTable.getSelectionModel().setSelectionInterval(actualIndex, actualIndex);
    }//GEN-LAST:event_addButtonActionPerformed
    
    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        // Add your handling code here:
        AttlistTableModel tm = (AttlistTableModel) attrTable.getModel();
        int index = attrTable.getSelectedRow();
        if (index > 0) {
            tm.moveRow(index, index - 1);
            attrTable.getSelectionModel().setSelectionInterval(index - 1,index - 1);
        }
    }//GEN-LAST:event_upButtonActionPerformed
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        AttlistTableModel tm = (AttlistTableModel) attrTable.getModel();
        
        int sel = attrTable.getSelectedRow();
        if (sel > -1) {
            tm.removeRow(sel);
            if (numRows() > 0) {
                if (sel <= numRows() - 1)
                    attrTable.getSelectionModel().setSelectionInterval(sel,sel);
                else
                    attrTable.getSelectionModel().setSelectionInterval(sel - 1, sel - 1);
            } else removeButton.setEnabled(false);
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        // Add your handling code here:
        AttlistTableModel tm = (AttlistTableModel) attrTable.getModel();
        int index = attrTable.getSelectedRow();
        if (index > -1 && index < numRows() - 1) {
            tm.moveRow(index, index + 1);
            attrTable.getSelectionModel().setSelectionInterval(index + 1,index + 1);
        }
    }//GEN-LAST:event_downButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JTable attrTable;
    private javax.swing.JButton upButton;
    private javax.swing.JButton downButton;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
    
    private TreeNamedObjectMap peer;
    private AttlistTableModel tableModel;
    
    /**
     */
    public void setObject (Object obj) {
        peer = (TreeNamedObjectMap) obj;
        tableModel = new AttlistTableModel(/*peer*/);
        attrTable.setModel(tableModel);
        attrTable.addKeyListener(new RowKeyListener(attrTable));
        
        /** First table column is row selector. */
        TableColumn column = null;
        for (int i = 0; i < COL_COUNT; i++) {
            column = attrTable.getColumnModel().getColumn (i);
            //column.setPreferredWidth (50);
        }
        
        updateView (peer);
        
        TreeObjectListProxyListener proxy = new TreeObjectListProxyListener(peer);
        proxy.addPropertyChangeListener(WeakListener.propertyChange(this, proxy));
    }
    
    
    /** Udate state accordingly*/
    public void propertyChange(final PropertyChangeEvent e) {
        if (e.getSource() == null)
            return;
        
        updateView (peer);
    }
    
    /** Update visualization accordingly. */
    private void updateView (TreeNamedObjectMap model) {
        tableModel.fireTableDataChanged();
    }
    
    //
    // class RowKeyListener
    //
    
    /** deletes whole row by pressing DELETE on row column. */
    private class RowKeyListener extends KeyAdapter {
        
        /** */
        private JTable table;
        
        
        //
        // init
        //
        
        public RowKeyListener (JTable table) {
            this.table = table;
        }
        
        
        //
        // itself
        //
        
        /**
         */
        public void keyReleased (KeyEvent e) {
            //Util.debug("Event: " + e); // NOI18N
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                tableModel.removeRow (table.getSelectedRow());
//                  peer.remove (peer.get (table.getSelectedRow()));
//                  tableModel.fireTableDataChanged();
            }
        }
    }
    
    
    //
    // class AttlistTableModel
    //
    
    /**
     *
     */
    private class AttlistTableModel extends AbstractTableModel {
        
        /** Serial Version UID */
        private static final long serialVersionUID = 1567143493464383838L;
        
        boolean  ascending = true;
        int SortedColumn = -1;
        int indexes[];
        
        //
        // init
        //
        
        /** Create a data node for a given data object.
         * The provided children object will be used to hold all child nodes.
         * @param obj object to work with
         * @param ch children container for the node
         */
        public AttlistTableModel () {
            super();
        }
        
        /** Returns the number of rows in the model */
        public int getRowCount () {
            return peer.size();
        }
        
        
        /** Returns the number of columns in the model */
        public int getColumnCount () {
            return COL_COUNT;
        }
        
        /** Returns the class for a model. */
        public Class getColumnClass (int index) {
            return String.class;
        }
        
        
        // Get unique attribute name for added blank rows
        private String getUniqueName(){
            int index = 0, i = 0;
            final String prefix = "attribute-"; // NOI18N
            String result = prefix + Integer.toString(index);
            while (i<numRows()){
                if (result.equals(attrTable.getValueAt(i,0))) {
                    result = prefix + Integer.toString(++index);
                    i = 0;
                } else  ++i;
            }
            return result;
        }
        
        /**
         */
        public void addRow () {
            
            TreeAttribute attr = (TreeAttribute) Lib.createAttributeDialog();
            if (attr != null) {
                boolean toSet = true;
                TreeAttribute oldAttribute = (TreeAttribute) peer.get (attr.getTreeName());
                if ( oldAttribute != null ) {
                    toSet = Lib.confirmAction
                        (Util.getString ("MSG_replace_attribute", attr.getQName()));
                }
                if ( toSet ) {
                    peer.add (attr);
                    SortedColumn = -1;
                    fireTableStructureChanged();
                }
            }
            /*
              try {
              peer.add (new TreeAttribute (getUniqueName(), ""));
              SortedColumn = -1;
              fireTableStructureChanged();
              } catch (TreeException exc) {
              Util.notifyTreeException (exc);
              }
            */
        }
        
        /**
         */
        public void removeRow (int row) {
            peer.remove(row);
            fireTableStructureChanged();
        }

        /**
         */
        public void moveRow (int fromIndex, int toIndex) {
            try {
                peer.switchObjects (fromIndex, toIndex);

                SortedColumn = -1;
                fireTableStructureChanged();
            } catch (TreeException exc) {
                Util.notifyTreeException (exc);
            }
        };
        
        /**
         */
        public Object getValueAt (int row, int column) {
            TreeAttribute attr = (TreeAttribute) peer.get(row);
            switch (column) {
            case COL_NAME:
                return attr.getQName();
            case COL_VALUE:
                return attr.getValue();
            default:
                return null;
            }
        }
        
        /**
         */
        public void setValueAt (Object val, int row, int column) {
            TreeAttribute attr = (TreeAttribute) peer.get (row);
            try {
                if (column == COL_NAME) {

                    String attrName = (String) val;

                    boolean toSet = true;
                    TreeAttribute oldAttribute = (TreeAttribute) peer.get (new TreeName (attrName));
                    if ( attr != oldAttribute ) {
                        if ( oldAttribute != null ) {
                            toSet = Lib.confirmAction
                                (Util.getString ("MSG_replace_attribute", attrName));
                        }
                    }
                    if ( toSet ) {
                        attr.setQName (attrName);
                    }
                    
                } else if (column == COL_VALUE) {
                    attr.setValue ((String) val);
                }
            } catch (TreeException exc) {
                Util.notifyTreeException (exc);
            }
        }
        
        /**
         */
        public String getColumnName (int column) {
            switch (column) {
            case COL_NAME:
                return Util.getString ("NAME_column_name");
            case COL_VALUE:
                return Util.getString ("NAME_column_value");
            default:
                return ""; // NOI18N
            }
        }
        
        /** Returns true for all cells which are editable. For a
         * a new cell is editable only name field.
         */
        public boolean isCellEditable (int rowIndex, int columnIndex) {
            return true;
        }
        
        /**
         */
        public void sortByColumn (int column) {
            
            if (SortedColumn == column)
                ascending = !ascending;
            else {
                SortedColumn = column;
                ascending = true;
            }
            
            indexes = new int[getRowCount()];
            for (int row = 0; row < getRowCount(); row++) {
                indexes[row] = row;
            }
            
            n2sort(column);
            
            int[] indx = new int[indexes.length];
            for (int row = 0; row < indx.length; row++) {
                indx[indexes[row]] = row;
            }
            
            try {
                peer.reorder (indx);
                fireTableChanged(new TableModelEvent(this));
            } catch (TreeException exc) {
                Util.notifyTreeException (exc);
            }
        }
        
        /**
         */
        private void n2sort (int col) {
            for (int i = 0; i < getRowCount(); i++) {
                for (int j = i + 1; j < getRowCount(); j++) {
                    if (compare(indexes[i], indexes[j], col) == -1) {
                        // swap it
                        int tmp = indexes[i];
                        indexes[i] = indexes[j];
                        indexes[j] = tmp;
                    }
                }
            }
        }
        
        /**
         */
        private int compare (int row1, int row2, int col) {
            int result = compareRowsByColumn(row1, row2, col);
            if (result != 0) {
                return ascending ? -result : result;
            }
            return 0;
        }
        
        /**
         */
        private int compareRowsByColumn (int row1, int row2, int column) {
            
            Class type = getColumnClass(column);
            
            Object o1 = getValueAt(row1, column);
            Object o2 = getValueAt(row2, column);
            
            
            // If both values are null, return 0.
            
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            
            // Compare String
            
            if (type == String.class) {
                String s1 = (String) getValueAt(row1, column);
                String s2 = (String) getValueAt(row2, column);
                int result = s1.compareTo(s2);
                
                if (result < 0) {
                    return -1;
                } else if (result > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
        
    } // end: class AttlistTableModel
    
    
    //
    // Header renderer
    //
    
    private class HeaderRenderer extends JLabel implements TableCellRenderer {
        
        /** Serial Version UID */
        private static final long serialVersionUID =-3658206203140258583L;
        
        public HeaderRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
            setBorder(UIManager.getBorder("TableHeader.cellBorder")); // NOI18N
            setToolTipText(headerToolTip);
        }
        
        public Component getTableCellRendererComponent (JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus,int row, int column) {
            
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }
            
            AttlistTableModel tm = (AttlistTableModel)table.getModel();
            if (column == tm.SortedColumn)
                setIcon(tm.ascending ? new javax.swing.ImageIcon(getClass().getResource("down.gif")): // NOI18N
                        new javax.swing.ImageIcon(getClass().getResource("up.gif"))); // NOI18N
            else setIcon(new javax.swing.ImageIcon(getClass().getResource("no.gif"))); // NOI18N
            
            setText((value == null) ? "" : value.toString()); // NOI18N
            return this;
        }
    } // end: class HeaderRenderer

}
