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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Dialog;
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
import java.util.Iterator;
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
import org.netbeans.modules.apisupport.project.Util;
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
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Tomas Musil
 */
final class UnitTestLibrariesNode extends AbstractNode {
    
    static final String UNIT_TEST_LIBRARIES_NAME = "unit libraries"; // NOI18N
    
    private static final String DISPLAY_NAME = getMessage("LBL_unit_test_libraries");
    
    private final Action[] actions;
    
    public UnitTestLibrariesNode(final NbModuleProject project) {
        super(new LibrariesChildren(project));
        setName(UNIT_TEST_LIBRARIES_NAME);
        setDisplayName(DISPLAY_NAME);
        actions = new Action[] {
            new AddUnitTestDependencyAction(project)
        };
    }
    
    public Image getIcon(int type) {
        return getIcon(false);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }
    
    private Image getIcon(boolean opened) {
        Image badge = Utilities.loadImage("org/netbeans/modules/apisupport/project/ui/resources/libraries-badge.png", true);
        return Utilities.mergeImages(UIUtil.getTreeFolderIcon(opened), badge, 8, 8);
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
    
    
    private static final class LibrariesChildren extends Children.Keys<Object> implements AntProjectListener {
        
        private static final String JUNIT = "junit"; //NOI18N
        
        private static final String JUNIT_CNB = "org.netbeans.modules.junit";
        
        private static final String NBJUNIT = "nbjunit"; //NOI18N
        
        private static final String NBJUNIT_CNB = "org.netbeans.modules.nbjunit";
        
        private static final String LIBRARIES_ICON =
                "org/netbeans/modules/apisupport/project/ui/resources/libraries.gif"; // NOI18N
        
        static final Action REMOVE_DEPENDENCY_ACTION = new RemoveDependencyAction();
        
        private final NbModuleProject project;
        
        private ImageIcon librariesIcon;
        
        LibrariesChildren(final NbModuleProject project) {
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            project.getHelper().addAntProjectListener(this);
            refreshKeys();
        }
        
        protected void removeNotify() {
            setKeys(Collections.emptySet());
            project.getHelper().removeAntProjectListener(this);
            super.removeNotify();
        }
        
        private void refreshKeys() {
            try {
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Object>() {
                    public Object run() throws Exception {
                        ProjectXMLManager pxm = new ProjectXMLManager(project);
                        List<Object> keys = new ArrayList<Object>();
                        if(isModuleInModuleList(JUNIT_CNB)) {
                            keys.add(JUNIT);
                        }
                        if(isModuleInModuleList(NBJUNIT_CNB)) {
                            keys.add(NBJUNIT);
                        }
                        SortedSet<TestModuleDependency> deps = new TreeSet<TestModuleDependency>(TestModuleDependency.CNB_COMPARATOR);
                        Set<TestModuleDependency> d =  pxm.getTestDependencies(
                                project.getModuleList()).get(TestModuleDependency.UNIT);
                        //draw only compile time deps
                        if(d != null){
                            for (TestModuleDependency tmd : d) {
                                if(tmd.isCompile()) {
                                    deps.add(tmd);
                                };
                            }
                            keys.addAll(deps);
                        }
                        setKeys(Collections.unmodifiableList(keys));
                        return null;
                    }
                });
            } catch (MutexException e) {
                assert false : e.getException();
            }
        }
        
        private boolean isModuleInModuleList(String cnb){
            ModuleEntry me = null;
            boolean result = false;
            try {
                me = project.getModuleList().getEntry(cnb);
                if(me != null) {
                    File moduleJar = me.getJarLocation();
                    result = moduleJar.exists();
                    
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            };
            return result;
        }
        
        
        protected Node[] createNodes(Object key) {
            Node node = null;
            //special nodes - junit, nbjunit
            if (JUNIT.equals(key) || NBJUNIT.equals(key)) {
                String cnb = null;
                if (JUNIT.equals(key)) {
                    cnb = JUNIT_CNB;
                } else {
                    cnb = NBJUNIT_CNB;
                }
                try {
                    ModuleEntry me = project.getModuleList().getEntry(cnb);
                    Icon icon = getLibrariesIcon(); // TODO a better icon for JUNIT
                    File junitJar = me.getJarLocation();
                    URL junitURL = Util.urlForJar(junitJar);
                    assert junitURL != null;
                    FileObject junitFO = URLMapper.findFileObject(junitURL);
                    String name = me.getLocalizedName();
                    node = ActionFilterNode.create(
                            PackageView.createPackageView(new LibrariesSourceGroup(junitFO, name, icon, icon)));
                    node.setName(name); //node does not have a name by default
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                TestModuleDependency dep = (TestModuleDependency) key;
                File srcF = dep.getModule().getSourceLocation();
                if (srcF == null) {
                    File jarF = dep.getModule().getJarLocation();
                    URL jarRootURL = Util.urlForJar(jarF);
                    assert jarRootURL != null;
                    FileObject root = URLMapper.findFileObject(jarRootURL);
                    ModuleEntry me = dep.getModule();
                    String name = me.getLocalizedName() + " - " + me.getCodeNameBase(); // NOI18N
                    Icon icon = getLibrariesIcon();
                    Node pvNode = ActionFilterNode.create(
                            PackageView.createPackageView(new LibrariesSourceGroup(root, name, icon, icon)));
                    node = new LibraryDependencyNode(dep, project, pvNode);
                    node.setName(me.getLocalizedName());
                } else {
                    node = new ProjectDependencyNode(dep, project);
                    node.setName(dep.getModule().getLocalizedName());
                }
                
            }
            
            assert node != null;
            return new Node[] { node };
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            // XXX this is a little strange but happens during project move. Bad ordering.
            // Probably bug in moving implementation (our or in general Project API).
            if (project.getHelper().resolveFileObject(AntProjectHelper.PROJECT_XML_PATH) != null) {
                refreshKeys();
            }
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // do not need
        }
        
        
        private Icon getLibrariesIcon() {
            if (librariesIcon == null) {
                librariesIcon = new ImageIcon(Utilities.loadImage(LIBRARIES_ICON, true));
            }
            return librariesIcon;
        }
        
    }
    
    private static final class ProjectDependencyNode extends AbstractNode {
        
        private final TestModuleDependency dep;
        private final NbModuleProject project;
        private Action[] actions;
        
        ProjectDependencyNode(final TestModuleDependency dep, final NbModuleProject project) {
            super(Children.LEAF, Lookups.fixed(new Object[] { dep, project, dep.getModule()}));
            this.dep = dep;
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
                result.add(new EditTestDependencyAction(dep, project));
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
        private final NbModuleProject project;
        private Action[] actions;
        
        LibraryDependencyNode(final TestModuleDependency dep,
                final NbModuleProject project, final Node original) {
            super(original, null, new ProxyLookup(new Lookup[] {
                original.getLookup(),
                Lookups.fixed(new Object[] { dep, project })
            }));
            this.dep = dep;
            this.project = project;
            setShortDescription(UnitTestLibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            if (actions == null) {
                Set<Action> result = new LinkedHashSet<Action>();
                result.add(new EditTestDependencyAction(dep, project));
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
        
        private final NbModuleProject project;
        
        AddUnitTestDependencyAction(final NbModuleProject project) {
            super(getMessage("CTL_AddTestDependency"));
            this.project = project;
        }
        
        //COPIED FROM LIBRARIES MOSTLY
        public void actionPerformed(ActionEvent ev) {
            SingleModuleProperties props = SingleModuleProperties.getInstance(project);
            final AddModulePanel addPanel = new AddModulePanel(props);
            final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                    getMessage("CTL_AddTestDependency"));
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
                        pxm.addTestDependency(TestModuleDependency.UNIT
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
    
    
    static final class RemoveDependencyAction extends CookieAction {
        
        protected void performAction(Node[] activatedNodes) {
            Map<NbModuleProject, Set<TestModuleDependency>> map = new HashMap<NbModuleProject, Set<TestModuleDependency>>();
            for (int i = 0; i < activatedNodes.length; i++) {
                TestModuleDependency dep = activatedNodes[i].getLookup().lookup(TestModuleDependency.class);
                assert dep != null;
                NbModuleProject project = activatedNodes[i].getLookup().lookup(NbModuleProject.class);
                assert project != null;
                Set<TestModuleDependency> deps = map.get(project);
                if (deps == null) {
                    deps = new HashSet<TestModuleDependency>();
                    map.put(project, deps);
                }
                deps.add(dep);
            }
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry me = (Map.Entry) it.next();
                NbModuleProject project = (NbModuleProject) me.getKey();
                Set deps = (Set) me.getValue();
                ProjectXMLManager pxm = new ProjectXMLManager(project);
                //remove dep one by one
                for (Iterator it2 = deps.iterator(); it2.hasNext();) {
                    TestModuleDependency rem = (TestModuleDependency) it2.next();
                    pxm.removeTestDependency(TestModuleDependency.UNIT, rem.getModule().getCodeNameBase());
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
        private final NbModuleProject project;
        
        EditTestDependencyAction(final TestModuleDependency testDep, final NbModuleProject project) {
            super(getMessage("CTL_EditDependency"));
            this.testDep = testDep;
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
                    final String UNIT = TestModuleDependency.UNIT;
                    pxm.removeTestDependency(UNIT, testDep.getModule().getCodeNameBase());
                    pxm.addTestDependency(UNIT, editedDep);
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

