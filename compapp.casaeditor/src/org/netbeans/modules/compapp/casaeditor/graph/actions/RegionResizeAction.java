/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
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
    
    private CasaModelGraphScene mScene;
    private Widget mLeftResizer;
    private Widget mMiddleResizer;

    
    public RegionResizeAction(
            CasaModelGraphScene scene, 
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
