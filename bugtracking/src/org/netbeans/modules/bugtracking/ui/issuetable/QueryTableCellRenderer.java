
package org.netbeans.modules.bugtracking.ui.issuetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.MessageFormat;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.Query;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
class QueryTableCellRenderer extends DefaultTableCellRenderer {

    private Query query;

    private static final int VISIBLE_START_CHARS = 0;
    private static Icon seenValueIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/seen-value.png")); // NOI18N

    private static final MessageFormat issueNewFormat       = getFormat("issueNewFormat");      // NOI18N
    private static final MessageFormat issueObsoleteFormat  = getFormat("issueObsoleteFormat"); // NOI18N
    private static final MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat"); // NOI18N

    private static final Color unevenLineColor              = new Color(0xf3f6fd);
    private static final Color newHighlightColor            = new Color(0x00b400);
    private static final Color modifiedHighlightColor       = new Color(0x0000ff);
    private static final Color obsoleteHighlightColor       = new Color(0x999999);

    public QueryTableCellRenderer(Query query) {
        this.query = query;
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(IssueTable.class, key);
        return new MessageFormat(format);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if(!query.isSaved()) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        
        IssueStyle style = null;
        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(value instanceof IssueNode.SeenProperty) {
            IssueNode.SeenProperty ps = (IssueNode.SeenProperty) value;
            renderer.setIcon(!ps.getValue() ? seenValueIcon : null);
            renderer.setText("");                                               // NOI18N
        } else {
            renderer.setIcon(null);
        }

        if(value instanceof IssueNode.IssueProperty) {
            style = getIssueStyle(table, (IssueProperty)value, isSelected, row);
        }

        if(renderer instanceof JComponent && style != null) {
            JComponent l = (JComponent) renderer;
            l.putClientProperty("format", style.format);                        // NOI18N
            ((JComponent) renderer).setToolTipText(style.tooltip);
            if(style.background != null) {
                l.setBackground(style.background);
            }
            if(style.foreground != null) {
                l.setForeground(style.foreground);
            }
        }
        return renderer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        MessageFormat format = (MessageFormat) getClientProperty("format");     // NOI18N
        String s = computeFitText(this, getText());
        if(format != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html>");                                                // NOI18N
            format.format(new Object[] {s}, sb, null);
            sb.append("</html>");                                               // NOI18N
            s = sb.toString();
        }
        setText(s);
        super.paintComponent(g);
    }

    private String computeFitText(JComponent comp, String text) {
        if (text == null || text.length() <= VISIBLE_START_CHARS + 3) return text;

        FontMetrics fm = comp.getFontMetrics(getFont());
        int width = comp.getSize().width;

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

    private static class IssueStyle {
        MessageFormat format;
        Color background;
        Color foreground;
        String tooltip;
    }

    private IssueStyle getIssueStyle(JTable table, IssueProperty p, boolean isSelected, int row) {
        IssueStyle style = new IssueStyle();
        Issue issue = p.getIssue();
        try {
            // set default values
            style.format     = null;
            style.foreground = isSelected ? Color.WHITE : table.getForeground();
            style.background = isSelected ? null        : getUnselectedBackground(row);

            // set text format and background depending on selection and issue status
            if(!query.contains(issue)) {
                // archived issues
                style.format     = isSelected ? style.format           : issueObsoleteFormat;
                style.background = isSelected ? obsoleteHighlightColor : style.background;
            } else {
                int status = query.getIssueStatus(issue);
                if(!issue.wasSeen()) {
                    switch(status) {
                        case Issue.ISSUE_STATUS_NEW :
                            style.format     = isSelected ? style.format      : issueNewFormat;
                            style.background = isSelected ? newHighlightColor : style.background;
                            break;
                        case Issue.ISSUE_STATUS_MODIFIED :
                            style.format     = isSelected ? style.format           : issueModifiedFormat;
                            style.background = isSelected ? modifiedHighlightColor : style.background;
                            break;
                    }
                }
            }
            Object o = p.getValue();
            if(o instanceof String) {
                style.tooltip = (String) o;
            }
        } catch (Exception ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
        }
        return style;
    }

    private Color getUnselectedBackground(int row) {
        return row % 2 != 0 ? unevenLineColor : Color.WHITE;
    }

}


