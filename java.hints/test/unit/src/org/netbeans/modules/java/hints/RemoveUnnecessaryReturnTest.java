/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class RemoveUnnecessaryReturnTest extends NbTestCase {

    public RemoveUnnecessaryReturnTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        return ;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .findWarning("3:8-3:16:verifier:ERR_UnnecessaryReturnStatement")
                .applyFix("FIX_UnnecessaryReturnStatement")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test() {\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testIfNoBlock() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        if (b) return ;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .findWarning("3:15-3:23:verifier:ERR_UnnecessaryReturnStatement")
                .applyFix("FIX_UnnecessaryReturnStatement")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(boolean b) {\n" +
                              "        if (b) { }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        if (b) { return ; }\n" +
                       "        System.err.println();\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void testNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; }\n" +
                       "                    System.err.println();\n" +
                       "                    break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void testNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; }\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void testNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public int test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return 1; }\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "        return 0;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void testNegCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { return ; }\n" +
                       "            case 1: { System.err.println(1); break; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void testSwitchRemove1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; } break;\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:34-4:42:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testSwitchRemove2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } break; };\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:36-4:44:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testSwitchRemove3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } };\n" +
                       "            case 1: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:36-4:44:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testSwitchRemove4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } };\n" +
                       "            case 1: { ; break; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:36-4:44:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testLastCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:34-4:42:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testNPE200462a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:34-4:42:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testNPE200462b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("4:34-4:42:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testInLoop201393() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int i) {\n" +
                       "        while (i-- > 0) {\n" +
                       "            System.err.println(1);\n" +
                       "            if (i == 3) return ;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void testNegFinally203576() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException(\"NullPointerException 1\");\n" +
                       "        } catch (NullPointerException e) {\n" +
                       "            throw new NullPointerException(\"NullPointerException 2\");\n" +
                       "        } finally {\n" +
                       "            System.out.println(\"Do I ever get printed?\");\n" +
                       "            return;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings();
    }

    public void TODOtestPosFinally203576() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException(\"NullPointerException 1\");\n" +
                       "        } catch (NullPointerException e) {\n" +
                       "            throw new NullPointerException(\"NullPointerException 2\");\n" +
                       "        } finally {\n" +
                       "            if (args.length == 0) { return ; }\n" +
                       "            return;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessaryReturn.class)
                .assertWarnings("<missing>");
    }
}
