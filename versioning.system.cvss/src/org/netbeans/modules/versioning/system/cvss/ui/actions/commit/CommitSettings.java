/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.spi.VersioningListener;
import org.netbeans.modules.versioning.util.ListenersSupport;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Customization of commits.
 * 
 * @author Maros Sandor
 */
public class CommitSettings extends javax.swing.JPanel implements PropertyChangeListener, TableModelListener {
    
    static final String COLUMN_NAME_NAME    = "name";
    static final String COLUMN_NAME_STICKY  = "sticky";
    static final String COLUMN_NAME_STATUS  = "status";
    static final String COLUMN_NAME_ACTION  = "action";
    static final String COLUMN_NAME_PATH    = "path";
    
    static final Object EVENT_SETTINGS_CHANGED = new Object();

    private CommitTable     commitTable;

    public static class CommitFile {
        private final CommitOptions options;
        private final CvsFileNode   node;

        public CommitFile(CvsFileNode node, CommitOptions options) {
            this.node = node;
            this.options = options;
        }

        public CommitOptions getOptions() {
            return options;
        }

        public CvsFileNode getNode() {
            return node;
        }
    }
    
    public CommitSettings() {
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(ss.width / 2, ss.height / 5 * 2));
        init();
    }

    /**
     * Set columns to display in the Commit table.
     * 
     * @param cols array of column names
     */ 
    void setColumns(String[] cols) {
        commitTable.setColumns(cols);
    }

    /**
     * Set file nodes to display in the Commit table.
     * 
     * @param nodes array of nodes
     */ 
    void setNodes(CvsFileNode[] nodes) {
        commitTable.setNodes(nodes);
    }
    
    public String getCommitMessage() {
        return taMessage.getText();
    }
    
    public void addNotify() {
        super.addNotify();
        CvsModuleConfig.getDefault().addPropertyChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        taMessage.selectAll();
    }

    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        CvsModuleConfig.getDefault().removePropertyChangeListener(this);
        super.removeNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (CvsModuleConfig.PROP_COMMIT_EXCLUSIONS.equals(evt.getPropertyName())) {
            commitTable.dataChanged();
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        }
    }

    public CommitFile [] getCommitFiles() {
        return commitTable.getCommitFiles();
    }
    
    private void init() {
        initComponents();
        errorLabel.setMinimumSize(errorLabel.getPreferredSize());
        errorLabel.setText("");
        jScrollPane1.setMinimumSize(jScrollPane1.getPreferredSize());
        commitTable = new CommitTable();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(commitTable.getComponent(), gridBagConstraints);
    }
    
    void setErrorLabel(String htmlErrorLabel) {
        errorLabel.setText(htmlErrorLabel);
    }

    public void setCommand(CommitCommand cmd) {
        taMessage.setText(cmd.getMessage());
        if (cmd.getToRevisionOrBranch() != null) {
            tfRevision.setText(cmd.getToRevisionOrBranch());
            cbRevision.setSelected(true);
        } else {
            cbRevision.setSelected(false);            
        }
        cbLocally.setSelected(!cmd.isRecursive());
    }

    public void updateCommand(CommitCommand cmd) {
        cmd.setMessage(taMessage.getText());
        cmd.setRecursive(!cbLocally.isSelected());
        if (cbRevision.isSelected()) {
            cmd.setToRevisionOrBranch(tfRevision.getText());
        } else {
            cmd.setToRevisionOrBranch(null);            
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

        cbLocally = new javax.swing.JCheckBox();
        cbRevision = new javax.swing.JCheckBox();
        tfRevision = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taMessage = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();

        cbLocally.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle").getString("MNE_CommitForm_Locally").charAt(0));
        org.openide.awt.Mnemonics.setLocalizedText(cbLocally, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle").getString("CTL_CommitForm_Locally"));
        cbRevision.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle").getString("MNE_CommitForm_Revision").charAt(0));
        org.openide.awt.Mnemonics.setLocalizedText(cbRevision, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle").getString("CTL_CommitForm_Revision"));

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        jLabel2.setLabelFor(taMessage);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle").getString("CTL_CommitForm_Message"));
        jLabel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 2, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(jLabel2, gridBagConstraints);

        taMessage.setColumns(30);
        taMessage.setLineWrap(true);
        taMessage.setRows(6);
        taMessage.setTabSize(4);
        taMessage.setWrapStyleWord(true);
        jScrollPane1.setViewportView(taMessage);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle").getString("CTL_CommitForm_FilesToCommit"));
        jLabel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(jLabel3, gridBagConstraints);

        errorLabel.setText("Y");
        errorLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(errorLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbLocally;
    private javax.swing.JCheckBox cbRevision;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea taMessage;
    private javax.swing.JTextField tfRevision;
    // End of variables declaration//GEN-END:variables
    
    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
}