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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarksPersistence;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarks;
import org.netbeans.modules.editor.bookmarks.URLBookmarks;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.swing.etable.ETable;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * View of all currently known bookmarks (from all opened projects).
 *
 * @author Miloslav Metelka
 */
public final class BookmarksView extends TopComponent implements ChangeListener, PropertyChangeListener {
    
    private static final int PREVIEW_PANE_REFRESH_DELAY = 300;
    
    /**
     * Invoked from layer.
     * @return bookmarks view instance.
     */
    public static TopComponent create() {
        return new BookmarksView();
    }
    
    public static ActionListener openAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TopComponent view = WindowManager.getDefault().findTopComponent("bookmarks"); // NOI18N
                if (view == null) {
                    throw new IllegalArgumentException("Cannot find bookmarksView component"); // NOI18N
                }
                view.open();
                view.requestActive();

            }
        };
    }

    private transient boolean treeViewShowing; // Whether viewed as tree or as a table
    private transient JSplitPane splitPane;
    private transient BookmarksTableView tableView;
    private transient BeanTreeView treeView;
    private transient JPanel previewPanel;

    private transient boolean dividerLocationSet;
    
    private transient JToggleButton bookmarksTreeButton;
    private transient JToggleButton bookmarksTableButton;
    
    private transient Timer previewRefreshTimer;
    private transient BookmarkInfo displayedBookmarkInfo;
    
    BookmarksView() {
    }
    
    @Override
    public String getName () {
        return NbBundle.getMessage (BookmarksView.class, "LBL_BookmarksView");
    }

    @Override
    protected String preferredID() {
        return "bookmarks"; // NOI18N
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    @Override
    public String getToolTipText () {
        return NbBundle.getMessage (BookmarksView.class, "LBL_BookmarksViewToolTip");// NOI18N
    }
    
    private void initLayoutAndComponents() {
        if (previewPanel == null) { // Not inited yet
            setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 1;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.weighty = 0.0;
            add(createLeftToolBar(), gridBagConstraints);
            
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setContinuousLayout(true);

            previewPanel = new JPanel();
            previewPanel.setLayout(new GridLayout(1, 1));
            fixScrollPaneinSplitPaneJDKIssue(previewPanel);
            splitPane.setRightComponent(previewPanel);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 1;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(splitPane, gridBagConstraints);

            // Make treeView visible
            // treeViewVisible = false;
            setTreeViewVisible(true);
            
            BookmarksPersistence.get().addChangeListener(
                    WeakListeners.change(this, BookmarksPersistence.get()));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (treeViewShowing) {
            updateTreeRootContext(getActiveExplorerManager());
        }
    }
    
    private void setTreeViewVisible(boolean treeViewVisible) {
        if (treeViewVisible != this.treeViewShowing) {
            this.treeViewShowing = treeViewVisible;
            TreeOrTableContainer container;
            boolean create;
            if (treeViewVisible) {
                create = (treeView == null);
                if (create) {
                    container = new TreeOrTableContainer();
                    container.getExplorerManager().addPropertyChangeListener(this);
                    updateTreeRootContext(container.getExplorerManager());
                    treeView = new BeanTreeView();
                    container.add(treeView);
                    fixScrollPaneinSplitPaneJDKIssue(treeView);
                    treeView.setRootVisible(false);
                    treeView.setDragSource(false);
                    treeView.setDropTarget(false);
                } else {
                    container = (TreeOrTableContainer) treeView.getParent();
                }

            } else { // Table view visible
                create = (tableView == null);
                if (create) {
                    tableView = new BookmarksTableView();
                    // ETable defines "enter" action => change its meaning
                    tableView.getTable().getActionMap().put("enter", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final BookmarkNode selectedNode = (BookmarkNode) getSelectedNode();
                            if (selectedNode != null) {
                                // Post into EDT (otherwise the focus would stay on ETable)
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        selectedNode.openInEditor();
                                    }
                                });
                            }
                        }
                    });
                    fixScrollPaneinSplitPaneJDKIssue(tableView);
                    container = new TreeOrTableContainer();
                    container.add(tableView);
                    updateTableNodes();
                    tableView.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            schedulePaneRefresh();
                        }
                    });
                } else {
                    container = (TreeOrTableContainer) tableView.getParent();
                }
            }
            int dividerLocation = splitPane.getDividerLocation();
            splitPane.setLeftComponent(container);
            splitPane.setDividerLocation(dividerLocation);
            splitPane.validate();
            if (!treeViewVisible && create) {
                updateTableColumnSizes();
            }
            bookmarksTreeButton.setSelected(treeViewVisible);
            bookmarksTableButton.setSelected(!treeViewVisible);
            revalidate();
            requestFocusTreeOrTable();
        }
    }
    
    private ExplorerManager getExplorerManager(JScrollPane treeOrTableView) {
        return ((TreeOrTableContainer)treeOrTableView.getParent()).getExplorerManager();
    }
    
    private void updateTableColumnSizes() {
        ETable table = tableView.getTable();
        Font font = tableView.getFont();
        FontMetrics fm = tableView.getFontMetrics(font);
        int maxCharWidth = fm.getMaxAdvance();
        int editingBorder = 4;
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn nameColumn = columnModel.getColumn(0);
        nameColumn.setPreferredWidth(8 * maxCharWidth + editingBorder); // 8 chars for name

        TableColumn keyColumn = columnModel.getColumn(1);
        keyColumn.setPreferredWidth(1 * maxCharWidth + editingBorder); // single char for key
        keyColumn.setMinWidth(keyColumn.getPreferredWidth());

        TableColumn locationColumn = columnModel.getColumn(2);
        Insets insets = tableView.getBorder().getBorderInsets(tableView);
        int remainingWidth = tableView.getParent().getWidth() - insets.left - insets.right;
        remainingWidth -= 2 * columnModel.getColumnMargin();
        remainingWidth -= nameColumn.getPreferredWidth();
        remainingWidth -= keyColumn.getPreferredWidth();
        locationColumn.setPreferredWidth(remainingWidth); // remaining space for location
    }

    private void updateTreeRootContext(ExplorerManager treeExplorerManager) {
        List<Project> prjs = BookmarksPersistence.get().allProjectsWithBookmarks();
        Node[] nodes = new Node[prjs.size()];
        int i = 0;
        for (Project prj : prjs) {
            ProjectBookmarksChildren children = new ProjectBookmarksChildren(prj);
            LogicalViewProvider lvp = prj.getLookup().lookup(LogicalViewProvider.class);
            Node prjNode = (lvp != null) ? lvp.createLogicalView() : null;
            if (prjNode == null) {
                prjNode = new AbstractNode(Children.LEAF);
                prjNode.setDisplayName(children.getProjectDisplayName());
            }
            nodes[i++] = new FilterNode(prjNode, children);
        }
        // Sort by project's display name
        Arrays.sort(nodes, new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return ((ProjectBookmarksChildren)n1.getChildren()).getProjectDisplayName().compareTo(
                        ((ProjectBookmarksChildren)n2.getChildren()).getProjectDisplayName());
            }
        });
        Children rootChildren = new Children.Array();
        rootChildren.add(nodes);
        Node rootNode = new AbstractNode(rootChildren);
        treeExplorerManager.setRootContext(rootNode);
        updateTableNodes();
    }
    
    private void updateTableNodes() {
        if (tableView != null) {
            List<BookmarkNode> bookmarkNodes = new ArrayList<BookmarkNode>();
            collectBookmarkNodes(bookmarkNodes, getExplorerManager(treeView).getRootContext());
            ((BookmarksTableModel)tableView.getTable().getModel()).setEntries(bookmarkNodes);
        }
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
    
    void requestFocusTreeOrTable() {
        if (treeViewShowing) {
            treeView.requestFocus();
        } else {
            tableView.requestFocus();
        }
        Node selectedNode = getSelectedNode();
        if (selectedNode == null) {
            ExplorerManager explorerManager = getActiveExplorerManager();
            Children rootChildren = explorerManager.getRootContext().getChildren();
            if (rootChildren.getNodesCount() > 0) {
                try {
                    explorerManager.setSelectedNodes(new Node[] { rootChildren.getNodeAt(0) });
                } catch (PropertyVetoException ex) {
                    // Ignored
                }
            }
        }
    }

    void refreshView() {
//        updateTreeRootContext(getExplorerManager(treeView));
        requestFocusTreeOrTable();
    }

    @Override
    protected void componentShowing() {
        // Load all bookmarks from all projects
        BookmarksPersistence.get().ensureAllProjectsBookmarksLoaded();
        initLayoutAndComponents();
        super.componentShowing();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
    }
    
    private void schedulePaneRefresh() {
        if (previewRefreshTimer == null) {
            previewRefreshTimer = new Timer(PREVIEW_PANE_REFRESH_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkShowPreview();
                }
            });
            previewRefreshTimer.setRepeats(false);
        }
        previewRefreshTimer.restart();
    }
    
    void checkShowPreview() {
        Node selectedNode = getSelectedNode();
        if (selectedNode instanceof BookmarkNode) {
            BookmarkNode bmNode = (BookmarkNode) selectedNode;
            final BookmarkInfo bookmarkInfo = bmNode.getUpdatedInfo();
            if (bookmarkInfo != displayedBookmarkInfo) {
                final FileObject fo = bmNode.getFileObject();
                try {
                    DataObject dob = DataObject.find(fo);
                    final EditorCookie ec = dob.getCookie(EditorCookie.class);
                    if (ec != null) {
                        Document doc = ec.getDocument();
                        if (doc == null) {
                            // Open document on background
                            RequestProcessor.getDefault().post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final Document d = ec.openDocument();
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                showPreview(fo, d, bookmarkInfo);
                                            }
                                        });
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            });
                        } else { // doc != null
                            showPreview(fo, doc, bookmarkInfo);
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    // Ignore preview
                }
            }
        }
    }
    
    void showPreview(FileObject fo, Document doc, BookmarkInfo bookmarkInfo) {
        if (bookmarkInfo != displayedBookmarkInfo) {
            int lineIndex = bookmarkInfo.getLineIndex();
            String mimeType = (String) doc.getProperty("mimeType");
            if (mimeType != null) {
                JEditorPane pane = new JEditorPane();
                EditorKit editorKit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
                pane.setEditorKit(editorKit);
                pane.setDocument(doc);
                Component editorComponent;
                EditorUI editorUI = Utilities.getEditorUI(pane);
                if (editorUI != null) {
                    editorComponent = editorUI.getExtComponent();
                } else {
                    editorComponent = new JScrollPane(pane);
                }
                previewPanel.removeAll();
                previewPanel.add(editorComponent);

                int offset = BookmarksPersistence.lineIndex2Offset(doc, lineIndex);
                pane.setCaretPosition(offset);
                displayedBookmarkInfo = bookmarkInfo;
                
                previewPanel.revalidate();
            }
        }
    }
    
    private ExplorerManager getActiveExplorerManager() {
        return getExplorerManager(treeViewShowing ? treeView : tableView);
    }
    
    Node getSelectedNode() {
        Node selectedNode = null;
        if (treeViewShowing) {
            ExplorerManager explorerManager = getActiveExplorerManager();
            Node[] selectedNodes = explorerManager.getSelectedNodes();
            selectedNode = (selectedNodes.length > 0) ? selectedNodes[0] : null;
        } else {
            int selectedRowIndex = tableView.getTable().getSelectedRow();
            if (selectedRowIndex != -1) {
                selectedNode = ((BookmarksTableModel)tableView.getTable().getModel()).getEntry(selectedRowIndex);
            }
        }
        return selectedNode;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!dividerLocationSet && splitPane != null && treeView != null) {
            dividerLocationSet = true;
            // setDividerLocation() only works when layout is finished
            splitPane.setDividerLocation(0.5d);
            splitPane.setResizeWeight(0.5d); // Resize in the same proportions
//            treeView.revalidate();
//            repaint();
        }
    }
    
    private JToolBar createLeftToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation(SwingConstants.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(true);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        
        JButton refreshButton = new JButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/refresh.png", false));
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshView();
            }
        });
        toolBar.add(refreshButton);

        toolBar.addSeparator();
        bookmarksTreeButton = new JToggleButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/bookmarksTree.png", false));
        bookmarksTreeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTreeViewVisible(true);
            }
        });
        toolBar.add(bookmarksTreeButton);

        bookmarksTableButton = new JToggleButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/bookmarksTable.png", false));
        bookmarksTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTreeViewVisible(false);
            }
        });
        toolBar.add(bookmarksTableButton);
        toolBar.addSeparator();

        return toolBar;
    }
    
    private static void fixScrollPaneinSplitPaneJDKIssue(Component c) {
        c.setMinimumSize(new Dimension(10, 10)); // Workaround for JSplitPane-containing-JScrollPane JDK bug
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("selectedNodes".equals(evt.getPropertyName())) {
            schedulePaneRefresh();
        }
    }
    
    private final class TreeOrTableContainer extends JPanel implements ExplorerManager.Provider {
        
        private ExplorerManager explorerManager;
        
        TreeOrTableContainer() {
            // Use GridLayout since BorderLayout does not behave well inside JSplitPane's left component
            // - it centers the contained component
            setLayout(new GridLayout(1, 1));
            fixScrollPaneinSplitPaneJDKIssue(this);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            if (explorerManager == null) {
                explorerManager = new ExplorerManager();
            }
            return explorerManager;
        }

    }

    private static final class ProjectBookmarksChildren extends Children.Keys<FileObject> {
        
        Project prj;
        
        String prjDisplayName;
        
        ProjectBookmarks projectBookmarks;
        
        ProjectBookmarksChildren(Project prj) {
            prjDisplayName = ProjectUtils.getInformation(prj).getDisplayName();
            this.projectBookmarks = BookmarksPersistence.get().getProjectBookmarks(prj, true);
            FileObject[] fos = BookmarksPersistence.get().getSortedFileObjects(projectBookmarks);
            setKeys(fos);
        }
        
        String getProjectDisplayName() {
            return prjDisplayName;
        }

        @Override
        protected Node[] createNodes(FileObject fo) {
            try {
                DataObject dob = DataObject.find(fo);
                Node node = dob.getNodeDelegate().cloneNode();
                URL url = fo.toURL();
                URLBookmarks urlBookmarks = projectBookmarks.get(url);
                return new Node[] { new FilterNode(node, new URLBookmarksChildren(urlBookmarks, fo)) };
            } catch (DataObjectNotFoundException ex) {
                throw new IllegalStateException(); // TODO generic node for FO
            }
        }

    }
    
    private static final class URLBookmarksChildren extends Children.Keys<Integer> {
        
        private URLBookmarks urlBookmarks;
        
        private FileObject fo;
        
        URLBookmarksChildren(URLBookmarks urlBookmarks, FileObject fo) {
            this.urlBookmarks = urlBookmarks;
            this.fo = fo;
        }
        
        public FileObject getFileObject() { // Used by BookmarkNode
            return fo;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(Collections.singleton(1)); // Fake key
        }
        
        @Override
        protected Node[] createNodes(Integer fakeKey) {
            Node[] bmNodes = null;
            try {
                DataObject dob = DataObject.find(fo);
                EditorCookie ec = dob.getCookie(EditorCookie.class);
                Document doc;
                if (ec != null && ((doc = ec.getDocument()) != null)) {
                    List<Bookmark> bmList = BookmarkList.get(doc).getBookmarks();
                    bmNodes = new Node[bmList.size()];
                    for (int i = 0; i < bmNodes.length; i++) {
                        bmNodes[i] = new BookmarkNode(fo, bmList.get(i));
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                // Use urlBookmarks in this case
            }
            
            if (bmNodes == null) {
                List<BookmarkInfo> bmInfos = urlBookmarks.getBookmarkInfos();
                bmNodes = new Node[bmInfos.size()];
                for (int i = 0; i < bmNodes.length; i++) {
                    bmNodes[i] = new BookmarkNode(fo, bmInfos.get(i));
                }
            }
            return bmNodes;
        }
        
    }

    private static final class BookmarksTableView extends JScrollPane { // Similar construct to explorer's TableView

        BookmarksTableView() {
            setViewportView(new BookmarksTable());
        }
        
        BookmarksTable getTable() {
            return (BookmarksTable) getViewport().getView();
        }

    }

}
