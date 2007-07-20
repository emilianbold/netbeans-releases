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
import org.netbeans.api.java.source.CompilationInfo;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.errors.ChangeType;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;


/**
 *
 * @author Sandip Chitale
 */
public class ChangeTypeTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of ChangeTypeTest */
    public ChangeTypeTest(String name) {
        super(name);
    }
    
    public void testIntToString() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {int i = \"s\";}", 41, "Change type of i to String");
    }
    
    public void testIntToStringFix() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {int i = \"s\";}",
                       41,
                       "Change type of i to String",
                       "package test; public class Test { String i = \"s\";}");
    }
    
    public void testStringToInt() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {String s = 5;}", 44, "Change type of s to int");
    }
    
    public void testStringToIntFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {String s = 5;}",
                44,
                "Change type of s to int",
                "package test; public class Test { int s = 5;}");
    }
    
    public void testStringToObject() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {String s = new Object();}", 44, "Change type of s to Object");
    }

    public void testStringToObjectFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {String s = new Object();}",
                44,
                "Change type of s to Object",
                "package test; public class Test {Object s = new Object();}");
    }
    
    public void testLocalVariableIntToString() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {int i = \"s\";}}", 62, "Change type of i to String");
    }
    
    public void testLocalVariableIntToStringFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {private void test() {int i = \"s\";}}",
                62,
                "Change type of i to String",
                "package test; public class Test {private void test() { String i = \"s\";}}"
                );
    }

    public void testLocalVariableStringToInt() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {String s = 5;}}", 65, "Change type of s to int");
    }
    
    public void testLocalVariableStringToIntFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {private void test() {String s = 5;}}",
                65,
                "Change type of s to int",
                "package test; public class Test {private void test() { int s = 5;}}");
    }

    public void testLocalVariableStringToObject() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {String s = new Object();}}", 65, "Change type of s to Object");
    }
    
    public void testLocalVariableStringToObjectFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {private void test() {String s = new Object();}}",
                65,
                "Change type of s to Object",
                "package test; public class Test {private void test() {Object s = new Object();}}");
    }
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) {
        List<Fix> fixes = new ChangeType().run(info, null, pos, path, null);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof ChangeTypeFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((ChangeTypeFix) f).getText();
    }
    
}
