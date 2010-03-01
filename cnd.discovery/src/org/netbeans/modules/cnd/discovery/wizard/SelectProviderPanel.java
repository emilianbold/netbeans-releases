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
package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public final class SelectProviderPanel extends JPanel implements CsmProgressListener {
    private static boolean SHOW_RESTRICT = Boolean.getBoolean("cnd.discovery.wizard.restrictSources"); // NOI18N
    private SelectProviderWizard wizard;
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
        rootFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        ComboBoxEditor editor = rootFolder.getEditor();
        Component component = editor.getEditorComponent();
        if (component instanceof JTextField) {
            ((JTextField)component).getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
                public void removeUpdate(DocumentEvent e) {
                    update();
                }
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });
        }
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

        rootFolderButton = new javax.swing.JButton();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        alertPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        labelForRoot = new javax.swing.JLabel();
        prividersComboBox = new javax.swing.JComboBox();
        labelForProviders = new javax.swing.JLabel();
        restrictSources = new javax.swing.JCheckBox();
        restrictCompile = new javax.swing.JCheckBox();
        rootFolder = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle"); // NOI18N
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

        instructionPanel.setEnabled(false);
        instructionPanel.setFocusable(false);
        instructionPanel.setRequestFocusEnabled(false);
        instructionPanel.setVerifyInputWhenFocusTarget(false);
        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        alertPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectProviderPanel.class, "selectedAdvancedLabel")); // NOI18N
        alertPanel.add(jLabel1, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        instructionPanel.add(alertPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        labelForRoot.setLabelFor(rootFolder);
        org.openide.awt.Mnemonics.setLocalizedText(labelForRoot, bundle.getString("ProjectRootFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(labelForRoot, gridBagConstraints);

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

        labelForProviders.setLabelFor(prividersComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelForProviders, bundle.getString("SelectDiscoveryProviderText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(labelForProviders, gridBagConstraints);

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

        rootFolder.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(rootFolder, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void providersComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_providersComboBoxItemStateChanged
        Object item = evt.getItem();
        if (item instanceof ProviderItem) {
            ProviderItem provider = (ProviderItem)item;
            instructionsTextArea.setText(provider.getDescription());
            wizard.stateChanged(null);
            if ("make-log".equals(provider.getID())) {// NOI18N
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
        JFileChooser fileChooser = new FileChooser(
                getString("ROOT_DIR_CHOOSER_TITLE_TXT"), // NOI18N
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                JFileChooser.DIRECTORIES_ONLY,
                null,
                getRootText(),
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
        Lookup.Result<DiscoveryProvider> providers = Lookup.getDefault().lookup(new Lookup.Template<DiscoveryProvider>(DiscoveryProvider.class));
        DefaultComboBoxModel model = (DefaultComboBoxModel)prividersComboBox.getModel();
        model.removeAllElements();
        ProjectProxy proxy = new ProjectProxy() {
            public boolean createSubProjects() {
                return false;
            }
            public Project getProject() {
                return wizardDescriptor.getProject();
            }

            public String getMakefile() {
                return null;
            }

            public String getSourceRoot() {
                return wizardDescriptor.getRootFolder();
            }

            public String getExecutable() {
                return wizardDescriptor.getBuildResult();
            }

            public String getWorkingFolder() {
                return null;
            }
        };
        List<ProviderItem> list = new ArrayList<ProviderItem>();
        for(DiscoveryProvider provider : providers.allInstances()){
            provider.clean();
            if (provider.isApplicable(proxy)) {
                list.add(new ProviderItem(provider));
            }
        }
        Collections.<ProviderItem>sort(list);
        for(ProviderItem item:list){
            model.addElement(item);
        }
        ProviderItem def = getDefaultProvider(list,proxy,wizardDescriptor);
        if (def != null){
            prividersComboBox.setSelectedItem(def);
        }
        String path = wizardDescriptor.getRootFolder();
        if (Utilities.isWindows()) {
            path = path.replace('/', File.separatorChar);
        }
        List<String> vector = new ArrayList<String>();
        vector.add(path);
        {
            Preferences prefs = NbPreferences.forModule(SelectProviderPanel.class);
            String old = prefs.get("rootFolder", ""); // NOI18N
            StringTokenizer st = new StringTokenizer(old, "\u0000"); // NOI18N
            int history = 5;
            while(st.hasMoreTokens()) {
                String s = st.nextToken();
                if (!vector.contains(s)) {
                    vector.add(s);
                    history--;
                    if (history == 0) {
                        break;
                    }
                }
            }
        }
        DefaultComboBoxModel rootModel = new DefaultComboBoxModel(vector.toArray());
        rootFolder.setModel(rootModel);
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < 35; i++) {
            buf.append("w"); // NOI18N
        }
        rootFolder.setPrototypeDisplayValue(buf.toString());
    }
    
    private String getRootText() {
        ComboBoxEditor editor = rootFolder.getEditor();
        if (editor != null) {
            Component component = editor.getEditorComponent();
            if (component instanceof JTextField) {
                return ((JTextField)component).getText();
            }
        }
        if (rootFolder.getSelectedItem() != null) {
            return rootFolder.getSelectedItem().toString();
        }
        return null;
    }

    private ProviderItem getDefaultProvider(List<ProviderItem> list, ProjectProxy proxy, DiscoveryDescriptor wizardDescriptor){
        ProviderItem def = null;
        int assurance = 0;
        for(ProviderItem item:list){
            if ("dwarf-executable".equals(item.getID())){ // NOI18N
                // select executable if make project has output
                // and output has debug information.
                item.getProvider().getProperty("executable").setValue(wizardDescriptor.getBuildResult()); // NOI18N
            } else if ("dwarf-folder".equals(item.getID())){ // NOI18N
                item.getProvider().getProperty("folder").setValue(wizardDescriptor.getRootFolder()); // NOI18N
            }
            int i = item.getProvider().canAnalyze(proxy);
            if (i > assurance) {
                def = item;
                assurance = i;
            }
        }
        return def;
    }
    
    void store(DiscoveryDescriptor wizardDescriptor) {
        ProviderItem provider = (ProviderItem)prividersComboBox.getSelectedItem();
        wizardDescriptor.setProvider(provider.getProvider());
        wizardDescriptor.setRootFolder(getRootText());
        {
            List<String> vector = new ArrayList<String>();
            vector.add(getRootText());
            for(int i = 0; i < rootFolder.getModel().getSize(); i++){
                String s = rootFolder.getModel().getElementAt(i).toString();
                if (!vector.contains(s)) {
                    vector.add(s);
                }
            }
            StringBuilder buf = new StringBuilder();
            for(String s : vector) {
                if (buf.length()>0) {
                    buf.append((char)0);
                }
                buf.append(s);
            }
            Preferences prefs = NbPreferences.forModule(SelectProviderPanel.class);
            prefs.put("rootFolder", buf.toString()); // NOI18N
        }
        ProviderProperty p = provider.getProvider().getProperty("restrict_source_root"); // NOI18N
        if (p != null) {
            if (restrictSources.isSelected()){
                p.setValue(getRootText());
            } else {
                p.setValue(""); // NOI18N
            }
        }
        p = provider.getProvider().getProperty("restrict_compile_root"); // NOI18N
        if (p != null) {
            if (restrictCompile.isSelected()){
                p.setValue(getRootText());
            } else {
                p.setValue(""); // NOI18N
            }
        }
    }
    
    boolean valid(DiscoveryDescriptor wizardDescriptor) {
  	wizardDescriptor.setMessage(null);
        String path = getRootText();
        if (path == null){
            return false;
        }
        File file = new File(path);
        if (!(file.exists() && file.isDirectory())) {
            return false;
        }
        ProviderItem provider = (ProviderItem)prividersComboBox.getSelectedItem();
        if ("model-folder".equals(provider.getID())){ // NOI18N
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


    private String getString(String key) {
        return NbBundle.getBundle(SelectProviderPanel.class).getString(key);
    }

    public void projectParsingStarted(CsmProject project) {
    }

    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    public void projectParsingFinished(CsmProject project) {
        wizard.stateChanged(null);
    }
    
    public void projectLoaded(CsmProject project) {
        wizard.stateChanged(null);
    }
    

    public void projectParsingCancelled(CsmProject project) {
    }

    public void fileInvalidated(CsmFile file) {
    }

    public void fileAddedToParse(CsmFile file) {
    }

    public void fileParsingStarted(CsmFile file) {
    }

    public void fileParsingFinished(CsmFile file) {
    }

    public void parserIdle() {
    }

    private static class ProviderItem implements Comparable<ProviderItem> {
        private DiscoveryProvider provider;
        private ProviderItem(DiscoveryProvider provider){
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
        
        public int compareTo(ProviderItem o) {
            return toString().compareTo( o.toString() );
        }
    }
}
