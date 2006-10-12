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

package org.netbeans.modules.derby.spi.support;

import java.io.File;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.RegisterDerby;
import org.openide.util.NbBundle;

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
     * Returns the Derby system home. Never returns null,
     * instead it returns an empty string if the location is unknown.
     */
    public static String getSystemHome() {
        return DerbyOptions.getDefault().getSystemHome();
    }
    
    /**
     * Returns the default Derby system home. It is not guaranteed that
     * the directory returned by this method exists.
     */
    public static String getDefaultSystemHome() {
        // issue 76908
        String propertySystemHome = System.getProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME);
        if (propertySystemHome != null) {
            return propertySystemHome;
        }

        String userHome = System.getProperty("user.home"); // NOI18N
        return new File(userHome, NbBundle.getMessage(DerbySupport.class, "LBL_DerbyDatabaseDirectory")).getAbsolutePath();
    }
    
    /**
     * Ensures the Derby database is started, that is, starts it if it is
     * not running and does nothing otherwise.
     *
     * @since 1.5
     */
    public static void ensureStarted() {
        RegisterDerby.getDefault().ensureStarted();
    }
}
