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
 * <p>Implemenmtation must
 * fulfill <a href="http://www.w3.org/TR/REC-xml#proc-types">XML 1.0</a>
 * specification for non-validating processors if no DTD is referenced and
 * specification for validating processors if a DTD is referenced.
 * It is allowed to extend the contract to support domain specifics semantics
 * checks. It must not change UI state.
 * <p>
 * <h3>Use Cases</h3>
 * <ul>
 * <li><b>Provider</b> needs to define domain specifics semantics checks such as
 *     XML Schema based validation, Ant script dependency cycles checking, etc.
 * <li><b>Client</b> needs to check XML document entities transparently without
 *     appriory knowledge how to perform it for specifics XML document type.
 * </ul>
 *
 * @author  Petr Kuzel
 * @deprecated XML tools API candidate
 */
public interface ValidateXMLCookie extends Node.Cookie {

    /**
     * Validate XML document entity.
     * @param report optional listener (<code>null</code> allowed)
     *               giving judgement details
     * @return true if validity check passes
     */
    boolean validateXML(ProcessorListener l);

}
