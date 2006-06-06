/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.graph.layout;

import org.netbeans.api.visual.graph.EdgeController;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.NodeController;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.*;

/**
 * @author David Kaspar
 */
public class TreeGraphLayout<N, E, NC extends NodeController<N>, EC extends EdgeController<E>> {

    private GraphScene<N, E, NC, EC> scene;
    private int originX;
    private int originY;
    private int verticalGap;
    private int horizontalGap;
    private boolean vertical;

    public TreeGraphLayout (GraphScene<N, E, NC, EC> scene, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        this.scene = scene;
        this.originX = originX;
        this.originY = originY;
        this.verticalGap = verticalGap;
        this.horizontalGap = horizontalGap;
        this.vertical = vertical;
    }

    public final void layout (NC rootNode) {
        if (rootNode == null)
            return;
        Collection<NC> allNodes = scene.getNodes ();
        ArrayList<NC> nodesToResolve = new ArrayList<NC> (allNodes);

        HashSet<NC> loadedSet = new HashSet<NC> ();
        Node root = new Node (rootNode, loadedSet);
        nodesToResolve.removeAll (loadedSet);
        if (vertical) {
            root.allocateHorizontally ();
            root.resolveVertically (originX, originY);
        } else {
            root.allocateVertically ();
            root.resolveHorizontally (originX, originY);
        }

        final HashMap<NC, Point> resultPosition = new HashMap<NC, Point> ();
        root.upload (resultPosition);

        for (NC node : nodesToResolve) {
            Point position = new Point ();
            // TODO - resolve others
            resultPosition.put (node, position);
        }

        for (Map.Entry<NC, Point> entry : resultPosition.entrySet ())
            entry.getKey ().getMainWidget ().setPreferredLocation (entry.getValue ());
    }

    protected Collection<NC> resolveChildren (NC node) {
        Collection<EC> edges = scene.findNodeEdges (node, false, true);
        HashSet<NC> nodes = new HashSet<NC> ();
        for (EC edge : edges)
            nodes.add (scene.getEdgeTarget (edge));
        return nodes;
    }

    private class Node {

        private NC node;
        private ArrayList<Node> children;

        private Rectangle relativeBounds;
        private int space;
        private int totalSpace;
        private Point point;

        public Node (NC node, HashSet<NC> loadedSet) {
            this.node = node;
            loadedSet.add (node);

            Collection<NC> list = resolveChildren (node);
            children = new ArrayList<Node> ();
            for (NC child : list)
                if (! loadedSet.contains (child))
                    children.add (new Node (child, loadedSet));
        }

        public int allocateHorizontally () {
            Widget widget = node.getMainWidget ();
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += horizontalGap;
                space += children.get (i).allocateHorizontally ();
            }
            totalSpace = Math.max (space, relativeBounds.width);
            return totalSpace;
        }

        public void resolveVertically (int x, int y) {
            point = new Point (x + totalSpace / 2, y - relativeBounds.y);
            x += (totalSpace - space) / 2;
            y += relativeBounds.height + verticalGap;
            for (Node child : children) {
                child.resolveVertically (x, y);
                x += child.totalSpace + horizontalGap;
            }
        }

        public int allocateVertically () {
            Widget widget = node.getMainWidget ();
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += verticalGap;
                space += children.get (i).allocateVertically ();
            }
            totalSpace = Math.max (space, relativeBounds.height);
            return totalSpace;
        }

        public void resolveHorizontally (int x, int y) {
            point = new Point (x - relativeBounds.x, y + totalSpace / 2);
            x += relativeBounds.width + horizontalGap;
            y += (totalSpace - space) / 2;
            for (Node child : children) {
                child.resolveHorizontally (x, y);
                y += child.totalSpace + verticalGap;
            }
        }

        public void upload (HashMap<NC, Point> result) {
            result.put (node, point);
            for (Node child : children)
                child.upload (result);
        }
    }

}
