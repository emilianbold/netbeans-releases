/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.LogicalViewProvider;
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
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * Provides a logical view of a NetBeans suite project.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class SuiteLogicalView implements LogicalViewProvider {
    
    // package private for unit test
    static final int MODULES_NODE_SCHEDULE = 100;
    
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
            return info.getName();
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
        
    }
    
    private static Children createRootChildren(final SuiteProject suite) {
        Node[] nodes = new Node[] { new ModulesNode(suite) };
        Children children = new Children.Array();
        children.add(nodes);
        return children;
    }
    
    /** Represent <em>Modules</em> node in the Suite Logical View. */
    static final class ModulesNode extends AbstractNode implements Runnable {
        
        public static final String SUITE_MODULES_ICON_PATH =
                "org/netbeans/modules/apisupport/project/suite/resources/suiteModules.gif"; // NOI18N
        public static final String SUITE_MODULES_OPENED_ICON_PATH =
                "org/netbeans/modules/apisupport/project/suite/resources/suiteModulesOpened.gif"; // NOI18N
        
        private RequestProcessor.Task task;
        private SuiteProject suite;
        
        ModulesNode(final SuiteProject suite) {
            super(createSuiteComponentNodes(suite));
            this.suite = suite;
            setName("modules"); // NOI18N
            setDisplayName(NbBundle.getMessage(SuiteLogicalView.class, "CTL_Modules"));
            suite.getHelper().addAntProjectListener(new AntProjectListener() {
                public void configurationXmlChanged(AntProjectEvent ev) {
                }
                public void propertiesChanged(AntProjectEvent ev) {
                    if (task == null) {
                        task = RequestProcessor.getDefault().create(ModulesNode.this);
                    }
                    task.schedule(MODULES_NODE_SCHEDULE); // batch by MODULES_NODE_SCHEDULE
                }
            });
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                new AddSuiteComponentAction(suite)
            };
        }
        
        public void run() {
            setChildren(createSuiteComponentNodes(suite));
        }
        
        public Image getIcon(int type) {
            return Utilities.loadImage(SUITE_MODULES_ICON_PATH);
        }
        
        public Image getOpenedIcon(int type) {
            return Utilities.loadImage(SUITE_MODULES_OPENED_ICON_PATH);
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
    
    private static Children createSuiteComponentNodes(final SuiteProject suite) {
        Set/*<Project>*/ subModules = SuiteUtils.getSubProjects(suite);
        Node[] nodes = new Node[subModules.size()];
        Children children = new Children.Array();
        int i = 0;
        for (Iterator it = subModules.iterator(); it.hasNext();) {
            NbModuleProject suiteComponent = (NbModuleProject) it.next();
            nodes[i++] = new SuiteComponentNode(suiteComponent);
        }
        children.add(nodes);
        return children;
    }
    
    /** Represent one module (a suite component) node. */
    private static final class SuiteComponentNode extends AbstractNode {
        
        private final static Action removeAction = new RemoveSuiteComponentAction();
        private final static Action defaultAction = new OpenProjectAction();
        
        public SuiteComponentNode(final NbModuleProject suiteComponent) {
            super(Children.LEAF, Lookups.fixed(new Object[] {suiteComponent}));
            ProjectInformation info = ProjectUtils.getInformation(suiteComponent);
            setName(info.getName());
            setDisplayName(info.getDisplayName());
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                defaultAction, removeAction
            };
        }
        
        public Action getPreferredAction() {
            return defaultAction;
        }
        
    }
    
    private static final class RemoveSuiteComponentAction extends NodeAction {
        
        protected void performAction(Node[] activatedNodes) {
            for (int i = 0; i < activatedNodes.length; i++) {
                NbModuleProject suiteComponent =
                        (NbModuleProject) activatedNodes[i].getLookup().lookup(NbModuleProject.class);
                assert suiteComponent != null : "NbModuleProject in lookup"; // NOI18N
                try {
                    SuiteUtils.removeModuleFromSuite(suiteComponent);
                    ProjectManager.getDefault().saveProject(suiteComponent);
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
    
    private static final class OpenProjectAction extends NodeAction {
        
        protected void performAction(Node[] activatedNodes) {
            final Project[] projects = new Project[activatedNodes.length];
            for (int i = 0; i < activatedNodes.length; i++) {
                NbModuleProject suiteComponent =
                        (NbModuleProject) activatedNodes[i].getLookup().lookup(NbModuleProject.class);
                assert suiteComponent != null : "NbModuleProject in lookup"; // NOI18N
                projects[i] = suiteComponent;
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
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
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
        
    }
    
}
