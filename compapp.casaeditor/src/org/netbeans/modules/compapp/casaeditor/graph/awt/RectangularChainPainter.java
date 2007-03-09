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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author Josh Sandusky
 */
public abstract class RectangularChainPainter implements Painter {
    
    public static final int ARC_WIDTH  =  4;
    public static final int ARC_HEIGHT =  4;

    private RectangularPaintProvider mProvider;
    private Painter mNextPainter;
    
    
    public RectangularChainPainter(RectangularPaintProvider provider, Painter nextPainter) {
        mProvider = provider;
        mNextPainter = nextPainter;
    }
    
    
    protected RectangularPaintProvider getProvider() {
        return mProvider;
    }
    
    public final void paint(Graphics2D g) {
        
        Rectangle bounds = mProvider.getClipRect();
        
        Shape previousClip = g.getClip();
        if (mProvider.isRounded()) {
            g.clip(new RoundRectangle2D.Float(
                    bounds.x, bounds.y, 
                    bounds.width, bounds.height, 
                    ARC_WIDTH, ARC_HEIGHT));
        } else {
            g.clip(new Rectangle2D.Float(
                    bounds.x, bounds.y, 
                    bounds.width, bounds.height));
        }
        
        chainPaint(g);
        
        if (mNextPainter != null) {
            mNextPainter.paint(g);
        }
                
        g.setClip(previousClip);
    }
    
    public abstract void chainPaint(Graphics2D g);
}
