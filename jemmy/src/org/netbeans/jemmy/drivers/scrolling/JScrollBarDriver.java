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

import javax.swing.JButton;
import javax.swing.JScrollBar;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JScrollBarOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * ScrollDriver for javax.swing.JScrollBar component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JScrollBarDriver extends AbstractScrollDriver {
    private final static int SMALL_INCREMENT = 1;
    private final static int MINIMAL_DRAGGER_SIZE = 5;
    private final static int RELATIVE_DRAG_STEP_LENGTH = 20;

    private QueueTool queueTool;

    /**
     * Constructs a JScrollBarDriver.
     */
    public JScrollBarDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JScrollBarOperator"});
        queueTool = new QueueTool();
    }

    public void scrollToMinimum(ComponentOperator oper, int orientation) {
	startDragging(oper);
	Point pnt = new Point(0, 0);
	drag(oper, pnt);
	Timeout sleepTime = oper.getTimeouts().create("Waiter.TimeDelta");
	while(((JScrollBarOperator)oper).getValue() > 
	      ((JScrollBarOperator)oper).getMinimum()) {
	    sleepTime.sleep();
	}
	drop(oper, pnt);
    }

    public void scrollToMaximum(ComponentOperator oper, int orientation) {
	startDragging(oper);
	Point pnt = new Point(oper.getWidth() - 1, oper.getHeight() - 1);
	drag(oper, pnt);
	Timeout sleepTime = oper.getTimeouts().create("Waiter.TimeDelta");
	while(((JScrollBarOperator)oper).getValue() > 
	      (((JScrollBarOperator)oper).getMaximum() - 
	       ((JScrollBarOperator)oper).getVisibleAmount())) {
	    sleepTime.sleep();
	}
	drop(oper, pnt);
    }

    protected void step(ComponentOperator oper, ScrollAdjuster adj) {
	JButtonOperator boper = findAButton(oper, adj.getScrollDirection());
	DriverManager.getButtonDriver(boper).push(boper);
    }

    protected void jump(final ComponentOperator oper, final ScrollAdjuster adj) {
        final JButtonOperator lessButton = findAButton(oper, adj.DECREASE_SCROLL_DIRECTION);
        final JButtonOperator moreButton = findAButton(oper, adj.INCREASE_SCROLL_DIRECTION);
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Choise expanding") {
                public Object launch() {
                    if(adj.getScrollDirection() != ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION) {
                        int x, y;
                        if       (((JScrollBarOperator)oper).getOrientation() == JScrollBar.HORIZONTAL) {
                            if       (adj.getScrollDirection() == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
                                x = moreButton.getX() - 1;
                            } else if(adj.getScrollDirection() == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
                                x = lessButton  .getX() + lessButton.getWidth();
                            } else {
                                return(null);
                            }
                            y = lessButton.getHeight() / 2;
                        } else if(((JScrollBarOperator)oper).getOrientation() == JScrollBar.VERTICAL) {
                            if       (adj.getScrollDirection() == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
                                y = moreButton.getY() - 1;
                            } else if(adj.getScrollDirection() == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
                                y = lessButton  .getY() + lessButton.getHeight();
                            } else {
                                return(null);
                            }
                            x = lessButton.getWidth() / 2;
                        } else {
                            return(null);
                        }
                        DriverManager.getMouseDriver(oper).
                            clickMouse(oper, x, y, 1, oper.getDefaultMouseButton(), 0, new Timeout("", 0));
                    }
                    return(null);
                }
            });
    }

    protected void startPushAndWait(ComponentOperator oper, int direction, int orientation) {
	JButtonOperator boper = findAButton(oper, direction);
	DriverManager.getButtonDriver(boper).press(boper);
    }

    protected void stopPushAndWait(ComponentOperator oper, int direction, int orientation) {
	JButtonOperator boper = findAButton(oper, direction);
	DriverManager.getButtonDriver(boper).release(boper);
    }

    protected Point startDragging(ComponentOperator oper) {
	JButtonOperator lessButton = findAButton(oper, ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
	JButtonOperator moreButton = findAButton(oper, ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
	Point pnt = getClickPoint((JScrollBarOperator)oper, lessButton, moreButton, ((JScrollBarOperator)oper).getValue());
	MouseDriver mdriver = DriverManager.getMouseDriver(oper);
	mdriver.moveMouse(oper, pnt.x, pnt.y);
	mdriver.pressMouse(oper, pnt.x, pnt.y, oper.getDefaultMouseButton(), 0);
	return(pnt);
    }

    protected void drop(ComponentOperator oper, Point pnt) {
	DriverManager.getMouseDriver(oper).
	    releaseMouse(oper, pnt.x, pnt.y, oper.getDefaultMouseButton(), 0);
    }

    protected void drag(ComponentOperator oper, Point pnt) {
	DriverManager.getMouseDriver(oper).
	    dragMouse(oper, pnt.x, pnt.y, oper.getDefaultMouseButton(), 0);
    }

    protected Timeout getScrollDeltaTimeout(ComponentOperator oper) {
	return(oper.getTimeouts().
	       create("ScrollbarOperator.DragAndDropScrollingDelta"));
    }

    protected boolean canDragAndDrop(ComponentOperator oper) {
	if(!isSmallIncrement((JScrollBarOperator)oper)) {
	    return(false);
	}
	boolean result = false;
	MouseDriver mdriver = DriverManager.getMouseDriver(oper);
	JButtonOperator less = findAButton(oper, ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
	JButtonOperator more = findAButton(oper, ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
	Point pnt = getClickPoint((JScrollBarOperator)oper, less, more, ((JScrollBarOperator)oper).getValue());
	mdriver.moveMouse(oper, pnt.x, pnt.y);
	mdriver.pressMouse(oper, pnt.x, pnt.y, oper.getDefaultMouseButton(), 0);
	result = ((JScrollBarOperator)oper).getValueIsAdjusting();
	mdriver.releaseMouse(oper, pnt.x, pnt.y, oper.getDefaultMouseButton(), 0);
	return(result && isSmallIncrement((JScrollBarOperator)oper));
    }

    protected boolean canJump(ComponentOperator oper) {
	return(isSmallIncrement((JScrollBarOperator)oper));
    }

    protected boolean canPushAndWait(ComponentOperator oper) {
	return(isSmallIncrement((JScrollBarOperator)oper));
    }

    protected int getDragAndDropStepLength(ComponentOperator oper) {
	JButtonOperator less = findAButton(oper, ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
	JButtonOperator more = findAButton(oper, ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
	int width = oper.getWidth() - less.getWidth() - more.getWidth();
	int height = oper.getHeight() - less.getHeight() - more.getHeight();
	int max = (width > height) ? width : height;
	if(max >= RELATIVE_DRAG_STEP_LENGTH * 2) {
	    return((int)(max / RELATIVE_DRAG_STEP_LENGTH));
	} else {
	    return(1);
	}
    }

    private boolean isSmallIncrement(JScrollBarOperator oper) {
	return(oper.getUnitIncrement(-1) <= SMALL_INCREMENT && 
	       oper.getUnitIncrement( 1) <= SMALL_INCREMENT);
    }

    private Point getClickPoint(JScrollBarOperator oper, JButtonOperator lessButton, JButtonOperator moreButton, int value) {
	int lenght = (oper.getOrientation() == JScrollBar.HORIZONTAL) ?
	    oper.getWidth()  - lessButton.getWidth()  - moreButton.getWidth() :
	    oper.getHeight() - lessButton.getHeight() - moreButton.getHeight();
	int subpos = (int)(((float)lenght / (oper.getMaximum() - oper.getMinimum())) * value);
	if(oper.getOrientation() == JScrollBar.HORIZONTAL) {
	    subpos = subpos + lessButton.getWidth();
	} else {
	    subpos = subpos + lessButton.getHeight();
	}
	subpos = subpos + MINIMAL_DRAGGER_SIZE / 2 + 1;
	return((oper.getOrientation() == JScrollBar.HORIZONTAL) ?
	       new Point(subpos, oper.getHeight() / 2) :
	       new Point(oper.getWidth() / 2, subpos));
    }

    private JButtonOperator findAButton(ComponentOperator oper, int direction) {
	return((direction == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) ?
	       ((JScrollBarOperator)oper).getDecreaseButton() : 
	       ((JScrollBarOperator)oper).getIncreaseButton());
    }
}
