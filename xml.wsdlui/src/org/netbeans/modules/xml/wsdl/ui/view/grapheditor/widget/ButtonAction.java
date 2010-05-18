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
    public State keyPressed (Widget widget, WidgetKeyEvent event) {
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
