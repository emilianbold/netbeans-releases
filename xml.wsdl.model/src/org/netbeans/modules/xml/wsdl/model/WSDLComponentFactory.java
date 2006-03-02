/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.RolePortType;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.ComponentFactory;

/**
 *
 * @author rico
 * Factory for providing concrete implementations of WSDLComponents
 */

public interface WSDLComponentFactory extends ComponentFactory<WSDLComponent> {
    <C extends WSDLComponent> C create(WSDLComponent parent, QName qName, Class<C> type);   
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

    //BPEL
    PropertyAlias createBPELPropertyAlias();
    CorrelationProperty createBPELCorrelationProperty();
    org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role createBPELRole();
    RolePortType createBPELRolePortType();
    PartnerLinkType createBPELPartnerLinkType();

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
