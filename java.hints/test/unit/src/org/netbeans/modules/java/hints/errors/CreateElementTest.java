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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.hints.errors;

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
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug92419", 123, new HashSet<String>(Arrays.asList(
                "CreateClass:org.netbeans.test.java.hints.XXXX:[]:CLASS"
        )));
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

    public void test111048() throws Exception {
	// do not offer to create method in non-writable file/class
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 202, Collections.<String>emptySet());
	// but do it in writable
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 231, new HashSet<String>(Arrays.asList(
		"CreateMethodFix:contains(java.lang.String string)boolean:org.netbeans.test.java.hints.Bug111048"
        )));
	// do not offer to create field/inner class in non-writable file/class
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 261, Collections.<String>emptySet());
	performTestAnalysisTest("org.netbeans.test.java.hints.Bug111048", 301, new HashSet<String>(Arrays.asList(
		"CreateInnerClass:org.netbeans.test.java.hints.Bug111048.fieldOrClass:[private]:CLASS",
		"CreateFieldFix:fieldOrClass:org.netbeans.test.java.hints.Bug111048:java.lang.Object:[private]"
        )));
    }

    public void test117431() throws Exception {
        //do not offer same hint more times for a same unknown variable
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug117431", 155, new HashSet<String>(Arrays.asList(
		"AddParameterOrLocalFix:ii:int:true",
		"CreateFieldFix:ii:org.netbeans.test.java.hints.Bug117431:int:[private, static]",
                "AddParameterOrLocalFix:ii:int:false"
        )));
        //but do offer for a different one
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug117431", 219, new HashSet<String>(Arrays.asList(
                "AddParameterOrLocalFix:kk:int:true",
                "CreateFieldFix:kk:org.netbeans.test.java.hints.Bug117431:int:[private, static]",
                "AddParameterOrLocalFix:kk:int:false"
        )));
    }

    public void testMethodArgument() throws Exception {
        //do not offer same hint more times for a same unknown variable
        performTestAnalysisTest("org.netbeans.test.java.hints.MethodArgument", 217, new HashSet<String>(Arrays.asList(
		"AddParameterOrLocalFix:xx:int:true",
		"CreateFieldFix:xx:org.netbeans.test.java.hints.MethodArgument:int:[private, static]",
                "AddParameterOrLocalFix:xx:int:false"
        )));
    }

    public void testConstructorArgument() throws Exception {
        //do not offer same hint more times for a same unknown variable
        performTestAnalysisTest("org.netbeans.test.java.hints.ConstructorArgument", 181, new HashSet<String>(Arrays.asList(
		"AddParameterOrLocalFix:xx:int:true",
		"CreateFieldFix:xx:org.netbeans.test.java.hints.ConstructorArgument:int:[private, static]",
                "AddParameterOrLocalFix:xx:int:false"
        )));
    }

    public void testEnumConstant() throws Exception {
        //test hint creating a new enum constant
        performTestAnalysisTest("org.netbeans.test.java.hints.EnumConstant", 118, new HashSet<String>(Arrays.asList(
                "CreateEnumConstant:D:org.netbeans.test.java.hints.EnumConstant.Name:org.netbeans.test.java.hints.EnumConstant.Name"
                )));
    }

    public void test180111() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Bug180111", 163, new HashSet<String>(Arrays.asList(
                "CreateMethodFix:create()void:org.netbeans.test.java.hints.Bug180111"
        )));
    }

    protected void performTestAnalysisTest(String className, int offset, Set<String> golden) throws Exception {
        prepareTest(className);

        DataObject od = DataObject.find(info.getFileObject());
        EditorCookie ec = (EditorCookie) od.getLookup().lookup(EditorCookie.class);

        Document doc = ec.openDocument();

        List<Fix> fixes = CreateElement.analyze(info, offset);
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
	    if (f instanceof CreateMethodFix) {
                real.add(((CreateMethodFix) f).toDebugString(info));
                continue;
	    }
	    if (f instanceof CreateClassFix) {
		real.add(((CreateClassFix) f).toDebugString(info));
		continue;
	    }

            if (f instanceof CreateEnumConstant) {
                real.add(((CreateEnumConstant) f).toDebugString(info));
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
