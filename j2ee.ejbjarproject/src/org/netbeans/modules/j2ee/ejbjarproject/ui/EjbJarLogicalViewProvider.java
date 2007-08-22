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

package org.netbeans.modules.j2ee.ejbjarproject.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;

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
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;


import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class EjbJarLogicalViewProvider implements LogicalViewProvider {
    private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("EjbJarLogicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final EjbJarProject project;
    private final AntProjectHelper helper;    
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
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
        Project project = root.getLookup().lookup(Project.class);
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
            return findNodeInConfigFiles(root, fo);
        }

        return null;
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
        Node configFiles = root.getChildren().findChild(J2eeProjectView.CONFIG_FILES_VIEW_NAME); // NOI18N
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
     * Used by EjbJarProjectCustomizer to mark the project as broken when it warns user
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
    private final class WebLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {

        private Action brokenLinksAction;
        private BrokenServerAction brokenServerAction;
        private boolean broken;
        private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
        
        // icon badging >>>
        private Set files;
        private Map fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map groupsListeners;
        // icon badging <<<
        
        public WebLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-j2ee-ejbjarproject/Nodes"), // NOI18N
                    createLookup(project));
            setIconBase( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon" ); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );            
            if (hasBrokenLinks()) {
                broken = true;
            }
            brokenLinksAction = new BrokenLinksAction();            
            brokenServerAction = new BrokenServerAction();
            J2eeModuleProvider moduleProvider = 
                           project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener((InstanceListener)WeakListeners.create(
                        InstanceListener.class, brokenServerAction, moduleProvider));
            setProjectFiles(project);
        }
        
        protected final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {                
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);                                
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }


        private final void setGroups(Collection groups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap();
            Set roots = new HashSet();
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                SourceGroup group = (SourceGroup) it.next();
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

        protected final void setFiles(Set files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
                    FileStatusListener fsl = (FileStatusListener) fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }
                        
            fileSystemListeners = new HashMap();
            this.files = files;
            if (files == null) return;

            Iterator it = files.iterator();
            Set hookedFileSystems = new HashSet();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
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
                    Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(e, "Can not get " + fo + " filesystem, ignoring...")); // NO18N
                }
            }
        }
        
        public java.awt.Image getIcon (int type) {
            java.awt.Image img = getMyIcon (type);

            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem ().getStatus ().annotateIcon (img, type, files);
                } catch (FileStateInvalidException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }

            return img;
        }

        public java.awt.Image getOpenedIcon (int type) {
            java.awt.Image img = getMyOpenedIcon(type);

            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem ().getStatus ().annotateIcon (img, type, files);
                } catch (FileStateInvalidException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }

            return img;
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
                if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
                    Iterator it = files.iterator();
                    while (it.hasNext()) {
                        FileObject fo = (FileObject) it.next();
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
        
        public Image getMyIcon(int type) {
            Image original = super.getIcon( type );                
            return broken || brokenServerAction.isEnabled() 
                    ? Utilities.mergeImages(original, Utilities.loadImage(BROKEN_PROJECT_BADGE), 8, 0) 
                    : original;
        }

        public Image getMyOpenedIcon(int type) {
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

            ResourceBundle bundle = NbBundle.getBundle(EjbJarLogicalViewProvider.class);
            
            J2eeModuleProvider provider = 
                           project.getLookup().lookup(J2eeModuleProvider.class);
            List<Action> actions = new ArrayList<Action>(30);
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
            actions.add(ProjectSensitiveActions.projectCommandAction( EjbProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null )); // NOI18N
            addFromLayers(actions, "Projects/Profiler_Actions_temporary"); //NOI18N
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
            
            // honor 57874 contract
            
            addFromLayers(actions, "Projects/Actions"); //NOI18N
            
            actions.add(null);
            
            if (brokenLinksAction != null && brokenLinksAction.isEnabled()) {
                actions.add(brokenLinksAction);
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());

            return actions.toArray(new javax.swing.Action[actions.size()]);
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

            private PropertyChangeListener weakPCL;

            public BrokenLinksAction() {
                putValue(Action.NAME, NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                setEnabled(broken);
                evaluator.addPropertyChangeListener(this);
                
                // When evaluator fires changes that platform properties were
                // removed the platform still exists in JavaPlatformManager.
                // That's why I have to listen here also on JPM:
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );                
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
                EjbJarLogicalViewProvider.this.addChangeListener ((ChangeListener)WeakListeners.change(this, EjbJarLogicalViewProvider.this));
            }

            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper, resolver, getBreakableProperties(), new String[]{EjbJarProjectProperties.JAVA_PLATFORM});
                run();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                refsMayChanged();
            }
            
            public void stateChanged (ChangeEvent evt) {
                refsMayChanged ();
            }                       
            
            public synchronized void run() {
                boolean old = broken;
                broken = hasBrokenLinks();
                if (old != broken) {
                    setEnabled(broken);
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                    //to refresh library references in private.properties
                    new EjbJarProjectProperties (project, updateHelper, evaluator, resolver).save();
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
                String j2eeSpec = EjbJarProjectProperties.getProperty(
                        EjbJarProjectProperties.J2EE_PLATFORM,
                        helper,
                        AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String instance = BrokenServerSupport.selectServer(j2eeSpec, J2eeModule.EJB);
                if (instance != null) {
                    EjbJarProjectProperties.setServerInstance(
                            project, helper, instance);
                }
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
                
                Project project = context.lookup(Project.class);
                ActionProvider ap = 
                               project.getLookup().lookup(ActionProvider.class); 
                
                ap.invokeAction( command, context );
                                
            }            
            
            public Action createContextAwareInstance( Lookup lookup ) {
                return new ActionImpl( command, name, lookup );
            }
        }
        
    }

}
