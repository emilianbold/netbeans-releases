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

package org.netbeans.modules.j2ee.ejbjarproject.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.LogicalViewChildren;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;


import org.netbeans.modules.j2ee.api.common.J2eeProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class EjbJarLogicalViewProvider implements LogicalViewProvider {
    
    private final EjbJarProject project;
    private final AntProjectHelper helper;    
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    
    
    public EjbJarLogicalViewProvider(EjbJarProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.updateHelper = updateHelper;
        assert updateHelper != null;
        this.helper = updateHelper.getAntProjectHelper();
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
    }
        
    public Node createLogicalView() {
        return new WebLogicalViewRootNode();
    }
    
    public Node findPath( Node root, Object target ) {
        Project project = (Project)root.getLookup().lookup( Project.class );
        if ( project == null ) {
            return null;
        }
        
        if ( target instanceof FileObject ) {
            FileObject fo = (FileObject)target;
            Project owner = FileOwnerQuery.getOwner( fo );
            if ( !project.equals( owner ) ) {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] nodes = root.getChildren().getNodes( true );
            for ( int i = 0; i < nodes.length; i++ ) {
                Node result = PackageView.findPath( nodes[i], target );
                if ( result != null ) {
                    return result;
                }
            }
        }

        return null;
    }
            
   private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }

    // Private innerclasses ----------------------------------------------------
   
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        EjbJarProjectProperties.JAVAC_CLASSPATH,
        EjbJarProjectProperties.DEBUG_CLASSPATH,
        EjbJarProjectProperties.RUN_TEST_CLASSPATH, 
        EjbJarProjectProperties.DEBUG_TEST_CLASSPATH, 
        EjbJarProjectProperties.JAVAC_TEST_CLASSPATH,
    };

    public boolean hasBrokenLinks() {
        return BrokenReferencesSupport.isBroken(helper, resolver, getBreakableProperties(), 
            new String[] {EjbJarProjectProperties.JAVA_PLATFORM});
    }
    
    private String[] getBreakableProperties() {
        SourceRoots roots = this.project.getSourceRoots();
        String[] srcRootProps = roots.getRootProperties();
        roots = this.project.getTestSourceRoots();
        String[] testRootProps = roots.getRootProperties();
        String[] result = new String [BREAKABLE_PROPERTIES.length + srcRootProps.length + testRootProps.length];
        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
        return result;
    }        

    /** Filter node containin additional features for the J2SE physical
     */
    private final class WebLogicalViewRootNode extends AbstractNode {

        private Action brokenLinksAction;
        private BrokenServerAction brokenServerAction;
        private boolean broken;
        private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
        
        public WebLogicalViewRootNode() {
            super( new LogicalViewChildren( project, updateHelper, evaluator, resolver ), createLookup( project ) ); 
            setIconBase( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon" ); // NOI18N
            setName( ProjectUtils.getInformation( project ).getDisplayName() );            
            if (hasBrokenLinks()) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
            brokenServerAction = new BrokenServerAction();
            J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener((InstanceListener)WeakListeners.create(
                        InstanceListener.class, brokenServerAction, moduleProvider));
        }
        
        public Image getIcon(int type) {
            Image original = super.getIcon( type );                
            return broken || brokenServerAction.isEnabled() 
                    ? Utilities.mergeImages(original, Utilities.loadImage(BROKEN_PROJECT_BADGE), 8, 0) 
                    : original;
        }

        public Image getOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);                
            return broken || brokenServerAction.isEnabled() 
                    ? Utilities.mergeImages(original, Utilities.loadImage(BROKEN_PROJECT_BADGE), 8, 0) 
                    : original;
        }            

        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                // ignore
            }
            return broken || brokenServerAction.isEnabled() ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
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

            ResourceBundle bundle = NbBundle.getBundle(EjbJarLogicalViewProvider.class);
            
            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            List actions = new ArrayList(30);
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null )); // NOI18N
            if (provider != null && provider.hasVerifierSupport()) {
                actions.add(ProjectSensitiveActions.projectCommandAction( "verify", bundle.getString( "LBL_VerifyAction_Name" ), null )); // NOI18N
            }
            actions.add(ProjectSensitiveActions.projectCommandAction( JavaProjectConstants.COMMAND_JAVADOC, bundle.getString( "LBL_JavadocAction_Name" ), null )); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( J2eeProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null )); // NOI18N
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(SystemAction.get( org.openide.actions.FindAction.class ));
            actions.add(null);
            if (brokenLinksAction != null) {
                actions.add(brokenLinksAction);
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());

            return (Action[])actions.toArray(new Action[actions.size()]);
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener {

            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                putValue(Action.NAME, NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            }

            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper, resolver, getBreakableProperties(), new String[]{EjbJarProjectProperties.JAVA_PLATFORM});
                if (!hasBrokenLinks()) {
                    disable();
                }
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (!broken) {
                    disable();
                    return;
                }
                broken = hasBrokenLinks();
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
        
        private class BrokenServerAction extends AbstractAction implements 
                    InstanceListener, PropertyChangeListener {

            private RequestProcessor.Task task = null;
            private boolean brokenServer;
            
            public BrokenServerAction() {
                putValue(Action.NAME, NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
                evaluator.addPropertyChangeListener(this);
                checkMissingServer();
            }
            
            public boolean isEnabled() {
                return brokenServer;
            }

            public void actionPerformed(ActionEvent e) {
                BrokenServerSupport.showCustomizer(project, helper);
                checkMissingServer();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (EjbJarProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
                    checkMissingServer();
                }
            }
                
            public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
            }

            public void instanceAdded(String serverInstanceID) {
                checkMissingServer();
            }

            public void instanceRemoved(String serverInstanceID) {
                checkMissingServer();
            }

            private void checkMissingServer() {
                boolean old = brokenServer;
                String servInstID = EjbJarProjectProperties.getProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE, helper, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                brokenServer = BrokenServerSupport.isBroken(servInstID);
                if (old != brokenServer) {
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
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
