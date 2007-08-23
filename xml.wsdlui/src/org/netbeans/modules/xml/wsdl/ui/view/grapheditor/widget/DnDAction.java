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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Point;
//import java.util.logging.Level;
//import java.util.logging.Logger;

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
//    private Logger mLogger = Logger.getLogger(DnDAction.class.getName());
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
        
        //Render a icon on the drag layer.
        ((PartnerScene)widget.getScene()).getDragOverLayer().dragOver(scenePoint, event);
        
        boolean isEventHandled = false;
        if (widget == currentHandler) {
            if (dragEnterTime >= 0) {
                if (System.currentTimeMillis() - dragEnterTime 
                        >= EXPAND_TIMEOUT) 
                {
                	
//                	mLogger.log(Level.SEVERE, currentHandler.toString() + " : Expanding");
                    currentHandler.expandForDragAndDrop();
//                    mLogger.log(Level.SEVERE, currentHandler.toString() + " : Expanded");
                    dragEnterTime = -1;
                }
            }
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragOver start");
            isEventHandled = currentHandler.dragOver(scenePoint, event);
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragOver end");
        } else if (widget instanceof DnDHandler) {
            if (currentHandler != null) {
//            	mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragExit start");
                currentHandler.dragExit();
//                mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragExit end");
            } 
            
            currentHandler = (DnDHandler) widget;
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : New CurrentHandler");
            if (currentHandler.isCollapsed()) {
                dragEnterTime = System.currentTimeMillis();
            } else {
                dragEnterTime = -1;
            }
            
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragOver start");
            isEventHandled = currentHandler.dragOver(scenePoint, event);
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragOver end");
        } else if (currentHandler != null) {
//        	mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragExit start");
            currentHandler.dragExit();
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : DragExit end");
            currentHandler = null;
            dragEnterTime = -1;
        }
        if (isEventHandled) {
            event.acceptDrag(event.getDropAction());
//            mLogger.log(Level.SEVERE, currentHandler.toString() + " : EventAccepted");
            return WidgetAction.State.CONSUMED;
        }
        
        event.rejectDrag();
//        mLogger.log(Level.SEVERE, currentHandler.toString() + " : EventRejected");
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
    
    private static final long EXPAND_TIMEOUT = 1500;
}
