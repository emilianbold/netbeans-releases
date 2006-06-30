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

package org.netbeans.jemmy.drivers;

/**
 * Auxiliary class making driver registration easier.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class ArrayDriverInstaller implements DriverInstaller {
    String[] ids;
    Object[] drivers;

    /**
     * Constructs an ArrayDriverInstaller object.
     * Both parameter arrays mush have same length,
     * <code>drivers</code> must keep instances of
     * <a href = "Driver.html">Driver</a> or
     * <a href = "Driver.html">LightDriver</a> implementations.
     * @param ids      an array of driver IDs
     * @param drivers  an array of drivers.
     */
    public ArrayDriverInstaller(String[] ids, Object[] drivers) {
	this.ids = ids;
	this.drivers = drivers;
    }

    /**
     * Installs drivers from the array passed into constructor.
     */
    public void install() {
	for(int i = 0; i < ids.length; i++) {
	    DriverManager.setDriver(ids[i], drivers[i]);
	}
    }
}
