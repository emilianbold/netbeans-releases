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


package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;
import org.netbeans.swing.tabcontrol.plaf.GenericGlowingChiclet;

/**
 *
 * @author  mkleint
 */
public class AquaSlidingButtonUI extends SlidingButtonUI {
    
    private static AquaSlidingButtonUI AQUA_INSTANCE = null;
    
    static Color[] rollover = new Color[]{
        new Color(222, 222, 227), new Color(220, 238, 255), new Color(190, 247, 255),
        new Color(205, 205, 205)};
    
    
    /** Creates a new instance of AquaSlidingButtonUI */
    private AquaSlidingButtonUI() {
    }
    
    
    /** Aqua ui for sliding buttons.  This class is public so it can be
     * instantiated by UIManager, but is of no interest as API. */
    public static ComponentUI createUI(JComponent c) {
        if (AQUA_INSTANCE == null) {
            AQUA_INSTANCE = new AquaSlidingButtonUI();
        }
        return AQUA_INSTANCE;
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        c.setFont (UIManager.getFont("Tree.font"));
    }


    protected void installBorder(AbstractButton b) {
        b.setBorder(BorderFactory.createEmptyBorder(5,2,2,2));    
    }
    
   private ChicletWrapper chic = new ChicletWrapper();
   protected void paintBackground(Graphics2D graph, AbstractButton b) {
        chic.setNotch(false, false);

        chic.setState(((SlidingButton) b).isBlinkState() ? GenericGlowingChiclet.STATE_ATTENTION : 0);
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth() - 2, b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw(graph);
        chic.setAllowVertical(false);
    }    
    
    protected void paintButtonPressed(Graphics graph, AbstractButton b) {
        chic.setNotch(false, false);
        int state = 0;
        state |= b.getModel().isSelected() ? GenericGlowingChiclet.STATE_SELECTED : 0;
        state |= b.getModel().isPressed() ? GenericGlowingChiclet.STATE_PRESSED : 0;
        state |= b.getModel().isRollover() ? GenericGlowingChiclet.STATE_ACTIVE : 0;
        state |= ((SlidingButton) b).isBlinkState() ? GenericGlowingChiclet.STATE_ATTENTION : 0;
        chic.setState(state);
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth() - 2, b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw((Graphics2D)graph);
        chic.setAllowVertical(false);
    }
    
   // ********************************
    //          Layout Methods
    // ********************************
    public Dimension getPreferredSize(JComponent c) {
	Dimension d = super.getPreferredSize(c);

        AbstractButton b = (AbstractButton)c;
        d.width += 9;
	d.height += 7; 
	return d;
    } 

    
}
