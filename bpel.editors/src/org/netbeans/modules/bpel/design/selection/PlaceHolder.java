/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.bpel.design.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geom.FEllipse;
import org.netbeans.modules.bpel.design.geom.FPath;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.geom.FShape;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

public abstract class PlaceHolder {
    
    private Pattern ownerPattern;
    private Pattern draggedPattern;
    private boolean mouseHover;
    private FPath path;
    private FShape shape;
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            float cx, float cy) {
        this(ownerPattern, draggedPattern, cx, cy, null);
    }
    

    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            FPoint center) {
        this(ownerPattern, draggedPattern, center.x, center.y, null);
    }
    
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern, FPath path) {
        this(ownerPattern, draggedPattern, path.point(0.5f), path);
    }
    
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            FPoint center, FPath path) {
        this(ownerPattern, draggedPattern, center.x, center.y, path);
    }
    
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            float cx, float cy, FPath path) {
        this.ownerPattern = ownerPattern;
        this.draggedPattern = draggedPattern;
        this.shape = SHAPE.centerTo(cx, cy);
        this.path = path;
    }
    
    
    public boolean contains(float px, float py) {
        return shape.position(px, py) >= 0;
    }
    
    
    public Pattern getDraggedPattern() {
        return draggedPattern;
    }
    
    
    public Pattern getOwnerPattern() {
        return ownerPattern;
    }
    
    
    public boolean isMouseHover() {
        return mouseHover;
    }
    
    
    public void dragEnter() {
        mouseHover = true;
    }
    
    public void dragExit() {
        mouseHover = false;
    }
    
    
    public abstract void drop();
    
    
    public void paint(Graphics2D g2) {
        Shape outerShape = GUtils.convert(shape);
        
        GUtils.setPaint(g2, OUTER_FILL);
        GUtils.fill(g2, outerShape);
        
        Point2D realCenter = GUtils.getNormalizedCenter(g2, outerShape);
        
        g2.translate(realCenter.getX(), realCenter.getY());
        
        GUtils.setPaint(g2, (isMouseHover()) ? INNER_FILL_MOUSE_HOVER : INNER_FILL);
        GUtils.fill(g2, INNER_SHAPE);
        
        GUtils.setPaint(g2, INNER_STROKE);
        GUtils.setSolidStroke(g2, 1);
        GUtils.draw(g2, INNER_SHAPE, false);
        
        g2.translate(-realCenter.getX(), -realCenter.getY());

        GUtils.setDashedStroke(g2, 1, 3);
        GUtils.setPaint(g2, OUTER_STROKE);
        GUtils.draw(g2, outerShape, true);
    }
    
  
    private static final Shape INNER_SHAPE = new Ellipse2D.Float(-5, -5, 10, 10);
    
    private static final FShape SHAPE = new FEllipse(20);
    
    private static final Color OUTER_FILL = new Color(0xFCFAF5);
    private static final Color OUTER_STROKE = new Color(0xFF9900);
    private static final Color INNER_FILL = new Color(0xFFFFFF);
    private static final Color INNER_FILL_MOUSE_HOVER = new Color(0xFF9900);
    private static final Color INNER_STROKE = new Color(0xB3B3B3);
}
