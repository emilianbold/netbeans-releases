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

import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.NbBundle;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class BundleEditPanel extends javax.swing.JPanel {

    private static final int DEFAULT_TABLE_WIDTH = 600;
    private static final int DEFAULT_KEY_WIDTH   = 150;

    private DataObject dobj;
    private PropertiesTableModel ptm;

    private ListSelectionModel rowSelections;
    private ListSelectionModel columnSelections;

    static final long serialVersionUID =-843810329041244483L;
    /** Creates new form BundleEditPanel */
    public BundleEditPanel(final DataObject obj, PropertiesTableModel ptm) {
        this.dobj = obj;
        this.ptm = ptm;

        initComponents ();

        theTable.setModel(ptm);

        // table cell editor
        JTextField textField = new JTextField();
        textField.setBorder(new LineBorder(Color.black));
        theTable.setDefaultEditor(PropertiesTableModel.StringPair.class,
                                  new PropertiesTableCellEditor(textField, textComment, textValue));

        // set the column widths
        TableColumn column = null;
        for (int i = 0; i < theTable.getColumnModel().getColumnCount(); i++) {
            column = theTable.getColumnModel().getColumn(i);
            if (i == 0)
                column.setPreferredWidth(DEFAULT_KEY_WIDTH - 20);
            else
                column.setPreferredWidth(((int)theTable.getPreferredScrollableViewportSize().getWidth() -
                                          DEFAULT_KEY_WIDTH - scrollPane.getInsets().left - scrollPane.getInsets().right) /
                                         (theTable.getColumnModel().getColumnCount() - 1));
        }

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
    private void initComponents () {//GEN-BEGIN:initComponents
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        jPanel1 = new javax.swing.JPanel ();
        jPanel1.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;

        scrollPane = new javax.swing.JScrollPane ();

        theTable = new javax.swing.JTable ();
        theTable.setPreferredScrollableViewportSize (new java.awt.Dimension(600, 300));
        theTable.setModel (new javax.swing.table.DefaultTableModel (
                               new Object [][] {

                               },
                               new String [] {

                               }
                           ));
        theTable.setAutoResizeMode (javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        theTable.setCellSelectionEnabled (true);

        scrollPane.setViewportView (theTable);

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.gridheight = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets (8, 8, 8, 8);
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        jPanel1.add (scrollPane, gridBagConstraints2);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.gridheight = -1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (jPanel1, gridBagConstraints1);

        jPanel2 = new javax.swing.JPanel ();
        jPanel2.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;

        commentLabel = new javax.swing.JLabel ();
        commentLabel.setText (java.util.ResourceBundle.getBundle("org/netbeans/modules/properties/Bundle").getString("LBL_CommentLabel"));

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (commentLabel, gridBagConstraints3);

        jScrollPane2 = new javax.swing.JScrollPane ();

        textComment = new javax.swing.JTextArea ();
        textComment.setLineWrap (true);
        textComment.setRows (2);
        textComment.setEditable (false);
        textComment.setEnabled (false);

        jScrollPane2.setViewportView (textComment);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets (0, 0, 8, 0);
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        jPanel2.add (jScrollPane2, gridBagConstraints3);

        valueLabel = new javax.swing.JLabel ();
        valueLabel.setText (java.util.ResourceBundle.getBundle("org/netbeans/modules/properties/Bundle").getString("LBL_ValueLabel"));

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (valueLabel, gridBagConstraints3);

        jScrollPane3 = new javax.swing.JScrollPane ();

        textValue = new javax.swing.JTextArea ();
        textValue.setLineWrap (true);
        textValue.setRows (2);
        textValue.setEditable (false);
        textValue.setEnabled (false);

        jScrollPane3.setViewportView (textValue);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.gridheight = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets (0, 0, 8, 0);
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        jPanel2.add (jScrollPane3, gridBagConstraints3);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = -1;
        gridBagConstraints1.gridheight = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.3;
        add (jPanel2, gridBagConstraints1);

        jPanel3 = new javax.swing.JPanel ();
        jPanel3.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints4;

        addButton = new javax.swing.JButton ();
        addButton.setText (java.util.ResourceBundle.getBundle("org/netbeans/modules/properties/Bundle").getString("LBL_AddPropertyButton"));
        addButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             addButtonActionPerformed (evt);
                                         }
                                     }
                                    );

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.insets = new java.awt.Insets (0, 8, 0, 8);
        jPanel3.add (addButton, gridBagConstraints4);

        removeButton = new javax.swing.JButton ();
        removeButton.setText (java.util.ResourceBundle.getBundle("org/netbeans/modules/properties/Bundle").getString("LBL_RemovePropertyButton"));
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.insets = new java.awt.Insets (8, 8, 8, 8);
        jPanel3.add (removeButton, gridBagConstraints4);

        jPanel4 = new javax.swing.JPanel ();

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.gridheight = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.weighty = 1.0;
        jPanel3.add (jPanel4, gridBagConstraints4);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        add (jPanel3, gridBagConstraints1);

    }//GEN-END:initComponents

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopEditing();
        PropertiesTableModel.StringPair sp =
            (PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(), 0);
        NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(
                                                java.text.MessageFormat.format(
                                                    NbBundle.getBundle(BundleEditPanel.class).getString("MSG_DeleteKeyQuestion"),
                                                    new Object[] {sp.getValue()}),
                                                NotifyDescriptor.OK_CANCEL_OPTION);
        if (TopManager.getDefault().notify(msg).equals(NotifyDescriptor.OK_OPTION)) {
            for (int i=0; i < ((PropertiesDataObject)dobj).getBundleStructure().getEntryCount(); i++) {
                PropertiesFileEntry entry = ((PropertiesDataObject)dobj).getBundleStructure().getNthEntry(i);
                if (entry != null) {
                    PropertiesStructure ps = entry.getHandler().getStructure();
                    if (ps != null) {
                        ps.deleteItem(sp.getValue());
                    }
                }
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        stopEditing();
        DialogDescriptor.InputLine descr = new DialogDescriptor.InputLine(
                                               NbBundle.getBundle(BundleEditPanel.class).getString ("CTL_PropertyKey"),
                                               NbBundle.getBundle(BundleEditPanel.class).getString("CTL_NewPropertyTitle"));

        boolean okPressed = TopManager.getDefault ().notify (descr).equals (NotifyDescriptor.OK_OPTION);
        if (okPressed && (descr.getInputText().trim().length() == 0)) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                               NbBundle.getBundle(NewPropertyDialog.class).getString ("ERR_PropertyEmpty"),
                                               NotifyDescriptor.ERROR_MESSAGE));
            return;
        }

        if (okPressed) {
            if (((PropertiesFileEntry)((MultiDataObject)dobj).getPrimaryEntry()).
                    getHandler().getStructure().addItem(
                        descr.getInputText(), "", ""))
                ;
            else {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                                   java.text.MessageFormat.format(
                                                       NbBundle.getBundle(BundleEditPanel.class).getString("MSG_KeyExists"),
                                                       new Object[] {descr.getInputText()}),
                                                   NotifyDescriptor.ERROR_MESSAGE);
                TopManager.getDefault().notify(msg);
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