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
 * AquaToolBarButtonUI.java
 *
 * Created on January 17, 2004, 1:54 PM
 */

package org.netbeans.swing.plaf.aqua;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.*;
import java.awt.image.BufferedImage;

/** A finder-style aqua toolbar button UI
 *
 * @author  Tim Boudreau
 */
class AquaToolBarButtonUI extends ButtonUI implements ChangeListener {
    private static BasicButtonListener listener = 
        new BasicButtonListener(null);
    
    /** Creates a new instance of AquaToolBarButtonUI */
    public AquaToolBarButtonUI() {
    }
    
    public void installUI (JComponent c) {
        AbstractButton b = (AbstractButton) c;
        b.addMouseListener (listener);
        b.addChangeListener(this);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setBorder (BorderFactory.createEmptyBorder());
   }
    
    public void uninstallUI(JComponent c) {
        c.removeMouseListener (listener);
    }
    
    public void stateChanged(ChangeEvent e) {
        ((AbstractButton) e.getSource()).repaint();
    }
    
    private final Rectangle scratch = new Rectangle();
    public void paint (Graphics g, JComponent c) {
        Rectangle r = c.getBounds(scratch);
        AbstractButton b = (AbstractButton) c;
        r.x = 0;
        r.y = 0;
        Paint temp = ((Graphics2D) g).getPaint();
        paintBackground ((Graphics2D)g, b, r);
        paintIcon (g, b, r);
        paintText (g, b, r);
        ((Graphics2D) g).setPaint(temp);
    }
    
    
    private FontMetrics fm = null; //We are not setting any custom fonts, can use one
    private void paintText (Graphics g, AbstractButton b, Rectangle r) {
        String s = b.getText();
        if (s == null || s.length() == 0) {
            return;
        }
        g.setColor (b.getForeground());
        Font f = b.getFont();
        if (b.isSelected()) {
            // don't use deriveFont() - see #49973 for details
            f = new Font(f.getName(), Font.BOLD, f.getSize());
        }
        g.setFont (f);
        FontMetrics fm = g.getFontMetrics();
        if (this.fm == null) {
            this.fm = fm;
        }        
        int x = 0;
        Icon ic = b.getIcon();
        if (ic != null) {
            x = ic.getIconWidth() + 2;
        } else {
            int w = fm.stringWidth (s);
            if (w <= r.width) {
                x = (r.width / 2) - (w / 2);
            }
        }
        int h = fm.getHeight();
        int y = fm.getMaxAscent();
        if (h <= r.height) {
            y += (r.height / 2) - (h / 2);
        }
        g.drawString (s, x, y);
    }
    
    private void paintBackground (Graphics2D g, AbstractButton b, Rectangle r) {
        if (!b.isEnabled()) {
        } else if (b.getModel().isPressed()) {
            compositeColor (g, r, Color.BLUE, 0.3f);
        } else if (b.getModel().isSelected()) {
            compositeColor (g, r, new Color (0, 120, 255), 0.2f);;
        }
    }
    
    private void compositeColor (Graphics2D g, Rectangle r, Color c, float alpha) {
        g.setColor (c);
        Composite comp = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, alpha));

        g.fillRect (r.x, r.y, r.width, r.height);
        g.setComposite(comp);
    }
    
    
    private static boolean isFirst (AbstractButton b) {
        if (b.getParent() != null && b.getParent().getComponentCount() > 1) {
            //The grip is always component 0, so see if the button
            //is component 1
            return b == b.getParent().getComponent(1);
        } else {
            return false;
        }
    }
    
    private void paintIcon (Graphics g, AbstractButton b, Rectangle r) {
        Icon ic = getIconForState (b);
        boolean noText = b.getText() == null || b.getText().length() == 0;
        if (ic != null) {
            int iconX = 0;
            int iconY = 0;
            int iconW = ic.getIconWidth();
            int iconH = ic.getIconHeight();
            
            if (iconW <= r.width && noText) {
                iconX = (r.width / 2) - (iconW / 2);
            }
            if (iconH <= r.height) {
                iconY = (r.height / 2) - (iconH / 2);
            }
            ic.paintIcon(b, g, iconX, iconY);
        }
    }
    
    private Icon getIconForState (AbstractButton b) {
        ButtonModel mdl = b.getModel();
        Icon result = null;
        if (!b.isEnabled()) {
            result = mdl.isSelected() ? b.getDisabledSelectedIcon() : b.getDisabledIcon();
            if (result == null && mdl.isSelected()) {
                result = b.getDisabledIcon();
            }
        } else {
            if (mdl.isArmed() && !mdl.isPressed()) {
                result = mdl.isSelected() ? b.getRolloverSelectedIcon() : b.getRolloverIcon();
                if (result == null & mdl.isSelected()) {
                    result = b.getRolloverIcon();
                }
            }
            if (mdl.isPressed()) {
                result = b.getPressedIcon();
            } else if (mdl.isSelected()) {
                result = b.getSelectedIcon();
            }
        }
        if (result == null) {
            result = b.getIcon();
        }
        return result;
    }
    
    private static final int minButtonSize = 32;
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton) c;
        
        boolean noText = 
            b.getText() == null || 
            b.getText().length() == 0;
            
        Icon ic = getIconForState((AbstractButton) c);
        int w = isFirst(b) ? 0 : minButtonSize;
        Dimension result = ic == null ? new Dimension (noText ? 32 : 0, minButtonSize) :
                new Dimension(Math.max(w, ic.getIconWidth()+1), 
                Math.max(minButtonSize,ic.getIconHeight() + 1));
        
        if (!noText) {
            FontMetrics fm = this.fm;
            if (fm == null && c.getGraphicsConfiguration() != null) {
                fm = c.getGraphicsConfiguration().createCompatibleImage(1,1)
                     .getGraphics().getFontMetrics(c.getFont());
            }
            if (fm == null) {
                //init
                fm = new BufferedImage(1, 1, 
                BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(c.getFont());
            }
            result.width += fm.stringWidth(b.getText());
        }
        return result;
    }    
}
