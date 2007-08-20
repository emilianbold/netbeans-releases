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

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Alexander Simon
 */
public final class DiscoveryWizardAction extends NodeAction {

    public static final String HELP_CONTEXT_SELECT_MODE = "CodeAssistanceWizardP1"; // NOI18N
    public static final String HELP_CONTEXT_SIMPLE_CONFIGURATION = "CodeAssistanceWizardP6"; // NOI18N
    public static final String HELP_CONTEXT_SELECT_PROVIDER = "CodeAssistanceWizardP2"; // NOI18N
    public static final String HELP_CONTEXT_SELECT_OBJECT_FILES = "CodeAssistanceWizardP3"; // NOI18N
    public static final String HELP_CONTEXT_CONSOLIDATION_STRATEGY = "CodeAssistanceWizardP4"; // NOI18N
    public static final String HELP_CONTEXT_SELECT_CONFIGURATION = "CodeAssistanceWizardP5"; // NOI18N

    protected void performAction(Node[] activatedNodes) {
        Collection<Project> projects = getMakeProjects(activatedNodes);
        if( projects == null || projects.size() == 0) {
            return;
        }
        invokeWizard(projects.iterator().next());
    }
    
    protected boolean enable(Node[] activatedNodes) {
        Collection<Project> projects = getMakeProjects(activatedNodes);
        if( projects == null || projects.size() == 0) {
            return false;
        }
        return true;
    }
    
    private void invokeWizard(Project project) {
        DiscoveryWizardDescriptor wizardDescriptor = new DiscoveryWizardDescriptor(getPanels());
        wizardDescriptor.setProject(project);
        wizardDescriptor.setRootFolder(findSourceRoot(project));
        wizardDescriptor.setBuildResult(findBuildResult(project));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(getString("WIZARD_TITLE_TXT")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
        }
        dialog.dispose();
    }
    
    private String findBuildResult(Project project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp==null){
            return null;
        }
        MakeConfigurationDescriptor make = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        Configuration conf = make.getConfs().getActive();
        if (conf instanceof MakeConfiguration){
            String output = ((MakeConfiguration)conf).getMakefileConfiguration().getOutput().getValue();
            if (output == null || output.length()==0){
                return null;
            }
            if (new File(output).isAbsolute()) {
                return output;
            }
            String base = getProjectDirectoryPath(project);
            output = FileUtil.normalizeFile(new File(base+'/'+output)).getAbsolutePath();
            return output;
        }
        return null;
    }
    
    
    private String getProjectDirectoryPath(Project project) {
        String base = project.getProjectDirectory().getPath();
        if (Utilities.isWindows()){
            base = base.replace('\\', '/');
        } else {
            base = File.separator+project.getProjectDirectory().getPath();
	}
	return base;
    }
    
    private String findSourceRoot(Project project) {
        String base = getProjectDirectoryPath(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp!=null){
            MakeConfigurationDescriptor make = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
            Folder folder = make.getLogicalFolders();
            Vector sources = folder.getFolders();
            List<String> roots = new ArrayList<String>();
            for (Object o : sources){
                Folder sub = (Folder)o;
                if (sub.isProjectFiles()) {
                    if (MakeConfigurationDescriptor.SOURCE_FILES_FOLDER.equals(sub.getName())) {
                        Vector v = sub.getFolders();
                        for (Object e : v){
                            Folder s = (Folder)e;
                            if (s.isProjectFiles()) {
                                roots.add(s.getName());
                            }
                        }
                    } else if (MakeConfigurationDescriptor.HEADER_FILES_FOLDER.equals(sub.getName()) ||
                            MakeConfigurationDescriptor.RESOURCE_FILES_FOLDER.equals(sub.getName())){
                        // skip
                    } else {
                        roots.add(sub.getName());
                    }
                }
            }
            if (roots.size()>0){
                String rootName = roots.get(0);
                Item[] items = make.getProjectItems();
                if (items.length>0){
                    String path =items[0].getPath();
                    StringBuilder newBase = null;
                    if (path.startsWith("..")){ // NOI18N
                        newBase = new StringBuilder(base);
                    } else {
                        newBase = new StringBuilder();
                    }
                    StringTokenizer st = new StringTokenizer(path, "/\\"); // NOI18N
                    while(st.hasMoreTokens()){
                        String segment = st.nextToken();
                        newBase.append(File.separator);
                        newBase.append(segment);
                        if (rootName.equals(segment) && st.hasMoreTokens()) {
                            //try {
                                return FileUtil.normalizeFile(new File(newBase.toString())).getAbsolutePath();
                            //} catch (IOException ex) {
                            //    ex.printStackTrace();
                            //}
                        }
                    }
                }
            }
        }
        return base;
    }
    
    /**
     * Gets the collection of native projects that correspond the given nodes.
     * @return in the case all nodes correspond to native projects -
     * collection of native projects; otherwise null
     */
    private Collection<Project> getMakeProjects(Node[] nodes) {
        Collection<Project> projects = new ArrayList<Project>();
        for (int i = 0; i < nodes.length; i++) {
            Project project = nodes[i].getLookup().lookup(Project.class);
            if(project == null) {
                return null;
            }
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if( pdp == null ) {
                return null;
            }
            MakeConfigurationDescriptor make = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
            if( make == null ) {
                return null;
            }
            Configurations confs = make.getConfs();
            if (confs == null) {
                return null;
            }
            Configuration conf = confs.getActive();
            if (conf instanceof MakeConfiguration){
                if (((MakeConfiguration)conf).isMakefileConfiguration()){
                    projects.add(project);
                }
            }
        }
        return projects;
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private InstantiatingIterator getPanels() {
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {
            new SelectModeWizard()
            ,new SelectProviderWizard()
            ,new SelectObjectFilesWizard()
            ,new ConsolidationStrategyWizard()
            ,new SelectConfigurationWizard()
        };
        WizardDescriptor.Panel[] simplepanels = new WizardDescriptor.Panel[] {
            panels[0]
            ,new SimpleConfigurationWizard()
        };
        String[] steps = new String[panels.length];
        String[] simple = new String[simplepanels.length];
        String[] advanced = new String[2];
        advanced[0] = panels[0].getComponent().getName();
        advanced[1] = "..."; // NOI18N
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            steps[i] = c.getName();
            setupComponent(steps, advanced, i, c);
            if (i < simple.length){
                c = simplepanels[i].getComponent();
                simple[i] = c.getName();
                if (i > 0 && i < simple.length){
                    setupComponent(simple, null, i, c);
                }
            }
        }
        
        return new DiscoveryWizardIterator(panels,simplepanels);
    }
    
    private void setupComponent(final String[] steps, final String[] advanced, final int i, final Component c) {
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent) c;
            // Sets step number of a component
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            // Sets steps names for a panel
            if (i == 0) {
                jc.putClientProperty("WizardPanel_contentData", advanced); // NOI18N
            } else {
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
            // Turn on subtitle creation on each step
            jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
            // Show steps on the left side with the image on the background
            jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
            // Turn on numbering of all steps
            jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
        }
    }
    
    public String getName() {
        return getString("ACTION_TITLE_TXT");
    }
    
    @Override
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(DiscoveryWizardAction.class).getString(key);
    }
    
}

