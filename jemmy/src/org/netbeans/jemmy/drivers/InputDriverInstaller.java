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
	    LightDriver mouseR = new MouseRobotDriver(robotAutoDelay);
	    DriverManager.removeDriver(DriverManager.KEY_DRIVER_ID,
				       keyR.getSupported());
	    DriverManager.removeDriver(DriverManager.MOUSE_DRIVER_ID,
				       mouseR.getSupported());
	    DriverManager.setDriver(DriverManager.KEY_DRIVER_ID, keyR);
	    DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, mouseR);
	}
    }
}
