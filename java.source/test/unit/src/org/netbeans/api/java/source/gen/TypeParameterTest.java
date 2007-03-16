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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.*;
import java.util.Collections;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Some test with type parameters.
 * 
 * @author Pavel Flaska
 */
public class TypeParameterTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of TypeParameterTest */
    public TypeParameterTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TypeParameterTest.class);
        return suite;
    }
    
    /**
     * #92531: rename in type parameter in class decl:
     * public MyList<Ex> extends ArrayList<Ex> implements List<Ex> { ...
     */
    public void testRenameTypeParamInClassDecl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<Ex> extends ArrayList<Ex> implements List<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<E> extends ArrayList<E> implements List<E> {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        // name
                        TypeParameterTree tpt = clazz.getTypeParameters().get(0);
                        workingCopy.rewrite(tpt, make.setLabel(tpt, "E"));
                        // extends
                        ParameterizedTypeTree ptt = (ParameterizedTypeTree) clazz.getExtendsClause();
                        IdentifierTree it = (IdentifierTree) ptt.getTypeArguments().get(0);
                        workingCopy.rewrite(it, make.Identifier("E"));
                        // implements
                        ptt = (ParameterizedTypeTree) clazz.getImplementsClause().get(0);
                        it = (IdentifierTree) ptt.getTypeArguments().get(0);
                        workingCopy.rewrite(it, make.Identifier("E"));
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testRenameWildCard() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<Ex> {\n" +
            "    public Object method(Collection<? extends Ex> c) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<E> {\n" +
            "    public Object method(Collection<? extends E> c) {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // name
                TypeParameterTree tpt = clazz.getTypeParameters().get(0);
                workingCopy.rewrite(tpt, make.setLabel(tpt, "E"));
                // method
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree vt = method.getParameters().get(0);
                ParameterizedTypeTree ptt = (ParameterizedTypeTree) vt.getType();
                WildcardTree wct = (WildcardTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(wct.getBound(), make.Identifier("E"));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testAddGenericImplements() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class MyList<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class MyList<Ex> implements Nothing<String> {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ParameterizedTypeTree ptt = make.ParameterizedType(
                        make.Identifier("Nothing"), 
                        Collections.<ExpressionTree>singletonList(make.Identifier("String"))
                );
                // name
                workingCopy.rewrite(clazz, make.addClassImplementsClause(clazz, ptt));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testAddGenericImplementsWithAutoImp() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class MyList<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class MyList<Ex> implements List<String> {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                TypeElement te = workingCopy.getElements().getTypeElement("java.util.List");
                ParameterizedTypeTree ptt = make.ParameterizedType(
                        make.QualIdent(te),
                        Collections.<ExpressionTree>singletonList(make.Identifier("String"))
                );
                // name
                workingCopy.rewrite(clazz, make.addClassImplementsClause(clazz, ptt));
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
