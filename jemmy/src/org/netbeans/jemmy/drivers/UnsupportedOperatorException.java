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

import org.netbeans.jemmy.JemmyException;

/**
 * Is thrown as a result of attempt to use driver for unsupported operator type.
 */
public class UnsupportedOperatorException extends JemmyException {

    /**
     * Constructor.
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
