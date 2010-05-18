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
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.DnDHandler;

/**
 *  
 * Action for drag and drop. Implement ConnectProvider isTargetWidget, isSourceWidget and createConnection.
 * Modified version of ConnectAction.
 *
 * @author David Kaspar
 * @author skini
 */
public class DragDropAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private DragDropDecorator decorator;
    private Widget interractionLayer;
    private ConnectProvider provider;

    private Widget dragWidget = null;
    private Widget sourceWidget = null;
    private Widget targetWidget = null;
    private Point startingPoint = null;
    private Cursor cursor = null;

    public DragDropAction (DragDropDecorator decorator, Widget interractionLayer, ConnectProvider provider) {
        this.decorator = decorator;
        this.interractionLayer = interractionLayer;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return sourceWidget != null;
    }

    public WidgetAction.State mousePressed (Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (isLocked ())
            return WidgetAction.State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
            if (provider.isSourceWidget (widget)) {
                sourceWidget = widget;
                targetWidget = null;
                startingPoint = new Point (event.getPoint ());
                dragWidget = decorator.createDragWidget (interractionLayer.getScene ());
                assert dragWidget != null;
                Point scenePoint = widget.convertLocalToScene(event.getPoint());
                scenePoint.x +=16;
                dragWidget.setPreferredLocation(interractionLayer.convertSceneToLocal(scenePoint));
                interractionLayer.addChild (dragWidget);
                cursor = widget.getScene().getView().getCursor();
                return WidgetAction.State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    public WidgetAction.State mouseReleased (Widget widget, WidgetAction.WidgetMouseEvent event) {
        Point point = event.getPoint ();
        boolean state = move (widget, point);
        if (state) {
            if (targetWidget != null)
                if (Math.abs (startingPoint.x - point.x) >= MIN_DIFFERENCE  ||  Math.abs (startingPoint.y - point.y) >= MIN_DIFFERENCE)
                    provider.createConnection (sourceWidget, targetWidget);
        }
        cancel ();
        return state ? State.CONSUMED : State.REJECTED;
    }

    private void cancel () {
        sourceWidget = null;
        targetWidget = null;
        startingPoint = null;
        dragWidget.getScene().getView().setCursor(cursor);
        dragWidget.removeFromParent();
        dragWidget = null;
    }

    public WidgetAction.State mouseDragged (Widget widget, WidgetAction.WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point point) {
        if (sourceWidget != widget)
            return false;

        Point targetSceneLocation = widget.convertLocalToScene (point);
        targetWidget = resolveTargetWidgetCore (interractionLayer.getScene (), targetSceneLocation);
        Point pt = new Point(targetSceneLocation);
        pt.x += 16;
        dragWidget.setPreferredLocation(interractionLayer.convertSceneToLocal(pt));
        
        return true;
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
        
        ConnectorState state = provider.isTargetWidget (sourceWidget, widget);
        if (state == ConnectorState.REJECT) {
            if (widget instanceof DnDHandler) {
                DnDHandler handler = (DnDHandler) widget;
                if (handler.isCollapsed()) handler.expandForDragAndDrop();
            }
            widget.getScene().getView().setCursor(DragSource.DefaultMoveNoDrop);
            return false;
        }
        if (state == ConnectorState.ACCEPT) {
            widget.getScene().getView().setCursor(DragSource.DefaultMoveDrop);
            result[0] = widget;
        }
        return true;
    }


    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        if (isLocked ()  &&  event.getKeyCode () == KeyEvent.VK_ESCAPE) {
            cancel ();
            return State.CONSUMED;
        }
        return super.keyPressed (widget, event);
    }

}
