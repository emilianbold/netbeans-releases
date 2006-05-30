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

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.DirectionalAnchor;
import org.netbeans.api.visual.graph.EdgeController;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.router.OrthogonalSearchRouter;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class VMDGraphScene extends GraphPinScene<String, String, String, VMDNodeController, EdgeController.StringEdge, VMDPinController> {

    private static final Color COLOR_HOVER = new Color (0xABC7DE);
    public static final String PIN_ID_DEFAULT = "#default"; // NOI18N

    private Widget backgroundLayer = new Widget (this);
    private Widget mainLayer = new Widget (this);
    private Widget connectionLayer = new Widget (this);
    private Widget upperLayer = new Widget (this);

    private OrthogonalSearchRouter.CollisionsCollector collisionsCollector;

    private MoveAction moveAction = new MoveAction ();
    private MouseHoverAction mouseHoverAction = new MyMouseHoverAction ();
    private PopupMenuAction popupMenuAction = new MyPopupMenuAction ();

    public VMDGraphScene () {
        addChild (backgroundLayer);
        addChild (mainLayer);
        addChild (connectionLayer);
        addChild (upperLayer);

        collisionsCollector = new OrthogonalSearchRouter.WidgetsCollisionCollector (mainLayer);

        getActions ().addAction (mouseHoverAction);
        getActions ().addAction(new ZoomAction ());
        getActions ().addAction(new PanAction ());
    }

    protected VMDNodeController attachNodeController (String node) {
        VMDNodeWidget widget = new VMDNodeWidget (this, mouseHoverAction);
        mainLayer.addChild (widget);

        widget.getActions ().addAction (moveAction);
        widget.getActions ().addAction (popupMenuAction);

        return new VMDNodeController (node, widget);
    }

    protected EdgeController.StringEdge attachEdgeController (String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget (this);
        connectionWidget.setRouter (new OrthogonalSearchRouter (collisionsCollector));
        connectionWidget.setSourceAnchorShape (AnchorShape.TRIANGLE_OUT);
        connectionWidget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        connectionLayer.addChild (connectionWidget);
        return new EdgeController.StringEdge (edge, connectionWidget);
    }

    protected VMDPinController attachPinController (VMDNodeController nodeController, String pin) {
        if (PIN_ID_DEFAULT.equals (pin)) {
            return new VMDPinController (pin, null);
        } else {
            VMDPinWidget widget = new VMDPinWidget (this, mouseHoverAction);
            nodeController.getNodeWidget ().addPin (widget);
            return new VMDPinController (pin, widget);
        }
    }

    protected void attachEdgeSource (EdgeController.StringEdge edgeController, VMDPinController sourcePinController) {
        ((ConnectionWidget) edgeController.getMainWidget ()).setSourceAnchor (getPinNode (sourcePinController).getNodeWidget().createAnchorPin(new DirectionalAnchor (sourcePinController.getMainWidget (), DirectionalAnchor.Kind.HORIZONTAL)));
    }

    protected void attachEdgeTarget (EdgeController.StringEdge edgeController, VMDPinController targetPinController) {
        ((ConnectionWidget) edgeController.getMainWidget ()).setTargetAnchor (getPinNode (targetPinController).getNodeWidget().getNodeAnchor ());
    }

    private static class MyMouseHoverAction extends MouseHoverAction.TwoStated {

        private Paint lastBackground;
        private boolean lastOpaque;

        protected void unsetHovering (Widget widget) {
            widget.setBackground (lastBackground);
            widget.setOpaque (lastOpaque);
        }

        protected void setHovering (Widget widget) {
            lastBackground = widget.getBackground ();
            lastOpaque = widget.isOpaque ();
            widget.setBackground (COLOR_HOVER);
            widget.setOpaque (true);
        }

    }

    private static class MyPopupMenuAction extends PopupMenuAction {

        public JPopupMenu getPopupMenu (Widget widget) {
            JPopupMenu popupMenu = new JPopupMenu ();
            popupMenu.add (new JMenuItem ("Open " + ((VMDNodeWidget) widget).getNodeName ()));
            return popupMenu;
        }

    }

}
