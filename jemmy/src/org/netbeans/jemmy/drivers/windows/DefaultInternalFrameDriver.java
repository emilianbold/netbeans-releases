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

package org.netbeans.jemmy.drivers.windows;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.WindowEvent;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;

import org.netbeans.jemmy.drivers.FrameDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;
import org.netbeans.jemmy.drivers.WindowDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultInternalFrameDriver extends SupportiveDriver 
    implements WindowDriver, FrameDriver {
    public DefaultInternalFrameDriver() {
	super(new Class[] {JInternalFrameOperator.class});
    }
    public void activate(ComponentOperator oper) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).moveToFront();
	((JInternalFrameOperator)oper).getTitleOperator().clickMouse();
    }
    public void close(ComponentOperator oper) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).moveToFront();
	((JButtonOperator)((JInternalFrameOperator)oper).getCloseButton()).push();
    }
    public void move(ComponentOperator oper, int x, int y) {
	checkSupported(oper);
	ComponentOperator titleOperator = ((JInternalFrameOperator)oper).getTitleOperator();
	titleOperator.dragNDrop(titleOperator.getCenterY(),
				titleOperator.getCenterY(),
				x - ((JInternalFrameOperator)oper).getX() + titleOperator.getCenterY(),
				y - ((JInternalFrameOperator)oper).getY() + titleOperator.getCenterY());
    }
    public void resize(ComponentOperator oper, int width, int height) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).
	    dragNDrop(((JInternalFrameOperator)oper).getWidth() - 1,
		      ((JInternalFrameOperator)oper).getHeight() - 1,
		      width - 1,
		      height - 1);
    }
    public void iconify(ComponentOperator oper) {
	checkSupported(oper);
	((JButtonOperator)((JInternalFrameOperator)oper).getMinimizeButton()).clickMouse();
    }
    public void deiconify(ComponentOperator oper) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).getIconOperator().pushButton();
    }
    public void maximize(ComponentOperator oper) {
	checkSupported(oper);
	if(!((JInternalFrameOperator)oper).isMaximum()) {
	    if(!((JInternalFrameOperator)oper).isSelected()) {
		activate(oper);
	    }
	    ((JInternalFrameOperator)oper).getMaximizeButton().push();
	}
    }
    public void demaximize(ComponentOperator oper) {
	checkSupported(oper);
	if(((JInternalFrameOperator)oper).isMaximum()) {
	    if(!((JInternalFrameOperator)oper).isSelected()) {
		activate(oper);
	    }
	    ((JInternalFrameOperator)oper).getMaximizeButton().push();
	}
    }
}
