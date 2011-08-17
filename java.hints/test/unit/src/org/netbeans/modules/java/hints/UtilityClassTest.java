/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
public class UtilityClassTest extends TreeRuleTestBase {
    
    public UtilityClassTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        SourceUtilsTestUtil.setLookup(new Object[0], getClass().getClassLoader());
    }
    
    
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public static boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:27-0:31:verifier:Utility class without constructor"
        );
    }
    public void testClassWithOnlyStaticMethodsAndFields() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public static boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            " public static final String PROP_X = null;";
            ;
        
        String gold = before + after + " private Test() { } }";
        performFixTest("test/Test.java", before + after + "}", before.length(), 
            "0:27-0:31:verifier:Utility class without constructor",
            "MSG_PrivateConstructor",
            gold
        );
    }
    public void testDisabledWhenNoMethodIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledWhenMehtodIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledExtendingNonObject() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends javax.swing.JPanel {" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledWhenConstructorIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public Test() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testNoExceptionForVeryBrokenClass() throws Exception {
        String before = "package test; public class Test { private static final cla";
        String after = "ss private static final class A{} }";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    
    public void testDisabledOnEnums() throws Exception {
        String before = "package test; public enum Te";
        String after = "st {" +
            " ONE, TWO;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledOnInterfaces() throws Exception {
        String before = "package test; public interface Te";
        String after = "st {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledOnAnnotations() throws Exception {
        String before = "package test; public @interface Te";
        String after = "st {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testMultipleConstructors() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Te|st {" +
                            "    public Test() { }" +
                            "    public Test(int i) { }" +
                            "}");
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return UtilityClass.withoutConstructor().run(info, path);
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
