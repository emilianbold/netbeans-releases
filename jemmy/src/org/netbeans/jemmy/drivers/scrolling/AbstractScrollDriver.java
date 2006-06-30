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

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.ScrollDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Superclass for all scroll drivers.
 * Contains all the logic of scrolling.
 * Tryes allowed operations in this order:
 * "jump", "drag'n'drop", "push'n'wait", "step".
 * Repeats "step" scrolling while scroller value is not equal
 * to the necessary value, but no more than <code>ADJUST_CLICK_COUNT</code>.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public abstract class AbstractScrollDriver extends LightSupportiveDriver implements ScrollDriver {

    /**
     * Maximal number of attemps to reach required position
     * by minimal scrolling operation.
     */
    public static final int ADJUST_CLICK_COUNT = 10;
    /**
     * Constructs an AbstractScrollDriver.
     * @param supported an array of supported class names
     */
    public AbstractScrollDriver(String[] supported) {
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

    /**
     * Performs minimal scrolling step.
     * @param oper an operator.
     * @param adj a scroll adjuster
     */
    protected abstract void step(ComponentOperator oper, ScrollAdjuster adj);

    /**
     * Performs maximal scroll step.
     * @param oper an operator.
     * @param adj a scroll adjuster
     */
    protected abstract void jump(ComponentOperator oper, ScrollAdjuster adj);

    /**
     * Presses something like a scroll button.
     * @param oper an operator.
     * @param direction - one of the ScrollAdjister.INCREASE_SCROLL_DIRECTION, 
     * ScrollAdjister.DECREASE_SCROLL_DIRECTION, ScrollAdjister.DO_NOT_TOUCH_SCROLL_DIRECTION values.
     * @param orientation one of the Adjustable.HORIZONTAL or Adjustable.VERTICAL values.
     */
    protected abstract void startPushAndWait(ComponentOperator oper, int direction, int orientation);

    /**
     * Releases something like a scroll button.
     * @param oper an operator.
     * @param direction - one of the ScrollAdjister.INCREASE_SCROLL_DIRECTION, 
     * ScrollAdjister.DECREASE_SCROLL_DIRECTION, ScrollAdjister.DO_NOT_TOUCH_SCROLL_DIRECTION values.
     * @param orientation one of the Adjustable.HORIZONTAL or Adjustable.VERTICAL values.
     */
    protected abstract void stopPushAndWait(ComponentOperator oper, int direction, int orientation);

    /**
     * Starts drag'n'drop scrolling.
     * @param oper an operator.
     * @return start drigging point.
     */
    protected abstract Point startDragging(ComponentOperator oper);

    /**
     * Drop at a specified point.
     * @param oper an operator.
     * @param pnt the point to drop.
     */
    protected abstract void drop(ComponentOperator oper, Point pnt);

    /**
     * Drag to a specified point.
     * @param oper an operator.
     * @param pnt the point to drag to.
     */
    protected abstract void drag(ComponentOperator oper, Point pnt);

    /**
     * Returns a timeout for sleeping between verifications during
     * "push and wait" scrolling.
     * @param oper an operator.
     * @return a timeout
     */
    protected abstract Timeout getScrollDeltaTimeout(ComponentOperator oper);

    /**
     * Tells if this driver allows to perform drag'n'drop scrolling.
     * @param oper an operator.
     * @return true if this driver allows to drag'n'drop.
     */
    protected abstract boolean canDragAndDrop(ComponentOperator oper);

    /**
     * Tells if this driver allows to perform jumps.
     * @param oper an operator.
     * @return true if this driver allows to jump.
     */
    protected abstract boolean canJump(ComponentOperator oper);

    /**
     * Tells if this driver allows to perform "push and wait" scrolling.
     * @param oper an operator.
     * @return true if this driver allows to "push and wait".
     */
    protected abstract boolean canPushAndWait(ComponentOperator oper);

    /**
     * Returns a number of pixels in one drag and drop scrolling.
     * @param oper an operator.
     * @return drag'n'drop step length.
     */
    protected abstract int getDragAndDropStepLength(ComponentOperator oper);

    /**
     * Performs drag'n'drop scrolling till scroller's value
     * does not cross required value.
     * @param oper an operator.
     * @param adj a scroll adjuster
     */
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

    /**
     * Performs jump scrolling till scroller's value
     * does not cross required value.
     * @param oper an operator.
     * @param adj a scroll adjuster
     */
    protected void doJumps(ComponentOperator oper, ScrollAdjuster adj) {
	int direction = adj.getScrollDirection();
	if(direction != adj.DO_NOT_TOUCH_SCROLL_DIRECTION) {
	    while(adj.getScrollDirection() == direction) {
		jump(oper, adj);
	    }
	}
    }
    /**
     * Performs "push and wait" scrolling till scroller's value
     * does not cross required value.
     * @param oper an operator.
     * @param adj a scroll adjuster
     */
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

    /**
     * Performs minimal scrollings till scroller's value
     * does not cross required value.
     * @param oper an operator.
     * @param adj a scroll adjuster
     */
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
