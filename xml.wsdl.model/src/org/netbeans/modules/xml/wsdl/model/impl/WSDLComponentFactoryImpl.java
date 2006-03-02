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

package org.netbeans.modules.xml.wsdl.model.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.RolePortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.CorrelationPropertyImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.PartnerLinkTypeImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.PropertyAliasImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.RolePortTypeImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPAddressImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPBindingImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPBodyImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPFaultImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPHeaderFaultImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPHeaderImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPOperationImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.WSDLSchemaImpl;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class WSDLComponentFactoryImpl implements WSDLComponentFactory {
    
    private WSDLModel model;
    /** Creates a new instance of WSDLComponentFactoryImpl */
    public WSDLComponentFactoryImpl(WSDLModel model) {
        this.model = model;
    }
    
    public WSDLComponent create(Element element, WSDLComponent context) {
        WSDLComponent comp = null;
 
        ElementFactory factory = ElementFactoryRegistry.getDefault()
                                      .get(WSDLElementFactoryProvider.getQName(element));
    
        if(factory != null ){
            comp =  factory.create(context, element);
        }
        return comp;
    }
    
    public <C extends WSDLComponent> C create(WSDLComponent context, QName qName, Class<C> type) {
        WSDLComponent comp = null;
        ElementFactory factory = ElementFactoryRegistry.getDefault().get(qName);
        if(factory != null ){
            comp =  factory.create(context, type);
        }
        return type.cast(comp);
    }
    
    public Port createPort() {	 
         return new PortImpl(model);
     }	 
     	 
     public Part createPart() {	 
         return new PartImpl(model);
     }	 
     	 
     public Output createOutput() {	 
         return new OutputImpl(model);
     }	 
     	 
     public Binding createBinding() {	 
         return new BindingImpl(model);
     }	 
     	 
     public BindingFault createBindingFault() {	 
         return new BindingFaultImpl(model);
     }	 
     	 
     public BindingInput createBindingInput() {	 
         return new BindingInputImpl(model);
     }	 
     	 
     public BindingOperation createBindingOperation() {	 
         return new BindingOperationImpl(model);
     }	 
     	 
     public BindingOutput createBindingOutput() {	 
         return new BindingOutputImpl(model);
     }	 
     	 
     public Documentation createDocumentation() {	 
         return new DocumentationImpl(model);
     }	 
     	 
     public Fault createFault() {	 
         return new FaultImpl(model);
     }	 
     	 
     public Import createImport() {	 
         return new ImportImpl(model);
     }	 
     	 
     public Input createInput() {	 
         return new InputImpl(model);
     }	 
     	 
     public Message createMessage() {	 
         return new MessageImpl(model);
     }	 
     	 
     public OneWayOperation createOneWayOperation() {	 
         return new OneWayOperationImpl(model);
     }

     public NotificationOperation createNotificationOperation() {	 
         return new NotificationOperationImpl(model);
     }
     public RequestResponseOperation createRequestResponseOperation() {	 
         return new RequestResponseOperationImpl(model);
     }

     public SolicitResponseOperation createSolicitResponseOperation() {	 
         return new SolicitResponseOperationImpl(model);
     }
    public Types createTypes() {
        return new TypesImpl(model);
    }

    public Service createService() {
        return new ServiceImpl(model);
    }

    public PortType createPortType() {
        return new PortTypeImpl(model);
    }
    
    // BPEL
    public PropertyAlias createBPELPropertyAlias() {
        return new PropertyAliasImpl(model);
    }

    public CorrelationProperty createBPELCorrelationProperty() {
        return new CorrelationPropertyImpl(model);
    }

    public org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role createBPELRole() {
        return new org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.RoleImpl(model);
    }

    public RolePortType createBPELRolePortType() {
        return new RolePortTypeImpl(model);
    }

    public PartnerLinkType createBPELPartnerLinkType() {
        return new PartnerLinkTypeImpl(model);
    }

    // SOAP
    
    public SOAPAddress createSOAPAddress() {
        return new SOAPAddressImpl(model);
    }

    public SOAPBinding createSOAPBinding() {
        return new SOAPBindingImpl(model);
    }

    public SOAPBody createSOAPBody() {
        return new SOAPBodyImpl(model);
    }

    public SOAPFault createSOAPFault() {
        return new SOAPFaultImpl(model);
    }

    public SOAPHeader createSOAPHeader() {
        return new SOAPHeaderImpl(model);
    }

    public SOAPHeaderFault createSOAPHeaderFault() {
        return new SOAPHeaderFaultImpl(model);
    }

    public SOAPOperation createSOAPOperation() {
        return new SOAPOperationImpl(model);
    }
    
    // XSD
    public WSDLSchema createWSDLSchema() {
        return new WSDLSchemaImpl(model);
    }
}

