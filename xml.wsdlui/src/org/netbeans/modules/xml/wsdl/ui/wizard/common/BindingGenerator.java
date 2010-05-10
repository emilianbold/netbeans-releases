/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * BindingGenerator.java
 *
 * Created on September 6, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.wsdl.ui.wizard.Command;
import org.netbeans.modules.xml.wsdl.ui.wizard.WsdlGenerationUtil;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class BindingGenerator implements Command {
    
    private WSDLModel mModel;
    
    private PortType mPortType;
    
    
    private Map mConfigurationMap;
    
    private WsdlGenerationUtil mUtil;
    
    private Binding mBinding;
    
    private Service mService;
    
    private Port mPort;
    
    /** Creates a new instance of BindingGenerator */
    public BindingGenerator(WSDLModel model, PortType pt, Map configurationMap) {
        this.mModel = model;
        this.mPortType = pt;
        this.mConfigurationMap = configurationMap;
        this.mUtil = new WsdlGenerationUtil(this.mModel);
    }
    
    public Binding getBinding() {
        return this.mBinding;
    }
    
    public Service getService() {
        return this.mService;
    }
    
    public Port getPort() {
        return this.mPort;
    }
    
    public void execute() {
        if (mPortType == null) {
            return;
        }

        //binding
        String bindingName = (String) this.mConfigurationMap.get(WSDLWizardConstants.BINDING_NAME);
        if (bindingName == null) {
            return;
        }
        Binding b = mModel.getFactory().createBinding();
        this.mBinding = b;
        b.setName(bindingName);
        mModel.getDefinitions().addBinding(b);
        NamedComponentReference<PortType> ptRef = b.createReferenceTo(this.mPortType, PortType.class);
        b.setType(ptRef);

        //Not used LocalizedTemplateGroup bindingType = (LocalizedTemplateGroup) this.mConfigurationMap.get(WSDLWizardConstants.BINDING_TYPE);
        //this could be null for a binding which does not have a sub type
        LocalizedTemplate bindingSubType = (LocalizedTemplate) this.mConfigurationMap.get(WSDLWizardConstants.BINDING_SUBTYPE);
        if (bindingSubType != null) {
            //binding protocol
            createAndAddBindingProtocol(b, bindingSubType);

            Collection<Operation> operations = mPortType.getOperations();
            for (Operation operation : operations) {
                //binding operation
                BindingOperation bo = this.mModel.getFactory().createBindingOperation();
                b.addBindingOperation(bo);
                NamedComponentReference<Operation> opRef = bo.createReferenceTo(operation, Operation.class);
                bo.setOperation(opRef);

                //binding operation protocol
                createAndAddBindingOperationProtocolElements(bo, bindingSubType, operation);
            }

        } else {
        //no binding subtype

        }

        Boolean autoCreateServicePort = (Boolean) mConfigurationMap.get(WSDLWizardConstants.AUTO_CREATE_SERVICEPORT);

        if (autoCreateServicePort != null && !autoCreateServicePort.booleanValue()) {
            return;
        }

        //service and port
        String serviceName = (String) this.mConfigurationMap.get(WSDLWizardConstants.SERVICE_NAME);
        String servicePortName = (String) this.mConfigurationMap.get(WSDLWizardConstants.SERVICEPORT_NAME);

        Collection<Service> services = mModel.getDefinitions().getServices();
        Service service = null;
        for (Service svc : services) {
            if (svc.getName().equals(serviceName)) {
                service = svc;
                break;
            }
        }

        if (service == null) {
            service = mModel.getFactory().createService();
            this.mService = service;
            service.setName(serviceName);
            mModel.getDefinitions().addService(service);
        }


        /*
        01/02/07, following code replaced to allow the reuse of empty port element in CASA (T. Li)
         */
        //  Port port = mModel.getFactory().createPort();
        //  this.mPort = port;
        //  port.setName(servicePortName);
        //  NamedComponentReference<Binding> bindingRef = port.createReferenceTo(b, Binding.class);
        //  port.setBinding(bindingRef);
        //  createAndAddServicePortProtocolElements(port, bindingSubType);
        //  service.addPort(port);

        Collection<Port> ports = service.getPorts();
        Port port = null;
        for (Port p : ports) {
            if (p.getName().equals(servicePortName)) {
                port = p;
                break;
            }
        }

        if (port == null) {
            port = mModel.getFactory().createPort();
            this.mPort = port;
            port.setName(servicePortName);
            service.addPort(port);
        }
        NamedComponentReference<Binding> bindingRef = port.createReferenceTo(b, Binding.class);
        port.setBinding(bindingRef);
        createAndAddServicePortProtocolElements(port, bindingSubType);

    }
    
     private void createAndAddBindingProtocol(Binding b, LocalizedTemplate bindingSubType) {
        this.mUtil.createAndAddExtensionElementAndAttribute(WSDLExtensibilityElements.ELEMENT_BINDING, bindingSubType, b);
    }
    
    
    private void createAndAddBindingOperationProtocolElements(BindingOperation bOperation, 
                                                              LocalizedTemplate bindingSubType,
                                                              Operation portTypeOperation) {
        

        this.mUtil.createAndAddExtensionElementAndAttribute(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION, bindingSubType, bOperation);
        
        if(portTypeOperation instanceof RequestResponseOperation) {
            Input input = portTypeOperation.getInput();
            Output output = portTypeOperation.getOutput();
            Collection<Fault> faults = portTypeOperation.getFaults();
            
            if(input != null) {
                BindingInput bIn = createAndAddBindingOperationInput(bOperation, bindingSubType);
                if (input.getAttribute(new StringAttribute(Named.NAME_PROPERTY)) != null) {
                    bIn.setName(input.getName());
                }
            }
            
            if(output != null) {
                BindingOutput bOut = createAndAddBindingOperationOutput(bOperation, bindingSubType);
                if (output.getAttribute(new StringAttribute(Named.NAME_PROPERTY)) != null) {
                    bOut.setName(output.getName());
                }
            }
            
            if(faults != null) {
                Iterator<Fault> it = faults.iterator();
                while(it.hasNext()) {
                    Fault fault = it.next();
                    BindingFault bFault = createAndAddBindingOperationFault(fault.getName(), bOperation, bindingSubType);
                }
            }
            
        } else if(portTypeOperation instanceof OneWayOperation) {                                                          
            Input input = portTypeOperation.getInput();
            
            if(input != null) {
                BindingInput bIn = createAndAddBindingOperationInput(bOperation, bindingSubType);
                if (input.getAttribute(new StringAttribute(Named.NAME_PROPERTY)) != null) {
                    bIn.setName(input.getName());
                }
            }
            
        } else if(portTypeOperation instanceof SolicitResponseOperation) {
            Input input = portTypeOperation.getInput();
            Output output = portTypeOperation.getOutput();
            Collection<Fault> faults = portTypeOperation.getFaults();
            
            if(input != null) {
                BindingInput bIn = createAndAddBindingOperationInput(bOperation, bindingSubType);
                if (input.getAttribute(new StringAttribute(Named.NAME_PROPERTY)) != null) {
                    bIn.setName(input.getName());
                }
            }
            
            if(output != null) {
                BindingOutput bOut = createAndAddBindingOperationOutput(bOperation, bindingSubType);
                if (output.getAttribute(new StringAttribute(Named.NAME_PROPERTY)) != null) {
                    bOut.setName(output.getName());
                }
            }
            
            if(faults != null) {
                Iterator<Fault> it = faults.iterator();
                while(it.hasNext()) {
                    Fault fault = it.next();
                    BindingFault bFault = createAndAddBindingOperationFault(fault.getName(), bOperation, bindingSubType);
                }
            }
        } else if(portTypeOperation instanceof NotificationOperation) {
            Output output = portTypeOperation.getOutput();
            if(output != null) {
                BindingOutput bOut = createAndAddBindingOperationOutput(bOperation, bindingSubType);
                if (output.getAttribute(new StringAttribute(Named.NAME_PROPERTY)) != null) {
                    bOut.setName(output.getName());
                }
            }
        }
    }
    
    private BindingInput createAndAddBindingOperationInput(BindingOperation bOperation, LocalizedTemplate bindingSubType) {
        BindingInput bIn = this.mModel.getFactory().createBindingInput();
        bOperation.setBindingInput(bIn);
        this.mUtil.createAndAddExtensionElementAndAttribute(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT,
                                                 bindingSubType,
                                                 bIn);
        
        return bIn;
    }
    
    
    private BindingOutput createAndAddBindingOperationOutput(BindingOperation bOperation, LocalizedTemplate bindingSubType) {
        BindingOutput bOut = this.mModel.getFactory().createBindingOutput();
        bOperation.setBindingOutput(bOut);
        this.mUtil.createAndAddExtensionElementAndAttribute(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_OUTPUT,
                                                 bindingSubType,
                                                 bOut);
        
        return bOut;
    }
    
    private BindingFault createAndAddBindingOperationFault(String faultName, BindingOperation bOperation, LocalizedTemplate bindingSubType) {
        BindingFault bFault = this.mModel.getFactory().createBindingFault();
        bOperation.addBindingFault(bFault);
        bFault.setName(faultName);
        this.mUtil.createAndAddExtensionElementAndAttribute(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_FAULT,
                                                 bindingSubType,
                                                 bFault);
        
        return bFault;
    }
    
    private void createAndAddServicePortProtocolElements(Port port, LocalizedTemplate bindingSubType) {
        this.mUtil.createAndAddExtensionElementAndAttribute(WSDLExtensibilityElements.ELEMENT_SERVICE_PORT,
                                                 bindingSubType,
                                                 port);
        
    }
}
