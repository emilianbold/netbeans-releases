/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.tree.ConfigurationFactory;
import org.netbeans.modules.cnd.discovery.wizard.tree.FileConfigurationNode;
import org.netbeans.modules.cnd.discovery.wizard.tree.FolderConfigurationNode;
import org.netbeans.modules.cnd.discovery.wizard.tree.IncludesListModel;
import org.netbeans.modules.cnd.discovery.wizard.tree.MacrosListModel;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationImpl;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationNode;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public final class SelectConfigurationPanel extends JPanel {
    private SelectConfigurationWizard wizard;
    private String oldConsolidation;
    private boolean showResulting;
    private boolean wasTerminated = false;
    private boolean isStoped = false;
    
    /** Creates new form DiscoveryVisualPanel2 */
    public SelectConfigurationPanel(SelectConfigurationWizard wizard) {
        this.wizard = wizard;
        initComponents();
        configurationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addListeners();
        clearListModels();
    }
    
    private void addListeners(){
        configurationTree.addTreeSelectionListener(new TreeSelectionListener(){
            public void valueChanged(TreeSelectionEvent e) {
                updateListModels();
            }
        });
        showInherited.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                showResulting = showInherited.isSelected();
                updateListModels();
            }
        });
    }
    
    private void updateListModels() {
        TreePath path = configurationTree.getSelectionPath();
        if (path != null) {
            Object selected = path.getLastPathComponent();
            if (selected instanceof ProjectConfigurationNode){
                ProjectConfigurationNode node = (ProjectConfigurationNode)selected;
                includesList.setModel(new IncludesListModel(node.getProject(),showResulting));
                macrosList.setModel(new MacrosListModel(node.getProject(),showResulting));
                includeInherate.setSelected(false);
                macroInherate.setSelected(false);
            } else if (selected instanceof FolderConfigurationNode){
                FolderConfigurationNode node = (FolderConfigurationNode)selected;
                includesList.setModel(new IncludesListModel(node.getFolder(),showResulting));
                macrosList.setModel(new MacrosListModel(node.getFolder(),showResulting));
                includeInherate.setSelected(node.isCheckedInclude());
                macroInherate.setSelected(node.isCheckedMacro());
            } else if (selected instanceof FileConfigurationNode){
                FileConfigurationNode node = (FileConfigurationNode)selected;
                includesList.setModel(new IncludesListModel(node.getFile(),showResulting));
                macrosList.setModel(new MacrosListModel(node.getFile(),showResulting));
                includeInherate.setSelected(node.isCheckedInclude());
                macroInherate.setSelected(node.isCheckedMacro());
            } else {
                clearListModels();
            }
        } else {
            clearListModels();
        }
    }
    
    private void clearListModels(){
        includesList.setModel(new EmptyListModel());
        macrosList.setModel(new EmptyListModel());
        includeInherate.setSelected(false);
        macroInherate.setSelected(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        configurationTree = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        includesList = new javax.swing.JList();
        includeInherate = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        macrosList = new javax.swing.JList();
        macroInherate = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        showInherited = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        setPreferredSize(new java.awt.Dimension(400, 400));
        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(200);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(configurationTree);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("TreeConfigurationTitle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        configurationTree.setRootVisible(false);
        configurationTree.setShowsRootHandles(true);
        jScrollPane1.setViewportView(configurationTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(200);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(includesList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("InludePathsListTitle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel3.add(jLabel2, gridBagConstraints);

        jScrollPane2.setViewportView(includesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(includeInherate, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("InheriteParentIncludePathsText"));
        includeInherate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeInherate.setEnabled(false);
        includeInherate.setFocusable(false);
        includeInherate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(includeInherate, gridBagConstraints);

        jSplitPane2.setTopComponent(jPanel3);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(macrosList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("UserMacrosListTitle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel4.add(jLabel3, gridBagConstraints);

        jScrollPane3.setViewportView(macrosList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(jScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(macroInherate, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("InheriteParentMacrosText"));
        macroInherate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        macroInherate.setEnabled(false);
        macroInherate.setFocusable(false);
        macroInherate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(macroInherate, gridBagConstraints);

        jSplitPane2.setRightComponent(jPanel4);

        jPanel2.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(showInherited, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("ShowInheritedConfigurationName"));
        showInherited.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showInherited.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel5.add(showInherited, gridBagConstraints);

        add(jPanel5, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents
    
    private String getString(String key) {
        return NbBundle.getBundle(SelectConfigurationPanel.class).getString(key);
    }
    
    private Icon getLoadingIcon() {
        String path = "org/netbeans/modules/cnd/discovery/wizard/resources/waitNode.gif"; // NOI18N
        Image image = Utilities.loadImage(path);
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }
    
    void read(final DiscoveryDescriptor wizardDescriptor) {
        String consolidation = wizardDescriptor.getLevel();
        boolean changedConsolidation = false;
        if (!consolidation.equals(oldConsolidation)) {
            oldConsolidation = consolidation;
            changedConsolidation = true;
        }
        if (wizardDescriptor.isInvokeProvider() || wasTerminated) {
            // clear model
            wizardDescriptor.setConfigurations(null);
            ConfigurationTreeModel model = new ConfigurationTreeModel();
            DefaultMutableTreeNode loading= new DefaultMutableTreeNode(getString("LoadingRootText")); // NOI18N
            ((DefaultMutableTreeNode)model.getRoot()).add(loading);
            DefaultTreeCellRenderer renderer= new DefaultTreeCellRenderer();
            configurationTree.setCellRenderer(renderer);
            renderer.setLeafIcon(getLoadingIcon());
            configurationTree.setModel(model);
            // count configurations in other thread.
            AnalyzingTask task = new AnalyzingTask(wizardDescriptor);
            RequestProcessor.getDefault().post(task);
            //task.start();
            isStoped = false;
            wasTerminated = true;
        } else if (changedConsolidation){
            List<ProjectConfiguration> projectConfigurations = wizardDescriptor.getConfigurations();
            if (projectConfigurations != null) {
                for(ProjectConfiguration project : projectConfigurations){
                    consolidateModel(project, consolidation);
                }
            }
            updateListModels();
        }
    }
    
    private static void consolidateModel(ProjectConfiguration project, String level){
        if (ConsolidationStrategyPanel.PROJECT_LEVEL.equals(level)){
            ConfigurationFactory.consolidateProject((ProjectConfigurationImpl)project);
        } else if (ConsolidationStrategyPanel.FOLDER_LEVEL.equals(level)){
            ConfigurationFactory.consolidateFolder((ProjectConfigurationImpl)project);
        } else if (ConsolidationStrategyPanel.FILE_LEVEL.equals(level)){
            ConfigurationFactory.consolidateFile((ProjectConfigurationImpl)project);
        }
    }
    
    public static void buildModel(final DiscoveryDescriptor wizardDescriptor){
        String rootFolder = wizardDescriptor.getRootFolder();
        DiscoveryProvider provider = wizardDescriptor.getProvider();
        String consolidation = wizardDescriptor.getLevel();
        boolean cutResult = wizardDescriptor.isCutResult();
        List<Configuration> configs = provider.analyze(new ProjectProxy() {
            public boolean createSubProjects() {
                return false;
            }
            public Object getProject() {
                return wizardDescriptor.getProject();
            }
        });
        List<ProjectConfiguration> projectConfigurations = new ArrayList<ProjectConfiguration>();
        List<String> includedFiles = new ArrayList<String>();
        wizardDescriptor.setIncludedFiles(includedFiles);
        for (Iterator<Configuration> it = configs.iterator(); it.hasNext();) {
            Configuration conf = it.next();
            includedFiles.addAll(conf.getIncludedFiles());
            List<ProjectProperties> langList = conf.getProjectConfiguration();
            for (Iterator<ProjectProperties> it2 = langList.iterator(); it2.hasNext();) {
                ProjectConfigurationImpl project = ConfigurationFactory.makeRoot(it2.next(), rootFolder, cutResult);
                consolidateModel(project, consolidation);
                projectConfigurations.add(project);
            }
        }
        wizardDescriptor.setInvokeProvider(false);
        wizardDescriptor.setConfigurations(projectConfigurations);
    }
    
    private void creteTreeModel(DiscoveryDescriptor wizardDescriptor){
        ConfigurationTreeModel model = new ConfigurationTreeModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        List<ProjectConfiguration> projectConfigurations = wizardDescriptor.getConfigurations();
        if (projectConfigurations != null) {
            for(ProjectConfiguration project : projectConfigurations){
                root.add(new ProjectConfigurationNode((ProjectConfigurationImpl)project));
            }
        }
        configurationTree.setCellRenderer(new DefaultTreeCellRenderer());
        configurationTree.setModel(model);
    }
    
    void store(DiscoveryDescriptor wizardDescriptor) {
        DiscoveryProvider provider = wizardDescriptor.getProvider();
        List<ProjectConfiguration> projectConfigurations = wizardDescriptor.getConfigurations();
        if (provider != null && wasTerminated){
            //System.out.println("Stop analyzing");
            isStoped = true;
            provider.stop();
        }
    }
    
    boolean isValid(DiscoveryDescriptor settings) {
        List<ProjectConfiguration> projectConfigurations = settings.getConfigurations();
        if (projectConfigurations == null || projectConfigurations.isEmpty()) {
            return false;
        }
        return projectConfigurations.get(0).getFiles().size()>0;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree configurationTree;
    private javax.swing.JCheckBox includeInherate;
    private javax.swing.JList includesList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JCheckBox macroInherate;
    private javax.swing.JList macrosList;
    private javax.swing.JCheckBox showInherited;
    // End of variables declaration//GEN-END:variables
    
    
    private static class ConfigurationTreeModel extends DefaultTreeModel {
        public ConfigurationTreeModel() {
            super(new DefaultMutableTreeNode("Root")); // NOI18N
        }
    }
    
    public static class EmptyListModel extends AbstractListModel {
        public int getSize() {
            return 0;
        }
        public Object getElementAt(int i) {
            return null;
        }
    }
    
    private class AnalyzingTask extends Thread {
        private DiscoveryDescriptor wizardDescriptor;
        public AnalyzingTask(DiscoveryDescriptor wizardDescriptor){
            this.wizardDescriptor = wizardDescriptor;
        }
        @Override
        public void run() {
            buildModel(wizardDescriptor);
            creteTreeModel(wizardDescriptor);
            wizard.stateChanged(null);
            //System.out.println("End analyzing");
            if (!isStoped){
                wasTerminated = false;
            }
        }
    }
}
