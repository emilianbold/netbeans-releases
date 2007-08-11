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
package org.netbeans.modules.java.hints.introduce;

import java.awt.Dialog;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceHintTest extends NbTestCase {
    
    public IntroduceHintTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LifecycleManager.getDefault().saveAll();
    }

    public void testCorrectSelection1() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;}}", 110 - 49, 111 - 49, true);
    }
    
    public void testCorrectSelection2() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;}}", 102 - 49, 112 - 49, false);
    }
    
    public void testCorrectSelection3() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int z = 0; int i = z + 2;}}", 121 - 49, 124 - 49, false);
    }
    
    public void testCorrectSelection4() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; System.err.println((\"x=\" + y).length());}}", 83, 102, true);
    }
    
    public void testCorrectSelection5() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; System.err.println((\"x=\" + y).length());}}", 64, 103, false);
    }
    
    public void testCorrectSelection6() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; System.err.println((\"x=\" + y).length());}}", 64, 104, false);
    }
    
    public void testCorrectSelection7() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; y = 2;}}", 64, 69, false);
    }
    
    public void testCorrectSelection8() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = (int)Math.round(1.2);}}", 111 - 49, 114 - 49, false);
    }
    
    public void testCorrectSelection9() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {long y = Math.round(1.2);}}", 111 - 49, 126 - 49, true);
    }
    
    public void testCorrectSelection10() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {String s = \"\"; int y = s.length();}}", 125 - 49, 135 - 49, true);
    }
    public void testFix1() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3;int name = y + 9; int x = name;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       2, 0);
    }
    
    public void testFix2() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3;int nueName = y + 9; int x = nueName;}}",
                       new DialogDisplayerImpl("nueName", false, false, true),
                       2, 0);
    }
    
    public void testFix3() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3;int name = y + 9; int x = name; x = y + 9;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       2, 0);
    }
    
    public void testFix4() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3;int name = y + 9; int x = name; x = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       2, 0);
    }
    
    public void testFix5() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       108 - 25, 113 - 25,
                       "package test; public class Test {public void test() {int y = 3;int name = y + 9; int x = name; x = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       2, 0);
    }
    
    public void testFix6() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       108 - 25, 113 - 25,
                       "package test; public class Test {public void test() {int y = 3;final int name = y + 9; int x = name; x = name;}}",
                       new DialogDisplayerImpl(null, true, true, true),
                       2, 0);
    }
    
    public void testFix7() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; if (true) y = y + 9; y = y + 9;}}",
                       103 - 25, 108 - 25,
                       "package test; public class Test {public void test() {int y = 3;int name = y + 9; if (true) y = name; y = y + 9;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       2, 0);
    }
    
    public void testFix8() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; if (true) y = y + 9; y = y + 9;}}",
                       114 - 25, 119 - 25,
                       "package test; public class Test {public void test() {int y = 3;int name = y + 9; if (true) y = name; y = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       2, 0);
    }
    
    public void testFix9() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 8 + 9;} public void test2() { int y = 8 + 9;}}",
                       86 - 25, 91 - 25,
                       "package test; public class Test {public void test() {int name = 8 + 9; int y = name;} public void test2() { int y = 8 + 9;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       3, 0);
    }
    
    public void testFix10() throws Exception {
        performFixTest("package test; public class Test {public void test(int y) {while (y != 7) {y = 3 + 4;} y = 3 + 4;}}",
                115 - 25, 120 - 25,
                       "package test; public class Test {public void test(int y) {int name = 3 + 4; while (y != 7) {y = name;} y = name;}}",
                new DialogDisplayerImpl(null, true, null, true),
                3, 0);
    }
    
    public void testSimple4() throws Exception {
        performSimpleSelectionVerificationTest("package test; import java.util.ArrayList; public class Test {public void test() {Object o = new ArrayList<String>();}}", 141 - 49, 164- 49, true);
    }
    
    public void testConstant1() throws Exception {
        performConstantAccessTest("package test; public class Test {public void test() {int i = 1 + 2;}}", 97 - 36, 102 - 36, true);
    }
    
    public void testConstant2() throws Exception {
        performConstantAccessTest("package test; public class Test {private int i = 0; public void test() {int x = 1 + i;}}", 116 - 36, 121 - 36, false);
    }
    
    public void testConstant3() throws Exception {
        performConstantAccessTest("package test; public class Test {private static int i = 0; public void test() {int x = 1 + i;}}", 123 - 36, 128 - 36, false);
    }
    
    public void testConstant4() throws Exception {
        performConstantAccessTest("package test; public class Test {private final int i = 0; public void test() {int x = 1 + i;}}", 122 - 36, 127 - 36, false);
    }
    
    public void testConstant5() throws Exception {
        performConstantAccessTest("package test; public class Test {private static final int i = 0; public void test() {int x = 1 + i;}}", 129 - 36, 134 - 36, true);
    }
    
    public void testConstantFix1() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4;}}",
                       86 - 25, 91 - 25,
                       "package test; public class Test { private static final int name = 3 + 4; public void test() {int y = name;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       3, 1);
    }
    
    public void testConstantFixNoVariable() throws Exception {
        performFixTest("package test; public class Test {int y = 3 + 4;}",
                       66 - 25, 71 - 25,
                       "package test; public class Test { private static final int name = 3 + 4; int y = name;}",
                       new DialogDisplayerImpl(null, false, false, true),
                       1, 0);
    }
    
    public void testConstantFix2() throws Exception {
        performFixTest("package test; public class Test {int y = 3 + 4; int z = 3 + 4;}",
                       66 - 25, 71 - 25,
                       "package test; public class Test { private static final int name = 3 + 4; int y = name; int z = name;}",
                       new DialogDisplayerImpl(null, true, false, true),
                       1, 0);
    }
    
    public void testConstantFix106490a() throws Exception {
        performFixTest("package test; public class Test {int y = 3 + 4; int z = 3 + 4;}",
                       66 - 25, 71 - 25,
                       "package test; public class Test { public static final int name = 3 + 4; int y = name; int z = name;}",
                       new DialogDisplayerImpl(null, true, false, true, EnumSet.of(Modifier.PUBLIC)),
                       1, 0);
    }
    
    public void testConstantFix106490b() throws Exception {
        performFixTest("package test; public class Test {int y = 3 + 4; int z = 3 + 4;}",
                       66 - 25, 71 - 25,
                       "package test; public class Test { static final int name = 3 + 4; int y = name; int z = name;}",
                       new DialogDisplayerImpl(null, true, false, true, EnumSet.noneOf(Modifier.class)),
                       1, 0);
    }
    
    public void testIntroduceFieldFix1() throws Exception {
        performCheckFixesTest("package test; public class Test {int y = 3 + 4; int z = 3 + 4;}",
                       73 - 32, 78 - 32,
                       "[IntroduceFix:name:2:CREATE_CONSTANT]");
    }
    
    public void testIntroduceFieldFix2() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;}}",
                       93 - 32, 98 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceFix:name:2:CREATE_CONSTANT]",
                       "[IntroduceField:name:2:false:false:[7, 7]]");
    }
    
    public void testIntroduceFieldFix3() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;} public void test2() {int u = 3 + 4;}}",
                       93 - 32, 98 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceFix:name:3:CREATE_CONSTANT]",
                       "[IntroduceField:name:3:false:false:[7, 6]]");
    }
    
    public void testIntroduceFieldFix4() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int u = 0; int y = u + 4; int z = u + 4;} public void test2() {int u = 0; int a = u + 4;}}",
                       104 - 32, 109 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceField:name:2:false:false:[1, 1]]");
    }
    
    public void testIntroduceFieldFix5() throws Exception {
        performCheckFixesTest("package test; public class Test {int u = 0; public void test() {int y = u + 4; int z = u + 4;} public void test2() {int a = u + 4;}}",
                       104 - 32, 109 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceField:name:3:false:false:[7, 6]]");
    }
    
    public void testIntroduceFieldFix7() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int u = 0; int y = u + 4; int z = u + 4;}}",
                       104 - 32, 109 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceField:name:2:false:false:[1, 1]]");
    }
    
    public void testIntroduceFieldFix8() throws Exception {
        performCheckFixesTest("package test; public class Test {int u = 0; public void test() {int y = u + 4; int z = u + 4;}}",
                       104 - 32, 109 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceField:name:2:false:false:[7, 7]]");
    }
    
    public void testIntroduceFieldFix9() throws Exception {
        performCheckFixesTest("package test; public class Test {int u = 0; public void test() {int y = u + 4; int z = u + 4;} private int i = 4;}",
                       108 - 32, 109 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceFix:name:3:CREATE_CONSTANT]",
                       "[IntroduceField:name:3:false:false:[7, 6]]");
    }
    
    public void testIntroduceFieldFix10() throws Exception {
        performCheckFixesTest("package test; public class Test {static int u = 0; public static void test() {int y = u + 4; int z = u + 4;}}",
                       118 - 32, 123 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceField:name:2:true:false:[3, 3]]");
    }
    
    public void testIntroduceFieldFix11() throws Exception {
        performCheckFixesTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;}}",
                       88 - 32, 93 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceFix:name:2:CREATE_CONSTANT]",
                       "[IntroduceField:name:2:false:true:[7, 7]]");
    }
    
    public void testIntroduceFieldFix12() throws Exception {
        performCheckFixesTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "[IntroduceFix:name:2:CREATE_VARIABLE]",
                       "[IntroduceFix:name:2:CREATE_CONSTANT]",
                       "[IntroduceField:name:2:false:false:[7, 7]]");
    }
    
    public void testIntroduceFieldFix13() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int name = 3 + 4; public Test() {int y = name; int z = 3 + 4;} public Test(int i) {}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, false, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix14() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int name; public Test() {name = 3 + 4; int y = name; int z = 3 + 4;} public Test(int i) {}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_METHOD, false, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix15() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int name; public Test() {name = 3 + 4; int y = name; int z = 3 + 4;} public Test(int i) {name = 3 + 4; }}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix16() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int i; public Test() {i = 3 + 4; int y = i; int z = 3 + 4;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix17() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int i; public Test() {i = 3 + 4; int y = i; int z = i;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, true, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix18() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { public int i; public Test() {i = 3 + 4; int y = i; int z = i;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, true, EnumSet.<Modifier>of(Modifier.PUBLIC), false, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix19() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { public final int i; public Test() {i = 3 + 4; int y = i; int z = i;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, true, EnumSet.<Modifier>of(Modifier.PUBLIC), true, true),
                       3, 2);
    }
    
    public void testIntroduceFieldFix20() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;}}",
                       86 - 25, 91 - 25,
                       "package test; public class Test { private int name; public Test() { name = 3 + 4; } public void test() {int y = name; int z = 3 + 4;}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 2);
    }
    
    public void testCorrectMethodSelection1() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;}}", 105 - 52, 115 - 52, true, new int[] {0, 0});
    }
    
    public void testCorrectMethodSelection2() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {int i = 3; i += 2; i += 3;}}", 116 - 52, 123 - 52, true, new int[] {1, 1});
    }
    
    public void testCorrectMethodSelection3() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;  i += 2; i += 3;}}", 116 - 52, 125 - 52, true, new int[] {1, 1});
    }
    
    public void testCorrectMethodSelection4() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {Object o = null;}}", 108 - 52, 121 - 52, false, new int[] {0, 0});
    }
    
    public void testCorrectMethodSelection5() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {Object o = null;}}", 105 - 52, 105 - 52, false, new int[] {0, 0});
    }
    
    public void testCorrectMethodSelection6() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {       Object o = null;}}", 107 - 52, 107 - 52, false, new int[] {0, 0});
    }
    
    public void testIntroduceMethodFix1() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;}}",
                       78 - 25, 92 - 25,
                       "package test; public class Test {public void test() {name(); int z = 3 + 4;} private void name() { int y = 3 + 4; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix2() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int z = y + 4;}}",
                       93 - 25, 107 - 25,
                       "package test; public class Test {public void test() {int y = 3 + 4;name(y); } private void name(int y) { int z = y + 4; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix3() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; y += 4; int z = y + 4;}}",
                       93 - 25, 100 - 25,
                       "package test; public class Test {public void test() {int y = 3 + 4; y = name(y); int z = y + 4;} private int name(int y) { y += 4; return y; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix4() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; y += 4; int a = 4; int z = y + a;}}",
                       93 - 25, 111 - 25,
                       null,
                       new DialogDisplayerImpl3("name", null, true), 0, -1);
    }
    
    public void testIntroduceMethodFix5() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int a = y + 4; int z = y + a;}}",
                       93 - 25, 107 - 25,
                       "package test; public class Test {public void test() {int y = 3 + 4; int a = name(y); int z = y + a;} private int name(int y) { int a = y + 4; return a; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix6() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test() throws IOException {int y = 3 + 4; throw new IOException();}}",
                       140 - 25, 164 - 25,
                       "package test; import java.io.IOException; public class Test {public void test() throws IOException {int y = 3 + 4;name(); } private void name() throws IOException { throw new IOException(); } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix7() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test() {while (true) {int y = 3 + 4;}}}",
                       120 - 25, 134 - 25,
                       "package test; import java.io.IOException; public class Test {public void test() {while (true) {name(); }} private void name() { int y = 3 + 4; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix8() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) break;}}}",
                       125 - 25, 145 - 25,
                       "package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if ( name(y)) break;}} private boolean name(int y) { if (--y <= 0) { return true; } return false; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix9() throws Exception {
        performErrorMessageTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) {y = 3; break;}} int u = y;}}",
                       134 - 34, 163 - 34,
                       IntroduceKind.CREATE_METHOD,
                       "ERR_Too_Many_Return_Values");
    }
    
    public void testIntroduceMethodFix10() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) { y = 2; break; } else { y = 3; break; }} int u = y;}}",
                       125 - 25, 179 - 25,
                       "package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {y = name(y); break; } int u = y;} private int name(int y) { if (--y <= 0) { y = 2; return y; } else { y = 3; return y; } } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix11() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) { break; } else { break; }}}}",
                       125 - 25, 165 - 25,
                       "package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {name(y); break; }} private void name(int y) { if (--y <= 0) { return; } else { return; } } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFix12() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { return 1; } else { return 2; }}}}",
                       96 - 25, 142 - 25,
                       "package test; public class Test {public int test(int y) {while (true) {return name(y); }} private int name(int y) { if (--y <= 0) { return 1; } else { return 2; } } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    //not working because of code generator bug:
    public void XtestIntroduceMethodFix13() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { while (true) break; } else { return 2; } return 3;}}}",
                       96 - 25, 152 - 25,
                       "package test; public class Test {public int test(int y) {while (true) { if (name(y)) { return 2; } return 3;}} private boolean name(int y) { if (--y <= 0) { while (true) { break; } } else { return true; } return false; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    //not working because of code generator bug:
    public void XtestIntroduceMethodFix14() throws Exception {
        performFixTest("package test; public class Test {public void test(int y) {if (3 != 4) return ;}}",
                       83 - 25, 103 - 25,
                       "package test; public class Test {public void test(int y) {if (3 != 4) return ;}}",
                       new DialogDisplayerImpl3("name", null, true));
    }
    
    public void testIntroduceMethodFixNeverEnds1() throws Exception {
        performFixTest("package test; public class Test {}    ",
                        60 - 25, 61 - 25,
                        null,
                        new DialogDisplayerImpl(null, null, null, false));
    }
    
    public void testIntroduceMethodFixNeverEnds2() throws Exception {
        performFixTest("     package test; public class Test {}",
                        26 - 25, 28 - 25,
                        null,
                        new DialogDisplayerImpl(null, null, null, false));
    }
    
    public void testIntroduceMethodFix106490a() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { return 1; } else { return 2; }}}}",
                       96 - 25, 142 - 25,
                       "package test; public class Test { public int name(int y) { if (--y <= 0) { return 1; } else { return 2; } } public int test(int y) {while (true) {return name(y); }}}",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PUBLIC), true));
    }
    
    public void testIntroduceMethodFix106490b() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { return 1; } else { return 2; }}}}",
                       96 - 25, 142 - 25,
                       "package test; public class Test {public int test(int y) {while (true) {return name(y); }} int name(int y) { if (--y <= 0) { return 1; } else { return 2; } } }",
                       new DialogDisplayerImpl3("name", EnumSet.noneOf(Modifier.class), true));
    }
    
    public void testIntroduceMethodFixStatic() throws Exception {
        performFixTest("package test; public class Test {public static int test(int y) {y += 5; return y;}}",
                       89 - 25, 96 - 25,
                       "package test; public class Test {public static int test(int y) { y = name(y); return y;} private static int name(int y) { y += 5; return y; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void testIntroduceMethod109663a() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {while (y < 10) {if (y == 0) break; else y++; int u = y;}}}",
                       106 - 25, 134 - 25,
                       IntroduceKind.CREATE_METHOD,
                       "ERR_Too_Many_Return_Values");
    }
    
    public void testIntroduceMethod109663b() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {while (y < 10) {if (y == 0) break; else y++;}}}",
                       106 - 25, 134 - 25,
                       IntroduceKind.CREATE_METHOD,
                       "ERR_Too_Many_Return_Values");
    }
    
    public void testIntroduceMethod109663c() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {do {if (y == 0) break; else y++;} while (y < 10); }}",
                       103 - 34, 131 - 34,
                       IntroduceKind.CREATE_METHOD,
                       "ERR_Too_Many_Return_Values");
    }
    
    public void testIntroduceMethod109663d() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {for ( ; y < 10; ) {if (y == 0) break; else y++;}}}",
                       118 - 34, 146 - 34,
                       IntroduceKind.CREATE_METHOD,
                       "ERR_Too_Many_Return_Values");
    }
    
    public void testIntroduceMethod109663e() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {for ( ; ; y++) {if (y == 0) break; else y++;}}}",
                       115 - 34, 143 - 34,
                       IntroduceKind.CREATE_METHOD,
                       "ERR_Too_Many_Return_Values");
    }
    
    public void testIntroduceMethod109663f() throws Exception {
        performFixTest("package test; public class Test {public static void test(int y) {for (int u = y ; ; ) {if (y == 0) break; else y++;}}}",
                       112 - 25, 140 - 25,
                       "package test; public class Test {public static void test(int y) {for (int u = y ; ; ) {if ( name(y)) break;}} private static boolean name(int y) { if (y == 0) { return true; } else { y++; } return false; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void testIntroduceMethod109663g() throws Exception {
        performFixTest("package test; public class Test {public static void test(int y) {for (Integer i : java.util.Arrays.asList(y)) {if (y == 0) break; else y++;}}}",
                       136 - 25, 164 - 25,
                       "package test; public class Test {public static void test(int y) {for (Integer i : java.util.Arrays.asList(y)) {if ( name(y)) break;}} private static boolean name(int y) { if (y == 0) { return true; } else { y++; } return false; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void test107689() throws Exception {
        performSimpleSelectionVerificationTest("package test; import java.util.List; public class Test {}",
                       53 - 32, 67 - 32, false);
    }
    
    public void testIntroduceMethod112552a() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; while (true) {if (first) {first = false;} else {break;}}}}",
                       130 - 25, 144 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; while (true) {if (first) {first = name();} else {break;}}} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void testIntroduceMethod112552b() throws Exception {
        performFixTest("package test; public class Test {public static void t(int a) {boolean first = true; while (true) {if (first) {while (a != 1) {first = false;}} else {break;}}}}",
                       151 - 25, 165 - 25,
                       "package test; public class Test {public static void t(int a) {boolean first = true; while (true) {if (first) {while (a != 1) {first = name();}} else {break;}}} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void testIntroduceMethod112552c() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; for (;;) {if (first) {first = false;} else {break;}}}}",
                       126 - 25, 140 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; for (;;) {if (first) {first = name();} else {break;}}} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void testIntroduceMethod112552d() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; do {if (first) {first = false;} else {break;}} while (true);}}",
                       120 - 25, 134 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; do {if (first) {first = name();} else {break;}} while (true);} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    public void testIntroduceMethod112552e() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; while (true) {first = false; while (first) {System.err.println();}}}}",
                       148 - 25, 169 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; while (true) {first = false; while (first) { name();}}} private static void name() { System.err.println(); } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
    }
    
    protected void prepareTest(String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        TestUtilities.copyStringToFile(FileUtil.toFile(data), code);
        
        data.refresh();
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
    }
    
    private CompilationInfo info;
    private Document doc;
    
    private void performSimpleSelectionVerificationTest(String code, int start, int end, boolean awaited) throws Exception {
        prepareTest(code);
        
        assertEquals(awaited, IntroduceHint.validateSelection(info, start, end) != null);
    }
    
    private void performStatementSelectionVerificationTest(String code, int start, int end, boolean awaited, int[] awaitedSpan) throws Exception {
        prepareTest(code);
        
        int[] actualSpan = new int[2];
        
        assertEquals(awaited, IntroduceHint.validateSelectionForIntroduceMethod(info, start, end, actualSpan) != null);
        
        if (awaited) {
           assertTrue(Arrays.toString(actualSpan), Arrays.equals(awaitedSpan, actualSpan));
        }
    }
    
    private void performConstantAccessTest(String code, int start, int end, boolean awaited) throws Exception {
        prepareTest(code);
        
        assertEquals(awaited, IntroduceHint.checkConstantExpression(info, IntroduceHint.validateSelection(info, start, end)));
    }
    
    private void performFixTest(String code, int start, int end, String golden, DialogDisplayer dd) throws Exception {
        performFixTest(code, start, end, golden, dd, 1, 0);
    }
    
    private void performFixTest(String code, int start, int end, String golden, DialogDisplayer dd, int numFixes, int useFix) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {dd});
        
        prepareTest(code);
        
        Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
        List<ErrorDescription> errors = IntroduceHint.computeError(info, start, end, null, errorMessages, new AtomicBoolean());
        
        if (golden == null) {
            assertEquals(errors.toString(), 0, errors.size());
            return ;
        }
        
        assertEquals(errorMessages.toString(), 1, errors.size());
        
        List<Fix> fixes = errors.get(0).getFixes().getFixes();
        
        assertEquals(fixes.toString(), numFixes, fixes.size());
        
        fixes.get(useFix).implement();
        
        String result = doc.getText(0, doc.getLength()).replaceAll("[ \t\n]+", " ");
        
        assertEquals(golden, result);
    }
    
    private void performErrorMessageTest(String code, int start, int end, IntroduceKind kind, String golden) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        prepareTest(code);
        
        Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
        List<ErrorDescription> errors = IntroduceHint.computeError(info, start, end, null, errorMessages, new AtomicBoolean());
        
        assertEquals(errors.toString(), 0, errors.size());
        assertEquals(golden, errorMessages.get(kind));
    }
    
    private void performCheckFixesTest(String code, int start, int end, String... goldenFixes) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        prepareTest(code);
        
        List<ErrorDescription> errors = IntroduceHint.computeError(info, start, end, null, new EnumMap<IntroduceKind, String>(IntroduceKind.class), new AtomicBoolean());
        
        assertEquals(errors.toString(), 1, errors.size());
        
        List<Fix> fixes = errors.get(0).getFixes().getFixes();
        List<String> fixNames = new LinkedList<String>();
        
        for (Fix f : fixes) {
            fixNames.add(f.toString());
        }
        
        assertEquals(Arrays.asList(goldenFixes), fixNames);
    }
    
    private static class DialogDisplayerImpl extends DialogDisplayer {

        private String name;
        private Boolean replaceAll;
        private Boolean declareFinal;
        private Set<Modifier> modifiers;
        private boolean ok;

        public DialogDisplayerImpl(String name, Boolean replaceAll, Boolean declareFinal, boolean ok) {
            this(name, replaceAll, declareFinal, ok, EnumSet.of(Modifier.PRIVATE));
        }
        
        public DialogDisplayerImpl(String name, Boolean replaceAll, Boolean declareFinal, boolean ok, Set<Modifier> modifiers) {
            this.name = name;
            this.replaceAll = replaceAll;
            this.declareFinal = declareFinal;
            this.ok = ok;
            this.modifiers = modifiers;
        }
        
        public Object notify(NotifyDescriptor descriptor) {
            IntroduceVariablePanel panel = (IntroduceVariablePanel) descriptor.getMessage();
            
            if (name != null) {
                panel.setVariableName(name);
            }
            
            if (replaceAll != null) {
                panel.setReplaceAll(replaceAll);
            }
            
            if (declareFinal != null) {
                panel.setDeclareFinal(declareFinal);
            }
            
            if (modifiers != null) {
                panel.setAccess(modifiers);
            }
            
            return ok ? descriptor.getOptions()[0] : descriptor.getOptions()[1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private static class DialogDisplayerImpl2 extends DialogDisplayer {

        private String fieldName;
        private Integer initializeIn;
        private Boolean replaceAll;
        private Set<Modifier> access;
        private Boolean declareFinal;
        private boolean ok;

        public DialogDisplayerImpl2(String fieldName, Integer initializeIn, Boolean replaceAll, Set<Modifier> access, Boolean declareFinal, boolean ok) {
            this.fieldName = fieldName;
            this.initializeIn = initializeIn;
            this.replaceAll = replaceAll;
            this.access = access;
            this.declareFinal = declareFinal;
            this.ok = ok;
        }

        
        public Object notify(NotifyDescriptor descriptor) {
            IntroduceFieldPanel panel = (IntroduceFieldPanel) descriptor.getMessage();
            
            if (fieldName != null) {
                panel.setFieldName(fieldName);
            }
            
            if (initializeIn != null) {
                panel.setInitializeIn(initializeIn);
            }
            
            if (replaceAll != null) {
                panel.setReplaceAll(replaceAll);
            }
            
            if (access  != null) {
                panel.setAccess(access);
            }
            
            if (declareFinal != null) {
                panel.setDeclareFinal(declareFinal);
            }
            
            return ok ? descriptor.getOptions()[0] : descriptor.getOptions()[1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private static class DialogDisplayerImpl3 extends DialogDisplayer {

        private String methodName;
        private Set<Modifier> access;
        private boolean ok;

        public DialogDisplayerImpl3(String methodName, Set<Modifier> access, boolean ok) {
            this.methodName = methodName;
            this.access = access;
            this.ok = ok;
        }

        public Object notify(NotifyDescriptor descriptor) {
            IntroduceMethodPanel panel = (IntroduceMethodPanel) descriptor.getMessage();
            
            if (methodName != null) {
                panel.setMethodName(methodName);
            }
            
            if (access  != null) {
                panel.setAccess(access);
            }
            
            return ok ? descriptor.getOptions()[0] : descriptor.getOptions()[1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
