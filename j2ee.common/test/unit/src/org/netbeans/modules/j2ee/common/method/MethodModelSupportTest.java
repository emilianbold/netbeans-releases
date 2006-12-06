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

package org.netbeans.modules.j2ee.common.method;

import java.io.InputStream;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.FakeJavaDataLoaderPool;
import org.netbeans.modules.j2ee.common.source.RepositoryImpl;
import org.netbeans.modules.j2ee.common.source.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class MethodModelSupportTest extends NbTestCase {
    
    private FileObject testFO;

    public MethodModelSupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class);

        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }
    
    public void testGetTypeName() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Elements elements = controller.getElements();
                Types types = controller.getTypes();
                
                String typeName = String.class.getName();
                String resolvedTypeName = MethodModelSupport.getTypeName(controller, elements.getTypeElement(typeName).asType());
                assertEquals(typeName, resolvedTypeName);
                
                typeName = InputStream.class.getName();
                resolvedTypeName = MethodModelSupport.getTypeName(controller, elements.getTypeElement(typeName).asType());
                assertEquals(typeName, resolvedTypeName);
                
                resolvedTypeName = MethodModelSupport.getTypeName(controller, types.getPrimitiveType(TypeKind.INT));
                assertEquals("int", resolvedTypeName);

                typeName = String.class.getName();
                resolvedTypeName = MethodModelSupport.getTypeName(controller, types.getArrayType(elements.getTypeElement(typeName).asType()));
                assertEquals("java.lang.String[]", resolvedTypeName);
            }
        });
    }
    
    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }

}
