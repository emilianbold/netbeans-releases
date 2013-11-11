
package org.netbeans.modules.odcs.tasks.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.SummaryProperty;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 *
 */
public class QueryCellRenderer implements TableCellRenderer {
    private static final MessageFormat subtasksFormat = getFormat("subtasksFormat");  // NOI18N
    private static final MessageFormat parentFormat = getFormat("parentFormat");      // NOI18N

    private final ODCSQuery odcsQuery;
    private Query query;
    private final QueryTableCellRenderer defaultIssueRenderer;
    private TwoLabelPanel twoLabelPanel;

    private static Icon subtaskIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/odcs/tasks/resources/subtask.png"));    // NOI18N

    private final IssueTable issueTable;
    private boolean resetRowHeight;

    private Map<Integer, Integer> tooLargeRows = new HashMap<Integer, Integer>();

    public QueryCellRenderer(ODCSQuery odcsQuery, IssueTable issueTable, QueryTableCellRenderer defaultIssueRenderer) {
        this.defaultIssueRenderer = defaultIssueRenderer;
        this.issueTable = issueTable;
        this.odcsQuery = odcsQuery;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        fixRowHeightIfNeeded(table, row);

        if(!(value instanceof SummaryProperty)) {
            return defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        SummaryProperty summaryProperty = (SummaryProperty) value;
        ODCSIssue issue = (ODCSIssue) summaryProperty.getIssueData();

        if(issue.hasSubtasks() || issue.isSubtask()) {
            return getSubtaskRenderer(issue, summaryProperty, table, isSelected, row);
        }
        return defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    private Component getSubtaskRenderer(ODCSIssue issue, SummaryProperty summaryProperty, JTable table, boolean isSelected, int row) {
        TwoLabelPanel panel = getTwoLabelPanel();
        RendererLabel label = !issue.getSubtasks().isEmpty() ? panel.north : panel.south;
        String summary = summaryProperty.getValue();
        TableCellStyle style = getStyle(table, summaryProperty, isSelected, row);
        MessageFormat northformat = issue.hasSubtasks() ? (style != null ? style.getFormat() : null) : (isSelected ? null : parentFormat);
        MessageFormat southformat = issue.hasSubtasks() ? (isSelected ? null : subtasksFormat) : (style != null ? style.getFormat() : null);
        String northtext = issue.hasSubtasks() ? summary : issue.getParentId();
        String southtext = issue.hasSubtasks() ? getSubtaskKeys(issue) : summary;
        panel.setFontSize(table.getFont(), label);
        panel.north.setText(northtext);
        panel.north.putClientProperty(QueryTableCellRenderer.PROPERTY_FORMAT, northformat);
        panel.north.putClientProperty(QueryTableCellRenderer.PROPERTY_HIGHLIGHT_PATTERN, style.getHighlightPattern());
        panel.south.setText(southtext);
        panel.south.putClientProperty(QueryTableCellRenderer.PROPERTY_FORMAT, southformat);
        panel.south.putClientProperty(QueryTableCellRenderer.PROPERTY_HIGHLIGHT_PATTERN, style.getHighlightPattern());
        panel.setToolTipText(summary);
        setRowColors(style, panel);
        adjustRowHeightIfNeeded(panel, table, row, true);
        return panel;
    }

    private TableCellStyle getStyle(JTable table, IssueProperty p, boolean isSelected, int row) {
        TableCellStyle style;
        if (odcsQuery.isSaved()) {
            style = QueryTableCellRenderer.getCellStyle(table, issueTable, p, isSelected, row);
        } else {
            style = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, p, isSelected, row);
        }
        return style;
    }

    private String getSubtaskKeys(ODCSIssue issue) {
        Collection<String> keys = issue.getSubtasks();
        StringBuilder keysBuffer = new StringBuilder();
        Iterator<String> it = keys.iterator();
        while(it.hasNext()) {
            keysBuffer.append(it.next());
            if (it.hasNext()) {
                keysBuffer.append(", "); // NOI18N
            }
        }
        return keysBuffer.toString();
    }

    private int defaultRowHeight = -1;
    public void resetDefaultRowHeight() {
        resetRowHeight = true;
    }

    private void adjustRowHeightIfNeeded(JPanel panel, JTable table, int row, boolean subtask) {

        if(defaultRowHeight == -1) {
            defaultRowHeight = table.getRowHeight();
        }

        int h = (int) panel.getPreferredSize().getHeight();
        h = h + table.getRowMargin();
        if(!subtask) {
            table.setRowHeight(row, h);
            if(h > 2 * defaultRowHeight) {
                tooLargeRows.put(row, h);
            }
        } else if (table.getRowHeight(row) < h) {
            table.setRowHeight(h);
            for (Map.Entry<Integer, Integer> e : tooLargeRows.entrySet()) {
                table.setRowHeight(e.getKey(), e.getValue());
            }
        } 
    }

    private static MessageFormat getFormat(String key) {
        return new MessageFormat(NbBundle.getMessage(QueryCellRenderer.class, key));
    }

    private void fixRowHeightIfNeeded(JTable table, int row) {
        if(defaultRowHeight == -1) {
            return;
        }
        if (resetRowHeight && table.getRowHeight(row) != defaultRowHeight) {
            table.setRowHeight(defaultRowHeight);
            resetRowHeight = false;
        }
    }

    private TwoLabelPanel getTwoLabelPanel() {
        if(twoLabelPanel == null) {
            twoLabelPanel = new TwoLabelPanel();
        }
        return twoLabelPanel;
    }

    private void setRowColors(TableCellStyle style, TwoLabelPanel panel) {
        QueryTableCellRenderer.setRowColors(style, panel.north);
        QueryTableCellRenderer.setRowColors(style, panel.south);
        QueryTableCellRenderer.setRowColors(style, panel);
    }

    /**
     * DO NOT call if query not saved yet.
     * 
     * @return 
     */
    private Query getQuery() {
        if(query == null)  {
            assert odcsQuery.isSaved();
            Repository repository = ODCSUtil.getRepository(odcsQuery.getRepository());
            Collection<Query> queries = repository.getQueries();
            Query aQuery = null;
            for (Query q : queries) {
                if(q.getDisplayName().equals(odcsQuery.getDisplayName())) {
                    aQuery = q;
                    break;
                }
            }
            this.query = aQuery;
            assert query != null;        
        }
        return query;
    }

    private class TwoLabelPanel extends JPanel {
        RendererLabel north = new RendererLabel();
        RendererLabel south = new RendererLabel();
        public TwoLabelPanel() {
            setLayout(new BorderLayout());
            add(north, BorderLayout.NORTH);
            add(south, BorderLayout.SOUTH);
            north.setFont(defaultIssueRenderer.getFont());
            south.setFont(defaultIssueRenderer.getFont());
            south.setIcon(subtaskIcon);
        }
        void setFontSize(Font defaultFont, RendererLabel defaultLabel) {
            Font smalerFont = new Font(defaultFont.getName(), defaultFont.getStyle(), (int) (defaultFont.getSize() * .85));
            if(defaultLabel == north) {
                north.setFont(defaultFont);
                south.setFont(smalerFont);
            } else {
                north.setFont(smalerFont);
                south.setFont(defaultFont);
            }
        }
    }

    private class MultiLabelPanel extends JPanel {
        public MultiLabelPanel() {
            setLayout(new GridBagLayout());
        }
    }

    private class RendererLabel extends JLabel {
        @Override
        public void paint(Graphics g) {
            QueryTableCellRenderer.processText(this);
            super.paint(g);
        }
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        @Override
        public void invalidate() {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        @Override
        public void validate() {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        @Override
        public void revalidate() {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        @Override
        public void repaint(long tm, int x, int y, int width, int height) {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        @Override
        public void repaint(Rectangle r) { }
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        @Override
        public void repaint() {}
    }

}
