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


package org.netbeans.modules.bpel.design.model.connections;

import java.util.Comparator;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;


public class ConnectionComparator implements Comparator<Connection> {

    private VisualElement visualElement;
    private Direction direction;


    public ConnectionComparator(VisualElement visualElement,
            Direction direction)
    {
        this.visualElement = visualElement;
        this.direction = direction;
    }


    public int compare(Connection c1, Connection c2) {
        VisualElement e1 = c1.getSource();
        VisualElement e2 = c2.getSource();
        Direction d1 = c1.getSourceDirection();
        Direction d2 = c2.getSourceDirection();

        if (e1 == visualElement) {
            e1 = c1.getTarget();
            d1 = c1.getTargetDirection();
        }

        if (e2 == visualElement) { 
            e2 = c2.getTarget();
            d2 = c2.getTargetDirection();
        }            

        int result;
        
        if (direction == Direction.TOP) {
            result = compareTop(e1, d1, e2, d2);
        } else if (direction == Direction.BOTTOM) {
            result = compareBottom(e1, d1, e2, d2);
        } else if (direction == Direction.LEFT) {
            result = compareLeft(e1, d1, e2, d2);
        } else {
            result = compareRight(e1, d1, e2, d2);
        }
        
        return (result == 0) ? (c1.getUID() - c2.getUID()) : result;
    }

    
    private int compareLeft(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.TOP) {
            n1 = 3;
            v1 = e1.getCenterX();
        } else if (d1 == Direction.BOTTOM) {
            n1 = 1;
            v1 = -e1.getCenterX();
        } else {
            n1 = 2;
            v1 = e1.getCenterY();
        }

        if (d2 == Direction.TOP) {
            n2 = 3;
            v2 = e2.getCenterX();
        } else if (d2 == Direction.BOTTOM) {
            n2 = 1;
            v2 = -e2.getCenterX();
        } else {
            n2 = 2;
            v2 = e2.getCenterY();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);
    }
    

    private int compareRight(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.TOP) {
            n1 = 3;
            v1 = -e1.getCenterX();
        } else if (d1 == Direction.BOTTOM) {
            n1 = 1;
            v1 = e1.getCenterX();
        } else {
            n1 = 2;
            v1 = e1.getCenterY();
        }

        if (d2 == Direction.TOP) {
            n2 = 3;
            v2 = -e2.getCenterX();
        } else if (d2 == Direction.BOTTOM) {
            n2 = 1;
            v2 = e2.getCenterX();
        } else {
            n2 = 2;
            v2 = e2.getCenterY();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);        
    }

    
    private int compareTop(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.RIGHT) {
            n1 = 1;
            v1 = e1.getCenterY();
        } else if (d1 == Direction.LEFT) {
            n1 = 3;
            v1 = -e1.getCenterY();
        } else {
            n1 = 2;
            v1 = e1.getCenterX();
        }

        if (d2 == Direction.RIGHT) {
            n2 = 1;
            v2 = e2.getCenterY();
        } else if (d2 == Direction.LEFT) {
            n2 = 3;
            v2 = -e2.getCenterY();
        } else {
            n2 = 2;
            v2 = e2.getCenterX();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);        
    }

    
    private int compareBottom(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.RIGHT) {
            n1 = 1;
            v1 = e1.getCenterY();
        } else if (d1 == Direction.LEFT) {
            n1 = 3;
            v1 = -e1.getCenterY();
        } else {
            n1 = 2;
            v1 = e1.getCenterX();
        }

        if (d2 == Direction.RIGHT) {
            n2 = 1;
            v2 = e2.getCenterY();
        } else if (d2 == Direction.LEFT) {
            n2 = 3;
            v2 = -e2.getCenterY();
        } else {
            n2 = 2;
            v2 = e2.getCenterX();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);        
    }
}
