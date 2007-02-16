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


package org.netbeans.modules.properties;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.BorderUIResource.BevelBorderUIResource;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.*;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;


/**
 * Panel which shows bundle of .properties files encapsulated by <code>PropertiesDataObject</code> in one table view.
 *
 * @author  Petr Jiricka
 */
public class BundleEditPanel extends JPanel implements PropertyChangeListener {
    
    /** PropertiesDataObject this panel presents. */
    private PropertiesDataObject obj;
    
    /** Document listener for value and comment textareas. */
    private DocumentListener listener;
    
    /** Class representing settings used in table view. */
    private static TableViewSettings settings;
    
    /** Generated serialized version UID. */
    static final long serialVersionUID =-843810329041244483L;
    
    
    /** Creates new form BundleEditPanel */
    public BundleEditPanel(final PropertiesDataObject obj, PropertiesTableModel propTableModel) {
        this.obj = obj;
        
        initComponents();
        initAccessibility();
        initSettings();
        
        // Sets table column model.
        table.setColumnModel(new TableViewColumnModel());
        
        // Sets table model.
        table.setModel(propTableModel);
        
        // Sets table cell editor.
        JTextField textField = new JTextField();
        // Force the document to accept newlines. The textField doesn't like
        // it, but the same document is used by the <code>textValue</code> text
        // area that must accept newlines.
        textField.getDocument().putProperty("filterNewlines",  Boolean.FALSE); // NOI18N
        textField.setBorder(new LineBorder(Color.black));
        textField.getAccessibleContext().setAccessibleName(NbBundle.getBundle(BundleEditPanel.class).getString("ACSN_CellEditor"));
        textField.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACSD_CellEditor"));
        listener = new ModifiedListener();
        table.setDefaultEditor(PropertiesTableModel.StringPair.class,
            new PropertiesTableCellEditor(textField, textComment, textValue, valueLabel, listener));
        
        // Sets renderer.
        table.setDefaultRenderer(PropertiesTableModel.StringPair.class, new TableViewRenderer());
        
        updateAddButton();
        
        // property change listener - listens to editing state of the table
        table.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("tableCellEditor")) { // NOI18N
                    updateEnabled();
                } else if (evt.getPropertyName().equals("model")) { // NOI18N
                    updateAddButton();
                }
            }
        });
        
        // listens on clikcs on table header, detects column and sort accordingly to chosen one
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel colModel = table.getColumnModel();
                int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
                // No column was clicked.
                if(columnModelIndex < 0)
                    return;
                int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
                // not detected column
                if (modelIndex < 0)
                    return;
                obj.getBundleStructure().sort(modelIndex);
            }
        });
        
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateSelection();
                    }
                });
            }
        });
        
    } // End of constructor.
    
    
    /** Stops editing if editing is in run. */
    protected void stopEditing() {
        if (!table.isEditing()) return;
        TableCellEditor cellEdit = table.getCellEditor();
        if (cellEdit != null)
            cellEdit.stopCellEditing();
    }
    
    /** Updates the enabled status of the fields */
    private void updateEnabled() {
        // always edit value
        textValue.setEditable(table.isEditing());
        textValue.setEnabled(table.isEditing());
        // sometimes edit the comment
        if (table.isEditing()) {
            PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)table.getCellEditor().getCellEditorValue();
            textComment.setEditable(sp.isCommentEditable());
            textComment.setEnabled(sp.isCommentEditable());
        } else {
            textComment.setEditable(false);
            textComment.setEnabled(false);
        }
    }
    
    private void updateSelection() {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        BundleStructure structure = obj.getBundleStructure();
        removeButton.setEnabled((row >= 0) && (!structure.isReadOnly()));
        String value;
        String comment;
        if (column == -1) {
            value = ""; // NOI18N
            comment = ""; // NOI18N
        } else if (column == 0) {
            Element.ItemElem elem = structure.getItem(0, row);
            value = structure.keyAt(row);
            comment = (elem != null) ? elem.getComment() : "";          //NOI18N
        } else {
            Element.ItemElem elem = structure.getItem(column-1, row);
            if (elem != null) {
                value = elem.getValue();
                comment = elem.getComment();
            } else {
                value = "";                                             //NOI18N
                comment = "";                                           //NOI18N
            }
        }
        textValue.getDocument().removeDocumentListener(listener);
        textComment.getDocument().removeDocumentListener(listener);
        textValue.setText(value);
        textComment.setText(comment);
        textValue.getDocument().addDocumentListener(listener);
        textComment.getDocument().addDocumentListener(listener);
    }
    
    private void updateAddButton() {
        addButton.setEnabled(!obj.getBundleStructure().isReadOnly());
    }
    
    /** Returns the main table with all values */
    public JTable getTable() {
        return table;
    }
    
    
    /** Initializes <code>settings</code> variable. */
    private void initSettings() {
        settings = TableViewSettings.getDefault();
                    
        // Listen on changes of setting settings.
        settings.addPropertyChangeListener(
            WeakListeners.propertyChange(this, settings)
        );
    }

    /**
     * Handler of settings changes
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // settings changed 
        BundleEditPanel.this.repaint();
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACS_BundleEditPanel"));
        
        table.getAccessibleContext().setAccessibleName(NbBundle.getBundle(BundleEditPanel.class).getString("ACSN_CTL_Table"));
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACSD_CTL_Table"));
        textValue.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACS_CTL_TEXTVALUE"));
        addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACS_LBL_AddPropertyButton"));
        textComment.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACS_CTL_TEXTCOMMENT"));
        autoResizeCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACS_CTL_AutoResize"));
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BundleEditPanel.class).getString("ACS_LBL_RemovePropertyButton"));
    }
    
    public boolean requestFocusInWindow() {
        return table.requestFocusInWindow();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tablePanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        table = new BundleTable();
        valuePanel = new javax.swing.JPanel();
        commentLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textComment = new javax.swing.JTextArea();
        valueLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textValue = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        autoResizeCheck = new javax.swing.JCheckBox();

        setFocusCycleRoot(true);
        setLayout(new java.awt.GridBagLayout());

        tablePanel.setLayout(new java.awt.GridBagLayout());

        table.setCellSelectionEnabled(true);
        scrollPane.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        tablePanel.add(scrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tablePanel, gridBagConstraints);

        valuePanel.setLayout(new java.awt.GridBagLayout());

        commentLabel.setLabelFor(textComment);
        org.openide.awt.Mnemonics.setLocalizedText(commentLabel, NbBundle.getBundle(BundleEditPanel.class).getString("LBL_CommentLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        valuePanel.add(commentLabel, gridBagConstraints);

        textComment.setEditable(false);
        textComment.setLineWrap(true);
        textComment.setRows(3);
        textComment.setEnabled(false);
        jScrollPane2.setViewportView(textComment);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        valuePanel.add(jScrollPane2, gridBagConstraints);

        valueLabel.setLabelFor(textValue);
        org.openide.awt.Mnemonics.setLocalizedText(valueLabel, NbBundle.getBundle(BundleEditPanel.class).getString("LBL_ValueLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 0);
        valuePanel.add(valueLabel, gridBagConstraints);

        textValue.setEditable(false);
        textValue.setLineWrap(true);
        textValue.setRows(3);
        textValue.setEnabled(false);
        jScrollPane3.setViewportView(textValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 11, 11, 0);
        valuePanel.add(jScrollPane3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(valuePanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, NbBundle.getBundle(BundleEditPanel.class).getString("LBL_AddPropertyButton")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        buttonPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getBundle(BundleEditPanel.class).getString("LBL_RemovePropertyButton")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 11, 11);
        buttonPanel.add(removeButton, gridBagConstraints);

        autoResizeCheck.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoResizeCheck, NbBundle.getBundle(BundleEditPanel.class).getString("CTL_AutoResize")); // NOI18N
        autoResizeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoResizeCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        buttonPanel.add(autoResizeCheck, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void autoResizeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoResizeCheckActionPerformed
        if(autoResizeCheck.isSelected())
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        else
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }//GEN-LAST:event_autoResizeCheckActionPerformed
    
    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int selectedRow = table.getSelectedRow();
        
        if(selectedRow == -1)
            return;
        
        stopEditing();
        String key = ((PropertiesTableModel.StringPair)table.getModel().getValueAt(selectedRow, 0)).getValue();
        
        // Don't remove elemnt with key == null ( this is only case -> when there is an empty file with comment only)
        if(key == null)
            return;
        
        NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(
        MessageFormat.format(
        NbBundle.getBundle(BundleEditPanel.class).getString("MSG_DeleteKeyQuestion"),
        new Object[] { key }
        ),
        NotifyDescriptor.OK_CANCEL_OPTION
        );
        
        if (DialogDisplayer.getDefault().notify(msg).equals(NotifyDescriptor.OK_OPTION)) {
            try {
                // Starts "atomic" acion for special undo redo manager of open support.
                obj.getOpenSupport().atomicUndoRedoFlag = new Object();
                
                for (int i=0; i < obj.getBundleStructure().getEntryCount(); i++) {
                    PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(i);
                    if (entry != null) {
                        PropertiesStructure ps = entry.getHandler().getStructure();
                        if (ps != null) {
                            ps.deleteItem(key);
                        }
                    }
                }
            } finally {
                // finishes "atomic" undo redo action for special undo redo manager of open support
                obj.getOpenSupport().atomicUndoRedoFlag = null;
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        stopEditing();
        
        final Dialog[] dialog = new Dialog[1];
        final Element.ItemElem item = new Element.ItemElem(
        null,
        new Element.KeyElem(null, ""), // NOI18N
        new Element.ValueElem(null, ""), // NOI18N
        new Element.CommentElem(null, "") // NOI18N
        );
        final JPanel panel = new PropertyPanel(item);
        
        DialogDescriptor dd = new DialogDescriptor(
        panel,
        NbBundle.getBundle(BundleEditPanel.class).getString("CTL_NewPropertyTitle"),
        true,
        DialogDescriptor.OK_CANCEL_OPTION,
        DialogDescriptor.OK_OPTION,
        new ActionListener() {
            public void actionPerformed(ActionEvent evt2) {
                // OK pressed
                if(evt2.getSource() == DialogDescriptor.OK_OPTION) {
                    dialog[0].setVisible(false);
                    dialog[0].dispose();
                    
                    final String key = item.getKey();
                    String value = item.getValue();
                    String comment = item.getComment();
                    
                    boolean keyAdded = false;
                    
                    try {
                        // Starts "atomic" acion for special undo redo manager of open support.
                        obj.getOpenSupport().atomicUndoRedoFlag = new Object();
                        
                        // add key to all entries
                        for (int i=0; i < obj.getBundleStructure().getEntryCount(); i++) {
                            PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(i);
                            
                            if (entry != null && !entry.getHandler().getStructure().addItem(key, value, comment)) {
                                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                MessageFormat.format(
                                NbBundle.getBundle(BundleEditPanel.class).getString("MSG_KeyExists"),
                                new Object[] {
                                    item.getKey(),
                                    Util.getLocaleLabel(entry)
                                }
                                ),
                                NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(msg);
                            } else {
                                keyAdded = true;
                            }
                        }
                    } finally {
                        // Finishes "atomic" undo redo action for special undo redo manager of open support.
                        obj.getOpenSupport().atomicUndoRedoFlag = null;
                    }
                    
                    if(keyAdded) {
                        // Item was added succesfully, go to edit it.
                        // PENDING: this is in request processor queue only
                        // due to reason that properties structure has just after
                        // adding new item inconsistence gap until it's reparsed anew.
                        // This should be removed when the parsing will be redsigned.
                        PropertiesRequestProcessor.getInstance().post(new Runnable() {
                            public void run() {
                                // Find indexes.
                                int rowIndex = obj.getBundleStructure().getKeyIndexByName(key);
                                
                                if((rowIndex != -1)) {
                                    final int row = rowIndex;
                                    final int column = 1; // Default locale.
                                    
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            // Autoscroll to cell if possible and necessary.
                                            if(table.getAutoscrolls()) {
                                                Rectangle cellRect = table.getCellRect(row, column, false);
                                                if (cellRect != null) {
                                                    table.scrollRectToVisible(cellRect);
                                                }
                                            }
                                            
                                            // Update selection & edit.
                                            table.getColumnModel().getSelectionModel().setSelectionInterval(column, column);
                                            table.getSelectionModel().setSelectionInterval(row, row);
                                            
                                            table.requestFocusInWindow();
                                            table.editCellAt(row, column);
                                        }
                                    });
                                }
                            }
                        });
                    }
                    
                    // Cancel pressed
                } else if (evt2.getSource() == DialogDescriptor.CANCEL_OPTION) {
                    dialog[0].setVisible(false);
                    dialog[0].dispose();
                }
            }
        }
        );
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialog[0].setVisible(true);
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox autoResizeCheck;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTextArea textComment;
    private javax.swing.JTextArea textValue;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JPanel valuePanel;
    // End of variables declaration//GEN-END:variables
    
    
    /** Header renderer used in table view. */
    private class TableViewHeaderRenderer extends DefaultTableCellRenderer {
        /** Sorted column. */
        private int column;
        
        /** Overrides superclass method. */
        public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
            
            this.column = column;
            
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    this.setForeground(header.getForeground());
                    this.setBackground(header.getBackground());
                    this.setFont(header.getFont());
                }
            }
            
            setText((value == null) ? "" : value.toString()); // NOI18N
            this.setBorder(UIManager.getBorder("TableHeader.cellBorder")); // NOI18N
            return this;
        }
        
        /** Overrides superclass method. Adds painting ascending/descending marks for sorted column header. */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            int sortIndex = table.convertColumnIndexToView(obj.getBundleStructure().getSortIndex());
            
            // If the column is the sorted one draw mark on its header.
            if(column == sortIndex ) {
                
                Color oldColor = g.getColor();
                
                FontMetrics fm = g.getFontMetrics();
                Rectangle space = fm.getStringBounds(" ", g).getBounds(); // NOI18N
                Rectangle mark = fm.getStringBounds("\u25B2", g).getBounds(); // NOI18N
                Rectangle bounds = this.getBounds();
                
                Insets insets = this.getInsets();
                
                BevelBorderUIResource bevelUI = (BevelBorderUIResource)BorderUIResource.getLoweredBevelBorderUIResource();
                
                boolean ascending = obj.getBundleStructure().getSortOrder();
                
                int x1, x2, x3, y1, y2, y3;
                
                if(ascending) {
                    // Ascending order.
                    x1 = space.width + mark.width/2;
                    x2 = space.width;
                    x3 = space.width + mark.width;
                    
                    y1 = bounds.y + insets.top+2;
                    y2 = bounds.y + bounds.height - insets.bottom-2;
                    y3 = y2;
                } else {
                    // Descending order.
                    x1 = space.width;
                    x2 = space.width + mark.width;
                    x3 = space.width + mark.width/2;
                    
                    y1 = bounds.y + insets.top + 2;
                    y2 = y1;
                    y3 = bounds.y + bounds.height - insets.bottom - 2;
                }
                
                // Draw bevel border.
                // Draw shadow outer color.
                g.setColor(bevelUI.getShadowOuterColor(this));
                if(ascending)
                    g.drawLine(x1, y1, x2, y2);
                else
                    g.drawPolyline(new int[] {x2, x1, x3}, new int[] {y2, y1, y3}, 3);
                    
                    // Draw shadow inner color.
                    g.setColor(bevelUI.getShadowInnerColor(this));
                    if(ascending)
                        g.drawLine(x1, y1+1, x2+1, y2-1);
                    else
                        g.drawPolyline(new int[] {x2-1, x1+1, x3}, new int[] {y2+1, y1+1, y3-1}, 3);
                        
                        // Draw highlihght outer color.
                        g.setColor(bevelUI.getHighlightOuterColor(this));
                        if(ascending)
                            g.drawPolyline(new int[] {x1, x3, x2}, new int[] {y1, y3, y2}, 3);
                        else
                            g.drawLine(x2, y2, x3, y3);
                        
                        // Draw highlight inner color.
                        g.setColor(bevelUI.getHighlightInnerColor(this));
                        if(ascending)
                            g.drawPolyline(new int[] {x1, x3-1, x2+1}, new int[] {y1+1, y3-1, y2-1}, 3);
                        else
                            g.drawLine(x2-1, y2+1, x3, y3-1);
                        
                        g.setColor(oldColor);
            }
        }
    } // End of inner class TableViewHeaderRenderer.
    
    
    /**
     * This subclass of Default column model is provided due correct set of column widths,
     * see the JTable and horizontal scrolling problem in Java Discussion Forum.
     */
    private class TableViewColumnModel extends DefaultTableColumnModel {
        /** Helper listener. */
        private AncestorListener ancestorListener;
        
        /** Table header rendrer. */
        private final TableCellRenderer headerRenderer = new TableViewHeaderRenderer();
        
        /** Overrides superclass method. */
        public void addColumn(TableColumn aColumn) {
            if (aColumn == null) {
                throw new IllegalArgumentException("Object is null"); // NOI18N
            }
            
            tableColumns.addElement(aColumn);
            aColumn.addPropertyChangeListener(this);
            
            // this method call is only difference with overriden superclass method
            adjustColumnWidths();
            
            // set header renderer this 'ugly' way (for each column),
            // in jdk1.2 is not possible to set default renderer
            // for JTableHeader like in jdk1.3
            aColumn.setHeaderRenderer(headerRenderer);
            
            // Post columnAdded event notification
            fireColumnAdded(new TableColumnModelEvent(this, 0,
            getColumnCount() - 1));
        }
        
        /** Helper method adjusting the table according top component or mode which contains it, the
         * minimal width of column is 1/10 of screen width. */
        private void adjustColumnWidths() {
            // The least initial width of column (1/10 of screen witdh).
            Rectangle screenBounds = org.openide.util.Utilities.getUsableScreenBounds();
            int columnWidth = screenBounds.width / 10;
            
            // Try to set widths according parent (viewport) width.
            int totalWidth = 0;
            TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, table);
            if(tc != null) {
                totalWidth = tc.getBounds().width;
            } else {
                if(ancestorListener == null) {
                    table.addAncestorListener(ancestorListener = new AncestorListener() {
                        /** If the ancestor is TopComponent adjustColumnWidths. */
                        public void ancestorAdded(AncestorEvent evt) {
                            if(evt.getAncestor() instanceof TopComponent) {
                                adjustColumnWidths();
                                table.removeAncestorListener(ancestorListener);
                                ancestorListener = null;
                            }
                        }
                        
                        /** Does nothing. */
                        public void ancestorMoved(AncestorEvent evt) {
                        }
                        
                        /** Does nothing. */
                        public void ancestorRemoved(AncestorEvent evt) {
                        }
                    });
                }
            }
            
            // Decrease of insets of scrollpane and insets set in layout manager.
            // Note: Layout constraints hardcoded instead of getting via method call ->
            // keep consistent with numbers in initComponents method.
            totalWidth -= scrollPane.getInsets().left + scrollPane.getInsets().right + 12 + 11;
            
            // Helper variable for keeping additional pixels which remains after division.
            int remainder = 0;
            
            // If calculations were succesful try to set the widths in case calculated width
            // for one column is not less than 1/10 of screen width.
            if(totalWidth > 0) {
                int computedColumnWidth = totalWidth / table.getColumnCount();
                if(computedColumnWidth > columnWidth) {
                    columnWidth = computedColumnWidth - table.getColumnModel().getColumnMargin();
                    remainder = totalWidth % table.getColumnCount();
                }
            }
            
            // Set the column widths.
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn column = table.getColumnModel().getColumn(i);
                
                // Add remainder to first column.
                if(i==0) {
                    // It is necessary to set both 'widths', see javax.swing.TableColumn.
                    column.setPreferredWidth(columnWidth + remainder);
                    column.setWidth(columnWidth + remainder);
                } else {
                    // It is necessary to set both 'widths', see javax.swing.TableColumn.
                    column.setPreferredWidth(columnWidth);
                    column.setWidth(columnWidth);
                }
            }
            
            // Recalculate total column width.
            recalcWidthCache();
            
            // Revalidate table so the widths will fit properly.
            table.revalidate();
            
            // Repaint header afterwards. Seems stupid but necessary.
            table.getTableHeader().repaint();
        }
    } // End of inner class TableViewColumnModel.
    
    
    /** Renderer which renders cells in table view. */
    private class TableViewRenderer extends DefaultTableCellRenderer {
        /** Overrides superclass method. */
        public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            if(value==null) return this;
            
            PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)value;                        
            
            setFont(settings.getFont());
            
            if(hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder") ); // NOI18N
            } else {
                setBorder(noFocusBorder);
            }
            
            String text = null;
            
            if(sp.getValue() != null) {
                text = sp.getValue();
            }
            
            // XXX Ugly hack to prevent problems showing 'html-ed' labels.
            if(BasicHTML.isHTMLString(text)) { // NOI18N
                text = " " + text; // NOI18N
            }
            
            setValue(text == null ? "" : text); // NOI18N
            
            // Set background color.
            if(sp.isKeyType())
                setBackground(settings.getKeyBackground());
            else {
                if( sp.getValue() != null)
                    setBackground(settings.getValueBackground());
                else
                    setBackground(settings.getShadowColor());
            }
            
            // Set foregound color.
            if(sp.isKeyType())
                setForeground(settings.getKeyColor());
            else
                setForeground(settings.getValueColor());
            
            // Optimization to avoid painting background if is the same like table's.
            Color back = getBackground();
            boolean colorMatch = (back != null) && (back.equals(table.getBackground()) ) && table.isOpaque();
            setOpaque(!colorMatch);
            
            return this;
        }
        
        /** Overrides superclass method. It adds the highlighting of search occurences in it. */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // If there is a highlihgt flag set do additional drawings.
            if(FindPerformer.getFindPerformer(BundleEditPanel.this.table).isHighlightSearch()) {
                String text = getText();
                String findString = FindPerformer.getFindPerformer(BundleEditPanel.this.table).getFindString();
                
                // If there is a findString and the cell could contain it go ahead.
                if(text != null && text.length()>0 && findString != null && findString.length()>0) {
                    int index = 0;
                    int width = (int)g.getFontMetrics().getStringBounds(findString, g).getWidth();
                    
                    Color oldColor = g.getColor();
                    // In each iteration highlight one occurence of findString in this cell.
                    while((index = text.indexOf(findString, index)) >= 0) {
                        
                        int x = (int)g.getFontMetrics().getStringBounds(text.substring(0, index), g).getWidth()+this.getInsets().left;
                        
                        g.setColor(settings.getHighlightBackground());
                        g.fillRect(x, 0, width, g.getClipBounds().height);
                        
                        g.setColor(settings.getHighlightColor());
                        g.drawString(findString, x, -(int)g.getFontMetrics().getStringBounds(findString, g).getY());
                        
                        index += findString.length();
                    }
                    // Reset original color.
                    g.setColor(oldColor);
                }
            }
        }
    } // End of inner class TableViewRenderer.
    
    
    
    /** <code>JTable</code> with one bug fix.
     * @see #removeEditorSilent */
    static class BundleTable extends JTable {
        
        public BundleTable(){
            super();
            this.setRowHeight(getCellFontHeight() + 1);
        }
        
        /**
         * The same like superclass removeEditor except it doesn't request focus back to table.
         * We need this kind of behaviour (see bug in IssueaZilla #9237). The table shoudl request focus
         * after canceling editing when is showing only (submit bug to jdk ?).
         * @see javax.swing.JTable#removeEditor */
        public void removeEditorSilent() {
            TableCellEditor editor = getCellEditor();
            if(editor != null) {
                editor.removeCellEditorListener(this);
                
                // requestFocus();
                if (editorComp != null) {
                    remove(editorComp);
                }
                
                Rectangle cellRect = getCellRect(editingRow, editingColumn, false);
                
                setCellEditor(null);
                setEditingColumn(-1);
                setEditingRow(-1);
                editorComp = null;
                
                repaint(cellRect);
            }
        }
        
        private int getCellFontHeight() {
            Font cellFont = UIManager.getFont("TextField.font");
            if (cellFont != null) {
                FontMetrics fm = getFontMetrics(cellFont);
                if (fm != null) {
                    return fm.getHeight();
                }
            }
            return 14;
        }        
        
    } // End of BundleTable class.
    
    private class ModifiedListener implements DocumentListener {
        
        public void changedUpdate(DocumentEvent e) {
            documentModified();
        }
        
        public void insertUpdate(DocumentEvent e) {
            documentModified();
        }
        
        public void removeUpdate(DocumentEvent e) {
            documentModified();
        }
        
        private void documentModified() {
            obj.setModified(true);
        }
        
    }
    
}
