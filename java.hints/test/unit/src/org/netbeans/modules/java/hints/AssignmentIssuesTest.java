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
package org.netbeans.modules.java.hints;

import org.junit.Test;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Dusan Balek
 */
public class AssignmentIssuesTest extends TestBase {

    public AssignmentIssuesTest(String name) {
        super(name, AssignmentIssues.class);
    }

    @Test
    public void testAssignmentToForLoopParam() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            i = 10;"
                + "        }\n"
                + "    }\n"
                + "}",
                "4:12-4:18:verifier:Assignment to for-loop parameter i");
    }

    @Test
    public void testAssignmentToForLoopParamSuppressed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    @SuppressWarnings(\"AssignmentToForLoopParameter\")"
                + "    public static void main(String... args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            i = 10;"
                + "        }\n"
                + "    }\n"
                + "}");
    }

    @Test
    public void testAssignmentToCatchBlockParameter() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        try {\n"
                + "        } catch (Exception e) {\n"
                + "            e = null;"
                + "        }"
                + "    }\n"
                + "}",
                "5:12-5:20:verifier:Assignment to catch-block parameter e");
    }

    @Test
    public void testAssignmentToCatchBlockParameterSuppressed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    @SuppressWarnings(\"AssignmentToCatchBlockParameter\")\n"
                + "    public static void main(String... args) {\n"
                + "        try {\n"
                + "        } catch (Exception e) {\n"
                + "            e = null;"
                + "        }"
                + "    }\n"
                + "}");
    }

    @Test
    public void testAssignmentToMethodParam() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        args = null;"
                + "    }\n"
                + "}",
                "3:8-3:19:verifier:Assignment to method parameter args");
    }

    @Test
    public void testAssignmentToMethodParamSuppressed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    @SuppressWarnings(\"AssignmentToMethodParameter\")\n"
                + "    public static void main(String... args) {\n"
                + "        args = null;"
                + "    }\n"
                + "}");
    }

    @Test
    public void testNestedAssignment() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 10;\n"
                + "        while ((i = 2 + i) > 10) {\n"
                + "        }\n"
                + "    }\n"
                + "}",
                "4:16-4:25:verifier:Nested assignment 'i = 2 + i'");
    }

    @Test
    public void testNestedAssignmentSuppressed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    @SuppressWarnings(\"NestedAssignment\")\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 10;\n"
                + "        while ((i = 2 + i) > 10) {\n"
                + "        }\n"
                + "    }\n"
                + "}");
    }

    @Test
    public void testIncrementDecrementUsed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 10;\n"
                + "        while (i++ > 10) {\n"
                + "        }\n"
                + "    }\n"
                + "}",
                "4:15-4:18:verifier:Value of increment expression 'i++' is used");
    }

    @Test
    public void testIncrementDecrementUsedSuppressed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    @SuppressWarnings(\"ValueOfIncrementOrDecrementUsed\")\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 10;\n"
                + "        while (i++ > 10) {\n"
                + "        }\n"
                + "    }\n"
                + "}");
    }

    @Test
    public void testReplaceAssignWithOpAssign() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 0;\n"
                + "        i = i - 10;\n"
                + "    }\n"
                + "}",
                "4:8-4:18:verifier:Assignment 'i = i - 10' is replacable with operator-assignment",
                "Replace assignment 'i = i - 10' with operator-assignment",
                ("package test;\n"
                + "public class Test {\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 0;\n"
                + "        i -= 10;\n"
                + "    }\n"
                + "}").replaceAll("[ \t\n]+", " "));
    }

    @Test
    public void testReplaceAssignWithOpAssignSuppressed() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n"
                + "public class Test {\n"
                + "    @SuppressWarnings(\"AssignmentReplaceableWithOperatorAssignment\")\n"
                + "    public static void main(String... args) {\n"
                + "        int i = 0;\n"
                + "        i = i - 10;\n"
                + "    }\n"
                + "}");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
}
