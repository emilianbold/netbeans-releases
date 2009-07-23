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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.bugtracking.issuetable;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueNode.IssueProperty;
import org.openide.util.NbBundle;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;

/**
 * @author Tomas Stupka
 */
public class IssueTable implements MouseListener, AncestorListener, KeyListener, PropertyChangeListener {

    private NodeTableModel  tableModel;
    private JTable          table;
    private JScrollPane     component;
    
    private TableSorter     sorter;

    private int seenColumnIdx = -1;
    private int recentChangesColumnIdx = -1;
    private Query query;
    private final ColumnDescriptor[] descriptors;

    private Filter filter;        
    private Filter[] filters;
    private Set<Issue> issues = new HashSet<Issue>();

    /**
     * Returns the issue table filters
     * @return
     */
    public Filter[] getDefinedFilters() {
        if(filters == null) {
            filters = new Filter[] {
                Filter.getAllFilter(query),
                Filter.getNewFilter(query),
                Filter.getNotSeenFilter(),
                Filter.getObsoleteDateFilter(query),
                 Filter.getAllButObsoleteDateFilter(query)
            };
        }
        return filters;
    }

    private static final Comparator<IssueProperty> NodeComparator = new Comparator<IssueProperty>() {
        public int compare(IssueProperty p1, IssueProperty p2) {
            Integer sk1 = (Integer) p1.getValue("sortkey"); // NOI18N
            if (sk1 != null) {
                Integer sk2 = (Integer) p2.getValue("sortkey"); // NOI18N
                return sk1.compareTo(sk2);
            } else {
                try {
                    return p1.compareTo(p2);
                } catch (Exception e) {
                    BugtrackingManager.LOG.log(Level.SEVERE, null, e);
                    return 0;
                }
            }
        }
    };

    
    public IssueTable(Query query, ColumnDescriptor[] descriptors) {
        assert query != null;
        assert descriptors != null;
        assert descriptors.length > 0;
        
        this.query = query;

        this.descriptors = descriptors;
        query.addNotifyListener(new NotifyListener());
        query.addPropertyChangeListener(this);

        tableModel = new NodeTableModel();
        sorter = new TableSorter(tableModel);

        sorter.setColumnComparator(Node.Property.class, NodeComparator);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.addKeyListener(this);
        table.setDefaultRenderer(Node.Property.class, new QueryTableCellRenderer(query));
        table.getTableHeader().setDefaultRenderer(new QueryTableHeaderRenderer(table.getTableHeader().getDefaultRenderer(), this, query));
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IssueTable.class, "ACSN_IssueTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IssueTable.class, "ACSD_IssueTable")); // NOI18N
        initColumns();
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        BugtrackingUtil.fixFocusTraversalKeys(table);
    }

    int getSeenColumnIdx() {
        return seenColumnIdx;
    }    

    public void setRenderer(TableCellRenderer renderer) {
        table.setDefaultRenderer(Node.Property.class, renderer);
    }

    public TableCellRenderer getRenderer() {
        return table.getDefaultRenderer(Node.Property.class);
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        List<IssueNode> issueNodes = new ArrayList<IssueNode>(issues.size());
        for (Issue issue : issues) {
            if(filter == null || filter.accept(issue)) {
                issueNodes.add(issue.getNode());
            }
        }
        setTableModel(issueNodes.toArray(new IssueNode[issueNodes.size()]));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Query.EVENT_QUERY_SAVED)) {
            initColumns();
        }
    }

    private class CellAction implements ActionListener {
        private final Rectangle bounds;
        private final ActionListener listener;
        public CellAction(Rectangle bounds, ActionListener listener) {
            this.bounds = bounds;
            this.listener = listener;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CellAction other = (CellAction) obj;
            if (this.bounds != other.bounds && (this.bounds == null || !this.bounds.equals(other.bounds))) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append("bounds=");
            sb.append(bounds);
            sb.append("]");
            return sb.toString();
        }
        public void actionPerformed(ActionEvent e) {
            listener.actionPerformed(e);
        }
    }
    private class Cell {
        private final int row;
        private final int column;
        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cell other = (Cell) obj;
            if (this.row != other.row) {
                return false;
            }
            if (this.column != other.column) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append("row=");
            sb.append(row);
            sb.append(",column=");
            sb.append(column);
            sb.append("]");
            return sb.toString();
        }
    }
    private final Map<Cell, Set<CellAction>> cellActions = new HashMap<Cell, Set<CellAction>>();
    public void addCellAction(int row, int column, Rectangle bounds, ActionListener l) {
        synchronized(cellActions) {
            Cell cell = new Cell(row, column);
            Set<CellAction> actions = cellActions.get(cell);
            if(actions == null) {
                actions = new HashSet<CellAction>(1);
                cellActions.put(cell, actions);
            }
            actions.add(new CellAction(bounds, l));
        }
    }

    public void removeCellActions(int row, int column) {
        Cell cell = new Cell(row, column);
        synchronized(cellActions) {
            cellActions.remove(cell);
        }
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (int i = 0; i < descriptors.length; i++) {
                    ColumnDescriptor desc = descriptors[i];
                    int w = desc.getWidth();
                    if(w > 0) {                        
                        table.getColumnModel().getColumn(i).setMinWidth(10);
                        table.getColumnModel().getColumn(i).setMaxWidth(10000);
                        table.getColumnModel().getColumn(i).setPreferredWidth(w);
                    }
                }

                if(query.isSaved()) {
                    table.getColumnModel().getColumn(recentChangesColumnIdx).setMaxWidth(10000);
                    table.getColumnModel().getColumn(recentChangesColumnIdx).setMinWidth(10);
                    table.getColumnModel().getColumn(recentChangesColumnIdx).setPreferredWidth(BugtrackingUtil.getColumnWidthInPixels(25, table));

                    table.getColumnModel().getColumn(seenColumnIdx).setMaxWidth(28);
                    table.getColumnModel().getColumn(seenColumnIdx).setPreferredWidth(28);
                }
            }
        });
    }    

    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {

    }

    public JComponent getComponent() {
        return component;
    }

    /**
     * Sets visible columns in the Versioning table.
     * 
     * @param columns array of column names, they must be one of SyncFileNode.COLUMN_NAME_XXXXX constants.  
     */ 
    public final void initColumns() {
        setModelProperties(query);        
        for (int i = 0; i < descriptors.length; i++) {
            sorter.setColumnComparator(i, null);
            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
//            if (IssueNode.COLUMN_NAME_STATUS.equals(tableColumns[i])) {
//                sorter.setSortingStatus(i, TableSorter.ASCENDING);
//                break;
//            }
        }
        setDefaultColumnSizes();        
    }

    private void setModelProperties(Query query) {        
        List<Node.Property> properties = new ArrayList<Node.Property>(descriptors.length + (query.isSaved() ? 2 : 0));
        int i = 0;
        for (; i < descriptors.length; i++) {
            ColumnDescriptor desc = descriptors[i];
            properties.add(desc);
        }
        if(query.isSaved()) {
            properties.add(new RecentChangesDescriptor());
            properties.add(new SeenDescriptor());
            recentChangesColumnIdx = i;
            seenColumnIdx = i + 1;
        }

        tableModel.setProperties(properties.toArray(new Node.Property[properties.size()]));
    }

    private void setTableModel(IssueNode[] nodes) {
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int column = table.columnAtPoint(e.getPoint());
        Rectangle rect;
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (row == -1) return;
            row = sorter.modelIndex(row);
            if(MouseUtils.isDoubleClick(e)) {
                Action action = tableModel.getNodes()[row].getPreferredAction();
                if (action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            } else {

                // seen column
                if(column == seenColumnIdx) {
                    IssueNode in = (IssueNode) tableModel.getNodes()[row];
                    final Issue issue = in.getLookup().lookup(Issue.class);
                    BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                        public void run() {
                            try {
                                issue.setSeen(!issue.wasSeen());
                            } catch (IOException ex) {
                                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                }
                // check for action
                CellAction[] actions = null;
                synchronized(cellActions) {
                    Cell cell = new Cell(row, column);
                    Set<CellAction> set = cellActions.get(cell);
                    actions = set != null ? set.toArray(new CellAction[set.size()]) : null;
                }
                if(actions != null) {
                    for (CellAction cellAction : actions) {
                        cellAction.actionPerformed(null);
                    }
                }
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\n') {
            int row = table.getSelectedRow();
            if (row != -1) {
                Action action = tableModel.getNodes()[row].getPreferredAction();
                if (action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == '\n') {
            int row = table.getSelectedRow();
            if (row != -1) {
                // Hack for bug 4486444
                e.consume();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    private class NotifyListener implements QueryNotifyListener {
        public void notifyData(final Issue issue) {
            issues.add(issue);
            if(filter == null || filter.accept(issue)) {
                final IssueNode node = issue.getNode();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        tableModel.insertNode(node);
                    }
                });
            }
        }
        public void started() {
            issues.clear();
            IssueTable.this.setTableModel(new IssueNode[0]);
        }
        public void finished() { }
    }

    private class SeenDescriptor extends ColumnDescriptor<Boolean> {
        public SeenDescriptor() {
            super(Issue.LABEL_NAME_SEEN, Boolean.class, "", NbBundle.getBundle(Issue.class).getString("CTL_Issue_Seen_Desc")); // NOI18N
        }
    }

    private class RecentChangesDescriptor extends ColumnDescriptor<String> {
        public RecentChangesDescriptor() {
            super(Issue.LABEL_RECENT_CHANGES, String.class, NbBundle.getBundle(Issue.class).getString("CTL_Issue_Recent"), NbBundle.getBundle(Issue.class).getString("CTL_Issue_Recent_Desc")); // NOI18N
        }
    }

}

