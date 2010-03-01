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
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
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

    public DiscoveryWizardAction(){
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected void performAction(Node[] activatedNodes) {
        Collection<Project> projects = getMakeProjects(activatedNodes);
        if( projects == null || projects.size() != 1) {
            return;
        }
        invokeWizard(projects.iterator().next());
    }
    
    protected boolean enable(Node[] activatedNodes) {
        Collection<Project> projects = getMakeProjects(activatedNodes);
        if( projects == null || projects.size() != 1) {
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
    
    /*package-local*/ static String findBuildResult(Project project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null || !pdp.gotDescriptor()){
            return null;
        }
        MakeConfigurationDescriptor make = pdp.getConfigurationDescriptor();
        MakeConfiguration conf = make.getActiveConfiguration();
        if (conf != null){
            String output = conf.getMakefileConfiguration().getOutput().getValue();
            if (output == null || output.length()==0){
                return null;
            }
            if (new File(output).isAbsolute()) {
                return output;
            }
            String base = getProjectDirectoryPath(project);
            output = CndFileUtils.normalizeFile(new File(base+'/'+output)).getAbsolutePath();
            return output;
        }
        return null;
    }
    
    
    /*package-local*/ static String getProjectDirectoryPath(Project project) {
        String base = project.getProjectDirectory().getPath();
        if (Utilities.isWindows()){
            base = base.replace('\\', '/');
        } else {
            base = File.separator+project.getProjectDirectory().getPath();
	}
	return base;
    }
    
    /*package-local*/ static String findSourceRoot(Project project) {
        String base = getProjectDirectoryPath(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null && pdp.gotDescriptor()){
            MakeConfigurationDescriptor make = pdp.getConfigurationDescriptor();
            Folder folder = make.getLogicalFolders();
            List<Folder> sources = folder.getFolders();
            List<String> roots = make.getAbsoluteSourceRoots();
            if (roots.size() > 0){
                return roots.get(0);
            }
            //List<String> roots = new ArrayList<String>();
            for (Object o : sources){
                Folder sub = (Folder)o;
                if (sub.isProjectFiles()) {
                    if (MakeConfigurationDescriptor.SOURCE_FILES_FOLDER.equals(sub.getName())) {
                        List<Folder> v = sub.getFolders();
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
                    String path =items[0].getAbsPath();
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
                                return CndFileUtils.normalizeFile(new File(newBase.toString())).getAbsolutePath();
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
            if( pdp == null || !pdp.gotDescriptor()) {
                return null;
            }
            MakeConfigurationDescriptor make = pdp.getConfigurationDescriptor();
            if( make == null ) {
                return null;
            }
            MakeConfiguration conf = make.getActiveConfiguration();
            if (conf != null && conf.isMakefileConfiguration()){
                projects.add(project);
            } else {
                return null;
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
    
    /*package-local*/ static void setupComponent(final String[] steps, final String[] advanced, final int i, final Component c) {
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent) c;
            // Sets step number of a component
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
            // Sets steps names for a panel
            if (i == 0) {
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, advanced); // NOI18N
            } else {
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
            // Turn on subtitle creation on each step
            jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
            // Show steps on the left side with the image on the background
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
            // Turn on numbering of all steps
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        }
    }
    
    @Override
    public String getName() {
        return getString("ACTION_TITLE_TXT");
    }
    
    @Override
    public String iconResource() {
        return null;
    }
    
    @Override
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

