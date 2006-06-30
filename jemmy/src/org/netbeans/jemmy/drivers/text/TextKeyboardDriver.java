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

import org.netbeans.jemmy.CharBindingMap;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.KeyDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.TextDriver;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Superclass for all TextDrivers using keyboard.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public abstract class TextKeyboardDriver extends LightSupportiveDriver implements TextDriver {
    /**
     * Constructs a TextKeyboardDriver.
     * @param supported an array of supported class names
     */
    public TextKeyboardDriver(String[] supported) {
	super(supported);
    }
    public void changeCaretPosition(ComponentOperator oper, int position) {
	DriverManager.getFocusDriver(oper).giveFocus(oper);
	checkSupported(oper);
	changeCaretPosition(oper, position, 0);
    }
    public void selectText(ComponentOperator oper, int startPosition, int finalPosition) {
	changeCaretPosition(oper, startPosition);
	DriverManager.getKeyDriver(oper).pressKey(oper, KeyEvent.VK_SHIFT, 0);
	changeCaretPosition(oper, finalPosition, InputEvent.SHIFT_MASK);
	DriverManager.getKeyDriver(oper).releaseKey(oper, KeyEvent.VK_SHIFT, 0);
    }
    public void clearText(ComponentOperator oper) {
	DriverManager.getFocusDriver(oper).giveFocus(oper);
	checkSupported(oper);
	KeyDriver kdriver = DriverManager.getKeyDriver(oper);
	Timeout pushTime = oper.getTimeouts().create("ComponentOperator.PushKeyTimeout");
	Timeout betweenTime = getBetweenTimeout(oper);
	while(getCaretPosition(oper) > 0) {
	    kdriver.typeKey(oper, KeyEvent.VK_BACK_SPACE, (char)KeyEvent.VK_BACK_SPACE, 0, pushTime);
	    betweenTime.sleep();
	}
	while(getText(oper).length() > 0) {
	    kdriver.pushKey(oper, KeyEvent.VK_DELETE, 0, pushTime);
	    betweenTime.sleep();
	}
    }
    public void typeText(ComponentOperator oper, String text, int caretPosition) {
	changeCaretPosition(oper, caretPosition);
	KeyDriver kDriver = DriverManager.getKeyDriver(oper);
	CharBindingMap map = oper.getCharBindingMap();
	Timeout pushTime = oper.getTimeouts().create("ComponentOperator.PushKeyTimeout");
	Timeout betweenTime = getBetweenTimeout(oper);
	char[] crs = text.toCharArray();
	for(int i = 0; i < crs.length; i++) {
	    kDriver.typeKey(oper, map.getCharKey(crs[i]), crs[i], map.getCharModifiers(crs[i]), pushTime);
	    betweenTime.sleep();
	}
    }
    public void changeText(ComponentOperator oper, String text) {
	clearText(oper);
	typeText(oper, text, 0);
    }
    public void enterText(ComponentOperator oper, String text) {
	changeText(oper, text);
	DriverManager.getKeyDriver(oper).pushKey(oper, KeyEvent.VK_ENTER, 0,
					     new Timeout("", 0));
    }

    /**
     * Returns operator's text.
     * @param oper an operator.
     * @return string representing component text.
     */
    public abstract String getText(ComponentOperator oper);

    /**
     * Returns current caret position.
     * @param oper an operator.
     * @return int represnting current operator's caret position.
     */
    public abstract int getCaretPosition(ComponentOperator oper);

    /**
     * Returns a caret position of selection start.
     * @param oper an operator.
     * @return int represnting index of operator's selection start.
     */
    public abstract int getSelectionStart(ComponentOperator oper);

    /**
     * Returns a caret position of selection end.
     * @param oper an operator.
     * @return int represnting index of operator's selection end.
     */
    public abstract int getSelectionEnd(ComponentOperator oper);

    /**
     * Returns an array of navigation keys.
     * @param oper an operator.
     * @return an array on NavigationKey instances.
     */
    public abstract NavigationKey[] getKeys(ComponentOperator oper);

    /**
     * Returns a timeout to sleep between text typing and caret operations.
     * @param oper an operator.
     * @return a Timeout instance.
     */
    public abstract Timeout getBetweenTimeout(ComponentOperator oper);

    /**
     * Changes current caret position to specifyed.
     * @param oper an operator.
     * @param position new caret position
     * @param preModifiers a modifiers (combination of <code>InputEvent.*_MASK</code> fields)
     * pushed before caret moving (like shift during text selection).
     */
    protected void changeCaretPosition(ComponentOperator oper, final int position, final int preModifiers){
	NavigationKey[] keys = getKeys(oper);
	for(int i = keys.length - 1; i >=0; i--) {
	    if(keys[i] instanceof OffsetKey) {
		moveCaret(oper, (OffsetKey)keys[i], position, preModifiers);
	    } else {
		moveCaret(oper, (GoAndBackKey)keys[i], position, preModifiers);
	    }
	}
    }
    private int difference(int one, int two) {
	if(one >= two) {
	    return(one - two);
	} else {
	    return(two - one);
	}
    }
    private void push(ComponentOperator oper, NavigationKey key, int preModifiers) {
	DriverManager.getKeyDriver(oper).
	    pushKey(oper, key.getKeyCode(), key.getModifiers() | preModifiers,
		    oper.getTimeouts().create("ComponentOperator.PushKeyTimeout"));
	getBetweenTimeout(oper).sleep();
    }
    private final void moveCaret(ComponentOperator oper, GoAndBackKey key, int position, int preModifiers) {
	int newDiff = difference(position, getCaretPosition(oper));
	int oldDiff = newDiff;
	QueueTool qTool = new QueueTool();
	qTool.setOutput(oper.getOutput().createErrorOutput());
	while(key.getDirection() * (position - getCaretPosition(oper)) > 0) {
	    oldDiff = newDiff;
	    push(oper, key, preModifiers);
	    qTool.waitEmpty();
	    newDiff = difference(position, getCaretPosition(oper));
	    if(newDiff == oldDiff) {
		return;
	    }
	};
	if(newDiff > oldDiff) {
	    push(oper, key.getBackKey(), preModifiers);
	}
    }
    private final void moveCaret(ComponentOperator oper, OffsetKey key, int position, int preModifiers) {
	if(gotToGo(oper, position, key.getExpectedPosition())) {
	    push(oper, key, preModifiers);
	}
    }
    private boolean gotToGo(ComponentOperator oper, int point, int offset) {
	return(difference(point, offset) < difference(point, getCaretPosition(oper)));
    }
}
