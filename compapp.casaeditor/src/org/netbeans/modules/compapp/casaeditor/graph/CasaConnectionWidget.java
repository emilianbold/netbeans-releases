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
package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;


import java.beans.PropertyChangeListener;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaPopupMenuAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaQoSEditAction;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.ConnectionNode;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.ClearConfigExtensionsAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * 
 * @author jqian
 */
public class CasaConnectionWidget extends ConnectionWidget implements CasaMinimizable {

    private static final Stroke STROKE_DEFAULT = new BasicStroke(1.0f);
    private static final Stroke STROKE_HOVERED = new BasicStroke(1.5f);
    private static final Stroke STROKE_SELECTED = new BasicStroke(2.0f);
    private static final Image IMAGE_QOS_BADGE_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/QoS.png");    // NOI18N

    private static final Image IMAGE_UNCONFIGURED_QOS_BADGE_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/UnConfiguredQoS.png");    // NOI18N

    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    private Widget mQoSWidget;
    private Widget mUnConfiguredQoSWidget;
    
    public CasaConnectionWidget(Scene scene) {
        super(scene);
        setSourceAnchorShape(AnchorShape.NONE);
        setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        setPaintControlPoints(true);
        setState(ObjectState.createNormal());

        initQoSWidgets();
    }
    
    public void updateQoSWidgets() {

        ObjectScene objectScene = (ObjectScene) getScene();
        final CasaConnection myCasaConnection =
                (CasaConnection) objectScene.findObject(CasaConnectionWidget.this);
        updateQoSWidgets(myCasaConnection);
    }

    private void initQoSWidgets() {

        mQoSWidget = new ImageWidget(getScene(), IMAGE_QOS_BADGE_ICON);
        mUnConfiguredQoSWidget =
                new ImageWidget(getScene(), IMAGE_UNCONFIGURED_QOS_BADGE_ICON);

        CasaQoSEditAction qosEditAction =
                new CasaQoSEditAction((CasaModelGraphScene) getScene());

        mQoSWidget.getActions().addAction(qosEditAction);
        mUnConfiguredQoSWidget.getActions().addAction(qosEditAction);

        CasaPopupMenuAction popupMenuAction = new CasaPopupMenuAction(new PopupMenuProvider() {

            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                JPopupMenu popupMenu = new JPopupMenu();

                CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
                CasaConnection casaConnection =
                        (CasaConnection) scene.findObject(CasaConnectionWidget.this);
                CasaNode node = scene.getNodeFactory().createNodeFor(casaConnection);

                ClearConfigExtensionsAction clearQoSAction = new ClearConfigExtensionsAction(
                        NbBundle.getMessage(ConnectionNode.class,
                        "CLEAR_QOS_CONFIG"), // NOI18N
                        node);
                popupMenu.add(clearQoSAction.getPopupPresenter());
                return popupMenu;
            }
        });
        mQoSWidget.getActions().addAction(popupMenuAction);
        //mUnConfiguredQoSWidget.getActions().addAction(popupMenuAction); 


        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ObjectScene objectScene = (ObjectScene) getScene();
                final CasaConnection myCasaConnection =
                        (CasaConnection) objectScene.findObject(CasaConnectionWidget.this);
                //System.out.println("obj for connection is " + myCasaConnection);
                if (myCasaConnection == null) {
                    return; // FIXME

                }

                updateQoSWidgets(myCasaConnection);

                myCasaConnection.getModel().addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        CasaConnection casaConnection = null;

                        Object source = evt.getSource();
                        if (source instanceof CasaExtensibilityElement) {
                            CasaComponent parent = (CasaExtensibilityElement) source;
                            while (parent != null &&
                                    parent instanceof CasaExtensibilityElement) {
                                parent = parent.getParent();
                            }

                            if (parent == null || !(parent instanceof CasaConnection)) {
                                return;
                            }

                            casaConnection = (CasaConnection) parent;
                        } else if (source instanceof CasaConnection) {
                            casaConnection = (CasaConnection) source;
                        }

                        if (casaConnection == myCasaConnection) {
                            updateQoSWidgets(myCasaConnection);
                        }
                    }
                });
            }
        });
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged(ObjectState previousState, ObjectState state) {
        Stroke stroke;
        Color fgColor;

        CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
        if (state.isSelected() || state.isFocused()) {
            bringToFront();
            stroke = STROKE_SELECTED;
            fgColor = customizer.getCOLOR_SELECTION();
        } else if (state.isHovered() || state.isHighlighted()) {
            bringToFront();
            stroke = STROKE_HOVERED;
            fgColor = customizer.getCOLOR_HOVERED_EDGE();
        } else {
            stroke = STROKE_DEFAULT;
            fgColor = customizer.getCOLOR_CONNECTION_NORMAL();
        }

        setStroke(stroke);
        setForeground(fgColor);
    }

    public void setForegroundColor(Color color) {
        setForeground(color);
    }

    @Override
    protected void notifyAdded() {
        super.notifyAdded();

        // Update the error badge location if the widget moves.
        Widget.Dependency errorDependency = new Widget.Dependency() {

            public void revalidateDependency() {
                if (getBounds() == null) {
                    return;
                }

                Anchor sourceAnchor = getSourceAnchor();
                if (sourceAnchor == null) {
                    return;
                }

                Widget sourceWidget = sourceAnchor.getRelatedWidget();
                if (sourceWidget == null) {
                    return;
                }

                Point p = sourceAnchor.getRelatedSceneLocation();
                int x = p.x + sourceWidget.getBounds().width / 2 + 10;
                int y = p.y - 6;

                /*CasaConnectionWidget.this.getControlPoints().size();
                CasaNodeWidgetEngine sesuWidget = 
                (CasaNodeWidgetEngine) sourceWidget.getParentWidget().getParentWidget();
                if (sesuWidget.isMinimized()) {
                
                }*/

                mQoSWidget.setPreferredLocation(new Point(x, y));
                mUnConfiguredQoSWidget.setPreferredLocation(new Point(x, y));
            }
        };

        mDependenciesRegistry.registerDependency(errorDependency);
    }

    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();

        mDependenciesRegistry.removeAllDependencies();

        if (mQoSWidget != null) {
            mQoSWidget.removeFromParent();
        }
        if (mQoSWidget != null) {
            mUnConfiguredQoSWidget.removeFromParent();
        }
    }

    private void updateQoSWidgets(CasaConnection casaConnection) {
        boolean needValidation = false;

        if (CasaFactory.getCasaCustomizer().getBOOLEAN_CLASSIC_QOS_STYLE()) {           
            if (isConnectionConfiguredWithQoS(casaConnection)) {
                if (getChildren().contains(mUnConfiguredQoSWidget)) {
                    removeChild(mUnConfiguredQoSWidget);
                    needValidation = true;
                }
                if (!getChildren().contains(mQoSWidget)) {
                    addChild(mQoSWidget);
                    needValidation = true;
                }
            } else {
                if (getChildren().contains(mQoSWidget)) {
                    removeChild(mQoSWidget);
                    needValidation = true;
                }
                if (!getChildren().contains(mUnConfiguredQoSWidget)) {
                    addChild(mUnConfiguredQoSWidget);
                    needValidation = true;
                }
            }
        } else {
            if (getChildren().contains(mUnConfiguredQoSWidget)) {
                removeChild(mUnConfiguredQoSWidget);
                needValidation = true;
            }
            if (getChildren().contains(mQoSWidget)) {
                removeChild(mQoSWidget);
                needValidation = true;
            }
        } 

        if (needValidation) {
            getScene().validate();
        }
    }

    private boolean isConnectionConfiguredWithQoS(CasaConnection casaConnection) {
        return casaConnection.getChildren().size() != 0;
    }

    public void setMinimized(boolean isMinimized) {
        if (isMinimized) {
            if (getChildren().contains(mUnConfiguredQoSWidget)) {
                removeChild(mUnConfiguredQoSWidget);
            }
            if (getChildren().contains(mQoSWidget)) {
                removeChild(mQoSWidget);
            }
        } else {
            ObjectScene objectScene = (ObjectScene) getScene();
            CasaConnection casaConnection =
                    (CasaConnection) objectScene.findObject(CasaConnectionWidget.this);
            if (isConnectionConfiguredWithQoS(casaConnection)) {
                if (!getChildren().contains(mQoSWidget)) {
                    addChild(mQoSWidget);
                }
            } else {
                if (!getChildren().contains(mUnConfiguredQoSWidget)) {
                    addChild(mUnConfiguredQoSWidget);
                }
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    getScene().validate();
                }
            });
        }
    }
}
