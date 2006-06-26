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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.action.PanAction;
import org.netbeans.api.visual.action.PopupMenuAction;
import org.netbeans.api.visual.action.ZoomAction;
import org.netbeans.api.visual.action.MoveControlPointAction;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.DirectionalAnchor;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.EdgeController;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.router.OrthogonalSearchRouter;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;

/**
 * @author David Kaspar
 */
public class VMDGraphScene extends GraphPinScene<String, String, String, VMDNodeController, EdgeController.StringEdge, VMDPinController> {

    public static final String PIN_ID_DEFAULT = "#default"; // NOI18N

    private LayerWidget backgroundLayer = new LayerWidget (this);
    private LayerWidget mainLayer = new LayerWidget (this);
    private LayerWidget connectionLayer = new LayerWidget (this);
    private LayerWidget upperLayer = new LayerWidget (this);

    private OrthogonalSearchRouter.CollisionsCollector collisionsCollector;

    private MoveControlPointAction moveControlPointAction = new MoveControlPointAction.OrthogonalMoveAction ();
    private PopupMenuAction popupMenuAction = new MyPopupMenuAction ();

    public VMDGraphScene () {
        addChild (backgroundLayer);
        addChild (mainLayer);
        addChild (connectionLayer);
        addChild (upperLayer);

        collisionsCollector = new OrthogonalSearchRouter.WidgetsCollisionCollector (mainLayer, connectionLayer);

        getActions ().addAction(new ZoomAction ());
        getActions ().addAction(new PanAction ());
    }

    protected VMDNodeController attachNodeController (String node) {
        VMDNodeWidget widget = new VMDNodeWidget (this);
        mainLayer.addChild (widget);

        widget.getActions ().addAction (createHoverAction ());
        widget.getActions ().addAction (createSelectAction ());
        widget.getActions ().addAction (popupMenuAction);
        widget.getActions ().addAction (createMoveAction ());

        return new VMDNodeController (node, widget);
    }

    protected EdgeController.StringEdge attachEdgeController (String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget (this);
        connectionWidget.setRouter (new OrthogonalSearchRouter (collisionsCollector));
        connectionWidget.setSourceAnchorShape (AnchorShape.TRIANGLE_OUT);
        connectionWidget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        connectionWidget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
        connectionWidget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
        connectionLayer.addChild (connectionWidget);

        connectionWidget.getActions ().addAction (createHoverAction ());
        connectionWidget.getActions ().addAction (createSelectAction ());
        connectionWidget.getActions ().addAction (moveControlPointAction);

        return new EdgeController.StringEdge (edge, connectionWidget);
    }

    protected VMDPinController attachPinController (VMDNodeController nodeController, String pin) {
        if (PIN_ID_DEFAULT.equals (pin)) {
            return new VMDPinController (pin, null);
        } else {
            VMDPinWidget widget = new VMDPinWidget (this);
            nodeController.getNodeWidget ().addPin (widget);
            widget.getActions ().addAction (createHoverAction ());
            widget.getActions ().addAction (createSelectAction ());

            return new VMDPinController (pin, widget);
        }
    }

    protected void attachEdgeSource (EdgeController.StringEdge edgeController, VMDPinController sourcePinController) {
        ((ConnectionWidget) edgeController.getMainWidget ()).setSourceAnchor (getPinNode (sourcePinController).getNodeWidget().createAnchorPin(new DirectionalAnchor (sourcePinController.getMainWidget (), DirectionalAnchor.Kind.HORIZONTAL)));
    }

    protected void attachEdgeTarget (EdgeController.StringEdge edgeController, VMDPinController targetPinController) {
        ((ConnectionWidget) edgeController.getMainWidget ()).setTargetAnchor (getPinNode (targetPinController).getNodeWidget().getNodeAnchor ());
    }

    private static class MyPopupMenuAction extends PopupMenuAction {

        public JPopupMenu getPopupMenu (Widget widget) {
            JPopupMenu popupMenu = new JPopupMenu ();
            popupMenu.add (new JMenuItem ("Open " + ((VMDNodeWidget) widget).getNodeName ()));
            return popupMenu;
        }

    }

}
