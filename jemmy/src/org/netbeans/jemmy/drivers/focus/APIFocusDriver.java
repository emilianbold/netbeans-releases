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

package org.netbeans.jemmy.drivers.focus;

import java.awt.event.FocusEvent;

import org.netbeans.jemmy.drivers.FocusDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

public class APIFocusDriver extends SupportiveDriver implements FocusDriver {
    EventDriver eDriver;
    public APIFocusDriver() {
	super(new Class[] {ComponentOperator.class});
	eDriver = new EventDriver();
    }
    public void giveFocus(ComponentOperator operator) {
	operator.requestFocus();
	eDriver.dispatchEvent(operator.getSource(),
			      new FocusEvent(operator.getSource(),
					     FocusEvent.FOCUS_GAINED));
    }
}
