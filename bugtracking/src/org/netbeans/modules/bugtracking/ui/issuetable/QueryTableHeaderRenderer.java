package org.netbeans.modules.bugtracking.ui.issuetable;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.bugtracking.spi.Query;
import org.openide.util.ImageUtilities;

class QueryTableHeaderRenderer extends DefaultTableCellRenderer {

    private JLabel seenCell = new JLabel();

    private static Icon seenHeaderIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/seen-header.png")); // NOI18N
    private final Query query;
    private TableCellRenderer delegate;
    private final IssueTable issueTable;

     public QueryTableHeaderRenderer(TableCellRenderer delegate, IssueTable issueTable, Query query) {
        super();
        this.query = query;
        this.issueTable = issueTable;
        this.delegate = delegate;
        seenCell.setIcon(seenHeaderIcon);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (query.isSaved() && column == issueTable.getSeenColumnIdx()) {
            Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            seenCell.setFont(c.getFont());
            seenCell.setForeground(c.getForeground());
            seenCell.setBorder(((JComponent) c).getBorder());
            return seenCell;
        } else {
            return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

}
