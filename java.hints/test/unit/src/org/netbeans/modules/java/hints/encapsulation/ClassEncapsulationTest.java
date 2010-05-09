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

package org.netbeans.modules.java.hints.encapsulation;

import java.util.prefs.Preferences;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.options.HintsSettings;

/**
 *
 * @author Tomas Zezula
 */
public class ClassEncapsulationTest extends TestBase {

    public ClassEncapsulationTest(final String name) {
        super(name, ClassEncapsulation.class);
    }

    public void testPublic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public class Inner {}\n"+
                            "}",
                            "2:17-2:22:verifier:Public Inner Class");
    }

    public void testProtected() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    protected class Inner {}\n"+
                            "}",
                            "2:20-2:25:verifier:Protected Inner Class");
    }

    public void testPackage() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    class Inner {}\n"+
                            "}",
                            "2:10-2:15:verifier:Package Visible Inner Class");
    }

    public void testPrivate() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private class Inner {}\n"+
                            "}");
    }

    public void testPublicStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public static class Inner {}\n"+
                            "}",
                            "2:24-2:29:verifier:Public Inner Class");
    }

    public void testProtectedStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    protected static class Inner {}\n"+
                            "}",
                            "2:27-2:32:verifier:Protected Inner Class");
    }

    public void testPackageStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    static class Inner {}\n"+
                            "}",
                            "2:17-2:22:verifier:Package Visible Inner Class");
    }

    public void testPrivateStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private static class Inner {}\n"+
                            "}");
    }

    public void testOuther() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "}\n"+
                            "class Outher {}\n");
    }

    public void testLocal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void foo() {\n"+
                            "        class Local {};\n"+
                            "    }\n"+
                            "}");
    }

    public void testEnumIgnore() throws Exception {
        Preferences p = RulesManager.getPreferences(ClassEncapsulation.class.getName() + ".publicCls", HintsSettings.getCurrentProfileId());

        p.putBoolean(ClassEncapsulation.ALLOW_ENUMS_KEY, true);

        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public enum E {A}\n" +
                            "}");
    }
}
