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
package org.netbeans.modules.search.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.Selectable;
import org.netbeans.modules.search.ui.AbstractSearchResultsPanel.RootNode;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class ResultsOutlineSupport {

    @StaticResource
    private static final String ROOT_NODE_ICON =
            "org/netbeans/modules/search/res/context.gif";              //NOI18N
    private static final int VERTICAL_ROW_SPACE = 2;

    OutlineView outlineView;
    private boolean replacing;
    private boolean details;
    private ResultsNode resultsNode;
    private ResultModel resultModel;
    private FolderTreeItem rootPathItem = new FolderTreeItem();
    private List<FileObject> rootFiles;
    private Node infoNode;
    private Node invisibleRoot;
    private List<TableColumn> allColumns = new ArrayList<TableColumn>(5);
    private ETableColumnModel columnModel;
    private List<MatchingObjectNode> matchingObjectNodes;
    private boolean closed = false;

    public ResultsOutlineSupport(boolean replacing, boolean details,
            ResultModel resultModel, List<FileObject> rootFiles,
            Node infoNode) {

        this.replacing = replacing;
        this.details = details;
        this.resultModel = resultModel;
        this.rootFiles = rootFiles;
        this.resultsNode = new ResultsNode();
        this.infoNode = infoNode;
        this.invisibleRoot = new RootNode(resultsNode, infoNode);
        this.matchingObjectNodes = new LinkedList<MatchingObjectNode>();
        createOutlineView();
    }

    private void createOutlineView() {
        outlineView = new OutlineView(UiUtils.getText(
                "BasicSearchResultsPanel.outline.nodes"));              //NOI18N
        outlineView.getOutline().setDefaultRenderer(Node.Property.class,
                new ResultsOutlineCellRenderer());
        setOutlineColumns();
        outlineView.addTreeExpansionListener(
                new ExpandingTreeExpansionListener());
        outlineView.getOutline().setRootVisible(false);
        outlineView.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)
                        != 0) {
                    if (outlineView.isDisplayable()) {
                        outlineView.expandNode(resultsNode);
                    }
                }
            }
        });
        outlineView.getOutline().getColumnModel().addColumnModelListener(
                new ColumnsListener());
        outlineView.getOutline().getInputMap().remove(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)); //#209949
        outlineView.getOutline().setShowGrid(false);
        Font font = outlineView.getOutline().getFont();
        FontMetrics fm = outlineView.getOutline().getFontMetrics(font);
        outlineView.getOutline().setRowHeight(
                Math.max(16, fm.getHeight()) + VERTICAL_ROW_SPACE);
    }

    public synchronized void closed() {
        clean();
        saveColumnState();
    }

    public synchronized void clean() {
        resultModel.close();
        for (MatchingObjectNode mo : matchingObjectNodes) {
            mo.clean();
        }
        closed = true;
    }

    private void loadColumnState() {
        String state;
        FindDialogMemory memory = FindDialogMemory.getDefault();
        if (replacing) {
            state = memory.getResultsColumnWidthsReplacing();
        } else if (details) {
            state = memory.getResultsColumnWidthsDetails();
        } else {
            state = memory.getResultsColumnWidths();
        }
        String[] parts = state.split("\\|");                            //NOI18N
        if (parts == null || parts.length != 2) {
            return;
        }
        String[] order = parts[1].split(":");                           //NOI18N
        for (int i = 0; i < order.length; i++) {
            try {
                int modelIndex = Integer.parseInt(order[i]);
                int oldIndex = columnModel.getColumnIndex(
                        allColumns.get(modelIndex).getIdentifier());
                columnModel.moveColumn(oldIndex, i);

            } catch (NumberFormatException e) {
            }
        }
        String[] widths = parts[0].split(":");                          //NOI18N
        for (int i = 0; i < widths.length && i < allColumns.size(); i++) {
            String widthStr = widths[i];
            if (widthStr != null && !widthStr.isEmpty()) {
                try {
                    int width = Integer.parseInt(widthStr);
                    if (width == -1) {
                        columnModel.setColumnHidden(allColumns.get(i), true);
                    } else {
                        allColumns.get(i).setPreferredWidth(width);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private void saveColumnState() {
        StringBuilder sb = new StringBuilder();
        for (TableColumn tc : allColumns) {
            if (columnModel.isColumnHidden(tc)) {
                sb.append(-1);
            } else {
                sb.append(tc.getWidth());
            }
            sb.append(":");                                             //NOI18N
        }
        sb.append("|");                                                 //NOI18N
        Enumeration<TableColumn> columns = columnModel.getColumns();
        while (columns.hasMoreElements()) {
            TableColumn tc = columns.nextElement();
            int index = allColumns.indexOf(tc);
            if (index >= 0) {
                sb.append(index);
                sb.append(":");                                         //NOI18N
            }
        }
        String str = sb.toString();
        if (replacing) {
            FindDialogMemory.getDefault().setResultsColumnWidthsReplacing(str);
        } else if (details) {
            FindDialogMemory.getDefault().setResultsColumnWidthsDetails(str);
        } else {
            FindDialogMemory.getDefault().setResultsColumnWidths(str);
        }
    }

    private void setOutlineColumns() {
        if (details) {
            outlineView.addPropertyColumn(
                    "detailsCount", UiUtils.getText( //NOI18N
                    "BasicSearchResultsPanel.outline.detailsCount"));   //NOI18N
        }
        outlineView.addPropertyColumn("path", UiUtils.getText(
                "BasicSearchResultsPanel.outline.path"));               //NOI18N
        outlineView.addPropertyColumn("size", UiUtils.getText(
                "BasicSearchResultsPanel.outline.size"));               //NOI18N
        outlineView.addPropertyColumn("lastModified", UiUtils.getText(
                "BasicSearchResultsPanel.outline.lastModified"));       //NOI18N
        outlineView.getOutline().setAutoResizeMode(
                Outline.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        this.columnModel =
                (ETableColumnModel) outlineView.getOutline().getColumnModel();
        Enumeration<TableColumn> cols = columnModel.getColumns();
        while (cols.hasMoreElements()) {
            allColumns.add(cols.nextElement());
        }
        loadColumnState();
        outlineView.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public void update() {
        resultsNode.update();
    }

    private class ExpandingTreeExpansionListener
            implements TreeExpansionListener {

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            Object lpc = event.getPath().getLastPathComponent();
            Node node = Visualizer.findNode(lpc);
            if (node != null) {
                expandOnlyChilds(node);
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    /**
     * Class for representation of the root node.
     */
    private class ResultsNode extends AbstractNode {

        private FlatChildren flatChildren;
        private FolderTreeChildren folderTreeChildren;
        private String htmlDisplayName = null;

        public ResultsNode() {
            super(new FlatChildren());
            this.flatChildren = (FlatChildren) this.getChildren();
            this.folderTreeChildren = new FolderTreeChildren(rootPathItem);
        }

        void update() {
            flatChildren.update();
        }

        void setFlatMode() {
            setChildren(flatChildren);
            expand();
        }

        void setFolderTreeMode() {
            setChildren(folderTreeChildren);
            expand();
        }

        private void expand() {
            outlineView.expandNode(resultsNode);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(ROOT_NODE_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

        @Override
        protected void createPasteTypes(Transferable t, List<PasteType> s) {
        }

        public void setHtmlAndRawDisplayName(String htmlName) {
            htmlDisplayName = htmlName == null
                    ? null
                    : "<html>" + htmlName + "</html>";       // #214330 //NOI18N
            String stripped = (htmlName == null)
                    ? null
                    : htmlName.replaceAll("<b>", "").replaceAll( //NOI18N
                    "</b>", "");                                        //NOI18N
            this.setDisplayName(stripped);
        }

        @Override
        public String getHtmlDisplayName() {
            return htmlDisplayName;
        }
    }

    private void expandOnlyChilds(Node parent) {
        if (parent.getChildren().getNodesCount(true) == 1) {
            Node onlyChild = parent.getChildren().getNodeAt(0);
            outlineView.expandNode(onlyChild);
            expandOnlyChilds(onlyChild);
        }
    }

    /**
     * Children of the main results node.
     *
     * Shows list of matching data objects.
     */
    private class FlatChildren extends Children.Keys<MatchingObject> {

        @Override
        protected Node[] createNodes(MatchingObject key) {
            return new Node[]{createNodeForMatchingObject(key)};
        }

        private synchronized void update() {
            setKeys(resultModel.getMatchingObjects());
        }
    }

    private Node createNodeForMatchingObject(MatchingObject key) {
        Node delegate;
        if (key.getDataObject() == null) {
            Node n = new AbstractNode(Children.LEAF);
            n.setDisplayName("Error"); //NOI18N
            return n;
        }
        delegate = key.getDataObject().getNodeDelegate();
        Children children;
        if (key.getTextDetails() == null
                || key.getTextDetails().isEmpty()) {
            children = Children.LEAF;
        } else {
            children = key.getDetailsChildren(replacing);
        }
        MatchingObjectNode mon =
                new MatchingObjectNode(delegate, children, key, replacing);
        matchingObjectNodes.add(mon);
        return mon;
    }

    public synchronized void addMatchingObject(MatchingObject mo) {
        if (closed) {
            return;
        }
        for (FileObject fo : rootFiles) {
            if (fo == mo.getFileObject()
                    || FileUtil.isParentOf(fo, mo.getFileObject())) {
                addToTreeView(rootPathItem,
                        getRelativePath(fo, mo.getFileObject()), mo);
                return;
            }
        }
        addToTreeView(rootPathItem,
                Collections.singletonList(mo.getFileObject()), mo);
    }

    private List<FileObject> getRelativePath(FileObject parent, FileObject fo) {
        List<FileObject> l = new LinkedList<FileObject>();
        FileObject part = fo;
        while (part != null) {
            l.add(0, part);
            if (part == parent) {
                break;
            }
            part = part.getParent();
        }
        return l;
    }

    private void addToTreeView(FolderTreeItem parentItem, List<FileObject> path,
            MatchingObject matchingObject) {
        for (FolderTreeItem pi : parentItem.getChildren()) {
            if (!pi.isPathLeaf()
                    && pi.getFolder().getPrimaryFile().equals(path.get(0))) {
                addToTreeView(pi, path.subList(1, path.size()), matchingObject);
                return;
            }
        }
        createInTreeView(parentItem, path, matchingObject);
    }

    private void createInTreeView(FolderTreeItem parentItem,
            List<FileObject> path, MatchingObject matchingObject) {
        if (path.size() == 1) {
            for (FolderTreeItem pi : parentItem.getChildren()) {
                if (pi.isPathLeaf()
                        && pi.getMatchingObject().equals(matchingObject)) {
                    return;
                }
            }
            parentItem.addChild(new FolderTreeItem(matchingObject));
        } else {
            try {
                FolderTreeItem newChild = new FolderTreeItem(
                        DataObject.find(path.get(0)));
                parentItem.addChild(newChild);
                createInTreeView(newChild, path.subList(1, path.size()),
                        matchingObject);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class FolderTreeItem implements Selectable {

        private static final String PROP_SELECTED = "selected";         //NOI18N
        private static final String PROP_CHILDREN = "children";         //NOI18N
        private DataObject folder = null;
        private MatchingObject matchingObject = null;
        private List<FolderTreeItem> children =
                new LinkedList<FolderTreeItem>();
        private boolean selected = true;
        PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        /**
         * Constructor for root node
         */
        public FolderTreeItem() {
        }

        public FolderTreeItem(MatchingObject matchingObject) {
            this.matchingObject = matchingObject;
        }

        public FolderTreeItem(DataObject file) {
            this.folder = file;
        }

        void addChild(FolderTreeItem pathItem) {
            children.add(pathItem);
            firePropertyChange(PROP_CHILDREN, null, null);
        }

        public DataObject getFolder() {
            return folder;
        }

        public List<FolderTreeItem> getChildren() {
            return children;
        }

        public MatchingObject getMatchingObject() {
            return matchingObject;
        }

        public boolean isPathLeaf() {
            return matchingObject != null;
        }

        @Override
        public boolean isSelected() {
            return selected;
        }

        @Override
        public void setSelected(boolean selected) {
            if (selected == this.selected) {
                return;
            }
            this.selected = selected;
            firePropertyChange(PROP_SELECTED, null, null);
        }

        @Override
        public void setSelectedRecursively(boolean selected) {
            if (this.selected == selected) {
                return;
            }
            if (this.isPathLeaf()) {
                this.getMatchingObject().setSelectedRecursively(selected);
            } else {
                for (FolderTreeItem child : children) {
                    child.setSelectedRecursively(selected);
                }
            }
            setSelected(selected);
        }

        public synchronized void addPropertyChangeListener(
                PropertyChangeListener listener) {
            changeSupport.addPropertyChangeListener(listener);
        }

        public synchronized void removePropertyChangeListener(
                PropertyChangeListener listener) {
            changeSupport.removePropertyChangeListener(listener);
        }

        public void firePropertyChange(String propertyName, Object oldValue,
                Object newValue) {
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    private class FolderTreeNode extends FilterNode {

        public FolderTreeNode(FolderTreeItem pathItem) {
            super(pathItem.getFolder().getNodeDelegate(),
                    new FolderTreeChildren(pathItem),
                    Lookups.fixed(pathItem,
                    new ReplaceCheckableNode(pathItem, replacing),
                    pathItem.getFolder().getPrimaryFile()));
            pathItem.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    fireIconChange();
                    toggleParentSelected(FolderTreeNode.this);
                }
            });
            if (!pathItem.isPathLeaf()) {
                setShortDescription(
                        pathItem.getFolder().getPrimaryFile().getPath());
            }
        }

        @Override
        public PasteType[] getPasteTypes(Transferable t) {
            return new PasteType[0];
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            return null;
        }

        @Override
        public Transferable drag() throws IOException {
            return UiUtils.DISABLE_TRANSFER;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }

    private class FolderTreeChildren extends Children.Keys<FolderTreeItem> {

        private FolderTreeItem item = null;

        public FolderTreeChildren(FolderTreeItem pathItem) {
            this.item = pathItem;
            pathItem.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(
                            FolderTreeItem.PROP_CHILDREN)) {
                        update();
                    }
                }
            });
        }

        @Override
        protected void addNotify() {
            update();
        }

        void update() {
            setKeys(item.getChildren());
        }

        @Override
        protected Node[] createNodes(FolderTreeItem key) {
            Node n;
            if (key.isPathLeaf()) {
                n = createNodeForMatchingObject(key.getMatchingObject());
            } else {
                n = new FolderTreeNode(key);
            }
            return new Node[]{n};
        }
    }

    public static void toggleParentSelected(Node node) {
        Node parent = node.getParentNode();
        if (parent == null) {
            return;
        }
        Selectable parentSelectable = parent.getLookup().lookup(
                Selectable.class);
        if (parentSelectable != null) {
            Node[] children = parent.getChildren().getNodes(true);
            boolean selectedChildFound = false;
            for (Node child : children) {
                Selectable childSelectable = child.getLookup().lookup(
                        Selectable.class);
                if (childSelectable != null && childSelectable.isSelected()) {
                    selectedChildFound = true;
                    break;
                }
            }
            if (parentSelectable.isSelected() != selectedChildFound) {
                parentSelectable.setSelected(selectedChildFound);
            }
        }
    }

    public void setFolderTreeMode() {
        resultsNode.setFolderTreeMode();
    }

    public void setFlatMode() {
        resultsNode.setFlatMode();
    }

    public OutlineView getOutlineView() {
        return outlineView;
    }

    public Node getRootNode() {
        return invisibleRoot;
    }

    public Node getResultsNode() {
        return resultsNode;
    }

    public void setResultsNodeText(String text) {
        resultsNode.setHtmlAndRawDisplayName(text);
    }

    private class ColumnsListener implements TableColumnModelListener {

        @Override
        public void columnAdded(TableColumnModelEvent e) {
            saveColumnState();
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            saveColumnState();
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            saveColumnState();
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            saveColumnState();
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
        }
    }
}
