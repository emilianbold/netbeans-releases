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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.widget;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an extension of the ConnectionWidget. Primarily it is used with FreeRouter and optionally Free*Anchor.
 * It has a method for adding and removing control points on specific locations.
 *
 * @author Alex
 */
// TODO - later, logic of this class can be moved to the ConnectionWidget class directly
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class FreeConnectionWidget extends ConnectionWidget {

    private double createSensitivity=1.00, deleteSensitivity=5.00;

    /**
     * Creates a free connection widget.
     * @param scene the scene
     */
    public FreeConnectionWidget (Scene scene) {
        super (scene);
    }

    /**
     * Creates a free connection widget with a specified create/delete sensitivity.
     * @param scene the scene
     * @param createSensitivity the sensitivity for adding a control point
     * @param deleteSensitivity the sensitivity for removing a control point
     */
    public FreeConnectionWidget (Scene scene, double createSensitivity, double deleteSensitivity) {
        super (scene);
        this.createSensitivity=createSensitivity; 
        this.deleteSensitivity=deleteSensitivity;
    }

    /**
     * Adds or removes a control point on a specified location
     * @param localLocation the local location
     */
    public void addRemoveControlPoint (Point localLocation) {
        ArrayList<Point> list = new ArrayList<Point> (getControlPoints());
            if(!removeControlPoint(localLocation,list,deleteSensitivity)){
                Point exPoint=null;int index=0;
                for (Point elem : list) {
                    if(exPoint!=null){
                        Line2D l2d=new Line2D.Double(exPoint,elem);
                        if(l2d.ptLineDist(localLocation)<createSensitivity){
                            list.add(index,localLocation);
                            break;
                        }
                    }
                    exPoint=elem;index++;
                }
            }
            setControlPoints(list,false);
    }
    
    private boolean removeControlPoint(Point point, ArrayList<Point> list, double deleteSensitivity){
        for (Point elem : list) {
            if(elem.distance(point)<deleteSensitivity){
                list.remove(elem);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a sensitivity.
     * @param createSensitivity the sensitivity for adding a control point
     * @param deleteSensitivity the sensitivity for removing a control point
     */
    public void setSensitivity(double createSensitivity, double deleteSensitivity){
        this.createSensitivity=createSensitivity; 
        this.deleteSensitivity=deleteSensitivity;
    } 
   
}
