/*
 * BindingGenerator.java
 *
 * Created on September 6, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplate;
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
        //binding
            String bindingName = (String) this.mConfigurationMap.get(WizardBindingConfigurationStep.BINDING_NAME);
            if (bindingName == null) return;
            Binding b = mModel.getFactory().createBinding();
            this.mBinding = b;
            b.setName(bindingName);
            NamedComponentReference<PortType> ptRef = b.createReferenceTo(this.mPortType, PortType.class);
            b.setType(ptRef);
            mModel.getDefinitions().addBinding(b);
            
            //Not used LocalizedTemplateGroup bindingType = (LocalizedTemplateGroup) this.mConfigurationMap.get(WizardBindingConfigurationStep.BINDING_TYPE);
            //this could be null for a binding which does not have a sub type
            LocalizedTemplate bindingSubType = (LocalizedTemplate) this.mConfigurationMap.get(WizardBindingConfigurationStep.BINDING_SUBTYPE);
            if(bindingSubType != null) {
                //binding protocol
                createAndAddBindingProtocol(b, bindingSubType);
                
                Collection<Operation> operations = mPortType.getOperations();
                for (Operation operation : operations) {
                    //binding operation
                    BindingOperation bo = this.mModel.getFactory().createBindingOperation();
                    NamedComponentReference<Operation> opRef = bo.createReferenceTo(operation, Operation.class);
                    bo.setOperation(opRef);
                    b.addBindingOperation(bo);
                    
                    //binding operation protocol
                    createAndAddBindingOperationProtocolElements(bo, bindingSubType, operation);
                }
                
            } else {
                //no binding subtype
                
            }
            
            //service and port
            String serviceName = (String) this.mConfigurationMap.get(WizardBindingConfigurationStep.SERVICE_NAME);
            String servicePortName = (String) this.mConfigurationMap.get(WizardBindingConfigurationStep.SERVICEPORT_NAME);
            
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
            
            Port port = mModel.getFactory().createPort();
            this.mPort = port;
            port.setName(servicePortName);
            NamedComponentReference<Binding> bindingRef = port.createReferenceTo(b, Binding.class);
            port.setBinding(bindingRef);
            createAndAddServicePortProtocolElements(port, bindingSubType);
            service.addPort(port);
            
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
                bIn.setName(input.getName());
            }
            
            if(output != null) {
                BindingOutput bOut = createAndAddBindingOperationOutput(bOperation, bindingSubType);
                bOut.setName(output.getName());
            }
            
            if(faults != null) {
                Iterator<Fault> it = faults.iterator();
                while(it.hasNext()) {
                    Fault fault = it.next();
                    BindingFault bFault = createAndAddBindingOperationFault(bOperation, bindingSubType);
                    bFault.setName(fault.getName());
                }
            }
            
        } else if(portTypeOperation instanceof OneWayOperation) {                                                          
            Input input = portTypeOperation.getInput();
            
            if(input != null) {
                BindingInput bIn = createAndAddBindingOperationInput(bOperation, bindingSubType);
                bIn.setName(input.getName());
            }
            
        } else if(portTypeOperation instanceof SolicitResponseOperation) {
            Input input = portTypeOperation.getInput();
            Output output = portTypeOperation.getOutput();
            Collection<Fault> faults = portTypeOperation.getFaults();
            
            if(input != null) {
                BindingInput bIn = createAndAddBindingOperationInput(bOperation, bindingSubType);
                bIn.setName(input.getName());
            }
            
            if(output != null) {
                BindingOutput bOut = createAndAddBindingOperationOutput(bOperation, bindingSubType);
                bOut.setName(output.getName());
            }
            
            if(faults != null) {
                Iterator<Fault> it = faults.iterator();
                while(it.hasNext()) {
                    Fault fault = it.next();
                    BindingFault bFault = createAndAddBindingOperationFault(bOperation, bindingSubType);
                    bFault.setName(fault.getName());
                }
            }
        } else if(portTypeOperation instanceof NotificationOperation) {
            Output output = portTypeOperation.getOutput();
            if(output != null) {
                BindingOutput bOut = createAndAddBindingOperationOutput(bOperation, bindingSubType);
                bOut.setName(output.getName());
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
    
    private BindingFault createAndAddBindingOperationFault(BindingOperation bOperation, LocalizedTemplate bindingSubType) {
        BindingFault bFault = this.mModel.getFactory().createBindingFault();
        bOperation.addBindingFault(bFault);
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
