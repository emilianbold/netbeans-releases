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
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class PanelProjectLocationVisual extends SettingsPanel implements DocumentListener, HelpCtx.Provider {

    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    public static final String PROP_MAIN_NAME = "mainName"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("Inot Hosts",1); // NOI18N
    private final PanelConfigureProject controller;
    private final String templateName;
    private String name;
    private boolean makefileNameChanged = false;
    private int type;
    private volatile boolean initialized = false;
    private static final Object FAKE_ITEM = new Object();
    private ExecutionEnvironment env;
    private FileSystem fileSystem;
    private char fsFileSeparator;

    /** Creates new form PanelProjectLocationVisual */
    public PanelProjectLocationVisual(PanelConfigureProject panel, String name, boolean showMakefileTextField, int type) {
        initComponents();
        this.controller = panel;
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

        createMainTextField.setText("main"); // NOI18N
        createMainTextField.getDocument().addDocumentListener(PanelProjectLocationVisual.this);

        if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            createMainComboBox.addItem("C"); // NOI18N
            createMainComboBox.addItem("C++"); // NOI18N
            createMainComboBox.addItem("Fortran"); // NOI18N
            String prefLanguage = MakeOptions.getInstance().getPrefApplicationLanguage();
            createMainComboBox.setSelectedItem(prefLanguage);
        } else if (type == NewMakeProjectWizardIterator.TYPE_DB_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            createMainComboBox.addItem("C"); // NOI18N
            createMainComboBox.addItem("C++"); // NOI18N
            createMainComboBox.setSelectedItem(0);
        } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            createMainComboBox.addItem("C++"); // NOI18N
            createMainComboBox.setSelectedIndex(0);
        } else {
            createMainCheckBox.setVisible(false);
            createMainCheckBox.setSelected(false);
            createMainTextField.setVisible(false);
            createMainComboBox.setVisible(false);
        }
        disableHostsInfo(this.hostComboBox, this.toolchainComboBox);
    }

    /*package*/static void disableHostsInfo(JComboBox hostComboBox, JComboBox toolchainComboBox) {
        // load hosts && toolchains
        hostComboBox.setEnabled(false);
        toolchainComboBox.setEnabled(false);
        hostComboBox.addItem(FAKE_ITEM);
        toolchainComboBox.addItem(FAKE_ITEM);
        hostComboBox.setRenderer(new MyDevHostListCellRenderer(FAKE_ITEM));
        toolchainComboBox.setRenderer(new MyToolchainListCellRenderer(FAKE_ITEM));
    }

    /*package*/static void updateToolchainsComponents(JComboBox hostComboBox, JComboBox toolchainComboBox, 
            Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enableHost, boolean enableToolchain) {

        hostComboBox.removeAllItems();
        toolchainComboBox.removeAllItems();
        if (records != null) {
            for (ServerRecord serverRecord : records) {
                hostComboBox.addItem(serverRecord);
            }
            hostComboBox.setSelectedItem(srToSelect);
            updateToolchains(toolchainComboBox, srToSelect);
            for(int i = 0; i < toolchainComboBox.getModel().getSize(); i++) {
                Object elementAt = toolchainComboBox.getModel().getElementAt(i);
                if (elementAt instanceof ToolCollectionItem) {
                    ToolCollectionItem item = (ToolCollectionItem) elementAt;
                    if (isDefaultCompilerSet && item.isDefaultCompilerSet()) {
                        toolchainComboBox.setSelectedIndex(i);
                        break;
                    } else {
                        if (item.getCompilerSet().equals(csToSelect)) {
                            toolchainComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
            hostComboBox.setEnabled(enableHost);
            toolchainComboBox.setEnabled(enableToolchain);
        }
    }

    private static Collection<ServerRecord> initServerRecords(ToolsCacheManager toolsCacheManager, ExecutionEnvironment ee) {
        Collection<ServerRecord> out = new ArrayList<ServerRecord>();

        Collection<ServerRecord> records = new ArrayList<ServerRecord>();
        if (toolsCacheManager != null && toolsCacheManager.getServerUpdateCache() != null) {
            records.addAll(toolsCacheManager.getServerUpdateCache().getHosts());
        } else {
            records.addAll(ServerList.getRecords());
        }
        if (ee != null) {
            ServerRecord r = ServerList.get(ee);
            if (r.isSetUp()) {
                records.add(r);
            }
        }

        for (ServerRecord serverRecord : records) {
            if (serverRecord.isSetUp() && !serverRecord.isDeleted()) {
                CompilerSetManager csm;
                if (toolsCacheManager != null && ee != null) {
                    csm = toolsCacheManager.getCompilerSetManagerCopy(ee, false);
                } else {
                    csm = CompilerSetManager.get(serverRecord.getExecutionEnvironment());
                }
                if (csm != null) {
                    csm.finishInitialization();
                    if (!csm.isEmpty() && !csm.isUninitialized()) {
                        out.add(serverRecord);
                    }
                }
            }
        }
        return out;
    }
    
    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewAppWizard"); // NOI18N
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
        String path = this.projectLocationTextField.getText();
        JFileChooser chooser = RemoteFileUtil.createFileChooser(FileSystemProvider.getFileSystem(env),
                NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_SelectProjectLocation"),
                null, JFileChooser.DIRECTORIES_ONLY, null, path, true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(projectDir.getAbsolutePath());
        }
        controller.fireChangeEvent();
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
            updateToolchains(toolchainComboBox, newItem);
            controller.fireChangeEvent(); // Notify that the panel changed
        }
    }

    /*package*/ static void updateToolchains(JComboBox toolchainComboBox, ServerRecord newItem) {
        // change toolchains
        CompilerSetManager csm = CompilerSetManager.get(newItem.getExecutionEnvironment());
        toolchainComboBox.removeAllItems();
        CompilerSet defaultCompilerSet  = csm.getDefaultCompilerSet();
        if (defaultCompilerSet != null) {
            toolchainComboBox.addItem(new ToolCollectionItem(defaultCompilerSet, true));
        }
        for (CompilerSet compilerSet : csm.getCompilerSets()) {
            toolchainComboBox.addItem(new ToolCollectionItem(compilerSet, false));
        }
        if (toolchainComboBox.getModel().getSize() > 0) {
            toolchainComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_hostComboBoxItemStateChanged

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    private boolean isValidMakeFile(String text){
        if (text.length() == 0) {
            return false;
        }
        if (text.contains("\\") || // NOI18N
            text.contains("/") || // NOI18N
            text.contains("..") || // NOI18N
            hasIllegalChar(text)) {
            return false;
        }
        return true;
    }

    private boolean isValidMainFile(String text){
        // unix allows a lot of strange names, but let's prohibit this for project
        // using symbols invalid on Windows
        if (text.length() == 0) {
            return true;
        }
        if (text.startsWith(" ") || // NOI18N
            text.startsWith("\\") || // NOI18N
            text.startsWith("/") || // NOI18N
            text.contains("..") || // NOI18N
            hasIllegalChar(text)) {
            return false;
        }
        return true;
    }

    public static boolean isValidProjectName(String text) {
        // unix allows a lot of strange names, but let's prohibit this for project
        // using symbols invalid on Windows
        if (text.length() == 0 || text.startsWith(" ") || // NOI18N
                text.contains("\\") || // NOI18N
                text.contains("/") || // NOI18N
                hasIllegalChar(text)) {
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

    private static boolean hasIllegalChar(String text) {
        return text.contains(":") || // NOI18N
               text.contains("*") || // NOI18N
               text.contains("?") || // NOI18N
               text.contains("\"") || // NOI18N
               text.contains("<") || // NOI18N
               text.contains(">") || // NOI18N
               text.contains("|");  // NOI18N
    }

   @Override
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (!initialized) {
            return false;
        }
        if (!isValidLocalProjectNameAndLocation(wizardDescriptor, projectNameTextField.getText(), projectLocationTextField.getText(), createdFolderTextField.getText())) {
            return false;
        }
        if (makefileTextField.getText().indexOf(" ") >= 0) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_SpacesInMakefile")); // NOI18N
            return false;
        }
        if (!isValidMakeFile(makefileTextField.getText())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalMakefileName")); // NOI18N
            return false;
        }
        if (createMainCheckBox.isSelected() && !isValidMainFile(createMainTextField.getText())){
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalMainFileName")); // NOI18N
            return false;
        }

        FileObject projectDirFO = fileSystem.findResource(createdFolderTextField.getText()); // can be null
        if (projectDirFO != null && projectDirFO.isValid()) {
            if (projectDirFO.isData()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", makefileTextField.getText()));  // NOI18N
                return false;
            }
            FileObject nbProjFO = projectDirFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
            if (nbProjFO != null && nbProjFO.isValid()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", MakeConfiguration.NBPROJECT_FOLDER)); // NOI18N
                return false;
            }
            FileObject makeFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + makefileTextField.getText());
            if (makeFO != null && makeFO.isValid()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", makefileTextField.getText()));  // NOI18N
                return false;
            }
            FileObject nbFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + MakeConfiguration.NBPROJECT_FOLDER);
            if (nbFO != null && nbFO.isValid()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", MakeConfiguration.NBPROJECT_FOLDER)); // NOI18N
                return false;
            }
            if (type != NewMakeProjectWizardIterator.TYPE_MAKEFILE) {
                FileObject destFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + MakeConfiguration.DIST_FOLDER);
                if (destFO != null && destFO.isValid()) {
                    // Folder exists and is not empty
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderExists")); // NOI18N
                    return false;
                }
                FileObject buildFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + MakeConfiguration.BUILD_FOLDER);
                if (buildFO != null && buildFO.isValid()) {
                    // Folder exists and is not empty
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderExists")); // NOI18N
                    return false;
                }
            }
        } else {
            FileObject existingParent = getExistingParent(createdFolderTextField.getText());
            if (existingParent == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly")); // NOI18N
                return false;
            }
            if (!existingParent.canWrite()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly")); // NOI18N
                return false;
            }
        }
        
        ServerRecord sr = (ServerRecord) hostComboBox.getSelectedItem();
        if (sr == null || !sr.isOnline()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_OfflineHost")); // NOI18N
        }
//        CompilerSetManager csm = CompilerSetManager.get(sr.getExecutionEnvironment());
//        CompilerSet cs = (CompilerSet) toolchainComboBox.getSelectedItem();
//        if (cs == null || csm == null || csm.isUninitialized() || csm.isEmpty()) {
//            // Toolchain is not specified
//            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, // NOI18N
//                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalToolchainName")); // NOI18N
//        }
        /*
        if (destFolder.getPath().indexOf(' ') >= 0) {
        wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
        NbBundle.getMessage(PanelProjectLocationVisual.class,"MSG_NoSpaces"));
        return false;
        }
         **/

        return true;
    }
    
    private FileObject getExistingParent(String path) {
        path = PathUtilities.getDirName(path);
        FileObject fo = fileSystem.findResource(path);
        while (fo == null) {
            path = PathUtilities.getDirName(path);
            if (path == null || path.length() == 0) {
                return null;
            } else {
                fo = fileSystem.findResource(path);
            }
        }
        return fo;
    }

    @Override
    void store(WizardDescriptor d) {

        String projectName = projectNameTextField.getText().trim();
        String location = projectLocationTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        if (CndPathUtilitities.isPathAbsolute(folder)) {
            String normalizeAbsolutePath = RemoteFileUtil.normalizeAbsolutePath(folder, env);
            FSPath path = new FSPath(fileSystem, normalizeAbsolutePath);
            d.putProperty(WizardConstants.PROPERTY_PROJECT_FOLDER, path);
        }
        d.putProperty(WizardConstants.PROPERTY_NAME, projectName);
        d.putProperty(WizardConstants.PROPERTY_GENERATED_MAKEFILE_NAME, makefileTextField.getText());
        if (CndPathUtilitities.isPathAbsolute(projectLocationTextField.getText())) {
            if (env.isLocal()) {
                File projectsDir = CndFileUtils.createLocalFile(projectLocationTextField.getText());
                if (projectsDir.isDirectory()) {
                    ProjectChooser.setProjectsFolder(projectsDir);
                }
            } else {
                RemoteFileUtil.setProjectsFolder(projectLocationTextField.getText(), env);
            }
        }

        d.putProperty(WizardConstants.MAIN_CLASS, null); // NOI18N

        MIMEExtensions cExtensions = MIMEExtensions.get(MIMENames.C_MIME_TYPE);
        MIMEExtensions ccExtensions = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
        MIMEExtensions fortranExtensions = MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE);

        d.putProperty("createMainFile", createMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        if (createMainCheckBox.isSelected() && createMainTextField.getText().length() > 0) {
            if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
                if (((String) createMainComboBox.getSelectedItem()).equals("C")) { // NOI18N
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + cExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cFiles/main.c"); // NOI18N
                } else if (((String) createMainComboBox.getSelectedItem()).equals("C++")) { // NOI18N
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cppFiles/main.cc"); // NOI18N
                } else if (((String) createMainComboBox.getSelectedItem()).equals("Fortran")) { // NOI18N
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + fortranExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/fortranFiles/fortranFixedFormatFile.f"); // NOI18N
                }
                MakeOptions.getInstance().setPrefApplicationLanguage((String)createMainComboBox.getSelectedItem());
            } else if(type == NewMakeProjectWizardIterator.TYPE_DB_APPLICATION) {
                if (((String) createMainComboBox.getSelectedItem()).equals("C")) { // NOI18N
                    d.putProperty("mainFileName", createMainTextField.getText() + ".pc"); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cFiles/main.pc"); // NOI18N
                } else {
                    d.putProperty("mainFileName", createMainTextField.getText() + ".pc"); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/ccFiles/main.pc"); // NOI18N
                }
            } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
                d.putProperty("mainFileName", createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                d.putProperty("mainFileTemplate", "Templates/qtFiles/main.cc"); // NOI18N
            }
        }
        Object obj = hostComboBox.getSelectedItem();
        if (obj != null && obj instanceof ServerRecord) {
            ServerRecord sr = (ServerRecord)obj;
            d.putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.toUniqueID(sr.getExecutionEnvironment()));
        }
        Object selectedItem = toolchainComboBox.getSelectedItem();
        if (selectedItem instanceof ToolCollectionItem) {
            ToolCollectionItem item = (ToolCollectionItem) selectedItem;
            d.putProperty(WizardConstants.PROPERTY_TOOLCHAIN, item.getCompilerSet());
            d.putProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT, item.isDefaultCompilerSet());
        }
    }

    @Override
    void read(WizardDescriptor settings) {
        initialized = false;
        
        env = (ExecutionEnvironment) settings.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV);
        final boolean enabledHost;
        if (env != null) {
            settings.putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.toUniqueID(env));
            enabledHost = false;
        } else {
            env = ExecutionEnvironmentFactory.getLocal();
            enabledHost = true;
        }

        fileSystem = FileSystemProvider.getFileSystem(env);
        fsFileSeparator = FileSystemProvider.getFileSeparatorChar(fileSystem);
        
        FSPath projectLocationFSPath = (FSPath) settings.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER); // File - SIC! for projects always local
        String projectName = null;
        String projectLocation;
        if (projectLocationFSPath == null) {
            projectLocation = RemoteFileUtil.getProjectsFolder(env);
            if (projectLocation == null) {
                projectLocation = getDefaultProjectDir(env);
            }
        } else {
            projectLocation = projectLocationFSPath.getPath();
            int i = projectLocation.lastIndexOf(fsFileSeparator);
            if (i > 0) {
                projectName = projectLocation.substring(i+1);
                projectLocation = projectLocation.substring(0,i);
            }
        }
        this.projectLocationTextField.setText(projectLocation);
        String hostUID = (String) settings.getProperty(WizardConstants.PROPERTY_HOST_UID);
        CompilerSet cs = (CompilerSet) settings.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        boolean isDefaultCompilerSet = Boolean.TRUE.equals(settings.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        Boolean readOnlyToolchain = (Boolean) settings.getProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN);
        RP.post(new DevHostsInitializer(hostUID, cs, isDefaultCompilerSet,
                readOnlyToolchain, (ToolsCacheManager) settings.getProperty(WizardConstants.PROPERTY_TOOLS_CACHE_MANAGER)) {
            @Override
            public void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enabled) {
                updateToolchainsComponents(PanelProjectLocationVisual.this.hostComboBox, PanelProjectLocationVisual.this.toolchainComboBox, records, srToSelect, csToSelect, isDefaultCompilerSet, enabledHost, enabled);
                initialized = true;
                controller.fireChangeEvent(); // Notify that the panel changed
            }
        });
        String prefferedName = (String) settings.getProperty(WizardConstants.PROPERTY_PREFERED_PROJECT_NAME); //NOI18N
        if (prefferedName != null && prefferedName.length() > 0) {
            name = prefferedName;
        }

        if (projectName == null) {
            if (name == null) {
                String workingDir = (String) settings.getProperty(WizardConstants.PROPERTY_WORKING_DIR); //NOI18N
                if (workingDir != null && workingDir.length() > 0 &&
                        (templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME) ||
                        templateName.equals(NewMakeProjectWizardIterator.FULL_REMOTE_PROJECT_NAME))) {
                    name = CndPathUtilitities.getBaseName(workingDir);
                } else {
                    String sourcesPath = (String) settings.getProperty(WizardConstants.PROPERTY_SOURCE_FOLDER_PATH); // NOI18N
                    if (sourcesPath != null && sourcesPath.length() > 0) {
                        name = CndPathUtilitities.getBaseName(sourcesPath);
                    }
                }
            }
            int baseCount = 1;
            String formater = name + "_{0}"; // NOI18N
            while ((projectName = validFreeProjectName(projectLocation, fsFileSeparator,
                    formater, baseCount)) == null) {
                baseCount++;
            }
            settings.putProperty(NewMakeProjectWizardIterator.PROP_NAME_INDEX, Integer.valueOf(baseCount));
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }
    
    private String getDefaultProjectDir(ExecutionEnvironment env) {
        String res = null;
        try {
            if (env.isLocal()) {
                res = ProjectChooser.getProjectsFolder().getPath();
            } else if (HostInfoUtils.isHostInfoAvailable(env)) {
                res = HostInfoUtils.getHostInfo(env).getUserDir() + fsFileSeparator + ProjectChooser.getProjectsFolder().getName();
            }
        } catch (IOException ex) {
        } catch (ConnectionManager.CancellationException ex) {
        }
        return res == null ? fileSystem.getRoot().getPath() : res;
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
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    // End of variables declaration//GEN-END:variables

    private String validFreeProjectName(String parentFolder, final char fs, final String formater, final int index) {
        String projectName = MessageFormat.format(formater, new Object[]{Integer.valueOf(index)});
        if (RemoteFileUtil.fileExists(parentFolder + fs + projectName, env)) { //NOI18N
            return null;
        }
        return projectName;
    }

    // Implementation of DocumentListener --------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    private void update(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
        if (this.createMainTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_MAIN_NAME, null, this.createMainTextField.getText());
        }
    }

    private static boolean isValidLocalProjectNameAndLocation(WizardDescriptor wizardDescriptor, String projectNameTextField, String projectLocationTextField, String createdFolderTextField) {
        if (!isValidProjectName(projectNameTextField)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName")); // NOI18N
            return false; // Display name not specified
        }
        if (!CndPathUtilitities.isPathAbsolute(projectLocationTextField)) { // empty field imcluded
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }
        File f = CndFileUtils.createLocalFile(projectLocationTextField).getAbsoluteFile();
        if (getCanonicalFile(f) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }
        final File destFolder = getCanonicalFile(CndFileUtils.createLocalFile(createdFolderTextField).getAbsoluteFile()); // project folder always local
        if (destFolder == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }
        return true;
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
            while (projectFolder.endsWith("/") || projectFolder.endsWith("\\")) { // NOI18N
                projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
            }
            createdFolderTextField.setText(projectFolder + fsFileSeparator + projectName);

            if (!makefileNameChanged) {
                // re-evaluate name of master project file.
                String makefileName;
                if (!templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME))
                {
                    makefileName = MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME;
                } else {
                    makefileName = contructProjectMakefileName(0);
                }

                for (int count = 0;;) {
                    String proposedMakefile = createdFolderTextField.getText() + fsFileSeparator + makefileName;
                    CndFileUtils.isExistingFile(fileSystem, makefileName);
                    if (!CndFileUtils.isExistingFile(fileSystem, proposedMakefile)
                            && !CndFileUtils.isExistingFile(fileSystem, proposedMakefile.toLowerCase())
                            && !CndFileUtils.isExistingFile(fileSystem, proposedMakefile.toUpperCase())) {
                        break;
                    }
                    makefileName = contructProjectMakefileName(count++);
                }
                makefileTextField.setText(makefileName);
                makefileNameChanged = false;
            }
        }
        controller.fireChangeEvent(); // Notify that the panel changed
    }

    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace(System.err);
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

    /*package*/ static final class MyDevHostListCellRenderer extends DefaultListCellRenderer {
        private final Object loadingMarker;

        public MyDevHostListCellRenderer(Object loadingItem) {
            loadingMarker = loadingItem;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (loadingMarker.equals(value)) {
                label.setText(NbBundle.getMessage(PanelProjectLocationVisual.class, "Loading_Host_Text")); // NOI18N
            } else {
                ServerRecord rec = (ServerRecord) value;
                if (rec != null) {
                    label.setText(rec.getDisplayName());
                }
            }
            return label;
        }
    }

    /*package*/ static final class MyToolchainListCellRenderer extends DefaultListCellRenderer {
        private final Object loadingMarker;

        public MyToolchainListCellRenderer(Object loadingItem) {
            loadingMarker = loadingItem;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (loadingMarker.equals(value)) {
                label.setText(NbBundle.getMessage(PanelProjectLocationVisual.class, "Loading_Toolchain_Text")); // NOI18N
            } else {
                label.setText(value.toString());
            }
            return label;
        }
    }

    /*package*/ abstract static class DevHostsInitializer implements Runnable {
        private final String hostUID;
        private final CompilerSet cs;
        private final boolean isDefaultCompilerSet;
        private final boolean readOnlyUI;
        private final ToolsCacheManager toolsCacheManager;
        
        // fields to be inited in worker thread and used in EDT
        private Collection<ServerRecord> records;
        private ServerRecord srToSelect;
        private CompilerSet csToSelect;
        
        public DevHostsInitializer(String hostUID, CompilerSet cs, boolean isDefaultCompilerSet, Boolean readOnlyToolchain, ToolsCacheManager toolsCacheManager) {
            this.hostUID = hostUID;
            this.cs = cs;
            this.isDefaultCompilerSet = isDefaultCompilerSet;
            this.readOnlyUI = readOnlyToolchain == null ? false : readOnlyToolchain.booleanValue();
            this.toolsCacheManager = toolsCacheManager;
        }

        @Override
        public void run() {
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    ExecutionEnvironment ee = (hostUID == null) ? null : ExecutionEnvironmentFactory.fromUniqueID(hostUID);
                    records = initServerRecords(toolsCacheManager, ee);
                    srToSelect = null;
                    if (ee != null) {
                        srToSelect = ServerList.get(ee);
                    }
                    if (!records.contains(srToSelect)) {
                        srToSelect = null;
                    }
                    if (srToSelect == null || srToSelect.isDeleted()) {
                        srToSelect = ServerList.getDefaultRecord();
                    }
                    if (cs == null) {
                        CompilerSetManager csm;
                        if (toolsCacheManager == null) {
                            csm = CompilerSetManager.get(srToSelect.getExecutionEnvironment());
                        } else {
                            csm = toolsCacheManager.getCompilerSetManagerCopy(srToSelect.getExecutionEnvironment(), false);
                        }
                        csToSelect = csm.getDefaultCompilerSet();
                    } else {
                        csToSelect = cs;
                    }
                } finally {
                    SwingUtilities.invokeLater(this);
                }
            } else {
                updateComponents(records, srToSelect, csToSelect, isDefaultCompilerSet, !readOnlyUI);
            }
        }

        public abstract void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect,
                CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enabled);
    }

    public final static class ToolCollectionItem {
        private final boolean defaultCompilerSet;
        private final CompilerSet compilerSet;
        private ToolCollectionItem(CompilerSet compilerSet, boolean defaultCompilerSet){
            this.defaultCompilerSet = defaultCompilerSet;
            this.compilerSet = compilerSet;
        }

        @Override
        public String toString() {
            String name = NbBundle.getMessage(PanelProjectLocationVisual.class, "Toolchain_Name_Text", compilerSet.getName(), compilerSet.getDisplayName());
            if (isDefaultCompilerSet()) {
                return getString("DefaultToolCollection")+" ("+name+")";
            } else {
                return name;
            }
        }

        public boolean isDefaultCompilerSet() {
            return defaultCompilerSet;
        }

        public CompilerSet getCompilerSet() {
            return compilerSet;
        }
    }
}
