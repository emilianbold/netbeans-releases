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
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;

/**
 * @author Ajit Bhate
 */
public abstract class AbstractTitledWidget extends AbstractMouseActionsWidget implements ExpandableWidget {
    
    private static final Color BORDER_COLOR = Color.GRAY;
    private static final int RADIUS = 10;
    
    private Color borderColor = BORDER_COLOR;
    private int radius = RADIUS;
    private int depth = radius/4;
    
    private boolean expanded;
    private transient HeaderWidget headerWidget;
    private transient Widget contentWidget;
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
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        depth = radius/4;
        setBorder(new RoundedBorder3D(radius, depth, 0, 0, color));
        headerWidget = new HeaderWidget(getScene(), this);
        headerWidget.setBorder(BorderFactory.createEmptyBorder(radius/2));
        headerWidget.setLayout(new LeftRightLayout(32));
        addChild(headerWidget);
        if(isExpandable()) {
            contentWidget = createContentWidget();
            expanded = ExpanderWidget.isExpanded(this, true);
            if(expanded) {
                expandWidget();
            } else {
                collapseWidget();
            }
            expander = new ExpanderWidget(getScene(), this, expanded);
        }
//        getActions().addAction(ButtonAction.DEFAULT);
    }
    
    protected Widget getContentWidget() {
        return contentWidget;
    }
    
    protected final Widget createContentWidget() {
        Widget widget = new Widget(getScene());
        widget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, radius));
        widget.setBorder(BorderFactory.createEmptyBorder(radius/2));
        return widget;
    }

    protected HeaderWidget getHeaderWidget() {
        return headerWidget;
    }
    
    protected ExpanderWidget getExpanderWidget() {
        return expander;
    }
    
    protected final void paintWidget() {
        Rectangle bounds = getClientArea();
        Graphics2D g = getGraphics();
        Paint oldPaint = g.getPaint();
        RoundRectangle2D rect = new RoundRectangle2D.Double(bounds.x + 0.75f,
                bounds.y, bounds.width - 1.5f, bounds.height, radius, radius);
        if(isExpanded()) {
            int titleHeight = headerWidget.getBounds().height;
            GradientPaint gp = new GradientPaint(bounds.x, bounds.y, borderColor,
                    bounds.x, bounds.y + titleHeight/2, borderColor.brighter(),true);
            g.setPaint(gp);
            Area titleArea = new Area(rect);
            titleArea.subtract(new Area(new Rectangle(bounds.x,
                    bounds.y + titleHeight, bounds.width, bounds.height)));
            g.fill(titleArea);
        } else {
            GradientPaint gp = new GradientPaint(bounds.x, bounds.y, borderColor,
                    bounds.x, bounds.y + bounds.height/2,
                    borderColor.brighter(),true);
            g.setPaint(gp);
            g.fill(rect);
        }
        g.setPaint(oldPaint);
    }
    
    protected void collapseWidget() {
        if(getContentWidget().getParentWidget()!=null) {
            removeChild(getContentWidget());
        }
    }
    
    protected void expandWidget() {
        if(getContentWidget().getParentWidget()==null) {
            addChild(getContentWidget());
        }
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
