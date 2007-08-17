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
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author joelle
 */
public class GridGraphLayoutUtility {

    public GridGraphLayoutUtility() {
    }

    public static <N,E,P> void performLayout( GraphPinScene<N, E, P> graph, boolean checker, int horizontalGap, int verticalGap ){
        
        Collection<N> allNodes = graph.getNodes();
        HashSet<N> unresolvedNodes = new HashSet<N> (allNodes);
        
        final HashMap<N, Collection<N>> node2connected = new HashMap<N,Collection<N>> ();
        for (N node : unresolvedNodes) {
            HashSet<N> connected = new HashSet<N> ();
            
            /* in general, there will be less edges than pins. */
            for( E edge : graph.getEdges()){
                P pinSource = graph.getEdgeSource(edge);
                if( graph.getPinNode(pinSource).equals(node)){
                    connected.add(node);
                }
                P pinTarget = graph.getEdgeTarget(edge);
                if( graph.getPinNode(pinTarget).equals(node)){
                    connected.add(node);
                }
            }
            node2connected.put(node, connected);
        }
        
        LinkedList<N> queue = new LinkedList<N> ();
        HashMap<N, Point> node2grid = new HashMap<N, Point> ();
        Rectangle gridBounds = new Rectangle();
        
        for (;;) {
            N node = queue.isEmpty() ? findNodeWithMaxEdges(unresolvedNodes, node2connected) : queue.poll();
            if (node == null)
                break;
            unresolvedNodes.remove(node);
            Point center = node2grid.get(node);
            if (center == null) {
                center = findCenter(node2grid, checker);
                node2grid.put(node, center);
                gridBounds.add(center);
            }
            Point index = new Point();
            ArrayList<N> connected = new ArrayList<N> (node2connected.get(node));
            Collections.sort(connected, new Comparator<N>() {
                public int compare(N node1, N node2) {
                    return node2connected.get(node1).size() - node2connected.get(node2).size();
                }
            });
            
            for (N conn : connected) {
                if (unresolvedNodes.contains(conn))
                    queue.offer(conn);
                if (node2grid.containsKey(conn))
                    continue;
                Point grid = resolvePoint(node2grid, center, index, checker);
                node2grid.put(conn, grid);
                gridBounds.add(grid);
            }
        }
        int[] xAxis = new int[gridBounds.width + 1];
        int[] yAxis = new int[gridBounds.height + 1];
        
        for (N node : allNodes) {
            Widget widget = graph.findWidget(node);
            if (widget == null)
                continue;
            Rectangle bounds = widget.getBounds();
            if (bounds == null)
                continue;
            Point grid = node2grid.get(node);
            xAxis[grid.x - gridBounds.x] = Math.max(xAxis[grid.x - gridBounds.x], bounds.width);
            yAxis[grid.y - gridBounds.y] = Math.max(yAxis[grid.y - gridBounds.y], bounds.height);
        }
        
        int pos;
        
        pos = horizontalGap / 2;
        for (int i = 0; i < xAxis.length; i++) {
            int add = xAxis[i];
            xAxis[i] = pos;
            pos += add + horizontalGap;
        }
        pos = verticalGap / 2;
        for (int i = 0; i < yAxis.length; i++) {
            int add = yAxis[i];
            yAxis[i] = pos;
            pos += add + verticalGap;
        }
        
        for (N node : allNodes) {
            Widget widget = graph.findWidget(node);
            if (widget == null)
                continue;
            Rectangle bounds = widget.getBounds();
            if (bounds == null)
                continue;
            Point grid = node2grid.get(node);
            widget.setPreferredLocation(new Point(xAxis[grid.x - gridBounds.x] - bounds.x, yAxis[grid.y - gridBounds.y]));
            //            setResolvedNodeLocation(graph, node, new Point(xAxis[grid.x - gridBounds.x] - bounds.x, yAxis[grid.y - gridBounds.y]));
        }
        graph.validate();
        
    }
    
    public static <N,E,P> void performLayout( GraphPinScene<N, E, P> scene ){
        performLayout( scene, false);
    }
    
    public static <N,E,P> void  performLayout( GraphPinScene<N, E, P> scene, boolean checker) {
        performLayout(scene, checker, 64, 64);
    }
    
    private static <N> N findNodeWithMaxEdges(HashSet<N> unresolvedNodes, HashMap<N, Collection<N>> node2connected) {
        N bestNode = null;
        int bestCount = Integer.MIN_VALUE;
        for (N node : unresolvedNodes) {
            int i = node2connected.get(node).size();
            if (i > bestCount) {
                bestNode = node;
                bestCount = i;
            }
        }
        return bestNode;
    }
    
    private static <N> Point  findCenter(HashMap<N, Point> node2grid, boolean checker) {
        int add = checker ? 2 : 1;
        for (int x = 0; ; x += add) {
            Point point = new Point(x, 0);
            if (! isOccupied(node2grid, point))
                return point;
        }
    }
    
    private static <N> boolean isOccupied(HashMap<N, Point> node2grid, Point point) {
        for (Point p : node2grid.values()) {
            if (point.x == p.x  &&  point.y == p.y)
                return true;
        }
        return false;
    }
    
    
    private static <N> Point resolvePoint(HashMap<N, Point> node2grid, Point center, Point index, boolean checker) {
        for (;;) {
            int max = 8 * index.y;
            index.x ++;
            if (index.x >= max) {
                index.y ++;
                index.x -= max;
            }
            
            Point point = index2point(index);
            point.x += center.x;
            point.y += center.y;
            
            if (checker)
                if (((point.x + point.y) & 1) != 0)
                    continue;
            
            if (! isOccupied(node2grid, point))
                return point;
        }
    }
    private static Point index2point(Point index) {
        int indexPos = index.x;
        int indexLevel = index.y;
        if (indexPos < indexLevel) {
            return new Point(indexLevel, indexPos);
        } else if (indexPos < 3 * indexLevel) {
            return new Point(indexLevel - (indexPos - indexLevel), indexLevel);
        } else if (indexPos < 5 * indexLevel) {
            return new Point(- indexLevel, indexLevel - (indexPos - 3 * indexLevel));
        } else if (indexPos < 7 * indexLevel) {
            return new Point((indexPos - 5 * indexLevel) - indexLevel, - indexLevel);
        } else if (indexPos < 8 * indexLevel) {
            return new Point(indexLevel, (indexPos - 7 * indexLevel) - indexLevel);
        }
        throw new InternalError("Index: " + indexPos);
    }
    
}
