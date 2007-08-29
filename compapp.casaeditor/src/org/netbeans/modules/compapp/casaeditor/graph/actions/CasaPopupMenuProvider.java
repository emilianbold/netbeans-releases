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
import java.awt.event.ActionEvent;
import javax.swing.*;

import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.CanvasNodeProxyContext;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

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
            Action lastActionAdded = null;
            for (Action action : actions) {
                Point sceneLocation = widget.convertLocalToScene(localLocation);
                if (casaNode.isValidSceneActionForLocation(action, widget, sceneLocation)) {
                    if (action instanceof AbstractAction) {
                        if (action instanceof Presenter.Popup) {
                            JMenuItem mi = ((Presenter.Popup) action).getPopupPresenter();
                            if (mi instanceof JMenu) {
                                popupMenu.add((JMenu) mi);
                            } else {
                                popupMenu.add(mi);
                            }
                        } else { // simple abstract action...
                            CanvasNodeActionProxy proxy = CanvasNodeActionProxy.createAction(
                                    action,
                                    sceneLocation,
                                    localLocation);
                            popupMenu.add(proxy);
                        }
                        hasActions = true;
                        lastActionAdded = action;
                    } else if (action instanceof NodeAction) {
                        // Cannot add a NodeAction directly to a popup menu.
                        JMenuItem menuItem = new JMenuItem();
                        popupMenu.add(menuItem);
                        Actions.connect(menuItem, action, true);
                        hasActions = true;
                        lastActionAdded = action;
                    } else if (action == null && lastActionAdded != null) {
                        popupMenu.addSeparator();
                        lastActionAdded = null;
                    }
                }
            }
        }

        return hasActions ? popupMenu : null;
    }


    private static class CanvasNodeActionProxy extends AbstractAction implements CanvasNodeProxyContext {
        private Action mActionable;
        private Point mLocalLocation;
        private Point mSceneLocation;


        private CanvasNodeActionProxy(
                Action actionable,
                Point sceneLocation,
                Point localLocation)
        {
            super(
                    (String) actionable.getValue(Action.NAME),
                    (Icon) actionable.getValue(Action.SMALL_ICON));
            mActionable = actionable;
            mSceneLocation = sceneLocation;
            mLocalLocation = localLocation;
        }


        public static CanvasNodeActionProxy createAction(
                Action actionable,
                Point sceneLocation,
                Point localLocation)
        {
            return new CanvasNodeActionProxy(actionable, sceneLocation, localLocation);
        }

        public Point getLocalLocation() {
            return mLocalLocation;
        }

        public Point getSceneLocation() {
            return mSceneLocation;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            actionEvent.setSource(this);
            mActionable.actionPerformed(actionEvent);
        }
    }
}
