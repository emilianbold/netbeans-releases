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

package org.netbeans.jemmy.drivers.buttons;

import org.netbeans.jemmy.drivers.ButtonDriver;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Driver to push a button by mouse click.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class ButtonMouseDriver extends LightSupportiveDriver implements ButtonDriver {
    public ButtonMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.ComponentOperator"});
    }
    public void press(ComponentOperator oper) {
	MouseDriver mdriver = DriverManager.getMouseDriver(oper);
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
	DriverManager.
	    getMouseDriver(oper).
	    releaseMouse(oper, 
			 oper.getCenterXForClick(),
			 oper.getCenterYForClick(),
			 oper.getDefaultMouseButton(),
			 0);
    }
    public void push(ComponentOperator oper) {
	DriverManager.
	    getMouseDriver(oper).
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
