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

package org.netbeans.modules.java.editor.imports;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.EventObject;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;

/**
 * JTable with custom renderer, so second column looks editable (JComboBox).
 * Second column also has CellEditor (also a JComboBox).
 *
 * @author  eakle, Martin Roskanin
 */
public class FixDuplicateImportStmts extends javax.swing.JPanel{
    private PackagesTblModel tblModel = null;
    
    public FixDuplicateImportStmts() {
        initComponents();
    }
    
    public void initPanel(String[] simpleNames, String[][] choices, String[] defaults) {
        initComponentsMore(simpleNames, choices, defaults);
        setAccessible();
    }
    
    private void initComponentsMore(String simpleNames[], String choices[][], String defaults[]) {
        tblModel = new PackagesTblModel(simpleNames.length);
        packagesTbl.setModel(tblModel);
        packagesTbl.setColumnSelectionInterval(1,1);        
        packagesTbl.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        packagesTbl.setSurrendersFocusOnKeystroke(true);
        

        for (int i=0; i<simpleNames.length; i++){
            packagesTbl.setValueAt(simpleNames[i], i, 0 );
        }
        
        for (int i=0; i<choices.length; i++){
            JComboBox combo = new JComboBox(choices[i]);
            combo.setSelectedItem(defaults[i]);
            combo.setRenderer(HtmlRenderer.createRenderer());
            combo.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_Combo_ACSD")); //NOI18N
            combo.getAccessibleContext().setAccessibleName(getBundleString("FixDupImportStmts_Combo_Name_ACSD")); //NOI18N
            packagesTbl.setValueAt(combo, i, 1 );
        }
        
        packagesTbl.setDefaultRenderer( JComponent.class, new JComponentCellRenderer() );
        packagesTbl.setDefaultEditor( JComponent.class, new JComponentCellEditor() );
        
        //packagesTbl.setPreferredScrollableViewportSize(new Dimension(560, 350));
        adjustTableSize(packagesTbl, 5, 560);
    
        // load localized text into widgets:
        jLabel1.setText(getBundleString("FixDupImportStmts_IntroLbl")); //NOI18N
        jLabel1.setLabelFor(packagesTbl);
        jLabel1.setDisplayedMnemonic(getBundleString("FixDupImportStmts_IntroLbl_Mnemonic").charAt(0)); // NOI18N
    }
    
    private static String getBundleString(String s) {
        return NbBundle.getMessage(FixDuplicateImportStmts.class, s);
    }
    
    
    private void setAccessible() {
        // establish initial focus in JTable.
        packagesTbl.requestFocusInWindow();
        
        // remove the built-in behavior of Enter in a JTable so it invoke the dialog's OK btn:
        enableEnterToClose(packagesTbl);
        getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_IntroLbl")); // NOI18N
    }
    
    public String[] getSelections() {
        return tblModel.getSelections();
    }
    
    // Set table size based on number of rows desired.
    private void adjustTableSize(JTable table, int rows, int width) {
        int margin = table.getIntercellSpacing().height;
        int unit = table.getRowHeight() + margin;
        int height = rows * unit -  margin;
        Dimension dim = new Dimension(width, height);
        table.setPreferredScrollableViewportSize(dim);
    }

    static private void enableEnterToClose(javax.swing.JTable table)
    {
        javax.swing.InputMap inputMap =
            table.getInputMap(table.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke("ENTER");    // NOI18N
        javax.swing.InputMap parentMap = inputMap.getParent();
	parentMap.remove(esc);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        packagesTbl = new JTableX();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        setPreferredSize(new java.awt.Dimension(560, 200));
        jLabel1.setText("<html>~More than one class found in classpath for some Type Name in source.  Select class to use in import statement for each Type Name below:</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        packagesTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"MyType", "com.foo.AllTypes"},
                {"OtherType", "com.foo.AllTypes"},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "~Type Name", "~Class to Import"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(packagesTbl);
        packagesTbl.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/java/Bundle").getString("FixDupImportStmts_Table_ACSD"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable packagesTbl;
    // End of variables declaration//GEN-END:variables
    
    // ------------------ Inner Classes -------------------------
    /*
     * Service information for a selected Organization
     */
    private static class PackagesTblModel extends javax.swing.table.DefaultTableModel {
        
        String columnNames[] = {
            getBundleString("FixDupImportStmts_TblCol1Hdr"), //NOI18N
            getBundleString("FixDupImportStmts_TblCol2Hdr")  //NOI18N
        };
        
        public PackagesTblModel(int rowCount) {
            super(rowCount, 2);
        }

        public String getColumnName(int col){
            return columnNames[col];
        }
        
        public boolean isCellEditable(int row, int col) {
            return (col == 1);
        }
                
        public String[] getSelections() {
            int rowCount = getRowCount();
            String ret[] = new String[rowCount];
            for (int i = 0; i<rowCount; i++ ){
                ret [i] = (String)((JComboBox)getValueAt(i, 1)).getSelectedItem();
            }
            return ret;
        }
        
    }

    // ----------------------
    
    // extend JTable to support a cell editor for each row.
    private static class JTableX extends JTable
     {
        //private boolean done = false;
 
         private boolean needCalcRowHeight = true;        
    
         public JTableX () {}
         
         public void updateUI() {
             super.updateUI();
             needCalcRowHeight = true;            
         }
         
         public void paint(Graphics g) {
             if (needCalcRowHeight) {
                 calcRowHeight(g);
             }
             super.paint(g);
         }
         
         /** Calculate the height of rows based on the current font.  This is
          *  done when the first paint occurs, to ensure that a valid Graphics
          *  object is available.
          */
         private void calcRowHeight(Graphics g) {
             Font f = getFont();
             FontMetrics fm = g.getFontMetrics(f);
             int rowHeight = (int) (fm.getHeight() * 1.4);
             needCalcRowHeight = false;
             setRowHeight(rowHeight);
         }
        
        
        // rows were not high enough to show a JComboBox renderer.
         /*
        public void paint(Graphics g) {
            if (!done) {
               this.setRowHeight((int)(this.getRowHeight() * 1.4));
               done = true;
            }
            super.paint(g);
        }
         
          */

        public TableCellRenderer getCellRenderer(int row, int column) {
                TableColumn tableColumn = getColumnModel().getColumn(column);
                TableCellRenderer renderer = tableColumn.getCellRenderer();
                if (renderer == null) {
                        Class c = getColumnClass(column);
                        if( c.equals(Object.class) )
                        {
                                Object o = getValueAt(row,column);
                                if( o != null )
                                        c = getValueAt(row,column).getClass();
                        }
                        renderer = getDefaultRenderer(c);
                }
                return renderer;
        }

        public TableCellEditor getCellEditor(int row, int column) {
                TableColumn tableColumn = getColumnModel().getColumn(column);
                TableCellEditor editor = tableColumn.getCellEditor();
                if (editor == null) {
                        Class c = getColumnClass(column);
                        if( c.equals(Object.class) )
                        {
                                Object o = getValueAt(row,column);
                                if( o != null )
                                        c = getValueAt(row,column).getClass();
                        }
                        editor = getDefaultEditor(c);
                }
                return editor;
        }
 
    }

    
    private class JComponentCellRenderer implements TableCellRenderer {
        public JComponentCellRenderer () {}
        
        public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
            return (JComponent)value;
        }
    }


    private class JComponentCellEditor implements TableCellEditor{
        
        protected EventListenerList listenerList = new EventListenerList();
        transient protected ChangeEvent changeEvent = null;
        
        protected JComponent editorComponent = null;
        
        public JComponentCellEditor () {}
        
        public Component getComponent() {
            return editorComponent;
        }
        
        
        public Object getCellEditorValue() {
            return editorComponent;
        }
        
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }
        
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }
 
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
        
        public void addCellEditorListener(CellEditorListener l) {
            listenerList.add(CellEditorListener.class, l);
        }
        
        public void removeCellEditorListener(CellEditorListener l) {
            listenerList.remove(CellEditorListener.class, l);
        }
        
        protected void fireEditingStopped() {
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==CellEditorListener.class) {
                    // Lazily create the event:
                    if (changeEvent == null)
                        changeEvent = new ChangeEvent(this);
                    ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
                }
            }
        }
        
        protected void fireEditingCanceled() {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==CellEditorListener.class) {
                    // Lazily create the event:
                    if (changeEvent == null)
                        changeEvent = new ChangeEvent(this);
                    ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
                }
            }
        }
        
        // implements javax.swing.table.TableCellEditor
        public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
            editorComponent = (JComponent)value;
            return editorComponent;
        }
        
    } // End of class JComponentCellEditor

}