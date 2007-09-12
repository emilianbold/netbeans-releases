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
package org.netbeans.modules.compapp.casaeditor.graph.actions;

import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaConnectionWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaPinWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;

/**
 * This is a version of ConnectAction that causes the connection
 * to be attempted on mouseDragged, not on mousePressed.
 *
 * @author David Kaspar
 */
public class CasaConnectAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private ConnectDecorator decorator;
    private Widget interractionLayer;
    private ConnectProvider provider;

    private ConnectionWidget dragConnectionWidget;
    private Widget connectionSourceWidget;
    private Widget connectionTargetWidget;
    private Widget mLastHoverWidget;
    private Point startingPoint;
    
    private CasaModelGraphScene mScene;
    private boolean mIsCommitted;
    private boolean mIsConnectionHintsSet;
    

    public CasaConnectAction(CasaModelGraphScene casaScene, Widget interractionLayer) {
        
        mScene = casaScene;
        
        this.decorator = new ConnectDecorator() {
            public ConnectionWidget createConnectionWidget(Scene scene) {
                ConnectionWidget widget = new CasaConnectionWidget(scene);
                widget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
                return widget;
            }
            public Anchor createSourceAnchor(Widget sourceWidget) {
                assert sourceWidget instanceof CasaPinWidget;
                return ((CasaPinWidget) sourceWidget).getAnchor();
            }
            public Anchor createTargetAnchor(Widget targetWidget) {
                assert targetWidget instanceof CasaPinWidget;
                return ((CasaPinWidget) targetWidget).getAnchor();
            }
            public Anchor createFloatAnchor(Point location) {
                return AnchorFactory.createFixedAnchor(location);
            }
        };
    
        this.provider = new ConnectProvider() {
            
            public void createConnection(Widget sourceWidget, Widget targetWidget) {
                ConsumesProvides info = new ConsumesProvides(mScene, sourceWidget, targetWidget);
                if (info.mConsumes != null && info.mProvides != null) {
                    for (CasaConnection connection : mScene.getModel().getCasaConnectionList(false)) {
                        CasaEndpointRef iterConsumes = mScene.getModel().getCasaEndpointRef(connection, true);
                        CasaEndpointRef iterProvides = mScene.getModel().getCasaEndpointRef(connection, false);
                        if (iterConsumes == null || iterProvides == null) {
                            continue;
                        }
                        if (iterConsumes.equals(info.mConsumes) && iterProvides.equals(info.mProvides)) {
                            // link already exists, cannot create an overlapping link
                            return;
                        }
                    }
                    mScene.getModel().addConnection(info.mConsumes, info.mProvides, info.getConsumesToProvidesDirection());
                }
            }
    
            public boolean hasCustomTargetWidgetResolver(Scene scene) {
                return false;
            }

            public boolean isSourceWidget(Widget widget) {
                return widget instanceof CasaPinWidget;
            }

            public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
                if (targetWidget instanceof CasaPinWidget && sourceWidget != targetWidget) {
                    return ConnectorState.ACCEPT;
                }
                return ConnectorState.REJECT;
            }

            public Widget resolveTargetWidget(Scene scene, Point point) {
                return null;
            }
        };
        
        this.interractionLayer = interractionLayer;
    }

    
    protected boolean isLocked () {
        return connectionSourceWidget != null;
    }

    public WidgetAction.State mousePressed (Widget widget, WidgetAction.WidgetMouseEvent event) {
        mIsCommitted = false;
        if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
            if (provider.isSourceWidget (widget)) {
                connectionSourceWidget = widget;
                connectionTargetWidget = null;
                startingPoint = new Point (event.getPoint ());
                dragConnectionWidget = decorator.createConnectionWidget (interractionLayer.getScene ());
                assert dragConnectionWidget != null;
                dragConnectionWidget.setSourceAnchor (decorator.createSourceAnchor (widget));
                // Do not lock the state nor consume the event because we have
                // not yet committed to drawing a connection. The user has only
                // pressed the mouse button - which does not necessarily mean
                // we want to create a link (it instead could be a select). 
                // If the user then drags the mouse, then we commit ourselves 
                // to attempting to create a conneciton.
                return State.CHAIN_ONLY;
            }
        } else {
            // The wrong button, or an incorrect number of clicks.
            // Cleanup our connection state if any exists.
            cleanup();
        }
        return State.REJECTED;
    }

    public WidgetAction.State mouseReleased (Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (connectionSourceWidget == null || widget != connectionSourceWidget) {
            return State.REJECTED;
        }
        Point point = event.getPoint ();
        move((CasaPinWidget) widget, point);
        if (
                Math.abs (startingPoint.x - point.x) >= MIN_DIFFERENCE  ||  
                Math.abs (startingPoint.y - point.y) >= MIN_DIFFERENCE) {
            provider.createConnection (connectionSourceWidget, connectionTargetWidget);
        }
        
        cleanup();
        
        return State.CONSUMED;
    }

    private void cleanup() {
        connectionSourceWidget = null;
        connectionTargetWidget = null;
        startingPoint = null;
        if (dragConnectionWidget != null) {
            dragConnectionWidget.setSourceAnchor(null);
            dragConnectionWidget.setTargetAnchor(null);
            if (interractionLayer.getChildren().contains(dragConnectionWidget)) {
                interractionLayer.removeChild (dragConnectionWidget);
            }
        }
        dragConnectionWidget = null;
        mIsCommitted = false;
        
        updateConnectionHints();
        
        ConnectionHintManager.sharedInstance().cleanup();
    }
    
    public WidgetAction.State mouseDragged (Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (connectionSourceWidget == null || widget != connectionSourceWidget) {
            return State.REJECTED;
        }
        
        if (!mIsCommitted) {
            // The user has dragged the mouse following the initial
            // mouse press - this commits us to attempting to create
            // a connection.
            interractionLayer.addChild (dragConnectionWidget);
            mIsCommitted = true;
        }
        
        move((CasaPinWidget) widget, event.getPoint ());
        
        updateConnectionHints();
        
        return State.createLocked (widget, this);
    }

    private void move (CasaPinWidget widget, Point point) {
        Point targetSceneLocation = widget.convertLocalToScene (point);
        // Do not allow the rectangle to extend into negative scene space.
        if (targetSceneLocation.x < 0) {
            targetSceneLocation.x = 0;
        }
        if (targetSceneLocation.y < 0) {
            targetSceneLocation.y = 0;
        }
        
        connectionTargetWidget = findTargetWidget(widget, targetSceneLocation);
        
        Anchor targetAnchor = null;
        if (connectionTargetWidget != null)
            targetAnchor = decorator.createTargetAnchor (connectionTargetWidget);
        if (targetAnchor == null)
            targetAnchor = decorator.createFloatAnchor (targetSceneLocation);
        dragConnectionWidget.setTargetAnchor (targetAnchor);
    }

    // For the given mouse scene location and drag source widget, determine
    // if we are hovering over a valid widget and if so, determine if we can
    // connect to such a widget. Show any connection hints if present.
    private Widget findTargetWidget(CasaPinWidget widget, Point targetSceneLocation) {
        CasaPinWidget hoverTargetWidget = (CasaPinWidget) resolveTargetWidgetCore(
                interractionLayer.getScene(),
                targetSceneLocation);
        
        // Handle connection hints.
        if (hoverTargetWidget != mLastHoverWidget) {
            if (mLastHoverWidget != null) {
                ConnectionHintManager.sharedInstance().widgetExited();
            }
            if (hoverTargetWidget != null) {
                ConnectionHintManager.sharedInstance().widgetEntered(widget, hoverTargetWidget);
            }
        }
        if (hoverTargetWidget != null) {
            ConnectionHintManager.sharedInstance().widgetMovedOver(widget, hoverTargetWidget);
        }
        mLastHoverWidget = hoverTargetWidget;
        
        // Determine if the connection is valid.
        if (hoverTargetWidget != null) {
            ConsumesProvides info = new ConsumesProvides(mScene, widget, hoverTargetWidget);
            if (
                    info.mConsumes != null && 
                    info.mProvides != null && 
                    mScene.getModel().canConnect(info.mConsumes, info.mProvides))
            {
                return hoverTargetWidget;
            }
        }
        
        return null;
    }
    
    private Widget resolveTargetWidgetCore (Scene scene, Point sceneLocation) {
        if (provider != null)
            if (provider.hasCustomTargetWidgetResolver (scene))
                return provider.resolveTargetWidget (scene, sceneLocation);
        Point sceneOrigin = scene.getLocation ();
        sceneLocation = new Point (sceneLocation.x + sceneOrigin.x, sceneLocation.y + sceneOrigin.y);
        Widget[] result = new Widget[]{null};
        resolveTargetWidgetCoreDive (result, scene, sceneLocation);
        return result[0];
    }

    private boolean resolveTargetWidgetCoreDive (Widget[] result, Widget widget, Point parentLocation) {
        if (interractionLayer.equals (widget))
            return false;
        Point widgetLocation = widget.getLocation ();
        Point location = new Point (parentLocation.x - widgetLocation.x, parentLocation.y - widgetLocation.y);

        if (! widget.getBounds ().contains (location))
            return false;

        java.util.List<Widget> children = widget.getChildren ();
        for (int i = children.size () - 1; i >= 0; i --) {
            if (resolveTargetWidgetCoreDive (result, children.get (i), location))
                return true;
        }

        if (! widget.isHitAt (location))
            return false;

        ConnectorState state = provider.isTargetWidget (connectionSourceWidget, widget);
        if (state == ConnectorState.REJECT)
            return false;
        if (state == ConnectorState.ACCEPT)
            result[0] = widget;
        return true;
    }

    private void updateConnectionHints() {
        if (
                connectionSourceWidget != null && 
                mIsCommitted &&
                !mIsConnectionHintsSet) {
            mIsConnectionHintsSet = true;
            updatePinHighlights((CasaEndpointRef) mScene.findObject(connectionSourceWidget));
        } else if (connectionSourceWidget == null) {
            mIsConnectionHintsSet = false;
            updatePinHighlights(null);
        }
    }
    
    private void updatePinHighlights(CasaEndpointRef srcRef) {
        for (CasaComponent node : mScene.getNodes()) {
            if (
                    node instanceof CasaPort ||
                    node instanceof CasaServiceEngineServiceUnit) {
                for (CasaComponent pin : mScene.getNodePins(node)) {
                    boolean isHighlighted = false;
                    CasaPinWidget targPin = (CasaPinWidget) mScene.findWidget(pin);
                    if (srcRef != null) {
                        CasaEndpointRef targRef = (CasaEndpointRef) pin;
                        if (mScene.getModel().canConnect(srcRef, targRef)) {
                            isHighlighted = true;
                        }
                    }
                    targPin.setHighlighted(isHighlighted);
                }
            }
        }
    }

    
    
    // Simply provides a convenient way of grabbing
    // Consumes/Provides info from the pin widgets.
    private static class ConsumesProvides {
        
        CasaConsumes mConsumes;
        CasaProvides mProvides;
        boolean mConsumesToProvidesDirection = true;
        
        public ConsumesProvides(CasaModelGraphScene scene, Widget src, Widget targ) {
            if (
                    src instanceof CasaPinWidget &&
                    targ instanceof CasaPinWidget) {
                Object sourceObject = scene.findObject(src);
                Object targetObject = scene.findObject(targ);
                if (sourceObject instanceof CasaConsumes) {
                    mConsumes = (CasaConsumes) sourceObject;
                    mConsumesToProvidesDirection = true;
                } else if (sourceObject instanceof CasaProvides) {
                    mProvides = (CasaProvides) sourceObject;
                    mConsumesToProvidesDirection = false;
                }
                if (targetObject instanceof CasaConsumes) {
                    mConsumes = (CasaConsumes) targetObject;
                    mConsumesToProvidesDirection = false;
                } else if (targetObject instanceof CasaProvides) {
                    mProvides = (CasaProvides) targetObject;
                    mConsumesToProvidesDirection = true;
                }
            }
        }
        public boolean getConsumesToProvidesDirection() {
            return mConsumesToProvidesDirection;
        }
    }
}
