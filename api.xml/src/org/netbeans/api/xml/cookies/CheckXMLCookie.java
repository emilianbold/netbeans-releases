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
