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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Frame;
import java.awt.Window;

import java.awt.event.WindowEvent;

import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jemmy.drivers.FrameDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultFrameDriver extends LightSupportiveDriver implements FrameDriver {
    EventDriver eDriver;
    public DefaultFrameDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.FrameOperator"});
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
