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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;

/**
 * @author Ajit Bhate
 */
public abstract class AbstractTitledWidget extends Widget implements ExpandableWidget {
    
    private static final Color BORDER_COLOR = Color.GRAY;
    private static final int RADIUS = 10;
    private static final int DEPTH = 3;
    private static final boolean raised = true;
    
    private Color borderColor = BORDER_COLOR;
    private int radius = RADIUS;
    
    private boolean expanded;
    private transient HeaderWidget headerWidget;
    private transient ExpanderWidget expander;
    
    /**
     * Creates a new instance of RoundedRectangleWidget
     * with default rounded radius and gap and no gradient color
     * @param scene scene this widget belongs to
     * @param radius of the rounded arc
     * @param color color of the border and gradient title
     */
    public AbstractTitledWidget(Scene scene, int radius, Color color) {
        super(scene);
        this.radius = radius;
        this.borderColor = color;
        setBorder(BorderFactory.createEmptyBorder(radius+(raised?DEPTH:0)));
        headerWidget = new HeaderWidget(getScene(), this);
        headerWidget.setLayout(new LeftRightLayout(32));
        addChild(headerWidget);
        if(isExpandable()) {
            expanded = ExpanderWidget.isExpanded(this, true);
            expander = new ExpanderWidget(getScene(), this, expanded);
        }
    }
    
    protected HeaderWidget getHeaderWidget() {
        return headerWidget;
    }
    
    protected ExpanderWidget getExpanderWidget() {
        return expander;
    }
    
    protected final void paintWidget() {
        Rectangle bounds = getBounds();
        Graphics2D g = getGraphics();
        Paint oldPaint = g.getPaint();
        if(raised) {
            g.setPaint(borderColor);
            RoundRectangle2D outerRect = new RoundRectangle2D.Double(
                    bounds.x + DEPTH, bounds.y + DEPTH,
                    bounds.width - DEPTH, bounds.height - DEPTH, radius, radius);
            Shape oldClip = g.getClip();
            g.setClip(bounds.x + bounds.width - DEPTH, bounds.y,
                    DEPTH, bounds.height);
            g.fill(outerRect);
            g.setClip(bounds.x, bounds.y + bounds.height - DEPTH,
                    bounds.width, DEPTH);
            g.fill(outerRect);
            g.setClip(oldClip);
            Arc2D arc = new Arc2D.Double(bounds.x + bounds.width - DEPTH - radius,
                    bounds.y + bounds.height - radius - DEPTH, radius, radius,0,-90,Arc2D.OPEN);
            GeneralPath gpath = new GeneralPath(arc);
            gpath.lineTo(bounds.x + bounds.width - DEPTH, bounds.y + bounds.height - DEPTH);
            gpath.closePath();
            g.fill(gpath);
        }
        RoundRectangle2D rect = new RoundRectangle2D.Double(
                bounds.x, bounds.y, bounds.width - (raised?DEPTH:0),
                bounds.height - (raised?DEPTH:0), radius, radius);
        if(isExpanded()) {
            Shape oldClip = g.getClip();
            int titleHeight = radius + headerWidget.getBounds().height + radius/2;
            GradientPaint gp = new GradientPaint(bounds.x, bounds.y, borderColor,
                    bounds.x, bounds.y + titleHeight/2, borderColor.brighter(),true);
            g.setClip(bounds.x, bounds.y, bounds.width, titleHeight);
            g.setPaint(gp);
            g.fill(rect);
            g.setClip(oldClip);
        } else {
            GradientPaint gp = new GradientPaint(bounds.x, bounds.y, borderColor,
                    bounds.x, (bounds.y + bounds.height - (raised?DEPTH:0))/2,
                    borderColor.brighter(),true);
            g.setPaint(gp);
            g.fill(rect);
        }
        g.setPaint(borderColor);
        g.draw(rect);
        g.setPaint(oldPaint);
    }
    
    protected void collapseWidget() {
    }
    
    protected void expandWidget() {
    }
    
    public Object hashKey() {
        return null;
    }
    
    public void setExpanded(boolean expanded) {
        if(!isExpandable()) return;
        if(this.expanded != expanded) {
            this.expanded = expanded;
            if(expanded) {
                expandWidget();
            } else {
                collapseWidget();
            }
            expander.setExpanded(expanded);
        }
    }
    
    public boolean isExpanded() {
        return expanded;
    }
    
    protected boolean isExpandable() {
        return true;
    }
}
