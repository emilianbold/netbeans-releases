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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class ManualArrayCopyTest extends TestBase {

    public ManualArrayCopyTest(String name) {
        super(name, ManualArrayCopy.class);
    }

    public void testArrayCopy1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        int[] target = new int[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 0; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "7:8-7:11:verifier:ERR_manual-array-copy",
                       "FIX_manual-array-copy",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(String[] args) {\n" +
                        "        int[] source = new int[3];\n" +
                        "        int[] target = new int[6];\n" +
                        "        int o = 3;\n" +
                        "\n" +
                        "        System.arraycopy(source, 0, target, o, source.length);\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testArrayCopy2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        int[] target = new int[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 2; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "7:8-7:11:verifier:ERR_manual-array-copy",
                       "FIX_manual-array-copy",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(String[] args) {\n" +
                        "        int[] source = new int[3];\n" +
                        "        int[] target = new int[6];\n" +
                        "        int o = 3;\n" +
                        "\n" +
                        "        System.arraycopy(source, 2, target, o + 2, source.length - 2);\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testArrayCopy3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        int[] target = new int[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 2; i < source.length; i++) {\n" +
                       "            target[i + o] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "7:8-7:11:verifier:ERR_manual-array-copy",
                       "FIX_manual-array-copy",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test(String[] args) {\n" +
                        "        int[] source = new int[3];\n" +
                        "        int[] target = new int[6];\n" +
                        "        int o = 3;\n" +
                        "\n" +
                        "        System.arraycopy(source, 2, target, 2 + o, source.length - 2);\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testArrayCollectionCopy1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        List<String> l = null;\n" +
                       "\n" +
                       "        for (int c = 0; c < args.length; c++) {\n" +
                       "            l.add(args[c]);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "5:8-5:11:verifier:ERR_manual-array-copy-coll",
                       "FIX_manual-array-copy-coll",
                       ("package test;\n" +
                        "import java.util.Arrays;\n" +
                        "public class Test {\n" +
                        "    public void test(String[] args) {\n" +
                        "        List<String> l = null;\n" +
                        "\n" +
                        "        l.addAll(Arrays.asList(args));\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testArrayCollectionCopy2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        List<String> l = null;\n" +
                       "\n" +
                       "        for (String s : args) {\n" +
                       "            l.add(s);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "5:8-5:11:verifier:ERR_manual-array-copy-coll",
                       "FIX_manual-array-copy-coll",
                       ("package test;\n" +
                        "import java.util.Arrays;\n" +
                        "public class Test {\n" +
                        "    public void test(String[] args) {\n" +
                        "        List<String> l = null;\n" +
                        "\n" +
                        "        l.addAll(Arrays.asList(args));\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}