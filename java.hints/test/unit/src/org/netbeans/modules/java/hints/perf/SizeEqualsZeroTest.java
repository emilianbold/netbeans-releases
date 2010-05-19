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
public class SizeEqualsZeroTest extends TestBase {

    public SizeEqualsZeroTest(String name) {
        super(name, SizeEqualsZero.class);
    }

    public void testSimple1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.size() == 0;\n" +
                       "     }\n" +
                       "}\n",
                       "3:21-3:34:verifier:.size() == 0",
                       ".size() == 0 => .isEmpty()",
                       ("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.isEmpty();\n" +
                       "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testSimple2() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.perf.SizeEqualsZero", HintsSettings.getCurrentProfileId());

        p.putBoolean(SizeEqualsZero.CHECK_NOT_EQUALS, true);
        
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.size() != 0;\n" +
                       "     }\n" +
                       "}\n",
                       "3:21-3:34:verifier:.size() != 0",
                       ".size() != 0 => !.isEmpty()",
                       ("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private void test(List l) {\n" +
                        "         boolean b = !l.isEmpty();\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testSimpleConfig() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.perf.SizeEqualsZero", HintsSettings.getCurrentProfileId());

        p.putBoolean(SizeEqualsZero.CHECK_NOT_EQUALS, false);
        
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "import java.util.List;" +
                            "public class Test {\n" +
                            "     private void test(List l) {\n" +
                            "         boolean b = l.size() != 0;\n" +
                            "     }\n" +
                            "}\n");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}