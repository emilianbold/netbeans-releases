
package org.netbeans.modules.jira.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
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
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.issue.JiraIssueNode;
import org.netbeans.modules.jira.issue.JiraIssueNode.PriorityProperty;
import org.netbeans.modules.jira.issue.JiraIssueNode.SummaryProperty;
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
    private static Icon subtaskIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/jira/resources/subtask.png"));    // NOI18N

    private final IssueTable issueTable;
    private boolean resetRowHeight;

    public JiraQueryCellRenderer(JiraQuery query, IssueTable issueTable, QueryTableCellRenderer defaultIssueRenderer) {
        this.query = query;
        this.defaultIssueRenderer = defaultIssueRenderer;
        this.issueTable = issueTable;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        fixRowHeightIfNeeded(table, row);

        if(!(value instanceof JiraIssueNode.SummaryProperty)) {
            JLabel renderer = (JLabel) defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(value instanceof PriorityProperty) {
                PriorityProperty property = (PriorityProperty) value;
                Priority priority = property.getPriority();
                Icon icon = JiraConfig.getInstance().getPriorityIcon(priority.getId());
                renderer.setIcon(icon);
                return renderer;
            }
//            else {
//                renderer.setIcon(null);
//            }
            return renderer;
        }

        SummaryProperty summaryProperty = (SummaryProperty) value;
        NbJiraIssue issue = (NbJiraIssue) summaryProperty.getIssue();
        String summary = summaryProperty.getValue();

        TableCellStyle style = null;
        if(issue.hasSubtasks()) {
            TwoLabelPanel panel = getTwoLabelPanel(table.getFont());
            panel.setFontSize(table.getFont(), panel.north);
            if(query.isSaved()) {
                style = QueryTableCellRenderer.getCellStyle(table, query, summaryProperty, isSelected, row);
            } else {
                style = QueryTableCellRenderer.getDefaultCellStyle(table, isSelected, row);
            }

            panel.north.setText(summary);
            panel.north.putClientProperty("format", style != null ? style.getFormat() : null);// NOI18N

            List<String> keys = issue.getSubtaskKeys();
            StringBuffer keysBuffer = new StringBuffer();
            Iterator<String> it = keys.iterator();
            while(it.hasNext()) {
                keysBuffer.append(it.next());
                if(it.hasNext()) keysBuffer.append(", "); // NOI18N
            }

            panel.south.setText(keysBuffer.toString()); 
            panel.south.putClientProperty("format", isSelected ? null : subtasksFormat); // NOI18N

            panel.setToolTipText(summary);
            setRowColors(style, panel);
            adjustRowHeightIfNeeded(panel, table, row);
            
            return panel;
        } else if(issue.isSubtask() ) {
            TwoLabelPanel panel = getTwoLabelPanel(table.getFont());
            panel.setFontSize(table.getFont(), panel.south);

            if(query.isSaved()) {
                style = QueryTableCellRenderer.getCellStyle(table, query, summaryProperty, isSelected, row);
            } else {
                style = QueryTableCellRenderer.getDefaultCellStyle(table, isSelected, row);
            }
            
            panel.north.setText(issue.getParentKey());
            panel.north.putClientProperty("format", isSelected ? null : parentFormat); // NOI18N

            panel.south.setText(summary); 
            panel.south.putClientProperty("format", style != null ? style.getFormat() : null); // NOI18N

            panel.setToolTipText(summary);
            setRowColors(style, panel);
            adjustRowHeightIfNeeded(panel, table, row);

            return panel;
        }

        return defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    private int defaultRowHeight = -1;
    public void resetDefaultRowHeight() {
        resetRowHeight = true;
    }
    
    private void adjustRowHeightIfNeeded(JPanel panel, JTable table, int row) {

        if(defaultRowHeight == -1) {
            defaultRowHeight = table.getRowHeight();
        }

        int h = (int) panel.getPreferredSize().getHeight();
        h = h + table.getRowMargin();
        if (table.getRowHeight(row) != h) {
            table.setRowHeight(h);
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

    private TwoLabelPanel getTwoLabelPanel(Font font) {
        if(twoLabelPanel == null) {
            twoLabelPanel = new TwoLabelPanel(font);
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
        public TwoLabelPanel(Font font) {
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

    private class RendererLabel extends JLabel {
        @Override
        public void paint(Graphics g) {
            QueryTableCellRenderer.fitText(this);
            super.paint(g);
        }
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        public void invalidate() {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        public void validate() {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        public void revalidate() {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        public void repaint(long tm, int x, int y, int width, int height) {}
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
        public void repaint(Rectangle r) { }
        /** overriden to no-op. {@see javax.swing.table.DefaultTableCellRenderer} for more information.*/
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
