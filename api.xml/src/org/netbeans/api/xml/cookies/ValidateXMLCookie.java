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
 * @deprecated  XML tools API candidate
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
