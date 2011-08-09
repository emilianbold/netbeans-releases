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

import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;

/**
 *
 * @author lahvac
 */
public class RemoveUnnecessaryReturnTest extends TestBase {

    public RemoveUnnecessaryReturnTest(String name) {
        super(name, RemoveUnnecessaryReturn.class);
    }

    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        return ;\n" +
                       "    }\n" +
                       "}\n",
                       "3:8-3:16:verifier:ERR_UnnecessaryReturnStatement",
                       "FixImpl",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testIfNoBlock() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(boolean b) {\n" +
                       "        if (b) return ;\n" +
                       "    }\n" +
                       "}\n",
                       "3:15-3:23:verifier:ERR_UnnecessaryReturnStatement",
                       "FixImpl",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(boolean b) {\n" +
                        "        if (b) { }\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testNeg1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void test(boolean b) {\n" +
                            "        if (b) { return ; }\n" +
                            "        System.err.println();\n" +
                            "    }\n" +
                            "}\n");
    }

    public void testNeg2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case true: if (b) { return ; }\n" +
                            "                       System.err.println();\n" +
                            "                       break;\n" +
                            "    }\n" +
                            "}\n");
    }

    public void testNeg3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case true: if (b) { return ; }\n" +
                            "            case false: System.err.println(); break;\n" +
                            "    }\n" +
                            "}\n");
    }

    public void testNeg4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public int test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case true: if (b) { return 1; }\n" +
                            "            case false: System.err.println(); break;\n" +
                            "    }\n" +
                            "}\n");
    }

    public void testSwitchRemove() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case true: if (b) { return ; } else break;\n" +
                            "            case false: System.err.println(); break;\n" +
                            "    }\n" +
                            "}\n",
                            "4:32-4:40:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testLastCase() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case false: if (b) { return ; }\n" +
                            "    }\n" +
                            "}\n",
                            "4:33-4:41:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testNPE200462a() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case false: if (b) { return ; }\n" +
                            "    }\n" +
                            "}\n",
                            "4:33-4:41:verifier:ERR_UnnecessaryReturnStatement");
    }

    public void testNPE200462b() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test(boolean b) {\n" +
                            "        switch (b) {\n" +
                            "            case false: if (b) { return ; }\n" +
                            "    }\n" +
                            "}\n",
                            "4:33-4:41:verifier:ERR_UnnecessaryReturnStatement");
    }
    
}
