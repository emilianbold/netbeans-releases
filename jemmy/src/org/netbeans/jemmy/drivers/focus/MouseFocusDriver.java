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

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.FocusDriver;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JScrollBarOperator;
import org.netbeans.jemmy.operators.JSliderOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.ListOperator;
import org.netbeans.jemmy.operators.ScrollbarOperator;
import org.netbeans.jemmy.operators.TextAreaOperator;
import org.netbeans.jemmy.operators.TextComponentOperator;
import org.netbeans.jemmy.operators.TextFieldOperator;

public class MouseFocusDriver extends SupportiveDriver implements FocusDriver {
    public MouseFocusDriver() {
	super(new Class[] {
		JListOperator.class, 
		JScrollBarOperator.class, 
		JSliderOperator.class, 
		JTableOperator.class, 
		JTextComponentOperator.class, 
		JTreeOperator.class, 
		ListOperator.class, 
		ScrollbarOperator.class, 
		TextAreaOperator.class, 
		TextComponentOperator.class, 
		TextFieldOperator.class});
    }
    public void giveFocus(ComponentOperator oper) {
	if(!oper.hasFocus()) {
	    DriverManager.getMouseDriver(oper).
		clickMouse(oper, oper.getCenterX(), oper.getCenterY(),
			   1, oper.getDefaultMouseButton(), 0, 
			   oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
	}
    }
}
