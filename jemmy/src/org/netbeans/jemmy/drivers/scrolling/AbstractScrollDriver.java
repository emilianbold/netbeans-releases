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

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.ScrollDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

public abstract class AbstractScrollDriver extends SupportiveDriver implements ScrollDriver {
    public static final int ADJUST_CLICK_COUNT = 10;
    public AbstractScrollDriver(Class[] supported) {
	super(supported);
    }
    public void scroll(ComponentOperator oper, ScrollAdjuster adj) {
	if(canJump(oper)) {
	    doJumps(oper, adj);
	}
	if(canDragAndDrop(oper)) {
	    doDragAndDrop(oper, adj);
	}
	if(canPushAndWait(oper)) {
	    doPushAndWait(oper, adj);
	}
	for(int i = 0; i < ADJUST_CLICK_COUNT; i++) {
	    doSteps(oper, adj);
	}
    }
    protected abstract void step(ComponentOperator oper, ScrollAdjuster adj);
    protected abstract void jump(ComponentOperator oper, ScrollAdjuster adj);
    protected abstract void startPushAndWait(ComponentOperator oper, int direction, int orientation);
    protected abstract void stopPushAndWait(ComponentOperator oper, int direction, int orientation);
    protected abstract Point startDragging(ComponentOperator oper);
    protected abstract void drop(ComponentOperator oper, Point pnt);
    protected abstract void drag(ComponentOperator oper, Point pnt);
    protected abstract Timeout getScrollDeltaTimeout(ComponentOperator oper);
    protected abstract boolean canDragAndDrop(ComponentOperator oper);
    protected abstract boolean canJump(ComponentOperator oper);
    protected abstract boolean canPushAndWait(ComponentOperator oper);
    protected abstract int getDragAndDropStepLength(ComponentOperator oper);
    protected void doDragAndDrop(ComponentOperator oper, ScrollAdjuster adj) {
	int direction = adj.getScrollDirection();
	if(direction != adj.DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    Point pnt = startDragging(oper);
	    while(adj.getScrollDirection() == direction) {
		drag(oper, pnt = increasePoint(oper, pnt, adj, direction));
	    }
	    drop(oper, pnt);
	}
    }
    protected void doJumps(ComponentOperator oper, ScrollAdjuster adj) {
	int direction = adj.getScrollDirection();
	if(direction != adj.DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    while(adj.getScrollDirection() == direction) {
		jump(oper, adj);
	    }
	}
    }
    protected void doPushAndWait(ComponentOperator oper, ScrollAdjuster adj) {
	int direction = adj.getScrollDirection();
	int orientation = adj.getScrollOrientation();
	if(direction != adj.DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    Timeout delta = getScrollDeltaTimeout(oper);
	    startPushAndWait(oper, direction, orientation);
	    while(adj.getScrollDirection() == direction) {
		delta.sleep();
	    }
	    stopPushAndWait(oper, direction, orientation);
	}
    }
    protected void doSteps(ComponentOperator oper, ScrollAdjuster adj) {
	int direction = adj.getScrollDirection();
	if(direction != adj.DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    while(adj.getScrollDirection() == direction) {
		step(oper, adj);
	    }
	}
    }
    private Point increasePoint(ComponentOperator oper, Point pnt, ScrollAdjuster adj, int direction) {
	return((adj.getScrollOrientation() == Adjustable.HORIZONTAL) ?
	       new Point(pnt.x + ((direction == 1) ? 1 : -1) * getDragAndDropStepLength(oper), pnt.y) :
	       new Point(pnt.x, pnt.y + ((direction == 1) ? 1 : -1) * getDragAndDropStepLength(oper)));
    }
}
