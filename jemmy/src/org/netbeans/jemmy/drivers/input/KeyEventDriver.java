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
    public KeyEventDriver(String[] supported) {
	super(supported);
    }
    public KeyEventDriver() {
	super();
    }
    public void pressKey(ComponentOperator oper, int keyCode, int modifiers) {
        pressKey(findNativeParent(oper.getSource()), keyCode, modifiers);
    }
    public void releaseKey(ComponentOperator oper, int keyCode, int modifiers) {
        releaseKey(findNativeParent(oper.getSource()), keyCode, modifiers);
    }
    public void pushKey(ComponentOperator oper, int keyCode, int modifiers, Timeout pushTime) {
        Component nativeContainer = findNativeParent(oper.getSource());
	pressKey(nativeContainer, keyCode, modifiers);
	pushTime.sleep();
	releaseKey(nativeContainer, keyCode, modifiers);
    }
    public void typeKey(ComponentOperator oper, int keyCode, char keyChar, int modifiers, Timeout pushTime) {
        Component nativeContainer = findNativeParent(oper.getSource());
	pressKey(nativeContainer, keyCode, modifiers);
	pushTime.sleep();
	dispatchEvent(nativeContainer,
		      new KeyEvent(nativeContainer, 
				   KeyEvent.KEY_TYPED, 
				   System.currentTimeMillis(), 
				   modifiers, KeyEvent.VK_UNDEFINED, keyChar));
	releaseKey(nativeContainer, keyCode, modifiers);
    }
    private void pressKey(Component nativeContainer, int keyCode, int modifiers) {
	dispatchEvent(nativeContainer,
		      new KeyEvent(nativeContainer, 
				   KeyEvent.KEY_PRESSED, 
				   System.currentTimeMillis(), 
				   modifiers, keyCode));
    }
    private void releaseKey(Component nativeContainer, int keyCode, int modifiers) {
	dispatchEvent(nativeContainer,
		      new KeyEvent(nativeContainer, 
				   KeyEvent.KEY_RELEASED, 
				   System.currentTimeMillis(), 
				   modifiers, keyCode));
    }
    private Component findNativeParent(Component source) {
        Component nativeOne = source;
        while(nativeOne != null) {
            if(!nativeOne.isLightweight()) {
                return(nativeOne);
            }
            nativeOne = nativeOne.getParent();
        }
        return(source);
    }
}
