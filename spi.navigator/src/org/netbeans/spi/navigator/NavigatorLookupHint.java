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

package org.netbeans.spi.navigator;

/** Hint for Navigator clients to link their Node's Lookup with 
 * Navigator content type.  
 * 
 * Usage: Implementation of this interface should be inserted into
 * client's specific node lookup (see Node.getLookup()).
 * When mentioned node gets selected, system will ask for content type
 * to show in Navigator UI.  
 *
 * @author Dafe Simonek
 */
public interface NavigatorLookupHint {

    /** Hitn for content type that should be used in Navigator 
     * 
     * @return String representation of content type (in mime-type style)
     */
    public String getContentType ();
    
}
