/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.core.api.support.java;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
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
        TestUtilities.setCacheFolder(getWorkDir());
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }

    public void testNewInstance() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
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
        runUserActionTask(testFO, new Task<CompilationController>() {
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
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                TypeElement typeElement = SourceUtils.newInstance(controller).getTypeElement();
                assertTrue(typeElement.getQualifiedName().contentEquals("foo.TestClass"));
            }
        });

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class AnotherClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                assertNull(SourceUtils.newInstance(controller));
            }
        });
    }

    public void testGetNoArgConstructor() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() {" +
                "   }" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                ExecutableElement constructor = srcUtils.getNoArgConstructor();
                assertNotNull(constructor);
                assertFalse(controller.getElementUtilities().isSynthetic(constructor));
            }
        });

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertNull(srcUtils.getNoArgConstructor());
            }
        });
    }

    public void testIsSubtype() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass implements java.io.Serializable {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
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
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertTrue(srcUtils.isSubtype("java.util.Enumeration<String>"));
                assertFalse(srcUtils.isSubtype("java.util.Enumeration<Object>"));
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, Task<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }
}
