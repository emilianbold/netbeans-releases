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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class ButtonAction extends WidgetAction.Adapter {
    
    private ButtonWidget currentButton = null;
    private ButtonWidget pressedButton = null;

    
    private long lastId = Long.MIN_VALUE;
    
    
    public ButtonAction() {
        
    }

    
    
    private boolean isProcessed(WidgetAction.WidgetMouseEvent event) {
        long currentId = event.getEventID();
        
        if (currentId != lastId) {
            lastId = currentId;
            return false;
        }
        
        return true;
    }
    
    
    public WidgetAction.State mouseReleased(Widget widget, 
            WidgetAction.WidgetMouseEvent event) 
    {
        if (isProcessed(event)) {
            return State.REJECTED;
        }
        
        if (pressedButton != null) {
            pressedButton.mouseReleased(pressedButton == currentButton);
            pressedButton = null;
        }
        
        return State.REJECTED;
    }

    
    public WidgetAction.State mousePressed(Widget widget, 
            WidgetAction.WidgetMouseEvent event) 
    {
        if (isProcessed(event)) {
            return State.REJECTED;
        }
        
        if (currentButton != null) {
            pressedButton = currentButton;
            pressedButton.mousePressed();
        }
        
        return State.REJECTED;
    }

    
    public WidgetAction.State mouseMoved(Widget widget, 
            WidgetAction.WidgetMouseEvent event) 
    {
        if (isProcessed(event)) {
            return State.REJECTED;
        }
        
        
        if (widget != currentButton) {
            if (currentButton != null) {
                currentButton.mouseExited();
                currentButton = null;
            }

            if (widget instanceof ButtonWidget) {
                currentButton = (ButtonWidget) widget;
                currentButton.mouseEntered();
            }
        }
        
        return State.REJECTED;
    }
    
    
    public WidgetAction.State mouseDragged(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return mouseMoved(widget, event);
    }    
    
    
    public WidgetAction.State mouseEntered(Widget widget, 
            WidgetAction.WidgetMouseEvent event) 
    {
        if (isProcessed(event)) {
            return State.REJECTED;
        }
        
        if (currentButton != null) {
            currentButton.mouseExited();
            currentButton = null;
        }
        
        if (widget instanceof ButtonWidget) {
            currentButton = (ButtonWidget) widget;
            currentButton.mouseEntered();
        }
        
        return State.REJECTED;
    }

    
    public WidgetAction.State mouseExited(Widget widget, 
            WidgetAction.WidgetMouseEvent event) 
    {
        if (isProcessed(event)) {
            return State.REJECTED;
        }
        
        if (currentButton != null) {
            currentButton.mouseExited();
            currentButton = null;
        }
        return State.REJECTED;
    }

    @Override
    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        boolean state = false;
        if (event.getKeyChar() == KeyEvent.VK_ENTER || event.getKeyChar() == KeyEvent.VK_SPACE) {
            Widget w = widget.getScene().getFocusedWidget();
            if (w instanceof ButtonWidget) {
                ((ButtonWidget) w).mouseReleased(true);
                state = true;
            }
        }
        return state ? State.CONSUMED : State.REJECTED;
    }
    
    
}
