/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.xml.cookies;

import org.openide.nodes.Node;

/**
 * Fast (and preferably standalone mode) XML parsed entity syntax checker.
 * <p>Implemenmtation should follow XML specification for non-validating
 * processors. Implementation is allowed to support any XML parsed entities.
 * It must not change UI state.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing non-validateable XML resources.
 *
 * @author      Petr Kuzel
 * @see         ValidateXMLCookie
 * @see         <a href="http://www.w3.org/TR/REC-xml#proc-types">XML 1.0</a>     
 */
public interface CheckXMLCookie extends Node.Cookie {
    
    /**
     * Check XML parsed entity for syntax wellformedness.
     * @param observer optional listener (<code>null</code> allowed)
     *               giving judgement details via {@link XMLProcessorDetail}s.
     * @return <code>true</code> if syntax check passes
     */
    boolean checkXML(CookieObserver observer);
    
}
