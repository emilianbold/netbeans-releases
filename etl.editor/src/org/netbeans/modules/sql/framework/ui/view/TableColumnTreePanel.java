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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.sql.framework.common.utils.NativeColumnOrderComparator;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;


/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class TableColumnTreePanel extends JPanel {

    private Comparator columnComparator = null;

    private DefaultMutableTreeNode rootNode;

    private List tables;

    private JTree tree;

    /**
     * Creates a new instance of TableTreeView using the given List of OTDs and showing
     * all components (OTDs, tables, and columns).
     *
     * @param otdList List containing OTDs to display
     */
    public TableColumnTreePanel(List tableList) {
        this(tableList, false);
    }

    /**
     * Creates a new instance of TableTreeView using the given List of OTDs and showing
     * all components (OTDs, tables, and columns).
     *
     * @param otdList List containing OTDs to display
     * @param useColumnOrdinalPosition true if columns should be sorted based on their
     *        ordinal position; false to sort by column name (ascending)
     */
    public TableColumnTreePanel(List tableList, boolean useColumnOrdinalPosition) {
        super();

        if (useColumnOrdinalPosition) {
            columnComparator = NativeColumnOrderComparator.getInstance();
        }
        this.tables = tableList;
        init();
    }

    /**
     * Gets List of selected items, if any, in this component.
     *
     * @return List (possibly empty) of user-selected items
     */
    public List getSelectedItems() {
        List itemList = new ArrayList();

        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                Object obj = ((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
                if (obj instanceof SQLDBColumn) {
                    SQLDBColumn column = (SQLDBColumn) obj;
                    if (column.isVisible()) {
                        itemList.add(obj);
                    }
                }
            }
        }

        return itemList;
    }

    public List getTableColumnNodes() {
        ArrayList tableNodes = new ArrayList();

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            tableNodes.add(rootNode.getChildAt(i));
        }

        return tableNodes;
    }

    /**
     * Associates the given JLabel with this component as its target whenever its
     * associated keyboard accelerator is triggered.
     *
     * @param myLabel label to associate with this component
     */
    public void setAsLabel(JLabel myLabel) {
        if (myLabel != null) {
            myLabel.setLabelFor(tree);
        }
    }

    public void setTables(List tbls) {
        this.tables = tbls;
        DefaultTreeModel treeModel = createTreeModel();
        tree.setModel(treeModel);
    }

    private void createColumnNodes(SQLDBTable table, DefaultMutableTreeNode parentNode) {
        List columnList = new ArrayList(table.getColumnList());
        Collections.sort(columnList, columnComparator);
        Iterator it = columnList.iterator();
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            TableColumnNode columnNode = new TableColumnNode.Leaf(column);
            if (column.isForeignKey() || column.isPrimaryKey()) {
                columnNode.setEnabled(false);
            } else {
                columnNode.setEnabled(true);
            }
            parentNode.add(columnNode);
        }
    }

    private DefaultTreeModel createTreeModel() {
        rootNode = new DefaultMutableTreeNode();
        DefaultTreeModel model = new DefaultTreeModel(rootNode);

        Iterator it = tables.iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            TableColumnNode columnAncestor = new TableColumnNode(table);
            rootNode.add(columnAncestor);

            createColumnNodes(table, columnAncestor);
            columnAncestor.setSelectedBasedOnChildren();
        }

        return model;
    }

    private void expandAllChildNodes(DefaultTreeModel model) {
        if (tree != null && model != null) {
            final int childCount = model.getChildCount(model.getRoot());
            for (int i = 0; i < childCount; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) model.getChild(model.getRoot(), i);
                tree.expandPath(new TreePath(node.getPath()));
            }
        }
    }

    private void init() {
        this.setLayout(new BorderLayout());

        tree = new JTree();
        DefaultTreeModel treeModel = createTreeModel();

        tree.setModel(treeModel);
        tree.setEditable(true);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellEditor(new TableColumnTreeCellEditor(this));
        tree.setCellRenderer(new TableColumnTreeCellRenderer());
        tree.setSelectionModel(null);

        ToolTipManager.sharedInstance().registerComponent(tree);
        expandAllChildNodes(treeModel);

        JScrollPane sPane = new JScrollPane(tree);
        this.add(BorderLayout.CENTER, sPane);
    }
}

