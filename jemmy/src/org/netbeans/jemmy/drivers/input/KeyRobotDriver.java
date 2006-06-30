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

    /**
     * Presses a key.
     * @param oper Operator to press a key on.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void pressKey(ComponentOperator oper, int keyCode, int modifiers) {
        pressKey(keyCode, modifiers);
    }

    /**
     * Releases a key.
     * @param oper Operator to release a key on.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void releaseKey(ComponentOperator oper, int keyCode, int modifiers) {
        releaseKey(keyCode, modifiers);
    }
}
