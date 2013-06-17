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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class RenameTest extends RefactoringTestBase {

    public RenameTest(String name) {
        super(name);
    }
    
    public void testRenameCasePackage() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        performRenameFolder(src.getFileObject("t"), "T");
        verifyContent(src,
                new File("T/A.java", "package T;\n"
                + "public class A {\n"
                + "}"));
    }
    
    public void test218766() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
        performRenameFolder(src.getFileObject("t"), "u");
        verifyContent(src,
                new File("u/A.java", "package u;\n"
                + "public class A {\n"
                + "}"));
        verifyContent(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
    }

    public void testRenameProp() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));

    }
    
    public void testRenamePropJavaDoc() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    /**\n"
                + "     * Update the value of property.\n"
                + "     * @param property the new value of property\n"
                + "     */\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, "renamed", props, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    /**\n"
                + "     * Update the value of property.\n"
                + "     * @param renamed the new value of property\n"
                + "     */\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));
    }
    
    public void testRenamePropJavaDoc2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    /**\n"
                + "     * Update the value of property.\n"
                + "     * @param property the new value of property\n"
                + "     */\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    /**\n"
                + "     * Update the value of renamed.\n"
                + "     * @param renamed the new value of renamed\n"
                + "     */\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));
    }

    public void testRenamePropUndoRedo() throws Exception { // #220547
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameGettersSetters(true);
        performRename(src.getFileObject("t/A.java"), 1, "renamed", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));
        UndoManager undoManager = UndoManager.getDefault();
        undoManager.setAutoConfirm(true);
        undoManager.undo(null);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int property;\n"
                + "    public void setProperty(int property) {\n"
                + "        this.property = property;\n"
                + "    }\n"
                + "    public int getProperty() {\n"
                + "        return property;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setProperty(1);\n"
                + "        return a.getProperty();\n"
                + "    }\n"
                + "}"));
        undoManager.redo(null);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int renamed;\n"
                + "    public void setRenamed(int renamed) {\n"
                + "        this.renamed = renamed;\n"
                + "    }\n"
                + "    public int getRenamed() {\n"
                + "        return renamed;\n"
                + "    }\n"
                + "    public int foo() {\n"
                + "        A a = new A();\n"
                + "        a.setRenamed(1);\n"
                + "        return a.getRenamed();\n"
                + "    }\n"
                + "}"));

    }
    
    public void test200224() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameTestClass(true);
        performRename(src.getFileObject("t/A.java"), -1, "B", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n" // XXX: Why use old filename, is it not renamed?
                + "public class B {\n"
                + "}"));
        verifyContent(test,
                new File("t/BTest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class BTest extends TestCase {\n"
                + "}"));
    }

    public void test62897() throws Exception { // #62897 rename class method renames test method as well
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "    public void testFoo() {\n"
                + "    }\n"
                + "}"));
        JavaRenameProperties props = new JavaRenameProperties();
        props.setIsRenameTestClassMethod(true);
        performRename(src.getFileObject("t/A.java"), 1, "fooBar", props, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n" // XXX: Why use old filename, is it not renamed?
                + "public class A {\n"
                + "    public void fooBar() {\n"
                + "    }\n"
                + "}"));
        verifyContent(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "    public void testFooBar() {\n"
                + "    }\n"
                + "}"));
    }
    
    public void test111953() throws Exception {
        writeFilesAndWaitForScan(src, new File("t/B.java", "class B { public void m(){};}"),
                new File("t/A.java", "class A extends B implements I{ public void m(){};}"),
                new File("t/I.java", "interface I { void m();}"),
                new File("t/J.java", "interface J { void m();}"),
                new File("t/C.java", "class C extends D implements I, J{ public void m(){};}"),
                new File("t/D.java", "class D { public void m(){};}"));
        performRename(src.getFileObject("t/B.java"), 1, "k", null, true, new Problem(false, "ERR_IsOverridden"), new Problem(false, "ERR_IsOverriddenOverrides"));
        verifyContent(src, new File("t/B.java", "class B { public void k(){};}"),
                new File("t/A.java", "class A extends B implements I{ public void k(){};}"),
                new File("t/I.java", "interface I { void m();}"),
                new File("t/J.java", "interface J { void m();}"),
                new File("t/C.java", "class C extends D implements I, J{ public void m(){};}"),
                new File("t/D.java", "class D { public void m(){};}"));
    }
    
    public void test195070() throws Exception { // #195070 - refactor/rename works wrong with override
        writeFilesAndWaitForScan(src, new File("t/A.java", "class A { public void bindSuper(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ bindSuper();}}"));
        performRename(src.getFileObject("t/A.java"), 1, "bind", null, true);
        verifyContent(src, new File("t/A.java", "class A { public void bind(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ super.bind();}}"));
        
        writeFilesAndWaitForScan(src, new File("t/A.java", "class A { public void bindSuper(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ bindSuper();}}"));
        performRename(src.getFileObject("t/A.java"), 1, "binding", null, true);
        verifyContent(src, new File("t/A.java", "class A { public void binding(){}}"),
                new File("t/B.java", "class B extends A { public void bind(){ binding();}}"));
    }
    
    public void test215139() throws Exception { // #215139 - [Rename] Method and Field rename incorrectly adds Type.super 
        writeFilesAndWaitForScan(src, new File("t/A.java", "class A { public void bindSuper(){}}"),
                new File("t/B.java", "class B { private A a = new A(); public void bind(){ a.bindSuper();}}"));
        performRename(src.getFileObject("t/A.java"), 1, "bind", null, true);
        verifyContent(src, new File("t/A.java", "class A { public void bind(){}}"),
                new File("t/B.java", "class B { private A a = new A(); public void bind(){ a.bind();}}"));
    }
    
    public void test202251() throws Exception { // #202251 - Refactoring code might lead to uncompilable code
        writeFilesAndWaitForScan(src, new File("test/Tool.java", "package test;\n"
                + "\n"
                + "import java.util.StringTokenizer;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>\n"
                + " */\n"
                + "public class Tool {\n"
                + "\n"
                + "    private Tool() {\n"
                + "    }\n"
                + "\n"
                + "    public static boolean compareNumberStrings(String first, String second) {\n"
                + "        return conpareNumberStrings(first, second, \".\");\n"
                + "    }\n"
                + "\n"
                + "    public static boolean conpareNumberStrings(String first, String second,\n"
                + "            String separator) {\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("test/Tool.java"), 2, "compareNumberStrings", null, true);
        verifyContent(src, new File("test/Tool.java", "package test;\n"
                + "\n"
                + "import java.util.StringTokenizer;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>\n"
                + " */\n"
                + "public class Tool {\n"
                + "\n"
                + "    private Tool() {\n"
                + "    }\n"
                + "\n"
                + "    public static boolean compareNumberStrings(String first, String second) {\n"
                + "        return compareNumberStrings(first, second, \".\");\n"
                + "    }\n"
                + "\n"
                + "    public static boolean compareNumberStrings(String first, String second,\n"
                + "            String separator) {\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test104819() throws Exception{ // #104819 [Rename] Cannot rename inner class to same name as class in same package
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "    public static class C {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "import t.A.C;"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 2, "B", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return B.c;\n"
                + "    }\n"
                + "    public static class B {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.B.c;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test200985() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static int a;\n"
                + "    static void m(int b){\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, "b", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static int b;\n"
                + "    static void m(int b){\n"
                + "        System.out.println(A.b);\n"
                + "    }\n"
                + "}"));
    }
    
    public void test200987() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int a;\n"
                + "}\n"
                + "class B extends A {\n"
                + "    void m(int b){\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, "b", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int b;\n"
                + "}\n"
                + "class B extends A {\n"
                + "    void m(int b){\n"
                + "        System.out.println(this.b);\n"
                + "    }\n"
                + "}"));
    }
    
    public void test202675() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("a/A.java", "package a;\n"
                + "import b.B;\n"
                + "public class A {\n"
                + "    B b;\n"
                + "}"),
                new File("b/B.java", "package b;\n"
                + "public class B {\n"
                + "}"));
        
        RefactoringSession rs = RefactoringSession.create("Rename");
        RenameRefactoring rr = new RenameRefactoring(Lookups.singleton(src.getFileObject("b/B.java")));
        rr.setNewName("C");
        rr.setSearchInComments(true);
        rr.prepare(rs);
        rs.doRefactoring(true);

        verifyContent(src,
                new File("a/A.java", "package a;\n"
                + "import b.C;\n"
                + "public class A {\n"
                + "    C b;\n"
                + "}"),
                new File("b/C.java", "package b;\n"
                + "public class C {\n"
                + "}"));
    }
    
    public void test104819_2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "    public static class C {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.C.c;\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 2, "B", null, true);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int foo() {\n"
                + "        return B.c;\n"
                + "    }\n"
                + "    public static class B {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.B.c;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test201610() throws Exception { // #201610 [rename class] introduces behavioral change
        writeFilesAndWaitForScan(src, new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class B extends A {\n"
                + "  public long k(){\n"
                + "    return 0;\n"
                + "  }\n"
                + "}"),
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends A {\n"
                + "  public long m(){\n"
                + "    return new B().k();\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "  protected long k(){\n"
                + "    return 1;\n"
                + "  }\n"
                + "}"));
        performRename(src.getFileObject("p1/B.java"), -1, "C", null, true);
        verifyContent(src, new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class C extends A {\n"
                + "  public long k(){\n"
                + "    return 0;\n"
                + "  }\n"
                + "}"),
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends A {\n"
                + "  public long m(){\n"
                + "    return new p1.C().k();\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "  protected long k(){\n"
                + "    return 1;\n"
                + "  }\n"
                + "}"));
    }
    
    public void test201608() throws Exception { // #201608 [rename class] introduces compilation error: Cycle detected: the type cannot extend/implement itself or one of its own member types
        writeFilesAndWaitForScan(src,
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends B {\n"
                + "}"),
                new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class B extends A {\n"
                + "  long k(  long a){\n"
                + "    return 1;\n"
                + "  }\n"
                + "  protected long k(  int a){\n"
                + "    return 0;\n"
                + "  }\n"
                + "  public long m(){\n"
                + "    return new B().k(2);\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "}"));
        performRename(src.getFileObject("p1/B.java"), -1, "C", null, true);
        verifyContent(src,
                new File("p2/C.java", "package p2;\n"
                + "import p1.*;\n"
                + "public class C extends p1.C {\n"
                + "}"),
                new File("p1/B.java", "package p1;\n"
                + "import p2.*;\n"
                + "public class C extends A {\n"
                + "  long k(  long a){\n"
                + "    return 1;\n"
                + "  }\n"
                + "  protected long k(  int a){\n"
                + "    return 0;\n"
                + "  }\n"
                + "  public long m(){\n"
                + "    return new C().k(2);\n"
                + "  }\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "public class A {\n"
                + "}"));
    }
    
    public void testJavadocClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("p2/C.java", "package p2;\n"
                + "public class C {\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "/**\n"
                + " * @see C\n"
                + " */\n"
                + "public class A {\n"
                + "    private C b;\n"
                + "}"));
        performRename(src.getFileObject("p2/C.java"), -1, "B", null, false);
        verifyContent(src,
                new File("p2/C.java", "package p2;\n"
                + "public class B {\n"
                + "}"),
                new File("p2/A.java", "package p2;\n"
                + "/**\n"
                + " * @see B\n"
                + " */\n"
                + "public class A {\n"
                + "    private B b;\n"
                + "}"));
    }
    
    public void testJavadocMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "    \n"
                + "    /**\n"
                + "     * @see #foo() we just call method foo()\n"
                + "     */\n"
                + "    public static void main() {\n"
                + "        new A().foo();\n"
                + "    }\n"
                + "}"));
        performRename(src.getFileObject("t/A.java"), 1, "fooBar", null, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void fooBar() {\n"
                + "    }\n"
                + "    \n"
                + "    /**\n"
                + "     * @see #fooBar() we just call method foo()\n"
                + "     */\n"
                + "    public static void main() {\n"
                + "        new A().fooBar();\n"
                + "    }\n"
                + "}"));
    }
    
    public void testComments() throws Exception{
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /**\n"
                + "     * @see A.C\n"
                + "     */\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "    public static class C {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "import t.A.C;"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return C.c;\n"
                + "    }\n"
                + "}"));

        performRename(src.getFileObject("t/A.java"), 2, "B", null, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /**\n"
                + "     * @see A.B\n"
                + "     */\n"
                + "    public int foo() {\n"
                + "        return B.c;\n"
                + "    }\n"
                + "    public static class B {\n"
                + "        public static int c = 5;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public int foo() {\n"
                + "        return A.B.c;\n"
                + "    }\n"
                + "}"));
    }

    private void performRename(FileObject source, final int position, final String newname, final JavaRenameProperties props, final boolean searchInComments, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                Tree method = cut.getTypeDecls().get(0);
                if (position >= 0) {
                    method = ((ClassTree) method).getMembers().get(position);
                }

                TreePath tp = TreePath.getPath(cut, method);
                r[0] = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(tp, javac)));
                r[0].setNewName(newname);
                r[0].setSearchInComments(searchInComments);
                if(props != null) {
                    r[0].getContext().add(props);
                }
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Rename");
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
    
    private void performRenameFolder(FileObject source, final String newname, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        r[0] = new RenameRefactoring(Lookups.singleton(source));
        r[0].setNewName(newname);
        RefactoringSession rs = RefactoringSession.create("Rename");
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
