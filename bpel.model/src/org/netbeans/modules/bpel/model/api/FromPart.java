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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;

/**
 * @author ads
 *         <p>
 *         Java class for tFromPart complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tFromPart&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *         &lt;attribute name=&quot;part&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *         &lt;attribute name=&quot;toVariable&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *       &lt;/restriction&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 * 
 *         This interface extends <code>VariableDeclaration</code> interface
 *         only if it child of OnEvent. In this case there is static analysis
 *         rule : 
 *         For <onEvent>, variables referenced by the variable attribute
 *         of <fromPart> elements or the variable attribute of an <onEvent>
 *         element are implicitly declared in the associated scope of the event
 *         handler. Variables of the same names MUST NOT be explicitly declared
 *         in the associated scope. The variable references are resolved to the
 *         associated scope only and MUST NOT be resolved to the ancestor
 *         scopes.
 *         We need "real" element to be referenced, so in this case we consider
 *         <code>FromPart</code> as <code>VariableDeclaration</code>
 *         and reference resolving will be realized to FromPart.
 *         But this is not actually correct ( variables should be considered
 *         as variables declared in Scope ).
 */
public interface FromPart extends BpelEntity , PartReference, ReferenceCollection,
    VariableDeclaration
    
{

    String TO_VARIABLE = "toVariable";      // NOI18N

    /**
     * @return Reference to variable ( "toVariable" attribute value ).
     */
    BpelReference<VariableDeclaration> getToVariable();

    /**
     * Set new reference to variable ( "toVariable" attribute value ).
     * 
     * @param variable
     *            New reference to variable.
     */
    void setToVariable( BpelReference<VariableDeclaration> variable );
}
