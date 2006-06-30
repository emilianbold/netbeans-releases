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

import org.netbeans.jemmy.JemmyException;

/**
 * Is thrown as a result of attempt to use driver for unsupported operator type.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class UnsupportedOperatorException extends JemmyException {

    /**
     * Constructor.
     * @param driver a driver
     * @param operator an operator
     */
    public UnsupportedOperatorException(Class driver, Class operator) {
	super(driver.getName() + " operators are not supported by " +
	      operator.getName() + " driver!");
    }

    /**
     * Checks if operator class is in the list of supported classes.
     * @param driver Driver class
     * @param supported Supported classes.
     * @param operator Operator class.
     * @throws UnsupportedOperatorException if class is not supported.
     */
    public static void checkSupported(Class driver, Class[] supported, Class operator) {
	for(int i = 0; i < supported.length; i++) {
	    if(supported[i].isAssignableFrom(operator)) {
		return;
	    }
	}
	throw(new UnsupportedOperatorException(driver, operator));
    }

    /**
     * Checks if operator class name is in the list of supported classes names.
     * @param driver Driver class
     * @param supported Supported classes names.
     * @param operator Operator class.
     * @throws UnsupportedOperatorException if class is not supported.
     */
    public static void checkSupported(Class driver, String[] supported, Class operator) {
        Class opClass = operator;
        do {
            for(int i = 0; i < supported.length; i++) {
                if(opClass.getName().equals(supported[i])) {
                    return;
                }
            }
        } while((opClass = opClass.getSuperclass()) != null);
	throw(new UnsupportedOperatorException(driver, operator));
    }
}
