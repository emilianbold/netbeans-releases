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
public class DefaultVisualizer implements ComponentVisualizer {
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
     */
    public void activateWindow(boolean yesOrNo) {
	window = yesOrNo;
    }

    /**
     * Informs that an internal frame contained component
     * should be activated.
     */
    public void activateInternalFrame(boolean yesOrNo) {
	internalFrame = yesOrNo;
    }

    /**
     * Informs that scrolling should be made.
     */
    public void scroll(boolean yesOrNo) {
	scroll = yesOrNo;
    }

    /**
     * Informs that tab switching should be made.
     */
    public void switchTab(boolean yesOrNo) {
	switchTab = yesOrNo;
    }

    protected void activate(WindowOperator winOper) 
	throws TimeoutExpiredException {
	winOper.activate();
    }

    protected void initInternalFrame(JInternalFrameOperator intOper) {
	if(!intOper.isSelected()) {
	    intOper.activate();
	}
    }

    protected void scroll(JScrollPaneOperator scrollOper, Component target) {
	if(!scrollOper.checkInside(target)) {
	    scrollOper.scrollToComponent(target);
	}
    }

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
	    if(window) {
		WindowOperator winOper = new WindowOperator(compOper.getWindow());
		winOper.copyEnvironment(compOper);
		winOper.setVisualizer(new EmptyVisualizer());
		activate(winOper);
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
}
