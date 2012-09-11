/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bookmarks.ui;

import java.beans.PropertyVetoException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkManager;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarks;
import org.netbeans.modules.editor.bookmarks.FileBookmarks;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Tree of nodes used for tree view and other purposes.
 *
 * @author Miloslav Metelka
 */
public final class BookmarksNodeTree {

    private Node rootNode;
    
    BookmarksNodeTree() {
    }
    
    public Node rootNode() {
        return rootNode;
    }
    
    public void rebuild(ExplorerManager explorerManager) {
        Node[] projectNodes;
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            Node selNode = getSelectedNode(explorerManager);
            BookmarkInfo selectedBookmark = null;
            ProjectBookmarks selectedProjectBookmarks = null;
            Node selectedProjectNode = null;
            FileBookmarks selectedFileBookmarks = null;
            if (selNode instanceof BookmarkNode) {
                selectedBookmark = ((BookmarkNode)selNode).getBookmarkInfo();
                selectedFileBookmarks = selectedBookmark.getFileBookmarks();
                selectedProjectBookmarks = selectedFileBookmarks.getProjectBookmarks();
            }
            
            List<ProjectBookmarks> loadedProjectBookmarks = lockedBookmarkManager.allLoadedProjectBookmarks();
            List<Node> projectNodeList = new ArrayList<Node>(loadedProjectBookmarks.size());
            for (ProjectBookmarks projectBookmarks : loadedProjectBookmarks) {
                if (projectBookmarks.containsAnyBookmarks()) {
                    FileObject[] sortedFileObjects = lockedBookmarkManager.getSortedFileObjects(projectBookmarks);
                    ProjectBookmarksChildren children = new ProjectBookmarksChildren(projectBookmarks, sortedFileObjects);
                    URI prjURI = projectBookmarks.getProjectURI();
                    Project prj = BookmarkUtils.findProject(prjURI);
                    LogicalViewProvider lvp = (prj != null) ? prj.getLookup().lookup(LogicalViewProvider.class) : null;
                    Node prjNode = (lvp != null) ? lvp.createLogicalView() : null;
                    if (prjNode == null) {
                        prjNode = new AbstractNode(Children.LEAF);
                        prjNode.setDisplayName(children.getProjectDisplayName());
                    }
                    Node n = new FilterNode(prjNode, children) {
                        @Override
                        public boolean canCopy() {
                            return false;
                        }
                        @Override
                        public boolean canCut() {
                            return false;
                        }
                        @Override
                        public boolean canDestroy() {
                            return false;
                        }
                        @Override
                        public boolean canRename() {
                            return false;
                        }
                    };
                    projectNodeList.add(n);
                    if (projectBookmarks == selectedProjectBookmarks) {
                        selectedProjectNode = n;
                    }
                }
            }
            projectNodes = new Node[projectNodeList.size()];
            projectNodeList.toArray(projectNodes);

            // Sort by project's display name
            Arrays.sort(projectNodes, new Comparator<Node>() {
                @Override
                public int compare(Node n1, Node n2) {
                    return ((ProjectBookmarksChildren) n1.getChildren()).getProjectDisplayName().compareTo(
                            ((ProjectBookmarksChildren) n2.getChildren()).getProjectDisplayName());
                }
            });
            Children rootChildren = new Children.Array();
            rootChildren.add(projectNodes);
            rootNode = new AbstractNode(rootChildren);
            if (explorerManager != null) {
                explorerManager.setRootContext(rootNode);
            }
            
            if (selectedProjectNode != null) {
                for (Node fileNodes : selectedProjectNode.getChildren().snapshot()) {
                    FileBookmarksChildren ch = (FileBookmarksChildren) fileNodes.getChildren();
                    if (ch.fileBookmarks == selectedFileBookmarks) {
                        for (Node bookmarkNode : ch.snapshot()) {
                            if (((BookmarkNode)bookmarkNode).getBookmarkInfo() == selectedBookmark) {
                                try {
                                    selNode = bookmarkNode;
                                    if (explorerManager != null) {
                                        explorerManager.setSelectedNodes(new Node[] { bookmarkNode });
                                    }
                                } catch (PropertyVetoException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
            }

            if (selNode == null && projectNodes.length > 0) { // Select first node
                try {
                    if (explorerManager != null) {
                        explorerManager.setSelectedNodes(new Node[] { projectNodes[0] });
                    }
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
    }
    
    public Map<BookmarkInfo,BookmarkNode> bookmark2NodeMap() {
        List<BookmarkNode> bNodes = bookmarkNodes(true);
        Map<BookmarkInfo,BookmarkNode> bookmark2NodeMap =
                new HashMap<BookmarkInfo, BookmarkNode>(bNodes.size() << 1, 0.5f);
        for (BookmarkNode bNode : bNodes) {
            bookmark2NodeMap.put(bNode.getBookmarkInfo(), bNode);
        }
        return bookmark2NodeMap;
    }
    
    public List<BookmarkNode> bookmarkNodes(boolean addBookmarksWindowNode) {
        List<BookmarkNode> bookmarkNodes = new ArrayList<BookmarkNode>();
        if (addBookmarksWindowNode) {
            bookmarkNodes.add(new BookmarkNode(BookmarkInfo.BOOKMARKS_WINDOW));
        }
        collectBookmarkNodes(bookmarkNodes, rootNode);
        return bookmarkNodes;
    }
    
    public Node findFirstBookmarkNode(ProjectBookmarks projectBookmarks, FileObject fo) {
        FileBookmarks fileBookmarks = projectBookmarks.get(fo.toURL());
        if (fileBookmarks != null && fileBookmarks.containsAnyBookmarks()) {
            if (rootNode != null) {
                List<Node> projectNodes = rootNode.getChildren().snapshot();
                for (Node pNode : projectNodes) {
                    ProjectBookmarksChildren pChildren =
                            (ProjectBookmarksChildren) pNode.getChildren();
                    if (pChildren.projectBookmarks == projectBookmarks) {
                        for (Node fNode : pChildren.snapshot()) {
                            FileBookmarksChildren fChildren = (FileBookmarksChildren) fNode.getChildren();
                            if (fChildren.fileBookmarks == fileBookmarks) {
                                List<Node> bNodes = fChildren.snapshot();
                                if (!bNodes.isEmpty()) {
                                    return bNodes.get(0);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void collectBookmarkNodes(List<BookmarkNode> bookmarkNodes, Node n) {
        if (n instanceof BookmarkNode) {
            bookmarkNodes.add((BookmarkNode)n);
        } else {
            for (Node cn : n.getChildren().snapshot()) {
                collectBookmarkNodes(bookmarkNodes, cn);
            }
        }
    }

    private Node getSelectedNode(ExplorerManager explorerManager) {
        if (explorerManager != null) {
            Node[] selectedNodes = explorerManager.getSelectedNodes();
            if (selectedNodes.length > 0) {
                return selectedNodes[0];
            }
        }
        return null;
    }

    static final class ProjectBookmarksChildren extends Children.Keys<FileObject> {
        
        final String projectDisplayName;
        
        final ProjectBookmarks projectBookmarks;
        
        ProjectBookmarksChildren(ProjectBookmarks projectBookmarks, FileObject[] sortedFileObjects) {
            this.projectBookmarks = projectBookmarks;
            URI prjURI = projectBookmarks.getProjectURI();
            Project prj = BookmarkUtils.findProject(prjURI);
            projectDisplayName = (prj != null)
                    ? ProjectUtils.getInformation(prj).getDisplayName()
                    : NbBundle.getMessage (BookmarksView.class, "LBL_NullProjectDisplayName");
            setKeys(sortedFileObjects);
        }
        
        String getProjectDisplayName() {
            return projectDisplayName;
        }

        @Override
        protected Node[] createNodes(FileObject fo) {
            Node foNode;
            try {
                DataObject dob = DataObject.find(fo);
                foNode = dob.getNodeDelegate().cloneNode();
            } catch (DataObjectNotFoundException ex) {
                foNode = new AbstractNode(Children.LEAF);
                foNode.setDisplayName(fo.getNameExt());
            }
            URL url = fo.toURL();
            FileBookmarks urlBookmarks = projectBookmarks.get(url);
            return new Node[]{new FilterNode(foNode, new FileBookmarksChildren(urlBookmarks, fo))};
        }

    }
    
    static final class FileBookmarksChildren extends Children.Array {
        
        final FileBookmarks fileBookmarks;
        
        FileBookmarksChildren(FileBookmarks fileBookmarks, FileObject fo) {
            super(toNodes(fileBookmarks));
            this.fileBookmarks = fileBookmarks;
        }

        public FileBookmarks getFileBookmarks() {
            return fileBookmarks;
        }
        
        @Override
        public boolean remove(Node[] arr) {
            boolean ret = super.remove(arr);
            if (ret) {
                BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
                try {
                    List<BookmarkInfo> removedBookmarks = new ArrayList<BookmarkInfo>(arr.length);
                    for (Node n : arr) {
                        removedBookmarks.add(((BookmarkNode)n).getBookmarkInfo());
                    }
                    lockedBookmarkManager.removeBookmarks(removedBookmarks);
                } finally {
                    lockedBookmarkManager.unlock();
                }
            }
            return ret;
        }
        
        private static List<Node> toNodes(FileBookmarks fb) {
            BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
            try {
                List<BookmarkInfo> bookmarks = fb.getBookmarks();
                List<Node> nodes = new ArrayList<Node>(bookmarks.size());
                for (int i = 0; i < bookmarks.size(); i++) {
                    BookmarkInfo bookmark = bookmarks.get(i);
                    BookmarkNode bookmarkNode = new BookmarkNode(bookmark);
                    nodes.add(bookmarkNode);
                }
                return nodes;
            } finally {
                lockedBookmarkManager.unlock();
            }
        }
        
    }

}
