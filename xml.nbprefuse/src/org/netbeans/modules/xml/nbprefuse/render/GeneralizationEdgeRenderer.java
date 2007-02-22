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
 * GeneralizationEdgeRenderer.java
 *
 * Created on October 30, 2005, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.openide.ErrorManager;
import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class GeneralizationEdgeRenderer 
                extends EdgeRenderer {
    
    
    protected static final Polygon GENERLIZATION_ARROW_HEAD =
            new Polygon(new int[] {0,-8,8,0}, new int[] {0,-16,-16,0}, 4);
    
    /** Creates a new instance of GeneralizationEdgeRenderer */
    public GeneralizationEdgeRenderer() {
        super();
        m_arrowHead = GENERLIZATION_ARROW_HEAD;
        setRenderType(RENDER_TYPE_DRAW);    // dflt RENDER_TYPE_DRAW_AND_FILL
        
    }
    
  
    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    public void render(Graphics2D g, VisualItem item) {
        // render the edge line
        super.render(g, item);
        // retain the following order of filling and drawing
        // fill first
        g.setPaint(Color.WHITE);
        g.fill(m_curArrow);
        // then draw outline
        g.setPaint(ColorLib.getColor(item.getStrokeColor()));
        g.draw(m_curArrow);
    }
    
}
