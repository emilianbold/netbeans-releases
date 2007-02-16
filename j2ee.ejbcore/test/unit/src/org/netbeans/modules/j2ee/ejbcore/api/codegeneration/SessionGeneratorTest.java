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
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateJavaEE14() throws IOException {
        TestModule testModule = ejb14();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE14");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE14");

        // Stateless EJB in Java EE 1.4
        
        SessionGenerator sessionGenerator = SessionGenerator.create("TestStatelessLR", packageFileObject, true, true, false, false, false, true);
        sessionGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Session session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, "TestStatelessLRBean");

        assertNotNull(session);
        assertEquals("TestStatelessLRSB", session.getDefaultDisplayName());
        assertEquals("TestStatelessLRBean", session.getEjbName());
        assertEquals("testGenerateJavaEE14.TestStatelessLRRemoteHome", session.getHome());
        assertEquals("testGenerateJavaEE14.TestStatelessLRRemote", session.getRemote());
        assertEquals("testGenerateJavaEE14.TestStatelessLRLocalHome", session.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestStatelessLRLocal", session.getLocal());
        assertEquals("testGenerateJavaEE14.TestStatelessLRBean", session.getEjbClass());
        assertEquals("Stateless", session.getSessionType());
        assertEquals("Container", session.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRRemote.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRRemoteHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRRemoteHome.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 1.4
        
        sessionGenerator = SessionGenerator.create("TestStatefulLR", packageFileObject, false, true, true, false, false, true);
        sessionGenerator.generate();
        session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, "TestStatefulLRBean");

        assertNotNull(session);
        assertEquals("TestStatefulLRSB", session.getDefaultDisplayName());
        assertEquals("TestStatefulLRBean", session.getEjbName());
        assertNull(session.getHome());
        assertNull(session.getRemote());
        assertEquals("testGenerateJavaEE14.TestStatefulLRLocalHome", session.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestStatefulLRLocal", session.getLocal());
        assertEquals("testGenerateJavaEE14.TestStatefulLRBean", session.getEjbClass());
        assertEquals("Stateful", session.getSessionType());
        assertEquals("Container", session.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestStatefulLRRemote.java"));
        assertNull(packageFileObject.getFileObject("TestStatefulLRRemoteHome.java"));
    }
    
    public void testGenerateJavaEE50() throws IOException {
        TestModule testModule = ejb50();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE50");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE50");

        // Stateless EJB in Java EE 5.0 defined in annotations
        
        SessionGenerator sessionGenerator = SessionGenerator.create("TestStateless", packageFileObject, true, true, false, true, false, false);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLocal.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessRemote.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 5.0 defined in annotations
        
        sessionGenerator = SessionGenerator.create("TestStateful", packageFileObject, true, false, true, true, false, false);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatefulBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulRemote.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatefulRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestStatefulLocal.java"));
    }
    
}
