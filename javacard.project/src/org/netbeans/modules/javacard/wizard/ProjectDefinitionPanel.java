/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.wizard;

import com.sun.javacard.AID;
import java.awt.Component;
import java.awt.event.ActionListener;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.constants.ProjectWizardKeys;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

public class ProjectDefinitionPanel extends JPanel implements DocumentListener, FocusListener, LookupListener, ActionListener {

    private final ChangeSupport supp = new ChangeSupport(this);
    private final ProjectKind kind;
    private final Lookup.Result<JavacardPlatform> platformRes;
    private final Lookup.Result<Card> serverRes;

    public ProjectDefinitionPanel(final ProjectDefinitionWizardPanel panel) {
        kind = panel.kind();
        initComponents();
        projectNameTextField.setText(kind.prototypeProjectName());
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        packageNameTextField.getDocument().addDocumentListener(this);
        webContextPathField.getDocument().addDocumentListener(this);
        servletMappingField.getDocument().addDocumentListener(this);
        JCProjectProperties props = new JCProjectProperties();
        props.setPlatformName (JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME);
        platformAndDevicePanel1.setPlatformAndCard(props);
        String key;
        switch (kind) {
            case CLASSIC_APPLET:
            case EXTENDED_APPLET:
                key = "APPLET_AID"; //NOI18N
                break;
            default:
                key = "WEB_CONTEXT_PATH"; //NOI18N
        }

        webContextPathLabel.setText(NbBundle.getMessage(
                ProjectDefinitionPanel.class,
                key));

        for (Component c : getComponents()) {
            if (c instanceof JTextComponent) {
                c.addFocusListener(this);
            }
        }
        inUpdate = false;
        dontUpdateWcp = false;

        serverRes = platformAndDevicePanel1.getLookup().lookupResult(Card.class);
        platformRes = platformAndDevicePanel1.getLookup().lookupResult(JavacardPlatform.class);
        serverRes.addLookupListener(this);
        platformRes.addLookupListener(this);
        serverRes.allInstances();
        platformRes.allInstances();
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.CreateProject"); //NOI18N
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        setAsMainProjectCheckBox = new javax.swing.JCheckBox();
        packageNameTextField = new javax.swing.JTextField();
        webContextPathLabel = new javax.swing.JLabel();
        webContextPathField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        servletMappingLabel = new javax.swing.JLabel();
        servletMappingField = new javax.swing.JTextField();
        gridbagSludge = new javax.swing.JPanel();
        platformAndDevicePanel1 = new org.netbeans.modules.javacard.api.PlatformAndDevicePanel();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "PROJECT_NAME")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(projectNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(projectNameTextField, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "PROJECT_LOCATION")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(projectLocationLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(projectLocationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "BROWSE")); // NOI18N
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(browseButton, gridBagConstraints);

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "PROJECT_FOLDER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(createdFolderLabel, gridBagConstraints);

        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(createdFolderTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 5, 0);
        add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(setAsMainProjectCheckBox, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "SET_AS_MAIN_PROJECT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(setAsMainProjectCheckBox, gridBagConstraints);

        packageNameTextField.setText(org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "yourpackagename")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(packageNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(webContextPathLabel, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "WEB_CONTEXT_PATH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(webContextPathLabel, gridBagConstraints);

        webContextPathField.setText(org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "INITIAL_PATH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(webContextPathField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ProjectDefinitionPanel.class, "BASE_PACKAGE_NAME")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel1, gridBagConstraints);

        servletMappingLabel.setLabelFor(servletMappingField);
        org.openide.awt.Mnemonics.setLocalizedText(servletMappingLabel, "Servlet Mapping");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(servletMappingLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.weightx = 1.0;
        add(servletMappingField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.weighty = 1.0;
        add(gridbagSludge, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(platformAndDevicePanel1, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == browseButton) {
            ProjectDefinitionPanel.this.browseButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) { //NOI18N
            String title = NbBundle.getMessage(
                    ProjectDefinitionPanel.class, "Select_Project_Location");
            JFileChooser chooser = new FileChooserBuilder(ProjectDefinitionPanel.class).setTitle(title).setDirectoriesOnly(true).createFileChooser();
            chooser.setMultiSelectionEnabled(true);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            fireChange();
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JPanel gridbagSludge;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField packageNameTextField;
    private org.netbeans.modules.javacard.api.PlatformAndDevicePanel platformAndDevicePanel1;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JTextField servletMappingField;
    private javax.swing.JLabel servletMappingLabel;
    private javax.swing.JCheckBox setAsMainProjectCheckBox;
    private javax.swing.JTextField webContextPathField;
    private javax.swing.JLabel webContextPathLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        projectNameTextField.requestFocus();
    }

    public boolean valid(WizardDescriptor wizardDescriptor) {
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }

        return areProjectNameAndLocationValid(wizardDescriptor) &&
                isPlatformSelectedOK(wizardDescriptor) &&
                isJCServerInstanceSelectedOK(wizardDescriptor) &&
                isBasePackageNameValid(wizardDescriptor) &&
                isDisplayNameValid(wizardDescriptor) &&
                isWebContextPathValid(wizardDescriptor) &&
                isSrcTgzURLValid(wizardDescriptor) &&
                isServletMappingValid(wizardDescriptor) &&
                isAppletAIDValid(wizardDescriptor) && 
                isTargetFolderValid(wizardDescriptor);
    }

    private boolean isAppletAIDValid (WizardDescriptor wizardDescriptor) {
        if (!kind.isApplet()) {
            return true;
        }
        String s = webContextPathField.getText();
        if (s.trim().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ProjectDefinitionPanel.class,
                    "ERR_EMPTY_AID")); //NOI18N
            return false;
        }
        try {
            AID.parse(s);
        } catch (IllegalArgumentException e) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isServletMappingValid(WizardDescriptor wizardDescriptor) {
        if (kind != ProjectKind.WEB) {
            return true;
        }
        if (servletMappingField.getText().trim().length() == 0 ||
                !servletMappingField.getText().startsWith("/")) {

            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ProjectDefinitionPanel.class,
                    "LBL_servletMappingInvalid")); //NOI18N

            return false;
        }
        return true;
    }

    private boolean areProjectNameAndLocationValid(WizardDescriptor wizardDescriptor) {
        ResourceBundle bundle = NbBundle.getBundle(ProjectDefinitionPanel.class);

        if (projectNameTextField.getText().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_name_invalid")); //NOI18N
            return false; // Project name not specified

        }
        String location = projectLocationTextField.getText();
        if (location.length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_folder_invalid")); //NOI18N
            return false;
        }
        File f = FileUtil.normalizeFile(
                new File(location).getAbsoluteFile());
        if (!f.exists()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_location_not_exist")); //NOI18N
            // It's only warning, do not return
        } else if (!f.isDirectory()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_folder_invalid")); //NOI18N
            return false;
        }

        final File destFolder = FileUtil.normalizeFile(
                new File(createdFolderTextField.getText()).getAbsoluteFile());

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_folder_cannot_create")); //NOI18N
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_folder_path_invalid")); //NOI18N
            return false;
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    bundle.getString("LBL_project_folder_exists")); //NOI18N
            return false;
        }

        return true;
    }

    private boolean isBasePackageNameValid(WizardDescriptor wizardDescriptor) {
        // Verify the validity of Hello class name
        String packageName = packageNameTextField.getText();
        try {
            parsePackageName(packageName);
        } catch (IllegalArgumentException e) {
            if (wizardDescriptor != null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ProjectDefinitionPanel.class,
                        "LBL_package_name_invalid")); //NOI18N
            }
            return false;
        }
        return true;
    }

    private boolean isDisplayNameValid(WizardDescriptor wizardDescriptor) {
        return true;
    }

    private boolean isWebContextPathValid(WizardDescriptor wizardDescriptor) {
        if (kind != ProjectKind.WEB) {
            return true;
        }
        // Verify the validity of Web Context Path
        String val = webContextPathField.getText();

        if (val.length() < 2 || val.charAt(0) != '/') {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ProjectDefinitionPanel.class,
                    "LBL_web_context_path_invalid")); //NOI18N
            return false;
        }

        for (int i = 0; i < val.length(); i++) {
            if (Character.isWhitespace(val.charAt(i))) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ProjectDefinitionPanel.class,
                        "LBL_web_context_path_invalid_Spaces")); //NOI18N
                return false;
            }
        }
        return true;
    }

    private boolean isTargetFolderValid(WizardDescriptor wizardDescriptor) {
        boolean result = new File (projectLocationTextField.getText()).exists();
        if (!result) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ProjectDefinitionPanel.class,
                    "ERR_BAD_LOCATION", projectLocationTextField.getText())); //NOI18N

        }
        return result;
    }

    private boolean isSrcTgzURLValid(WizardDescriptor wizardDescriptor) {
        return true;
    }

    private boolean isJCServerInstanceSelectedOK(WizardDescriptor wizardDescriptor) {
        boolean result = serverRes.allItems().size() > 0;
        if (!result) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ProjectDefinitionPanel.class,
                    "NO_SERVER_SELECTED")); //NOI18N
            return false;
        }
        return true;
    }

    private boolean isPlatformSelectedOK(WizardDescriptor wizardDescriptor) {
        boolean result = platformRes.allItems().size() > 0;
        if (!result) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ProjectDefinitionPanel.class,
                    "NO_PLATFORM_SELECTED")); //NOI18N
            return false;
        } else {
            JavacardPlatform p = platformRes.allInstances().iterator().next();
            if (!p.isValid()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ProjectDefinitionPanel.class,
                        "PLATFORM_INVALID", p.getDisplayName())); //NOI18N
                return false;
            }
            if (kind != null && !p.supportedProjectKinds().contains(kind)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ProjectDefinitionPanel.class,
                        "PLATFORM_DOES_NOT_SUPPORT_PROJECT_KIND", p.getDisplayName(), //NOI18N
                        kind.getDisplayName())); //NOI18N
                return false;
            }
        }

        return true;
    }

    public void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        Collection<? extends JavacardPlatform> platformColl = platformRes.allInstances();
        boolean invalidPlatform;
        if (!platformColl.isEmpty()) {
            JavacardPlatform platform = platformColl.iterator().next();
            invalidPlatform = !platform.isValid();
            d.putProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM, platform.getSystemName());
            d.putProperty("activeplatform", platform.getSystemName()); //NOI18N //XXX constant?
        } else {
            invalidPlatform = true;
            d.putProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM, JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME);
            d.putProperty("activeplatform", JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME); //NOI18N //XXX constant?
        }
        if (!invalidPlatform) {
            Collection<? extends Card> cards = serverRes.allInstances();
            if (!cards.isEmpty()) {
                d.putProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE, cards.iterator().next().getSystemId());
                d.putProperty("activedevice", cards.iterator().next().getSystemId()); //NOI18N //XXX locate constant
            }
        } else {
                d.putProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE, "Default Device"); //XXX locate constant
                d.putProperty("activedevice", "Default Device"); //NOI18N //XXX locate constant
        }
        d.putProperty("projdir", new File(folder)); //NOI18N
        d.putProperty(ProjectWizardKeys.WIZARD_PROP_PROJECT_NAME, name);
        d.putProperty(ProjectWizardKeys.WIZARD_PROP_BASE_PACKAGE_NAME, packageNameTextField.getText());
        d.putProperty(ProjectWizardKeys.WIZARD_PROP_MAIN_CLASS_NAME, name.replaceAll("[\\p{Punct}\\s]+", "_")); //NOI18N
        d.putProperty(ProjectWizardKeys.WIZARD_PROP_DISPLAY_NAME, projectNameTextField.getText());
        switch (kind) {
            case CLASSIC_APPLET:
            case EXTENDED_APPLET:
                d.putProperty(ProjectWizardKeys.WIZARD_PROP_APPLET_AID, webContextPathField.getText());
                break;
            default:
                d.putProperty(ProjectWizardKeys.WIZARD_PROP_WEB_CONTEXT_PATH, webContextPathField.getText());
                d.putProperty(ProjectWizardKeys.WIZARD_PROP_SERVLET_MAPPING, servletMappingField.getText());
                break;

        }
    }

    public void read(WizardDescriptor settings) {
        inUpdate = true;
        String activePlatform = (String) settings.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        String activeDevice = (String) settings.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
        platformAndDevicePanel1.setPlatformAndCard (activePlatform, activeDevice);

        File projectLocation = (File) settings.getProperty("projdir"); //NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String projectName = (String) settings.getProperty(ProjectWizardKeys.WIZARD_PROP_PROJECT_NAME);
        if (projectName == null) {
            projectName = org.openide.util.NbBundle.getMessage(
                    ProjectDefinitionPanel.class, "DEFAULT_PROJECT_NAME"); //NOI18N
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();

        String servletMapping = (String) settings.getProperty(ProjectWizardKeys.WIZARD_PROP_SERVLET_MAPPING);
        servletMappingField.setText(servletMapping);
        String packageName =
                (String) settings.getProperty(ProjectWizardKeys.WIZARD_PROP_BASE_PACKAGE_NAME);
        this.packageNameTextField.setText(packageName);
        switch (kind) {
            case WEB:
            case EXTENSION_LIBRARY:
            case CLASSIC_LIBRARY:
                webContextPathField.setText(
                        (String) settings.getProperty(ProjectWizardKeys.WIZARD_PROP_WEB_CONTEXT_PATH));
                break;
            case CLASSIC_APPLET:
            case EXTENDED_APPLET:
                webContextPathField.setText(
                        (String) settings.getProperty(ProjectWizardKeys.WIZARD_PROP_APPLET_AID));
                break;

        }
        setSomeDefaults();
        inUpdate = false;
        dontUpdateWcp = false;
        dontUpdateServletMapping = false;
        if (kind != ProjectKind.WEB) {
            remove(servletMappingField);
            remove(servletMappingLabel);
        }
        if (kind.isLibrary()) {
            remove(webContextPathField);
            remove(webContextPathLabel);
        }
    }

    private void setSomeDefaults() {
        String prefix = kind.prototypeProjectName();
        int i = 1;
        for (i = 1; i < Integer.MAX_VALUE; i++) {
            File f = new File(projectLocationTextField.getText(), prefix + i);
            if (!f.exists()) {
                break;
            }
        }
        projectNameTextField.setText(prefix + i);
        packageNameTextField.setText((prefix + i).toLowerCase());
        //Store most recently used package in preferences - probably a better
        //chance of being right than the project name
        String lastPkg = NbPreferences.forModule(ProjectDefinitionPanel.class).get(PREF_LAST_PACKAGE, null);
        if (lastPkg != null) {
            packageNameTextField.setText(lastPkg);
        }
        switch (kind) {
            case CLASSIC_APPLET:
            case EXTENDED_APPLET:
                webContextPathField.setText(Utils.generateAppletAID (
                        packageNameTextField.getText(), projectNameTextField.getText()).toString());
                break;
            default:
                webContextPathField.setText("/" + prefix.toLowerCase() + i); //NOI18N
        }
        servletMappingField.setText('/' + projectNameTextField.getText().toLowerCase()); //NOI18N
        updateOtherProperties();
    }

    public void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    private void documentChanged(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(ProjectWizardKeys.WIZARD_PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
            updateOtherProperties();
        } else if (!inUpdate && (kind == ProjectKind.CLASSIC_APPLET || kind == ProjectKind.EXTENDED_APPLET) && e.getDocument() == packageNameTextField.getDocument()) {
            updateOtherProperties();
        }
        if (!inUpdate && isShowing()) {
            maybeTurnOffAutogeneration(e);
            if (e.getDocument() == packageNameTextField.getDocument() && isBasePackageNameValid(null)) {
                NbPreferences.forModule(ProjectDefinitionPanel.class).put(PREF_LAST_PACKAGE, packageNameTextField.getText().trim());
            }
        }
    }
    private static final String PREF_LAST_PACKAGE = "package"; //NOI18N

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void insertUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    private void maybeTurnOffAutogeneration(DocumentEvent e) {
        if (e.getDocument() == projectNameTextField.getDocument()) {
            return;
        }
        if (e.getDocument() == webContextPathField.getDocument()) {
            dontUpdateWcp = true;
        }
        if (e.getDocument() == servletMappingField.getDocument()) {
            dontUpdateServletMapping = true;
        }
    }
    boolean dontUpdateWcp;
    boolean dontUpdateServletMapping;
    boolean inUpdate;

    private void updateOtherProperties() {
        inUpdate = true;
        try {
            String projectname = projectNameTextField.getText();
            if (kind == ProjectKind.WEB && !dontUpdateWcp) {
                String proposedName = '/' + projectname.toLowerCase();
                while (proposedName.length() > 1 && Character.isDigit(proposedName.charAt(proposedName.length() - 1))) {
                    proposedName = proposedName.substring(0, proposedName.length() - 1);
                }
                webContextPathField.setText(proposedName);
            } else if (kind.isApplet() && !dontUpdateWcp) {
                String packageName = packageNameTextField.getText();
                String className = projectNameTextField.getText();
                webContextPathField.setText(Utils.generateAppletAID(packageName, className).toString());
            } else if (!dontUpdateWcp && (kind == ProjectKind.CLASSIC_APPLET || kind == ProjectKind.EXTENDED_APPLET)) {
                String proposedText = Utils.generateAppletAID(
                        packageNameTextField.getText(), projectname).toString();
                webContextPathField.setText(proposedText);
            }
            if (kind == ProjectKind.WEB && !dontUpdateServletMapping) {
                String proposedName = "/" + projectname.toLowerCase();
                while (proposedName.length() > 1 && Character.isDigit(proposedName.charAt(proposedName.length() - 1))) {
                    proposedName = proposedName.substring(0, proposedName.length() - 1);
                }
                servletMappingField.setText(proposedName);
            }
        } finally {
            inUpdate = false;
        }
    }

    /** Handles changes in the Project name and project directory, */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name

            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();

            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar +
                    projectName);
            //}

        }
        fireChange();

    }

    private static String parsePackageName(String rawName)
            throws IllegalArgumentException {
        StringTokenizer st = new StringTokenizer(rawName, ".", true);
        StringBuffer packageName = new StringBuffer();
        boolean expectDelimeter = false;

        // Check that name is of form: <Java id> ( "." <Java id> )* //NOI18N
        while (st.hasMoreTokens()) {
            String id = st.nextToken();

            if (expectDelimeter) {
                if (!id.equals(".")) {
                    throw new IllegalArgumentException(NbBundle.getMessage(
                            ProjectDefinitionPanel.class,
                            "Not_a_valid_class_name.")); //NOI18N
                }
                expectDelimeter = false;
                continue;
            }
            expectDelimeter = true;
            if (id.length() == 0) {
                throw new IllegalArgumentException(NbBundle.getMessage(
                        ProjectDefinitionPanel.class,
                        "Not_a_valid_class_name.")); //NOI18N
            }
            if (!Utilities.isJavaIdentifier(id)) {
                throw new IllegalArgumentException(NbBundle.getMessage(
                        ProjectDefinitionPanel.class,
                        "Not_a_valid_class_name.")); //NOI18N
            }
            if (packageName.length() > 0) {
                packageName.append('.');
            }
            packageName.append(id);
        }
        // Check that the last token was a Java id, not '.'
        if (!expectDelimeter) {
            throw new IllegalArgumentException(NbBundle.getMessage(
                    ProjectDefinitionPanel.class, "Not_a_valid_package_name.")); //NOI18N
        }
        return packageName.toString();
    }

    public void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }

    public void fireChange() {
        supp.fireChange();
    }

    public void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }

    public void focusGained(FocusEvent e) {
        ((JTextComponent) e.getComponent()).selectAll();
    }

    public void focusLost(FocusEvent e) {
    }

    public void resultChanged(LookupEvent arg0) {
        fireChange();
    }
}
