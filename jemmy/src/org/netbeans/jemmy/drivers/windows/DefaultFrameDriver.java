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
import java.awt.Frame;
import java.awt.Window;

import java.awt.event.WindowEvent;

import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jemmy.drivers.SupportiveDriver;
import org.netbeans.jemmy.drivers.FrameDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultFrameDriver extends SupportiveDriver implements FrameDriver {
    EventDriver eDriver;
    public DefaultFrameDriver() {
	super(new Class[] {FrameOperator.class});
	eDriver = new EventDriver();
    }
    public void iconify(ComponentOperator oper) {
	checkSupported(oper);
 	eDriver.dispatchEvent(oper.getSource(), 
			  new WindowEvent((Window)oper.getSource(),
					  WindowEvent.WINDOW_ICONIFIED));
	((FrameOperator)oper).setState(Frame.ICONIFIED);
    }
    public void deiconify(ComponentOperator oper) {
	checkSupported(oper);
 	eDriver.dispatchEvent(oper.getSource(), 
			      new WindowEvent((Window)oper.getSource(),
					      WindowEvent.WINDOW_DEICONIFIED));
	((FrameOperator)oper).setState(Frame.NORMAL);
    }
    public void maximize(ComponentOperator oper) {
	checkSupported(oper);
	((FrameOperator)oper).setLocation(0, 0);
	Dimension ssize = Toolkit.getDefaultToolkit().getScreenSize();
	((FrameOperator)oper).setSize(ssize.width, ssize.height);
    }
    public void demaximize(ComponentOperator oper) {
	checkSupported(oper);
    }
}
