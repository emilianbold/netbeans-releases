/*
 * HeaderWidget.java
 *
 * Created on January 19, 2007, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author radval
 */
public class HeaderWidget extends Widget {
    
    private ExpanderWidget mExpanderWidget;
    
    /** Creates a new instance of HeaderWidget */
    public HeaderWidget(Scene scene, ExpanderWidget expanderWidget) {
        super(scene);
        mExpanderWidget = expanderWidget;
        WidgetAction action = new HeaderWidgetAction();
        getActions().addAction(action);
    }
    
    class HeaderWidgetAction extends WidgetAction.Adapter {
        public WidgetAction.State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {
            if(event.getClickCount() == 2) {
                toggleUI();
                return WidgetAction.State.CONSUMED;
            }
            
            return WidgetAction.State.REJECTED;
        }

        public WidgetAction.State keyReleased(Widget widget, WidgetAction.WidgetKeyEvent event) {
            if(event.getKeyCode() == KeyEvent.VK_ENTER) {
                toggleUI();
                return WidgetAction.State.CONSUMED;
            }
            
            return WidgetAction.State.REJECTED;
        }
        

    }
    
    private void toggleUI() {
        mExpanderWidget.setExpanded(!mExpanderWidget.isExpanded());
    }
}
