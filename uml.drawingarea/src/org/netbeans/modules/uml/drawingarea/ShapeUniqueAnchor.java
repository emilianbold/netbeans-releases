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
package org.netbeans.modules.uml.drawingarea;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import org.netbeans.modules.uml.drawingarea.view.WidgetShape;
import org.openide.util.Lookup;

/**
 * The RectangularUniqueAnchor is similiar to the RectangularAnchor.  The
 * problem with the RectanglurAnchor is that all connection widgets go
 * to the center of the node.  So if you have two nodes and two edges between
 * the nodes, the two edges will be placed on top of each other.  The
 * RectangularUniqueAnchor will make sure that two connection widgets will
 * not be connected to the same location on the widgets edge.
 * 
 * @author Trey Spiva
 */
// TODO - scene component location is not 100% attach to the bounding rectangle when the line goes far to the bottom-left-bottom direction
public final class ShapeUniqueAnchor extends Anchor
{

    private boolean includeBorders;
    private boolean requiresRecalculation = true;
    private HashMap<Entry, Result> results = new HashMap<Entry, Result>();

    public ShapeUniqueAnchor(Widget widget, boolean includeBorders)
    {
        super(widget);
        this.includeBorders = includeBorders;
    }

    /**
     * Notifies when an entry is registered
     * @param entry the registered entry
     */
    protected void notifyEntryAdded(Entry entry)
    {
        requiresRecalculation = true;
    }

    /**
     * Notifies when an entry is unregistered
     * @param entry the unregistered entry
     */
    protected void notifyEntryRemoved(Entry entry)
    {
        results.remove(entry);
        requiresRecalculation = true;
    }

    /**
     * Notifies when the anchor is going to be revalidated.
     * @since 2.8
     */
    protected void notifyRevalidate()
    {
        requiresRecalculation = true;
    }

    /**
     * Computes a result (position and direction) for a specific entry.
     * @param entry the entry
     * @return the calculated result
     */
    public Result compute(Entry entry)
    {
        recalculate();
        return results.get(entry);
    }
    public void recalculate()
    {
        if (! requiresRecalculation)
            return;
        
        HashMap<Entry, Float> topmap = new HashMap<Entry, Float> ();
        HashMap<Entry, Float> bottommap = new HashMap<Entry, Float> ();
        HashMap<Entry, Float> leftmap = new HashMap<Entry, Float> ();
        HashMap<Entry, Float> rightmap = new HashMap<Entry, Float> ();
        
        ArrayList <Entry> topList = new ArrayList <Entry>();
        ArrayList <Entry> bottomList = new ArrayList <Entry>();
        ArrayList <Entry> leftList = new ArrayList <Entry>();
        ArrayList <Entry> rightList = new ArrayList <Entry>();
        
        
        
        Widget widget = getRelatedWidget();
        
        Lookup lookup = widget.getLookup();
        
        Rectangle bounds = null;
        WidgetShape shape = null;
        if(lookup != null)
        {
            shape = lookup.lookup(WidgetShape.class);
            if(shape != null)
            {
                bounds = shape.getBounds();
            }
        }
        
        if(bounds == null)
        {
            bounds = widget.getBounds();
            if (!includeBorders)
            {
                Insets insets = widget.getBorder().getInsets();
                bounds.x += insets.left;
                bounds.y += insets.top;
                bounds.width -= insets.left + insets.right;
                bounds.height -= insets.top + insets.bottom;
            }
            bounds = widget.convertLocalToScene(bounds);
        }
        
        for(Entry entry : getEntries())
        {
            Point relatedLocation = getRelatedSceneLocation();
            Point oppositeLocation = getOppositeSceneLocation(entry);

            if (bounds.isEmpty() || relatedLocation.equals(oppositeLocation))
            {
//                return new Anchor.Result(relatedLocation, Anchor.DIRECTION_ANY);
            }
            float dx = oppositeLocation.x - relatedLocation.x;
            float dy = oppositeLocation.y - relatedLocation.y;

            float ddx = Math.abs(dx) / (float) bounds.width;
            float ddy = Math.abs(dy) / (float) bounds.height;

            //Anchor.Direction direction;

            // self link case, always route from right edge to bottom
            if (ddx == 0 && ddy == 0)
            {
                if (entry.isAttachedToConnectionSource())
                {
                    rightmap.put(entry, 0f);
                    rightList.add(entry);
                }
                else
                {
                    bottommap.put(entry, 0f);
                    bottomList.add(entry);
                }
            }
            else if (ddx >= ddy)
            {
                //direction = dx >= 0.0f ? Direction.RIGHT : Direction.LEFT;
                if(dx > 0.0f)
                {
                    rightmap.put(entry, dy / dx);
                    rightList.add(entry);
                }
                else
                {
                    leftmap.put(entry, - dy / dx);
                    leftList.add(entry);
                }
            }
            else
            {
                //direction = dy >= 0.0F ? Direction.BOTTOM : Direction.TOP;
                if(dy >= 0.0F)
                {
                    bottommap.put(entry,  dx / dy);
                    bottomList.add(entry);
                }
                else
                {
                    topmap.put(entry, -dx / dy);
                    topList.add(entry);
                }
            }
        }
        
        int edgeGap = 0;
        int len = rightList.size();
        Entry[] sortedEntries = toArray(rightList, rightmap);

        // Inside the loop I need to now calculate the new slop (based on the 
        // location of the entries new point), and then 
        int x = bounds.x + bounds.width + edgeGap;
        for (int a = 0; a < len; a ++)
        {
            Entry curEntry = sortedEntries[a];
            int y = bounds.y + (a + 1) * bounds.height / (len + 1);
            
            Point newPt = null;
            if(shape != null)
            {
                newPt = shape.getIntersection(getOppositeSceneLocation(curEntry), new Point(x, y));
            }
            else
            {
                newPt = new Point (x, y);
            }
            
            results.put (curEntry, new Result (newPt, Direction.RIGHT));
        }

        len = leftList.size();
        sortedEntries = toArray(leftList, leftmap);
        
        x = bounds.x - edgeGap;
        for (int a = 0; a < len; a ++)
        {
            Entry curEntry = sortedEntries[a];
            int y = bounds.y + (a + 1) * bounds.height / (len + 1);
            
            Point newPt = null;
            if(shape != null)
            {
                newPt = shape.getIntersection(getOppositeSceneLocation(curEntry), new Point(x, y));
            }
            else
            {
                newPt = new Point (x, y);
            }
            
            results.put (curEntry, new Result (newPt, Direction.LEFT));
        }
        
        len = topList.size();
        sortedEntries = toArray(topList,topmap);
        
        int y = bounds.y - edgeGap;
        for (int a = 0; a < len; a ++)
        {
            Entry curEntry = sortedEntries[a];
            x = bounds.x + (a + 1) * bounds.width / (len + 1);
            
            Point newPt = null;
            if(shape != null)
            {
                newPt = shape.getIntersection(getOppositeSceneLocation(curEntry), new Point(x, y));
            }
            else
            {
                newPt = new Point (x, y);
            }
            
            results.put (curEntry, new Result (newPt, Direction.TOP));
        }

        len = bottomList.size();
        sortedEntries = toArray(bottomList,bottommap);

        y = bounds.y + bounds.height + edgeGap;
        for (int a = 0; a < len; a ++)
        {
            Entry curEntry = sortedEntries[a];
            x = bounds.x + (a + 1) * bounds.width / (len + 1);
            
            Point newPt = null;
            if(shape != null)
            {
                newPt = shape.getIntersection(getOppositeSceneLocation(curEntry), new Point(x, y));
            }
            else
            {
                newPt = new Point (x, y);
            }
            
            results.put (curEntry, new Result (newPt, Direction.BOTTOM));
        }
     
        requiresRecalculation = false;
    }

    @Override
    public Point getOppositeSceneLocation(Entry entry)
    {
        Point retVal = super.getOppositeSceneLocation(entry);
        
        // If the connection widget has connection points we need to find the 
        // connection point that is closes to the related widget.
        //
        // There are always two, one for the source and target ends.
        ConnectionWidget connection = entry.getAttachedConnectionWidget();
        if((connection != null) && (connection.getControlPoints().size() > 2))
        {
            List < Point > points = connection.getControlPoints();
            if(entry.isAttachedToConnectionSource() == true)
            {
                // The source end starts from the start of the collection of points.
                retVal = points.get(1);
            }
            else
            {
                // The target end starts from the end of the collection of points.
                retVal = points.get(points.size() - 2);
            }
        }
        
        return retVal;
    }
    
    
    private Entry[] toArray(List < Entry > entries, final HashMap<Entry, Float> map)
    {
        Set<Entry> keys = map.keySet();
        
        Entry[] retVal = new Entry[entries.size()];
        if(entries.size() > 0)
        {
            entries.toArray(retVal);
        
            Arrays.sort(retVal, new Comparator<Entry>()
            {

                public int compare(Entry o1, Entry o2)
                {
                    float f = map.get(o1) - map.get(o2);
                    if (f > 0.0f)
                    {
                        return 1;
                    }
                    else if (f < 0.0f)
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            });
        }
        return retVal;
    }
}