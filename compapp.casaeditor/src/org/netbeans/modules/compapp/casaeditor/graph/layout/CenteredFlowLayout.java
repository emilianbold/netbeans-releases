/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
 * This layout does not attempt to find the shortest connection paths, rather,
 * it just allows for large blank spaces to be filled. If a need arises to
 * reduce connection paths (no such need currently identified), then this
 * layout can be replaced.
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
        CasaNodeWidget carryoverWidget = row.add(widget);
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
        private Comparator<CasaNodeWidget> mComparator = new XOrderComparator();
        
        
        public LayoutRow(int allowedWidth, int edgeSpacing, int verticalSpacing) {
            mAllowedWidth = allowedWidth;
            mEdgeSpacing = edgeSpacing;
            mVerticalSpacing = verticalSpacing;
            mUsedWidth = edgeSpacing * 2;
        }
        
        public boolean canAdd(Widget widget) {
            return
                    mElements.size() == 0 ||
                    mUsedWidth + widget.getBounds().width + X_BUFFER < mAllowedWidth;
        }
        
        public void positionWidgets_preserve(Map<CasaNodeWidget, Rectangle> widgetMap) {
            // First sort the widgets in order of x location
            Collections.sort(mElements, mComparator);
            
            for (CasaNodeWidget widget : mElements) {
                widgetMap.put(widget, new Rectangle(
                        widget.getLocation(),
                        widget.getBounds().getSize()));
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
                Rectangle rect = new Rectangle(new Point(xOffset, yOffset), widget.getBounds().getSize());
                xOffset += widget.getBounds().width + X_BUFFER_RIGHT + centeringGap;
                widgetMap.put(widget, rect);
                maxY = Math.max(maxY, yOffset + widget.getBounds().height);
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
                        widget.getBounds().getSize());
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
        
        public CasaNodeWidget add(CasaNodeWidget widget) {
            if (canAdd(widget)) {
                forceAdd(widget);
            } else {
                return widget;
            }
            return null;
        }
        
        private void forceAdd(CasaNodeWidget widget) {
            mUsedWidth += widget.getBounds().width + X_BUFFER;
            mElements.add(widget);
        }
        
        
        
        private static class XOrderComparator implements Comparator<CasaNodeWidget> {
            public int compare(CasaNodeWidget w1, CasaNodeWidget w2) {
                return w1.getLocation().x - w2.getLocation().x;
            }
        }
    }
}
