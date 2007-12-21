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

import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.Operation;

/**
 * It seems "operation" attribute should be String, not QName. But it refers to
 * operation that is defined in wsdl file. We don't provide reference to
 * operation object. Operation can be identified via additional attribute ( in
 * holder that contain this attribute ) i.e. portType that have QName type.
 * 
 * @author ads
 */
public interface OperationReference extends ReferenceCollection {

    /**
     * operation atribute name.
     */
    String OPERATION = "operation";     // NOI18N

    /**
     * Gets the value of the operation property.
     * 
     * @return possible object is reference to Operation object in WSDL model.
     */
    WSDLReference<Operation> getOperation();

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *            allowed object is reference to Operation object in WSDL model.
     */
    void setOperation( WSDLReference<Operation> value );
}
