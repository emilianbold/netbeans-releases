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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


/**
 * Component for visual editing of Map of macroiations. When you enter new
 * macroiation with the already used macro, it will replace the existing one.
 * macroiations with empty expanded form are perfectly valid, but macroiations
 * with empty macro field are simply ignored.
 *
 * @author  David Konecny
 */

public class MacrosEditorPanel extends javax.swing.JPanel {

    PairStringModel model;

    // The master we talk to about changes in map
    private MacrosEditor editor;
    
    private FontSizeTable macrosTable;

    /** Creates new form MacrosEditorPanel */
    public MacrosEditorPanel(MacrosEditor editor) {
        this.editor = editor;
        model = new PairStringModel();
        initComponents ();

        macrosTable = new FontSizeTable();        
        macrosTable.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        macrosTable.setModel(model);
        macrosTable.setShowVerticalLines(false);
        macrosTable.setShowHorizontalLines(false);
        macrosTable.setSelectionMode( DefaultListSelectionModel.SINGLE_SELECTION );
        // Set the width of columns to 30% and 70%
        TableColumnModel col = macrosTable.getColumnModel();
        col.getColumn( 0 ).setMaxWidth( 3000 );
        col.getColumn( 0 ).setPreferredWidth( 30 );
        col.getColumn( 1 ).setMaxWidth( 7000 );
        col.getColumn( 1 ).setPreferredWidth( 70 );
        macrosPane.setViewportView(macrosTable);
        
        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP")); // NOI18N
        macrosTable.getAccessibleContext().setAccessibleName(getBundleString("ACSN_MEP_Table")); // NOI18N
        macrosTable.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Table")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Add")); // NOI18N
        editButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Edit")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Remove")); // NOI18N
        enableButtons(false);

        macrosTable.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { 
                SwingUtilities.getAncestorOfClass(Window.class, MacrosEditorPanel.this).setVisible(false);
            }},
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_FOCUSED
        );
            
        macrosTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                
                // XXX - hack because of bugparade's 4801274
                if (macrosTable.getRowCount() == 0){
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
        return NbBundle.getMessage(MacrosEditorPanel.class, s);
    }        
    
    /**
     * Fill in editor with initial values
     */
    public void setValue( Map m ) {
        HashMap hm;
        if (m != null)
            hm = new HashMap(m);
        else
            hm = new HashMap();
        if (hm.containsKey(null)) {
            hm.remove(null);
        }
        // Our model is the one and only holding data
        model.setData( new TreeMap( hm ) );
        // select first item, just to have something selected
        if( model.getRowCount() > 0 ) macrosTable.setRowSelectionInterval( 0, 0 );
    }

    /**
     * Take the result of users modifications
     */
    public Map getValue() {
        return model.getData();
    }

    /**
     * Tell the editor (and in round the system), that user've changed
     * macros mapping.
     */
    private void notifyEditor() {
        if( editor != null ) editor.customEditorChange();
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        macrosPane = new javax.swing.JScrollPane();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(macrosPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, getBundleString( "MEP_Add" ));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, getBundleString( "MEP_Edit" ));
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

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, getBundleString( "MEP_Remove" ));
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
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String[] macro = getMacro( null );
        // If user canceled entering, do noting
        if( macro == null ) return;
        int index = model.putPair( macro );  // can silently replace existing mapping
        macrosTable.setRowSelectionInterval( index, index );
        notifyEditor();
    }//GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int index = macrosTable.getSelectedRow();
        if( index != -1 ) {  // is something selected?
            String[] pair = model.getPair( index );
            pair = getMacro( pair );
            if( pair != null ) {
                model.removePair( index );
                index = model.putPair( pair );
                macrosTable.setRowSelectionInterval( index, index );
                notifyEditor();
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = macrosTable.getSelectedRow();
        if( index != -1 ) { // is something selected?
            model.removePair( index );
            if( index >= model.getRowCount() ) index--;
            if( index >= 0 ) macrosTable.setRowSelectionInterval( index, index );
            notifyEditor();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    /**
     * Creates a dialog asking user for pair of Strings.
     * @param macro value to be preset in dialog, or <CODE>null</CODE>
     * @return String[2] filled with {macro, expand}
     * or <CODE>null</CODE> if canceled.
     */
    private String[] getMacro( String[] macro ) {
        MacroInputPanel input = new MacroInputPanel();
        // set HELP_ID of parent 
        HelpCtx.setHelpIDString( input, (HelpCtx.findHelp(this) != null ? HelpCtx.findHelp(this).getHelpID() : null) );
        if( macro != null ) input.setMacro( macro ); // preset value

        DialogDescriptor dd = new DialogDescriptor ( input, getBundleString( "MEP_EnterMacro" ) ); // NOI18N
        Dialog dial = org.openide.DialogDisplayer.getDefault().createDialog(dd);
        input.requestFocus();  // Place caret in it, hopefully
        dial.setVisible(true); // let the user tell us their wish

        if( dd.getValue() == DialogDescriptor.OK_OPTION ) {
            String[] retVal = input.getMacro();
            if( ! "".equals( retVal[0] )  ) {// NOI18N don't allow empty macro
                int existingKeyPosition = model.containsKey(retVal[0]);
                
                if (existingKeyPosition >= 0){
                    // ignore if user edits value and doesn't change the key
                    if ( macro!=null && macro[0].equals(retVal[0]) ) return retVal;
                    
                    String[] existingPair = model.getPair(existingKeyPosition);
                    NotifyDescriptor NDConfirm = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(MacrosEditorPanel.class, "MEP_Overwrite", retVal[0] ),
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
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane macrosPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    /**
     * TableModel of sorted map of string pairs, provides additional functions
     * for setting, getting and modifying it's content.
     */
    private class PairStringModel extends javax.swing.table.AbstractTableModel {

        String[] columns = { getBundleString( "MEP_MacroTitle" ),     // NOI18N
                             getBundleString( "MEP_ExpandTitle" ) };   // NOI18N

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
