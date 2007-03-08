/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
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
import org.openide.WizardDescriptor.Panel;
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
    
    private String findSourceRoot(Project project) {
        String base = null;
        if (Utilities.isWindows()){
            base = project.getProjectDirectory().getPath();
            base = base.replace('\\', '/');
        } else {
            base = File.separator+project.getProjectDirectory().getPath();
        }
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
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
                    StringBuffer newBase = null;
                    if (path.startsWith("..")){ // NOI18N
                        newBase = new StringBuffer(base);
                    } else {
                        newBase = new StringBuffer();
                    }
                    StringTokenizer st = new StringTokenizer(path, "/\\"); // NOI18N
                    while(st.hasMoreTokens()){
                        String segment = st.nextToken();
                        newBase.append(File.separator);
                        newBase.append(segment);
                        if (rootName.equals(segment) && st.hasMoreTokens()) {
                            try {
                                return new File(newBase.toString()).getCanonicalPath();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
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
            Object o = nodes[i].getValue("Project"); // NOI18N
            if( ! (o instanceof  Project) ) {
                return null;
            }
            Project project = (Project)o;
            ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
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
            new SelectProviderWizard(false)
            ,new SelectObjectFilesWizard(false)
            ,new ConsolidationStrategyWizard(false)
            ,new SelectConfigurationWizard(false)
        };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
            }
        }
        
        return new DiscoveryWizardIterator(panels);
    }
    
    public String getName() {
        return getString("ACTION_TITLE_TXT");
    }
    
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(DiscoveryWizardAction.class).getString(key);
    }
    
    
    public static class DiscoveryWizardIterator implements InstantiatingIterator {
        private DiscoveryWizardDescriptor wizard;
        private Panel[] panels ;
        private int index = 0;
        /** Creates a new instance of DiscoveryWizardIterator */
        public DiscoveryWizardIterator(Panel[] panels ) {
            this.panels = panels;
        }
        
        public Set instantiate() throws IOException {
            DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(wizard);
            return generator.makeProject();
        }
        
        public void initialize(WizardDescriptor wizard) {
            this.wizard = (DiscoveryWizardDescriptor)wizard;
        }
        
        public void uninitialize(WizardDescriptor wizard) {
            DiscoveryWizardDescriptor wiz = (DiscoveryWizardDescriptor)wizard;
            wiz.clean();
            wizard = null;
            panels = null;
        }

        public WizardDescriptor.Panel current() {
            return panels[index];
        }

        public String name() {
            return null;
        }

        public boolean hasNext() {
            return index < (panels.length - 1);
        }

        public boolean hasPrevious() {
            return index > 0;
        }

        public synchronized void nextPanel() {
            if ((index + 1) == panels.length) {
                throw new java.util.NoSuchElementException();
            }
            index++;
        }

        public synchronized void previousPanel() {
            if (index == 0) {
                throw new java.util.NoSuchElementException();
            }
            index--;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }
}

