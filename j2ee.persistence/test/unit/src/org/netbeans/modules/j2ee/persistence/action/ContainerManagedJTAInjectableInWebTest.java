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

package org.netbeans.modules.j2ee.persistence.action;

import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import java.io.File;
import junit.framework.*;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public class ContainerManagedJTAInjectableInWebTest extends EntityManagerGenerationTestSupport {
    
    public ContainerManagedJTAInjectableInWebTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File javaxAnnotation = new File(getWorkDir(), "javax" + File.separator + "annotation");
        javaxAnnotation.mkdirs();
        TestUtilities.copyStringToFile(
                new File(javaxAnnotation, "Resource.java"), 
                "package javax.annotation; public @interface Resource{}");
    }
    
    public void testGenerate() throws Exception{
        
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "}"
                );
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("create");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setQueryAttribute("");
        options.setReturnType("Object");
        
        FileObject result = generate(FileUtil.toFileObject(testFile), options);
        assertFile(result);
    }
    
    public void testGenerateWithExistingEM() throws Exception{
        
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n" +
                "import javax.persistence.EntityManager;\n" +
                "import javax.persistence.Resource;\n\n" +
                "public class Test {\n\n" +
                "    private EntityManager myEm;\n" +
                "}"
                );
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("create");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setQueryAttribute("");
        options.setReturnType("Object");
        
        FileObject result = generate(FileUtil.toFileObject(testFile), options);
        assertFile(getGoldenFile("testGenerateWithExistingEM.pass"), FileUtil.toFile(result));
    }



    protected Class<? extends EntityManagerGenerationStrategy> getStrategyClass() {
        return ContainerManagedJTAInjectableInWeb.class;
    }
}

