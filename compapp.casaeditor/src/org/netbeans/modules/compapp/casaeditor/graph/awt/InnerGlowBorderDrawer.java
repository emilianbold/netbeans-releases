/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
