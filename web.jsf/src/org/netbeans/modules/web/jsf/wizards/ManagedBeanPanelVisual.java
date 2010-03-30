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

package org.netbeans.modules.web.jsf.wizards;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
//import org.netbeans.modules.web.struts.StrutsConfigUtilities;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class ManagedBeanPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, ListDataListener, DocumentListener {
    
    private final DefaultComboBoxModel scopeModel = new DefaultComboBoxModel();
    private boolean isCDIEnabled = false;
    /**
     * Creates new form PropertiesPanelVisual
     */
    public ManagedBeanPanelVisual(Project proj) {
        initComponents();
        
        WebModule wm = WebModule.getWebModule(proj.getProjectDirectory());
        if (wm != null){
            String[] configFiles = JSFConfigUtilities.getConfigFiles(wm);
            if (configFiles.length > 0){
                FileObject documentBase = wm.getDocumentBase();
                ArrayList<String> files = new ArrayList<String>();
                for (int i = 0; i < configFiles.length; i++){
                    if (documentBase.getFileObject(configFiles[i]) != null)
                        files.add(configFiles[i]);
                }
                configFiles = (String[])files.toArray(new String[files.size()]);
            }
            jComboBoxConfigFile.setModel(new javax.swing.DefaultComboBoxModel(configFiles));
            //No config files found
            if (configFiles.length==0) {
                jCheckBox1.setEnabled(false);
                jComboBoxConfigFile.setEnabled(false);
            } else {
                Profile profile = wm.getJ2eeProfile();
                if (profile != Profile.JAVA_EE_6_FULL && profile!=Profile.JAVA_EE_6_WEB) {
                    jCheckBox1.setSelected(true);
                    jCheckBox1.setEnabled(false);
                }
            }
        }
        Object[] scopes;
        isCDIEnabled = JSFUtils.isCDIEnabled(wm);
        if (isCDIEnabled) {
            scopes = ManagedBeanIterator.NamedScope.values();
        } else {
            scopes = ManagedBean.Scope.values();
        }

        for (Object scope : scopes) {
            scopeModel.addElement(scope);
        }

        jTextFieldName.setText("newJSFManagedBean");
        jTextFieldName.getDocument().addDocumentListener(this);
        
//        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FormBeanNewPanelVisual.class, "ACS_BeanFormProperties"));  // NOI18N
    }

    private void updateScopeModel(boolean addToConfig) {
        if (isCDIEnabled && addToConfig) {
            scopeModel.removeAllElements();
            for (ManagedBean.Scope scope : ManagedBean.Scope.values()) {
                scopeModel.addElement(scope);
            }
        } else if (isCDIEnabled && !addToConfig) {
            scopeModel.removeAllElements();
            for (ManagedBeanIterator.NamedScope scope : ManagedBeanIterator.NamedScope.values()) {
                scopeModel.addElement(scope);
            }
        } else {
            return;
        }
        jComboBoxScope.setModel(scopeModel);
        repaint();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelConfigFile = new javax.swing.JLabel();
        jComboBoxConfigFile = new javax.swing.JComboBox();
        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelScope = new javax.swing.JLabel();
        jComboBoxScope = new javax.swing.JComboBox();
        jLabelDesc = new javax.swing.JLabel();
        jScrollPaneDesc = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabelConfigFile.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_ConfigFile").charAt(0));
        jLabelConfigFile.setLabelFor(jComboBoxConfigFile);
        jLabelConfigFile.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_ConfigFile")); // NOI18N

        jComboBoxConfigFile.setEnabled(jCheckBox1.isSelected());
        jComboBoxConfigFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxConfigFileActionPerformed(evt);
            }
        });

        jLabelName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Name").charAt(0));
        jLabelName.setLabelFor(jTextFieldName);
        jLabelName.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Name")); // NOI18N

        jLabelScope.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_Scope").charAt(0));
        jLabelScope.setLabelFor(jComboBoxScope);
        jLabelScope.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Scope")); // NOI18N

        jComboBoxScope.setModel(scopeModel);

        jLabelDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_BeanDescription").charAt(0));
        jLabelDesc.setLabelFor(jTextAreaDesc);
        jLabelDesc.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_BeanDescription")); // NOI18N

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPaneDesc.setViewportView(jTextAreaDesc);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        jTextAreaDesc.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BeanDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Add_data_to_conf_file")); // NOI18N
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelName)
                    .add(jLabelScope)
                    .add(jLabelDesc)
                    .add(jLabelConfigFile))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .add(jComboBoxScope, 0, 302, Short.MAX_VALUE)
                    .add(jScrollPaneDesc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .add(jComboBoxConfigFile, 0, 302, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(jCheckBox1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelConfigFile)
                    .add(jComboBoxConfigFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelScope))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelDesc)
                    .add(jScrollPaneDesc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)))
        );

        jComboBoxConfigFile.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ConfigurationFile")); // NOI18N
        jComboBoxScope.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ManagedBeanScope")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxConfigFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxConfigFileActionPerformed
        fireChange();
    }//GEN-LAST:event_jComboBoxConfigFileActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        boolean addToConfig = jCheckBox1.isSelected();
        jComboBoxConfigFile.setEnabled(addToConfig);
        updateScopeModel(addToConfig);
    }//GEN-LAST:event_jCheckBox1ItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBoxConfigFile;
    private javax.swing.JComboBox jComboBoxScope;
    private javax.swing.JLabel jLabelConfigFile;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelScope;
    private javax.swing.JScrollPane jScrollPaneDesc;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        String configFile = (String) jComboBoxConfigFile.getSelectedItem();

        Project project = Templates.getProject(wizardDescriptor);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());

        SourceGroup[] sources = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sources.length == 0) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_No_Sources_found"));
            return false;
        }
        
        if (configFile==null) {
            if (!Utilities.isJavaEE6((TemplateWizard) wizardDescriptor) && !isAddBeanToConfig() && !(JSFUtils.isJavaEE5((TemplateWizard) wizardDescriptor) && JSFUtils.isJSF20(wm))) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_NoConfigFile"));
                return false;
            }
            return true;
        }
        
        FileObject dir = wm.getDocumentBase();
        FileObject fo = dir.getFileObject(configFile);
        FacesConfig facesConfig = ConfigurationUtils.getConfigModel(fo, true).getRootComponent();
        if (facesConfig == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_InvalidConfigFile"));
            return false;
        }
        
        String name = jTextFieldName.getText();
        if (name.trim().equals("")) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_InvalidBeanName"));
            return false;
        }

        /* XXX not ready yet, more interactions need to be considered before finalized.
        Collection<ManagedBean> beans = facesConfig.getManagedBeans();
        for (ManagedBean managedBean : beans) {
            if (name.equals(managedBean.getManagedBeanName())) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_ExistBeanName"));
                return false;
            }
        }
        */

        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }
    
    void read(WizardDescriptor settings) {
    }
    
    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.CONFIG_FILE, jComboBoxConfigFile.getSelectedItem());
        settings.putProperty(WizardProperties.NAME, jTextFieldName.getText());
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
    
    private final Set<ChangeListener> listeners = new HashSet(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }

    public void setManagedBeanName(String name) {
        jTextFieldName.setText(name);
    }

    public String getManagedBeanName() {
        return jTextFieldName.getText();
    }

    public boolean isAddBeanToConfig() {
        return jCheckBox1.isSelected();
    }

    public void insertUpdate(DocumentEvent e) {
        fireChange();
    }

    public void removeUpdate(DocumentEvent e) {
        fireChange();
    }

    public void changedUpdate(DocumentEvent e) {
        fireChange();
    }
}
