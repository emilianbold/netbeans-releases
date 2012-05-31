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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.modules.java.hints.spiimpl.TestUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * TODO: mostly tested indirectly through IntroduceHintTest, should be rather
 * tested here
 *
 * @author lahvac
 */
public class FlowTest extends NbTestCase {

    public FlowTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil
                .prepareTest(new String[0], new Object[0]);
        super
                .setUp();
    }

    public void testSimple() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        if (i == 0) ii = 3;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary1() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = i == 0 && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = true && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testBinary3() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = false && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2");
    }

    public void testBinary4() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = i == 0 || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary5() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = false || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testBinary6() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = true || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2");
    }

    public void test197666() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        boolean b = i == 1 && true;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1");
    }

    public void test198233() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        boolean b = i == 1 && true;\n" +
                    "        System.err.println(i`i);\n" +
                    "        ===\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "1");
    }

    public void testIncorrectDeadBranch() throws Exception {
        performDeadBranchTest("package test;\n" +
                              "public class Test {\n" +
                              "    public void i() {\n" +
                              "        if (!i.getAndSet(true)) {\n" +
                              "            System.err.println(\"\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "    private final java.util.concurrent.atomic.AtomicBoolean i = new java.util.concurrent.atomic.AtomicBoolean();\n" +
                              "}\n");
    }

    public void testTryCatch() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } catch (Exception e) {\n" +
                    "            ii = 2;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "2");
    }

    public void testTryCatchFinally() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } catch (Exception e) {\n" +
                    "            ii = 2;\n" +
                    "        } finally {\n" +
                    "            ii = 3;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testTryFinally() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } finally {\n" +
                    "            ii = 3;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testTryFinally2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 0;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } catch (Exception e) {\n" +
                    "            ii = 2;\n" +
                    "        } finally {\n" +
                    "            System.err.println(i`i);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1",
                    "2");
    }

    public void testSwitch1() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p, int r) {\n" +
                    "        int ii;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; break;\n" +
                    "            case 1: if (r > 5) {\n" +
                    "                         ii = 5;\n" +
                    "                         break;\n" +
                    "                    }\n" +
                    "                    ii = 2;\n" +
                    "            case 2: ii = 3; break;\n" +
                    "            default: ii = 4; break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "5",
                    "3",
                    "4");
    }

    public void testSwitch2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p) {\n" +
                    "        int ii;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; break;\n" +
                    "            case 1: ii = 2;\n" +
                    "            case 2: ii = 3; return;\n" +
                    "            default: ii = 4; break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "4");
    }

    public void testSwitch3() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p) {\n" +
                    "        int ii = 0;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void testSwitch4() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(String str) {\n" +
                    "        final int mm = 1;\n" +
                    "        int b = 0;\n" +
                    "        switch (str.length()) {\n" +
                    "            case 0: break;\n" +
                    "            case 1: b |= m`m; break;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "1");
    }

    public void testForUpdate() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        for (int ii = 0; ii < 100; ii = ii + 1) {\n" +
                    "            System.err.println(i`i);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "ii + 1");
    }

    public void testForEach() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(String... args) {\n" +
                    "        boolean ff = true;\n" +
                    "        for (String a : args) {\n" +
                    "            if (!f`f) System.err.println(1);\n" +
                    "            ff = false;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "true",
                    "false");
    }

    public void testAnonymous() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        final String model = \"\";\n" +
                    "        java.util.Collections.sort(java.util.Collections.emptyList(), new java.util.Comparator<Object>() {\n" +
                    "            public int compare(Object o1, Object o2) {\n" +
                    "                return 0;\n" +
                    "            }\n" +
                    "        });\n" +
                    "        System.err.println(mod`el);\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"");
    }

    public void test198975() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii + +=;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "1");
    }

    public void test199335() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        List<Object> ll = null;\n" +
                    "        for (Object str : ll) {\n" +
                    "            if (str instanceof String) {\n" +
                    "                System.err.println(st`r);\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "<null>");
    }

    public void testAssert() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        boolean bb = false;\n" +
                    "        assert bb = true;\n" +
                    "        System.err.println(b`b);\n" +
                    "    }\n" +
                    "}\n",
                    false,
                    "false",
                    "true");
    }

    public void testTryFinallyAndReturn() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        boolean bb = false;\n" +
                    "        try {\n" +
                    "            bb = true;\n" +
                    "            return ;\n" +
                    "        } finally {\n" +
                    "            System.err.println(b`b);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }

    public void testWhileWriteInCondition() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        while (i`i-- > 0);\n" +
                    "        System.err.println(ii);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "ii--");
    }

    public void testForWriteInCondition() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        for (;i`i-- > 0;);\n" +
                    "        System.err.println(ii);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "ii--");
    }

    public void testWhileLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        while (t`p != null) {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testForLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        for (String i = null; i != t`p; ) {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testEnhancedForLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(String[] args) {\n" +
                    "        String tp = \"\";\n" +
                    "        for (String i : args) {\n" +
                    "            if (t`p != null) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testDoWhileLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        do {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        } while (t`p != null);\n" +
                    "    }\n" +
                    "}\n",
                    "null");
    }

    public void testLabeledLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        LOOP: while (t`p != null) {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue LOOP;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testContinue204845() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        if (t`p.length() == 0) {\n" +
                    "            continue ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "\"\"");
    }

    public void test205347a() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void reallyUsed() {\n" +
                    "        boolean again = true;\n" +
                    "        for (;;) {\n" +
                    "            if (ag`ain) {\n" +
                    "                again = false;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            break;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "true",
                    "false");
    }

    public void test205347b() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void reallyUsed() {\n" +
                    "        int ii = 0;\n" +
                    "        for (;; ii++) {\n" +
                    "            if (i`i < 100) {\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            break;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "ii++");
    }

    public void test210520a() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            if (i == 0) throw new Exception();\n" +
                    "            rr = 1;\n" +
                    "            if (i == 1) throw new Exception();\n" +
                    "            rr = 2;\n" +
                    "            if (i == 1) throw new java.io.IOException();\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1",
                    "2");
    }

    public void test210520b() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            t1();\n" +
                    "            rr = 1;\n" +
                    "            t1();\n" +
                    "        } catch (java.io.IOException e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() throws java.io.FileNotFoundException {\n" +
                    "         throw new java.io.FileNotFoundException();\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void test210520c() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            t1();\n" +
                    "            rr = 1;\n" +
                    "            t1();\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() {\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void test210520d() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            new java.io.FileInputStream(\"\");\n" +
                    "            rr = 1;\n" +
                    "            new java.io.FileInputStream(\"\");\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() {\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void test210520e() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            assert i == 2;\n" +
                    "            rr = 1;\n" +
                    "            assert i == 3;\n" +
                    "        } catch (Throwable e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() {\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }
    
    public void test211926a() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(String... args) {\n" +
                    "        boolean empty = false;\n" +
                    "        for (String a : args) {\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void test211926b() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f(Collection<String> args) {\n" +
                    "        boolean empty = false;\n" +
                    "        for (Iterator<String> it = args.iterator(); it.hasNext(); ) {\n" +
                    "            String a = it.next();\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void test211926c() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f(Collection<String> args) {\n" +
                    "        boolean empty = false;\n" +
                    "        Iterator<String> it = args.iterator();\n" +
                    "        while (it.hasNext()) {\n" +
                    "            String a = it.next();\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void test211926d() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f(Collection<String> args) {\n" +
                    "        boolean empty = false;\n" +
                    "        Iterator<String> it = args.iterator();\n" +
                    "        do {\n" +
                    "            String a = it.next();\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        } while (it.hasNext());\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }

    public void testDeadBranch207514() throws Exception {
        performDeadBranchTest("package test;\n" +
                              "public class Test {\n" +
                              "    public void i() {\n" +
                              "        if (false) |{\n" +
                              "            System.err.println(\"\");\n" +
                              "        }|\n" +
                              "    }\n" +
                              "    private final java.util.concurrent.atomic.AtomicBoolean i = new java.util.concurrent.atomic.AtomicBoolean();\n" +
                              "}\n");
    }

    private void prepareTest(String code, boolean allowErrors) throws Exception {
        clearWorkDir();

        FileObject workFO = FileUtil
                .toFileObject(getWorkDir());

        assertNotNull(workFO);

        FileObject sourceRoot = workFO
                .createFolder("src");
        FileObject buildRoot = workFO
                .createFolder("build");
        FileObject cache = workFO
                .createFolder("cache");

        FileObject data = FileUtil
                .createData(sourceRoot, "test/Test.java");

        org.netbeans.api.java.source.TestUtilities
                .copyStringToFile(FileUtil
                .toFile(data), code);

        data
                .refresh();

        SourceUtilsTestUtil
                .prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject
                .find(data);
        EditorCookie ec = od
                .getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec
                .openDocument();

        doc
                .putProperty(Language.class, JavaTokenId
                .language());
        doc
                .putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource
                .forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil
                .getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);

        if (!allowErrors) {
            assertTrue(info
                    .getDiagnostics()
                    .toString(), info
                    .getDiagnostics()
                    .isEmpty());
        }
    }
    private CompilationInfo info;
    private Document doc;

    private void performTest(String code, String... assignments) throws Exception {
        performTest(code, false, assignments);
    }

    private void performTest(String code, boolean allowErrors, String... assignments) throws Exception {
        int[] span = new int[1];

        code = TestUtilities
                .detectOffsets(code, span, "`");

        prepareTest(code, allowErrors);

        FlowResult flow = Flow
                .assignmentsForUse(info, new AtomicBoolean());
        TreePath sel = info
                .getTreeUtilities()
                .pathFor(span[0]);

        Set<String> actual = new HashSet<String>();

        for (TreePath tp : flow
                .getAssignmentsForUse()
                .get(sel
                .getLeaf())) {
            if (tp == null) {
                actual
                        .add("<null>");
            } else {
                actual
                        .add(tp
                        .getLeaf()
                        .toString());
            }
        }

        assertEquals(new HashSet<String>(Arrays
                .asList(assignments)), actual);
    }

    private void performDeadBranchTest(String code) throws Exception {
        List<String> splitted = new LinkedList<String>(Arrays
                .asList(code
                .split(Pattern
                .quote("|"))));
        List<Integer> goldenSpans = new ArrayList<Integer>(splitted
                .size() - 1);
        StringBuilder realCode = new StringBuilder();

        realCode
                .append(splitted
                .remove(0));

        for (String s : splitted) {
            goldenSpans
                    .add(realCode
                    .length());
            realCode
                    .append(s);
        }

        prepareTest(realCode
                .toString(), false);

        FlowResult flow = Flow
                .assignmentsForUse(info, new AtomicBoolean());

        List<Integer> actual = new ArrayList<Integer>(2 * flow
                .getDeadBranches()
                .size());

        for (Tree dead : flow
                .getDeadBranches()) {
            actual
                    .add((int) info
                    .getTrees()
                    .getSourcePositions()
                    .getStartPosition(info
                    .getCompilationUnit(), dead));
            actual
                    .add((int) info
                    .getTrees()
                    .getSourcePositions()
                    .getEndPosition(info
                    .getCompilationUnit(), dead));
        }

        assertEquals(goldenSpans, actual);
    }
}
