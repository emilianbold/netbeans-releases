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

/**
 * KeyDriver using robot operations.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class KeyRobotDriver extends RobotDriver implements KeyDriver {

    /**
     * Constructs a KeyRobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public KeyRobotDriver(Timeout autoDelay) {
	super(autoDelay);
    }

    /**
     * Constructs a KeyRobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     * @param supported an array of supported class names
     */
    public KeyRobotDriver(Timeout autoDelay, String[] supported) {
	super(autoDelay, supported);
    }

    public void pushKey(ComponentOperator oper, int keyCode, int modifiers, Timeout pushTime) {
	pressKey(oper, keyCode, modifiers);
	pushTime.sleep();
	releaseKey(oper, keyCode, modifiers);
    }

    public void typeKey(ComponentOperator oper, int keyCode, char keyChar, int modifiers, Timeout pushTime) {
	pushKey(oper, keyCode, modifiers, pushTime);
    }

}
