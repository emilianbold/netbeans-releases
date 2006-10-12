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

package org.netbeans.modules.xml.wsdl.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * @author rico
 * Factory for providing concrete implementations of WSDLComponents
 */

public interface WSDLComponentFactory extends ComponentFactory<WSDLComponent> {
    WSDLComponent create(WSDLComponent parent, QName qName);   
    Binding createBinding();
    BindingFault createBindingFault();
    BindingInput createBindingInput();
    BindingOperation createBindingOperation();
    BindingOutput createBindingOutput();
    Documentation createDocumentation();
    Fault createFault();
    Import createImport();
    Input createInput();
    Message createMessage();
    OneWayOperation createOneWayOperation();
    SolicitResponseOperation createSolicitResponseOperation();
    RequestResponseOperation createRequestResponseOperation();
    NotificationOperation createNotificationOperation();
    Output createOutput();
    Part createPart();
    Port createPort();
    PortType createPortType();
    Service createService();
    Types createTypes();

    // SOAP
    SOAPAddress createSOAPAddress();
    SOAPBinding createSOAPBinding();
    SOAPBody createSOAPBody();
    SOAPFault createSOAPFault();
    SOAPHeader createSOAPHeader();
    SOAPHeaderFault createSOAPHeaderFault();
    SOAPOperation createSOAPOperation();
    
    // XSD
    WSDLSchema createWSDLSchema();
}
