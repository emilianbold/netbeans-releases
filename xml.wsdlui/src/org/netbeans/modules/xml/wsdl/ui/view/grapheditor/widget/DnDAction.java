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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class DnDAction extends WidgetAction.Adapter {

    
    private DnDHandler currentHandler = null;
    private long lastId = Long.MIN_VALUE;            
    private long dragEnterTime = -1;
    
    private List<DnDHandler> priorHandlers;
    
    public DnDAction() {
    
    }

    @Override
    public WidgetAction.State dragExit(Widget widget, 
            WidgetAction.WidgetDropTargetEvent event) 
    {
        if (isEventProcessed(event.getEventID())) {
            return WidgetAction.State.REJECTED;
        }
        
        //remove the icon from drag layer.
        ((PartnerScene) widget.getScene()).getDragOverLayer().resetLayer();
        
        
        if (currentHandler != null) {
            currentHandler.dragExit();
            currentHandler = null;
        }

        return WidgetAction.State.REJECTED;
    }

    
    @Override
    public WidgetAction.State dragOver(Widget widget, 
            WidgetAction.WidgetDropTargetDragEvent event) 
    {
        if (isEventProcessed(event.getEventID())) {
            return WidgetAction.State.REJECTED;
        }

        Point scenePoint = widget.convertLocalToScene(event.getPoint());
        
        boolean isEventHandled = false;
        
        //Render a icon on the drag layer.
        ((PartnerScene)widget.getScene()).getDragOverLayer().dragOver(scenePoint, event);
        
        
        //Global prior handler get the first chance to handle all DnD events.
        if (priorHandlers != null && !priorHandlers.isEmpty()) {
            //If a global handler is the currentHandler then use it.
            if (currentHandler != null && priorHandlers.contains(currentHandler)) {
                isEventHandled = currentHandler.dragOver(scenePoint, event);
            }
            
            if (!isEventHandled) {
                for (DnDHandler handler : priorHandlers) {
                    if (handler.dragOver(scenePoint, event)) {
                        if (currentHandler != null && currentHandler != handler) {
                            currentHandler.dragExit();
                        }
                        currentHandler = handler;
                        isEventHandled = true;
                        break;
                    }
                }
            }
        }
        
        if (!isEventHandled) {
            if (widget == currentHandler) {
                if (dragEnterTime >= 0) {
                    if (System.currentTimeMillis() - dragEnterTime 
                            >= EXPAND_TIMEOUT) 
                    {
                        currentHandler.expandForDragAndDrop();
                        dragEnterTime = -1;
                    }
                }
                isEventHandled = currentHandler.dragOver(scenePoint, event);
            } else if (widget instanceof DnDHandler) {
                if (currentHandler != null) {
                    currentHandler.dragExit();
                } 

                currentHandler = (DnDHandler) widget;
                if (currentHandler.isCollapsed()) {
                    dragEnterTime = System.currentTimeMillis();
                } else {
                    dragEnterTime = -1;
                }

                isEventHandled = currentHandler.dragOver(scenePoint, event);
            } else if (currentHandler != null) {
                currentHandler.dragExit();
                currentHandler = null;
                dragEnterTime = -1;
            }
        }
        
        if (isEventHandled) {
            event.acceptDrag(event.getDropAction());
            return WidgetAction.State.CONSUMED;
        }
        
        event.rejectDrag();
        return WidgetAction.State.REJECTED;
    } 

    
    @Override
    public WidgetAction.State dragEnter(Widget widget, 
            WidgetAction.WidgetDropTargetDragEvent event) 
    {
        return dragOver(widget, event);
    }

    
    @Override
    public WidgetAction.State drop(Widget widget, 
            WidgetAction.WidgetDropTargetDropEvent event) 
    {
        if (isEventProcessed(event.getEventID())) {
            return WidgetAction.State.REJECTED;
        }

        Point scenePoint = widget.convertLocalToScene(event.getPoint());
        //remove the icon from drag layer.
        ((PartnerScene)widget.getScene()).getDragOverLayer().resetLayer();
        
        boolean isEventHandled = false;
        if (currentHandler != null) {
            isEventHandled = currentHandler.drop(scenePoint, event);
            currentHandler = null;
        }
        
        if (isEventHandled) {
            event.acceptDrop(event.getDropAction());
            return WidgetAction.State.CONSUMED;
        }
        
        event.rejectDrop();
        return WidgetAction.State.REJECTED;
    }
    
    
    private boolean isEventProcessed(long eventId) 
    {
        if (eventId != lastId) {
            lastId = eventId;
            return false;
        }
        
        return true;
    }
    
    public void addPriorHandler(DnDHandler handler) {
        if (priorHandlers == null) {
            priorHandlers = new ArrayList<DnDHandler>();
        }
        priorHandlers.add(handler);
    }
    
    public void removePriorHandler(DnDHandler handler) {
        if (priorHandlers == null) return;
        priorHandlers.remove(handler);
    }
    
    private static final long EXPAND_TIMEOUT = 1500;
}
