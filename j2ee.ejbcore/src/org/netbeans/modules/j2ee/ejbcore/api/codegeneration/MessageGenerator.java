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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.jmi.reflect.JmiException;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.AttributeValue;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MessageEJBWizardPanel;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class MessageGenerator {
    private EjbGenerationUtil genUtil = new EjbGenerationUtil();
    private static final String MESSAGE_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"MessageBean.xml"; //NOI18N
    
    public FileObject generate(String ejbName, FileObject pkg, MessageEJBWizardPanel wizardPanel, Project project) throws IOException, VersionNotSupportedException {
        boolean ejb30 = false;
        try {
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
            ejb30 = EjbJar.VERSION_3_0.equals(DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit()).getVersion().toString());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        if (ejb30) {
            return generateEjb3(ejbName, pkg, ((MessageEJBWizardPanel) wizardPanel).isQueue(), project);
        } else {
            return generateEjb21(ejbName, pkg, ((MessageEJBWizardPanel) wizardPanel).isQueue(), project);
        }
    }
    
    private FileObject generateEjb21(String ejbName, FileObject pkg, boolean isQueue, Project project) throws IOException, VersionNotSupportedException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
        
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
        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
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
        
        // use simple names in all generated classes, use imports
        FileObject beanFO = pkg.getFileObject(EjbGenerationUtil.getBaseName(mb.getEjbClass()), "java"); // NOI18N
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            JavaMetamodel.getManager().setClassPath(beanFO);
            JMIUtils.fixImports(mb.getEjbClass());
            rollback = false;
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        
        JMIUtils.saveClass(mb.getEjbClass(), beanFO);
        return beanFO;
    }
    
    private FileObject generateEjb3(String name, FileObject pkg, boolean isQueue, Project project) {
        JavaClass javaClass = null;
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            javaClass = JMIGenerationUtil.createEntityClass(pkg, name);
            JMIGenerationUtil.addInterface(javaClass, "javax.jms.MessageListener");
            AttributeValue mappedNameAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "mappedName", "jms/" + name);
            
            // ActivationConfigProperty
            List<Annotation> activationConfigSubAnnotations = new ArrayList<Annotation>();
            AttributeValue[] attributes;
            // acknowledgeMode
            attributes = new AttributeValue[] {
                JMIGenerationUtil.createAttributeValue(javaClass, "propertyName", "acknowledgeMode"),
                JMIGenerationUtil.createAttributeValue(javaClass, "propertyValue", "Auto-acknowledge")
            };
            activationConfigSubAnnotations.add(JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.ActivationConfigProperty", Arrays.asList(attributes)));
            if (isQueue) {
                // destinationType
                attributes = new AttributeValue[] {
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyName", "destinationType"),
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyValue", "javax.jms.Queue")
                };
                activationConfigSubAnnotations.add(JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.ActivationConfigProperty", Arrays.asList(attributes)));
            } else {
                // destinationType
                attributes = new AttributeValue[] {
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyName", "destinationType"),
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyValue", "javax.jms.Topic")
                };
                activationConfigSubAnnotations.add(JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.ActivationConfigProperty", Arrays.asList(attributes)));
                // subscriptionDurability
                attributes = new AttributeValue[] {
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyName", "subscriptionDurability"),
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyValue", "Durable")
                };
                activationConfigSubAnnotations.add(JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.ActivationConfigProperty", Arrays.asList(attributes)));
                
                // not recognized by EJB 3.0 schema, but needed when subscriptionDurability=Durable
                // client id
                attributes = new AttributeValue[] {
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyName", "clientId"),
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyValue", name)
                };
                activationConfigSubAnnotations.add(JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.ActivationConfigProperty", Arrays.asList(attributes)));
                // subscriptionName
                attributes = new AttributeValue[] {
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyName", "subscriptionName"),
                    JMIGenerationUtil.createAttributeValue(javaClass, "propertyValue", name)
                };
                activationConfigSubAnnotations.add(JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.ActivationConfigProperty", Arrays.asList(attributes)));
            }
            AttributeValue activationConfigAttributeValue = JMIGenerationUtil.createAttributeValue(javaClass, "activationConfig", activationConfigSubAnnotations);

            List messageDrivenAttributes = new ArrayList(2);
            messageDrivenAttributes.add(mappedNameAttibuteValue);
            messageDrivenAttributes.add(activationConfigAttributeValue);
            Annotation messageDrivenAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.MessageDriven", messageDrivenAttributes);
            javaClass.getAnnotations().add(messageDrivenAnnotation);

            Method onMessageMethod = JMIGenerationUtil.createMethod(javaClass, "onMessage", Modifier.PUBLIC, "void");
            Parameter messageParameter = JMIGenerationUtil.createParameter(javaClass, "message", "javax.jms.Message");
            onMessageMethod.getParameters().add(messageParameter);
            javaClass.getFeatures().add(onMessageMethod);
            rollback = false;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } catch (JmiException jmie) {
            ErrorManager.getDefault().notify(jmie);
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        
        if(!rollback) {
            // Create server resources for this bean.
            // 
            // !PW Posted via RequestProcessor for now because the merged annotion provider
            // does not have any information for this bean if this is invoked syncronously.
            // We need to find a more stable mechanism for the, perhaps new API that directly
            // accepts the annotation reference created above.  This construct is too fragile.
            //
            // Note: Even with 1s (1000ms) delay, sometimes the data was still not available.
            // 
            final J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            final String ejbName = name;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    if(pwm != null) {
                        pwm.getConfigSupport().ensureResourceDefinedForEjb(ejbName, "message-driven"); //NOI18N
                    }
                }
            }, 2000);
        }
        
        return javaClass == null ? null : JavaModel.getFileObject(javaClass.getResource());
    }
    
}
