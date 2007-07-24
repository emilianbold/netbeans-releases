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

package org.netbeans.modules.ruby.railsprojects.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.netbeans.modules.ruby.railsprojects.GenerateAction;
import org.netbeans.modules.ruby.railsprojects.RailsActionProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.railsprojects.MigrateAction;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.UpdateHelper;
import org.netbeans.modules.ruby.railsprojects.plugins.PluginAction;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.RakeTargetsAction;
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
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
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

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class RailsLogicalViewProvider implements LogicalViewProvider {
    
    //private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("RubyPhysicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final RailsProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    private List changeListeners;
    
    public RailsLogicalViewProvider(RailsProject project, UpdateHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
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
        return new RailsLogicalViewRootNode();
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
        Project project = (Project) root.getLookup().lookup(Project.class);
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
                TreeRootNode.PathFinder pf2 = (TreeRootNode.PathFinder) nodes[i].getLookup().lookup(TreeRootNode.PathFinder.class);
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
            this.changeListeners = new ArrayList();
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
     * Used by RailsProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
     */
    public void testBroken() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (this.changeListeners == null) {
                return;
            }
            _listeners = (ChangeListener[]) this.changeListeners.toArray(
                    new ChangeListener[this.changeListeners.size()]);
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
        RailsProjectProperties.JAVAC_CLASSPATH,
        RailsProjectProperties.RUN_CLASSPATH,
        RailsProjectProperties.DEBUG_CLASSPATH,
        RailsProjectProperties.RUN_TEST_CLASSPATH,
        RailsProjectProperties.DEBUG_TEST_CLASSPATH,
        RailsProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    
    private static Image brokenProjectBadge = Utilities.loadImage("org/netbeans/modules/ruby/railsprojects/ui/resources/brokenProjectBadge.gif", true);
    
    /** Filter node containin additional features for the Ruby physical
     */
    private final class RailsLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        private Set files;
        private Map fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map groupsListeners;
        
        public RailsLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-ruby-railsprojects/Nodes"),  // NOI18N
                  Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/ruby/railsprojects/ui/resources/rails.png"); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
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
                roots.add(fo);
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
            if (files == null) {
                return;
            }
            
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
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
//        public String getHtmlDisplayName() {
//            String dispName = super.getDisplayName();
//            try {
//                dispName = XMLUtil.toElementContent(dispName);
//            } catch (CharConversionException ex) {
//                return dispName;
//            }
//            // XXX text colors should be taken from UIManager, not hard-coded!
//            //return broken || illegalState ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
//            return null;
//        }
        
        public Image getIcon(int type) {
            Image img = getMyIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        private Image getMyIcon(int type) {
            Image original = super.getIcon(type);
//            return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
            return original;
        }
        
        public Image getOpenedIcon(int type) {
            Image img = getMyOpenedIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        private Image getMyOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            //return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
            return original;
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
            return new HelpCtx(RailsLogicalViewRootNode.class);
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
        
        @SuppressWarnings("unchecked")
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(RailsLogicalViewProvider.class);
            
            List actions = new ArrayList();
            
            actions.add(SystemAction.get(GenerateAction.class));
            actions.add(null);
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            //actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
            //actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
            //actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            //actions.add(null);
            actions.add(SystemAction.get(RakeTargetsAction.class));
            actions.add(SystemAction.get(MigrateAction.class));
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RAILS_CONSOLE, bundle.getString("LBL_ConsoleAction_Name"), null)); // NOI18N
            actions.add(SystemAction.get(PluginAction.class));
            //actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RDOC, bundle.getString("LBL_RDocAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            if (AutoTestSupport.isInstalled()) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_AUTOTEST, bundle.getString("LBL_AutoTest"), null)); // NOI18N
            }
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
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
            
            try {
                FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Projects/Actions"); // NOI18N
                if (fo != null) {
                    DataObject dobj = DataObject.find(fo);
                    FolderLookup actionRegistry = new FolderLookup((DataFolder)dobj);
                    Lookup.Template query = new Lookup.Template(Object.class);
                    Lookup lookup = actionRegistry.getLookup();
                    Iterator it = lookup.lookup(query).allInstances().iterator();
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof Action) {
                            actions.add(next);
                        } else if (next instanceof JSeparator) {
                            actions.add(null);
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                // data folder for existing fileobject expected
                ErrorManager.getDefault().notify(ex);
            }
            
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return (Action[]) actions.toArray(new Action[actions.size()]);
            
        }
    }
}
