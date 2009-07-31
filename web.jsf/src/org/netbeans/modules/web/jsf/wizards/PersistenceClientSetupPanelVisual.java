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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.SourceGroupUISupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.dialogs.BrowseFolders;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Buzek
 */
public class PersistenceClientSetupPanelVisual extends javax.swing.JPanel implements DocumentListener {
    
    private WizardDescriptor wizard;
    private Project project;
    private JTextComponent jpaPackageComboBoxEditor, jsfPackageComboBoxEditor;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    /** Creates new form CrudSetupPanel */
    public PersistenceClientSetupPanelVisual(WizardDescriptor wizard) {
        this.wizard = wizard;
        initComponents();


        JComboBox[] combos = {jpaPackageComboBox, jsfPackageComboBox};
        for (int i = 0; i < combos.length; i++) {
            JTextComponent comboEditor = ((JTextComponent)combos[i].getEditor().getEditorComponent());
            if (i == 0) {
                jpaPackageComboBoxEditor = comboEditor;
            }
            else {
                jsfPackageComboBoxEditor = comboEditor;
            }
            Document packageComboBoxDocument = comboEditor.getDocument();
            packageComboBoxDocument.addDocumentListener(this);
        }
        
        jsfFolder.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent e) {
                changeSupport.fireChange();
            }            
            public void keyReleased(KeyEvent e) {
                changeSupport.fireChange();
            } 
            public void keyTyped(KeyEvent e) {
                changeSupport.fireChange();
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jsfFolder = new javax.swing.JTextField();
        browseFolderButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        jsfPackageLabel = new javax.swing.JLabel();
        jsfPackageComboBox = new javax.swing.JComboBox();
        ajaxifyCheckbox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jpaPackageLabel = new javax.swing.JLabel();
        jpaPackageComboBox = new javax.swing.JComboBox();

        setName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_JSFPagesAndClasses")); // NOI18N

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_JSF_Pages").charAt(0));
        jLabel2.setLabelFor(jsfFolder);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        jLabel2.setText(bundle.getString("LBL_JSF_pages_folder")); // NOI18N

        browseFolderButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Browse").charAt(0));
        browseFolderButton.setText(bundle.getString("LBL_Browse")); // NOI18N
        browseFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseFolderButtonActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("MSG_Jsf_Pages_Location")); // NOI18N

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Project").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Location").charAt(0));
        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        jsfPackageLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Package").charAt(0));
        jsfPackageLabel.setLabelFor(jsfPackageComboBox);
        jsfPackageLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Package")); // NOI18N

        jsfPackageComboBox.setEditable(true);

        ajaxifyCheckbox.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_AJAXIFY_APP")); // NOI18N
        ajaxifyCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ajaxifyCheckboxActionPerformed(evt);
            }
        });

        jLabel6.setText(bundle.getString("MSG_Jpa_Jsf_Packages")); // NOI18N

        jpaPackageLabel.setLabelFor(jpaPackageComboBox);
        jpaPackageLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_Controller_Package")); // NOI18N

        jpaPackageComboBox.setEditable(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectLabel)
                            .add(locationLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, locationComboBox, 0, 488, Short.MAX_VALUE)
                            .add(projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(jpaPackageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jpaPackageComboBox, 0, 417, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jsfPackageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsfPackageComboBox, 0, 429, Short.MAX_VALUE))
                    .add(jLabel4)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsfFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseFolderButton))
                    .add(ajaxifyCheckbox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel6)
                .add(13, 13, 13)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationLabel)
                    .add(locationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jpaPackageLabel)
                    .add(jpaPackageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jsfPackageLabel)
                    .add(jsfPackageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(29, 29, 29)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(browseFolderButton)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel2)
                        .add(jsfFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(ajaxifyCheckbox)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_JSF_pages_folder")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_JSF_pages_folder")); // NOI18N
        jsfFolder.getAccessibleContext().setAccessibleDescription("null");
        browseFolderButton.getAccessibleContext().setAccessibleDescription("null");
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jsf_Pages_Location")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jsf_Pages_Location")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Project")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription("null");
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription("null");
        jsfPackageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Package")); // NOI18N
        jsfPackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Package")); // NOI18N
        jsfPackageComboBox.getAccessibleContext().setAccessibleDescription("null");
        ajaxifyCheckbox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_AJAXIFY_APP")); // NOI18N
        ajaxifyCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_AJAXIFY_APP")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jpa_Jsf_Packages")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jpa_Jsf_Packages")); // NOI18N
        jpaPackageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_Controller_Package")); // NOI18N
        jpaPackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_Controller_Package")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed
        
    private void browseFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFolderButtonActionPerformed
        Sources s = (Sources) Templates.getProject(wizard).getLookup().lookup(Sources.class);
        org.netbeans.api.project.SourceGroup[] groups = s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
        if (fo!=null) {
            String res = "/"+JSFConfigUtilities.getResourcePath(groups,fo,'/',true);
            jsfFolder.setText(res);
        }
    }//GEN-LAST:event_browseFolderButtonActionPerformed

    private void ajaxifyCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ajaxifyCheckboxActionPerformed
        // TODO add your handling code here:
        changeSupport.fireChange();
}//GEN-LAST:event_ajaxifyCheckboxActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ajaxifyCheckbox;
    private javax.swing.JButton browseFolderButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox jpaPackageComboBox;
    private javax.swing.JLabel jpaPackageLabel;
    private javax.swing.JTextField jsfFolder;
    private javax.swing.JComboBox jsfPackageComboBox;
    private javax.swing.JLabel jsfPackageLabel;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    // End of variables declaration//GEN-END:variables
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    boolean valid(WizardDescriptor wizard) {
//        List<Entity> entities = (List<Entity>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
//        String controllerPkg = getJsfPackage();
//        
//        boolean filesAlreadyExist = false;
//        String troubleMaker = "";
//        for (Entity entity : entities) {
//            String entityClass = entity.getClass2();
//            String simpleClassName = JSFClientGenerator.simpleClassName(entityClass);
//            String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
//            String folder = jsfFolder.getText().endsWith("/") ? jsfFolder.getText() : jsfFolder.getText() + "/";
//            folder = folder + firstLower;
//            String controller = controllerPkg + "." + simpleClassName + "Controller";
//            String fqn = getJsfPackage().length() > 0 ? getJsfPackage().replace('.', '/') + "/" + simpleClassName : simpleClassName;
//            if (getLocationValue().getRootFolder().getFileObject(fqn + "Controller.java") != null) {
//                filesAlreadyExist = true;
//                troubleMaker = controllerPkg + "." + simpleClassName + "Controller.java";
//                break;
//            }
//            if (getLocationValue().getRootFolder().getFileObject(fqn + "Converter.java") != null) {
//                filesAlreadyExist = true;
//                troubleMaker = controllerPkg + "." + simpleClassName + "Converter.java";
//                break;
//            }
//        }
//        if (filesAlreadyExist) {
//            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,                                  // NOI18N
//                NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_FilesAlreadyExist", troubleMaker));
//            return false;
//        }
//        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
        
            if (Util.isContainerManaged(project)) {
                ClassPath cp = ClassPath.getClassPath(getLocationValue().getRootFolder(), ClassPath.COMPILE);
                ClassLoader cl = cp.getClassLoader(true);
                try {
                    Class.forName("javax.transaction.UserTransaction", false, cl);
                }
                catch (ClassNotFoundException cnfe) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_UserTransactionUnavailable"));
                    return false;
                }
            }
        
            Sources srcs = (Sources) project.getLookup().lookup(Sources.class);
            SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            FileObject pagesRootFolder = sgWeb[0].getRootFolder();
            File pagesRootFolderAsFile = FileUtil.toFile(pagesRootFolder);
            String jsfFolderText = jsfFolder.getText();
            try {
                String canonPath = new File(pagesRootFolderAsFile, jsfFolderText).getCanonicalPath();
            }
            catch (IOException ioe) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_JsfTargetChooser_InvalidJsfFolder"));
                return false;
            }
        
            String[] packageNames = {getJpaPackage(), getJsfPackage()};
            for (int i = 0; i < packageNames.length; i++) {
                if (packageNames[i].trim().equals("")) { // NOI18N
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_JavaTargetChooser_CantUseDefaultPackage"));
                    return false;
                }

                if (!JavaIdentifiers.isValidPackageName(packageNames[i])) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class,"ERR_JavaTargetChooser_InvalidPackage")); //NOI18N
                    return false;
                }

                if (!SourceGroups.isFolderWritable(getLocationValue(), packageNames[i])) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_JavaTargetChooser_UnwritablePackage")); //NOI18N
                    return false;
                }
            }

            if (ajaxifyCheckbox.isSelected()) {
                if (LibraryManager.getDefault().getLibrary("jsf-extensions") == null) { //NOI18N
                    wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_JsfExtensionsLibraryRequired"));
                    return true;
                }
            }
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            return true;
    }
    
    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getJsfPackage() {
        return jsfPackageComboBoxEditor.getText();
    }
    
    public String getJpaPackage() {
        return jpaPackageComboBoxEditor.getText();
    }

    private void locationChanged() {
        updateSourceGroupPackages();
//        changeSupport.fireChange();
    }
    
    void read(WizardDescriptor settings) {
        jsfFolder.setText((String) settings.getProperty(WizardProperties.JSF_FOLDER));
        
        project = Templates.getProject(settings);
        FileObject targetFolder = Templates.getTargetFolder(settings);
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

         SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);      
         SourceGroupUISupport.connect(locationComboBox, sourceGroups);

        jsfPackageComboBox.setRenderer(PackageView.listRenderer());

        updateSourceGroupPackages();

        // set default source group and package cf. targetFolder
        if (targetFolder != null) {
//            SourceGroup targetSourceGroup = SourceGroupSupport.getFolderSourceGroup(sourceGroups, targetFolder);
//            if (targetSourceGroup != null) {
//                locationComboBox.setSelectedItem(targetSourceGroup);
//                String targetPackage = SourceGroupSupport.getPackageForFolder(targetSourceGroup, targetFolder);
//                if (targetPackage != null) {
//                    jsfPackageComboBoxEditor.setText(targetPackage);
//                }
//            }
            if (FileUtil.isParentOf(WebModule.getWebModule(
                    targetFolder).getDocumentBase(), targetFolder)) {
                Sources s = (Sources) Templates.getProject(wizard).getLookup().lookup(Sources.class);
                SourceGroup[] groups = s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
                jsfFolder.setText("/"+JSFConfigUtilities.getResourcePath(groups,targetFolder,'/',true));
            }
        }

        if(J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported())
        {
            //change label if we will generate session beans
            jpaPackageLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_SessionBean_Package")); // NOI18N
        }
        //

    }
    
    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.JSF_FOLDER, jsfFolder.getText());
        String jpaPkg = getJpaPackage();
        String jsfPkg = getJsfPackage();
        settings.putProperty(WizardProperties.JPA_CLASSES_PACKAGE, jpaPkg);
        settings.putProperty(WizardProperties.JSF_CLASSES_PACKAGE, jsfPkg);
        settings.putProperty(WizardProperties.AJAXIFY_JSF_CRUD, Boolean.valueOf(ajaxifyCheckbox.isSelected()));
        String[] pkgs = {jpaPkg, jsfPkg};
        try {
            for (int i = 0; i < pkgs.length; i++) {
                FileObject fo = getLocationValue().getRootFolder();
                String pkgSlashes = pkgs[i].replace('.', '/');
                FileObject targetFolder = fo.getFileObject(pkgSlashes);
                if (targetFolder == null) {
                    targetFolder = FileUtil.createFolder(fo, pkgSlashes);
                }
                if (i == 1) {
                    Templates.setTargetFolder(settings, targetFolder);
                }
                else {
                    settings.putProperty(WizardProperties.JPA_CLASSES_PACKAGE_FILE_OBJECT, targetFolder);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateSourceGroupPackages() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        JComboBox[] combos = {jpaPackageComboBox, jsfPackageComboBox};
        for (JComboBox combo : combos) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            combo.setModel(model);
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        changeSupport.fireChange();
    }

    public void removeUpdate(DocumentEvent e) {
        changeSupport.fireChange();
    }

    public void changedUpdate(DocumentEvent e) {
        changeSupport.fireChange();
    }
    
}
