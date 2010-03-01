/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.tasklist.todo.settings;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class ToDoCustomizer extends javax.swing.JPanel {
    
    private boolean changed = false;
    private boolean isUpdating = false;
    
    /** Creates new form ToDoCustomizer */
    public ToDoCustomizer() {
        initComponents();
        table.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                enableButtons();
            }
        });
        jScrollPane1.getViewport().setOpaque( false );
        enableButtons();
    }
    
    private void enableButtons() {
        int selIndex = table.getSelectedRow();
        btnChange.setEnabled( selIndex >= 0 );
        btnRemove.setEnabled( selIndex >= 0 );
    }
    
    void update() {
        isUpdating = true;
        Collection<String> patterns = Settings.getDefault().getPatterns();
        table.setModel( createModel( patterns ) );
        table.setTableHeader( null );
        checkScanCommentsOnly.setSelected( Settings.getDefault().isScanCommentsOnly() );
        changed = false;
        isUpdating = false;
    }
    
    void applyChanges() {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        ArrayList<String> patterns = new ArrayList<String>( model.getRowCount() );
        for( int i=0; i<model.getRowCount(); i++ ) {
            patterns.add( model.getValueAt(i, 0).toString() );
        }
        Settings.getDefault().setPatterns( patterns );
        Settings.getDefault().setScanCommentsOnly( checkScanCommentsOnly.isSelected() );
    }
    
    boolean isDataValid() {
        return table.getRowCount() > 0;
    }
    
    boolean isChanged() {
        return changed;
    }
    
    private DefaultTableModel createModel( Collection<String> patterns ) {
        DefaultTableModel model = new DefaultTableModel( 
                new Object[] { NbBundle.getMessage( ToDoCustomizer.class, "ToDoCustomizer.TableHeader" ) }, patterns.size() ); //NOI18N
        int row = 0;
        for( String p : patterns ) {
            model.setValueAt( p, row++, 0 );
        }
        return model;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnAdd = new javax.swing.JButton();
        btnChange = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new MyTable();
        checkScanCommentsOnly = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        btnRemove = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnChange, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnChange.text")); // NOI18N
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        jScrollPane1.setOpaque(false);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table.setOpaque(false);
        table.setTableHeader(null);
        jScrollPane1.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(checkScanCommentsOnly, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.checkScanCommentsOnly.text")); // NOI18N
        checkScanCommentsOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkScanCommentsOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkScanCommentsOnly.setOpaque(false);
        checkScanCommentsOnly.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scanCommentsOnlyChanged(evt);
            }
        });

        jLabel1.setLabelFor(table);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.TableHeader")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ToDoCustomizer.class, "ToDoCustomizer.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnChange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 81, Short.MAX_VALUE)))
            .addComponent(jLabel1)
            .addComponent(checkScanCommentsOnly)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkScanCommentsOnly))
        );
    }// </editor-fold>//GEN-END:initComponents

private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
    TableCellEditor editor = table.getCellEditor();
    if( null != editor )
        editor.cancelCellEditing();
    
    boolean wasValid = isDataValid();
    
    int selRow = table.getSelectedRow();
    if( selRow < 0 )
        return;
    DefaultTableModel model = (DefaultTableModel)table.getModel();
    model.removeRow( selRow );
    if( selRow > model.getRowCount()-1 )
        selRow--;
    if( selRow >= 0 )
        table.getSelectionModel().setSelectionInterval( selRow, selRow );
    
    boolean wasChanged = changed;
    changed = true;
    firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
    
    firePropertyChange( OptionsPanelController.PROP_VALID, new Boolean(wasValid), new Boolean(isDataValid()));
}//GEN-LAST:event_btnRemoveActionPerformed

private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
    int selRow = table.getSelectedRow();
    if( selRow < 0 )
        return;
    final boolean wasChanged = changed;
    table.editCellAt( selRow, 0 );
    final TableCellEditor editor = table.getCellEditor();
    editor.addCellEditorListener( new CellEditorListener() {
        public void editingStopped(ChangeEvent e) {
            editor.removeCellEditorListener( this );
            changed = true;
            firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
            firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        }

        public void editingCanceled(ChangeEvent e) {
            editor.removeCellEditorListener( this );
        }
    });
}//GEN-LAST:event_btnChangeActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.addRow( new Object[] { NbBundle.getMessage( ToDoCustomizer.class, "ToDoCustomizer.DefaultPattern") } ); //NOI18N
        table.getSelectionModel().setSelectionInterval( model.getRowCount()-1, model.getRowCount()-1 );
        final boolean wasChanged = changed;
        table.editCellAt( model.getRowCount()-1, 0 );
        final TableCellEditor editor = table.getCellEditor();
        editor.addCellEditorListener( new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                editor.removeCellEditorListener( this );
                changed = true;
                firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
                firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
            }

            public void editingCanceled(ChangeEvent e) {
                editor.removeCellEditorListener( this );
            }
        });
}//GEN-LAST:event_btnAddActionPerformed

private void scanCommentsOnlyChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scanCommentsOnlyChanged
    if( isUpdating )
        return;
    boolean wasChanged = changed;
    changed = true;
    firePropertyChange( OptionsPanelController.PROP_CHANGED, new Boolean(wasChanged), Boolean.TRUE);
}//GEN-LAST:event_scanCommentsOnlyChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox checkScanCommentsOnly;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
    
    private static class MyTable extends JTable {

        @Override
        public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component res = super.prepareEditor( editor, row, column );
            if( res instanceof JTextComponent ) {
                final JTextComponent txt = (JTextComponent)res;
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        txt.selectAll();
                        txt.requestFocusInWindow();
                    }
                });
            }
            return res;
        }
        
    }
}
