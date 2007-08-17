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

package org.netbeans.modules.j2ee.persistence.wizard.dao;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for <code>EjbFacadeWizardIterator</code>.
 *
 * @author Erno Mononen
 */
public class EjbFacadeWizardIteratorTest extends SourceTestSupport {
    
    public EjbFacadeWizardIteratorTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() throws Exception{
        super.setUp();
        File javaxEjb = new File(getWorkDir(), "javax" + File.separator + "ejb");
        javaxEjb.mkdirs();
        TestUtilities.copyStringToFile(new File(javaxEjb, "Stateless.java"), "package javax.ejb; public @interface Stateless{}");
        TestUtilities.copyStringToFile(new File(javaxEjb, "Local.java"), "package javax.ejb; public @interface Local{}");
        TestUtilities.copyStringToFile(new File(javaxEjb, "Remote.java"), "package javax.ejb; public @interface Remote{}");
    }
    
    public void testCreateInterface() throws Exception {
        
        final String name = "Test";
        final String annotationType = "javax.ejb.Remote";
        final String pkgName = "foo";
        File pkg = new File(getWorkDir(), pkgName);
        pkg.mkdir();
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        
        String golden =
                "package " + pkgName +";\n\n" + 
                "import " + annotationType + ";\n\n" +
                "@" + Util.simpleClassName(annotationType) + "\n" +
                "public interface " + name + " {\n" +
                "}";
        FileObject result = wizardIterator.createInterface(name, annotationType, FileUtil.toFileObject(pkg));
        assertEquals(golden, TestUtilities.copyFileObjectToString(result));
    }
    
    public void testAddMethodToInterface() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String originalContent =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public interface Test {\n" +
                "}";
        
        TestUtilities.copyStringToFile(testFile, originalContent);
        
        String golden =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public interface Test {\n\n" +
                "    void testMethod(Object entity);\n" +
                "}";
        
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("testMethod");
        options.setReturnType("void");
        options.setParameterName("entity");
        options.setParameterType("Object");
        wizardIterator.addMethodToInterface(Collections.<GenerationOptions>singletonList(options), FileUtil.toFileObject(testFile));
        assertEquals(golden, TestUtilities.copyFileToString(testFile));
        
    }
    
    public void testGetUniqueClassName() throws IOException{
        File testFile = new File(getWorkDir(), "Test.java");
        testFile.mkdir();
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        String result = wizardIterator.getUniqueClassName("Test", FileUtil.toFileObject(getWorkDir()));
        assertEquals("Test_1", result);
        
        File testFile2 = new File(getWorkDir(), "Test_1.java");
        testFile2.mkdir();
        result = wizardIterator.getUniqueClassName("Test", FileUtil.toFileObject(getWorkDir()));
        assertEquals("Test_2", result);
    }
    
    public void testGenerate() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String originalContent =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "@javax.persistence.Entity\n" +
                "public class Test {\n" +
                "}";
        
        final String pkgName = "foo";
        File pkg = new File(getWorkDir(), pkgName);
        pkg.mkdir();

        TestUtilities.copyStringToFile(testFile, originalContent);
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        Set<FileObject> result = wizardIterator.generate(
                FileUtil.toFileObject(pkg), "Test", pkgName,
                true, true, ContainerManagedJTAInjectableInEJB.class);
               
        assertEquals(3, result.size());
        
        for (FileObject each : result){
            assertFile(FileUtil.toFile(each), getGoldenFile(each.getNameExt()));
        }
        
    }
    
}
