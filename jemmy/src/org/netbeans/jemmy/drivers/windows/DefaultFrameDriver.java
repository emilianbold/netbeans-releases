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
