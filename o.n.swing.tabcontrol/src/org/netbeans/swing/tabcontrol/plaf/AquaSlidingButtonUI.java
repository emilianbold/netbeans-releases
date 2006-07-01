/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
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
    
   private ChicletWrapper chic = new ChicletWrapper();
   protected void paintBackground(Graphics2D graph, AbstractButton b) {
        chic.setNotch(false, false);

        chic.setState(((SlidingButton) b).isBlinkState() ? GenericGlowingChiclet.STATE_ATTENTION : 0);
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth(), b.getHeight() - 2);
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
        chic.setBounds(0, 1, b.getWidth(), b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw((Graphics2D)graph);
        chic.setAllowVertical(false);
    }
    
   // ********************************
    //          Layout Methods
    // ********************************
    public Dimension getPreferredSize(JComponent c) {
        SlidingButton slide = (SlidingButton) c;
	Dimension d = new Dimension(super.getPreferredSize(c));
        Insets i = c.getInsets();
        int orientation = slide.getOrientation();
        
        if (orientation == SlideBarDataModel.SOUTH) {
            if (i.top + i.bottom < 5)
                d.height += 5;
            if (i.left + i.right < 7)
                d.width += 7;
        }
        else {
            if (i.top + i.bottom < 7)
                d.height += 7;
            if (i.left + i.right < 5)
                d.width += 5;
        }

	return d;
    }
}
