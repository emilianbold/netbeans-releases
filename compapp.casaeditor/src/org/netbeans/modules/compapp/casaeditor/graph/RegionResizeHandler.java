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
* RegionResizeHandler.java
*
* Created on November 10, 2006, 4:15 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;

/**
 *
 * @author Josh Sandusky
 */
public class RegionResizeHandler implements MoveStrategy, MoveProvider {
    
    private CasaModelGraphScene mScene;
    private Widget mLeftResizer;
    private Widget mMiddleResizer;
    private CasaRegionWidget mBindingRegion;
    private CasaRegionWidget mEngineRegion;
    private CasaRegionWidget mExternalRegion;
    private int mOriginalXPosition;
    private Map<CasaNodeWidget, Point> mOriginalChildWidgetLocationMap;
    
    
    public RegionResizeHandler(
            CasaModelGraphScene scene,
            Widget leftResizer,
            Widget middleResizer)
    {
        mScene = scene;
        mLeftResizer = leftResizer;
        mMiddleResizer = middleResizer;
    }
    
    
    /*
     *
     * MOVE STRATEGY - Performs bounds checking on the resizer movements.
     *                 As the user drags a resizer to determine how large
     *                 a region should be, the location of the resizer needs
     *                 to be restricted in order to prevent:
     *                 - regions with zero size
     *                 - regions that chop off their own child widgets
     */
    
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        if        (widget == mLeftResizer) {
            checkBoundary(suggestedLocation, null, mBindingRegion);
        } else if (widget == mMiddleResizer) {
            checkBoundary(suggestedLocation, mLeftResizer, mEngineRegion);
        }
        suggestedLocation.y = originalLocation.y;
        return suggestedLocation;
    }
    
    private void checkBoundary(Point suggestedLocation, Widget minimumEdgeResizer, CasaRegionWidget region) {
        int boundary = RegionUtilities.MIN_REGION_WIDTH;
        int minimumResizerEdge = 0;
        if (minimumEdgeResizer != null) {
            minimumResizerEdge = minimumEdgeResizer.getLocation().x + RegionUtilities.RESIZER_WIDTH;
            boundary += minimumResizerEdge;
        }
        // When we move the resizer to the left, child nodes that the resizer
        // encounters will also be moved to the left. Thus, the minimum width
        // of the region is the largest width among our child nodes.
        for (Widget child : region.getChildren()) {
            if (child instanceof CasaNodeWidget) {
                if (minimumResizerEdge > 0) {
                    boundary = Math.max(boundary, minimumResizerEdge + child.getBounds().width);
                } else {
                    boundary = Math.max(boundary, child.getBounds().width);
                }
            }
        }
        if (suggestedLocation.x < boundary) {
            suggestedLocation.x = boundary;
        }
    }
    
    
    /*
     *
     * MOVE PROVIDER - Performs the actual resizing of the region.
     *                 When a region is resized, some of the other regions 
     *                 may need to be translated so that the boundaries of
     *                 the regions meet. Region resizers must exist at
     *                 region boundaries, so they may need to be translated
     *                 (moved in the x direction) as well.
     */

    public void movementStarted(Widget widget) {
        mBindingRegion  = RegionUtilities.getRegionWidget(mScene, CasaRegion.Name.WSDL_ENDPOINTS);
        mEngineRegion   = RegionUtilities.getRegionWidget(mScene, CasaRegion.Name.JBI_MODULES);
        mExternalRegion = RegionUtilities.getRegionWidget(mScene, CasaRegion.Name.EXTERNAL_MODULES);
        
        // reset our saved list of child locations
        mOriginalChildWidgetLocationMap = null;
    }
    
    public void movementFinished(Widget widget) {
        // free memory
        mOriginalChildWidgetLocationMap = null;
    }
    
    private void moveRegions(Widget widget, int xTranslation) {
        if        (widget == mLeftResizer) {
            // Resize left region (and translate all others).
            shiftWidth(mBindingRegion, xTranslation);
            shiftX(mEngineRegion,      xTranslation);
            shiftX(mMiddleResizer,     xTranslation);
            shiftX(mExternalRegion,    xTranslation);
            rightAlignRegion(mBindingRegion);
            RegionUtilities.stretchSceneWidthOnly(mScene);

        } else if (widget == mMiddleResizer) {
            // Resize middle region (and translate the right region).
            shiftWidth(mEngineRegion, xTranslation);
            shiftX(mExternalRegion,   xTranslation);
            RegionUtilities.stretchSceneWidthOnly(mScene);
        }
    }
    
    private void shiftWidth(Widget widget, int shift) {
        widget.setPreferredBounds(new Rectangle(
                0, // This is a resize, not a move, so do not change (x, y).
                0,
                widget.getBounds().width + shift,
                widget.getBounds().height));
        ((CasaRegionWidget) widget).persistWidth();
    }
    
    private void shiftX(Widget widget, int shift) {
        widget.setPreferredLocation(new Point(
                widget.getLocation().x + shift,
                widget.getLocation().y));
    }
    
    private void rightAlignRegion(CasaRegionWidget region) {
        // Align child nodes along the right edge of the region.
        Point localRegionBoundary = region.convertSceneToLocal(
                new Point(region.getPreferredBounds().width, 0));
        for (Widget child : region.getChildren()) {
            if (child instanceof CasaNodeWidget) {
                Point newChildLocation = new Point(
                        localRegionBoundary.x - child.getBounds().width,
                        child.getLocation().y);
                child.setPreferredLocation(newChildLocation);
                child.resolveBounds(child.getPreferredLocation(), child.getBounds());
            }
        }
    }
    
    public Point getOriginalLocation(Widget widget) {
        mOriginalXPosition = widget.getPreferredLocation().x;
        return widget.getPreferredLocation();
    }
    
    public void setNewLocation(Widget widget, Point resizerLocation) {
        int currentX = widget.getPreferredLocation().x;
        widget.setPreferredLocation(resizerLocation);
        if        (widget == mLeftResizer) {
            moveChildNodesLeft(widget, mBindingRegion);
        } else if (widget == mMiddleResizer) {
            moveChildNodesLeft(widget, mEngineRegion);
        }
        moveRegions(widget, widget.getPreferredLocation().x - currentX);
    }
    
    private void moveChildNodesLeft(Widget resizerWidget, CasaRegionWidget region) {
        // As the resizer is moved to the left, it squeezes the region
        // to a smaller width. The resizer may run into widgets as the region
        // is being squished, in which case we move the widgets to the left
        // so they still fit within the region. However, we also save the
        // original locations of the widgets so that if we move the resizer
        // to the right (thereby stretching the region), we can restore the
        // widget locations - if at least up to the resizer (right edge of the region).
        
        if (mOriginalChildWidgetLocationMap == null) {
            // save the list of child widget locations so that we can
            // potentially restore them if needed
            mOriginalChildWidgetLocationMap = new HashMap<CasaNodeWidget, Point>();
            for (Widget child : region.getChildren()) {
                if (child instanceof CasaNodeWidget) {
                    mOriginalChildWidgetLocationMap.put(
                            (CasaNodeWidget) child, 
                            child.getLocation());
                }
            }
        }
        
        Rectangle resizerRect = new Rectangle(
                region.convertSceneToLocal(resizerWidget.getPreferredLocation()),
                resizerWidget.getBounds().getSize());
        
        for (CasaNodeWidget child : mOriginalChildWidgetLocationMap.keySet()) {
            Rectangle childRect = new Rectangle(
                    child.getLocation(), 
                    child.getBounds().getSize());
            childRect.width += RegionUtilities.HORIZONTAL_EXPANSION_GAP;
            Point originalChildLocation = mOriginalChildWidgetLocationMap.get(child);

            Point childEdgeLocation = new Point(
                    resizerRect.x - child.getBounds().width, 
                    child.getLocation().y);
            childEdgeLocation.x -= RegionUtilities.HORIZONTAL_EXPANSION_GAP;
            childEdgeLocation.x = childEdgeLocation.x < 0 ? 0 : childEdgeLocation.x;
            
            if (childRect.intersects(resizerRect)) {
                // Move child left.
                child.setPreferredLocation(childEdgeLocation);
                ((CasaNodeWidget) child).persistLocation();
                
            } else if (originalChildLocation.x + childRect.width > resizerRect.x) {
                // Move child back towards its original location - up to the resizer location.
                child.setPreferredLocation(childEdgeLocation);
                ((CasaNodeWidget) child).persistLocation();
            }
        }
    }
}
