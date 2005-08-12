/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    }
    
    private void setupColumns() {
        Node.Property [] columns = new Node.Property[5];
        ResourceBundle loc = NbBundle.getBundle(DiffTreeTable.class);
        columns[0] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_NAME, String.class, "", "");
        columns[0].setValue("TreeColumnTTV", Boolean.TRUE);
        columns[1] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_REVISION, String.class, loc.getString("LBL_DiffTree_Column_Revision"), loc.getString("LBL_DiffTree_Column_Revision_Desc"));
        columns[2] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_DATE, String.class, loc.getString("LBL_DiffTree_Column_Time"), loc.getString("LBL_DiffTree_Column_Time_Desc"));
        columns[3] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_USERNAME, String.class, loc.getString("LBL_DiffTree_Column_Username"), loc.getString("LBL_DiffTree_Column_Username_Desc"));
        columns[4] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        setProperties(columns);
    }
    
    private void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = getWidth();
                treeTable.getColumnModel().getColumn(0).setPreferredWidth(width * 25 / 100);
                treeTable.getColumnModel().getColumn(1).setPreferredWidth(width * 10 / 100);
                treeTable.getColumnModel().getColumn(2).setPreferredWidth(width * 15 / 100);
                treeTable.getColumnModel().getColumn(3).setPreferredWidth(width * 10 / 100);
                treeTable.getColumnModel().getColumn(4).setPreferredWidth(width * 40 / 100);
            }
        });
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
        rootNode = new RevisionsRootNode();
        ExplorerManager em = ExplorerManager.find(this);
        em.setRootContext(rootNode);
        setDefaultColumnSizes();
    }

    public void setResults(List results) {
        this.results = results;
        ExplorerManager em = ExplorerManager.find(this);
        if (em != null) {
            rootNode = new RevisionsRootNode();
            em.setRootContext(rootNode);
        }
    }
    
    private class RevisionsRootNode extends AbstractNode {
    
        public RevisionsRootNode() {
            super(new RevisionsRootNodeChildren(), Lookups.singleton(results));
        }

        public String getName() {
            return "revision";
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
                node = new RevisionNode(((SearchHistoryPanel.DispRevision) key).getRevision());
            }
            return new Node[] { node };
        }
    }
}
