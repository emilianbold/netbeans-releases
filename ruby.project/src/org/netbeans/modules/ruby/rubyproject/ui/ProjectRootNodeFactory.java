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

package org.netbeans.modules.ruby.rubyproject.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

public class ProjectRootNodeFactory implements NodeFactory {
    
    public NodeList createNodes(Project p) {
        RubyProject project = p.getLookup().lookup(RubyProject.class);
        assert project != null;
        return new RootChildren(project);
    }
    
    private static class RootChildren implements NodeList<RootChildNode>, ChangeListener {
        
        private final FileChangeListener rootFOListener;
        private final RubyProject project;
        private final List<ChangeListener> changeListeners;
        
        public RootChildren(RubyProject proj) {
            rootFOListener = new RootFileChangeListener();
            FileObject prjRoot = proj.getProjectDirectory();
            prjRoot.addFileChangeListener(WeakListeners.create(FileChangeListener.class, rootFOListener, prjRoot));
            changeListeners = new CopyOnWriteArrayList<ChangeListener>();
            project = proj;
        }
        
        public List<RootChildNode> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.emptyList();
            }
            
            // source roots
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
            // Here we're adding sources, tests
            List<RootChildNode> result =  new ArrayList<RootChildNode>();
            for( int i = 0; i < groups.length; i++ ) {
                result.add(new RootChildNode(groups[i]));
            }
            
            // files under project's root
            result.addAll(getRootFiles());
            return result;
        }
        
        /** Returns nodes representing files under project's root. */
        private List<? extends RootChildNode> getRootFiles() {
            FileObject rootDir = project.getProjectDirectory();
            List<RootChildNode> rootFiles =  new ArrayList<RootChildNode>();
            FileObject rakeFile = RakeSupport.findRakeFile(project);
            if (rakeFile != null && rootDir.equals(rakeFile.getParent())) {
                rootFiles.add(new RootChildNode(rakeFile));
            }
            for (FileObject rootChild : rootDir.getChildren()) {
                if (rootChild.isFolder() || RakeSupport.isRakeFile(rootChild)) {
                    continue;
                }
                rootFiles.add(new RootChildNode(rootChild));
            }
            return rootFiles;
        }
        
        public void addChangeListener(ChangeListener l) {
            changeListeners.add(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            changeListeners.remove(l);
        }
        
        private void fireChange() {
            for (ChangeListener changeListener : changeListeners) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }

        public Node node(RootChildNode key) {
            if (key.group == null) {
                try {
                    if (RakeSupport.isRakeFile(key.fileObject)) {
                        return new RakeSupport.RakeNode(key.fileObject);
                    } else {
                        DataObject dobj = DataObject.find(key.fileObject);
                        return new FilterNode(dobj.getNodeDelegate());
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return new FolderViewFilterNode(key.group, project);
        }

        public void addNotify() {
            getSources().addChangeListener(this);
        }
        
        public void removeNotify() {
            getSources().removeChangeListener(this);
        }
        
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }

        final class RootFileChangeListener extends FileChangeAdapter {

            public @Override void fileFolderCreated(FileEvent fe) {
                stateChanged(null);
            }

            public @Override void fileDataCreated(FileEvent fe) {
                stateChanged(null);
            }

            public @Override void fileDeleted(FileEvent fe) {
                stateChanged(null);
            }

            public @Override void fileRenamed(FileRenameEvent fe) {
                stateChanged(null);
            }
        }
    }

    private static class RootChildNode {
        
        private final SourceGroup group;
        private final FileObject fileObject;
        
        RootChildNode(SourceGroup group) {
            this.group = group;
            this.fileObject = group.getRootFolder();
        }
        
        RootChildNode(FileObject fileObject) {
            this.group = null;
            this.fileObject = fileObject;
        }
        
        public @Override int hashCode() {
            return fileObject.hashCode();
        }
        
        public @Override boolean equals(Object obj) {
            if (!(obj instanceof RootChildNode)) {
                return false;
            } else {
                RootChildNode otherKey = (RootChildNode) obj;
                String thisDisplayName = group == null ? null : group.getDisplayName();
                String otherDisplayName = otherKey.group == null ? null : otherKey.group.getDisplayName();
                // XXX what is the operator binding order supposed to be here??
                return fileObject.equals(otherKey.fileObject) &&
                        (thisDisplayName == null ? otherDisplayName == null : thisDisplayName.equals(otherDisplayName));
            }
        }

        public @Override String toString() {
            return "ProjectRootNodeFactory[fileObject: " + fileObject + ", group: " + group + "]"; // NOI18N
        }
        
    }
    
    private static class FolderViewFilterNode extends FilterNode {
        
        protected String nodeName;
        private final Project project;
        private Action[] actions;
        
        FolderViewFilterNode(final SourceGroup sourceGroup, final Project project) {
            super(getOriginalNode(sourceGroup));
            this.project = project;
            this.nodeName = "Sources"; // NOI18N
        }

        private static Node getOriginalNode(final SourceGroup group) {
            // Guard condition, if the project is (closed) and deleted but not
            // yet GCed and the view is switched, the source group is not valid.
            if (group == null) {
                return new AbstractNode(Children.LEAF);
            }
            FileObject root = group.getRootFolder();
            // Guard as above
            if (root == null || !root.isValid()) {
                return new AbstractNode(Children.LEAF);
            }
            return new TreeRootNode(group);
        }

        public @Override Action[] getActions(boolean context) {
            if (actions == null) {
                actions = new Action[] {
                    CommonProjectActions.newFileAction(),
                    null,
                    SystemAction.get(FileSystemAction.class),
                    null,
                    SystemAction.get(FindAction.class),
                    null,
                    SystemAction.get(PasteAction.class),
                    null,
                    SystemAction.get(ToolsAction.class),
                    null,
                    new PreselectPropertiesAction(project, nodeName)};
            }
            return actions;
        }
        
    }
    
    /** The special properties action. */
    private static class PreselectPropertiesAction extends AbstractAction {
        
        private final Project project;
        private final String nodeName;
        private final String panelName;
        
        PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
}
        
        PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(ProjectRootNodeFactory.class, "LBL_Properties_Action"));
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }
        
        public void actionPerformed(ActionEvent e) {
            CustomizerProviderImpl cp = project.getLookup().lookup(CustomizerProviderImpl.class);
            if (cp != null) {
                cp.showCustomizer(nodeName, panelName);
            }
            
        }
    }
    
}
