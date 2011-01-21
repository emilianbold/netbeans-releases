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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class TinyTest extends TestBase {

    public TinyTest(String name) {
        super(name, Tiny.class);
    }

    public void testSimpleFlip() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private boolean test(List l) {\n" +
                       "         return l.e|quals(this);\n" +
                       "     }\n" +
                       "}\n",
                       "3:18-3:24:hint:Flip .equals",
                       "Flip .equals",
                       ("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private boolean test(List l) {\n" +
                        "         return this.equals(l);\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testFlipImplicitThis() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private boolean test(List l) {\n" +
                       "         return e|quals(l);\n" +
                       "     }\n" +
                       "}\n",
                       "3:16-3:22:hint:Flip .equals",
                       "Flip .equals",
                       ("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private boolean test(List l) {\n" +
                        "         return l.equals(this);\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }
    
    public void testConvertBase1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = 1|8;\n" +
                       "}\n",
                       "2:27-2:29:hint:ERR_convertToDifferentBase",
                       "FIX_convertToDifferentBase_16",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private final int I = 0x12;\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }
    
    public void testConvertBase2() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = 1|8;\n" +
                       "}\n",
                       "2:27-2:29:hint:ERR_convertToDifferentBase",
                       "FIX_convertToDifferentBase_2",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private final int I = 0b10010;\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }
    
    public void testConvertBaseLong() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = 42|94967296L;\n" +
                       "}\n",
                       "2:28-2:39:hint:ERR_convertToDifferentBase",
                       "FIX_convertToDifferentBase_16",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private final long I = 0x100000000L;\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }
    
    public void testConvertBaseNegative1() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = -42|94967296L;\n" +
                       "}\n",
                       "2:28-2:40:hint:ERR_convertToDifferentBase",
                       "FIX_convertToDifferentBase_16",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private final long I = 0xffffffff00000000L;\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }
    
    public void testConvertBaseNegative2() throws Exception {
        setSourceLevel("1.7");
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = 0xffffffff|00000000L;\n" +
                       "}\n",
                       "2:28-2:47:hint:ERR_convertToDifferentBase",
                       "FIX_convertToDifferentBase_10",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "     private final long I = -4294967296L;\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    //TODO: can be generalized?
    @Override
    protected void prepareTest(String fileName, String code) throws Exception {
        int caret = code.indexOf('|');

        assertTrue(String.valueOf(caret), caret >= 0);
        super.prepareTest(fileName, code.substring(0, caret) + code.substring(caret + 1));
        setTestFileCaretLocation(caret);
    }

}