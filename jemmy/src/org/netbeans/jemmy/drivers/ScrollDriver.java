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

/**
 * Defines how to work with scrollable components such as
 * <code>javax.swing.JScrollBar</code>, <code>javax.swing.JScrollPane</code>, <code>javax.swing.JSlider</code>, ...
 */
public interface ScrollDriver extends Driver {

    /**
     * Changes value to a minimum.
     * @param oper Scroller operator.
     * @param orientation <code>java.awt.Adjustable.HORIZONTAL</code> or <code>java.awt.Adjustable.VERTICAL</code>
     */
    public void scrollToMinimum(ComponentOperator oper, int orientation);

    /**
     * Changes value to a maximum.
     * @param oper Scroller operator.
     * @param orientation <code>java.awt.Adjustable.HORIZONTAL</code> or <code>java.awt.Adjustable.VERTICAL</code>
     */
    public void scrollToMaximum(ComponentOperator oper, int orientation);

    /**
     * Changes value.
     * @param oper Scroller operator.
     * @param ScrollAdjuster Object defines scroll position.
     */
    public void scroll(ComponentOperator oper, ScrollAdjuster adj);
}
