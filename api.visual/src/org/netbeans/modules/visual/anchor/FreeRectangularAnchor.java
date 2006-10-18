/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.anchor;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import org.netbeans.api.visual.anchor.Anchor.Direction;

/**
 * @author Alex
 */
public final class FreeRectangularAnchor extends Anchor {
    
    private boolean includeBorders;
    
    public FreeRectangularAnchor(Widget widget, boolean includeBorders) {
        super(widget);
        this.includeBorders = includeBorders;
    }
    
    public Result compute(Entry entry) {
        assert entry.getAttachedConnectionWidget()instanceof ConnectionWidget;
        Point relatedLocation = getRelatedSceneLocation();
        
        Widget widget = (Widget)getRelatedWidget();
        ConnectionWidget fcw=entry.getAttachedConnectionWidget();
        Point oppositeLocation =fcw.getControlPoint(1);
        if(oppositeLocation==null){
            oppositeLocation=getOppositeSceneLocation(entry);
        }else
            if(entry.equals(fcw.getSourceAnchorEntry())){
            oppositeLocation =fcw.getControlPoint(1);
            }else{
            oppositeLocation =fcw.getControlPoint(fcw.getControlPoints().size()-2);
            }
        Rectangle bounds = widget.getBounds();
        if (! includeBorders) {
            Insets insets = widget.getBorder().getInsets();
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        bounds = widget.convertLocalToScene(bounds);
        
        if (bounds.isEmpty()  || relatedLocation.equals(oppositeLocation))
            return new Anchor.Result(relatedLocation, Anchor.DIRECTION_ANY);
        
        float dx = oppositeLocation.x - relatedLocation.x;
        float dy = oppositeLocation.y - relatedLocation.y;
        
        float ddx = Math.abs(dx) / (float) bounds.width;
        float ddy = Math.abs(dy) / (float) bounds.height;
        
        Anchor.Direction direction;
        
        if (ddx >= ddy) {
            direction = dx >= 0.0f ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = dy >= 0.0f ? Direction.BOTTOM : Direction.TOP;
        }
        
        float scale = 0.5f / Math.max(ddx, ddy);
        
        Point point = new Point(Math.round(relatedLocation.x + scale * dx), Math.round(relatedLocation.y + scale * dy));
        return new Anchor.Result(point, direction);
    }
    
}
