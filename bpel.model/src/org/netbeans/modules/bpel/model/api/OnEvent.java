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

import org.netbeans.modules.bpel.model.api.events.VetoException;



/**
 * @author ads
 *         <p>
 *         Java class for tOnEvent complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tOnEvent&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tOnMsgCommon&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}scope&quot;/&gt;
 *         &lt;/sequence&gt;
 *         &lt;attribute name=&quot;messageType&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *         &lt;attribute name=&quot;variable&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 * 
 * This interface should not extend VariableReference becuase it doesn't
 *         reference to Variable. It declare new local variable.
 */
public interface OnEvent extends OnMessageCommon,ScopeHolder,
    ElementReference, MessageTypeReference,VariableDeclaration, 
    VariableDeclarationScope
{

    String VARIABLE = VariableReference.VARIABLE;
    
    /**
     * Gets the value of the variable property.
     * This is variable declaration. Opposite to VariableSpec interface where
     * this attribute is reference to variable.   
     * 
     * @return possible object is String.
     */
    String getVariable();

    /**
     * Removes variable attribute.
     */
    void removeVariable();

    /**
     * Sets the value of the variable property.
     * 
     * @param value
     *            allowed object is String.
     * @throws VetoException { @link VetoException } will be thrown
     * if value is not acceptable name for variable.
     */
    void setVariable( String value ) throws VetoException;
}
