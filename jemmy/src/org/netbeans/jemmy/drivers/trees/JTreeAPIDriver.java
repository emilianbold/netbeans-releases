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

package org.netbeans.jemmy.drivers.trees;

import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import javax.swing.tree.TreePath;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.TextDriver;
import org.netbeans.jemmy.drivers.TreeDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * TreeDriver for javax.swing.JTree component type.
 * Uses API calls.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JTreeAPIDriver extends LightSupportiveDriver implements TreeDriver {
    /**
     * Constructs a JTreeAPIDriver.
     */
    public JTreeAPIDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTreeOperator"});
    }

    public void selectItem(ComponentOperator oper, int index) {
	selectItems(oper, new int[] {index});
    }

    public void selectItems(ComponentOperator oper, int[] indices) {
	checkSupported(oper);
	((JTreeOperator)oper).clearSelection();
	((JTreeOperator)oper).addSelectionRows(indices);
    }

    public void expandItem(ComponentOperator oper, int index) {
	checkSupported(oper);
	((JTreeOperator)oper).expandRow(index);
    }

    public void collapseItem(ComponentOperator oper, int index) {
	checkSupported(oper);
	((JTreeOperator)oper).collapseRow(index);
    }

    public void editItem(ComponentOperator oper, int index, Object newValue, Timeout waitEditorTime) {
	JTextComponentOperator textoper = startEditingAndReturnEditor(oper, index, waitEditorTime);
	TextDriver text = DriverManager.getTextDriver(JTextComponentOperator.class);
	text.clearText(textoper);
	text.typeText(textoper, newValue.toString(), 0);
	((JTreeOperator)oper).stopEditing();
    }

    public void startEditing(ComponentOperator oper, int index, Timeout waitEditorTime) {
	startEditing(oper, index, waitEditorTime);
    }

    private JTextComponentOperator startEditingAndReturnEditor(ComponentOperator oper, int index, Timeout waitEditorTime) {
	checkSupported(oper);
	JTreeOperator toper = (JTreeOperator)oper;
	toper.startEditingAtPath(toper.getPathForRow(index));
	toper.getTimeouts().
	    setTimeout("ComponentOperator.WaitComponentTimeout", waitEditorTime.getValue());
	return(new JTextComponentOperator((JTextComponent)toper.
					  waitSubComponent(new JTextComponentOperator.
							   JTextComponentFinder())));
    }
}
