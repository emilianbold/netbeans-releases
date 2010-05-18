/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.RectangularSelectDecorator;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.ZoomManager;

/**
 *
 * @author Sheryl Su
 * this class is copied from graph library with slight modification in mouse pressed logic to detect
 * max zoom factor
 */
public class UMLRectangularSelectAction extends WidgetAction.LockedAdapter {

    private RectangularSelectDecorator decorator;
    private LayerWidget interractionLayer;
    private RectangularSelectProvider provider;

    private Widget selectionWidget;
    private Rectangle selectionSceneRectangle;

    public UMLRectangularSelectAction (RectangularSelectDecorator decorator, LayerWidget interractionLayer, RectangularSelectProvider provider) {
        this.decorator = decorator;
        this.interractionLayer = interractionLayer;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return selectionSceneRectangle != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (widget.getScene().getZoomFactor() >= ZoomManager.MAX_ZOOM_PERCENT/100)
            return State.REJECTED;
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            selectionWidget = decorator.createSelectionWidget ();
            assert selectionWidget != null;
            interractionLayer.addChild (selectionWidget);
            selectionSceneRectangle = new Rectangle (widget.convertLocalToScene (event.getPoint ()));
            move (widget, event.getPoint ());
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (selectionSceneRectangle != null) {
            move (widget, event.getPoint ());
            selectionWidget.getParentWidget ().removeChild (selectionWidget);
            provider.performSelection (selectionSceneRectangle);
            selectionWidget = null;
            selectionSceneRectangle = null;
        }
        return State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        if (selectionSceneRectangle != null) {
            move (widget, event.getPoint ());
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    private void resolveSelectionWidgetLocationBounds () {
        selectionWidget.setPreferredLocation (selectionSceneRectangle.getLocation ());
        int w = selectionSceneRectangle.width;
        int h = selectionSceneRectangle.height;
        selectionWidget.setPreferredBounds (new Rectangle (w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h));
    }

    private void move (Widget widget, Point newLocation) {
        Point sceneLocation = widget.convertLocalToScene (newLocation);
        selectionSceneRectangle.width = sceneLocation.x - selectionSceneRectangle.x;
        selectionSceneRectangle.height = sceneLocation.y - selectionSceneRectangle.y;
        resolveSelectionWidgetLocationBounds ();
    }

}
