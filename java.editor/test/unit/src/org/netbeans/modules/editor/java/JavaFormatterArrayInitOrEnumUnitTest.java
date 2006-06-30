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

package org.netbeans.modules.editor.java;

/**
 * Java formatter tests.
 *
 * @autor Miloslav Metelka
 */
public class JavaFormatterArrayInitOrEnumUnitTest extends JavaFormatterUnitTestCase {

    public JavaFormatterArrayInitOrEnumUnitTest(String testMethodName) {
        super(testMethodName);
    }

    public void testReformatIntArray() {
        setLoadDocumentText(
                "void m() {\n" +
                "int[] array = {\n" +
                "1, 2, 3};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    int[] array = {\n" +
                "        1, 2, 3};\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArray() {
        setLoadDocumentText(
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\"\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    String[] array = {\n" +
                "        \"first\",\n" +
                "        \"second\"\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArrayExtraComma() {
        setLoadDocumentText(
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\",\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    String[] array = {\n" +
                "        \"first\",\n" +
                "        \"second\",\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArrayRBraceOnSameLine() {
        setLoadDocumentText(
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\"};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    String[] array = {\n" +
                "        \"first\",\n" +
                "        \"second\"};\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatNewObjectArray() {
        setLoadDocumentText(
                "void m() {\n" +
                "Object[] array = {\n" +
                "new Object(),\n" +
                "new String(\"second\"),\n" +
                "new Object()\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    Object[] array = {\n" +
                "        new Object(),\n" +
                "        new String(\"second\"),\n" +
                "        new Object()\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatNewObjectArrayMultiLine() {
        setLoadDocumentText(
                "void m() {\n" +
                "Object[] array = {\n" +
                "new Object(),\n" +
                "new String(\n" +
                "\"second\"),\n" +
                "new Object()\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    Object[] array = {\n" +
                "        new Object(),\n" +
                "        new String(\n" +
                "                \"second\"),\n" +
                "        new Object()\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArrayArgument() {
        setLoadDocumentText(
                "void m() {\n" +
                "a(new String[] {\n" +
                "\"first\",\n" +
                "\"second\"\n" +
                "});\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    a(new String[] {\n" +
                "        \"first\",\n" +
                "        \"second\"\n" +
                "    });\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatObjectArrayArgument() {
        setLoadDocumentText(
                "void m() {\n" +
                "a(new Object[] {\n" +
                "new Object(),\n" +
                "\"second\"\n" +
                "});\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    a(new Object[] {\n" +
                "        new Object(),\n" +
                "        \"second\"\n" +
                "    });\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatMultiArray() {
        setLoadDocumentText(
                "static int[][] CONVERT_TABLE={\n" +
                "{1,2},{2,3},\n" +
                "{3,4},{4,5},{5,6},\n" +
                "{6,7},{7,8},{8,9}};\n" +
                "f();\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "static int[][] CONVERT_TABLE={\n" +
                "    {1,2},{2,3},\n" +
                "    {3,4},{4,5},{5,6},\n" +
                "    {6,7},{7,8},{8,9}};\n" +
                "f();\n"
        );
    }

    
    public void testReformatSimpleEnum() {
        setLoadDocumentText(
                "public enum SimpleEnum {\n" +
                "ONE,\n" +
                "TWO,\n" +
                "THREE\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect simple enum reformating",
                "public enum SimpleEnum {\n" +
                "    ONE,\n" +
                "    TWO,\n" +
                "    THREE\n" +
                "}\n");
    }
    
    public void testReformatNestedEnum() {
        setLoadDocumentText(
                "public enum SimpleEnum {\n" +
                "ONE,\n" +
                "TWO,\n" +
                "THREE;\n" +
                "public enum NestedEnum {\n" +
                "A,\n" +
                "B\n" +
                "}\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect nested enum reformating",
                "public enum SimpleEnum {\n" +
                "    ONE,\n" +
                "    TWO,\n" +
                "    THREE;\n" +
                "    public enum NestedEnum {\n" +
                "        A,\n" +
                "        B\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testReformatComplexEnum() {
        setLoadDocumentText(
                "public enum ComplexEnum {\n" +
                "ONE,\n" +
                "TWO {\n" +
                "public void op(int a,\n" +
                "int b,\n" +
                "int c) {\n" +
                "}\n" +
                "public class Inner {\n" +
                "int a, b,\n" +
                "c,\n" +
                "d;\n" +
                "}\n" +
                "},\n" +
                "THREE\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect complex enum reformating",
                "public enum ComplexEnum {\n" +
                "    ONE,\n" +
                "    TWO {\n" +
                "        public void op(int a,\n" +
                "                int b,\n" +
                "                int c) {\n" +
                "        }\n" +
                "        public class Inner {\n" +
                "            int a, b,\n" +
                "                    c,\n" +
                "                    d;\n" +
                "        }\n" +
                "    },\n" +
                "    THREE\n" +
                "}\n");
    }
    
}
