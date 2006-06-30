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

/**
 * Allows to declare supported operator classes.
 */
abstract public class LightSupportiveDriver implements LightDriver {
    private String[] supported;

    /**
     * Creates an instance.
     * @param supported Array of operator classes which are supported by this driver.
     */
    public LightSupportiveDriver(String[] supported) {
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
    public String[] getSupported() {
	return(supported);
    }
}
