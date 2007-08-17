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
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
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
    }
    
    public void testCreateInterface() throws Exception {
        
        final String name = "Test";
        final String annotationType = "javax.ejb.Remote";
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        
        String golden =
                "@" + annotationType + "\n" +
                "public interface " + name + " {\n" +
                "}\n";
        FileObject result = wizardIterator.createInterface(name, annotationType, FileUtil.toFileObject(getWorkDir()));
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
                "void testMethod(Object entity) ;\n" +
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
        
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        String result = wizardIterator.getUniqueClassName("Test", FileUtil.toFileObject(getWorkDir()));
        System.out.println("Result: " + result);
        assertEquals("Test2", result);
        
        File testFile2 = new File(getWorkDir(), "Test2.java");
        wizardIterator.getUniqueClassName("Test", FileUtil.toFileObject(getWorkDir()));
        assertEquals("Test3", result);
    }
    
    public void testGenerate() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String originalContent =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "@javax.persistence.Entity\n" +
                "public class Test {\n" +
                "}";
        
        TestUtilities.copyStringToFile(testFile, originalContent);
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        Set<FileObject> result = new HashSet<FileObject>();
        wizardIterator.generate(FileUtil.toFileObject(getWorkDir()), "Test", 
                "org.netbeans.test", true, true, ContainerManagedJTAInjectableInEJB.class);
        
        
        assertEquals(3, result.size());
        
        for (FileObject each : result){
            assertFile(FileUtil.toFile(each), getGoldenFile(each.getNameExt()));
        }
        
    }
    
}
