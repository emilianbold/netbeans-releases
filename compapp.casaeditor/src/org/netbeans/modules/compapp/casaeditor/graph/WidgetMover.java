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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;

/**
 *
 * @author jsandusky
 */
public class WidgetMover implements MoveStrategy, MoveProvider {
    
    private CasaWrapperModel mModel;
    private boolean mCanMoveHorizontal;
    private boolean mCanStretchHorizontal;
    private Point mSnapPoint;
    private Point mOriginalLocation;
    
    
    public WidgetMover(
            CasaWrapperModel model, 
            boolean canMoveHorizontal,
            boolean canStretchHorizontal) {
        mModel = model;
        mCanMoveHorizontal = canMoveHorizontal;
        mCanStretchHorizontal = canStretchHorizontal;
    }
    
    
    // MOVE STRATEGY
    
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        
        if (originalLocation.equals(suggestedLocation)) {
            return originalLocation;
        }
        
        mSnapPoint = null;
        
        assert widget instanceof CasaNodeWidget;
        CasaNodeWidget moveWidget = (CasaNodeWidget) widget;
        Point retPoint = new Point();
        retPoint.x = originalLocation.x;
        retPoint.y = originalLocation.y;
        CasaRegionWidget region = (CasaRegionWidget) moveWidget.getParentWidget();
        Rectangle parentBounds = region.getBounds();
        Rectangle widgetBounds = moveWidget.getBounds();
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        
        if (parentBounds != null && widgetBounds != null) {
            int regionHSpace  = parentBounds.x + parentBounds.width;
            int minRegionX    = parentBounds.x + RegionUtilities.HORIZONTAL_LEFT_WIDGET_GAP;
            int widgetHExtent = widgetBounds.width + RegionUtilities.HORIZONTAL_RIGHT_WIDGET_GAP;
            
            // Endure widget location is within the region bounds.
            if (mCanMoveHorizontal) {
                if (suggestedLocation.x < minRegionX) {
                    retPoint.x = minRegionX;
                } else if (
                        !mCanStretchHorizontal && 
                        suggestedLocation.x + widgetHExtent > regionHSpace) {
                    retPoint.x = regionHSpace - widgetHExtent;
                    retPoint.x = retPoint.x < 0 ? 0 : retPoint.x;
                } else {
                    retPoint.x = suggestedLocation.x;
                }
            }
            
            if (suggestedLocation.y < parentBounds.y) {
                retPoint.y = parentBounds.y;
            } else {
                retPoint.y = suggestedLocation.y;
            }
            
            // If widget can move horizontally but cannot cause the region to stretch,
            // then ensure the widget doesn't move horizontally if there is no 
            // horizontal space for the widget to move.
            if (
                    mCanMoveHorizontal     && 
                    !mCanStretchHorizontal && 
                    regionHSpace - (minRegionX + widgetHExtent) <= 0) {
                retPoint.x = originalLocation.x;
            }
            
            // Ensure widget location is not on top of the region label.
            if (retPoint.y < region.getTitleYOffset()) {
                retPoint.y = region.getTitleYOffset();
            }
        }
        
        // Ensure the widget location is not directly on top of another widget.
        if (moveWidget instanceof CasaNodeWidgetBinding) {
            // TODO we currently only check overlap on wsdl endpoints.
            mSnapPoint = checkWidgetOverlap(moveWidget, retPoint.x, retPoint.y);
        }
        
        return retPoint;
    }
    
    private static CasaNodeWidget getOverlappedWidget(
            Widget parentWidget, 
            Widget[] widgetsToIgnore, 
            Rectangle boundsToCheck)
    {
        for (Widget childWidget : parentWidget.getChildren()) {
            if (childWidget instanceof CasaNodeWidget) {
                boolean isIgnored = false;
                for (Widget widgetToIgnore : widgetsToIgnore) {
                    if (childWidget == widgetToIgnore) {
                        isIgnored = true;
                        break;
                    }
                }
                if (!isIgnored) {
                    CasaNodeWidget childNodeWidget = (CasaNodeWidget) childWidget;
                    if (childNodeWidget.getEntireBounds().intersects(boundsToCheck)) {
                        return childNodeWidget;
                    }
                }
            }
        }
        return null;
    }
        
    private static Point checkWidgetOverlap(
            CasaNodeWidget moveWidget, 
            int suggestedX, 
            int suggestedY)
    {
        CasaNodeWidget overlappedWidget = getOverlappedWidget(
                moveWidget.getParentWidget(), 
                new Widget[] { moveWidget }, 
                moveWidget.getEntireBounds());
        if (overlappedWidget != null) {
            return fixWidgetOverlap(moveWidget, overlappedWidget, suggestedX, suggestedY);
        }
        return null;
    }
    
    private static Point fixWidgetOverlap(
            CasaNodeWidget moveWidget, 
            CasaNodeWidget overlappedWidget,
            int suggestedX,
            int suggestedY)
    {
        Rectangle moveWidgetRect = moveWidget.getEntireBounds();
        Rectangle overlappedWidgetRect = overlappedWidget.getEntireBounds();
        
        int topOfMoveWidget    = moveWidget.getLocation().y;
        int bottomOfMoveWidget = moveWidget.getLocation().y + moveWidgetRect.height;
        int halfwayOfMoveWidget =
                moveWidget.getLocation().y +
                (moveWidgetRect.height / 2);
        int halfwayOfOverlappedWidget =
                overlappedWidget.getLocation().y +
                (overlappedWidgetRect.height / 2);
        
        // We have four conditions we need to check for.
        
        if (
        // 1. bottom of moveWidget  is below halfway of overlappedWidget
                bottomOfMoveWidget <= halfwayOfOverlappedWidget) {
            // set the snap to keep moveWidget above
            Point fixedLocation = new Point(suggestedX, suggestedY);
            fixedLocation.y = overlappedWidget.getLocation().y - moveWidgetRect.height;
            return fixedLocation;
            
        } else if (
        // 2. bottom of moveWidget  is below halfway of overlappedWidget
        //    halfway of moveWidget is above halfway of overlappedWidget
                bottomOfMoveWidget > halfwayOfOverlappedWidget &&
                halfwayOfMoveWidget < halfwayOfOverlappedWidget) {
            // move overlappedWidget up
            Point newOverlappedWidgetLocation = overlappedWidget.getLocation();
            int overlap = moveWidgetRect.intersection(overlappedWidgetRect).height;
            int nonOverlap = overlappedWidgetRect.height - overlap;
            newOverlappedWidgetLocation.y = moveWidget.getLocation().y - overlap;
            moveOverlappedWidget(overlappedWidget, moveWidget, newOverlappedWidgetLocation, overlappedWidgetRect);
            
            // set the snap to move moveWidget down
            Point fixedLocation = new Point(suggestedX, suggestedY);
            fixedLocation.y += nonOverlap;
            return fixedLocation;

            
        } else if (
        // 3. top of moveWidget     is above halfway of overlappedWidget
        //    halfway of moveWidget is below halfway of overlappedWidget
                topOfMoveWidget < halfwayOfOverlappedWidget &&
                halfwayOfMoveWidget > halfwayOfOverlappedWidget) {
            // move overlappedWidget down
            Point newOverlappedWidgetLocation = overlappedWidget.getLocation();
            int overlap = moveWidgetRect.intersection(overlappedWidgetRect).height;
            int nonOverlap = overlappedWidgetRect.height - overlap;
            newOverlappedWidgetLocation.y = 
                    (moveWidget.getLocation().y + moveWidgetRect.height) - 
                    nonOverlap;
            moveOverlappedWidget(overlappedWidget, moveWidget, newOverlappedWidgetLocation, overlappedWidgetRect);
            
            // set the snap to move moveWidget up
            Point fixedLocation = new Point(suggestedX, suggestedY);
            fixedLocation.y -= nonOverlap;
            return fixedLocation;

        } else if (
        // 4. top of moveWidget     is below halfway of overlappedWidget
                topOfMoveWidget >= halfwayOfOverlappedWidget) {
            // set the snap to keep moveWidget below
            Point fixedLocation = new Point(suggestedX, suggestedY);
            fixedLocation.y = overlappedWidget.getLocation().y + overlappedWidgetRect.height;
            return fixedLocation;
        }
        
        return null;
    }
    
    private static void moveOverlappedWidget(
            CasaNodeWidget overlappedWidget, 
            CasaNodeWidget moveWidget, 
            Point newOverlappedWidgetLocation,
            Rectangle overlappedWidgetRect)
    {
        Rectangle proposedOverlappedWidgetRect = new Rectangle(
                newOverlappedWidgetLocation, 
                new Dimension(overlappedWidgetRect.width, overlappedWidgetRect.height));
        if (getOverlappedWidget(
                overlappedWidget.getParentWidget(), 
                new Widget[] { moveWidget, overlappedWidget }, 
                proposedOverlappedWidgetRect) == null) {
            overlappedWidget.setPreferredLocation(newOverlappedWidgetLocation);
            overlappedWidget.persistLocation();
        }
    }
    
    
    
    // MOVE PROVIDER
    
    public void movementStarted(Widget widget) {
        mOriginalLocation = widget.getLocation();
    }
    
    public void movementFinished(Widget widget) {
        // Widget not moved, ignore.
        if (widget.getPreferredLocation().equals(mOriginalLocation)) {
            return;
        }
        
        if (mSnapPoint != null) {
            widget.setPreferredLocation(mSnapPoint);
            mSnapPoint = null;
        }
        
        // If we mvoe a middle/right region widget so that it overlaps another,
        // then we simply revert the move.
        if (widget instanceof CasaNodeWidgetEngine) {
            CasaNodeWidget moveWidget = (CasaNodeWidget) widget;
            CasaNodeWidget overlappedWidget = getOverlappedWidget(
                    moveWidget.getParentWidget(),
                    new Widget[] { moveWidget },
                    moveWidget.getEntireBounds());
            if (overlappedWidget != null) {
                widget.getScene().getSceneAnimator().animatePreferredLocation(
                        widget,
                        mOriginalLocation);
            }
        }
        
        // Save the widget location.
        if (widget instanceof CasaNodeWidget) {
            ((CasaNodeWidget) widget).persistLocation();
        }
        
        mOriginalLocation = null;
    }
    
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }
    
    public void setNewLocation(Widget widget, Point location) {
        if (widget.getPreferredLocation().equals(location)) {
            return;
        }
        
        // Ensure that once we have moved the widget, it is 
        // brought in front of the other widgets it may be moved over.
        widget.bringToFront();
        
        Point previousLocation = widget.getLocation();
        if (
                previousLocation != null &&
                previousLocation.x != location.x &&
                previousLocation.y != location.y)
        {
            // Ensure that the widget, if indeed it was moved, is always
            // in front - so that the user sees it on top during the move.
            widget.bringToFront();
        }
        
        CasaRegionWidget region = (CasaRegionWidget) widget.getParentWidget();
        Rectangle parentBounds = region.getBounds();
        Rectangle widgetBounds = widget.getBounds();
        
        widget.setPreferredLocation(location);
        
        boolean needsWidthStretch = false;
        boolean needsHeightStretch = false;
        if (parentBounds != null && widgetBounds != null) {
            if (mCanMoveHorizontal && mCanStretchHorizontal) {
                if (location.x + widgetBounds.width + RegionUtilities.HORIZONTAL_RIGHT_WIDGET_GAP > parentBounds.x + parentBounds.width) {
                    needsWidthStretch = true;
                }
            }
            if (location.y + widgetBounds.height + RegionUtilities.VERTICAL_EXPANSION_GAP > parentBounds.y + parentBounds.height) {
                needsHeightStretch = true;
            }
        }
        
        if        (needsWidthStretch  && !needsHeightStretch) {
            RegionUtilities.stretchSceneWidthOnly((CasaModelGraphScene) widget.getScene());
        } else if (needsHeightStretch && !needsWidthStretch) {
            RegionUtilities.stretchSceneHeightOnly((CasaModelGraphScene) widget.getScene());
        } else if (needsWidthStretch && needsHeightStretch) {
            RegionUtilities.stretchScene((CasaModelGraphScene) widget.getScene());
        }
    }
}
