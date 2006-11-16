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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.beans.PropertyVetoException;
import java.awt.Window;
import java.awt.Point;
import java.awt.Cursor;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Treetable to show results of Search History action.
 * 
 * @author Maros Sandor
 */
class DiffTreeTable extends TreeTableView implements MouseListener, MouseMotionListener {
    
    private RevisionsRootNode rootNode;
    private List results;

    public DiffTreeTable() {
        treeTable.setShowHorizontalLines(true);
        treeTable.setShowVerticalLines(false);
        setRootVisible(false);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setupColumns();

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);
        tree.setCellRenderer(renderer);
        
        treeTable.addMouseListener(this);
        treeTable.addMouseMotionListener(this);
    }

    private SearchHistoryPanel.DispRevision getRevisionWithTagsAt(Point p) {
        int row = treeTable.rowAtPoint(p);
        int column = treeTable.columnAtPoint(p);
        if (row == -1 || column == -1) return null;
        Object o = treeTable.getValueAt(row, column);
        if (o instanceof Node.Property) {
            Node.Property tags = (Node.Property) o;
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) tags.getValue("dispRevision");
            if (drev != null && drev.getBranches() != null && drev.getBranches().size() + drev.getTags().size() > 1) {
                return drev;
            }
        }
        return null;
    }
    
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        SearchHistoryPanel.DispRevision drev = getRevisionWithTagsAt(p);
        if (drev != null) {
            Window w = SwingUtilities.windowForComponent(treeTable);
            SwingUtilities.convertPointToScreen(p, treeTable);
            p.x += 10;
            p.y += 10;
            SummaryView.showAllTags(w, p, drev);
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        if (getRevisionWithTagsAt(e.getPoint()) != null) {
            treeTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            treeTable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void setupColumns() {
        ResourceBundle loc = NbBundle.getBundle(DiffTreeTable.class);
        Node.Property [] columns;
        if (CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_SEARCHHISTORY_FETCHTAGS, true)) {
            columns = new Node.Property[5];
            columns[3] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_TAGS, List.class, loc.getString("LBL_DiffTree_Column_Tags"), loc.getString("LBL_DiffTree_Column_Tags_Desc"));
            columns[4] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        } else {
            columns = new Node.Property[4];
            columns[3] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        }
        columns[0] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_NAME, String.class, "", "");  // NOI18N
        columns[0].setValue("TreeColumnTTV", Boolean.TRUE); // NOI18N
        columns[1] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_DATE, String.class, loc.getString("LBL_DiffTree_Column_Time"), loc.getString("LBL_DiffTree_Column_Time_Desc"));
        columns[2] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_USERNAME, String.class, loc.getString("LBL_DiffTree_Column_Username"), loc.getString("LBL_DiffTree_Column_Username_Desc"));
        setProperties(columns);
    }
    
    private void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = getWidth();
                if (CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_SEARCHHISTORY_FETCHTAGS, true)) {
                    treeTable.getColumnModel().getColumn(0).setPreferredWidth(width * 20 / 100);
                    treeTable.getColumnModel().getColumn(1).setPreferredWidth(width * 15 / 100);
                    treeTable.getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);
                    treeTable.getColumnModel().getColumn(3).setPreferredWidth(width * 10 / 100);
                    treeTable.getColumnModel().getColumn(4).setPreferredWidth(width * 45 / 100);
                } else {
                    treeTable.getColumnModel().getColumn(0).setPreferredWidth(width * 25 / 100);
                    treeTable.getColumnModel().getColumn(1).setPreferredWidth(width * 15 / 100);
                    treeTable.getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);
                    treeTable.getColumnModel().getColumn(3).setPreferredWidth(width * 50 / 100);
                }
            }
        });
    }

    void setSelection(int idx) {
        treeTable.getSelectionModel().setValueIsAdjusting(false);
        treeTable.scrollRectToVisible(treeTable.getCellRect(idx, 1, true));
        treeTable.getSelectionModel().setSelectionInterval(idx, idx);
    }

    void setSelection(SearchHistoryPanel.ResultsContainer container) {
        RevisionNode node = (RevisionNode) getNode(rootNode, container);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    void setSelection(SearchHistoryPanel.DispRevision revision) {
        RevisionNode node = (RevisionNode) getNode(rootNode, revision);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private Node getNode(Node node, Object obj) {
        Object object = node.getLookup().lookup(obj.getClass());
        if (obj.equals(object)) return node;
        Enumeration children = node.getChildren().nodes();
        while (children.hasMoreElements()) {
            Node child = (Node) children.nextElement();
            Node result = getNode(child, obj);
            if (result != null) return result;
        }
        return null;
    }

    public int [] getSelection() {
        return treeTable.getSelectedRows();
    }

    public int getRowCount() {
        return treeTable.getRowCount();
    }

    private static class ColumnDescriptor extends PropertySupport.ReadOnly {
        
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    public void addNotify() {
        super.addNotify();
        ExplorerManager em = ExplorerManager.find(this);
        em.setRootContext(rootNode);
        setDefaultColumnSizes();
    }

    public void setResults(List results) {
        this.results = results;
        rootNode = new RevisionsRootNode();
        ExplorerManager em = ExplorerManager.find(this);
        if (em != null) {
            em.setRootContext(rootNode);
        }
    }
    
    private class RevisionsRootNode extends AbstractNode {
    
        public RevisionsRootNode() {
            super(new RevisionsRootNodeChildren(), Lookups.singleton(results));
        }

        public String getName() {
            return "revision"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name");
        }

        public String getShortDescription() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name_Desc");
        }
    }

    private class RevisionsRootNodeChildren extends Children.Keys {
    
        public RevisionsRootNodeChildren() {
        }

        protected void addNotify() {
            refreshKeys();
        }

        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
    
        private void refreshKeys() {
            setKeys(results);
        }
    
        protected Node[] createNodes(Object key) {
            RevisionNode node;
            if (key instanceof SearchHistoryPanel.ResultsContainer) {
                node = new RevisionNode((SearchHistoryPanel.ResultsContainer) key);
            } else { // key instanceof SearchHistoryPanel.DispRevision
                node = new RevisionNode(((SearchHistoryPanel.DispRevision) key));
            }
            return new Node[] { node };
        }
    }
}
