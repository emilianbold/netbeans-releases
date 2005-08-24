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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class MessageGenerator {
    private EjbGenerationUtil genUtil = new EjbGenerationUtil();
    private static final String MESSAGE_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"MessageBean.xml"; //NOI18N
    
    public void generate(String ejbName, FileObject pkg,
                         boolean isQueue, Project project)
    throws IOException, VersionNotSupportedException {
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = provider.getDDRoot(ejbModule.getDeploymentDescriptor());

        ejbName = EjbGenerationUtil.uniqueSingleEjbName(ejbName, ejbJar);
        
        String pkgName = genUtil.getSelectedPackageName(pkg, project);
        Bean b = genUtil.getDefaultBean();
        b.setCommentDataEjbName(ejbName + "Bean");
        b.setClassname(true);
        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
        if (pkgName!=null) {
            b.setClassnamePackage(pkgName);
        }
        
        // generate bean class
        String beanClass = genUtil.generateBeanClass(MESSAGE_TEMPLATE, b, pkgName, pkg);
        
        ///
        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        ///

        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        MessageDriven mb = null;
        if (beans == null) {
            beans  = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        mb = beans.newMessageDriven();
        ActivationConfig config = mb.newActivationConfig();
        ActivationConfigProperty destProp = config.newActivationConfigProperty();
        destProp.setActivationConfigPropertyName("destinationType"); // NOI18N
        ActivationConfigProperty ackProp = config.newActivationConfigProperty();
        ackProp.setActivationConfigPropertyName("acknowledgeMode"); // NOI18N
        ackProp.setActivationConfigPropertyValue("Auto-acknowledge"); // NOI18N
        config.addActivationConfigProperty(ackProp);
        if (isQueue) {
            String queue = "javax.jms.Queue"; // NOI18N
            mb.setMessageDestinationType(queue);
            destProp.setActivationConfigPropertyValue(queue);
        } else {
            String topic = "javax.jms.Topic"; // NOI18N
            mb.setMessageDestinationType(topic);
            destProp.setActivationConfigPropertyValue(topic); 
            ActivationConfigProperty durabilityProp = config.newActivationConfigProperty();
            durabilityProp.setActivationConfigPropertyName("subscriptionDurability"); // NOI18N
            durabilityProp.setActivationConfigPropertyValue("Durable"); // NOI18N
            config.addActivationConfigProperty(durabilityProp);

            ActivationConfigProperty clientIdProp = config.newActivationConfigProperty();
            clientIdProp.setActivationConfigPropertyName("clientId"); // NOI18N
            clientIdProp.setActivationConfigPropertyValue(ejbName + "Bean"); // NOI18N
            config.addActivationConfigProperty(clientIdProp);

            ActivationConfigProperty subscriptionNameProp = config.newActivationConfigProperty();
            subscriptionNameProp.setActivationConfigPropertyName("subscriptionName"); // NOI18N
            subscriptionNameProp.setActivationConfigPropertyValue(ejbName + "Bean"); // NOI18N
            config.addActivationConfigProperty(subscriptionNameProp);
 
        }
        config.addActivationConfigProperty(destProp);
        mb.setActivationConfig(config);
        mb.setEjbName(ejbName + "Bean");
        mb.setDisplayName(ejbName+"MDB");
        mb.setEjbClass(beanClass);
        mb.setTransactionType(MessageDriven.TRANSACTION_TYPE_CONTAINER);
        
        beans.addMessageDriven(mb);
        // add transaction requirements
        AssemblyDescriptor ad = ejbJar.getSingleAssemblyDescriptor();
        if (ad == null) {
            ad = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(ad);
        }
        MessageDestination md = ad.newMessageDestination();
        String ejbNameBase = EjbGenerationUtil.getEjbNameBase(mb.getEjbName());
        String destinationLink = ejbNameBase+"Destination"; //NOI18N
        md.setDisplayName("Destination for " + ejbNameBase);
        md.setMessageDestinationName(destinationLink); 
        ad.addMessageDestination(md);
            
        mb.setMessageDestinationLink(destinationLink);
        ContainerTransaction ct = ad.newContainerTransaction();
        ct.setTransAttribute("Required"); //NOI18N
        org.netbeans.modules.j2ee.dd.api.ejb.Method m = ct.newMethod();
        m.setEjbName(ejbName + "Bean");
        m.setMethodName("*"); //NOI18N
        ct.addMethod(m);
        ad.addContainerTransaction(ct);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
        pwm.getConfigSupport().ensureResourceDefinedForEjb(ejbName + "Bean", "message-driven"); //NOI18N
    }
    
}
