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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;



public class ConnectionManager {
    

    
    private Map<Connection, FPoint> startPoints = new HashMap<Connection, FPoint>();
    private Map<Connection, FPoint> endPoints = new HashMap<Connection, FPoint>();
    
    
    public ConnectionManager() {
      
    }

    
   
    
    
    public void layoutConnections(Pattern startFrom) {
        calculatePoints(startFrom);
        applyPoints();
        
        startPoints.clear();
        endPoints.clear();
    }
    
    
    private void calculatePoints(Pattern pattern) {
        for (VisualElement element : pattern.getElements()) {
            processVisualElement(element);
        }
        
        if (pattern instanceof CompositePattern) {
            CompositePattern compositePattern = (CompositePattern) pattern;
            BorderElement border = compositePattern.getBorder();
            
            if (border != null) {
                processVisualElement(border);
            }
            
            for (Pattern nestedPattern : compositePattern.getNestedPatterns()) {
                calculatePoints(nestedPattern);
            }
        }
    }
    
    
    
    private Set<Connection> getConnectionsSet() {
        Set<Connection> connections1 = startPoints.keySet();
        Set<Connection> connections2 = endPoints.keySet();

        Set<Connection> connections = new HashSet<Connection>();
        Set<Connection> connectionsToRemove = new HashSet<Connection>();
        
        for (Connection c : connections1) {
            if (connections2.contains(c)) {
                connections.add(c);
            } else {
                connectionsToRemove.add(c);
            }
        }
        
        for (Connection c : connections2) {
            if (connections1.contains(c)) {
                connections.add(c);
            } else {
                connectionsToRemove.add(c);
            }
        }

        for (Connection c : connectionsToRemove) {
            c.remove();
        }
        
        return connections;        
    }
    
    
    private void applyPoints() {
        
        
        for (Connection connection : getConnectionsSet()) {
                    

            
            FPoint startPoint = startPoints.get(connection);
            FPoint endPoint = endPoints.get(connection);
            
            connection.setStartAndEndPoints(startPoint, endPoint);
        }
        

    }
    
    
    private void processVisualElement(VisualElement element) {
        SortedSet<Connection> tConnections = createConnectionSortedSet(element, 
                Direction.TOP);
        SortedSet<Connection> rConnections = createConnectionSortedSet(element, 
                Direction.RIGHT);
        SortedSet<Connection> bConnections = createConnectionSortedSet(element, 
                Direction.BOTTOM);
        SortedSet<Connection> lConnections = createConnectionSortedSet(element, 
                Direction.LEFT);
        
        // collect and sort connections
        
        for (Connection connection : element.getOutcomingConnections()) {
            switch (connection.getSourceDirection()) {
                case TOP:    tConnections.add(connection); break;
                case RIGHT:  rConnections.add(connection); break;
                case BOTTOM: bConnections.add(connection); break;
                default:     lConnections.add(connection);
            }
        }
        
        for (Connection connection : element.getIncomingConnections()) {
            switch (connection.getTargetDirection()) {
                case TOP:    tConnections.add(connection); break;
                case RIGHT:  rConnections.add(connection); break;
                case BOTTOM: bConnections.add(connection); break;
                default:     lConnections.add(connection);
            }
        }
        
        // calculate start and end points
        
        int tCount = tConnections.size();
        int rCount = rConnections.size();
        int bCount = bConnections.size();
        int lCount = lConnections.size();
        
        if (tCount > 0) {
            double step = element.getWidth() / (tCount + 1);
            double x = element.getX() + step;
            double y = element.getY();
            
            if (tCount == 1) {
                storeConnectionPoint(element, tConnections.first(), x, y);
            } else {
                for (Connection connection : tConnections) {
                    storeConnectionPoint(element, connection, x, y);
                    x += step;
                }
            }
        }
        
        if (rCount > 0) {
            double step = element.getHeight() / (rCount + 1);
            double x = element.getX() + element.getWidth();
            double y = element.getY() + step;

            if (rCount == 1) {
                storeConnectionPoint(element, rConnections.first(), x, y);
            } else {
                for (Connection connection : rConnections) {
                    storeConnectionPoint(element, connection, x, y);
                    y += step;
                }
            }
        }

        if (bCount > 0) {
            double step = element.getWidth() / (bCount + 1);
            double x = element.getX() + step;
            double y = element.getY() + element.getHeight();
            
            if (bCount == 1) {
                storeConnectionPoint(element, bConnections.first(), x, y);
            } else {
                for (Connection connection : bConnections) {
                    storeConnectionPoint(element, connection, x, y);
                    x += step;
                }
            }
        }
        
        if (lCount > 0) {
            double step = element.getHeight() / (lCount + 1);
            double x = element.getX();
            double y = element.getY() + step;
            
            if (lCount == 1) {
                storeConnectionPoint(element, lConnections.first(), x, y);
            } else {
                for (Connection connection : lConnections) {
                    storeConnectionPoint(element, connection, x, y);
                    y += step;
                }
            }
        }
    }
    
    
    private void storeConnectionPoint(VisualElement element, 
            Connection connection, double x, double y) 
    {
        FPoint point = new FPoint(x, y);
        if (connection.getSource() == element) { // start point
            startPoints.put(connection, point);
        } else { // end point
            endPoints.put(connection, point);
        }
    }
    

    
    private SortedSet<Connection> createConnectionSortedSet(
            VisualElement element, Direction direction) 
    {
        return new TreeSet<Connection>(new ConnectionComparator(element, 
                direction));
    }    

    
    public void reconnectAll(Pattern pattern) {
         reconnectDownFrom(pattern);
    }
    
    
    public void reconnectUpFrom(CompositePattern pattern) {
        for (CompositePattern p = pattern; p != null; p = p.getParent()) {
            p.reconnectElements();
        }
    }
    
    
    public void reconnectDownFrom(Pattern pattern) {
        
                
        pattern.reconnectElements();
        if (pattern instanceof CompositePattern)  {
            
            for (Pattern p : ((CompositePattern)pattern).getNestedPatterns()) {
                    reconnectDownFrom(p);
            }
        }
    }
    


    
    
//    private class MCComparator implements Comparator<MessageConnection> {
//        public int compare(MessageConnection c1, MessageConnection c2) {
//            FPoint p11 = startPoints.get(c1);
//            FPoint p12 = endPoints.get(c1);
//                    
//            FPoint p21 = startPoints.get(c2);
//            FPoint p22 = endPoints.get(c2);
//
//            float y1 = (p11.x <= p12.x) ? p11.y : p12.y;
//            float y2 = (p21.x <= p22.x) ? p21.y : p22.y;
//            
//            if (y1 < y2) { 
//                return -1;
//            } else if (y1 > y2) {
//                return 1;
//            }
//            
//            return c1.getUID() - c2.getUID();
//        }
//    }
    
    
    /**
     * Static utility methods
     */
    public static void connectVerticaly(
            VisualElement startElement, Connection startConnection,
            Pattern pattern,
            Connection endConnection, VisualElement endElement) 
    {
        connectVerticaly(startElement, startConnection,
                pattern.getFirstElement(), pattern.getLastElement(),
                endConnection, endElement);
    }

    
    public static void connectVerticaly(
            VisualElement startElement, Connection startConnection,
            VisualElement element,
            Connection endConnection, VisualElement endElement) 
    {
        connectVerticaly(startElement, startConnection,
                element, element,
                endConnection, endElement);
    }
            
    
    public static void connectVerticaly(
            VisualElement startElement, Connection startConnection,
            VisualElement firstElement, VisualElement lastElement, 
            Connection endConnection, VisualElement endElement) 
    {
        double x11 = startElement.getX();
        double x12 = x11 + startElement.getWidth();
        
        double x21 = endElement.getX();
        double x22 = x21 + endElement.getWidth();
        
        Direction d1;
        Direction d2;
        
        if (firstElement.getX() >= x12) {
            d1 = Direction.RIGHT;
        } else if (firstElement.getX() + firstElement.getWidth() <= x11) {
            d1 = Direction.LEFT;
        } else {
            d1 = Direction.BOTTOM;
        }
        
        if (lastElement.getX() >= x22) {
            d2 = Direction.RIGHT;
        } else if (lastElement.getX() + lastElement.getWidth() <= x21) {
            d2 = Direction.LEFT;
        } else {
            d2 = Direction.TOP;
        }
        
        startConnection.connect(startElement, d1, firstElement, Direction.TOP);
        endConnection.connect(lastElement, Direction.BOTTOM, endElement, d2);
    }
}
