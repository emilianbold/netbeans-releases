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
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class EntityGeneratorTest extends TestBase {
    
    public EntityGeneratorTest(String testName) {
        super(testName);
    }

    public void testGenerateJavaEE14() throws IOException {
        TestModule testModule = createEjb21Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE14");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE14");

        // CMP Entity EJB in Java EE 1.4
        
        EntityGenerator entityGenerator = new EntityGenerator("TestCmp", packageFileObject, true, true, true, "java.lang.Long", null, true);
        entityGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Entity entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_NAME, "TestCmpBean");

        assertNotNull(entity);
        assertEquals("TestCmpEB", entity.getDefaultDisplayName());
        assertEquals("TestCmpBean", entity.getEjbName());
        assertEquals("testGenerateJavaEE14.TestCmpRemoteHome", entity.getHome());
        assertEquals("testGenerateJavaEE14.TestCmpRemote", entity.getRemote());
        assertEquals("testGenerateJavaEE14.TestCmpLocalHome", entity.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestCmpLocal", entity.getLocal());
        assertEquals("testGenerateJavaEE14.TestCmpBean", entity.getEjbClass());
        assertEquals("Container", entity.getPersistenceType());
        assertEquals("java.lang.Long", entity.getPrimKeyClass());
        assertFalse(entity.isReentrant());
        assertEquals("TestCmp", entity.getAbstractSchemaName());
        assertEquals(1, entity.getCmpField().length);
        assertEquals("pk", entity.getCmpField()[0].getFieldName());
        assertEquals("pk", entity.getPrimkeyField());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpRemote.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpRemoteHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpRemoteHome.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // BMP Entity EJB in Java EE 1.4
        
        entityGenerator = new EntityGenerator("TestBmp", packageFileObject, false, true, false, "java.lang.Long", null, true);
        entityGenerator.generate();
        entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_NAME, "TestBmpBean");

        assertNotNull(entity);
        assertEquals("TestBmpEB", entity.getDefaultDisplayName());
        assertEquals("TestBmpBean", entity.getEjbName());
        assertNull(entity.getHome());
        assertNull(entity.getRemote());
        assertEquals("testGenerateJavaEE14.TestBmpLocalHome", entity.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestBmpLocal", entity.getLocal());
        assertEquals("testGenerateJavaEE14.TestBmpBean", entity.getEjbClass());
        assertEquals("Bean", entity.getPersistenceType());
        assertEquals("java.lang.Long", entity.getPrimKeyClass());
        assertFalse(entity.isReentrant());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestBmpBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestBmpBean.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestBmpLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestBmpLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestBmpLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestBmpLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestBmpRemote.java"));
        assertNull(packageFileObject.getFileObject("TestBmpRemoteHome.java"));
    }
    
}
