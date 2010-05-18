/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileBinding;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.validator.Utils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

/**
 * Various convenient apis
 * 
 * @author jalmero
 */
public class FileUtilities {
    
    private static final Logger mLogger = Logger.
            getLogger(FileUtilities.class.getName());
    
    public static FileAddress getFileAddress(FileBinding fileBinding) {
        FileAddress fileAddress = null;
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<FileAddress> fileAddresses = port.
                                    getExtensibilityElements(FileAddress.class).
                                    iterator();
                            
                            // 1 fileaddress for 1 binding
                            while (fileAddresses.hasNext()) {
                                return fileAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return fileAddress;
    }

    public static FileAddress getFileAddress(Definitions defs) {
        FileAddress fileAddress = null;
        if (defs != null) {
            Iterator<Service> services = defs.getServices().iterator();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if (port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                            Iterator<FileAddress> fileAddresses = port.
                                    getExtensibilityElements(FileAddress.class).
                                    iterator();
                            
                            // 1 fileaddress for 1 binding
                            while (fileAddresses.hasNext()) {
                                return fileAddresses.next();
                            }
                    }
                }
            }
        }
        return fileAddress;
    }    
    
    public static Operation getOperation(Binding binding,
            String selectedOperation) {
        Operation op = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    op = bop.getOperation().get();
                    break;
                }
            }
        }
        return op;
    }    
    
    public BindingInput getBindingInput(Binding binding,
            String selectedOperation) {
        BindingInput bindingInput = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    bindingInput = bop.getBindingInput();
                    break;
                }
            }
        }
        return bindingInput;
    }   

    public BindingOutput getBindingOutput(Binding binding,
            String selectedOperation) {
        BindingOutput bindingOutput = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    bindingOutput = bop.getBindingOutput();
                    break;
                }
            }
        }
        return bindingOutput;
    }     
    
    public static Message createMessage(WSDLModel model, String msgName) {        
        Message newMessage = getMessage(msgName, model);
        if ((newMessage == null) && (model != null)) {
            try {   
                if (!model.isIntransaction()) {
                    model.startTransaction();
                }
                newMessage = model.getFactory().createMessage();
                newMessage.setName(msgName);
                model.getDefinitions().addMessage(newMessage);
            } catch (Exception ex) {
                mLogger.finer(ex.getMessage());
//            } finally {
//                if (model.isIntransaction()) {
//                    model.endTransaction();
//                }                
            }  
        }
        return newMessage;
    }   
    
    public static Part createPart(WSDLModel model, Message msg, String name) {
        Part newPart = getMessagePart(name, msg);
        if ((newPart == null) && (model != null)) {
            try {
                if (!model.isIntransaction()) {
                    model.startTransaction();
                }                
                newPart = model.getFactory().createPart();
                newPart.setName(name);
                newPart.setType(newPart.createSchemaReference(
                        FileUtilities.getPrimitiveType("string"), 
                        GlobalType.class));
                msg.addPart(newPart);
            }catch (Exception ex) {
                mLogger.finer(ex.getMessage());
            }  
        }
        return newPart;
    }

    public static Part getMessagePart(String name, Message message) {
        Part part = null;
        Collection parts = message.getParts();

        Iterator it = parts.iterator();
        while(it.hasNext()) {
            part = (Part) it.next();
            if(name.equals(part.getName())) {
                return part;
            }
        }
        return null;
    }    
    
    public static GlobalSimpleType getPrimitiveType(String typeName){
        SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
        for(GlobalSimpleType ptype: primitives){
            if(ptype.getName().equals(typeName)){
                return ptype;
            }
        }
        return null;
    }
    
    public boolean isMessageExists(String name, WSDLModel document) {
        boolean exists = false;
        Collection messages = document.getDefinitions().getMessages();
        
        Iterator it = messages.iterator();
        while(it.hasNext()) {
            Message message = (Message) it.next();
            if(name.equals(message.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }    
    
    public static Message getMessage(String name, WSDLModel document) {        
        Collection messages = document.getDefinitions().getMessages();        
        Iterator it = messages.iterator();
        while(it.hasNext()) {
            Message message = (Message) it.next();
            if(name.equals(message.getName())) {
                return message;
            }
        }        
        return null;
    }   
    
    public static Collection getInputParts(Binding binding, String opName) {
        Collection<Part> inputParts = new ArrayList<Part>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            while(iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Input input = op.getInput();
                    if (input != null) {
                        NamedComponentReference<Message> messageIn = input.getMessage();
                        if ((messageIn != null) && (messageIn.get() != null)) {
                            Message msgIn = messageIn.get();
                            Collection parts = msgIn.getParts();
                            Iterator partIter = parts.iterator();
                            while (partIter.hasNext()) {
                                Part part = (Part) partIter.next();
                                inputParts.add(part);
                            }
                        }
                    }
                }
            }
        }
        return inputParts;
    }

    public static Collection getOutputParts(Binding binding, String opName) {
        Collection<Part> outputParts = new ArrayList<Part>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            while(iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Output output = op.getOutput();
                    if (output != null) {
                        NamedComponentReference<Message> messageOut = 
                                output.getMessage();
                        if ((messageOut != null) && (messageOut.get() != null)) {
                            Message msgOut = messageOut.get();
                            Collection parts = msgOut.getParts();
                            Iterator partIter = parts.iterator();
                            while (partIter.hasNext()) {
                                Part part = (Part) partIter.next();
                                outputParts.add(part);
                            }
                        }
                    }
                }
            }
        }
        return outputParts;
    }
    
    public static Part createPartAndSetType(WSDLModel model, Message parentMessage,
            String partName, int messageType, GlobalType gT, GlobalElement gE) {
        Part newPart = null;
        if ((model != null) && (parentMessage != null)) {
            newPart = createPart(model,
                    parentMessage, partName);
            if (newPart != null) {
                // then create a type per Message Type selected
                // ie xsd:string, xsd:<...>, or ns:typeA something
                setPartType(newPart, messageType, gT, gE);
                
                if (getMessagePart(newPart.getName(), 
                        parentMessage) == null) {
                    parentMessage.addPart(newPart);
                }                
            }
        }
        return newPart;
    }

    public static String getPartTypeOrElementString(Part part) {
        String s = getPartTypeString(part);
        
        if (s == null) {
            s = getPartElementString(part);
        }
        
        if (s == null) {
            s = NbBundle.getMessage(FileUtilities.class, "FileUtilities.LBL_Undefined"); // NOI18N
        }
        
        return s;
    }
        
    public static String getPartTypeString(Part part) {
        if (part.getType() == null) return null;
        return convertQNameToString(part.getType().getQName());
    }
    
    
    public static String getPartElementString(Part part) {
        if (part.getElement() == null) return null;
        return convertQNameToString(part.getElement().getQName());
    }
    
    
    private static String convertQNameToString(QName qname) {
        if (qname == null) return null;
        return qname.getPrefix() + ":" + qname.getLocalPart(); // NOI18N
    }
    
    public static Project getProject(WSDLComponent component) {
        if (component != null) {
            WSDLModel wsdlModel = component.getModel();
            ModelSource modelSource = wsdlModel.getModelSource();
            FileObject wsdlFile = modelSource.getLookup().lookup(FileObject.class);
            if(wsdlFile != null) {
                return FileOwnerQuery.getOwner(wsdlFile);
            }
        } 
        return null;
    }
    
    public static void setPartType(Part part, int messageType, GlobalType gT, 
         GlobalElement gE) {
        SchemaComponent schemaComponent = null;
        if ((part != null) && (part.getModel() != null)) {
            if (!part.getModel().isIntransaction()) {
               part.getModel().startTransaction(); 
            }        
        }
        
        if (messageType == FileConstants.XML_MESSAGE_TYPE) {
            if (gT != null) {
                part.setType(part.createSchemaReference(
                        gT, GlobalType.class));
                part.setElement(null);
                schemaComponent = gT;
            } else if (gE != null) {
                part.setElement(part.createSchemaReference(
                        gE, GlobalElement.class));
                part.setType(null);  
                schemaComponent = gE;
            }
        } else if (messageType ==
                FileConstants.TEXT_MESSAGE_TYPE) {
            part.setType(part.createSchemaReference(
                    FileUtilities.getPrimitiveType("string"),
                    GlobalType.class));
            part.setElement(null);
        } else if (messageType ==
                FileConstants.ENCODED_MESSAGE_TYPE) {
            if (gT != null) {
                part.setType(part.createSchemaReference(
                        gT, GlobalType.class));
                part.setElement(null);
                schemaComponent = gT;
            } else if (gE != null) {
                part.setType(null);
                part.setElement(part.createSchemaReference(
                        gE, GlobalElement.class));
                schemaComponent = gE;
            }
        } else if (messageType ==
                FileConstants.BINARY_MESSAGE_TYPE) {
            part.setType(part.createSchemaReference(
                    FileUtilities.getPrimitiveType(FileConstants.BASE64_BINARY),
                    GlobalType.class));
            part.setElement(null);
        } 
        BindingComponentUtils.addSchemaImport(schemaComponent, part.getModel());                
    } 
    
    public PortType getPortType(String bindingName, FileAddress fileAddress) {
        if ((fileAddress != null) && (fileAddress.getParent() != null)) {
            Port parentPort = (Port) fileAddress.getParent();
            Service parentService = (Service) parentPort.getParent();
            Definitions defs = (Definitions) parentService.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }
                NamedComponentReference<PortType> portType = binding.getType();
                if (binding.getName().equals(bindingName)) {
                    return portType.get();
                }
            }
        }
        return null;
    }
    
    public static PortType getPortType(FileAddress fileAddress) {
        PortType portType = null;
        if (fileAddress == null) {
            return portType;
        }
        Port port = (Port) fileAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            if ((binding != null) && (binding.getType() != null)) {                
                NamedComponentReference<PortType> pType = binding.getType();
                portType = pType.get();
            }
        }  
        return portType;
    }
    
    public static void setPart(int messageType, FileMessage fileMessage, String partName) {
        if (messageType == FileConstants.BINARY_MESSAGE_TYPE) {
            fileMessage.setAttribute(FileMessage.ATTR_FILE_TYPE, 
                    FileConstants.BINARY);            
        } else if (messageType == FileConstants.TEXT_MESSAGE_TYPE) {
            fileMessage.setAttribute(FileMessage.ATTR_FILE_TYPE, null); 
        }  else if (messageType == FileConstants.ENCODED_MESSAGE_TYPE) {
            fileMessage.setAttribute(FileMessage.ATTR_FILE_TYPE, null); 
        }  else if (messageType == FileConstants.XML_MESSAGE_TYPE) {
            fileMessage.setAttribute(FileMessage.ATTR_FILE_TYPE, null);
        }     
        // update on if changed
        if (fileMessage.getPart() != null) {
            if (!fileMessage.getPart().equals(partName)) {
                fileMessage.setPart(partName);
            }
        } else {
            if (partName != null) {
                fileMessage.setPart(partName);
            }
        }       
    }
    
    /**
     * Returns true if the value is a valid variable format
     * @param val
     * @return boolean
     */
    public static boolean validVariableEntry(String val) {
        boolean valid = true;
        if ((val != null) && (val.length() > 0)) {
            try {
                Integer intVal = Integer.valueOf(val);
            } catch (NumberFormatException ex) {
                // check for valid variable format   
                try {
                    if (!Utils.hasMigrationEnvVarRef(val)) {
                        valid = false;
                    }
                } catch (Exception utilEx) {
                     mLogger.log(Level.FINER, utilEx.getMessage());
                }
            }
        }
        return valid;
    }
        
}
