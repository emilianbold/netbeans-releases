/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk;

import java.util.Collections;
import java.util.prefs.Preferences;
import org.netbeans.modules.java.hints.analyzer.OverridePreferences;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.options.HintsSettings;

/**
 *
 * @author lahvac
 */
public class AddUnderscoresTest extends TestBase {
    public AddUnderscoresTest(String name) {
        super(name, AddUnderscores.class);
    }

    public void testSimpleAdd() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 12345678;\n" +
                       "}\n",
                       "2:37-2:45:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 12_345_678;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testSettings() throws Exception {
        AddUnderscores.setSizeForRadix(prefs, 2, 5);
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0B1010101010101010;\n" +
                       "}\n",
                       "2:37-2:55:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0B1_01010_10101_01010;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testHexLong() throws Exception {
        AddUnderscores.setSizeForRadix(prefs, 16, 3);
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0xA5A5A5A5A5A5A5A5L;\n" +
                       "}\n",
                       "2:37-2:56:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0xA_5A5_A5A_5A5_A5A_5A5L;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testAlreadyHasUnderscores1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private static final int CONST = 0xA5A5A5A5_A5A5A5A5L;\n" +
                            "}\n");
    }

    public void testAlreadyHasUnderscores2() throws Exception {
        AddUnderscores.setReplaceLiteralsWithUnderscores(prefs, true);
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0xA5A5A5A5A5A5A5A_5L;\n" +
                       "}\n",
                       "2:37-2:57:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0xA5A5_A5A5_A5A5_A5A5L;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testZeroIsNotOctal() throws Exception {
        assertEquals(10, AddUnderscores.radixInfo("0").radix);
        assertEquals(10, AddUnderscores.radixInfo("0L").radix);
    }

    public void testIgnoreOctalConstantsForNow() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private static final int CONST = 0123;\n" +
                            "}\n");
    }

    private Preferences prefs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        prefs = new OverridePreferences(RulesManager.getPreferences(AddUnderscores.ID, HintsSettings.getCurrentProfileId()));
        HintsSettings.setPreferencesOverride(Collections.singletonMap(AddUnderscores.ID, prefs));

        setSourceLevel("1.7");
    }

    @Override
    protected void tearDown() throws Exception {
        HintsSettings.setPreferencesOverride(Collections.<String, Preferences>emptyMap());
        prefs = null;
        super.tearDown();
    }

}
