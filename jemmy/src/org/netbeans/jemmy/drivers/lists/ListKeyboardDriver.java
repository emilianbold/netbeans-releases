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

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.KeyDriver;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.MultiSelListDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ListOperator;

public class ListKeyboardDriver extends ListAPIDriver implements MultiSelListDriver {
    public ListKeyboardDriver() {
	super();
    }
    public void selectItem(ComponentOperator oper, int index) {
	ListOperator loper = (ListOperator)oper;
	if(loper.isMultipleMode()) {
	    super.selectItem(loper, index);
	}
	DriverManager.getFocusDriver(oper).giveFocus(oper);
	KeyDriver   kDriver = DriverManager.getKeyDriver(oper);
	int current = loper.getSelectedIndex();
	int diff = 0;
	int key = 0;
	if(index > current) {
	    diff = index - current;
	    key = KeyEvent.VK_DOWN;
	} else {
	    diff = current - index;
	    key = KeyEvent.VK_UP;
	}
	Timeout pushTime = oper.getTimeouts().create("ComponentOperator.PushKeyTimeout");
	for(int i = 0; i < diff; i++) {
	    kDriver.pushKey(oper, key, 0, pushTime);
	}
	kDriver.pushKey(oper, KeyEvent.VK_ENTER, 0, pushTime);
    }
}
