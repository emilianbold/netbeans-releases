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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class AddParameterOrLocalFixTest extends ErrorHintsTestBase {
    
    private boolean parameter = true;
    
    public AddParameterOrLocalFixTest(String testName) {
        super(testName);
    }

    public void testAddBeforeVararg() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(String... a) {bbb = 0;}}",
                       91 - 25,
                       "AddParameterOrLocalFix:bbb:int:true",
                       "package test; public class Test {public void test(int bbb,String... a) {bbb = 0;}}");
    }

    public void testAddToTheEnd() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(String[] a) {bbb = 0;}}",
                       90 - 25,
                       "AddParameterOrLocalFix:bbb:int:true",
                       "package test; public class Test {public void test(String[] a, int bbb) {bbb = 0;}}");
    }

    public void testAddToTheEmptyParamsList() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {bbb = 0;}}",
                       80 - 25,
                       "AddParameterOrLocalFix:bbb:int:true",
                       "package test; public class Test {public void test(int bbb) {bbb = 0;}}");
    }

    public void testAddLocalVariableWithComments() throws Exception {
        parameter = false;

        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {int a;\n //test\n |bbb = 0;\n int c; }}",
                       "AddParameterOrLocalFix:bbb:int:false",
                       "package test; public class Test {public void test() {int a; //test int bbb = 0; int c; }}");
    }

    public void testAddLocalVariableNotInPlace() throws Exception {
        parameter = false;
        boolean orig = ErrorFixesFakeHint.isCreateLocalVariableInPlace();

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public void test() {int a;\n |bbb = 0;\n int c; }}",
                    "AddParameterOrLocalFix:bbb:int:false",
                    "package test; public class Test {public void test() {int bbb; int a; bbb = 0; int c; }}");
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(orig);
        }
    }

    public void testAddLocalVariableNotInPlaceInConstr() throws Exception {
        parameter = false;

        boolean orig = ErrorFixesFakeHint.isCreateLocalVariableInPlace();

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public Test() {super();\n int a;\n |bbb = 0;\n int c; }}",
                    "AddParameterOrLocalFix:bbb:int:false",
                    "package test; public class Test {public Test() {super(); int bbb; int a; bbb = 0; int c; }}");
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(orig);
        }
    }

    public void testInsideBlock() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {if (true) {int aaa = 0; |bbb = aaa; }}}",
                       "AddParameterOrLocalFix:bbb:int:false",
                       "package test; public class Test {public void test() {if (true) {int aaa = 0;int bbb = aaa; }}}");
    }

    public void testInsideBlockWithPreviousDeclaration() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {Object[] array = new Object[10];for (int i = 0; i < array.length; i++) {Object item = array[i + 1];item = array[i];}int j = 0;while (j < 10) {|item = array[j];j--;}}}",
                       "AddParameterOrLocalFix:item:java.lang.Object:false",
                       "package test; public class Test {public void test() {Object[] array = new Object[10];for (int i = 0; i < array.length; i++) {Object item = array[i + 1];item = array[i];}int j = 0;while (j < 10) {Object item = array[j]; j--;}}}");
    }

    public void testInsideParentBlock() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {{foo = \"bar\";}|foo = \"bar\";}}",
                       "AddParameterOrLocalFix:foo:java.lang.String:false",
                       "package test; public class Test {public void test() {String foo; {foo = \"bar\";}foo = \"bar\";}}");
    }

    public void testEnhancedForLoopEmptyList() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (|ttt : java.util.Collections.emptyList()) {}\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ttt:java.lang.Object:false",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (Object ttt : java.util.Collections.emptyList()) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopExtendedNumber() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         java.util.List<? extends Number> l = null;\n" +
                "         for (|ttt : l) {}\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ttt:java.lang.Number:false",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         java.util.List<? extends Number> l = null;\n" +
                "         for (Number ttt : l) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopStringArray() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         String[] a = null;\n" +
                "         for (|ttt : a) {}\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ttt:java.lang.String:false",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         String[] a = null;\n" +
                "         for (String ttt : a) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopPrimitiveArray() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         int[] a = null;\n" +
                "         for (|ttt : a) {}\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ttt:int:false",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         int[] a = null;\n" +
                "         for (int ttt : a) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopNotImported() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (|date : someMethod()) {\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:date:java.util.Date:false",
                ("package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (Date date : someMethod()) {\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopInsideItsBody() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (date : someMethod()) {\n" +
                "             Date local = |date;\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:date:java.util.Date:false",
                ("package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (Date date : someMethod()) {\n" +
                "             Date local = date;\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testAssignmentToValid181120() throws Exception {
        parameter = false;
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public String test(int i) {\n" +
                "         String s;\n" +
                "         s = test(i|i);\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ii:int:false",
                ("package test;\n" +
                 "import java.util.Date;\n" +
                 "public class Test {\n" +
                 "     public String test(int i) {\n" +
                 "         String s;\n" +
                 "         int ii;\n" +
                 "         s = test(ii);\n" +
                 "     }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws IOException {
        List<Fix> fixes = CreateElement.analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof AddParameterOrLocalFix) {
                if (((AddParameterOrLocalFix) f).isParameter() == parameter)
                    result.add(f);
            }
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((AddParameterOrLocalFix) f).toDebugString(info);
    }
}
