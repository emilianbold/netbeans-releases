/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel;
import org.netbeans.modules.apisupport.project.ui.customizer.EditDependencyPanel;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Martin Krauskopf
 */
final class LibrariesNode extends AbstractNode {
    
    static final String LIBRARIES_NAME = "libraries"; // NOI18N
    
    private static final String DISPLAY_NAME = getMessage("LBL_libraries");
    
    private Action[] actions;
    
    public LibrariesNode(final NbModuleProject project) {
        super(new LibrariesChildren(project));
        setName(LIBRARIES_NAME);
        setDisplayName(DISPLAY_NAME);
        if (Util.getModuleType(project) == NbModuleTypeProvider.SUITE_COMPONENT) {
            actions = new Action[] {
                new AddModuleDependencyAction(project),
                new SuiteLogicalView.AddNewLibraryWrapperAction(project, project)
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
        StringBuffer shortDesc = new StringBuffer("<html>" + // NOI18N
                "<u>" + dep.getModuleEntry().getCodeNameBase() + "</u><br>"); // NOI18N
        if (dep.hasImplementationDepedendency()) {
            shortDesc.append("<br><font color=\"red\">" + getMessage("CTL_ImplementationDependency") + "</font>");
        }
        if (dep.hasCompileDependency()) {
            shortDesc.append("<br>" + getMessage("CTL_NeededToCompile"));
        }
        if (dep.getReleaseVersion() != null) {
            shortDesc.append("<br>" + NbBundle.getMessage(LibrariesNode.class, "CTL_MajorReleaseVersion",
                    dep.getReleaseVersion()));
        }
        if (dep.getSpecificationVersion() != null) {
            shortDesc.append("<br>" + NbBundle.getMessage(LibrariesNode.class, "CTL_SpecificationVersion",
                    dep.getSpecificationVersion()));
        }
        shortDesc.append("</html>"); // NOI18N
        return shortDesc.toString();
    }
    
    private static String getMessage(String bundleKey) {
        return NbBundle.getMessage(LibrariesNode.class, bundleKey);
    }
    
    private static final class LibrariesChildren extends Children.Keys implements AntProjectListener {
        
        private static final String JDK_PLATFORM_NAME = "jdkPlatform"; // NOI18N
        
        private static final String LIBRARIES_ICON =
                "org/netbeans/modules/apisupport/project/ui/resources/libraries.gif"; // NOI18N
        
        static final Action OPEN_PROJECT_ACTION = new OpenProjectAction();
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
            setKeys(Collections.EMPTY_SET);
            project.getHelper().removeAntProjectListener(this);
            super.removeNotify();
        }
        
        private void refreshKeys() {
            // Since miscellaneous operations may be run upon the project.xml
            // of individual projects we could be called from the same thread
            // with already acquired ProjectManager.mutex. This could lead to
            // refreshing during the misconfigurated suite/suite_component
            // relationship.
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        ProjectManager.mutex().readAccess(new Mutex.ExceptionAction() {
                            public Object run() throws Exception {
                                ProjectXMLManager pxm = new ProjectXMLManager(project);
                                List keys = new ArrayList();
                                keys.add(JDK_PLATFORM_NAME);
                                SortedSet deps = new TreeSet(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
                                deps.addAll(pxm.getDirectDependencies());
                                keys.addAll(deps);
                                setKeys(Collections.EMPTY_SET); // XXX workaround for bad ModuleDependency comparision mechanism implementation
                                setKeys(Collections.unmodifiableList(keys));
                                return null;
                            }
                        });
                    } catch (MutexException e) {
                        assert false : e.getException();
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
            refreshKeys();
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // do not need
        }
        
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
        
        private Icon getLibrariesIcon() {
            if (librariesIcon == null) {
                librariesIcon = new ImageIcon(Utilities.loadImage(LIBRARIES_ICON, true));
            }
            return librariesIcon;
        }
        
    }
    
    private static final class ProjectDependencyNode extends AbstractNode {
        
        private ModuleDependency dep;
        private NbModuleProject project;
        private Action[] actions;
        
        ProjectDependencyNode(final ModuleDependency dep, final NbModuleProject project) {
            super(Children.LEAF, Lookups.fixed(new Object[] { dep, project }));
            this.dep = dep;
            this.project = project;
            ModuleEntry me = dep.getModuleEntry();
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            setDisplayName(me.getLocalizedName());
            setShortDescription(LibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            if (actions == null) {
                Set result = new LinkedHashSet();
                result.add(LibrariesChildren.OPEN_PROJECT_ACTION);
                result.add(new EditDependencyAction(dep, project));
                result.add(new ShowJavadocAction(dep, project));
                result.add(LibrariesChildren.REMOVE_DEPENDENCY_ACTION);
                actions = (Action[]) result.toArray(new Action[result.size()]);
            }
            return actions;
        }
        
        public Action getPreferredAction() {
            return getActions(false)[0]; // open
        }
        
    }
    
    private static final class LibraryDependencyNode extends FilterNode {
        
        private ModuleDependency dep;
        private NbModuleProject project;
        private Action[] actions;
        
        LibraryDependencyNode(final ModuleDependency dep,
                final NbModuleProject project, final Node original) {
            super(original, null, new ProxyLookup(new Lookup[] {
                original.getLookup(),
                Lookups.fixed(new Object[] { dep, project })
            }));
            this.dep = dep;
            this.project = project;
            setShortDescription(LibrariesNode.createHtmlDescription(dep));
        }
        
        public Action[] getActions(boolean context) {
            if (actions == null) {
                Set result = new LinkedHashSet();
                result.add(new EditDependencyAction(dep, project));
                Action[] superActions = super.getActions(false);
                for (int i = 0; i < superActions.length; i++) {
                    if (superActions[i] instanceof FindAction) {
                        result.add(superActions[i]);
                    }
                }
                result.add(new ShowJavadocAction(dep, project));
                result.add(LibrariesChildren.REMOVE_DEPENDENCY_ACTION);
                actions = (Action[]) result.toArray(new Action[result.size()]);
            }
            return actions;
        }
        
        public Action getPreferredAction() {
            return getActions(false)[0]; // edit
        }
        
    }
    
    private static final class AddModuleDependencyAction extends AbstractAction {
        
        private final NbModuleProject project;
        
        AddModuleDependencyAction(final NbModuleProject project) {
            super(getMessage("CTL_AddModuleDependency"));
            this.project = project;
        }
        
        public void actionPerformed(ActionEvent ev) {
            // XXX duplicated from CustomizerLibraries --> Refactor
            SingleModuleProperties props = SingleModuleProperties.getInstance(project);
            final AddModulePanel addPanel = new AddModulePanel(props);
            final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                    getMessage("CTL_AddModuleDependencyTitle"));
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
                ModuleDependency[] newDeps = addPanel.getSelectedDependencies();
                ProjectXMLManager pxm = new ProjectXMLManager(project);
                try {
                    for (int i = 0; i < newDeps.length; i++) {
                        pxm.addDependency(newDeps[i]);
                    }
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    ErrorManager.getDefault().annotate(e, "Cannot add selected dependencies: " + Arrays.asList(newDeps)); // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
            }
            d.dispose();
        }
        
    }
    
    private static final class RemoveDependencyAction extends CookieAction {
        
        protected void performAction(Node[] activatedNodes) {
            // we have to count with multiple selection from multiple projects
            Map/*<NbModuleProject, Set<ModuleDependency>>*/ map = new HashMap();
            for (int i = 0; i < activatedNodes.length; i++) {
                ModuleDependency dep = (ModuleDependency) activatedNodes[i].getLookup().lookup(ModuleDependency.class);
                assert dep != null;
                NbModuleProject project = (NbModuleProject) activatedNodes[i].getLookup().lookup(NbModuleProject.class);
                assert project != null;
                Set deps = (Set) map.get(project);
                if (deps == null) {
                    deps = new HashSet();
                    map.put(project, deps);
                }
                deps.add(dep);
            }
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry me = (Map.Entry) it.next();
                NbModuleProject project = (NbModuleProject) me.getKey();
                Set deps = (Set) me.getValue();
                ProjectXMLManager pxm = new ProjectXMLManager(project);
                pxm.removeDependencies(deps);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    ErrorManager.getDefault().annotate(e, "Problem during dependencies removing"); // NOI18N
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
            return new Class[] { ModuleDependency.class, NbModuleProject.class };
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
                    SortedSet/*<Dependency>*/ deps = new TreeSet(pxm.getDirectDependencies());
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
            if (Util.getModuleType(project) == NbModuleTypeProvider.NETBEANS_ORG) {
                currectJavadoc = Util.findJavadocForNetBeansOrgModules(dep);
            } else {
                currectJavadoc = Util.findJavadoc(dep, project.getPlatform(true));
            }
            return currectJavadoc != null;
        }
        
    }
    
    private static final class OpenProjectAction extends CookieAction {
        
        protected void performAction(Node[] activatedNodes) {
            try {
                final Project[] projects = new Project[activatedNodes.length];
                for (int i = 0; i < activatedNodes.length; i++) {
                    ModuleDependency dep = (ModuleDependency) activatedNodes[i].
                            getLookup().lookup(ModuleDependency.class);
                    assert dep != null;
                    File prjDir = dep.getModuleEntry().getSourceLocation();
                    assert prjDir != null;
                    Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDir));
                    assert project != null;
                    projects[i] = project;
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        String previousText = StatusDisplayer.getDefault().getStatusText();
                        StatusDisplayer.getDefault().setStatusText(
                                getMessage("MSG_OpeningProjects"));
                        OpenProjects.getDefault().open(projects, false);
                        StatusDisplayer.getDefault().setStatusText(previousText);
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
            return new Class[] { ModuleDependency.class };
        }
        
    }
    
}
