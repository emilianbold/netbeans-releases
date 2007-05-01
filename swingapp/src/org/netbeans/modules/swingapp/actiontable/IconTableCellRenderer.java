package org.netbeans.modules.swingapp.actiontable;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


public class IconTableCellRenderer extends DefaultTableCellRenderer {
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) comp;
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (value instanceof ImageIcon) {
            ImageIcon ii = (ImageIcon) value;
            if (ii.getIconHeight() > 16) {
                int scaledW = ii.getIconWidth() * 16 / ii.getIconHeight();
                ii = new ImageIcon(ii.getImage().getScaledInstance(scaledW, 16, 0));
            }
            label.setIcon(ii);
        }  else {
            label.setIcon(null);
        }
        label.setText(null);
        return comp;
    }
}
