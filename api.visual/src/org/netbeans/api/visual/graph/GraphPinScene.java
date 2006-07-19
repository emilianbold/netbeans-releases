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
// TODO - PinCtrl should not have NodeCtrl reference, add NodeCtrl getPinNode (PinCtrl)
// TODO - is it asserted that removing a node removes all its pins
// TODO - is it asserted that removing a pin disconnects all the attached edges
// TODO - attachNodeController should be called first, then value of NodeController.getID should be used and storaged in structures, similarly for attachEdgeController and attachPinController
public abstract class GraphPinScene<Node, Edge, Pin, NodeCtrl extends NodeController<Node>, EdgeCtrl extends EdgeController<Edge>, PinCtrl extends PinController<Pin>> extends ObjectScene {

    private HashMap<Node, NodeCtrl> nodeControllers = new HashMap<Node, NodeCtrl> ();
    private HashMap<Edge, EdgeCtrl> edgeControllers = new HashMap<Edge, EdgeCtrl> ();
    private HashMap<NodeCtrl, HashMap<Pin, PinCtrl>> nodePinControllers = new HashMap<NodeCtrl, HashMap<Pin, PinCtrl>> ();
    private HashMap<PinCtrl, NodeCtrl> pinNodeControllers = new HashMap<PinCtrl, NodeCtrl> ();
    private HashMap<EdgeCtrl, PinCtrl> edgeSourcePinControllers = new HashMap<EdgeCtrl, PinCtrl> ();
    private HashMap<EdgeCtrl, PinCtrl> edgeTargetPinControllers = new HashMap<EdgeCtrl, PinCtrl> ();
    private HashMap<PinCtrl, List<EdgeCtrl>> pinInputEdgeControllers = new HashMap<PinCtrl, List<EdgeCtrl>> ();
    private HashMap<PinCtrl, List<EdgeCtrl>> pinOutputEdgeControllers = new HashMap<PinCtrl, List<EdgeCtrl>> ();

    public GraphPinScene () {
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
        nodePinControllers.put (nodeController, new HashMap<Pin, PinCtrl> ());
        return nodeController;
    }

    public final void removeNode (NodeCtrl nodeController) {
        assert nodeController != null;
        HashMap<Pin, PinCtrl> pinControllers = nodePinControllers.get (nodeController);
        for (PinCtrl pinController : pinControllers.values ())
            removePin (pinController);
        nodePinControllers.remove (nodeController);
        nodeControllers.remove (nodeController.getNode ());
        removeWidgets (nodeController);
        removeObject (nodeController);
    }

    public final NodeCtrl getNodeController (Node node) {
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

    public final EdgeCtrl getEdgeController (Edge edge) {
        return edgeControllers.get (edge);
    }

    public final PinCtrl addPin (NodeCtrl nodeController, Pin pin) {
        HashMap<Pin, PinCtrl> pinControllers = nodePinControllers.get (nodeController);
        assert pinControllers != null;
        assert ! pinControllers.containsKey (pin);
        PinCtrl pinController = attachPinController (nodeController, pin);
        assert ! pinNodeControllers.containsKey (pinController);
        assert pinController != null;
        addObject (pinController);
        pin = pinController.getPin ();
        pinControllers.put (pin, pinController);
        pinNodeControllers.put (pinController, nodeController);
        pinInputEdgeControllers.put (pinController, new ArrayList<EdgeCtrl> ());
        pinOutputEdgeControllers.put (pinController, new ArrayList<EdgeCtrl> ());
        return pinController;
    }

    public final void removePin (PinCtrl pinController) {
        assert pinController != null;
        NodeCtrl nodeController = pinNodeControllers.get (pinController);
        assert nodeController != null;
        HashMap<Pin, PinCtrl> pinControllers = nodePinControllers.get (nodeController);
        for (EdgeCtrl edgeController : findPinEdges (pinController, true, false))
            setEdgeSource (edgeController, null);
        for (EdgeCtrl edgeController : findPinEdges (pinController, false, true))
            setEdgeTarget (edgeController, null);
        pinInputEdgeControllers.remove (pinController);
        pinOutputEdgeControllers.remove (pinController);
        pinControllers.remove (pinController.getPin ());
        pinNodeControllers.remove (pinController);
        removeWidgets (pinController);
        removeObject (pinController);
    }

    public final NodeCtrl getPinNode (PinCtrl pinController) {
        return pinNodeControllers.get (pinController);
    }

    public final PinCtrl getPinController (NodeCtrl nodeController, Pin pin) {
        if (nodeController == null)
            return null;
        return nodePinControllers.get (nodeController).get (pin);
    }

    public final void setEdgeSource (EdgeCtrl edgeController, PinCtrl sourcePinController) {
        PinCtrl oldPinController = edgeSourcePinControllers.put (edgeController, sourcePinController);
        if (oldPinController == sourcePinController)
            return;
        if (oldPinController != null)
            pinOutputEdgeControllers.get (oldPinController).remove (edgeController);
        attachEdgeSource (edgeController, sourcePinController);
        if (sourcePinController != null)
            pinOutputEdgeControllers.get (sourcePinController).add (edgeController);

    }

    public final void setEdgeTarget (EdgeCtrl edgeController, PinCtrl targetPinController) {
        PinCtrl oldPinController = edgeTargetPinControllers.put (edgeController, targetPinController);
        if (oldPinController == targetPinController)
            return;
        if (oldPinController != null)
            pinInputEdgeControllers.get (oldPinController).remove (edgeController);
        attachEdgeTarget (edgeController, targetPinController);
        if (targetPinController != null)
            pinInputEdgeControllers.get (targetPinController).add (edgeController);
    }

    public final Collection<PinCtrl> getNodePins (NodeCtrl nodeController) {
        return Collections.unmodifiableCollection (nodePinControllers.get (nodeController).values ());
    }

    public final PinCtrl getEdgeSource (EdgeCtrl edgeController) {
        return edgeSourcePinControllers.get (edgeController);
    }

    public final PinCtrl getEdgeTarget (EdgeCtrl edgeController) {
        return edgeTargetPinControllers.get (edgeController);
    }

    public final Collection<EdgeCtrl> findPinEdges (PinCtrl pinController, boolean allowOutputEdges, boolean allowInputEdges) {
        ArrayList<EdgeCtrl> list = new ArrayList<EdgeCtrl> ();
        if (allowInputEdges)
            list.addAll (pinInputEdgeControllers.get (pinController));
        if (allowOutputEdges)
            list.addAll (pinOutputEdgeControllers.get (pinController));
        return list;
    }

    public final Collection<EdgeCtrl> findEdgeBetween (PinCtrl sourcePinController, PinCtrl targetPinController) {
        HashSet<EdgeCtrl> list = new HashSet<EdgeCtrl> ();
        List<EdgeCtrl> inputEdgeControllers = pinInputEdgeControllers.get (targetPinController);
        List<EdgeCtrl> outputEdgeControllers = pinOutputEdgeControllers.get (sourcePinController);
        for (EdgeCtrl edgeController : inputEdgeControllers)
            if (outputEdgeControllers.contains (edgeController))
                list.add (edgeController);
        return list;
    }

    protected abstract NodeCtrl attachNodeController (Node node);

    protected abstract EdgeCtrl attachEdgeController (Edge edge);

    protected abstract PinCtrl attachPinController (NodeCtrl nodeController, Pin pin);

    protected abstract void attachEdgeSource (EdgeCtrl edgeController, PinCtrl sourcePinController);

    protected abstract void attachEdgeTarget (EdgeCtrl edgeController, PinCtrl targetPinController);

    public static abstract class StringGraph extends GraphPinScene<String, String, String, NodeController.StringNode, EdgeController.StringEdge, PinController.StringPin> {

    }

}
