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

/**
 * Defines how to work with frames.
 */
public interface FrameDriver {

    /**
     * Iconifies a frame.
     * @param oper Frame operator.
     */
    public void iconify(ComponentOperator oper);

    /**
     * Deiconifies a frame.
     * @param oper Frame operator.
     */
    public void deiconify(ComponentOperator oper);

    /**
     * Maximizes a frame.
     * @param oper Frame operator.
     */
    public void maximize(ComponentOperator oper);

    /**
     * Demaximizes a frame.
     * @param oper Frame operator.
     */
    public void demaximize(ComponentOperator oper);
}
