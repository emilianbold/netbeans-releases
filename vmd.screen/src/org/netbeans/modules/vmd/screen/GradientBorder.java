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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.screen;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;

/**
 * Painting algorithm copied from VMDNodeBorder
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class GradientBorder extends AbstractBorder {
    
    static final Color COLOR_BORDER = new Color(0xBACDF0);
    static final Color COLOR_SELECTION = Color.BLUE;
    
    private static final int INSET_SIZE = 2;
    private static final Insets INSETS = new Insets(INSET_SIZE, INSET_SIZE, INSET_SIZE, INSET_SIZE);
    private static final Color COLOR1 = new Color(221, 235, 246, 64);
    private static final Color COLOR2 = new Color(255, 255, 255, 64);
    private static final Color COLOR3 = new Color(214, 235, 255, 64);
    private static final Color COLOR4 = new Color(241, 249, 253, 64);
    private static final Color COLOR5 = new Color(255, 255, 255, 64);
    
    private boolean isSelected;
    
    public GradientBorder(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D gr = (Graphics2D) g;
        Shape previousClip = gr.getClip();
        gr.clip(new RoundRectangle2D.Float(x, y, width, height, 4, 4));
        
        Rectangle bounds = new Rectangle(x, y, width, height);
        drawGradient(gr, bounds, COLOR1, COLOR2, 0f, 0.3f);
        drawGradient(gr, bounds, COLOR2, COLOR3, 0.3f, 0.764f);
        drawGradient(gr, bounds, COLOR3, COLOR4, 0.764f, 0.927f);
        drawGradient(gr, bounds, COLOR4, COLOR5, 0.927f, 1f);
        
        gr.setColor(COLOR_BORDER);
        gr.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1, height - 1, 4, 4));
        
        if (isSelected) {
            gr.setColor(COLOR_SELECTION);
            gr.drawRect(x + 3, y + 3, width - 7, height - 7);
        }
        
        gr.setClip(previousClip);
    }
    
    private void drawGradient(Graphics2D gr, Rectangle bounds, Color color1, Color color2, float y1, float y2) {
        y1 = bounds.y + y1 * bounds.height;
        y2 = bounds.y + y2 * bounds.height;
        gr.setPaint(new GradientPaint(bounds.x, y1, color1, bounds.x, y2, color2));
        gr.fill(new Rectangle.Float(bounds.x, y1, bounds.x + bounds.width, y2));
    }
    
    public Insets getBorderInsets(Component c) {
        return INSETS;
    }
    
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = INSET_SIZE;
        return insets;
    }
    
    public boolean isBorderOpaque() {
        return true;
    }
}
