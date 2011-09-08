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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.wsdl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.e2e.api.schema.SchemaException;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingFault;
import org.netbeans.modules.e2e.api.wsdl.BindingInput;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.BindingOutput;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Fault;
import org.netbeans.modules.e2e.api.wsdl.Input;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Output;
import org.netbeans.modules.e2e.api.wsdl.Part;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBody;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPFault;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPHeader;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.schema.SchemaParser;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPAddressImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPBindingImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPBodyImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPConstants;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPFaultImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPHeaderImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPOperationImpl;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Michal Skvor
 */
public class WSDLParser extends DefaultHandler {

    private static final String SCHEMA  = "http://www.w3.org/2001/XMLSchema"; // NOI18N
        
    /* WSDL states */
    private static final String DEFINITION          = "definitions"; // NOI18N
    private static final String MESSAGE             = "message"; // NOI18N
    private static final String PART                = "part"; // NOI18N
    private static final String SERVICE             = "service"; // NOI18N
    private static final String PORT                = "port"; // NOI18N
    private static final String PORT_TYPE           = "portType"; // NOI18N
    private static final String OPERATION           = "operation"; // NOI18N
    
    private static final String BINDING             = "binding"; // NOI18N
    private static final String BINDING_OPERATION   = "binding-operation"; // NOI18N
    private static final String BINDING_INPUT       = "binding-input"; // NOI18N
    private static final String BINDING_OUTPUT      = "binding-output"; // NOI18N
    private static final String BINDING_FAULT       = "binding-fault"; // NOI18N

    private static final String INPUT               = "input"; // NOI18N
    private static final String OUTPUT              = "output"; // NOI18N
    private static final String FAULT               = "fault"; // NOI18N
    
    private List<WSDL2Java.ValidationResult> validationResults;    
    private URL                              myOriginalWsdlUri;
    
    public WSDLParser(URL originalWsdlUri) {
        validationResults = new ArrayList<WSDL2Java.ValidationResult>();
        myOriginalWsdlUri = originalWsdlUri;
    }

    public Definition parse(String uri) throws WSDLException {
        definition = new DefinitionImpl();

//        DefaultHandler handler = new DefaultHandlerImpl( definition );
        SchemaParser schemaParser = new SchemaParser();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware(true);

            SAXParser parser = spf.newSAXParser();
            parser.parse(uri, this);
            try {
                /*
                 * Fix for IZ#120452 - JSR172: Client isn't generated when there is a relative path to xsd in wsdl
                 */
                schemaParser.parseLocation(myOriginalWsdlUri, definition.getTargetNamespace());
            } catch (SchemaException ex) {
                throw new WSDLException(ex);
            }
        } catch (WSDLException e) {
            validationResults.add(new WSDL2Java.ValidationResult(
                    WSDL2Java.ValidationResult.ErrorLevel.FATAL, e.getMessage()));
        } catch (SAXException e) {
            validationResults.add(new WSDL2Java.ValidationResult(
                    WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Error during parsing : " + e.getMessage())); // NOI18N
        } catch (ParserConfigurationException e) {
            validationResults.add(new WSDL2Java.ValidationResult(
                    WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Error during parsing : " + e.getMessage())); // NOI18N
        } catch (IOException e) {
            validationResults.add(new WSDL2Java.ValidationResult(
                    WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Communication error : " + e.getMessage())); // NOI18N
        }
        definition.setSchemaHolder(schemaParser.getSchemaHolder());
        validationResults.addAll(schemaParser.getValidationResults());

        return definition;
    }

    public List<WSDL2Java.ValidationResult> getValidationResults() {
        return Collections.unmodifiableList(validationResults);
    }

    private Stack<String> state = new Stack<String>();
    
    private Definition definition;
    private Message message;
    private Part part;
    private Service service;
    private Port port;
    private PortType portType;
    private Operation operation;
    private Input input;
    private Output output;
    private Fault fault;
    
    private Binding binding;
    private BindingOperation bindingOperation;
    private BindingInput bindingInput;
    private BindingOutput bindingOutput;
    private BindingFault bindingFault;
    
    private Map<String, String> prefixMapping = new HashMap<String, String>();
    private String targetNamespace;
    
    private SOAPAddress soapAddress;
    private SOAPBinding soapBinding;
    private SOAPOperation soapOperation;
    private SOAPBody soapBody;
    private SOAPHeader soapHeader;
    private SOAPHeaderFault soapHeaderFault;
    private SOAPFault soapFault;
    
    private String tagString;

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMapping.put(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        tagString = "";
        if (uri.equals(SOAPConstants.SOAP_URI)) {
            // soap:address
            if (localName.equals(SOAPConstants.ADDRESS.getLocalPart()) & state.peek().equals(PORT)) {
                soapAddress = new SOAPAddressImpl(attributes.getValue("location")); // NOI18N
                return;
            }
            // soap:binding
            if (localName.equals(SOAPConstants.BINDING.getLocalPart()) && state.peek().equals(BINDING)) {
                String style = attributes.getValue("style") != null ? attributes.getValue("style") : SOAPConstants.STYLE_DOCUMENT; // NOI18N
                soapBinding = new SOAPBindingImpl(attributes.getValue("transport"), style); // NOI18N
                return;
            }
            // soap:operation
            if (localName.equals(SOAPConstants.OPERATION.getLocalPart()) && state.peek().equals(BINDING_OPERATION)) {
                soapOperation = new SOAPOperationImpl();
                // TODO: validation
                soapOperation.setSoapActionURI(attributes.getValue("soapAction")); // NOI18N
                if (attributes.getValue("style") == null && soapBinding == null) { // NOI18N
                    throw new SAXException(new WSDLException("Missing mandatory SOAP style definition.")); // NOI18N
                }
                String style = attributes.getValue("style") != null ? attributes.getValue("style") : soapBinding.getStyle(); // NOI18N
                soapOperation.setStyle(style);

                return;
            }
            // soap:body
            if (localName.equals(SOAPConstants.BODY.getLocalPart()) && (state.peek().equals(BINDING_INPUT) || state.peek().equals(BINDING_OUTPUT))) {
                soapBody = new SOAPBodyImpl(attributes.getValue("use")); // NOI18N
                // TODO: parse parts
                // TODO: parse encoding styles
                soapBody.setNamespaceURI(attributes.getValue("namespace")); // NOI18N
                return;
            }
            // soap:header
            if (localName.equals(SOAPConstants.HEADER.getLocalPart()) && (state.peek().equals(BINDING_INPUT) || state.peek().equals(BINDING_OUTPUT))) {
                soapHeader = new SOAPHeaderImpl(parseQName(attributes.getValue("message")), // NOI18N
                        attributes.getValue("part"), attributes.getValue("use")); // NOI18N
                // TODO: parse encoding styles
                soapHeader.setNamespaceURI(attributes.getValue("namespace")); // NOI18N
                return;
            }
            // soap:headerfault
            // TODO: soap:headerfault
            // soap:fault
            if (localName.equals(SOAPConstants.FAULT.getLocalPart()) && state.peek().equals(BINDING_FAULT)) {
                soapFault = new SOAPFaultImpl(attributes.getValue("name"), attributes.getValue("use")); // NOI18N
                // TODO: parse encoding style
                soapFault.setNamespaceURI(attributes.getValue("namespace")); // NOI18N
                return;
            }
        }

        if (uri.equals(WSDLConstants.WSDL_URI)) {
            if (localName.equals(DEFINITION) && state.empty()) {
                targetNamespace = attributes.getValue("targetNamespace"); // NOI18N
                definition.setTargetNamespace(targetNamespace);
                state.push(DEFINITION);
                return;
            }

            // Message
            if (localName.equals(WSDLConstants.MESSAGE.getLocalPart()) && state.peek().equals(DEFINITION)) {
                state.push(MESSAGE);
                String name = attributes.getValue("name"); // NOI18N
                message = new MessageImpl(new QName(targetNamespace, name));
                return;
            }
            // Message Part
            if (localName.equals(WSDLConstants.PART.getLocalPart()) && state.peek().equals(MESSAGE)) {
                state.push(PART);
                part = new PartImpl(attributes.getValue("name"), // NOI18N
                        parseQName(attributes.getValue("type")), parseQName(attributes.getValue("element"))); // NOI18N
                return;
            }

            // PortType
            if (localName.equals(WSDLConstants.PORT_TYPE.getLocalPart()) && state.peek().equals(DEFINITION)) {
                state.push(PORT_TYPE);
                portType = new PortTypeImpl(new QName(targetNamespace, attributes.getValue("name"))); // NOI18N
                return;
            }
            // Operation
            if (localName.equals(WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals(PORT_TYPE)) {
                state.push(OPERATION);
                operation = new OperationImpl(attributes.getValue("name")); // NOI18N
                // TODO: parameterOrder
                return;
            }
            if (localName.equals(WSDLConstants.INPUT.getLocalPart()) && state.peek().equals(OPERATION)) {
                state.push(INPUT);
                QName messageRef = parseQName(attributes.getValue("message")); // NOI18N
                Message message = definition.getMessage(messageRef);
                if (message == null) {
                    message = new MessageImpl.MessageReferenceImpl(messageRef);
                }
                input = new InputImpl(attributes.getValue("name"), // NOI18N
                        message);
                return;
            }
            if (localName.equals(WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals(OPERATION)) {
                state.push(OUTPUT);
                QName messageRef = parseQName(attributes.getValue("message")); // NOI18N
                Message message = definition.getMessage(messageRef);
                if (message == null) {
                    message = new MessageImpl.MessageReferenceImpl(messageRef);
                }
                output = new OutputImpl(attributes.getValue("name"), // NOI18N
                        message);
                return;
            }
            // Fault
            if (localName.equals(WSDLConstants.FAULT.getLocalPart()) && state.peek().equals(OPERATION)) {
                state.push(FAULT);
                QName messageName = parseQName(attributes.getValue("message")); // NOI18N
                fault = new FaultImpl(attributes.getValue("name"), // NOI18N
                        definition.getMessage(messageName));
                return;
            }

            // Binding
            if (localName.equals(WSDLConstants.BINDING.getLocalPart()) && state.peek().equals(DEFINITION)) {
                state.push(BINDING);
                QName typeQName = parseQName(attributes.getValue("type")); // NOI18N
                binding = new BindingImpl(attributes.getValue("name")); // NOI18N
                PortType portType = definition.getPortType(typeQName);
                if (portType == null) {
                    portType = new PortTypeImpl.PortTypeReferenceImpl(typeQName);
                }

                binding.setPortType(portType);
                return;
            }
            // BindingOperation
            if (localName.equals(WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals(BINDING)) {
                state.push(BINDING_OPERATION);
                bindingOperation = new BindingOperationImpl(attributes.getValue("name")); // NOI18N
                return;
            }
            if (localName.equals(WSDLConstants.INPUT.getLocalPart()) && state.peek().equals(BINDING_OPERATION)) {
                state.push(BINDING_INPUT);
                bindingInput = new BindingInputImpl(attributes.getValue("name")); // NOI18N
                return;
            }
            if (localName.equals(WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals(BINDING_OPERATION)) {
                state.push(BINDING_OUTPUT);
                bindingOutput = new BindingOutputImpl(attributes.getValue("name")); // NOI18N
                return;
            }
            if (localName.equals(WSDLConstants.FAULT.getLocalPart()) && state.peek().equals(BINDING_OPERATION)) {
                state.push(BINDING_FAULT);
                bindingFault = new BindingFaultImpl(attributes.getValue("name")); // NOI18N
                return;
            }

            // Service
            if (localName.equals(WSDLConstants.SERVICE.getLocalPart()) && state.peek().equals(DEFINITION)) {
                state.push(SERVICE);
                service = new ServiceImpl(attributes.getValue("name")); // NOI18N
                return;
            }
            // Port
            if (localName.equals(WSDLConstants.PORT.getLocalPart()) && state.peek().equals(SERVICE)) {
                state.push(PORT);
                Binding b = definition.getBinding(parseQName(attributes.getValue("binding")).getLocalPart()); // NOI18N
                // TODO: check for null
                port = new PortImpl(attributes.getValue("name"), b); // NOI18N
                return;
            }
            // Import
            if (localName.equals(WSDLConstants.IMPORT.getLocalPart()) && state.peek().equals(DEFINITION)) {
                try {
                    String namespace = attributes.getValue("namespace"); // NOI18N
                    String location = attributes.getValue("location"); // NOI18N

                    /*
                     * Fix for IZ#153030 - JSR172: WSDL Validation failed if it contains imported wsdl with relative path
                     */
                    URI u = myOriginalWsdlUri.toURI();
                    URI sl = u.normalize().resolve(location);
                    WSDLParser parser = new WSDLParser(sl.toURL());
                    Definition d = parser.parse(sl.toString());

                    for (Binding b : d.getBindings().values()) {
                        definition.addBinding(b);
                    }
                    for (Message m : d.getMessages().values()) {
                        definition.addMessage(m);
                    }
                    for (Service s : d.getServices().values()) {
                        definition.addService(s);
                    }
                    for (PortType pt : d.getPortTypes().values()) {
                        definition.addPortType(pt);
                    }
                    if (d.getSchemaHolder() != null) {
                        definition.setSchemaHolder(d.getSchemaHolder());
                    }

                    validationResults.addAll(parser.getValidationResults());
                    return;
                } catch (WSDLException e) {
                    Exceptions.printStackTrace(e);
                } catch (URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (uri.equals(SOAPConstants.SOAP_URI)) {
            // soap:address
            if (localName.equals(SOAPConstants.ADDRESS.getLocalPart()) & state.peek().equals(PORT)) {
                port.addExtensibilityElement(soapAddress);
                return;
            }
            // soap:binding
            if (localName.equals(SOAPConstants.BINDING.getLocalPart()) && state.peek().equals(BINDING)) {
                binding.addExtensibilityElement(soapBinding);
                return;
            }
            // soap:operation
            if (localName.equals(SOAPConstants.OPERATION.getLocalPart()) && state.peek().equals(BINDING_OPERATION)) {
                // TODO: validation
                bindingOperation.addExtensibilityElement(soapOperation);
                return;
            }
            // soap:body
            if (localName.equals(SOAPConstants.BODY.getLocalPart())) {
                if (state.peek().equals(BINDING_INPUT)) {
                    bindingInput.addExtensibilityElement(soapBody);
                }
                if (state.peek().equals(BINDING_OUTPUT)) {
                    bindingOutput.addExtensibilityElement(soapBody);
                }
                return;
            }
            // soap:header
            if (localName.equals(SOAPConstants.HEADER.getLocalPart())) {
                if (state.peek().equals(BINDING_INPUT)) {
                    bindingInput.addExtensibilityElement(soapHeader);
                }
                if (state.peek().equals(BINDING_OUTPUT)) {
                    bindingOutput.addExtensibilityElement(soapHeader);
                }
                return;
            }
            // soap:headerfault
            // TODO: header fault
            // soap:fault
            if (localName.equals(SOAPConstants.FAULT.getLocalPart()) && state.peek().equals(BINDING_FAULT)) {
                bindingFault.addExtensibilityElement(soapFault);
                return;
            }
        }

        if (uri.equals(WSDLConstants.WSDL_URI)) {
//                System.err.println("</" + localName + ">" );
            if (localName.equals(WSDLConstants.DEFINITIONS.getLocalPart()) && state.peek().equals(DEFINITION)) {
                state.pop();
                return;
            }

            // Message
            if (localName.equals(WSDLConstants.MESSAGE.getLocalPart()) && state.peek().equals(MESSAGE)) {
                state.pop();
                definition.addMessage(message);
                return;
            }
            // Message Part
            if (localName.equals(WSDLConstants.PART.getLocalPart()) && state.peek().equals(PART)) {
                state.pop();
                message.addPart(part);
                return;
            }

            // PortType
            if (localName.equals(WSDLConstants.PORT_TYPE.getLocalPart()) && state.peek().equals(PORT_TYPE)) {
                state.pop();
                definition.addPortType(portType);
                return;
            }
            // Operation
            if (localName.equals(WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals(OPERATION)) {
                state.pop();
                portType.addOperation(operation);
                return;
            }
            // Input
            if (localName.equals(WSDLConstants.INPUT.getLocalPart()) && state.peek().equals(INPUT)) {
                state.pop();
                operation.setInput(input);
                return;
            }
            // Output
            if (localName.equals(WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals(OUTPUT)) {
                state.pop();
                operation.setOutput(output);
                return;
            }
            // Fault
            if (localName.equals(WSDLConstants.FAULT.getLocalPart()) && state.peek().equals(FAULT)) {
                state.pop();
                operation.addFault(fault);
                return;
            }

            // Binding
            if (localName.equals(WSDLConstants.BINDING.getLocalPart()) && state.peek().equals(BINDING)) {
                state.pop();
                definition.addBinding(binding);
                return;
            }
            // BindingOperation
            if (localName.equals(WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals(BINDING_OPERATION)) {
                state.pop();
                binding.addBindingOperation(bindingOperation);
                return;
            }
            // BindingInput
            if (localName.equals(WSDLConstants.INPUT.getLocalPart()) && state.peek().equals(BINDING_INPUT)) {
                state.pop();
                bindingOperation.setBindingInput(bindingInput);
                return;
            }
            // BindingOutput
            if (localName.equals(WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals(BINDING_OUTPUT)) {
                state.pop();
                bindingOperation.setBindingOutput(bindingOutput);
                return;
            }
            // BindingFault
            if (localName.equals(WSDLConstants.FAULT.getLocalPart()) && state.peek().equals(BINDING_FAULT)) {
                state.pop();
                bindingOperation.addBindingFault(bindingFault);
                return;
            }

            // Service
            if (localName.equals(WSDLConstants.SERVICE.getLocalPart()) && state.peek().equals(SERVICE)) {
                state.pop();
                definition.addService(service);
                return;
            }
            // Port
            if (localName.equals(WSDLConstants.PORT.getLocalPart()) && state.peek().equals(PORT)) {
                state.pop();
                service.addPort(port);
                return;
            }

            if (localName.equals(WSDLConstants.DOCUMENTATION.getLocalPart())) {
                if (state.peek().equals(OPERATION)) {
                    operation.setDocumentation(tagString);
                }
                return;
            }
        }
    }

    public QName parseQName(String qName) {
        if (qName == null) {
            return null;
        }
        int colonPos = qName.indexOf(':'); // NOI18N
        if (colonPos > 0) {
            String prefix = qName.substring(0, colonPos);
            String uri = prefixMapping.get(prefix);
            return new QName(uri, qName.substring(colonPos + 1), prefix);
        }
        return new QName(targetNamespace, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tagString += new String(ch, start, length);
    }
    
}
