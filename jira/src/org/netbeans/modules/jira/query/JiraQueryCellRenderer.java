
package org.netbeans.modules.jira.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.TableCellRenderer;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.SummaryProperty;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.issue.JiraIssueNode.MultiValueFieldProperty;
import org.netbeans.modules.jira.issue.JiraIssueNode.PriorityProperty;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 *
 */
public class JiraQueryCellRenderer implements TableCellRenderer {
    private static final MessageFormat subtasksFormat = getFormat("subtasksFormat");  // NOI18N
    private static final MessageFormat parentFormat = getFormat("parentFormat");      // NOI18N

    private final JiraQuery query;
    private final QueryTableCellRenderer defaultIssueRenderer;
    private TwoLabelPanel twoLabelPanel;
    private MultiLabelPanel multiLabelPanel;

    private static Icon subtaskIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/jira/resources/subtask.png"));    // NOI18N

    private final IssueTable issueTable;
    private boolean resetRowHeight;

    private Map<Integer, Integer> tooLargeRows = new HashMap<Integer, Integer>();

    public JiraQueryCellRenderer(JiraQuery query, IssueTable issueTable, QueryTableCellRenderer defaultIssueRenderer) {
        this.query = query;
        this.defaultIssueRenderer = defaultIssueRenderer;
        this.issueTable = issueTable;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        fixRowHeightIfNeeded(table, row);

        if(!(value instanceof SummaryProperty) && !(value instanceof MultiValueFieldProperty)) {
            JLabel renderer = (JLabel) defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(value instanceof PriorityProperty) {
                return getPriorityRenderer(value, renderer);
            }
            return renderer;
        }

        if(value instanceof MultiValueFieldProperty) {
            return getMultivalueRenderer(value, table, isSelected, hasFocus, row, column);
        }

        SummaryProperty summaryProperty = (SummaryProperty) value;
        NbJiraIssue issue = (NbJiraIssue) summaryProperty.getIssue();

        if(issue.hasSubtasks() || issue.isSubtask()) {
            return getSubtaskRenderer(issue, summaryProperty, table, isSelected, row);
        }
        return defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    private Component getPriorityRenderer(Object value, JLabel renderer) {
        PriorityProperty property = (PriorityProperty) value;
        Priority priority = property.getPriority();
        Icon icon = JiraConfig.getInstance().getPriorityIcon(priority.getId());
        renderer.setIcon(icon);
        return renderer;
    }

    private Component getMultivalueRenderer(Object value, JTable table, boolean isSelected, boolean hasFocus, int row, int column) {
        MultiValueFieldProperty p = (MultiValueFieldProperty) value;
        MultiLabelPanel panel = getMultiLabelPanel();
        String stringValue = p.getValue();
        String[] values = stringValue.split(","); // NOI18N
        if (values.length < 2) {
            return (JLabel) defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        TableCellStyle style = getStyle(table, p, isSelected, row);
        for (int i = 0; i < values.length; i++) {
            RendererLabel label = new RendererLabel();
            String s = values[i];
            label.setFont(defaultIssueRenderer.getFont());
            label.setText(s);
            label.putClientProperty(QueryTableCellRenderer.PROPERTY_FORMAT, style.getFormat()); // NOI18N
            label.putClientProperty(QueryTableCellRenderer.PROPERTY_HIGHLIGHT_PATTERN, style.getHighlightPattern()); // NOI18N
            QueryTableCellRenderer.setRowColors(style, label);
            QueryTableCellRenderer.setRowColors(style, panel);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.gridy = i;
            panel.add(label, c);
        }
        adjustRowHeightIfNeeded(panel, table, row, false);
        return panel;
    }

    private Component getSubtaskRenderer(NbJiraIssue issue, SummaryProperty summaryProperty, JTable table, boolean isSelected, int row) {
        TwoLabelPanel panel = getTwoLabelPanel();
        RendererLabel label = issue.hasSubtasks() ? panel.north : panel.south;
        String summary = summaryProperty.getValue();
        TableCellStyle style = getStyle(table, summaryProperty, isSelected, row);
        MessageFormat northformat = issue.hasSubtasks() ? (style != null ? style.getFormat() : null) : (isSelected ? null : parentFormat);
        MessageFormat southformat = issue.hasSubtasks() ? (isSelected ? null : subtasksFormat) : (style != null ? style.getFormat() : null);
        String northtext = issue.hasSubtasks() ? summary : issue.getParentKey();
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
        TableCellStyle style = null;
        if (query.isSaved()) {
            style = QueryTableCellRenderer.getCellStyle(table, query, issueTable, p, isSelected, row);
        } else {
            style = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, p, isSelected, row);
        }
        return style;
    }

    private String getSubtaskKeys(NbJiraIssue issue) {
        List<String> keys = issue.getSubtaskKeys();
        StringBuilder keysBuffer = new StringBuilder();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
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
        return new MessageFormat(NbBundle.getMessage(JiraQueryCellRenderer.class, key));
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

    private MultiLabelPanel getMultiLabelPanel() {
        if(multiLabelPanel == null) {
            multiLabelPanel = new MultiLabelPanel();
        }
        multiLabelPanel.removeAll();
        return multiLabelPanel;
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

    private void addParentAction(int row, int column, JLabel label, final NbJiraIssue issue) {
        issueTable.addCellAction(row, column, label.getBounds(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = issue.getParentID();
                NbJiraIssue parent = (NbJiraIssue) issue.getRepository().getIssueCache().getIssue(id);
                if(parent != null) {
                    parent.open();
                } else {
                    Issue.open(issue.getRepository(), id); // XXX show a wrong message in progress bar! opening ID instead of opening KEY
                }
            }
        });
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

    private class RowAdjuster implements ActionListener {
        private final Map<Integer, Integer> rowSizes = new HashMap<Integer, Integer>();
        private Timer timer = new Timer(5000, this);
        private JTable table;
        private boolean reset = false;
        private int defaultHeight = -1;
        void adjustRowSize(JPanel panel, JTable table, int row) {
            if(this.table == null) this.table = table;
            int rowHeight = table.getRowHeight(row);
            if(defaultHeight == -1) defaultHeight = rowHeight;
            int h = (int) panel.getPreferredSize().getHeight() + table.getRowMargin();
            if (rowHeight != h) {
                if(!reset) {
                    table.setRowHeight(h);
                    reset = true;
                }
                synchronized(rowSizes) {
                    rowSizes.put(row, h);
                }
                if(timer.isRunning()) {
                    timer.restart();
                } else {
                    timer.start();
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows;
            int[] heights;
            Integer row;
            synchronized(rowSizes) {
                rows = new int[rowSizes.size()];
                heights = new int[rowSizes.size()];
                Iterator<Integer> it = rowSizes.keySet().iterator();
                int idx = 0;
                while(it.hasNext()) {
                    row = it.next();
                    rows[idx] = row;
                    heights[idx] = rowSizes.get(row);
                    idx++;
                    it.remove();
                }
            }
            reset = false;
            table.setRowHeight(defaultHeight);
            for (int i = 0; i < heights.length; i++) {
                table.setRowHeight(rows[i], heights[i]);
            }
            timer.stop();
        }
    }

}
