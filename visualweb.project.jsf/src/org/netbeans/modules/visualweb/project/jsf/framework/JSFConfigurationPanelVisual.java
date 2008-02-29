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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.project.jsf.framework;

// <RAVE>
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
// </RAVE>

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
// import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.ExtenderController.Properties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Po-Ting Wu
 */
public class JSFConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener  {

    private Project project;
    private JSFConfigurationPanel panel;
    private boolean customizer;
    
    private ArrayList <Library> jsfLibraries;
    private boolean webModule25Version;
    private String serverInstanceID;
    private boolean addJSF = false;

    // <RAVE> Visual Web JSF Backwards Compatibility Kit & Default Bean Package
    private boolean addJAXRPC = false;
    private boolean addRowset = false;
    private boolean beanPackageModified = false;
    // </RAVE>
    
    /** Creates new form JSFConfigurationPanelVisual */
    public JSFConfigurationPanelVisual(JSFConfigurationPanel panel, Project project, boolean customizer) {
        this.project = project;
        this.panel = panel;
        this.customizer = customizer;
        
        initComponents();
        
        // <RAVE>
        remove(jsfTabbedPane);
        add(confPanel, "card10");
        // </RAVE>
        initLibraries();

        tURLPattern.getDocument().addDocumentListener(this);
        cbPackageJars.setVisible(false);
        
        if (customizer){
            enableComponents(false);
        } else {
            updateLibrary();
        }
    }
    
    private void initLibraries(){
        Library libraries[] = LibraryManager.getDefault().getLibraries();
        Vector <String> items = new Vector();
        jsfLibraries = new ArrayList();
        
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].getName().startsWith("JSF-") || libraries[i].getName().equals("jsf12")) { //NOI18N
                String displayName = libraries[i].getDisplayName();
                items.add(displayName);
                jsfLibraries.add(libraries[i]);
            }
        }
        
        cbLibraries.setModel(new DefaultComboBoxModel(items));
        if (items.size() == 0){
            rbRegisteredLibrary.setEnabled(false);
            cbLibraries.setEnabled(false);
            rbNewLibrary.setSelected(true);
            panel.setLibrary(null);
        } else {
            rbRegisteredLibrary.setEnabled(true);
            rbRegisteredLibrary.setSelected(true);
            cbLibraries.setEnabled(true);
        }
        repaint();
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jsfTabbedPane = new javax.swing.JTabbedPane();
        confPanel = new javax.swing.JPanel();
        beanPackageLabel = new javax.swing.JLabel();
        beanPackageTextField = new javax.swing.JTextField();
        lServletName = new javax.swing.JLabel();
        tServletName = new javax.swing.JTextField();
        lURLPattern = new javax.swing.JLabel();
        tURLPattern = new javax.swing.JTextField();
        cbValidate = new javax.swing.JCheckBox();
        cbVerify = new javax.swing.JCheckBox();
        cbPackageJars = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        libPanel = new javax.swing.JPanel();
        rbRegisteredLibrary = new javax.swing.JRadioButton();
        cbLibraries = new javax.swing.JComboBox();
        rbNewLibrary = new javax.swing.JRadioButton();
        lDirectory = new javax.swing.JLabel();
        jtFolder = new javax.swing.JTextField();
        jbBrowse = new javax.swing.JButton();
        lVersion = new javax.swing.JLabel();
        jtVersion = new javax.swing.JTextField();
        rbNoneLibrary = new javax.swing.JRadioButton();

        setLayout(new java.awt.CardLayout());

        beanPackageLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/project/jsf/framework/Bundle").getString("MNE_DefaultBackingPackageLabelMnemonic").charAt(0));
        beanPackageLabel.setLabelFor(beanPackageTextField);
        beanPackageLabel.setText(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_DefaultBackingPackageLabel")); // NOI18N

        beanPackageTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                beanPackageTextFieldKeyReleased(evt);
            }
        });

        lServletName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/project/jsf/framework/Bundle").getString("MNE_lServletName").charAt(0));
        lServletName.setLabelFor(tServletName);
        lServletName.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_Servlet_Name")); // NOI18N

        tServletName.setEditable(false);
        tServletName.setText("Faces Servlet");

        lURLPattern.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/project/jsf/framework/Bundle").getString("MNE_lURLPattern").charAt(0));
        lURLPattern.setLabelFor(tURLPattern);
        lURLPattern.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_URL_Pattern")); // NOI18N

        tURLPattern.setText("/faces/*");

        cbValidate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/project/jsf/framework/Bundle").getString("MNE_cbValidate").charAt(0));
        cbValidate.setSelected(true);
        cbValidate.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Validate_XML")); // NOI18N
        cbValidate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbValidate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbVerify.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/project/jsf/framework/Bundle").getString("MNE_cbVerify").charAt(0));
        cbVerify.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Verify_Objects")); // NOI18N
        cbVerify.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbVerify.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbPackageJars.setSelected(true);
        cbPackageJars.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Package_JARs")); // NOI18N
        cbPackageJars.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbPackageJars.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jPanel1.setEnabled(false);
        jPanel1.setFocusable(false);
        jPanel1.setRequestFocusEnabled(false);

        org.jdesktop.layout.GroupLayout confPanelLayout = new org.jdesktop.layout.GroupLayout(confPanel);
        confPanel.setLayout(confPanelLayout);
        confPanelLayout.setHorizontalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 395, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, confPanelLayout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(confPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(cbPackageJars))
                            .add(confPanelLayout.createSequentialGroup()
                                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(beanPackageLabel)
                                    .add(lServletName)
                                    .add(confPanelLayout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(lURLPattern)
                                            .add(cbValidate))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(confPanelLayout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(cbVerify))
                                    .add(tServletName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                                    .add(tURLPattern, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                                    .add(beanPackageTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE))))))
                .add(12, 12, 12))
        );
        confPanelLayout.setVerticalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(beanPackageLabel)
                    .add(beanPackageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lServletName)
                    .add(tServletName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lURLPattern)
                    .add(tURLPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(17, 17, 17)
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbValidate)
                    .add(cbVerify))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbPackageJars)
                .add(51, 51, 51)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        beanPackageTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_DefaultBackingPackageDesc")); // NOI18N
        tServletName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_ServletName")); // NOI18N
        tURLPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_Mapping")); // NOI18N
        cbValidate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_ValidateXML")); // NOI18N
        cbVerify.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_VerifyObjects")); // NOI18N
        cbPackageJars.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_PackageJarToWar")); // NOI18N

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Configuration"), confPanel); // NOI18N

        libPanel.setAlignmentX(0.2F);
        libPanel.setAlignmentY(0.2F);

        buttonGroup1.add(rbRegisteredLibrary);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/project/jsf/framework/Bundle"); // NOI18N
        rbRegisteredLibrary.setText(bundle.getString("LBL_REGISTERED_LIBRARIES")); // NOI18N
        rbRegisteredLibrary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRegisteredLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbRegisteredLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbRegisteredLibraryItemStateChanged(evt);
            }
        });

        cbLibraries.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLibrariesActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbNewLibrary);
        rbNewLibrary.setText(bundle.getString("LBL_CREATE_NEW_LIBRARY")); // NOI18N
        rbNewLibrary.setToolTipText(bundle.getString("MSG_CreatingLibraries")); // NOI18N
        rbNewLibrary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbNewLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbNewLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNewLibraryItemStateChanged(evt);
            }
        });

        lDirectory.setText(bundle.getString("LBL_INSTALL_DIR")); // NOI18N
        lDirectory.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N

        jtFolder.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N
        jtFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtFolderKeyPressed(evt);
            }
        });

        jbBrowse.setText(bundle.getString("LBL_Browse")); // NOI18N
        jbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        lVersion.setText(bundle.getString("LBL_VERSION")); // NOI18N
        lVersion.setToolTipText(bundle.getString("HINT_Version")); // NOI18N

        jtVersion.setToolTipText(bundle.getString("HINT_Version")); // NOI18N
        jtVersion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtVersionKeyReleased(evt);
            }
        });

        buttonGroup1.add(rbNoneLibrary);
        rbNoneLibrary.setText(bundle.getString("LBL_Any_Library")); // NOI18N
        rbNoneLibrary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbNoneLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbNoneLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNoneLibraryItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout libPanelLayout = new org.jdesktop.layout.GroupLayout(libPanel);
        libPanel.setLayout(libPanelLayout);
        libPanelLayout.setHorizontalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(rbRegisteredLibrary)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbLibraries, 0, 332, Short.MAX_VALUE))
                    .add(rbNewLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lDirectory)
                            .add(lVersion))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jtVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                            .add(jtFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jbBrowse))
                    .add(rbNoneLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE))
                .addContainerGap())
        );
        libPanelLayout.setVerticalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbRegisteredLibrary)
                    .add(cbLibraries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbNewLibrary)
                .add(7, 7, 7)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jbBrowse)
                    .add(lDirectory)
                    .add(jtFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lVersion)
                    .add(jtVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbNoneLibrary)
                .addContainerGap(125, Short.MAX_VALUE))
        );

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Libraries"), libPanel); // NOI18N

        add(jsfTabbedPane, "card10");
    }// </editor-fold>//GEN-END:initComponents

    private void beanPackageTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_beanPackageTextField1KeyReleased
// TODO add your handling code here:
    }//GEN-LAST:event_beanPackageTextField1KeyReleased

    private void beanPackageTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_beanPackageTextFieldKeyReleased
        if (beanPackageTextField.getText().length() == 0)
            beanPackageModified = false;
        else
            beanPackageModified = true;
        panel.fireChangeEvent();
    }//GEN-LAST:event_beanPackageTextFieldKeyReleased
    
private void rbNoneLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNoneLibraryItemStateChanged
    updateLibrary();
}//GEN-LAST:event_rbNoneLibraryItemStateChanged

private void jtVersionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtVersionKeyReleased
    checkNewLibrarySetting();
}//GEN-LAST:event_jtVersionKeyReleased

private void rbNewLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNewLibraryItemStateChanged
    updateLibrary();
}//GEN-LAST:event_rbNewLibraryItemStateChanged

private void cbLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLibrariesActionPerformed
    panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()));
}//GEN-LAST:event_cbLibrariesActionPerformed

private void rbRegisteredLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbRegisteredLibraryItemStateChanged
    updateLibrary();
}//GEN-LAST:event_rbRegisteredLibraryItemStateChanged

private void jbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(NbBundle.getMessage(JSFConfigurationPanelVisual.class,"LBL_SelectLibraryLocation")); //NOI18N
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File projectDir = chooser.getSelectedFile();
        jtFolder.setText(projectDir.getAbsolutePath());
        checkNewLibrarySetting();
    }
}//GEN-LAST:event_jbBrowseActionPerformed

private void jtFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtFolderKeyPressed
    checkNewLibrarySetting();
}//GEN-LAST:event_jtFolderKeyPressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel beanPackageLabel;
    protected javax.swing.JTextField beanPackageTextField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbLibraries;
    private javax.swing.JCheckBox cbPackageJars;
    private javax.swing.JCheckBox cbValidate;
    private javax.swing.JCheckBox cbVerify;
    private javax.swing.JPanel confPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbBrowse;
    private javax.swing.JTabbedPane jsfTabbedPane;
    private javax.swing.JTextField jtFolder;
    private javax.swing.JTextField jtVersion;
    private javax.swing.JLabel lDirectory;
    private javax.swing.JLabel lServletName;
    private javax.swing.JLabel lURLPattern;
    private javax.swing.JLabel lVersion;
    private javax.swing.JPanel libPanel;
    private javax.swing.JRadioButton rbNewLibrary;
    private javax.swing.JRadioButton rbNoneLibrary;
    private javax.swing.JRadioButton rbRegisteredLibrary;
    private javax.swing.JTextField tServletName;
    private javax.swing.JTextField tURLPattern;
    // End of variables declaration//GEN-END:variables

    void enableComponents(boolean enable) {
        Component[] components;
        
        components = confPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }
        
        components = libPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }
    }
    
    boolean valid() {
        ExtenderController controller = panel.getController();
        // <RAVE> Checking default Bean Package
        String beanPkg = beanPackageTextField.getText();
        if (!customizer && !JsfProjectUtils.isValidJavaPackageName(beanPkg)) {
            String errMsg = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_InvalidPackage");
            controller.setErrorMessage(errMsg); // NOI18N
            return false;
        }
        // </RAVE>

        String urlPattern = tURLPattern.getText();
        if (urlPattern == null || urlPattern.trim().equals("")){
            controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsEmpty"));
            return false;
        }
        if (!isPatternValid(urlPattern)){
            controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsNotValid"));
            return false;
        }
        
        if (!customizer) {
            Properties properties = controller.getProperties();
            String j2eeLevel = (String)properties.getProperty("j2eeLevel"); //NOI18N
            String currentServerInstanceID = (String)properties.getProperty("serverInstanceID"); //NOI18N
            if (j2eeLevel != null && currentServerInstanceID != null) {
                boolean currentWebModule25Version;
                if (j2eeLevel.equals("1.5")) //NOI81N
                    currentWebModule25Version = true;
                else
                    currentWebModule25Version = false;
                if (!currentServerInstanceID.equals(serverInstanceID) || currentWebModule25Version != webModule25Version) {
                    webModule25Version = currentWebModule25Version;
                    serverInstanceID = currentServerInstanceID;
                    initLibSettings(webModule25Version, serverInstanceID);
                }
            }

            // <RAVE>
            initBackwardsKitSettings("1.5".equals(j2eeLevel), currentServerInstanceID); // NOI18N
            // </RAVE>
        }
        
        // <RAVE>
        boolean addJSF11 = false;
        if (addJSF) {
            if ((rbNewLibrary.isSelected() && (jtFolder.getText().trim().length() <= 0 || jtVersion.getText().trim().length() <= 0))
                || (rbRegisteredLibrary.isSelected() && cbLibraries.getItemCount() <= 0)) {
                addJSF11 = true;
            }
        }

        if (addJSF11 || addJAXRPC || addRowset) {
            controller.setErrorMessage(JsfProjectUtils.getBackwardsKitMesg(addJSF11, addJAXRPC, addRowset)); //NOI18N
            return false;
        }
        // </RAVE>
        
        if (rbNewLibrary.isSelected()) {
            String folder = jtFolder.getText().trim();
            String version = jtVersion.getText().trim();
            if (folder.length() <= 0) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_EmptyJSFFolder")); //NOI18N
                return false;
            }
            File jsfFolder = new File(folder);
            if (!jsfFolder.exists()) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_NonExistingJSFFolder")); //NOI18N
                return false;
            }
            if (!jsfFolder.isDirectory()) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_NotJSFFolder")); //NOI18N
                return false;
            }
            FileObject fo = FileUtil.toFileObject(jsfFolder);
            FileObject[] ch = fo.getChildren();
            int counter = 0;
            for (int i = 0; i < ch.length; i++) {
                FileObject child = ch[i];
                if (child.getName().equalsIgnoreCase("jsf-api") || child.getName().equalsIgnoreCase("jsf-impl")) //NOI18N
                    counter++;
            }
            if (counter != 2) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_NotValidJSFFolder")); //NOI18N
                return false;
            }
            if (version.length() <= 0) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_EmptyJSFVersion")); //NOI18N
                return false;
            }
            Library lib = JSFUtils.getJSFLibrary(version);
            if (lib != null) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_AlreadyExists")); //NOI18N
                return false;
            }
        }

        controller.setErrorMessage(null);
        return true;
    }
    
    // <RAVE> Default Bean Package
    protected boolean isBeanPackageModified() {
         return beanPackageModified;
    }
    // </RAVE>

    private boolean isPatternValid(String pattern){
        if (pattern.startsWith("*.")){
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1
                    && p.indexOf('/') == -1 && !p.trim().equals(""))
                return true;
        }
        // pattern = "/.../*", where ... can't be empty.
        if ((pattern.length() > 3) && pattern.endsWith("/*") && pattern.startsWith("/"))
            return true;
        return false;
    }
    
    void update() {
        Properties properties = panel.getController().getProperties();
        if ("1.5".equals((String)properties.getProperty("j2eeLevel"))) //NOI81N
            webModule25Version = true;
        else
            webModule25Version = false;
        
        serverInstanceID = (String)properties.getProperty("serverInstanceID"); //NOI18N
        initLibSettings(webModule25Version, serverInstanceID);

        // <RAVE>
        initBackwardsKitSettings(webModule25Version, serverInstanceID);
        // </RAVE>
    }
    
    private void initLibSettings(boolean webModule25Version, String serverInstanceID) {
        try {
            addJSF = false;
            File[] cp;
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            // j2eeplatform can be null, when the target server is not accessible.
            if (platform != null) {
                cp = platform.getClasspathEntries();
            }
            else {
                cp = new File[0];
            }
            boolean isJSF = Util.containsClass(Arrays.asList(cp), JSFUtils.FACES_EXCEPTION);
            if (isJSF)
                rbNoneLibrary.setSelected(true);
            else if (webModule25Version) {
                Library jsf12 = LibraryManager.getDefault().getLibrary("jsf12");
                if (jsf12 != null) {
                    rbRegisteredLibrary.setSelected(true);
                    cbLibraries.setSelectedItem(jsf12.getDisplayName());
                } else
                    rbNewLibrary.setSelected(true);
            } else {
                // <RAVE>
                Library lib = LibraryManager.getDefault().getLibrary("jsf1102");
                /*
                Library[] libs = LibraryManager.getDefault().getLibraries();
                Library lib = null;
                for (int i = 0; i < libs.length; i++) {
                    if (libs[i].getDisplayName().startsWith("JSF-")) {
                        lib = libs[i];
                        break;
                    }
                }
                </RAVE> */
                if (lib != null) {
                    rbRegisteredLibrary.setSelected(true);
                    cbLibraries.setSelectedItem(lib.getDisplayName());
                } else
                    rbNewLibrary.setSelected(true);
                    addJSF = true;
            }
        } catch (IOException exc) {
        }
    }

    // <RAVE>
    private void initBackwardsKitSettings(boolean webModule25Version, String serverInstanceID) {
        // It's a J2EE 1.4 project
        addJAXRPC = false;
        if (!webModule25Version) {
            Library libJAXRPC = LibraryManager.getDefault().getLibrary("jaxrpc16"); // NOI18N

            // IDE does not have the JAXRPC support
            if (libJAXRPC == null) {
                // Server does not have the JAXRPC support
                try {
                    File[] cp;
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
                    // j2eeplatform can be null, when the target server is not accessible.
                    if (platform != null) {
                        cp = platform.getClasspathEntries();
                    } else {
                        cp = new File[0];
                    }
                    addJAXRPC = !Util.containsClass(Arrays.asList(cp), "javax.xml.rpc.Service"); // NOI18N
                } catch (IOException exc) {
                }
            }
        }

        String setSrcLevel = (project == null) ? null : JsfProjectUtils.getSourceLevel(project);
        if (setSrcLevel == null) {
            Properties properties = panel.getController().getProperties();
            setSrcLevel = (String) properties.getProperty("setSourceLevel"); //NOI18N
        }

        // It's a J2SE 1.4 project
        addRowset = false;
        if ("1.4".equals(setSrcLevel)) { // NOI18N
            Library libRowset = LibraryManager.getDefault().getLibrary("rowset-ri"); // NOI18N
            // IDE doesn't have the Rowset RI support
            if (libRowset == null) {
                addRowset = true;
            }
        }
    }
    // </RAVE>

    void store(WizardDescriptor d) {
//        projectLocationPanel.store(d);
//        optionsPanel.store(d);
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JSFConfigurationPanelVisual.class);
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    // <RAVE> Default Bean Package
    public String getBeanPackage(){
        return beanPackageTextField.getText();
    }
    
    public void setBeanPackage(String pkg_name){
        beanPackageTextField.setText(pkg_name);
    }
    // </RAVE>
    
    public String getServletName(){
        return tServletName.getText();
    }
    
    public void setServletName(String name){
        tServletName.setText(name);
    }
    
    public String getURLPattern(){
        return tURLPattern.getText();
    }
    
    public void setURLPattern(String pattern){
        tURLPattern.setText(pattern);
    }
    
    public boolean validateXML(){
        return cbValidate.isSelected();
    }
    
    public void setValidateXML(boolean ver){
        cbValidate.setSelected(ver);
    }
    
    public boolean verifyObjects(){
        return cbVerify.isSelected();
    }
    
    public void setVerifyObjects(boolean val){
        cbVerify.setSelected(val);
    }
    
    public boolean packageJars(){
        return cbPackageJars.isSelected();
    }
    
    private void updateLibrary(){
        if (cbLibraries.getItemCount() == 0)
            rbRegisteredLibrary.setEnabled(false);
        
        if (rbNoneLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NONE);
            panel.getController().setErrorMessage(null);
        } else if (rbRegisteredLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(true);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.USED);
            if (jsfLibraries.size() > 0){
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()));
            }
            panel.getController().setErrorMessage(null);
        } else if (rbNewLibrary.isSelected()){
            enableNewLibraryComponent(true);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NEW);
            checkNewLibrarySetting();
        }
    }
    
    private void enableDefinedLibraryComponent(boolean enabled){
        cbLibraries.setEnabled(enabled);
    }
    
    private void enableNewLibraryComponent(boolean enabled){
        lDirectory.setEnabled(enabled);
        jtFolder.setEnabled(enabled);
        jbBrowse.setEnabled(enabled);
        lVersion.setEnabled(enabled);
        jtVersion.setEnabled(enabled);
    }

    private void checkNewLibrarySetting(){
        String message = null;
        String fileName = jtFolder.getText();
        if (fileName == null || "".equals(fileName)){
            message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_PathIsNotFaceletsFolder");
        } else {
            File folder = new File(fileName);
            if (!JSFUtils.isJSFInstallFolder(folder)){
                message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_PathIsNotFaceletsFolder");
            } else {
                panel.setInstallFolder(folder);
                
                String version = jtVersion.getText().trim();
                if (version == null || "".equals(version)){
                    message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_VersionHasToBeDefined");
                } else{
                    String name = "jsf-"+ JSFUtils.convertLibraryVersion(version);  //NOI18N
                    int length = jsfLibraries.size();
                    for (int i = 0; i < length; i++) {
                        if(jsfLibraries.get(i).getName().equals(name)){
                            message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_VersionAlreadyExist");
                        }
                    }
                }
                if (message == null){
                    panel.setNewLibraryVersion(jtVersion.getText().trim());
                }
            }
        }
        panel.getController().setErrorMessage(message);
    }

}
