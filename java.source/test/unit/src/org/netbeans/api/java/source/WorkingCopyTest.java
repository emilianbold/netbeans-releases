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

package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import java.io.File;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class WorkingCopyTest extends NbTestCase {

    public WorkingCopyTest(String name) {
        super(name);
    }

    public void testToPhaseAfterRewrite() throws Exception {
        clearWorkDir();
        File f = new File(getWorkDir(), "TestClass.java");
        TestUtilities.copyStringToFile(f,
                "package foo;" +
                "public class TestClass{" +
                "   public void foo() {" +
                "   }" +
                "}");
        FileObject fo = FileUtil.toFileObject(f);
        JavaSource javaSource = JavaSource.forFileObject(fo);
        javaSource.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {
            }
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                TreeMaker maker = copy.getTreeMaker();
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                TypeElement serializableElement = copy.getElements().getTypeElement("java.io.Serializable");
                ExpressionTree serializableTree = maker.QualIdent(serializableElement);
                ClassTree newClassTree = maker.addClassImplementsClause(classTree, serializableTree);

                copy.rewrite(classTree, newClassTree);
                // remove the following to make the test pass
                copy.toPhase(Phase.RESOLVED);
            }
        }).commit();

        javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                TypeElement testClassElement = copy.getElements().getTypeElement("foo.TestClass");
                TypeMirror serializableType = copy.getElements().getTypeElement("java.io.Serializable").asType();
                boolean serializableFound = false;
                for (TypeMirror type : testClassElement.getInterfaces()) {
                    if (copy.getTypes().isSameType(serializableType, type)) {
                        serializableFound = true;
                    }
                }
                assertTrue("TestClass should implement Serializable", serializableFound);
            }
        }, true);
    }
}
