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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.drivers.input;

import java.awt.Component;

import java.awt.event.MouseEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * MouseDriver using robot operations.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class MouseRobotDriver extends RobotDriver implements MouseDriver {
        
    /**
     * Constructs a MouseRobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public MouseRobotDriver(Timeout autoDelay) {
        super(autoDelay);
    }
    
    /**
     * Constructs a MouseRobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     * @param supported an array of supported class names
     * @param smooth - whether to move mouse smooth from one ppoint to another.
     */
    public MouseRobotDriver(Timeout autoDelay, boolean smooth) {
        super(autoDelay, smooth);
    }

    /**
     * Constructs a MouseRobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     * @param supported an array of supported class names
     */
    public MouseRobotDriver(Timeout autoDelay, String[] supported) {
        super(autoDelay, supported);
    }
    
    /**
     * Constructs a MouseRobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     * @param supported an array of supported class names
     * @param smooth - whether to move mouse smooth from one ppoint to another.
     */
    public MouseRobotDriver(Timeout autoDelay, String[] supported, boolean smooth) {
        super(autoDelay, supported, smooth);
    }
    
    public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
        pressMouse(mouseButton, modifiers);
    }
    
    public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
        releaseMouse(mouseButton, modifiers);
    }
    
    public void moveMouse(ComponentOperator oper, int x, int y) {
        moveMouse(getAbsoluteX(oper, x), getAbsoluteY(oper, y));
    }
    
    public void clickMouse(ComponentOperator oper, int x, int y, int clickCount, int mouseButton,
            int modifiers, Timeout mouseClick) {
        clickMouse(getAbsoluteX(oper, x), getAbsoluteY(oper, y), clickCount, mouseButton, modifiers, mouseClick);
    }
    
    public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
        moveMouse(getAbsoluteX(oper, x), getAbsoluteY(oper, y));
    }
    
    public void dragNDrop(ComponentOperator oper, int start_x, int start_y, int end_x, int end_y,
            int mouseButton, int modifiers, Timeout before, Timeout after) {
        dragNDrop(getAbsoluteX(oper, start_x), getAbsoluteY(oper, start_y), getAbsoluteX(oper, end_x), getAbsoluteY(oper, end_y), mouseButton, modifiers, before, after);
    }
    
    public void enterMouse(ComponentOperator oper) {
        moveMouse(oper, oper.getCenterXForClick(), oper.getCenterYForClick());
    }
    
    public void exitMouse(ComponentOperator oper) {
        //better not go anywhere
        //exit will be executed during the next
        //mouse move anyway.
        //	moveMouse(oper, -1, -1);
    }
    
    /**
     * Returns absolute x coordinate for relative x coordinate.
     * @param oper an operator
     * @param x a relative x coordinate.
     * @return an absolute x coordinate.
     */
    protected int getAbsoluteX(ComponentOperator oper, int x) {
        return(oper.getSource().getLocationOnScreen().x + x);
    }
    
    /**
     * Returns absolute y coordinate for relative y coordinate.
     * @param oper an operator
     * @param y a relative y coordinate.
     * @return an absolute y coordinate.
     */
    protected int getAbsoluteY(ComponentOperator oper, int y) {
        return(oper.getSource().getLocationOnScreen().y + y);
    }
}
