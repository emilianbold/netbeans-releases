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
package org.netbeans.modules.visual.graph.layout;

import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class TreeGraphLayout<N,E> extends GraphLayout<N,E> {

    private int originX;
    private int originY;
    private int verticalGap;
    private int horizontalGap;
    private boolean vertical;

    private N rootNode;

    public TreeGraphLayout (int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        this.originX = originX;
        this.originY = originY;
        this.verticalGap = verticalGap;
        this.horizontalGap = horizontalGap;
        this.vertical = vertical;
    }

    public void setRootNode (N rootNode) {
        this.rootNode = rootNode;
    }

    protected void performGraphLayout (UniversalGraph<N, E> graph) {
        if (rootNode == null)
            return;
        Collection<N> allNodes = graph.getNodes ();
        if (! allNodes.contains (rootNode))
            return;
        ArrayList<N> nodesToResolve = new ArrayList<N> (allNodes);

        HashSet<N> loadedSet = new HashSet<N> ();
        Node root = new Node (graph, rootNode, loadedSet);
        nodesToResolve.removeAll (loadedSet);
        if (vertical) {
            root.allocateHorizontally (graph);
            root.resolveVertically (originX, originY);
        } else {
            root.allocateVertically (graph);
            root.resolveHorizontally (originX, originY);
        }

        final HashMap<N, Point> resultPosition = new HashMap<N, Point> ();
        root.upload (resultPosition);

        for (N node : nodesToResolve) {
            Point position = new Point ();
            // TODO - resolve others
            resultPosition.put (node, position);
        }

        for (Map.Entry<N, Point> entry : resultPosition.entrySet ())
            setResolvedNodeLocation (graph, entry.getKey (), entry.getValue ());
    }

    protected void performNodesLayout (UniversalGraph<N, E> universalGraph, Collection<N> nodes) {
        throw new UnsupportedOperationException (); // TODO
    }

    private class Node {

        private N node;
        private ArrayList<Node> children;

        private Rectangle relativeBounds;
        private int space;
        private int totalSpace;
        private Point point;

        private Node (UniversalGraph<N, E> graph, N node, HashSet<N> loadedSet) {
            this.node = node;
            loadedSet.add (node);

            children = new ArrayList<Node> ();
            for (E edge: graph.findNodeEdges (node, true, false)) {
                N child = graph.getEdgeTarget (edge);
                if (child != null  &&  ! loadedSet.contains (child))
                    children.add (new Node (graph, child, loadedSet));
            }
        }

        private int allocateHorizontally (UniversalGraph<N, E> graph) {
            Widget widget = graph.getScene ().findWidget (node);
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += horizontalGap;
                space += children.get (i).allocateHorizontally (graph);
            }
            totalSpace = Math.max (space, relativeBounds.width);
            return totalSpace;
        }

        private void resolveVertically (int x, int y) {
            point = new Point (x + totalSpace / 2, y - relativeBounds.y);
            x += (totalSpace - space) / 2;
            y += relativeBounds.height + verticalGap;
            for (Node child : children) {
                child.resolveVertically (x, y);
                x += child.totalSpace + horizontalGap;
            }
        }

        private int allocateVertically (UniversalGraph<N, E> graph) {
            Widget widget = graph.getScene ().findWidget (node);
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += verticalGap;
                space += children.get (i).allocateVertically (graph);
            }
            totalSpace = Math.max (space, relativeBounds.height);
            return totalSpace;
        }

        private void resolveHorizontally (int x, int y) {
            point = new Point (x - relativeBounds.x, y + totalSpace / 2);
            x += relativeBounds.width + horizontalGap;
            y += (totalSpace - space) / 2;
            for (Node child : children) {
                child.resolveHorizontally (x, y);
                y += child.totalSpace + verticalGap;
            }
        }

        private void upload (HashMap<N, Point> result) {
            result.put (node, point);
            for (Node child : children)
                child.upload (result);
        }
    }

}
