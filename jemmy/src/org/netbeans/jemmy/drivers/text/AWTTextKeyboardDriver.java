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

package org.netbeans.jemmy.drivers.text;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.TextAreaOperator;
import org.netbeans.jemmy.operators.TextComponentOperator;

/**
 * TextDriver for AWT text component types.
 * Uses keyboard operations.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class AWTTextKeyboardDriver extends TextKeyboardDriver {
    /**
     * Constructs a AWTTextKeyboardDriver.
     */
    public AWTTextKeyboardDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.TextComponentOperator"});
    }
    public String getText(ComponentOperator oper) {
	return(((TextComponentOperator)oper).getText());
    }
    public int getCaretPosition(ComponentOperator oper) {
	return(((TextComponentOperator)oper).getCaretPosition());
    }
    public int getSelectionStart(ComponentOperator oper) {
	return(((TextComponentOperator)oper).getSelectionStart());
    }
    public int getSelectionEnd(ComponentOperator oper) {
	return(((TextComponentOperator)oper).getSelectionEnd());
    }
    public NavigationKey[] getKeys(ComponentOperator oper) {
	boolean multiString = oper instanceof TextAreaOperator;
	NavigationKey[] result = new NavigationKey[multiString ? 4 : 2];
	result[0] = new UpKey  (KeyEvent.VK_LEFT , 0);
	result[1] = new DownKey(KeyEvent.VK_RIGHT, 0);
	((  UpKey)result[0]).setDownKey((DownKey)result[1]);
	((DownKey)result[1]).setUpKey  ((UpKey  )result[0]);
	if(multiString) {
	    result[2] = new UpKey  (KeyEvent.VK_UP  , 0);
	    result[3] = new DownKey(KeyEvent.VK_DOWN, 0);
	    ((  UpKey)result[2]).setDownKey((DownKey)result[3]);
	    ((DownKey)result[3]).setUpKey  ((UpKey  )result[2]);
	}
	return(result);
    }
    public Timeout getBetweenTimeout(ComponentOperator oper) {
	return(oper.getTimeouts().create("TextComponentOperator.BetweenKeysTimeout"));
    }
}
