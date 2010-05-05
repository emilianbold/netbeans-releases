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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
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
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class BindingOperationGenerator implements Command {
    
    private WSDLModel mModel;
    
    
    private WsdlGenerationUtil mUtil;
    
    private Binding mBinding;
    
    private String namespace;

    private Operation[] mOperations;
    private BindingOperation lastAddedBindingOperation;
    
    public BindingOperationGenerator(Binding binding, String namespace, Operation[] operations) {
        this.mModel = binding.getModel();
        this.mUtil = new WsdlGenerationUtil(this.mModel);
        this.namespace = namespace;
        this.mOperations = operations;
        mBinding = binding;
    }
    
    public BindingOperation getBindingOperation() {
        return lastAddedBindingOperation;
    }
    
    public void execute() {
        LocalizedTemplate bindingSubType = null;
 /*       if (namespace != null) {
            ExtensibilityElementTemplateFactory factory = new ExtensibilityElementTemplateFactory();
            ExtensibilityElementTemplateProvider provider = factory.getProvider(namespace);
            String template = provider.getTemplateUsed(mBinding);
            if (template != null) {
                TemplateGroup group = factory.getExtensibilityElementTemplateGroup(namespace);
                //get the proper LTG
                LocalizedTemplateGroup lGroup = factory.getLocalizedTemplateGroup(group);
                LocalizedTemplate[] lTemplates = lGroup.getTemplate();
                for (LocalizedTemplate lTemplate : lTemplates) {
                    if (lTemplate.getDelegate().getName().equals(template)) {
                        bindingSubType = lTemplate;
                        break;
                    }
                }

            }
        }*/
        
        if (bindingSubType != null) {
            for (Operation operation : mOperations) {
                //binding operation
                BindingOperation bo = this.mModel.getFactory().createBindingOperation();
                mBinding.addBindingOperation(bo);
                NamedComponentReference<Operation> opRef = bo.createReferenceTo(operation, Operation.class);
                bo.setOperation(opRef);

                //binding operation protocol
                createAndAddBindingOperationProtocolElements(bo, bindingSubType, operation);
                lastAddedBindingOperation = bo;
            }

        } else {
            for (Operation operation : mOperations) {
                BindingOperation bindingOperation = mModel.getFactory().createBindingOperation();
                if(operation != null) {
                    bindingOperation.setName(operation.getName());
                    Input input = operation.getInput();
                    Output output = operation.getOutput();
                    Collection<Fault> faults = operation.getFaults();
                    if(input != null) {
                        BindingInput bIn = mModel.getFactory().createBindingInput();
                        if(input.getName() != null) {
                            bIn.setName(input.getName());
                        }
                        bindingOperation.setBindingInput(bIn);
                    }
                    
                    if(output != null) {
                        BindingOutput bOut = mModel.getFactory().createBindingOutput();
                        if(output.getName() != null) {
                            bOut.setName(output.getName());
                        }
                        bindingOperation.setBindingOutput(bOut);
                    }
                    
                    if(faults != null) {
                        for (Fault fault : faults) {
                            BindingFault bFault = mModel.getFactory().createBindingFault();
                            if(fault.getName() != null) {
                                bFault.setName(fault.getName());
                            }
                            bindingOperation.addBindingFault(bFault);
                        }
                        
                    }
                }
                mBinding.addBindingOperation(bindingOperation);
                lastAddedBindingOperation = bindingOperation;
            }

        }

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
                    createAndAddBindingOperationFault(fault.getName(), bOperation, bindingSubType);
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
                    createAndAddBindingOperationFault(fault.getName(), bOperation, bindingSubType);
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
    
}
