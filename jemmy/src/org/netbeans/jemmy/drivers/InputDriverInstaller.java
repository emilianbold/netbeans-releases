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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseEventDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;

import org.netbeans.jemmy.operators.ButtonOperator;
import org.netbeans.jemmy.operators.CheckboxOperator;
import org.netbeans.jemmy.operators.ChoiceOperator;
import org.netbeans.jemmy.operators.LabelOperator;
import org.netbeans.jemmy.operators.ListOperator;
import org.netbeans.jemmy.operators.ScrollPaneOperator;
import org.netbeans.jemmy.operators.ScrollbarOperator;
import org.netbeans.jemmy.operators.TextAreaOperator;
import org.netbeans.jemmy.operators.TextComponentOperator;
import org.netbeans.jemmy.operators.TextFieldOperator;


/**
 * Installs drivers for low-level drivers.
 * 
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class InputDriverInstaller {
    Timeout robotAutoDelay;
    boolean useEventDrivers;
    boolean smooth = false;

    /**
     * Constructs an InputDriverInstaller object. 
     * @param useEventDrivers Tells whether to use event drivers, otherwise robot drivers.
     * @param robotAutoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public InputDriverInstaller(boolean useEventDrivers, Timeout robotAutoDelay) {
	this.robotAutoDelay = robotAutoDelay;
	this.useEventDrivers = useEventDrivers;
    }

    /**
     * Constructs an InputDriverInstaller object. Takes autodelay time
     * from JemmyProperties' timeouts.
     * @param useEventDrivers Tells whether to use event drivers, otherwise robot drivers.
     */
    public InputDriverInstaller(boolean useEventDrivers) {
	this(useEventDrivers,
	     JemmyProperties.getCurrentTimeouts().
	     create("EventDispatcher.RobotAutoDelay"));
    }
    /**
     * Constructs an InputDriverInstaller object. Takes autodelay time
     * from JemmyProperties' timeouts.
     * @param useEventDrivers Tells whether to use event drivers, otherwise robot drivers.
     * @param smooth whether to move mouse smoothly.
     */
    public InputDriverInstaller(boolean useEventDrivers, boolean smooth) {
	this(useEventDrivers);
        this.smooth = smooth;
    }

    /**
     * Constructs an InputDriverInstaller object. Uses event drivers.
     * @param robotAutoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public InputDriverInstaller(Timeout robotAutoDelay) {
	this(true,
	     robotAutoDelay);
    }
    /**
     * Constructs an InputDriverInstaller object. Takes autodelay time
     * from JemmyProperties' timeouts. Uses event drivers.
     */
    public InputDriverInstaller() {
	this(true);
    }
    static {
	Class clss= EventDispatcher.class;
    }

    /**
     * Installs input drivers.
     */
    public void install() {
	if(useEventDrivers) {
	    LightDriver keyE = new KeyEventDriver();
	    LightDriver mouseE = new MouseEventDriver();
	    DriverManager.removeDriver(DriverManager.KEY_DRIVER_ID,
				       keyE.getSupported());
	    DriverManager.removeDriver(DriverManager.MOUSE_DRIVER_ID,
				       mouseE.getSupported());
	    DriverManager.setDriver(DriverManager.KEY_DRIVER_ID, keyE);
	    DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, mouseE);
	    try {
		String[] awtOperators = 
		    {
			"org.netbeans.jemmy.operators.ButtonOperator",
			"org.netbeans.jemmy.operators.CheckboxOperator",
			"org.netbeans.jemmy.operators.ChoiceOperator",
			"org.netbeans.jemmy.operators.LabelOperator",
			"org.netbeans.jemmy.operators.ListOperator",
			"org.netbeans.jemmy.operators.ScrollPaneOperator",
			"org.netbeans.jemmy.operators.ScrollbarOperator",
			"org.netbeans.jemmy.operators.TextAreaOperator",
			"org.netbeans.jemmy.operators.TextComponentOperator",
			"org.netbeans.jemmy.operators.TextFieldOperator"
		    };
		LightDriver keyR = new KeyRobotDriver(robotAutoDelay, awtOperators);
		LightDriver mouseR = new MouseRobotDriver(robotAutoDelay, awtOperators);
		DriverManager.removeDriver(DriverManager.KEY_DRIVER_ID,
					   keyR.getSupported());
		DriverManager.removeDriver(DriverManager.MOUSE_DRIVER_ID,
					   mouseR.getSupported());
		DriverManager.setDriver(DriverManager.KEY_DRIVER_ID, keyR);
		DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, mouseR);
	    } catch(JemmyException e) {
		if(!(e.getInnerException() instanceof ClassNotFoundException)) {
		    throw(e);
		}
	    }
	} else {
	    LightDriver keyR = new KeyRobotDriver(robotAutoDelay);
	    LightDriver mouseR = new MouseRobotDriver(robotAutoDelay, smooth);
	    DriverManager.removeDriver(DriverManager.KEY_DRIVER_ID,
				       keyR.getSupported());
	    DriverManager.removeDriver(DriverManager.MOUSE_DRIVER_ID,
				       mouseR.getSupported());
	    DriverManager.setDriver(DriverManager.KEY_DRIVER_ID, keyR);
	    DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, mouseR);
	}
    }
}
