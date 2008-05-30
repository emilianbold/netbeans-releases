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
package org.netbeans.modules.visual.layout;

import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;

import java.awt.*;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public final class ConnectionWidgetLayout implements Layout {

    private ConnectionWidget connectionWidget;
    private HashMap<Widget, LayoutFactory.ConnectionWidgetLayoutAlignment> alignments;
    private HashMap<Widget, Float> percentagePlacements;
    private HashMap<Widget, Integer> distancePlacements;

    public ConnectionWidgetLayout (ConnectionWidget connectionWidget) {
        this.connectionWidget = connectionWidget;
    }

    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
        assert childWidget != null;
        assert alignment != null;

        if (alignments == null)
            alignments = new HashMap<Widget, LayoutFactory.ConnectionWidgetLayoutAlignment> ();
        alignments.put (childWidget, alignment);

        if (percentagePlacements == null)
            percentagePlacements = new HashMap<Widget, Float> ();
        percentagePlacements.put (childWidget, placementInPercentage);

        if (distancePlacements != null)
            distancePlacements.remove (childWidget);
    }

    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
        assert childWidget != null;
        assert alignment != null;

        if (alignments == null)
            alignments = new HashMap<Widget, LayoutFactory.ConnectionWidgetLayoutAlignment> ();
        alignments.put (childWidget, alignment);

        if (percentagePlacements != null)
            percentagePlacements.remove (childWidget);

        if (distancePlacements == null)
            distancePlacements = new HashMap<Widget, Integer> ();
        distancePlacements.put (childWidget, placementAtDistance);
    }

    public void removeConstraint (Widget childWidget) {
        assert childWidget != null;

        if (alignments != null)
            alignments.remove (childWidget);
        if (percentagePlacements != null)
            percentagePlacements.remove (childWidget);
        if (distancePlacements != null)
            distancePlacements.remove (childWidget);
    }

    public void layout (Widget widget) {
        assert connectionWidget == widget;

        connectionWidget.calculateRouting ();
        java.util.List<Point> controlPoints = connectionWidget.getControlPoints ();
        boolean empty = controlPoints == null  ||  controlPoints.size () <= 0;

        double totalDistance = 0.0;
        double[] distances = new double[empty ? 0 : controlPoints.size () - 1];
        for (int i = 0; i < distances.length; i ++)
            distances[i] = totalDistance += GeomUtil.distanceSq (controlPoints.get (i), controlPoints.get (i + 1));

        for (Widget child : widget.getChildren ()) {
            Float percentage = percentagePlacements != null ? percentagePlacements.get (child) : null;
            Integer distance = distancePlacements != null ? distancePlacements.get (child) : null;

            if (empty)
                layoutChildAt (child, new Point ());
            else if (percentage != null) {
                if (percentage <= 0.0)
                    layoutChildAt (child, connectionWidget.getFirstControlPoint ());
                else if (percentage >= 1.0)
                    layoutChildAt (child, connectionWidget.getLastControlPoint ());
                else
                    layoutChildAtDistance (distances, (int) (percentage * totalDistance), child, controlPoints);
            } else if (distance != null) {
                if (distance < 0)
                    layoutChildAtDistance (distances, distance + (int) totalDistance, child, controlPoints);
                else
                    layoutChildAtDistance (distances, distance, child, controlPoints);
            } else
                layoutChildAt (child, new Point ());
        }
    }

    public boolean requiresJustification (Widget widget) {
        return false;
    }

    public void justify (Widget widget) {
    }

    private void layoutChildAtDistance (double[] distances, int lineDistance, Widget child, java.util.List<Point> controlPoints) {
        int index = distances.length - 1;
        for (int i = 0; i < distances.length; i ++) {
            if (lineDistance < distances[i]) {
                index = i;
                break;
            }
        }

        double segmentStartDistance = index > 0 ? distances[index - 1] : 0;
        double segmentLength = distances[index] - segmentStartDistance;
        double segmentDistance = lineDistance - segmentStartDistance;

        if (segmentLength == 0.0) {
            layoutChildAt (child, controlPoints.get (index));
            return;
        }

        Point p1 = controlPoints.get (index);
        Point p2 = controlPoints.get (index + 1);

        double segmentFactor = segmentDistance / segmentLength;

        layoutChildAt (child, new Point ((int) (p1.x + (p2.x - p1.x) * segmentFactor), (int) (p1.y + (p2.y - p1.y) * segmentFactor)));
    }

    private void layoutChildAt (Widget childWidget, Point linePoint) {
        if (!childWidget.isVisible ()) {
            childWidget.resolveBounds (new Point (linePoint.x, linePoint.y), new Rectangle ());
            return;
        }
        Rectangle preferredBounds = childWidget.getPreferredBounds ();
        LayoutFactory.ConnectionWidgetLayoutAlignment alignment = null;
        if (alignments != null)
            alignment = alignments.get (childWidget);
        if (alignment == null)
            alignment = LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        Point referencePoint = getReferencePoint (alignment, preferredBounds);
        Point location = childWidget.getPreferredLocation ();
        if (location != null)
            referencePoint.translate (-location.x, -location.y);
        childWidget.resolveBounds (new Point (linePoint.x - referencePoint.x, linePoint.y - referencePoint.y), preferredBounds);
    }

    private Point getReferencePoint (LayoutFactory.ConnectionWidgetLayoutAlignment alignment, Rectangle rectangle) {
        LayoutFactory.ConnectionWidgetLayoutAlignment adjusted = adjustAlignment(alignment);
        
        switch (adjusted) {
            case BOTTOM_CENTER:
                return new Point (GeomUtil.centerX (rectangle), rectangle.y - 1);
            case BOTTOM_LEFT:
                return new Point (rectangle.x + rectangle.width, rectangle.y - 1);
            case BOTTOM_RIGHT:
                return new Point (rectangle.x - 1, rectangle.y - 1);
            case CENTER:
                return GeomUtil.center (rectangle);
            case CENTER_LEFT:
                return new Point (rectangle.x + rectangle.width, GeomUtil.centerY (rectangle));
            case CENTER_RIGHT:
                return new Point (rectangle.x - 1, GeomUtil.centerY (rectangle));
            case NONE:
                return new Point ();
            case TOP_CENTER:
                return new Point (GeomUtil.centerX (rectangle), rectangle.y + rectangle.height);
            case TOP_LEFT:
                return new Point (rectangle.x + rectangle.width, rectangle.y + rectangle.height);
            case TOP_RIGHT:
                return new Point (rectangle.x - 1, rectangle.y + rectangle.height);
            default:
                return new Point ();
        }
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment adjustAlignment(LayoutFactory.ConnectionWidgetLayoutAlignment alignment)
    {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = alignment;
        
        if(alignment == LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_SOURCE)
        {
            Point sourcePt = connectionWidget.getFirstControlPoint();
            Rectangle sourceBounds = getSourceBounds();

            retVal = calculateBestCenterAlignment(sourcePt, sourceBounds);
        }
        else if(alignment == LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_TARGET)
        {
            Point targetPt = connectionWidget.getLastControlPoint();
            Rectangle targetBounds = getTargetBounds();

            
            retVal = calculateBestCenterAlignment(targetPt, targetBounds);
        }
        else if(alignment == LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_SOURCE)
        {
            Point sourcePt = connectionWidget.getFirstControlPoint();
            Rectangle sourceBounds = getSourceBounds();
            
            retVal = calculateBestBottomAlignment(sourcePt, sourceBounds);
        }
        else if(alignment == LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_TARGET)
        {   
            Point targetPt = connectionWidget.getLastControlPoint();
            Rectangle targetBounds = getTargetBounds();

            retVal = calculateBestBottomAlignment(targetPt, targetBounds);
        }
        else if(alignment == LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_SOURCE)
        {
            Point sourcePt = connectionWidget.getFirstControlPoint();
            Rectangle sourceBounds = getSourceBounds();
            
            retVal = calculateBestTopAlignment(sourcePt, sourceBounds);
        }
        else if(alignment == LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_TARGET)
        {
            Point targetPt = connectionWidget.getLastControlPoint();
            Rectangle targetBounds = getTargetBounds();

            retVal = calculateBestTopAlignment(targetPt, targetBounds);
        }
        
        return retVal;
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment calculateBestCenterAlignment(Point point,
                                                                         Rectangle bounds)
    {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = 
                LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        
        if(bounds != null)
        {
            if(point.x <= bounds.x)
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_LEFT;
            }
            else if(point.x >= (bounds.x + bounds.width))
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_RIGHT;
            }
            else 
            if(point.y <= bounds.y)
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER;
            }
            else if(point.y >= (bounds.y + bounds.height))
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_CENTER;
            }
        }
        
        return retVal;
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment calculateBestBottomAlignment(Point point,
                                                                         Rectangle bounds)
    {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = 
                LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        
        if(point.x <= bounds.x)
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_LEFT;
        }
        else if(point.x >= (bounds.x + bounds.width))
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_RIGHT;
        }
        else if(point.y <= bounds.y)
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT;
        }
        else if(point.y >= (bounds.y + bounds.height))
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_LEFT;
        }
        
        return retVal;
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment calculateBestTopAlignment(Point point,
                                                                      Rectangle bounds)
    {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = 
                LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        
        if(point.x <= bounds.x)
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT;
        }
        else if(point.x >= (bounds.x + bounds.width))
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT;
        }
        else 
        if(point.y <= bounds.y)
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT;
        }
        else if(point.y >= (bounds.y + bounds.height))
        {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_RIGHT;
        }
        
        return retVal;
    }
    
    private Rectangle getSourceBounds()
    {
        Widget source = connectionWidget.getSourceAnchor().getRelatedWidget();
            
        if(source != null)
        {
            Point sourceLocation = source.getLocation();
            Rectangle clientArea = source.getClientArea();
            return new Rectangle(sourceLocation, clientArea.getSize());
        }
        
        return null;
    }

    private Rectangle getTargetBounds()
    {
        Widget target = connectionWidget.getTargetAnchor().getRelatedWidget();
                
        if(target != null)
        {
            Point targetLocation = target.getLocation();
            Rectangle targetArea = target.getClientArea();
            return new Rectangle(targetLocation, targetArea.getSize());
        }
        
        return null;
    }
}
