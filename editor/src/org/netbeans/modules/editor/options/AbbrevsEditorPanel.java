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

package org.netbeans.modules.editor.options;

import java.awt.Dialog;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.openide.*;
import org.openide.util.NbBundle;


/**
 * Component for visual editing of Map of abbreviations. When you enter new
 * abbreviation with the already used abbrev, it will replace the existing one.
 * abbreviations with empty expanded form are perfectly valid, but abbreviations
 * with empty abbrev field are simply ignored.
 *
 * @author  Petr Nejedly
 */

public class AbbrevsEditorPanel extends javax.swing.JPanel {

    private static ResourceBundle bundle = NbBundle.getBundle( AbbrevsEditorPanel.class );

    PairStringModel model;

    // The master we talk to about changes in map
    private AbbrevsEditor editor;

    /** Creates new form AbbrevsEditorPanel */
    public AbbrevsEditorPanel( AbbrevsEditor editor ) {
        this.editor = editor;
        model = new PairStringModel();
        initComponents ();
    }

    /**
     * Fill in editor with initial values
     */
    public void setValue( Map m ) {
        // Our model is the one and only holding data
        model.setData( new TreeMap( m ) );
        // select first item, just to have something selected
        if( model.getRowCount() > 0 ) abbrevsTable.setRowSelectionInterval( 0, 0 );
    }

    /**
     * Take the result of users modifications
     */
    public Map getValue() {
        return model.getData();
    }

    /**
     * Tell the editor (and in round the system), that user've changed
     * abbrevs mapping.
     */
    private void notifyEditor() {
        if( editor != null ) editor.customEditorChange();
    }


    private void initComponents () {//GEN-BEGIN:initComponents
        abbrevsPane = new javax.swing.JScrollPane ();
        abbrevsTable = new javax.swing.JTable ();
        addButton = new javax.swing.JButton ();
        editButton = new javax.swing.JButton ();
        removeButton = new javax.swing.JButton ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));


        abbrevsTable.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        abbrevsTable.setModel (model);
        abbrevsTable.setShowVerticalLines (false);
        abbrevsTable.setShowHorizontalLines (false);
        abbrevsTable.setSelectionMode( DefaultListSelectionModel.SINGLE_SELECTION );
        // Set the width of columns to 30% and 70%
        TableColumnModel col = abbrevsTable.getColumnModel();
        col.getColumn( 0 ).setMaxWidth( 3000 );
        col.getColumn( 0 ).setPreferredWidth( 30 );
        col.getColumn( 1 ).setMaxWidth( 7000 );
        col.getColumn( 1 ).setPreferredWidth( 70 );

        abbrevsPane.setViewportView (abbrevsTable);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridheight = 4;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (abbrevsPane, gridBagConstraints1);

        addButton.setText (bundle.getString( "AEP_Add" ));
        addButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             addButtonActionPerformed (evt);
                                         }
                                     }
                                    );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 5, 0);
        add (addButton, gridBagConstraints1);

        editButton.setText (bundle.getString( "AEP_Edit" ));
        editButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              editButtonActionPerformed (evt);
                                          }
                                      }
                                     );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 5, 0);
        add (editButton, gridBagConstraints1);

        removeButton.setText (bundle.getString( "AEP_Remove" ));
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add (removeButton, gridBagConstraints1);

    }//GEN-END:initComponents

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String[] abbrev = getAbbrev( null );
        // If user canceled entering, do noting
        if( abbrev == null ) return;
        int index = model.putPair( abbrev );  // can silently replace existing mapping
        abbrevsTable.setRowSelectionInterval( index, index );
        notifyEditor();
    }//GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int index = abbrevsTable.getSelectedRow();
        if( index != -1 ) {  // is something selected?
            String[] pair = model.getPair( index );
            pair = getAbbrev( pair );
            if( pair != null ) {
                model.removePair( index );
                index = model.putPair( pair );
                abbrevsTable.setRowSelectionInterval( index, index );
                notifyEditor();
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = abbrevsTable.getSelectedRow();
        if( index != -1 ) { // is something selected?
            model.removePair( index );
            if( index >= model.getRowCount() ) index--;
            if( index >= 0 ) abbrevsTable.setRowSelectionInterval( index, index );
            notifyEditor();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    /**
     * Creates a dialog asking user for pair of Strings.
     * @param abbrev value to be preset in dialog, or <CODE>null</CODE>
     * @return String[2] filled with {abbrev, expand}
     * or <CODE>null</CODE> if canceled.
     */
    private String[] getAbbrev( String[] abbrev ) {
        AbbrevInputPanel input = new AbbrevInputPanel();
        if( abbrev != null ) input.setAbbrev( abbrev ); // preset value

        DialogDescriptor dd = new DialogDescriptor ( input, bundle.getString( "AEP_EnterAbbrev" ) ); // NOI18N
        Dialog dial = TopManager.getDefault().createDialog(dd);
        input.requestFocus();  // Place caret in it, hopefully
        dial.show(); // let the user tell us their wish

        if( dd.getValue() == DialogDescriptor.OK_OPTION ) {
            String[] retVal = input.getAbbrev();
            if( ! "".equals( retVal[0] )  ) return retVal;  // NOI18N don't allow empty abbrev
        }
        return null; // cancel or empty
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane abbrevsPane;
    private javax.swing.JTable abbrevsTable;
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    /**
     * TableModel of sorted map of string pairs, provides additional functions
     * for setting, getting and modifying it's content.
     */
    private class PairStringModel extends javax.swing.table.AbstractTableModel {

        String[] columns = { bundle.getString( "AEP_AbbrevTitle" ),     // NOI18N
                             bundle.getString( "AEP_ExpandTitle" ) };   // NOI18N

        TreeMap data;
        String[] keys;

        public PairStringModel() {
            data = new TreeMap();
            keys = new String[0];
        }

        public void setData( TreeMap data ) {
            this.data = data;
            updateKeys();
        }

        private void updateKeys() {
            keys = (String[])data.keySet().toArray( new String[0] );
            fireTableDataChanged(); // we make general changes to table, invalidate whole
        }

        public TreeMap getData() {
            return data;
        }

        public int getRowCount() {
            return keys.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int column) {
            if( column == 0 ) return keys[row];
            else return data.get( keys[row] );
        }

        public int putPair( String[] pair ) {
            data.put( pair[0], pair[1] );
            updateKeys();
            return Arrays.binarySearch( keys, pair[0] );  // it should always find
        }

        public void removePair( int row ) {
            data.remove( getValueAt( row, 0 ) );
            updateKeys();
        }

        public String[] getPair( int row ) {
            String key = (String)getValueAt( row, 0 );
            String[] retVal = { key, (String)data.get( key ) };
            return retVal;
        }
    }

}