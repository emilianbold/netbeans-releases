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

package org.netbeans.modules.viewmodel;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.PropertySupport;
import org.openide.windows.TopComponent;


/**
 * Implements table visual representation of the data from models, using Outline view.
 *
 * @author   Martin Entlicher
 */
public class OutlineTable extends JPanel implements
ExplorerManager.Provider, PropertyChangeListener {

    private final Logger logger = Logger.getLogger(OutlineTable.class.getName());
    
    private ExplorerManager     explorerManager;
    private final MyTreeTable   treeTable;
    Node.Property[]             columns; // Accessed from tests
    private TableColumn[]       tableColumns;
    private int[]               columnVisibleMap; // Column index -> visible index
    //private IndexedColumn[]     icolumns;
    private boolean             isDefaultColumnAdded;
    private int                 defaultColumnIndex; // The index of the tree column
    private boolean             ignoreMove; // Whether to ignore column movement events
    //private List                expandedPaths = new ArrayList ();
    TreeModelRoot               currentTreeModelRoot; // Accessed from test
    private Models.CompoundModel model;
    
    
    public OutlineTable () {
        setLayout (new BorderLayout ());
            treeTable = new MyTreeTable ();
            treeTable.getOutline().setRootVisible (false);
            treeTable.setVerticalScrollBarPolicy 
                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            treeTable.setHorizontalScrollBarPolicy 
                (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add (treeTable, "Center");  //NOI18N
        treeTable.getTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            // Track column visibility changes.
            //   No impact on order property
            //   Change visibility map

            public void columnAdded(TableColumnModelEvent e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("columnAdded("+e+") to = "+e.getToIndex());
                    TableColumnModel tcme = (TableColumnModel) e.getSource();
                    logger.fine(" column header = '"+tcme.getColumn(e.getToIndex()).getHeaderValue()+"'");
                    dumpColumnVisibleMap();
                }
                if (tableColumns != null && e.getToIndex() >= 0) {
                    // It does not say *which* column was added to the toIndex.
                    int visibleIndex = e.getToIndex();
                    int columnIndex = -1;
                    TableColumnModel tcm = treeTable.getTable().getColumnModel();
                    ETableColumnModel ecm = (ETableColumnModel) tcm;
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (tableColumns[i] != null) {
                            boolean wasHidden = columns[i].isHidden();
                            boolean isHidden = ecm.isColumnHidden(tableColumns[i]);
                            if (wasHidden == true && isHidden == false) {
                                columnIndex = i;
                                break;
                            }
                        }
                    }
                    logger.fine("  to index = "+visibleIndex+", column index = "+columnIndex);
                    if (columnIndex != -1) {
                        int prefferedVisibleIndex = columnVisibleMap[columnIndex];
                        // check if there's a visible column with the same visible index and lower order
                        int columnVisibleIndex = prefferedVisibleIndex;
                        int corder = getColumnOrder(columns[columnIndex]);
                        for (int i = 0; i < columnVisibleMap.length; i++) {
                            if (columnVisibleMap[i] == prefferedVisibleIndex) {
                                if (corder > getColumnOrder(columns[i]) && !columns[i].isHidden()) {
                                    prefferedVisibleIndex++;
                                    break;
                                }
                            }
                        }
                        logger.fine("  to index = "+visibleIndex+", column = "+columns[columnIndex].getDisplayName()+", columnVisibleIndex = "+columnVisibleIndex+", prefferedVisibleIndex = "+prefferedVisibleIndex);
                        columns[columnIndex].setHidden(false);
                        columnVisibleMap[columnIndex] = prefferedVisibleIndex;
                        for (int i = 0; i < columnVisibleMap.length; i++) {
                            if (columnVisibleMap[i] >= columnVisibleIndex && i != columnIndex &&
                                getColumnOrder(columns[i]) >= corder) {
                                
                                columnVisibleMap[i]++;
                            }
                        }
                        if (prefferedVisibleIndex >= 0 && prefferedVisibleIndex != visibleIndex) {
                            logger.fine("moveColumn("+visibleIndex+", "+prefferedVisibleIndex+")");
                            ignoreMove = true;
                            try {
                                treeTable.getTable().getColumnModel().moveColumn(visibleIndex, prefferedVisibleIndex);
                            } finally {
                                ignoreMove = false;
                            }
                        }
                    }
                }
                if (logger.isLoggable(Level.FINE)) {
                    dumpColumnVisibleMap();
                }
            }

            public void columnRemoved(TableColumnModelEvent e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("columnRemoved("+e+") from = "+e.getFromIndex());
                    dumpColumnVisibleMap();
                }
                if (tableColumns != null && e.getFromIndex() >= 0) {
                    int visibleIndex = e.getFromIndex();
                    logger.fine("  from index = "+visibleIndex);
                    int columnIndex = getColumnIndex(visibleIndex);
                    if (columnIndex != -1) {
                        columns[columnIndex].setHidden(true);
                        for (int i = 0; i < columnVisibleMap.length; i++) {
                            if (columnVisibleMap[i] >= visibleIndex && columnVisibleMap[i] > 0) {
                                columnVisibleMap[i]--;
                            }
                        }
                    }
                }
                if (logger.isLoggable(Level.FINE)) {
                    dumpColumnVisibleMap();
                }
            }

            public void columnMoved(TableColumnModelEvent e) {
                if (tableColumns == null || ignoreMove) return ;
                int from = e.getFromIndex();
                int to = e.getToIndex();
                if (from == to) {
                    // Ignore Swing strangeness
                    return ;
                }
                int fc = getColumnIndex(from);
                int tc = getColumnIndex(to);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("columnMoved("+e+") from = "+from+", to = "+to);
                    logger.fine("  from = "+from+", to = "+to);
                    logger.fine("  fc = "+fc+", tc = "+tc);
                    TableColumnModel tcme = (TableColumnModel) e.getSource();
                    logger.fine(" column headers = '"+tcme.getColumn(e.getFromIndex()).getHeaderValue()+"' => '"+tcme.getColumn(e.getToIndex()).getHeaderValue()+"'");
                    dumpColumnVisibleMap();
                }
                int toColumnOrder = getColumnOrder(columns[tc]);
                if (from < to) {
                    for (int i = from + 1; i <= to; i++) {
                        // Iterate through columns whose visible index is 'i'
                        // and whose order is between 'from order' and 'to order'
                        // and adjust the order and visible map
                        for (int ic = 0; ic < columnVisibleMap.length; ic++) {
                            if (ic != fc && i == columnVisibleMap[ic]) {
                                int order = getColumnOrder(columns[ic]);
                                if (order <= toColumnOrder && order > getColumnOrder(columns[fc])) {
                                    setColumnOrder(columns[ic], order - 1);
                                    columnVisibleMap[ic]--;
                                }
                            }
                        }
                    }
                } else {
                    for (int i = from - 1; i >= to; i--) {
                        // Iterate through columns whose visible index is 'i'
                        // and whose order is between 'from order' and 'to order'
                        // and adjust the order and visible map
                        for (int ic = 0; ic < columnVisibleMap.length; ic++) {
                            if (i == columnVisibleMap[ic]) {
                                int order = getColumnOrder(columns[ic]);
                                if (order < getColumnOrder(columns[fc]) && order >= toColumnOrder) {
                                    setColumnOrder(columns[ic], getColumnOrder(columns[ic]) + 1);
                                    columnVisibleMap[ic]++;
                                }
                            }
                        }
                    }
                }
                setColumnOrder(columns[fc], toColumnOrder);
                columnVisibleMap[fc] = to;
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("After move:");
                    dumpColumnVisibleMap();
                }
            }
            
            public void columnMarginChanged(ChangeEvent e) {}
            public void columnSelectionChanged(ListSelectionEvent e) {}
        });
        ActionMap map = getActionMap();
        ExplorerManager manager = getExplorerManager();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        setFocusable(false);
    }

    private void dumpColumnVisibleMap() {
        logger.fine("");
        logger.fine("Column Visible Map ("+columnVisibleMap.length+"):");
        for (int i = 0; i < columnVisibleMap.length; i++) {
            logger.fine(" map["+i+"] = "+columnVisibleMap[i]+"; columnOrder["+i+"] = "+getColumnOrder(columns[i]));
        }
        logger.fine("");
    }

    private int getColumnOrder(Node.Property column) {
        Integer order = (Integer) column.getValue(Column.PROP_ORDER_NUMBER);
        if (order == null) {
            return -1;
        } else {
            return order.intValue();
        }
    }

    private void setColumnOrder(Node.Property column, int order) {
        column.setValue(Column.PROP_ORDER_NUMBER, order);
    }

    private int getColumnIndex(int visibleIndex) {
        for (int i = 0; i < columnVisibleMap.length; i++) {
            if (visibleIndex == columnVisibleMap[i] && !columns[i].isHidden()) {
                return i;
            }
        }
        return -1;
    }
    
    public void setModel (Models.CompoundModel model) {
        // 2) save current settings (like columns, expanded paths)
        //List ep = treeTable.getExpandedPaths ();
        saveWidths ();
        saveSortedState();
        
        this.model = model;
        
        // 1) destroy old model
        if (currentTreeModelRoot != null) 
            currentTreeModelRoot.destroy ();
        
        // 3) no model => set empty root node & return
        if (model == null) {
            getExplorerManager ().setRootContext (
                new AbstractNode (Children.LEAF)
            );
            return;
        }
        
        // 4) set columns for given model
        String[] nodesColumnName = new String[] { null };
        Node.Property[] columnsToSet = createColumns (model, nodesColumnName);
        treeTable.setNodesColumnName(nodesColumnName[0]);
        currentTreeModelRoot = new TreeModelRoot (model, treeTable);
        TreeModelNode rootNode = currentTreeModelRoot.getRootNode ();
        getExplorerManager ().setRootContext (rootNode);
        // The root node must be ready when setting the columns
        treeTable.setProperties (columnsToSet);
        updateTableColumns(columnsToSet);
        //treeTable.getTable().tableChanged(new TableModelEvent(treeTable.getOutline().getModel()));
        //getExplorerManager ().setRootContext (rootNode);
        
        // 5) set root node for given model
        // Moved to 4), because the new root node must be ready when setting columns
        
        // 6) update column widths & expanded nodes
        updateColumnWidthsAndSorting();
        //treeTable.expandNodes (expandedPaths);
        // TODO: this is a workaround, we should find a better way later
        /* We must not call children here - it can take a long time...
         * the expansion is performed in TreeModelNode.TreeModelChildren.applyChildren()
        final List backupPath = new ArrayList (expandedPaths);
        if (backupPath.size () == 0)
            TreeModelNode.getRequestProcessor ().post (new Runnable () {
                public void run () {
                    try {
                        final Object[] ch = TreeTable.this.model.getChildren 
                            (TreeTable.this.model.getRoot (), 0, 0);
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                expandDefault (ch);
                            }
                        });
                    } catch (UnknownTypeException ex) {}
                }
            });
        else
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    treeTable.expandNodes (backupPath);
                }
            });
         */
        //if (ep.size () > 0) expandedPaths = ep;

        // Sort of hack(?) After close/open of the view the table becomes empty,
        // it looks like the root node stays unexpanded for some reason.
        treeTable.expandNode(rootNode);
    }
    
    public ExplorerManager getExplorerManager () {
        if (explorerManager == null) {
            explorerManager = new ExplorerManager ();
        }
        return explorerManager;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        TopComponent tc = (TopComponent) SwingUtilities.
            getAncestorOfClass (TopComponent.class, this);
        if (tc == null) return;
        if (propertyName.equals (TopComponent.Registry.PROP_CURRENT_NODES)) {
            ExplorerUtils.activateActions(getExplorerManager(), equalNodes());
        } else
        if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            tc.setActivatedNodes ((Node[]) evt.getNewValue ());
        }
    }
    
    private boolean equalNodes () {
        Node[] ns1 = TopComponent.getRegistry ().getCurrentNodes ();
        Node[] ns2 = getExplorerManager ().getSelectedNodes ();
        if (ns1 == ns2) return true;
        if ( (ns1 == null) || (ns2 == null) ) return false;
        if (ns1.length != ns2.length) return false;
        int i, k = ns1.length;
        for (i = 0; i < k; i++)
            if (!ns1 [i].equals (ns2 [i])) return false;
        return true;
    }
    
    private Node.Property[] createColumns (Models.CompoundModel model, String[] nodesColumnName) {
        ColumnModel[] cs = model.getColumns ();
        int i, k = cs.length;
        columns = new Column[k];
        //icolumns = new IndexedColumn[k];
        columnVisibleMap = new int[k];
        isDefaultColumnAdded = false;
        boolean addDefaultColumn = true;
        List<Node.Property> columnList = new ArrayList<Node.Property>(k);
        int d = 0;
        for (i = 0; i < k; i++) {
            Column c = new Column(cs [i]);
            columns[i] = c;
            //IndexedColumn ic = new IndexedColumn(c, i, cs[i].getCurrentOrderNumber());
            //icolumns[i] = ic;
            int order = cs[i].getCurrentOrderNumber();
            if (order == -1) {
                order = i;
            }
            order += d;
            columnVisibleMap[i] = order;
            if (cs[i].getType() != null) {
                columnList.add(c);
            } else {
                nodesColumnName[0] = Actions.cutAmpersand(cs[i].getDisplayName());
                addDefaultColumn = false;
                defaultColumnIndex = i;
                if (cs[i].getCurrentOrderNumber() == -1) {
                    // By default let this be the first column and increase the orders
                    columnVisibleMap[i] = 0;
                    for (int j = 0; j < i; j++) {
                        columnVisibleMap[j]++;
                    }
                    d = 1;
                }
            }
        }
        if (addDefaultColumn) {
            PropertySupport.ReadWrite[] columns2 =
                new PropertySupport.ReadWrite [columns.length + 1];
            System.arraycopy (columns, 0, columns2, 1, columns.length);
            columns2 [0] = new DefaultColumn ();
            nodesColumnName[0] = columns2[0].getDisplayName();
            columns = columns2;
            int[] columnVisibleMap2 = new int[columnVisibleMap.length + 1];
            columnVisibleMap2[0] = 0;
            for (i = 0; i < k; i++) {
                columnVisibleMap2[i + 1] = columnVisibleMap[i] + 1;
            }
            columnVisibleMap = columnVisibleMap2;
            isDefaultColumnAdded = true;
            defaultColumnIndex = 0;
        }
        // Check visible map (order) for duplicities and gaps
        checkOrder(columnVisibleMap);

        int[] columnOrder = new int[columnVisibleMap.length];
        System.arraycopy(columnVisibleMap, 0, columnOrder, 0, columnOrder.length);

        for (i = 0; i < columnVisibleMap.length; i++) {
            setColumnOrder(columns[i], columnOrder[i]);
            if (columns[i].isHidden()) {
                int order = columnOrder[i];
                for (int j = 0; j < columnVisibleMap.length; j++) {
                    if (columnOrder[j] >= order && columnVisibleMap[j] > 0) {
                        columnVisibleMap[j]--;
                    }
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createColumns:");
            dumpColumnVisibleMap();
        }

        Node.Property[] columnProps = columnList.toArray(new Node.Property[]{});
        tableColumns = null;
        return columnProps;
    }

    /** Squeeze gaps and split duplicities to make it a permutation. */
    private void checkOrder(int[] orders) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("checkOrder(");
            for (int i = 0; i < orders.length; i++) {
                msg.append(orders[i]);
                msg.append(", ");
            }
            msg.append("\b\b)");
            logger.fine(msg.toString());
        }
        int n = orders.length;
        // Find and squeeze gaps:
        for (int i = 0; i < n; i++) {
            // Check if 'i' order is there:
            int min = Integer.MAX_VALUE;
            int j;
            for (j = 0; j < n; j++) {
                if (i == orders[j]) {
                    break;
                } else if (orders[j] > i) {
                    min = Math.min(min, orders[j]);
                }
            }
            if (j == n && min != Integer.MAX_VALUE) {
                // 'i' not found, shift:
                int shift = min - i;
                for (j = 0; j < n; j++) {
                    if (orders[j] > i) {
                        orders[j] -= shift;
                    }
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("  squeezed: ");
            for (int i = 0; i < orders.length; i++) {
                msg.append(orders[i]);
                msg.append(", ");
            }
            msg.append("\b\b)");
            logger.fine(msg.toString());
        }
        // Find and split duplicities:
        int[] duplicates = new int[n];
        for (int i = 0; i < n; i++) {
            int d = ++duplicates[orders[i]];
            if (d > 1) {
                int o = orders[i];
                for (int j = 0; j < n; j++) {
                    if (orders[j] > o || orders[j] == o && j >= i) {
                        if (j <= i) duplicates[orders[j]]--;
                        orders[j]++;
                        if (j <= i) duplicates[orders[j]]++;
                    }
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("  splitted: ");
            for (int i = 0; i < orders.length; i++) {
                msg.append(orders[i]);
                msg.append(", ");
            }
            msg.append("\b\b)");
            logger.fine(msg.toString());
        }
    }

    private void updateTableColumns(Property[] columnsToSet) {
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        int d = (isDefaultColumnAdded) ? 0 : 1;
        int ci = 0;
        int tci = d;
        TableColumn[] tableColumns = new TableColumn[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (ci < columnsToSet.length && columns[i] == columnsToSet[ci]) {
                TableColumn tc = tcm.getColumn(tci);
                tableColumns[i] = tc;
                if (columns[i].isHidden()) {
                    ecm.setColumnHidden(tc, true);
                } else {
                    tci++;
                }
                ci++;
            } else {
                tableColumns[i] = tcm.getColumn(0);
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateTableColumns("+columns.length+"):");
            for (int i = 0; i < columns.length; i++) {
                logger.fine("Column["+i+"] ("+columns[i].getDisplayName()+") = "+tableColumns[i].getHeaderValue());
            }
        }
        setColumnsOrder();
        this.tableColumns = tableColumns;
    }

    // Re-order the UI columns according to the defined order
    private void setColumnsOrder() {
        logger.fine("setColumnsOrder()");
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        //int[] shift = new int[columns.length];
        int defaultColumnVisibleIndex = 0;
        for (int i = 0; i < defaultColumnIndex; i++) {
            if (!columns[i].isHidden()) defaultColumnVisibleIndex++;
        }
        if (defaultColumnVisibleIndex != 0) {
            logger.fine(" move default column("+0+", "+defaultColumnVisibleIndex+")");
            tcm.moveColumn(0, defaultColumnVisibleIndex);
        }

        int n = tcm.getColumnCount();
        int[] order = new int[n];
        int ci = 0;
        for (int i = 0; i < n; i++, ci++) {
            while (ci < columns.length && columns[ci].isHidden()) ci++;
            if (ci >= columns.length) break;
            order[i] = columnVisibleMap[ci];
            logger.fine("    order["+i+"] = "+order[i]);
        }
        for (int i = 0; i < n; i++) {
            int j = 0;
            for (; j < n; j++) {
                if (order[j] == i) {
                    break;
                }
            }
            logger.fine("  order["+j+"] = "+i);
            if (j != i) {
                for (int k = j; k > i; k--) {
                    order[k] = order[k-1];
                }
                order[i] = i;
                logger.fine(" move column("+j+", "+i+")");
                tcm.moveColumn(j, i);
            }
        }
    }

    private boolean isHiddenColumn(int index) {
        if (tableColumns == null || tableColumns[index] == null) {
            return false;
        }
        ETableColumnModel ecm = (ETableColumnModel) treeTable.getTable().getColumnModel();
        return ecm.isColumnHidden(tableColumns[index]);
    }

    void updateColumnWidthsAndSorting() {
        logger.fine("\nupdateColumnWidthsAndSorting():");
        int i, k = columns.length;
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        for (i = 0; i < k; i++) {
            if (isHiddenColumn(i)) {
                continue;
            }
            int visibleOrder = columnVisibleMap[i];
            logger.fine("  visibleOrder["+i+"] = "+visibleOrder+", ");
            ETableColumn tc;
            try {
                tc = (ETableColumn) tcm.getColumn (visibleOrder);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(aioobex,
                        "Column("+i+") "+columns[i].getName()+" visible index = "+visibleOrder));
                continue ;
            }
            logger.fine("  GUI column = "+tc.getHeaderValue());
            if (columns[i] instanceof Column) {
                Column c = (Column) columns[i];
                logger.fine("    Retrieved width "+c.getColumnWidth()+" from "+columns[i].getDisplayName()+"["+i+"] for "+tc.getHeaderValue());
                tc.setPreferredWidth(c.getColumnWidth());
                if (c.isSorted()) {
                    ecm.setColumnSorted(tc, !c.isSortedDescending(), 1);
                }
            }
        }
    }

    private void saveWidths () {
        if (columns == null) return;
        int i, k = columns.length;
        if (k == 0) return ;
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        Enumeration<TableColumn> etc = tcm.getColumns();
        boolean defaultState = true;
        while(etc.hasMoreElements()) {
            if (etc.nextElement().getWidth() != 75) {
                defaultState = false;
                break;
            }
        }
        if (defaultState) {
            // All columns have the default width 75.
            // It's very likely that the table was not fully initialized => do not save anything.
            return ;
        }
        logger.fine("\nsaveWidths():");
        for (i = 0; i < k; i++) {
            if (isHiddenColumn(i)) {
                continue;
            }
            int visibleOrder = columnVisibleMap[i];
            logger.fine("  visibleOrder["+i+"] = "+visibleOrder+", ");
            TableColumn tc;
            try {
                tc = tcm.getColumn (visibleOrder);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(aioobex,
                        "Column("+i+") "+columns[i].getName()+" visible index = "+visibleOrder));
                continue ;
            }
            logger.fine("  GUI column = "+tc.getHeaderValue());
            if (columns[i] instanceof Column) {
                logger.fine("    Setting width "+tc.getWidth()+" from "+tc.getHeaderValue()+" to "+columns[i].getDisplayName()+"["+i+"]");
                ((Column) columns[i]).setColumnWidth(tc.getWidth());
            }
        }
    }
    
    private void saveSortedState () {
        if (columns == null) return;
        int i, k = columns.length;
        if (k == 0) return ;
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        Enumeration<TableColumn> etc = tcm.getColumns();
        logger.fine("\nsaveSortedState():");
        for (i = 0; i < k; i++) {
            if (isHiddenColumn(i)) {
                continue;
            }
            int visibleOrder = columnVisibleMap[i];
            logger.fine("  visibleOrder["+i+"] = "+visibleOrder+", ");
            ETableColumn tc;
            try {
                tc = (ETableColumn) tcm.getColumn (visibleOrder);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(aioobex,
                        "Column("+i+") "+columns[i].getName()+" visible index = "+visibleOrder));
                continue ;
            }
            logger.fine("  GUI column = "+tc.getHeaderValue());
            if (columns[i] instanceof Column) {
                logger.fine("    Setting sorted "+tc.isSorted()+" descending "+(!tc.isAscending())+" to "+columns[i].getDisplayName()+"["+i+"]");
                ((Column) columns[i]).setSorted(tc.isSorted());
                ((Column) columns[i]).setSortedDescending(!tc.isAscending());
            }
        }
    }

    private void expandDefault (Object[] nodes) {
        int i, k = nodes.length;
        for (i = 0; i < k; i++)
            try {
                if (model.isExpanded (nodes [i]))
                    expandNode (nodes [i]);
            } catch (UnknownTypeException ex) {
            }
    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        return treeTable.requestFocusInWindow ();
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        TopComponent.getRegistry ().addPropertyChangeListener (this);
        ExplorerUtils.activateActions(getExplorerManager (), true);
        getExplorerManager ().addPropertyChangeListener (this);
    }
    
    @Override
    public void removeNotify () {
        TopComponent.getRegistry ().removePropertyChangeListener (this);
        ExplorerUtils.activateActions(getExplorerManager (), false);
        getExplorerManager ().removePropertyChangeListener (this);
        super.removeNotify ();
    }
    
    public boolean isExpanded (Object node) {
        Node n = currentTreeModelRoot.findNode (node);
        if (n == null) return false; // Something what does not exist is not expanded ;-)
        return treeTable.isExpanded (n);
    }

    public void expandNode (Object node) {
        Node n = currentTreeModelRoot.findNode (node);
        if (treeTable != null && n != null)
            treeTable.expandNode (n);
    }

    public void collapseNode (Object node) {
        Node n = currentTreeModelRoot.findNode (node);
        treeTable.collapseNode (n);
    }
    
    private static class MyTreeTable extends OutlineView {
        MyTreeTable () {
            super ();
            Outline outline = getOutline();
            outline.setShowHorizontalLines (true);
            outline.setShowVerticalLines (false);
            filterInputMap(outline, JComponent.WHEN_FOCUSED);
            filterInputMap(outline, JComponent.WHEN_IN_FOCUSED_WINDOW);
            filterInputMap(outline, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }
        
        private void filterInputMap(JComponent component, int condition) {
            InputMap imap = component.getInputMap(condition);
            if (imap instanceof ComponentInputMap) {
                imap = new F8FilterComponentInputMap(component, imap);
            } else {
                imap = new F8FilterInputMap(imap);
            }
            component.setInputMap(condition, imap);
        }
        
        JTable getTable () {
            return getOutline();
        }

        void setNodesColumnName(String name) {
            OutlineModel m = getOutline().getOutlineModel();
            if (m instanceof DefaultOutlineModel) {
                ((DefaultOutlineModel) m).setNodesColumnLabel(name);
            }
        }
        
        /*
        public List getExpandedPaths () {
            List result = new ArrayList ();
            ExplorerManager em = ExplorerManager.find (this);
            TreeNode rtn = Visualizer.findVisualizer (
                em.getRootContext ()
            );
            TreePath tp = new TreePath (rtn); // Get the root
            
            Enumeration exPaths = tree.getExpandedDescendants (tp); 
            if (exPaths == null) return result;
            for (;exPaths.hasMoreElements ();) {
                TreePath ep = (TreePath) exPaths.nextElement ();
                Node en = Visualizer.findNode (ep.getLastPathComponent ());
                String[] path = NodeOp.createPath (en, em.getRootContext ());
                result.add (path);
            }
            return result;
        }
         */
        
        /** Expands all the paths, when exists
         */
        public void expandNodes (List exPaths) {
            for (Iterator it = exPaths.iterator (); it.hasNext ();) {
                String[] sp = (String[]) it.next ();
                TreePath tp = stringPath2TreePath (sp);
                if (tp != null) {
                    getOutline().expandPath(tp);
                    Rectangle rect = getOutline().getPathBounds(tp);
                    if (rect != null) {
                        getOutline().scrollRectToVisible(rect);
                    }
                }
            }
        }

        /** Converts path of strings to TreePath if exists null otherwise
         */
        private TreePath stringPath2TreePath (String[] sp) {
            ExplorerManager em = ExplorerManager.find (this);
            try {
                Node n = NodeOp.findPath (em.getRootContext (), sp); 
                
                // Create the tree path
                TreeNode tns[] = new TreeNode [sp.length + 1];
                
                for (int i = sp.length; i >= 0; i--) {
                    tns[i] = Visualizer.findVisualizer (n);
                    n = n.getParentNode ();
                }                
                return new TreePath (tns);
            } catch (NodeNotFoundException e) {
                return null;
            }
        }
    }
    
    private static final class F8FilterComponentInputMap extends ComponentInputMap {
        
        private KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        
        public F8FilterComponentInputMap(JComponent component, InputMap imap) {
            super(component);
            setParent(imap);
        }

        @Override
        public Object get(KeyStroke keyStroke) {
            if (f8.equals(keyStroke)) {
                return null;
            } else {
                return super.get(keyStroke);
            }
        }
    }
    
    private static final class F8FilterInputMap extends InputMap {
        
        private KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        
        public F8FilterInputMap(InputMap imap) {
            setParent(imap);
        }

        @Override
        public Object get(KeyStroke keyStroke) {
            if (f8.equals(keyStroke)) {
                return null;
            } else {
                return super.get(keyStroke);
            }
        }
    }
    
}

