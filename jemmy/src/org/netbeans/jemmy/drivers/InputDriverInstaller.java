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
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseEventDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;


public class InputDriverInstaller {
    Timeout robotAutoDelay;
    boolean useEventDrivers;
    public InputDriverInstaller(boolean useEventDrivers, Timeout robotAutoDelay) {
	this.robotAutoDelay = robotAutoDelay;
	this.useEventDrivers = useEventDrivers;
    }
    public InputDriverInstaller(boolean useEventDrivers) {
	this(useEventDrivers,
	     JemmyProperties.getCurrentTimeouts().
	     create("EventDispatcher.RobotAutoDelay"));
    }
    public InputDriverInstaller(Timeout robotAutoDelay) {
	this(true,
	     robotAutoDelay);
    }
    public InputDriverInstaller() {
	this(true);
    }
    static {
	Class clss= EventDispatcher.class;
    }
    public void install() {
	Driver keyR = new KeyRobotDriver(robotAutoDelay);
	Driver keyE = new KeyEventDriver();
	Driver mouseR = new MouseRobotDriver(robotAutoDelay);
	Driver mouseE = new MouseEventDriver();
	DriverManager.removeDriver(DriverManager.KEY_DRIVER_ID,
				   keyE.getSupported());
	DriverManager.removeDriver(DriverManager.KEY_DRIVER_ID,
				   keyR.getSupported());
	DriverManager.removeDriver(DriverManager.MOUSE_DRIVER_ID,
				   mouseE.getSupported());
	DriverManager.removeDriver(DriverManager.MOUSE_DRIVER_ID,
				   mouseR.getSupported());
	DriverManager.setDriver(DriverManager.KEY_DRIVER_ID, keyR);
	DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, mouseR);
	if(useEventDrivers) {
	    DriverManager.setDriver(DriverManager.KEY_DRIVER_ID, keyE);
	    DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, mouseE);
	}
    }
}
