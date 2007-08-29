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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.ServiceLocatorStrategy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public final class SendJMSGenerator {
    
    private static final String PRODUCES = org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef.MESSAGE_DESTINATION_USAGE_PRODUCES;
    
    private final MessageDestination messageDestination;
    private final Project mdbHolderProject;
    private boolean supportsInjection;
    
    public SendJMSGenerator(MessageDestination messageDestination, Project mdbHolderProject) {
        this.messageDestination = messageDestination;
        this.mdbHolderProject = mdbHolderProject;
    }
    
    public void genMethods(
            EnterpriseReferenceContainer container,
            final String className,
            String connectionFactoryName,
            FileObject fileObject,
            ServiceLocatorStrategy slStrategy,
            J2eeModuleProvider j2eeModuleProvider) throws IOException {
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] isInjectionTarget = new boolean[1];
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                isInjectionTarget[0] = InjectionTargetQuery.isInjectionTarget(controller, typeElement);
            }
        }, true);
        supportsInjection = isInjectionTarget[0];
        String destinationFieldName = null;
        String connectionFactoryFieldName = null;
        String factoryName = connectionFactoryName;
        String destinationName = null;
        
        if (supportsInjection){
            destinationName = messageDestination.getName();
            connectionFactoryFieldName = createInjectedField(fileObject, className, factoryName, "javax.jms.ConnectionFactory"); // NO18N
            String type = messageDestination.getType() == MessageDestination.Type.QUEUE ? "javax.jms.Queue" : "javax.jms.Topic"; // NO18N
            destinationFieldName = createInjectedField(fileObject, className, destinationName, type);
        } else {
            factoryName = generateConnectionFactoryReference(container, factoryName, fileObject, className);
            destinationName = generateDestinationReference(container, fileObject, className);
        }
        String sendMethodName = createSendMethod(fileObject, className, messageDestination.getName());
        createJMSProducer(fileObject, className, factoryName, connectionFactoryFieldName, destinationName,
                destinationFieldName,sendMethodName, slStrategy);
        
        if (messageDestination != null) {
            try {
                if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
                    //in the current implementation, reference name is the same as the destination name...
                    j2eeModuleProvider.getConfigSupport().bindMessageDestinationReference(
                            messageDestination.getName(), factoryName, messageDestination.getName(), messageDestination.getType());
                } else if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
                        //in the current implementation, reference name is the same as the destination name...
                        bindMessageDestinationReferenceForEjb(j2eeModuleProvider, fileObject, className,
                                messageDestination.getName(), factoryName, messageDestination.getName(), messageDestination.getType());
                    }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
    }

    private void bindMessageDestinationReferenceForEjb(J2eeModuleProvider j2eeModuleProvider,
            FileObject fileObject,final String className,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type destType) throws ConfigurationException, IOException {

            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
        
        final String[] ejbName = new String[1];
        final String[] ejbType = new String[1];
        
        metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(className);
                ejbName[0] = ejb.getEjbName();
                if (ejb instanceof Session) {
                    ejbType[0] = EnterpriseBeans.SESSION;
                } else if (ejb instanceof MessageDriven) {
                    ejbType[0] = EnterpriseBeans.MESSAGE_DRIVEN;
                } else if (ejb instanceof Entity) {
                    ejbType[0] = EnterpriseBeans.ENTITY;
                }
                return null;
            }
        });
        
        if (ejbName[0] != null && ejbType[0] != null) {
            j2eeModuleProvider.getConfigSupport().bindMessageDestinationReferenceForEjb(
                    ejbName[0], ejbType[0], referenceName, connectionFactoryName, destName, destType);
        }
    }        

    private String generateConnectionFactoryReference(EnterpriseReferenceContainer container, String referenceName, FileObject referencingFile, String referencingClass) throws IOException {
        ResourceReference ref = ResourceReference.create(
                referenceName,
                "javax.jms.ConnectionFactory",
                ResourceRef.RES_AUTH_CONTAINER,
                ResourceRef.RES_SHARING_SCOPE_SHAREABLE,
                null
                );
        return container.addResourceRef(ref, referencingFile, referencingClass);
    }
    
    private String generateDestinationReference(EnterpriseReferenceContainer container, FileObject referencingFile, String referencingClass) throws IOException {
        // this may need to generalized later if jms producers are expected
        // in web modules
        ProjectInformation projectInformation = ProjectUtils.getInformation(mdbHolderProject);
        String link = projectInformation.getName() + ".jar#" + messageDestination.getName();
        Project referenceingProject = FileOwnerQuery.getOwner(referencingFile);
        if (mdbHolderProject.equals(referenceingProject)) {
            link = link.substring(link.indexOf('#') + 1);
        }
        MessageDestinationReference ref = MessageDestinationReference.create(
                messageDestination.getName(),
                messageDestination.getType() == MessageDestination.Type.QUEUE ? "javax.jms.Queue" : "javax.jms.Topic",
                PRODUCES,
                link
                );
        return container.addDestinationRef(ref, referencingFile, referencingClass);
    }
    
    /**
     * Creates an injected resource field for the given <code>target</code>. The name
     * of the field will be derivated from the given <code>destinationName</code>.
     * @param target the target class
     * @param mappedName the value for resource's mappedName attribute
     * @param fieldType the class of the field.
     * @return name of the created field.
     */
    private String createInjectedField(FileObject fileObject, String className, String destinationName, String fieldType) throws IOException {
        String fieldName = Utils.jndiNameToCamelCase(destinationName, true, "jms");
        _RetoucheUtil.generateAnnotatedField(
                fileObject,
                className,
                "javax.annotation.Resource",
                fieldName,
                fieldType,
                Collections.singletonMap("name", destinationName),
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
        return fieldName;
    }
    
    private String createSendMethod(FileObject fileObject, final String className, String destination) throws IOException {
        final MethodModel.Variable[] parameters = new MethodModel.Variable[] {
            MethodModel.Variable.create("javax.jms.Session", "session"),
            MethodModel.Variable.create(Object.class.getName(), "messageData")
        };
        String methodName = "createJMSMessageFor" + Utils.jndiNameToCamelCase(destination, true, null);
        final MethodModel methodModel = MethodModel.create(
                methodName,
                "javax.jms.Message",
                "// TODO create and populate message to send\n" +
                "// javax.jms.TextMessage tm = session.createTextMessage();\n" +
                "// tm.setText(messageData.toString());\n"+
                "// return tm;\n",
                Arrays.asList(parameters),
                Collections.singletonList("javax.jms.JMSException"),
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        return methodName;
    }
    
    private void createJMSProducer(
            FileObject fileObject,
            final String className,
            String connectionFactoryName,
            String connectionFactoryFieldName,
            String destinationName,
            String destinationFieldName,
            String sendMethodName,
            ServiceLocatorStrategy slStrategy) throws IOException {
        String destName = destinationName.substring(destinationName.lastIndexOf('/') + 1);
        StringBuffer destBuff = new StringBuffer(destName);
        destBuff.setCharAt(0, Character.toUpperCase(destBuff.charAt(0)));
        
        boolean namingException = false;
        String body = null;
        if (supportsInjection){
            body = getSendJMSCodeWithInjectedFields(connectionFactoryFieldName, destinationFieldName, sendMethodName);
        } else if (slStrategy == null) {
            body = getSendJMSCode(connectionFactoryName, destinationName, sendMethodName);
            namingException = true;
        } else {
            body = getSendJMSCode(connectionFactoryName, destinationName, sendMethodName, slStrategy, fileObject, className);
        }
        
        final MethodModel methodModel = MethodModel.create(
                "sendJMSMessageTo" + destBuff,
                "void",
                body,
                Collections.singletonList(MethodModel.Variable.create(Object.class.getName(), "messageData")),
                namingException ? Arrays.asList("javax.naming.NamingException", "javax.jms.JMSException") : Collections.singletonList("javax.jms.JMSException"),
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    /**
     * @return String representing the code for send jms method using injected
     * fields.
     */
    private String getSendJMSCodeWithInjectedFields(String connectionFactoryFieldName,
            String destinationFieldName,
            String messageMethodName){
        
        return MessageFormat.format(
                "javax.jms.Connection connection = null;\n" +
                "javax.jms.Session session = null;\n" +
                "try '{' \n" +
                "connection = {0}.createConnection();\n" +
                "session = connection.createSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);\n" +
                "javax.jms.MessageProducer messageProducer = session.createProducer({1});\n" +
                "messageProducer.send({2}(session, messageData));\n" +
                " '}' finally '{'\n" +
                "if (session != null) '{'\n"+
                " session.close();\n" +
                "'}'\n" +
                "if (connection != null) '{'\n" +
                "connection.close();\n" +
                "'}'\n" +
                "'}'\n",
                connectionFactoryFieldName, destinationFieldName, messageMethodName);
    }
    
    private String getSendJMSCode(String connectionName, String destinationName,
            String messageMethodName, ServiceLocatorStrategy sls,
            FileObject fileObject, String className) {
        String connectionFactory = sls.genJMSFactory(connectionName, fileObject, className);
        String destination = sls.genDestinationLookup(destinationName, fileObject, className);
        return MessageFormat.format(
                "javax.jms.ConnectionFactory cf = (javax.jms.ConnectionFactory) " + connectionFactory + ";\n" +
                "javax.jms.Connection conn = null;\n" +
                "javax.jms.Session s = null;\n" +
                "try '{' \n" +
                "conn = cf.createConnection();\n" +
                "s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);\n" +
                "javax.jms.Destination destination = (javax.jms.Destination) " + destination + ";\n" +
                "javax.jms.MessageProducer mp = s.createProducer(destination);\n" +
                "mp.send({2}(s,messageData));\n" +
                " '}' finally '{'\n" +
                "if (s != null) '{'\n"+
                " s.close();\n" +
                "'}'\n" +
                "if (conn != null) '{'\n" +
                "conn.close();\n" +
                "'}'\n" +
                "'}'\n",
                new Object[] {connectionName, destinationName, messageMethodName});
    }
    
    private String getSendJMSCode(String connectionName, String destinationName,
            String messageMethodName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" +
                "javax.jms.ConnectionFactory cf = (javax.jms.ConnectionFactory) c.lookup(\"java:comp/env/{0}\");\n" +
                "javax.jms.Connection conn = null;\n" +
                "javax.jms.Session s = null;\n" +
                "try '{' \n" +
                "conn = cf.createConnection();\n" +
                "s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);\n" +
                "javax.jms.Destination destination = (javax.jms.Destination) c.lookup(\"java:comp/env/{1}\");\n" +
                "javax.jms.MessageProducer mp = s.createProducer(destination);\n" +
                "mp.send({2}(s,messageData));\n" +
                " '}' finally '{'\n" +
                "if (s != null) '{'\n"+
                " s.close();\n" +
                "'}'\n" +
                "if (conn != null) '{'\n" +
                "conn.close();\n" +
                "'}'\n" +
                "'}'\n",
                new Object[] {connectionName, destinationName, messageMethodName});
    }
    
}
