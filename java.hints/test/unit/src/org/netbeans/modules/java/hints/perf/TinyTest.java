/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.perf;

import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class TinyTest extends TestBase {

    public TinyTest(String name) {
        super(name, Tiny.class);
    }

    public void testStringConstructor1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa);\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:30:verifier:new String(...)",
                       "Remove new String(...)",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private String test(String aa) {\n" +
                        "         return aa;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testStringConstructor2() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", HintsSettings.getCurrentProfileId());

        p.putBoolean(Tiny.SC_IGNORE_SUBSTRING, true);

        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private String test(String aa) {\n" +
                            "         return new String(aa.substring(1));\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testStringConstructor3() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", HintsSettings.getCurrentProfileId());

        p.putBoolean(Tiny.SC_IGNORE_SUBSTRING, false);

        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa.substring(1));\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:43:verifier:new String(...)",
                       "Remove new String(...)",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private String test(String aa) {\n" +
                        "         return aa.substring(1);\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testStringConstructor4() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", HintsSettings.getCurrentProfileId());

        p.putBoolean(Tiny.SC_IGNORE_SUBSTRING, true);

        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(this.substring(1));\n" +
                       "     }\n" +
                       "     private String substring(int i) {return null;}\n" +
                       "}\n",
                       "3:16-3:45:verifier:new String(...)",
                       "Remove new String(...)",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private String test(String aa) {\n" +
                        "         return this.substring(1);\n" +
                        "     }\n" +
                       "     private String substring(int i) {return null;}\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }


    public void testStringEqualsEmpty1SL15() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:29:verifier:$string.equals(\"\")",
                       "$string.length() == 0",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private boolean test(String aa) {\n" +
                       "         return aa.length() == 0;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testStringEqualsEmpty2SL15() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return !aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "3:17-3:30:verifier:$string.equals(\"\")",
                       "$string.length() != 0",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private boolean test(String aa) {\n" +
                       "         return aa.length() != 0;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    //XXX: test the 1.6 version

    public void testLengthOneStringIndexOf1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"a\") != 0;\n" +
                       "     }\n" +
                       "}\n",
                       "3:27-3:30:verifier:indexOf(\"a\")",
                       "indexOf('.')",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\'a\') != 0;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testLengthOneStringIndexOf2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"'\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n",
                       "3:27-3:30:verifier:indexOf(\"'\")",
                       "indexOf('.')",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\'\\'\', 2) != 0;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testLengthOneStringIndexOf3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\\"\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n",
                       "3:27-3:31:verifier:indexOf(\"\\\"\")",
                       "indexOf('.')",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private boolean test(String aa) {\n" +
                        "         return aa.indexOf(\'\"\', 2) != 0;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testGetClassInsteadOfDotClass1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private Class<?> test() {\n" +
                       "         return new String().getClass();\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:39:verifier:ERR_GetClassInsteadOfDotClass",
                       "FIX_GetClassInsteadOfDotClass",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private Class<?> test() {\n" +
                        "         return String.class;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testGetClassInsteadOfDotClass2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private Class<?> test() {\n" +
                       "         return new LinkedList(java.util.Arrays.asList(1, 2)).getClass();\n" +
                       "     }\n" +
                       "}\n",
                       "4:16-4:72:verifier:ERR_GetClassInsteadOfDotClass",
                       "FIX_GetClassInsteadOfDotClass",
                       ("package test;\n" +
                        "import java.util.LinkedList;\n" +
                        "public class Test {\n" +
                        "     private Class<?> test() {\n" +
                        "         return LinkedList.class;\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testConstantIntern1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return \"foo-bar\".intern();\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:34:verifier:ERR_ConstantIntern",
                       "FIX_ConstantIntern",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return \"foo-bar\";\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testConstantIntern2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return (\"foo\" + \"-\" + \"bar\").intern();\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:46:verifier:ERR_ConstantIntern",
                       "FIX_ConstantIntern",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return \"foo\" + \"-\" + \"bar\";\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testConstantIntern3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         return (\"foo\" + \"-\" + \"bar\").intern().length();\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:46:verifier:ERR_ConstantIntern",
                       "FIX_ConstantIntern",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private int test() {\n" +
                        "         return (\"foo\" + \"-\" + \"bar\").length();\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testEnumSet1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private java.util.Set<java.lang.annotation.RetentionPolicy> test() {\n" +
                            "         return new java.util.HashSet<java.lang.annotation.RetentionPolicy>();\n" +
                            "     }\n" +
                            "}\n",
                            "3:16-3:77:verifier:ERR_Tiny_enumSet");
    }

    public void testEnumMap1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                            "         return new java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean>();\n" +
                            "     }\n" +
                            "}\n",
                            "3:16-3:86:verifier:ERR_Tiny_enumMap");
    }

    public void testEnumMap2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                            "         return new java.util.EnumMap<java.lang.annotation.RetentionPolicy, Boolean>(java.lang.annotation.RetentionPolicy.class);\n" +
                            "     }\n" +
                            "}\n");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}