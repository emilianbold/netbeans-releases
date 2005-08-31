/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;


/**
 * Provide action for using an e-mail
 * @author Petr Blaha
 */
public class SendEmailAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        JavaClass beanClass = JMIUtils.getJavaClassFromNode(nodes[0]);
        FileObject srcFile = JavaModel.getFileObject(beanClass.getResource());
        Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
        
        //make sure configuration is ready
        J2eeModuleProvider pwm = (J2eeModuleProvider) enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        
        EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
        enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        
        SendEmailPanel p = new SendEmailPanel(erc.getServiceLocatorName()); //NOI18N
        DialogDescriptor nd = new DialogDescriptor(
                p,
                NbBundle.getMessage(SendEmailAction.class, "LBL_SpecifyMailResource"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SendEmailPanel.class),
                null
                );
        Object option = DialogDisplayer.getDefault().notify(nd);
        if (option == NotifyDescriptor.OK_OPTION) {
            try {
                
                String jndiName = generateJNDILookup(
                        p.getJndiName(), erc,
                        beanClass.getName());
                String serviceLocator = p.getServiceLocator();
                ServiceLocatorStrategy serviceLocatorStrategy = null;
                if (serviceLocator != null) {
                    serviceLocatorStrategy =
                            ServiceLocatorStrategy.create(enterpriseProject, srcFile,
                            serviceLocator);
                }
                generateMethods(beanClass, jndiName, serviceLocatorStrategy);
                if (serviceLocator != null) {
                    erc.setServiceLocatorName(serviceLocator);
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
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private String generateJNDILookup(String jndiName,
            EnterpriseReferenceContainer erc,
            String className) throws IOException {
        if(jndiName.startsWith("mail/")){ //NOI18N
            jndiName = jndiName.substring(5);
        }
        ResourceRef ref = erc.createResourceRef(className);
        ref.setResRefName("mail/" + jndiName); // NOI18N
        ref.setResAuth(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_AUTH_CONTAINER);
        ref.setResSharingScope(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_SHARING_SCOPE_SHAREABLE);
        ref.setResType("javax.mail.Session"); //NOI18N
        return erc.addResourceRef(ref, className);
    }
    
    private void generateMethods(JavaClass ce, String jndiName, ServiceLocatorStrategy sl){
        generateSendMailMethod(ce);
        generateLookupMethod(ce,jndiName,sl);
    }
    
    private void generateSendMailMethod(JavaClass ce){
        Method me = JMIUtils.createMethod(ce);
        me.setModifiers(Modifier.PRIVATE);
        me.setName("sendMail");
        me.setType(JMIUtils.resolveType("void"));
        // add parameters
        List parameters = me.getParameters();
        parameters.add(JMIUtils.createParameter(me,"email",JMIUtils.resolveType("String"),false));
        parameters.add(JMIUtils.createParameter(me,"subject",JMIUtils.resolveType("String"),false));
        parameters.add(JMIUtils.createParameter(me,"body",JMIUtils.resolveType("String"),false));
        JMIUtils.addException(me, javax.naming.NamingException.class.getName());
        JMIUtils.addException(me, "javax.mail.MessagingException");
        me.setBodyText(getSendCode());
        ce.getContents().add(me);
    }
    
    private String getSendCode(){
        return "javax.mail.Session session = getSession();\n" +
        "javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);\n" +
        "message.setSubject(subject);\n" +
        "message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(email, false));\n" +
        "message.setText(body);\n" +                                                                                     
        "javax.mail.Transport.send(message);\n";
    }
    
    private void generateLookupMethod(JavaClass ce, String jndiName, ServiceLocatorStrategy sl) {
        Method me = JMIUtils.createMethod(ce);
        me.setModifiers(Modifier.PRIVATE);
        me.setName("getSession");
        me.setType(JMIUtils.resolveType("javax.mail.Session"));
        JMIUtils.addException(me, javax.naming.NamingException.class.getName());
        if (sl == null) {
            me.setBodyText(getSessionCode(jndiName));
        } else {
            me.setBodyText(getSessionCode(jndiName, sl, ce));
        }
        ce.getContents().add(me);
    }
    
    private String getSessionCode(String jndiName, ServiceLocatorStrategy sl, JavaClass target) {
        String mailLookupString = sl.genMailSession(jndiName, target);
        return "return (javax.mail.Session) " + mailLookupString + ";\n"; // NOI18N
    }
    
    private String getSessionCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.mail.Session) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] {jndiName});
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
	JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
        return jc == null ? false : !jc.isInterface();
    }
    
    protected void initialize() {
        super.initialize();
    }
}
