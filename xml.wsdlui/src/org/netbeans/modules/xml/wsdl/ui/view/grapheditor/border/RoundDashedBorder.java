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
/*
 * RoundDashedBorder.java
 *
 * Created on August 19, 2006, 4:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.border.Border;

/**
 *
 * @author useradmin
 */
public class RoundDashedBorder implements Border {
    
    private int mArcWidth;
    private int mArcHeight;
    float[] mDash;
    int mThickness = 0;
    Insets mInsets;
    Color mFillColor;
    Color mDrawColor;
    Stroke mStroke;
    
    /** Creates a new instance of RoundDashedBorder */
    public RoundDashedBorder (int arcWidth, 
                              int arcHeight, 
                              float[] dash, 
                              int thickness,
                              Insets insets,
                              Color fillColor, 
                              Color drawColor ) {
        
        this.mArcWidth = arcWidth;
        this.mArcHeight = arcHeight;
        this.mDash = dash;
        this.mThickness = thickness;
        this.mInsets = insets;
        this.mFillColor = fillColor;
        this.mDrawColor = drawColor;
        
        if (thickness < 1) {
            throw new IllegalArgumentException("Invalid thickness: " + thickness);
        }
        
        mStroke = new BasicStroke(mThickness,
                                  BasicStroke.CAP_BUTT,
                                  BasicStroke.JOIN_ROUND,
                                  BasicStroke.JOIN_MITER,
                                  mDash,
                                  0);
    
    }

    public Insets getInsets() {
        if(this.mInsets == null) {
            this.mInsets = new Insets(mThickness,mThickness,mThickness,mThickness);
        }
        return this.mInsets;
    }

    public void paint(Graphics2D gr, Rectangle bounds) {
        Stroke oldStroke = gr.getStroke();
        Color oldColor = gr.getColor();
        gr.setStroke(mStroke);
        
        if (mFillColor != null) {
            gr.setColor (mFillColor);
            gr.fill (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, mArcWidth, mArcHeight));
        }
        if (mDrawColor != null) {
            gr.setColor (mDrawColor);
            gr.drawRect(bounds.x,bounds.y,bounds.width-mThickness,bounds.height-mThickness);
        
            //gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - mThickness, bounds.height - mThickness, mArcWidth, mArcHeight));
            //gr.drawRoundRect(bounds.x, bounds.y, bounds.width - mThickness, bounds.height -mThickness, mArcWidth, mArcHeight);
        }
        
//        gr.setStroke(oldStroke);
//        gr.setColor(oldColor);
    }

    public boolean isOpaque() {
        return true;
    }
 

}
