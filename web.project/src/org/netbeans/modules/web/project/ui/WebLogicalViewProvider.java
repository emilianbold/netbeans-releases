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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
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
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.common.ui.BrokenDatasourceSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.SourceRoots;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class WebLogicalViewProvider implements LogicalViewProvider {

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
        FileObject rootfo = helper.getAntProjectHelper().resolveFileObject(evaluator.getProperty (WebProjectProperties.WEB_DOCBASE_DIR));
        String relPath = FileUtil.getRelativePath(rootfo, fo);
        if (relPath == null) {
            return null;
        }
        int idx = relPath.lastIndexOf('.'); //NOI18N
        if (idx != -1)
            relPath = relPath.substring(0, idx);
        StringTokenizer st = new StringTokenizer(relPath, "/"); //NOI18N
        Node result = NodeOp.findChild(root,rootfo.getName());
        while (st.hasMoreTokens()) {
            result = NodeOp.findChild(result, st.nextToken());
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
    
    // Private innerclasses ----------------------------------------------------

    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        WebProjectProperties.JAVAC_CLASSPATH,
        WebProjectProperties.DEBUG_CLASSPATH,
        WebProjectProperties.RUN_TEST_CLASSPATH, 
        WebProjectProperties.DEBUG_TEST_CLASSPATH, 
        WebProjectProperties.JAVAC_TEST_CLASSPATH,
        WebProjectProperties.WAR_CONTENT_ADDITIONAL,
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
    
    private static Image brokenProjectBadge = Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/brokenProjectBadge.gif" ); // NOI18N
    
    /** Filter node containin additional features for the J2SE physical
     */
    private final class WebLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {

        private final Action brokenLinksAction;
        private final BrokenServerAction brokenServerAction;
        private final BrokenDatasourceAction brokenDatasourceAction;
        private boolean broken;

        // icon badging >>>
        private Set<FileObject> files;
        private Map<FileSystem,FileStatusListener> fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;        
        private ChangeListener sourcesListener;
        private Map<SourceGroup,PropertyChangeListener> groupsListeners;
        // icon badging <<<

        
        public WebLogicalViewRootNode() {
//            super( new WebViews.LogicalViewChildren( project, helper, evaluator, resolver ), createLookup( project ) );
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-web-project/Nodes"), 
                  Lookups.singleton(project));
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
            setProjectFiles(project);
        }

        protected void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {                
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);                                
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }


        private void setGroups(Collection<SourceGroup> groups) {
            if (groupsListeners != null) {
                for (Map.Entry<SourceGroup,PropertyChangeListener> e : groupsListeners.entrySet()) {
                    e.getKey().removePropertyChangeListener(e.getValue());
                }
            }
            groupsListeners = new HashMap<SourceGroup,PropertyChangeListener>();
            Set<FileObject> roots = new HashSet<FileObject>();
            for (SourceGroup group : groups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                if (project.getProjectDirectory().equals(fo)) {
                    // add rather children of project's root folder than the
                    // folder itself (cf. #78994)
                    Enumeration en = project.getProjectDirectory().getChildren(false);
                    while (en.hasMoreElements()) {
                        FileObject child = (FileObject) en.nextElement();
                        if (FileOwnerQuery.getOwner(child) == project) {
                            roots.add(child);
                        }
                    }
                } else {
                    roots.add(fo);
                }
            }
            setFiles(roots);
        }

        protected void setFiles(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                for (Map.Entry<FileSystem,FileStatusListener> e : fileSystemListeners.entrySet()) {
                    e.getKey().removeFileStatusListener(e.getValue());
                }
            }
                        
            fileSystemListeners = new HashMap<FileSystem,FileStatusListener>();
            this.files = files;
            if (files == null)
                return;

            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
            for (FileObject fo : files) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(Exceptions.attachMessage(e, "Cannot get " + fo + " filesystem, ignoring...")); // NO18N
                }
            }
        }
        
        public Image getIcon (int type) {
            Image img = getMyIcon (type);

            if (files != null && !files.isEmpty()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }

            return img;
        }

        public Image getOpenedIcon (int type) {
            Image img = getMyOpenedIcon(type);

            if (files != null && !files.isEmpty()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }

            return img;
        }
        
        @Override                                                                                                            
        public String getShortDescription() {                                                                                
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());                              
            return NbBundle.getMessage(WebLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName); // NO18N             
        }   

        public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
        }

        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }

            synchronized (privateLock) {
                if ((!iconChange && event.isIconChange())  || (!nameChange && event.isNameChange())) {
                    for (FileObject fo : files) {
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }

            task.schedule(50);  // batch by 50 ms
        }

        // sources change
        public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
        }

        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
        }
        
        public Image getMyIcon( int type ) {
            Image original = super.getIcon( type );                
            return broken || brokenServerAction.isEnabled() || brokenDatasourceAction.isEnabled() ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }

        public Image getMyOpenedIcon( int type ) {
            Image original = super.getOpenedIcon(type);                
            return broken || brokenServerAction.isEnabled() || brokenDatasourceAction.isEnabled() ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;            
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

            addFromLayers(actions, "Projects/Profiler_Actions_temporary"); //NOI18N
            
            actions.add(ProjectSensitiveActions.projectCommandAction( RestSupport.COMMAND_TEST_RESTBEANS, bundle.getString( "LBL_TestRestBeansAction_Name" ), null )); // NOI18N
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
            
            addFromLayers(actions, "Projects/Actions"); //NOI18N
            
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
        
        private void addFromLayers(List<Action> actions, String path) {
            Lookup look = Lookups.forPath(path);
            for (Object next : look.lookupAll(Object.class)) {
                if (next instanceof Action) {
                    actions.add((Action) next);
                } else if (next instanceof JSeparator) {
                    actions.add(null);
                }
            }
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
                    helper.requestSave();
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
                    project.getWebProjectProperties().save();
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
                String j2eeSpec = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).
                        getProperty(WebProjectProperties.J2EE_PLATFORM);
                String instance = BrokenServerSupport.selectServer(j2eeSpec, J2eeModule.WAR);
                if (instance != null) {
                    WebProjectProperties.setServerInstance(
                            project, helper, instance);
                }
                checkMissingServer();
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
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
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
                AuxiliaryConfiguration ac = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class);
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
