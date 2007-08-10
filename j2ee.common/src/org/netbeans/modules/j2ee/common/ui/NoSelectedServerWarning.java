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

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Shows a warning that no server is set and allows choose it.
 *
 * @author Pavel Buzek
 */
public final class NoSelectedServerWarning extends JPanel {
    
    public static final String OK_ENABLED = "ok_enabled"; //NOI18N
    
    private final String j2eeSpec;
    
    private NoSelectedServerWarning(Object[] moduleTypes, String j2eeSpec) {       
        this.j2eeSpec = j2eeSpec;
        
        initComponents();
        serverList.setModel(new ServerListModel(moduleTypes, j2eeSpec));
        if (serverList.getModel().getSize() > 0) {
            jTextArea2.setVisible(false);
        }
        initServerList();
    }
    
    private void initServerList() {
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                boolean enabled = !serverList.isSelectionEmpty();
                firePropertyChange(OK_ENABLED, !enabled, enabled);
            }
        }
        );
        serverList.setCellRenderer(new ServersRenderer());
    }
    
    /**
     * Show the "no selected server" dialog and let the user choose server instance from
     * the list.
     *
     * @param moduleTypes module types that servers should support
     * @param j2eeSpec lowest j2ee specification version that servers should support
     * @param title dialog title
     * @param description dialog accessible description
     *
     * @return serverInstanceId of the selected server instance, <code>null</code>
     *         if canceled.
     */
    public static String selectServerDialog(Object[] moduleTypes, String j2eeSpec, String title, String description) {
        NoSelectedServerWarning panel = new NoSelectedServerWarning(moduleTypes, j2eeSpec);
        Object[] options = new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        };
        final DialogDescriptor desc = new DialogDescriptor(panel, title, true, options,
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        desc.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.getAccessibleContext().setAccessibleDescription(description);
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(NoSelectedServerWarning.OK_ENABLED)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            desc.setValid(((Boolean)newvalue).booleanValue());
                        }
                    }
                }
            }
            );
            desc.setValid(panel.getSelectedInstance() != null);
            panel.setSize(panel.getPreferredSize());
            dlg.pack();
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return desc.getValue() == DialogDescriptor.OK_OPTION
                ? panel.getSelectedInstance()
                : null;
    }
    
    /** Returns the selected server instance Id or null if no instance was selected.
     *
     * @return server instance ID or null if no instance is selected
     */
    public String getSelectedInstance() {
        if (serverList.getSelectedIndex() == -1) {
            return null;
        } else {
            return (String)serverList.getSelectedValue();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        serverList = new javax.swing.JList();
        jTextArea1 = new javax.swing.JTextArea();
        jTextArea2 = new javax.swing.JTextArea();
        jButtonAddServer = new javax.swing.JButton();
        listLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 280));
        setLayout(new java.awt.GridBagLayout());

        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 100));

        serverList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serverList.setPreferredSize(null);
        serverList.setVisibleRowCount(4);
        jScrollPane2.setViewportView(serverList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(jScrollPane2, gridBagConstraints);

        jTextArea1.setColumns(25);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(NbBundle.getMessage(NoSelectedServerWarning.class, "LBL_NoSelectedServerWarning_jLabel1")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jTextArea1, gridBagConstraints);

        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setText(org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "LBL_NoSuitableServerWarning_jLabel2")); // NOI18N
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setFocusable(false);
        jTextArea2.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jTextArea2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddServer, org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "LBL_AddServer")); // NOI18N
        jButtonAddServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddServerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jButtonAddServer, gridBagConstraints);
        jButtonAddServer.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "ACSN_AddServer")); // NOI18N
        jButtonAddServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "ACSD_AddServer")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(listLabel, org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "LBL_NoSelectedServerWarning_listLabel", new Object[] {Util.getJ2eeSpecificationLabel(j2eeSpec)})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(listLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonAddServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddServerActionPerformed
        ServerListModel model = (ServerListModel) serverList.getModel();
        String selectedValue = (String) serverList.getSelectedValue();
        boolean wasEmpty = (model.getSize() == 0);
        String newSelectedValue = ServerManager.showAddServerInstanceWizard();
        if (newSelectedValue != null) {
            selectedValue = newSelectedValue;
        }
        model.refreshModel();
        boolean isEmpty = (model.getSize() == 0);
        if (wasEmpty != isEmpty) {
            jTextArea2.setVisible(isEmpty);
        }
        if (selectedValue != null) {
            // if the last selected server instance still exists, select it again
            serverList.setSelectedValue(selectedValue, true);
        } else {
            serverList.clearSelection();
        }
    }//GEN-LAST:event_jButtonAddServerActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddServer;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JLabel listLabel;
    private javax.swing.JList serverList;
    // End of variables declaration//GEN-END:variables
    
    
    private static final class ServerListModel extends AbstractListModel {
        
        private String[] instances;
        
        private final Object[] moduleTypes;
        private final String j2eeSpec;
        
        public ServerListModel(Object[] moduleTypes, String j2eeSpec) {
            this.moduleTypes = moduleTypes;
            this.j2eeSpec = j2eeSpec;
            instances = Deployment.getDefault().getServerInstanceIDs(moduleTypes, j2eeSpec);
        }
        
        public synchronized int getSize() {
            return instances.length;
        }
        
        public synchronized Object getElementAt(int index) {
            if (index >= 0 && index < instances.length) {
                return instances [index];
            } else {
                return null;
            }
        }
        
        public synchronized void refreshModel() {
            int oldLength = instances.length;
            instances = Deployment.getDefault().getServerInstanceIDs(moduleTypes, j2eeSpec);
            if (instances.length > 0) {
                fireContentsChanged(this, 0, instances.length - 1);
            } else if (oldLength > 0) {
                fireIntervalRemoved(this, 0, oldLength - 1);
            }
        }
        
    }
    
    private static final class ServersRenderer extends JLabel implements ListCellRenderer {
        
        ServersRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof String) {
                String id = (String) value;
                setText(Deployment.getDefault().getServerInstanceDisplayName(id));
                //                setIcon (ProjectUtils.getInformation (prj).getIcon ());
            } else {
                setText(value.toString());
                setIcon(null);
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                //setBorder (BorderFactory.createLineBorder (Color.BLACK));
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                //setBorder (null);
            }
            return this;
        }
    }
    
}
