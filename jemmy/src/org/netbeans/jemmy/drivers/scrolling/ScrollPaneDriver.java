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
import java.awt.Scrollbar;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.ScrollPaneOperator;

/**
 * ScrollDriver for java.awt.ScrollPane component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class ScrollPaneDriver extends AWTScrollDriver {
    private static final int CLICK_OFFSET = 5;
    /**
     * Constructs a ScrollPaneDriver.
     */
    public ScrollPaneDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.ScrollPaneOperator"});
    }

    public void scrollToMinimum(ComponentOperator oper, final int orientation) {
	final Adjustable adj = 
	    (orientation == Scrollbar.HORIZONTAL) ?
	    ((ScrollPaneOperator)oper).getHAdjustable() :
	    ((ScrollPaneOperator)oper).getVAdjustable();
	scroll(oper, 
	       new ScrollAdjuster() {
		public int getScrollDirection() {
		    return((adj.getMinimum() < adj.getValue()) ? 
			   DECREASE_SCROLL_DIRECTION :
			   DO_NOT_TOUCH_SCROLL_DIRECTION);
		}
		public int getScrollOrientation() {
		    return(orientation);
		}
		public String getDescription() {
		    return("Scroll to minimum");
		}
	    });
    }

    public void scrollToMaximum(ComponentOperator oper, final int orientation) {
	final Adjustable adj = 
	    (orientation == Scrollbar.HORIZONTAL) ?
	    ((ScrollPaneOperator)oper).getHAdjustable() :
	    ((ScrollPaneOperator)oper).getVAdjustable();
	scroll(oper, 
	       new ScrollAdjuster() {
		public int getScrollDirection() {
		    return(((adj.getMaximum() - adj.getVisibleAmount()) > adj.getValue()) ? 
			   INCREASE_SCROLL_DIRECTION :
			   DO_NOT_TOUCH_SCROLL_DIRECTION);
		}
		public int getScrollOrientation() {
		    return(orientation);
		}
		public String getDescription() {
		    return("Scroll to maximum");
		}
	    });
    }

    protected Point getClickPoint(ComponentOperator oper, int direction, int orientation) {
	int x, y;
	if       (orientation == Scrollbar.HORIZONTAL) {
	    int offset = ((ScrollPaneOperator)oper).
		isScrollbarVisible(Scrollbar.VERTICAL) ? 
		((ScrollPaneOperator)oper).getVScrollbarWidth() : 0;
	    if       (direction == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
		x = oper.getWidth() - 1 - CLICK_OFFSET - offset;
	    } else if(direction == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
		x = CLICK_OFFSET;
	    } else {
		return(null);
	    }
	    y = oper.getHeight() - ((ScrollPaneOperator)oper).getHScrollbarHeight() / 2;
	} else if(orientation == Scrollbar.VERTICAL) {
	    int offset = ((ScrollPaneOperator)oper).
		isScrollbarVisible(Scrollbar.HORIZONTAL) ? 
		((ScrollPaneOperator)oper).getHScrollbarHeight() : 0;
	    if       (direction == ScrollAdjuster.INCREASE_SCROLL_DIRECTION) {
		y = oper.getHeight() - 1 - CLICK_OFFSET - offset;
	    } else if(direction == ScrollAdjuster.DECREASE_SCROLL_DIRECTION) {
		y = CLICK_OFFSET;
	    } else {
		return(null);
	    }
	    x = oper.getWidth() - ((ScrollPaneOperator)oper).getVScrollbarWidth() / 2;
	} else {
	    return(null);
	}
	return(new Point(x, y));
    }
}
