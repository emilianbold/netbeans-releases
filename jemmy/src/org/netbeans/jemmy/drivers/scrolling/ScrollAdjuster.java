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

/**
 * Specifies scrolling criteria.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public interface ScrollAdjuster {
    /**
     * Increase scroll direction.
     */
    public static final int INCREASE_SCROLL_DIRECTION = 1;

    /**
     * Decrease scroll direction.
     */
    public static final int DECREASE_SCROLL_DIRECTION = -1;

    /**
     * Specifies that necessary value has been reached..
     */
    public static final int DO_NOT_TOUCH_SCROLL_DIRECTION = 0;

    /**
     * Returns scroll direction to reach necessary scroller value.
     * @return one of the values: INCREASE_SCROLL_DIRECTION, DECREASE_SCROLL_DIRECTION or DO_NOT_TOUCH_SCROLL_DIRECTION.
     */
    public int getScrollDirection();

    /**
     * Returns scrolling orientation.
     * @return one of the values: Adjustable.HORIZONTAL or Adjustable.VERTICAL.
     */
    public int getScrollOrientation();

    /**
     * Returns a printable scrolling description.
     * @return a description.
     */
    public String getDescription();
}
