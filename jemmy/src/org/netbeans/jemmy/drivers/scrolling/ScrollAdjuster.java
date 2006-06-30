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
