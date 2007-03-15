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
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
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
    
    /** This method adds new operation to wsdl file 
     */
    public void addWsOperation(WSDLModel wsdlModel, 
            String operationName, 
            GlobalElement[] parameterTypes, 
            GlobalElement returnType, 
            GlobalElement faultType) {
        
        WSDLComponentFactory factory = wsdlModel.getFactory();
        Definitions definitions = wsdlModel.getDefinitions();
        int counter = 0;
        //create message for each schema element
        String messageNameBase = operationName;
        String messageName = messageNameBase;
        String partNameBase = "parameter";
        String partName = partNameBase;
        wsdlModel.startTransaction();
        //for(int i = 0; i < inputParms.length; i++){
        //assume one parameter for now
        GlobalElement inputParameter = parameterTypes[0];
        Message inputMessage = factory.createMessage();
        inputMessage.setName(messageName);
        Part part = factory.createPart();
        part.setName(partName);
        NamedComponentReference<GlobalElement> ref = part.createSchemaReference(inputParameter, GlobalElement.class);
        part.setElement(ref);
        inputMessage.addPart(part);
        definitions.addMessage(inputMessage);
        messageName = messageNameBase + "_" + ++counter;
        partName = partNameBase + "_" + counter;
        //}
        Message outputMessage = factory.createMessage();
        outputMessage.setName(operationName + "Response");
        Part outpart = factory.createPart();
        outpart.setName("result");
        NamedComponentReference<GlobalElement> outref = part.createSchemaReference(returnType, GlobalElement.class);
        part.setElement(outref);
        outputMessage.addPart(outpart);
        definitions.addMessage(outputMessage);

        RequestResponseOperation operation = factory.createRequestResponseOperation();
        operation.setName(operationName);
        Input input = factory.createInput();
        NamedComponentReference<Message> inputRef = input.createReferenceTo(inputMessage, Message.class);
        input.setName(operationName);
        input.setMessage(inputRef);
        operation.setInput(input);

        Output output = factory.createOutput();
        NamedComponentReference<Message> outputRef = input.createReferenceTo(outputMessage, Message.class);
        output.setName(operationName + "Response");
        output.setMessage(outputRef);
        operation.setOutput(output);
        //this is bogus: need to get the correct porttype
        PortType portType = definitions.getPortTypes().iterator().next();
        portType.addOperation(operation);
        wsdlModel.endTransaction();
    }
    /** call wsimport to generate java artifacts
     * generate WsdlModel to find information about the new operation
     * add new menthod to implementation class
     */
    public void generateJavaArtifacts(org.netbeans.modules.websvc.api.jaxws.project.config.Service service,
            final FileObject implementationClass, final String operationName) {
        Project project = FileOwnerQuery.getOwner(implementationClass);
        invokeWsImport(project,service);
        try {
            WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFile.toURL());
            modeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel wsdlModel) {
                    MethodGenerator generator = new MethodGenerator(wsdlModel,implementationClass);
                    generator.generateMethod(operationName);
                }
            },true);
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void invokeWsImport(Project project, org.netbeans.modules.websvc.api.jaxws.project.config.Service service) {
         if (project!=null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
            try {
                String name = service.getName();
                ExecutorTask wsimportTask =
                    ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-service-clean-"+name,"wsimport-service-"+name},null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } 
        }
    }
}
    
