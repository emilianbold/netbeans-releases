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

package org.netbeans.jemmy.drivers.tables;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.TableDriver;
import org.netbeans.jemmy.drivers.TextDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

public class JTableMouseDriver extends LightSupportiveDriver implements TableDriver {
    QueueTool queueTool;
    public JTableMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JTableOperator"});;
	queueTool = new QueueTool();
    }
    public void selectCell(ComponentOperator oper, int row, int column) {
	clickOnCell((JTableOperator)oper, row, column, 1);
    }
    public void editCell(ComponentOperator oper, int row, int column, Object value) {
	JTableOperator toper = (JTableOperator)oper;
	toper.scrollToCell(row, column);
	if(!toper.isEditing() ||
	   toper.getEditingRow() != row ||
	   toper.getEditingColumn() != column) {
	    clickOnCell((JTableOperator)oper, row, column, 2);
	}
	JTextComponentOperator textoper = 
	    new JTextComponentOperator((JTextComponent)toper.
				       waitSubComponent(new JTextComponentOperator.
							JTextComponentFinder()));
	TextDriver text = DriverManager.getTextDriver(JTextComponentOperator.class);
	text.clearText(textoper);
	text.typeText(textoper, value.toString(), 0);
	DriverManager.getKeyDriver(oper).
	    pushKey(textoper, KeyEvent.VK_ENTER, 0,
		    oper.getTimeouts().
		    create("ComponentOperator.PushKeyTimeout"));
    }
    protected void clickOnCell(final JTableOperator oper, final int row, final int column, final int clickCount) {
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    Point point = oper.getPointToClick(row, column);
                    DriverManager.getMouseDriver(oper).
                        clickMouse(oper, point.x, point.y, clickCount, 
                                   oper.getDefaultMouseButton(), 
                                   0, 
                                   oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
    }
}

