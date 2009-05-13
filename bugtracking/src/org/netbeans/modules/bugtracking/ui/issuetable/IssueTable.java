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

package org.netbeans.modules.bugtracking.ui.issuetable;

import java.awt.Component;
import java.io.IOException;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.Query.ColumnDescriptor;
import org.openide.util.NbBundle;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Query.Filter;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * @author Tomas Stupka
 */
public class IssueTable implements MouseListener, AncestorListener, KeyListener {

    private NodeTableModel  tableModel;
    private JTable          table;
    private JScrollPane     component;
    
    private TableSorter     sorter;

    private int seenColumnIdx = -1;
    private Query query;

    private Filter filter;

    private static final MessageFormat issueNewFormat = getFormat("issueNewFormat");     // NOI18N
    private static final MessageFormat issueObsoleteFormat = getFormat("issueObsoleteFormat"); // NOI18N
    private static final MessageFormat issueModifiedFormat = getFormat("issueModifiedFormat"); // NOI18N
    private static final Color unevenLineColor = new Color(0xf3f6fd);
    private static final Color newHighlightColor = new Color(0x00b400);
    private static final Color modifiedHighlightColor = new Color(0x0000ff);
    private static final Color obsoleteHighlightColor = new Color(0x999999);
    private static final Color defaultForegroundColor = new JLabel().getForeground();

    private static Icon seenHeaderIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/seen-header.png")); // NOI18N
    private static Icon seenValueIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/seen-value.png")); // NOI18N
    private Set<Issue> issues = new HashSet<Issue>();

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
    
    public IssueTable(Query query) {
        this.query = query; 
        query.addNotifyListener(new NotifyListener());

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
        table.setDefaultRenderer(Node.Property.class, new CellRenderer());
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer(table.getTableHeader().getDefaultRenderer()));
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IssueTable.class, "ACSN_IssueTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IssueTable.class, "ACSD_IssueTable")); // NOI18N
        initColumns();
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        BugtrackingUtil.fixFocusTraversalKeys(table);
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

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(query.isSaved()) {
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
        ColumnDescriptor[] descs = query.getColumnDescriptors();
        for (int i = 0; i < descs.length; i++) {
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
        ColumnDescriptor[] descs = query.getColumnDescriptors();
        List<Node.Property> properties = new ArrayList<Node.Property>(descs.length + (query.isSaved() ? 2 : 0));
        int i = 0;
        for (; i < descs.length; i++) {
            ColumnDescriptor desc = descs[i];
            properties.add(desc);
        }
        if(query.isSaved()) {
            properties.add(new RecentChangesDescriptor());
            properties.add(new SeenDescriptor());
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
        if (SwingUtilities.isLeftMouseButton(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) return;
            row = sorter.modelIndex(row);
            if(MouseUtils.isDoubleClick(e)) {
                Action action = tableModel.getNodes()[row].getPreferredAction();
                if (action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            } else {
                int column = table.columnAtPoint(e.getPoint());
                if(column != seenColumnIdx) return;
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
        }
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(IssueTable.class, key);
        return new MessageFormat(format);
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
    
    private class CellRenderer extends DefaultTableCellRenderer {
        private JLabel seenCell = new JLabel();
        private static final int VISIBLE_START_CHARS = 0;
                
        private String computeFitText(String text) {
            if (text == null || text.length() <= VISIBLE_START_CHARS + 3) return text;

            FontMetrics fm = getFontMetrics(getFont());
            int width = getSize().width;

            String sufix = "..."; // NOI18N
            int sufixLength = fm.stringWidth(sufix);
            int desired = width - sufixLength - 10;
            if (desired <= 0) return text;

            for (int i = 0; i <= text.length() - 1; i++) {
                String prefix = text.substring(0, i);
                int swidth = fm.stringWidth(prefix);
                if (swidth >= desired) {
                    return prefix.length() > 0 ? prefix + sufix: text;
                }
            }
            return text;
        }

        public CellRenderer() {
            seenCell.setBackground(Color.RED);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            MessageFormat format = null;
            Color background = null;
            Color foreground = null;
            Component renderer = null;
            String tooltip = null;
            if(value instanceof IssueNode.SeenProperty) {
                IssueNode.SeenProperty ps = (IssueNode.SeenProperty) value;
                seenCell.setIcon(!ps.getValue() ? seenValueIcon : null);
                renderer = seenCell;
            }
            Issue issue = null;
            if(query.isSaved() && value instanceof IssueNode.IssueProperty) {
                IssueProperty p = (IssueNode.IssueProperty) value;
                try {
                    issue = p.getIssue();
                    if(!query.contains(issue)) {
                        format = issueObsoleteFormat;
                        if(isSelected) {
                            background = obsoleteHighlightColor;
                        } else {
                            background = row % 2 != 0 ? unevenLineColor : Color.WHITE;
                        }
                    } else {
                        int status = query.getIssueStatus(issue);
                        if(!issue.wasSeen()) {
                            switch(status) {
                                case Issue.ISSUE_STATUS_NEW :
                                    format = issueNewFormat;
                                    background = newHighlightColor;
                                    break;
                                case Issue.ISSUE_STATUS_MODIFIED :
                                    format = issueModifiedFormat;
                                    background = modifiedHighlightColor;
                                    break;
                            }
                        }
                    }
                    Object o = p.getValue();
                    if(o instanceof String) {
                        tooltip = (String) o;
                    }
                } catch (Exception ex) {
                    BugtrackingManager.LOG.log(Level.WARNING, null, ex);
                }
            }
            if(renderer == null) {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            if(isSelected) {
                format = null;
                if(issue != null && issue.wasSeen()) {
                    foreground = Color.WHITE;
                }
            } else {
                background = row % 2 != 0 ? unevenLineColor : Color.WHITE;
                foreground = defaultForegroundColor;
            }
            if(renderer instanceof JComponent) {
                JComponent l = (JComponent) renderer;
                l.putClientProperty("format", format);                          // NOI18N
                ((JComponent) renderer).setToolTipText(tooltip);
                if(background != null) {
                    l.setBackground(background);
                }
                if(foreground != null) {
                    l.setForeground(foreground);
                } 
            }
            return renderer;
        }

        @Override
        protected void paintComponent(Graphics g) {
            MessageFormat format = (MessageFormat) getClientProperty("format"); // NOI18N
            String s = computeFitText(getText());
            if(format != null) {
                StringBuffer sb = new StringBuffer();
                sb.append("<html>"); // NOI18N
                format.format(new Object[] {s}, sb, null);
                sb.append("</html>"); // NOI18N
                s = sb.toString();
            }
            setText(s);
            super.paintComponent(g);
        }
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        private JLabel seenCell = new JLabel();
        private TableCellRenderer delegate;

        public HeaderRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
            seenCell.setIcon(seenHeaderIcon);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if(query.isSaved() && column == seenColumnIdx) {

                Component c = delegate.getTableCellRendererComponent (table, value,
                                                       isSelected,
                                                       hasFocus,
                                                       row, column);

                seenCell.setFont (c.getFont ());
                seenCell.setForeground (c.getForeground ());
                seenCell.setBorder (((JComponent) c).getBorder ());

                return seenCell;
            } else {
                return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
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

