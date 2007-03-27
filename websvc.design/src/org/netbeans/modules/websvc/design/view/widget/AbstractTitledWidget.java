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
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;

/**
 * @author Ajit Bhate
 */
public abstract class AbstractTitledWidget extends Widget implements ExpandableWidget {
    
    private static final Color FILL_COLOR_DARK = Color.WHITE;
    private static final Color FILL_COLOR_LIGHT = Color.WHITE;
    private static final Color BORDER_COLOR = Color.GRAY;
    private static final int RADIUS = 10;
    private static final int DEPTH = 2;
    private static final boolean raised = true;
    
    private Color fillColorDark = FILL_COLOR_DARK;
    private Color fillColorLight = FILL_COLOR_LIGHT;
    private Color borderColor = BORDER_COLOR;
    private int radius = RADIUS;
    
    private boolean expanded;
    private transient HeaderWidget headerWidget;
    private transient ExpanderWidget expander;
    
    /**
     * Creates a new instance of RoundedRectangleWidget
     * with default rounded radius and gap and no gradient color
     * @param scene
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
            g.fillRoundRect(bounds.x+DEPTH, bounds.y+DEPTH, bounds.width-DEPTH,
                    bounds.height-DEPTH, radius, radius);
        }
        if(isExpanded()) {
            int titleHeight = headerWidget.getBounds().height;
            int bodyY = bounds.y + radius + titleHeight + radius/2 ;
            int footerY = bounds.y + bounds.height - radius/2 - (raised?DEPTH:0)*2;
            GradientPaint gp = new GradientPaint(bounds.x, bounds.y, borderColor,
                    bounds.x, bodyY, borderColor.brighter());
            g.setPaint(gp);
            g.fillRoundRect(bounds.x, bounds.y, bounds.width-(raised?DEPTH:0),
                    bodyY-bounds.y+radius/2, radius, radius);
            gp = new GradientPaint(bounds.x, footerY-radius, borderColor,
                    bounds.x, bounds.y+bounds.height-(raised?DEPTH:0), borderColor.brighter());
            g.setPaint(gp);
            g.fillRoundRect(bounds.x, footerY-radius/2, bounds.width-(raised?DEPTH:0),
                    bounds.height-footerY-(raised?DEPTH:0)-radius/2-(raised?DEPTH:0), radius, radius);
            GradientPaint gpb = new GradientPaint(bounds.x, bodyY, fillColorDark,
                    bounds.x, footerY, fillColorLight);
            g.setPaint(gpb);
            g.fillRect(bounds.x, bodyY, bounds.width-(raised?DEPTH:0),footerY-bodyY);
        } else {
            GradientPaint gp = new GradientPaint(bounds.x, bounds.y, borderColor,
                    bounds.x, bounds.y+bounds.height, borderColor.brighter());
            g.setPaint(gp);
            g.fillRoundRect(bounds.x, bounds.y, bounds.width-(raised?DEPTH:0),
                    bounds.height-(raised?DEPTH:0), radius, radius);
        }
        g.setPaint(borderColor);
        g.drawRoundRect(bounds.x, bounds.y, bounds.width-(raised?DEPTH:0),
                bounds.height-(raised?DEPTH:0), radius, radius);
        g.setPaint(oldPaint);
    }
    
    public void collapseWidget() {
    }
    
    public void expandWidget() {
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
