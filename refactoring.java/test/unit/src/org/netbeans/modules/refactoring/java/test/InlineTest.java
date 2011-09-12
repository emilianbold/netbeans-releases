/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.InlineRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineTest extends RefactoringTestBase {

    public InlineTest(String name) {
        super(name);
    }

    public void testInlineTemp() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String text = \"text\";\n"
                + "        System.out.println(text);\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(\"text\");\n"
                + "    }\n"
                + "}"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, b = 2, c = 6;\n"
                + "        System.out.println(b);\n"
                + "    }\n"
                + "}"));

        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, c = 6;\n"
                + "        System.out.println(2);\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, b = 2, c = 6;\n"
                + "        System.out.println(c);\n"
                + "    }\n"
                + "}"));

        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, b = 2;\n"
                + "        System.out.println(6);\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 + 2;\n"
                + "        System.out.println(1 + a * 3);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 + (1 + 2) * 3);\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 + 2;\n"
                + "        System.out.println(2 * a + 3);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(2 * (1 + 2) + 3);\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 + 2;\n"
                + "        System.out.println(3 - a);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(3 - (1 + 2));\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 - 2;\n"
                + "        System.out.println(3 - a);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(3 - (1 - 2));\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String a = 3 + \"euro\";\n"
                + "        System.out.println(9 + a);\n"
                + "    }\n"
                + "}"));

        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(9 + (3 + \"euro\"));\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String a = getString();\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "    public String getString() {\n"
                + "        return \"text\";\n"
                + "    }\n"
                + "}"));

        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(getString());\n"
                + "    }\n"
                + "    public String getString() {\n"
                + "        return \"text\";\n"
                + "    }\n"
                + "}"));
    }

    public void testCannotInline() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        Object[] x = {\"a\", \"b\"};\n"
                + "        System.out.println(x);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineNotCompoundArrayInit"), new Problem(false, "WRN_InlineChange"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        Object[] x = null;\n"
                + "        System.out.println(x.length);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineNullVarInitializer"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int i;\n"
                + "        if(true)"
                + "            i = 3;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineNoVarInitializer"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int i = 0;\n"
                + "        i = 3;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineAssignedOnce"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int i = 0;\n"
                + "    }\n"
                + "}"));
        r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(false, "WRN_InlineNotUsed"));
    }
    
    public void testInlineConstant() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int a = 10 + 20;\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - a);\n"
                + "    }\n"
                + "}"));

        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineConstantRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static final int a = 10 + 20;\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - a);\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - A.a);\n"
                + "    }\n"
                + "}"));

        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineConstantRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodCasualDiffProblemMaybe() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public static void main(String[] args) {\n"
                + "        printGreeting();\n"
                + "        printGreeting();\n"
                + "        printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private boolean moreThanFiveDeliveries() {\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        return numberOfLateDeliveries > 5;"
                + "    }\n"
                + "    public int getRating() {\n"
                + "        return (moreThanFiveDeliveries()) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int getRating() {\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        return (numberOfLateDeliveries > 5) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"));
    }
    
    public void testInlineMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting() {\n"
                + "        return \"Hello World!\";\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        getInstance().getGreeting().toString();\n"
                + "    }\n"
                + "    private A getInstance() {\n"
                + "        return new A();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void testMethod() {\n"
                + "        \"Hello World!\".toString();\n"
                + "    }\n"
                + "    private A getInstance() {\n"
                + "        return new A();\n"
                + "    }\n"
                + "}"));
  }
    public void testInlineMethodMultipleFiles() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }
    public void testInlineMethodMultipleFilesAccessor() throws Exception {
        writeFilesAndWaitForScan(src,
        new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static String message = \"Hello World!\";\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodLocalAccessors"));
    }
    
    public void testInlineMethodParameters() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        String message = \"Hello \" + name + \"!\";"
                + "        return name + \": \" + message;\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void testMethod() {\n"
                + "        String message = \"Hello \" + \"World\" + \"!\";"
                + "        System.out.println(\"World\" + \": \" + message);\n"
                + "    }\n"
                + "}"));
    }
    
    public void testCannotInlineMethodUsedOnce() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        printGreet();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(false, "WRN_InlineNotUsed"));
    }
    
    public void testCannotInlineMethodVoidReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "        return\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodVoidReturn"));
    }
    public void testCannotInlineMethodRecursion() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int fact(int n) {\n"
                + "        int result;\n"
                + "        if (n == 1) {\n"
                + "            result = 1;\n"
                + "        else\n"
                + "            result = fact(n - 1) * n;\n"
                + "        return result;\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(\"Factorial of 3 is: \" + fact(3));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodRecursion"));
    }
    public void testCannotInlineMethodMultipleReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        if(name.length() > 3)\n"
                + "            return name;\n"
                + "        else\n"
                + "            return name + \"...\";\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodMultipleReturn"));
    }
    public void testCannotInlineMethodNoLastReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        throw new UnsupportedOperationException(\"Not yet implemented\");\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodNoLastReturn"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        try {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "        finally {\n"
                + "            return \"Hello Finally\";\n"
                + "        }\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2, new Problem(true, "ERR_InlineMethodNoLastReturn"));
    }
    public void testCannotInlineMethodNoAccessors() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(C.message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("v/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "    static String message = \"Hello World!\";\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodNoAccessors"));
    }
    public void testCannotInlineMethodPolymorphic() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A extends B {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello\");"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodPolymorphic"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B extends A {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello\");"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2, new Problem(true, "ERR_InlineMethodPolymorphic"));
    }
    
    public void testCannotInlineMethodNameClash() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A extends B {\n"
                + "    public void printGreeting() {\n"
                + "        String message = \"Hello World!\";\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        String message = \"Hello\";\n"
                + "        printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodNameClash"));
    }
    
    public void test198821() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A implements B{\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public interface B {\n"
                + "    public void printGreeting();\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodPolymorphic"));
    }
    
    public void test199068() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 3;\n"
                + "        switch(a) {\n"
                + "            case 1:\n"
                + "                int b = 5;\n"
                + "                System.out.println(b);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree testMethod = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(1);
                SwitchTree switchTree = (SwitchTree) testMethod.getBody().getStatements().get(1);
                CaseTree case1 = switchTree.getCases().get(0);
                VariableTree variable = (VariableTree) case1.getStatements().get(0);

                TreePath tp = TreePath.getPath(cut, variable);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.TEMP);
            }
        }, true);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 3;\n"
                + "        switch(a) {\n"
                + "            case 1:\n"
                + "                System.out.println(5);\n"
                + "    }\n"
                + "}"));
    }

    private void createInlineConstantRefactoring(FileObject source, final int position, final InlineRefactoring[] r) throws IOException, IllegalArgumentException {
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                VariableTree variable = (VariableTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(position);

                TreePath tp = TreePath.getPath(cut, variable);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.CONSTANT);
            }
        }, true);
    }

    private void createInlineTempRefactoring(FileObject source, final int position, final InlineRefactoring[] r) throws IllegalArgumentException, IOException {
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree testMethod = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(1);
                VariableTree variable = (VariableTree) testMethod.getBody().getStatements().get(position);

                TreePath tp = TreePath.getPath(cut, variable);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.TEMP);
            }
        }, true);
    }

    private void createInlineMethodRefactoring(FileObject source, final int position, final InlineRefactoring[] r) throws IllegalArgumentException, IOException {
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree method = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(position);

                TreePath tp = TreePath.getPath(cut, method);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.METHOD);
            }
        }, true);
    }

    private void performRefactoring(final InlineRefactoring[] r, Problem... expectedProblems) {
        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
