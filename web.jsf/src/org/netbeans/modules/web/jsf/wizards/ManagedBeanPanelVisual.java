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

package org.netbeans.modules.web.jsf.wizards;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
//import org.netbeans.modules.web.struts.StrutsConfigUtilities;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


public class ManagedBeanPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, ListDataListener {
    
    /**
     * Creates new form PropertiesPanelVisual
     */
    public ManagedBeanPanelVisual(Project proj) {
        initComponents();
        
        WebModule wm = WebModule.getWebModule(proj.getProjectDirectory());
        String[] configFiles = JSFConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
        jComboBoxConfigFile.setModel(new javax.swing.DefaultComboBoxModel(configFiles));
        
        
//        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FormBeanNewPanelVisual.class, "ACS_BeanFormProperties"));  // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelConfigFile = new javax.swing.JLabel();
        jComboBoxConfigFile = new javax.swing.JComboBox();
        jLabelScope = new javax.swing.JLabel();
        jComboBoxScope = new javax.swing.JComboBox();
        jLabelDesc = new javax.swing.JLabel();
        jScrollPaneDesc = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jLabelConfigFile.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_ConfigFile").charAt(0));
        jLabelConfigFile.setLabelFor(jComboBoxConfigFile);
        jLabelConfigFile.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_ConfigFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelConfigFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxConfigFile, gridBagConstraints);
        jComboBoxConfigFile.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("ACSD_ConfigurationFile"));

        jLabelScope.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_Scope").charAt(0));
        jLabelScope.setLabelFor(jComboBoxScope);
        jLabelScope.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Scope"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelScope, gridBagConstraints);

        jComboBoxScope.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "request", "session", "application", "none" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxScope, gridBagConstraints);
        jComboBoxScope.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("ACSD_ManagedBeanScope"));

        jLabelDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_BeanDescription").charAt(0));
        jLabelDesc.setLabelFor(jTextAreaDesc);
        jLabelDesc.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_BeanDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabelDesc, gridBagConstraints);

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPaneDesc.setViewportView(jTextAreaDesc);
        jTextAreaDesc.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("ACSD_BeanDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPaneDesc, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxConfigFile;
    private javax.swing.JComboBox jComboBoxScope;
    private javax.swing.JLabel jLabelConfigFile;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelScope;
    private javax.swing.JScrollPane jScrollPaneDesc;
    private javax.swing.JTextArea jTextAreaDesc;
    // End of variables declaration//GEN-END:variables
    
    boolean valid(WizardDescriptor wizardDescriptor) {
//        String superclass = (String) jComboBoxSuperclass.getEditor().getItem();
//        String configFile = (String) jComboBoxConfigFile.getSelectedItem();
        
//        return (superclass != null && !superclass.trim().equals("") && configFile != null && !configFile.trim().equals(""));
        
        return true;
    }
    
    void read(WizardDescriptor settings) {
    }
    
    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.CONFIG_FILE, jComboBoxConfigFile.getSelectedItem());
        settings.putProperty(WizardProperties.SCOPE, jComboBoxScope.getSelectedItem());
        settings.putProperty(WizardProperties.DESCRIPTION, jTextAreaDesc.getText());
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ManagedBeanPanelVisual.class);
    }
    
    public void intervalRemoved(ListDataEvent e) {
    }
    
    public void intervalAdded(ListDataEvent e) {
    }
    
    public void contentsChanged(ListDataEvent e) {

    }
    
}
