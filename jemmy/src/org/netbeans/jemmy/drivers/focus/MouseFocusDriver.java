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

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.FocusDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MouseDriver;

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

public class MouseFocusDriver extends LightSupportiveDriver implements FocusDriver {
    private QueueTool queueTool;
    public MouseFocusDriver() {
	super(new String[] {
		"org.netbeans.jemmy.operators.JListOperator", 
		"org.netbeans.jemmy.operators.JScrollBarOperator", 
		"org.netbeans.jemmy.operators.JSliderOperator", 
		"org.netbeans.jemmy.operators.JTableOperator", 
		"org.netbeans.jemmy.operators.JTextComponentOperator", 
		"org.netbeans.jemmy.operators.JTreeOperator", 
		"org.netbeans.jemmy.operators.ListOperator", 
		"org.netbeans.jemmy.operators.ScrollbarOperator", 
		"org.netbeans.jemmy.operators.TextAreaOperator", 
		"org.netbeans.jemmy.operators.TextComponentOperator", 
		"org.netbeans.jemmy.operators.TextFieldOperator"});
        queueTool = new QueueTool();
    }
    public void giveFocus(final ComponentOperator oper) {
	if(!oper.hasFocus()) {
            queueTool.invokeSmoothly(new QueueTool.QueueAction("Mouse click to get focus") {
                    public Object launch() {
                        DriverManager.getMouseDriver(oper).
                            clickMouse(oper, oper.getCenterXForClick(), oper.getCenterYForClick(),
                                       1, oper.getDefaultMouseButton(), 0, 
                                       oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
                        return(null);
                    }
                });
            oper.waitHasFocus();
	}
    }
}
