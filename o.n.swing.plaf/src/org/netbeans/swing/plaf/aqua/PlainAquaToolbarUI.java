/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PlainAquaToolbarUI.java
 *
 * Created on January 17, 2004, 3:00 AM
 */

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.BoxLayout;
import javax.swing.border.Border;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

/** A ToolbarUI subclass that gets rid of all borders
 * on buttons and provides a finder-style toolbar look.
 * 
 * @author  Tim Boudreau
 */
public class PlainAquaToolbarUI extends BasicToolBarUI implements ContainerListener {
    private static final AquaTbBorder aquaborder = new AquaTbBorder();
//    private static final AquaToolbarLayout layout = new AquaToolbarLayout();
    
    private static final Color UPPER_GRADIENT_TOP = new Color(255,255,255);
    private static final Color UPPER_GRADIENT_BOTTOM = new Color (228,230,232);
    
    private static final Color LOWER_GRADIENT_TOP = new Color(228,227,215);
    private static final Color LOWER_GRADIENT_BOTTOM = new Color(249,249,249);
        
    
    /** Creates a new instance of PlainAquaToolbarUI */
    public PlainAquaToolbarUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new PlainAquaToolbarUI();
    }
    
    public void installUI( JComponent c ) {
        super.installUI(c);
        //Editor will try install a custom border - just use ours
        UIManager.put ("Nb.Editor.Toolbar.border", aquaborder);
        c.setBorder(aquaborder);
        c.setOpaque(true);
        c.addContainerListener(this);
        
        installButtonUIs (c);
    }
    
    public void uninstallUI (JComponent c) {
        super.uninstallUI (c);
        c.setBorder (null);
        c.removeContainerListener(this);
    }
    
    public void paint(Graphics g, JComponent c) {
        if (!c.isOpaque()) {
            return;
        }
        UIUtils.configureRenderingHints(g);
        Color temp = g.getColor();
        Dimension size = c.getSize();
        
        Shape s = aquaborder.getInteriorShape(size.width, size.height);
        Shape clip = g.getClip();
        if (clip != null) {
            Area a = new Area(clip);
            a.intersect(new Area(s));
            g.setClip (a);
        } else {
            g.setClip(s);
        }
        
        Graphics2D g2d = (Graphics2D) g;
        //g.setColor (Color.ORANGE);

        g2d.setPaint (aquaborder.getUpperPaint(size.width,size.height));
        g2d.fill (aquaborder.getUpperBevelShape(size.width, size.height));
        g2d.setPaint (aquaborder.getLowerPaint(size.width,size.height));
        g2d.fill (aquaborder.getLowerBevelShape(size.width, size.height));
        
        
        g.setClip (clip);
        g.setColor(temp);
    }
    
    
    protected Border createRolloverBorder() {
        return BorderFactory.createEmptyBorder(2,2,2,2);
    }
    
    protected Border createNonRolloverBorder() {
        return createRolloverBorder();
    }
    
    private Border createNonRolloverToggleBorder() {
        return createRolloverBorder();
    }
    
    protected void setBorderToRollover(Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setBorderPainted(false);
            ((AbstractButton) c).setBorder(BorderFactory.createEmptyBorder());
//            ((AbstractButton) c).setContentAreaFilled(false);
            ((AbstractButton) c).setOpaque(false);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    protected void setBorderToNormal(Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setBorderPainted(false);
//            ((AbstractButton) c).setContentAreaFilled(false);
            ((AbstractButton) c).setOpaque(false);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    public void setFloating(boolean b, Point p) {
        //nobody wants this
    }
    
    private void installButtonUI (Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setUI(buttonui);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    private void installButtonUIs (Container parent) {
        Component[] c = parent.getComponents();
        for (int i=0; i < c.length; i++) {
            installButtonUI(c[i]);
        }
    }
    
    private static final ButtonUI buttonui = new AquaToolBarButtonUI();
    public void componentAdded(ContainerEvent e) {
        installButtonUI (e.getChild());
        Container c = (Container) e.getSource();
        if ("editorToolbar".equals (c.getName())) {
            //It's an editor toolbar.  Aqua's combo box ui paints outside
            //of its literal component bounds, and doesn't honor opacity.
            //Need to ensure the toolbar is tall enough that its border is
            //not hidden.
            Dimension min = new Dimension (32, 34);
            ((JComponent)e.getContainer()).setPreferredSize(min);
        }
    }
    
    public void componentRemoved(ContainerEvent e) {
        //do nothing
    }

    private static boolean isFinderLook (Component c) {
        if (c instanceof JComponent) {
            return Boolean.TRUE.equals (((JComponent) c).getClientProperty("finderLook"));
        }
        return false;
    }
    
    
    static class AquaTbBorder implements Border {

        int arcsize = 13;
        
        public Insets getBorderInsets(Component c) {
            return new Insets (2,4,0,0);
        }
        
        public boolean isBorderOpaque() {
            return true;
        }
        
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            UIUtils.configureRenderingHints(g);
            y+=2; //leave a gap at the top
            h-=2; //leave a gap at the top
            
            boolean finderLook = isFinderLook (c);
            Color col;
            if (finderLook) {
                col = mezi(UIManager.getColor("controlShadow"), 
                    UIManager.getColor("control")); //NOI18N
            } else {
                col = UIManager.getColor("controlShadow");
            }
            
            g.setColor(col);
            
            int ytop = y;
            
            drawUpper (g, x, y, ytop, w, h);

            g.setColor (mezi (col, UIManager.getColor("control"))); //NOI18N
            if (finderLook) {
                drawUpper (g, x+1, y, ytop+1, w-2, h-1);
            }
            
            if (finderLook) {
                col = mezi(UIManager.getColor("controlShadow"), 
                    UIManager.getColor("control")); //NOI18N
            }
            if (finderLook) {
                drawLower (g, x, y, w, h, col, finderLook);
            } else {
//                drawLower (g, x, y, w, h, Color.LIGHT_GRAY, finderLook);
                drawLower (g, x, y-1, w, h, col, finderLook);
                g.setColor(new Color(200,200,200));
                g.drawLine (x+(arcsize/2)-3, y+h-1, x+w-(arcsize/2), y+h-1);
            }
        }
        
        private void drawLower (Graphics g, int x, int y, int w, int h, Color col, boolean finderLook) {

            g.setColor(col);
            g.drawLine(x, y+(arcsize/2), x, y+h-(arcsize / 2));
            g.drawLine(x+w-1, y+(arcsize/2), x+w-1, y+h-(arcsize / 2));

            if (!finderLook) {
                g.setColor(new Color(220,220,220));
                g.drawArc (x-1, y+1+h-arcsize, arcsize, arcsize, 180, 90);
                g.drawArc ((x+1)+w-(arcsize+1), y+1+h-(arcsize+1), arcsize, arcsize, 270, 90);
                g.setColor(col);
            }
            
            g.drawArc (x, y+h-arcsize, arcsize, arcsize, 180, 90);
            g.drawArc (x+w-(arcsize+1), y+h-(arcsize+1), arcsize, arcsize, 270, 90);

            if (!finderLook) {
                
                g.setColor (new Color(80,80,80));
            }
            g.drawLine (x+(arcsize/2)-3, y+h-1, x+w-(arcsize/2), y+h-1);
        }
        
        private void drawUpper (Graphics g, int x, int y, int ytop, int w, int h) {
            g.drawArc (x, ytop, arcsize, arcsize, 90, 90);

            g.drawArc (x+w-(arcsize+1), ytop, arcsize, arcsize, 90, -90);
            
            g.drawLine(x+(arcsize/2), ytop, x+w-(arcsize/2), ytop);
        }
        
        Paint getUpperPaint (Color top, Color bottom, int w, int h) {
            GradientPaint result = 
                UIUtils.getGradientPaint (0, h/4, top, 0, (h/2) + (h/4),
                bottom, false);
            return result;
        }
        
        Paint getLowerPaint (Color top, Color bottom, int w, int h) {
            GradientPaint result =
                UIUtils.getGradientPaint (0, h/2, top, 0, (h/2) + (h/4),
                bottom, false);
            
            return result;
        }
        
        Paint getUpperPaint (int w, int h) {
            return getUpperPaint (UPPER_GRADIENT_TOP, UPPER_GRADIENT_BOTTOM, w, h);
        }

        Paint getLowerPaint (int w, int h) {
            return getLowerPaint (LOWER_GRADIENT_TOP, 
                LOWER_GRADIENT_BOTTOM , w, h);
        }
        
        
        Shape getInteriorShape(int w, int h) {
            RoundRectangle2D r2d = new RoundRectangle2D.Double(0, 0, w, h, arcsize, arcsize);
            return r2d;
        }
        
        Shape getUpperBevelShape(int w, int h) {
            int[] xpoints = new int[] {
                0,
                0,
                h / 2,
                w - (h / 4),
                w,
                w,
                0
            };
            
            int[] ypoints = new int[] {
                0,
                h - (h / 4),
                h / 2,
                h / 2,
                h / 4,
                0,
                0
            };
            Polygon p = new Polygon (xpoints, ypoints, ypoints.length);
            return p;
        }
        
        Shape getLowerBevelShape(int w, int h) {
            int[] xpoints = new int[] {
                0,
                0,
                h / 4,
                w - (h / 4),
                w,
                w,
                0
            };
            
            int[] ypoints = new int[] {
                h,
                h - (h / 4),
                h / 2,
                h / 2,
                h / 4,
                h,
                h
                
            };
            Polygon p = new Polygon (xpoints, ypoints, ypoints.length);
            return p;
        }
    }
    
    private static Color mezi (Color c1, Color c2) {
        return new Color((c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2);
    }
    
    public static void main (String[] args) {
        javax.swing.JFrame jf = new javax.swing.JFrame();
        javax.swing.JToolBar jtb = new javax.swing.JToolBar();
        jtb.setUI (new PlainAquaToolbarUI());
        jf.getContentPane().setLayout (new java.awt.BorderLayout());
        jf.getContentPane().add (jtb, java.awt.BorderLayout.NORTH);
        javax.swing.JButton b = new javax.swing.JButton("Some button");
        jtb.add (b);
        javax.swing.JButton b2 = new javax.swing.JButton("Another button");
        jtb.add(b2);
        
        jf.setBounds (20,20, 400, 300);
        
        javax.swing.JTextArea foo = new javax.swing.JTextArea("Foodbar");
        jf.getContentPane().add (foo, java.awt.BorderLayout.SOUTH);
        
        jf.show();
    }
}
