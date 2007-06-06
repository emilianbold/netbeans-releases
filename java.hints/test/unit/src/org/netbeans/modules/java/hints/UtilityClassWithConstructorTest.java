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
public class UtilityClassWithConstructorTest extends TreeRuleTestBase {
    
    public UtilityClassWithConstructorTest(String testName) {
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
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testClassWithOnlyStaticMethodsAndFields() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public static boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            " public static final String PROP_X = null;";
            ;
        
        String gold = before + after + " private Test() { } }";
        performAnalysisTest("test/Test.java", before + after + "}", before.length());
    }
    public void testDisabledWhenMehtodIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testEnabledWhenConstructorIsThere() throws Exception {
        String before = "package test; public class Test extends Object {" +
            " public Te";
        String after = "st() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        String golden = (before + after).replace("public Test()", "private Test()");
        performFixTest("test/Test.java", before + after, before.length(), 
            "0:56-0:60:hint:Utility class with visible constructor",
            "FixUtilityClass",
            golden
        );
    }
    public void testDisabledWhenPrivateConstructorIsThere() throws Exception {
        String before = "package test; public class Test extends Object {" +
            " private Te";
        String after = "st() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledWhenPackagePrivateConstructorIsThere() throws Exception {
        String before = "package test; public class Test extends Object {" +
            " Te";
        String after = "st() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return UtilityClass.withConstructor().run(info, path);
    }
    
    private String sourceLevel = "1.5";
    
}
