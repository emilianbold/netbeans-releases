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
import java.awt.Rectangle;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;

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
    
    protected void moveWidget(final CasaNodeWidget widget, Point location, boolean isRightAligned) {
        location = adjustLocation(widget, location.x, location.y, isRightAligned);
        ((CasaModelGraphScene) widget.getScene()).persistLocation(widget, location);
        if (isAnimating()) {
            animateTo(widget, location);
        } else {
            widget.setPreferredLocation(location);
        }
    }
    
    private Point adjustLocation(
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
            if (isAnimating() && currentLocation.x != suggestedX) {
                // Do not animate horizontally for right-aligned widgets.
                widget.setPreferredLocation(new Point(suggestedX, currentLocation.y));
            }
        }
        return new Point(suggestedX, suggestedY);
    }
    
    private void animateTo(final Widget widget, Point location) {
        final CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        
        Object widgetObject = scene.findObject(widget);
        if (
                scene.getSelectedObjects().size() == 1 && 
                scene.getSelectedObjects().contains(widgetObject)) {
            // If the widget is selected:
            // 1. we ensure there is enough space to fit it (widget may be new)
            // 2. we scroll to it if it is hidden
            //    note: step 1. is required for step 2. to work, because we cannot
            //          scroll within an area if the area is too small to scroll around in
            AnimatorListener listener = new AnimatorListener() {
                public void animatorFinished(AnimatorEvent event) {
                    // Other preferredLocation animations may not want this behavior, so
                    // we remove ourselves as a listener so we only trigger from here.
                    scene.getSceneAnimator().getPreferredLocationAnimator().removeAnimatorListener(this);
                    // stretch the scene to ensure the widget fits within its region
                    // a widget may not fit within its region if it was just dropped from the palette
                    RegionUtilities.stretchScene(scene);
                    Rectangle sceneRect = widget.convertLocalToScene(new Rectangle(
                            widget.getLocation(),
                            widget.getBounds().getSize()));
                    scene.getView().scrollRectToVisible(sceneRect);
                }
                public void animatorStarted(AnimatorEvent event)  {}
                public void animatorReset(AnimatorEvent event)    {}
                public void animatorPreTick(AnimatorEvent event)  {}
                public void animatorPostTick(AnimatorEvent event) {}
            };
            scene.getSceneAnimator().getPreferredLocationAnimator().addAnimatorListener(listener);
        }
        
        scene.getSceneAnimator().animatePreferredLocation(widget, location);
    }
}
