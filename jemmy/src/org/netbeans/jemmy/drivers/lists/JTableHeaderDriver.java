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

package org.netbeans.jemmy.drivers.lists;

import java.awt.Point;

import java.awt.event.InputEvent;

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.OrderedListDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableHeaderOperator;

public class JTableHeaderDriver extends SupportiveDriver implements OrderedListDriver {
    private QueueTool queueTool;
    public JTableHeaderDriver() {
	super(new Class[] {JTableHeaderOperator.class});
        queueTool = new QueueTool();
    }
    public void selectItem(ComponentOperator oper, int index) {
	clickOnHeader((JTableHeaderOperator)oper, index);
    }
    public void selectItems(ComponentOperator oper, int[] indices) {
	clickOnHeader((JTableHeaderOperator)oper, indices[0]);
	for(int i = 1; i < indices.length; i++) {
	    clickOnHeader((JTableHeaderOperator)oper, indices[i], InputEvent.CTRL_MASK);
	}
    }
    public void moveItem(ComponentOperator oper, int moveColumn, int moveTo) {
        Point start = ((JTableHeaderOperator)oper).getPointToClick(moveColumn);
        Point   end = ((JTableHeaderOperator)oper).getPointToClick(moveTo);
        oper.dragNDrop(start.x, start.y, end.x, end.y);
    }
    protected void clickOnHeader(JTableHeaderOperator oper, int index) {
	clickOnHeader(oper, index, 0);
    }
    protected void clickOnHeader(final JTableHeaderOperator oper, final int index, final int modifiers) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Column selecting") {
                public Object launch() {
                    Point toClick = ((JTableHeaderOperator)oper).getPointToClick(index);
                    DriverManager.getMouseDriver(oper).
                        clickMouse(oper, 
                                   toClick.x,
                                   toClick.y,
                                   1, oper.getDefaultMouseButton(), modifiers,
                                   oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
    }
}
