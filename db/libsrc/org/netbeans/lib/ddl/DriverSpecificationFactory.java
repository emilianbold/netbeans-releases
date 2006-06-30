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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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