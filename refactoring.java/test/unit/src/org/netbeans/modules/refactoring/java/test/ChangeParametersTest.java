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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;

/**
 *
 * @author Ralph Ruijs
 */
public class ChangeParametersTest extends RefactoringTestBase {

    public ChangeParametersTest(String name) {
        super(name);
    }
    
    public void test199738() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param x the value of x\n"
                + "     * @param y the value of y\n"
                + "     */\n"
                + "    public void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2, 1);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t; public class B extends A {\n"
                + "    public void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = new ParameterInfo[] {new ParameterInfo(1, "y", "int", null), new ParameterInfo(0, "x", "int", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.UPDATE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * \n"
                + "     * @param y the value of y\n"
                + "     * @param x the value of x\n"
                + "     */\n"
                + "    public void testMethod(int y, int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(1, 2);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t; public class B extends A {\n"
                + "    public void testMethod(int y, int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testAddParameter() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2, 1);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testCompatible() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, true);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         testMethod(x, 1);\n"
                + "    }\n"
                + "\n"
                + "    public static void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testConstructor() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public A(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 0, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public A(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A(2, 1);\n"
                + "    }\n"
                + "}\n"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public A(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A(2);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(0, "x", "String", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 0, false, new Problem(false, "WRN_isNotAssignable"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public A(String x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A(2);\n"
                + "    }\n"
                + "}\n"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public A(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public A(int x, int z) {\n"
                + "         System.out.println(x + z);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 0, false, new Problem(false, "ERR_existingConstructor"));
    }
    
    public void test114328() throws Exception { // [Change parameters] Check if method with the same signature already exists
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void testMethod(int x, int z) {\n"
                + "         System.out.println(x + z);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "ERR_existingMethod"));
    }
    
    public void test114328_2() throws Exception { // [Change parameters] Check if method with the same signature already exists
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A extends B{\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t; public class B {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         System.out.println(x + z);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "ERR_existingMethod"));
    }
    
    public void test114321() throws Exception { // [Change parameters] Check if method is accessible when modifier is changed
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null)};
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t; public class B {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         A a = new A();\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"));
        performChangeParameters(EnumSet.of(Modifier.PRIVATE), null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "ERR_StrongAccMod"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "    public void secondMethod(int x, int z) {\n"
                + "         A a = new A();\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"));
        performChangeParameters(EnumSet.of(Modifier.PRIVATE), null, null, paramTable, Javadoc.NONE, 1, false);

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"),
                new File("y/B.java", "package y; public class B {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         t.A a = new t.A();\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"));
        performChangeParameters(EnumSet.noneOf(Modifier.class), null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "ERR_StrongAccMod"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t; public class B {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         t.A a = new t.A();\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"));
        performChangeParameters(EnumSet.noneOf(Modifier.class), null, null, paramTable, Javadoc.NONE, 1, false);
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"),
                new File("y/B.java", "package y; public class B {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         t.A a = new t.A();\n"
                + "         a.testMethod(x + z);\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"),
                new File("y/C.java", "package y; public class C {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         t.A a = new t.A();\n"
                + "         a.testMethod(x + z);\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"));
        performChangeParameters(EnumSet.of(Modifier.PROTECTED), null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "ERR_StrongAccMod"), new Problem(false, "ERR_StrongAccMod"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"),
                new File("y/B.java", "package y; public class B extends t.A {\n"
                + "    public void testMethod(int x, int z) {\n"
                + "         t.A a = new t.A();\n"
                + "         a.testMethod(x + z);\n"
                + "         a.testMethod(x + z);\n"
                + "    }\n"
                + "}\n"));
        performChangeParameters(EnumSet.of(Modifier.PROTECTED), null, null, paramTable, Javadoc.NONE, 1, false);
    }

    public void test194592() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(-1, "x", "String", "\"\"")};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(String x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(\"\");\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test56114() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(-1, "x", "String", "\"\"")};
        performChangeParameters(null, "changedMethod", "void", paramTable, Javadoc.NONE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void changedMethod(String x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        changedMethod(\"\");\n"
                + "    }\n"
                + "}\n"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(-1, "x", "String", "\"\"")};
        performChangeParameters(null, "testMethod", "String", paramTable, Javadoc.NONE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static String testMethod(String x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(\"\");\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test54688() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "y", "int", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int y) {\n"
                + "         System.out.println(y);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(0, "34s", "int", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false, new Problem(true, "ERR_InvalidIdentifier"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "         System.out.println(y);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2, 3);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(0, "y", "int", null), new ParameterInfo(1, "x", "int", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false, new Problem(true, "ERR_NameAlreadyUsed"), new Problem(true, "ERR_NameAlreadyUsed"));
    }
    
    public void test53147() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "int", null), new ParameterInfo(-1, "y", "int", "1")};
        performChangeParameters(null, null, null, paramTable, Javadoc.GENERATE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param x the value of x\n"
                + "     * @param y the value of y\n"
                + "     */\n"
                + "    public static void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2, 1);\n"
                + "    }\n"
                + "}\n"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param x the value of x\n"
                + "     * @param y the value of y\n"
                + "     */\n"
                + "    public static void testMethod(int x, int y) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2, 1);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(1, "y", "int", null), new ParameterInfo(0, "x", "int", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.UPDATE, 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * \n"
                + "     * @param y the value of y\n"
                + "     * @param x the value of x\n"
                + "     */\n"
                + "    public static void testMethod(int y, int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(1, 2);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test83483() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        ParameterInfo[] paramTable = {new ParameterInfo(0, "x", "String", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "WRN_isNotAssignable"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(String x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
        paramTable = new ParameterInfo[] {new ParameterInfo(0, "x", "Strings", null)};
        performChangeParameters(null, null, null, paramTable, Javadoc.NONE, 1, false, new Problem(false, "WRN_canNotResolve"), new Problem(false, "WRN_isNotAssignable"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(Strings x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
    }

    private void performChangeParameters(final Set<Modifier> modifiers, String methodName, String returnType, ParameterInfo[] paramTable, final Javadoc javadoc, final int position, final boolean compatible, Problem... expectedProblems) throws Exception {
        final ChangeParametersRefactoring[] r = new ChangeParametersRefactoring[1];

        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                MethodTree testMethod = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(position);
                TreePath tp = TreePath.getPath(cut, testMethod);
                r[0] = new ChangeParametersRefactoring(TreePathHandle.create(tp, javac));

                Set<Modifier> modifierSet = modifiers;
                if(modifiers == null) {
                    modifierSet = new HashSet<Modifier>(testMethod.getModifiers().getFlags());
                }
                r[0].setModifiers(modifierSet);
                r[0].setOverloadMethod(compatible);
                r[0].getContext().add(javadoc);
            }
        }, true);

        r[0].setParameterInfo(paramTable);
        r[0].setMethodName(methodName);
        r[0].setReturnType(returnType);

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
    
    private boolean problemIsFatal(List<Problem> problems) {
        for (Problem problem : problems) {
            if (problem.isFatal()) {
                return true;
            }
        }
        return false;
    }
}
