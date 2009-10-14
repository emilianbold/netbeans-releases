/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.graph;

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
    
    private Point mOriginalLocation;
    private boolean mIsWidgetFronted;
    
    
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
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        if (scene.findObject(widget) == null) {
            return null;
        }
        
        if (originalLocation.equals(suggestedLocation)) {
            return originalLocation;
        }
        
        assert widget instanceof CasaNodeWidget;
        CasaNodeWidget moveWidget = (CasaNodeWidget) widget;
        Point retPoint = new Point();
        retPoint.x = originalLocation.x;
        retPoint.y = originalLocation.y;
        CasaRegionWidget region = (CasaRegionWidget) moveWidget.getParentWidget();
        Rectangle parentBounds = region.getBounds();
        Rectangle widgetBounds = moveWidget.getBounds();
        
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
    
    
    
    // MOVE PROVIDER
    
    public void movementStarted(Widget widget) {
        mOriginalLocation = widget.getLocation();
        mIsWidgetFronted = false;
    }
    
    public void movementFinished(Widget widget) {
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        if (scene.findObject(widget) == null) {
            return;
        }
        
        Point tmpOriginalLocation = mOriginalLocation;
        mOriginalLocation = null;
        mIsWidgetFronted = false;
        
        // Widget not moved, ignore.
        Point location = widget.getPreferredLocation();
        if (location.equals(tmpOriginalLocation)) {
            return;
        }
        scene.persistLocation((CasaNodeWidget) widget, location);
        
        CasaNodeWidget moveWidget = (CasaNodeWidget) widget;
        CasaNodeWidget overlappedWidget = getOverlappedWidget(
                moveWidget.getParentWidget(),
                new Widget[] { moveWidget },
                moveWidget.getEntireBounds());
        if (overlappedWidget != null) {
            if (widget instanceof CasaNodeWidgetEngine) {
                // If we move a middle/right region widget so that it overlaps another,
                // then we simply revert the move.
                widget.setPreferredLocation(tmpOriginalLocation);
            } else if (widget instanceof CasaNodeWidgetBinding) {
                // If we move a left region widget so that it overlaps another,
                // then we invoke the region layout to ensure space.
                scene.progressiveRegionLayout(scene.getBindingRegion(), true);
            }
        }
    }
    
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }
    
    public void setNewLocation(Widget widget, Point location) {
        if (location == null) {
            return;
        }
        
        if (widget.getPreferredLocation().equals(location)) {
            return;
        }
        
        if (
                !mIsWidgetFronted &&
                mOriginalLocation != null &&
                !mOriginalLocation.equals(location))
        {
            // Ensure that the widget, if indeed it was moved, is brought to
            // the front - so that the user sees it on top during the move.
            mIsWidgetFronted = true;
            // bringToFront is rather slow during an actual mouse move, so
            // we avoid calling this more than we need to.
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
