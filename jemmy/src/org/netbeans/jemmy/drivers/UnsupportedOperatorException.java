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

public class UnsupportedOperatorException extends JemmyException {
    public UnsupportedOperatorException(Class driver, Class operator) {
	super(driver.getName() + " operators are not supported by " +
	      operator.getName() + " driver!");
    }
    public static void checkSupported(Class driver, Class[] supported, Class operator) {
	for(int i = 0; i < supported.length; i++) {
	    if(supported[i].isAssignableFrom(operator)) {
		return;
	    }
	}
	throw(new UnsupportedOperatorException(driver, operator));
    }
}
