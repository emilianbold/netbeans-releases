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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Point;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author jsandusky
 */
public class CasaPopupMenuProvider implements PopupMenuProvider {
    
    private Widget mWidget;
    private CasaModelGraphScene mScene;
    private CasaWrapperModel mModel;
    
    
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        mWidget = widget;
        mScene = (CasaModelGraphScene) widget.getScene();
        mModel = mScene.getModel();
        
        Node node = null;
        if (widget instanceof CasaModelGraphScene) {
            node = mScene.getNodeFactory().createModelNode(mModel);
        } else {
            Object widgetData = mScene.findObject(mWidget);
            if (widgetData instanceof CasaComponent) {
                node = mScene.getNodeFactory().createNodeFor((CasaComponent) widgetData);
            }
        }
        
        boolean hasActions = false;
        JPopupMenu popupMenu = new JPopupMenu();
        
        if (node instanceof CasaNode) {
            CasaNode casaNode = (CasaNode) node;
            Action[] actions = node.getActions(true);
            for (Action action : actions) {
                Point sceneLocation = widget.convertLocalToScene(localLocation);
                if (casaNode.isValidSceneActionForLocation(action, widget, sceneLocation)) {
                    if (action instanceof AbstractAction) {
                        popupMenu.add(action);
                        hasActions = true;
                    } else if (action instanceof NodeAction) {
                        // Cannot add a NodeAction directly to a popup menu.
                        JMenuItem menuItem = new JMenuItem();
                        popupMenu.add(menuItem);
                        Actions.connect(menuItem, action, true);
                        hasActions = true;
                    } else if (action == null) {
                        popupMenu.addSeparator();
                    }
                }
            }
        }
        
        return hasActions ? popupMenu : null;
    }
}
