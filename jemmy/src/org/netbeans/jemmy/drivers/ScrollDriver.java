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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

public interface ScrollDriver extends Driver {
    public void scrollToMinimum(ComponentOperator oper, int orientation);
    public void scrollToMaximum(ComponentOperator oper, int orientation);
    public void scroll(ComponentOperator oper, ScrollAdjuster adj);
}
