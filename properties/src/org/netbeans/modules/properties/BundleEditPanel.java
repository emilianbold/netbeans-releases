/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.awt.Color;
import java.awt.SystemColor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.SwingUtilities;
import javax.swing.JTable;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;

import org.netbeans.modules.properties.syntax.PropertiesOptions;
import org.netbeans.modules.properties.syntax.PropertiesTokenContext;

import org.openide.util.NbBundle;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.options.SystemOption;
import org.openide.TopManager;


/**
 * @author  Petr Jiricka
 */
public class BundleEditPanel extends javax.swing.JPanel {

    private static final int DEFAULT_TABLE_WIDTH = 600;
    private static final int DEFAULT_KEY_WIDTH   = 150;

    private DataObject dobj;
    private PropertiesTableModel ptm;

    private ListSelectionModel rowSelections;
    private ListSelectionModel columnSelections;

    static final long serialVersionUID =-843810329041244483L;

    // colors for table view 
    private static Color keyColor;
    private static Color valueColor;
    private static Color shadowColor;
    private static Color keyBackground;
    private static Color valueBackground;
    
    private SettingsChangeListener settingsListener;

    
    /** Creates new form BundleEditPanel */
    public BundleEditPanel(final DataObject obj, PropertiesTableModel ptm) {
        this.dobj = obj;
        this.ptm = ptm;

        initComponents ();

        // this subclass of Default column model is provided only due to set of widths
        // of columns (it works bad under jdk 1.2)
        theTable.setColumnModel(new javax.swing.table.DefaultTableColumnModel() {
            public void addColumn(TableColumn aColumn) {
                if (aColumn == null) {
                    throw new IllegalArgumentException("Object is null");
                }

                tableColumns.addElement(aColumn);
                aColumn.addPropertyChangeListener(this);
                recalcWidthCache();

                setColumnWidths(totalColumnWidth);
                
                // Post columnAdded event notification
                fireColumnAdded(new javax.swing.event.TableColumnModelEvent(this, 0,
                                                          getColumnCount() - 1));
            }
            
            protected void recalcWidthCache() {
                try {
                    totalColumnWidth = theTable.getWidth();
                } catch(NullPointerException e) {
                    // just catch it, no handling
                }
                if (totalColumnWidth == 0)
                    super.recalcWidthCache();
            }
        });
        
        theTable.setModel(ptm);

        // table cell editor
        JTextField textField = new JTextField();
        textField.setBorder(new LineBorder(Color.black));
        theTable.setDefaultEditor(PropertiesTableModel.StringPair.class,
                                  new PropertiesTableCellEditor(textField, textComment, textValue));

        // set listening on changes of color settings
        Settings.addSettingsChangeListener(settingsListener = new SettingsChangeListener() {
                public void settingsChange(SettingsChangeEvent evt) {
                    // maybe could be refined
                    updateColors((PropertiesOptions)SystemOption.findObject(PropertiesOptions.class, true));
                    BundleEditPanel.this.repaint();
                }
            }
        );
        
        // set colors
        updateColors((PropertiesOptions)SystemOption.findObject(PropertiesOptions.class, true));
        
        // set renderer
        theTable.setDefaultRenderer(PropertiesTableModel.StringPair.class, new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int column) {
  
                java.awt.Component c = super.getTableCellRendererComponent(table,
                    UtilConvert.unicodesToChars(((PropertiesTableModel.StringPair)value).getValue()),
                    isSelected, hasFocus, row, column);
         
                PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)value;
                
                // set backgound
                if(sp.isKeyType())
                    c.setBackground(keyBackground);
                else {
                    if( sp.getValue() != null)
                        c.setBackground(valueBackground);
                    else
                        c.setBackground(shadowColor);
                }

                // set foregound
                if(sp.isKeyType())
                    c.setForeground(keyColor);
                else
                    c.setForeground(valueColor);
                
                return c;
            }
        });

        // selection listeners
        rowSelections = theTable.getSelectionModel();
        rowSelections.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    rowSelections = (ListSelectionModel)e.getSource();
                    selectionChanged();
                }
            });
        columnSelections = theTable.getColumnModel().getSelectionModel();
        columnSelections.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    columnSelections = (ListSelectionModel)e.getSource();
                    selectionChanged();
                }
            });

        // property change listener - listens to editing state of the table
        theTable.addPropertyChangeListener(new PropertyChangeListener() {
                                               public void propertyChange(PropertyChangeEvent evt) {
                                                   if (evt.getPropertyName().equals("tableCellEditor")) {
                                                       updateEnabled();
                                                   }
                                               }
                                           });

    }

    /** Updates colors from properties options. */
    private void updateColors(PropertiesOptions options) {
        Map map = options.getColoringMap();
        Coloring keyColoring = (Coloring)map.get(PropertiesTokenContext.contextPath.getFullTokenName(
            PropertiesTokenContext.KEY));
        keyColor = keyColoring.getForeColor();
        keyBackground = keyColoring.getBackColor();
        Coloring valueColoring = (Coloring)map.get(PropertiesTokenContext.contextPath.getFullTokenName(
            PropertiesTokenContext.VALUE));
        valueColor = valueColoring.getForeColor();
        valueBackground = valueColoring.getBackColor();

        shadowColor = options.getShadowTableCell();

        if(keyColor == null) keyColor = ((Coloring)map.get("default")).getBackColor(); // NOI18N
        if(keyBackground == null) keyBackground = ((Coloring)map.get("default")).getBackColor(); // NOI18N
        if(valueColor == null) valueColor = ((Coloring)map.get("default")).getBackColor(); // NOI18N
        if(valueBackground == null) valueBackground = ((Coloring)map.get("default")).getBackColor(); // NOI18N
        if(shadowColor == null) shadowColor = new Color(SystemColor.controlHighlight.getRGB());                    
    }
    
    // see above setting of table column model
    /** Calculates width of columns from the width of table component. */
    private void setColumnWidths(int entireWidth) {
        // set the column widths
        for (int i = 0; i < theTable.getColumnModel().getColumnCount(); i++) {
            TableColumn column = theTable.getColumnModel().getColumn(i);
  
            column.setWidth(entireWidth/theTable.getColumnModel().getColumnCount());            
        }
    }
    
    void stopEditing() {
        if (!theTable.isEditing()) return;
        TableCellEditor cellEdit = theTable.getCellEditor();
        if (cellEdit != null)
            cellEdit.stopCellEditing();
    }

    private void selectionChanged() {
        // label for the key/value
        if (columnSelections.isSelectionEmpty() || (columnSelections.getMaxSelectionIndex() > 0))
            valueLabel.setText(NbBundle.getBundle(PropertiesOpen.class).getString("LBL_ValueLabel"));
        else
            valueLabel.setText(NbBundle.getBundle(PropertiesOpen.class).getString("LBL_KeyLabel"));

        // remove button
        if (rowSelections.isSelectionEmpty() ||
                rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex()) {
            removeButton.setEnabled(false);
        }
        else {
            removeButton.setEnabled(true);
        }

        // fields at the bottom
        if (rowSelections.isSelectionEmpty() || columnSelections.isSelectionEmpty() ||
                rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex() ||
                columnSelections.getMinSelectionIndex() != columnSelections.getMaxSelectionIndex()) {
            if (!theTable.isEditing()) {
                textComment.setText("");
                textValue.setText("");
            }
        }
        else {
            if (!theTable.isEditing()) {
                PropertiesTableModel.StringPair sp =
                    (PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(),
                            columnSelections.getMinSelectionIndex());
                textComment.setText(sp.getComment());
                textValue.setText(sp.getValue());

                /*          boolean edit = theTable.editCellAt(rowSelections.getMinSelectionIndex(),
                                                           columnSelections.getMinSelectionIndex());*/
            }

            // the selection is ok - edit, if not already editing this field
            if (theTable.getEditingRow()    != rowSelections.getMinSelectionIndex() ||
                    theTable.getEditingColumn() != columnSelections.getMinSelectionIndex()) {
                SwingUtilities.invokeLater(new Runnable() {
                                               public void run() {
                                                   theTable.editCellAt(rowSelections.getMinSelectionIndex(),
                                                                       columnSelections.getMinSelectionIndex());
                                               }
                                           });
            }
        }
    }

    /** Updates the enabled status of the fields */
    private void updateEnabled() {
        // always edit value
        textValue.setEditable(theTable.isEditing());
        textValue.setEnabled(theTable.isEditing());
        // sometimes edit the comment
        if (theTable.isEditing()) {
            PropertiesTableModel.StringPair sp =
                (PropertiesTableModel.StringPair)theTable.getCellEditor().getCellEditorValue();
            textComment.setEditable(sp.isCommentEditable());
            textComment.setEnabled(sp.isCommentEditable());
        }
        else {
            textComment.setEditable(false);
            textComment.setEnabled(false);
        }
    }

    /** Returns the main table with all values */
    public JTable getTable() {
        return theTable;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        theTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        commentLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textComment = new javax.swing.JTextArea();
        valueLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textValue = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        jPanel1.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        
          theTable.setModel(new javax.swing.table.DefaultTableModel (
            new Object [][] {
                
            },
            new String [] {
                
            }
            ));
            theTable.setCellSelectionEnabled(true);
            scrollPane.setViewportView(theTable);
            
            gridBagConstraints2 = new java.awt.GridBagConstraints();
          gridBagConstraints2.gridwidth = 0;
          gridBagConstraints2.gridheight = 0;
          gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints2.insets = new java.awt.Insets(8, 8, 8, 8);
          gridBagConstraints2.weightx = 1.0;
          gridBagConstraints2.weighty = 1.0;
          jPanel1.add(scrollPane, gridBagConstraints2);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.gridheight = -1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(jPanel1, gridBagConstraints1);
        
        
        jPanel2.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        
        commentLabel.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_CommentLabel"));
          gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.insets = new java.awt.Insets(0, 8, 0, 8);
          gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
          jPanel2.add(commentLabel, gridBagConstraints3);
          
          
        
          textComment.setLineWrap(true);
            textComment.setRows(2);
            textComment.setEditable(false);
            textComment.setEnabled(false);
            jScrollPane2.setViewportView(textComment);
            
            gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.gridwidth = 0;
          gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints3.insets = new java.awt.Insets(0, 0, 8, 0);
          gridBagConstraints3.weightx = 1.0;
          gridBagConstraints3.weighty = 1.0;
          jPanel2.add(jScrollPane2, gridBagConstraints3);
          
          
        valueLabel.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_ValueLabel"));
          gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints3.insets = new java.awt.Insets(0, 8, 0, 8);
          gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
          jPanel2.add(valueLabel, gridBagConstraints3);
          
          
        
          textValue.setLineWrap(true);
            textValue.setRows(2);
            textValue.setEditable(false);
            textValue.setEnabled(false);
            jScrollPane3.setViewportView(textValue);
            
            gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.gridwidth = 0;
          gridBagConstraints3.gridheight = 0;
          gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints3.insets = new java.awt.Insets(0, 0, 8, 0);
          gridBagConstraints3.weightx = 1.0;
          gridBagConstraints3.weighty = 1.0;
          jPanel2.add(jScrollPane3, gridBagConstraints3);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = -1;
        gridBagConstraints1.gridheight = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.3;
        add(jPanel2, gridBagConstraints1);
        
        
        jPanel3.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints4;
        
        addButton.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_AddPropertyButton"));
          addButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  addButtonActionPerformed(evt);
              }
          }
          );
          gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridwidth = 0;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints4.insets = new java.awt.Insets(0, 8, 0, 8);
          jPanel3.add(addButton, gridBagConstraints4);
          
          
        removeButton.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_RemovePropertyButton"));
          removeButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  removeButtonActionPerformed(evt);
              }
          }
          );
          gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridwidth = 0;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints4.insets = new java.awt.Insets(8, 8, 8, 8);
          jPanel3.add(removeButton, gridBagConstraints4);
          
          
        gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridwidth = 0;
          gridBagConstraints4.gridheight = 0;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints4.weightx = 1.0;
          gridBagConstraints4.weighty = 1.0;
          jPanel3.add(jPanel4, gridBagConstraints4);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 0, 0, 0);
        add(jPanel3, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopEditing();
        String key = ((PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(), 0)).getValue();
        
        // dont't remove elemnt with key == null ( this is only case -> when there is an empty file with comment only)
        if(key == null) return; 
        
        NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(
                                                java.text.MessageFormat.format(
                                                    NbBundle.getBundle(BundleEditPanel.class).getString("MSG_DeleteKeyQuestion"),
                                                    new Object[] { key }),
                                                NotifyDescriptor.OK_CANCEL_OPTION);
                                                    
        if (TopManager.getDefault().notify(msg).equals(NotifyDescriptor.OK_OPTION)) {
            try {
                // starts "atomic" acion for special undo redo manager of opend support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = new Object();

                for (int i=0; i < ((PropertiesDataObject)dobj).getBundleStructure().getEntryCount(); i++) {
                    PropertiesFileEntry entry = ((PropertiesDataObject)dobj).getBundleStructure().getNthEntry(i);
                    if (entry != null) {
                        PropertiesStructure ps = entry.getHandler().getStructure();
                        if (ps != null) {
                            ps.deleteItem(key);
                        }
                    }
                }
            } finally {
                // finishes "atomic" undo redo action for special undo redo manager of open support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = null;
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        stopEditing();
        DialogDescriptor.InputLine descr = new DialogDescriptor.InputLine(
                                               NbBundle.getBundle(BundleEditPanel.class).getString ("CTL_PropertyKey"),
                                               NbBundle.getBundle(BundleEditPanel.class).getString("CTL_NewPropertyTitle"));

        boolean okPressed = TopManager.getDefault ().notify (descr).equals (NotifyDescriptor.OK_OPTION);

        if (okPressed) {
            try {
                // starts "atomic" acion for special undo redo manager of opend support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = new Object();

                String key = UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(descr.getInputText()));
                // add key to all entries
                for (int i=0; i < ((PropertiesDataObject)dobj).getBundleStructure().getEntryCount(); i++) {            
                    PropertiesFileEntry entry = ((PropertiesDataObject)dobj).getBundleStructure().getNthEntry(i);
                    if (!entry.getHandler().getStructure().addItem(key, "", "")) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                                           java.text.MessageFormat.format(
                                                               NbBundle.getBundle(BundleEditPanel.class).getString("MSG_KeyExists"),
                                                               new Object[] {UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(descr.getInputText()))}),
                                                           NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(msg);
                    }
                }
            } finally {
                // finishes "atomic" undo redo action for special undo redo manager of open support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = null;
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable theTable;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea textComment;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea textValue;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables

}
