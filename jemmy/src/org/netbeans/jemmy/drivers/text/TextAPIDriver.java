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

package org.netbeans.jemmy.drivers.text;

import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.TextDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.TextComponentOperator;

/**
 * Superclass for all TextDrivers using API calls.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public abstract class TextAPIDriver extends LightSupportiveDriver implements TextDriver {

    /**
     * Constructs a ChoiceDriver.
     * @param supported an array of supported class names
     */
    public TextAPIDriver(String[] supported) {
	super(supported);
    }

    public void changeCaretPosition(ComponentOperator oper, int position) {
	checkSupported(oper);
	if(oper instanceof TextComponentOperator) {
	    ((TextComponentOperator)oper).setCaretPosition(position);
	} else {
	    ((JTextComponentOperator)oper).setCaretPosition(position);
	}
    }

    public void selectText(ComponentOperator oper, int startPosition, int finalPosition) {
	checkSupported(oper);
	int start = (startPosition < finalPosition) ? startPosition : finalPosition;
	int end   = (startPosition > finalPosition) ? startPosition : finalPosition;
	if(oper instanceof TextComponentOperator) {
	    TextComponentOperator toper = ((TextComponentOperator)oper);
	    toper.setSelectionStart(start);
	    toper.setSelectionEnd(end);
	} else {
	    JTextComponentOperator toper = ((JTextComponentOperator)oper);
	    toper.setSelectionStart(start);
	    toper.setSelectionEnd(end);
	}
    }

    public void clearText(ComponentOperator oper) {
	if(oper instanceof TextComponentOperator) {
	    ((TextComponentOperator)oper).setText("");
	} else {
	    ((JTextComponentOperator)oper).setText("");
	}
    }

    public void typeText(ComponentOperator oper, String text, int caretPosition) {
	checkSupported(oper);
	String curtext = getText(oper);
	int realPos = caretPosition;
	if(getSelectionStart(oper) == realPos ||
	   getSelectionEnd(oper) == realPos) {
	    if(getSelectionEnd(oper) == realPos) {
		realPos = realPos - (getSelectionEnd(oper) - getSelectionStart(oper));
	    }
	    curtext = 
		curtext.substring(0, getSelectionStart(oper)) + 
		curtext.substring(getSelectionEnd(oper));
	}
	changeText(oper, 
		   curtext.substring(0, realPos) + text + 
		   curtext.substring(realPos));
    }

    public void changeText(ComponentOperator oper, String text) {
	checkSupported(oper);
	if(oper instanceof TextComponentOperator) {
	    ((TextComponentOperator)oper).setText(text);
	} else {
	    ((JTextComponentOperator)oper).setText(text);
	}
    }
    public void enterText(ComponentOperator oper, String text) {
	changeText(oper, text);
	DriverManager.getKeyDriver(oper).
	    pushKey(oper, KeyEvent.VK_ENTER, 0,
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
}
