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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.graph.awt;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 *
 * @author jsandusky
 */
public class InnerGlowBorderDrawer {
    
    private static final Color COLOR_TRANSPARENT = new Color(255, 255, 255, 0);
    
    
    /**
     * Paints a "glow" around the inner edges of the given rectangle.
     * The glow effect is simply a gradient transition from a partially 
     * translucent color to a fully translucent color - all around the inner
     * edges of the specified rectangle.
     *
     * @param graphics         the graphics object to paint on
     * @param bounds           the rectangle
     * @param baseGlowColor    the base color to base the glow off of (alpha channel ignored)
     * @param transitionForce  a floating point number between 0.0f and 1.0f reflecting the strength of the glow transition
     * @param glowSize         glow width, in pixels
     */
    public static void paintInnerGlowBorder(
            Graphics2D graphics, 
            Rectangle bounds, 
            Color baseGlowColor, 
            float transitionForce,
            int glowSize)
    {
        assert transitionForce > 0.0f && transitionForce <= 1.0f;
        
        Color glowColor = new Color(
                baseGlowColor.getRed(), 
                baseGlowColor.getGreen(),
                baseGlowColor.getBlue(),
                (int) (transitionForce * 255.0f));
        
        // top
        graphics.setPaint(new GradientPaint(bounds.x, bounds.y, glowColor, bounds.x, bounds.y + glowSize, COLOR_TRANSPARENT));
        graphics.fill(new Rectangle(bounds.x, bounds.y, bounds.x + bounds.width, glowSize));

        // bottom
        graphics.setPaint(new GradientPaint(bounds.x, bounds.y + bounds.height, glowColor, bounds.x, (bounds.y + bounds.height) - glowSize, COLOR_TRANSPARENT));
        graphics.fill(new Rectangle(bounds.x, (bounds.y + bounds.height) - glowSize, bounds.x + bounds.width, glowSize));
        
        // left
        graphics.setPaint(new GradientPaint(bounds.x, bounds.y, glowColor, bounds.x + glowSize, bounds.y, COLOR_TRANSPARENT));
        graphics.fill(new Rectangle(bounds.x, bounds.y, bounds.x + glowSize, bounds.y + bounds.height));
        
        // right
        graphics.setPaint(new GradientPaint(bounds.x + bounds.width, bounds.y, glowColor, (bounds.x + bounds.width) - glowSize, bounds.y, COLOR_TRANSPARENT));
        graphics.fill(new Rectangle((bounds.x + bounds.width) - glowSize, bounds.y, glowSize, bounds.y + bounds.height));
    }
}
