/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.actions.GrailsServerCommandAction;
import org.netbeans.modules.groovy.grailsproject.actions.GrailsProjectDeleteAction;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
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
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class GrailsLogicalViewProvider implements LogicalViewProvider {
    
    private final GrailsProject project;
    GrailsLogicalViewRootNode rootNode = null;
       
    public GrailsLogicalViewProvider(GrailsProject project) {
        this.project = project;
        }
    
    public Node createLogicalView() {
        if (rootNode == null){
            rootNode = new GrailsLogicalViewRootNode();
        }
        
        return rootNode;
    }
    
    
    //==========================================================================
    
    private final class GrailsLogicalViewRootNode extends AbstractNode implements ChangeListener, FileStatusListener, PropertyChangeListener, Runnable {
        
        private ChangeListener sourcesListener;
        private Map<SourceGroup, PropertyChangeListener> groupsListeners;
        private RequestProcessor.Task task;
        private Set<FileObject> files;
        private Map<FileSystem, FileStatusListener> fileSystemListeners;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        
        public GrailsLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(
                    project,
                    "Projects/org-netbeans-modules-groovy-grailsproject/Nodes"),
                    Lookups.singleton(project)
                    );
            setProjectFiles(project);
            
            String prefix = "";
            
            if(!Utilities.isWindows())
                prefix = File.separator;
                        
            setShortDescription("Grails Project in " + prefix + project.getProjectDirectory().getPath());
        }
        
        public Image getIcon(int type) {
            return Utilities.loadImage("org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png");
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
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

        protected final void setFiles(Set<FileObject> files) {
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

        public void stateChanged(ChangeEvent event) {
            setProjectFiles(project);
        }
        
        public void propertyChange(PropertyChangeEvent event) {
            setProjectFiles(project);
        }

        @Override
        public Action[] getActions(boolean context) {
            return getAdditionalActions();
        }

        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
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
            
            task.schedule(50); // batch by 50 ms
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
        
        private Action[] getAdditionalActions() {
            
            List<Action> actions = new ArrayList<Action>();
            
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(new GrailsServerCommandAction(project));
            actions.add(SystemAction.get(FindAction.class));
            actions.add(new GrailsProjectDeleteAction(project));
            actions.add(CommonProjectActions.customizeProjectAction());
            
            // honor 57874 contact
            addFromLayers(actions, "Projects/Actions"); //NOI18N
            
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
                Node result = TreeRootNode.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

}
