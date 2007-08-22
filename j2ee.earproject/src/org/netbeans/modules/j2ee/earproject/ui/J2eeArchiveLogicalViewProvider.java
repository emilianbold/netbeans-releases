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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.BrokenProjectSupport;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.UpdateHelper;
import org.netbeans.modules.j2ee.earproject.ui.actions.AddModuleAction;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Support for creating logical views.
 */
public class J2eeArchiveLogicalViewProvider implements LogicalViewProvider {
    
    private final EarProject project;
    protected final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    protected final ReferenceHelper resolver;
    private final List<? extends Action> specialActions =
            Collections.singletonList(SystemAction.get(AddModuleAction.class));
    private final AntBasedProjectType abpt;
    
    public J2eeArchiveLogicalViewProvider(EarProject project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver,
            AntBasedProjectType abpt) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
        this.abpt = abpt;
    }
    
    public Node createLogicalView() {
        return new ArchiveLogicalViewRootNode();
    }
    
    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        // XXX this is incorrect - must handle case that target instanceof DataObject
        // (since that is likely to be what is in lookup)
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            // trying to find node in docbase
            Node result = findNodeUnderConfiguration(root, fo);
            if (result != null) {
                return result;
            }
            // trying to find node in sources
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = nodes.length-1; i >= 0; i--) {
                result = PackageView.findPath(nodes[i], target);
                if (result!=null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    private Node findNodeUnderConfiguration(Node root, FileObject fo) {
        FileObject rootfo = helper.getAntProjectHelper().resolveFileObject(evaluator.getProperty(EarProjectProperties.META_INF));
        String relPath = FileUtil.getRelativePath(rootfo, fo);
        if (relPath == null) {
            return null;
        }
        int idx = relPath.indexOf('.'); //NOI18N
        if (idx != -1) {
            relPath = relPath.substring(0, idx);
        }
        StringTokenizer st = new StringTokenizer(relPath, "/"); //NOI18N
        Node result = root.getChildren().findChild(J2eeProjectView.CONFIG_FILES_VIEW_NAME);
        while (result != null && st.hasMoreTokens()) {
            result = NodeOp.findChild(result, st.nextToken());
        }
        return result;
    }
    
    private static Lookup createLookup( Project project, AntProjectHelper c ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        Lookup ret = null;
        if (null == c) {
            ret = Lookups.fixed( new Object[] { project, rootFolder });
        } else {
            ret = Lookups.fixed( new Object[] { project, rootFolder, c } );
        }
        return ret;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        EarProjectProperties.JAVAC_CLASSPATH,
        EarProjectProperties.DEBUG_CLASSPATH,
        EarProjectProperties.JAR_CONTENT_ADDITIONAL,
    };
    
    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
                new String[] { EarProjectProperties.JAVA_PLATFORM});
    }
    
    private String getIconBase() {
        IconBaseProvider ibp = project.getLookup().lookup(IconBaseProvider.class);
        return (null == ibp)
                ? "org/netbeans/modules/j2ee/earproject/ui/resources/" // NOI18N
                : ibp.getIconBase();
    }
    
    /** Package private for unit test only. */
    final class ArchiveLogicalViewRootNode extends AbstractNode  implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        
        private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/j2ee/earproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
        
        private Action brokenLinksAction;
        private final BrokenServerAction brokenServerAction;
        private final BrokenProjectSupport brokenProjectSupport;
        private boolean broken;
        
        // icon badging >>>
        private Set<FileObject> files;
        private Map<FileSystem, FileStatusListener> fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map<SourceGroup, PropertyChangeListener> groupsListeners;
        // icon badging <<<
        
        public ArchiveLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-j2ee-earproject/Nodes"), // NOI18N
                    createLookup(project, helper.getAntProjectHelper()));
            setIconBaseWithExtension(getIconBase() + "projectIcon.gif"); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper.getAntProjectHelper(), resolver)) {
                broken = true;
            }
            brokenServerAction = new BrokenServerAction();
            J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener(WeakListeners.create(InstanceListener.class, brokenServerAction, moduleProvider));
            refreshProjectFiles();
            this.brokenProjectSupport = project.getLookup().lookup(BrokenProjectSupport.class);
            this.brokenProjectSupport.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    checkProjectValidity();
                }
            });
        }
        
        private void refreshProjectFiles() {
            setFiles(getProjectFiles());
        }
        
        /** Package private for unit test only. */
        Set<FileObject> getProjectFiles() {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            return getProjectFiles(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }
        
        private Set<FileObject> getProjectFiles(Collection<SourceGroup> groups) {
            if (groupsListeners != null) {
                for (SourceGroup group : groupsListeners.keySet()) {
                    PropertyChangeListener pcl = groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            Set<FileObject> files = new HashSet<FileObject>();
            for (SourceGroup group : groups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject groupRoot = group.getRootFolder();
                if (project.getProjectDirectory().equals(groupRoot)) {
                    // add rather children of project's root folder than the
                    // folder itself (cf. #78994)
                    Enumeration en = project.getProjectDirectory().getChildren(false);
                    while (en.hasMoreElements()) {
                        FileObject child = (FileObject) en.nextElement();
                        if (FileOwnerQuery.getOwner(child) == project) {
                            files.add(child);
                        }
                    }
                } else { // add group's root (may be tweaked if needed)
                    files.add(groupRoot);
                }
            }
            return files;
        }
        
        private final void setFiles(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                for (FileSystem fs : fileSystemListeners.keySet()) {
                    FileStatusListener fsl = fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }
            
            fileSystemListeners = new HashMap<FileSystem, FileStatusListener>();
            this.files = files;
            if (files == null) {
                return;
            }
            
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
                    Exceptions.attachLocalizedMessage(e, "Can not get " + fo + " filesystem, ignoring...");  // NO18N
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
        }
        
        private synchronized void checkProjectValidity() {
            boolean old = broken;
            broken = brokenProjectSupport.hasBrokenArtifacts();
            if (!broken) {
                broken = hasBrokenLinks(helper.getAntProjectHelper(), resolver);
            }
            if (old != broken) {
                getBrokenLinksAction().setEnabled(broken);
                fireIconChange();
                fireOpenedIconChange();
                fireDisplayNameChange(null, null);
            }
        }
        
        public Action getBrokenLinksAction() {
            if (broken && brokenLinksAction == null) {
                brokenLinksAction = new BrokenLinksAction();
            }
            return brokenLinksAction;
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
        
        public Image getIcon(int type) {
            Image img = getMyIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
            
            return img;
        }
        
        public Image getOpenedIcon(int type) {
            Image img = getMyOpenedIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
            
            return img;
        }
        
        public Action[] getActions( boolean context ) {
            return context ? super.getActions(true) : getAdditionalActions();
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
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
            refreshProjectFiles();
        }
        
        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            refreshProjectFiles();
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
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(ArchiveLogicalViewRootNode.class);
        }
        
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(J2eeArchiveLogicalViewProvider.class);
            
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            List<Action> actions = new ArrayList<Action>();
            actions.addAll(specialActions);
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.addAll(Arrays.asList(new Action[] {
                null,
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
            }));
            if (provider != null && provider.hasVerifierSupport()) {
                actions.add(ProjectSensitiveActions.projectCommandAction( "verify", bundle.getString( "LBL_VerifyAction_Name" ), null )); // NOI18N
            }
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( EjbProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null));
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
            actions.add(SystemAction.get( FindAction.class ));
            
            addFromLayers(actions, "Projects/Actions"); //NOI18N

            actions.add(null);
            
            if (broken) {
                actions.add(getBrokenLinksAction());
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
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
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, Runnable {
            private static final long serialVersionUID = 1L;
            
            private RequestProcessor.Task task = null;
            private final PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
                putValue(Action.NAME, NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
            }
            
            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, BREAKABLE_PROPERTIES, new String[]{ EarProjectProperties.JAVA_PLATFORM});
                brokenProjectSupport.adjustReferences();
                checkProjectValidity();
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                // check project state whenever there was a property change
                // or change in list of platforms.
                // Coalesce changes since they can come quickly:
                if (task == null) {
                    task = RequestProcessor.getDefault().create(this);
                }
                task.schedule(100);
            }
            
            public void run() {
                checkProjectValidity();
            }
            
        }
        
        private class BrokenServerAction extends AbstractAction implements InstanceListener, PropertyChangeListener {
            private static final long serialVersionUID = 1L;
            
            private boolean brokenServer;
            
            public BrokenServerAction() {
                putValue(Action.NAME, NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
                evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
                checkMissingServer();
            }
            
            public boolean isEnabled() {
                return brokenServer;
            }
            
            public void actionPerformed(ActionEvent e) {
                EarProjectProperties app = new EarProjectProperties(project, resolver, abpt);
                String j2eeSpec = (String) app.get(EarProjectProperties.J2EE_PLATFORM);
                String instance = BrokenServerSupport.selectServer(j2eeSpec, J2eeModule.EAR);
                if (instance != null) {
                    app.put(EarProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    app.store();
                }
                checkMissingServer();
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                if (EarProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
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
                String serverInstanceID = helper.getAntProjectHelper().getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
                brokenServer = BrokenServerSupport.isBroken(serverInstanceID);
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
            private static final long serialVersionUID = 1L;
            
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
                
                Project project = context.lookup( Project.class );
                ActionProvider ap = project.getLookup().lookup( ActionProvider.class);
                
                ap.invokeAction( command, context );
            }
            
            public Action createContextAwareInstance( Lookup lookup ) {
                return new ActionImpl( command, name, lookup );
            }
        }
        
    }
    
}
