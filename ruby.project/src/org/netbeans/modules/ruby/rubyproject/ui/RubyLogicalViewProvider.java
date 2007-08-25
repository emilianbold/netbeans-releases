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

package org.netbeans.modules.ruby.rubyproject.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ruby.rubyproject.RubyActionProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.RakeTargetsAction;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
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
 * @author Petr Hrebejk
 */
public class RubyLogicalViewProvider implements LogicalViewProvider {
    
    //private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("RubyPhysicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final RubyProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    private List<ChangeListener> changeListeners;
    
    public RubyLogicalViewProvider(RubyProject project, UpdateHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
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
        return new RubyLogicalViewRootNode();
    }
    
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    public ReferenceHelper getRefHelper() {
        return resolver;
    }
    
    public UpdateHelper getUpdateHelper() {
        return helper;
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
                TreeRootNode.PathFinder pf2 = nodes[i].getLookup().lookup(TreeRootNode.PathFinder.class);
                if (pf2 != null) {
                    Node n =  pf2.findPath(nodes[i], target);
                    if (n != null) {
                        return n;
                    }
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
     * Used by RubyProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
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
    
//    private static Lookup createLookup( Project project ) {
//        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
//        // XXX Remove root folder after FindAction rewrite
//        return Lookups.fixed(new Object[] {project, rootFolder});
//    }
    
    
    // Private innerclasses ----------------------------------------------------------------
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        RubyProjectProperties.JAVAC_CLASSPATH,
        RubyProjectProperties.RUN_CLASSPATH,
        RubyProjectProperties.DEBUG_CLASSPATH,
        RubyProjectProperties.RUN_TEST_CLASSPATH,
        RubyProjectProperties.DEBUG_TEST_CLASSPATH,
        RubyProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    
    public boolean hasBrokenLinks () {
//        return BrokenReferencesSupport.isBroken(helper.getRakeProjectHelper(), resolver, getBreakableProperties(),
//                new String[] {RubyProjectProperties.JAVA_PLATFORM});
        return false;
    }
    
    public boolean hasInvalidJdkVersion () {
//        String javaSource = this.evaluator.getProperty("javac.source");     //NOI18N
//        String javaTarget = this.evaluator.getProperty("javac.target");    //NOI18N
//        if (javaSource == null && javaTarget == null) {
//            //No need to check anything
//            return false;
//        }
//        
//        final String platformId = this.evaluator.getProperty("platform.active");  //NOI18N
//        final JavaPlatform activePlatform = RubyProjectUtil.getActivePlatform (platformId);
//        if (activePlatform == null) {
//            return true;
//        }        
//        SpecificationVersion platformVersion = activePlatform.getSpecification().getVersion();
//        try {
//            return (javaSource != null && new SpecificationVersion (javaSource).compareTo(platformVersion)>0)
//                   || (javaTarget != null && new SpecificationVersion (javaTarget).compareTo(platformVersion)>0);
//        } catch (NumberFormatException nfe) {
//            ErrorManager.getDefault().log("Invalid javac.source: "+javaSource+" or javac.target: "+javaTarget+" of project:"
//                +this.project.getProjectDirectory().getPath());
//            return true;
//        }
        return false;
    }
    
//    private String[] getBreakableProperties() {
//        SourceRoots roots = this.project.getSourceRoots();
//        String[] srcRootProps = roots.getRootProperties();
//        roots = this.project.getTestSourceRoots();
//        String[] testRootProps = roots.getRootProperties();
//        String[] result = new String [BREAKABLE_PROPERTIES.length + srcRootProps.length + testRootProps.length];
//        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
//        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
//        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
//        return result;
//    }
    
    private static Image brokenProjectBadge = Utilities.loadImage("org/netbeans/modules/ruby/rubyproject/ui/resources/brokenProjectBadge.gif", true);
    
    /** Filter node containin additional features for the Ruby physical
     */
    private final class RubyLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        
        private Image icon;
        private Lookup lookup;
        //private Action brokenLinksAction;
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
        
        public RubyLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-ruby-rubyproject/Nodes"), 
                  Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/ruby/rubyproject/ui/resources/jruby.png");
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks()) {
                broken = true;
            }
            else if (hasInvalidJdkVersion ()) {
                illegalState = true;
            }
            //brokenLinksAction = new BrokenLinksAction();
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
                for (SourceGroup group : groupsListeners.keySet()) {
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
        
        protected final void setFiles(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
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
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NOI18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        public @Override String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            // XXX text colors should be taken from UIManager, not hard-coded!
            return broken || illegalState ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        public @Override Image getIcon(int type) {
            Image img = getMyIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        private Image getMyIcon(int type) {
            Image original = super.getIcon(type);
            return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }
        
        public @Override Image getOpenedIcon(int type) {
            Image img = getMyOpenedIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        private Image getMyOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
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
        
        public @Override Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }
        
        public @Override boolean canRename() {
            return true;
        }
        
        public @Override void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        public @Override HelpCtx getHelpCtx() {
            return new HelpCtx(RubyLogicalViewRootNode.class);
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
        
        // Private methods -------------------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(RubyLogicalViewProvider.class);
            
            List<Action> actions = new ArrayList<Action>();
            
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(SystemAction.get(RakeTargetsAction.class));
            actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_RDOC, bundle.getString("LBL_RDocAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
            if (AutoTestSupport.isInstalled()) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_AUTOTEST, bundle.getString("LBL_AutoTest"), null)); // NOI18N
            }
            actions.add(CommonProjectActions.setProjectConfigurationAction());
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
            
            Collection<? extends Object> res = Lookups.forPath("Projects/Actions").lookupAll(Object.class); // NOI18N
            if (!res.isEmpty()) {
                actions.add(null);
                for (Object next : res) {
                    if (next instanceof Action) {
                        actions.add((Action) next);
                    } else if (next instanceof JSeparator) {
                        actions.add(null);
                    }
                }
            }

            actions.add(null);
//            if (broken) {
//                actions.add(brokenLinksAction);
//            }
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return actions.toArray(new Action[actions.size()]);
        }
        
//        private boolean isBroken() {
//            return this.broken;
//        }
//        
//        private void setBroken(boolean broken) {
//            this.broken = broken;
//            //brokenLinksAction.setEnabled(broken);
//            fireIconChange();
//            fireOpenedIconChange();
//            fireDisplayNameChange(null, null);
//        }
//        
//        private void setIllegalState (boolean illegalState) {
//            this.illegalState = illegalState;
//            fireIconChange();
//            fireOpenedIconChange();
//            fireDisplayNameChange(null, null);
//        }
//        
//        /** This action is created only when project has broken references.
//         * Once these are resolved the action is disabled.
//         */
//        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, ChangeListener, Runnable {
//            
//            private RequestProcessor.Task task = null;
//            
//            private PropertyChangeListener weakPCL;
//            
//            public BrokenLinksAction() {
//                putValue(Action.NAME, NbBundle.getMessage(RubyLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
//                setEnabled(broken);
//                evaluator.addPropertyChangeListener(this);
//                // When evaluator fires changes that platform properties were
//                // removed the platform still exists in JavaPlatformManager.
//                // That's why I have to listen here also on JPM:
//                weakPCL = WeakListeners.propertyChange(this, JavaPlatformManager.getDefault());
//                JavaPlatformManager.getDefault().addPropertyChangeListener(weakPCL);
//                RubyLogicalViewProvider.this.addChangeListener((ChangeListener) WeakListeners.change(this, RubyLogicalViewProvider.this));
//            }
//            
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    helper.requestSave();
//                    BrokenReferencesSupport.showCustomizer(helper.getRakeProjectHelper(), resolver, getBreakableProperties(), new String[] {RubyProjectProperties.JAVA_PLATFORM});
//                    run();
//                } catch (IOException ioe) {
//                    ErrorManager.getDefault().notify(ioe);
//                }
//            }
//            
//            public void propertyChange(PropertyChangeEvent evt) {
//                refsMayChanged();
//            }
//            
//            
//            public void stateChanged(ChangeEvent evt) {
//                refsMayChanged();
//            }
//            
//            public synchronized void run() {
//                boolean old = RubyLogicalViewRootNode.this.broken;
//                boolean broken = hasBrokenLinks();
//                if (old != broken) {
//                    setBroken(broken);
//                }
//                
//                old = RubyLogicalViewRootNode.this.illegalState;
//                broken = hasInvalidJdkVersion ();
//                if (old != broken) {
//                    setIllegalState(broken);
//                }
//            }
//            
//            private void refsMayChanged() {
//                // check project state whenever there was a property change
//                // or change in list of platforms.
//                // Coalesce changes since they can come quickly:
//                if (task == null) {
//                    task = BROKEN_LINKS_RP.create(this);
//                }
//                task.schedule(100);
//            }
//            
//        }
//        
    }
    
}
