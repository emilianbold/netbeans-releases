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

import java.io.IOException;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
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

    public GenerationUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class);
        clearWorkDir();
        workDir = FileUtil.toFileObject(getWorkDir());
    }

    public void testCreateClass() throws Exception {
        FileObject javaFO = GenerationUtils.createClass(workDir, "TestClass", "Javadoc");
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
        FileObject javaFO = GenerationUtils.createInterface(workDir, "TestClass", "Javadoc");
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

    private static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }
}
