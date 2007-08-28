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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import org.netbeans.api.java.source.CompilationInfo;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class CreateClassTest extends ErrorHintsTestBase {
    
    public CreateClassTest(String name) {
        super(name);
    }
    
    public void testCreateClassForNewExpression() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1);}}", 114 - 48, "CreateClass:test.NonExisting:[]:CLASS");
    }
    
    public void testCreateClassVariable() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {NonExisting n;}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS");
    }
    
    public void testCreateMemberSelect() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {NonExisting.call();}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS");
    }
    
    public void testPerformCreateClass() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test {public static void test() {NonExisting f;}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS", "test/NonExisting.java", "package test; class NonExisting { public NonExisting() { } } ");
    }
    
    public void testPerformCreateClassParams() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1, \"\");}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS", "test/NonExisting.java", "package test; class NonExisting { public NonExisting(int i, String string) { } } ");
    }
    
    public void disabledtestPerformCreateClassInPackage() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test {public static void test() {new test2.NonExisting(1, \"\");}}", 112 - 48, "CreateClass:test2.NonExisting:[]:CLASS", "test2/NonExisting.java", "package test2; class NonExisting { public NonExisting(int i,String string) { } } ");
    }
    
    public void testCreateInnerClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {Test.NonExisting x;}}", 119- 48, "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }
    
    public void testCreateInnerClassPerform() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {Test.NonExisting x;}}",
                       94 - 25,
                       "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS",
                       "package test; public class Test {public static void test() {Test.NonExisting x;} private static class NonExisting { public NonExisting() { } } }");
    }
    
    public void testCreateInnerClassForNewClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new Test.NonExisting(1);}}", 121- 48, "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }
    
    public void testCreateInnerClassForNewClassPerform() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Test.NonExisting(1);}}",
                       100 - 25,
                       "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS",
                       "package test; public class Test {public static void test() {new Test.NonExisting(1);} private static class NonExisting { public NonExisting(int i) { } } }");
    }
    
    public void testCreateClassTypeParameter() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test<T extends Number&CharSequence> {public static void test() {Test<NonExisting> t;}}", 150 - 48, "CreateClass:test.NonExisting:[]:CLASS");
    }
    
    public void testPerformCreateClassTypeParameter() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test<T extends Number&CharSequence> {public static void test() {Test<NonExisting> t;}}", 150 - 48, "CreateClass:test.NonExisting:[]:CLASS", "test/NonExisting.java", "package test; class NonExisting extends Number implements CharSequence { public NonExisting() { } } ");
    }
    
    public void testPerformCreateInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static class X implements Test.NonExisting {}}",
                       119 - 43,
                       "CreateInnerClass:test.Test.NonExisting:[private, static]:INTERFACE",
                       "package test; public class Test {public static class X implements Test.NonExisting {} private static interface NonExisting { } }");
    }
    
    public void testPerformCreateForExtends() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test {public static class X extends NonExisting {}}",
                            92 - 25,
                            "CreateClass:test.NonExisting:[]:CLASS");
    }
    
    public void testPerformCreateInterfaceGeneric() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static class X implements java.util.concurrent.Future<NonExisting> {}}",
                       123 - 25,
                       "CreateClass:test.NonExisting:[]:CLASS",
                       "test/NonExisting.java",
                       "package test; class NonExisting { public NonExisting() { } } ");
    }
    
    public void testGenericNC1() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Unknown1<Unknown2>();}}",
                       93 - 25,
                       "CreateClass:test.Unknown1:[]:CLASS"
                       );
    }
    
    public void testGenericNC2() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Unknown1<Unknown2>();}}",
                       101 - 25,
                       "CreateClass:test.Unknown2:[]:CLASS"
                       );
    }
    
    public void testDeclWithTypeArguments() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {Unknown1<String> s;}}",
                       88 - 25,
                       "CreateClass:test.Unknown1:[]:CLASS",
                       "test/Unknown1.java",
                       "package test; class Unknown1<T> { public Unknown1() { } } "
                       );
    }
    
    public void testGenericCreateWithTypeArguments() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Unknown1<String>(1);}}",
                       92 - 25,
                       "CreateClass:test.Unknown1:[]:CLASS",
                       "test/Unknown1.java",
                       "package test; class Unknown1<T> { public Unknown1(int i) { } } "
                       );
    }
    
    public void testCreateFromTopLevelClass() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test implements UU {}",
                       69 - 25,
                       "CreateClass:test.UU:[]:INTERFACE");
    }
    
    public void testCreate106773() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public UU g() {return null;}}",
                       66 - 25,
                       "CreateClass:test.UU:[]:CLASS");
    }
    
    public void testCreateFromNewArray() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public Object g() {return new Unknown[0];}}",
                       91 - 25,
                       "CreateClass:test.Unknown:[]:CLASS");
    }
    
    public void testCreate108016() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public void g() {new Runnable() {public void run() {new Runnable() {public void run() {DDD.ddd();}};}};}}",
                       147 - 25,
                       "CreateClass:test.DDD:[]:CLASS");
    }
    
    /**
     * Test offering creation of a new Exception type
     */ 
    public void testCreate113905() throws Exception {
	performAnalysisTest("test/Test.java",
                       "package test; public class Test {public void g() throws FooException{}}", 
		       84 - 25, 
		       "CreateClass:test.FooException:[]:CLASS");
    }
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws IOException {
        List<Fix> fixes = new CreateElement().analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateClassFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateClassFix) f).toDebugString(info);
    }

    @Override
    protected String[] getAdditionalLayers() {
        return new String[] {"org/netbeans/modules/java/hints/errors/empty-class-template.xml"};
    }
    
}
