/*
 * BmpConnectAction.java
 *
 * Created on August 30, 2006, 5:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
