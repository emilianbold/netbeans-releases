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

import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.Lookup;


/**
 * Provide action for using an e-mail
 * @author Petr Blaha
 */
public class SendEmailAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
//        JavaClass beanClass = JMIUtils.getJavaClassFromNode(nodes[0]);
//        FileObject srcFile = JavaModel.getFileObject(beanClass.getResource());
//        Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
//        
//        //make sure configuration is ready
//        J2eeModuleProvider pwm = (J2eeModuleProvider) enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
//        pwm.getConfigSupport().ensureConfigurationReady();
//        
//        EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
//        enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
//        
//        SendEmailPanel p = new SendEmailPanel(erc.getServiceLocatorName()); //NOI18N
//        final DialogDescriptor nd = new DialogDescriptor(
//                p,
//                NbBundle.getMessage(SendEmailAction.class, "LBL_SpecifyMailResource"),
//                true,
//                DialogDescriptor.OK_CANCEL_OPTION,
//                DialogDescriptor.OK_OPTION,
//                DialogDescriptor.DEFAULT_ALIGN,
//                new HelpCtx(SendEmailPanel.class),
//                null
//                );
//        
//        p.addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (evt.getPropertyName().equals(SendEmailPanel.IS_VALID)) {
//                    Object newvalue = evt.getNewValue();
//                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
//                        nd.setValid(((Boolean)newvalue).booleanValue());
//                    }
//                }
//            }
//        });
//        p.checkJndiName();
//        
//        Object option = DialogDisplayer.getDefault().notify(nd);
//        if (option == NotifyDescriptor.OK_OPTION) {
//            try {
//                
//                // TODO: for now I am leaving explicit creation of resource element in model here,
//                // because model builder doesn't handle @Resource annotation.
//                // Later it should be conditional - disabled for Java EE 5 projects
//                // and it should be autodiscovered by annotation listening.
//                String jndiName = generateJNDILookup(
//                        p.getJndiName(), erc,
//                        beanClass.getName());
//                String serviceLocator = p.getServiceLocator();
//                ServiceLocatorStrategy serviceLocatorStrategy = null;
//                if (serviceLocator != null) {
//                    serviceLocatorStrategy =
//                            ServiceLocatorStrategy.create(enterpriseProject, srcFile,
//                            serviceLocator);
//                }
//                generateMethods(enterpriseProject, beanClass, jndiName, p.getJndiName(), serviceLocatorStrategy);
//                fixImports(beanClass);
//                if (serviceLocator != null) {
//                    erc.setServiceLocatorName(serviceLocator);
//                }
//                
//                
//            } catch (IOException ioe) {
//                NotifyDescriptor ndd = new NotifyDescriptor.Message(ioe.getMessage(),
//                        NotifyDescriptor.ERROR_MESSAGE);
//                DialogDisplayer.getDefault().notify(ndd);
//            }
//        }
    }
    
    public String getName() {
        return NbBundle.getMessage(SendEmailAction.class, "LBL_SendEmailAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
//    private String generateJNDILookup(String jndiName,
//            EnterpriseReferenceContainer erc,
//            String className) throws IOException {
//        ResourceRef ref = erc.createResourceRef(className);
//        ref.setResRefName(jndiName); // NOI18N
//        ref.setResAuth(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_AUTH_CONTAINER);
//        ref.setResSharingScope(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_SHARING_SCOPE_SHAREABLE);
//        ref.setResType("javax.mail.Session"); //NOI18N
//        return erc.addResourceRef(ref, className);
//    }
//    
//    private void generateMethods(Project project, JavaClass ce, String jndiName, String simpleName, ServiceLocatorStrategy sl){
//        boolean rollback = true;
//        try{
//            JMIUtils.beginJmiTransaction(true);
//            String memberName = JMIUtils.uniqueMemberName(ce, simpleName, "mailResource"); //NOI18N
//            if (Utils.isJavaEE5orHigher(project) &&
//                    InjectionTargetQuery.isInjectionTarget(ce)) {
//                generateInjectedField(ce, simpleName, memberName);
//                generateSendMailMethod(ce, memberName, null);
//            } else {
//                String sessionGetter = generateLookupMethod(ce, jndiName, simpleName, sl);
//                generateSendMailMethod(ce, memberName, sessionGetter);
//            }
//            rollback = false;
//        } finally {
//            JMIUtils.endJmiTransaction(rollback);
//        }
//    }
//    
//    /**
//     * Attempts to fix imports of the given <code>javaClass</code>. Executed
//     * in own transaction since fix imports would fail if called within a transaction
//     *  that has already changed the body of the given class.
//     */
//    private void fixImports(JavaClass javaClass){
//        boolean rollback = true;
//        try{
//            JMIUtils.beginJmiTransaction(true);
//            JMIUtils.fixImports(javaClass);
//            rollback = false;
//        } finally {
//            JMIUtils.endJmiTransaction(rollback);
//        }
//    }
//    
//    private void generateSendMailMethod(JavaClass ce, String sessionVariableName, String sessionGetter){
//        Method me = JMIUtils.createMethod(ce);
//        me.setModifiers(Modifier.PRIVATE);
//        String methodName = JMIUtils.uniqueMemberName(ce, "sendMail", "mailResource"); //NOI18N
//        me.setName(methodName);
//        me.setType(JMIUtils.resolveType("void"));
//        // add parameters
//        List parameters = me.getParameters();
//        parameters.add(JMIUtils.createParameter(me,"email",JMIUtils.resolveType("String"),false));
//        parameters.add(JMIUtils.createParameter(me,"subject",JMIUtils.resolveType("String"),false));
//        parameters.add(JMIUtils.createParameter(me,"body",JMIUtils.resolveType("String"),false));
//        JMIUtils.addException(me, javax.naming.NamingException.class.getName());
//        JMIUtils.addException(me, "javax.mail.MessagingException");
//        me.setBodyText(getSendCode(sessionVariableName, sessionGetter));
//        ce.getContents().add(me);
//    }
//    
//    private String getSendCode(String sessionVariableName, String sessionGetter){
//        return (sessionGetter != null ? "javax.mail.Session " + sessionVariableName + " = " + sessionGetter + "();\n" : "") +
//                "javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(" + sessionVariableName + ");\n" +
//                "message.setSubject(subject);\n" +
//                "message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(email, false));\n" +
//                "message.setText(body);\n" +
//                "javax.mail.Transport.send(message);\n";
//    }
//    
//    private String generateLookupMethod(JavaClass ce, String jndiName, String simpleName, ServiceLocatorStrategy sl) {
//        Method me = JMIUtils.createMethod(ce);
//        me.setModifiers(Modifier.PRIVATE);
//        String sessionGetter = "get" + simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
//        sessionGetter = JMIUtils.uniqueMemberName(ce, sessionGetter, "mailResource");
//        me.setName(sessionGetter);
//        me.setType(JMIUtils.resolveType("javax.mail.Session"));
//        JMIUtils.addException(me, javax.naming.NamingException.class.getName());
//        if (sl == null) {
//            me.setBodyText(getSessionCode(jndiName));
//        } else {
//            me.setBodyText(getSessionCode(jndiName, sl, ce));
//        }
//        ce.getContents().add(me);
//        return sessionGetter;
//    }
//    
//    private String getSessionCode(String jndiName, ServiceLocatorStrategy sl, JavaClass target) {
//        String mailLookupString = sl.genMailSession(jndiName, target);
//        return "return (javax.mail.Session) " + mailLookupString + ";\n"; // NOI18N
//    }
//    
//    private String getSessionCode(String jndiName) {
//        return MessageFormat.format(
//                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
//                "return (javax.mail.Session) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
//                new Object[] {jndiName});
//    }
//    
//    private void generateInjectedField(JavaClass jc, String jndiName, String simpleName) {
//        int modifier = InjectionTargetQuery.isStaticReferenceRequired(jc) ? (Modifier.STATIC | Modifier.PRIVATE) : Modifier.PRIVATE;
//        Field f = JMIGenerationUtil.createField(jc, simpleName, modifier, "javax.mail.Session");
//        AttributeValue av = JMIGenerationUtil.createAttributeValue(jc, "name", jndiName);
//        Annotation a = JMIGenerationUtil.createAnnotation(jc, "javax.annotation.Resource", Collections.singletonList(av));
//        f.getAnnotations().add(a);
//        jc.getFeatures().add(0, f);
//    }
    
    protected boolean enable(Node[] nodes) {
        //TODO: RETOUCHE
        return false;
//        if (nodes == null || nodes.length != 1) {
//            return false;
//        }
//        JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
//        if (jc == null) {
//            return false;
//        }
//        FileObject srcFile = JavaModel.getFileObject(jc.getResource());
//        Project project = FileOwnerQuery.getOwner(srcFile);
//        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
//        String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
//        if (serverInstanceId == null) {
//            return true;
//        }
//        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
//        if (platform == null) {
//            return true;
//        }
//        if (!platform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
//            return false;
//        }
//        return !jc.isInterface();
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
}
