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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class PanelProjectLocationVisual extends SettingsPanel implements DocumentListener, HelpCtx.Provider {

    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    private PanelConfigureProject panel;
    private String templateName;
    private String name;
    private boolean makefileNameChanged = false;
    private int type;
    private boolean initialized = false;

    /** Creates new form PanelProjectLocationVisual */
    public PanelProjectLocationVisual(PanelConfigureProject panel, String name, boolean showMakefileTextField, int type) {
        initComponents();
        this.panel = panel;
        this.name = name;
        this.templateName = name;
        this.type = type;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(PanelProjectLocationVisual.this);
        projectLocationTextField.getDocument().addDocumentListener(PanelProjectLocationVisual.this);
        if (showMakefileTextField) {
            makefileTextField.getDocument().addDocumentListener(PanelProjectLocationVisual.this);
            makefileTextField.getDocument().addDocumentListener(new MakefileDocumentListener());
        } else {
            makefileTextField.setVisible(false);
            makefileLabel.setVisible(false);
        }

        // Accessibility
        makefileTextField.getAccessibleContext().setAccessibleDescription(getString("AD_MAKEFILE"));

        setAsMainCheckBox.setVisible(true);

        createMainTextField.setText("main"); // NOI18N

        if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            createMainComboBox.addItem("C"); // NOI18N
            createMainComboBox.addItem("C++"); // NOI18N
            String prefLanguage = MakeOptions.getInstance().getPrefApplicationLanguage();
            createMainComboBox.setSelectedItem(prefLanguage);
        } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            createMainComboBox.addItem("C++"); // NOI18N
            createMainComboBox.setSelectedIndex(0);
        } else {
            createMainCheckBox.setVisible(false);
            createMainTextField.setVisible(false);
            createMainComboBox.setVisible(false);
        }
        // init hosts
        ServerRecord defaultRecord = ServerList.getDefaultRecord();
        for (ServerRecord serverRecord : ServerList.getRecords()) {
            hostComboBox.addItem(serverRecord);
        }
        hostComboBox.setRenderer(new MyDevHostListCellRenderer());
        toolchainComboBox.setRenderer(new MyToolchainListCellRenderer());
        updateToolchains(defaultRecord);
        initialized = true;
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewAppWizard"); // NOI18N
    }

    public CompilerSet getProjectCompilerSet() {
        return (CompilerSet) this.toolchainComboBox.getSelectedItem();
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
        makefileLabel = new javax.swing.JLabel();
        makefileTextField = new javax.swing.JTextField();
        createMainCheckBox = new javax.swing.JCheckBox();
        createMainTextField = new javax.swing.JTextField();
        createMainComboBox = new javax.swing.JComboBox();
        setAsMainCheckBox = new javax.swing.JCheckBox();
        hostLabel = new javax.swing.JLabel();
        toolchainLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        toolchainComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(projectNameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_projectNameLabel")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectNameLabel")); // NOI18N

        projectNameTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(projectNameTextField, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(projectLocationLabel, gridBagConstraints);
        projectLocationLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_projectLocationLabel")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectLocationLabel")); // NOI18N

        projectLocationTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(projectLocationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_browseButton")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_browseButton")); // NOI18N

        createdFolderLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MN_NWP1_CreatedProjectFolder_Lablel").charAt(0));
        createdFolderLabel.setLabelFor(createdFolderTextField);
        createdFolderLabel.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(createdFolderLabel, gridBagConstraints);
        createdFolderLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_createdFolderLabel")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_createdFolderLabel")); // NOI18N

        createdFolderTextField.setColumns(20);
        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 5, 0);
        add(createdFolderTextField, gridBagConstraints);

        makefileLabel.setLabelFor(makefileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(makefileLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_MAKEFILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(makefileLabel, gridBagConstraints);

        makefileTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(makefileTextField, gridBagConstraints);

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, bundle.getString("LBL_createMainfile")); // NOI18N
        createMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        createMainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMainCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(createMainCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(createMainTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(createMainComboBox, gridBagConstraints);

        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, bundle.getString("LBL_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(setAsMainCheckBox, gridBagConstraints);

        hostLabel.setLabelFor(hostComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(hostLabel, gridBagConstraints);

        toolchainLabel.setLabelFor(toolchainComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(toolchainLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_TOOLCHAIN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(toolchainLabel, gridBagConstraints);

        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(hostComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(toolchainComboBox, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACSN_PanelProjectLocationVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACSD_PanelProjectLocationVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setDialogTitle(NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_SelectProjectLocation")); // NOI18N
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocationTextField.getText();
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(projectDir.getAbsolutePath());
        }
        panel.fireChangeEvent();
    }//GEN-LAST:event_browseLocationAction

    private void createMainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMainCheckBoxActionPerformed
        // TODO add your handling code here:
        createMainTextField.setEnabled(createMainCheckBox.isSelected());
        createMainComboBox.setEnabled(createMainCheckBox.isSelected());
}//GEN-LAST:event_createMainCheckBoxActionPerformed

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hostComboBoxItemStateChanged
        if (!initialized) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            ServerRecord newItem = (ServerRecord) evt.getItem();
            updateToolchains(newItem);
        }
    }

    private void updateToolchains(ServerRecord newItem) {
        // change toolchains
        CompilerSetManager csm = CompilerSetManager.get(newItem.getExecutionEnvironment());
        toolchainComboBox.removeAllItems();
        for (CompilerSet compilerSet : csm.getCompilerSets()) {
            toolchainComboBox.addItem(compilerSet);
        }
        toolchainComboBox.setSelectedItem(csm.getDefaultCompilerSet());
    }//GEN-LAST:event_hostComboBoxItemStateChanged

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    private boolean isValidProjectName(String text) {
        // unix allows a lot of strange names, but let's prohibit this for project
        // using symbols invalid on Windows
        if (text.length() == 0 || text.startsWith(" ") || // NOI18N
                text.contains("\\") || // NOI18N
                text.contains("/") || // NOI18N
                text.contains(":") || // NOI18N
                text.contains("*") || // NOI18N
                text.contains("?") || // NOI18N
                text.contains("\"") || // NOI18N
                text.contains("<") || // NOI18N
                text.contains(">") || // NOI18N
                text.contains("|")) {  // NOI18N
            return false;
        }
        // check ability to create file with specified name on target OS
        boolean ok = false;
        try {
            File file = File.createTempFile(text + "dummy", "");// NOI18N
            ok = true;
            file.delete();
        } catch (Exception ex) {
            // failed to create
        }
        return ok;
    }

    @Override
    boolean valid(WizardDescriptor wizardDescriptor) {

        if (!isValidProjectName(projectNameTextField.getText())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName")); // NOI18N
            return false; // Display name not specified
        }
        File f = new File(projectLocationTextField.getText()).getAbsoluteFile();
        if (getCanonicalFile(f) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
            return false;
        }
        final File destFolder = getCanonicalFile(new File(createdFolderTextField.getText()).getAbsoluteFile());
        if (destFolder == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
            return false;
        }
        if (makefileTextField.getText().indexOf(" ") >= 0) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_SpacesInMakefile")); // NOI18N
            return false;
        }
        if (makefileTextField.getText().indexOf("/") >= 0 || makefileTextField.getText().indexOf("\\") >= 0) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalMakefileName")); // NOI18N
            return false;
        }

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly")); // NOI18N
            return false;
        }
        if (destFolder.exists()) {
            if (destFolder.isFile()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_NotAFolder", makefileTextField.getText()));  // NOI18N
                return false;
            }
            if (new File(destFolder.getPath(), makefileTextField.getText()).exists()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_MakefileExists", makefileTextField.getText()));  // NOI18N
                return false;
            }
            if (new File(destFolder.getPath(), MakeConfiguration.NBPROJECT_FOLDER).exists() ||
                    new File(destFolder.getPath(), MakeConfiguration.BUILD_FOLDER).exists() ||
                    new File(destFolder.getPath(), MakeConfiguration.DIST_FOLDER).exists()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderExists")); // NOI18N
                return false;
            }
        }
        if (toolchainComboBox.getSelectedItem() == null) {
            // Toolchain is not specified
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalToolchainName")); // NOI18N
        }
        /*
        if (destFolder.getPath().indexOf(' ') >= 0) {
        wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
        NbBundle.getMessage(PanelProjectLocationVisual.class,"MSG_NoSpaces"));
        return false;
        }
         **/

        return true;
    }

    @Override
    void store(WizardDescriptor d) {

        String projectName = projectNameTextField.getText().trim();
        String location = projectLocationTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        d.putProperty( /*XXX Define somewhere */"projdir", new File(folder)); // NOI18N
        d.putProperty( /*XXX Define somewhere */"name", projectName); // NOI18N
        d.putProperty( /*XXX Define somewhere */"makefilename", makefileTextField.getText()); // NOI18N
        File projectsDir = new File(this.projectLocationTextField.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder(projectsDir);
        }

        d.putProperty( /*XXX Define somewhere */"setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        d.putProperty( /*XXX Define somewhere */"mainClass", null); // NOI18N

        MIMEExtensions cExtensions = MIMEExtensions.get("text/x-c"); // NOI18N
        MIMEExtensions ccExtensions = MIMEExtensions.get("text/x-c++"); // NOI18N

        d.putProperty("createMainFile", createMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        if (createMainCheckBox.isSelected() && createMainTextField.getText().length() > 0) {
            if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
                if (((String) createMainComboBox.getSelectedItem()).equals("C")) { // NOI18N
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + cExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cFiles/main.c"); // NOI18N
                } else {
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cppFiles/main.cc"); // NOI18N
                }
                MakeOptions.getInstance().setPrefApplicationLanguage((String)createMainComboBox.getSelectedItem());
            } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
                d.putProperty("mainFileName", createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                d.putProperty("mainFileTemplate", "Templates/qtFiles/main.cc"); // NOI18N
            }
        }
        d.putProperty("serverRecord", hostComboBox.getSelectedItem()); // NOI18N
        d.putProperty("toolchain", toolchainComboBox.getSelectedItem()); // NOI18N
    }

    @Override
    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty("projdir");  //NOI18N
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        ServerRecord sr = (ServerRecord) settings.getProperty("serverRecord");
        if (sr == null || sr.isDeleted()) {
            sr = ServerList.getDefaultRecord();
        }
        this.hostComboBox.setSelectedItem(sr);
        CompilerSet cs = (CompilerSet) settings.getProperty("toolchain");
        if (cs == null) {
            CompilerSetManager csm = CompilerSetManager.get(sr.getExecutionEnvironment());
            cs = csm.getDefaultCompilerSet();
        }
        this.toolchainComboBox.setSelectedItem(cs);
        String projectName = (String) settings.getProperty("displayName"); //NOI18N
        if (projectName == null) {
            String workingDir = (String) settings.getProperty("buildCommandWorkingDirTextField"); //NOI18N
            if (workingDir != null && workingDir.length() > 0 && templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)) {
                name = CndPathUtilitities.getBaseName(workingDir);
            }
            int baseCount = 1;
            String formater = name + "_{0}"; // NOI18N
            while ((projectName = validFreeProjectName(projectLocation, formater, baseCount)) == null) {
                baseCount++;
            }
            settings.putProperty(NewMakeProjectWizardIterator.PROP_NAME_INDEX, Integer.valueOf(baseCount));
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JComboBox createMainComboBox;
    private javax.swing.JTextField createMainTextField;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel makefileLabel;
    private javax.swing.JTextField makefileTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    // End of variables declaration//GEN-END:variables

    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String projectName = MessageFormat.format(formater, new Object[]{Integer.valueOf(index)});
        File file = new File(parentFolder, projectName);
        return file.exists() ? null : projectName;
    }

    // Implementation of DocumentListener --------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    class MakefileDocumentListener implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            makefileNameChanged = true;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            makefileNameChanged = true;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            makefileNameChanged = true;
        }
    }

    private String contructProjectMakefileName(int count) {
        String makefileName = projectNameTextField.getText() + "-" + MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME; // NOI18N
        if (count > 0) {
            makefileName += "" + count + ".mk"; // NOI18N
        } else {
            makefileName += ".mk"; // NOI18N
        }
        return makefileName;
    }

    /** Handles changes in the Project name and project directory
     */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            String projectName = projectNameTextField.getText().trim();
            String projectFolder = projectLocationTextField.getText().trim();
            while (projectFolder.endsWith("/")) { // NOI18N
                projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
            }
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);

            if (!makefileNameChanged) {
                // re-evaluate name of master project file.
                String makefileName;
                if (!templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)) // NOI18N
                {
                    makefileName = MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME;
                } else {
                    makefileName = contructProjectMakefileName(0);
                }

                for (int count = 0;;) {
                    String proposedMakefile = createdFolderTextField.getText() + File.separatorChar + makefileName;
                    if (!new File(proposedMakefile).exists() && !new File(proposedMakefile.toLowerCase()).exists() && !new File(proposedMakefile.toUpperCase()).exists()) {
                        break;
                    }
                    makefileName = contructProjectMakefileName(count++);
                }
                makefileTextField.setText(makefileName);
                makefileNameChanged = false;
            }
        }
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PanelProjectLocationVisual.class);
        }
        return bundle.getString(s);
    }

    private static final class MyDevHostListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ServerRecord rec = (ServerRecord) value;
            if (rec != null) {
                label.setText(rec.getDisplayName());
            }
            return label;
        }
    }

    private static final class MyToolchainListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            CompilerSet cs = (CompilerSet) value;
            if (cs != null) {
                label.setText(cs.getDisplayName());
            }
            return label;
        }
    }
}
