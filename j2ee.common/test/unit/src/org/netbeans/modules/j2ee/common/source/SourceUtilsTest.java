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

package org.netbeans.modules.j2ee.common.source;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea, Martin Adamek
 */
public class SourceUtilsTest extends NbTestCase {

    private FileObject testFO;

    public SourceUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class);

        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }

    public void testNewInstance() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("foo.TestClass");
                SourceUtils srcUtils = SourceUtils.newInstance(controller, typeElement);
                assertSame(typeElement, srcUtils.getTypeElement());
                assertEquals(controller.getTrees().getTree(typeElement), srcUtils.getClassTree());

                ClassTree classTree = (ClassTree)controller.getCompilationUnit().getTypeDecls().get(0);
                srcUtils = SourceUtils.newInstance(controller, classTree);
                assertSame(classTree, srcUtils.getClassTree());
                TreePath classTreePath = controller.getTrees().getPath(controller.getCompilationUnit(), classTree);
                typeElement = (TypeElement)controller.getTrees().getElement(classTreePath);
                assertEquals(typeElement, srcUtils.getTypeElement());

                srcUtils = SourceUtils.newInstance(controller);
                assertSame(srcUtils.getTypeElement(), typeElement);
                assertSame(srcUtils.getClassTree(), classTree);
            }
        });
    }

    public void testPhase() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(JavaSource.Phase.ELEMENTS_RESOLVED, controller.getPhase());
            }
        });
    }

    public void testMainTypeElement() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}" +
                "class AnotherClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                TypeElement typeElement = SourceUtils.newInstance(controller).getTypeElement();
                assertTrue(typeElement.getQualifiedName().contentEquals("foo.TestClass"));
            }
        });

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class AnotherClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                assertNull(SourceUtils.newInstance(controller));
            }
        });
    }

    public void testGetDefaultConstructor() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() {" +
                "   }" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                ExecutableElement constructor = srcUtils.getDefaultConstructor();
                assertNotNull(constructor);
                assertFalse(controller.getElementUtilities().isSyntetic(constructor));
            }
        });

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertNull(srcUtils.getDefaultConstructor());
            }
        });
    }

    public void testHasMainMethod() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public static void main(String[] args) {" +
                "   }" +
                "   public void method() {" +
                "   }" +
                "}");
        assertTrue(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public void method1() {" +
                "   }" +
                "   public static void main(String[] args) {" +
                "   }" +
                "   public void method2() {" +
                "   }" +
                "}");
        assertTrue(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public void method() {" +
                "   }" +
                "   public static void main(String[] args) {" +
                "   }" +
                "}");
        assertTrue(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   static void main(String[] args) {" +
                "   }" +
                "   public void method() {" +
                "   }" +
                "   public static void main(Integer[] args) {" +
                "   }" +
                "}");
        assertFalse(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public static boolean main(String[] args) {" +
                "   }" +
                "   public void method() {" +
                "   }" +
                "   public static void main() {" +
                "   }" +
                "}");
        assertFalse(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public void main(String[] args) {" +
                "   }" +
                "   public void method() {" +
                "   }" +
                "}");
        assertFalse(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public interface TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public void main(String[] args) {" +
                "   }" +
                "   public void method() {" +
                "   }" +
                "}");
        assertFalse(SourceUtils.hasMainMethod(testFO));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() {" +
                "   }" +
                "}");
        assertFalse(SourceUtils.hasMainMethod(testFO));
    }

    public void testIsSubtype() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass implements java.io.Serializable {" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertTrue(srcUtils.isSubtype("java.io.Serializable"));
                assertFalse(srcUtils.isSubtype("java.lang.Cloneable"));
            }
        });
    }

    public void testIsSubtypeGenerics() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "import java.util.Enumeration;" +
                "public class TestClass implements Enumeration<String> {" +
                "    public boolean hasMoreElement() {" +
                "        return false;" +
                "    }" +
                "    public String nextElement() {" +
                "        return null;" +
                "    }" +
                "}");
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertTrue(srcUtils.isSubtype("java.util.Enumeration<String>"));
                assertFalse(srcUtils.isSubtype("java.util.Enumeration<Object>"));
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }
}
