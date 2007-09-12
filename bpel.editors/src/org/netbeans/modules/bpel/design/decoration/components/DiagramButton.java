/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bpel.design.decoration.components;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import org.netbeans.modules.bpel.design.decoration.components.ZoomableDecorationComponent;

/**
 *
 * @author aa160298
 */
public class DiagramButton extends JButton implements ZoomableDecorationComponent {

    private double zoom = 1;
    private Icon2D icon2D;
    
    
    public DiagramButton(Icon2D icon2D) {
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorder(null);
        setFocusable(false);
        this.icon2D = icon2D;
    }

    
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_PURE);
        
        double strokeWidth = STROKE.getLineWidth();
        
        Shape border = new RoundRectangle2D.Double(
                strokeWidth / 2, 
                strokeWidth / 2,
                getWidth() - strokeWidth,
                getHeight() - strokeWidth, 6, 6);
        
        ButtonModel model = getModel();
        
        if (model.isArmed()) {
            g2.setPaint(ARMED_FILL);
            g2.fill(border);
        } else if (model.isRollover()) {
            g2.setPaint(DEFAULT_FILL);
            g2.fill(border);
        }
        

        if (model.isRollover()) {
            g2.setPaint(ROLLOVER_STROKE);
            g2.setStroke(STROKE);
            g2.draw(border);
        }

        int inset = 1 + (int) Math.round(zoom * (DEFAULT_INSET - 1));
        int iconWidth = getWidth() - 2 * inset;
        int iconHeight = getHeight() - 2 * inset;
        
        if ((iconWidth > 0) && (iconHeight > 0)) {
            icon2D.paint(g, inset, inset, iconWidth, iconHeight);
        }
    }


    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    
    
    public Dimension getPreferredSize() {
        int w = (int) Math.max(1, Math.round(zoom * DEFAULT_WIDTH));
        int h = (int) Math.max(1, Math.round(zoom * DEFAULT_HEIGHT));
        return new Dimension(w, h);
    }
    
    
    private static final int DEFAULT_WIDTH = 14;
    private static final int DEFAULT_HEIGHT = 14;
    private static final int DEFAULT_INSET = 3;
    
    private static final BasicStroke STROKE = new BasicStroke(1.2f);
    private static final Color DEFAULT_FILL = new Color(0xFFFFFF);
    private static final Color ARMED_FILL = new Color(0xEEEEEE);
    private static final Color ROLLOVER_STROKE = new Color(0xA9A9A9);
}
