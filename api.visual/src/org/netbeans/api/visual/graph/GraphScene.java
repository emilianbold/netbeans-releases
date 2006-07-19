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

import org.netbeans.api.visual.model.ObjectController;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

import java.util.*;

/**
 * @author David Kaspar
 */
// TODO - is it asserted that removing a node disconnects all the attached edges
// TODO - attachNodeController should be called first, then value of NodeController.getID should be used and storaged in structures, similarly for attachEdgeController
public abstract class GraphScene<Node, Edge, NodeCtrl extends NodeController<Node>, EdgeCtrl extends EdgeController<Edge>> extends ObjectScene {

    private HashMap<Node, NodeCtrl> nodeControllers = new HashMap<Node, NodeCtrl> ();
    private HashMap<Edge, EdgeCtrl> edgeControllers = new HashMap<Edge, EdgeCtrl> ();
    private HashMap<EdgeCtrl, NodeCtrl> edgeSourceNodeControllers = new HashMap<EdgeCtrl, NodeCtrl> ();
    private HashMap<EdgeCtrl, NodeCtrl> edgeTargetNodeControllers = new HashMap<EdgeCtrl, NodeCtrl> ();
    private HashMap<NodeCtrl, List<EdgeCtrl>> nodeInputEdgeControllers = new HashMap<NodeCtrl, List<EdgeCtrl>> ();
    private HashMap<NodeCtrl, List<EdgeCtrl>> nodeOutputEdgeControllers = new HashMap<NodeCtrl, List<EdgeCtrl>> ();

    public GraphScene () {
    }

    private void removeWidgets (ObjectController controller) {
        for (Widget widget : controller.getWidgets ())
            widget.getParentWidget ().removeChild (widget);
    }

    public final NodeCtrl addNode (Node node) {
        assert ! nodeControllers.containsKey (node);
        NodeCtrl nodeController = attachNodeController (node);
        assert nodeController != null;
        addObject (nodeController);
        node = nodeController.getNode ();
        nodeControllers.put (node, nodeController);
        nodeInputEdgeControllers.put (nodeController, new ArrayList<EdgeCtrl> ());
        nodeOutputEdgeControllers.put (nodeController, new ArrayList<EdgeCtrl> ());
        return nodeController;
    }

    public final void removeNode (NodeCtrl nodeController) {
        assert nodeController != null;
        for (EdgeCtrl edgeController : findNodeEdges (nodeController, true, false))
            setEdgeSource (edgeController, null);
        for (EdgeCtrl edgeController : findNodeEdges (nodeController, false, true))
            setEdgeTarget (edgeController, null);
        nodeInputEdgeControllers.remove (nodeController);
        nodeOutputEdgeControllers.remove (nodeController);
        nodeControllers.remove (nodeController.getNode ());
        removeWidgets (nodeController);
        removeObject (nodeController);
    }

    public final Collection<NodeCtrl> getNodes () {
        return Collections.unmodifiableCollection (nodeControllers.values ());
    }

    public final NodeCtrl findNodeController (Node node) {
        return nodeControllers.get (node);
    }

    public final EdgeCtrl addEdge (Edge edge) {
        assert ! edgeControllers.containsKey (edge);
        EdgeCtrl edgeController = attachEdgeController (edge);
        assert edgeController != null;
        addObject (edgeController);
        edge = edgeController.getEdge ();
        edgeControllers.put (edge, edgeController);
        return edgeController;
    }

    public final void removeEdge (EdgeCtrl edgeController) {
        assert edgeController != null;
        setEdgeSource (edgeController, null);
        setEdgeTarget (edgeController, null);
        edgeControllers.remove (edgeController.getEdge ());
        removeWidgets (edgeController);
        removeObject (edgeController);
    }

    public final Collection<EdgeCtrl> getEdges () {
        return Collections.unmodifiableCollection (edgeControllers.values ());
    }

    public final EdgeCtrl findEdgeController (Edge edge) {
        return edgeControllers.get (edge);
    }

    public final void setEdgeSource (EdgeCtrl edgeController, NodeCtrl sourceNodeController) {
        NodeCtrl oldNodeController = edgeSourceNodeControllers.put (edgeController, sourceNodeController);
        if (oldNodeController == sourceNodeController)
            return;
        if (oldNodeController != null)
            nodeOutputEdgeControllers.get (oldNodeController).remove (edgeController);
        attachEdgeSource (edgeController, sourceNodeController);
        if (sourceNodeController != null)
            nodeOutputEdgeControllers.get (sourceNodeController).add (edgeController);

    }

    public final void setEdgeTarget (EdgeCtrl edgeController, NodeCtrl targetNodeController) {
        NodeCtrl oldNodeController = edgeTargetNodeControllers.put (edgeController, targetNodeController);
        if (oldNodeController == targetNodeController)
            return;
        if (oldNodeController != null)
            nodeInputEdgeControllers.get (oldNodeController).remove (edgeController);
        attachEdgeTarget (edgeController, targetNodeController);
        if (targetNodeController != null)
            nodeInputEdgeControllers.get (targetNodeController).add (edgeController);
    }

    public final NodeCtrl getEdgeSource (EdgeCtrl edgeController) {
        return edgeSourceNodeControllers.get (edgeController);
    }

    public final NodeCtrl getEdgeTarget (EdgeCtrl edgeController) {
        return edgeTargetNodeControllers.get (edgeController);
    }

    public final Collection<EdgeCtrl> findNodeEdges (NodeCtrl nodeController, boolean allowOutputEdges, boolean allowInputEdges) {
        ArrayList<EdgeCtrl> list = new ArrayList<EdgeCtrl> ();
        if (allowInputEdges)
            list.addAll (nodeInputEdgeControllers.get (nodeController));
        if (allowOutputEdges)
            list.addAll (nodeOutputEdgeControllers.get (nodeController));
        return list;
    }

    public final Collection<EdgeCtrl> findEdgeBetween (NodeCtrl sourceNodeController, NodeCtrl targetNodeController) {
        HashSet<EdgeCtrl> list = new HashSet<EdgeCtrl> ();
        List<EdgeCtrl> inputEdgeControllers = nodeInputEdgeControllers.get (targetNodeController);
        List<EdgeCtrl> outputEdgeControllers = nodeOutputEdgeControllers.get (sourceNodeController);
        for (EdgeCtrl edgeController : inputEdgeControllers)
            if (outputEdgeControllers.contains (edgeController))
                list.add (edgeController);
        return list;
    }

    protected abstract NodeCtrl attachNodeController (Node node);

    protected abstract EdgeCtrl attachEdgeController (Edge edge);

    protected abstract void attachEdgeSource (EdgeCtrl edgeController, NodeCtrl sourceNodeController);

    protected abstract void attachEdgeTarget (EdgeCtrl edgeController, NodeCtrl targetNodeController);
    
    public static abstract class StringGraph extends GraphScene<String, String, NodeController.StringNode, EdgeController.StringEdge> {
        
    }

}
