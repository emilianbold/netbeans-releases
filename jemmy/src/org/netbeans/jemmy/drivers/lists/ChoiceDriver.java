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

package org.netbeans.jemmy.drivers.lists;

import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.KeyDriver;
import org.netbeans.jemmy.drivers.ListDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ChoiceOperator;

public class ChoiceDriver extends SupportiveDriver implements ListDriver {
    public ChoiceDriver() {
	super(new Class[] {ChoiceOperator.class});
    }
    public void selectItem(ComponentOperator oper, int index) {
	int current = ((ChoiceOperator)oper).getSelectedIndex();
	int diff = 0;
	int key = 0;
	if(index > current) {
	    diff = index - current;
	    key = KeyEvent.VK_DOWN;
	} else {
	    diff = current - index;
	    key = KeyEvent.VK_UP;
	}
	DriverManager.getMouseDriver(oper).
	    clickMouse(oper, oper.getCenterXForClick(), oper.getCenterYForClick(),
		       1, oper.getDefaultMouseButton(), 0,
		       oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
	KeyDriver kdriver = DriverManager.getKeyDriver(oper);
	Timeout pushTimeout = oper.getTimeouts().create("ComponentOperator.PushKeyTimeout");
	for(int i = 0; i < diff; i++) {
	    kdriver.pushKey(oper, key, 0, pushTimeout);
	}
	kdriver.pushKey(oper, KeyEvent.VK_ENTER, 0, pushTimeout);
    }
}
