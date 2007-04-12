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

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.awt.Dimension;
import java.awt.Point;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;

/**
 *
 * @author Josh Sandusky
 */
public abstract class CustomizablePersistLayout implements Layout {
    
    private int mYSpacing;
    private boolean mIsAnimating;
    private boolean mIsAdjustingForOverlapOnly;
    
    
    public void justify(Widget widget) {
    }
    
    public boolean requiresJustification (Widget widget) {
        return false;
    }
    
    public void setYSpacing(int spacing) {
        mYSpacing = spacing;
    }
    
    public void setIsAdjustingForOverlapOnly(boolean isOverlapOnly) {
        mIsAdjustingForOverlapOnly = isOverlapOnly;
    }
    
    public void setIsAnimating(boolean isAnimating) {
        mIsAnimating = isAnimating;
    }
    
    protected int getYSpacing() {
        return mYSpacing;
    }
    
    protected boolean isAdjustingForOverlapOnly() {
        return mIsAdjustingForOverlapOnly;
    }
    
    protected boolean isAnimating() {
        return mIsAnimating;
    }
    
    protected void moveWidget(CasaNodeWidget widget, Point location, boolean isRightAligned) {
        location = adjustLocation(widget, location.x, location.y, isRightAligned);
        if (isAnimating()) {
            widget.getScene().getSceneAnimator().animatePreferredLocation(widget, location);
        } else {
            widget.setPreferredLocation(location);
        }
    }
    
    
    private static Point adjustLocation(
            Widget widget,
            int suggestedX,
            int suggestedY,
            boolean isRightAligned)
    {
        CasaRegionWidget region = (CasaRegionWidget) widget.getParentWidget();
        // Ensure widget location is not on top of the region label.
        if (suggestedY < region.getTitleYOffset()) {
            suggestedY = region.getTitleYOffset();
        }
        Dimension widgetSize = widget.getBounds().getSize();
        if (isRightAligned) {
            suggestedX = region.getBounds().width - widgetSize.width;
            Point currentLocation = widget.getLocation();
            if (currentLocation.x != suggestedX) {
                // Do not animate horizontally for right-aligned widgets.
                widget.setPreferredLocation(new Point(suggestedX, currentLocation.y));
            }
        } else if (suggestedX + widgetSize.width > region.getBounds().width) {
            suggestedX =
                    region.getBounds().width -
                    widgetSize.width -
                    30; // Position the widget a short gap from the right edge.
            if (suggestedX  < 0) {
                suggestedX = 0;
            }
        }

        return new Point(suggestedX, suggestedY);
    }
}
