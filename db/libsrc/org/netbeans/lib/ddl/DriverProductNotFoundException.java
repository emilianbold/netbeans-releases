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

/**
* System is not able to locate appropriate resources to create DriverSpecification object
* (object describing the driver). It means that driver product is not supported by system.
* You can write your own description file. If you are sure that it is, please check
* location of description files.
*
* @author Radko Najman
*/
public class DriverProductNotFoundException extends Exception
{
    static final long serialVersionUID =-1108211224066947350L;

    /** Driver name */
    private String drvName;

    /** Creates new exception
    * @param desc The text describing the exception
    */
    public DriverProductNotFoundException(String spec) {
        super ();
        drvName = spec;
    }

    /** Creates new exception with text specified string.
    * @param spec Driver name
    * @param desc The text describing the exception
    */
    public DriverProductNotFoundException(String spec, String desc) {
        super (desc);
        drvName = spec;
    }

    /** Returns driver name.
    * This driver is not supported by system. You can write your own description file.
    */
    public String getDriverName()
    {
        return drvName;
    }
}

/*
 * <<Log>>
 *  1    Gandalf   1.0         12/15/99 Radko Najman    
 * $
 */