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
public class StaticAccessTest extends TreeRuleTestBase {
    
    public StaticAccessTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        SourceUtilsTestUtil.setLookup(new Object[0], getClass().getClassLoader());
    }
    
    
    
    public void testCallingStaticMethodInInitializer() throws Exception {
        String before = "package test; class Test {\n" +
            "{\n" +
            "Boolean b = null;\n" +
            "b = b.valu";
        String after = "eOf(true);\n" +
            "}\n" +
            "}\n";

        String golden = (before + after).replace('\n', ' ').replace("b.value", "Boolean.value");
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:4-3:5:verifier:AS1valueOf",
            "MSG_StaticAccessText",
            golden
        );
    }
    
    public void testCallingStaticMethod() throws Exception {
        String before = "package test; class Test {\n" +
            "public void nic() {\n" +
            "Boolean b = null;\n" +
            "b = b.valu";
        String after = "eOf(true);\n" +
            "}\n" +
            "}\n";

        String golden = ("package test; class Test {\n" +
            "public void nic() {\n" +
            "Boolean b = null;\n" +
            "b = Boolean.valueOf(true);\n" +
            "}\n" +
            "}\n").replace('\n', ' ');
        
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:4-3:5:verifier:AS1valueOf",
            "MSG_StaticAccessText",
            golden
        );
    }
    
    public void testCallingNonStaticStringMethod() throws Exception {
        String before = "package test; class Test {\n" +
            "public void nic() {\n" +
            "String s = \"some\";\n" +
            "int x = s.last";
        String after = "IndexOf('x');" +
            "}" +
            "";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    
    
    public void testOkCallingStaticMethod() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = Boolean.valu";
        String after = "eOf(true);";
        
        performAnalysisTest("test/Test.java", before + after, before.length()); 
    }
    public void testAccessingStaticField() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = b.TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:50-0:51:verifier:AS0TRUE"
        );
    }
    public void testOkAccessingStaticField() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = Boolean.TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testAccessingStaticFieldViaMethod() throws Exception {
        String before = "package test; class Test {" +
            "static Boolean b() { return null; }" +
            "{" +
            " Object x = b().TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:74-0:77:verifier:AS0TRUE"
        );
    }
    public void testOkToCallEqualsOnString() throws Exception {
        String before = "package test; class Test {" +
            "public void run() {\n" +
            "String s = null;\n" +
            "boolean b = \"A\".e";
        String after =         "quals(s);\n" +
            "}" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testCorrectResolution() throws Exception {
        String before = "package test; class Test {" +
            "public void run() {\n" +
            "Test t = null;\n" +
            "t.t";
        String after =         "est(2);\n" +
            "}\n" +
            "public void test() {}\n" + 
            "public static void test(int i) {}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(),
                "2:0-2:1:verifier:AS1test"
        );
    }
    
    public void testIgnoreErrors1() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "aaa.getClass().getNa|me();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors2() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "aaa.getCl|ass();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors3() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(String aaa) {\n" +
            "aaa.ff|f();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors4() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "super.ff|f();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new StaticAccess().run(info, path);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
    
    private String sourceLevel = "1.5";
    
    static {
        NbBundle.setBranding("test");
    }
    
}
