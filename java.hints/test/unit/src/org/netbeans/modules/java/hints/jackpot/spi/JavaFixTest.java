/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.LifecycleManager;
import org.openide.modules.SpecificationVersion;
import org.openide.util.MapFormat;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixTest extends TestBase {

    public JavaFixTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testSimpleDate() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5 (16 May 2005)\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testLongText() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.123.2.1 - branch propsheet_issue_29447\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.123.2.1")));
    }

    public void testModuleName() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since org.openide.filesystems 7.15\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("7.15")));
    }

    public void testModuleNameMajor() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since org.openide/1 4.42\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("4.42")));
    }

    public void testEnd() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5 */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testOpenAPI() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since OpenAPI version 2.12" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("2.12")));

    }

    private SpecificationVersion computeSpecVersion(String javadoc) throws Exception {
        prepareTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    javadoc +
                    "     public void test() {\n" +
                    "     }\n" +
                    "}\n");

        TypeElement te = info.getElements().getTypeElement("test.Test");
        ExecutableElement method = ElementFilter.methodsIn(te.getEnclosedElements()).iterator().next();

        return JavaFix.computeSpecVersion(info, method);
    }

    public void testArithmetic1() throws Exception {
        performArithmeticTest("1 + 2", "3");
        performArithmeticTest("1f + 2", "3.0F");
        performArithmeticTest("1 + 2f", "3.0F");
        performArithmeticTest("1.0 + 2f", "3.0");
        performArithmeticTest("1 + 2.0", "3.0");
        performArithmeticTest("1L + 2", "3L");
    }

    public void testArithmetic2() throws Exception {
        performArithmeticTest("1 * 2", "2");
        performArithmeticTest("1f * 2", "2.0F");
        performArithmeticTest("1 * 2f", "2.0F");
        performArithmeticTest("1.0 * 2f", "2.0");
        performArithmeticTest("1 * 2.0", "2.0");
        performArithmeticTest("1L * 2", "2L");
    }

    public void testArithmetic3() throws Exception {
        performArithmeticTest("4 / 2", "2");
        performArithmeticTest("4f / 2", "2.0F");
        performArithmeticTest("4 / 2f", "2.0F");
        performArithmeticTest("4.0 / 2f", "2.0");
        performArithmeticTest("4 / 2.0", "2.0");
        performArithmeticTest("4L / 2", "2L");
    }

    public void testArithmetic4() throws Exception {
        performArithmeticTest("5 % 2", "1");
        performArithmeticTest("5f % 2", "1.0F");
        performArithmeticTest("5 % 2f", "1.0F");
        performArithmeticTest("5.0 % 2f", "1.0");
        performArithmeticTest("5 % 2.0", "1.0");
        performArithmeticTest("5L % 2", "1L");
    }

    public void testArithmetic5() throws Exception {
        performArithmeticTest("5 - 2", "3");
        performArithmeticTest("5f - 2", "3.0F");
        performArithmeticTest("5 - 2f", "3.0F");
        performArithmeticTest("5.0 - 2f", "3.0");
        performArithmeticTest("5 - 2.0", "3.0");
        performArithmeticTest("5L - 2", "3L");
    }

    public void testArithmetic6() throws Exception {
        performArithmeticTest("5 | 2", "7");
        performArithmeticTest("5L | 2", "7L");
        performArithmeticTest("5 | 2L", "7L");
    }

    public void testArithmetic7() throws Exception {
        performArithmeticTest("5 & 4", "4");
        performArithmeticTest("5L & 4", "4L");
        performArithmeticTest("5 & 4L", "4L");
    }

    public void testArithmetic8() throws Exception {
        performArithmeticTest("5 ^ 4", "1");
        performArithmeticTest("5L ^ 4", "1L");
        performArithmeticTest("5 ^ 4L", "1L");
    }

    public void testArithmetic9() throws Exception {
        performArithmeticTest("5 << 2", "20");
        performArithmeticTest("5L << 2", "20L");
        performArithmeticTest("5 << 2L", "20L");
    }

    public void testArithmeticA() throws Exception {
        performArithmeticTest("-20 >> 2", "-5");
        performArithmeticTest("-20L >> 2", "-5L");
        performArithmeticTest("-20 >> 2L", "-5L");
    }

    public void testArithmeticB() throws Exception {
        performArithmeticTest("-20 >>> 2", "1073741819");
    }

    public void testArithmeticC() throws Exception {
        performArithmeticTest("0 + -20", "-20");
        performArithmeticTest("0 + +20", "20");
    }

    public void testArithmeticComplex() throws Exception {
        performArithmeticTest("1 + 2 * 4 - 5", "4");
        performArithmeticTest("1f + 2 * 4.0 - 5", "4.0");
        performArithmeticTest("1L + 2 * 4 - 5", "4L");
    }

    private static final String ARITHMETIC = "public class Test { private Object o = __VAL__; }";
    private void performArithmeticTest(String orig, String nue) throws Exception {
        String code = replace("0");

        prepareTest("Test.java", code);
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        VariableTree variable = (VariableTree) clazz.getMembers().get(1);
        ExpressionTree init = variable.getInitializer();
        TreePath tp = new TreePath(new TreePath(new TreePath(new TreePath(info.getCompilationUnit()), clazz), variable), init);
        Fix fix = JavaFix.rewriteFix(info, "A", tp, orig, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap(), Collections.<String, TypeMirror>emptyMap());
        fix.implement();

        String golden = replace(nue);
        String out = doc.getText(0, doc.getLength());

        assertEquals(golden, out);

        LifecycleManager.getDefault().saveAll();
    }

    private static String replace(String val) {
        MapFormat f = new MapFormat(Collections.singletonMap("VAL", val));

        f.setLeftBrace("__");
        f.setRightBrace("__");

        return f.format(ARITHMETIC);
    }

    public void testRewriteWithParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = new String(\"a\" + \"b\").length();\n" +
                           "}\n",
                           "new String($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = (\"a\" + \"b\").length();\n" +
		           "}\n");
    }

    public void testRewriteWithParenthesis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = Integer.valueOf(1 + 2) * 3;\n" +
                           "}\n",
                           "Integer.valueOf($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = (1 + 2) * 3;\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = new String(\"a\" + \"b\").length();\n" +
                           "}\n",
                           "new String($1)=>java.lang.String.format(\"%s%s\", $1, \"\")",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = String.format(\"%s%s\", \"a\" + \"b\", \"\").length();\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    String s = (\"a\" + \"b\").intern();\n" +
                           "}\n",
                           "($1).intern()=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    String s = \"a\" + \"b\";\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis3() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = Integer.valueOf(1 + 2) + 3;\n" +
                           "}\n",
                           "Integer.valueOf($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = 1 + 2 + 3;\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis4() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = Integer.valueOf(1 * 2) + 3;\n" +
                           "}\n",
                           "Integer.valueOf($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = 1 * 2 + 3;\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis5() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = new Integer(1 * 2).hashCode();\n" +
                           "}\n",
                           "$1.hashCode()=>$1.hashCode()",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = new Integer(1 * 2).hashCode();\n" +
		           "}\n");
    }

    public void testTopLevelRewriteWithoutParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = (1 + 2) * 2;\n" +
                           "}\n",
                           "$1 + $2=>3",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = 3 * 2;\n" +
		           "}\n");
    }

    public void testTopLevelRewriteKeepParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = (1 * 2) + 2;\n" +
                           "}\n",
                           "$1 * $2=>2",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = (2) + 2;\n" +
		           "}\n");
    }

    public void testTopLevelRewriteKeepParenthesis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { if (1 > 2) ; }\n" +
                           "}\n",
                           "$1 > $2=>false",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    { if (false) ; }\n" +
		           "}\n");
    }
    
    public void testRewriteCatchMultiVariable() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { try { } catch {NullPointerException ex} { } }\n" +
                           "}\n",
                           "try { } catch $catches$ => try { new Object(); } catch $catches$",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    { try {      new Object();\n } catch {NullPointerException ex} { } }\n" +
		           "}\n");
    }

    public void testRewriteCaseMultiVariable() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { int i = 0; switch (i) {case 0: System.err.println(1); break; case 1: System.err.println(2); break; case 2: System.err.println(3); break; }\n" +
                           "}\n",
                           "switch ($v) { case $p$ case 2: $stmts$; } => switch ($v) { case $p$ case 3: $stmts$; }",
                           "package test;\n" +
                           "public class Test {\n" +
                           //XXX: whitespaces:
//                           "    { int i = 0; switch (i) {case 0: System.err.println(1); break; case 1: System.err.println(2); break; case 3: System.err.println(3); break; }\n" +
                           "    { int i = 0; switch (i) {case 0: System.err.println(1); break; case 1: System.err.println(2); break; case   3: System.err.println(3); break; }\n" +
		           "}\n");
    }

    public void testRewriteMemberSelectVariable() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { java.io.File f = null; boolean b = f.isDirectory(); }\n" +
                           "}\n",
                           "$file.$m() => foo.Bar.$m($file)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    { java.io.File f = null; boolean b = foo.Bar.isDirectory(f); }\n" +
		           "}\n");
    }

    public void testCarefulRewriteInImports() throws Exception {
        performRewriteTest("package test;\n" +
                           "import javax.swing.text.AbstractDocument;\n" +
                           "public class Test {\n" +
                           "}\n",
                           "javax.swing.text.AbstractDocument => javax.swing.text.Document",
                           "package test;\n" +
                           "import javax.swing.text.Document;\n" +
                           "public class Test {\n" +
		           "}\n");
    }

    public void testRemoveFromParent1() throws Exception {
        performRemoveFromParentTest("package test;\n" +
                                    "public class Test {\n" +
                                    "    private int I;" +
                                    "}\n",
                                    "$mods$ int $f;",
                                    "package test;\n" +
                                    "public class Test {\n" +
                                    "}\n");
    }

    public void testRemoveFromParent2() throws Exception {
        performRemoveFromParentTest("package test;\n" +
                                    "public class Test extends java.util.ArrayList {\n" +
                                    "}\n",
                                    "java.util.ArrayList",
                                    "package test;\n" +
                                    "public class Test {\n" +
                                    "}\n");
    }

    public void testUnresolvableTarget() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test extends java.util.ArrayList {\n" +
                           "}\n",
                           "java.util.ArrayList => Test",
                           "package test;\n" +
                           "public class Test extends Test {\n" +
                           "}\n");
    }

    public void testTryWithResourceTarget() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        InputStream in = null;\n" +
                           "        try {\n" +
                           "        } finally {\n" +
                           "            in.close()\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "$type $var = $init; try {} finally {$var.close();} => try ($type $var = $init) {} finally {$var.close();}",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
//                           "        try (InputStream in = null) {\n" +
                           //XXX:
                           "        try (final InputStream in = null) {\n" +
                           "        } finally {\n" +
                           "            in.close()\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void performRewriteTest(String code, String rule, String golden) throws Exception {
	prepareTest("test/Test.java", code);

        final String[] split = rule.split("=>");
        assertEquals(2, split.length);
        HintDescription hd = HintDescriptionFactory.create()
                                                   .setTriggerPattern(PatternDescription.create(split[0], Collections.<String, String>emptyMap()))
                                                   .setWorker(new HintDescription.Worker() {
            @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "", JavaFix.rewriteFix(ctx, "", ctx.getPath(), split[1])));
            }
        }).produce();

        Map<PatternDescription, List<HintDescription>> patternHints = new HashMap<PatternDescription, List<HintDescription>>();
        HashMap<Kind, List<HintDescription>> kindHints = new HashMap<Kind, List<HintDescription>>();

        RulesManager.sortOut(Collections.singleton(hd), kindHints, patternHints);
        List<ErrorDescription> computeHints = new HintsInvoker(info, new AtomicBoolean()).computeHints(info, kindHints, patternHints);

        assertEquals(computeHints.toString(), 1, computeHints.size());

        Fix fix = computeHints.get(0).getFixes().getFixes().get(0);

	fix.implement();

        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

    public void performRemoveFromParentTest(String code, String rule, String golden) throws Exception {
	prepareTest("test/Test.java", code);

        HintDescription hd = HintDescriptionFactory.create()
                                                   .setTriggerPattern(PatternDescription.create(rule, Collections.<String, String>emptyMap()))
                                                   .setWorker(new HintDescription.Worker() {
            @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "", JavaFix.removeFromParent(ctx, "", ctx.getPath())));
            }
        }).produce();

        Map<PatternDescription, List<HintDescription>> patternHints = new HashMap<PatternDescription, List<HintDescription>>();
        HashMap<Kind, List<HintDescription>> kindHints = new HashMap<Kind, List<HintDescription>>();

        RulesManager.sortOut(Collections.singleton(hd), kindHints, patternHints);
        List<ErrorDescription> computeHints = new HintsInvoker(info, new AtomicBoolean()).computeHints(info, kindHints, patternHints);

        assertEquals(computeHints.toString(), 1, computeHints.size());

        Fix fix = computeHints.get(0).getFixes().getFixes().get(0);

	fix.implement();

        assertEquals(golden, doc.getText(0, doc.getLength()));
    }
}
