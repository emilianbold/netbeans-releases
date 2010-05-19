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
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.ImportClassTest;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * The following shell script was used to generate the code snippets
 *
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 *
 * but it will break if the source includes single-line comments or strange characters in Strings.
 *
 * @author Samuel Halliday
 * @see ImportClassTest
 */
public class StaticImportTest extends TreeRuleTestBase {

    private final StaticImport computer = new StaticImport();

    public StaticImportTest(String name) {
        super(name);
    }

    public void testStaticImportHint1() throws Exception {
        String test = "package test; public class Test { public Test() { Math.|abs(1); } }";
        String golden = "package test; import static java.lang.Math.abs; public class Test { public Test() { abs(1); } }";
        performFixTest(test, golden);
    }

    public void testStaticImportHint2() throws Exception {
        String test = "package test; public class Test { public Test() { Test.get|Logger(); } public static void getLogger() { } }";
        String golden = "package test; public class Test { public Test() { getLogger(); } public static void getLogger() { } }";
        performFixTest(test, golden);
    }

    public void testStaticImportHint3() throws Exception {
        String test = "package test; public class Test extends Foo { public Test() { Foo.f|oo(); } } class Foo { static protected void foo() { } }";
        String golden = "package test; public class Test extends Foo { public Test() { foo(); } } class Foo { static protected void foo() { } }";
        performFixTest(test, golden);
    }

    public void testStaticImportHint4() throws Exception {
        String test = "package test; import java.util.Calendar; import static java.util.Calendar.*; public class Test { public Test() { Calendar.getInstance|(); } }";
        String golden = "package test; import java.util.Calendar; import static java.util.Calendar.*; public class Test { public Test() { getInstance(); } }";
        performFixTest(test, golden);
    }

    // XXX disabled... the experimental hint does the error cases
//    public void testStaticImportHint5() throws Exception {
//        String test = "package test; import java.util.logging.Logger; public class Test { public Test() { Logger.getLogger|(\"\"); } public static void getLogger() { } }";
//        performAnalysisTest(test);
//    }
//
//    public void testStaticImportHint6() throws Exception {
//        String test = "package test; public class Test extends Foo { public Test() { Bar.foo|(); } } class Foo { static protected void foo() { } } class Bar { static protected void foo() { } }";
//        performAnalysisTest(test);
//    }
//
//    public void testStaticImportHint7() throws Exception {
//        String test = "package test; import javax.crypto.KeyAgreement; import static java.util.Calendar.*; public class Test { public Test() throws Exception { KeyAgreement.getInstance|(\"\"); } }";
//        performAnalysisTest(test);
//    }

    public void testStaticImportHint8() throws Exception {
        String test = "package test; public class Test extends Foo { class FooBar { FooBar() { Foo.foo|(); } } } class Foo { static protected void foo() { } }";
        String golden = "package test; public class Test extends Foo { class FooBar { FooBar() { foo(); } } } class Foo { static protected void foo() { } }";
        performFixTest(test, golden);
    }

    // XXX disabled... the experimental hint does the error cases
//    public void testStaticImportHint9() throws Exception {
//        String test = "package test; public class Test extends Foo { class FooBar { FooBar() { Bar.foo|(); } } } class Foo { static protected void foo() { } } class Bar { static protected void foo() { } }";
//        performAnalysisTest(test);
//    }

    // test is single line source code for test.Test, | in the member select, space before
    // golden is the output to test against
    // sn is the simple name of the static method
    private void performFixTest(String test, String golden) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        int end = test.indexOf("(", offset) - 1;
        assertTrue(end > 0);
        int start = test.lastIndexOf(" ", offset) + 1;
        assertTrue(start > 0);
        performFixTest("test/Test.java",
                test.replace("|", ""),
                offset,
                "0:" + start + "-0:" + end + ":hint:" + NbBundle.getMessage(StaticImport.class, "ERR_StaticImport"),
                StaticImport.class.getSimpleName(),
                golden);
    }

    // test is single line source code for test.Test, | in the member select, space before
    // completes successfully if there are no hints presented
    private void performAnalysisTest(String test) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        performAnalysisTest("test/Test.java", test.replace("|", ""), offset);
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return computer.run(info, path);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof StaticImport.FixImpl) {
            return StaticImport.class.getSimpleName();
        } else {
            return super.toDebugString(info, f);
        }
    }
}