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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.ExtenderController.Properties;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Pisl, Radko Najman
 */
public class JSFConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener  {

    private JSFConfigurationPanel panel;
    private boolean customizer;
    
    private final List<LibraryItem> jsfLibraries = new ArrayList<LibraryItem>();
    private boolean libsInitialized;
    private boolean webModule25Version;
    private String serverInstanceID;
    
    
    /** Creates new form JSFConfigurationPanelVisual */
    public JSFConfigurationPanelVisual(JSFConfigurationPanel panel, boolean customizer) {
        this.panel = panel;
        this.customizer = customizer;
        
        initComponents();
        
        tURLPattern.getDocument().addDocumentListener(this);
        cbPackageJars.setVisible(false);

    }

    @Override
    public void addNotify() {
        super.addNotify();
        initLibraries();

        if (customizer) {
            enableComponents(false);
        } else {
            updateLibrary();
        }
    }

    void initLibraries() {
        if (libsInitialized) {
            return;
        }

        Vector <String> items = new Vector <String>();
        jsfLibraries.clear();
        List<URL> content;
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { // NOI18N
                continue;
            }

            content = library.getContent("classpath"); //NOI18N
            try {
                if (Util.containsClass(content, JSFUtils.FACES_EXCEPTION)) {
                    items.add(library.getDisplayName());
                    boolean isJSF12 = Util.containsClass(content, JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
                    if (isJSF12) {
                        jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_2));
                    } else {
                        jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_1));
                    }
                }
            } catch (IOException exception) {
                Exceptions.printStackTrace(exception);
            }
        }

        cbLibraries.setModel(new DefaultComboBoxModel(items));
        if (items.size() == 0) {
            rbRegisteredLibrary.setEnabled(false);
            cbLibraries.setEnabled(false);
            rbNewLibrary.setSelected(true);
            panel.setLibrary(null);
        } else {
            rbRegisteredLibrary.setEnabled(true);
            rbRegisteredLibrary.setSelected(true);
            cbLibraries.setEnabled(true);
        }

        libsInitialized = true;
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jsfTabbedPane = new javax.swing.JTabbedPane();
        confPanel = new javax.swing.JPanel();
        lServletName = new javax.swing.JLabel();
        tServletName = new javax.swing.JTextField();
        lURLPattern = new javax.swing.JLabel();
        tURLPattern = new javax.swing.JTextField();
        cbValidate = new javax.swing.JCheckBox();
        cbVerify = new javax.swing.JCheckBox();
        cbPackageJars = new javax.swing.JCheckBox();
        libPanel = new javax.swing.JPanel();
        rbRegisteredLibrary = new javax.swing.JRadioButton();
        cbLibraries = new javax.swing.JComboBox();
        rbNewLibrary = new javax.swing.JRadioButton();
        lDirectory = new javax.swing.JLabel();
        jtFolder = new javax.swing.JTextField();
        jbBrowse = new javax.swing.JButton();
        lVersion = new javax.swing.JLabel();
        jtNewLibraryName = new javax.swing.JTextField();
        rbNoneLibrary = new javax.swing.JRadioButton();

        setLayout(new java.awt.CardLayout());

        lServletName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lServletName").charAt(0));
        lServletName.setLabelFor(tServletName);
        lServletName.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_Servlet_Name")); // NOI18N

        tServletName.setEditable(false);
        tServletName.setText("Faces Servlet");

        lURLPattern.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lURLPattern").charAt(0));
        lURLPattern.setLabelFor(tURLPattern);
        lURLPattern.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_URL_Pattern")); // NOI18N

        tURLPattern.setText("/faces/*");

        cbValidate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_cbValidate").charAt(0));
        cbValidate.setSelected(true);
        cbValidate.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Validate_XML")); // NOI18N
        cbValidate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbVerify.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_cbVerify").charAt(0));
        cbVerify.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Verify_Objects")); // NOI18N
        cbVerify.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbPackageJars.setSelected(true);
        cbPackageJars.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Package_JARs")); // NOI18N
        cbPackageJars.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout confPanelLayout = new org.jdesktop.layout.GroupLayout(confPanel);
        confPanel.setLayout(confPanelLayout);
        confPanelLayout.setHorizontalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(confPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(cbPackageJars))
                    .add(confPanelLayout.createSequentialGroup()
                        .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(confPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lURLPattern)
                                    .add(cbValidate)))
                            .add(confPanelLayout.createSequentialGroup()
                                .add(11, 11, 11)
                                .add(lServletName)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tServletName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                            .add(cbVerify)
                            .add(tURLPattern, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))))
                .addContainerGap())
        );
        confPanelLayout.setVerticalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap())
        );

        tServletName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_ServletName")); // NOI18N
        tURLPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_Mapping")); // NOI18N
        cbValidate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_ValidateXML")); // NOI18N
        cbVerify.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_VerifyObjects")); // NOI18N
        cbPackageJars.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_PackageJarToWar")); // NOI18N

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Configuration"), confPanel); // NOI18N

        libPanel.setAlignmentX(0.2F);
        libPanel.setAlignmentY(0.2F);

        buttonGroup1.add(rbRegisteredLibrary);
        rbRegisteredLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbRegLibs").charAt(0));
        rbRegisteredLibrary.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        rbRegisteredLibrary.setText(bundle.getString("LBL_REGISTERED_LIBRARIES")); // NOI18N
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
        rbNewLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbCrNewLib").charAt(0));
        rbNewLibrary.setText(bundle.getString("LBL_CREATE_NEW_LIBRARY")); // NOI18N
        rbNewLibrary.setToolTipText(bundle.getString("MSG_CreatingLibraries")); // NOI18N
        rbNewLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbNewLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNewLibraryItemStateChanged(evt);
            }
        });

        lDirectory.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lJSFDir").charAt(0));
        lDirectory.setLabelFor(jtFolder);
        lDirectory.setText(bundle.getString("LBL_INSTALL_DIR")); // NOI18N
        lDirectory.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N

        jtFolder.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N
        jtFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtFolderKeyPressed(evt);
            }
        });

        jbBrowse.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Browse").charAt(0));
        jbBrowse.setText(bundle.getString("LBL_Browse")); // NOI18N
        jbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        lVersion.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lJSFVer").charAt(0));
        lVersion.setLabelFor(jtNewLibraryName);
        lVersion.setText(bundle.getString("LBL_VERSION")); // NOI18N
        lVersion.setToolTipText(bundle.getString("HINT_Version")); // NOI18N

        jtNewLibraryName.setToolTipText(bundle.getString("HINT_Version")); // NOI18N
        jtNewLibraryName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtNewLibraryNameKeyReleased(evt);
            }
        });

        buttonGroup1.add(rbNoneLibrary);
        rbNoneLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbNoAppend").charAt(0));
        rbNoneLibrary.setText(bundle.getString("LBL_Any_Library")); // NOI18N
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
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbLibraries, 0, 338, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, libPanelLayout.createSequentialGroup()
                                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jtNewLibraryName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                                    .add(jtFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jbBrowse))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, rbNewLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                    .add(rbNoneLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .add(lDirectory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))))
                .addContainerGap())
        );
        libPanelLayout.setVerticalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbRegisteredLibrary)
                    .add(cbLibraries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(rbNewLibrary)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jbBrowse)
                    .add(jtFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lDirectory))
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jtNewLibraryName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lVersion))
                .add(1, 1, 1)
                .add(rbNoneLibrary)
                .addContainerGap())
        );

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Libraries"), libPanel); // NOI18N

        add(jsfTabbedPane, "card10");
        jsfTabbedPane.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

private void rbNoneLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNoneLibraryItemStateChanged
    updateLibrary();
    if (rbNoneLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbNoneLibraryItemStateChanged

private void jtNewLibraryNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtNewLibraryNameKeyReleased
    panel.setNewLibraryName(jtNewLibraryName.getText().trim());
}//GEN-LAST:event_jtNewLibraryNameKeyReleased

private void rbNewLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNewLibraryItemStateChanged
    updateLibrary();
    if (rbNewLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbNewLibraryItemStateChanged

private void cbLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLibrariesActionPerformed
    panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
}//GEN-LAST:event_cbLibrariesActionPerformed

private void rbRegisteredLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbRegisteredLibraryItemStateChanged
    updateLibrary();
    if (rbRegisteredLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbRegisteredLibraryItemStateChanged

private void jbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(NbBundle.getMessage(JSFConfigurationPanelVisual.class,"LBL_SelectLibraryLocation")); //NOI18N
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setCurrentDirectory(new File(jtFolder.getText().trim()));
    
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File projectDir = chooser.getSelectedFile();
        jtFolder.setText(projectDir.getAbsolutePath());
        setNewLibraryFolder();
    }
}//GEN-LAST:event_jbBrowseActionPerformed

private void jtFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtFolderKeyPressed
    setNewLibraryFolder();
}//GEN-LAST:event_jtFolderKeyPressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbLibraries;
    private javax.swing.JCheckBox cbPackageJars;
    private javax.swing.JCheckBox cbValidate;
    private javax.swing.JCheckBox cbVerify;
    private javax.swing.JPanel confPanel;
    private javax.swing.JButton jbBrowse;
    private javax.swing.JTabbedPane jsfTabbedPane;
    private javax.swing.JTextField jtFolder;
    private javax.swing.JTextField jtNewLibraryName;
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
        String urlPattern = tURLPattern.getText();
        if (urlPattern == null || urlPattern.trim().equals("")) { // NOI18N
            controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsEmpty"));
            return false;
        }
        if (!isPatternValid(urlPattern)) {
            controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsNotValid"));
            return false;
        }
        
        if (customizer) {
            return true;
        }

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
        
        if (rbRegisteredLibrary.isSelected()) {
            if (cbLibraries.getItemCount() <= 0) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_MissingJSF")); //NOI18N
                return false;
            }
            if (!webModule25Version) {
                if (jsfLibraries.size() == 0) {
                    return false;
                }
                int index = cbLibraries.getSelectedIndex();
                JSFVersion libraryVersion = jsfLibraries.get(index).getVersion();
                if (libraryVersion.compareTo(JSFVersion.JSF_1_1) > 0) {
                    controller.setErrorMessage(NbBundle.getMessage(JSFUtils.class, "ERROR_REQUIRED_JSF_VERSION"));
                    return false;
                }
            }
            
        }
        
        if (rbNewLibrary.isSelected()) {
            // checking, whether the folder is the right one
            String folder = jtFolder.getText().trim();
            String message;
            
            if (webModule25Version) {
                message = JSFUtils.isJSFInstallFolder(new File(folder), JSFVersion.JSF_1_2);
            } else {
                message = JSFUtils.isJSFInstallFolder(new File(folder), JSFVersion.JSF_1_1);
            }
            if (message != null) {
                controller.setErrorMessage(message); 
                return false;
            }
            // checking new library name
            String newLibraryName = jtNewLibraryName.getText().trim();
            if (newLibraryName.length() <= 0) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_EmptyNewLibraryName")); //NOI18N
                return false;
            }
            
            message = checkLibraryName(newLibraryName);
            if (message != null) {
                controller.setErrorMessage(message); 
                return false;
            }
            Library lib = LibraryManager.getDefault().getLibrary(newLibraryName);
            if (lib != null) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_AlreadyExists")); //NOI18N
                return false;
            }
        }
                
        controller.setErrorMessage(null);
        return true;
    }
    
    private static final char[] INVALID_PATTERN_CHARS = {'%', '+'}; // NOI18N

    private boolean isPatternValid(String pattern) {
        for (char c : INVALID_PATTERN_CHARS) {
            if (pattern.indexOf(c) != -1) {
                return false;
            }
        }
        
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
    }
    
    /**  Method looks at the project classpath and is looking for javax.faces.FacesException.
     *   If there is not this class on the classpath, then is offered appropriate jsf library
     *   according web module version.
     */
    private void initLibSettings(boolean webModule25Version, String serverInstanceID) {
        try {
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
            boolean isJSF12 = Util.containsClass(Arrays.asList(cp), JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
            
            if ((isJSF && isJSF12 && webModule25Version) // JSF 1.2 for Java EE 5 
                    || (isJSF && !webModule25Version)){ // JSF 1.1 for J2EE 1.x
                rbNoneLibrary.setSelected(true);
            }
            else {
                Library profferedLibrary = null;
                if (webModule25Version) {
                    //if the web module follows 2.5 specification, select jsf 1.2, which is budnled
                    profferedLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSF_1_2_NAME);
                }
                else {
                    // select the jsf 1.1 library, if it's installed
                    profferedLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSF_1_1_NAME);
                }
                
                if (profferedLibrary != null) {
                    // if there is a proffered library, select
                    rbRegisteredLibrary.setSelected(true);
                    cbLibraries.setSelectedItem(profferedLibrary.getDisplayName());
                }
                else {
                    // there is not a proffered library -> select one or select creating new one
                    if (jsfLibraries.size() == 0) {
                        rbNewLibrary.setSelected(true);
                    }
                }
            }
        } catch (IOException exception) {
            Exceptions.printStackTrace(exception);
        }
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
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
            }
            panel.getController().setErrorMessage(null);
        } else if (rbNewLibrary.isSelected()){
            enableNewLibraryComponent(true);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NEW);
            setNewLibraryFolder();
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
        jtNewLibraryName.setEnabled(enabled);
    }

    private void setNewLibraryFolder() {
        String fileName = jtFolder.getText();
        
        if (fileName == null || "".equals(fileName)) {
            panel.setInstallFolder(null);
        } else {
            File folder = new File(fileName);
            panel.setInstallFolder(folder);            
        }
    }
    
    // the name of the library is used as ant property
    private static final Pattern VALID_PROPERTY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N
    
    private String checkLibraryName(String name) {
        String message = null;
        if (name.length() == 0) {
            message = NbBundle.getMessage(JSFUtils.class, "ERROR_InvalidLibraryName");
        } else {
            if (!VALID_PROPERTY_NAME.matcher(name).matches()) {
                message = NbBundle.getMessage(JSFUtils.class, "ERROR_InvalidLibraryNameCharacters");
            }
        }
        return message;
    }
    
    private static class LibraryItem {
        
        private Library library;
        private JSFVersion version;
        
        public LibraryItem(Library library, JSFVersion version) {
            this.library = library;
            this.version = version;
        }
        
        public Library getLibrary() {
            return library;
        }

        public JSFVersion getVersion() {
            return version;
        }
        
        public String toString() {
            return library.getDisplayName();
        }
    }
}
