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

package org.netbeans.modules.j2ee.clientproject.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectUtil;
import org.netbeans.modules.j2ee.clientproject.SourceRoots;
import org.netbeans.modules.j2ee.clientproject.UpdateHelper;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.CustomizerLibraries;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectWebServicesClientSupport;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
 * @author Petr Hrebejk
 */
public class AppClientLogicalViewProvider implements LogicalViewProvider {
    
    private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("AppClientLogicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final AppClientProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private List<ChangeListener> changeListeners;
    
    // Web service client
    private static final Object KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
    private static final String KEY_SETUP_DIR = "setupDir"; //NOI18N
    private static final String KEY_CONF_DIR = "confDir"; //NOI18N
    
    public AppClientLogicalViewProvider(AppClientProject project, UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
    }
    
    public Node createLogicalView() {
        return new AppClientLogicalViewRootNode();
    }
    
    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
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
    
    
    
    public synchronized void addChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            this.changeListeners = new ArrayList<ChangeListener>();
        }
        this.changeListeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            return;
        }
        this.changeListeners.remove(l);
    }
    
    /**
     * Used by AppClientProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     */
    public void testBroken() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (this.changeListeners == null) {
                return;
            }
            _listeners = this.changeListeners.toArray(new ChangeListener[this.changeListeners.size()]);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (int i=0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(event);
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        AppClientProjectProperties.JAVAC_CLASSPATH,
//        AppClientProjectProperties.RUN_CLASSPATH, take it from target server
        AppClientProjectProperties.DEBUG_CLASSPATH,
        AppClientProjectProperties.RUN_TEST_CLASSPATH,
        AppClientProjectProperties.DEBUG_TEST_CLASSPATH,
        AppClientProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    
    public boolean hasBrokenLinks() {
        return BrokenReferencesSupport.isBroken(helper.getAntProjectHelper(), resolver, getBreakableProperties(),
                new String[] {AppClientProjectProperties.JAVA_PLATFORM});
    }
    
    public boolean hasInvalidJdkVersion() {
        String javaSource = this.evaluator.getProperty("javac.source");     //NOI18N
        String javaTarget = this.evaluator.getProperty("javac.target");    //NOI18N
        if (javaSource == null && javaTarget == null) {
            //No need to check anything
            return false;
        }
        
        final String platformId = this.evaluator.getProperty(AppClientProjectProperties.JAVA_PLATFORM);  //NOI18N
        final JavaPlatform activePlatform = AppClientProjectUtil.getActivePlatform(platformId);
        if (activePlatform == null) {
            return true;
        }
        SpecificationVersion platformVersion = activePlatform.getSpecification().getVersion();
        try {
            return (javaSource != null && new SpecificationVersion(javaSource).compareTo(platformVersion)>0)
            || (javaTarget != null && new SpecificationVersion(javaTarget).compareTo(platformVersion)>0);
        } catch (NumberFormatException nfe) {
            Logger.getLogger("global").log(Level.INFO,
                    "Invalid javac.source: " + javaSource + " or javac.target: " + javaTarget + " of project:" + this.project.getProjectDirectory().getPath()); // NOI18N
            return true;
        }
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
    
    private static Image brokenProjectBadge = Utilities.loadImage("org/netbeans/modules/j2ee/clientproject/ui/resources/brokenProjectBadge.gif", true);
    
    /** Filter node containin additional features for the J2SE physical
     */
    private final class AppClientLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        
        private Action brokenLinksAction;
        private BrokenServerAction brokenServerAction;
        private boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
        private boolean illegalState;   //Represents a state where project is not in legal state, eg invalid source/target level
        
        // icon badging >>>
        private Set<FileObject> files;
        private Map<FileSystem, FileStatusListener> fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map<SourceGroup, PropertyChangeListener> groupsListeners;
        //private Project project;
        // icon badging <<<
        
        public AppClientLogicalViewRootNode() {
            super(new LogicalViewChildren(project, evaluator, helper, resolver), Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/clientproject/ui/resources/appclient.gif"); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks()) {
                broken = true;
            } else if (hasInvalidJdkVersion()) {
                illegalState = true;
            }
            brokenLinksAction = new BrokenLinksAction();
            brokenServerAction = new BrokenServerAction();
            J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener(
                    WeakListeners.create(InstanceListener.class, brokenServerAction, moduleProvider));
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
        
        
        private void setGroups(Collection groups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            Set<FileObject> roots = new HashSet<FileObject>();
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                SourceGroup group = (SourceGroup) it.next();
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                roots.add(fo);
            }
            setFiles(roots);
        }
        
        protected void setFiles(Set<FileObject> files) {
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
            
            Iterator it = files.iterator();
            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
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
                    Logger.getLogger("global").log(Level.INFO, null, Exceptions.attachMessage(e, "Cannot get " + fo + " filesystem, ignoring...")); // NOI18N
                }
            }
        }
        
        @Override
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            // XXX text colors should be taken from UIManager, not hard-coded!
            return broken || illegalState || brokenServerAction.isEnabled() ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        @Override
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
        
        private Image getMyIcon(int type) {
            Image original = super.getIcon(type);
            return broken || illegalState || brokenServerAction.isEnabled()
                   ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0)
                   : original;
        }
        
        @Override
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
        
        private Image getMyOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return broken || illegalState || brokenServerAction.isEnabled()
                   ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0)
                   : original;
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
                if ((iconChange == false && event.isIconChange()) || (nameChange == false && event.isNameChange())) {
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
            
            task.schedule(50); // batch by 50 ms
        }
        
        // sources change
        public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
        }
        
        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
        }
        
        @Override
        public Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }
        
        @Override
        public boolean canRename() {
            return true;
        }
        
        @Override
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        /*
        public boolean canDestroy() {
            return true;
        }
         
        public void destroy() throws IOException {
            System.out.println("Destroy " + project.getProjectDirectory());
            LogicalViews.closeProjectAction().actionPerformed(new ActionEvent(this, 0, ""));
            project.getProjectDirectory().delete();
        }
         */
        
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(AppClientLogicalViewProvider.class);
            
            List<Action> actions = new ArrayList<Action>(30);
            
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            if (provider != null && provider.hasVerifierSupport()) {
                actions.add(ProjectSensitiveActions.projectCommandAction( "verify", bundle.getString( "LBL_VerifyAction_Name" ), null )); // NOI18N
            }
            actions.add(ProjectSensitiveActions.projectCommandAction(JavaProjectConstants.COMMAND_JAVADOC, bundle.getString("LBL_JavadocAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(EjbProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
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
            actions.add(SystemAction.get(FindAction.class));
            
            // honor 57874 contact
 
            Lookup lookup = Lookups.forPath("Projects/Actions"); // NOI18N
            Lookup.Template query = new Lookup.Template(Object.class);
            Iterator it = lookup.lookup(query).allInstances().iterator();
            if (it.hasNext()) {
                actions.add(null);
            }
            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof Action) {
                    actions.add((Action) next);
                } else if (next instanceof JSeparator) {
                    actions.add(null);
                }
            }
            
            actions.add(null);
            if (brokenLinksAction != null && brokenLinksAction.isEnabled()) {
                actions.add(brokenLinksAction);
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());

            return actions.toArray(new Action[actions.size()]);
        }
        
        private boolean isBroken() {
            return this.broken;
        }
        
        private void setBroken(boolean broken) {
            this.broken = broken;
            brokenLinksAction.setEnabled(broken);
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        private void setIllegalState(boolean illegalState) {
            this.illegalState = illegalState;
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, ChangeListener, Runnable {
            private static final long serialVersionUID = 1L;
            
            private RequestProcessor.Task task = null;
            
            private final PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                putValue(Action.NAME, NbBundle.getMessage(AppClientLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                setEnabled(broken);
                evaluator.addPropertyChangeListener(this);
                // When evaluator fires changes that platform properties were
                // removed the platform still exists in JavaPlatformManager.
                // That's why I have to listen here also on JPM:
                weakPCL = WeakListeners.propertyChange(this, JavaPlatformManager.getDefault());
                JavaPlatformManager.getDefault().addPropertyChangeListener(weakPCL);
                AppClientLogicalViewProvider.this.addChangeListener(WeakListeners.change(this, AppClientLogicalViewProvider.this));
            }
            
            public void actionPerformed(ActionEvent e) {
                try {
                    helper.requestSave();
                    BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, getBreakableProperties(), new String[] {AppClientProjectProperties.JAVA_PLATFORM});
                    run();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                refsMayChanged();
            }
            
            
            public void stateChanged(ChangeEvent evt) {
                refsMayChanged();
            }
            
            public synchronized void run() {
                boolean old = AppClientLogicalViewRootNode.this.broken;
                boolean broken = hasBrokenLinks();
                if (old != broken) {
                    setBroken(broken);
                }
                
                old = AppClientLogicalViewRootNode.this.illegalState;
                broken = hasInvalidJdkVersion();
                if (old != broken) {
                    setIllegalState(broken);
                }
            }
            
            private void refsMayChanged() {
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
            private static final long serialVersionUID = 1L;

            private RequestProcessor.Task task = null;
            private boolean brokenServer;
            
            public BrokenServerAction() {
                putValue(Action.NAME, NbBundle.getMessage(AppClientLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
                evaluator.addPropertyChangeListener(this);
                checkMissingServer();
            }
            
            @Override
            public boolean isEnabled() {
                return brokenServer;
            }

            public void actionPerformed(ActionEvent e) {
                String j2eeSpec = AppClientProjectProperties.getProperty(
                        AppClientProjectProperties.J2EE_PLATFORM,
                        helper.getAntProjectHelper(),
                        AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String instance = BrokenServerSupport.selectServer(j2eeSpec, J2eeModule.CLIENT);
                if (instance != null) {
                    AppClientProjectProperties.setServerInstance(
                            project, helper.getAntProjectHelper(), instance);
                }
                checkMissingServer();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (AppClientProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
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
                String servInstID = AppClientProjectProperties.getProperty(AppClientProjectProperties.J2EE_SERVER_INSTANCE, helper.getAntProjectHelper(), AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                brokenServer = BrokenServerSupport.isBroken(servInstID);
                if (old != brokenServer) {
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
            }
        }        
    }
    
    private static final class LogicalViewChildren extends Children.Keys<Object> implements ChangeListener {
        
        private static final Object LIBRARIES = "Libs"; //NOI18N
        private static final Object TEST_LIBRARIES = "TestLibs"; //NOI18N
        private static final String WSDL_FOLDER=AppClientProjectWebServicesClientSupport.WSDL_FOLDER;
        
        private final Project project;
        private final PropertyEvaluator evaluator;
        private final UpdateHelper helper;
        private final ReferenceHelper resolver;
        private final SourceRoots testSources;
        
        private final WsdlCreationListener wsdlListener;
        private final MetaInfListener metaInfListener;
        private FileObject wsdlFolder;
        private Car jp;
        
        public LogicalViewChildren(AppClientProject project, PropertyEvaluator evaluator, UpdateHelper helper, ReferenceHelper resolver) {
            this.project = project;
            this.evaluator = evaluator;
            this.helper = helper;
            this.resolver = resolver;
            this.testSources = project.getTestSourceRoots();
            this.metaInfListener = new MetaInfListener();
            this.wsdlListener = new WsdlCreationListener();
            Car jps[] = Car.getCars(project);
            assert jps.length > 0;
            jp = jps[0];
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            getSources().addChangeListener(this);
            
            AntProjectHelper projectHelper = helper.getAntProjectHelper();
            String prop = evaluator.getProperty(AppClientProjectProperties.META_INF); //NOI18N
            if (prop!=null) {
                FileObject metaInf = projectHelper.resolveFileObject(prop);
                if (metaInf!=null) {
                    metaInf.addFileChangeListener(metaInfListener);
                }
            }
            prop = evaluator.getProperty(AppClientProjectProperties.SRC_DIR); //NOI18N
            if (prop!=null) {
                FileObject srcDir = projectHelper.resolveFileObject(prop);
                if (srcDir!=null) {
                    srcDir.addFileChangeListener(metaInfListener);
                }
            }
            
            //XXX: Not very nice, the wsdlFolder should be hold by this class because it listens on it
            WebServicesClientSupport wsClientSupportImpl = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            try {
                if (wsClientSupportImpl != null) {
                    wsdlFolder = wsClientSupportImpl.getWsdlFolder(false);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (wsdlFolder != null) {
                wsdlFolder.addFileChangeListener(wsdlListener);
            }
            setKeys(getKeys());
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<Object>emptySet());
            getSources().removeChangeListener(this);
            
            AntProjectHelper projectHelper = helper.getAntProjectHelper();
            String prop = evaluator.getProperty(AppClientProjectProperties.META_INF); //NOI18N
            if (prop!=null) {
                FileObject metaInf = projectHelper.resolveFileObject(prop);
                if (metaInf!=null) {
                    metaInf.addFileChangeListener(metaInfListener);
                }
            }
            prop = evaluator.getProperty(AppClientProjectProperties.SRC_DIR); //NOI18N
            if (prop!=null) {
                FileObject srcDir = projectHelper.resolveFileObject(prop);
                if (srcDir!=null) {
                    srcDir.removeFileChangeListener(metaInfListener);
                }
            }
            if (wsdlFolder != null) {
                wsdlFolder.removeFileChangeListener(wsdlListener);
            }
            
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Node[] result;
            if (key == LIBRARIES) {
                //Libraries Node
                result = new Node[] {
                    new LibrariesNode(
                            NbBundle.getMessage(AppClientLogicalViewProvider.class,"CTL_LibrariesNode"),
                            project,
                            evaluator,
                            helper,
                            resolver,
                            AppClientProjectProperties.RUN_CLASSPATH,
                            new String[] {AppClientProjectProperties.BUILD_CLASSES_DIR},
                            AppClientProjectProperties.JAVA_PLATFORM, // NOI18N
                            AppClientProjectProperties.J2EE_SERVER_INSTANCE,
                            new Action[] {
                        LibrariesNode.createAddProjectAction(project, AppClientProjectProperties.JAVAC_CLASSPATH,
                                                         ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES),
                        LibrariesNode.createAddLibraryAction(project, helper.getAntProjectHelper(), AppClientProjectProperties.JAVAC_CLASSPATH,
                                                         ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES),
                        LibrariesNode.createAddFolderAction(project, AppClientProjectProperties.JAVAC_CLASSPATH,
                                                         ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES),
                        null,
                        new PreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE) // NOI18N
                        },
                    ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES)
                };
            } else if (key == TEST_LIBRARIES) {
                result = new Node[] {
                    new LibrariesNode(
                            NbBundle.getMessage(AppClientLogicalViewProvider.class,"CTL_TestLibrariesNode"),
                            project,
                            evaluator,
                            helper,
                            resolver,
                            AppClientProjectProperties.RUN_TEST_CLASSPATH,
                            new String[] {
                        AppClientProjectProperties.BUILD_TEST_CLASSES_DIR,
                        AppClientProjectProperties.JAVAC_CLASSPATH,
                        AppClientProjectProperties.BUILD_CLASSES_DIR,
                            },
                            null,
                            null,
                            new Action[] {
                        LibrariesNode.createAddProjectAction(project, AppClientProjectProperties.JAVAC_TEST_CLASSPATH, null),
                        LibrariesNode.createAddLibraryAction(project, helper.getAntProjectHelper(), AppClientProjectProperties.JAVAC_TEST_CLASSPATH, null),
                        LibrariesNode.createAddFolderAction(project, AppClientProjectProperties.JAVAC_TEST_CLASSPATH, null),
                        null,
                        new PreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE_TESTS), // NOI18N
                            },
                            null
                    ),
                };
            }
            // else if (key instanceof SourceGroup) {
            else if (key instanceof SourceGroupKey) {
                //Source root
                //result = new Node[] {new PackageViewFilterNode(((SourceGroupKey) key).group, project)};
                result = new Node[] {PackageView.createPackageView(((SourceGroupKey) key).group)};
            } else if (key == KEY_SETUP_DIR) {
                result = new Node[] {J2eeProjectView.createServerResourcesNode(project)};
            } else if (key == KEY_CONF_DIR) {
                result = new Node[] {J2eeProjectView.createConfigFilesView(jp.getMetaInf())};
            } else if (key == KEY_SERVICE_REFS) {
                java.util.Map<String, String> properties = ((AppClientProject) project).getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
                String serverInstance = properties.get(AppClientProjectProperties.J2EE_SERVER_INSTANCE);
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                
                Car wm = Car.getCar(project.getProjectDirectory());
                result = null;
                if (wm!=null && (J2eeModule.JAVA_EE_5.equals(wm.getJ2eePlatformVersion()))) {
                    JAXWSClientView view = JAXWSClientView.getJAXWSClientView();
                    result = view == null ? new Node[0] : new Node[] {view.createJAXWSClientView(project)};
                } else {             
                    FileObject clientRoot = project.getProjectDirectory();
                    WebServicesClientView clientView = WebServicesClientView.getWebServicesClientView(clientRoot);
                    if (clientView != null) {
                        WebServicesClientSupport wss = WebServicesClientSupport.getWebServicesClientSupport(clientRoot);
                        if (wss!=null) {
                            FileObject wsdlFolder = wss.getWsdlFolder();
                            if (wsdlFolder!=null) {
                                FileObject[] children = wsdlFolder.getChildren();
                                boolean foundWsdl = false;
                                for (int i=0;i<children.length;i++) {
                                    if (children[i].getExt().equalsIgnoreCase(WSDL_FOLDER)) { //NOI18N
                                        foundWsdl=true;
                                        break;
                                    }
                                }
                                if (foundWsdl) {
                                    result = new Node[] {clientView.createWebServiceClientView(wsdlFolder)};
                                }
                            }
                        }
                    }
                }
            } else {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }
        
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setKeys(getKeys());
                }
            });
        }
        
        // Private methods -----------------------------------------------------
        
        private Collection<Object> getKeys() {
            //#60800, #61584 - when the project is deleted externally do not try to create children, the source groups
            //are not valid
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.<Object>emptyList();
            }
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            
            List<Object> result =  new ArrayList<Object>(groups.length);
            result.add(KEY_CONF_DIR);
            result.add(KEY_SETUP_DIR);
            for( int i = 0; i < groups.length; i++ ) {
                result.add(new SourceGroupKey(groups[i]));
            }
            result.add(LIBRARIES);
            URL[] testRoots = this.testSources.getRootURLs();
            boolean addTestSources = false;
            for (int i = 0; i < testRoots.length; i++) {
                File f = new File(URI.create(testRoots[i].toExternalForm()));
                if (f.exists()) {
                    addTestSources = true;
                    break;
                }
            }
            if (addTestSources) {
                result.add(TEST_LIBRARIES);
            }
            
            //show the ws client node iff there are some clients
            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
            JAXWSClientSupport jwcss = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
            if ((jwcss != null) && (jaxWsModel != null) &&  (jaxWsModel.getClients().length > 0)) {
                result.add(KEY_SERVICE_REFS);
            } else {
                WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
                List wsClients = wscs.getServiceClients();
                if ((wsClients != null)  && (!wsClients.isEmpty())) {
                    result.add(KEY_SERVICE_REFS);
                }
            }
            
            return result;
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }
        
        private static class SourceGroupKey {
            
            public final SourceGroup group;
            public final FileObject fileObject;
            
            SourceGroupKey(SourceGroup group) {
                this.group = group;
                this.fileObject = group.getRootFolder();
            }
            
            @Override
            public int hashCode() {
                return fileObject.hashCode();
            }
            
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof SourceGroupKey)) {
                    return false;
                } else {
                    SourceGroupKey otherKey = (SourceGroupKey) obj;
                    String thisDisplayName = this.group.getDisplayName();
                    String otherDisplayName = otherKey.group.getDisplayName();
                    // XXX what is the operator binding order supposed to be here??
                    return fileObject.equals(otherKey.fileObject) &&
                            thisDisplayName == null ? otherDisplayName == null : thisDisplayName.equals(otherDisplayName);
                }
            }
            
        }
        
        private final class WsdlCreationListener extends FileChangeAdapter {
            
            @Override
            public void fileDataCreated(FileEvent fe) {
                if (WSDL_FOLDER.equalsIgnoreCase(fe.getFile().getExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                }
            }
            
            @Override
            public void fileDeleted(FileEvent fe) {
                if (WSDL_FOLDER.equalsIgnoreCase(fe.getFile().getExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                } else if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                }
            }
        }
        
        private final class MetaInfListener extends FileChangeAdapter {
            
            @Override
            public void fileFolderCreated(FileEvent fe) {
                if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    fe.getFile().addFileChangeListener(wsdlListener);
                } else if (fe.getFile().isFolder() && "META-INF".equals(fe.getFile().getName())) { //NOI18N
                    fe.getFile().addFileChangeListener(metaInfListener);
                }
            }
            
            @Override
            public void fileDeleted(FileEvent fe) {
                if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    fe.getFile().removeFileChangeListener(wsdlListener);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            refreshKey(KEY_SERVICE_REFS);
                        }
                    });
                } else if (fe.getFile().isFolder() && "META-INF".equals(fe.getFile().getName())) { //NOI18N
                    fe.getFile().removeFileChangeListener(metaInfListener);
                }
            }
        }
    }
    
    /** Yet another cool filter node just to add properties action
     */
    private static class PackageViewFilterNode extends FilterNode {
        
        private final String nodeName;
        private final Project project;
        
        Action[] actions;
        
        public PackageViewFilterNode(SourceGroup sourceGroup, Project project) {
            super(PackageView.createPackageView(sourceGroup));
            this.project = project;
            this.nodeName = "Sources"; // NOI18N
        }
        
        
        @Override
        public Action[] getActions(boolean context) {
            if (!context) {
                if (actions == null) {
                    Action superActions[] = super.getActions(context);
                    actions = new Action[superActions.length + 2];
                    System.arraycopy(superActions, 0, actions, 0, superActions.length);
                    actions[superActions.length] = null;
                    actions[superActions.length + 1] = new PreselectPropertiesAction(project, nodeName);
                }
                return actions;
            } else {
                return super.getActions(context);
            }
        }
        
    }
    
    
    /** The special properties action
     */
    private static class PreselectPropertiesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        private final Project project;
        private final String nodeName;
        private final String panelName;
        
        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }
        
        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(AppClientLogicalViewProvider.class, "LBL_Properties_Action"));
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }
        
        public void actionPerformed(ActionEvent e) {
            // J2SECustomizerProvider cp = (J2SECustomizerProvider) project.getLookup().lookup(J2SECustomizerProvider.class);
            CustomizerProviderImpl cp = project.getLookup().lookup(CustomizerProviderImpl.class);
            if (cp != null) {
                cp.showCustomizer(nodeName, panelName);
            }
            
        }
    }
    
}
