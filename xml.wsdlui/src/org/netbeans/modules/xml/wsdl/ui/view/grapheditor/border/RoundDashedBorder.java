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
