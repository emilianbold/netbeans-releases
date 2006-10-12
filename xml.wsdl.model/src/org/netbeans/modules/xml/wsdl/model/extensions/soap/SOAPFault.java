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

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.xam.Reference;

/**
 * Represents the fault element under the wsdl:fault element for SOAP binding
 *
 * @author rico
 */
public interface SOAPFault extends SOAPMessageBase {
    public static final String NAME_PROPERTY = "name";
    /**
     * Set SOAP binding fault name.
     */
    void setName(String name);
    
    /**
     * Return SOAP binding fault name.
     */
    String getName();
 
    /**
     * Set SOAP binding fault using give reference.
     */
    void setFault(Reference<Fault> fault);
    
    /**
     * @return reference to operation fault.
     */
    Reference<Fault> getFault();
}
