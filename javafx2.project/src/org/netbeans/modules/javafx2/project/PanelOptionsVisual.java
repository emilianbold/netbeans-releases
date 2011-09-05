/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.javafx2.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.StringTokenizer;
import javax.swing.ComboBoxModel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * @author  phrebejk, Anton Chechel
 */
public class PanelOptionsVisual extends SettingsPanel implements ActionListener, PropertyChangeListener {

    private static boolean lastMainClassCheck = true; // XXX Store somewhere
    public static final String SHARED_LIBRARIES = "sharedLibraries"; // NOI18N
    private PanelConfigureProject panel;
    private boolean valid;
    private String currentLibrariesLocation;
    private String projectLocation;
    private final NewJFXProjectWizardIterator.WizardType type;
    
    private ComboBoxModel platformsModel;
    private ListCellRenderer platformsCellRenderer;
    private JavaPlatformChangeListener jpcl;

    public PanelOptionsVisual(PanelConfigureProject panel, NewJFXProjectWizardIterator.WizardType type) {
        platformsModel = PlatformUiSupport.createPlatformComboBoxModel("default_platform");
        platformsCellRenderer = PlatformUiSupport.createPlatformListCellRenderer();
        
        initComponents();
        // copied from CustomizerLibraries
        platformComboBox.putClientProperty ("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
        jpcl = new JavaPlatformChangeListener();
        JavaPlatformManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(jpcl, JavaPlatformManager.getDefault()));

        this.panel = panel;
        this.type = type;
        currentLibrariesLocation = "." + File.separatorChar + "lib"; // NOI18N
        txtLibFolder.setText(currentLibrariesLocation);
        cbSharableActionPerformed(null);

        switch (type) {
            case LIB:
                setAsMainCheckBox.setVisible(false);
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                break;
            case APP:
                createMainCheckBox.addActionListener(this);
                createMainCheckBox.setSelected(lastMainClassCheck);
                mainClassTextField.setEnabled(lastMainClassCheck);
                break;
            case EXT:
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                break;
        }

        setAsMainCheckBox.setSelected(WizardSettings.getSetAsMain(type));
        this.mainClassTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                mainClassChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                mainClassChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                mainClassChanged();
            }
        });
        this.txtLibFolder.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                librariesLocationChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                librariesLocationChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                librariesLocationChanged();
            }
        });
        
//        J2SEProjectProperties uiProps = context.lookup(J2SEProjectProperties.class);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createMainCheckBox) {
            lastMainClassCheck = createMainCheckBox.isSelected();
            mainClassTextField.setEnabled(lastMainClassCheck);
            this.panel.fireChangeEvent();
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final String propName = event.getPropertyName();
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(propName)) {
            final String projectName = (String) event.getNewValue();
            this.mainClassTextField.setText(createMainClassName(projectName));
        } else if (PanelProjectLocationVisual.PROP_PROJECT_LOCATION.equals(propName)) {
            projectLocation = (String) event.getNewValue();
        }
    }

    static String createMainClassName(final String projectName) {

        final StringBuilder pkg = new StringBuilder();
        final StringBuilder main = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        boolean needsEscape = false;
        String part;
        for (int i = 0; i < projectName.length(); i++) {
            final char c = projectName.charAt(i);
            if (first) {
                if (!Character.isJavaIdentifierStart(c)) {
                    if (Character.isJavaIdentifierPart(c)) {
                        needsEscape = true;
                        sb.append(c);
                        first = false;
                    }
                } else {
                    sb.append(c);
                    first = false;
                }
            } else {
                if (Character.isJavaIdentifierPart(c)) {
                    sb.append(c);
                } else if (sb.length() > 0) {
                    part = sb.toString();
                    if (pkg.length() > 0) {
                        pkg.append('.');    //NOI18N
                    }
                    if (needsEscape || !Utilities.isJavaIdentifier(part.toLowerCase())) {
                        pkg.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PackageNamePrefix")); // NOI18N
                    }
                    pkg.append(part.toLowerCase());
                    if (!needsEscape || main.length() > 0) {
                        main.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                    }
                    sb = new StringBuilder();
                    first = true;
                    needsEscape = false;
                }
            }
        }
        if (sb.length() > 0) {
            part = sb.toString();
            if (pkg.length() > 0) {
                pkg.append('.'); // NOI18N
            }
            if (needsEscape || !Utilities.isJavaIdentifier(part.toLowerCase())) {
                pkg.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PackageNamePrefix")); // NOI18N
            }
            pkg.append(part.toLowerCase());
            if (!needsEscape || main.length() > 0) {
                main.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        if (main.length() == 0) {
            main.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_ClassName")); // NOI18N
        }
        return pkg.length() == 0 ? main.toString() : String.format("%s.%s", pkg.toString(), main.toString()); // NOI18N
    }

    private JavaPlatform getSelectedPlatform() {
        Object selectedItem = this.platformComboBox.getSelectedItem();
        JavaPlatform platform = (selectedItem == null ? null : PlatformUiSupport.getPlatform(selectedItem));
        return platform;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbSharable = new javax.swing.JCheckBox();
        lblLibFolder = new javax.swing.JLabel();
        txtLibFolder = new javax.swing.JTextField();
        btnLibFolder = new javax.swing.JButton();
        lblHint = new javax.swing.JLabel();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        setAsMainCheckBox = new javax.swing.JCheckBox();
        lblPlatform = new javax.swing.JLabel();
        platformComboBox = new javax.swing.JComboBox();
        btnManagePlatforms = new javax.swing.JButton();
        preloaderCheckBox = new javax.swing.JCheckBox();
        lblPreloaderProject = new javax.swing.JLabel();
        txtPreloaderProject = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();

        cbSharable.setSelected(SharableLibrariesUtils.isLastProjectSharable());
        org.openide.awt.Mnemonics.setLocalizedText(cbSharable, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_SharableProject_Checkbox")); // NOI18N
        cbSharable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSharableActionPerformed(evt);
            }
        });

        lblLibFolder.setLabelFor(txtLibFolder);
        org.openide.awt.Mnemonics.setLocalizedText(lblLibFolder, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Location_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnLibFolder, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Browse_Button")); // NOI18N
        btnLibFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLibFolderActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "HINT_LibrariesFolder")); // NOI18N

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createMainCheckBox")); // NOI18N

        mainClassTextField.setText("com.myapp.Main");

        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_setAsMainCheckBox")); // NOI18N

        lblPlatform.setLabelFor(platformComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(lblPlatform, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Platform_ComboBox")); // NOI18N

        platformComboBox.setModel(platformsModel);
        platformComboBox.setRenderer(platformsCellRenderer);
        platformComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                platformComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnManagePlatforms, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Manage_Button")); // NOI18N
        btnManagePlatforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManagePlatformsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(preloaderCheckBox, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Preloader_Checkbox")); // NOI18N
        preloaderCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                preloaderCheckBoxItemStateChanged(evt);
            }
        });

        lblPreloaderProject.setLabelFor(txtPreloaderProject);
        org.openide.awt.Mnemonics.setLocalizedText(lblPreloaderProject, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_PreloaderName_TextBox")); // NOI18N
        lblPreloaderProject.setEnabled(false);

        txtPreloaderProject.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PanelOptions_Preloader_Project_Name")); // NOI18N
        txtPreloaderProject.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblPreloaderProject)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPreloaderProject, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPlatform)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(platformComboBox, 0, 272, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnManagePlatforms))
            .addGroup(layout.createSequentialGroup()
                .addComponent(preloaderCheckBox)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(cbSharable)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblLibFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLibFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLibFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(createMainCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainClassTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                    .addComponent(setAsMainCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPlatform)
                    .addComponent(platformComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManagePlatforms))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preloaderCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPreloaderProject)
                    .addComponent(txtPreloaderProject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSharable)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtLibFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLibFolder))
                    .addComponent(lblLibFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createMainCheckBox)
                    .addComponent(mainClassTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setAsMainCheckBox)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        cbSharable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_sharableProject")); // NOI18N
        txtLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_LibrariesLocation")); // NOI18N
        btnLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_browseLibraries")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_createMainCheckBox")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_createMainCheckBox")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_setAsMainCheckBox")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbSharableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSharableActionPerformed
        txtLibFolder.setEnabled(cbSharable.isSelected());
        btnLibFolder.setEnabled(cbSharable.isSelected());
        lblHint.setEnabled(cbSharable.isSelected());
        lblLibFolder.setEnabled(cbSharable.isSelected());
        if (cbSharable.isSelected()) {
            txtLibFolder.setText(currentLibrariesLocation);
        } else {
            txtLibFolder.setText(""); //NOi18N
        }
}//GEN-LAST:event_cbSharableActionPerformed

    private void btnLibFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLibFolderActionPerformed
        // below folder is used just for relativization:
        File f = FileUtil.normalizeFile(new File(projectLocation
                + File.separatorChar + "project_folder")); // NOI18N
        String curr = SharableLibrariesUtils.browseForLibraryLocation(txtLibFolder.getText().trim(), this, f);
        if (curr != null) {
            currentLibrariesLocation = curr;
            if (cbSharable.isSelected()) {
                txtLibFolder.setText(currentLibrariesLocation);
            }
        }
}//GEN-LAST:event_btnLibFolderActionPerformed

private void btnManagePlatformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManagePlatformsActionPerformed
        PlatformsCustomizer.showCustomizer(getSelectedPlatform());
}//GEN-LAST:event_btnManagePlatformsActionPerformed

private void platformComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_platformComboBoxItemStateChanged
        this.panel.fireChangeEvent();
}//GEN-LAST:event_platformComboBoxItemStateChanged

private void preloaderCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_preloaderCheckBoxItemStateChanged
    txtPreloaderProject.setEnabled(preloaderCheckBox.isSelected());
}//GEN-LAST:event_preloaderCheckBoxItemStateChanged

    @Override
    boolean valid(WizardDescriptor settings) {
        if (!JavaFXPlatformUtils.isJavaFXEnabled(getSelectedPlatform())) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.notFXPlatform")); // NOI18N
            return false;
        }
        
        if (cbSharable.isSelected()) {
            String location = txtLibFolder.getText();
            if (projectLocation != null) {
                if (new File(location).isAbsolute()) {
                    settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.absolutePath")); // NOI18N
                } else {
                    File projectLoc = FileUtil.normalizeFile(new File(projectLocation));
                    File libLoc = PropertyUtils.resolveFile(projectLoc, location);
                    if (!CollocationQuery.areCollocated(projectLoc, libLoc)) {
                        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.relativePath")); // NOI18N
                    }
                }
            }
        }

        if (mainClassTextField.isVisible() && mainClassTextField.isEnabled()) {
            if (!valid) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelOptionsVisual.class, "ERROR_IllegalMainClassName")); // NOI18N
            }
            return this.valid;
        } else {
            return true;
        }
    }

    @Override
    void read(WizardDescriptor d) {
    }

    @Override
    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    @Override
    void store(WizardDescriptor d) {
        Templates.setDefinesMainProject(d, setAsMainCheckBox.isSelected());
        WizardSettings.setSetAsMain(type, setAsMainCheckBox.isSelected());
        d.putProperty("mainClass", createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null); // NOI18N
        d.putProperty(SHARED_LIBRARIES, cbSharable.isSelected() ? txtLibFolder.getText() : null);
        
        String platformName = getSelectedPlatform().getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
        d.putProperty(JFXProjectProperties.JAVA_PLATFORM_NAME, platformName);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLibFolder;
    private javax.swing.JButton btnManagePlatforms;
    private javax.swing.JCheckBox cbSharable;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblLibFolder;
    private javax.swing.JLabel lblPlatform;
    private javax.swing.JLabel lblPreloaderProject;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JComboBox platformComboBox;
    private javax.swing.JCheckBox preloaderCheckBox;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JTextField txtLibFolder;
    private javax.swing.JTextField txtPreloaderProject;
    // End of variables declaration//GEN-END:variables

    private void mainClassChanged() {
        String mainClassName = this.mainClassTextField.getText();
        StringTokenizer tk = new StringTokenizer(mainClassName, "."); //NOI18N
        boolean valid = true;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                valid = false;
                break;
            }
        }
        this.valid = valid;
        this.panel.fireChangeEvent();
    }

    private void librariesLocationChanged() {
        this.panel.fireChangeEvent();

    }
    
    private class JavaPlatformChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
//            if (evt.getPropertyName().equals(JavaFXPlatformUtils.PROPERTY_JAVA_FX)) {
                PanelOptionsVisual.this.panel.fireChangeEvent();
//            }
        }
        
    }
}
