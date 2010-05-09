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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.awt.EventQueue;
import java.awt.Image;
import org.netbeans.modules.javacard.spi.ActionNames;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.actions.Single;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

class JCLogicalViewProvider implements LogicalViewProvider {

    private final RequestProcessor RP =
            new RequestProcessor("JavaCardPhysicalViewProvider.RP"); // NOI18N
    private JCProject project;

    public JCLogicalViewProvider(JCProject project) {
        this.project = project;
    }

    public Node createLogicalView() {
        return new JavaCardLogicalViewRootNode();
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

            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private class JavaCardLogicalViewRootNode extends AbstractNode
            implements ChangeListener, PropertyChangeListener,
            FileStatusListener, Runnable {

        private Set<FileObject> files;
        private ChangeListener sourcesListener;
        private Map<FileSystem, FileStatusListener> fileSystemListeners;
        private Map<SourceGroup, PropertyChangeListener> groupsListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;

        public JavaCardLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project,
                    project.kind().nodeFactoryPath()),
                    Lookups.singleton(project));
            setIconBaseWithExtension(project.kind().iconPath());
            super.setName(ProjectUtils.getInformation(project).getDisplayName());
            setProjectFiles(project);
            project.addChangeListener(this);
        }

        /**
         * If JC Dev Kit home dir is not specified, then the display name of
         * Java Card project gets highlited red.
         */
        @Override
        public String getHtmlDisplayName() {
            if (project.isBadPlatformOrCard()) {
                return "<font color='!nb.errorForeground'>" + getDisplayName();
            }
            return null;
        }

        @Override
        public Image getIcon(int type) {
            Image result = super.getIcon(type);
            if (project.isBadPlatformOrCard()) {
                Image brokenProjectBadge = ImageUtilities.loadImage(
                        "org/netbeans/modules/javacard/resources/brokenProjectBadge.png"); //NOI18N
                result = ImageUtilities.mergeImages(result, brokenProjectBadge,
                        8, 0);
            }
            return result;
        }

        @Override
        public Action[] getActions(boolean context) {
            return getAdditionalActions();
        }

        final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }

        private Action[] getAdditionalActions() {
            ResourceBundle bundle =
                    NbBundle.getBundle(JCLogicalViewProvider.class);

            List<Action> actions = new ArrayList<Action>();

            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionProvider.COMMAND_BUILD,
                    bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionProvider.COMMAND_REBUILD,
                    bundle.getString("LBL_ReBuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionProvider.COMMAND_CLEAN,
                    bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionNames.COMMAND_JC_LOAD,
                    bundle.getString("LBL_JCLoad_Action_Name"), null)); //NOI18N
            if (project.kind().isApplication()) {
                actions.add(ProjectSensitiveActions.projectCommandAction(
                        ActionNames.COMMAND_JC_CREATE,
                        bundle.getString("LBL_JCCreate_Action_Name"), null)); //NOI18N
                actions.add(ProjectSensitiveActions.projectCommandAction(
                        ActionNames.COMMAND_JC_DELETE,
                        bundle.getString("LBL_JCDelete_Action_Name"), null)); //NOI18N
            }
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionNames.COMMAND_JC_UNLOAD,
                    bundle.getString("LBL_JCUnload_Action_Name"), null)); //NOI18N
            actions.add(null);
            if(project.kind().isClassic()) {
                    actions.add(ProjectSensitiveActions.projectCommandAction(
                            ActionNames.COMMAND_JC_GENPROXY,
                            bundle.getString("LBL_JCGenProxy_Action_Name"), null)); //NOI18N
                    actions.add(null);
            }
            if (!project.kind().isLibrary()) {
                actions.add(ProjectSensitiveActions.projectCommandAction(
                        ActionProvider.COMMAND_RUN,
                        bundle.getString("LBL_RunAction_Name"), null)); //NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(
                    ActionProvider.COMMAND_DEBUG,
                    bundle.getString("LBL_DebugAction_Name"), null)); //NOI18N
            }
//            actions.add(CommonProjectActions.setProjectConfigurationAction());
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));

            if (project.isBadPlatformOrCard()) {
                actions.add (new ResolveAction());
            }

//            addFromLayers(actions, "Projects/Actions"); //NOI18N

            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());

            return actions.toArray(new Action[actions.size()]);
        }

        private final void setGroups(Collection<SourceGroup> groups) {
            if (groupsListeners != null) {
                for (Map.Entry<SourceGroup, PropertyChangeListener> e : groupsListeners.entrySet()) {
                    e.getKey().removePropertyChangeListener(e.getValue());
                }
            }
            groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            Set<FileObject> roots = new HashSet<FileObject>();
            for (SourceGroup group : groups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                roots.add(fo);
            }
            setFiles(roots);
        }

        final void setFiles(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                for (Map.Entry<FileSystem, FileStatusListener> e : fileSystemListeners.entrySet()) {
                    e.getKey().removeFileStatusListener(e.getValue());
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
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(
                            this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }

        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof SourceGroup) {
                setProjectFiles(project);
            }
        }

        // sources change or platform change
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof Sources) {
                RP.post(new Runnable() {
                    public void run() {
                        setProjectFiles(project);
                    }
                });
            } else if (e.getSource() instanceof JCProject) {
                //platform or device changed, may have become invalid or
                //valid.  Need to invokeLater to get around
                //ProjectManager.mutex vs. Children.mutex
                EventQueue.invokeLater (new Runnable() {
                    public void run() {
                        fireIconChange();
                        fireDisplayNameChange(null, getDisplayName());
                    }
                });
            }
        }

        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RP.create(this);
            }

            synchronized (privateLock) {
                if ((iconChange == false && event.isIconChange()) || (nameChange == false && event.isNameChange())) {
                    for (FileObject fo : files) {
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }

            task.schedule(100); // batch by 50 ms
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
    }

    private static final class ResolveAction extends Single<JCProject> {
        ResolveAction() {
            super (JCProject.class, NbBundle.getMessage(ResolveAction.class,
                    "RESOLVE_BROKEN_PLATFORM_ACTION"), null); //NOI18N
        }

        @Override
        protected void actionPerformed(JCProject target) {
            target.showSelectPlatformAndDeviceDialog();
        }

    }
}
