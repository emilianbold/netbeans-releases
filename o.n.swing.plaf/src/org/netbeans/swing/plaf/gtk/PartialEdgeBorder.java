/*
 * PartialEdgeBorder.java
 *
 * Created on April 8, 2004, 7:49 PM
 */

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import org.netbeans.swing.plaf.util.UIUtils;

/**
 * Drop shadow extension for right edges of tab displayers
 * 
 * @author  Tim Boudreau
 */
public class PartialEdgeBorder implements Border {
    private Insets ins;
    /** Creates a new instance of PartialEdgeBorder */
    public PartialEdgeBorder(int width) {
        ins = new Insets (0,0,0,width);
    }
    
    public Insets getBorderInsets(Component c) {
        return ins;
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color ctrl = UIManager.getColor ("control"); //NOI18N
        Color base = UIManager.getColor("controlShadow");
        Color curr;
        GradientPaint gp = UIUtils.getGradientPaint (x + width - ins.right, y + (height / 2), ctrl, x + width - ins.right, y + height, base, false);
        ((Graphics2D) g).setPaint (gp);
        g.drawLine (x + width - ins.right, y + (height / 2), x + width - ins.right, y + height);
        for (int i=1; i < ins.right-1; i++) {
            curr = AdaptiveMatteBorder.colorTowards (base, ctrl, ins.right, i + 1);
            int xpos = x + width - ins.right + i;
            int ypos = y + (height / 3) + (i * 2);
            gp = UIUtils.getGradientPaint(xpos, ypos, ctrl, xpos, y + height, curr, false);
            ((Graphics2D) g).setPaint (gp);
            g.drawLine (xpos, ypos, xpos, y + height);
        }
    }
    
}
