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

package org.netbeans.jemmy.drivers.buttons;

import org.netbeans.jemmy.drivers.ButtonDriver;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

public class ButtonMouseDriver extends SupportiveDriver implements ButtonDriver {
    public ButtonMouseDriver() {
	super(new Class[] {ComponentOperator.class});
    }
    public void press(ComponentOperator oper) {
	MouseDriver mdriver = DriverManager.getMouseDriver(oper.getClass());
	mdriver.moveMouse(oper, 
			  oper.getCenterXForClick(),
			  oper.getCenterYForClick());
	mdriver.pressMouse(oper, 
			   oper.getCenterXForClick(),
			   oper.getCenterYForClick(),
			   oper.getDefaultMouseButton(),
			   0);
    }
    public void release(ComponentOperator oper) {
	DriverManager.getMouseDriver(oper.getClass()).
	    releaseMouse(oper, 
		       oper.getCenterXForClick(),
		       oper.getCenterYForClick(),
		       oper.getDefaultMouseButton(),
		       0);
    }
    public void push(ComponentOperator oper) {
	DriverManager.getMouseDriver(oper.getClass()).
	    clickMouse(oper, 
		       oper.getCenterXForClick(),
		       oper.getCenterYForClick(),
		       1, 
		       oper.getDefaultMouseButton(),
		       0,
		       oper.getTimeouts().
		       create("ComponentOperator.MouseClickTimeout"));
    }
}
