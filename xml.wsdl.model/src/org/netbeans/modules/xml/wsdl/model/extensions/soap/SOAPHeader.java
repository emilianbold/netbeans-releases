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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 * Represents a header element under the wsdl:input or wsdl:output element for SOAP binding
 *
 * @author rico
 * @author Nam Nguyen
 */
public interface SOAPHeader extends SOAPHeaderBase, ExtensibilityElement.UpdaterProvider {
    public static final String HEADER_FAULT_PROPERTY = "headerFault";
    
    Collection<SOAPHeaderFault> getSOAPHeaderFaults();
    void addSOAPHeaderFault(SOAPHeaderFault soapHeaderFault);
    void removeSOAPHeaderFault(SOAPHeaderFault soapHeaderFault);
}
