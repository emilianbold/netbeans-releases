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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class MessageGeneratorTest extends TestBase {
    
    public MessageGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateJavaEE14() throws IOException, VersionNotSupportedException {
        TestModule testModule = ejb14();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE14");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE14");

        // Queue based MessageDriven EJB in Java EE 1.4
        
        MessageGenerator generator = MessageGenerator.create("TestMDBQueue", packageFileObject, true, false, true);
        generator.generate();
        
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        MessageDriven messageDriven = (MessageDriven) enterpriseBeans.findBeanByName(
                EnterpriseBeans.MESSAGE_DRIVEN, MessageDriven.EJB_NAME, "TestMDBQueueBean");
        assertNotNull(messageDriven);
        assertEquals("TestMDBQueueMDB", messageDriven.getDefaultDisplayName());
        assertEquals("TestMDBQueueBean", messageDriven.getEjbName());
        assertEquals("testGenerateJavaEE14.TestMDBQueueBean", messageDriven.getEjbClass());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("javax.jms.Queue", messageDriven.getMessageDestinationType());
        assertEquals("TestMDBQueueBeanDestination", messageDriven.getMessageDestinationLink());
        ActivationConfig activationConfig = messageDriven.getActivationConfig();
        assertEquals(2, activationConfig.getActivationConfigProperty().length);
        ActivationConfigProperty acProperty = activationConfig.getActivationConfigProperty()[0];
        assertEquals("acknowledgeMode", acProperty.getActivationConfigPropertyName());
        assertEquals("Auto-acknowledge", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[1];
        assertEquals("destinationType", acProperty.getActivationConfigPropertyName());
        assertEquals("javax.jms.Queue", acProperty.getActivationConfigPropertyValue());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestMDBQueueBean.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Topic based MessageDriven EJB in Java EE 1.4
        
        generator = MessageGenerator.create("TestMDBTopic", packageFileObject, false, false, true);
        generator.generate();
        
        messageDriven = (MessageDriven) enterpriseBeans.findBeanByName(
                EnterpriseBeans.MESSAGE_DRIVEN, MessageDriven.EJB_NAME, "TestMDBTopicBean");
        assertNotNull(messageDriven);
        assertEquals("TestMDBTopicMDB", messageDriven.getDefaultDisplayName());
        assertEquals("TestMDBTopicBean", messageDriven.getEjbName());
        assertEquals("testGenerateJavaEE14.TestMDBTopicBean", messageDriven.getEjbClass());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("javax.jms.Topic", messageDriven.getMessageDestinationType());
        assertEquals("TestMDBTopicBeanDestination", messageDriven.getMessageDestinationLink());
        activationConfig = messageDriven.getActivationConfig();
        assertEquals(5, activationConfig.getActivationConfigProperty().length);
        acProperty = activationConfig.getActivationConfigProperty()[0];
        assertEquals("acknowledgeMode", acProperty.getActivationConfigPropertyName());
        assertEquals("Auto-acknowledge", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[1];
        assertEquals("subscriptionDurability", acProperty.getActivationConfigPropertyName());
        assertEquals("Durable", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[2];
        assertEquals("clientId", acProperty.getActivationConfigPropertyName());
        assertEquals("TestMDBTopicBean", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[3];
        assertEquals("subscriptionName", acProperty.getActivationConfigPropertyName());
        assertEquals("TestMDBTopicBean", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[4];
        assertEquals("destinationType", acProperty.getActivationConfigPropertyName());
        assertEquals("javax.jms.Topic", acProperty.getActivationConfigPropertyValue());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBTopicBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestMDBTopicBean.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // added by both previous generators
        
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        List<String> messageDestinationNames = new ArrayList<String>();
        for (MessageDestination messageDestination : assemblyDescriptor.getMessageDestination()) {
            messageDestinationNames.add(messageDestination.getMessageDestinationName());
        }
        assertTrue(messageDestinationNames.contains("TestMDBQueueBeanDestination"));
        assertTrue(messageDestinationNames.contains("TestMDBTopicBeanDestination"));
    }
    
    public void testGenerateJavaEE50() throws IOException {
        TestModule testModule = ejb14();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE50");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE50");
        
        // Queue based MessageDriven EJB in Java EE 5 defined in annotation
        
        MessageGenerator generator = MessageGenerator.create("TestMDBQueue", packageFileObject, true, true, false);
        generator.generate();
        
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestMDBQueueBean.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Topic based MessageDriven EJB in Java EE 5 defined in annotation
        
        generator = MessageGenerator.create("TestMDBTopic", packageFileObject, false, true, false);
        generator.generate();
        
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestMDBQueueBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
    }

}
