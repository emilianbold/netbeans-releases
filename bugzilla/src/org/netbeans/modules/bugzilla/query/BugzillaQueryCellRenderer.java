
package org.netbeans.modules.bugzilla.query;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.issue.BugzillaIssueNode.PriorityProperty;

/**
 *
 * @author Tomas Stupka
 *
 */
public class BugzillaQueryCellRenderer implements TableCellRenderer {

    private final QueryTableCellRenderer defaultIssueRenderer;

    public BugzillaQueryCellRenderer(QueryTableCellRenderer defaultIssueRenderer) {
        this.defaultIssueRenderer = defaultIssueRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel renderer = (JLabel) defaultIssueRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(!(value instanceof PriorityProperty)) {
            return renderer;
        }
        PriorityProperty p = (PriorityProperty) value;
        String priority = p.getValue();
        renderer.setIcon(BugzillaConfig.getInstance().getPriorityIcon(priority));
        return renderer;
    }
    
}
