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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to simulate keyboard operations.
 */
public interface KeyDriver {

    /**
     * Presses a key.
     * @param oper Component operator.
     * @param keyCode Key code (<code>KeyEvent.VK_*</code> value)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void pressKey(ComponentOperator oper, int keyCode, int modifiers);

    /**
     * Releases a key.
     * @param oper Component operator.
     * @param keyCode Key code (<code>KeyEvent.VK_*</code> value)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void releaseKey(ComponentOperator oper, int keyCode, int modifiers);

    /**
     * Pushes a key.
     * @param oper Component operator.
     * @param keyCode Key code (<code>KeyEvent.VK_*</code> value)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     * @param pushTime Time between pressing and releasing.
     */
    public void pushKey(ComponentOperator oper, int keyCode, int modifiers, Timeout pushTime);

    /**
     * Types a symbol.
     * @param oper Component operator.
     * @param keyCode Key code (<code>KeyEvent.VK_*</code> value)
     * @param keyChar Symbol to be typed.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     * @param pushTime Time between pressing and releasing.
     */
    public void typeKey(ComponentOperator oper, int keyCode, char keyChar, int modifiers, Timeout pushTime);
}
