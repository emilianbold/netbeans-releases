/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.workflow.project.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openide.nodes.*;
import org.openide.util.*;
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
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class WorkflowproLogicalViewProvider implements LogicalViewProvider {

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;

    public WorkflowproLogicalViewProvider(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
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
        return new DtelLogicalViewRootNode(new ProjectViews.LogicalViewChildren(helper, evaluator, project));
    }

    public Node findPath( Node root, Object target ) {
        // XXX implement
        return null;
    }

   private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }

    // Private innerclasses ----------------------------------------------------

    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        IcanproProjectProperties.JAVAC_CLASSPATH,
        IcanproProjectProperties.DEBUG_CLASSPATH,
        IcanproProjectProperties.SRC_DIR,
    };

    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
            new String[] {IcanproProjectProperties.JAVA_PLATFORM});
    }

    /** Filter node containin additional features for the J2SE physical
     */
    private final class DtelLogicalViewRootNode extends AbstractNode {

        private Action brokenLinksAction;
        private boolean broken;


        public DtelLogicalViewRootNode(Children children) {
            //super( new WorkflowproViews.LogicalViewChildren( helper, evaluator, project ), createLookup( project ) );
            super(children, createLookup( project ) );
            setIconBaseWithExtension( "org/netbeans/modules/workflow/project/ui/resources/workflowproProjectIcon.gif" ); // NOI18N
            setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
        }

        @Override
        public String getShortDescription() {
            String prjDirDispName =
                    FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(WorkflowproLogicalViewProvider.class,
                    "HINT_Project_Root_Node", prjDirDispName); // NOI18N
        }

        public Action[] getActions( boolean context ) {
            if ( context )
                return super.getActions( true );
            else
                return getAdditionalActions();
        }

        public boolean canRename() {
            return false;
        }

        // Private methods -------------------------------------------------

        private Action[] getAdditionalActions() {

            ResourceBundle bundle = NbBundle.getBundle(IcanproLogicalViewProvider.class);

            return new Action[] {
                // disable new action at the top...
                // CommonProjectActions.newFileAction(),
                // null,
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
                null,
                ProjectSensitiveActions.projectCommandAction( IcanproConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( IcanproConstants.COMMAND_DEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null ), // NOI18N
                null,
                CommonProjectActions.setAsMainProjectAction(),
                CommonProjectActions.openSubprojectsAction(),
                CommonProjectActions.closeProjectAction(),
                CommonProjectActions.renameProjectAction(),
                CommonProjectActions.moveProjectAction(),
                CommonProjectActions.copyProjectAction(),
                CommonProjectActions.deleteProjectAction(),                
                null,
                SystemAction.get( org.openide.actions.FindAction.class ),
                null,
                SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                null,
                brokenLinksAction,
                CommonProjectActions.customizeProjectAction(),
            };
        }

        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener {

            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                putValue(Action.NAME, NbBundle.getMessage(IcanproLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            }

            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{IcanproProjectProperties.JAVA_PLATFORM});
                if (!hasBrokenLinks(helper, resolver)) {
                    disable();
                }
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

        private Actions() {} // This is a factory

        public static Action createAction( String key, String name, boolean global ) {
            return new ActionImpl( key, name, global ? Utilities.actionsGlobalContext() : null );
        }

        private static class ActionImpl extends AbstractAction implements ContextAwareAction {

            Lookup context;
            String name;
            String command;

            public ActionImpl( String command, String name, Lookup context ) {
                super( name );
                this.context = context;
                this.command = command;
                this.name = name;
            }

            public void actionPerformed( ActionEvent e ) {

                Project project = (Project)context.lookup( Project.class );
                ActionProvider ap = (ActionProvider)project.getLookup().lookup( ActionProvider.class);

                ap.invokeAction( command, context );

            }

            public Action createContextAwareInstance( Lookup lookup ) {
                return new ActionImpl( command, name, lookup );
            }
        }

    }
}
