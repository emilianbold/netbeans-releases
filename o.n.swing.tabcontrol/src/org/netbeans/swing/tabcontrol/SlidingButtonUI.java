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

package org.netbeans.swing.tabcontrol;

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.text.View;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;

/** Button UI for SlidingButton component that can paint rotated text.
 * To change appearance, provide a subclass of this class or subclass of ComponentUI
 * and register it via UIDefaults in standard Swing way. 
 * Typically the methods of interest when
 * subclassing are <code>paintBackground</code> and <code>paintIconAndText</code>.
 * <p>
 * Instances of this class should be stateless, taking all data from model,
 * such that a single instance can manage any number of buttons.
 *
 * @see SlidingButton
 *
 * @author  Tim Boudreau, Dafe Simonek
 */
public class SlidingButtonUI extends BasicToggleButtonUI {

    //TODO - just a temporary solution to have some default impl..
    private static final BasicToggleButtonUI INSTANCE = new SlidingButtonUI();
    
    /** Private, no need for outer classes to instantiate */
    protected SlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }  
    
    protected String getPropertyPrefix() {
        //TODO -define own prefix?
        return "ToggleButton.";
    }

//    /** Overridden to not install keyboard actions (the buttons aren't focusable
//     * anyway) and not invoke the overhead of BasicHTML */
//    public void installUI(JComponent c) {
//        super.
//        installDefaults((AbstractButton) c);
//        installListeners((AbstractButton) c);
//        installBorder((AbstractButton) c);
//    }
//    
//    /** Install a border on the button */
//    protected void installBorder (AbstractButton b) {
//        // XXX
////        b.setBorder (BorderFactory.createEtchedBorder());
//    }
//    
//    /** Overridden to not uninstall keyboard actions (the buttons aren't focusable
//     * anyway) and not invoke the overhead of BasicHTML */
//    public void uninstallUI(JComponent c) {
//        uninstallListeners((AbstractButton) c);
//        uninstallDefaults((AbstractButton) c);
//    }
//    
//    /** Overridden to not call super.installDefaults() and only set the button
//     * to be non-focusable */
//    public void installDefaults (AbstractButton b) {
////        b.setFocusable (false);
//        super.installDefaults(b);
//        
//    }
    
    public void paint(Graphics g, JComponent c) {
       AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Dimension size = b.getSize();
        FontMetrics fm = g.getFontMetrics();
        Insets i = c.getInsets();
        Rectangle viewRect = new Rectangle(size);
//        viewRect.x += i.left;
//        viewRect.y += i.top;
//        viewRect.width -= (i.right + viewRect.x);
//        viewRect.height -= (i.bottom + viewRect.y);
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Font f = c.getFont();
        g.setFont(f);
        
        Rectangle rotatedViewRect;
        Rectangle rotatedIconRect = new Rectangle();
        Rectangle rotatedTextRect = new Rectangle();
        SlidingButton slide = (SlidingButton)b;
        Graphics2D g2d = (Graphics2D)g;
        int orientation = slide.getOrientation();
        if (orientation == SlideBarDataModel.SOUTH) {
            rotatedViewRect = new Rectangle(0, 0, viewRect.width, viewRect.height);
        } else {
            rotatedViewRect = new Rectangle(0, 0, viewRect.height, viewRect.width);
        }
        
        // layout the text and icon
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            rotatedViewRect, rotatedIconRect, rotatedTextRect,
	    b.getText() == null ? 0 : b.getIconTextGap());
        
        if (orientation == SlideBarDataModel.SOUTH) {
            iconRect = new Rectangle(viewRect.x + rotatedIconRect.x, viewRect.y + rotatedIconRect.y,
                                     rotatedIconRect.width, rotatedIconRect.height);
            textRect = new Rectangle(viewRect.x + rotatedTextRect.x, viewRect.y + rotatedTextRect.y,    
                                     rotatedTextRect.width, rotatedTextRect.height);
        }
        if (orientation == SlideBarDataModel.WEST) {
            iconRect = new Rectangle(viewRect.x + rotatedIconRect.y,
                                     viewRect.y + viewRect.height - rotatedIconRect.x - rotatedIconRect.width,
                                     rotatedIconRect.height, 
                                     rotatedIconRect.width);
            textRect = new Rectangle(viewRect.x + rotatedTextRect.y,
                                     viewRect.y + viewRect.height - rotatedTextRect.y - rotatedTextRect.width,
                                     rotatedTextRect.height, 
                                     rotatedTextRect.width);
        }
        if (orientation == SlideBarDataModel.EAST) {
            iconRect = new Rectangle(viewRect.x + viewRect.width - rotatedIconRect.y - rotatedIconRect.height,
                                     viewRect.y + rotatedIconRect.x,
                                     rotatedIconRect.height, 
                                     rotatedIconRect.width);
            textRect = new Rectangle(viewRect.x + viewRect.width - rotatedTextRect.y - rotatedTextRect.height,
                                     viewRect.y + rotatedTextRect.x,
                                     rotatedTextRect.height, 
                                     rotatedTextRect.width);
        }

        g.setColor(b.getBackground());

        if (model.isArmed() && model.isPressed() || model.isSelected() || model.isRollover()) {
            paintButtonPressed(g,b);
	} else if (b.isOpaque()) {
            paintBackground(g2d, b);
	}
	
        
        
        // Paint the Icon
        if(b.getIcon() != null) { 
            paintIcon(g, b, iconRect);
        }
	
        // Draw the Text
        if(text != null && !text.equals("")) {
            
            
            AffineTransform saveTr = g2d.getTransform();
            if (orientation != SlideBarDataModel.SOUTH) {
                if (orientation == SlideBarDataModel.WEST) {
                    // rotate 90 degrees counterclockwise for WEST orientation
                    g2d.rotate( -Math.PI / 2 );
                    g2d.translate(-c.getHeight(), 0 );
                } else {
                    // rotate 90 degrees clockwise for EAST orientation
                    g2d.rotate( Math.PI / 2 );
                    g2d.translate( 0, - c.getWidth() );
                }
            }
            
            
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, rotatedTextRect);
            } else {
                paintText(g, b, rotatedTextRect, text);
            }
            
            
            // restore transformation
            g2d.setTransform(saveTr);
        }
        
        // draw the dashed focus line.
        if (b.isFocusPainted() && b.hasFocus()) {
	    paintFocus(g, b, viewRect, textRect, iconRect);
        }        
 
    }  
    
    protected void paintBackground(Graphics2D g, AbstractButton b) {
        Dimension size = b.getSize();
        
        Insets insets = b.getInsets();
        Insets margin = b.getMargin();
        
        g.fillRect(insets.left - margin.left,
            insets.top - margin.top,
        size.width - (insets.left-margin.left) - (insets.right - margin.right),
        size.height - (insets.top-margin.top) - (insets.bottom - margin.bottom));
    }

    
    
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    public Dimension getPreferredSize(JComponent c) {
        Dimension prefSize = super.getPreferredSize(c);
        SlidingButton button = (SlidingButton) c;
        int orientation = button.getOrientation();
        
        if (orientation != SlideBarDataModel.SOUTH) {
            // flip dimensions
            int helper = prefSize.width;
            prefSize.width = prefSize.height;
            prefSize.height = helper;
        }
        
        return prefSize;
    }    

    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
}
