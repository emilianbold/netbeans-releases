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
import java.awt.Window;

import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.WindowOperator;

import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.WindowDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultWindowDriver extends LightSupportiveDriver implements WindowDriver {
    EventDriver eDriver;
    public DefaultWindowDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.WindowOperator"});
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
 	eDriver.dispatchEvent(oper.getSource(), 
			      new ComponentEvent((Window)oper.getSource(),
                                                 ComponentEvent.COMPONENT_RESIZED));
    }
}
