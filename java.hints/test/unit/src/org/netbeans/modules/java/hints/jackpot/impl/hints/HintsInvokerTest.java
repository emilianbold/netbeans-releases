/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.hints.jackpot.impl.MessageImpl;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;

/**
 *
 * @author user
 */
public class HintsInvokerTest extends TreeRuleTestBase {

    public HintsInvokerTest(String name) {
        super(name);
    }

//    public static TestSuite suite() {
//        NbTestSuite r = new NbTestSuite();
//        r.addTest(new HintsInvokerTest("testPatternVariable1"));
//        return r;
//    }

    public void testPattern1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "import java.io.File;\n" +
                            "public class Test {\n" +
                            "     private void test(File f) {\n" +
                            "         f.toURL();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:HINT");
    }

    public void testPattern2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private void test(java.io.File f) {\n" +
                            "         f.toURL();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:HINT");
    }

    public void testKind1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private void test(java.io.File f) {\n" +
                            "         f.toURL();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:HINT");
    }

    public void testPatternVariable1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         {\n" +
                       "             int y;\n" +
                       "             y = 1;\n" +
                       "         }\n" +
                       "         int z;\n" +
                       "         {\n" +
                       "             int y;\n" +
                       "             z = 1;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-7:10:verifier:HINT",
                       "FixImpl",
                       "package test; public class Test { private void test() { { int y = 1; } int z; { int y; z = 1; } } } ");
    }

    public void testPatternAssert1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         assert true : \"\";\n" +
                            "     }\n" +
                            "}\n",
                            "4:9-4:15:verifier:HINT");
    }

    public void testPatternStatementAndSingleStatementBlockAreSame() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private int test() {\n" +
                            "         if (true) {\n" +
                            "             return 0;\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "4:9-4:11:verifier:HINT");
    }

    public void testPatternFalseOccurrence() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private int test(java.io.File f) {\n" +
                            "         f.toURI().toURL();\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testStatementVariables1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (true)\n" +
                       "             System.err.println(1);\n" +
                       "         else\n" +
                       "             System.err.println(2);\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:11:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (!true)\n" +
                       "             System.err.println(2);\n" +
                       "         else\n" +
                       "             System.err.println(1);\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testStatementVariables2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (true)\n" +
                       "             return 1;\n" +
                       "         else\n" +
                       "             return 2;\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:11:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (!true)\n" +
                       "             return 2;\n" +
                       "         else\n" +
                       "             return 1;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariables1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         int i = 3;\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n",
                       "3:29-10:6:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         float i = 3;\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariables2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         int i = 3;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n",
                       "3:29-6:6:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         float i = 3;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariables3() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         System.err.println();\n" +
                       "         System.err.println();\n" +
                       "         int i = 3;\n" +
                       "         System.err.println(i);\n" +
                       "         System.err.println(i);\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n",
                       "3:24-10:6:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         System.err.println();\n" +
                       "         System.err.println();\n" +
                       "         float i = 3;\n" +
                       "         System.err.println(i);\n" +
                       "         System.err.println(i);\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariablesAndBlocks() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {" +
                       "         if (true)\n" +
                       "             System.err.println();\n" +
                       "     }\n" +
                       "}\n",
                       "3:35-3:37:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {" +
                       "         if (!true) {\n" +
                       "             System.err.println();\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testOneStatement2MultipleBlock() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:32:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testOneStatement2MultipleStatement() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         if (true)\n" +
                       "             System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "5:13-5:36:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         if (true) {\n" +
                       "             System.err.println(\"\");\n" +
                       "             System.err.println(\"\");\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiple2OneStatement1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "3:25-6:6:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiple2OneStatement2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         int i = 0;\n" +
                       "         System.err.println(\"\");\n" +
                       "         System.err.println(\"\");\n" +
                       "         i++;\n" +
                       "     }\n" +
                       "}\n",
                       "3:25-8:6:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         int i = 0;\n" +
                       "         System.err.println(\"\");\n" +
                       "         i++;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMemberSelectInsideMemberSelect() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     public Test test;\n" +
                       "     public String name;\n" +
                       "     private void test() {\n" +
                       "         Test t = null;\n" +
                       "         String s = t.test.toString();\n" +
                       "     }\n" +
                       "}\n",
                       "7:22-7:26:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     public Test test;\n" +
                       "     public String name;\n" +
                       "     private void test() {\n" +
                       "         Test t = null;\n" +
                       "         String s = t.getTest().toString();\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testPackageInfo() throws Exception {
        performAnalysisTest("test/package-info.java",
                            "|package test;\n");
    }

    public void testSuppressWarnings() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "@SuppressWarnings(\"test\")\n" +
                            "public class Test {\n" +
                            "     public Test test;\n" +
                            "     public String name;\n" +
                            "     private void test() {\n" +
                            "         Test t = null;\n" +
                            "         String s = t.test.toString();\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testRewriteOneToMultipleClassMembers() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int i;\n" +
                       "}\n",
                       "3:17-3:18:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int i;\n" +
                       "     public int getI() {\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testImports1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         new java.util.LinkedList();\n" +
                       "     }" +
                       "}\n",
                       "4:9-4:35:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         new ArrayList();\n" +
                       "     }" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testImports2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         LinkedList l;\n" +
                       "     }" +
                       "}\n",
                       "4:20-4:21:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         ArrayList l;\n" +
                       "     }" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiParameters() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.asList(\"a\", \"b\", \"c\"); }\n" +
                       "}\n",
                       "3:14-3:20:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.asList(\"d\", \"a\", \"b\", \"c\"); }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTypeParametersMethod() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.<String>asList(\"a\", \"b\", \"c\"); }\n" +
                       "}\n",
                       "3:22-3:28:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.<String>asList(\"d\", \"a\", \"b\", \"c\"); }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTypeParametersNewClass() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.Arrays;\n" +
                       "import java.util.HashSet;\n" +
                       "public class Test {\n" +
                       "     { new HashSet<String>(Arrays.<String>asList(\"a\", \"b\", \"c\")); }\n" +
                       "}\n",
                       "4:7-4:64:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.Arrays;\n" +
                       "import java.util.HashSet;\n" +
                       "public class Test {\n" +
                       "     { new HashSet<String>(Arrays.<String>asList(\"d\", \"a\", \"b\", \"c\")); }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testChangeFieldType1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     private String name = null;\n" +
                       "}\n",
                       "2:20-2:24:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     private CharSequence name = null;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testChangeFieldType2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     String name = null;\n" +
                       "}\n",
                       "2:12-2:16:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     CharSequence name = null;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testChangeFieldType3() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     private static final String name = \"test\".substring(0, 4);\n" +
                       "}\n",
                       "2:33-2:37:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     private static final CharSequence name = \"test\".substring(0, 4);\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testIdentifier() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     private int l;" +
                       "     {System.err.println(l);}\n" +
                       "}\n",
                       "2:44-2:45:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     private int l;" +
                       "     {System.err.println(2);}\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    private static final Map<String, HintDescription> test2Hint;

    static {
        test2Hint = new HashMap<String, HintDescription>();
        test2Hint.put("testPattern1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$1.toURL()", Collections.singletonMap("$1", "java.io.File"))).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPattern2", test2Hint.get("testPattern1"));
        test2Hint.put("testKind1", HintDescriptionFactory.create().setTriggerKind(Kind.METHOD_INVOCATION).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPatternVariable1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("{ $1 $2; $2 = $3; }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("{ $1 $2 = $3; }")).produce());
        Map<String, String> constraints = new HashMap<String, String>();

        constraints.put("$1", "boolean");
        constraints.put("$2", "java.lang.Object");

        test2Hint.put("testPatternAssert1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("assert $1 : $2;", constraints)).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPatternStatementAndSingleStatementBlockAreSame", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("if ($1) return $2;", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPatternFalseOccurrence", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$1.toURL()", Collections.singletonMap("$1", "java.io.File"))).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testStatementVariables1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("if ($1) $2; else $3;", constraints)).setWorker(new WorkerImpl("if (!$1) $3; else $2;")).produce());
        test2Hint.put("testStatementVariables2", test2Hint.get("testStatementVariables1"));
        test2Hint.put("testMultiStatementVariables1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("{ $pref$; int $i = 3; $inf$; return $i; }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("{ $pref$; float $i = 3; $inf$; return $i; }")).produce());
        test2Hint.put("testMultiStatementVariables2", test2Hint.get("testMultiStatementVariables1"));
        test2Hint.put("testMultiStatementVariables3", test2Hint.get("testMultiStatementVariables1"));
        test2Hint.put("testMultiStatementVariablesAndBlocks", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("if ($c) {$s1$; System.err.println(); $s2$; }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("if (!$c) {$s1$; System.err.println(); $s2$; }")).produce());
        test2Hint.put("testOneStatement2MultipleBlock", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("System.err.println($1);", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("System.err.println($1); System.err.println($1);")).produce());
        test2Hint.put("testOneStatement2MultipleStatement", test2Hint.get("testOneStatement2MultipleBlock"));
        test2Hint.put("testMultiple2OneStatement1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("System.err.println($1); System.err.println($2);", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("System.err.println($1);")).produce());
        test2Hint.put("testMultiple2OneStatement2", test2Hint.get("testMultiple2OneStatement1"));
        test2Hint.put("testMemberSelectInsideMemberSelect", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$Test.test", Collections.<String, String>singletonMap("$Test", "test.Test"))).setWorker(new WorkerImpl("$Test.getTest()")).produce());
        test2Hint.put("testPackageInfo", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$Test.test", Collections.<String, String>singletonMap("$Test", "test.Test"))).setWorker(new WorkerImpl("$Test.getTest()")).produce());
        HintMetadata metadata = HintMetadata.create("no-id", "", "", "", true, HintMetadata.Kind.HINT_NON_GUI, HintSeverity.WARNING, Collections.singletonList("test"));
        test2Hint.put("testSuppressWarnings", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$Test.test", Collections.<String, String>singletonMap("$Test", "test.Test"))).setWorker(new WorkerImpl("$Test.getTest()")).setMetadata(metadata).produce());
        test2Hint.put("testRewriteOneToMultipleClassMembers", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("private int i;", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("private int i; public int getI() { return i; }")).produce());
        test2Hint.put("testImports1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("new LinkedList()", Collections.<String, String>emptyMap(), "import java.util.LinkedList;")).setWorker(new WorkerImpl("new ArrayList()", "import java.util.ArrayList;\n")).produce());
        test2Hint.put("testImports2", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("LinkedList $0;", Collections.<String, String>emptyMap(), "import java.util.LinkedList;")).setWorker(new WorkerImpl("ArrayList $0;", "import java.util.ArrayList;\n")).produce());
        test2Hint.put("testMultiParameters", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("java.util.Arrays.asList($1$)", Collections.<String,String>emptyMap())).setWorker(new WorkerImpl("java.util.Arrays.asList(\"d\", $1$)")).produce());
        test2Hint.put("testTypeParametersMethod", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("java.util.Arrays.<$T>asList($1$)", Collections.<String,String>emptyMap())).setWorker(new WorkerImpl("java.util.Arrays.<$T>asList(\"d\", $1$)")).produce());
        test2Hint.put("testTypeParametersNewClass", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("new java.util.HashSet<$T1$>(java.util.Arrays.<$T$>asList($1$))", Collections.<String,String>emptyMap())).setWorker(new WorkerImpl("new java.util.HashSet<$T1$>(java.util.Arrays.<$T$>asList(\"d\", $1$))")).produce());
        test2Hint.put("testChangeFieldType1", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$modifiers$ java.lang.String $name = $initializer;", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("$modifiers$ java.lang.CharSequence $name = $initializer;")).produce());
        test2Hint.put("testChangeFieldType2", test2Hint.get("testChangeFieldType1"));
        test2Hint.put("testChangeFieldType3", test2Hint.get("testChangeFieldType1"));
        test2Hint.put("testIdentifier", HintDescriptionFactory.create().setTriggerPattern(PatternDescription.create("$i", Collections.<String, String>singletonMap("$i", "int"))).setWorker(new WorkerImpl("2")).produce());
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        HintDescription hd = test2Hint.get(getName());

        assertNotNull(hd);

        Map<Kind, List<HintDescription>> kind2Hints = new HashMap<Kind, List<HintDescription>>();
        Map<PatternDescription, List<HintDescription>> pattern2Hint = new HashMap<PatternDescription, List<HintDescription>>();
        RulesManager.sortOut(Collections.singletonList(hd), kind2Hints, pattern2Hint);

        return new HintsInvoker(info, new AtomicBoolean()).computeHints(info, new TreePath(info.getCompilationUnit()), kind2Hints, pattern2Hint, new LinkedList<MessageImpl>());
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }

    @Override
    public void testIssue105979() throws Exception {}

    @Override
    public void testIssue108246() throws Exception {}

    @Override
    public void testIssue113933() throws Exception {}

    @Override
    public void testNoHintsForSimpleInitialize() throws Exception {}

    private static final class WorkerImpl implements Worker {

        private final String fix;
        private final Collection<String> imports;

        public WorkerImpl() {
            this(null);
        }

        public WorkerImpl(String fix) {
            this.fix = fix;
            this.imports = Collections.emptyList();
        }

        public WorkerImpl(String fix, String firstImport, String... imports) {
            this.fix = fix;
            this.imports = new LinkedList<String>();
            this.imports.add(firstImport);
            this.imports.addAll(Arrays.asList(imports));
        }

        public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
            if (ctx.getInfo().getTreeUtilities().isSynthetic(ctx.getPath())) {
                return null;
            }

            List<Fix> fixes = new LinkedList<Fix>();

            if (fix != null) {
                fixes.add(JavaFix.rewriteFix(ctx.getInfo(), "Rewrite", ctx.getPath(), fix, ctx.getVariables(), ctx.getMultiVariables(), ctx.getVariableNames(), /*XXX*/Collections.<String, TypeMirror>emptyMap(), imports.toArray(new String[0])));
            }
            
            return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "HINT", fixes.toArray(new Fix[0])));
        }
    }

}