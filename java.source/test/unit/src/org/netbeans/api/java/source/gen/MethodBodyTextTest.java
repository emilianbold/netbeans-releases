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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import com.sun.source.tree.*;
import java.io.File;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Tests indentation of newly generated body text in method.
 *
 * @author Pavel Flaska
  */
public class MethodBodyTextTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodBodyTextTest */
    public MethodBodyTextTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(MethodBodyTextTest.class);
//        suite.addTest(new MethodBodyTextTest("testSetBodyText"));
//        suite.addTest(new MethodBodyTextTest("testCreateWithBodyText"));
        suite.addTest(new MethodBodyTextTest("testCreateReturnBooleanBodyText"));
//        suite.addTest(new MethodBodyTextTest("testModifyBodyText"));
        suite.addTest(new MethodBodyTextTest("testReplaceConstrBody"));
        suite.addTest(new MethodBodyTextTest("testReplaceMethod"));
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodBodyText.java");
    }
    
    public void testSetBodyText() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testSetBodyText");
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
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        BlockTree newBody = make.createMethodBody(node, "{ System.err.println(\"Nothing.\"); }");
                        workingCopy.rewrite(node.getBody(), newBody);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String golden = TestUtilities.copyFileToString(
            getFile(getGoldenDir(), getGoldenPckg() + "testSetBodyText_MethodBodyTextTest.pass")
        );
        assertEquals(golden, res);
    }
    
    public void testCreateWithBodyText() throws java.io.IOException, FileStateInvalidException {
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
                        StringBuffer body = new StringBuffer();
                        body.append("{ System.out.println(\"Again Nothing\"); }");
                        MethodTree method = make.Method(
                            make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                            "method2",
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            body.toString(),
                            null
                        );
                        ClassTree copy = make.addClassMember(clazz, method);
                        workingCopy.rewrite(clazz, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String golden = TestUtilities.copyFileToString(
            getFile(getGoldenDir(), getGoldenPckg() + "testCreateWithBodyText_MethodBodyTextTest.pass")
        );
        assertEquals(golden, res);
    }
    
    public void testCreateReturnBooleanBodyText() throws java.io.IOException, FileStateInvalidException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree node  = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                StringBuffer body = new StringBuffer();
                body.append("{ return false; }");
                MethodTree method = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                        "equals",
                        make.PrimitiveType(TypeKind.BOOLEAN),
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        body.toString(),
                        null
                        );
                ClassTree clazz = make.addClassMember(node, method);
                workingCopy.rewrite(node, clazz);
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        // there is "return 0" instead
        String result = TestUtilities.copyFileToString(testFile);
        System.err.println(result);
        assertTrue(result.contains("return false"));
    }
    
    public void testModifyBodyText() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testModifyBodyText");
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
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        String body = "{ List l; }";
                        BlockTree copy = make.createMethodBody(node, body);
                        workingCopy.rewrite(node.getBody(), copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String golden = TestUtilities.copyFileToString(
            getFile(getGoldenDir(), getGoldenPckg() + "testModifyBodyText_MethodBodyTextTest.pass")
        );
        assertEquals(golden, res);
    }

    /**
     * Replace constructor body. -- In old constructor, syntetic super()
     * was in the body, no syntetic element in new constructor body. 
     * 
     * #93740
     */
    public void testReplaceConstrBody() throws Exception {
        System.err.println("testReplaceConstrBody");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {" +
            "System.err.println(null);\n" +
            "\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "    }\n" +
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
                        MethodTree method = (MethodTree) clazz.getMembers().get(0);
                        ExpressionStatementTree statement = make.ExpressionStatement(
                            make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                make.MemberSelect(
                                    make.MemberSelect(
                                        make.Identifier("System"),
                                        "err"
                                    ),
                                    "println"
                                ),
                                Collections.singletonList(
                                    make.Literal(null)
                                )
                            )
                        );
                        BlockTree newBody = make.Block(
                                Collections.<StatementTree>singletonList(statement),
                                false
                        );
                        workingCopy.rewrite(method.getBody(), newBody);
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
     * #93730 - incorrectly diffed method invocation.
     */
    public void testReplaceMethod() throws Exception {
        System.err.println("testReplaceMethod");
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "        for(int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"In loop\");\n" +
            "        }\n" +
            "        Thread.currentThread();\n" +
            "    }\n" +
            "}\n");
        
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "    public Object method() {\n" +
            "        System.out.println(\"Ahoj svete!\");\n" +
            "    }\n" +
            "}\n";
                 
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree meth = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{System.out.println(\"Ahoj svete!\");}";
                MethodTree newMeth = make.Method(
                        meth.getModifiers(),
                        meth.getName(),
                        meth.getReturnType(),
                        meth.getTypeParameters(),
                        meth.getParameters(),
                        meth.getThrows(),
                        bodyText,
                        (ExpressionTree) meth.getDefaultValue()
                );
                workingCopy.rewrite(meth.getBody(), newMeth.getBody());
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/indent/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/indent/MethodBodyTextTest/";
    }

}
