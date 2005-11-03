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

/** Hint for Navigator clients to link <code>Lookup</code> of their
 * <code>TopComponent</code> with Navigator content type.<p></p>  
 * 
 * Usage: Implementation of this interface should be inserted into
 * client's specific topComponent's lookup, see 
 * <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent.getLookup()</a>
 * method. When mentioned <code>TopComponent</code> gets active in the system, system will
 * ask <code>NavigatorLookupHint</code> implementation for content type
 * to show in Navigator UI.
 *
 * @author Dafe Simonek
 */
public interface NavigatorLookupHint {

    /** Hint for content type that should be used in Navigator 
     * 
     * @return String representation of content type (in mime-type style)
     */
    public String getContentType ();
    
}
