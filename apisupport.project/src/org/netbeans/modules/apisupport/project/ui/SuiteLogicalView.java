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
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
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
    
    private static final class SuiteRootNode extends AbstractNode {
        
        private final SuiteProject suite;
        
        public SuiteRootNode(final SuiteProject suite) {
            super(createRootChildren(suite), Lookups.fixed(new Object[] {suite}));
            ProjectInformation info = ProjectUtils.getInformation(suite);
            setName(info.getName());
            setDisplayName(info.getDisplayName());
            this.suite = suite;
        }
        
        public Action[] getActions(boolean context) {
            return SuiteActions.getProjectActions(suite);
        }
        
        public Image getIcon(int type) {
            return Utilities.loadImage(SuiteProject.SUITE_ICON_PATH, true);
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
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
        
        private RequestProcessor.Task task;
        private SuiteProject suite;
        
        ModulesNode(final SuiteProject suite) {
            super(createSuiteComponentNodes(suite));
            this.suite = suite;
            setName("Modules");
            setDisplayName("Modules");
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
        
    }
    
    private static final class AddSuiteComponentAction extends AbstractAction {
        
        private final SuiteProject suite;
        
        public AddSuiteComponentAction(final SuiteProject suite) {
            super(NbBundle.getMessage(SuiteLogicalView.class, "CTL_AddModule"));
            this.suite = suite;
        }
        
        public void actionPerformed(ActionEvent evt) {
            final Project project = UIUtil.chooseSuiteComponent(
                    WindowManager.getDefault().getMainWindow(),
                    ProjectUtils.getInformation(suite).getDisplayName());
            if (project != null) {
                SubprojectProvider spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
                Set/*<Project>*/ subModules = spp.getSubprojects();
                if (!subModules.contains(project)) {
                    try {
                        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                            public Object run() throws IOException {
                                SuiteUtils.addModule(suite, (NbModuleProject) project);
                                return Boolean.TRUE;
                            }
                        });
                        // and save the project
                        if (result == Boolean.TRUE) {
                            ProjectManager.getDefault().saveProject(suite);
                        }
                    } catch (MutexException e) {
                        ErrorManager.getDefault().notify((IOException)e.getException());
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        }
    }
    
    private static Children createSuiteComponentNodes(final SuiteProject suite) {
        SubprojectProvider spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
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
        
        private NbModuleProject suiteComponent;
        
        public SuiteComponentNode(final NbModuleProject suiteComponent) {
            super(Children.LEAF);
            this.suiteComponent = suiteComponent;
            ProjectInformation info = ProjectUtils.getInformation(suiteComponent);
            setName(info.getName());
            setDisplayName(info.getDisplayName());
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                new OpenProjectAction(suiteComponent),
                new RemoveSuiteComponentAction(suiteComponent)
            };
        }
        
    }
    
    private static final class RemoveSuiteComponentAction extends AbstractAction {
        
        private final NbModuleProject suiteComponent;
        
        public RemoveSuiteComponentAction(final NbModuleProject suiteComponent) {
            super(NbBundle.getMessage(SuiteLogicalView.class, "CTL_RemoveModule"));
            this.suiteComponent = suiteComponent;
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                    public Object run() throws IOException {
                        SuiteUtils.removeModuleFromSuite(suiteComponent);
                        return Boolean.TRUE;
                    }
                });
                // and save the project
                if (result == Boolean.TRUE) {
                    ProjectManager.getDefault().saveProject(suiteComponent);
                }
            } catch (MutexException e) {
                ErrorManager.getDefault().notify((IOException)e.getException());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    private static final class OpenProjectAction extends AbstractAction {
        
        private final NbModuleProject suiteComponent;
        
        public OpenProjectAction(final NbModuleProject suiteComponent) {
            super(NbBundle.getMessage(SuiteLogicalView.class, "CTL_OpenProject"));
            this.suiteComponent = suiteComponent;
        }
        
        public void actionPerformed(ActionEvent evt) {
            OpenProjects.getDefault().open(new Project[] {suiteComponent}, false);
        }
    }
    
}
