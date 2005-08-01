/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;

/**
 * Button UI that can paint rotated text, which is used by {@link BasicSlidingTabDisplayerUI}.
 * Uses the lightweight HTML renderer for good performance when rendering HTML strings.  It is
 * intended as a UI for {@link BasicSlidingTabDisplayerUI.IndexButton}.  Provide a subclass
 * of this class via UIDefaults to change the painting behavior or appearance of the tab buttons in
 * &quot;sliding&quot; style tabbed controls.  Typically the only method of interest when
 * subclassing is {@link #paintBackground}.
 * <p>
 * As with its superclass {@link BasicToggleButtonUI}, instances of this class should be stateless,
 * such that a single instance can manage any number of buttons.
 *
 * @see BasicSlidingTabDisplayerUI
 * @see org.netbeans.swing.tabcontrol.TabbedContainer#TYPE_SLIDING
 * @author Tim Boudreau
 */
public class SlidingTabDisplayerButtonUI extends BasicToggleButtonUI {
    private static final SlidingTabDisplayerButtonUI INSTANCE = new SlidingTabDisplayerButtonUI();
    /** Creates a new instance of SlidingTabDisplayerButtonUI */
    private SlidingTabDisplayerButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }    

    /** Overridden to not install keyboard actions (the buttons aren't focusable
     * anyway) and not invoke the overhead of BasicHTML */
    public void installUI(JComponent c) {
        installDefaults((AbstractButton) c);
        installListeners((AbstractButton) c);
        installBorder((AbstractButton) c);
    }
    
    /** Install a border on the button */
    protected void installBorder (AbstractButton b) {
        b.setBorder (BorderFactory.createEtchedBorder());
    }
    
    /** Overridden to not uninstall keyboard actions (the buttons aren't focusable
     * anyway) and not invoke the overhead of BasicHTML */
    public void uninstallUI(JComponent c) {
        uninstallListeners((AbstractButton) c);
        uninstallDefaults((AbstractButton) c);
    }
    
    /** Overridden to not call super.installDefaults() and only set the button
     * to be non-focusable */
    public void installDefaults (AbstractButton b) {
        b.setFocusable (false);
    }
    
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    public Dimension getPreferredSize(JComponent c) {
        return null;  //Layout manager handles this anyway, nobody should ask for it
    }    

    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
    
    /** Provides the painting logic.  Note that this does not call any of the
     * painting methods of BasicToggleButtonUI */
    public final void paint(Graphics g, JComponent c) {
        
        BasicSlidingTabDisplayerUI.IndexButton b = 
            (BasicSlidingTabDisplayerUI.IndexButton) c;
        
        Graphics2D g2d = (Graphics2D) g;
        
        paintBackground (g2d, b);
        
        Object orientation = b.getOrientation();
        
        AffineTransform tr = g2d.getTransform();
        if (orientation == TabDisplayer.ORIENTATION_EAST) {
             g2d.rotate( Math.PI / 2 ); 
             g2d.translate( 0, - c.getWidth() );
        } else if (orientation == TabDisplayer.ORIENTATION_WEST) {
             g2d.rotate(-Math.PI / 2); 
             g2d.translate(-c.getHeight(), 0);
        }

        paintIconAndText (g2d, b, orientation);
        g2d.setTransform (tr);
    }
    
    /** Paints the tab background */
    protected void paintBackground (Graphics2D g, BasicSlidingTabDisplayerUI.IndexButton b) {
        Color c = b.isSelected() ? Color.ORANGE : b.getBackground();
        g.setColor (c);
        g.fillRect (0, 0, b.getWidth(), b.getHeight());
    }
    
    /** Paints the icon and text using the HTML mini renderer */
    protected final void paintIconAndText (Graphics2D g, BasicSlidingTabDisplayerUI.IndexButton b, Object orientation) {
        FontMetrics fm = g.getFontMetrics(b.getFont());
        Insets ins = b.getInsets();
        
        boolean flip = orientation == TabDisplayer.ORIENTATION_EAST || 
            orientation == TabDisplayer.ORIENTATION_WEST;
        
        int txtX = flip ? ins.top : ins.left;
        
        int txtY = orientation == TabDisplayer.ORIENTATION_EAST ? ins.right :
            orientation == TabDisplayer.ORIENTATION_WEST ? ins.left : ins.top;
            
        int txtW = flip ? b.getHeight() - (ins.top + ins.bottom): 
            b.getWidth() - (ins.left + ins.right);
        
        int iconX = txtX;
        int iconY = txtY;
        
        int txtH = fm.getHeight();
        txtY += fm.getMaxAscent();
        
        Icon icon = b.getIcon();
        
        int iconH = icon.getIconHeight();
        int iconW = icon.getIconWidth();

        int workingHeight;
        if (flip) {
            workingHeight = b.getWidth() - (ins.left + ins.right);
        } else {
            workingHeight = b.getHeight() - (ins.top + ins.bottom);
        }
        txtY += (workingHeight / 2) - (txtH / 2);
        iconY += (workingHeight / 2) - (iconH / 2);
        
        if (icon != null && iconW > 0 && iconH > 0) {
            txtX += iconW + b.getIconTextGap();
            icon.paintIcon (b, g, iconX, iconY);
            txtW -= iconH + b.getIconTextGap();
        }
        
        HtmlRenderer.renderString(b.getText(), g, txtX, txtY, txtW, txtH, b.getFont(),
              b.getForeground(), HtmlRenderer.STYLE_TRUNCATE, true);
    }

    private static SlidingTabDisplayerButtonUI AQUA_INSTANCE = null;
    
    /** Aqua ui for sliding buttons.  This class is public so it can be 
     * instantiated by UIManager, but is of no interest as API. */
    public static final class Aqua extends SlidingTabDisplayerButtonUI {
        public static ComponentUI createUI(JComponent c) {
            if (AQUA_INSTANCE == null) {
                AQUA_INSTANCE = new Aqua();
            }
            return AQUA_INSTANCE;
        }

        protected void installBorder (AbstractButton b) {
            b.setBorder (BorderFactory.createEmptyBorder (5,2,2,2));
        }

        protected void paintBackground (Graphics2D g, BasicSlidingTabDisplayerUI.IndexButton b) {
            GenericGlowingChiclet chic = GenericGlowingChiclet.INSTANCE;
            int state = 0;
            state |= b.isSelected() ? chic.STATE_SELECTED : 0;
            state |= b.getModel().isPressed() ? chic.STATE_PRESSED : 0;
            state |= b.isActive() ? chic.STATE_ACTIVE : 0;

            chic.setState(state);
            chic.setArcs(0.2f, 0.2f, 0.2f, 0.2f);
            chic.setBounds (0, 1, b.getWidth(), b.getHeight());
            chic.setAllowVertical(true);
            chic.draw(g);
            chic.setAllowVertical(false);
        }
    }
}
