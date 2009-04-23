/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel;
import org.netbeans.modules.apisupport.project.ui.customizer.EditTestDependencyPanel;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup.Pair;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Tomas Musil
 */
final class UnitTestLibrariesNode extends AbstractNode {
    
    private final Action[] actions;
    
    public UnitTestLibrariesNode(String testType, final NbModuleProject project) {
        super(new LibrariesChildren(testType, project));
        setName(testType);
        setDisplayName(getMessage("LBL_" + testType + "_test_libraries"));
        actions = new Action[] {
            new AddUnitTestDependencyAction(testType, project)
        };
    }
    
    public Image getIcon(int type) {
        return getIcon(false);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }
    
    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/apisupport/project/ui/resources/libraries-badge.png", true);
        return ImageUtilities.mergeImages(UIUtil.getTreeFolderIcon(opened), badge, 8, 8);
    }
    
    public Action[] getActions(boolean context) {
        return actions;
    }
    
    private static String createHtmlDescription(final TestModuleDependency dep) {
        StringBuffer shortDesc = new StringBuffer("<html><u>" + dep.getModule().getCodeNameBase() + "</u><br>"); // NOI18N
        if (dep.isTest()) {
            shortDesc.append("<br>" + getMessage("CTL_test"));
        }
        if (dep.isCompile()) {
            shortDesc.append("<br>").append(getMessage("CTL_compile"));
        }
        if (dep.isRecursive()) {
            shortDesc.append("<br>").append(getMessage("CTL_recursive"));
        }
        shortDesc.append("</html>"); // NOI18N
        return shortDesc.toString();
    }
    
    private static String getMessage(String bundleKey) {
        return NbBundle.getMessage(UnitTestLibrariesNode.class, bundleKey);
    }
    
    
    private static final class LibrariesChildren extends Children.Keys<TestModuleDependency> implements AntProjectListener {
        
        private static final String LIBRARIES_ICON =
                "org/netbeans/modules/apisupport/project/ui/resources/libraries.gif"; // NOI18N
        
        static final Action REMOVE_DEPENDENCY_ACTION = new RemoveDependencyAction();
        
        private final String testType;
        private final NbModuleProject project;
        
        private ImageIcon librariesIcon;
        
        LibrariesChildren(String testType, final NbModuleProject project) {
            this.testType = testType;
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            project.getHelper().addAntProjectListener(this);
            refreshKeys();
        }
        
        protected void removeNotify() {
            setKeys(Collections.<TestModuleDependency>emptySet());
            project.getHelper().removeAntProjectListener(this);
            super.removeNotify();
        }
        
        private void refreshKeys() {
            try {
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Object>() {
                    public Object run() throws Exception {
                        ProjectXMLManager pxm = new ProjectXMLManager(project);
                        final List<TestModuleDependency> keys = new ArrayList<TestModuleDependency>();
                        SortedSet<TestModuleDependency> deps = new TreeSet<TestModuleDependency>(TestModuleDependency.CNB_COMPARATOR);
                        Set<TestModuleDependency> d =  pxm.getTestDependencies(
                                project.getModuleList()).get(testType);
                        //draw only compile time deps
                        if(d != null){
                            for (TestModuleDependency tmd : d) {
                                if(tmd.isCompile()) {
                                    deps.add(tmd);
                                }
                            }
                            keys.addAll(deps);
                        }
                        ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {
                            public void run() {
                                setKeys(Collections.unmodifiableList(keys));
                            }
                        });
                        return null;
                    }
                });
            } catch (MutexException e) {
                assert false : e.getException();
            }
        }
        
        protected Node[] createNodes(TestModuleDependency dep) {
            Node node = null;
            File srcF = dep.getModule().getSourceLocation();
            if (srcF == null) {
                File jarF = dep.getModule().getJarLocation();
                URL jarRootURL = FileUtil.urlForArchiveOrDir(jarF);
                assert jarRootURL != null;
                FileObject root = URLMapper.findFileObject(jarRootURL);
                ModuleEntry me = dep.getModule();
                String name = me.getLocalizedName() + " - " + me.getCodeNameBase(); // NOI18N
                Icon icon = getLibrariesIcon();
                Node pvNode = ActionFilterNode.create(
                        PackageView.createPackageView(new LibrariesSourceGroup(root, name, icon, icon)));
                node = new LibraryDependencyNode(dep, testType, project, pvNode);
                node.setName(me.getLocalizedName());
            } else {
                node = new ProjectDependencyNode(dep, testType, project);
                node.setName(dep.getModule().getLocalizedName());
            }
            assert node != null;
            return new Node[] { node };
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            // XXX this is a little strange but happens during project move. Bad ordering.
            // Probably bug in moving implementation (our or in general Project API).
            if (! project.isRunInAtomicAction() && project.getHelper().resolveFileObject(AntProjectHelper.PROJECT_XML_PATH) != null) {
                refreshKeys();
            }
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // do not need
        }
        
        
        private Icon getLibrariesIcon() {
            if (librariesIcon == null) {
                librariesIcon = ImageUtilities.loadImageIcon(LIBRARIES_ICON, true);
            }
            return librariesIcon;
        }
        
    }
    
    private static final class ProjectDependencyNode extends AbstractNode {
        
        private final TestModuleDependency dep;
        private final String testType;
        private final NbModuleProject project;
        private Action[] actions;
        
        ProjectDependencyNode(final TestModuleDependency dep, String testType, final NbModuleProject project) {
            super(Children.LEAF, Lookups.fixed(new Object[] { dep, project, dep.getModule(), testType}));
            this.dep = dep;
            this.testType = testType;
            this.project = project;
            ModuleEntry me = dep.getModule();
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            setDisplayName(me.getLocalizedName());
            setShortDescription(UnitTestLibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            
            if (actions == null) {
                Set<Action> result = new LinkedHashSet<Action>();
                // Open project action
                result.add(SystemAction.get(LibrariesNode.OpenProjectAction.class));
                // Edit dependency action
                result.add(new EditTestDependencyAction(dep, testType, project));
                // Remove dependency
                result.add(LibrariesChildren.REMOVE_DEPENDENCY_ACTION);
                actions = result.toArray(new Action[result.size()]);
            }
            return actions;
        }
        
        public Action getPreferredAction() {
            return getActions(false)[0]; // open
        }
        
    }
    
    private static final class LibraryDependencyNode extends FilterNode {
        
        private final TestModuleDependency dep;
        private final String testType;
        private final NbModuleProject project;
        private Action[] actions;
        
        LibraryDependencyNode(final TestModuleDependency dep,
                String testType,
                final NbModuleProject project, final Node original) {
            super(original, null, new ProxyLookup(new Lookup[] {
                original.getLookup(),
                Lookups.fixed(new Object[] { dep, project, testType })
            }));
            this.dep = dep;
            this.testType = testType;
            this.project = project;
            setShortDescription(UnitTestLibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            if (actions == null) {
                Set<Action> result = new LinkedHashSet<Action>();
                result.add(new EditTestDependencyAction(dep, testType, project));
                Action[] superActions = super.getActions(false);
                for (int i = 0; i < superActions.length; i++) {
                    if (superActions[i] instanceof FindAction) {
                        result.add(superActions[i]);
                    }
                }
                result.add(LibrariesChildren.REMOVE_DEPENDENCY_ACTION);
                actions = result.toArray(new Action[result.size()]);
            }
            return actions;
        }
        
        public Action getPreferredAction() {
            return getActions(false)[0];
        }
        
    }
    
    static final class AddUnitTestDependencyAction extends AbstractAction {
        
        private final String testType;
        private final NbModuleProject project;
        
        AddUnitTestDependencyAction(String testType, final NbModuleProject project) {
            super(getMessage("CTL_AddTestDependency_" + testType));
            this.testType = testType;
            this.project = project;
        }
        
        //COPIED FROM LIBRARIES MOSTLY
        public void actionPerformed(ActionEvent ev) {
            SingleModuleProperties props = SingleModuleProperties.getInstance(project);
            final AddModulePanel addPanel = new AddModulePanel(props);
            final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                    getMessage("CTL_AddTestDependency_" + testType));
            descriptor.setHelpCtx(new HelpCtx(AddModulePanel.class));
            descriptor.setClosingOptions(new Object[0]);
            final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
            descriptor.setButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (DialogDescriptor.OK_OPTION.equals(e.getSource()) &&
                            addPanel.getSelectedDependencies().length == 0) {
                        return;
                    }
                    d.setVisible(false);
                    d.dispose();
                }
            });
            d.setVisible(true);
            if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                // dialog returns
                ModuleDependency[] newDeps = addPanel.getSelectedDependencies();
                ProjectXMLManager pxm = new ProjectXMLManager(project);
                try {
                    for (int i = 0; i < newDeps.length; i++) {
                        // by default, add compile-time dependency
                        pxm.addTestDependency(testType
                                ,new TestModuleDependency(newDeps[i].getModuleEntry(), false, false, true));
                        ProjectManager.getDefault().saveProject(project);
                    }
                } catch (Exception e) {
                    //IOEX
                    ErrorManager.getDefault().annotate(e, "Cannot add dependencies, probably IO error: " + Arrays.asList(newDeps)); // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
            }
            d.dispose();
        }
        
        
        
    }

    private static final class Pair<F, S> {
        public F first;
        public S second;
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
    
    static final class RemoveDependencyAction extends CookieAction {
        protected void performAction(Node[] activatedNodes) {
            Map<NbModuleProject, Set<Pair<TestModuleDependency, String>>> map =
                    new HashMap<NbModuleProject, Set<Pair<TestModuleDependency, String>>>();
            for (int i = 0; i < activatedNodes.length; i++) {
                Lookup lkp = activatedNodes[i].getLookup();
                TestModuleDependency dep = lkp.lookup(TestModuleDependency.class);
                assert dep != null;
                NbModuleProject project = lkp.lookup(NbModuleProject.class);
                assert project != null;
                String testType = lkp.lookup(String.class);
                assert testType != null;
                Set<Pair<TestModuleDependency, String>> deps = map.get(project);
                if (deps == null) {
                    deps = new HashSet<Pair<TestModuleDependency, String>>();
                    map.put(project, deps);
                }
                deps.add(new Pair<TestModuleDependency, String>(dep, testType));
            }
            for (Map.Entry<NbModuleProject,Set<Pair<TestModuleDependency, String>>> me : map.entrySet()) {
                NbModuleProject project = me.getKey();
                ProjectXMLManager pxm = new ProjectXMLManager(project);
                //remove dep one by one
                for (Pair<TestModuleDependency, String> pair : me.getValue()) {
                    pxm.removeTestDependency(pair.second, pair.first.getModule().getCodeNameBase());
                }
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    ErrorManager.getDefault().annotate(e, "Problem during test dependencies removing"); // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
            }
            
        }
        
        public String getName() {
            return getMessage("CTL_RemoveDependency");
        }
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        protected boolean asynchronous() {
            return false;
        }
        
        protected int mode() {
            return CookieAction.MODE_ALL;
        }
        
        protected Class[] cookieClasses() {
            return new Class[] {TestModuleDependency.class, NbModuleProject.class };
        }
        
        
    }
    
    
    static final class EditTestDependencyAction extends AbstractAction {
        
        private final TestModuleDependency testDep;
        private final String testType;
        private final NbModuleProject project;
        
        EditTestDependencyAction(final TestModuleDependency testDep, String testType, final NbModuleProject project) {
            super(getMessage("CTL_EditDependency"));
            this.testDep = testDep;
            this.testType = testType;
            this.project = project;
        }
        
        
        public void actionPerformed(ActionEvent ev) {
            final EditTestDependencyPanel editTestPanel = new EditTestDependencyPanel(testDep);
            DialogDescriptor descriptor = new DialogDescriptor(editTestPanel,
                    NbBundle.getMessage(LibrariesNode.class, "CTL_EditModuleDependencyTitle",
                    testDep.getModule().getLocalizedName()));
            descriptor.setHelpCtx(new HelpCtx(EditTestDependencyPanel.class));
            Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
            d.setVisible(true);
            if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                TestModuleDependency editedDep = editTestPanel.getEditedDependency();
                try {
                    ProjectXMLManager pxm = new ProjectXMLManager(project);
                    pxm.removeTestDependency(testType, testDep.getModule().getCodeNameBase());
                    pxm.addTestDependency(testType, editedDep);
                    ProjectManager.getDefault().saveProject(project);
                    
                    
                } catch (IOException e) {
                    ErrorManager.getDefault().annotate(e, "Cannot store dependency: " + editedDep); // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
                
                
            }
            d.dispose();
        }
    }
    
}

