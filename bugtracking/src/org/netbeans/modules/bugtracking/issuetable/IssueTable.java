/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableColumn;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.openide.util.NbBundle;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.bugtracking.BugtrackingConfig;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.ui.query.IssueTableSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.awt.MouseUtils;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor.Task;

/**
 * @author Tomas Stupka
 */
public class IssueTable implements MouseListener, AncestorListener, KeyListener, PropertyChangeListener {

    private NodeTableModel  tableModel;
    private JTable          table;
    private JScrollPane     component;

    private TableSorter     sorter;

    private Query query;
    private ColumnDescriptor[] descriptors;

    private Filter filter;
    private Filter[] filters;
    private Set<Issue> issues = new HashSet<Issue>();

    private QueryTableHeaderRenderer queryTableHeaderRenderer;

    private Task storeColumnsTask;
    private final StoreColumnsHandler storeColumnsWidthHandler;
    private final JButton colsButton;
    private boolean savedQueryInitialized;
    private SummaryTextFilter textFilter;

    private static final String CONFIG_DELIMITER = "<=>";                       // NOI18N

    /**
     * Implement in an issue to provide access to its IssueNode
     */
    public static interface NodeProvider {
        IssueNode getNode();
    }

    private static final Comparator<IssueProperty> NodeComparator = new Comparator<IssueProperty>() {
        @Override
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
        sorter = new TableSorter(tableModel, this);
        
        initFilters();

        sorter.setColumnComparator(Node.Property.class, NodeComparator);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        ImageIcon ic = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/columns_16.png", true)); // NOI18N
        colsButton = new javax.swing.JButton(ic);
        colsButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TreeTableView.class, "ACN_ColumnsSelector")); //NOI18N
        colsButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TreeTableView.class, "ACD_ColumnsSelector")); //NOI18N
        colsButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if(tableModel.selectVisibleColumns()) {
                        storeColumnsTask.schedule(1000);
                    }
                }
            }
        );
        component.setCorner(JScrollPane.UPPER_RIGHT_CORNER, colsButton);

        table.addMouseListener(this);
        table.addKeyListener(this);
        table.setDefaultRenderer(Node.Property.class, new QueryTableCellRenderer(query, this));
        queryTableHeaderRenderer = new QueryTableHeaderRenderer(table.getTableHeader().getDefaultRenderer(), this, query);
        table.getTableHeader().setDefaultRenderer(queryTableHeaderRenderer);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IssueTable.class, "ACSN_IssueTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IssueTable.class, "ACSD_IssueTable")); // NOI18N

        initColumns();
        table.getTableHeader().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                table.getColumnModel().addColumnModelListener(tcml);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                table.getColumnModel().removeColumnModelListener(tcml);
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        BugtrackingUtil.fixFocusTraversalKeys(table);

        storeColumnsWidthHandler = new StoreColumnsHandler();
        storeColumnsTask = BugtrackingManager.getInstance()
                                                .getRequestProcessor()
                                                .create(storeColumnsWidthHandler);

        IssueTableSupport.getInstance().put(query, this);
    }

    /**
     * Returns the issue table filters
     * @return
     */
    public Filter[] getDefinedFilters() {
        return filters;
    }

    /**
     * Reset the filter criteria set in
     * {@link #setFilterBySummary(java.lang.String, boolean, boolean, boolean) }
     */
    public void resetFilterBySummary() {
        setFilterIntern(filter);
    }

    /**
     * Switch highlighting in rows matching the filter criteria set in
     * {@link #setFilterBySummary(java.lang.String, boolean, boolean, boolean) }
     * @param on
     */
    public void switchFilterBySummaryHighlight(boolean on) {
        assert textFilter != null;
        if(textFilter == null) {
            return;
        }
        textFilter.setHighlighting(on);
        table.repaint();
    }

    /**
     * Given values are used to filter the current issue hitlist
     *
     * @param searchText
     * @param regular
     * @param wholeWords
     * @param matchCase
     */
    public void setFilterBySummary(String searchText, boolean regular, boolean wholeWords, boolean matchCase) {
        if(textFilter == null) {
            textFilter = new SummaryTextFilter();
        }
        textFilter.setText(searchText, regular, wholeWords, matchCase);
        setFilterIntern(textFilter);
    }

    /**
     * Sets the renderer in the underlying JTable
     * @param renderer
     */
    public void setRenderer(TableCellRenderer renderer) {
        table.setDefaultRenderer(Node.Property.class, renderer);
    }

    /**
     * Gets the renderer from the underlying JTable
     * @return
     */
    public TableCellRenderer getRenderer() {
        return table.getDefaultRenderer(Node.Property.class);
    }

    /**
     * Sets a filter on the current issue hitlist
     * @param filter
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
        setFilterIntern(filter);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Query.EVENT_QUERY_SAVED)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    initColumns();
                }
            });
        }
    }

    /**
     * Returns a UI component holding this tables visual representation
     * @return
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * Sets visible columns in the Versioning table.
     *
     * @param columns array of column names, they must be one of SyncFileNode.COLUMN_NAME_XXXXX constants.
     */
    public final void initColumns() {
        if(savedQueryInitialized) {
            return;
        }
        setModelProperties(query);
        if(descriptors.length > 0) {
            Map<Integer, Integer> sorting = getColumnSorting();
            if(descriptors.length > 1) {
                for (int i = 0; i < descriptors.length; i++) {
                    int visibleIdx = tableModel.getVisibleIndex(i);
                    Integer order = sorting.get(visibleIdx);
                    if(order != null) {
                        sorter.setSortingStatus(visibleIdx, order); 
                    } else {
                        if(i == 0) {
                            sorter.setSortingStatus(0, TableSorter.ASCENDING); // default sorting by first column
                        } else {
                            sorter.setColumnComparator(i, null);
                            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                        }                        
                    }
                }
            }
        }
        setDefaultColumnSizes();
        if(query.isSaved()) {
            savedQueryInitialized = true;
        }
    }

    /**
     * Callback from sorter. It also throws an event when the order is changed, unfortunatelly
     * that also applyies for chages caused by refreshing a query and there is no way to 
     * distinguish between those events. 
     */
    void sortOrderChanged() {
        // sorting changed
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < sorter.getColumnCount(); i++) {
            if(i > 0) {
                sb.append(CONFIG_DELIMITER);
            }
            sb.append(i).append(CONFIG_DELIMITER).append(sorter.getSortingStatus(i));
        }
        BugtrackingConfig.getInstance().storeColumnSorting(getColumnsKey(), sb.toString());
    }

    private Map<Integer, Integer> getColumnSorting() {
        String sortingString = BugtrackingConfig.getInstance().getColumnSorting(getColumnsKey());
        if(sortingString == null || sortingString.equals("")) {
            return Collections.EMPTY_MAP;
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        String[] sortingArray = sortingString.split(CONFIG_DELIMITER);
        for (int i = 0; i < sortingArray.length; i+=2) {
            try {
                map.put(Integer.parseInt(sortingArray[i]),
                        Integer.parseInt(sortingArray[i + 1]));
            } catch (NumberFormatException e) {
                BugtrackingManager.LOG.log(Level.FINE, null, e);
            } catch (ArrayIndexOutOfBoundsException e) {
                BugtrackingManager.LOG.log(Level.FINE, null, e);
            }
        }
        return map;
    }

    private void initFilters() {
        filters = new Filter[]{Filter.getAllFilter(query), Filter.getNotSeenFilter(query), Filter.getObsoleteDateFilter(query), Filter.getAllButObsoleteDateFilter(query)};
        filter = filters[0]; // preset the first filter as default
    }
    
    int getSeenColumnIdx() {
        return tableModel.getIndexForPropertyName(IssueNode.LABEL_NAME_SEEN);
    }

    int getRecentChangesColumnIdx() {
        return tableModel.getIndexForPropertyName(IssueNode.LABEL_RECENT_CHANGES);
    }

    private void setFilterIntern(Filter filter) {
        List<IssueNode> issueNodes = new ArrayList<IssueNode>(issues.size());
        for (Issue issue : issues) {
            if (filter == null || filter.accept(issue)) {
                issueNodes.add(((NodeProvider) issue).getNode());
            }
        }
        setTableModel(issueNodes.toArray(new IssueNode[issueNodes.size()]));
    }

    SummaryTextFilter getSummaryFilter() {
        return textFilter;
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
            StringBuilder sb = new StringBuilder();
            sb.append("[");             // NOI18N
            sb.append("bounds=");       // NOI18N
            sb.append(bounds);
            sb.append("]");             // NOI18N
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
            StringBuilder sb = new StringBuilder();
            sb.append("[");         // NOI18N
            sb.append("row=");      // NOI18N
            sb.append(row);
            sb.append(",column=");  // NOI18N
            sb.append(column);
            sb.append("]");         // NOI18N
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
            @Override
            public void run() {
                int[] widths = BugtrackingConfig.getInstance().getColumnWidths(getColumnsKey());
                Map<String, Integer> persistedColumnsMap = getPersistedColumnValues();
                if(persistedColumnsMap.size() > 0) {
                    final TableColumnModel columnModel = table.getColumnModel();
                    int columnCount = columnModel.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        String id = tableModel.getColumnId(i);
                        Integer w = persistedColumnsMap.get(id);
                        if(w != null && w > 0) {
                            setColumnWidth(i, w);
                        }
                    }
                } else if(widths != null && widths.length > 0) {
                    // XXX for backward comp. remove together with BugtrackingConfig.getInstance().getColumnWidths
                    int columnCount = table.getColumnModel().getColumnCount();
                    for (int i = 0; i < widths.length && i < columnCount; i++) {
                        int w = widths[i];
                        if(w > 0) {
                            setColumnWidth(i, w);
                        }
                    }
                } else {
                    ColumnDescriptor[] visibleDescriptors = getVisibleDescriptors();
                    for (int i = 0; i < visibleDescriptors.length; i++) {
                        ColumnDescriptor desc = visibleDescriptors[i];
                        int w = desc.getWidth();
                        if(w > 0) {
                            setColumnWidth(i, w);
                        } else if(w == 0) {
                            setWidthForFit(i);
                        }
                    }
                    if(query.isSaved()) {
                        int w = BugtrackingUtil.getColumnWidthInPixels(25, table);
                        setColumnWidth(getRecentChangesColumnIdx(), w);
                    }
                }

                if(query.isSaved()) {
                    int seenIdx = getSeenColumnIdx();
                    table.getColumnModel().getColumn(seenIdx).setMaxWidth(28);
                    table.getColumnModel().getColumn(seenIdx).setPreferredWidth(28);
                }
            }

            private void setColumnWidth(int i, int w) {
                table.getColumnModel().getColumn(i).setMinWidth(10);
                table.getColumnModel().getColumn(i).setMaxWidth(10000);
                table.getColumnModel().getColumn(i).setPreferredWidth(w);
            }

            private void setWidthForFit(int i) {
                TableColumn c = table.getColumnModel().getColumn(i);
                Component comp = queryTableHeaderRenderer.getTableCellRendererComponent(table, c.getHeaderValue(), false, false, 0, i);
                if(comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    int w = label.getPreferredSize().width;
                    if(w > -1) {
                        setColumnWidth(i, w);
                    }
                }
            }

        });
    }

    private ColumnDescriptor[] getVisibleDescriptors() {
        List<ColumnDescriptor> visible = new LinkedList<ColumnDescriptor>();
        for (ColumnDescriptor d : descriptors) {
            if(d.isVisible()) {
                visible.add(d);
            }
        }
        return visible.toArray(new ColumnDescriptor[visible.size()]);
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    @Override
    public void ancestorMoved(AncestorEvent event) { }

    @Override
    public void ancestorRemoved(AncestorEvent event) { }

    private void setModelProperties(Query query) {
        List<ColumnDescriptor> properties = new ArrayList<ColumnDescriptor>(descriptors.length + (query.isSaved() ? 2 : 0));
        int i = 0;
        for (; i < descriptors.length; i++) {
            ColumnDescriptor desc = descriptors[i];
            properties.add(desc);
        }
        if(query.isSaved()) {
            properties.add(new RecentChangesDescriptor());
            properties.add(new SeenDescriptor());
        }

        // set visibility dependeing on persisted values
        Map<String, Integer> persistedColumnsMap = getPersistedColumnValues();
        if(persistedColumnsMap.size() > 0) {
            for (ColumnDescriptor cd : properties) {
                cd.setVisible(persistedColumnsMap.containsKey(cd.getName()));
            }
        }
        descriptors = properties.toArray(new ColumnDescriptor[properties.size()]);
        tableModel.setProperties(descriptors);        
    }

    private Map<String, Integer> getPersistedColumnValues() {
        String columns = BugtrackingConfig.getInstance().getColumns(getColumnsKey());
        String[] visibleColumns = columns.split(CONFIG_DELIMITER);                         // NOI18N
        if(visibleColumns.length <= 1) {
            return Collections.EMPTY_MAP;
        }
        Map<String, Integer> ret = new HashMap<String, Integer>();
        for (int i = 0; i < visibleColumns.length; i=i+2) {
            try {
                ret.put(visibleColumns[i], Integer.parseInt(visibleColumns[i + 1]));
            } catch (NumberFormatException nfe) {
                ret.put(visibleColumns[i], -1);
                BugtrackingManager.LOG.log(Level.WARNING, visibleColumns[i], nfe);
            }
        }
        return ret;
    }

    private void setTableModel(IssueNode[] nodes) {
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int column = table.columnAtPoint(e.getPoint());
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
                if(column == getSeenColumnIdx()) {
                    IssueNode in = (IssueNode) tableModel.getNodes()[row];
                    final Issue issue = in.getLookup().lookup(Issue.class);
                    BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            IssueCacheUtils.switchSeen(issue);
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

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\n') {                                     // NOI18N
            int row = table.getSelectedRow();
            if (row != -1) {
                row = sorter.modelIndex(row);
                Action action = tableModel.getNodes()[row].getPreferredAction();
                if (action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == '\n') {                                     // NOI18N
            int row = table.getSelectedRow();
            if (row != -1) {
                // Hack for bug 4486444
                e.consume();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    private class NotifyListener implements QueryNotifyListener {
        @Override
        public void notifyData(final Issue issue) {
            assert issue instanceof NodeProvider;
            issues.add(issue);
            if(filter == null || filter.accept(issue)) {
                final IssueNode node = ((NodeProvider)issue).getNode();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        tableModel.insertNode(node);
                    }
                });
            }
        }
        @Override
        public void started() {
            issues.clear();
            IssueTable.this.setTableModel(new IssueNode[0]);
        }
        @Override
        public void finished() { }
    }

    private class SeenDescriptor extends ColumnDescriptor<Boolean> {
        public SeenDescriptor() {
            super(IssueNode.LABEL_NAME_SEEN, Boolean.class, "", NbBundle.getBundle(Issue.class).getString("CTL_Issue_Seen_Desc"), -1, true, true); // NOI18N
        }
    }

    private class RecentChangesDescriptor extends ColumnDescriptor<String> {
        public RecentChangesDescriptor() {
            super(IssueNode.LABEL_RECENT_CHANGES, String.class, NbBundle.getBundle(Issue.class).getString("CTL_Issue_Recent"), NbBundle.getBundle(Issue.class).getString("CTL_Issue_Recent_Desc"), -1, true, true); // NOI18N
        }
    }

    private String getColumnsKey() {
        String name = query.getDisplayName();
        if(name == null) {
            name = "#find#issues#hitlist#table#";               // NOI18N
        }
        return query.getRepository().getID() + ":" + name;      // NOI18N
    }

    private class StoreColumnsHandler implements Runnable {
        @Override
        public void run() {            
            TableColumnModel cm = table.getColumnModel();
            int count = cm.getColumnCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(tableModel.getColumnId(i));
                sb.append(CONFIG_DELIMITER);                                               // NOI18N
                sb.append(cm.getColumn(i).getWidth());
                if(i < count - 1) {
                    sb.append(CONFIG_DELIMITER);                                           // NOI18N
                }
            }
            BugtrackingConfig.getInstance().storeColumns(getColumnsKey(), sb.toString());
        }
    }

    private TableColumnModelListener tcml = new TableColumnModelListener() {
        @Override
        public void columnAdded(TableColumnModelEvent e) {}
        @Override
        public void columnRemoved(TableColumnModelEvent e) {}
        @Override
        public void columnMoved(TableColumnModelEvent e) {
            int from = e.getFromIndex();
            int to = e.getToIndex();
            if(from == to) {
                return;
            }
            table.getTableHeader().getColumnModel().getColumn(from).setModelIndex(from);
            table.getTableHeader().getColumnModel().getColumn(to).setModelIndex(to);
            tableModel.moveColumn(from, to);
        }
        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {}
        @Override
        public void columnMarginChanged(ChangeEvent e) {
            storeColumnsTask.schedule(1000);
        }
    };

}

