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

import java.awt.Point;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import javax.swing.tree.TreePath;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;
import org.netbeans.jemmy.drivers.TextDriver;
import org.netbeans.jemmy.drivers.TreeDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

public class JTreeMouseDriver extends SupportiveDriver implements TreeDriver {
    public JTreeMouseDriver() {
	super(new Class[] {JTreeOperator.class});
    }
    public void selectItem(ComponentOperator oper, int index) {
	selectItems(oper, new int[] {index});
    }
    public void selectItems(ComponentOperator oper, int[] indices) {
	checkSupported(oper);
	MouseDriver mdriver = DriverManager.getMouseDriver(oper.getClass());
	JTreeOperator toper = (JTreeOperator)oper;
	Point p = toper.getPointToClick(indices[0]);
	Timeout clickTime = oper.getTimeouts().create("ComponentOperator.MouseClickTimeout");
	mdriver.clickMouse(oper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
			   0, clickTime);
	for(int i = 1; i < indices.length; i++) {
	    p = toper.getPointToClick(indices[i]);
	    toper.scrollToRow(indices[i]);
	    mdriver.clickMouse(oper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
			       InputEvent.CTRL_MASK, clickTime);
	}
    }
    public void expandItem(ComponentOperator oper, int index) {
	checkSupported(oper);
	JTreeOperator toper = (JTreeOperator)oper;
	MouseDriver mdriver = DriverManager.getMouseDriver(oper.getClass());
	if(!toper.isExpanded(index)) {
	    Point p = toper.getPointToClick(index);
	    mdriver.clickMouse(oper, p.x, p.y, 2, Operator.getDefaultMouseButton(),
			       0, oper.getTimeouts().
			       create("ComponentOperator.MouseClickTimeout"));
	}
    }

    public void collapseItem(ComponentOperator oper, int index) {
	checkSupported(oper);
	JTreeOperator toper = (JTreeOperator)oper;
	MouseDriver mdriver = DriverManager.getMouseDriver(oper.getClass());
	if(toper.isExpanded(index)) {
	    Point p = toper.getPointToClick(index);
	    mdriver.clickMouse(oper, p.x, p.y, 2, Operator.getDefaultMouseButton(),
			       0, oper.getTimeouts().
			       create("ComponentOperator.MouseClickTimeout"));
	}
    }

    public void editItem(ComponentOperator oper, int index, Object newValue, Timeout waitEditorTime) {
	checkSupported(oper);
	JTreeOperator toper = (JTreeOperator)oper;
	Point p = toper.getPointToClick(index);
	MouseDriver mdriver = DriverManager.getMouseDriver(oper.getClass());
	mdriver.clickMouse(oper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
			   0, oper.getTimeouts().
			   create("ComponentOperator.MouseClickTimeout"));
	oper.getTimeouts().sleep("JTreeOperator.BeforeEditTimeout");
	mdriver.clickMouse(oper, p.x, p.y, 1, Operator.getDefaultMouseButton(),
			   0, oper.getTimeouts().
			   create("ComponentOperator.MouseClickTimeout"));
	toper.getTimeouts().
	    setTimeout("ComponentOperator.WaitComponentTimeout", waitEditorTime.getValue());
	JTextComponentOperator textoper = 
	    new JTextComponentOperator((JTextComponent)toper.
				       waitSubComponent(new JTextComponentOperator.
							JTextComponentFinder()));
	TextDriver text = DriverManager.getTextDriver(JTextComponentOperator.class);
	text.clearText(textoper);
	text.typeText(textoper, newValue.toString(), 0);
	DriverManager.getKeyDriver(oper.getClass()).
	    pushKey(textoper, KeyEvent.VK_ENTER, 0,
		    oper.getTimeouts().
		    create("ComponentOperator.PushKeyTimeout"));
    }

}
