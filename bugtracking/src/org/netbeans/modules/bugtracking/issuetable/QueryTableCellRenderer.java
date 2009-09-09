
package org.netbeans.modules.bugtracking.issuetable;

import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
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
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QueryTableCellRenderer extends DefaultTableCellRenderer {

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

        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        renderer.setIcon(null);
        if(!query.isSaved()) {
            TableCellStyle style = getDefaultCellStyle(table, isSelected, row);
            setRowColors(style, renderer);
            return renderer;
        }
        
        TableCellStyle style = null;
        if(value instanceof IssueNode.SeenProperty) {
            IssueNode.SeenProperty ps = (IssueNode.SeenProperty) value;
            renderer.setIcon(!ps.getValue() ? seenValueIcon : null);
            renderer.setText("");                                               // NOI18N
        } 

        if(value instanceof IssueNode.IssueProperty) {
            style = getCellStyle(table, query, (IssueProperty)value, isSelected, row);
        }

        if(renderer instanceof JComponent && style != null) {
            JComponent l = (JComponent) renderer;
            l.putClientProperty("format", style.format);                        // NOI18N
            ((JComponent) renderer).setToolTipText(style.tooltip);
            setRowColors(style, l);
        }
        return renderer;
    }

    @Override
    protected void paintComponent(Graphics g) {        
        fitText(this);
        super.paintComponent(g);
    }

    public static void fitText(JLabel label) {
        MessageFormat format = (MessageFormat) label.getClientProperty("format");     // NOI18N
        String s = computeFitText(label);
        if(format != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html>");                                                // NOI18N
            format.format(new Object[] {s}, sb, null);
            sb.append("</html>");                                               // NOI18N
            s = sb.toString();
        }
        label.setText(s);
    }

    private static String computeFitText(JLabel label) {
        String text = label.getText();
        if (text == null || text.length() <= VISIBLE_START_CHARS + 3) return text;
        
        Icon icon = label.getIcon();
        int iconWidth = icon != null ? icon.getIconWidth() : 0;
        
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int width = label.getSize().width - iconWidth;

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

    public static class TableCellStyle {
        private MessageFormat format;
        private Color background;
        private Color foreground;
        private String tooltip;

        public TableCellStyle(MessageFormat format, Color background, Color foreground, String tooltip) {
            this.background = background;
            this.foreground = foreground;
            this.tooltip = tooltip;
            this.format = format;
        }
        public Color getBackground() {
            return background;
        }
        public Color getForeground() {
            return foreground;
        }
        public MessageFormat getFormat() {
            return format;
        }
        public String getTooltip() {
            return tooltip;
        }
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append("background=");
            sb.append(background);
            sb.append(", foreground=");
            sb.append(foreground);
            sb.append(", format=");
            sb.append(format != null ? format.toPattern() : null);
            sb.append(", tooltip=");
            sb.append(tooltip);
            sb.append("]");
            return sb.toString();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TableCellStyle other = (TableCellStyle) obj;
            if (this.format != other.format && (this.format == null || !this.format.equals(other.format))) {
                return false;
            }
            if (this.background != other.background && (this.background == null || !this.background.equals(other.background))) {
                return false;
            }
            if (this.foreground != other.foreground && (this.foreground == null || !this.foreground.equals(other.foreground))) {
                return false;
            }
            if ((this.tooltip == null) ? (other.tooltip != null) : !this.tooltip.equals(other.tooltip)) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

    }

    public static TableCellStyle getCellStyle(JTable table, Query query, IssueProperty p, boolean isSelected, int row) {
        TableCellStyle style = getDefaultCellStyle(table, isSelected, row);
        try {
            // set text format and background depending on selection and issue status
            Issue issue = p.getIssue();
            if(!query.contains(issue)) {
                // archived issues
                style.format     = isSelected ? style.format           : issueObsoleteFormat;
                style.background = isSelected ? obsoleteHighlightColor : style.background;
            } else {
                int status = query.getIssueStatus(issue);
                if(!IssueCacheUtils.wasSeen(issue)) {
                    switch(status) {
                        case IssueCache.ISSUE_STATUS_NEW :
                            style.format     = isSelected ? style.format      : issueNewFormat;
                            style.background = isSelected ? newHighlightColor : style.background;
                            break;
                        case IssueCache.ISSUE_STATUS_MODIFIED :
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

    public static TableCellStyle getDefaultCellStyle(JTable table, boolean isSelected, int row) {
        // set default values
        return new TableCellStyle(
            null,                                                                       // format
            isSelected ? table.getSelectionBackground() : getUnselectedBackground(row), // background
            isSelected ? Color.WHITE : table.getForeground(),                           // foreground
            null                                                                        // tooltip
        );
    }

    private static Color getUnselectedBackground(int row) {
        return row % 2 != 0 ? unevenLineColor : Color.WHITE;
    }

    public static void setRowColors(TableCellStyle style, JComponent l) {
        if(style == null) {
            assert false;
            return; // prefer to do nothing instead of breaking the rendering with an NPE
        }
        if (style.background != null) {
            l.setBackground(style.background);
        }
        if (style.foreground != null) {
            l.setForeground(style.foreground);
        }
    }
}


