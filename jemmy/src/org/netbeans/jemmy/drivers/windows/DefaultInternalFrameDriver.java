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

package org.netbeans.jemmy.drivers.windows;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.WindowEvent;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;

import org.netbeans.jemmy.drivers.FrameDriver;
import org.netbeans.jemmy.drivers.InternalFrameDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.WindowDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultInternalFrameDriver extends LightSupportiveDriver 
    implements WindowDriver, FrameDriver, InternalFrameDriver {
    public DefaultInternalFrameDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JInternalFrameOperator"});
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
    public Component getTitlePane(ComponentOperator operator) {
	ComponentSearcher cs = new ComponentSearcher((Container)operator.getSource());
	cs.setOutput(operator.getOutput().createErrorOutput());
	return(cs.findComponent(new ComponentChooser() {
					public boolean checkComponent(Component comp) {
					    if(System.getProperty("java.version").startsWith("1.2")) {
						return(comp.getClass().getName().endsWith("InternalFrameTitlePane"));
					    } else {
						return(comp instanceof BasicInternalFrameTitlePane);
					    }
					}
					public String getDescription() {
					    return("Title pane");
					}
				    }));
    }
}
