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

        String golden = "package test; class Test {" +
            "{" +
            "Boolean b = null;" +
            "b = Boolean.valueOf(true);";
        
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:4-3:5:verifier:Accessing static field",
            "FixStaticAccess",
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
            "3:4-3:5:verifier:Accessing static field",
            "FixStaticAccess",
            golden
        );
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
            "0:50-0:51:verifier:Accessing static field"
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
            "0:74-0:77:verifier:Accessing static field"
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

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new StaticAccess().run(info, path);
    }
    
    private String sourceLevel = "1.5";
    
}
