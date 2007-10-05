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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.KeyDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * TextDriver for swing text component types.
 * Uses keyboard operations.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class SwingTextKeyboardDriver extends TextKeyboardDriver {
    /**
     * Constructs a SwingTextKeyboardDriver.
     */
    public SwingTextKeyboardDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTextComponentOperator"});
    }
    public void clearText(ComponentOperator oper) {
	if(oper instanceof JTextAreaOperator ||
	   oper instanceof JEditorPaneOperator) {
	    DriverManager.getFocusDriver(oper).giveFocus(oper);
	    KeyDriver kdriver = DriverManager.getKeyDriver(oper);
	    selectText(oper, 0, getText(oper).length());
	    kdriver.pushKey(oper, KeyEvent.VK_DELETE, 0, 
			    oper.getTimeouts().create("ComponentOperator.PushKeyTimeout"));
	} else {
	    super.clearText(oper);
	}
    }
    public String getText(ComponentOperator oper) {
	return(((JTextComponentOperator)oper).getDisplayedText());
    }
    public int getCaretPosition(ComponentOperator oper) {
	return(((JTextComponentOperator)oper).getCaretPosition());
    }
    public int getSelectionStart(ComponentOperator oper) {
	return(((JTextComponentOperator)oper).getSelectionStart());
    }
    public int getSelectionEnd(ComponentOperator oper) {
	return(((JTextComponentOperator)oper).getSelectionEnd());
    }
    public NavigationKey[] getKeys(ComponentOperator oper) {
	boolean multiString = 
	    oper instanceof JTextAreaOperator ||
	    oper instanceof JEditorPaneOperator;
	NavigationKey[] result = new NavigationKey[multiString ? 8 : 4];
	result[0] = new UpKey  (KeyEvent.VK_LEFT , 0);
	result[1] = new DownKey(KeyEvent.VK_RIGHT, 0);
	((  UpKey)result[0]).setDownKey((DownKey)result[1]);
	((DownKey)result[1]).setUpKey  ((  UpKey)result[0]);
	if(multiString) {
	    result[2] = new UpKey  (KeyEvent.VK_UP  , 0);
	    result[3] = new DownKey(KeyEvent.VK_DOWN, 0);
	    ((  UpKey)result[2]).setDownKey((DownKey)result[3]);
	    ((DownKey)result[3]).setUpKey  ((  UpKey)result[2]);
	    result[4] = new UpKey  (KeyEvent.VK_PAGE_UP  , 0);
	    result[5] = new DownKey(KeyEvent.VK_PAGE_DOWN, 0);
	    ((  UpKey)result[4]).setDownKey((DownKey)result[5]);
	    ((DownKey)result[5]).setUpKey  ((  UpKey)result[4]);
	    result[6] = new HomeKey(KeyEvent.VK_HOME, InputEvent.CTRL_MASK);
	    result[7] = new  EndKey(KeyEvent.VK_END , InputEvent.CTRL_MASK, this, oper);
	} else {
	    result[2] = new HomeKey(KeyEvent.VK_HOME, 0);
	    result[3] = new  EndKey(KeyEvent.VK_END , 0, this, oper);
	}
	return(result);
    }
    public Timeout getBetweenTimeout(ComponentOperator oper) {
	return(oper.getTimeouts().create("TextComponentOperator.BetweenKeysTimeout"));
    }
}
