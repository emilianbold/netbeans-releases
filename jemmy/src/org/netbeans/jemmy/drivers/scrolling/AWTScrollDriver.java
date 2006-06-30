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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
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
