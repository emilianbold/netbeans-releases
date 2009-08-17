/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
