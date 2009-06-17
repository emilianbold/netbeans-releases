
package org.netbeans.modules.jira.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
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
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.bugtracking.spi.IssueNode.IssueProperty;
import org.netbeans.modules.jira.issue.JiraIssueNode;
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
    private static Icon hookIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/jira/resources/hook.png"));    // NOI18N

    private RowAdjuster rowAdjuster = new RowAdjuster();

    public JiraQueryCellRenderer(JiraQuery query, QueryTableCellRenderer defaultIssueRenderer) {
        this.query = query;
        this.defaultIssueRenderer = defaultIssueRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if(!(value instanceof JiraIssueNode.SummaryProperty)) {
            return defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        IssueProperty issueProperty = (IssueProperty) value;
        NbJiraIssue issue = (NbJiraIssue) issueProperty.getIssue();

        TableCellStyle style = null;
        if(issue.hasSubtasks()) {
            TwoLabelPanel panel = getTwoLabelPanel(table.getFont());
            
            if(query.isSaved()) {
                style = QueryTableCellRenderer.getCellStyle(table, query, issueProperty, isSelected, row);
            } else {
                style = QueryTableCellRenderer.getDefaultCellStyle(table, isSelected, row);
            }

            panel.north.setText(value.toString()); // XXX toString ???
            panel.north.putClientProperty("format", style != null ? style.getFormat() : null);                        // NOI18N

            List<String> keys = issue.getSubtaskKeys();
            StringBuffer keysBuffer = new StringBuffer();
            Iterator<String> it = keys.iterator();
            while(it.hasNext()) {
                keysBuffer.append(it.next());
                if(it.hasNext()) keysBuffer.append(", ");
            }

            panel.south.setText(keysBuffer.toString()); 
            panel.south.putClientProperty("format", isSelected ? null : parentFormat); // NOI18N

            panel.setToolTipText(value.toString());  // XXX toString ???
            setRowColors(style, panel);
            adjustRowSize(panel, table, row);
            
            return panel;
        } else if(issue.isSubtask() ) {
            TwoLabelPanel panel = getTwoLabelPanel(table.getFont());

            if(query.isSaved()) {
                style = QueryTableCellRenderer.getCellStyle(table, query, issueProperty, isSelected, row);
            } else {
                style = QueryTableCellRenderer.getDefaultCellStyle(table, isSelected, row);
            }
            
            panel.north.setText(issue.getParentKey());
            panel.north.putClientProperty("format", style != null ? style.getFormat() : null); // NOI18N

            panel.south.setText(value.toString()); // XXX toString ???
            panel.south.putClientProperty("format", isSelected ? null : subtasksFormat); // NOI18N

            panel.setToolTipText(value.toString());  // XXX toString ???
            setRowColors(style, panel);
            adjustRowSize(panel, table, row);
            
            return panel;
        }

        return defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    private void adjustRowSize(JPanel panel, JTable table, int row) {
        int h = (int) panel.getPreferredSize().getHeight();
        h = h + table.getRowMargin();
        if (table.getRowHeight(row) != h) {
            table.setRowHeight(h);
        }
    }

    private static MessageFormat getFormat(String key) {
        return new MessageFormat(NbBundle.getMessage(JiraQueryCellRenderer.class, key));
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

    private class TwoLabelPanel extends JPanel {
        AdjustableJLabel north = new AdjustableJLabel();
        AdjustableJLabel south = new AdjustableJLabel();
        public TwoLabelPanel(Font font) {
//            setOpaque(false);
            setLayout(new BorderLayout());
            add(north, BorderLayout.NORTH);
            add(south, BorderLayout.SOUTH);
            north.setFont(defaultIssueRenderer.getFont());
            south.setFont(defaultIssueRenderer.getFont());
            south.setIcon(hookIcon);
        }        
    }

    private class AdjustableJLabel extends JLabel {
        @Override
        public void paint(Graphics g) {
            QueryTableCellRenderer.fitText(this);
            super.paint(g);
        }
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
