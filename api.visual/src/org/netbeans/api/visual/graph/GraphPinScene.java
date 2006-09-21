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
import org.netbeans.modules.visual.util.GeomUtil;

import java.util.*;

/**
 * @author David Kaspar
 */
// TODO - is it asserted that removing a node removes all its pins
// TODO - is it asserted that removing a pin disconnects all the attached edges
public abstract class GraphPinScene<N, E, P> extends ObjectScene {

    private HashSet<N> nodes = new HashSet<N> ();
    private Set<N> nodesUm = Collections.unmodifiableSet (nodes);
    private HashSet<E> edges = new HashSet<E> ();
    private Set<E> edgesUm = Collections.unmodifiableSet (edges);
    private HashSet<P> pins = new HashSet<P> ();
    private Set<P> pinsUm = Collections.unmodifiableSet (pins);

    private HashMap<N, HashSet<P>> nodePins = new HashMap<N, HashSet<P>> ();
    private HashMap<P, N> pinNodes = new HashMap<P, N> ();
    private HashMap<E, P> edgeSourcePins = new HashMap<E, P> ();
    private HashMap<E, P> edgeTargetPins = new HashMap<E, P> ();
    private HashMap<P, List<E>> pinInputEdges = new HashMap<P, List<E>> ();
    private HashMap<P, List<E>> pinOutputEdges = new HashMap<P, List<E>> ();

    public GraphPinScene () {
    }

    public final Widget addNode (N node) {
        assert node != null  &&  ! nodes.contains (node);
        Widget widget = attachNodeWidget (node);
        addObject (node, widget);
        nodes.add (node);
        nodePins.put (node, new HashSet<P> ());
        notifyNodeAdded (node, widget);
        return widget;
    }

    public final void removeNode (N node) {
        assert node != null  &&  nodes.contains (node);
        for (P pin : new HashSet<P> (nodePins.get (node)))
            removePin (pin);
        nodes.remove (node);
        nodePins.remove (node);
        Widget widget = findWidget (node);
        removeObject (node);
        detachNodeWidget (node, widget);
    }

    public final Collection<N> getNodes () {
        return nodesUm;
    }

    public final Widget addEdge (E edge) {
        assert edge != null  &&  ! edges.contains (edge);
        Widget widget = attachEdgeWidget (edge);
        addObject (edge, widget);
        edges.add (edge);
        notifyEdgeAdded (edge, widget);
        return widget;
    }

    public final void removeEdge (E edge) {
        assert edge != null  &&  edges.contains (edge);
        setEdgeSource (edge, null);
        setEdgeTarget (edge, null);
        edges.remove (edge);
        edgeSourcePins.remove (edge);
        edgeTargetPins.remove (edge);
        Widget widget = findWidget (edge);
        removeObject (edge);
        detachEdgeWidget (edge, widget);
    }

    public final Collection<E> getEdges () {
        return edgesUm;
    }

    public final Widget addPin (N node, P pin) {
        assert node != null  &&  pin != null  &&  ! pins.contains (pin);
        Widget widget = attachPinWidget (node, pin);
        addObject (pin, widget);
        pins.add (pin);
        nodePins.get (node).add (pin);
        pinNodes.put (pin, node);
        pinInputEdges.put (pin, new ArrayList<E> ());
        pinOutputEdges.put (pin, new ArrayList<E> ());
        notifyPinAdded (node, pin, widget);
        return widget;
    }

    public final void removePin (P pin) {
        assert pin != null  &&  pins.contains (pin);
        for (E edge : findPinEdges (pin, true, false))
            setEdgeSource (edge, null);
        for (E edge : findPinEdges (pin, false, true))
            setEdgeTarget (edge, null);
        pins.remove (pin);
        N node = pinNodes.remove (pin);
        nodePins.get (node).remove (pin);
        pinInputEdges.remove (pin);
        pinOutputEdges.remove (pin);
        Widget widget = findWidget (pin);
        removeObject (pin);
        detachPinWidget (pin, widget);
    }

    public final N getPinNode (P pin) {
        return pinNodes.get (pin);
    }

    public final Collection<P> getPins () {
        return pinsUm;
    }

    public final Collection<P> getNodePins (N node) {
        if (node == null)
            return null;
        HashSet<P> ps = nodePins.get (node);
        if (ps == null)
            return null;
        return Collections.unmodifiableCollection (ps);
    }

    public final void setEdgeSource (E edge, P sourcePin) {
        assert edge != null  &&  edges.contains (edge);
        if (sourcePin != null)
            assert pins.contains (sourcePin);
        P oldPin = edgeSourcePins.put (edge, sourcePin);
        if (GeomUtil.equals (oldPin, sourcePin))
            return;
        if (oldPin != null)
            pinOutputEdges.get (oldPin).remove (edge);
        if (sourcePin != null)
            pinOutputEdges.get (sourcePin).add (edge);
        attachEdgeSourceAnchor (edge, oldPin, sourcePin);
    }

    public final void setEdgeTarget (E edge, P targetPin) {
        assert edge != null  &&  edges.contains (edge);
        if (targetPin != null)
            assert pins.contains (targetPin);
        P oldPin = edgeTargetPins.put (edge, targetPin);
        if (GeomUtil.equals (oldPin, targetPin))
            return;
        if (oldPin != null)
            pinInputEdges.get (oldPin).remove (edge);
        if (targetPin != null)
            pinInputEdges.get (targetPin).add (edge);
        attachEdgeTargetAnchor (edge, oldPin, targetPin);
    }

    public final P getEdgeSource (E edge) {
        return edgeSourcePins.get (edge);
    }

    public final P getEdgeTarget (E edge) {
        return edgeTargetPins.get (edge);
    }

    public final Collection<E> findPinEdges (P pin, boolean allowOutputEdges, boolean allowInputEdges) {
        ArrayList<E> list = new ArrayList<E> ();
        if (allowInputEdges)
            list.addAll (pinInputEdges.get (pin));
        if (allowOutputEdges)
            list.addAll (pinOutputEdges.get (pin));
        return list;
    }

    @Deprecated
    public final Collection<E> findEdgeBetween (P sourcePin, P targetPin) {
        return findEdgesBetween (sourcePin, targetPin);
    }

    public final Collection<E> findEdgesBetween (P sourcePin, P targetPin) {
        HashSet<E> list = new HashSet<E> ();
        List<E> inputEdges = pinInputEdges.get (targetPin);
        List<E> outputEdges = pinOutputEdges.get (sourcePin);
        for (E edge : inputEdges)
            if (outputEdges.contains (edge))
                list.add (edge);
        return list;
    }

    public boolean isNode (Object object) {
        return nodes.contains (object);
    }

    public boolean isPin (Object object) {
        return pins.contains (object);
    }

    public boolean isEdge (Object object) {
        return edges.contains (object);
    }

    protected void detachNodeWidget (N node, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    protected void detachPinWidget (P pin, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    protected void detachEdgeWidget (E edge, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    protected void notifyNodeAdded (N node, Widget widget) {
    }

    protected void notifyEdgeAdded (E edge, Widget widget) {
    }

    protected void notifyPinAdded (N node, P pin, Widget widget) {
    }

    protected abstract Widget attachNodeWidget (N node);

    protected abstract Widget attachPinWidget (N node, P pin);

    protected abstract Widget attachEdgeWidget (E edge);

    protected abstract void attachEdgeSourceAnchor (E edge, P oldSourcePin, P sourcePin);

    protected abstract void attachEdgeTargetAnchor (E edge, P oldTargetPin, P targetPin);

    public static abstract class StringGraph extends GraphPinScene<String, String, String> {

    }

}
