/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author rico
 * Represents a header element under the wsdl:input or wsdl:output element for SOAP binding
 */
public interface SOAPHeader extends SOAPHeaderBase {
    public static final String HEADER_FAULT_PROPERTY = "headerFault";
    
    Collection<SOAPHeaderFault> getSOAPHeaderFaults();
    void addSOAPHeaderFault(SOAPHeaderFault soapHeaderFault);
    void removeSOAPHeaderFault(SOAPHeaderFault soapHeaderFault);
}
