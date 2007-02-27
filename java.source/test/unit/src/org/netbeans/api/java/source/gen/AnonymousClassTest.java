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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.*;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class AnonymousClassTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AnonymousClassTest 
     * @param name 
     */
    public AnonymousClassTest(String name) {
        super(name);
    }
    
    /**
     * @return 
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AnonymousClassTest.class);
//        suite.addTest(new AnonymousClassTest("testAddMethodToInvocParam"));
        return suite;
    }
    
    /**
     * #96364: When completing NewClassTree parameter in invocation,
     * nothing is generated.
     * Example:
     *
     * method(new Runnable| ); 
     * 
     * should be completed to
     * 
     * method(new Runnable {
     *            public void run() {
     *            }
     *        });
     */
    public void testAddMethodToInvocParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "class Test {\n" +
            "    void method(Runnable r) {\n" +
            "        method(new Runnable() {});\n" +
            "    }" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "class Test {\n" +
            "    void method(Runnable r) {\n" +
            "        method(new Runnable() {\n" +
            "    public void run() {\n" +
            "    }\n" +
            "});\n" +
            "    }}\n";

        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree testClass = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) testClass.getMembers().get(1);
                
                ExpressionStatementTree est = (ExpressionStatementTree) method.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                NewClassTree nct = (NewClassTree) mit.getArguments().get(0);
                MethodTree m = make.Method(
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                    "run",
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null
                );
                workingCopy.rewrite(nct.getClassBody(), make.addClassMember(nct.getClassBody(), m));
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
