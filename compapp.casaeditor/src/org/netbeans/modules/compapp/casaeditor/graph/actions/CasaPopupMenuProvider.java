/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
