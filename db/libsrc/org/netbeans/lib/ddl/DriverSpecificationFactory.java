/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.ddl;

import java.sql.*;
import java.util.Set;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/** 
* The factory interface used for creating instances of DriverSpecification class. 
* DriverSpecificationFactory collects information about available driver 
* description files. Then it's able to specify if system can control 
* the driver (specified by product name). It also provides a list of supported
* drivers.
*
* @author Radko Najman
*/
public interface DriverSpecificationFactory {

	/** Returns array of driver products supported by system. It returns
	* string array only, not the DriverSpecification array.
	*/
	public Set supportedDrivers();
	
	/** Returns true if driver (specified by driverName) is 
	* supported by system. Does not throw exception if it doesn't.
	* @param ddriverName Driver product name as given from DatabaseMetaData
	* @return True if driver product is supported.
	*/	
	public boolean isDriverSupported(String driverName);
	
	/** Creates instance of DriverSpecification class; a driver-specification
	* class. This object knows about used driver.
	* @param driverName Driver name
	* @return DriverSpecification object.
	*/
	public DriverSpecification createDriverSpecification(String driverName);

}

/*
* <<Log>>
*/