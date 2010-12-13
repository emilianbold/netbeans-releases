/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertToStringSwitchTest extends TestBase {

    public ConvertToStringSwitchTest(String name) {
        super(name, ConvertToStringSwitch.class);
    }

    public void testSimple() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     public void test() {" +
                       "         String g = null;" +
                       "         if (g == \"j\") {" +
                       "             System.err.println(1);" +
                       "         } else if (g == \"k\") {" +
                       "             System.err.println(2);" +
                       "         } else if (g == \"l\") {" +
                       "             System.err.println(3);" +
                       "         }" +
                       "     }" +
                       "}",
                       "0:91-0:93:verifier:Convert to switch",
                       "FixImpl",
                       "package test;public class Test { public void test() { String g = null;switch (g) { case \"j\": System.err.println(1); break; case \"k\": System.err.println(2); break; case \"l\": System.err.println(3); break; } }}");
    }

    public void testSimpleFlow() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     public int test(int r) throws Exception {" +
                       "         String g = null;\n" +
                       "         if (g == \"j\") {" +
                       "             System.err.println(1);" +
                       "             return 1;" +
                       "         } else if (g == \"k\") {" +
                       "             System.err.println(2);" +
                       "             if (r >= 0) {" +
                       "                 return 2;" +
                       "             } else {" +
                       "                 return 3;" +
                       "             }" +
                       "         } else if (g == \"l\") {" +
                       "             System.err.println(3);" +
                       "         } else if (g == \"z\") {" +
                       "             try {" +
                       "                 throw new java.io.FileNotFoundException();" +
                       "             } catch (java.io.IOException e) {}" +
                       "         } else if (g == \"a\") {" +
                       "             try {" +
                       "                 throw new java.io.IOException();" +
                       "             } catch (java.io.FileNotFoundException e) {}" +
                       "         } else {\n" +
                       "             throw new IllegalStateExceptin();\n" +
                       "         }\n" +
                       "         return 11;\n" +
                       "     }" +
                       "}",
                       "1:9-1:11:verifier:Convert to switch",
                       "FixImpl",
                       ("package test;" +
                       "public class Test {" +
                       "     public int test(int r) throws Exception {" +
                       "         String g = null;" +
                       "         switch (g) {\n" +
                       "             case \"j\":\n" +
                       "                 System.err.println(1);" +
                       "                 return 1;" +
                       "             case \"k\":\n" +
                       "                 System.err.println(2);" +
                       "                 if (r >= 0) {" +
                       "                     return 2;" +
                       "                 } else {" +
                       "                     return 3;" +
                       "                 }\n" +
                       "             case \"l\":\n" +
                       "                 System.err.println(3);" +
                       "                 break;" +
                       "             case \"z\":\n" +
                       "                 try {" +
                       "                     throw new java.io.FileNotFoundException();" +
                       "                 } catch (java.io.IOException e) {}" +
                       "                 break;" +
                       "             case \"a\":\n" +
                       "                 try {" +
                       "                     throw new java.io.IOException();" +
                       "                 } catch (java.io.FileNotFoundException e) {}" +
                       "             default:\n" +
                       "                 throw new IllegalStateExceptin();\n" +
                       "         }\n" +
                       "         return 11;\n" +
                       "     }" +
                       "}").replaceAll("[ \t\n]+", " "));
    }

    public void testOr() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     public void test() {" +
                       "         String g = null;" +
                       "         if (g == \"j\" || g == \"m\") {" +
                       "             System.err.println(1);" +
                       "         } else if (g == \"k\") {" +
                       "             System.err.println(2);" +
                       "         } else if (g == \"l\" || g == \"n\") {" +
                       "             System.err.println(3);" +
                       "         } else {" +
                       "             System.err.println(4);" +
                       "             return;" +
                       "         }" +
                       "     }" +
                       "}",
                       "0:91-0:93:verifier:Convert to switch",
                       "FixImpl",
                       "package test;public class Test { public void test() { String g = null;switch (g) { case \"j\": case \"m\": System.err.println(1); break; case \"k\": System.err.println(2); break; case \"l\": case \"n\": System.err.println(3); break; default: System.err.println(4); return; } }}");
    }

    public void testStringEqualsObject() throws Exception {
        setSourceLevel("1.7");
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         Object g = null;\n" +
                       "         if (\"j\".equals(g)) {" +
                       "             System.err.println(1);" +
                       "         } else if (\"k\".equals(g)) {" +
                       "             System.err.println(2);" +
                       "         } else {\n" +
                       "             System.err.println(3);" +
                       "         }\n" +
                       "     }" +
                       "}");
    }

    public void testVariableDeclarations() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     private int a, b;"+
                       "     public void test() throws Exception {" +
                       "         String g = null;\n" +
                       "         if (g == \"j\") {" +
                       "             int i = 1;" +
                       "             int z = 1;" +
                       "             System.err.println(i + z);" +
                       "         } else if (g == \"k\") {" +
                       "             int i = 2;" +
                       "             System.err.println(i);" +
                       "         } else if (g == \"l\") {" +
                       "             int j = 1;" +
                       "             System.err.println(j);" +
                       "         } else if (g == \"z\") {" +
                       "             int z = 1;" +
                       "             System.err.println(z);" +
                       "         } else if (g == \"a\") {" +
                       "             int a = 1;" +
                       "             System.err.println(a);" +
                       "         } else if (g == \"b\") {" +
                       "             int b = 1;" +
                       "             System.err.println(a + b);" +
                       "         }\n" +
                       "     }" +
                       "}",
                       "1:9-1:11:verifier:Convert to switch",
                       "FixImpl",
                       ("package test;" +
                       "public class Test {" +
                       "     private int a, b;"+
                       "     public void test() throws Exception {" +
                       "         String g = null;" +
                       "         switch (g) {\n" +
                       "             case \"j\": {\n" +
                       "                 int i = 1;" +
                       "                 int z = 1;" +
                       "                 System.err.println(i + z);" +
                       "                 break;" +
                       "             }" +
                       "             case \"k\": {\n" +
                       "                 int i = 2;" +
                       "                 System.err.println(i);" +
                       "                 break;" +
                       "             }" +
                       "             case \"l\":\n" +
                       "                 int j = 1;" +
                       "                 System.err.println(j);" +
                       "                 break;" +
                       "             case \"z\": {\n" +
                       "                 int z = 1;" +
                       "                 System.err.println(z);" +
                       "                 break;" +
                       "             }" +
                       "             case \"a\": {\n" +
                       "                 int a = 1;" +
                       "                 System.err.println(a);" +
                       "                 break;" +
                       "             }" +
                       "             case \"b\":\n" +
                       "                 int b = 1;" +
                       "                 System.err.println(a + b);" +
                       "                 break;" +
                       "         }\n" +
                       "     }" +
                       "}").replaceAll("[ \t\n]+", " "));
    }

    public void testNonLocalBreak() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     private int a, b;"+
                       "     public void test() throws Exception {" +
                       "         for (;;) {\n" +
                       "             String g = null;\n" +
                       "             if (g == \"j\") {" +
                       "                 System.err.println(1);" +
                       "                 break;" +
                       "             } else if (g == \"k\") {" +
                       "                 System.err.println(2);" +
                       "                 break;" +
                       "             }\n" +
                       "         }\n" +
                       "     }" +
                       "}",
                       "2:13-2:15:verifier:Convert to switch",
                       "FixImpl",
                       ("package test;" +
                       "public class Test {" +
                       "     private int a, b;"+
                       "     public void test() throws Exception {" +
                       "         OUTER: for (;;) {\n" +
                       "             String g = null;\n" +
                       "             switch (g) {" +
                       "                 case \"j\":" +
                       "                     System.err.println(1);" +
                       "                     break OUTER;" +
                       "                 case \"k\":" +
                       "                     System.err.println(2);" +
                       "                     break OUTER;" +
                       "             }\n" +
                       "         }\n" +
                       "     }" +
                       "}").replaceAll("[ \t\n]+", " "));
    }

    public void testNonConstantString() throws Exception {
        setSourceLevel("1.7");
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     private static String nonConstant = \"a\";" +
                       "     public void test() throws Exception {" +
                       "         String g = null;\n" +
                       "         if (\"j\".equals(g)) {" +
                       "             System.err.println(1);" +
                       "         } else if (nonConstant.equals(g)) {" +
                       "             System.err.println(2);" +
                       "         } else {\n" +
                       "             System.err.println(3);" +
                       "         }\n" +
                       "     }" +
                       "}");
    }

    public void testComments1() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "     private int a, b;"+
                       "     public void test() throws Exception {" +
                       "         String g = null;\n" +
                       "         //comment\n" +
                       "         if (g == \"j\") {//foo1\n" +
                       "             System.err.println(1);\n" +
                       "             //foo2\n" +
                       "         } else if (g == \"k\") {" +
                       "             System.err.println(2);" +
                       "         } else if (g == \"l\") {" +
                       "             System.err.println(3);" +
                       "         }\n" +
                       "     }" +
                       "}",
                       "2:9-2:11:verifier:Convert to switch",
                       "FixImpl",
                       ("package test;" +
                       "public class Test {" +
                       "     private int a, b;"+
                       "     public void test() throws Exception {" +
                       "         String g = null;" +
                       "         //comment\n" +
                       "         switch (g) {\n" +
                       "             case \"j\":\n" +
                       "                 //foo1\n" +
                       "                 System.err.println(1);" +
                       "                 //foo2\n" +
                       "                 break;" +
                       "             case \"k\":\n" +
                       "                 System.err.println(2);" +
                       "                 break;" +
                       "             case \"l\":\n" +
                       "                 System.err.println(3);" +
                       "                 break;" +
                       "         }\n" +
                       "     }" +
                       "}").replaceAll("[ \t\n]+", " "));
    }

    public void testNoEquals() throws Exception {
        setSourceLevel("1.7");

        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch", HintsSettings.getCurrentProfileId());
        boolean oldSetting = p.getBoolean(ConvertToStringSwitch.KEY_ALSO_EQ, ConvertToStringSwitch.DEF_ALSO_EQ);

        try {
            p.putBoolean(ConvertToStringSwitch.KEY_ALSO_EQ, false);
            performAnalysisTest("test/Test.java",
                                "package test;" +
                                "public class Test {" +
                                "     public void test() throws Exception {" +
                                "         String g = null;\n" +
                                "         if (\"j\" == g) {" +
                                "             System.err.println(1);" +
                                "         } else if (\"l\" == g) {" +
                                "             System.err.println(2);" +
                                "         } else {\n" +
                                "             System.err.println(3);" +
                                "         }\n" +
                                "     }" +
                                "}");
        } finally {
            p.putBoolean(ConvertToStringSwitch.KEY_ALSO_EQ, oldSetting);
        }
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }

}