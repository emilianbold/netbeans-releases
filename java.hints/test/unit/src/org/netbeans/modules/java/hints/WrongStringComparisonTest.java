/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class WrongStringComparisonTest extends TreeRuleTestBase {

    private WrongStringComparison wsc;

    public WrongStringComparisonTest(String name) {
        super(name);
        wsc = new WrongStringComparison();
    }

    public void testSimple() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=");
    }
    
    public void testDisableWhenCorrectlyCheckedAsIn111441() throws Exception {
        String code = "package test;" +
                      "public class Test {" +
                      "    private String s;" +
                      "    private void test() {" +
                      "        Test t = null;" +
                      "        boolean b = this.s !";
        
        String codeAfter = "= t.s && (this.s == null || !this.s.equals(t.s));" +
                           "    }" +
                           "}";
        performAnalysisTest("test/Test.java", code + codeAfter, code.length());
    }

    public void testFixWithTernaryNullCheck() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() with null check (ternary)]",
                            "package test;public class Test { private String s; private void test() { String t = null; if (s == null ? t == null : s.equals(t)); }}");
    }

    public void testFixWithoutNullCheck() throws Exception {
        WrongStringComparison.setTernaryNullCheck(wsc.getPreferences(HintsSettings.getCurrentProfileId()), false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals()]",
                            "package test;public class Test { private String s; private void test() { String t = null; if (s.equals(t)); }}");
    }

    public void testFixWithNullCheck() throws Exception {
        WrongStringComparison.setTernaryNullCheck(wsc.getPreferences(HintsSettings.getCurrentProfileId()), false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        String t = null;" +
                            "        if (s =|= t);" +
                            "    }" +
                            "}",
                            "0:114-0:120:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() with null check]",
                            "package test;public class Test { private String s; private void test() { String t = null; if ((s == null && t == null) || (s != null && s.equals(t))); }}");
    }

    public void testFixWithStringLiteralFirst() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        if (\"\" =|= s);" +
                            "    }" +
                            "}",
                            "0:90-0:97:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals()]",
                            "package test;public class Test { private String s; private void test() { if (\"\".equals(s)); }}");
    }

    public void testFixWithStringLiteralSecondReverseOperands() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        if (s =|= \"\");" +
                            "    }" +
                            "}",
                            "0:90-0:97:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() and reverse operands]",
                            "package test;public class Test { private String s; private void test() { if (\"\".equals(s)); }}");
    }

    public void testFixWithStringLiteralSecondNullCheck() throws Exception {
        WrongStringComparison.setStringLiteralsFirst(wsc.getPreferences(HintsSettings.getCurrentProfileId()), false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        if (s =|= \"\");" +
                            "    }" +
                            "}",
                            "0:90-0:97:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals() with null check]",
                            "package test;public class Test { private String s; private void test() { if (s != null && s.equals(\"\")); }}");
    }

    public void testFixWithStringLiteralSecondNoNullCheck() throws Exception {
        WrongStringComparison.setStringLiteralsFirst(wsc.getPreferences(HintsSettings.getCurrentProfileId()), false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private String s;" +
                            "    private void test() {" +
                            "        if (s =|= \"\");" +
                            "    }" +
                            "}",
                            "0:90-0:97:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals()]",
                            "package test;public class Test { private String s; private void test() { if (s.equals(\"\")); }}");
    }

    public void testFixWithTwoStringLiterals() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test() {" +
                            "        if (\"\" =|= \"\");" +
                            "    }" +
                            "}",
                            "0:69-0:77:verifier:Comparing Strings using == or !=",
                            "[WrongStringComparisonFix:Use equals()]",
                            "package test;public class Test { private void test() { if (\"\".equals(\"\")); }}");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return wsc.run(info, path);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }
}
