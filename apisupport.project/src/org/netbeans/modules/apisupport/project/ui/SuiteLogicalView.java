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

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.NewNbModuleWizardIterator;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * Provides a logical view of a NetBeans suite project.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class SuiteLogicalView implements LogicalViewProvider {
    
    private final SuiteProject suite;
    
    public SuiteLogicalView(final SuiteProject suite) {
        this.suite = suite;
    }
    
    public Node createLogicalView() {
        return new SuiteRootNode(suite);
    }
    
    public Node findPath(Node root, Object target) {
        // XXX
        return null;
    }
    
    /** Package private for unit test only. */
    static final class SuiteRootNode extends AnnotatedNode
            implements PropertyChangeListener {
        
        private static final Image ICON = Utilities.loadImage(SuiteProject.SUITE_ICON_PATH, true);
        
        private final SuiteProject suite;
        private final ProjectInformation info;
        
        SuiteRootNode(final SuiteProject suite) {
            super(createRootChildren(suite), Lookups.fixed(new Object[] {suite}));
            this.suite = suite;
            info = ProjectUtils.getInformation(suite);
            info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
            setFiles(getProjectFiles());
        }
        
        /** Package private for unit test only. */
        Set getProjectFiles() {
            Set files = new HashSet();
            Enumeration en = suite.getProjectDirectory().getChildren(false);
            while (en.hasMoreElements()) {
                FileObject child = (FileObject) en.nextElement();
                if (FileOwnerQuery.getOwner(child) == suite) {
                    files.add(child);
                }
            }
            return files;
        }
        
        public String getName() {
            return info.getDisplayName();
        }
        
        public String getDisplayName() {
            return info.getDisplayName();
        }
        
        public String getShortDescription() {
            return NbBundle.getMessage(SuiteLogicalView.class, "HINT_suite_project_root_node",
                    FileUtil.getFileDisplayName(suite.getProjectDirectory()));
        }
        
        public Action[] getActions(boolean context) {
            return SuiteActions.getProjectActions(suite);
        }
        
        public Image getIcon(int type) {
            return annotateIcon(ICON, type);
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type); // the same in the meantime
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ProjectInformation.PROP_NAME)) {
                fireNameChange(null, getName());
            } else if (evt.getPropertyName().equals(ProjectInformation.PROP_DISPLAY_NAME)) {
                fireDisplayNameChange(null, getDisplayName());
            }
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(suite, name);
        }
        
    }
    
    private static Children createRootChildren(final SuiteProject suite) {
        Node[] nodes = new Node[] { new ModulesNode(suite) };
        Children children = new Children.Array();
        children.add(nodes);
        return children;
    }
    
    /** Represent <em>Modules</em> node in the Suite Logical View. */
    static final class ModulesNode extends AbstractNode {
        
        private SuiteProject suite;
        
        ModulesNode(final SuiteProject suite) {
            super(new ModuleChildren(suite));
            this.suite = suite;
            setName("modules"); // NOI18N
            setDisplayName(NbBundle.getMessage(SuiteLogicalView.class, "CTL_Modules"));
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                new AddNewSuiteComponentAction(suite),
                new AddNewLibraryWrapperAction(suite),
                new AddSuiteComponentAction(suite),
            };
        }
        
        private Image getIcon(boolean opened) {
            Image badge = Utilities.loadImage("org/netbeans/modules/apisupport/project/suite/resources/module-badge.gif", true);
            return Utilities.mergeImages(UIUtil.getTreeFolderIcon(opened), badge, 9, 9);
        }
        
        public Image getIcon(int type) {
            return getIcon(false);
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(true);
        }
        
        static final class ModuleChildren extends Children.Keys/*<NbModuleProject>*/ implements ChangeListener {
            
            private final SubprojectProvider spp;
            
            public ModuleChildren(SuiteProject suite) {
                this.spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
                spp.addChangeListener(this);
            }
            
            protected void addNotify() {
                updateKeys();
            }
            
            private void updateKeys() {
                // e.g.(?) Explorer view under Children.MUTEX subsequently calls e.g.
                // SuiteProject$Info.getSimpleName() which acquires ProjectManager.mutex(). And
                // since this method might be called under ProjectManager.mutex() write access
                // and updateKeys() --> setKeys() in turn calls Children.MUTEX write access,
                // deadlock is here, so preventing it... (also got this under read access)
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        // #70112: sort them.
                        SortedSet/*<NbModuleProject>*/ subModules = new TreeSet(Util.projectDisplayNameComparator());
                        subModules.addAll(spp.getSubprojects());
                        setKeys(subModules);
                    }
                });
            }
            
            protected void removeNotify() {
                spp.removeChangeListener(this);
                setKeys(Collections.EMPTY_SET);
            }
            
            protected Node[] createNodes(Object key) {
                return new Node[] { new SuiteComponentNode((NbModuleProject) key) };
            }
            
            public void stateChanged(ChangeEvent ev) {
                updateKeys();
            }
            
        }
        
    }
    
    private static final class AddSuiteComponentAction extends AbstractAction {
        
        private final SuiteProject suite;
        
        public AddSuiteComponentAction(final SuiteProject suite) {
            super(NbBundle.getMessage(SuiteLogicalView.class, "CTL_AddModule"));
            this.suite = suite;
        }
        
        public void actionPerformed(ActionEvent evt) {
            NbModuleProject project = UIUtil.chooseSuiteComponent(
                    WindowManager.getDefault().getMainWindow(),
                    suite);
            if (project != null) {
                if (!SuiteUtils.contains(suite, project)) {
                    try {
                        SuiteUtils.addModule(suite, (NbModuleProject) project);
                        ProjectManager.getDefault().saveProject(suite);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                } else {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(SuiteLogicalView.class, "MSG_SuiteAlreadyContainsCNB", project.getCodeNameBase())));
                }
            }
        }
        
    }
    
    private static final class AddNewSuiteComponentAction extends AbstractAction {
        
        private final SuiteProject suite;
        
        public AddNewSuiteComponentAction(final SuiteProject suite) {
            super(NbBundle.getMessage(SuiteLogicalView.class, "CTL_AddNewModule"));
            this.suite = suite;
        }
        
        public void actionPerformed(ActionEvent evt) {
            NewNbModuleWizardIterator iterator = NewNbModuleWizardIterator.createSuiteComponentIterator(suite);
            UIUtil.runProjectWizard(iterator, "CTL_NewModuleProject"); // NOI18N
        }
        
    }
    
    static final class AddNewLibraryWrapperAction extends AbstractAction {
        
        private final Project suiteProvider;
        private final NbModuleProject target;
        
        public AddNewLibraryWrapperAction(final Project suiteProvider, final NbModuleProject target) {
            super(NbBundle.getMessage(SuiteLogicalView.class, "CTL_AddNewLibrary"));
            this.suiteProvider = suiteProvider;
            this.target = target;
        }
        
        public AddNewLibraryWrapperAction(final Project suiteProvider) {
            this(suiteProvider, null);
        }
        
        public void actionPerformed(ActionEvent evt) {
            NbModuleProject project = UIUtil.runLibraryWrapperWizard(suiteProvider);
            if (project != null && target != null) {
                try {
                    Util.addDependency(target, project);
                    ProjectManager.getDefault().saveProject(target);
                } catch (IOException e) {
                    assert false : e;
                }
            }
        }
        
    }
    
    /** Represent one module (a suite component) node. */
    private static final class SuiteComponentNode extends AbstractNode {
        
        private final static Action REMOVE_ACTION = new RemoveSuiteComponentAction();
        private final static Action OPEN_ACTION = new OpenProjectAction();
        
        public SuiteComponentNode(final NbModuleProject suiteComponent) {
            super(Children.LEAF, Lookups.fixed(new Object[] {suiteComponent}));
            ProjectInformation info = ProjectUtils.getInformation(suiteComponent);
            setName(info.getName());
            setDisplayName(info.getDisplayName());
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            info.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == ProjectInformation.PROP_DISPLAY_NAME) {
                        SuiteComponentNode.this.setDisplayName((String) evt.getNewValue());
                    } else if (evt.getPropertyName() == ProjectInformation.PROP_NAME) {
                        SuiteComponentNode.this.setName((String) evt.getNewValue());
                    }
                }
            });
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                OPEN_ACTION, REMOVE_ACTION
            };
        }
        
        public Action getPreferredAction() {
            return OPEN_ACTION;
        }
        
    }
    
    private static final class RemoveSuiteComponentAction extends NodeAction {
        
        protected void performAction(Node[] activatedNodes) {
            for (int i = 0; i < activatedNodes.length; i++) {
                final NbModuleProject suiteComponent =
                        (NbModuleProject) activatedNodes[i].getLookup().lookup(NbModuleProject.class);
                assert suiteComponent != null : "NbModuleProject in lookup"; // NOI18N
                try {
                    NbModuleProject[] modules = SuiteUtils.getDependentModules(suiteComponent);
                    boolean remove = true;
                    if (modules.length > 0) {
                        StringBuffer sb = new StringBuffer("<ul>"); // NOI18N
                        for (int j = 0; j < modules.length; j++) {
                            sb.append("<li>" + ProjectUtils.getInformation(modules[j]).getDisplayName() + "</li>"); // NOI18N
                        }
                        sb.append("</ul>"); // NOI18N
                        String confirmMessage = NbBundle.getMessage(SuiteLogicalView.class, "MSG_RemovingModuleMessage",
                                ProjectUtils.getInformation(suiteComponent).getDisplayName(), sb.toString());
                        NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(confirmMessage,
                                NbBundle.getMessage(SuiteLogicalView.class, "CTL_RemovingModuleTitle"),
                                NotifyDescriptor.OK_CANCEL_OPTION);
                        DialogDisplayer.getDefault().notify(confirm);
                        if (confirm.getValue() == NotifyDescriptor.CANCEL_OPTION) {
                            remove = false;
                        }
                    }
                    if (remove) {
                        SuiteUtils.removeModuleFromSuiteWithDependencies(suiteComponent);
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        public String getName() {
            return NbBundle.getMessage(SuiteLogicalView.class, "CTL_RemoveModule");
        }
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        protected boolean asynchronous() {
            return false;
        }
        
    }
    
    private static final class OpenProjectAction extends CookieAction {
        
        protected void performAction(Node[] activatedNodes) {
            final Project[] projects = new Project[activatedNodes.length];
            for (int i = 0; i < activatedNodes.length; i++) {
                Project project = (Project) activatedNodes[i].getLookup().lookup(Project.class);
                projects[i] = project;
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    String previousText = StatusDisplayer.getDefault().getStatusText();
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(SuiteLogicalView.class, "MSG_OpeningProjects"));
                    OpenProjects.getDefault().open(projects, false);
                    StatusDisplayer.getDefault().setStatusText(previousText);
                }
            });
        }
        
        public String getName() {
            return NbBundle.getMessage(SuiteLogicalView.class, "CTL_OpenProject");
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
            return new Class[] { Project.class };
        }
        
    }
    
}
