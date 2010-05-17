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

package org.netbeans.modules.uml.widgets;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author treyspiva
 */
public class PolygonLayout implements Layout
{
    
    public PolygonLayout()
    {
    }

    public void layout(Widget widget)
    {
        Dimension total = new Dimension();
        for (Widget child : widget.getChildren())
        {
            if (! child.isVisible())
                continue;
            Dimension size = child.getPreferredBounds().getSize();
            if (size.width > total.width)
                total.width = size.width;
            if (size.height > total.height)
                total.height = size.height;
        }
        for (Widget child : widget.getChildren())
        {
            Point location = child.getPreferredBounds().getLocation();
            Point testLoc = new Point(-1, -20);
            child.resolveBounds(new Point(1, 20), new Rectangle(testLoc, total));
        }
    }

    public boolean requiresJustification(Widget widget)
    {
        return true;
    }
    
    //
    // Algorithm to use:
    // 1) Sort the children by X position
    // 2) Sort the children by Y position
    // 3) Collapse the polygon points by the relational points.
    // 4) Iterate over the children in X ascending order.  
    //    4.1) Adjust the polygon points based on the childs weight
    //    4.2) collect childrens X and width values
    // 5) Iterate over the children in Y ascending order. 
    //    5.1) Adjust the polygon points based on the childs weight
    //    5.2) collect childrens y and heigth values
    // 6) resolve the childrens bounds based on the collection data.
    // 7) Update the polygons points, based on the adjusted point data.
    //
    public void justify(Widget widget)
    {
        if(widget instanceof PolygonWidget)
        {
            
            PolygonWidget polygonWidget = (PolygonWidget)widget;
            
            Rectangle bounds = widget.getClientArea();
            int widgetWidth = (int)bounds.getWidth() - 1;
            int widgetHeight = (int)bounds.getHeight() - 1;
            
            if(polygonWidget.getPoints() == null)
            {
                return;
            }
            
            CollapsedPolygon collapsePolygon = new CollapsedPolygon(polygonWidget.getPoints(), 
                                                                    widgetWidth, 
                                                                    widgetHeight);
            
            // Iterate thru the children to adjust the polygon points
            for (Widget child : widget.getChildren())
            {
                PolygonConstraints constraint = (PolygonConstraints)widget.getChildConstraint(child);
                if ((child.isVisible()) && (constraint != null))
                {
                    double left = collapsePolygon.getX(constraint.getLeft());
                    double right = collapsePolygon.getX(constraint.getRight());

                    Rectangle childBounds = child.getPreferredBounds();
                    if(constraint.getWidthWeight() == PolygonConstraints.VertexWeight.PREFERRED)
                    {
                        if(childBounds != null)
                        {
                            right = left + childBounds.width;
                            collapsePolygon.changeX(constraint.getRight(), right);
                        }
                    }

                    double top = collapsePolygon.getY(constraint.getTop());
                    double bottom = collapsePolygon.getY(constraint.getBottom());
                    if(constraint.getHeightWeight() == PolygonConstraints.VertexWeight.PREFERRED)
                    {
                        if(childBounds != null)
                        {
                            bottom = top + childBounds.height;
                            collapsePolygon.changeY(constraint.getBottom(), bottom);
                        }
                    }
                }
            }
            
            // Now iterate over the child to adjust the children
            for (Widget child : widget.getChildren())
            {
                PolygonConstraints constraint = (PolygonConstraints)widget.getChildConstraint(child);
                
                if ((child.isVisible()) && (constraint != null))
                {
                    double left = collapsePolygon.getX(constraint.getLeft());
                    double right = collapsePolygon.getX(constraint.getRight());
                    double top = collapsePolygon.getY(constraint.getTop());
                    double bottom = collapsePolygon.getY(constraint.getBottom());
                    
                    Point location = child.getPreferredBounds().getLocation();
                    Dimension size = new Dimension((int)(right - left), (int)(bottom - top));
                    
                    if(location.x < 0)
                    {
                        left += Math.abs(location.x);
                    }
                    
                    if(location.y < 0)
                    {
                        top += Math.abs(location.y);
                    }
                    
                    child.resolveBounds(new Point((int)left, (int)top), new Rectangle(location, size));
                }
                else
                {
                    Rectangle clientArea = widget.getClientArea();
                    child.resolveBounds(clientArea.getLocation(), new Rectangle());
                }
            }
            
            polygonWidget.setPolygon(collapsePolygon.createPolygon());
        }

    }
    
    public class CollapsedPolygon
    {
        private ArrayList < Double > yValues = new ArrayList < Double >();
        private ArrayList < Double > xValues = new ArrayList < Double >();
        
        private ArrayList < Integer > xIndexes = new ArrayList <Integer>();
        private ArrayList < Integer > yIndexes = new ArrayList <Integer>();
        
        public CollapsedPolygon(Point2D[] relPoints, int width, int height)
        {
            for(Point2D pt : relPoints)
            {
                double x = pt.getX() * width;
                double y = pt.getY() * height;
                
                if(xValues.contains(x) == true)
                {
                    xIndexes.add(xValues.indexOf(x));
                }
                else
                {
                    xValues.add(x);
                    xIndexes.add(xValues.size() - 1);
                }
                
                if(yValues.contains(y) == true)
                {
                    yIndexes.add(yValues.indexOf(y));
                }
                else
                {
                    yValues.add(y);
                    yIndexes.add(yValues.size() - 1);
                }
            }
        }
        
        public Polygon createPolygon()
        {
            Polygon retVal = new Polygon();
            
            for(int index = 0; index < yIndexes.size(); index++)
            {
                int xIndex = xIndexes.get(index);
                int yIndex = yIndexes.get(index);
                retVal.addPoint(xValues.get(xIndex).intValue(), 
                               yValues.get(yIndex).intValue());
            }
            
            return retVal;
        }
        
        public void changeX(int index, double newValue)
        {
            int xIndex = xIndexes.get(index);
            xValues.set(xIndex, newValue);
        }
        
        public void changeY(int index, double newValue)
        {
            int yIndex = yIndexes.get(index);
            yValues.set(yIndex, newValue);
        }
        
        public double getX(int index)
        {
            int xIndex = xIndexes.get(index);
            return xValues.get(xIndex);
        }
        
        public double getY(int index)
        {
            int yIndex = yIndexes.get(index);
            return yValues.get(yIndex);
        }
    }
    
    
}
