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

package org.netbeans.jemmy.drivers.trees;

import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import javax.swing.tree.TreePath;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.SupportiveDriver;
import org.netbeans.jemmy.drivers.TextDriver;
import org.netbeans.jemmy.drivers.TreeDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

public class JTreeAPIDriver extends SupportiveDriver implements TreeDriver {
    public JTreeAPIDriver() {
	super(new Class[] {JTreeOperator.class});
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
