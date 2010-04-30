/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import com.sun.source.tree.Tree.Kind;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.modules.SpecificationVersion;

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
}