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
