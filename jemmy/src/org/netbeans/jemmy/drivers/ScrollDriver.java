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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

/**
 * Defines how to work with scrollable components such as
 * <code>javax.swing.JScrollBar</code>, <code>javax.swing.JScrollPane</code>, <code>javax.swing.JSlider</code>, ...
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public interface ScrollDriver {

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
     * @param adj Object defines scroll position.
     */
    public void scroll(ComponentOperator oper, ScrollAdjuster adj);
}
