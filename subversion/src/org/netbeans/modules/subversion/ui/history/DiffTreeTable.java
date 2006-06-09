/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.util.*;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;

/**
 * Treetable to show results of Search History action.
 * 
 * @author Maros Sandor
 */
class DiffTreeTable extends TreeTableView {
    
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
    }
    
    private void setupColumns() {
        Node.Property [] columns = new Node.Property[4];
        ResourceBundle loc = NbBundle.getBundle(DiffTreeTable.class);
        columns[0] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_NAME, String.class, "", "");  // NOI18N
        columns[0].setValue("TreeColumnTTV", Boolean.TRUE); // NOI18N
        columns[1] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_DATE, String.class, loc.getString("LBL_DiffTree_Column_Time"), loc.getString("LBL_DiffTree_Column_Time_Desc"));
        columns[2] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_USERNAME, String.class, loc.getString("LBL_DiffTree_Column_Username"), loc.getString("LBL_DiffTree_Column_Username_Desc"));
        columns[3] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        setProperties(columns);
    }
    
    private void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = getWidth();
                treeTable.getColumnModel().getColumn(0).setPreferredWidth(width * 25 / 100);
                treeTable.getColumnModel().getColumn(1).setPreferredWidth(width * 15 / 100);
                treeTable.getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);
                treeTable.getColumnModel().getColumn(3).setPreferredWidth(width * 50 / 100);
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
