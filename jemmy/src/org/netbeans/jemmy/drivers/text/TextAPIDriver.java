/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
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
