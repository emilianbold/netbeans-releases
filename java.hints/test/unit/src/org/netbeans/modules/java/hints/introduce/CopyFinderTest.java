/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import junit.framework.TestSuite;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.hints.introduce.CopyFinder.MethodDuplicateDescription;
import org.netbeans.modules.java.hints.introduce.CopyFinder.VariableAssignments;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.jackpot.impl.pm.Pattern;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class CopyFinderTest extends NbTestCase {

    public CopyFinderTest(String testName) {
        super(testName);
    }

//    public static TestSuite suite() {
//        NbTestSuite nb = new NbTestSuite();
//
//        nb.addTest(new CopyFinderTest("testCorrectSite3"));
//
//        return nb;
//    }
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testSimple1() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
    }

//    public void testSimple2() throws Exception {
//        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
//    }

    public void testSimple3() throws Exception {
        performTest("package test; public class Test {public void test() {int i = System.currentTimeMillis(); y = System.currentTimeMillis();}}", 83 - 22, 109 - 22, 115 - 22, 141 - 22);
    }

    public void testSimple4() throws Exception {
        performTest("package test; import java.util.ArrayList; public class Test {public void test() {Object o = new ArrayList<String>();o = new ArrayList<String>();}}", 114 - 22, 137- 22, 142 - 22, 165 - 22);
    }

    public void testSimple5() throws Exception {
        performTest("package test; public class Test {public void test() {Object o = null; String s = (String) o; s = (String) o; s = (String) null; o = (Object) o;}}", 103 - 22, 113 - 22, 119 - 22, 129 - 22);
    }

    public void testSimple6() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i;} public void test2() {int i = 0; y = i + i; y = i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
    }

    public void testSimple7() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i != 0 ? i + i : i * i; y = i != 0 ? i + i : i * i; y = i != 1 ? i + i : i * i; y = i == 0 ? i + i : i * i; y = i != 0 ? i * i : i * i; y = i != 0 ? i + i : i + i; y = i != 0 ? i + i : i * 1;}}", 90 - 22, 112 - 22, 118 - 22, 140 - 22);
    }

    public void testSimple8() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = -i; y = -i; y = +i; y = +y;}}", 94 - 22, 96 - 22, 102 - 22, 104 - 22);
    }

    public void testSimple9() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = i *= 9; y = i *= 9; y = i /= 9; y = i *= 8; y = y *= 9;}}", 94 - 22, 100 - 22, 106 - 22, 112 - 22);
    }

    public void testSimple10() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = null; int y = i[1]; y = i[1]; y = i[y]; y = i[0];}}", 99 - 22, 103 - 22, 109 - 22, 113 - 22);
    }

    public void testSimple11() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = new int[0]; i = new int[0]; i = new int[1];}}", 85 - 22, 95 - 22, 101 - 22, 111 - 22);
    }

    public void testSimple12() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = new int[1]; i = new int[1]; i = new int[0];}}", 85 - 22, 95 - 22, 101 - 22, 111 - 22);
    }

    public void testSimple13() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = (i); y = (i); y = i;}}", 94 - 22, 97 - 22, 103 - 22, 106 - 22);
    }

    public void testSimple14() throws Exception {
        performTest("package test; public class Test {public void test() {Object o = null; boolean b = o instanceof String; b = o instanceof String; b = o instanceof Object;}}", 104 - 22, 123 - 22, 129 - 22, 148 - 22);
    }

    public void testSimple15() throws Exception {
        performTest("package test; public class Test {private int x = 1; private int y = 1; public void test() {int x = 1; int y = 1;}}", 90 - 22, 91 - 22, 71 - 22, 72 - 22, 121 - 22, 122 - 22, 132 - 22, 133 - 22);
    }

    public void testSimple16() throws Exception {
        performTest("package test; public class Test {public void test(int i) {int y = \"\".length(); test(\"\".length());} }", 88 - 22, 99 - 22, 106 - 22, 117 - 22);
    }

    public void testSimple17() throws Exception {
        performTest("package test; public class Test {public void test2() {int a = test(test(test(1))); a = test(test(test(1))); a = test(test(test(1)));} public int test(int i) {return 0;} }", 94 - 22, 101 - 22, 119 - 22, 126 - 22, 144 - 22, 151 - 22);
    }

    public void testMemberSelectAndIdentifierAreSame() throws Exception {
        performTest("package test; import static java.lang.String.*; public class Test {public void test1() {|String.valueOf(2)|; |valueOf(2)|;} }");
    }

    public void testVariables1() throws Exception {
        performVariablesTest("package test; import static java.lang.String.*; public class Test {public void test1() {String.valueOf(2+4);} }",
                             "java.lang.String.valueOf($1)",
                             new Pair[] {new Pair<String, int[]>("$1", new int[] {134 - 31, 137 - 31})},
                             new Pair[0]);
    }

    public void testAssert1() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; |assert i == 1;| |assert i == 1;|}}");
    }

    public void testReturn1() throws Exception {
        performTest("package test; public class Test {public int test1() {|return 1;|} public int test2() {|return 1;|}}");
    }

    public void testIf1() throws Exception {
        performTest("package test; public class Test {public void test() { int i = 0; int j; |if (i == 0) {j = 1;} else {j = 2;}| |if (i == 0) {j = 1;} else {j = 2;}| } }");
    }

    public void testExpressionStatement1() throws Exception {
        performTest("package test; public class Test {public void test() { int i = 0; |i = 1;| |i = 1;| } }");
    }

    public void testBlock1() throws Exception {
        performTest("package test; public class Test {public void test() { int i = 0; |{i = 1;}| |{i = 1;}| } }");
    }

    public void testSynchronized1() throws Exception {
        performTest("package test; public class Test {public void test() { Object o = null; int i = 0; |synchronized (o) {i = 1;}| |synchronized (o) {i = 1;}| } }");
    }

//    public void testEnhancedForLoop() throws Exception {
//        performTest("package test; public class Test {public void test(Iterable<String> i) { |for (String s : i) { System.err.println(); }| |for (String s : i) { System.err.println(); }| }");
//    }

//    public void testConstants() throws Exception {
//        performTest("package test; public class Test {public static final int A = 3; public void test() { int i = |3|; i = |test.Test.A|; } }");
//    }

    public void testOverridingImplementing1() throws Exception {
        performVariablesTest("package test; public class Test implements Runnable { { this.run(); } public void run() { } } }",
                             "$0{java.lang.Runnable}.run()",
                             new Pair[] {new Pair<String, int[]>("$0", new int[] {56, 60})},
                             new Pair[0]);
    }

    public void testMemberSelectCCE() throws Exception {
        //should not throw a CCE
        //(selected regions are not duplicates)
        performTest("package test; public class Test {public static class T extends Test { public void test() { |Test.test|(); |System.err.println|(); } } }", false);
    }

    public void testLocalVariable() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() { { int y; y = 1; } int z; { int y; z = 1; } } }",
                             "{ int $1; $1 = 1; }",
                             new Pair[0],
                             new Pair[] {new Pair<String, String>("$1", "y")});
    }

    public void testStatementAndSingleBlockStatementAreSame1() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() { { int x; { x = 1; } } } }",
                             "{ int $1; $1 = 1; }",
                             new Pair[0],
                             new Pair[] {new Pair<String, String>("$1", "x")});
    }

    public void testStatementAndSingleBlockStatementAreSame2() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() { { int x; x = 1; } } }",
                             "{ int $1; { $1 = 1; } }",
                             new Pair[0],
                             new Pair[] {new Pair<String, String>("$1", "x")});
    }

    public void testStatementVariables() throws Exception {
        performVariablesTest("package test; public class Test {public int test1() { if (true) return 1; else return 2; } }",
                             "if ($1) $2; else $3;",
                             new Pair[] {
                                  new Pair<String, int[]>("$1", new int[] {89 - 31, 93 - 31}),
                                  new Pair<String, int[]>("$2", new int[] {95 - 31, 104 - 31}),
                                  new Pair<String, int[]>("$3", new int[] {110 - 31, 119 - 31})
                             },
                             new Pair[0]);
    }

    public void testThrowStatement() throws Exception {
        performVariablesTest("package test; public class Test {public void test() { throw new NullPointerException(); throw new IllegalStateException();} }",
                             "throw new NullPointerException()",
                             new Pair[0],
                             new Pair[0]);
    }

    public void testMultiStatementVariables1() throws Exception {
        performVariablesTest("package test; public class Test { public int test1() { System.err.println(); System.err.println(); int i = 3; System.err.println(i); System.err.println(i); return i; } }",
                             "{ $s1$; int $i = 3; $s2$; return $i; }",
                             new Pair[0],
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {55, 76, 77, 98}),
                                  new Pair<String, int[]>("$s2$", new int[] {110, 132, 133, 155})
                             },
                             new Pair[] {new Pair<String, String>("$i", "i")});
    }

    public void testMultiStatementVariables2() throws Exception {
        performVariablesTest("package test; public class Test { public int test1() { int i = 3; return i; } }",
                             "{ $s1$; int $i = 3; $s2$; return $i; }",
                             new Pair[0],
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {}),
                                  new Pair<String, int[]>("$s2$", new int[] {}),
                             },
                             new Pair[] {new Pair<String, String>("$i", "i")});
    }

    public void testMultiStatementVariablesAndBlocks1() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) {$s1$; System.err.println(); $s2$; }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {}),
                                  new Pair<String, int[]>("$s2$", new int[] {}),
                             },
                             new Pair[0]);
    }

    public void testMultiStatementVariablesAndBlocks2() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) {$s1$; System.err.println(); }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {}),
                             },
                             new Pair[0]);
    }

    public void testMultiStatementVariablesAndBlocks3() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) {System.err.println(); $s2$; }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s2$", new int[] {}),
                             },
                             new Pair[0]);
    }

    public void testMultiStatementVariablesAndBlocks4() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) { $s$; }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s$", new int[] {66, 87}),
                             },
                             new Pair[0]);
    }

    public void testVariableVerification() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a, String[] b) { for (int c = 0; c < a.length; c++) { String s = b[c]; System.err.println(s); } } }",
                             "for(int $i = 0; $i < $array.length; $i++) { $T $var = $array[$i]; $stmts$; }",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testFor() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { for (int c = 0; c < a.length; c++) { String s = a[c]; System.err.println(s); } } }",
                             "for(int $i = 0; $i < $array.length; $i++) { $T $var = $array[$i]; $stmts$; }",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {117 - 31, 118 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {134 - 31, 140 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {151 - 31, 173 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$i", "c"),
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testEnhancedFor() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { for (String s : a) { System.err.println(s); } } }",
                             "for($T $var : $array) { $stmts$; }",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {113 - 31, 114 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {102 - 31, 108 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {118 - 31, 140 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testWhile() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { int c = 0; while  (c < a.length) { String s = a[c]; System.err.println(s); c++; } } }",
                             "while ($i < $array.length) { $T $var = $array[$i]; $stmts$; $i++; }",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {120 - 31, 121 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {132 - 31, 138 - 31}),
                                  new Pair<String, int[]>("$i", new int[] {116 - 31, 117 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {149 - 31, 171 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testDoWhile() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { int c = 0; do { String s = a[c]; System.err.println(s); c++; } while  (c < a.length); } }",
                             "do { $T $var = $array[$i]; $stmts$; $i++; } while ($i < $array.length);",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {124 - 31, 125 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {113 - 31, 119 - 31}),
                                  new Pair<String, int[]>("$i", new int[] {126 - 31, 127 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {130 - 31, 152 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testArrayType() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { int[][] a; } }",
                             "$T[]",
                             new Pair[] {
                                  new Pair<String, int[]>("$T", new int[] {87 - 31, /*92*//*XXX:*/94 - 31}),
                             },
                             new Pair[0],
                             new Pair[0]);
    }

    public void testSemiMatchPackage() throws Exception {
        performVariablesTest("package test; import javax.lang.model.type.TypeMirror; public class Test { }",
                             "$T{java.lang.Object}.type",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testNullType() throws Exception {
        performVariablesTest("package javax.lang.model.type; public class Test { }",
                             "$T{java.lang.Object}.type",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testTryCatch() throws Exception {
        performVariablesTest("package test; import java.io.*; public class Test { public void test() { InputStream ins = null; try { ins = new FileInputStream(\"\"); } catch (IOException e) { e.printStackTrace(); } finally {ins.close();} } }",
                             "try {$stmts$;} catch (java.io.IOException $e) {$e.printStackTrace();} finally {$finally$;}",
                             new Pair[] {
                                   new Pair<String, int[]>("$e", new int[] {176 - 31 - 2, 189 - 31 - 2}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {134 - 31, 166 - 31 - 2}),
                                  new Pair<String, int[]>("$finally$", new int[] {225 - 31 - 2, 237 - 31 - 2}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$e", "e"),
                             });
    }

    public void testMultiParameters1() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.asList(\"a\", \"b\", \"c\"); }",
                             "java.util.Arrays.asList($1$)",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$1$", new int[] {60, 63, 65, 68, 70, 73}),
                             },
                             new Pair[] {
                             });
    }

    public void testMultiParameters2() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.asList(new String(\"a\"), \"b\", \"c\"); }",
                             "java.util.Arrays.asList(new String(\"a\"), $1$)",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$1$", new int[] {77, 80, 82, 85}),
                             },
                             new Pair[] {
                             });
    }

    public void testMultiParameters3() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.asList(); }",
                             "java.util.Arrays.asList($1$)",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$1$", new int[] {}),
                             },
                             new Pair[] {
                             });
    }

    public void testTypeParameters() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.<String>asList(\"a\", \"b\"); }",
                             "java.util.Arrays.<$1>asList($1$)",
                             new Pair[] {
                                   new Pair<String, int[]>("$1", new int[] {85 - 31, 91 - 31}),
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             });
    }

    public void testModifiers() throws Exception {
        performVariablesTest("package test; public class Test { private String s; }",
                             "$mods$ java.lang.String $name;",
                             new Pair[] {
                                 new Pair<String, int[]>("$name", new int[] {65 - 31, 82 - 31}),
                                 new Pair<String, int[]>("$mods$", new int[] {65 - 31, 72 - 31}), //XXX: shouldn't this be a multi-variable?
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                                  new Pair<String, String>("$name", "s"),
                             });
    }

    public void testVariableIsFullPattern1() throws Exception {
        performVariablesTest("package test; public class Test { private int a; {System.err.println(a);} }",
                             "$v{int}",
                             new Pair[] {
                                 new Pair<String, int[]>("$v", new int[] {100 - 31, 101 - 31}),
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             });
    }

    public void testVariableIsFullPattern2() throws Exception {
        performVariablesTest("package test; public class Test { private int a; {System.err.println(a);} }",
                             "$v{int}",
                             new Pair[] {
                                 new Pair<String, int[]>("$v", new int[] {100 - 31, 101 - 31}),
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             false,
                             true);
    }

    public void testNoCCEForVariableName() throws Exception {
        performVariablesTest("package test; public class Test { { int[] arr = null; int a; arr[a] = 0;} }",
                             "int $a; $a = 0;",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTrees1() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); } else { System.err.println(); System.err.println(); } } }",
                             "if ($c) $s; else $s;",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables1() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); System.err.println(); } else { System.err.println(); System.err.println(); System.err.println(); } } }",
                             "if ($c) { $s$;} else { $s$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables2() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(1); System.err.println(); } else System.err.println(1); } }",
                             "if ($c) { System.err.println(1); $s2$; } else { System.err.println(1); $s2$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables3() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); System.err.println(1); } else System.err.println(1); } }",
                             "if ($c) { $s1$; System.err.println(1); } else { $s1$; System.err.println(1); }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void XtestVerifySameTreesMultiVariables4() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); System.err.println(1); System.err.println(); } else System.err.println(1); } }",
                             "if ($c) { $s1$; System.err.println(1); $s2$; } else { $s1$; System.err.println(1); $s2$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables5() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(1); } else System.err.println(2); } }",
                             "if ($c) { $s$; } else { $s$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testSimpleRemapping1() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        int i = 0;\n" +
                             "        |System.err.println(i);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        int a = 0;\n" +
                             "        |System.err.println(a);|\n" +
                             "    }\n" +
                             "}\n",
                             "i");
    }

    public void testSimpleRemapping2() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        int i = 0;\n" +
                             "        |System.err.println(i);\n" +
                             "         int i2 = 0;\n" +
                             "         System.err.println(i2);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        int a = 0;\n" +
                             "        |System.err.println(a);\n" +
                             "         int a2 = 0;\n" +
                             "         System.err.println(a2);|\n" +
                             "    }\n" +
                             "}\n",
                             "i");
    }

    public void testSimpleRemapping3() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        |int i = 0;\n" +
                             "         System.err.println(i);\n" +
                             "         int i2 = 0;\n" +
                             "         System.err.println(i2);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        |int a = 0;\n" +
                             "         System.err.println(a);\n" +
                             "         int a2 = 0;\n" +
                             "         System.err.println(a2);|\n" +
                             "    }\n" +
                             "}\n");
    }

    public void testSimpleRemapping4() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        int i = 0;\n" +
                             "        |System.err.println(i);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        int[] a = {0};\n" +
                             "        |System.err.println(a[0]);|\n" +
                             "    }\n" +
                             "}\n",
                             "i");
    }

    public void testVariableMemberSelect() throws Exception {
        performVariablesTest("package test; public class Test {public void test(String str) { str.length(); str.length(); } public void test1(String str) { str.length(); str.isEmpty(); } }",
                             "{ $str.$method(); $str.$method(); }",
                             new Pair[0],
                             new Pair[] {new Pair<String, String>("$method", "length")});
    }

    public void testCorrectSite1() throws Exception {
        performVariablesTest("package test; public class Test { public void test(Object o) { o.wait(); } }",
                             "$s{java.util.concurrent.locks.Condition}.wait()",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testCorrectSite2() throws Exception {
        performVariablesTest("package test; public class Test { public void test(Object o) { wait(); } }",
                             "$s{java.util.concurrent.locks.Condition}.wait()",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testCorrectSite3() throws Exception {
        performVariablesTest("package test; public abstract class Test implements java.util.concurrent.locks.Condition { public void test() { new Runnable() { public void run() { wait(); } } } }",
                             "$s{java.util.concurrent.locks.Condition}.wait()",
                             new Pair[0],// {new Pair<String, int[]>("$s", new int[] {-1, -1})},
                             new Pair[0],
                             new Pair[0]);
    }

    public void testCorrectSite4() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { foo.stop(); } }",
                             "$s{java.lang.Thread}.stop()",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    protected void prepareTest(String code) throws Exception {
        prepareTest(code, -1);
    }

    protected void prepareTest(String code, int testIndex) throws Exception {
        File workDirWithIndexFile = testIndex != (-1) ? new File(getWorkDir(), Integer.toString(testIndex)) : getWorkDir();
        FileObject workDirWithIndex = FileUtil.toFileObject(workDirWithIndexFile);

        if (workDirWithIndex != null) {
            workDirWithIndex.delete();
        }

        workDirWithIndex = FileUtil.createFolder(workDirWithIndexFile);

        assertNotNull(workDirWithIndexFile);

        FileObject sourceRoot = workDirWithIndex.createFolder("src");
        FileObject buildRoot  = workDirWithIndex.createFolder("build");
        FileObject cache = workDirWithIndex.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");

        TestUtilities.copyStringToFile(data, code);

        data.refresh();

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }
    
    private static String findRegions(String code, List<int[]> regions) {
        String[] split = code.split("\\|");
        StringBuilder filtered = new StringBuilder();

        filtered.append(split[0]);

        int offset = split[0].length();

        for (int cntr = 1; cntr < split.length; cntr += 2) {
            int[] i = new int[] {
                offset,
                offset + split[cntr].length()
            };

            regions.add(i);

            filtered.append(split[cntr]);
            filtered.append(split[cntr + 1]);

            offset += split[cntr].length();
            offset += split[cntr + 1].length();
        }

        return filtered.toString();
    }
    
    protected CompilationInfo info;
    private Document doc;
    
    private void performTest(String code) throws Exception {
        performTest(code, true);
    }

    private void performTest(String code, boolean verify) throws Exception {
        List<int[]> result = new LinkedList<int[]>();

        code = findRegions(code, result);

        int testIndex = 0;

        for (int[] i : result) {
            int[] duplicates = new int[2 * (result.size() - 1)];
            int cntr = 0;
            List<int[]> l = new LinkedList<int[]>(result);

            l.remove(i);

            for (int[] span : l) {
                duplicates[cntr++] = span[0];
                duplicates[cntr++] = span[1];
            }

            doPerformTest(code, i[0], i[1], testIndex++, verify, duplicates);
        }
    }

    protected void performTest(String code, int start, int end, int... duplicates) throws Exception {
        doPerformTest(code, start, end, -1, true, duplicates);
    }

    protected void doPerformTest(String code, int start, int end, int testIndex, int... duplicates) throws Exception {
        doPerformTest(code, start, end, testIndex, true, duplicates);
    }

    protected void doPerformTest(String code, int start, int end, int testIndex, boolean verify, int... duplicates) throws Exception {
        prepareTest(code, testIndex);

        TreePath path = info.getTreeUtilities().pathFor((start + end) / 2 + 1);

        while (path != null) {
            Tree t = path.getLeaf();
            SourcePositions sp = info.getTrees().getSourcePositions();

            if (   start == sp.getStartPosition(info.getCompilationUnit(), t)
                && end   == sp.getEndPosition(info.getCompilationUnit(), t)) {
                break;
            }

            path = path.getParentPath();
        }

        assertNotNull(path);

        Collection<TreePath> result = computeDuplicates(path);

        //        assertEquals(f.result.toString(), duplicates.length / 2, f.result.size());

        if (verify) {
            int[] dupes = new int[result.size() * 2];
            int   index = 0;

            for (TreePath tp : result) {
                dupes[index++] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf());
                dupes[index++] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());
            }

            assertTrue("Was: " + Arrays.toString(dupes) + " should have been: " + Arrays.toString(duplicates), Arrays.equals(duplicates, dupes));
        }
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, String>[] duplicatesNames) throws Exception {
        performVariablesTest(code, pattern, duplicatesPos, new Pair[0], duplicatesNames);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, int[]>[] multiStatementPos, Pair<String, String>[] duplicatesNames) throws Exception {
        performVariablesTest(code, pattern, duplicatesPos, multiStatementPos, duplicatesNames, false);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, int[]>[] multiStatementPos, Pair<String, String>[] duplicatesNames, boolean noOccurrences) throws Exception {
        performVariablesTest(code, pattern, duplicatesPos, multiStatementPos, duplicatesNames, noOccurrences, false);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, int[]>[] multiStatementPos, Pair<String, String>[] duplicatesNames, boolean noOccurrences, boolean useBulkSearch) throws Exception {
        prepareTest(code, -1);

        Pattern patternObj = Pattern.compile(info, pattern);
        TreePath patternPath = new TreePath(new TreePath(info.getCompilationUnit()), patternObj.getPattern());
        Map<TreePath, VariableAssignments> result;

        if (useBulkSearch) {
            result = new HashMap<TreePath, VariableAssignments>();

            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternObj.getPatternCode());

            for (Entry<String, Collection<TreePath>> e : BulkSearch.getDefault().match(info, new TreePath(info.getCompilationUnit()), bulkPattern).entrySet()) {
                for (TreePath tp : e.getValue()) {
                    VariableAssignments vars = computeVariables(info, patternPath, tp, new AtomicBoolean(), patternObj.getConstraints());

                    if (vars != null) {
                        result.put(tp, vars);
                    }
                }
            }
        } else {
            result = computeDuplicates(info, patternPath, new TreePath( info.getCompilationUnit()), new AtomicBoolean(), patternObj.getConstraints());
        }

        if (noOccurrences) {
            assertEquals(0, result.size());
            return ;
        }

        assertSame(1, result.size());

        Map<String, int[]> actual = new HashMap<String, int[]>();

        for (Entry<String, TreePath> e : result.values().iterator().next().variables.entrySet()) {
            int[] span = new int[] {
                (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), e.getValue().getLeaf()),
                (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), e.getValue().getLeaf())
            };

            actual.put(e.getKey(), span);
        }

        for (Pair<String, int[]> dup : duplicatesPos) {
            int[] span = actual.remove(dup.getA());

            if (span == null) {
                fail(dup.getA());
            }
            assertTrue(dup.getA() + ":" + Arrays.toString(span), Arrays.equals(span, dup.getB()));
        }

        Map<String, int[]> actualMulti = new HashMap<String, int[]>();

        for (Entry<String, Collection<? extends TreePath>> e : result.values().iterator().next().multiVariables.entrySet()) {
            int[] span = new int[2 * e.getValue().size()];
            int i = 0;

            for (TreePath tp : e.getValue()) {
                span[i++] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf());
                span[i++] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());
            }

            actualMulti.put(e.getKey(), span);
        }

        for (Pair<String, int[]> dup : multiStatementPos) {
            int[] span = actualMulti.remove(dup.getA());

            if (span == null) {
                fail(dup.getA());
            }
            assertTrue(dup.getA() + ":" + Arrays.toString(span), Arrays.equals(span, dup.getB()));
        }

        Map<String, String> golden = new HashMap<String, String>();

        for ( Pair<String, String> e : duplicatesNames) {
            golden.put(e.getA(), e.getB());
        }

        assertEquals(golden, result.values().iterator().next().variables2Names);
    }

    protected VariableAssignments computeVariables(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        return CopyFinder.computeVariables(info, searchingFor, scope, cancel, designedTypeHack);
    }

    protected Map<TreePath, VariableAssignments> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        return CopyFinder.computeDuplicates(info, searchingFor, scope, cancel, designedTypeHack);
    }

    private void performRemappingTest(String code, String... remappableVariables) throws Exception {
        List<int[]> regions = new LinkedList<int[]>();

        code = findRegions(code, regions);

        prepareTest(code, -1);

        int[] statements = new int[2];

        int[] currentRegion = regions.get(0);
        TreePathHandle tph = IntroduceHint.validateSelectionForIntroduceMethod(info, currentRegion[0], currentRegion[1], statements);

        assertNotNull(tph);

        TreePath tp = tph.resolve(info);

        assertNotNull(tp);

        BlockTree bt = (BlockTree) tp.getLeaf();
        List<TreePath> searchFor = new LinkedList<TreePath>();

        for (StatementTree t : bt.getStatements().subList(statements[0], statements[1] + 1)) {
            searchFor.add(new TreePath(tp, t));
        }

        final Set<VariableElement> vars = new HashSet<VariableElement>();

        for (final String name : remappableVariables) {
            new TreePathScanner<Object, Object>() {
                @Override
                public Object visitVariable(VariableTree node, Object p) {
                    if (node.getName().contentEquals(name)) {
                        vars.add((VariableElement) info.getTrees().getElement(getCurrentPath()));
                    }

                    return super.visitVariable(node, p);
                }
            }.scan(info.getCompilationUnit(), null);
        }

        Collection<? extends MethodDuplicateDescription> result = CopyFinder.computeDuplicatesAndRemap(info, searchFor, new TreePath(info.getCompilationUnit()), vars, new AtomicBoolean());
        Set<List<Integer>> realSpans = new HashSet<List<Integer>>();

        for (MethodDuplicateDescription mdd : result) {
            List<? extends StatementTree> parentStatements = CopyFinder.getStatements(mdd.firstLeaf);
            int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), parentStatements.get(mdd.dupeStart));
            int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), parentStatements.get(mdd.dupeEnd));

            realSpans.add(Arrays.asList(startPos, endPos));
        }

        Set<List<Integer>> goldenSpans = new HashSet<List<Integer>>();

        for (int[] region : regions) {
            if (region == currentRegion) continue;

            int[] stmts = new int[2];
            TreePathHandle gtph = IntroduceHint.validateSelectionForIntroduceMethod(info, region[0], region[1], stmts);

            assertNotNull(gtph);

            TreePath gtp = gtph.resolve(info);

            assertNotNull(gtp);

            BlockTree b = (BlockTree) gtp.getLeaf();

            int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), b.getStatements().get(stmts[0]));
            int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), b.getStatements().get(stmts[1]));

            goldenSpans.add(Arrays.asList(startPos, endPos));
        }

        assertEquals(goldenSpans, realSpans);
    }

    protected Collection<TreePath> computeDuplicates(TreePath path) {
        return computeDuplicates(info, path, new TreePath(info.getCompilationUnit()), new AtomicBoolean(), null).keySet();
    }

    public static final class Pair<A, B> {
        private final A a;
        private final B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

    }
}
