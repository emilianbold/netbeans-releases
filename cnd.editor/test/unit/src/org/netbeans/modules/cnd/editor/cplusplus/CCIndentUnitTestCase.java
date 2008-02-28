/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 * @author Alexander Simon
 */
public class CCIndentUnitTestCase extends CCFormatterBaseUnitTestCase {

    public CCIndentUnitTestCase(String testMethodName) {
        super(testMethodName);
    }

    // indent new line tests
    
    public void testJavadocEnterNothingAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * text|\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " *|\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterTextAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * break|text\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * break\n"
                + " * |text\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterStarAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * text|*/\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " |*/\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintln() {
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnLineThree() {
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\"|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\"\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnAfterSemiColon() {
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\");|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\");\n"
                + "    |\n"
                + "\n"
                );
        
    }
    
//    public void testEnterInMultiLineClassDeclaration() {
//        setLoadDocumentText(
//                "public class C\n"
//                + "        : Runnable\n {|\n"
//                + "}\n"
//                );
//        indentNewLine();
//        assertDocumentTextAndCaret("Incorrect new-line indent",
//                "public class C\n"
//                + "        : Runnable {\n"
//                + "    |\n"
//                + "}\n"
//                );
//        
//    }
    
    public void testEnterAfterIf() {
        setLoadDocumentText(
                "if (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterFor() {
        setLoadDocumentText(
                "for (int i = 0; i < 10; i++)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "for (int i = 0; i < 10; i++)\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterWhile() {
        setLoadDocumentText(
                "while (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "while (true)\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterDo() {
        setLoadDocumentText(
                "do|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "do\n"
                + "    |\n"
                );
    }
    
    
    public void testEnterAfterIfStmt() {
        setLoadDocumentText(
                "if (true)\n"
                + "    stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    stmt;\n"
                + "|\n"
                );
    }
    
    public void testEnterAfterIfElse() {
        setLoadDocumentText(
                "if (true)\n"
                + "    stmt;\n"
                + "else|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterIfElseStmt() {
        setLoadDocumentText(
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    stmt;\n"
                + "|\n"
                );
    }
    
    public void testEnterAfterIfMultiLine() {
        setLoadDocumentText(
                "if (1 < 5|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (1 < 5\n"
                + "        |\n"
                );
    }
    
    public void testEnterAfterIfMultiLine2() {
        setLoadDocumentText(
                "if (1 < 5|)\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (1 < 5\n"
                + "        |)\n"
                );
    }
    
    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
     */
    public void testIdentUnbalancedBraces() {
        setLoadDocumentText(
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif|\n" +
            "    }\n" +
            "}\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of unbalanced braces",
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "        \n" +
            "    }\n" +
            "}\n");
    }

    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
     */
    public void testIdentUnbalancedBraces2() {
        setLoadDocumentText(
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "    }|\n" +
            "}\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of unbalanced braces",
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "    }\n" +
            "    \n" +
            "}\n");
    }

//    /**
//     * Test reformatting of unbalanced braces
//     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
//     */
//    public void testIdentUnbalancedBraces3() {
//        setLoadDocumentText(
//            "void foo() {\n" +
//            "#if A\n" +
//            "    if (0) {\n" +
//            "#else\n" +
//            "    if (1) {\n" +
//            "#endif\n" +
//            "    }\n" +
//            "|}\n");
//        indentNewLine();
//        assertDocumentText("Incorrect identing of unbalanced braces",
//            "void foo() {\n" +
//            "#if A\n" +
//            "    if (0) {\n" +
//            "#else\n" +
//            "    if (1) {\n" +
//            "#endif\n" +
//            "    }\n" +
//            "\n" + 
//            "}\n");
//    }
    
    
    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=97305
     */
    public void testIdentMain() {
        setCppEditorKit(false);
        setLoadDocumentText(
            "int main() {|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int main() {\n" +
            "    \n");
    }

    public void testIZ101099() {
        setLoadDocumentText(
                "template <class T>|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent IZ101099",
                "template <class T>\n"+
                "|\n"
                );
    }

    public void testIZ122489() {
        setLoadDocumentText(
                "Cpu::Cpu(int units) :\n"+
                "   Module(units) {\n"+
                "}|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent IZ122489",
                "Cpu::Cpu(int units) :\n"+
                "   Module(units) {\n"+
                "}\n"+
                "|\n"
                );
    }

    /**
     * test parameter aligning
     */
    public void testIdentMethodParameters() {
        setCppEditorKit(false);
        setLoadDocumentText(
            "int longmain(int a,|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int longmain(int a,\n" +
            "        \n");
    }

    /**
     * test parameter aligning
     */
    public void testIdentMethodParameters2() {
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        try {
            setCppEditorKit(false);
            setLoadDocumentText(
                "int longmain(int a,|\n");
            indentNewLine();
            assertDocumentText("Incorrect identing of main",
                "int longmain(int a,\n" +
                "             \n");
        } finally{
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineMethodParams, false);
        }
    }

    /**
     * test parameter aligning
     */
    public void testIdentCallParameters() {
        setCppEditorKit(false);
        setLoadDocumentText(
            "a = longmain(a,|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "a = longmain(a,\n" +
            "        \n");
    }

    /**
     * test parameter aligning
     */
    public void testIdentCallParameters2() {
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        try {
            setLoadDocumentText(
                "a = longmain(a,|\n");
            indentNewLine();
            assertDocumentText("Incorrect identing of main",
                "a = longmain(a,\n" +
                "             \n");
        } finally{
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, false);
        }
    }
}
