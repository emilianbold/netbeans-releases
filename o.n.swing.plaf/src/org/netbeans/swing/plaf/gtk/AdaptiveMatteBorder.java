/*
 * AdaptiveMatteBorder.java
 *
 * Created on April 7, 2004, 5:54 PM
 */

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import org.netbeans.swing.plaf.util.UIUtils;

/**
 * A matte border with a few twists - can do drop shadow; also, for toolbars,
 * will check the position of the component and return taller insets for the
 * top row, so it's offset from the menu, but not present a doubled border
 * for lower rows; same thing for toolbars adjacent to each other.
 *
 * @author  Tim Boudreau
 */
public class AdaptiveMatteBorder implements Border {
    private Insets insets;
    private int shadowDepth;
    private boolean topLeftInsets;
    
    /** Creates a new instance of AdaptiveMatteBorder */
    public AdaptiveMatteBorder(boolean t, boolean l, boolean b, boolean r, int shadowDepth, boolean topLeftInsets) {
        insets = new Insets (t ? topLeftInsets ? shadowDepth + 1 : 1 : 0, l ? topLeftInsets ? shadowDepth + 1: 1 : 0, b ? 1 + shadowDepth : shadowDepth, r ? 1 + shadowDepth : shadowDepth);
        this.shadowDepth = shadowDepth;
        this.topLeftInsets = topLeftInsets;
    }
    
    public AdaptiveMatteBorder(boolean t, boolean l, boolean b, boolean r, int shadowDepth) {
        this (t, l, b, r, shadowDepth, false);
    }
    
    private Insets maybeOmitInsets (Insets ins, Component c) {
        if (shadowDepth <= 0 || !topLeftInsets) {
            return ins;
        }
        Insets result = new Insets(ins.top, ins.left, ins.right, ins.bottom);
        Point p = c.getLocation();
        if (p.x > 10) {
            result.left = 1;
        }
        if (p.y > 10) {
            result.top = 1;
        }
        return result;
    }
    
    public Insets getBorderInsets(Component c) {
        return maybeOmitInsets(insets, c);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }

    
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color color = g.getColor();
        Insets ins = getBorderInsets(c);
        Point p = c.getLocation();
        
        //This will always really come from the theme on GTK
        g.setColor (UIManager.getColor("controlShadow"));  //NOI18N
        w -= shadowDepth;
        h -= shadowDepth;
        if (topLeftInsets) {
            if (p.y <= 10) {
                y += shadowDepth;
                h -= shadowDepth;
            }
            if (p.x <= 10) {
                x += shadowDepth;
                w -= shadowDepth;
            }
        }
        if (ins.top > 0) {
            g.drawLine (x, y, x + w, y);
        }
        if (ins.left > 0) {
            g.drawLine (x, y, x, y + h);
        }
        if (ins.right > 0) {
            g.drawLine (x + w, y, x + w, y + h);
        }
        if (ins.bottom > 0) {
            g.drawLine (x, y + h, x + w, y + h);
        }
        
        if (shadowDepth > 1) {
            Color ctrl = UIManager.getColor ("control"); //NOI18N
            Color base = UIManager.getColor("controlShadow");
        /*
            ctrl = new Color (ctrl.getRed(), ctrl.getGreen(), ctrl.getBlue(), 255);
            
            xpoints[0] = x+1;
            ypoints[0] = y + h;
            
            xpoints[1] = x + 1 + shadowDepth;
            ypoints[1] = y + h + 1 + shadowDepth;
            
            xpoints[2] = x + shadowDepth + w;
            ypoints[2] = ypoints[1];
            
            xpoints[3] = x + w;
            ypoints[3] = y + h + 1;
            
            GradientPaint gp = UIUtils.getGradientPaint (xpoints[0], ypoints[0], base,
                xpoints[0], ypoints[1], ctrl, false);
            ((Graphics2D) g).setPaint (gp);
            g.fillPolygon(xpoints, ypoints, 4);
            
            xpoints[0] = x + w + 1;
            ypoints[0] = y;
            
            xpoints[1] = x + w + shadowDepth;
            ypoints[1] = y + shadowDepth;
            
            gp = UIUtils.getGradientPaint (xpoints[0], ypoints[0], base,
                xpoints[0], ypoints[1], ctrl, false);
            ((Graphics2D) g).setPaint (gp);
            g.fillPolygon(xpoints, ypoints, 4);
         */
            
            
            Color curr;
            for (int i = 1; i < shadowDepth; i++) {
                curr = colorTowards (base, ctrl, shadowDepth, i + 1);
                g.setColor (curr);
                if (ins.right > 0) {
                    g.drawLine (x + w + i, y, x + w + i, y + h + i - 1);
                }
                if (ins.bottom > 0) {
                    g.drawLine (x + i, y + h + i, x + w + i, y + h + i);
                }
            }
        }
        g.setColor (color);
    }
    
//    private static int[] xpoints = new int[4];
//    private static int[] ypoints = new int[4];
    
/*    
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color color = g.getColor();
        Insets ins = getBorderInsets(c);
        Point p = c.getLocation();
        
        //This will always really come from the theme on GTK
        g.setColor (UIManager.getColor("controlShadow"));  //NOI18N
        w -= shadowDepth;
        h -= shadowDepth;
        if (topLeftInsets) {
            if (p.y <= 10) {
                y += shadowDepth;
                h -= shadowDepth;
            }
            if (p.x <= 10) {
                x += shadowDepth;
                w -= shadowDepth;
            }
        }
        if (ins.top > 0) {
            g.drawLine (x, y, x + w, y);
        }
        if (ins.left > 0) {
            g.drawLine (x, y, x, y + h);
        }
        if (ins.right > 0) {
            g.drawLine (x + w, y, x + w, y + h);
        }
        if (ins.bottom > 0) {
            g.drawLine (x, y + h, x + w, y + h);
        }
        if (shadowDepth > 1) {
            Color ctrl = UIManager.getColor ("control"); //NOI18N
            Color base = UIManager.getColor("controlShadow");
            
            Color curr;
            for (int i = 1; i < shadowDepth; i++) {
                curr = colorTowards (base, ctrl, shadowDepth, i + 1);
                g.setColor (curr);
                if (ins.right > 0) {
                    g.drawLine (x + w + i, y, x + w + i, y + h + i - 1);
                }
                if (ins.bottom > 0) {
                    g.drawLine (x + i, y + h + i, x + w + i, y + h + i);
                }
            }
        }
        g.setColor (color);
    }
 */
    
    private static final float[] comps = new float[4];
    static final Color colorTowards (Color base, Color target, float steps, float step) {
        base.getColorComponents(comps);
        comps[3] = 1f - (step / steps);
        return new Color (comps[0], comps[1], comps[2], comps[3]);
    }
    
}
