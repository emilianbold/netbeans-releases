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

package org.netbeans.jemmy.drivers.input;

import java.awt.Component;

import java.awt.event.MouseEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

public class MouseRobotDriver extends RobotDriver implements MouseDriver {
    public MouseRobotDriver(Timeout autoDelay) {
	super(autoDelay);
    }
    public MouseRobotDriver(Timeout autoDelay, String[] supported) {
	super(autoDelay, supported);
    }
    public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
	pressModifiers(oper, modifiers);
	makeAnOperation("mousePress", 
			new Object[] {new Integer(mouseButton)}, 
			new Class[] {Integer.TYPE});
    }
    public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
	makeAnOperation("mouseRelease", 
			new Object[] {new Integer(mouseButton)}, 
			new Class[] {Integer.TYPE});
	releaseModifiers(oper, modifiers);
    }
    public void moveMouse(ComponentOperator oper, int x, int y) {
	makeAnOperation("mouseMove", 
			new Object[] {new Integer(getAbsoluteX(oper, x)), 
				      new Integer(getAbsoluteY(oper, y))}, 
			new Class[] {Integer.TYPE, Integer.TYPE});
    }
    public void clickMouse(ComponentOperator oper, int x, int y, int clickCount, int mouseButton, 
			   int modifiers, Timeout mouseClick) {
	pressModifiers(oper, modifiers);
	moveMouse(oper, x, y);
	makeAnOperation("mousePress", 
			new Object[] {new Integer(mouseButton)}, 
			new Class[] {Integer.TYPE});
	for(int i = 1; i < clickCount; i++) {
	    makeAnOperation("mouseRelease", 
			    new Object[] {new Integer(mouseButton)}, 
			    new Class[] {Integer.TYPE});
	    makeAnOperation("mousePress", 
			    new Object[] {new Integer(mouseButton)}, 
			    new Class[] {Integer.TYPE});
	}
	mouseClick.sleep();
	makeAnOperation("mouseRelease", 
			new Object[] {new Integer(mouseButton)}, 
			new Class[] {Integer.TYPE});
	releaseModifiers(oper, modifiers);
    }
    public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
	moveMouse(oper, x, y);
    }
    public void dragNDrop(ComponentOperator oper, int start_x, int start_y, int end_x, int end_y, 
			  int mouseButton, int modifiers, Timeout before, Timeout after) {
	moveMouse(oper, start_x, start_y);
	pressMouse(oper, start_x, start_y, mouseButton, modifiers);
	before.sleep();
	moveMouse(oper, end_x, end_y);
	after.sleep();
	releaseMouse(oper, end_x, end_y, mouseButton, modifiers);
    }
    public void enterMouse(ComponentOperator oper) {
	moveMouse(oper, oper.getCenterXForClick(), oper.getCenterYForClick());
    }
    public void exitMouse(ComponentOperator oper) {
	//better not go anywhere
	//exit will be executed during the next
	//mouse move anyway.
	//	moveMouse(oper, -1, -1);
    }
    protected int getAbsoluteX(ComponentOperator oper, int x) {
	return(oper.getSource().getLocationOnScreen().x + x);
    }
    protected int getAbsoluteY(ComponentOperator oper, int y) {
	return(oper.getSource().getLocationOnScreen().y + y);
    }
}
