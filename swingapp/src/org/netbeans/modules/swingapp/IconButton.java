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

package org.netbeans.modules.swingapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import javax.swing.JButton;

/**
 * A custom JButton which shows a selected icon or a dashed rectangle if
 * there is no icon selected.
 * @author joshua.marinacci@sun.com
 */
public class IconButton extends JButton {
    private String iconText;
    /** Creates a new instance of IconButton */
    public IconButton() {
    }
    
    public void setIconText(String iconText) {
        this.iconText = iconText;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if(this.getIcon() == null) {
            Graphics2D g2  = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            
            float[] dash = new float[] { 10f, 3f };
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,1f,dash, 0f));
            
            float in = 2;
            Shape rect = generateSeamlessRoundRect(in,in,(float)getWidth()-in*2,(float)getHeight()-in*2,12f);
            g2.draw(rect);
            g2.getFont().getStringBounds(iconText,g2.getFontRenderContext());
            g2.dispose();
        } else {
            super.paintComponent(g);
        }
    }

    // this is a special round rect that will draw a dashed stroke symmetrically.
    private static GeneralPath generateSeamlessRoundRect(final float rx, final float ry, 
            final float rw, final float rh, final float rr) {
        
        GeneralPath gp = new GeneralPath();
        // upper left
        gp.append(new Arc2D.Float(rx,ry, rr,rr, 0+90+45, 45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx, ry+rr/2, rx, ry+rh/2),true);
        gp.append(new Arc2D.Float(rx,ry, rr,rr, 0+90+45, -45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx+rr/2, ry, rx+rw/2, ry),true);
        
        
        // lower left
        gp.append(new Arc2D.Float(rx,ry+rh-rr, rr,rr, 0+180+45, -45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx, ry+rh-rr, rx, ry+rh/2),true);
        gp.append(new Arc2D.Float(rx,ry+rh-rr, rr,rr, 0+180+45, +45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx+rr/2, ry+rh, rx+rw/2, ry+rh),true);
        
        
        // lower right
        gp.append(new Arc2D.Float(rx+rw-rr,ry+rh-rr, rr,rr, -45, -45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx+rw-rr/2, ry+rh, rx+rw/2, ry+rh),true);
        gp.append(new Arc2D.Float(rx+rw-rr,ry+rh-rr, rr,rr, -45, 45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx+rw, ry+rh-rr, rx+rw, ry+rh/2),true);
        
        
        // upper right
        gp.append(new Arc2D.Float(rx+rw-rr, ry, rr,rr, 45, -45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx+rw, ry+rr/2, rx+rw, ry+rh/2),true);
        gp.append(new Arc2D.Float(rx+rw-rr, ry, rr,rr, 45, +45, Arc2D.OPEN),false);
        gp.append(new Line2D.Float(rx+rw-rr/2, ry, rx+rw/2, ry),true);
        return gp;
    }
    
    
}
