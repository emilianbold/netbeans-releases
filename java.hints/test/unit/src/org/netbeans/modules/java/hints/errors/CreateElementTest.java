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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.errors.AddParameterOrLocalFix;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;


/**
 *
 * @author Jan Lahoda
 */
public class CreateElementTest extends HintsTestBase {
    
    /** Creates a new instance of CreateElementTest */
    public CreateElementTest(String name) {
        super(name);
    }
    
    public void testBinaryOperator() throws Exception {
        Set<String> golden = new HashSet<String>(Arrays.asList(
            "CreateFieldFix:p:org.netbeans.test.java.hints.BinaryOperator:int:[private, static]",
            "AddParameterOrLocalFix:p:int:true",
            "AddParameterOrLocalFix:p:int:false"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 218, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 255, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 294, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.BinaryOperator", 333, golden);
    }
    
    public void testEnhancedForLoop() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.EnhancedForLoop", 186, new HashSet<String>(Arrays.asList(
            "CreateFieldFix:u:org.netbeans.test.java.hints.EnhancedForLoop:java.lang.Iterable<java.lang.String>:[private, static]",
            "AddParameterOrLocalFix:u:java.lang.Iterable<java.lang.String>:true",
            "AddParameterOrLocalFix:u:java.lang.Iterable<java.lang.String>:false"
        )));
        
//        performTestAnalysisTest("org.netbeans.test.java.hints.EnhancedForLoop", 244, new HashSet<String>(Arrays.asList(
//                "CreateFieldFix:u:org.netbeans.test.java.hints.EnhancedForLoop:java.lang.Iterable<java.util.List<? extends java.lang.String>>:[private, static]",
//                "AddParameterOrLocalFix:u:java.lang.Iterable<java.util.List<? extends java.lang.String>>:true",
//                "AddParameterOrLocalFix:u:java.lang.Iterable<java.util.List<? extends java.lang.String>>:false"
//        )));
    }
    
    public void testArrayAccess() throws Exception {
        Set<String> simpleGoldenWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.ArrayAccess:int[]:[private, static]",
                "AddParameterOrLocalFix:x:int[]:true",
                "AddParameterOrLocalFix:x:int[]:false"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 170, simpleGoldenWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 188, simpleGoldenWithLocal);
        
        Set<String> simpleGoldenWithoutLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.ArrayAccess:int[]:[private]"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 262, simpleGoldenWithoutLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 283, simpleGoldenWithoutLocal);
        
        Set<String> indexGoldenWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:u:org.netbeans.test.java.hints.ArrayAccess:int:[private, static]",
                "AddParameterOrLocalFix:u:int:true",
                "AddParameterOrLocalFix:u:int:false"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 335, indexGoldenWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 377, indexGoldenWithLocal);
        
        Set<String> indexGoldenWithoutLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:u:org.netbeans.test.java.hints.ArrayAccess:int:[private]"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 359, indexGoldenWithoutLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 401, indexGoldenWithoutLocal);
        
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayAccess", 442, new HashSet<String>(Arrays.asList(
                "CreateFieldFix:s:org.netbeans.test.java.hints.ArrayAccess:java.lang.Object[][]:[private]"
        )));
    }
    
    public void testAssignment() throws Exception {
        Set<String> golden = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.Assignment:int:[private, static]",
                "AddParameterOrLocalFix:x:int:true",
                "AddParameterOrLocalFix:x:int:false"
                ));
        performTestAnalysisTest("org.netbeans.test.java.hints.Assignment", 174, golden);
        performTestAnalysisTest("org.netbeans.test.java.hints.Assignment", 186, golden);
    }
    
    public void testVariableDeclaration() throws Exception {
        Set<String> golden = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.VariableDeclaration:int:[private, static]",
                "AddParameterOrLocalFix:x:int:true",
                "AddParameterOrLocalFix:x:int:false"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.VariableDeclaration", 186, golden);
    }
    
    public void testAssert() throws Exception {
        Set<String> goldenC = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:c:org.netbeans.test.java.hints.Assert:boolean:[private, static]",
                "AddParameterOrLocalFix:c:boolean:true",
                "AddParameterOrLocalFix:c:boolean:false"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Assert", 159, goldenC);
        
        Set<String> goldenS = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:s:org.netbeans.test.java.hints.Assert:java.lang.Object:[private, static]",
                "AddParameterOrLocalFix:s:java.lang.Object:true",
                "AddParameterOrLocalFix:s:java.lang.Object:false"
                ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Assert", 163, goldenS);
    }
    
    public void testParenthesis() throws Exception {
        Set<String> goldenC = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:x:org.netbeans.test.java.hints.Parenthesis:int[][]:[private]"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Parenthesis", 203, goldenC);
    }
    
    public void testIfAndLoops() throws Exception {
        Set<String> simple = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.IfAndLoops:boolean:[private, static]",
                "AddParameterOrLocalFix:a:boolean:true",
                "AddParameterOrLocalFix:a:boolean:false"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 194, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 247, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 309, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 368, simple);
        
        Set<String> complex = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.IfAndLoops:boolean[]:[private]"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 214, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 270, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 336, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.IfAndLoops", 395, complex);
    }
    
    public void testTarget() throws Exception {
        Set<String> simple = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.Target:int:[private, static]",
                "AddParameterOrLocalFix:a:int:true",
                "AddParameterOrLocalFix:a:int:false"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 186, simple);
        
        Set<String> complex = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.Target:int:[private]"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 203, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 224, complex);
        performTestAnalysisTest("org.netbeans.test.java.hints.Target", 252, complex);
    }
    
    public void testMemberSelect() throws Exception {
        Set<String> simpleWithStatic = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.MemberSelect:int:[private, static]"
        ));
        Set<String> simple = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:a:org.netbeans.test.java.hints.MemberSelect:int:[private]"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 236, simpleWithStatic);
        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 268, simpleWithStatic);
        
        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 290, simple);
        performTestAnalysisTest("org.netbeans.test.java.hints.MemberSelect", 311, simple);
    }
    
    public void testSimple() throws Exception {
        Set<String> simpleJLOWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Object:[private, static]",
                "AddParameterOrLocalFix:e:java.lang.Object:true",
                "AddParameterOrLocalFix:e:java.lang.Object:false"
        ));
        Set<String> simpleJLO = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Object:[private]"
        ));
        Set<String> simpleJLEWithLocal = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Exception:[private, static]",
                "AddParameterOrLocalFix:e:java.lang.Exception:true",
                "AddParameterOrLocalFix:e:java.lang.Exception:false"
        ));
        Set<String> simpleJLE = new HashSet<String>(Arrays.asList(
                "CreateFieldFix:e:org.netbeans.test.java.hints.Simple:java.lang.Exception:[private]"
        ));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 192, simpleJLEWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 211, simpleJLE);
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 245, simpleJLOWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 275, simpleJLO);
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 302, simpleJLOWithLocal);
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 342, simpleJLO);
    }
    
    public void testUnary() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 382, Collections.singleton("CreateFieldFix:i:org.netbeans.test.java.hints.Simple:int:[private]"));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 398, Collections.singleton("CreateFieldFix:b:org.netbeans.test.java.hints.Simple:byte:[private]"));
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 409, Collections.singleton("CreateFieldFix:b:org.netbeans.test.java.hints.Simple:byte:[private]"));
        
        performTestAnalysisTest("org.netbeans.test.java.hints.Simple", 415, Collections.singleton("CreateFieldFix:l:org.netbeans.test.java.hints.Simple:int:[private]"));
    }
    
    public void testTypevarsAndEnums() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 221, Collections.<String>emptySet());
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 243, Collections.<String>emptySet());
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 265, Collections.<String>emptySet());
        performTestAnalysisTest("org.netbeans.test.java.hints.TypevarsAndErrors", 287, Collections.<String>emptySet());
    }
    
    public void testReturn() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 164, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:l:int:false",
                "AddParameterOrLocalFix:l:int:true",
                "CreateFieldFix:l:org.netbeans.test.java.hints.Return:int:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 220, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:l:java.util.List:false",
                "AddParameterOrLocalFix:l:java.util.List:true",
                "CreateFieldFix:l:org.netbeans.test.java.hints.Return:java.util.List:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 284, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:l:java.util.List<java.lang.String>:false",
                "AddParameterOrLocalFix:l:java.util.List<java.lang.String>:true",
                "CreateFieldFix:l:org.netbeans.test.java.hints.Return:java.util.List<java.lang.String>:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Return", 340, Collections.<String>emptySet());
    }
    
    public void test92419() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug92419", 123, Collections.<String>emptySet());
    }
    
    public void testConditionalExpression() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 203, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:b:boolean:false",
                "AddParameterOrLocalFix:b:boolean:true",
                "CreateFieldFix:b:org.netbeans.test.java.hints.CondExpression:boolean:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 235, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:b:boolean:false",
                "AddParameterOrLocalFix:b:boolean:true",
                "CreateFieldFix:b:org.netbeans.test.java.hints.CondExpression:boolean:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 207, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:d:java.lang.CharSequence:false",
                "AddParameterOrLocalFix:d:java.lang.CharSequence:true",
                "CreateFieldFix:d:org.netbeans.test.java.hints.CondExpression:java.lang.CharSequence:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.CondExpression", 243, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:d:java.lang.CharSequence:false",
                "AddParameterOrLocalFix:d:java.lang.CharSequence:true",
                "CreateFieldFix:d:org.netbeans.test.java.hints.CondExpression:java.lang.CharSequence:[private]"
        )));
    }
    
    public void testArrayInitializer() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayInitializer", 210, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:f:java.io.File:false",
                "AddParameterOrLocalFix:f:java.io.File:true",
                "CreateFieldFix:f:org.netbeans.test.java.hints.ArrayInitializer:java.io.File:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayInitializer", 248, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:f:java.io.File:false",
                "AddParameterOrLocalFix:f:java.io.File:true",
                "CreateFieldFix:f:org.netbeans.test.java.hints.ArrayInitializer:java.io.File:[private]"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.ArrayInitializer", 281, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:i:int:false",
                "AddParameterOrLocalFix:i:int:true",
                "CreateFieldFix:i:org.netbeans.test.java.hints.ArrayInitializer:int:[private]"
        )));
    }
    
    public void test105415() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug105415", 138, Collections.<String>emptySet());
    }

    public void test112846() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug112846", 152, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:xxx:double[]:false",
                "AddParameterOrLocalFix:xxx:double[]:true",
                "CreateFieldFix:xxx:org.netbeans.test.java.hints.Bug112846:double[]:[private]"
        )));
    }
    
    protected void performTestAnalysisTest(String className, int offset, Set<String> golden) throws Exception {
        prepareTest(className);
        
        DataObject od = DataObject.find(info.getFileObject());
        EditorCookie ec = (EditorCookie) od.getLookup().lookup(EditorCookie.class);
        
        Document doc = ec.openDocument();
        
        CreateElement ce = new CreateElement();
        List<Fix> fixes = ce.analyze(info, offset);
        Set<String> real = new HashSet<String>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateFieldFix) {
                real.add(((CreateFieldFix) f).toDebugString(info));
                continue;
            }
            if (f instanceof AddParameterOrLocalFix) {
                real.add(((AddParameterOrLocalFix) f).toDebugString(info));
                continue;
            }
            
            fail("Fix of incorrect type: " + f.getClass());
        }
        
        assertEquals(golden, real);
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/CreateElementTest/";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
}
