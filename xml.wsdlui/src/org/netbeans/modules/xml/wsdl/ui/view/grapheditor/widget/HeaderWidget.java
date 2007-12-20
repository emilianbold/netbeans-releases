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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget that controls the expanded state of an expander widget by
 * responding to certain input events.
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class HeaderWidget extends Widget {
    /** The expander we control. */
    private ExpanderWidget mExpanderWidget;

    /**
     * Creates a new instance of HeaderWidget.
     *
     * @param  scene           the Scene to which we belong.
     * @param  expanderWidget  the expander to control.
     */
    public HeaderWidget(Scene scene, ExpanderWidget expanderWidget) {
        super(scene);
        mExpanderWidget = expanderWidget;
        getActions().addAction(new HeaderWidgetAction());
    }

    /**
     * Toggle the expander widget between expanded and collapsed state.
     */
    private void toggleState() {
        mExpanderWidget.setExpanded(!mExpanderWidget.isExpanded());
    }

    /**
     * Responds to input events to control the expander.
     */
    private class HeaderWidgetAction extends WidgetAction.Adapter {

        // Intercept keyPressed() to avoid conflict with the inplace editor.
        public WidgetAction.State keyPressed(Widget widget,
                WidgetAction.WidgetKeyEvent event) {
        	if (event.getKeyChar() == KeyEvent.VK_ENTER && (event.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0){
        		toggleState();
        		return WidgetAction.State.CONSUMED;
        	}
            return WidgetAction.State.REJECTED;
        }

        // Intercept mouseClicked() to avoid conflict with the inplace editor.
        public WidgetAction.State mouseClicked(Widget widget,
                WidgetAction.WidgetMouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 &&
                    event.getClickCount() == 2) {
                toggleState();
                return WidgetAction.State.CONSUMED;
            }
            return WidgetAction.State.REJECTED;
        }
    }
}
