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
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Andrei Badea
 */
public class GenerationUtilsTest extends NbTestCase {

    private FileObject workDir;
    private FileObject testFO;

    public GenerationUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class);

        clearWorkDir();
        workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }

    public void testPhase() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                assertEquals(JavaSource.Phase.ELEMENTS_RESOLVED, copy.getPhase());
            }
        });
    }

    public void testCreateClass() throws Exception {
        FileObject javaFO = GenerationUtils.createClass(workDir, "NewTestClass", "Javadoc");
        runUserActionTask(javaFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(ElementKind.CLASS, srcUtils.getTypeElement().getKind());
                assertTrue(srcUtils.getDefaultConstructor() != null);
                // TODO assert for Javadoc
            }
        });
    }

    public void testCreateInterface() throws Exception {
        FileObject javaFO = GenerationUtils.createInterface(workDir, "NewTestClass", "Javadoc");
        runUserActionTask(javaFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertEquals(ElementKind.INTERFACE, srcUtils.getTypeElement().getKind());
                // TODO assert for Javadoc
            }
        });
    }

    public void testCreateClassEnsuresDefaultConstructor() throws Exception {
        // replacing the Java template for classes with one without a default constructor
        RepositoryImpl.MultiFileSystemImpl systemFS = (RepositoryImpl.MultiFileSystemImpl)Repository.getDefault().getDefaultFileSystem();
        FileObject classTemplate = systemFS.getRoot().getFileObject("Templates/Classes/Class.java");
        TestUtilities.copyStringToFileObject(classTemplate,
                "package Templates.Classes;" +
                "public class Class {" +
                "}");
        try {
            // assert a default constructor is added even when the template did not contain one
            FileObject javaFO = GenerationUtils.createClass(workDir, "TestClass2", "Javadoc");
            runUserActionTask(javaFO, new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    assertTrue(SourceUtils.newInstance(controller).getDefaultConstructor() != null);
                }
            });
        } finally {
            // cleaning the changes to the system file system
            systemFS.reset();
        }
    }

    public void testCreateClassEnsuresPublicConstructor() throws Exception {
        // replacing the Java template for classes with one with a non-public default constructor
        RepositoryImpl.MultiFileSystemImpl systemFS = (RepositoryImpl.MultiFileSystemImpl)Repository.getDefault().getDefaultFileSystem();
        FileObject classTemplate = systemFS.getRoot().getFileObject("Templates/Classes/Class.java");
        TestUtilities.copyStringToFileObject(classTemplate,
                "package Templates.Classes;" +
                "public class Class {" +
                "   private Class() {" +
                "   }" +
                "}");
        try {
            // assert a default constructor is added even when the template did not contain one
            FileObject javaFO = GenerationUtils.createClass(workDir, "TestClass2", "Javadoc");
            runUserActionTask(javaFO, new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    ExecutableElement constructor = SourceUtils.newInstance(controller).getDefaultConstructor();
                    assertNotNull(constructor);
                    assertFalse(constructor.getModifiers().contains(Modifier.PRIVATE));
                }
            });
        } finally {
            // cleaning the changes to the system file system
            systemFS.reset();
        }
    }

    public void testCreateAnnotation() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                AnnotationTree annotationTree = genUtils.createAnnotation("java.lang.SuppressWarnings",
                        Collections.singletonList(genUtils.createAnnotationArgument("value", "foo")));
                ClassTree newClassTree = genUtils.addAnnotation(annotationTree, genUtils.getClassTree());
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                SuppressWarnings annotation = srcUtils.getTypeElement().getAnnotation(SuppressWarnings.class);
                assertNotNull(annotation);
                assertEquals(1, annotation.value().length);
                assertEquals("foo", annotation.value()[0]);
            }
        });
    }

    public void testCreateProperty() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   private Object x;" +
                "   public TestClass() {" +
                "   }" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                VariableTree field = genUtils.createField(Modifier.PRIVATE, "java.lang.String", "someProp");
                MethodTree getter = genUtils.createPropertyGetterMethod("java.lang.String", "someProp");
                MethodTree setter = genUtils.createPropertySetterMethod("java.lang.String", "someProp");
                TreeMaker make = copy.getTreeMaker();
                ClassTree newClassTree = genUtils.getClassTree();
                newClassTree = make.insertClassMember(newClassTree, 0, field);
                newClassTree = make.addClassMember(newClassTree, getter);
                newClassTree = make.addClassMember(newClassTree, setter);
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        // TODO check the field and methods
    }

    public void testAddImplementsClause() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree newClassTree = genUtils.addImplementsClause(genUtils.getClassTree(), "java.io.Serializable");
                newClassTree = genUtils.addImplementsClause(newClassTree, "java.lang.Cloneable");
                copy.rewrite(genUtils.getClassTree(), newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                assertImplements(controller, srcUtils.getTypeElement(), "java.io.Serializable");
                assertImplements(controller, srcUtils.getTypeElement(), "java.lang.Cloneable");
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }

    private static ModificationResult runModificationTask(FileObject javaFile, CancellableTask<WorkingCopy> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        return javaSource.runModificationTask(taskToTest);
    }

    private static void assertImplements(CompilationController controller, TypeElement typeElement, String interfaceName) {
        TypeMirror interfaceType = controller.getElements().getTypeElement("java.io.Serializable").asType();
        for (TypeMirror type : typeElement.getInterfaces()) {
            if (controller.getTypes().isSameType(interfaceType, type)) {
                return;
            }
        }
        fail("Type " + typeElement + " does not implement " + interfaceName);
    }
}
