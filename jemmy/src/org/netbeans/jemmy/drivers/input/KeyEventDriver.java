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

/**
 * KeyDriver using event dispatching.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class KeyEventDriver extends EventDriver implements KeyDriver {

    /**
     * Constructs a KeyEventDriver object.
     * @param supported an array of supported class names
     */
    public KeyEventDriver(String[] supported) {
	super(supported);
    }
    /**
     * Constructs an KeyEventDriver object suporting ComponentOperator.
     */
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
