/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.modules.visual.layout.ConnectionWidgetLayout;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class FreeConnectionWidget extends ConnectionWidget {

    public FreeConnectionWidget (Scene scene) {
        super (scene);
    }

    public void addDeleteControlPoint (Point point,double createSensitivity, double deleteSensitivity) {
        ArrayList<Point> list = new ArrayList<Point> (getControlPoints());
            if(!removePoint(point,list,deleteSensitivity)){
                Point exPoint=null;int index=0;
                for (Point elem : list) {
                    if(exPoint!=null){
                        Line2D l2d=new Line2D.Double(exPoint,elem);
                        if(l2d.ptLineDist(point)<createSensitivity){
                            list.add(index,point);
                            break;
                        }
                    }
                    exPoint=elem;index++;
                }
            }
            setControlPoints(list,false);
    }
    
    private boolean removePoint(Point point, ArrayList<Point> list, double deleteSensitivity){
        for (Point elem : list) {
            if(elem.distance(point)<deleteSensitivity){
                list.remove(elem);
                return true;
            }
        }
        return false;
    }
    
    public Point getControlPoint (int count) {
        List<Point> controlPoints=getControlPoints();
        if (controlPoints.size () <= 0) return null;
        return new Point (controlPoints.get (count));
    }
    
    
   
}
