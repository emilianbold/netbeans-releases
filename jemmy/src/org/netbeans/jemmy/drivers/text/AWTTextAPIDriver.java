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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.TextComponentOperator;

/**
 * TextDriver for AWT component types.
 * Uses API calls.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class AWTTextAPIDriver extends TextAPIDriver {

    /**
     * Constructs a AWTTextAPIDriver.
     */
    public AWTTextAPIDriver() {
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
}
