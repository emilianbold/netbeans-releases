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
import java.awt.Window;

import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.WindowOperator;

import org.netbeans.jemmy.drivers.SupportiveDriver;
import org.netbeans.jemmy.drivers.WindowDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultWindowDriver extends SupportiveDriver implements WindowDriver {
    EventDriver eDriver;
    public DefaultWindowDriver() {
	super(new Class[] {WindowOperator.class});
	eDriver = new EventDriver();
    }
    public void activate(ComponentOperator oper) {
	checkSupported(oper);
 	if(((WindowOperator)oper).getFocusOwner() == null) {
 	    ((WindowOperator)oper).toFront();
 	}
 	eDriver.dispatchEvent(oper.getSource(), 
			      new WindowEvent((Window)oper.getSource(),
					      WindowEvent.WINDOW_ACTIVATED));
 	eDriver.dispatchEvent(oper.getSource(), 
			      new FocusEvent((Window)oper.getSource(),
                                             FocusEvent.FOCUS_GAINED));
    }
    public void close(ComponentOperator oper) {
	checkSupported(oper);
 	eDriver.dispatchEvent(oper.getSource(), 
			      new WindowEvent((Window)oper.getSource(),
					      WindowEvent.WINDOW_CLOSING));
	((WindowOperator)oper).setVisible(false);
    }
    public void move(ComponentOperator oper, int x, int y) {
	checkSupported(oper);
	((WindowOperator)oper).setLocation(x, y);
    }
    public void resize(ComponentOperator oper, int width, int height) {
	checkSupported(oper);
	((WindowOperator)oper).setSize(width, height);
    }
}
