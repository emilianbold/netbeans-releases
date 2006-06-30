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

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator.ComponentVisualizer;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * 
 * Used as component visualizer by default.
 *
 * @see org.netbeans.jemmy.operators.Operator#setVisualizer(Operator.ComponentVisualizer)
 * @see org.netbeans.jemmy.operators.Operator.ComponentVisualizer
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */
public class DefaultVisualizer implements ComponentVisualizer, Cloneable {
    private boolean window = true;
    private boolean internalFrame = true;
    private boolean scroll = false;
    private boolean switchTab = false;
    private boolean modal = false;

    public DefaultVisualizer() {
    }

    /**
     * Forces vizualizer to check that component is
     * on the top modal dialog or no modal dialog
     * displayed.
     * @param yesOrNo If true, JemmyInputException will be throught
     * if component is not on the top modal dialog and a modal dialog
     * is dislayed.
     */
    public void checkForModal(boolean yesOrNo) {
	modal = yesOrNo;
    }

    /**
     * Informs that a window contained component should be activated.
     * @param yesOrNo true if windows need to be activated.
     */
    public void activateWindow(boolean yesOrNo) {
	window = yesOrNo;
    }

    /**
     * Informs that an internal frame contained component
     * should be activated.
     * @param yesOrNo true if internal frames need to be activated.
     */
    public void activateInternalFrame(boolean yesOrNo) {
	internalFrame = yesOrNo;
    }

    /**
     * Informs that scrolling should be made.
     * @param yesOrNo true if scroll panes need to be scrolled.
     */
    public void scroll(boolean yesOrNo) {
	scroll = yesOrNo;
    }

    /**
     * Informs that tab switching should be made.
     * @param yesOrNo true if tabbed panes need to be switched.
     */
    public void switchTab(boolean yesOrNo) {
	switchTab = yesOrNo;
    }

    /**
     * Returns true if window is active.
     * @param winOper an operator representing the window.
     * @return true is window is active.
     */
    protected boolean isWindowActive(WindowOperator winOper) {
        return(winOper.isFocused() && winOper.isActive());
    }

    /**
     * Performs an atomic window-activization precedure.
     * A window is sopposed to be prepared for the activization
     * (i.e. put "to front").
     * @param winOper an operator representing the window.
     */
    protected void makeWindowActive(WindowOperator winOper) {
        winOper.activate();
    }

    /**
     * Activates a window. Uses makeWindowActive if necessary.
     * @param winOper an operator representing the window.
     * @see #makeWindowActive
     */
    protected void activate(WindowOperator winOper) {
        boolean active = isWindowActive(winOper);
	winOper.toFront();
        if(!active) {
            makeWindowActive(winOper);
        }
    }

    /**
     * Inits an internal frame.
     * @param intOper an operator representing the frame.
     */
    protected void initInternalFrame(JInternalFrameOperator intOper) {
	if(!intOper.isSelected()) {
	    intOper.activate();
	}
    }

    /**
     * Scrolls JScrollPane to make the component visible.
     * @param scrollOper an operator representing a scroll pane.
     * @param target a component - target to be made visible.
     */
    protected void scroll(JScrollPaneOperator scrollOper, Component target) {
	if(!scrollOper.checkInside(target)) {
	    scrollOper.scrollToComponent(target);
	}
    }

    /**
     * Switches tabs to make the component visible.
     * @param tabOper an operator representing a tabbed pane.
     * @param target a component - target to be made visible.
     */
    protected void switchTab(JTabbedPaneOperator tabOper, Component target) {
	int tabInd = 0;
	for(int j = 0; j < tabOper.getTabCount(); j++) {
	    if(target == tabOper.getComponentAt(j)) {
		tabInd = j;
		break;
	    }
	}
	if(tabOper.getSelectedIndex() != tabInd) {
	    tabOper.selectPage(tabInd);
	}
    }

    /**
     * Prepares the component for user input.
     * @param compOper an operator representing the component.
     * @throws JemmyInputException
     * @see #checkForModal(boolean)
     */
    public void makeVisible(ComponentOperator compOper) {
	try {
	    if(modal) {
		Dialog modalDialog = JDialogOperator.getTopModalDialog();
		if(modalDialog != null &&
		   compOper.getWindow() != modalDialog) {
		    throw(new JemmyInputException("Component is not on top modal dialog.", 
						  compOper.getSource()));
		}
	    }
            WindowOperator winOper = new WindowOperator(compOper.getWindow());
	    if(window) {
		winOper.copyEnvironment(compOper);
		winOper.setVisualizer(new EmptyVisualizer());
		activate(winOper);
	    }
            if(internalFrame && compOper instanceof JInternalFrameOperator) {
                initInternalFrame((JInternalFrameOperator)compOper);
            }
	    Container[] conts = compOper.getContainers();
	    for(int i = conts.length - 1; i >=0 ; i--) {
		if       (internalFrame && conts[i] instanceof JInternalFrame) {
		    JInternalFrameOperator intOper = new JInternalFrameOperator((JInternalFrame)conts[i]);
		    intOper.copyEnvironment(compOper);
		    intOper.setVisualizer(new EmptyVisualizer());
		    initInternalFrame(intOper);
		} else if(scroll        && conts[i] instanceof JScrollPane) {
		    JScrollPaneOperator scrollOper = new JScrollPaneOperator((JScrollPane)conts[i]);
		    scrollOper.copyEnvironment(compOper);
		    scrollOper.setVisualizer(new EmptyVisualizer());
		    scroll(scrollOper, compOper.getSource());
		} else if(switchTab     && conts[i] instanceof JTabbedPane) {
		    JTabbedPaneOperator tabOper = new JTabbedPaneOperator((JTabbedPane)conts[i]);
		    tabOper.copyEnvironment(compOper);
		    tabOper.setVisualizer(new EmptyVisualizer());
		    switchTab(tabOper, i == 0 ? compOper.getSource() : conts[i - 1]);
		}
	    }
	} catch(TimeoutExpiredException e) {
	    JemmyProperties.getProperties().getOutput().printStackTrace(e);
	}
    }

    /**
     * Creates an exact copy of this visualizer.
     * @return new instance.
     */
    public DefaultVisualizer cloneThis() {
        try {
            return((DefaultVisualizer)super.clone());
        } catch(CloneNotSupportedException e) {
            //that's impossible
            throw(new JemmyException("Even impossible happens :)", e));
        }
    }

}
