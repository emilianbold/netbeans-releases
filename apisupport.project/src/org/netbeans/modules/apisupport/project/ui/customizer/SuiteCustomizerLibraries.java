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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;

/**
 * Represents <em>Libraries</em> panel in Suite customizer.
 *
 * @author Martin Krauskopf
 */
final class SuiteCustomizerLibraries extends NbPropertyPanel.Suite
        implements Comparator, ExplorerManager.Provider, ChangeListener {
    private ExplorerManager manager;
    private ModuleEntry[] platformModules;
    private ProjectCustomizer.Category cat;
    
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerLibraries(final SuiteProperties suiteProps, ProjectCustomizer.Category cat) {
        super(suiteProps, SuiteCustomizerLibraries.class);
        initComponents();
        this.cat = cat;
        initAccessibility();
        manager = new ExplorerManager();
        refresh();
        
        
        view.setProperties(new Node.Property[] { ENABLED_PROP_TEMPLATE });
        view.setRootVisible(false);
        view.setDefaultActionAllowed(false);
        
        suiteProps.getBrandingModel().addChangeListener(this);
        suiteProps.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (SuiteProperties.NB_PLATFORM_PROPERTY.equals(evt.getPropertyName())) {
                    refresh();
                }
            }
        });
        
        javaPlatformCombo.setRenderer(JavaPlatformComponentFactory.javaPlatformListCellRenderer());
    }
    
    void refresh() {
        refreshJavaPlatforms();
        refreshPlatforms();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                refreshModules();
            }
        });
        updateJavaPlatformEnabled();
    }
    
    private void refreshModules() {
        platformModules = getProperties().getActivePlatform().getModules();
        Node root = createPlatformModulesNode();
        manager.setRootContext(root);
        synchronized (this) {
            universe = null;
        }
        updateDependencyWarnings();
    }
    
    private void refreshJavaPlatforms() {
        javaPlatformCombo.setModel(JavaPlatformComponentFactory.javaPlatformListModel());
        javaPlatformCombo.setSelectedItem(getProperties().getActiveJavaPlatform());
    }
    
    private void refreshPlatforms() {
        platformValue.setModel(new PlatformComponentFactory.NbPlatformListModel()); // refresh
        platformValue.setSelectedItem(getProperties().getActivePlatform());
        platformValue.requestFocus();
    }
    
    public void store() {
        Set enabledClusters = new TreeSet();
        Set disabledModules = new TreeSet();
        
        Node[] clusters = getExplorerManager().getRootContext().getChildren().getNodes();
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i] instanceof Enabled) {
                Enabled e = (Enabled)clusters[i];
                if (e.isEnabled()) {
                    enabledClusters.add(e.getName());
                    Node[] modules = e.getChildren().getNodes();
                    for (int j = 0; j < modules.length; j++) {
                        if (modules[j] instanceof Enabled) {
                            Enabled m = (Enabled)modules[j];
                            if (!m.isEnabled()) {
                                disabledModules.add(m.getName());
                            }
                        }
                    }
                }
            }
        }
        
        getProperties().setEnabledClusters((String[]) enabledClusters.toArray(new String[enabledClusters.size()]));
        getProperties().setDisabledModules((String[]) disabledModules.toArray(new String[disabledModules.size()]));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformsPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        javaPlatformLabel = new javax.swing.JLabel();
        javaPlatformCombo = new javax.swing.JComboBox();
        javaPlatformButton = new javax.swing.JButton();
        filler = new javax.swing.JLabel();
        view = new org.openide.explorer.view.TreeTableView();
        viewLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        platformsPanel.setLayout(new java.awt.GridBagLayout());

        platformValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                platformValueItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        platformsPanel.add(platformValue, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        platformsPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_ManagePlatform_a"));
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        platformsPanel.add(managePlafsButton, gridBagConstraints);

        javaPlatformLabel.setLabelFor(javaPlatformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_Java_Platform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformLabel, gridBagConstraints);

        javaPlatformCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                javaPlatformComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformButton, NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_Manage_Java_Platforms"));
        javaPlatformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaPlatformButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformsPanel.add(javaPlatformButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(platformsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        view.setBorder(javax.swing.UIManager.getBorder("ScrollPane.border"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(view, gridBagConstraints);

        viewLabel.setLabelFor(view);
        org.openide.awt.Mnemonics.setLocalizedText(viewLabel, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_PlatformModules"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 0);
        add(viewLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void javaPlatformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaPlatformButtonActionPerformed
        PlatformsCustomizer.showCustomizer((JavaPlatform) javaPlatformCombo.getSelectedItem());
    }//GEN-LAST:event_javaPlatformButtonActionPerformed

    private void javaPlatformComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_javaPlatformComboItemStateChanged
        getProperties().setActiveJavaPlatform((JavaPlatform) javaPlatformCombo.getSelectedItem());
    }//GEN-LAST:event_javaPlatformComboItemStateChanged
    
    private void platformValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_platformValueItemStateChanged
        getProperties().setActivePlatform((NbPlatform) platformValue.getSelectedItem());
        refreshModules();
        updateJavaPlatformEnabled();
    }//GEN-LAST:event_platformValueItemStateChanged
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        refreshPlatforms();
    }//GEN-LAST:event_managePlatforms
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filler;
    private javax.swing.JButton javaPlatformButton;
    private javax.swing.JComboBox javaPlatformCombo;
    private javax.swing.JLabel javaPlatformLabel;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JPanel platformsPanel;
    private org.openide.explorer.view.TreeTableView view;
    private javax.swing.JLabel viewLabel;
    // End of variables declaration//GEN-END:variables
    
    
    private Node createPlatformModulesNode() {
        HashSet disabledModuleCNB = new HashSet(Arrays.asList(getProperties().getDisabledModules()));
        HashSet enabledClusters = new HashSet(Arrays.asList(getProperties().getEnabledClusters()));
        
        HashMap clusterToChildren = new HashMap();
        
        Children.SortedArray clusters = new Children.SortedArray();
        clusters.setComparator(this);
        AbstractNode n = new AbstractNode(clusters);
        n.setName(getMessage("LBL_ModuleListClusters"));
        n.setDisplayName(getMessage("LBL_ModuleListClustersModules"));
        
        for (int i = 0; i < platformModules.length; i++) {
            Children clusterChildren = (Children)clusterToChildren.get(platformModules[i].getClusterDirectory());
            if (clusterChildren == null) {
                Children.SortedArray modules = new Children.SortedArray();
                modules.setComparator(this);
                clusterChildren = modules;
                
                String clusterName = platformModules[i].getClusterDirectory().getName();
                Enabled cluster = new Enabled(modules, enabledClusters.contains(clusterName));
                cluster.setName(clusterName);
                cluster.setIconBaseWithExtension(SuiteProject.SUITE_ICON_PATH);
                clusterToChildren.put(platformModules[i].getClusterDirectory(), modules);
                n.getChildren().add(new Node[] { cluster });
            }
            
            String cnb = platformModules[i].getCodeNameBase();
            AbstractNode module = new Enabled(Children.LEAF, !disabledModuleCNB.contains(cnb));
            module.setName(cnb);
            module.setDisplayName(platformModules[i].getLocalizedName());
            String desc = platformModules[i].getShortDescription();
            String tooltip;
            if (desc != null) {
                if (desc.startsWith("<html>")) { // NOI18N
                    tooltip = "<html>" + NbBundle.getMessage(SuiteCustomizerLibraries.class, "SuiteCustomizerLibraries.HINT_module_desc", cnb, desc.substring(6));
                } else {
                    tooltip = NbBundle.getMessage(SuiteCustomizerLibraries.class, "SuiteCustomizerLibraries.HINT_module_desc", cnb, desc);
                }
            } else {
                tooltip = NbBundle.getMessage(SuiteCustomizerLibraries.class, "SuiteCustomizerLibraries.HINT_module_no_desc", cnb);
            }
            module.setShortDescription(tooltip);
            module.setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            
            clusterChildren.add(new Node[] { module });
        }
        
        return n;
    }
    
    public int compare(Object o1, Object o2) {
        Node n1 = (Node)o1;
        Node n2 = (Node)o2;
        
        return n1.getDisplayName().compareTo(n2.getDisplayName());
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private static final Set<String> DISABLED_PLATFORM_MODULES = new HashSet();
    
    static {
        // Probably not needed for most platform apps, and won't even work under JNLP.
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.autoupdate"); // NOI18N
        // XXX the following would not be shown in regular apps anyway, because they are autoloads,
        // but they *are* shown in JNLP apps because currently even unused autoloads are enabled under JNLP:
        // Just annoying; e.g. shows Runtime tab prominently.
        DISABLED_PLATFORM_MODULES.add("org.openide.execution"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.core.execution"); // NOI18N
        // Similar - unlikely to really be wanted by typical platform apps, and show some GUI.
        DISABLED_PLATFORM_MODULES.add("org.openide.io"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.core.output2"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.core.multiview"); // NOI18N
        // this one is useful only for writers of apps showing local disk
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.favorites"); // NOI18N
        // And these are deprecated:
        DISABLED_PLATFORM_MODULES.add("org.openide.compat"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.openide.util.enumerations"); // NOI18N
    }
    
    public void stateChanged(ChangeEvent ev) {
        if (getProperties().getBrandingModel().isBrandingEnabled()) {
            // User is turning on branded mode. Let's take a guess: they want to
            // exclude the usual suspects from the module list. We do not want to set
            // these excludes on a new suite because user might want to use real IDE as the platform
            // (i.e. not be creating an app, but rather be creating some modules for the IDE).
            // Only do this if there are no existing exclusions.
            Node[] clusters = getExplorerManager().getRootContext().getChildren().getNodes();
            for (int i = 0; i < clusters.length; i++) {
                if (clusters[i] instanceof Enabled) {
                    Enabled e = (Enabled) clusters[i];
                    if (!e.isEnabled()) {
                        return;
                    } else {
                        Node[] modules = e.getChildren().getNodes();
                        for (int j = 0; j < modules.length; j++) {
                            if (modules[j] instanceof Enabled) {
                                Enabled m = (Enabled) modules[j];
                                if (!m.isEnabled()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            // #64443: prompt first.
            if (!UIUtil.showAcceptCancelDialog(
                    getMessage("SuiteCustomizerLibraries.title.exclude_ide_modules"),
                    getMessage("SuiteCustomizerLibraries.text.exclude_ide_modules"),
                    getMessage("SuiteCustomizerLibraries.button.exclude"),
                    getMessage("SuiteCustomizerLibraries.button.skip"),
                    NotifyDescriptor.QUESTION_MESSAGE)) {
                return;
            }
            // OK, continue.
            for (int i = 0; i < clusters.length; i++) {
                if (clusters[i] instanceof Enabled) {
                    Enabled e = (Enabled) clusters[i];
                    if (e.getName().startsWith("platform")) { // NOI18N
                        Node[] modules = e.getChildren().getNodes();
                        for (int j = 0; j < modules.length; j++) {
                            if (modules[j] instanceof Enabled) {
                                Enabled m = (Enabled) modules[j];
                                if (DISABLED_PLATFORM_MODULES.contains(m.getName())) {
                                    m.setEnabled(false);
                                }
                            }
                        }
                    } else {
                        e.setEnabled(false);
                    }
                }
            }
        }
    }
    
    final class Enabled extends AbstractNode {
        private boolean enabled;
        private Children standard;
        
        public Enabled(Children ch, boolean enabled) {
            super(ch);
            this.standard = ch;
            this.enabled = enabled;
            
            Sheet s = Sheet.createDefault();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            ss.put(new EnabledProp(this));
            setSheet(s);
        }
        
        public void setEnabled(boolean s) {
            if (s == enabled) {
                return;
            }
            enabled = s;
            //refresh childern
            Node[] all = standard.getNodes();
            for (int i = 0; i < all.length; i++) {
                Node nn = all[i];
                if (nn instanceof Enabled) {
                    Enabled en = (Enabled)nn;
                    en.firePropertyChange(null, null, null);
                }
            }
            //refresh parent
            Node n = getParentNode();
            if (n instanceof Enabled) {
                Enabled en = (Enabled)n;
                en.firePropertyChange(null, null, null);
            }
            updateDependencyWarnings();
        }
        
        public boolean isEnabled() {
            return enabled;
        }
    }
    
    private static final EnabledProp ENABLED_PROP_TEMPLATE = new EnabledProp(null);
    private static final class EnabledProp extends PropertySupport.ReadWrite {
        
        private Enabled node;
        private PropertyEditor editor;
        
        public EnabledProp(Enabled node) {
            super("enabled", Boolean.TYPE, getMessage("LBL_ModuleListEnabled"), getMessage("LBL_ModuleListEnabledShortDescription"));
            this.node = node;
        }
        
        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            node.setEnabled(((Boolean)val).booleanValue());
        }
        
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            Children ch = node.getChildren();
            if (ch == Children.LEAF) {
                return Boolean.valueOf(node.isEnabled());
            } else {
                Node[] arr = ch.getNodes();
                boolean on = false;
                boolean off = false;
                for (int i = 0; i < arr.length; i++) {
                    Enabled n = (Enabled)arr[i];
                    if (n.isEnabled()) {
                        on = true;
                    } else {
                        off = true;
                    }
                    
                    if (on && off && node.isEnabled()) {
                        return null;
                    }
                }
                
                return Boolean.valueOf(on && node.isEnabled());
            }
        }
        
        public boolean canWrite() {
            Node parent = node.getParentNode();
            if (parent instanceof Enabled) {
                // cluster node
                return ((Enabled)parent).isEnabled();
            }
            return true;
        }
        
        public PropertyEditor getPropertyEditor() {
            if (editor == null) {
                editor = super.getPropertyEditor();
            }
            return editor;
        }
        
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(CustomizerDisplay.class, key);
    }
    
    private void initAccessibility() {
        managePlafsButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ManagePlafsButton"));
        platformValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PlatformValue"));
        javaPlatformCombo.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformCombo"));
        javaPlatformButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformButton"));
    }
    
    // #65924: show warnings if some dependencies cannot be satisfied
    
    interface UniverseModule {
        String getCodeNameBase();
        int getReleaseVersion();
        SpecificationVersion getSpecificationVersion();
        String getImplementationVersion();
        Set<String> getProvidedTokens();
        Set<String> getRequiredTokens();
        Set<Dependency> getModuleDependencies();
        String getCluster();
        String getDisplayName();
    }
    
    private static abstract class AbstractUniverseModule implements UniverseModule {
        protected final ManifestManager mm;
        protected AbstractUniverseModule(ManifestManager mm) {
            this.mm = mm;
        }
        public int getReleaseVersion() {
            String s = mm.getReleaseVersion();
            return s != null ? Integer.parseInt(s) : -1;
        }
        public String getImplementationVersion() {
            return mm.getImplementationVersion();
        }
        public Set<String> getProvidedTokens() {
            return new HashSet(Arrays.asList(mm.getProvidedTokens()));
        }
        public Set<String> getRequiredTokens() {
            Set s = new HashSet(Arrays.asList(mm.getRequiredTokens()));
            Iterator it = s.iterator();
            while (it.hasNext()) {
                String tok = (String) it.next();
                if (tok.startsWith("org.openide.modules.ModuleFormat") || tok.startsWith("org.openide.modules.os.")) { // NOI18N
                    it.remove();
                }
            }
            s.addAll(Arrays.asList(mm.getNeededTokens()));
            return s;
        }
        public String toString() {
            return getCodeNameBase();
        }
    }
    
    private static final class PlatformModule extends AbstractUniverseModule {
        private final ModuleEntry entry;
        public PlatformModule(ModuleEntry entry) throws IOException {
            super(ManifestManager.getInstanceFromJAR(entry.getJarLocation()));
            this.entry = entry;
        }
        public String getCodeNameBase() {
            return entry.getCodeNameBase();
        }
        public SpecificationVersion getSpecificationVersion() {
            String s = entry.getSpecificationVersion();
            return s != null ? new SpecificationVersion(s) : null;
        }
        public Set<Dependency> getModuleDependencies() {
            return mm.getModuleDependencies();
        }
        public String getCluster() {
            return entry.getClusterDirectory().getName();
        }
        public String getDisplayName() {
            return entry.getLocalizedName();
        }
    }
    
    private static final class SuiteModule extends AbstractUniverseModule {
        private final NbModuleProject project;
        private final Set<Dependency> dependencies;
        public SuiteModule(NbModuleProject project) {
            super(ManifestManager.getInstance(project.getManifest(), false));
            this.project = project;
            dependencies = new HashSet();
            // Cannot use ProjectXMLManager since we need to report also deps on nonexistent modules.
            Element dataE = project.getPrimaryConfigurationData();
            Element depsE = Util.findElement(dataE, "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            Iterator<Element> deps = Util.findSubElements(depsE).iterator();
            while (deps.hasNext()) {
                Element dep = (Element) deps.next();
                Element run = Util.findElement(dep, "run-dependency", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (run == null) {
                    continue;
                }
                String text = Util.findText(Util.findElement(dep, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED)); // NOI18N
                Element relverE = Util.findElement(run, "release-version", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (relverE != null) {
                    text += '/' + Util.findText(relverE);
                }
                Element specverE = Util.findElement(run, "specification-version", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (specverE != null) {
                    text += " > " + Util.findText(specverE);
                } else {
                    Element implver = Util.findElement(run, "implementation-version", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                    if (implver != null) {
                        // Will special-case '*' as an impl version to mean "match anything".
                        text += " = *"; // NOI18N
                    }
                }
                dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, text));
            }
        }
        public String getCodeNameBase() {
            return project.getCodeNameBase();
        }
        public SpecificationVersion getSpecificationVersion() {
            String s = project.getSpecVersion();
            return s != null ? new SpecificationVersion(s) : null;
        }
        public Set<Dependency> getModuleDependencies() {
            return dependencies;
        }
        public String getCluster() {
            return null;
        }
        public String getDisplayName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
    }

    private RequestProcessor.Task updateDependencyWarningsTask;
    private void updateDependencyWarnings() {
        // XXX avoid running unless and until we become visible, perhaps
        if (updateDependencyWarningsTask == null) {
            updateDependencyWarningsTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    doUpdateDependencyWarnings();
                }
            });
        }
        updateDependencyWarningsTask.schedule(0);
    }
    
    static Set<UniverseModule> loadUniverseModules(ModuleEntry[] platformModules, Set<NbModuleProject> suiteModules) throws IOException {
        Set universe = new LinkedHashSet();
        Iterator<NbModuleProject> it = suiteModules.iterator();
        while (it.hasNext()) {
            universe.add(new SuiteModule((NbModuleProject) it.next()));
        }
        for (int i = 0; i < platformModules.length; i++) {
            universe.add(new PlatformModule(platformModules[i]));
        }
        return universe;
    }
    
    static String[] findWarning(Set<UniverseModule> universeModules, Set<String> enabledClusters, Set<String> disabledModules) {
        SortedMap<String,UniverseModule> sortedModules = new TreeMap();
        Set<UniverseModule> excluded = new HashSet();
        Map<String,Set<UniverseModule>> providers = new HashMap();
        Iterator it = universeModules.iterator();
        while (it.hasNext()) {
            UniverseModule m = (UniverseModule) it.next();
            String cnb = m.getCodeNameBase();
            String cluster = m.getCluster();
            if (cluster != null && (!enabledClusters.contains(cluster) || disabledModules.contains(cnb))) {
                excluded.add(m);
            }
            sortedModules.put(cnb, m);
            Iterator<String> provides = m.getProvidedTokens().iterator();
            while (provides.hasNext()) {
                String tok = (String) provides.next();
                Set<UniverseModule> providersOf = (Set) providers.get(tok);
                if (providersOf == null) {
                    providersOf = new TreeSet(UNIVERSE_MODULE_COMPARATOR);
                    providers.put(tok, providersOf);
                }
                providersOf.add(m);
            }
        }
        it = sortedModules.values().iterator();
        while (it.hasNext()) {
            UniverseModule m = (UniverseModule) it.next();
            if (excluded.contains(m)) {
                continue;
            }
            String[] warning = findWarning(m, sortedModules, providers, excluded);
            if (warning != null) {
                return warning;
            }
        }
        return null;
    }
    private static final Comparator<UniverseModule> UNIVERSE_MODULE_COMPARATOR = new Comparator() {
        Collator COLL = Collator.getInstance();
        public int compare(Object o1, Object o2) {
            return COLL.compare(((UniverseModule) o1).getDisplayName(), ((UniverseModule) o2).getDisplayName());
        }
    };
    
    private Set<UniverseModule> universe;
    private /* #71791 */ synchronized void doUpdateDependencyWarnings() {
        if (universe == null) {
            try {
                Set<NbModuleProject> suiteModules = getProperties().getSubModules();
                universe = loadUniverseModules(platformModules, suiteModules);
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                return; // any warnings would probably be wrong anyway
            }
        }
        
        Set enabledClusters = new TreeSet();
        Set disabledModules = new TreeSet();
        
        Node[] clusters = getExplorerManager().getRootContext().getChildren().getNodes();
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i] instanceof Enabled) {
                Enabled e = (Enabled) clusters[i];
                if (e.isEnabled()) {
                    enabledClusters.add(e.getName());
                    Node[] modules = e.getChildren().getNodes();
                    for (int j = 0; j < modules.length; j++) {
                        if (modules[j] instanceof Enabled) {
                            Enabled m = (Enabled) modules[j];
                            if (!m.isEnabled()) {
                                disabledModules.add(m.getName());
                            }
                        }
                    }
                }
            }
        }
        
        final String[] warning = findWarning(universe, enabledClusters, disabledModules);
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (warning != null) {
                    String key = warning[0];
                    String[] args = new String[warning.length - 1];
                    System.arraycopy(warning, 1, args, 0, args.length);
                    cat.setErrorMessage(NbBundle.getMessage(SuiteCustomizerLibraries.class, key, args));
                } else {
                    cat.setErrorMessage(null);
                }
            }
        });
        
    }

    private static String[] findWarning(UniverseModule m, Map<String,UniverseModule> modules, Map<String,Set<UniverseModule>> providers, Set<UniverseModule> excluded) {
        // Check module dependencies:
        SortedSet<Dependency> deps = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                Dependency d1 = (Dependency) o1;
                Dependency d2 = (Dependency) o2;
                return d1.getName().compareTo(d2.getName());
            }
        });
        deps.addAll(m.getModuleDependencies());
        Iterator it = deps.iterator();
        while (it.hasNext()) {
            Dependency d = (Dependency) it.next();
            String codename = d.getName();
            String cnb;
            int mrvLo, mrvHi;
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                cnb = codename;
                mrvLo = -1;
                mrvHi = -1;
            } else {
                cnb = codename.substring(0, slash);
                String mrv = codename.substring(slash + 1);
                int dash = mrv.lastIndexOf('-');
                if (dash == -1) {
                    mrvLo = mrvHi = Integer.parseInt(mrv);
                } else {
                    mrvLo = Integer.parseInt(mrv.substring(0, dash));
                    mrvHi = Integer.parseInt(mrv.substring(dash + 1));
                }
            }
            UniverseModule dep = (UniverseModule) modules.get(cnb);
            if (dep == null) {
                if (m.getCluster() != null) {
                    return new String[] {"ERR_platform_no_dep", m.getDisplayName(), m.getCluster(), cnb};
                } else {
                    return new String[] {"ERR_suite_no_dep", m.getDisplayName(), cnb};
                }
            }
            if (excluded.contains(dep)) {
                assert dep.getCluster() != null;
                if (m.getCluster() != null) {
                    return new String[] {"ERR_platform_excluded_dep", m.getDisplayName(), m.getCluster(), dep.getDisplayName(), dep.getCluster()};
                } else {
                    return new String[] {"ERR_suite_excluded_dep", m.getDisplayName(), dep.getDisplayName(), dep.getCluster()};
                }
            }
            if (dep.getReleaseVersion() < mrvLo || dep.getReleaseVersion() > mrvHi) {
                if (m.getCluster() != null) {
                    return new String[] {"ERR_platform_bad_dep_mrv", m.getDisplayName(), m.getCluster(), dep.getDisplayName()};
                } else {
                    return new String[] {"ERR_suite_bad_dep_mrv", m.getDisplayName(), dep.getDisplayName()};
                }
            }
            if (d.getComparison() == Dependency.COMPARE_SPEC) {
                SpecificationVersion needed = new SpecificationVersion(d.getVersion());
                SpecificationVersion found = dep.getSpecificationVersion();
                if (found == null || found.compareTo(needed) < 0) {
                    if (m.getCluster() != null) {
                        return new String[] {"ERR_platform_bad_dep_spec", m.getDisplayName(), m.getCluster(), dep.getDisplayName()};
                    } else {
                        return new String[] {"ERR_suite_bad_dep_spec", m.getDisplayName(), dep.getDisplayName()};
                    }
                }
            } else if (d.getComparison() == Dependency.COMPARE_IMPL) {
                String needed = d.getVersion();
                if (!needed.equals("*") && !needed.equals(dep.getImplementationVersion())) { // NOI18N
                    assert m.getCluster() != null;
                    return new String[] {"ERR_platform_bad_dep_impl", m.getDisplayName(), m.getCluster(), dep.getDisplayName()};
                }
            }
        }
        // Now check token availability:
        Iterator toks = new TreeSet(m.getRequiredTokens()).iterator();
        while (toks.hasNext()) {
            String tok = (String) toks.next();
            UniverseModule wouldBeProvider = null;
            boolean found = false;
            Set<UniverseModule> possibleProviders = (Set) providers.get(tok);
            if (possibleProviders != null) {
                it = possibleProviders.iterator();
                while (it.hasNext()) {
                    UniverseModule p = (UniverseModule) it.next();
                    if (excluded.contains(p)) {
                        if (wouldBeProvider == null) {
                            wouldBeProvider = p;
                        }
                    } else {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                if (wouldBeProvider != null) {
                    assert wouldBeProvider.getCluster() != null;
                    if (m.getCluster() != null) {
                        return new String[] {"ERR_platform_only_excluded_providers", tok, m.getDisplayName(), m.getCluster(), wouldBeProvider.getDisplayName(), wouldBeProvider.getCluster()}; // NOI18N
                    } else {
                        return new String[] {"ERR_suite_only_excluded_providers", tok, m.getDisplayName(), wouldBeProvider.getDisplayName(), wouldBeProvider.getCluster()}; // NOI18N
                    }
                } else {
                    if (m.getCluster() != null) {
                        return new String[] {"ERR_platform_no_providers", tok, m.getDisplayName(), m.getCluster()}; // NOI18N
                    } else {
                        return new String[] {"ERR_suite_no_providers", tok, m.getDisplayName()}; // NOI18N
                    }
                }
            }
        }
        // All clear for this module.
        return null;
    }

    private void updateJavaPlatformEnabled() { // #71631
        boolean enabled = ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1;
        javaPlatformCombo.setEnabled(enabled);
        javaPlatformButton.setEnabled(enabled); // #72061
    }
    
}
