/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.project.ui.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.common.ui.BrokenDatasourceSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.loaders.DataFolder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class WebLogicalViewProvider implements LogicalViewProvider2 {

    private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("WebLogicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    private static final RequestProcessor BROKEN_DATASOURCE_RP = new RequestProcessor("WebLogicalViewProvider.BROKEN_DATASOURCE_RP"); //NOI18N
    
    private final WebProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public WebLogicalViewProvider(WebProject project, UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
    }

    public Node createLogicalView() {
        return new WebLogicalViewRootNode();
    }

    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null)
            return null;

        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner))
                return null; // Don't waste time if project does not own the fo
            
            // trying to find node in sources
            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null)
                    return result;
            }
            
            // trying to find node in config files
            Node result = findNodeInConfigFiles(root, fo);
            if (result != null)
                return result;
            
            // trying to find node in docbase
            result = findNodeInDocBase(root, fo);
            if (result != null)
                return result;
            
        }
        return null;
    }
    
    private Node findNodeInDocBase(Node root, FileObject fo) {
        String prop = evaluator.getProperty (WebProjectProperties.WEB_DOCBASE_DIR);
        if (prop == null) {
            // if project does not have web document root:
            return null;
        }
        FileObject rootfo = helper.getAntProjectHelper().resolveFileObject(prop);
        String relPath = FileUtil.getRelativePath(rootfo, fo);
        if (relPath == null) {
            return null;
        }
        int idx = relPath.lastIndexOf('/'); //NOI18N
        if (idx != -1)
        {
            relPath = relPath.substring(0, idx);
        }
        else
        {
            relPath = "";
        }

        StringTokenizer st = new StringTokenizer(relPath, "/"); //NOI18N
        Node result = NodeOp.findChild(root,rootfo.getName());
        if (result == null) {
            return null;
        }

        while (st.hasMoreTokens()) {
            result = NodeOp.findChild(result, st.nextToken());
            if (result == null) {
                return null;
            }
        }

        for (Node child : result.getChildren().getNodes(true))
        {
           DataObject dobj = child.getLookup().lookup(DataObject.class);
           if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt()))
           {
               result = child;
               break;
           }
        }
        
        return result;
    }
    
    private Node findNodeInConfigFiles(Node root, FileObject fo) {
        // XXX ugly, some node names contain the extension and other don't
        // so retrieving the node name from the corresp. DataObject
        String nodeName;
        try {
            DataObject dobj = DataObject.find(fo);
            nodeName = dobj.getName();
        } catch (DataObjectNotFoundException e) {
            nodeName = fo.getName();
        }
        Node configFiles = root.getChildren().findChild("configurationFiles"); // NOI18N
        if (configFiles == null) {
            return null;
        }
        return NodeOp.findChild(configFiles, nodeName);
    }
    
    public void addChangeListener (ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener (ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /**
     * Used by WebProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
     */
    public void testBroken () {
        changeSupport.fireChange();
    }
    
    private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }
    // Private innerclasses ----------------------------------------------------

    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        ProjectProperties.JAVAC_CLASSPATH,
        WebProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.RUN_TEST_CLASSPATH,
        WebProjectProperties.DEBUG_TEST_CLASSPATH, 
        ProjectProperties.JAVAC_TEST_CLASSPATH,
        WebProjectProperties.WAR_CONTENT_ADDITIONAL,
        ProjectProperties.ENDORSED_CLASSPATH,
        WebProjectProperties.WEB_DOCBASE_DIR
    };

    public boolean hasBrokenLinks() {
        return BrokenReferencesSupport.isBroken(helper.getAntProjectHelper(), resolver, getBreakableProperties(),
            new String[] {WebProjectProperties.JAVA_PLATFORM});
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
    
    private boolean isDeployOnSaveSupportedAndDisabled() {
        boolean deployOnSaveEnabled = Boolean.valueOf(project.evaluator().getProperty(
                WebProjectProperties.J2EE_DEPLOY_ON_SAVE));
        if (deployOnSaveEnabled) {
            return false;
        }

        boolean deployOnSaveSupported = false;
        try {
            String instanceId = project.evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
            if (instanceId != null) {
                deployOnSaveSupported = Deployment.getDefault().getServerInstance(instanceId)
                        .isDeployOnSaveSupported();
            }
        } catch (InstanceRemovedException ex) {
            // false
        }
        return deployOnSaveSupported;
    }
    
    /** Filter node containin additional features for the J2SE physical
     */
    private final class WebLogicalViewRootNode extends AbstractNode {

        private final Action brokenLinksAction;
        private final BrokenServerAction brokenServerAction;
        private final BrokenDatasourceAction brokenDatasourceAction;
        private boolean broken;
        private boolean deployOnSaveDisabled;

        public WebLogicalViewRootNode() {
//            super( new WebViews.LogicalViewChildren( project, helper, evaluator, resolver ), createLookup( project ) );
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-web-project/Nodes"), 
                  createLookup(project));
            setIconBaseWithExtension("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif"); //NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks()) {
                broken = true;
            }
            brokenLinksAction = new BrokenLinksAction();
            brokenServerAction = new BrokenServerAction();
            brokenDatasourceAction = new BrokenDatasourceAction();
            J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener((InstanceListener)WeakListeners.create(
                        InstanceListener.class, brokenServerAction, moduleProvider));
            deployOnSaveDisabled = isDeployOnSaveSupportedAndDisabled();
        }

        @Override                                                                                                            
        public String getShortDescription() {                                                                                
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());                              
            return NbBundle.getMessage(WebLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName); // NO18N             
        }   

        @Override
        public Image getIcon( int type ) {
            Image original = super.getIcon( type );
            if (broken || brokenServerAction.isEnabled() || brokenDatasourceAction.isEnabled()) {
                return ImageUtilities.mergeImages(original, ProjectProperties.ICON_BROKEN_BADGE.getImage(), 8, 0);
            } else if (deployOnSaveDisabled) {
                return DeployOnSaveUtils.badgeDisabledDeployOnSave(original);
            } else {
                return original;
            }
        }

        @Override
        public Image getOpenedIcon( int type ) {
            Image original = super.getOpenedIcon(type);
            if (broken || brokenServerAction.isEnabled() || brokenDatasourceAction.isEnabled()) {
                return ImageUtilities.mergeImages(original, ProjectProperties.ICON_BROKEN_BADGE.getImage(), 8, 0);
            } else if (deployOnSaveDisabled) {
                return DeployOnSaveUtils.badgeDisabledDeployOnSave(original);
            } else {
                return original;
            }
        }

        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            return broken || brokenServerAction.isEnabled() || brokenDatasourceAction.isEnabled() ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }

        public Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }

        public boolean canRename() {
            return true;
        }
        
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(WebLogicalViewRootNode.class);
        }
        
        // Private methods -------------------------------------------------

        private void setDeployOnSaveDisabled (boolean value) {
            this.deployOnSaveDisabled = value;
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        private Action[] getAdditionalActions() {

            ResourceBundle bundle = NbBundle.getBundle(WebLogicalViewProvider.class);

            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            List<Action> actions = new ArrayList<Action>();
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
            actions.add(ProjectSensitiveActions.projectCommandAction( WebProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null )); // NOI18N
            actions.addAll(Utilities.actionsForPath("Projects/Profiler_Actions_temporary")); //NOI18N
            actions.addAll(Utilities.actionsForPath("Projects/Rest_Actions_holder")); //NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
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
            actions.add(SystemAction.get( org.openide.actions.FindAction.class ));            
            
            // honor 57874 contact
            
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); //NOI18N
            
            actions.add(null);
            
            if (brokenLinksAction != null && brokenLinksAction.isEnabled()) {
                actions.add(brokenLinksAction);
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
            }
            if (brokenDatasourceAction.isEnabled()) {
                actions.add(brokenDatasourceAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return actions.toArray(new Action[actions.size()]);
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, ChangeListener, Runnable {

            private RequestProcessor.Task task = null;

            private final PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                putValue(Action.NAME,NbBundle.getMessage(WebLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                setEnabled(broken);
                evaluator.addPropertyChangeListener( this );
                // When evaluator fires changes that platform properties were
                // removed the platform still exists in JavaPlatformManager.
                // That's why I have to listen here also on JPM:
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );                
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
                WebLogicalViewProvider.this.addChangeListener ((ChangeListener)WeakListeners.change(this,WebLogicalViewProvider.this));
            }

            public void actionPerformed(ActionEvent e) {
                try {
                    helper.requestUpdate();
                    BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, getBreakableProperties(), new String[]{WebProjectProperties.JAVA_PLATFORM});
                    run();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            public void propertyChange(PropertyChangeEvent evt) {
                refsMayChanged();
            }
            
            public void stateChanged (ChangeEvent evt) {
                refsMayChanged ();
            }                       

            public synchronized void run() {
                boolean old = WebLogicalViewRootNode.this.broken;
                broken = hasBrokenLinks();
                if (old != broken) {
                    setEnabled(broken);
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
                old = WebLogicalViewRootNode.this.deployOnSaveDisabled;
                boolean dosDisabled = isDeployOnSaveSupportedAndDisabled();
                if (old != dosDisabled) {
                    setDeployOnSaveDisabled(dosDisabled);
                }                
            }
            
            public void refsMayChanged() {
                // check project state whenever there was a property change
                // or change in list of platforms.
                // Coalesce changes since they can come quickly:
                if (task == null) {
                    task = BROKEN_LINKS_RP.create(this);
                }
                task.schedule(100);
            }

        }
        
        private class BrokenServerAction extends AbstractAction implements 
                    InstanceListener, PropertyChangeListener {

            private boolean brokenServer;
            
            public BrokenServerAction() {
                putValue(Action.NAME,NbBundle.getMessage(WebLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
                evaluator.addPropertyChangeListener(this);
                checkMissingServer();
            }
            
            public boolean isEnabled() {
                return brokenServer;
            }

            public void actionPerformed(ActionEvent e) {
                Profile j2eeProfile = Profile.fromPropertiesString(helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).
                        getProperty(WebProjectProperties.J2EE_PLATFORM));
                if (j2eeProfile == null) {
                    j2eeProfile = Profile.JAVA_EE_5; // NOI18N
                    Logger.getLogger(WebLogicalViewProvider.class.getName()).warning(
                            "project ["+project.getProjectDirectory()+"] is missing "+WebProjectProperties.J2EE_PLATFORM+". " + // NOI18N
                            "default value will be used instead: "+j2eeProfile); // NOI18N
                    updateJ2EESpec(project, project.getAntProjectHelper(), j2eeProfile);
                }
                String instance = BrokenServerSupport.selectServer(j2eeProfile, J2eeModule.Type.WAR);
                if (instance != null) {
                    WebProjectProperties.setServerInstance(
                            project, helper, instance);
                }
                checkMissingServer();
            }

            private void updateJ2EESpec(final Project project, final AntProjectHelper helper, final Profile j2eeProfile) {
                ProjectManager.mutex().postWriteRequest(new Runnable() {
                    public void run() {
                        try {
                            EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            projectProps.put(WebProjectProperties.J2EE_PLATFORM, j2eeProfile.toPropertiesString());
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                if (WebProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
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
                String servInstID = evaluator.getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
                brokenServer = BrokenServerSupport.isBroken(servInstID);
                if (old != brokenServer) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireIconChange();
                            fireOpenedIconChange();
                            fireDisplayNameChange(null, null);
                        }
                    });
                }
            }
        } 
        
        // For checking projects that use database connections to see if these connections are available
        private class BrokenDatasourceAction extends AbstractAction implements Runnable, ConnectionListener {
            private volatile boolean brokenDatasource;
            private RequestProcessor.Task task = null;
            
            public BrokenDatasourceAction() {
                // Create a weak listener so that the connection listener can be GC'd when listener for a project is no longer referenced
                ConnectionManager.getDefault().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getDefault()));
 
                
                // For now make sure BrokenDatasourceAction only applies to visualweb/Creator projects
                // The if-statement here can be expanded to support other types of projects                
                putValue(Action.NAME,
                        NbBundle.getMessage(WebLogicalViewProvider.class,
                        "LBL_Fix_Broken_Datasource_Action")); //NOI18N
                checkMissingDatabaseConnection();                
            }
            
            // Used to check to see if project is a visualweb or Creator project
            private boolean isVisualWebLegacyProject() {
                boolean isLegacyProject = false;
                
                // Check if Web module is a visualweb 5.5.x or Creator project
                AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(project);
                Element auxElement = ac.getConfigurationFragment("creator-data", "http://www.sun.com/creator/ns", true); //NOI18N
                
                // if project is a visualweb or creator project then find out whether it is a legacy project
                if (auxElement != null) {
                    String version = auxElement.getAttribute("jsf.project.version"); //NOI18N
                    if (version != null) {
                        if (!version.equals("4.0")) { //NOI18N
                            isLegacyProject = true;
                        }
                    }
                }
                
                return isLegacyProject;
            }
            
            public boolean isEnabled() {
                return brokenDatasource;
            }
            
            public void actionPerformed(ActionEvent e) {//
                BrokenDatasourceSupport.fixDatasources(project);
                checkMissingDatabaseConnection();
            }
            
            private void checkMissingDatabaseConnection() {
                // Checking for valid database connections for each data source
                // may take awhile, so run in a separate thread
                if (task == null) {
                    task = BROKEN_DATASOURCE_RP.create(this);
                }
                task.schedule(100);
                
            }
            
            public void run() {
                doCheckMissingDatabaseConnection();
            }
            
            /*
             * Badge project node, change font color of display name to red and
             * post an alert dialog if not the first time
             */
            private void doCheckMissingDatabaseConnection() {
                boolean old = brokenDatasource;
                
                // if the project has any broken data sources then set the brokenDatasource flag to true
                if (!BrokenDatasourceSupport.getBrokenDatasources(project).isEmpty()) {
                    brokenDatasource = true;
                } else {
                    brokenDatasource = false;
                }
                
                if (old != brokenDatasource) {
                    // make changes in EDT thread
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setEnabled(brokenDatasource);
                            fireIconChange();
                            fireOpenedIconChange();
                            fireDisplayNameChange(null, null);
                            if (brokenDatasource && !isVisualWebLegacyProject()) {
                                BrokenDatasourceSupport.showAlert();
                            }
                        }
                    });
                }                                
            }
            
            public void connectionsChanged() {
                checkMissingDatabaseConnection();
            }
        }
    }

}
