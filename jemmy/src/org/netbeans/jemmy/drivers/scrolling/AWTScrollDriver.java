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

package org.netbeans.jemmy.drivers.scrolling;

import java.awt.Adjustable;
import java.awt.Point;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * ScrollDriver for awt components.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public abstract class AWTScrollDriver extends AbstractScrollDriver {
    private QueueTool queueTool;

    /**
     * Constructs a ChoiceDriver.
     * @param supported an array of supported class names
     */
    public AWTScrollDriver(String[] supported) {
	super(supported);
        queueTool = new QueueTool();
    }
    protected void step(final ComponentOperator oper, final ScrollAdjuster adj) {
	if(adj.getScrollDirection() != ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                    public Object launch() {
                        Point clickPoint = getClickPoint(oper, adj.getScrollDirection(), adj.getScrollOrientation());
                        if(clickPoint != null) {
                            DriverManager.getMouseDriver(oper).
                                clickMouse(oper, clickPoint.x, clickPoint.y, 1, 
                                           Operator.getDefaultMouseButton(),
                                           0, 
                                           oper.getTimeouts().
                                           create("ComponentOperator.MouseClickTimeout"));
                        }
                        return(null);
                    }
                });
	}
    }
    protected void jump(ComponentOperator oper, ScrollAdjuster adj) {}
    protected void startPushAndWait(final ComponentOperator oper, final int direction, final int orientation) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                public Object launch() {
                    Point clickPoint = getClickPoint(oper, direction, orientation);
                    if(clickPoint != null) {
                        MouseDriver mdriver = DriverManager.getMouseDriver(oper);
                        mdriver.moveMouse(oper, clickPoint.x, clickPoint.y);
                        mdriver.pressMouse(oper, clickPoint.x, clickPoint.y,
                                           Operator.getDefaultMouseButton(),
                                           0);
                    }
                    return(null);
                }
            });
    }
    protected void stopPushAndWait(final ComponentOperator oper, final int direction, final int orientation) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                public Object launch() {
                    Point clickPoint = getClickPoint(oper, direction, orientation);
                    if(clickPoint != null) {
                        MouseDriver mdriver = DriverManager.getMouseDriver(oper);
                        mdriver.releaseMouse(oper, clickPoint.x, clickPoint.y,
                                             Operator.getDefaultMouseButton(),
                                             0);
                    }
                    return(null);
                }
            });
    }
    protected Point startDragging(ComponentOperator oper) {
	return(null);
    }
    protected void drop(ComponentOperator oper, Point pnt) {}
    protected void drag(ComponentOperator oper, Point pnt) {}
    protected Timeout getScrollDeltaTimeout(ComponentOperator oper) {
	return(oper.getTimeouts().
	       create("ScrollbarOperator.DragAndDropScrollingDelta"));
    }
    protected boolean canDragAndDrop(ComponentOperator oper) {
	return(false);
    }
    protected boolean canJump(ComponentOperator oper) {
	return(false);
    }
    protected boolean canPushAndWait(ComponentOperator oper) {
	return(true);
    }
    protected int getDragAndDropStepLength(ComponentOperator oper) {
	return(1);
    }
    /**
     * Defines a click point which needs to be used in
     * order to increase/decrease scroller value.
     * @param oper an operator.
     * @param direction - one of the ScrollAdjister.INCREASE_SCROLL_DIRECTION, 
     * ScrollAdjister.DECREASE_SCROLL_DIRECTION, ScrollAdjister.DO_NOT_TOUCH_SCROLL_DIRECTION values.
     * @param orientation one of the Adjustable.HORIZONTAL or Adjustable.VERTICAL values.
     * @return a point to click.
     */
    protected abstract Point getClickPoint(ComponentOperator oper, int direction, int orientation);
}
