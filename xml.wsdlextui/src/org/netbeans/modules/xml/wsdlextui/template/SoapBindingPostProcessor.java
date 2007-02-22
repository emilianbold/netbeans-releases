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

package org.netbeans.modules.xml.wsdlextui.template;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;

/**
 *
 * @author radval
 */
public class SoapBindingPostProcessor {
    
    private String mWsdlTargetNamespace;
    
    private static final String SOAP_LOCATION_PPREFIX = "http://localhost:18181/";
    
    /** Creates a new instance of SoapBindingPostProcessor */
    public SoapBindingPostProcessor() {
    }
    
    public void postProcess(String wsdlTargetNamespace, Port port) {
        this.mWsdlTargetNamespace = wsdlTargetNamespace;
        
        List<ExtensibilityElement> ees =  port.getExtensibilityElements();
        Iterator<ExtensibilityElement> it = ees.iterator();
        
        while(it.hasNext()) {
            ExtensibilityElement ee = it.next();
            if(ee instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) ee;
                WSDLComponent parent = port.getParent();
                if(parent != null && parent instanceof Service) {
                    Service service = (Service) parent;
                    soapAddress.setLocation(SOAP_LOCATION_PPREFIX + service.getName() + "/" + port.getName() );
                }
            }
        }
    }
    
    public void postProcess(String wsdlTargetNamespace, Binding binding) {
        this.mWsdlTargetNamespace = wsdlTargetNamespace;
        
        SOAPBinding.Style style = null;
        
        List<ExtensibilityElement> ee = binding.getExtensibilityElements();
        Iterator<ExtensibilityElement> it = ee.iterator();
        while(it.hasNext()) {
            ExtensibilityElement e = it.next();
            if(e instanceof SOAPBinding) {
                SOAPBinding sBinding = (SOAPBinding) e;
                style = sBinding.getStyle();
                break;
            }
        }
        
        if(style != null) {
            Collection<BindingOperation> bOps = binding.getBindingOperations();
            Iterator<BindingOperation> itBops = bOps.iterator();
            while(itBops.hasNext()) {
                BindingOperation op = itBops.next();
                processBindingOperation(style, op);
            }
        }
    }
    
    private void processBindingOperation(SOAPBinding.Style style, BindingOperation bindingOperation) {
        BindingInput bIn = bindingOperation.getBindingInput();
        if (bIn != null) {
            processBindingOperationInput(style, bIn);
        }
        
        BindingOutput bOut = bindingOperation.getBindingOutput();
        if (bOut != null) {
            processBindingOperationOutput(style, bOut);
        }
        
        Collection<BindingFault> bFaults = bindingOperation.getBindingFaults();
        if (bFaults != null && !bFaults.isEmpty()) {
            Iterator<BindingFault> it = bFaults.iterator();
            while(it.hasNext()) {
                BindingFault bFault = it.next();
                processBindingOperationFault(style, bFault);
            }
        }
    }
    
    
    private void processBindingOperationInput(SOAPBinding.Style style, BindingInput bIn) {
        if(style.equals(SOAPBinding.Style.RPC)) {
            List<ExtensibilityElement> eeList = bIn.getExtensibilityElements();
            Iterator<ExtensibilityElement> it =  eeList.iterator();
            while(it.hasNext()) {
                ExtensibilityElement ee = it.next();
                if(ee instanceof SOAPBody) {
                    SOAPBody sBody = (SOAPBody) ee;
                    sBody.setNamespace(mWsdlTargetNamespace);
                }
            }
        }
    }
    
    private void processBindingOperationOutput(SOAPBinding.Style style, BindingOutput bOut ) {
        if(style.equals(SOAPBinding.Style.RPC)) {
            List<ExtensibilityElement> eeList = bOut.getExtensibilityElements();
            Iterator<ExtensibilityElement> it =  eeList.iterator();
            while(it.hasNext()) {
                ExtensibilityElement ee = it.next();
                if(ee instanceof SOAPBody) {
                    SOAPBody sBody = (SOAPBody) ee;
                    sBody.setNamespace(mWsdlTargetNamespace);
                }
            }
        }
    }
    
    private void processBindingOperationFault(SOAPBinding.Style style, BindingFault bFault) {
        
            List<ExtensibilityElement> eeList = bFault.getExtensibilityElements();
            Iterator<ExtensibilityElement> it =  eeList.iterator();
            while(it.hasNext()) {
                ExtensibilityElement ee = it.next();
                if(ee instanceof SOAPFault) {
                    SOAPFault sFault = (SOAPFault) ee;
                    sFault.setName(bFault.getName());
                    if(style.equals(SOAPBinding.Style.RPC)) {
                        sFault.setNamespace(mWsdlTargetNamespace);
                    }
                }
            }
        
    }
    
}
