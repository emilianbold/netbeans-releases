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

package org.netbeans.modules.autoupdate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Radek Matous
 */
public class SettingsTab extends javax.swing.JPanel {
    final DetailsPanel details;
    private Listener listener;
    private static final RequestProcessor RP = new RequestProcessor ();
    private final RequestProcessor.Task searchTask = RP.create (new Runnable (){
        public void run () {
            String filter = tfSearch.getText ().trim ();
            if (filter != null) {
                getSettingsTableModel ().setFilter (filter);
            }
        }
    });
    private boolean modulesOnly = Utilities.modulesOnly ();
    
    /** Creates new form SettingsTab */
    public SettingsTab (PluginManagerUI manager, DetailsPanel details) {
        initComponents ();
        getSettingsTableModel ().setPluginManager (manager);
        TableColumn activeColumn = jTable1.getColumnModel ().getColumn (0);
        activeColumn.setMaxWidth (jTable1.getTableHeader ().getHeaderRect (0).width);
        this.details = details;
        addListener ();
        cbOnlyModules.setSelected (modulesOnly);
    }
    
    public String getDisplayName () {
        return NbBundle.getMessage (SettingsTab.class, "SettingsTab_displayName"); //NOI18N
    }
    
    private void addListener () {
        if (listener == null) {
            listener = new SettingsTab.Listener ();
            tfSearch.getDocument ().addDocumentListener (listener);
            jTable1.getSelectionModel ().addListSelectionListener (listener);
            jTable1.addFocusListener (listener);
            getSettingsTableModel ().addTableModelListener (listener);
        }
    }
    
    private void removeListener () {
        if (listener != null) {
            tfSearch.getDocument ().removeDocumentListener (listener);
            jTable1.getSelectionModel ().removeListSelectionListener (listener);
            jTable1.removeFocusListener (listener);
            getSettingsTableModel ().removeTableModelListener (listener);
            listener = null;
        }
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        getSettingsTableModel ().refreshModel ();
        addListener ();
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        removeListener ();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lHeader = new javax.swing.JLabel();
        lSearch = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        bEdit = new javax.swing.JButton();
        bNew = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        bProxy = new javax.swing.JButton();
        cbOnlyModules = new javax.swing.JCheckBox();

        lHeader.setLabelFor(jTable1);
        org.openide.awt.Mnemonics.setLocalizedText(lHeader, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lHeader.text")); // NOI18N

        lSearch.setLabelFor(tfSearch);
        org.openide.awt.Mnemonics.setLocalizedText(lSearch, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lSearch.text")); // NOI18N

        jTable1.setModel(new SettingsTableModel());
        jScrollPane1.setViewportView(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bEdit.text")); // NOI18N
        bEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEditActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bNew, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bNew.text")); // NOI18N
        bNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bRemove.text")); // NOI18N
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bProxy, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bProxy.text")); // NOI18N
        bProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bProxyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbOnlyModules, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab_cbOnlyModules")); // NOI18N
        cbOnlyModules.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbOnlyModules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOnlyModulesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(lHeader)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 138, Short.MAX_VALUE)
                                .add(lSearch)
                                .add(4, 4, 4)
                                .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(bEdit, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(bNew, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(bRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(cbOnlyModules)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 473, Short.MAX_VALUE)
                        .add(bProxy)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {bEdit, bNew, bProxy, bRemove}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lHeader)
                    .add(lSearch))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(bEdit)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bNew)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bRemove))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bProxy)
                    .add(cbOnlyModules))
                .add(20, 20, 20))
        );

        lHeader.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lHeader.adesc")); // NOI18N
        lSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lSearch.adesc")); // NOI18N
        bEdit.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bEdit.adesc")); // NOI18N
        bNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bNew.adesc")); // NOI18N
        bRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bRemove.adesc")); // NOI18N
        bProxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bProxy.adesc")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.adesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
private void cbOnlyModulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOnlyModulesActionPerformed
    JCheckBox cb = (JCheckBox) evt.getSource ();
    if (modulesOnly != cb.isSelected ()) {
        modulesOnly = cb.isSelected ();
        Utilities.setModulesOnly (cb.isSelected ());
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                Utilities.presentRefreshProviders (getSettingsTableModel ().getPluginManager (), false);
                getSettingsTableModel ().getPluginManager ().tableStructureChanged ();
                getSettingsTableModel ().getPluginManager ().updateUnitsChanged ();
            }
        });
    }
}//GEN-LAST:event_cbOnlyModulesActionPerformed

private void bEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
    final int rowIndex = jTable1.getSelectedRow ();
    if (rowIndex != -1) {
        final UpdateUnitProvider provider = getSettingsTableModel ().getUpdateUnitProvider (rowIndex);
        if (provider == null) return;
        final UpdateUnitProviderPanel panel = new UpdateUnitProviderPanel (provider.isEnabled (),
                provider.getDisplayName (), // display name
                provider.getProviderURL ().toExternalForm (), // URL
                true); // editing
        DialogDescriptor descriptor = getCustomizerDescriptor (panel);
        panel.getOKButton ().addActionListener (new ActionListener (){
            public void actionPerformed (ActionEvent arg0) {
                setData (provider, panel);
                getSettingsTableModel ().refreshModel ();
                jTable1.getSelectionModel ().setSelectionInterval (rowIndex, rowIndex);
                
            }
        });
        DialogDisplayer.getDefault ().createDialog (descriptor).setVisible (true);
    }
    
}//GEN-LAST:event_bEditActionPerformed

private void bNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNewActionPerformed
    final UpdateUnitProviderPanel panel = new UpdateUnitProviderPanel (true,
            NbBundle.getMessage (SettingsTab.class, "SettingsTab_NewProviderName"), // NOI18N
            NbBundle.getMessage (SettingsTab.class, "SettingsTab_NewProviderURL"), // NOI18N
            false);
    DialogDescriptor descriptor = getCustomizerDescriptor (panel);
    panel.getOKButton ().addActionListener (new ActionListener (){
        public void actionPerformed (ActionEvent arg0) {
            try {
                getSettingsTableModel ().add
                        (panel.getProviderName (),
                        panel.getProviderName (),
                        new URL (panel.getProviderURL ()),
                        panel.isActive ());
                getSettingsTableModel ().refreshModel ();
                SettingsTableModel model = getSettingsTableModel ();
                for (int i = 0; i < model.getRowCount (); i++) {
                    String providerName = model.getValueAt (i, 1).toString ();
                    if (panel.getProviderName () != null && panel.getProviderName ().equals (providerName)) {
                        jTable1.getSelectionModel ().setSelectionInterval (i, i);
                    }
                    
                }
                
                
            } catch(MalformedURLException mex) {
                Exceptions.printStackTrace (mex);
            }
        }
    });
    DialogDisplayer.getDefault ().createDialog (descriptor).setVisible (true);
}//GEN-LAST:event_bNewActionPerformed


private void bProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bProxyActionPerformed
    OptionsDisplayer.getDefault ().open ("General"); //NOI18N
}//GEN-LAST:event_bProxyActionPerformed

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        SettingsTableModel model = getSettingsTableModel ();
        int[] rowIndexes = jTable1.getSelectedRows ();
        for (int rowIndex : rowIndexes) {
            if (rowIndex != -1) {
                model.remove (rowIndex);
            }
        }
        model.refreshModel ();
        if (rowIndexes.length > 0) {
            if (model.getRowCount () > rowIndexes[0]) {
                jTable1.getSelectionModel ().setSelectionInterval (rowIndexes[0], rowIndexes[0]);
            } else {
                jTable1.getSelectionModel ().setSelectionInterval (0, 0);
            }
        }
}//GEN-LAST:event_bRemoveActionPerformed
    
    private void setData (final UpdateUnitProvider provider, UpdateUnitProviderPanel panel) {
        provider.setDisplayName (panel.getProviderName ());
        boolean forceRead = false;
        boolean refreshModel = false;
        try {
            URL oldUrl = provider.getProviderURL ();
            URL newUrl = new URL (panel.getProviderURL ());
            if (! oldUrl.equals (newUrl)) {
                provider.setProviderURL (newUrl);
                refreshModel = true;
                forceRead = true;
            }
        } catch(MalformedURLException mex) {
            Exceptions.printStackTrace (mex);
        }
        boolean oldValue = provider.isEnabled ();
        if (oldValue != panel.isActive ()) {
            refreshModel = true;
        }
        if (refreshModel) {
            provider.setEnable (panel.isActive ());
            if (oldValue && ! forceRead) {
                // was enabled and won't be more -> remove it from model
                getSettingsTableModel ().getPluginManager ().updateUnitsChanged ();
            } else {
                // was enabled and won't be more -> add it from model and read its content
                final boolean force = forceRead;
                RequestProcessor.getDefault ().post (new Runnable () {
                    public void run () {
                        Utilities.presentRefreshProvider (provider, getSettingsTableModel ().getPluginManager (), force);
                        getSettingsTableModel ().getPluginManager ().updateUnitsChanged ();
                    }
                });
            }
        }
    }
    private static DialogDescriptor getCustomizerDescriptor (UpdateUnitProviderPanel panel) {
        JButton bOK = panel.getOKButton ();
        Object[] options = new Object[2];
        options[0] = bOK;
        options[1] = DialogDescriptor.CANCEL_OPTION;
        
        DialogDescriptor descriptor = new DialogDescriptor (panel,panel.getDisplayName (),true,options,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null, null);
        return descriptor;
    }
    
    public SettingsTableModel getSettingsTableModel () {
        return ((SettingsTableModel)jTable1.getModel ());
    }
    
    private class Listener implements ListSelectionListener, FocusListener, TableModelListener, DocumentListener {
        public void valueChanged (ListSelectionEvent arg0) {
            modelOrSelectionChanged ();
        }
        
        public void focusGained (FocusEvent arg0) {
            enableDisableRemove ();
        }
        
        public void focusLost (FocusEvent arg0) {
            enableDisableRemove ();
        }
        
        private boolean canEditEnable (int [] rows) {
            if (rows == null || rows.length != 1) {
                return false;
            }
            UpdateUnitProvider p = getSettingsTableModel ().getUpdateUnitProvider (rows [0]);
            return p != null && p.getProviderURL () != null;
        }
        
        private void enableDisableRemove () {
            int rowIndex = jTable1.getSelectedRow ();
            SettingsTableModel model = getSettingsTableModel ();
            UpdateUnitProvider uup = (rowIndex >=0) ? model.getUpdateUnitProvider (rowIndex) : null;
            
            boolean enable = rowIndex != -1 &&  uup != null;
            bRemove.setEnabled (enable);
            bEdit.setEnabled (canEditEnable (jTable1.getSelectedRows ()));
        }
        
        public void tableChanged (TableModelEvent arg0) {
            modelOrSelectionChanged ();
        }
        
        private void modelOrSelectionChanged () {
            int rowIndex = jTable1.getSelectedRow ();
            if (rowIndex != -1) {
                UpdateUnitProvider uup =
                        ((SettingsTableModel)jTable1.getModel ()).getUpdateUnitProvider (rowIndex);
                if (uup != null) {
                    StringBuffer sb = new StringBuffer ();
                    sb.append ("<h2>" + uup.getDisplayName () + "</h2>"); // NOI18N
                    URL u= uup.getProviderURL ();
                    if (u != null) {
                        sb.append ("<b>" + getBundle ("SettingsTab_UpdateUnitProvider_Description") + "</b><br>"); // NOI18N
                        sb.append ("<b>" + getBundle ("SettingsTab_UpdateUnitProvider_URL") +  // NOI18N
                                " </b><a href=\"" + u.toExternalForm () + "\">" + u.toExternalForm () + "<br>"); // NOI18N
                    }
                    details.getDetails ().setText (sb.toString ());
                }
            } else {
                ListSelectionModel lsm = jTable1.getSelectionModel ();
                lsm.setSelectionInterval (0, 0);
            }
            enableDisableRemove ();
        }
        public void insertUpdate (DocumentEvent arg0) {
            updateFilter ();
        }
        
        public void removeUpdate (DocumentEvent arg0) {
            updateFilter ();
        }
        
        public void changedUpdate (DocumentEvent arg0) {
            updateFilter ();
        }
        
        private void updateFilter () {
            searchTask.schedule (350);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bNew;
    private javax.swing.JButton bProxy;
    private javax.swing.JButton bRemove;
    private javax.swing.JCheckBox cbOnlyModules;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lHeader;
    private javax.swing.JLabel lSearch;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables
    
    public static String getBundle (String key) {
        return NbBundle.getMessage (SettingsTab.class, key);
    }
}
