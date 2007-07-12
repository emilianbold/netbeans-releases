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

import java.io.*;
import com.sun.source.tree.*;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import static org.netbeans.api.java.source.JavaSource.*;

/**
 * Test enum modifications.
 * 
 * @author Pavel Flaska
 */
public class EnumTest extends GeneratorTest {
    
    /** Creates a new instance of EnumTest */
    public EnumTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(EnumTest.class);
//        suite.addTest(new EnumTest("testConstantRename"));
//        suite.addTest(new EnumTest("testAddMethodAfterConstants"));
//        suite.addTest(new EnumTest("testRenameConstantCheckJavadoc"));
//        suite.addTest(new EnumTest("testRenameWithInit"));
        return suite;
    }

    /**
     * Test renames 'A' constant to 'A2' constant in code written below:
     * 
     * <code>
     * public enum Test {
     *     A, B, C;
     *    
     *     public void enumMethod() {
     *     }
     * }
     * </code>
     */
    public void testConstantRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A2, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                        VariableTree copy = make.setLabel(vt, "A2");
                        workingCopy.rewrite(vt, copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Test adds the method at the end of the class, demonstrates #104839
     * 
     * Original:
     * 
     * <code>
     * public enum Test {
     *     A, B, C;
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     *     A, B, C;
     *    
     *     public void enumMethod() {
     *     }
     * }
     * </code>
     */
    public void testAddMethodAfterConstants() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "\n" +
            "    public void enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // create method.
                ClassTree copy = make.addClassMember(clazz, m(make));
                workingCopy.rewrite(clazz, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    private MethodTree m(TreeMaker make) {
        // create method
        MethodTree newMethod = make.Method(
            make.Modifiers( 
                Collections.singleton(Modifier.PUBLIC), // modifiers
                Collections.<AnnotationTree>emptyList() // annotations
            ), // modifiers and annotations
            "enumMethod", // name
            make.PrimitiveType(TypeKind.VOID), // return type
            Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
            Collections.<VariableTree>emptyList(), // parameters
            Collections.<ExpressionTree>emptyList(),  // throws 
            make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
            null // default value - not applicable here, used by annotations
        );
        return newMethod;
    }
    
    // #105959
    public void testRenameConstantCheckJavadoc() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
                "/*\n" +
                " * RequestType.java\n" +
                " *\n" +
                " * Created on January 6, 2007, 7:11 PM\n" +
                " */\n" +
                "\n" +
                "package quantum.protocol.version0;\n" +
                "\n" +
                "/**\n" +
                " * Protocol request types.\n" +
                " *\n" +
                " * @author Gili Tzabari\n" +
                " */\n" +
                "public enum RequestType\n" +
                "{\n" +
                "  /**\n" +
                "   * Create a new connection.\n" +
                "   */\n" +
                "  NEW_SESSION,\n" +
                "  /**\n" +
                "   * Return the id of a class.\n" +
                "   */\n" +
                "  GET_CLASS_ID,\n" +
                "  /**\n" +
                "   * Return the id of an object.\n" +
                "   */\n" +
                "  GET_OBJECT_ID,\n" +
                "  /**\n" +
                "   * Return the id of a method.\n" +
                "   */\n" +
                "  GET_METHOD_ID,\n" +
                "  /**\n" +
                "   * Invoke a method.\n" +
                "   */\n" +
                "  INVOKE_METHOD,\n" +
                "  /**\n" +
                "   * Retrieves a remote resource.\n" +
                "   */\n" +
                "  GET_RESOURCE;\n" +
                "}"
            );
        String golden =
                "/*\n" +
                " * RequestType.java\n" +
                " *\n" +
                " * Created on January 6, 2007, 7:11 PM\n" +
                " */\n" +
                "\n" +
                "package quantum.protocol.version0;\n" +
                "\n" +
                "/**\n" +
                " * Protocol request types.\n" +
                " *\n" +
                " * @author Gili Tzabari\n" +
                " */\n" +
                "public enum RequestType\n" +
                "{\n" +
                "  /**\n" +
                "   * Create a new connection.\n" +
                "   */\n" +
                "  NEW_SESSION_2,\n" +
                "  /**\n" +
                "   * Return the id of a class.\n" +
                "   */\n" +
                "  GET_CLASS_ID,\n" +
                "  /**\n" +
                "   * Return the id of an object.\n" +
                "   */\n" +
                "  GET_OBJECT_ID,\n" +
                "  /**\n" +
                "   * Return the id of a method.\n" +
                "   */\n" +
                "  GET_METHOD_ID,\n" +
                "  /**\n" +
                "   * Invoke a method.\n" +
                "   */\n" +
                "  INVOKE_METHOD,\n" +
                "  /**\n" +
                "   * Retrieves a remote resource.\n" +
                "   */\n" +
                "  GET_RESOURCE;\n" +
                "}";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "NEW_SESSION_2"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Test adds the method at the end of the class, demonstrates #104839
     * 
     * Original:
     * 
     * <code>
     * public enum Test {
     *     A(1), B(2), C(3);
     * 
     *     public Test(int i) {
     *     }
     * }
     * </code>
     * 
     * Expected result:
     * 
     * <code>
     * public enum Test {
     *     A(1), B2(2), C(3);
     *    
     *     public Test(int i) {
     *     }
     * }
     * </code>
     */
    public void testRenameWithInit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A(1), B(2), C(3);\n" +
            "\n" +
            "    public Test(int i) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A(1), B2(2), C(3);\n" +
            "\n" +
            "    public Test(int i) {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "B2"));
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
