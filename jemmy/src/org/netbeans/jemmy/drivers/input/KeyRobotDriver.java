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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.KeyDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

public class KeyRobotDriver extends RobotDriver implements KeyDriver {

    public KeyRobotDriver(Timeout autoDelay) {
	super(autoDelay);
    }

    /**
     * Pushs key.
     * @param keyCode Key code (KeyEvent.VK_* value)
     * @param modifiers Modifiers (combination of InputEvent.*_MASK fields)
     */
    public void pushKey(ComponentOperator oper, int keyCode, int modifiers, Timeout pushTime) {
	pressKey(oper, keyCode, modifiers);
	pushTime.sleep();
	releaseKey(oper, keyCode, modifiers);
    }

    /**
     * Types one char.
     * @param keyCode Key code (KeyEvent.VK_* value)
     * @param keyChar Char to be typed.
     * @param modifiers Modifiers (combination of InputEvent.*_MASK fields)
     */
    public void typeKey(ComponentOperator oper, int keyCode, char keyChar, int modifiers, Timeout pushTime) {
	pushKey(oper, keyCode, modifiers, pushTime);
    }

}
