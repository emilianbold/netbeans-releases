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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.options;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Component for visual editing of Map of abbreviations. When you enter new
 * abbreviation with the already used abbrev, it will replace the existing one.
 * abbreviations with empty expanded form are perfectly valid, but abbreviations
 * with empty abbrev field are simply ignored.
 *
 * @author  Petr Nejedly
 * @deprecated Without any replacement.
 */

public class AbbrevsEditorPanel extends javax.swing.JPanel {

    PairStringModel model;

    // The master we talk to about changes in map
    private AbbrevsEditor editor;
    
    private FontSizeTable abbrevsTable;

    /** Creates new form AbbrevsEditorPanel */
    public AbbrevsEditorPanel( AbbrevsEditor editor ) {
        this.editor = editor;
        model = new PairStringModel();
        initComponents ();

        abbrevsTable = new FontSizeTable();
        abbrevsTable.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        abbrevsTable.setModel(model);
        abbrevsTable.setShowHorizontalLines(false);
        abbrevsTable.setShowVerticalLines(false);
        abbrevsTable.setSelectionMode( DefaultListSelectionModel.SINGLE_SELECTION );
        // Set the width of columns to 30% and 70%
        TableColumnModel col = abbrevsTable.getColumnModel();
        col.getColumn( 0 ).setMaxWidth( 3000 );
        col.getColumn( 0 ).setPreferredWidth( 30 );
        col.getColumn( 1 ).setMaxWidth( 7000 );
        col.getColumn( 1 ).setPreferredWidth( 70 );
        abbrevsPane.setViewportView(abbrevsTable);
        
        
        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AEP")); // NOI18N
        abbrevsTable.getAccessibleContext().setAccessibleName(getBundleString("ACSN_AEP_Table")); // NOI18N
        abbrevsTable.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AEP_Table")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AEP_Add")); // NOI18N
        editButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AEP_Edit")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_AEP_Remove")); // NOI18N
        enableButtons(false);
        
        abbrevsTable.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { 
                SwingUtilities.getAncestorOfClass(Window.class, AbbrevsEditorPanel.this).setVisible(false);
            }},
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_FOCUSED
        );
            
        abbrevsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                
                // XXX - hack because of bugparade's 4801274
                if (abbrevsTable.getRowCount() == 0){
                    enableButtons(false);
                    return;
                }
                
                // valid fix of #35096
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                enableButtons(!lsm.isSelectionEmpty());
            }
        });
            
    }

    private void enableButtons(boolean enable){
        editButton.setEnabled(enable);
        removeButton.setEnabled(enable);
    }
    
    private String getBundleString(String s) {
        return NbBundle.getMessage(AbbrevsEditorPanel.class, s);
    }        
    

    /**
     * Fill in editor with initial values
     */
    public void setValue( Map m ) {
        // Our model is the one and only holding data
        if (m != null)
            model.setData( new TreeMap( m ) );
        else
            model.setData( new TreeMap() );

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


    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        abbrevsPane = new javax.swing.JScrollPane();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 11, 11)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(abbrevsPane, gridBagConstraints);

        addButton.setMnemonic(getBundleString("AEP_Add_Mnemonic").charAt (0));
        addButton.setText(getBundleString( "AEP_Add" ));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(addButton, gridBagConstraints);

        editButton.setMnemonic(getBundleString("AEP_Edit_Mnemonic").charAt (0));
        editButton.setText(getBundleString( "AEP_Edit" ));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(editButton, gridBagConstraints);

        removeButton.setMnemonic(getBundleString("AEP_Remove_Mnemonic").charAt (0));
        removeButton.setText(getBundleString( "AEP_Remove" ));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(removeButton, gridBagConstraints);

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
        // set HELP_ID of parent 
        HelpCtx.setHelpIDString( input, (HelpCtx.findHelp(this) != null ? HelpCtx.findHelp(this).getHelpID() : null) );
        if( abbrev != null ) input.setAbbrev( abbrev ); // preset value

        DialogDescriptor dd = new DialogDescriptor ( input, getBundleString("AEP_EnterAbbrev" ) ); // NOI18N
        Dialog dial = org.openide.DialogDisplayer.getDefault().createDialog(dd);
        input.requestFocus();  // Place caret in it, hopefully
        dial.setVisible(true); // let the user tell us their wish

        if( dd.getValue() == DialogDescriptor.OK_OPTION ) {
            String[] retVal = input.getAbbrev();
            if( ! "".equals( retVal[0] )  ){ // NOI18N don't allow empty abbrev
                int existingKeyPosition = model.containsKey(retVal[0]);
                
                if (existingKeyPosition >= 0){
                    // ignore if user edits value and doesn't change the key
                    if ( abbrev!=null && abbrev[0].equals(retVal[0]) ) return retVal;
                    
                    String[] existingPair = model.getPair(existingKeyPosition);
                    NotifyDescriptor NDConfirm = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(AbbrevsEditorPanel.class, "AEP_Overwrite", new Object[] {retVal[0], existingPair[1], retVal[1]}),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
                    );
                    
                    org.openide.DialogDisplayer.getDefault().notify(NDConfirm);
                    if (NDConfirm.getValue()!=NDConfirm.YES_OPTION){
                        return null;
                    }
                }
                return retVal;
            }
        }
        return null; // cancel or empty
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane abbrevsPane;
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    /**
     * TableModel of sorted map of string pairs, provides additional functions
     * for setting, getting and modifying it's content.
     */
    private class PairStringModel extends javax.swing.table.AbstractTableModel {

        String[] columns = { getBundleString( "AEP_AbbrevTitle" ),     // NOI18N
                             getBundleString( "AEP_ExpandTitle" ) };   // NOI18N

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
        
        public int containsKey( String key ){
            return Arrays.binarySearch( keys, key );
        }
    }

    
     private final class FontSizeTable extends JTable{
 
         private boolean needCalcRowHeight = true; 
         
         public FontSizeTable () {}
         
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
             int rowHeight = fm.getHeight();
             needCalcRowHeight = false;
             setRowHeight(rowHeight);
         }
         
     }
    
}
