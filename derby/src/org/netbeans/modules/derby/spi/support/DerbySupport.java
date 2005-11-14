/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby.spi.support;

import java.io.File;
import org.netbeans.modules.derby.DerbyOptions;

/**
 *
 * @author Andrei Badea
 */
public final class DerbySupport {
    
    private DerbySupport() {
    }
    
    /**
     * Sets the location of the Derby installation and registers the Derby drivers.
     * The Derby installation must have a lib subdirectory in which the Derby JAR
     * files are located.
     *
     * @param location the jars locations. This must be an existing directory.
     */
    public static void setLocation(String location) {
        DerbyOptions.getDefault().setLocation(location);
    }
    
    /**
     * Returns the location of the Derby jars. Never returns null,
     * instead it returns an empty string if the location is unknown.
     */
    public static String getLocation() {
        return DerbyOptions.getDefault().getLocation();
    }
    
    /** 
     * Sets the Derby system home, that is, the directory 
     * where the Derby databases are located.
     */
    public static void setSystemHome(String systemHome) {
        DerbyOptions.getDefault().setSystemHome(systemHome);
    }
    
    /** 
     * Returns the Derby system home.
     */
    public static String getSystemHome() {
        return DerbyOptions.getDefault().getSystemHome();
    }
}
