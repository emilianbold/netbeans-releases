/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach
 */
public class DoubleCheckTest extends TreeRuleTestBase {
    
    public DoubleCheckTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        SourceUtilsTestUtil.setLookup(new Object[0], getClass().getClassLoader());
    }
    
    
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; public class Test {" +
            "  private static Test INST;" +
            "public static Test factory() {" +
            "  if (INST == null) {" +
            "    synchro";
        String after = "nized (Test.class) {" +
            "      if (INST == null) {" +
            "        INST = new Test();" +
            "      }" +
            "    }" +
            "  }" +
            "  return INST;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:115-0:127:verifier:ERR_DoubleCheck"
        );
    }
    public void testSomeCodeAfterTheOuterIf() throws Exception {
        String before = "package test; public class Test {" +
            "  private static Test INST;" +
            "  private static int cnt;" +
            "public static Test factory() {" +
            "  if (INST == null) {" +
            "    synchro";
        String after = "nized (Test.class) {" +
            "      if (INST == null) {" +
            "        INST = new Test();" +
            "      }" +
            "    }" +
            "    cnt++;" +
            "  }" +
            "  return INST;" +
            "}";
        // no hint, probably
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDifferentVariable() throws Exception {
        String before = "package test; public class Test {" +
            "  private static Test INST;" +
            "  private static Object cnt;" +
            "public static Test factory() {" +
            "  if (cnt == null) {" +
            "    synchro";
        String after = "nized (Test.class) {" +
            "      if (INST == null) {" +
            "        INST = new Test();" +
            "      }" +
            "    }" +
            "  }" +
            "  return INST;" +
            "}";
        // no hint, for sure
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testNoNPEWhenBrokenCondition() throws Exception {
        String before = "package test; public class Test {" +
            "  private static Test INST;" +
            "  private static Object cnt;" +
            "public static Test factory() {" +
            "  if (INST == nu) {" +
            "    synchro";
        String after = "nized (Test.class) {" +
            "      if (INST == nu) {" +
            "        INST = new Test();" +
            "      }" +
            "    }" +
            "  }" +
            "  return INST;" +
            "}";
        // no hint, for sure
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testApplyClassWithOnlyStaticMethods() throws Exception {
        String before1 = "package test; public class Test {\n" +
            "private static Test INST;\n" +
            "public static Test factory() {\n";
        String before2 = 
              "if (INST == null) {\n";
        String before3 =
                "synchro";
        String after1 = "nized (INST) {\n" +
                  "if (INST == null) {\n" +
                    "INST = new test.Test();\n" +
                  "}\n" +
                "}\n";
        String after2 =
               "}\n";
        String after3 =
              "return INST;\n" +
            "}\n";
        String after4 = "\n";
        
        String before = before1 + before2 + before3;
        String after = after1 + after2 + after3 + after4; 
        
        String golden = (before1 + before3 + after1 + after3).replace("\n", " ");
        performFixTest("test/Test.java", before + after, before.length(), 
            "4:0-4:12:verifier:ERR_DoubleCheck",
            "FIX_DoubleCheck",
            golden
        );
    }
    public void testVolatileJDK5IZ153334() throws Exception {
        String code = "package test; public class Test {\n" +
            "private static volatile Test INST;\n" +
            "public static Test factory() {\n" +
              "if (INST == null) {\n" +
                "synchro|nized (INST) {\n" +
                  "if (INST == null) {\n" +
                    "INST = new test.Test();\n" +
                  "}\n" +
                "}\n" +
               "}\n" +
              "return INST;\n" +
            "}\n";
        
        performAnalysisTest("test/Test.java", code);
    }

    public void testVolatileJDK4IZ153334() throws Exception {
        sourceLevel = "1.4";
        
        String code = "package test; public class Test {\n" +
            "private static volatile Test INST;\n" +
            "public static Test factory() {\n" +
              "if (INST == null) {\n" +
                "synchro|nized (INST) {\n" +
                  "if (INST == null) {\n" +
                    "INST = new test.Test();\n" +
                  "}\n" +
                "}\n" +
               "}\n" +
              "return INST;\n" +
            "}\n";

        performAnalysisTest("test/Test.java",
                            code,
                            "4:0-4:12:verifier:ERR_DoubleCheck");
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new DoubleCheck().run(info, path);
    }
    
    private String sourceLevel = "1.5";

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }
    
}
