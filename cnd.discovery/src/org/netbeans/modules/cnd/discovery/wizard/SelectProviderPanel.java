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
package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 */
public final class SelectProviderPanel extends JPanel implements CsmProgressListener {
    private static final boolean SHOW_RESTRICT = Boolean.getBoolean("cnd.discovery.wizard.restrictSources"); // NOI18N
    private final SelectProviderWizard wizard;
    public static final boolean USE_PROJECT_PROPERTIES = true;
    private static final String ROOT_PROPERTY_KEY = "rootFolder"; // NOI18N
    /** Creates new form SelectProviderVisualPanel1 */
    public SelectProviderPanel(SelectProviderWizard wizard) {
        this.wizard = wizard;
        initComponents();
        if (!SHOW_RESTRICT){
            restrictSources.setVisible(false);
            restrictCompile.setVisible(false);
        }
        addListeners();
    }
    
    private void addListeners(){
        ((ExpandableEditableComboBox)rootFolder).addChangeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        CsmListeners.getDefault().addProgressListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelForRoot = new javax.swing.JLabel();
        rootFolder = new ExpandableEditableComboBox();
        rootFolderButton = new javax.swing.JButton();
        labelForProviders = new javax.swing.JLabel();
        prividersComboBox = new javax.swing.JComboBox();
        restrictSources = new javax.swing.JCheckBox();
        restrictCompile = new javax.swing.JCheckBox();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        alertPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        labelForRoot.setLabelFor(rootFolder);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelForRoot, bundle.getString("ProjectRootFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(labelForRoot, gridBagConstraints);

        rootFolder.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(rootFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rootFolderButton, bundle.getString("ROOT_DIR_BROWSE_BUTTON_TXT")); // NOI18N
        rootFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rootFolderButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(rootFolderButton, gridBagConstraints);

        labelForProviders.setLabelFor(prividersComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelForProviders, bundle.getString("SelectDiscoveryProviderText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(labelForProviders, gridBagConstraints);

        prividersComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                providersComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(prividersComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(restrictSources, org.openide.util.NbBundle.getMessage(SelectProviderPanel.class, "RestrictSourcesText")); // NOI18N
        restrictSources.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(restrictSources, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(restrictCompile, org.openide.util.NbBundle.getMessage(SelectProviderPanel.class, "RESTRICT_COMPILE_PATH")); // NOI18N
        restrictCompile.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(restrictCompile, gridBagConstraints);

        instructionPanel.setEnabled(false);
        instructionPanel.setFocusable(false);
        instructionPanel.setRequestFocusEnabled(false);
        instructionPanel.setVerifyInputWhenFocusTarget(false);
        instructionPanel.setLayout(new java.awt.BorderLayout());

        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        instructionPanel.add(instructionsTextArea, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        alertPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectProviderPanel.class, "selectedAdvancedLabel")); // NOI18N
        alertPanel.add(jLabel1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(alertPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void providersComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_providersComboBoxItemStateChanged
        Object item = evt.getItem();
        if (item instanceof ProviderItem) {
            ProviderItem provider = (ProviderItem)item;
            instructionsTextArea.setText(provider.getDescription());
            wizard.stateChanged(null);
            if (DiscoveryExtension.MAKE_LOG_PROVIDER.equals(provider.getID())) {
                restrictCompile.setSelected(true);
            } else {
                restrictCompile.setSelected(false);
            }
        }
}//GEN-LAST:event_providersComboBoxItemStateChanged
    
    private void update() {
        wizard.stateChanged(null);
    }
    
    private void rootFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rootFolderButtonActionPerformed
        FileObject projectDirectory = wizard.getWizardDescriptor().getProject().getProjectDirectory();
        ExecutionEnvironment execEnv = FileSystemProvider.getExecutionEnvironment(projectDirectory);
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(execEnv,
                getString("ROOT_DIR_CHOOSER_TITLE_TXT"), // NOI18N
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                JFileChooser.DIRECTORIES_ONLY,
                null, getRootText(),
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        rootFolder.setSelectedItem(path);
    }//GEN-LAST:event_rootFolderButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel alertPanel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel labelForProviders;
    private javax.swing.JLabel labelForRoot;
    private javax.swing.JComboBox prividersComboBox;
    private javax.swing.JCheckBox restrictCompile;
    private javax.swing.JCheckBox restrictSources;
    private javax.swing.JComboBox rootFolder;
    private javax.swing.JButton rootFolderButton;
    // End of variables declaration//GEN-END:variables
    
    void read(final DiscoveryDescriptor wizardDescriptor) {
        DefaultComboBoxModel model = (DefaultComboBoxModel)prividersComboBox.getModel();
        model.removeAllElements();
        ProjectProxy proxy = new ProjectProxy() {
            @Override
            public boolean createSubProjects() {
                return false;
            }
            @Override
            public Project getProject() {
                return wizardDescriptor.getProject();
            }

            @Override
            public String getMakefile() {
                return null;
            }

            @Override
            public String getSourceRoot() {
                return wizardDescriptor.getRootFolder();
            }

            @Override
            public String getExecutable() {
                return wizardDescriptor.getBuildResult();
            }

            @Override
            public String getWorkingFolder() {
                return null;
            }

            @Override
            public boolean mergeProjectProperties() {
                return false;
            }

            @Override
            public boolean resolveSymbolicLinks() {
                return wizardDescriptor.isResolveSymbolicLinks();
            }
        };
        DiscoveryProvider defProvider = (DiscoveryProvider) ((WizardDescriptor)wizardDescriptor).getProperty("PreferedProvider"); // NOI18N
        ProviderItem def = null;
        List<ProviderItem> list = new ArrayList<>();
        for(DiscoveryProvider provider : DiscoveryProviderFactory.findAllProviders()){
            if (provider.isApplicable(proxy)) {
                final ProviderItem providerItem = new ProviderItem(provider);
                if (defProvider != null && defProvider.getID().equals(provider.getID())) {
                    def = providerItem;
                }
                list.add(providerItem);
            }
        }
        Collections.<ProviderItem>sort(list);
        for(ProviderItem item:list){
            model.addElement(item);
        }
        if (def != null){
            prividersComboBox.setSelectedItem(def);
        }
        String path = wizardDescriptor.getRootFolder();
        FileSystem fileSystem = null;
        try {
            fileSystem = wizard.getWizardDescriptor().getProject().getProjectDirectory().getFileSystem();
            if (CndFileUtils.isLocalFileSystem(fileSystem) && Utilities.isWindows()) {
                path = path.replace('/', CndFileUtils.getFileSeparatorChar(fileSystem));
            }
        } catch (FileStateInvalidException ex) {
        }
        Preferences preferences;
        if (USE_PROJECT_PROPERTIES) {
            preferences = ProjectUtils.getPreferences(wizardDescriptor.getProject(), SelectProviderPanel.class, false);
        } else {
            preferences = NbPreferences.forModule(SelectProviderPanel.class);
        }
        ((ExpandableEditableComboBox)rootFolder).setStorage(ROOT_PROPERTY_KEY, preferences);
        if (fileSystem != null) {
            ((ExpandableEditableComboBox)rootFolder).setEnv(FileSystemProvider.getExecutionEnvironment(fileSystem));
        }
        ((ExpandableEditableComboBox)rootFolder).read(path);
    }
    
    private String getRootText() {
        return ((ExpandableEditableComboBox)rootFolder).getText();
    }

    void store(DiscoveryDescriptor wizardDescriptor) {
        ProviderItem provider = (ProviderItem)prividersComboBox.getSelectedItem();
        wizardDescriptor.setProvider(provider.getProvider());
        wizardDescriptor.setRootFolder(getRootText());
        Preferences preferences;
        if (USE_PROJECT_PROPERTIES) {
            preferences = ProjectUtils.getPreferences(wizardDescriptor.getProject(), SelectProviderPanel.class, false);
        } else {
            preferences = NbPreferences.forModule(SelectProviderPanel.class);
        }
        ((ExpandableEditableComboBox)rootFolder).setStorage(ROOT_PROPERTY_KEY, preferences);
        ((ExpandableEditableComboBox)rootFolder).store();
        ProviderProperty p = provider.getProvider().getProperty(ProviderPropertyType.RestrictSourceRootPropertyType.key());
        if (p != null) {
            if (restrictSources.isSelected()){
                ProviderPropertyType.RestrictSourceRootPropertyType.setProperty(provider.getProvider(), getRootText());
            } else {
                ProviderPropertyType.RestrictSourceRootPropertyType.setProperty(provider.getProvider(), ""); // NOI18N
            }
        }
        p = provider.getProvider().getProperty(ProviderPropertyType.RestrictCompileRootPropertyType.key()); // NOI18N
        if (p != null) {
            if (restrictCompile.isSelected()){
                ProviderPropertyType.RestrictCompileRootPropertyType.setProperty(provider.getProvider(), getRootText());
            } else {
                ProviderPropertyType.RestrictCompileRootPropertyType.setProperty(provider.getProvider(), ""); // NOI18N
            }
        }
    }
    
    boolean valid(DiscoveryDescriptor wizardDescriptor) {
  	wizardDescriptor.setMessage(null);
        String path = getRootText();
        if (path == null){
      	    wizardDescriptor.setMessage(getString("SelectModeRootFolderError", path)); // NOI18N
            return false;
        }
        FSPath file;
        try {
            file = new FSPath(wizard.getWizardDescriptor().getProject().getProjectDirectory().getFileSystem(), path);
        } catch (FileStateInvalidException ex) {
      	    wizardDescriptor.setMessage(getString("SelectModeRootFolderError", path)); // NOI18N
            return false;
        }
        FileObject fo = file.getFileObject();
        if (fo == null || !fo.isValid() || !fo.isFolder()) {
      	    wizardDescriptor.setMessage(getString("SelectModeRootFolderError", path)); // NOI18N
            return false;
        }
        ProviderItem provider = (ProviderItem)prividersComboBox.getSelectedItem();
        if (DiscoveryExtension.MODEL_FOLDER_PROVIDER.equals(provider.getID())){ // NOI18N
            Project project = wizardDescriptor.getProject();
            if (project != null){
                CsmProject langProject = CsmModelAccessor.getModel().getProject(project);
                if (langProject != null && langProject.isStable(null)){
                    return true;
                }
            }
      	    wizardDescriptor.setMessage(getString("ModelNotFinishParsing")); // NOI18N
            return false;
        }
        return true;
    }

   void showAlert(DiscoveryDescriptor wizardDescriptor){
        Object o = ((WizardDescriptor)wizardDescriptor).getProperty("ShowAlert");
        alertPanel.setVisible(Boolean.TRUE.equals(o));
    }


    private String getString(String key, String ... params) {
        return NbBundle.getMessage(SelectProviderPanel.class, key, params);
    }

    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
        wizard.stateChanged(null);
    }
    
    @Override
    public void projectLoaded(CsmProject project) {
        wizard.stateChanged(null);
    }
    

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
    }

    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }

    static class ProviderItem implements Comparable<ProviderItem> {
        private final DiscoveryProvider provider;
        ProviderItem(DiscoveryProvider provider){
            this.provider = provider;
        }
        @Override
        public String toString(){
            return provider.getName();
        }
        public String getID(){
            return provider.getID();
        }
        public String getDescription(){
            return provider.getDescription();
        }
        public DiscoveryProvider getProvider(){
            return provider;
        }
        
        @Override
        public int compareTo(ProviderItem o) {
            return toString().compareTo(o.toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ProviderItem)) {
                return false;
            }
            return toString().equals(obj.toString());
        }

        @Override
        public int hashCode() {
           return toString().hashCode();
        }
    }
}
