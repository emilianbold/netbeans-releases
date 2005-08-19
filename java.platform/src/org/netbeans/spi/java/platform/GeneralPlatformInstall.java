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

package org.netbeans.spi.java.platform;

/**
 * An super class of all the platform installers. You never subclass directly
 * this class but either the {@link CustomPlatformInstall} or {@link PlatformInstall}
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class GeneralPlatformInstall {
        
    GeneralPlatformInstall() {
    }
    
    /**
     * Gets the display name of the platform.
     * XXX does this mean "the display name of the platform type"?
     * @return the display name
     */
    public abstract String getDisplayName ();
    
}
