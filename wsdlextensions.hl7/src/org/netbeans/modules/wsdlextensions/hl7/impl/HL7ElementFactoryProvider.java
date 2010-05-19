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

package org.netbeans.modules.wsdlextensions.hl7.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 ** @author raghunadh.teegavarapu@sun.com
*/
public class HL7ElementFactoryProvider {
    
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7BindingImpl(context.getModel(), element);
        }
    }

    public static class AddressFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.ADDRESS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7AddressImpl(context.getModel(), element);
        }
    }

    public static class OperationFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7OperationImpl(context.getModel(), element);
        }
    }

    public static class MessageFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.MESSAGE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7MessageImpl(context.getModel(), element);
        }
    }

	 public static class ProtocolPropertiesFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.POTOCOLPROPERTIES.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7ProtocolPropertiesImpl(context.getModel(), element);
        }
    }
	public static class CommunicationControlsFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.COMMUNICATIONCONTROLS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7CommunicationControlsImpl(context.getModel(), element);
        }
    }

	public static class CommunicationControlFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(HL7QName.COMMUNICATIONCONTROL.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HL7CommunicationControlImpl(context.getModel(), element);
        }
    }
}
