/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

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
            "0:115-0:127:verifier:Remove the outer conditional statement"
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
                    "INST = new Test();\n" +
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
            "4:0-4:12:verifier:Remove the outer conditional statement",
            "FixDoubleCheck",
            golden
        );
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new DoubleCheck().run(info, path);
    }
    
    private String sourceLevel = "1.5";
    
}
