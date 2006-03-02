/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
