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

package org.netbeans.jemmy.drivers.scrolling;

import javax.swing.JButton;
import javax.swing.JSplitPane;

import org.netbeans.jemmy.ComponentSearcher;

import org.netbeans.jemmy.drivers.ButtonDriver;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.ScrollDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;

/**
 * ScrollDriver for javax.swing.JSplitPane component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JSplitPaneDriver extends LightSupportiveDriver implements ScrollDriver {

    /**
     * Constructs a JSplitPaneDriver.
     */
    public JSplitPaneDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JSplitPaneOperator"});
    }

    public void scroll(ComponentOperator oper, ScrollAdjuster adj) {
	moveDividerTo((JSplitPaneOperator)oper, adj);
    }

    public void scrollToMinimum(ComponentOperator oper, int orientation) {
	expandTo((JSplitPaneOperator)oper, 0);
    }

    public void scrollToMaximum(ComponentOperator oper, int orientation) {
	expandTo((JSplitPaneOperator)oper, 1);
    }

    private void moveDividerTo(JSplitPaneOperator oper, ScrollAdjuster adj) {
	ContainerOperator divOper = oper.getDivider();
	/* workaround */
	if(oper.getDividerLocation() == -1) {
	    moveTo(oper, divOper, divOper.getCenterX() - 1, divOper.getCenterY() - 1);
	    if(oper. getDividerLocation() == -1) {
		moveTo(oper, divOper, divOper.getCenterX() + 1, divOper.getCenterY() + 1);
	    }
	}
	if(oper.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    moveOnce(oper, divOper, adj, 0, oper.getWidth());
	} else {
	    moveOnce(oper, divOper, adj, 0, oper.getHeight());
	}
    }

    private void moveOnce(JSplitPaneOperator oper, 
			  ContainerOperator divOper,
			  ScrollAdjuster adj, 
			  int leftPosition, 
			  int rightPosition) {
	int currentLocation = oper.getDividerLocation();
	int currentPosition = 0;
	if(oper.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    currentPosition = (int)(divOper.getLocationOnScreen().getX() -
				    oper.getLocationOnScreen().getX());
	} else {
	    currentPosition = (int)(divOper.getLocationOnScreen().getY() -
				    oper.getLocationOnScreen().getY());
	}
	int nextPosition = 0;
	if       (adj.getScrollDirection() == adj.DECREASE_SCROLL_DIRECTION) {
	    nextPosition = (int)((currentPosition + leftPosition) / 2);
	    moveToPosition(oper, divOper, nextPosition - currentPosition);
	    if(currentPosition == (int)(divOper.getLocationOnScreen().getY() -
					oper.getLocationOnScreen().getY())) {
		return;
	    }
	    moveOnce(oper, divOper, adj, leftPosition, currentPosition);
	} else if(adj.getScrollDirection() == adj.INCREASE_SCROLL_DIRECTION) {
	    nextPosition = (int)((currentPosition + rightPosition) / 2);
	    moveToPosition(oper, divOper, nextPosition - currentPosition);
	    if(currentPosition == (int)(divOper.getLocationOnScreen().getY() -
					oper.getLocationOnScreen().getY())) {
		return;
	    }
	    moveOnce(oper, divOper, adj, currentPosition, rightPosition);
	} else { // (currentLocation == dividerLocation) - stop point
	    return;
	}
    }

    private void moveTo(JSplitPaneOperator oper, ComponentOperator divOper, int x, int y) {
	DriverManager.getMouseDriver(divOper).
	    dragNDrop(divOper, divOper.getCenterX(), divOper.getCenterY(), x, y,
		      oper.getDefaultMouseButton(), 0, 
		      oper.getTimeouts().create("ComponentOperator.BeforeDragTimeout"), 
		      oper.getTimeouts().create("ComponentOperator.AfterDragTimeout"));
    }

    private void moveToPosition(JSplitPaneOperator oper, ComponentOperator divOper, int nextPosition) {
	if(System.getProperty("java.version").startsWith("1.2")) {
	    oper.setDividerLocation(nextPosition);
	}
	if(oper.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
	    moveTo(oper, divOper, divOper.getCenterX() + nextPosition, divOper.getCenterY());
	} else {
	    moveTo(oper, divOper, divOper.getCenterX(), divOper.getCenterY() + nextPosition);
	}
    }

    private void expandTo(JSplitPaneOperator oper, int index) {
	ContainerOperator divOper = oper.getDivider();
	JButtonOperator bo = 
	    new JButtonOperator((JButton)divOper.
				waitSubComponent(new JButtonOperator.
						 JButtonFinder(ComponentSearcher.
							       getTrueChooser("JButton")),
						 index));
	bo.copyEnvironment(divOper);
	ButtonDriver bdriver = DriverManager.getButtonDriver(bo);
	bdriver.push(bo);
	bdriver.push(bo);
    }
}
