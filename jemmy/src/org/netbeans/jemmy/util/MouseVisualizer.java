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

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator.ComponentVisualizer;
import org.netbeans.jemmy.operators.WindowOperator;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * Activates windows by mouse click on (0,0) coordinates.
 * 
 * @see org.netbeans.jemmy.operators.Operator#setVisualizer(Operator.ComponentVisualizer)
 * @see org.netbeans.jemmy.operators.Operator.ComponentVisualizer
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */
public class MouseVisualizer extends DefaultVisualizer {

    public MouseVisualizer() {
    }

    protected void activate(WindowOperator winOper) 
	throws TimeoutExpiredException {
	if(winOper.getFocusOwner() == null) {
	    super.activate(winOper);
	    winOper.getEventDispatcher().checkComponentUnderMouse(false);
	    winOper.setDispatchingModel(winOper.getDispatchingModel() |
					JemmyProperties.ROBOT_MODEL_MASK);
	    winOper.clickMouse(0, 0, 1);
	}
    }
}
