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

package org.netbeans.jemmy.drivers.lists;

import java.awt.Point;

import java.awt.event.InputEvent;

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.OrderedListDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableHeaderOperator;

/**
 * List driver for javax.swing.table.JTableHeader component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JTableHeaderDriver extends LightSupportiveDriver implements OrderedListDriver {
    private QueueTool queueTool;

    /**
     * Constructs a JTableHeaderDriver.
     */
    public JTableHeaderDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTableHeaderOperator"});
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

    /**
     * Clicks on a column header.
     * @param oper an operator to click on.
     * @param index column index.
     */
    protected void clickOnHeader(JTableHeaderOperator oper, int index) {
	clickOnHeader(oper, index, 0);
    }

    /**
     * Clicks on a column header.
     * @param oper an operator to click on.
     * @param index column index.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
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
