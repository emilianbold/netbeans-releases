/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.suggestions;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class TooStrongCastTest extends NbTestCase {
    public static final String FIX_SPECIFIC_CATCH = "Use specific type in catch";
    private String fileName;
    
    public TooStrongCastTest(String name) {
        super(name);
    }

    private HintTest createHintTest(String n) throws Exception {
        this.fileName = n;
        return HintTest.create().input("org/netbeans/test/java/hints/TooStrongCastTest/" + n + ".java", code(n));
    }
    
    private String c() throws Exception { return code(fileName); }
    
    private String code(String name) throws IOException {
        FileObject f = FileUtil.toFileObject(getDataDir());
        return f.getFileObject("org/netbeans/test/java/hints/TooStrongCastTest/" + name + ".java").asText();
    }
    
    private String g() throws Exception { return golden(fileName); }
    
    private String golden(String name) throws IOException {
        FileObject f = FileUtil.toFileObject(getDataDir());
        return f.getFileObject("goldenfiles/org/netbeans/test/java/hints/TooStrongCastTest/" + name + ".java").asText();
    }
    
    private String f() { return f(fileName); }
    
    private String f(String name) {
        return "org/netbeans/test/java/hints/TooStrongCastTest/" + name + ".java";
    }
    
    private static final String[] ARRAY_WARNINGS = {
        "13:12-13:15:verifier:Unnecessary cast to int", 
        "15:11-15:21:verifier:Type cast to byte is too strong. int should be used instead", 
        "16:12-16:16:verifier:Unnecessary cast to byte", 
        "16:19-16:22:verifier:Unnecessary cast to int"
    };
    
    private static final String[] ASSIGNMENT_WARNINGS = {
        "9:13-9:17:verifier:Unnecessary cast to List", 
        "16:12-16:19:verifier:Type cast to List is too strong. Collection should be used instead", 
        "23:13-23:17:verifier:Unnecessary cast to long"
    };
    
    private static final String[] EXPRESSION_WARNINGS = {
        "17:13-17:16:verifier:Unnecessary cast to int", 
        "25:12-25:19:verifier:Type cast to byte is too strong. int should be used instead", 
        "41:22-41:28:verifier:Unnecessary cast to String", 
        "47:17-47:20:verifier:Unnecessary cast to int"
    };
    
    private static final String[] METHOD_WARNINGS = {
        "20:10-20:19:verifier:Unnecessary cast to JViewport", 
        "27:41-27:63:verifier:Type cast to MouseWheelEvent is too strong. MouseEvent should be used instead", 
        "33:22-33:29:verifier:Unnecessary cast to JButton", 
        "39:22-39:37:verifier:Type cast to SuperDerived is too strong. Derived should be used instead"
    };

    private static final String[] RETURN_WARNINGS = {
        "17:15-17:22:verifier:Type cast to List is too strong. Collection should be used instead", 
        "25:26-25:33:verifier:Type cast to List is too strong. Collection should be used instead", 
        "40:15-40:30:verifier:Type cast to List<String> is too strong. Collection<String> should be used instead", 
        "45:15-45:25:verifier:Type cast to List<T> is too strong. Collection<T> should be used instead"
    };
    
    private static final String[] THROW_WARNINGS = {
        "14:18-14:43:verifier:Type cast to FileNotFoundException is too strong. IOException should be used instead", 
        "16:18-16:46:verifier:Type cast to IllegalArgumentException is too strong. RuntimeException should be used instead"
    };
    
    public void testArrayAccess() throws Exception {
        createHintTest("ArrayAccess").
            run(TooStrongCast.class).
            assertWarnings(ARRAY_WARNINGS);
    }
    
    public void testAssignment() throws Exception {
        createHintTest("CastAssignment").
            run(TooStrongCast.class).
            assertWarnings(ASSIGNMENT_WARNINGS);
    }

    public void testExpression() throws Exception {
        createHintTest("CastExpressions").
            run(TooStrongCast.class).
            assertWarnings(EXPRESSION_WARNINGS);
    }
    
    public void testMethods() throws Exception {
        createHintTest("CastMethods").
            run(TooStrongCast.class).
            assertWarnings(METHOD_WARNINGS);
    }
    
    public void testReturnType() throws Exception {
        createHintTest("ReturnType").
            run(TooStrongCast.class).
            assertWarnings(RETURN_WARNINGS);
    }
    
    public void testThrowExpression() throws Exception {
        createHintTest("ThrowExpression").
            run(TooStrongCast.class).
            assertWarnings(THROW_WARNINGS);
    }
    
    /**
     * Checks that a type (null) explicitly casted to varargs array
     * type does not produce a warning
     * @throws Exception 
     */
    public void testVarargsOK() throws Exception {
        HintTest.create().
            input("package test;\n" +
                "public class Test {\n" +
                "    private void varargMethod(String s, CharSequence... args) {}\n" +
                "    \n" +
                "    public void varargsCall() {\n" +
                "        varargMethod(\"ble\", (CharSequence[])null); \n" +
                "    }\n" +
                "}\n" +
                "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    public void testVarargRedudantCastToItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() {\n" +
            "        varargMethod(\"ble\", \"fuj\", (CharSequence)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:36-4:48:verifier:Unnecessary cast to CharSequence");
    }
    
    public void testVarargStrongCastToVarArray() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        Object arr = null;\n" +
            "        varargMethod(\"ble\", (String[])arr);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:28-5:41:verifier:Type cast to String[] is too strong. CharSequence[] should be used instead");
    }
    
    public void testVarargRedundantCastArray() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        String[] arr = null;\n" +
            "        varargMethod(\"ble\", (String[])arr);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:29-5:37:verifier:Unnecessary cast to String[]");
    }

    /**
     * It is unnecessary to cast a String- variable passed on vararg
     * position.
     * 
     * @throws Exception 
     */
    public void testVarargRedundantCastFirstItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        String s = \"\";\n" +
            "        varargMethod(\"ble\", (String)s);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:29-5:35:verifier:Unnecessary cast to String");
    }

    /**
     * It is OK to cast null item to item type to avoid 'possibly ambiguous null'
     * type warning from varargs hint
     * 
     * @throws Exception 
     */
    public void testVarargOKCastNullItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        varargMethod(\"ble\", (CharSequence)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }

    public void testVarargStrongCastNullItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        varargMethod(\"ble\", (String)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:28-4:40:verifier:Type cast to String is too strong. CharSequence should be used instead");
    }

    public void testVarargStrongCastFirstItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        Object s = \"\";\n" +
            "        varargMethod(\"ble\", (String)s);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:28-5:37:verifier:Type cast to String is too strong. CharSequence should be used instead");
    }

    public void testVarargStromgCastToItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() {\n" +
            "        varargMethod(\"ble\", \"fuj\", (String)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:36-4:42:verifier:Unnecessary cast to String");
    }
    
    /**
     * Do not hint if the typecast is needed to select the appropriate method
     * @throws Exception 
     */
    public void testOKAmbiguousMethod() throws Exception {
        HintTest.create().
            input("package test;\n" +
"import java.io.Serializable;\n" +
"abstract class VarArgsCast {\n" +
"    public void varargsCall() { \n" +
"        String value = null;\n" +
"        findByPropertyValue(\"\", (Serializable)value, true);  \n" +
"        \n" +
"    }\n" +
"    abstract Object findByPropertyValue(String name, Object value);\n" +
"    abstract Object findByPropertyValue(String name, String value, boolean ignoreCase);\n" +
"    abstract Object findByPropertyValue(String name, Serializable value, boolean ignoreCase);\n" +
"}\n" +
"").
            run(TooStrongCast.class).
            assertWarnings("");
    }
    
    public void testStrongOverloadedMethd() throws Exception {
        HintTest.create().
            input("package test;\n" +
"import java.io.Serializable;\n" +
"abstract class VarArgsCast {\n" +
"    public void varargsCall() { \n" +
"        Integer value = null;\n" +
"        findByPropertyValue(\"\", (Serializable)value, true);  \n" +
"        \n" +
"    }\n" +
"    abstract Object findByPropertyValue(String name, Object value);\n" +
"    abstract Object findByPropertyValue(String name, String value, boolean ignoreCase);\n" +
"    abstract Object findByPropertyValue(String name, Serializable value, boolean ignoreCase);\n" +
"}\n" +
"").
            run(TooStrongCast.class).
            assertWarnings("5:33-5:45:verifier:Unnecessary cast to Serializable");
    }
    
    public void testNoHintInferredType() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections;\n" +
            "import java.util.List;\n" +
            "\n" +
            "class VarArgsCast {\n" +
            "    void bu() {\n" +
            "        List<String> strings = new ArrayList<String>();\n" +
            "        strings.addAll(Collections.nCopies(10, (String)null));\n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }
}
