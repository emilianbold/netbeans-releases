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

package org.netbeans.modules.websvc.design.schema2java;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.design.util.SourceUtils;
import org.netbeans.modules.websvc.design.view.actions.ParamModel;
import org.netbeans.modules.websvc.design.view.actions.Utils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;


/**
 *
 * @author mkuchtiak, rcruz
 */
public class OperationGeneratorHelper {
    File wsdlFile;
    
    /** Creates a new instance of MethodGeneratorHelper */
    public OperationGeneratorHelper(File wsdlFile) {
        this.wsdlFile=wsdlFile;
    }
    
    /** This method adds new operation to the wsdl file
     */
    public Operation addWsOperation(WSDLModel wsdlModel,
            String portTypeName,
            String operationName,
            List<ParamModel> parameterTypes,
            ReferenceableSchemaComponent returnType,
            List<ParamModel> faultTypes) {
        
        WSDLComponentFactory factory = wsdlModel.getFactory();
        Definitions definitions = wsdlModel.getDefinitions();
        Types types = definitions.getTypes();
        
        String messageName = operationName+"Message"; //NOI18N
        String partName = operationName+"Part"; //NOI18N
        String paramTypeName = operationName+"Type"; //NOI18N
        String responseTypeName = operationName+"ResponseType"; //NOI18N
        String responseElementName = operationName+"Response"; //NOI18N
        String responseMessageName = operationName+"ResponseMessage"; //NOI18N
        String responsePartName = operationName+"ResponsePart"; //NOI18N
        
        Operation operation = null;
        try {
            wsdlModel.startTransaction();
            
            // we need to distinct between void operation and one way
            //if(returnType != null) {
                operation = factory.createRequestResponseOperation();
            //} else {
            //    operation = factory.createOneWayOperation();
            //}
            operation.setName(operationName);
            
            Message inputMessage=null;
            GlobalElement paramElement=null;
            GlobalElement returnElement = null;
            List<GlobalElement> faultElements = new ArrayList<GlobalElement>();
            List<Message> faultMessages = new ArrayList<Message>();
            
            SchemaModel schemaModel = null;
            Schema schema = null;
            
            Collection<Schema> schemas = types.getSchemas();
            Iterator<Schema> it = schemas.iterator();
            if (it.hasNext()) {
                schema = it.next();
                schemaModel = schema.getModel();
            }
                
            if (parameterTypes.size()==0 || parameterTypes.size() > 1 || (parameterTypes.size() == 1 && isPrimitiveType(parameterTypes.get(0)))) {
                if(schemaModel == null) {
                    schemaModel = createSchemaModel(factory, definitions, types);
                    schema = schemaModel.getSchema();
                }

                //wrap the parameters in a global element
                GlobalComplexType complexType = schemaModel.getFactory().createGlobalComplexType();
                complexType.setName(paramTypeName);
                Sequence seq = schemaModel.getFactory().createSequence();
                complexType.setDefinition(seq);
                schema.addComplexType(complexType);

                for(ParamModel param : parameterTypes) {
                    addElementToSequence(seq, schemaModel, param);
                }

                paramElement = schemaModel.getFactory().createGlobalElement();
                paramElement.setName(getUniqueGlobalElementName(schema, operationName)); //NOI18N
                NamedComponentReference<GlobalType> complexTypeRef = schema.createReferenceTo((GlobalType)complexType, GlobalType.class);
                paramElement.setType(complexTypeRef);
                schema.addElement(paramElement);
            } else{ //there is only one parameter and it is not primitive
                ParamModel paramModel = parameterTypes.get(0);
                ReferenceableSchemaComponent ref = paramModel.getParamType();
                if (ref instanceof GlobalElement){
                    paramElement = (GlobalElement)ref;
                } else if (ref instanceof GlobalType) {
                    if(schemaModel == null){
                        schemaModel = createSchemaModel(factory, definitions, types);
                        schema = schemaModel.getSchema();
                    }
                    paramElement = schemaModel.getFactory().createGlobalElement();
                    paramElement.setName(getUniqueGlobalElementName(schema, operationName)); //NOI18N
                    NamedComponentReference<GlobalType> typeRef = schema.createReferenceTo((GlobalType)ref, GlobalType.class);
                    paramElement.setType(typeRef);
                    schema.addElement(paramElement);
                }
            }

            if (paramElement!=null) {
                if(inputMessage == null){
                    inputMessage = factory.createMessage();
                    inputMessage.setName(messageName);
                    definitions.addMessage(inputMessage);
                }
                Part part = factory.createPart();
                part.setName(partName);
                NamedComponentReference<GlobalElement> ref = part.createSchemaReference(paramElement, GlobalElement.class);
                part.setElement(ref);
                inputMessage.addPart(part);
            }

            if (inputMessage!=null) {
                Input input = factory.createInput();
                NamedComponentReference<Message> inputRef = input.createReferenceTo(inputMessage, Message.class);
                input.setName(operationName);
                input.setMessage(inputRef);
                operation.setInput(input);
            }
            
            // create schema elements for faults
            for(ParamModel faultModel : faultTypes) {
                ReferenceableSchemaComponent faultType = faultModel.getParamType();
                if (faultType instanceof GlobalType) {
                    if(schemaModel == null){
                        schemaModel = createSchemaModel(factory, definitions, types);
                        schema = schemaModel.getSchema();
                    }
                    GlobalElement faultElement = schemaModel.getFactory().createGlobalElement();
                    NamedComponentReference<GlobalType> typeRef = schemaModel.getSchema().createReferenceTo((GlobalType)faultType, GlobalType.class);
                    faultElement.setName(getUniqueGlobalElementName(schema,faultModel.getParamName()));
                    faultElement.setType(typeRef);
                    schema.addElement(faultElement);
                    faultElements.add(faultElement);
                } else if (faultType instanceof GlobalElement) {
                    faultElements.add((GlobalElement)faultType);
                }
            }
            
            Message outputMessage=null;
            if(returnType != null) {  //if the operation returns something
                if (isPrimitiveType(returnType)){
                    if(schemaModel == null){
                        schemaModel = createSchemaModel(factory, definitions, types);
                        schema = schemaModel.getSchema();
                    }
                    GlobalComplexType responseComplexType = schemaModel.getFactory().createGlobalComplexType();
                    responseComplexType.setName(responseTypeName); //NOI18N
                    Sequence seq1 = schemaModel.getFactory().createSequence();
                    responseComplexType.setDefinition(seq1);
                    schema.addComplexType(responseComplexType);
                    LocalElement el = schemaModel.getFactory().createLocalElement();
                    NamedComponentReference<GlobalType> typeRef = schema.createReferenceTo((GlobalType)returnType, GlobalType.class);
                    el.setName("result"); //NOI18N
                    el.setType(typeRef);
                    seq1.appendContent(el);
                    
                    returnElement = schemaModel.getFactory().createGlobalElement();
                    returnElement.setName(this.getUniqueGlobalElementName(schema, responseElementName));
                    NamedComponentReference<GlobalType> responseTypeRef = schema.createReferenceTo((GlobalType)responseComplexType, GlobalType.class);
                    returnElement.setType(responseTypeRef);
                    schema.addElement(returnElement);
                } else {
                    if(returnType instanceof GlobalElement){
                        returnElement = (GlobalElement)returnType;
                    }
                }
            } else {
                // return type == null
                if(schemaModel == null){
                        schemaModel = createSchemaModel(factory, definitions, types);
                        schema = schemaModel.getSchema();
                }
                GlobalComplexType responseComplexType = schemaModel.getFactory().createGlobalComplexType();
                responseComplexType.setName(responseTypeName); //NOI18N
                Sequence seq1 = schemaModel.getFactory().createSequence();
                responseComplexType.setDefinition(seq1);
                schema.addComplexType(responseComplexType);

                returnElement = schemaModel.getFactory().createGlobalElement();
                returnElement.setName(this.getUniqueGlobalElementName(schema, responseElementName));
                NamedComponentReference<GlobalType> responseTypeRef = schema.createReferenceTo((GlobalType)responseComplexType, GlobalType.class);
                returnElement.setType(responseTypeRef);
                schema.addElement(returnElement);
            }
                
            if (returnElement!=null) {
                outputMessage = factory.createMessage();
                outputMessage.setName(responseMessageName);
                Part outpart = factory.createPart();
                outpart.setName(responsePartName);
                NamedComponentReference<GlobalElement> outref = outpart.createSchemaReference(returnElement, GlobalElement.class);
                outpart.setElement(outref);
                outputMessage.addPart(outpart);
                definitions.addMessage(outputMessage);
            }

            if (outputMessage!=null) {
                Output output = factory.createOutput();
                NamedComponentReference<Message> outputRef = output.createReferenceTo(outputMessage, Message.class);
                output.setName(responseElementName);
                output.setMessage(outputRef);
                operation.setOutput(output);
            }
            
            // faults
            for (GlobalElement faultElement:faultElements) {
                Message faultMessage = factory.createMessage();
                faultMessage.setName(faultElement.getName());
                definitions.addMessage(faultMessage);
                
                Part part = factory.createPart();
                part.setName("fault"); //NOI18N
                NamedComponentReference<GlobalElement> ref = part.createSchemaReference(faultElement, GlobalElement.class);
                part.setElement(ref);
                
                faultMessage.addPart(part);
                faultMessages.add(faultMessage);
            }
            
            for (Message faultMessage:faultMessages) {
                Fault fault = factory.createFault();
                NamedComponentReference<Message> ref = fault.createReferenceTo(faultMessage, Message.class);
                fault.setName(faultMessage.getName());
                fault.setMessage(ref);
                operation.addFault(fault);
            }
            
            Collection<PortType> portTypes = definitions.getPortTypes();
            PortType portType = null;
            for(PortType p : portTypes){
                if(p.getName().equals(portTypeName)){
                    portType = p;
                    break;
                }
            }
            if(portType != null){
                portType.addOperation(operation);
            } else{
                //TODO: what will we do if portType is not found?
                return null;
            }
            
            //Add binding section for operation, if there is a binding section
            //Assume SOAP binding only
            Collection<Binding> bindings = definitions.getBindings();
            Binding binding = null;
            if(portType != null && bindings.size() > 0){
                //find binding for portType
                binding = findBindingForPortType(bindings, portType);
                if(binding != null){
                    //determine if it is soap binding
                    List<SOAPBinding> soapBindings = binding.getExtensibilityElements(SOAPBinding.class);
                    if(soapBindings.size() > 0){  //it is SOAP binding
                        //get the SOAP Binding
                        SOAPBinding soapBinding = soapBindings.iterator().next();
                        //is style specified at the soap binding level?
                        Style style = soapBinding.getStyle();
                        BindingOperation bOp = factory.createBindingOperation();
                        bOp.setName(operation.getName());
                        
                        SOAPOperation soapOperation = factory.createSOAPOperation();
                        soapOperation.setSoapAction("");  //TODO: have user set this in UI?
                        //if style is not specified at the SOAP binding level, specify it
                        //at the SOAP operation level.
                        //TODO: For now, assume it is document style. We need to determine
                        //style based on the message.
                        if(style == null){
                            soapOperation.setStyle(Style.DOCUMENT);
                        }
                        bOp.addExtensibilityElement(soapOperation);
                        //create input binding to SOAP
                        if(inputMessage != null){
                            //For now, assume all parms are to be put in the body
                            //TODO: based on the WebParm annotation, we need to determine
                            //if a certain part is for a header
                            BindingInput bindingInput = factory.createBindingInput();
                            SOAPBody soapBody = factory.createSOAPBody();
                            //TODO: for multiple messages, need to specify parts
                            //Always has to be literal
                            soapBody.setUse(SOAPMessageBase.Use.LITERAL);
                            bindingInput.addExtensibilityElement(soapBody);
                            bOp.setBindingInput(bindingInput);
                        }
                        //create output binding to SOAP
                        if(outputMessage != null){
                            //TODO: same comments as in InputMessage
                            BindingOutput bindingOutput = factory.createBindingOutput();
                            SOAPBody soapBody = factory.createSOAPBody();
                            soapBody.setUse(SOAPMessageBase.Use.LITERAL);
                            bindingOutput.addExtensibilityElement(soapBody);
                            bOp.setBindingOutput(bindingOutput);
                        }
                        //create fault binding to SOAP
                        for (Message faultMessage:faultMessages) {
                            BindingFault bindingFault = factory.createBindingFault();
                            bindingFault.setName(faultMessage.getName());
                            SOAPFault soapFault = factory.createSOAPFault();
                            soapFault.setName(faultMessage.getName());
                            soapFault.setUse(SOAPMessageBase.Use.LITERAL);
                            bindingFault.addExtensibilityElement(soapFault);
                            bOp.addBindingFault(bindingFault);
                        }
                        binding.addBindingOperation(bOp);
                        //TODO: Need to handle faults!!!
                    }else{
                        return null; //Not SOAP binding, we cannot do anything
                    }
                    
                }
            }
            return operation;
        } finally {
            wsdlModel.endTransaction();
        }
    }
    
    private boolean isPrimitiveType(ParamModel parameterType){
        return (Utils.getPrimitiveType(parameterType.getParamType().getName()) != null);
    }
    
    private boolean isPrimitiveType(ReferenceableSchemaComponent comp){
        return (Utils.getPrimitiveType(comp.getName()) !=  null);
    }
    
    private void addElementToSequence(Sequence sequence, SchemaModel schemaModel, ParamModel param) {
        
        ReferenceableSchemaComponent paramType = param.getParamType();
        if (paramType instanceof GlobalType) {
            LocalElement el = schemaModel.getFactory().createLocalElement();
            NamedComponentReference<GlobalType> typeRef = schemaModel.getSchema().createReferenceTo((GlobalType)paramType, GlobalType.class);
            el.setName(param.getParamName());
            el.setType(typeRef);
            sequence.appendContent(el);
        } else if (paramType instanceof GlobalElement) {
            ElementReference el = schemaModel.getFactory().createElementReference();
            NamedComponentReference<GlobalElement> typeRef = schemaModel.getSchema().createReferenceTo((GlobalElement)paramType, GlobalElement.class);
            el.setRef(typeRef);
            sequence.appendContent(el);
        }
    }
    
    private Binding findBindingForPortType(Collection<Binding> bindings, PortType portType){
        for(Binding b : bindings){
            NamedComponentReference<PortType> portTypeRef = b.getType();
            if(portTypeRef.references(portType)){
                return b;
            }
        }
        return null;
    }
    
    public void removeWSOperation(WSDLModel wsdlModel,
            String portTypeName,
            String operationName){
        
        PortType portType = null;
        Operation operation = null;
        try{
            wsdlModel.startTransaction();
            Definitions definitions = wsdlModel.getDefinitions();
            Collection<PortType> portTypes = definitions.getPortTypes();
            for(PortType pt : portTypes){
                if(pt.getName().equals(portTypeName)){
                    portType = pt;
                    break;
                }
            }
            if(portType != null){
                Collection<Operation> operations = portType.getOperations();
                for(Operation op : operations){
                    String opName = convertOperationName(op.getName());
                    if(opName.equals(operationName)){
                        operation = op;
                        break;
                    }
                }
                if(operation != null){
                    portType.removeOperation(operation);
                    
                    Collection<Binding> bindings = definitions.getBindings();
                    Binding binding = null;
                    if(bindings.size() > 0){
                        //find binding for portType
                        binding = findBindingForPortType(bindings, portType);
                        if(binding != null){
                            Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
                            BindingOperation bindingOperation = null;
                            for(BindingOperation bindingOp : bindingOperations){
                                //TODO: Is this enough??
                                //TODO: should we resolve the binding operation reference??
                                if(operationName.equals(bindingOp.getName())){
                                    bindingOperation = bindingOp;
                                    break;
                                }
                            }
                            if(bindingOperation != null){
                                binding.removeBindingOperation(bindingOperation);
                            }
                        }
                    }
                }
            }
        }finally{
            wsdlModel.endTransaction();
        }
        
    }
    
    //converts the wsdlOperation name to Java name according to JAXWS rules
    private String convertOperationName(final String wsdlOperation){
        String name = wsdlOperation;
        String firstChar = name.substring(0,1);
        firstChar = firstChar.toLowerCase();
        name= firstChar.concat(name.substring(1));
        return name;
    }
    
    /** call wsimport to generate java artifacts
     * generate WsdlModel to find information about the new operation
     * add new menthod to implementation class
     */
    public void generateJavaArtifacts(String serviceName,
            final FileObject implementationClass, final String operationName, final boolean remove) {
        Project project = FileOwnerQuery.getOwner(implementationClass);
        invokeWsImport(project,serviceName);
        try {
            WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFile.toURI().toURL());
            modeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel wsdlModel) {
                    MethodGenerator generator = new MethodGenerator(wsdlModel,implementationClass);
                    if(!remove){
                        generator.generateMethod(operationName);
                    }else{
                        generator.removeMethod(operationName);
                    }
                }
            },true);
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public String getPortTypeName(FileObject implBean){
        JavaSource javaSource = JavaSource.forFileObject(implBean);
        final String[] endpointInterface = new String[1];
        if (javaSource!=null) {
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    SourceUtils srcUtils = SourceUtils.newInstance(controller);
                    TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                    if(srcUtils != null && wsElement != null){
                        List<? extends AnnotationMirror> annotations = srcUtils.getTypeElement().getAnnotationMirrors();
                        for (AnnotationMirror anMirror : annotations) {
                            Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                            for(ExecutableElement ex:expressions.keySet()) {
                                if (ex.getSimpleName().contentEquals("endpointInterface")) {
                                    String interfaceName =  (String)expressions.get(ex).getValue();
                                    if(interfaceName != null){
                                        endpointInterface[0] = URLEncoder.encode(interfaceName,"UTF-8"); //NOI18N
                                        break;
                                    }
                                }
                            }
                            
                        } // end if
                    } // end for
                }
                public void cancel() {}
            };
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            String seiName = endpointInterface[0];
            if(seiName != null){
                int index = seiName.lastIndexOf(".");
                if(index != -1){
                    seiName = seiName.substring(index + 1);
                }
                System.out.println("seiName: " + seiName);
                return seiName;
            }
        }
        return null;
    }
    
    
    //TODO: This is a temporary utility. This will go away when the copying of the
    //modified wsdl from the src/conf directory to the WEB-INF/wsdl directory is
    //done in the build script.
    public static FileObject getWsdlFolderForService(FileObject fo, String name) throws IOException {
        JAXWSSupport jaxwssupport = JAXWSSupport.getJAXWSSupport(fo);
        FileObject globalWsdlFolder = jaxwssupport.getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject(name);
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        return globalWsdlFolder.createFolder(name);
    }
    
    //TODO: This is a temporary utility. This will go away when the copying of the
    //modified wsdl from the src/conf directory to the WEB-INF/wsdl directory is
    //done in the build script.
    public static FileObject getLocalWsdlFolderForService(FileObject fo, String serviceName){
        JAXWSSupport jaxwssupport = JAXWSSupport.getJAXWSSupport(fo);
        return jaxwssupport.getLocalWsdlFolderForService(serviceName, false);
    }
    
    private void invokeWsImport(Project project, final String serviceName) {
        if (project!=null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
            try {
                ExecutorTask wsimportTask =
                        ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-service-clean-"+serviceName,"wsimport-service-"+serviceName},null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
    }
    
    private String getUniqueGlobalElementName(Schema schema, String baseName){
        String bName = baseName;
        int suffix = 0;
        Collection<GlobalElement> elements = schema.getElements();
        for(GlobalElement element : elements){
            if(element.getName().equals(bName)){
                bName = baseName + "_" + ++suffix;
            }
        }
        return bName;
    }
    
    private SchemaModel createSchemaModel(WSDLComponentFactory factory, Definitions definitions, Types types) {
        WSDLSchema wsdlSchema = factory.createWSDLSchema();
        types.addExtensibilityElement(wsdlSchema);
        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
        schemaModel.getSchema().setTargetNamespace(definitions.getTargetNamespace());
        return schemaModel;
    }
}

