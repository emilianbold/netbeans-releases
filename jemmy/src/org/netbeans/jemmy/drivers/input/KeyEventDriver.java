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

package org.netbeans.jemmy.drivers.input;

import java.awt.AWTEvent;
import java.awt.Component;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.ComponentIsNotVisibleException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.KeyDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

public class KeyEventDriver extends EventDriver implements KeyDriver {
    public KeyEventDriver() {
	super();
    }
    public void pressKey(ComponentOperator oper, int keyCode, int modifiers) {
	dispatchEvent(oper.getSource(),
		      new KeyEvent(oper.getSource(), 
				   KeyEvent.KEY_PRESSED, 
				   System.currentTimeMillis(), 
				   modifiers, keyCode));
    }
    public void releaseKey(ComponentOperator oper, int keyCode, int modifiers) {
	dispatchEvent(oper.getSource(),
		      new KeyEvent(oper.getSource(), 
				   KeyEvent.KEY_RELEASED, 
				   System.currentTimeMillis(), 
				   modifiers, keyCode));
    }
    public void pushKey(ComponentOperator oper, int keyCode, int modifiers, Timeout pushTime) {
	pressKey(oper, keyCode, modifiers);
	pushTime.sleep();
	releaseKey(oper, keyCode, modifiers);
    }
    public void typeKey(ComponentOperator oper, int keyCode, char keyChar, int modifiers, Timeout pushTime) {
	pressKey(oper, keyCode, modifiers);
	pushTime.sleep();
	dispatchEvent(oper.getSource(),
		      new KeyEvent(oper.getSource(), 
				   KeyEvent.KEY_TYPED, 
				   System.currentTimeMillis(), 
				   modifiers, KeyEvent.VK_UNDEFINED, keyChar));
	releaseKey(oper, keyCode, modifiers);
    }
}
