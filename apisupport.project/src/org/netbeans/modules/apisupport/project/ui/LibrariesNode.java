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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.ModulesNodeFactory.AddNewLibraryWrapperAction;
import org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel;
import org.netbeans.modules.apisupport.project.ui.customizer.EditDependencyPanel;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.actions.FindAction;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Martin Krauskopf
 */
final class LibrariesNode extends AbstractNode {
    
    static final String LIBRARIES_NAME = "libraries"; // NOI18N
    
    private static final String DISPLAY_NAME = getMessage("LBL_libraries");
    
    /** Package private for unit tests only. */
    static final RequestProcessor RP = new RequestProcessor();
    
    private final Action[] actions;
    
    public LibrariesNode(final NbModuleProject project) {
        super(new LibrariesChildren(project));
        setName(LIBRARIES_NAME);
        setDisplayName(DISPLAY_NAME);
        if (Util.getModuleType(project) == NbModuleProvider.SUITE_COMPONENT) {
            actions = new Action[] {
                new AddModuleDependencyAction(project),
                new AddNewLibraryWrapperAction(project, project)
            };
        } else {
            actions = new Action[] {
                new AddModuleDependencyAction(project),
            };
        }
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
    
    private static String createHtmlDescription(final ModuleDependency dep) {
        // assemble an html short description (tooltip actually)
        StringBuffer shortDesc = new StringBuffer("<html><u>" + dep.getModuleEntry().getCodeNameBase() + "</u><br>"); // NOI18N
        if (dep.hasImplementationDepedendency()) {
            shortDesc.append("<br><font color=\"red\">" + getMessage("CTL_ImplementationDependency") + "</font>");
        }
        if (dep.hasCompileDependency()) {
            shortDesc.append("<br>").append(getMessage("CTL_NeededToCompile"));
        }
        if (dep.getReleaseVersion() != null) {
            shortDesc.append("<br>").append(NbBundle.getMessage(LibrariesNode.class, "CTL_MajorReleaseVersion",
                    dep.getReleaseVersion()));
        }
        if (dep.getSpecificationVersion() != null) {
            shortDesc.append("<br>").append(NbBundle.getMessage(LibrariesNode.class, "CTL_SpecificationVersion",
                    dep.getSpecificationVersion()));
        }
        shortDesc.append("</html>"); // NOI18N
        return shortDesc.toString();
    }
    
    private static String getMessage(String bundleKey) {
        return NbBundle.getMessage(LibrariesNode.class, bundleKey);
    }
    
    private static final class LibrariesChildren extends Children.Keys<Object/*JDK_PLATFORM_NAME|ModuleDependency*/> implements AntProjectListener {
        
        private static final String JDK_PLATFORM_NAME = "jdkPlatform"; // NOI18N
        
        private static final String LIBRARIES_ICON =
                "org/netbeans/modules/apisupport/project/ui/resources/libraries.gif"; // NOI18N
        
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
            // Since miscellaneous operations may be run upon the project.xml
            // of individual projects we could be called from the same thread
            // with already acquired ProjectManager.mutex. This could lead to
            // refreshing during the misconfigurated suite/suite_component
            // relationship.
            RP.post(new Runnable() {
                public void run() {
                    try {
                        ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                            public Void run() throws IOException {
                                ProjectXMLManager pxm = new ProjectXMLManager(project);
                                final List<Object> keys = new ArrayList<Object>();
                                keys.add(JDK_PLATFORM_NAME);
                                SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
                                deps.addAll(pxm.getDirectDependencies());
                                keys.addAll(deps);
                                // XXX still not good when dependency was just edited, since Children use
                                // hashCode/equals (probably HashMap) to find lastly selected node (so neither
                                // SortedSet would help here). Use probably wrapper instead to keep selection.
                                RP.post(new Runnable() {
                                    public void run() {
                                        setKeys(Collections.unmodifiableList(keys));
                                    }
                                });
                                return null;
                            }
                        });
                    } catch (MutexException e) {
                        Logger.getLogger(LibrariesNode.class.getName()).log(Level.FINE, null, e);
                    }
                }
            });
        }
        
        protected Node[] createNodes(Object key) {
            Node node;
            if (key == JDK_PLATFORM_NAME) {
                node = PlatformNode.create(project.evaluator(), "nbjdk.home"); // NOI18N
            } else {
                ModuleDependency dep = (ModuleDependency) key;
                File srcF = dep.getModuleEntry().getSourceLocation();
                if (srcF == null) {
                    File jarF = dep.getModuleEntry().getJarLocation();
                    URL jarRootURL = Util.urlForJar(jarF);
                    assert jarRootURL != null;
                    FileObject root = URLMapper.findFileObject(jarRootURL);
                    ModuleEntry me = dep.getModuleEntry();
                    String name = me.getLocalizedName() + " - " + me.getCodeNameBase(); // NOI18N
                    Icon icon = getLibrariesIcon();
                    Node pvNode = ActionFilterNode.create(
                            PackageView.createPackageView(new LibrariesSourceGroup(root, name, icon, icon)));
                    node = new LibraryDependencyNode(dep, project, pvNode);
                } else {
                    node = new ProjectDependencyNode(dep, project);
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
        
        /*
        private Node getNodeDelegate(final File jarF) {
            Node n = null;
            assert jarF != null;
            FileObject jar = FileUtil.toFileObject(jarF);
            if (jarF != null) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(jar);
                    if (dobj != null) {
                        n = dobj.getNodeDelegate();
                    }
                } catch (DataObjectNotFoundException e) {
                    assert false : e;
                }
            }
            return n;
        }
         */
        
        private Icon getLibrariesIcon() {
            if (librariesIcon == null) {
                librariesIcon = new ImageIcon(Utilities.loadImage(LIBRARIES_ICON, true));
            }
            return librariesIcon;
        }
        
    }
    
    private static final class ProjectDependencyNode extends AbstractNode {
        
        private final ModuleDependency dep;
        private final NbModuleProject project;
        
        ProjectDependencyNode(final ModuleDependency dep, final NbModuleProject project) {
            super(Children.LEAF, Lookups.fixed(dep, project, dep.getModuleEntry()));
            this.dep = dep;
            this.project = project;
            ModuleEntry me = dep.getModuleEntry();
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            setDisplayName(me.getLocalizedName());
            setShortDescription(LibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                SystemAction.get(OpenProjectAction.class),
                new EditDependencyAction(dep, project),
                new ShowJavadocAction(dep, project),
                SystemAction.get(RemoveAction.class),
            };
        }
        
        public Action getPreferredAction() {
            return getActions(false)[0]; // open
        }

        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            removeDependency(project, dep);
        }
        
    }
    
    private static final class LibraryDependencyNode extends FilterNode {
        
        private final ModuleDependency dep;
        private final NbModuleProject project;
        
        LibraryDependencyNode(final ModuleDependency dep,
                final NbModuleProject project, final Node original) {
            super(original, null, new ProxyLookup(original.getLookup(), Lookups.fixed(dep, project)));
            this.dep = dep;
            this.project = project;
            setShortDescription(LibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                new EditDependencyAction(dep, project),
                SystemAction.get(FindAction.class),
                new ShowJavadocAction(dep, project),
                SystemAction.get(RemoveAction.class),
            };
        }
        
        public Action getPreferredAction() {
            return new EditDependencyAction(dep, project);
        }

        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            removeDependency(project, dep);
        }
        
    }
    
    private static void removeDependency(NbModuleProject project, ModuleDependency dep) throws IOException {
        new ProjectXMLManager(project).removeDependencies(Collections.singleton(dep));
        ProjectManager.getDefault().saveProject(project);
    }
    
    private static final class AddModuleDependencyAction extends AbstractAction {
        
        private final NbModuleProject project;
        
        AddModuleDependencyAction(final NbModuleProject project) {
            super(getMessage("CTL_AddModuleDependency"));
            this.project = project;
        }
        
        public void actionPerformed(ActionEvent ev) {
            SingleModuleProperties props = SingleModuleProperties.getInstance(project);
            ModuleDependency[] newDeps = AddModulePanel.selectDependencies(props);
            ProjectXMLManager pxm = new ProjectXMLManager(project);
            try {
                for (ModuleDependency dep : newDeps) {
                    pxm.addDependency(dep);
                }
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException e) {
                ErrorManager.getDefault().annotate(e, "Cannot add selected dependencies: " + Arrays.asList(newDeps)); // NOI18N
                ErrorManager.getDefault().notify(e);
            }
        }

    }
    
    private static final class EditDependencyAction extends AbstractAction {
        
        private final ModuleDependency dep;
        private final NbModuleProject project;
        
        EditDependencyAction(final ModuleDependency dep, final NbModuleProject project) {
            super(getMessage("CTL_EditDependency"));
            this.dep = dep;
            this.project = project;
        }
        
        public void actionPerformed(ActionEvent ev) {
            // XXX duplicated from CustomizerLibraries --> Refactor
            EditDependencyPanel editPanel = new EditDependencyPanel(dep,
                    NbPlatform.getPlatformByDestDir(dep.getModuleEntry().getDestDir()));
            DialogDescriptor descriptor = new DialogDescriptor(editPanel,
                    NbBundle.getMessage(LibrariesNode.class, "CTL_EditModuleDependencyTitle",
                    dep.getModuleEntry().getLocalizedName()));
            descriptor.setHelpCtx(new HelpCtx(EditDependencyPanel.class));
            Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
            d.setVisible(true);
            if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                ModuleDependency editedDep = editPanel.getEditedDependency();
                try {
                    ProjectXMLManager pxm = new ProjectXMLManager(project);
                    SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(pxm.getDirectDependencies());
                    deps.remove(dep);
                    deps.add(editedDep);
                    pxm.replaceDependencies(deps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    ErrorManager.getDefault().annotate(e, "Cannot store dependency: " + editedDep); // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
            }
            d.dispose();
        }
        
    }
    
    private static final class ShowJavadocAction extends AbstractAction {
        
        private final ModuleDependency dep;
        private final NbModuleProject project;
        
        private URL currectJavadoc;
        
        ShowJavadocAction(final ModuleDependency dep, final NbModuleProject project) {
            super(getMessage("CTL_ShowJavadoc"));
            this.dep = dep;
            this.project = project;
        }
        
        public void actionPerformed(ActionEvent ev) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(currectJavadoc);
        }
        
        public boolean isEnabled() {
            if (Util.getModuleType(project) == NbModuleProvider.NETBEANS_ORG) {
                currectJavadoc = Util.findJavadocForNetBeansOrgModules(dep);
            } else {
                currectJavadoc = Util.findJavadoc(dep, project.getPlatform(true));
            }
            return currectJavadoc != null;
        }
        
    }
    
    static final class OpenProjectAction extends CookieAction {
        
        protected void performAction(Node[] activatedNodes) {
            try {
                final Project[] projects = new Project[activatedNodes.length];
                for (int i = 0; i < activatedNodes.length; i++) {
                    ModuleEntry me = activatedNodes[i].getLookup().lookup(ModuleEntry.class);
                    assert me != null;
                    File prjDir = me.getSourceLocation();
                    assert prjDir != null;
                    Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDir));
                    assert project != null;
                    projects[i] = project;
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        StatusDisplayer.getDefault().setStatusText(
                                getMessage("MSG_OpeningProjects"));
                        OpenProjects.getDefault().open(projects, false);
                    }
                });
            } catch (IOException e) {
                assert false : e;
            }
        }
        
        public boolean isEnabled() {
            return true;
        }
        
        public String getName() {
            return getMessage("CTL_Open");
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
            return new Class[] { ModuleDependency.class, TestModuleDependency.class };
        }
        
    }
    
    private static final class RemoveAction extends DeleteAction {
        
        public String getName() {
            return getMessage("CTL_RemoveDependency");
        }

        protected void initialize() {
            super.initialize();
            putValue(Action.ACCELERATOR_KEY, SystemAction.get(DeleteAction.class).getValue(Action.ACCELERATOR_KEY));
        }
        
    }
    
}
