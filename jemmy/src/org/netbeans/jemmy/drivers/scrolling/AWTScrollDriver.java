/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
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

public abstract class AWTScrollDriver extends AbstractScrollDriver {
    private QueueTool queueTool;
    public AWTScrollDriver(Class[] supported) {
	super(supported);
        queueTool = new QueueTool();
    }
    protected void step(final ComponentOperator oper, final ScrollAdjuster adj) {
	if(adj.getScrollDirection() != ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                    public Object launch() {
                        Point clickPoint = getClickPoint(oper, adj.getScrollDirection(), adj.getScrollOrientation());
                        DriverManager.getMouseDriver(oper).
                            clickMouse(oper, clickPoint.x, clickPoint.y, 1, 
                                       Operator.getDefaultMouseButton(),
                                       0, 
                                       oper.getTimeouts().
                                       create("ComponentOperator.MouseClickTimeout"));
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
                    MouseDriver mdriver = DriverManager.getMouseDriver(oper);
                    mdriver.moveMouse(oper, clickPoint.x, clickPoint.y);
                    mdriver.pressMouse(oper, clickPoint.x, clickPoint.y,
                                       Operator.getDefaultMouseButton(),
                                       0);
                    return(null);
                }
            });
    }
    protected void stopPushAndWait(final ComponentOperator oper, final int direction, final int orientation) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                public Object launch() {
                    Point clickPoint = getClickPoint(oper, direction, orientation);
                    MouseDriver mdriver = DriverManager.getMouseDriver(oper);
                    mdriver.releaseMouse(oper, clickPoint.x, clickPoint.y,
                                         Operator.getDefaultMouseButton(),
                                         0);
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
    protected abstract Point getClickPoint(ComponentOperator oper, int direction, int orientation);
}
