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

package org.netbeans.modules.versioning.spi;

/**
 * Top level interface, all versioning systems must implement it. 
 * 
 * @author Maros Sandor
 */
public interface VersioningSystem {
    
    /**
     * Adds a new listener to versioning events. Events are typically fired after a versioning action completes
     * and statuses of files change. Adding the same listener more than once has no effect.  
     * 
     * @param listener a versioning listener
     */ 
    void addVersioningListener(VersioningListener listener);

    /**
     * Removes a versioning listener. Removing listener that is not registered has no effect.  
     * 
     * @param listener a versioning listener
     */ 
    void removeVersioningListener(VersioningListener listener);
}
