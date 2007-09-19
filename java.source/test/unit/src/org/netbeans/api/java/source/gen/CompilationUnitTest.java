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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import java.io.File;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author tom
 */
public class CompilationUnitTest extends GeneratorTestMDRCompat {

    public CompilationUnitTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CompilationUnitTest.class);
        return suite;
    }

    public void testNewCompilationUnit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");
        FileObject fakeFO = FileUtil.createData(fakeFile);

        FileObject rootFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject emptyJava = FileUtil.createData(rootFS, "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.createData(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo, fakeFO);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = treeMaker.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                ClassTree clazz = treeMaker.Class(
                        treeMaker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Krtek",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = treeMaker.addCompUnitTypeDecl(newTree, clazz);
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        System.err.println(res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}