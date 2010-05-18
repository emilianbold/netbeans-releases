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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;

/**
 * Taken from David Kaspar's PopupMenuAction.
 * Slight modification to change the selection before opening the popup.
 *
 * @author Josh Sandusky
 */
public class CasaPopupMenuAction extends WidgetAction.Adapter {
    
    private PopupMenuProvider provider;

    
    public CasaPopupMenuAction (PopupMenuProvider provider) {
        this.provider = provider;
    }

    
    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  Delegates check code
     * to {@link #handleMouseEvent(Widget, WidgetMouseEvent)}.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #handleMouseEvent(Widget, WidgetMouseEvent)
     */
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  Delegates check code
     * to {@link #handleMouseEvent(Widget, WidgetMouseEvent)}.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #handleMouseEvent(Widget, WidgetMouseEvent)
     */
    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }
    
    protected State handleMouseEvent (Widget widget, WidgetMouseEvent event) {
        // Different OSes use different MouseEvents (Pressed/Released) to
        // signal that an event is a PopupTrigger.  So, the mousePressed(...)
        // and mouseReleased(...) methods delegate to this method to
        // handle the MouseEvent.
        State retState = State.REJECTED;
        if (event.isPopupTrigger ()) {
            retState = bringPopupMenu(widget, event.getPoint());
        }
        return retState;
    }
    
    private State bringPopupMenu(Widget widget, Point localLocation) {
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        Object widgetData = null;
        if (widget instanceof CasaModelGraphScene) {
            widgetData = scene.getModel();
        } else {
            widgetData = scene.findObject(widget);
        }
        if (
                !(widgetData instanceof CasaComponent) &&
                !(widgetData instanceof CasaWrapperModel)) {
            return State.REJECTED;
        }

        // First select the widgets, fire necessary selection events.
        if (widgetData instanceof CasaComponent) {
            CasaComponent component = (CasaComponent) widgetData;
            Set<CasaComponent> objectsToSelect = new HashSet<CasaComponent>();
            objectsToSelect.add((CasaComponent) widgetData);
            scene.userSelectionSuggested(objectsToSelect, false);
        }

        JPopupMenu popupMenu = provider.getPopupMenu (widget, localLocation);
        if (popupMenu != null) {
            Point point = 
                    scene.convertSceneToView (widget.convertLocalToScene(localLocation));
            popupMenu.show(scene.getView(), point.x, point.y);
        }
        return State.CONSUMED;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        State retState = State.REJECTED;
        //widget = widget.getScene().getFocusedWidget();
        if ((event.getModifiers () & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK  &&  event.getKeyCode () == KeyEvent.VK_F10) {
            Point location = new Point();
            location.setLocation(widget.getBounds().getCenterX(), widget.getBounds().getY());
            retState = bringPopupMenu(widget, location);
            //retState = bringPopupMenu(widget, widget.getBounds().getLocation());
        }
        return retState;
    }

}
