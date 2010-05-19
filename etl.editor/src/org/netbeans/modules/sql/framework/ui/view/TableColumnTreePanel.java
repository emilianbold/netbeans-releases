/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.sql.framework.model.DBColumn;

import org.netbeans.modules.sql.framework.common.utils.NativeColumnOrderComparator;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;


/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class TableColumnTreePanel extends JPanel {

    private Comparator<DBColumn> columnComparator = null;

    private DefaultMutableTreeNode rootNode;

    private List tables;

    private JTree tree;

    /**
     * Creates a new instance of TableTreeView using the given List of Databases and showing
     * all components (Databases, tables, and columns).
     *
     * @param dbList List containing Databases to display
     */
    public TableColumnTreePanel(List tableList) {
        this(tableList, false);
    }

    /**
     * Creates a new instance of TableTreeView using the given List of Databases and showing
     * all components (Db, tables, and columns).
     *
     * @param DbList List containing Databases to display
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
    public List<DBColumn> getSelectedItems() {
        List<DBColumn> itemList = new ArrayList<DBColumn>();

        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                Object obj = ((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
                if (obj instanceof SQLDBColumn) {
                    SQLDBColumn column = (SQLDBColumn) obj;
                    if (column.isVisible()) {
                        itemList.add((SQLDBColumn) obj);
                    }
                }
            }
        }
        return itemList;
    }

    public List<TreeNode> getTableColumnNodes() {
        List<TreeNode> tableNodes = new ArrayList<TreeNode>();
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

    public void setTables(List<DBTable> tbls) {
        this.tables = tbls;
        DefaultTreeModel treeModel = createTreeModel();
        tree.setModel(treeModel);
    }

    private void createColumnNodes(DBTable table, DefaultMutableTreeNode parentNode) {
        List<DBColumn> columnList = new ArrayList<DBColumn>(table.getColumnList());
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

