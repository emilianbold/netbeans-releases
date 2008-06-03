/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.MigrateAction;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.plugins.PluginAction;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunnerAction;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectEvent;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectListener;
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
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
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
    private List<ChangeListener> changeListeners;
    
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
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        if (target instanceof FileObject) {
            FileObject targetFO = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(targetFO);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] rootChildren = root.getChildren().getNodes(true);
            for (int i = 0; i < rootChildren.length; i++) {
                TreeRootNode.PathFinder pf2 = rootChildren[i].getLookup().lookup(TreeRootNode.PathFinder.class);
                if (pf2 != null) {
                    Node n =  pf2.findPath(rootChildren[i], target);
                    if (n != null) {
                        return n;
                    }
                }
                FileObject childFO = rootChildren[i].getLookup().lookup(DataObject.class).getPrimaryFile();
                if (targetFO.equals(childFO)) {
                    return rootChildren[i];
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
        RailsProjectProperties.JAVAC_CLASSPATH,
        RailsProjectProperties.RUN_CLASSPATH,
        RailsProjectProperties.DEBUG_CLASSPATH,
        RailsProjectProperties.RUN_TEST_CLASSPATH,
        RailsProjectProperties.DEBUG_TEST_CLASSPATH,
        RailsProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    
//    private static Image brokenProjectBadge = Utilities.loadImage("org/netbeans/modules/ruby/railsprojects/ui/resources/brokenProjectBadge.gif", true);
    
    /** Filter node containin additional features for the Ruby physical
     */
    private final class RailsLogicalViewRootNode extends AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        
        private Set<FileObject> files;
        private Map<FileSystem, FileStatusListener> fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map<SourceGroup, PropertyChangeListener> groupsListeners;
        private final RSpecSupport rspecSupport;

        
        public RailsLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-ruby-railsprojects/Nodes"),  // NOI18N
                  Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/ruby/railsprojects/ui/resources/rails.png"); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            setProjectFiles(project);
            helper.getRakeProjectHelper().addRakeProjectListener(new RakeProjectListener() {
                public void configurationXmlChanged(RakeProjectEvent ev) {
                    fireShortDescriptionChange(null, null);
                }

                public void propertiesChanged(RakeProjectEvent ev) {
                    fireShortDescriptionChange(null, null);
                }
            });
            this.rspecSupport = new RSpecSupport(project);
        }

        public @Override String getShortDescription() {
            String platformDesc = RubyPlatform.platformDescriptionFor(project);
            if (platformDesc == null) {
                platformDesc = NbBundle.getMessage(RailsLogicalViewProvider.class, "RailsLogicalViewProvider.PlatformNotFound");
            }
            String dirName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(RailsLogicalViewProvider.class, "RailsLogicalViewProvider.ProjectTooltipDescription", dirName, platformDesc);
        }

        protected final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }
        
        
        private final void setGroups(Collection<SourceGroup> groups) {
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
            for (SourceGroup group : groups) {
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
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NOI18N
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
//            return broken || illegalState ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
            return original;
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
            fireShortDescriptionChange(null, null);
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
            actions.add(SystemAction.get(RakeRunnerAction.class));
            actions.add(SystemAction.get(MigrateAction.class));
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RAILS_CONSOLE, bundle.getString("LBL_ConsoleAction_Name"), null)); // NOI18N
            actions.add(SystemAction.get(PluginAction.class));
            //actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RDOC, bundle.getString("LBL_RDocAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            if (AutoTestSupport.isInstalled(project)) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_AUTOTEST, bundle.getString("LBL_AutoTest"), null)); // NOI18N
            }
            if (rspecSupport.isRSpecInstalled()) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RSPEC, bundle.getString("LBL_RSpec"), null)); // NOI18N
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
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return (Action[]) actions.toArray(new Action[actions.size()]);
        }

        public @Override String toString() {
            return super.toString() + "[project=" + project + "]"; // NOI18N
        }
    }
}
