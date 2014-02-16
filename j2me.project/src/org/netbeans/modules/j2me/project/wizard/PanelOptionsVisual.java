/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2013 Sun
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
package org.netbeans.modules.j2me.project.wizard;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.StringTokenizer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.j2me.project.wizard.J2MEProjectWizardIterator.WizardType;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;

/**
 * @author Roman Svitanic
 */
public class PanelOptionsVisual extends SettingsPanel implements PropertyChangeListener, DocumentListener {

    private static boolean lastMainClassCheck = true; // XXX Store somewhere
    private final WizardType type;
    private PanelConfigureProject panel;
    private String currentLibrariesLocation;
    private String projectLocation;
    private boolean isMainClassValid;

    private PlatformDevicesPanel platformDevicesPanel;

    PanelOptionsVisual(PanelConfigureProject panel, WizardType type) {
        this.panel = panel;
        this.type = type;

        preInitComponents();
        initComponents();
        postInitComponents();
    }

    private void preInitComponents() {
    }

    private void postInitComponents() {
        platformDevicesPanel = new PlatformDevicesPanel(panel, type);
        platformDevicesContainer.add(platformDevicesPanel, BorderLayout.CENTER);

        currentLibrariesLocation = "." + File.separatorChar + "lib"; // NOI18N
        txtLibFolder.setText(currentLibrariesLocation);
        cbSharableActionPerformed(null);

        switch (type) {
            case APPLICATION:
                createMainCheckBox.setSelected(lastMainClassCheck);
                mainClassTextField.setEnabled(lastMainClassCheck);
                break;
            case SAMPLE:
                setBottomPanelAreaVisible(false);
                break;
        }

        mainClassTextField.getDocument().addDocumentListener(this);
        txtLibFolder.getDocument().addDocumentListener(this);
        progressLabel.setVisible(false);
        progressPanel.setVisible(false);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final String propName = event.getPropertyName();
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(propName)) {
            final String projectName = (String) event.getNewValue();
            mainClassTextField.setText(createMainClassName(projectName, type));
        } else if (PanelProjectLocationVisual.PROP_PROJECT_LOCATION.equals(propName)) {
            projectLocation = (String) event.getNewValue();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged(e.getDocument());
    }

    private void documentChanged(Document doc) {
        if (txtLibFolder.getDocument().equals(doc)) {
            librariesLocationChanged();
        } else if (mainClassTextField.getDocument().equals(doc)) {
            mainClassChanged();
        }
    }

    private static String createMainClassName(final String projectName, final WizardType type) {

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
            main.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_MidletName")); // NOI18N
        }
        return pkg.length() == 0 ? main.toString() : String.format("%s.%s", pkg.toString(), main.toString()); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cbSharable = new javax.swing.JCheckBox();
        lblLibFolder = new javax.swing.JLabel();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        btnLibFolder = new javax.swing.JButton();
        lblHint = new javax.swing.JLabel();
        txtLibFolder = new javax.swing.JTextField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel2 = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        progressPanel = new javax.swing.JPanel();
        platformDevicesContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        cbSharable.setSelected(SharableLibrariesUtils.isLastProjectSharable());
        org.openide.awt.Mnemonics.setLocalizedText(cbSharable, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_SharableProject_Checkbox")); // NOI18N
        cbSharable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSharableActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cbSharable, gridBagConstraints);
        cbSharable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_sharableProject")); // NOI18N
        cbSharable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_sharableProject")); // NOI18N

        lblLibFolder.setLabelFor(txtLibFolder);
        org.openide.awt.Mnemonics.setLocalizedText(lblLibFolder, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Location_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(lblLibFolder, gridBagConstraints);
        lblLibFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_labelLibrariesFolder")); // NOI18N
        lblLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_labelLibrariesFolder")); // NOI18N

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createMainCheckBox")); // NOI18N
        createMainCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createMainCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 10);
        add(createMainCheckBox, gridBagConstraints);
        createMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_createMainCheckBox")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_createMainCheckBox")); // NOI18N

        mainClassTextField.setText("com.myapp.Midlet");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        add(mainClassTextField, gridBagConstraints);
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 13, 0);
        add(jSeparator2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(btnLibFolder, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Browse_Button")); // NOI18N
        btnLibFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLibFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(btnLibFolder, gridBagConstraints);
        btnLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_browseLibraries")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "HINT_LibrariesFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(lblHint, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.2;
        jPanel1.add(txtLibFolder, gridBagConstraints);
        txtLibFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_LibrariesLocation")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        add(filler2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jPanel2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Progress_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(progressLabel, gridBagConstraints);
        progressLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_progressLabel")); // NOI18N
        progressLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_progressLabel")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(progressPanel, gridBagConstraints);

        platformDevicesContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(platformDevicesContainer, gridBagConstraints);

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

private void createMainCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createMainCheckBoxItemStateChanged
    lastMainClassCheck = createMainCheckBox.isSelected();
    mainClassTextField.setEnabled(lastMainClassCheck);
    this.panel.fireChangeEvent();
}//GEN-LAST:event_createMainCheckBoxItemStateChanged

    private void setBottomPanelAreaVisible(boolean visible) {
        cbSharable.setVisible(visible);
        lblLibFolder.setVisible(visible);
        jPanel1.setVisible(visible);
        txtLibFolder.setVisible(visible);
        btnLibFolder.setVisible(visible);
        lblHint.setVisible(visible);
        createMainCheckBox.setVisible(visible);
        mainClassTextField.setVisible(visible);
        jSeparator2.setVisible(visible);
    }

    @Override
    boolean valid(WizardDescriptor settings) {
        setBottomPanelAreaVisible(type != WizardType.SAMPLE);

        if (cbSharable.isSelected()) {
            String location = txtLibFolder.getText();
            if (projectLocation != null) {
                if (new File(location).isAbsolute()) {
                    settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.absolutePath")); // NOI18N
                } else {
                    File projectLoc = FileUtil.normalizeFile(new File(projectLocation));
                    File libLoc = PropertyUtils.resolveFile(projectLoc, location);
                    if (!CollocationQuery.areCollocated(projectLoc.toURI(), libLoc.toURI())) {
                        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(PanelOptionsVisual.class, "WARN_PanelOptionsVisual.relativePath")); // NOI18N
                    }
                }
            }
        }

        if (mainClassTextField.isVisible() && mainClassTextField.isEnabled()) {
            if (!isMainClassValid) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelOptionsVisual.class, "ERROR_IllegalMainClassName")); // NOI18N
                return false;
            }
        }
        return true && platformDevicesPanel.valid(settings);
    }

    @Override
    synchronized void read(WizardDescriptor d) {
        platformDevicesPanel.read(d);
    }

    @Override
    void validate(WizardDescriptor d) throws WizardValidationException {
        platformDevicesPanel.validate(d);
    }

    @Override
    void store(WizardDescriptor d) {
        //TODO: store values from panel when leaving this panel
        d.putProperty(J2MEProjectWizardIterator.MIDLET_CLASS, createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null);
        d.putProperty(J2MEProjectWizardIterator.SHARED_LIBRARIES, cbSharable.isSelected() ? txtLibFolder.getText() : null);
        platformDevicesPanel.store(d);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLibFolder;
    private javax.swing.JCheckBox cbSharable;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblLibFolder;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JPanel platformDevicesContainer;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JTextField txtLibFolder;
    // End of variables declaration//GEN-END:variables

    private void mainClassChanged() {
        String mainClassName = mainClassTextField.getText();
        StringTokenizer tk = new StringTokenizer(mainClassName, "."); //NOI18N
        boolean isValid = true;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                isValid = false;
                break;
            }
        }
        isMainClassValid = !mainClassName.isEmpty() && isValid;
        panel.fireChangeEvent();
    }

    private void librariesLocationChanged() {
        panel.fireChangeEvent();
    }
}
