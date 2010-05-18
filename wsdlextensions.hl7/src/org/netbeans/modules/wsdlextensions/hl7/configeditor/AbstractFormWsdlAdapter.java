/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vishnuvardhan P.R
 * @author Jun Qian
 */
public class AbstractFormWsdlAdapter extends AbstractFormModel {

    private static final Logger logger = Logger.getLogger(
            AbstractFormWsdlAdapter.class.getName());

    private WSDLModel model;

    private Schema defaultInlineSchema = null;
    private HashMap<String, String> locationNamespaceMap = null;
    private WSDLSchema wsdlSchema = null;

    /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public AbstractFormWsdlAdapter(WSDLModel model) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        this.model = model;
    }

    public void commit() {
        Definitions defs = model.getDefinitions();
        PortType portType = defs.getPortTypes().iterator().next();
        Binding binding = defs.getBindings().iterator().next();

        for (Message message : defs.getMessages()) {
            defs.removeMessage(message);
        }

        for (Operation operation : portType.getOperations()) {
            portType.removeOperation(operation);
        }

        for (BindingOperation bindingOperation : binding.getBindingOperations()) {
            binding.removeBindingOperation(bindingOperation);
        }

        WSDLComponentFactory factory = model.getFactory();

        if (wsdlSchema != null ) { //&& defaultInlineSchema.getImports().size() > 0) {
            defs.getTypes().addExtensibilityElement(wsdlSchema);
        }

        for (int i = 0; i < getOperationNames().size(); i++) {

            String operationName = getOperationNames().get(i);
            String messageType = getMessageTypes().get(i);
            ElementOrType inputEOT = getInputEOTs().get(i);
            ElementOrType outputEOT = getOutputEOTs().get(i);

            if (inputEOT != null) {
                createSchema(inputEOT, defaultInlineSchema, locationNamespaceMap);
            }
            if (outputEOT != null) {
                createSchema(outputEOT, defaultInlineSchema, locationNamespaceMap);
            }
            
            // message section
            Message requestMessage = null;
            Message responseMessage = null;

            if (inputEOT != null) {
                requestMessage = factory.createMessage();
                requestMessage.setName(operationName + "Request");

                Part requestMessagePart = factory.createPart();
                requestMessagePart.setName("part1");
                updatePart(inputEOT, requestMessagePart);

                requestMessage.addPart(requestMessagePart);
                defs.addMessage(requestMessage);
            }

            if (outputEOT != null) {
                responseMessage = factory.createMessage();
                responseMessage.setName(operationName + "Response");

                Part responseMessagePart = factory.createPart();
                responseMessagePart.setName("part1");
                updatePart(outputEOT, responseMessagePart);

                responseMessage.addPart(responseMessagePart);
                defs.addMessage(responseMessage);
            }

            // operation section
            Operation operation = outputEOT == null ? 
                factory.createOneWayOperation() :
                factory.createRequestResponseOperation(); // TODO: what about OutOnly and OutIn?
            operation.setName(operationName);

            String operationInputName = null;
            String operationOutputName = null;

            if (inputEOT != null) {
                operationInputName = operationName + "Input";
                Input input = factory.createInput();
                input.setName(operationInputName);
                input.setMessage(input.createReferenceTo(requestMessage, Message.class));
                operation.setInput(input);
            }

            if (outputEOT != null) {
                operationOutputName = operationName + "Output";
                Output output = factory.createOutput();
                output.setName(operationOutputName);
                output.setMessage(output.createReferenceTo(responseMessage, Message.class));
                operation.setOutput(output);
            }

            portType.addOperation(operation);

            // binding operation section
            BindingOperation bindingOperation = factory.createBindingOperation();
            bindingOperation.setName(operationName);

            WSDLComponent hl7Operation = factory.create(bindingOperation,
                    HL7QName.OPERATION.getQName());
            if (messageType != null) {
                hl7Operation.setAttribute("messageType",
                        new StringAttribute("messageType"), messageType);
            }
            bindingOperation.addExtensibilityElement((ExtensibilityElement) hl7Operation);

            if (inputEOT != null) {
                BindingInput bindingInput = factory.createBindingInput();
                bindingInput.setName(operationInputName);

                WSDLComponent hl7InputMessage = factory.create(bindingOperation,
                        HL7QName.MESSAGE.getQName());
                hl7InputMessage.setAttribute("use", new StringAttribute("use"), "encoded");
                hl7InputMessage.setAttribute("encodingStyle",
                        new StringAttribute("encodingStyle"), "hl7encoder-1.0");
                hl7InputMessage.setAttribute("part", new StringAttribute("part"), "part1");
                bindingInput.addExtensibilityElement((ExtensibilityElement) hl7InputMessage);
                bindingOperation.setBindingInput(bindingInput);
            }

            if (outputEOT != null) {
                BindingOutput bindingOutput = factory.createBindingOutput();
                bindingOutput.setName(operationOutputName);

                WSDLComponent hl7OutputMessage = factory.create(bindingOperation,
                        HL7QName.MESSAGE.getQName());
                hl7OutputMessage.setAttribute("use", new StringAttribute("use"), "encoded");
                hl7OutputMessage.setAttribute("encodingStyle",
                        new StringAttribute("encodingStyle"), "hl7encoder-1.0");
                hl7OutputMessage.setAttribute("part", new StringAttribute("part"), "part1");
                bindingOutput.addExtensibilityElement((ExtensibilityElement) hl7OutputMessage);
                bindingOperation.setBindingOutput(bindingOutput);
            }

            binding.addBindingOperation(bindingOperation);
        }
    }

    public static class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        public Class getType() { return String.class; }
        public String getName() { return name; }
        public Class getMemberType() { return null; }
    }

    public void focus(WSDLModel mModel) {
        Definitions def = mModel.getDefinitions();
        Types types = def.getTypes();
        if (types == null) {
            logger.info("types is null. Creating types");
            types = mModel.getFactory().createTypes();
            def.setTypes(types);
        }
        //if <schema> already exists, grap the schema object in defaultInlineSchema

        String wsdlTNS = def.getTargetNamespace();
        if (wsdlTNS != null) {
            Collection<Schema> schmas = types.getSchemas();
            if (schmas != null) {
                for (Schema s : schmas) {
                    if (s.getTargetNamespace() != null && s.getTargetNamespace().equals(wsdlTNS)) {
                        defaultInlineSchema = s;
                        logger.info("Found default inline schema");
                        break;
                    }
                }
            }
        }
        //If defaultInlineSchema does not exists, create one
        wsdlSchema = null;
        if (defaultInlineSchema == null) {
            wsdlSchema = mModel.getFactory().createWSDLSchema();
            SchemaModel schemaModel = wsdlSchema.getSchemaModel();
            defaultInlineSchema = schemaModel.getSchema();
            defaultInlineSchema.setTargetNamespace(mModel.getDefinitions().getTargetNamespace());

            logger.info("Could not find defaultInlineSchema. Created one");
        }

        locationNamespaceMap = new HashMap<String, String>();
    }

    private void createSchema(ElementOrType eot, Schema defaultInlineSchema,
            HashMap<String, String> locationNamespaceMap) {

        GlobalElement element = eot.getElement();
        GlobalType type = eot.getType();
        SchemaModel schModel = null;
        if (element != null) {
            schModel = element.getModel();
            logger.info("Element Selected for eot");
        } else if (type != null) {
            schModel = type.getModel();
            logger.info("Type Selected for eot");
        }

        if (schModel != null) {
            logger.info("schModel is not null");
            String schemaTNS = schModel.getSchema().getTargetNamespace();
            if (schemaTNS != null && !schemaTNS.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {

                FileObject fo = schModel.getModelSource().getLookup().lookup(FileObject.class);

                if (fo != null) {
                    String path = null;
                    // generate absolute URI, this will get changed later in wizard post process import
                    path = FileUtil.toFile(fo).toURI().toString();
                    if (path != null &&
                            !(locationNamespaceMap.containsKey(schemaTNS) &&
                            locationNamespaceMap.get(schemaTNS).equals(path))) {
                        Import schemaImport = defaultInlineSchema.getModel().getFactory().createImport();
                        schemaImport.setNamespace(schemaTNS);
                        schemaImport.setSchemaLocation(path);
                        defaultInlineSchema.addExternalReference(schemaImport);
                        locationNamespaceMap.put(schemaTNS, path);
                        logger.info("Added schemaIimport to defaultInlineSchema");
//                    mImports.add(schemaImport);
                    }
                }
            }
        }
    }

    private QName constructQName(WSDLModel wsdlModel, String name) {
        if (name == null) {
            return null;
        }

        QName qName = null;
        int prefixIndex = name.lastIndexOf(":");
        String prefix = "";
        String namespace = null;
        String localPart = null;
        if (prefixIndex != -1) {
            prefix = name.substring(0, prefixIndex);
            localPart = name.substring(prefixIndex + 1);
            namespace = ((AbstractDocumentComponent) wsdlModel.getDefinitions()).lookupNamespaceURI(prefix);
        } else {
            localPart = name;
            namespace = wsdlModel.getDefinitions().getTargetNamespace();
        }

        qName = new QName(namespace, localPart, prefix);

        return qName;
    }

    private void updatePart(ElementOrType eot, Part part) {

        GlobalElement element = eot.getElement();
        GlobalType type = eot.getType();
        if (element != null) {
            part.setType(null);
            NamedComponentReference<GlobalElement> elementRef =
                    part.createSchemaReference(element, GlobalElement.class);
            if (elementRef != null) {
                part.setElement(elementRef);
                logger.info("Updated the part with element:" + elementRef);
            }
        } else if (type != null) {
            part.setElement(null);
            NamedComponentReference<GlobalType> typeRef =
                    part.createSchemaReference(type, GlobalType.class);
            if (typeRef != null) {
                part.setType(typeRef);
                logger.info("Updated the part with Type:" + typeRef);
            }
        }
    }
}
