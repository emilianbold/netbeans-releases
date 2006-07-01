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
