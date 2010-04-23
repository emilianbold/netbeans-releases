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
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class PointlessBitwiseExpressionTest extends TestBase {

    public PointlessBitwiseExpressionTest (String name) {
        super (name, PointlessBitwiseExpression.class);
    }

    @Test
    public void test1 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i & 0;\n" +
            "    }\n" +
            "}",
            "4:20-4:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test2 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = 0 & i;\n" +
            "    }\n" +
            "}",
            "4:20-4:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test3 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i | 0;\n" +
            "    }\n" +
            "}",
            "4:20-4:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test4 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = 0 | i;\n" +
            "    }\n" +
            "}",
            "4:20-4:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_const_1 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i & T;\n" +
            "    }\n" +
            "}",
            "5:20-5:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_const_2 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = T & i;\n" +
            "    }\n" +
            "}",
            "5:20-5:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_const_3 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i | T;\n" +
            "    }\n" +
            "}",
            "5:20-5:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_const_4 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = T | i;\n" +
            "    }\n" +
            "}",
            "5:20-5:25:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_sh_1 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i >> 0;\n" +
            "    }\n" +
            "}",
            "4:20-4:26:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_sh_const_1 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i >> T;\n" +
            "    }\n" +
            "}",
            "5:20-5:26:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_sh_2 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i >>> 0;\n" +
            "    }\n" +
            "}",
            "4:20-4:27:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_sh_const_2 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i >>> T;\n" +
            "    }\n" +
            "}",
            "5:20-5:27:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_sh_3 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i << 0;\n" +
            "    }\n" +
            "}",
            "4:20-4:26:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    @Test
    public void test_sh_const_3 () throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    static final int T = 0;\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i << T;\n" +
            "    }\n" +
            "}",
            "5:20-5:26:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }

    public void test184758a() throws Exception {
        performAnalysisTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
	    "        int modifiers = 0;\n" +
	    "        boolean addFlag = true;\n" +
	    "        modifiers = modifiers | (addFlag ? 1 : 0);\n" +
            "    }\n" +
            "}");
    }

    public void test184758b() throws Exception {
        performFixTest (
            "test/Test.java",
            "package test;\n" +
            "class Test {\n" +
            "    void test () {\n" +
            "        int i = 10;\n" +
            "        boolean b = i << (0+0);\n" +
            "    }\n" +
            "}",
            "4:20-4:30:verifier:Pointless bitwise expression",
            "FixImpl",
            (
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        boolean b = i;\n" +
                "    }\n" +
                "}"
            ).replaceAll ("[ \t\n]+", " ")
        );
    }
    
    static {
        NbBundle.setBranding ("test");
    }
}