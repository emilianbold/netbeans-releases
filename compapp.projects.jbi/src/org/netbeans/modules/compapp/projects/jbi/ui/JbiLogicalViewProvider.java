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

package org.netbeans.modules.compapp.projects.jbi.ui;

import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.test.ui.TestNode;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.openide.nodes.*;

import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;

import java.awt.event.ActionEvent;
import java.awt.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.api.*;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;

/**
 * Support for creating logical views.
 *
 * @author Petr Hrebejk
 */
public class JbiLogicalViewProvider implements LogicalViewProvider {
    // Private innerclasses ----------------------------------------------------
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
//        JbiProjectProperties.JAVAC_CLASSPATH, JbiProjectProperties.DEBUG_CLASSPATH,
        JbiProjectProperties.JBI_CONTENT_ADDITIONAL, JbiProjectProperties.SRC_DIR
    };
    
    private final JbiProject project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    
    /** The open folder icon */
    private static Image mIcon =
            new ImageIcon(JbiLogicalViewProvider.class.getClassLoader().getResource
            ("org/netbeans/modules/compapp/projects/jbi/ui/resources/composite_application_project.png")).getImage(); // NOI18N
    
    /** The open folder icon */
    private static Image mEmpty =
            new ImageIcon(JbiLogicalViewProvider.class.getClassLoader().getResource
            ("org/netbeans/modules/compapp/projects/jbi/ui/resources/brokenProjectBadge.gif")).getImage(); // NOI18N
    
    private static Image mEmptyIcon = mIcon;
    
    private JbiLogicalViewRootNode jRoot;
    private boolean isEmpty = false;
    
    /**
     * Creates a new JbiLogicalViewProvider object.
     *
     * @param project DOCUMENT ME!
     * @param helper DOCUMENT ME!
     * @param evaluator DOCUMENT ME!
     * @param spp DOCUMENT ME!
     * @param resolver DOCUMENT ME!
     */
    public JbiLogicalViewProvider(
            JbiProject project, AntProjectHelper helper, PropertyEvaluator evaluator,
            SubprojectProvider spp, ReferenceHelper resolver
            ) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
        
        if (mEmpty != null) {
            mEmptyIcon = Utilities.mergeImages(mIcon, mEmpty, 8, 0);
        }
        
//        isEmpty = isProjectEmpty();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Node createLogicalView() {
        jRoot = new JbiLogicalViewRootNode();
        Children kids = jRoot.getChildren();
        
        final JbiProjectProperties pps = 
                new JbiProjectProperties(project, helper, resolver);
        
        try {
            //helper.addAntProjectListener(epp);
            kids.add(new Node[]{new JbiModuleViewNode(pps, project)});
            if (project.getTestDirectory() != null) {
                kids.add(new Node[]{new TestNode(pps, project)});
            }
        } catch (Exception ioe) {
            org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
        }

        // In case test directory is missing initially and created later (IZ 115285)
        FileChangeListener fileChangeListener = new FileChangeAdapter() {
            @Override
            public void fileFolderCreated(FileEvent fe) {   
                String testDirName = (String) project.getProjectProperties().
                        get(JbiProjectProperties.TEST_DIR);                
                if (fe.getFile().getNameExt().equals(testDirName)) {  
                    Children kids = jRoot.getChildren();
                    kids.add(new Node[]{new TestNode(pps, project)});
                    jRoot.refreshNode();
                }
            }
            
            @Override
            public void fileRenamed(FileRenameEvent fe) {
                String oldName = fe.getName();
                String oldExt = fe.getExt();
                String oldNameExt = (oldExt == null || oldExt.trim().equals("")) ? // NOI18N
                    oldName : (oldName + "." + oldExt); // NOI18N
                
                String testDirName = (String) project.getProjectProperties().
                        get(JbiProjectProperties.TEST_DIR);
                
                if (oldNameExt.equals(testDirName)) {
                    Children kids = jRoot.getChildren();
                    for (Node node : kids.getNodes()) {
                        if (node instanceof TestNode) {
                            kids.remove(new Node[] {node});
                            break;
                        }
                    }
                }
                
                String newNameExt = fe.getFile().getNameExt();
                if (newNameExt.equals(testDirName)) {
                    Children kids = jRoot.getChildren();
                    kids.add(new Node[]{new TestNode(pps, project)});
                    jRoot.refreshNode();
                }
            }
            
            // Test dir deletion is automatically taken care of by TestNode
        };
        project.getProjectDirectory().addFileChangeListener(fileChangeListener);        
        
        return jRoot;
    }
    
    
    /**
     * DOCUMENT ME!
     *
     */
    public void refreshRootNode() {
        boolean newEmpty = isProjectEmpty();
        if (newEmpty != isEmpty) {
            isEmpty = newEmpty;
            if (jRoot != null) {
                jRoot.refreshNode();
            }
        }
    }
    
    private boolean isProjectEmpty() {
        String comps = helper.getStandardPropertyEvaluator().getProperty(
                JbiProjectProperties.JBI_CONTENT_ADDITIONAL
                );
        
        if (comps != null && comps.trim().length() > 0) {
            return false;
        } else {
            return ! CasaHelper.containsWSDLPort(project);
        }
    }
    
    // Only used for detecting whether the "Debug (BPEL)" action should be 
    // enabled or not
    private boolean projectContainsBPELModule() {
        String comps = helper.getStandardPropertyEvaluator().getProperty(
                JbiProjectProperties.JBI_CONTENT_COMPONENT
                );
                
        return comps != null && comps.contains("sun-bpel-engine"); // NOI18N
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @param root DOCUMENT ME!
     * @param target DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        if (target instanceof DataObject) {
            target = ((DataObject)target).getPrimaryFile();
        }
        
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                Node result = PackageView.findPath(nodes[i], target);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    private static Lookup createLookup(Project project) {
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed(new Object[] {project, rootFolder});
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param helper DOCUMENT ME!
     * @param resolver DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(
                helper, resolver, BREAKABLE_PROPERTIES,
                new String[] {} //JbiProjectProperties.JAVA_PLATFORM}
        );
    }
    
    /**
     * Filter node containin additional features for the J2SE physical
     */
    private final class JbiLogicalViewRootNode extends AbstractNode {
        private Action brokenLinksAction;
        private boolean broken;
        private SubprojectListener subprojectListener;
        
        /**
         * Creates a new JbiLogicalViewRootNode object.
         */
        public JbiLogicalViewRootNode() {
            super(new JbiViews.LogicalViewChildren(helper, evaluator, project), 
                    createLookup(project));
            setIconBaseWithExtension("org/netbeans/modules/compapp/projects/jbi/ui/resources/composite_application_project.png"); // NOI18N
            super.setName(ProjectUtils.getInformation(project).getDisplayName());
            
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
            }
            brokenLinksAction = new BrokenLinksAction();
            subprojectListener = new SubprojectListener();
            
            final SubprojectProvider subprojectProvider = 
                    project.getLookup().lookup(SubprojectProvider.class);
            subprojectProvider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    Project subproject = (Project) e.getSource();
                    FileObject subprojectFO = subproject.getProjectDirectory();
                    if (subprojectProvider.getSubprojects().contains(subproject)) {
                        subprojectFO.addFileChangeListener(subprojectListener);
                    } else {
                        subprojectFO.removeFileChangeListener(subprojectListener);
                    }
                }                
            });
            
            updateSubprojectListeners();
        }
        
        private void updateSubprojectListeners() {
            SubprojectProvider subprojectProvider = 
                    project.getLookup().lookup(SubprojectProvider.class);
            for (Project subproject : subprojectProvider.getSubprojects()) {
                FileObject subprojectFO = subproject.getProjectDirectory();
                subprojectFO.removeFileChangeListener(subprojectListener);
                subprojectFO.addFileChangeListener(subprojectListener);
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(JbiLogicalViewProvider.class);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param context DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Action[] getActions(boolean context) {
            if (context) {
                return super.getActions(true);
            } else {
                return getAdditionalActions(context);
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param type DOCUMENT ME!
         * @return DOCUMENT ME!
         */
        public Image getIcon(int type) {
            return broken || isEmpty ? mEmptyIcon : mIcon;
        }
        
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            return broken || isEmpty ? "<font color=\"#A40000\">" + dispName + "</font>" : null; // NOI18N
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param type DOCUMENT ME!
         * @return DOCUMENT ME!
         */
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public void refreshNode() {
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean canRename() {
            return true; 
        }
        
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        // Private methods -------------------------------------------------
        private Action[] getAdditionalActions(boolean context) {
            ResourceBundle bundle = NbBundle.getBundle(JbiLogicalViewProvider.class);
            
            List<Action> actions = new ArrayList<Action>();
            
            actions.add(ProjectSensitiveActions.projectSensitiveAction(
                    new AddProjectAction(true),
                    bundle.getString("LBL_AddProjectAction_Name"),  // NOI18N
                    null
                    ));

            actions.add(ProjectSensitiveActions.projectSensitiveAction(
                    new AddProjectAction(false),
                    bundle.getString("LBL_AddExternalProjectAction_Name"),  // NOI18N
                    null
                    ));
            
            // Add Actions added by other modules.
            /*
            List<CompAppProjectActionPerformer> otherActions =
                    PluggableProjectActionsProvider.getInstance().getProjectActions();
            if ((otherActions != null) && (otherActions.size() > 0)) {
                for (CompAppProjectActionPerformer act : otherActions){
                    actions.add(ProjectSensitiveActions.projectSensitiveAction(
                            act, act.getLabel(), act.getIcon()));
                }
            }
            todo: 11/12/07, remove the above code when switching to using project plugins
            */
            
            JbiInstalledProjectPluginInfo plugins = JbiInstalledProjectPluginInfo.getProjectPluginInfo();
            if (plugins != null) {
                List<InternalProjectTypePlugin> plist = plugins.getUncategorizedProjectPluginList();
                for (InternalProjectTypePlugin plugin : plist) {
                    List<JbiProjectActionPerformer> acts = plugin.getProjectActions();
                    for (JbiProjectActionPerformer act : acts) {
                        if (act.getActionType().equalsIgnoreCase(JbiProjectActionPerformer.ACT_ADD_PROJECT)) {
                            actions.add(ProjectSensitiveActions.projectSensitiveAction(
                                act, act.getLabel(), act.getIcon()));
                        }
                    }
                }
            }
            
            actions.add(CommonProjectActions.newFileAction());
            
            // Create CASA on demand  //chikkala: moved to service composition node action.
            
            actions.add(null);

            actions.add(ProjectSensitiveActions.projectCommandAction(
                    JbiProjectConstants.COMMAND_JBIBUILD,
                    bundle.getString("LBL_JbiBuildAction_Name"),  // NOI18N
                    null
                    ));
            
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    JbiProjectConstants.COMMAND_JBICLEANBUILD,
                    bundle.getString("LBL_JbiCleanBuildAction_Name"),  // NOI18N
                    null
                    ));
            
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionProvider.COMMAND_CLEAN, 
                    bundle.getString("LBL_CleanAction_Name"),  // NOI18N
                    null
                    ));
            actions.add(null);
                        
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    JbiProjectConstants.COMMAND_DEPLOY, 
                    bundle.getString("LBL_DeployAction_Name"), // NOI18N
                    null
                    ));
                        
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    JbiProjectConstants.COMMAND_UNDEPLOY, 
                    bundle.getString("LBL_UnDeployAction_Name"), // NOI18N
                    null
                    ));
            // Start Test Framework
            actions.add(null);
            Action testAction = ProjectSensitiveActions.projectCommandAction(
                    JbiProjectConstants.COMMAND_TEST, 
                    bundle.getString("LBL_TestAction_Name"), // NOI18N
                    null
                    );
            actions.add(testAction);
            Action debugAction = ProjectSensitiveActions.projectCommandAction(
                    ActionProvider.COMMAND_DEBUG, 
                    bundle.getString("LBL_DebugAction_Name"), // NOI18N
                    null
                    );
            actions.add(new ActionDecorator(debugAction) {
                public boolean isEnabled() {
                    return projectContainsBPELModule();
                }                
            });            
            // End Test Framework
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            
            // honor 57874 contact
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
                        
            if (broken) {
                actions.add(brokenLinksAction);
            }
            
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
                        
            return actions.toArray(new Action[actions.size()]);            
        }
        
        /**
         * This action is created only when project has broken references. Once these are resolved
         * the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener {
            /**
             * Creates a new BrokenLinksAction object.
             */
            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                putValue(
                        Action.NAME,
                        NbBundle.getMessage(
                        JbiLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action" // NOI18N
                        )
                        );
            }
            
            /**
             * DOCUMENT ME!
             *
             * @param e DOCUMENT ME!
             */
            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(
                        helper, resolver, BREAKABLE_PROPERTIES,
                        new String[] {} //JbiProjectProperties.JAVA_PLATFORM}
                );
                
                if (!hasBrokenLinks(helper, resolver)) {
                    disable();
                    
                    // Make sure the target component list is not corrupted.
                    project.getProjectProperties().fixComponentTargetList();
                    
                    // Update ASI.xml which could be corrupted due to the 
                    // broken reference.
                    project.getProjectProperties().saveAssemblyInfo();

                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
                
                updateSubprojectListeners();
                
                // #135031: Update JbiModuleNode's children after resolving broken references
                for (Node childNode : jRoot.getChildren().getNodes()) {
                    if (childNode instanceof JbiModuleViewNode) {
                        JbiModuleViewChildren moduleViewChildren = 
                                (JbiModuleViewChildren) childNode.getChildren();
                        moduleViewChildren.modelChanged(null); // force update
                        break;
                    }
                }
            }
            
            /**
             * DOCUMENT ME!
             *
             * @param evt DOCUMENT ME!
             */
            public void propertyChange(PropertyChangeEvent evt) {
                if (!broken) {
                    disable();
                    return;
                }
                
                broken = hasBrokenLinks(helper, resolver);
                
                if (!broken) {
                    disable();
                }
            }
            
            private void disable() {
                broken = false;
                setEnabled(false);
                evaluator.removePropertyChangeListener(this);
//                fireIconChange();
//                fireOpenedIconChange();
//                fireDisplayNameChange(null, null);
            }
        }       
        
        /**
         * A file change listener on subproject changes.
         */
        private class SubprojectListener extends FileChangeAdapter {

            public void fileDeleted(FileEvent fe) {
                checkBrokenLinks();
            }

            public void fileRenamed(FileRenameEvent fe) {
                checkBrokenLinks();
            }
            
            private void checkBrokenLinks() {
                boolean newBroken = hasBrokenLinks(helper, resolver);
                if (newBroken != broken) {
                    broken = newBroken;
                    brokenLinksAction.setEnabled(broken);
                    refreshNode();
                }
            }
        }
    }
}

/**
 * Action wrapper.
 */
class ActionDecorator implements Action {

    private Action realAction;
    
    ActionDecorator(Action action) {
        realAction = action;
    }

    public Object getValue(String key) {
        return realAction.getValue(key);
    }

    public void putValue(String key, Object value) {
        realAction.putValue(key, value);
    }

    public void setEnabled(boolean b) {
        realAction.setEnabled(b);
    }

    public boolean isEnabled() {
        return realAction.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        realAction.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        realAction.removePropertyChangeListener(listener);
    }

    public void actionPerformed(ActionEvent e) {
        realAction.actionPerformed(e);
    }
        
}
