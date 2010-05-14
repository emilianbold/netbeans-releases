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

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * @author ads
 */
public interface FaultNameReference {

    /**
     * faultName attribute name.
     */
    String FAULT_NAME = "faultName";    // NOI18N

    /**
     * Getter for faultName attribute.
     * @return possible object is {@link QName }
     */
    QName getFaultName();

    /**
     * Sets the value of the faultName property.
     *
     * @param value
     *            allowed object is {@link QName }
     * @throws VetoException {@link VetoException} Will be thrown is
     * <code>value</code> have either bad namespace uri ( that is not represent
     * URI by RFC ) or bad local part ( that not represent NCName ).
     * This verification is needed because Java QName doesn't perform it.  
     */
    void setFaultName( QName value ) throws VetoException;

}
