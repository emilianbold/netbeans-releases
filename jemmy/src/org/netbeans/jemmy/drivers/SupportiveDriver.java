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
 * Allows to declare supported operator classes.
 */
abstract public class SupportiveDriver implements Driver {
    private Class[] supported;

    /**
     * Creates an instance.
     * @param supported Array of operator classes which are supported by this driver.
     */
    public SupportiveDriver(Class[] supported) {
	this.supported = supported;
    }
    /**
     * Throws <code>UnsupportedOperatorException</code> exception if 
     * parameter's class is not in list of supported classes.
     * @param oper Operator whose class should be checked.
     * @throws UnsupportedOperatorException
     */
    public void checkSupported(ComponentOperator oper) {
	UnsupportedOperatorException.checkSupported(getClass(), supported, oper.getClass());
    }

    /**
     * Returns array of operator classes which are supported by this driver.
     */
    public Class[] getSupported() {
	return(supported);
    }
}
