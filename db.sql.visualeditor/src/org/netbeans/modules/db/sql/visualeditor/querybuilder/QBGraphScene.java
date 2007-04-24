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
package  org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;

import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.router.Router;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JList;

/**
 * @author Jim Davidson
 */
public class QBGraphScene extends GraphScene {

    private LayerWidget 	mainLayer;
    private LayerWidget 	connectionLayer;

    private WidgetAction 	moveAction = ActionFactory.createMoveAction ();
    private WidgetAction 	mouseHoverAction = ActionFactory.createHoverAction (new MyHoverProvider ());
    // private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction (new MyPopupProvider()) ;

    private int 		pos = 0;
    
    private Router 		router;
 
    public QBGraphScene (QueryBuilderGraphFrame qbGF) {
        mainLayer = new LayerWidget (this);
        connectionLayer = new LayerWidget (this);
        addChild (mainLayer);
        addChild (connectionLayer);

	router = RouterFactory.createOrthogonalSearchRouter (mainLayer, connectionLayer);
        getActions().addAction (mouseHoverAction);
        // getActions().addAction(ActionFactory.createZoomAction());
	// ToDo: qbGF has been passed in only to support the menu; eventually, merge QBGF into this
        getActions().addAction(ActionFactory.createPopupMenuAction(qbGF));
    }

    protected Widget attachNodeWidget (Object node) {
	Widget widget = new Widget(this);
	widget.setLayout(LayoutFactory.createVerticalFlowLayout());
	widget.setBorder(BorderFactory.createLineBorder());
	widget.getActions().addAction(moveAction);

	ComponentWidget componentWidget = null;
	LabelWidget label = null;

	if (node instanceof QBNodeComponent) // we have a qbNodeComponent
	{
	    label = new LabelWidget(this, ((QBNodeComponent)node).getNodeName());
	    componentWidget = new ComponentWidget(this, (QBNodeComponent)node);
	}

	label.setOpaque(true);
	label.setBackground(Color.LIGHT_GRAY);
	widget.addChild(label);

	widget.addChild(componentWidget);

	mainLayer.addChild (widget);
	return widget;
    }

    protected Widget attachEdgeWidget (Object edge) {
	ConnectionWidget connectionWidget = new ConnectionWidget (this);
//	connectionWidget.setRouter(router);
        connectionLayer.addChild (connectionWidget);
        return connectionWidget;
    }

    protected void attachEdgeSourceAnchor (Object edge, Object oldSourceNode, Object sourceNode) {
        ((ConnectionWidget) findWidget (edge)).setSourceAnchor (AnchorFactory.createRectangularAnchor (findWidget (sourceNode)));
    }

    protected void attachEdgeTargetAnchor (Object edge, Object oldTargetNode, Object targetNode) {
        ((ConnectionWidget) findWidget (edge)).setTargetAnchor (AnchorFactory.createRectangularAnchor (findWidget (targetNode)));
    }

    public LayerWidget getMainLayer () {
        return mainLayer;
    }

    public LayerWidget getConnectionLayer () {
        return connectionLayer;
    }

    // Create a node using the contents of the QueryBuilderTable
    public Widget addNode(String nodeName, QueryBuilderTableModel qbTableModel)
    {
	QBNodeComponent qbNC = new QBNodeComponent(nodeName, qbTableModel);
	return this.addNode(qbNC);
    }

    private static class MyHoverProvider implements TwoStateHoverProvider {

	public void unsetHovering (Widget widget) {
	    widget.setBackground (Color.WHITE);
	}

	public void setHovering (Widget widget) {
	    widget.setBackground (Color.CYAN);
	}
    }

//     private static class MyPopupMenuProvider implements PopupMenuProvider {

// 	public JPopupMenu getPopupMenu (Widget widget, Point localLocation) {
// 	    JPopupMenu popupMenu = new JPopupMenu ();
// 	    popupMenu.add (new JMenuItem ("Open " + ((UMLClassWidget) widget).getClassName ()));
// 	    return popupMenu;
// 	}
//     }

}
