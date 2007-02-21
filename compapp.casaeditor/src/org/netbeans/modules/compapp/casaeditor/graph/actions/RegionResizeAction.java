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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.compapp.casaeditor.graph.*;


/**
 * @author Josh Sandusky (based off of David Kaspar's MoveAction)
 */
public final class RegionResizeAction extends WidgetAction.LockedAdapter {

    private MoveStrategy mStrategy;
    private MoveProvider mProvider;

    private Widget movingWidget;
    private Point dragSceneLocation;
    private Point originalSceneLocation;
    
    private Scene mScene;
    private Widget mLeftResizer;
    private Widget mMiddleResizer;

    
    public RegionResizeAction(
            Scene scene, 
            Widget leftResizer, 
            Widget middleResizer,
            RegionResizeHandler handler)
    {
        mScene = scene;
        mLeftResizer = leftResizer;
        mMiddleResizer = middleResizer;
        mProvider = handler;
        mStrategy = handler;
    }
    

    protected boolean isLocked () {
        return movingWidget != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            
            // Swap out the Scene widget with the actual widget being moved.
            // The actual widget that is moved is the resizer widget.
            if        (isLeftHit(event)) {
                widget = mLeftResizer;
            } else if (isMiddleHit(event)) {
                widget = mMiddleResizer;
            } else {
                return State.REJECTED;
            }
            
            // Make the resizer visible while we're moving it.
            widget.setOpaque(true);
            
            movingWidget = widget;
            originalSceneLocation = mProvider.getOriginalLocation(widget);
            if (originalSceneLocation == null)
                originalSceneLocation = new Point ();
            dragSceneLocation = event.getPoint();
            mProvider.movementStarted (widget);
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (!isLocked()) {
            return State.REJECTED;
        }
        
        // Swap out the Scene widget with the actual widget being moved.
        widget = movingWidget;
        
        // Resize finished, hide the resizer.
        widget.setOpaque(false);
        
        boolean state = move (widget, event.getPoint ());
        if (state) {
            movingWidget = null;
            mProvider.movementFinished (widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        if (!isLocked()) {
            return State.REJECTED;
        }
        
        // Swap out the Scene widget with the actual widget being moved.
        widget = movingWidget;
        
        return 
                move(widget, event.getPoint ()) ? 
                    State.createLocked (widget, this) : 
                    State.REJECTED;
    }

    private boolean move (Widget widget, Point newLocation) {
        if (movingWidget != widget)
            return false;
        newLocation = widget.convertLocalToScene (newLocation);
        Point location = new Point(
                originalSceneLocation.x + newLocation.x - dragSceneLocation.x, 
                originalSceneLocation.y + newLocation.y - dragSceneLocation.y);
        mProvider.setNewLocation (
                widget, 
                mStrategy.locationSuggested(widget, originalSceneLocation, location));
        return true;
    }

    // Whether the mouse is currently over the left resizer.
    private boolean isLeftHit(WidgetMouseEvent event) {
        return mLeftResizer.isHitAt(mLeftResizer.convertSceneToLocal(event.getPoint()));
    }
    
    // Whether the mouse is currently over the middle resizer.
    private boolean isMiddleHit(WidgetMouseEvent event) {
        return mMiddleResizer.isHitAt(mMiddleResizer.convertSceneToLocal(event.getPoint()));
    }
    
    // Handles the hover-over visual feedback, which is the resize cursor.
    public State mouseMoved(Widget widget, WidgetMouseEvent event) {
        if (isLocked()) {
            return State.REJECTED;
        }
        
        if        (isLeftHit(event)) {
            mScene.getView().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else if (isMiddleHit(event)) {
            mScene.getView().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else if (mScene.getView().getCursor() != Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
            mScene.getView().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return State.REJECTED;
    }
}
