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

public interface ScrollAdjuster {
    public static final int INCREASE_SCROLL_DIRECTION = 1;
    public static final int DECREASE_SCROLL_DIRECTION = -1;
    public static final int DO_NOT_TOUCH_SCROLL_DIRECTION = 0;
    public int getScrollDirection();
    public int getScrollOrientation();
    public String getDescription();
}
