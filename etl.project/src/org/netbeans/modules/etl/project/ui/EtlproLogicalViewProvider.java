
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
package org.netbeans.modules.etl.project.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.modules.compapp.projects.base.ui.IcanproLogicalViewProvider;
import org.netbeans.modules.compapp.projects.base.IcanproConstants;
import org.netbeans.modules.etl.project.EtlproProject;
import org.netbeans.modules.etl.project.MasterIndexAction;
import org.netbeans.modules.mashup.db.wizard.NewFlatfileDatabaseWizardAction;
import org.netbeans.modules.mashup.db.wizard.NewFlatfileTableAction;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class EtlproLogicalViewProvider implements LogicalViewProvider {

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    //private static transient final Localizer mLoc = Localizer.get();

    public EtlproLogicalViewProvider(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
    }

    public Node createLogicalView() {
        return new EtlLogicalViewRootNode();
    }

    public Node findPath(Node root, Object target) {
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = (DataObject) node.getLookup().
                        lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = target instanceof DataObject ? ((DataObject) target).getPrimaryFile() : (FileObject) target;
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);
                    List/*<String>*/ path = Collections.list(
                            new StringTokenizer(relPath, "/")); // NOI18N
                    // XXX see original code for justification
                    path.set(path.size() - 1, targetFO.getName());
                    try {
                        Node found = NodeOp.findPath(node,
                                Collections.enumeration(path));

                        // The code below is fix for #84948. 
                        if (hasObject(found, target)) {
                            return found;
                        }
                        Node parent = found.getParentNode();
                        Children kids = parent.getChildren();
                        children = kids.getNodes();
                        for (Node child : children) {
                            if (hasObject(child, target)) {
                                return child;
                            }
                        }

                    } catch (NodeNotFoundException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        DataObject dataObject = (DataObject) node.getLookup().lookup(
                DataObject.class);
        if (dataObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            if (dataObject.equals(obj)) {
                return true;
            }
            FileObject fileObject = ((DataObject) obj).getPrimaryFile();
            return hasObject(node, fileObject);
        } else if (obj instanceof FileObject) {
            FileObject fileObject = dataObject.getPrimaryFile();
            return obj.equals(fileObject);
        } else {
            return false;
        }
    }

    private static Lookup createLookup(Project project) {
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed(new Object[]{project, rootFolder});

    }
    // Private innerclasses ----------------------------------------------------
    private static final String[] BREAKABLE_PROPERTIES = new String[]{
        IcanproProjectProperties.JAVAC_CLASSPATH,
        IcanproProjectProperties.DEBUG_CLASSPATH,
        IcanproProjectProperties.SRC_DIR,
    };

    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
                new String[]{IcanproProjectProperties.JAVA_PLATFORM});
    }

    /** Filter node containin additional features for the J2SE physical
     */
    private final class EtlLogicalViewRootNode extends AbstractNode {

        private Action brokenLinksAction;
        private boolean broken;

        public EtlLogicalViewRootNode() {
            super(new EtlproViews.LogicalViewChildren(helper, evaluator, project), createLookup(project));
            setIconBaseWithExtension("org/netbeans/modules/etl/project/ui/resources/etlproProjectIcon.gif"); // NOI18N
            setName(ProjectUtils.getInformation(project).getDisplayName());
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
        }

        @Override
        public Action[] getActions(boolean context) {
            EtlproProject pro = (EtlproProject) project;
            String prj_locn = pro.getProjectDirectory().getPath();
            try {
                prj_locn = pro.getProjectDirectory().getFileSystem().getRoot().toString() + prj_locn;
            } catch (FileStateInvalidException ex) {
               // Exceptions.printStackTrace(ex);
            }
            MashupTableWizardIterator.setProjectInfo(pro.getName(), prj_locn, true);
            if (context) {
                return super.getActions(true);
            } else {
                return getAdditionalActions();
            }
        }

        @Override
        public boolean canRename() {
            return true;
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public boolean canCut() {
            return true;
        }

        @Override
        public void setName(String arg0) {
            super.setName(arg0);
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = Sheet.createDefault();
            Sheet.Set set = Sheet.createPropertiesSet();
            String nbBundle8 = "Name";
            String nbBundle9 = "ETL Project Name";
            Property nameProp = new PropertySupport.Name(this,nbBundle8,nbBundle9);
            set.put(nameProp);
            sheet.put(set);
            return sheet;
        }

        // Private methods -------------------------------------------------
        private Action[] getAdditionalActions() {

            ResourceBundle bundle = NbBundle.getBundle(IcanproLogicalViewProvider.class);
            String nbBundle1 = "Build";
            String nbBundle2 = "Clean & Build";
            String nbBundle3 = "Clean";
            String nbBundle4 = "Generate WSDL";
            String nbBundle5 = "Generate Schema...";
            String nbBundle6 = "Redeploy Project";
            String nbBundle7 = "Deploy Project";
            String nbBundle10 = "Generate Bulk Loader";


            List<Action> actions = new ArrayList<Action>(Arrays.asList(
                        CommonProjectActions.newFileAction(),
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, nbBundle1, null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD,nbBundle2 , null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, nbBundle3, null), // NOI18N
                        null,
                        ProjectSensitiveActions.projectCommandAction(EtlproProject.COMMAND_GENWSDL, nbBundle4, null), // NOI18N
                        //ProjectSensitiveActions.projectCommandAction(EtlproProject.COMMAND_SCHEMA, nbBundle5, null), // NOI18N
			SystemAction.get(MasterIndexAction.class),
                        ProjectSensitiveActions.projectCommandAction(EtlproProject.COMMAND_BULK_LOADER,nbBundle10, null), // NOI18N
                        null,
                        SystemAction.get(NewFlatfileDatabaseWizardAction.class),
                        SystemAction.get(NewFlatfileTableAction.class),
                        //SystemAction.get(FlatfileDBViewerAction.class),
                        null,
                        ProjectSensitiveActions.projectCommandAction(IcanproConstants.COMMAND_REDEPLOY, nbBundle6, null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(IcanproConstants.COMMAND_DEPLOY, nbBundle7, null), // NOI18N
                        null,
                        CommonProjectActions.setAsMainProjectAction(),
                        CommonProjectActions.openSubprojectsAction(),
                        CommonProjectActions.closeProjectAction(),
                        null,
                        CommonProjectActions.renameProjectAction(),
                        CommonProjectActions.moveProjectAction(),
                        CommonProjectActions.copyProjectAction(),
                        CommonProjectActions.deleteProjectAction(),
                        null,
                        SystemAction.get(org.openide.actions.FindAction.class)));
            actions.addAll(Utilities.actionsForPath("Projects/Actions"));
            actions.addAll(Arrays.asList(
                        null,
                        SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                        null,
                        brokenLinksAction,
                        CommonProjectActions.customizeProjectAction()));
            return actions.toArray(new Action[actions.size()]);
        }

        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener {

            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                String nbBundle1 = "Resolve Reference Problems...";
                putValue(Action.NAME, nbBundle1);
            }

            public void actionPerformed(ActionEvent e) {
                /*BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{IcanproProjectProperties.JAVA_PLATFORM});
                if (!hasBrokenLinks(helper, resolver)) {
                disable();
                }*/
            }

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
                fireIconChange();
                fireOpenedIconChange();
            }
        }
    }

    /** Factory for project actions.<BR>
     * XXX This class is a candidate for move to org.netbeans.spi.project.ui.support
     */
    public static class Actions {

        private Actions() {
        } // This is a factory

        public static Action createAction(String key, String name, boolean global) {
            return new ActionImpl(key, name, global ? Utilities.actionsGlobalContext() : null);
        }

        private static class ActionImpl extends AbstractAction implements ContextAwareAction {

            Lookup context;
            String name;
            String command;

            public ActionImpl(String command, String name, Lookup context) {
                super(name);
                this.context = context;
                this.command = command;
                this.name = name;
            }

            public void actionPerformed(ActionEvent e) {

                Project project = (Project) context.lookup(Project.class);
                ActionProvider ap = (ActionProvider) project.getLookup().lookup(ActionProvider.class);
                ap.invokeAction(command, context);

            }

            public Action createContextAwareInstance(Lookup lookup) {
                return new ActionImpl(command, name, lookup);
            }
        }
    }
}
