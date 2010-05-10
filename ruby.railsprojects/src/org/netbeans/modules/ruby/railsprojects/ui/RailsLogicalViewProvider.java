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

import java.awt.EventQueue;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ruby.railsprojects.GenerateAction;
import org.netbeans.modules.ruby.railsprojects.RailsActionProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.codecoverage.RubyCoverageProvider;
import org.netbeans.modules.ruby.railsprojects.MigrateAction;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.plugins.PluginAction;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.IrbAction;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.TestActionConfiguration;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.bundler.BundlerSupport;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunnerAction;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;
import org.netbeans.modules.ruby.rubyproject.ui.RubyBaseLogicalViewProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectEvent;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectListener;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
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
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;


/**
 * Logical view provider for Rails project.
 */
public final class RailsLogicalViewProvider extends RubyBaseLogicalViewProvider {

    private static final RequestProcessor requestProcessor = new RequestProcessor("Rails Annotation"); // NOI18N
    
    public RailsLogicalViewProvider(
            final RailsProject project,
            final UpdateHelper helper,
            final PropertyEvaluator evaluator,
            final ReferenceHelper resolver) {
        super(project, helper, evaluator, resolver);
    }
    
    public Node createLogicalView() {
        return new RailsLogicalViewRootNode();
    }

    @Override
    protected Node findWithPathFinder(final Node root, final FileObject target) {
        TreeRootNode.PathFinder pf2 = root.getLookup().lookup(TreeRootNode.PathFinder.class);
        if (pf2 != null) {
            Node n = pf2.findPath(root, target);
            if (n != null) {
                return n;
            }
        }
        return null;
    }

    /** Filter node containin additional features for the Rails physical. */
    private final class RailsLogicalViewRootNode extends AbstractNode
            implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {

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
            super(NodeFactorySupport.createCompositeChildren(getProject(), "Projects/org-netbeans-modules-ruby-railsprojects/Nodes"),  // NOI18N
                  Lookups.singleton(getProject()));
            setIconBaseWithExtension("org/netbeans/modules/ruby/railsprojects/ui/resources/rails.png"); // NOI18N
            super.setName( ProjectUtils.getInformation( getProject() ).getDisplayName() );
            setProjectFiles(getProject());
            getUpdateHelper().getRakeProjectHelper().addRakeProjectListener(new RakeProjectListener() {
                public void configurationXmlChanged(RakeProjectEvent ev) {
                    fireShortDescriptionChange();
                }

                public void propertiesChanged(RakeProjectEvent ev) {
                    fireShortDescriptionChange();
                }
            });
            this.rspecSupport = new RSpecSupport(getProject());
        }

        private void fireShortDescriptionChange() {
            // cf. issue #149066
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    fireShortDescriptionChange(null, null);
                }
            });
        }

        public @Override String getShortDescription() {
            String platformDesc = RubyPlatform.platformDescriptionFor(getProject());
            if (platformDesc == null) {
                platformDesc = NbBundle.getMessage(RailsLogicalViewProvider.class, "RailsLogicalViewProvider.PlatformNotFound");
            }
            String dirName = FileUtil.getFileDisplayName(getProject().getProjectDirectory());
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
        
        public @Override Image getIcon(int type) {
            Image img = super.getIcon(type);
            
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
            //return broken || illegalState ? ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
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
                task = requestProcessor.create(this);
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
            setProjectFiles(getProject());
            fireShortDescriptionChange();
        }
        
        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(getProject());
        }
        
        public @Override Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }
        
        public @Override boolean canRename() {
            return true;
        }
        
        public @Override void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(getProject(), s);
        }
        
        public @Override HelpCtx getHelpCtx() {
            return new HelpCtx(RailsLogicalViewRootNode.class);
        }
        
        // Private methods -------------------------------------------------------------
        
        private Action[] getAdditionalActions() {

            bundlerSupport.initialize();
            
            ResourceBundle bundle = NbBundle.getBundle(RailsLogicalViewProvider.class);
            
            List<Action> actions = new ArrayList<Action>();
            
            actions.add(SystemAction.get(GenerateAction.class));
            actions.add(null);
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(SystemAction.get(RakeRunnerAction.class));
            actions.add(SystemAction.get(IrbAction.class));
            actions.add(SystemAction.get(MigrateAction.class));
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RAILS_CONSOLE, bundle.getString("LBL_ConsoleAction_Name"), null)); // NOI18N
            actions.add(SystemAction.get(PluginAction.class));
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            if (AutoTestSupport.isInstalled(getProject(), TestType.AUTOTEST) 
                    && TestActionConfiguration.enable(RailsActionProvider.COMMAND_AUTOTEST, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_AUTOTEST, bundle.getString("LBL_AutoTest"), null)); // NOI18N
            }
            if (AutoTestSupport.isInstalled(getProject(), TestType.AUTOSPEC)
                    && TestActionConfiguration.enable(RailsActionProvider.COMMAND_AUTOSPEC, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_AUTOSPEC, bundle.getString("LBL_AutoSpec"), null)); // NOI18N
            }
            if (rspecSupport.isRSpecInstalled()
                    && TestActionConfiguration.enable(RailsActionProvider.COMMAND_RSPEC, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RailsActionProvider.COMMAND_RSPEC, bundle.getString("LBL_RSpec"), null)); // NOI18N
            }

            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
            if (TestActionConfiguration.enable(RailsActionProvider.COMMAND_TEST, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
            }

            if (bundlerSupport.installed()) {
                actions.add(bundlerSupport.createAction());
            }
            actions.add(RubyCoverageProvider.createCoverageAction(getProject()));
            actions.add(null);

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
            actions.add(null);
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N

            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return actions.toArray(new Action[actions.size()]);
        }

        public @Override String toString() {
            return super.toString() + "[project=" + getProject() + "]"; // NOI18N
        }
    }
}
