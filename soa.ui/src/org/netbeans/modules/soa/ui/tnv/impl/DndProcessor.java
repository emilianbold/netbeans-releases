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

package org.netbeans.modules.soa.ui.tnv.impl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Provide processing of DnD operation for single axis. 
 *
 * The following abbreviation are used:
 * MV - Main View
 * TNV - Thumbnail view
 * VA - Visible Area
 *
 * There are 2 coordinate systems: MV and TNV.
 *
 * @author supernikita
 */
public class DndProcessor {
    
    
    private DndStartPositionState dndStart;
    
    // Indicates if the current position of the mouse is inside of
    // the TNV region at the start of DnD operation
    private transient boolean isInsideOfDndStartRegion = false;
    
    /** Creates a new instance of DndProcessor */
    public DndProcessor() {
    }
    
    public void startDnd(
            int eventPoint, // Current DnD event point)
            ThumbnailPositionState tnState, // corrent state
            double zoom) {
        //
        double mvPoint = eventPoint / zoom + tnState.tnvPosition;
        //
        double tnvMinEdge = tnState.tnvPosition + tnState.vaHalfSize;
        double tnvMaxEdge = tnState.tnvPosition + tnState.tnvSize - tnState.vaHalfSize;
        //
        isInsideOfDndStartRegion = (mvPoint > tnvMinEdge) && (mvPoint < tnvMaxEdge);
        //
        dndStart = new DndStartPositionState(
                eventPoint, mvPoint, tnState.tnvPosition, tnvMinEdge, tnvMaxEdge);
    }
    
    public void stopDnd() {
        dndStart = null;
    }
    
    /**
     * Track the mouse drag event to change the position of visible part.
     */
    public ThumbnailPositionState processDnD(
            int eventPoint, // Current DnD event point
            ThumbnailPositionState tnState, // corrent state
            double zoom) { // Current position of the TNV region (minimum)
        //
        if (dndStart == null) {
            // DnD isn't started
            return null;
        }
        //
        // Convert coordinates from TNV to the main view.
        double newCenterPoint;
        if (isInsideOfDndStartRegion) {
            newCenterPoint = eventPoint / zoom + tnState.tnvPosition;
        } else {
            newCenterPoint = dndStart.mvPoint + (eventPoint - dndStart.eventPoint) / zoom;
        }
        //
        boolean newIsInsideOfDndStartRegion =
                (newCenterPoint > dndStart.tnvMinEdge) &&
                (newCenterPoint < dndStart.tnvMaxEdge);
        
        if (newIsInsideOfDndStartRegion != isInsideOfDndStartRegion) {
            isInsideOfDndStartRegion = newIsInsideOfDndStartRegion;
            // System.out.println("isInsideOfDndStartRegion = " + isInsideOfDndStartRegion);
            //
            // Recalculate center point again
            if (isInsideOfDndStartRegion) {
                newCenterPoint = eventPoint / zoom + tnState.tnvPosition;
            } else {
                double tnvShift = tnState.tnvPosition - dndStart.tnvPosition;
                newCenterPoint = dndStart.mvPoint +
                        (eventPoint - dndStart.eventPoint) / zoom +
                        tnvShift;
            }
        }
        //
        // ==================================================================
        // Correct center position according to the main view size
        //
        // Determine edges of the MA area and correct the new location of the center
        // such way so the main visible area remains inside of the full area.
        double minMvEdge = tnState.vaHalfSize;
        double maxMvEdge = tnState.mvSize - tnState.vaHalfSize;
        //
        // Indicates if the X coordinate of the center is inside of edges of the full area.
        boolean isInsideOfMvRainge = true;
        //
        if (newCenterPoint < minMvEdge) {
            newCenterPoint = minMvEdge;
            isInsideOfMvRainge = false;
        }
        if (newCenterPoint > maxMvEdge) {
            newCenterPoint = maxMvEdge;
            isInsideOfMvRainge = false;
        }
        //
        // ==================================================================
        // Move the visible area to the new position.
        //
        tnState.vaPosition = newCenterPoint - tnState.vaHalfSize;
        //
        // ==================================================================
        //
        // Indicates if the current center point is out of the TNV visible rectangle edges
        // is taken at the moment of DnD start.
        boolean isOutOfEdge = false;
        //
        // If the edges of the full area aren't reached, then the TNV visible area
        // can be possibly corrected.
        if (isInsideOfMvRainge) {
            //
            boolean stickToMinEdge;
            boolean stickToMaxEdge;
            //
            // Determine the edges of the TNV visible area and correct the position of
            // the TNV visible area such way so the main visible area remains inside of
            // the TNV visible area.
            //
            stickToMinEdge = newCenterPoint < dndStart.tnvMinEdge;
            if (stickToMinEdge) {
                isOutOfEdge = true;
            }
            //
            stickToMaxEdge = newCenterPoint > dndStart.tnvMaxEdge;
            if (stickToMaxEdge) {
                isOutOfEdge = true;
            }
            //
            if (stickToMinEdge && stickToMaxEdge) {
                // The dnd shouldn't come to resizing of the TNV
                assert false : "invalid case";
                tnState.tnvPosition = tnState.vaPosition;
                tnState.tnvSize = tnState.vaHalfSize * 2d;
            } else if (stickToMinEdge) {
                tnState.tnvPosition = tnState.vaPosition;
            } else if (stickToMaxEdge) {
                tnState.tnvPosition = tnState.vaPosition +
                        tnState.vaHalfSize * 2d - tnState.tnvSize;
            } else {
                // Nothing to do because there isn't a horizontal stick
            }
        }
        return tnState;
    }
    
    
}
