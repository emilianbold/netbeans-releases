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
package org.netbeans.api.visual.graph;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.util.GeomUtil;

import java.util.*;

/**
 * @author David Kaspar
 */
public abstract class GraphScene<N, E> extends ObjectScene {

    private HashSet<N> nodes = new HashSet<N> ();
    private Set<N> nodesUm = Collections.unmodifiableSet (nodes);

    private HashSet<E> edges = new HashSet<E> ();
    private Set<E> edgesUm = Collections.unmodifiableSet (edges);

    private HashMap<E, N> edgeSourceNodes = new HashMap<E, N> ();
    private HashMap<E, N> edgeTargetNodes = new HashMap<E, N> ();

    private HashMap<N, List<E>> nodeInputEdges = new HashMap<N, List<E>> ();
    private HashMap<N, List<E>> nodeOutputEdges = new HashMap<N, List<E>> ();

    public GraphScene () {
    }

    public final Widget addNode (N node) {
        assert node != null  &&  ! nodes.contains (node);
        Widget widget = attachNodeWidget (node);
        addObject (node, widget);
        nodes.add (node);
        nodeInputEdges.put (node, new ArrayList<E> ());
        nodeOutputEdges.put (node, new ArrayList<E> ());
        return widget;
    }

    public final void removeNode (N node) {
        assert node != null  &&  nodes.contains (node);
        for (E edge : findNodeEdges (node, true, false))
            setEdgeSource (edge, null);
        for (E edge : findNodeEdges (node, false, true))
            setEdgeTarget (edge, null);
        nodeInputEdges.remove (node);
        nodeOutputEdges.remove (node);
        Widget widget = findWidget (node);
        removeObject (node);
        detachNodeWidget (node, widget);
    }

    public final void removeNodeWithEdges (N node) {
        for (E edge : findNodeEdges (node, true, true))
            removeEdge (edge);
        removeNode (node);
    }

    public final Collection<N> getNodes () {
        return nodesUm;
    }

    public final Widget addEdge (E edge) {
        assert edge != null  &&  ! edges.contains (edge);
        Widget widget = attachEdgeWidget (edge);
        addObject (edge, widget);
        edges.add (edge);
        return widget;
    }

    public final void removeEdge (E edge) {
        assert edge != null  &&  edges.contains (edge);
        setEdgeSource (edge, null);
        setEdgeTarget (edge, null);
        edges.remove (edge);
        edgeSourceNodes.remove (edge);
        edgeTargetNodes.remove (edge);
        Widget widget = findWidget (edge);
        removeObject (edge);
        detachEdgeWidget (edge, widget);
    }

    public final Collection<E> getEdges () {
        return edgesUm;
    }

    public final void setEdgeSource (E edge, N sourceNode) {
        assert edge != null  &&  edges.contains (edge);
        if (sourceNode != null)
            assert nodes.contains (sourceNode);
        N oldNode = edgeSourceNodes.put (edge, sourceNode);
        if (GeomUtil.equals (oldNode, sourceNode))
            return;
        if (oldNode != null)
            nodeOutputEdges.get (oldNode).remove (edge);
        if (sourceNode != null)
            nodeOutputEdges.get (sourceNode).add (edge);
        attachEdgeSourceAnchor (edge, oldNode, sourceNode);
    }

    public final void setEdgeTarget (E edge, N targetNode) {
        assert edge != null  &&  edges.contains (edge);
        if (targetNode != null)
            assert nodes.contains (targetNode);
        N oldNode = edgeTargetNodes.put (edge, targetNode);
        if (GeomUtil.equals (oldNode, targetNode))
            return;
        if (oldNode != null)
            nodeInputEdges.get (oldNode).remove (edge);
        if (targetNode != null)
            nodeInputEdges.get (targetNode).add (edge);
        attachEdgeTargetAnchor (edge, oldNode, targetNode);
    }

    public final N getEdgeSource (E edge) {
        return edgeSourceNodes.get (edge);
    }

    public final N getEdgeTarget (E edge) {
        return edgeTargetNodes.get (edge);
    }

    public final Collection<E> findNodeEdges (N node, boolean allowOutputEdges, boolean allowInputEdges) {
        ArrayList<E> list = new ArrayList<E> ();
        if (allowInputEdges)
            list.addAll (nodeInputEdges.get (node));
        if (allowOutputEdges)
            list.addAll (nodeOutputEdges.get (node));
        return list;
    }

    public final Collection<E> findEdgeBetween (N sourceNode, N targetNode) {
        HashSet<E> list = new HashSet<E> ();
        List<E> inputEdges = nodeInputEdges.get (targetNode);
        List<E> outputEdges = nodeOutputEdges.get (sourceNode);
        for (E edge : inputEdges)
            if (outputEdges.contains (edge))
                list.add (edge);
        return list;
    }

    public boolean isNode (Object object) {
        return nodes.contains (object);
    }

    public boolean isEdge (Object object) {
        return edges.contains (object);
    }

    protected void detachNodeWidget (N node, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    protected void detachEdgeWidget (E edge, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    protected abstract Widget attachNodeWidget (N node);

    protected abstract Widget attachEdgeWidget (E edge);

    protected abstract void attachEdgeSourceAnchor (E edge, N oldSourceNode, N sourceNode);

    protected abstract void attachEdgeTargetAnchor (E edge, N oldTargetNode, N targetNode);
    
    public static abstract class StringGraph extends GraphScene<String, String> {
        
    }

}
