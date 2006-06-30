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

import java.awt.Point;

import javax.swing.JSlider;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JSliderOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * A scroll driver serving JSlider component.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JSliderDriver extends AbstractScrollDriver {
    private QueueTool queueTool;

    /**
     * Constructs a JSliderDriver object.
     */
    public JSliderDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JSliderOperator"});
        queueTool = new QueueTool();
    }

    public void scrollToMinimum(final ComponentOperator oper, int orientation) {
        checkSupported(oper);
	scroll(oper, 
	       new ScrollAdjuster() {
		public int getScrollDirection() {
		    return((((JSliderOperator)oper).getMinimum() < 
			    ((JSliderOperator)oper).getValue()) ? 
			   DECREASE_SCROLL_DIRECTION :
			   DO_NOT_TOUCH_SCROLL_DIRECTION);
		}
		public int getScrollOrientation() {
		    return(((JSliderOperator)oper).getOrientation());
		}
		public String getDescription() {
		    return("Scroll to minimum");
		}
	    });
    }

    public void scrollToMaximum(final ComponentOperator oper, int orientation) {
        checkSupported(oper);
	scroll(oper, 
	       new ScrollAdjuster() {
		public int getScrollDirection() {
		    return((((JSliderOperator)oper).getMaximum() > 
			    ((JSliderOperator)oper).getValue()) ? 
			   INCREASE_SCROLL_DIRECTION :
			   DO_NOT_TOUCH_SCROLL_DIRECTION);
		}
		public int getScrollOrientation() {
		    return(((JSliderOperator)oper).getOrientation());
		}
		public String getDescription() {
		    return("Scroll to maximum");
		}
	    });
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

    protected void jump(ComponentOperator oper, ScrollAdjuster adj) {
        //cannot
    }

    protected void startPushAndWait(final ComponentOperator oper, final int direction, final int orientation) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Start scrolling") {
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
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Stop scrolling") {
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
        //cannot
        return(null);
    }

    protected void drop(ComponentOperator oper, Point pnt) {
        //cannot
    }

    protected void drag(ComponentOperator oper, Point pnt) {
        //cannot
    }

    protected Timeout getScrollDeltaTimeout(ComponentOperator oper) {
	return(oper.getTimeouts().create("JSliderOperator.ScrollingDelta"));
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
        return(0);
    }
    
    private Point getClickPoint(ComponentOperator oper, int direction, int orientation) {
	int x, y;
        boolean inverted = ((JSliderOperator)oper).getInverted();
        int realDirection = ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION;
        if(inverted) {
            if       (direction == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
                realDirection = ScrollAdjuster.DECREASE_SCROLL_DIRECTION; 
            } else if(direction == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
                realDirection = ScrollAdjuster.INCREASE_SCROLL_DIRECTION; 
            } else {
                return(null);
            }
        } else {
            realDirection = direction;
        }
	if       (orientation == JSlider.HORIZONTAL) {
	    if       (realDirection == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
		x = oper.getWidth() - 1;
	    } else if(realDirection == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
		x = 0;
	    } else {
		return(null);
	    }
	    y = oper.getHeight() / 2;
	} else if(orientation == JSlider.VERTICAL) {
	    if       (realDirection == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
		y = 0;
	    } else if(realDirection == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
		y = oper.getHeight() - 1;
	    } else {
		return(null);
	    }
	    x = oper.getWidth() / 2;
	} else {
	    return(null);
	}
	return(new Point(x, y));
    }
}
