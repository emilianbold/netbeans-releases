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

package org.netbeans.modules.compapp.javaee.sunresources.tool.graph;

import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode.CMapNodeType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode;

import java.awt.Image;
import java.awt.Rectangle;
import java.util.Hashtable;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.netbeans.api.visual.action.WidgetAction;

/**
 * main class for graphical representation of connectivity map
 * this where it maintains the widgets and mapping structures
 * 
 * @author echou
 *
 */
public class CMapScene extends Scene {
    
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    
    // mapping of graphical node to actually model node
    private Hashtable<Widget, CMapNode> nodeMap;
    
    public CMapScene(String title) {
        nodeMap = new Hashtable<Widget, CMapNode> ();
        
        addChild(new LabelWidget(this, title));
        
        mainLayer = new LayerWidget (this);
        addChild(mainLayer);
        connectionLayer = new LayerWidget (this);
        addChild(connectionLayer);
        
    }

    public Widget addCMapNode(CMapNode node, Image image) {
        IconNodeWidget widget = new IconNodeWidget (this);
        widget.setImage (image);
        
        String nodeName = null;
        if (node.getType() == CMapNodeType.RESOURCE) {
            nodeName = ((ResourceNode) node).getLogicalName();
        } else {
            nodeName = node.getLogicalName();
        }
        nodeMap.put(widget, node);
        
        widget.setLabel(nodeName);
        widget.setPreferredBounds(new Rectangle(100, 25));
        
        mainLayer.addChild(widget);
        return widget;
    }


    public Widget addCMapEdge (String edgeName, Widget source, Widget target) {
        ConnectionWidget edge = new ConnectionWidget(this);
        edge.setSourceAnchor(AnchorFactory.createCircularAnchor(source, 32));
        edge.setTargetAnchor(AnchorFactory.createCircularAnchor(target, 32));
        edge.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        edge.setRouter(RouterFactory.createOrthogonalSearchRouter(
                mainLayer, connectionLayer));
        connectionLayer.addChild(edge);
        
        return edge;
    }


    public void registerNodeWidgetAction(WidgetAction a) {
        for (Widget w : mainLayer.getChildren()) {
            w.getActions().addAction(a);
        }
    }

    public void registerEdgeWidgetAction(WidgetAction a) {
        for (Widget w : connectionLayer.getChildren()) {
            w.getActions().addAction(a);
        }
    }

    public void repaintScene() {
        super.repaint();
    }

    public CMapNode findNode(Widget w) {
        return this.nodeMap.get(w);
    }
    
}
