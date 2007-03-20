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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;

/**
 * Attempts to lay out widgets in a left-to-right fashion, in a snake-like
 * fashion downwards. The widgets in each row are centered.
 *
 * The layout can attempt to preserve widget locations, and if this flag is set,
 * a widget will only be moved if it is sitting on top of another widget 
 * (colliding with it). In this case, the entire row of the widget is re-layed out.
 *
 * @author Josh Sandusky
 */
public class CenteredFlowLayout {
    
    private static int X_BUFFER_LEFT  = 10;
    private static int X_BUFFER_RIGHT = 10;
    private static int X_BUFFER = X_BUFFER_LEFT + X_BUFFER_RIGHT;
    
    private List<LayoutRow> mRows = new ArrayList<LayoutRow>();
    private int mWidth;
    private int mCurrentRowIndex = 0;
    private int mEdgeSpacing;
    private int mVerticalSpacing;
    
    
    public CenteredFlowLayout(int width, int edgeSpacing, int verticalSpacing) {
        mWidth = width;
        mEdgeSpacing = edgeSpacing;
        mVerticalSpacing = verticalSpacing;
    }
    
    
    public void add(CasaNodeWidget widget) {
        LayoutRow row = null;
        if (mRows.size() < mCurrentRowIndex + 1) {
            row = new LayoutRow(mWidth, mEdgeSpacing, mVerticalSpacing);
            mRows.add(row);
        } else {
            row = mRows.get(mCurrentRowIndex);
        }
        CasaNodeWidget carryoverWidget = row.add(widget, false);
        if (carryoverWidget != null) {
            mCurrentRowIndex++;
            add(carryoverWidget);
        }
    }
    
    public void positionWidgets(
            int yOffset,
            Map<CasaNodeWidget, Rectangle> widgetMap,
            boolean isPreservingLocations) {
        for (LayoutRow row : mRows) {
            if (isPreservingLocations) {
                row.positionWidgets_preserve(widgetMap);
            } else {
                yOffset = row.positionWidgets_recalculate(yOffset, widgetMap);
            }
        }
    }
    
    
    
    private static class LayoutRow {
        
        private static final int NO_COLLISION = -1;
        private List<CasaNodeWidget> mElements = new ArrayList<CasaNodeWidget>();
        private int mUsedWidth;
        private int mAllowedWidth;
        private int mEdgeSpacing;
        private int mVerticalSpacing;
        private Comparator mComparator = new XOrderComparator();
        
        public LayoutRow(int allowedWidth, int edgeSpacing, int verticalSpacing) {
            mAllowedWidth = allowedWidth;
            mEdgeSpacing = edgeSpacing;
            mVerticalSpacing = verticalSpacing;
            mUsedWidth = edgeSpacing * 2;
        }
        
        public boolean canAdd(Widget widget) {
            return
                    mElements.size() == 0 ||
                    mUsedWidth + widget.getPreferredBounds().width + X_BUFFER < mAllowedWidth;
        }
        
        public void positionWidgets_preserve(Map<CasaNodeWidget, Rectangle> widgetMap) {
            // First sort the widgets in order of x location
            Collections.sort(mElements, mComparator);
            
            for (CasaNodeWidget widget : mElements) {
                widgetMap.put(widget, new Rectangle(
                        widget.getLocation(),
                        widget.getPreferredBounds().getSize()));
            }
            
            int rowCollisionYOffset = getRowCollisionYOffset(widgetMap);
            if (rowCollisionYOffset != NO_COLLISION) {
                calculateNewPositions(rowCollisionYOffset, widgetMap);
            }
        }
        
        public int positionWidgets_recalculate(int yOffset, Map<CasaNodeWidget, Rectangle> widgetMap) {
            // First sort the widgets in order of x location
            Collections.sort(mElements, mComparator);
            
            return calculateNewPositions(yOffset, widgetMap);
        }
        
        private int calculateNewPositions(int yOffset, Map<CasaNodeWidget, Rectangle> widgetMap) {
            int centeringGap = (int) (((float) (mAllowedWidth - mUsedWidth)) / (float) (mElements.size() * 2));
            int xOffset = mEdgeSpacing;
            int maxY = yOffset;
            for (CasaNodeWidget widget : mElements) {
                xOffset += centeringGap + X_BUFFER_LEFT;
                Rectangle rect = new Rectangle(new Point(xOffset, yOffset), widget.getPreferredBounds().getSize());
                xOffset += widget.getPreferredBounds().width + X_BUFFER_RIGHT + centeringGap;
                widgetMap.put(widget, rect);
                maxY = Math.max(maxY, yOffset + widget.getPreferredBounds().height);
            }
            maxY += mVerticalSpacing;
            return maxY;
        }
        
        private int getRowCollisionYOffset(Map<CasaNodeWidget, Rectangle> widgetMap) {
            boolean hasCollision = false;
            int rowCollisionYOffset = NO_COLLISION;
            int indexInRow = 0;
            for (CasaNodeWidget widget : mElements) {
                // collision detection
                Rectangle widgetRect = new Rectangle(
                        widget.getLocation(),
                        widget.getPreferredBounds().getSize());
                for (CasaNodeWidget iterWidget : widgetMap.keySet()) {
                    if (widget == iterWidget) {
                        continue;
                    }
                    if (
                            mElements.contains(iterWidget) &&
                            mElements.indexOf(iterWidget) > indexInRow) {
                        continue;
                    }
                    Rectangle iterRect = widgetMap.get(iterWidget);
                    if (widgetRect.intersects(iterRect)) {
                        hasCollision = true;
                    }
                    if (hasCollision) {
                        // collision
                        if (mElements.contains(iterWidget)) {
                            // a collision with a widget in the same row
                            rowCollisionYOffset = Math.max(
                                    rowCollisionYOffset,
                                    iterWidget.getLocation().y);
                        } else {
                            // a collision with a widget in a preceeding row
                            rowCollisionYOffset = Math.max(
                                    rowCollisionYOffset,
                                    iterRect.getLocation().y + iterRect.height + mVerticalSpacing);
                        }
                    }
                }
                indexInRow++;
            }
            return rowCollisionYOffset;
        }
        
        public CasaNodeWidget add(CasaNodeWidget widget, boolean swap) {
            if (canAdd(widget)) {
                forceAdd(widget);
            } else if (swap) {
                return swapAdd(widget);
            } else {
                return widget;
            }
            return null;
        }
        
        private void forceAdd(CasaNodeWidget widget) {
            mUsedWidth += widget.getPreferredBounds().width + X_BUFFER;
            mElements.add(widget);
        }
        
        private CasaNodeWidget swapAdd(CasaNodeWidget widget) {
            CasaNodeWidget lastElement = mElements.remove(mElements.size() - 1);
            mElements.add(widget);
            
            mUsedWidth = mEdgeSpacing * 2;
            for (Widget iterWidget : mElements) {
                mUsedWidth += iterWidget.getPreferredBounds().width + X_BUFFER;
            }
            
            return lastElement;
        }
        
        
        
        private static class XOrderComparator implements Comparator<CasaNodeWidget> {
            public int compare(CasaNodeWidget w1, CasaNodeWidget w2) {
                return w1.getLocation().x - w2.getLocation().x;
            }
        }
    }
}
