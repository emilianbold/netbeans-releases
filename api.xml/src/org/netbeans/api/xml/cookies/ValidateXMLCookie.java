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
 * Validate XML document entity (in polyform manner).
 * <p>Implemenmtation must follow XML 1.0 specification for processors
 * conformance. It is allowed to extend the contract to support domain
 * specifics semantics checks (i.e. XML Schema). It must not change UI state.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing validateable XML resources.
 * <p>
 * <h3>Use Cases</h3>
 * <ul>
 * <li><b>Provider</b> needs to define domain specifics semantics checks such as
 *     XML Schema based validation, Ant script dependency cycles checking, etc.
 * <li><b>Client</b> needs to check XML document entities transparently without
 *     appriory knowledge how to perform it for specifics XML document type.
 * </ul>
 *
 * @author      Petr Kuzel
 * @see         CheckXMLCookie
 * @see         <a href="http://www.w3.org/TR/REC-xml#proc-types">XML 1.0</a>     
 */
public interface ValidateXMLCookie extends Node.Cookie {

    /**
     * Validate XML document entity.
     * @param observer Optional listener (<code>null</code> allowed)
     *               giving judgement details via {@link XMLProcessorDetail}s.
     * @return true if validity check passes.
     */
    boolean validateXML(CookieObserver observer);

}
