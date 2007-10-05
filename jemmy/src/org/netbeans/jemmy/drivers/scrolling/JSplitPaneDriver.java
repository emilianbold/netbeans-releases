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
