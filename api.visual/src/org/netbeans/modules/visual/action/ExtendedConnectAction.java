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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.action;

import java.awt.event.KeyEvent;
import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author alex
 */
public class ExtendedConnectAction extends ConnectAction{
    
    private boolean ctrl=false;
    private int keyCode=KeyEvent.VK_CONTROL;//TODO
    
    public ExtendedConnectAction(ConnectDecorator decorator, Widget interractionLayer, ConnectProvider provider) {
        super(decorator,interractionLayer,provider);
    }
    
    public WidgetAction.State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (ctrl) {
            return  super.mousePressed(widget,event);
        }else return State.REJECTED;
    }
    
    public WidgetAction.State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (ctrl) {
            return  super.mouseReleased(widget,event);
        }else return State.REJECTED;
    }
    
    public WidgetAction.State keyPressed(Widget widget, WidgetKeyEvent event) {
        if (keyCode==event.getKeyCode()) {
            ctrl=true;
        }
        return State.CONSUMED;
    }
    
    public WidgetAction.State keyReleased(Widget widget, WidgetKeyEvent event) {
        if (keyCode==event.getKeyCode()) {
            ctrl=false;
        }
        return State.REJECTED;
    }
    
    public void setKeyCode(int keyCode){
        this.keyCode=keyCode;
    }
    
}
