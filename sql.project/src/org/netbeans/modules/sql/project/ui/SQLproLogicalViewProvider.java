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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.sql.project.SQLproProject;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.modules.compapp.projects.base.ui.IcanproLogicalViewProvider;
import org.netbeans.modules.sql.project.wsdl.GenFiles;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class SQLproLogicalViewProvider implements LogicalViewProvider {

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;


    public SQLproLogicalViewProvider(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
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
        return new DtelLogicalViewRootNode();
    }

    public Node findPath( Node root, Object target ) {
        // XXX implement
        return null;
    }

   private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> roots = new ArrayList<SourceGroup>();
        SourceGroup[] javaRoots = 
            sources.getSourceGroups(SQLproProject.SOURCES_TYPE_ICANPRO);
        roots.addAll(Arrays.asList(javaRoots));
        if (roots.isEmpty()) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots.addAll(Arrays.asList(sourceGroups));
        }
        DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
        rootFolder = folder != null ? folder : rootFolder;
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
       // return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
           // new String[] {IcanproProjectProperties.JAVA_PLATFORM});
		   return false;
    }

    /** Filter node containin additional features for the J2SE physical
     */
    private final class DtelLogicalViewRootNode extends AbstractNode {

        private Action brokenLinksAction;
        private boolean broken;


        public DtelLogicalViewRootNode() {
            super( new SQLproViews.LogicalViewChildren( helper, evaluator ), createLookup( project ) );
            setIconBaseWithExtension( "org/netbeans/modules/sql/project/ui/resources/sqlproProjectIcon.gif" ); // NOI18N
            setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
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

            ResourceBundle bundle = NbBundle.getBundle(SQLproLogicalViewProvider.class);

			List<Action> actions = new ArrayList<Action>();

            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
                actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null )); // NOI18N
                actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null )); // NOI18N
                actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null )); // NOI18N
//                null,
//                ProjectSensitiveActions.projectCommandAction( IcanproConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null ), // NOI18N
//                ProjectSensitiveActions.projectCommandAction( IcanproConstants.COMMAND_DEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null ), // NOI18N
               // actions.add(ProjectSensitiveActions.projectCommandAction( IcanproConstants.POPULATE_CATALOG, bundle.getString( "LBL_Populate_Catalog" ), null )); // NOI18N
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
				generateWSDL(actions);
				actions.add(null);
                actions.add(SystemAction.get( org.openide.actions.FindAction.class ));
                actions.addAll(Utilities.actionsForPath("Projects/Actions"));
                actions.add(null);
                actions.add(brokenLinksAction);
                actions.add(CommonProjectActions.customizeProjectAction());
            
            return actions.toArray(new Action[actions.size()]);

         /*   return new Action[] {
                // disable new action at the top...
                 CommonProjectActions.newFileAction(),
                 null,
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
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
                generateWSDL(),
                null,
                actions.add(SystemAction.get(org.openide.actions.FindAction.class),
                // add versioning support
                addFromLayers(actions, "Projects/Actions"),
                actions.add(null),
                actions.add(brokenLinksAction),
                actions.add(CommonProjectActions.customizeProjectAction())
            )};*/
        }

        private void generateWSDL(List<Action> actions) {
        	Object genFiles = SystemAction.findObject(GenFiles.class, true);
        	try {
	        	Object[] params =  new Object[1];
	        	params[0]= project;
	        	Class[] cls =  new Class[1];
	        	cls[0] = Project.class;
	        	params[0]= project;
	        	Method meth=GenFiles.class.getMethod("setProject", cls);
	        	meth.invoke(genFiles, params);
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        	actions.add((Action)genFiles);
        	//genFiles.setProject(project);
           // return (Action)genFiles;
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
            /*    BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{IcanproProjectProperties.JAVA_PLATFORM});
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
