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

import java.io.File;
import junit.framework.*;
import com.sun.source.tree.ClassTree;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.jackpot.test.TestUtilities;
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
    
    protected EntityManagerGenerationStrategy getStrategy(WorkingCopy workingCopy, TreeMaker make, ClassTree clazz, GenerationOptions options){
        return new ContainerManagedJTAInjectableInWeb(workingCopy, make, clazz, getPersistenceUnit(), options);
    }
}

