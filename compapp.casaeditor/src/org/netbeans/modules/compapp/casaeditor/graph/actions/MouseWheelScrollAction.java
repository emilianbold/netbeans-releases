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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Component;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;

/**
 * @author Josh Sandusky
 */
public final class MouseWheelScrollAction extends WidgetAction.Adapter {

    
    public MouseWheelScrollAction() {
    }

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        Scene scene = widget.getScene ();
        int amount = event.getWheelRotation ();
        
        if (scene != null && scene.getView() != null) {
            JScrollPane scrollPane = null;
            Component parent = scene.getView().getParent();
            while (parent != null) {
                if (parent instanceof JScrollPane) {
                    scrollPane = (JScrollPane) parent;
                    break;
                }
                parent = parent.getParent();
            }
            if (scrollPane != null) {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                while (amount > 0) {
                    vBar.setValue(vBar.getValue() + vBar.getUnitIncrement(1));
                    amount --;
                }
                while (amount < 0) {
                    vBar.setValue(vBar.getValue() - vBar.getUnitIncrement(-1));
                    amount ++;
                }
                return WidgetAction.State.CONSUMED;
            }
        }
        
        return WidgetAction.State.REJECTED;
    }

}
