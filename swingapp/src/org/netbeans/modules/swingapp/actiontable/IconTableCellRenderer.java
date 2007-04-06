package org.netbeans.modules.swingapp.actiontable;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class IconTableCellRenderer extends DefaultTableCellRenderer {
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) comp;
        if (value instanceof Icon) {
            label.setIcon((Icon) value);
        }  else {
            label.setIcon(null);
        }
        return comp;
    }
    
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());
        if (getIcon() != null) {
            int w = getIcon().getIconWidth();
            int h = getIcon().getIconWidth();
            Graphics2D gfx = (Graphics2D) g.create();
            if (h > getHeight()) {
                float scale = (float)getHeight()/(float)h;
                gfx.scale(scale,scale);
            }
            getIcon().paintIcon(this, gfx, 0, 0);
            gfx.dispose();
        }  else {
            super.paintComponent(g);
        }
    }
}