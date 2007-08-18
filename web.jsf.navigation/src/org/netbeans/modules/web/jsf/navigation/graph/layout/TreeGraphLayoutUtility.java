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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author joelle
 */
public class TreeGraphLayoutUtility <N,E,P> {
    
        private final GraphPinScene<N, E, P> scene;
        private final int originX;
        private final int originY;
        private final int verticalGap;
        private final int horizontalGap;
        private final boolean vertical;
    
    /**
     * Creates a graph-oriented tree layout.
     * @param scene the GraphScene where the layout is used
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     */
        private TreeGraphLayoutUtility (GraphPinScene<N, E, P> scene, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
            this.scene = scene;
            this.originX = originX;
            this.originY = originY;
            this.verticalGap = verticalGap;
            this.horizontalGap = horizontalGap;
            this.vertical = vertical;
        }
    
    public final static <N,E,P> void performLayout( GraphPinScene <N, E, P> scene) {
        performLayout( scene, 100, 100, 50, 50, true);
    }
    
    public final static <N,E,P> void performLayout( GraphPinScene<N, E, P> graph, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical){
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
        
        N root = findNodeWithMaxEdges(unresolvedNodes, node2connected);
        TreeGraphLayoutUtility<N,E,P> utility = new TreeGraphLayoutUtility<N,E,P>(graph,100, 100, 50, 50, true );
        utility.layout(root);
    }
    
    /**
     * Invokes the layout for a specified root node.
     * @param rootNode the root node
     */
    public final void layout(N rootNode) {
        if (rootNode == null) {
            return;
        }
        Collection<N> allNodes = scene.getNodes();
        ArrayList<N> nodesToResolve = new ArrayList<N> (allNodes);
        
        HashSet<N> loadedSet = new HashSet<N> ();
        Node root = new Node(rootNode, loadedSet);
        nodesToResolve.removeAll(loadedSet);
        if (vertical) {
            root.allocateHorizontally();
            root.resolveVertically(originX, originY);
        } else {
            root.allocateVertically();
            root.resolveHorizontally(originX, originY);
        }
        
        final HashMap<N, Point> resultPosition = new HashMap<N, Point> ();
        root.upload(resultPosition);
        
        for (N node : nodesToResolve) {
            Point position = new Point();
            // TODO - resolve others
            resultPosition.put(node, position);
        }
        
        for (Map.Entry<N, Point> entry : resultPosition.entrySet()) {
            scene.findWidget(entry.getKey()).setPreferredLocation(entry.getValue());
        }
        scene.validate();
    }
    
    /**
     * Collects a collection of children nodes of a specified node.
     * @param node the node
     * @return the collection of children
     */
    protected Collection<N> resolveChildren(N node) {
        
        HashSet<N> nodes = new HashSet<N> ();
        Collection<E> allEdges = scene.getEdges();
        for( E edge : allEdges){
            P pinSource = scene.getEdgeSource(edge);
            if( scene.getPinNode(pinSource).equals(node)){
                nodes.add(node);
            }
        }
//        Collection<E> edges = scene.findNodeEdges(node, false, true);
//        HashSet<N> nodes = new HashSet<N> ();
//        for (E edge : edges)
//            nodes.add(scene.getEdgeTarget(edge));
        return nodes;
    }
    
    private class Node {
        
        private final N node;
        private final ArrayList<Node> children;
        
        private Rectangle relativeBounds;
        private int space;
        private int totalSpace;
        private Point point;
        
        private Node(N node, HashSet<N> loadedSet) {
            this.node = node;
            loadedSet.add(node);
            
            Collection<N> list = resolveChildren(node);
            children = new ArrayList<Node> ();
            for (N child : list) {
                if (! loadedSet.contains(child)) {
                    children.add(new Node(child, loadedSet));
                }
            }
        }
        
        private int allocateHorizontally() {
            Widget widget = scene.findWidget(node);
            widget.getLayout().layout(widget);
            relativeBounds = widget.getPreferredBounds();
            space = 0;
            for (int i = 0; i < children.size(); i++) {
                if (i > 0) {
                    space += horizontalGap;
                }
                space += children.get(i).allocateHorizontally();
            }
            totalSpace = Math.max(space, relativeBounds.width);
            return totalSpace;
        }
        
        private void resolveVertically(int paramX, int paramY) {
            int x = paramX;
            int y = paramY;
            point = new Point(x + totalSpace / 2, y - relativeBounds.y);
            x += (totalSpace - space) / 2;
            y += relativeBounds.height + verticalGap;
            for (Node child : children) {
                child.resolveVertically(x, y);
                x += child.totalSpace + horizontalGap;
            }
        }
        
        private int allocateVertically() {
            Widget widget = scene.findWidget(node);
            widget.getLayout().layout(widget);
            relativeBounds = widget.getPreferredBounds();
            space = 0;
            for (int i = 0; i < children.size(); i++) {
                if (i > 0) {
                    space += verticalGap;
                }
                space += children.get(i).allocateVertically();
            }
            totalSpace = Math.max(space, relativeBounds.height);
            return totalSpace;
        }
        
        private void resolveHorizontally(int paramX, int paramY) {
            int x = paramX;
            int y = paramY;
            point = new Point(x - relativeBounds.x, y + totalSpace / 2);
            x += relativeBounds.width + horizontalGap;
            y += (totalSpace - space) / 2;
            for (Node child : children) {
                child.resolveHorizontally(x, y);
                y += child.totalSpace + verticalGap;
            }
        }
        
        private void upload(HashMap<N, Point> result) {
            result.put(node, point);
            for (Node child : children) {
                child.upload(result);
            }
        }
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
    
}
