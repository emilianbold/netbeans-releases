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
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
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
        String packageName = "testGenerateJavaEE14";
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject(packageName);
        if (packageFileObject == null) {
            packageFileObject = sourceRoot.createFolder(packageName);
        }
        MessageGenerator generator = MessageGenerator.create("TestMDB", packageFileObject, true, false, true);
        generator.generate();
        
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        MessageDriven messageDriven = (MessageDriven) enterpriseBeans.findBeanByName(
                EnterpriseBeans.MESSAGE_DRIVEN, MessageDriven.EJB_NAME, "TestMDBBean");
        assertNotNull(messageDriven);
        assertEquals("TestMDBMDB", messageDriven.getDefaultDisplayName());
        assertEquals("TestMDBBean", messageDriven.getEjbName());
        assertEquals("testGenerateJavaEE14.TestMDBBean", messageDriven.getEjbClass());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("javax.jms.Queue", messageDriven.getMessageDestinationType());
        assertEquals("TestMDBBeanDestination", messageDriven.getMessageDestinationLink());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("Container", messageDriven.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBBean.java")), 
                getGoldenFile("TestMDBBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
    }
    
}
