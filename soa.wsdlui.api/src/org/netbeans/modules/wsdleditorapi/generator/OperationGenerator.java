/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * OperationGenerator.java
 *
 * Created on September 6, 2006, 4:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdleditorapi.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mbhasin
 */
public class OperationGenerator implements Command {
    
    private WSDLModel mModel;
    
    private Map mConfigurationMap;
    
    private PortType mParentPortType;
    
    private Operation mOperation;
    
    private List<Message> mNewMessageList = new ArrayList<Message>();
    
    private Collection<Import> mImports = new ArrayList<Import>();
    
    /** Creates a new instance of OperationGenerator */
    public OperationGenerator(WSDLModel model, PortType parent, Map configurationMap) {
        this.mModel = model;
        this.mParentPortType = parent;
        this.mConfigurationMap = configurationMap;        
    }
    
    public Operation getOperation() {
        return this.mOperation;
    }
    
    public List<Message> getNewMessages() {
        return this.mNewMessageList;
    }
    
    public Collection<Import> getImports() {
        return this.mImports;
    }
    
    public void execute() {
        if(mModel != null) {
            //operation
            String operationName = (String) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_NAME);
            OperationType ot = (OperationType) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_TYPE);
            
            this.mOperation = createOperation(ot, mModel);
            this.mOperation.setName(operationName);
            this.mParentPortType.addOperation(this.mOperation);
            
            //opertion type
            List<PartAndElementOrType> inputMessageParts = 
                    (List<PartAndElementOrType>) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_INPUT);
            
            List<PartAndElementOrType> outputMessageParts = 
                    (List<PartAndElementOrType>) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_OUTPUT);
            
            List<PartAndElementOrType> faultMessageParts = 
                    (List<PartAndElementOrType>) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_FAULT);

            SchemaImportsGenerator schemaImportGenerator = new SchemaImportsGenerator(this.mModel, mConfigurationMap);
            schemaImportGenerator.execute();
            mImports.addAll(schemaImportGenerator.getImports());
            
            String inputMessageName = (String) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_INPUT_MESSAGE);
            String outputMessageName = (String) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_OUTPUT_MESSAGE);
            String faultMessageName  = (String) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_FAULT_MESSAGE);
                    
            processOperationType(ot, 
                                this.mOperation, 
                                inputMessageParts, 
                                outputMessageParts, 
                                faultMessageParts,
                                inputMessageName,
                                outputMessageName,
                                faultMessageName);
        }
       
    }
    
    private Operation createOperation(OperationType ot, 
                                          WSDLModel model) {
        Operation operation = null;
        if(ot.getOperationType().equals(OperationType.OPERATION_REQUEST_REPLY)) {
            operation = model.getFactory().createRequestResponseOperation();
        } else if(ot.getOperationType().equals(OperationType.OPERATION_ONE_WAY)) {
            operation = model.getFactory().createOneWayOperation();
        } else if (ot.getOperationType().equals(OperationType.OPERATION_SOLICIT_RESPONSE)) {
            operation = model.getFactory().createSolicitResponseOperation();
        } else if (ot.getOperationType().equals(OperationType.OPERATION_NOTIFICATION)) {
            operation = model.getFactory().createNotificationOperation();
        }

        return operation;
    }
    
    
    private void processOperationType(OperationType ot, 
                                      Operation op,
                                      List<PartAndElementOrType> inputMessageParts,
                                      List<PartAndElementOrType> outputMessageParts,
                                      List<PartAndElementOrType> faultMessageParts,
                                      String inputMessageName,
                                      String ouputMessageName,
                                      String faultMessageName) {
        Message inputMessage = null;
        Message outputMessage = null;
        Message faultMessage = null;
        NamedComponentReference<Message> inMessageRef = null;
        NamedComponentReference<Message> outMessageRef = null;
        NamedComponentReference<Message> faultMessageRef = null;
        
        //inputMessageName is provided by dialog not from wizard and this name could be name of 
        //new message which needs to be created or it could point to name of existing message.
        if(inputMessageName != null) {
            inputMessage = findMessage(inputMessageName);
        } else {
            //we are from wizard and inputMessageName is not provided
            inputMessageName = NameGenerator.getInstance().generateUniqueInputMessageName(op.getName(), this.mModel);
        }

        
        //inputMessageParts are provided
        if(inputMessage == null && inputMessageParts != null) {
            //inputMessage is not an existing message then create and add new message
            inputMessage = createAndAddMessage(inputMessageParts);
            inputMessage.setName(inputMessageName);
        }
        
        if(inputMessage != null) {
            inMessageRef = op.createReferenceTo(inputMessage, Message.class);
            createAndAddInput(op, inMessageRef); 
        }

        //ouputMessageName is provided by dialog not from wizard and this name could be name of 
        //new message which needs to be created or it could point to name of existing message.
        if(ouputMessageName != null) {
           outputMessage = findMessage(ouputMessageName);
        } else {
            //we are from wizard and ouputMessageName is not provided
            ouputMessageName = NameGenerator.getInstance().generateUniqueOutputMessageName(op.getName(), this.mModel);
        }
        
        if(outputMessage == null && outputMessageParts != null) {
            
            //ouputMessageName is not an existing message then create and add new message
            outputMessage = createAndAddMessage(outputMessageParts);
            outputMessage.setName(ouputMessageName);
        }
        
        if(outputMessage != null) {
            outMessageRef = op.createReferenceTo(outputMessage, Message.class);
            createAndAddOuput(op, outMessageRef);
        }
        
        //faultMessageName is provided by dialog not from wizard and this name could be name of 
        //new message which needs to be created or it could point to name of existing message.
        if(faultMessageName != null) {
            faultMessage = findMessage(faultMessageName);
        } else {
            faultMessageName = NameGenerator.getInstance().generateUniqueFaultMessageName(op.getName(), this.mModel);
        } 
        
        //if faultMessage is null meaning did not find existing message so we must create a new message
        if(faultMessage == null && faultMessageParts != null && faultMessageParts.size() > 0) {
            //for fault we create them only if user specifies atleast one part
            faultMessage = createAndAddMessage(faultMessageParts);
            faultMessage.setName(faultMessageName);
            
        }
        
        if(faultMessage != null) {
            faultMessageRef = op.createReferenceTo(faultMessage, Message.class);
            createAndAddFault(op, faultMessageRef);    
        }
        
    }
    
    private Input createAndAddInput(Operation op, NamedComponentReference<Message> messageRef) {
        Input in = this.mModel.getFactory().createInput();
        
        String operationInputName = NameGenerator.getInstance().generateUniqueOperationInputName(op);
        in.setName(operationInputName);
        op.setInput(in);
        if(messageRef != null) {
            in.setMessage(messageRef);
        }
        
        return in;
    }
    
    private Output createAndAddOuput(Operation op, NamedComponentReference<Message> messageRef) {
        Output out = this.mModel.getFactory().createOutput();
        
        String operationOutputName = NameGenerator.getInstance().generateUniqueOperationOutputName(op);
        out.setName(operationOutputName);
        op.setOutput(out);
        if(messageRef != null) {
            out.setMessage(messageRef);
        }
        
        return out;
    }
    
    private Fault createAndAddFault(Operation op, NamedComponentReference<Message> messageRef) {
        Fault fault = this.mModel.getFactory().createFault();
        
        String operationFaultName = NameGenerator.getInstance().generateUniqueOperationFaultName(op);
        fault.setName(operationFaultName);
        op.addFault(fault);
        if(messageRef != null) {
            fault.setMessage(messageRef);
        }
        
        return fault;
    }
    
    private Message createAndAddMessage(List<PartAndElementOrType> inputMessageParts) {
        Message msg = this.mModel.getFactory().createMessage();
        this.mModel.getDefinitions().addMessage(msg);
        mNewMessageList.add(msg);
        
        if(inputMessageParts != null) {
            Iterator<PartAndElementOrType> it = inputMessageParts.iterator();
            while(it.hasNext()) {
                PartAndElementOrType partOrElement = it.next();
                String partName = partOrElement.getPartName();
                ElementOrType elementOrType  = partOrElement.getElementOrType();

                Part part = this.mModel.getFactory().createPart();
                part.setName(partName);
                msg.addPart(part);
                if(elementOrType != null) {
                    GlobalElement element = elementOrType.getElement();
                    GlobalType type = elementOrType.getType();
                    if(element != null) {
                        NamedComponentReference<GlobalElement> elementRef =  part.createSchemaReference(element, GlobalElement.class);
                        if(elementRef != null) {
                            part.setElement(elementRef);
                        }
                    } else if(type != null) {
                        NamedComponentReference<GlobalType> typeRef =  part.createSchemaReference(type, GlobalType.class);
                        if(typeRef != null) {
                            part.setType(typeRef);
                        }
                    }

                }
            }
        }
        return msg;
    }
    
    private Message findMessage(String messageName) {
        QName qName = constructQName(messageName);
        if(qName != null) {
            return this.mModel.findComponentByName(qName, Message.class);
        }
        
        return null;
    }
    
    private QName constructQName(String name) {
        if(name == null) {
            return null;
        }
        
        QName qName = null;
        int prefixIndex = name.lastIndexOf(":");
        String prefix = "";
        String namespace = null;
        String localPart = null;
        if(prefixIndex != -1) {
            prefix = name.substring(0, prefixIndex);
            localPart = name.substring(prefixIndex + 1);
            namespace = ((AbstractDocumentComponent) this.mModel.getDefinitions()).lookupNamespaceURI(prefix);
        } else {
            localPart = name;
            namespace = this.mModel.getDefinitions().getTargetNamespace();
        }
        
        qName = new QName(namespace, localPart, prefix);
        
        return qName;        
    }
}
