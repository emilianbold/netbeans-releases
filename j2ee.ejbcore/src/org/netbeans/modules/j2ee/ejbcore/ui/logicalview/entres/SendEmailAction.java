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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.Lookup;

/**
 * Provide action for using an e-mail
 * 
 * @author Petr Blaha
 */
public class SendEmailAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        ElementHandle<TypeElement> beanClass = null;
        try {
            beanClass = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return;
        }
        FileObject srcFile = nodes[0].getLookup().lookup(FileObject.class);
        Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
        
        //make sure configuration is ready
        J2eeModuleProvider pwm = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        
        EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        
        SendEmailPanel sendEmailPanel = new SendEmailPanel(erc.getServiceLocatorName()); //NOI18N
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                sendEmailPanel,
                NbBundle.getMessage(SendEmailAction.class, "LBL_SpecifyMailResource"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SendEmailPanel.class),
                null
                );
        
        sendEmailPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(SendEmailPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dialogDescriptor.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        sendEmailPanel.checkJndiName();
        
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (option == NotifyDescriptor.OK_OPTION) {
            try {
                
                // TODO: for now I am leaving explicit creation of resource element in model here,
                // because model builder doesn't handle @Resource annotation.
                // Later it should be conditional - disabled for Java EE 5 projects
                // and it should be autodiscovered by annotation listening.
                String jndiName = generateJNDILookup(sendEmailPanel.getJndiName(), erc, srcFile, beanClass.getQualifiedName());
                if (jndiName != null) {
                    String serviceLocator = sendEmailPanel.getServiceLocator();
                    ServiceLocatorStrategy serviceLocatorStrategy = null;
                    if (serviceLocator != null) {
                        serviceLocatorStrategy = ServiceLocatorStrategy.create(enterpriseProject, srcFile, serviceLocator);
                    }
                    generateMethods(enterpriseProject, srcFile, beanClass.getQualifiedName(), jndiName, sendEmailPanel.getJndiName(), serviceLocatorStrategy);
                    if (serviceLocator != null) {
                        erc.setServiceLocatorName(serviceLocator);
                    }
                }
            } catch (IOException ioe) {
                NotifyDescriptor ndd = new NotifyDescriptor.Message(ioe.getMessage(),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndd);
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(SendEmailAction.class, "LBL_SendEmailAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private String generateJNDILookup(String jndiName, EnterpriseReferenceContainer erc, FileObject fileObject, String className) throws IOException {
        ResourceReference resourceReference = ResourceReference.create(
                jndiName,
                "javax.mail.Session", // NOI18N
                ResourceRef.RES_AUTH_CONTAINER,
                ResourceRef.RES_SHARING_SCOPE_SHAREABLE,
                null
                );
        return erc.addResourceRef(resourceReference, fileObject, className);
    }
    
    private void generateMethods(Project project, FileObject fileObject, String className, 
            String jndiName, String simpleName, ServiceLocatorStrategy slStrategy) throws IOException{
        String memberName = _RetoucheUtil.uniqueMemberName(fileObject, className, simpleName, "mailResource"); //NOI18N
        if (Utils.isJavaEE5orHigher(project) && InjectionTargetQuery.isInjectionTarget(fileObject, className)) {
            generateInjectedField(fileObject, className, simpleName, memberName);
            generateSendMailMethod(fileObject, className, memberName, null);
        } else {
            String sessionGetter = generateLookupMethod(fileObject, className, jndiName, simpleName, slStrategy);
            generateSendMailMethod(fileObject, className, memberName, sessionGetter);
        }
    }
    
    private void generateSendMailMethod(FileObject fileObject, final String className, String sessionVariableName, String sessionGetter) throws IOException{
        
        List<MethodModel.Variable> parameters = Arrays.asList(new MethodModel.Variable[] {
            MethodModel.Variable.create("java.lang.String", "email"),
            MethodModel.Variable.create("java.lang.String", "subject"),
            MethodModel.Variable.create("java.lang.String", "body")
        });
        
        List<String> exceptions = Arrays.asList(new String[] {
            javax.naming.NamingException.class.getName(),
            "javax.mail.MessagingException"
        });
        
        final MethodModel methodModel = MethodModel.create(
                _RetoucheUtil.uniqueMemberName(fileObject, className, "sendMail", "mailResource"),
                "void",
                getSendCode(sessionVariableName, sessionGetter),
                parameters,
                exceptions,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    private String getSendCode(String sessionVariableName, String sessionGetter){
        return (sessionGetter != null ? "javax.mail.Session " + sessionVariableName + " = " + sessionGetter + "();\n" : "") +
                "javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(" + sessionVariableName + ");\n" +
                "message.setSubject(subject);\n" +
                "message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(email, false));\n" +
                "message.setText(body);\n" +
                "javax.mail.Transport.send(message);\n";
    }
    
    private String generateLookupMethod(FileObject fileObject, final String className, String jndiName, String simpleName, 
            ServiceLocatorStrategy slStrategy) throws IOException {
        String sessionGetter = "get" + simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        sessionGetter = _RetoucheUtil.uniqueMemberName(fileObject, className, sessionGetter, "mailResource");
        String body = null;
        if (slStrategy == null) {
            body = getSessionCode(jndiName);
        } else {
            body = getSessionCode(jndiName, slStrategy, fileObject, className);
        }
        final MethodModel methodModel = MethodModel.create(
                sessionGetter,
                "javax.mail.Session",
                body,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList(javax.naming.NamingException.class.getName()),
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        return sessionGetter;
    }
    
    private String getSessionCode(String jndiName, ServiceLocatorStrategy slStrategy, FileObject fileObject, String className) {
        String mailLookupString = slStrategy.genMailSession(jndiName, fileObject, className);
        return "return (javax.mail.Session) " + mailLookupString + ";\n"; // NOI18N
    }
    
    private String getSessionCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.mail.Session) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] {jndiName});
    }
    
    private void generateInjectedField(FileObject fileObject, String className, String jndiName, String simpleName) throws IOException {
        _RetoucheUtil.generateAnnotatedField(
                fileObject,
                className,
                "javax.annotation.Resource",
                simpleName,
                "javax.mail.Session",
                Collections.singletonMap("name", jndiName),
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
        if (serverInstanceId == null) {
            return true;
        }
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
        if (platform == null) {
            return true;
        }
        if (!platform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
            return false;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] isInterface = new boolean[1];
        try {
            final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            if (elementHandle == null || javaSource == null) {
                return false;
            }
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = elementHandle.resolve(controller);
                    isInterface[0] = ElementKind.INTERFACE == typeElement.getKind();
                }
            }, true);
            return elementHandle == null ? false : !isInterface[0];
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
}
