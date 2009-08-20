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
public class IndentTestCase extends EditorBase {

    public IndentTestCase(String testMethodName) {
        super(testMethodName);
    }

    // indent new line tests
    
    public void testJavadocEnterNothingAfterCaret() {
        setDefaultsOptions();
        setLoadDocumentText(
                "/**\n"
                + " * text|\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " * |\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterTextAfterCaret() {
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
        setLoadDocumentText(
                "if (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    |\n"
                );
    }

    public void testEnterAfterIfHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "if (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "  |\n"
                );
    }

    public void testEnterAfterIfBraceHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "if (true)\n" +
                "  {|\n" +
                "  }\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n" +
                "  {\n" +
                "    |\n" +
                "  }\n" 
                );
    }

    public void testEnterAfterIfBraceHalf2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  if (true)\n" +
                "    {|\n" +
                "    }\n" +
                "}\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "int foo()\n" +
                "{\n" +
                "  if (true)\n" +
                "    {\n" +
                "      |\n" +
                "    }\n" +
                "}\n"
                );
    }
    
    public void testEnterAfterFor() {
        setDefaultsOptions();
        setLoadDocumentText(
                "for (int i = 0; i < 10; i++)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "for (int i = 0; i < 10; i++)\n"
                + "    |\n"
                );
    }

    public void testEnterAfterForHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "for (int i = 0; i < 10; i++)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "for (int i = 0; i < 10; i++)\n"
                + "  |\n"
                );
    }
    
    public void testEnterAfterWhile() {
        setDefaultsOptions();
        setLoadDocumentText(
                "while (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "while (true)\n"
                + "    |\n"
                );
    }

    public void testEnterAfterWhileHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "while (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "while (true)\n" +
                "  |\n"
                );
    }

    public void testEnterAfterDo() {
        setDefaultsOptions();
        setLoadDocumentText(
                "do|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "do\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterDoHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "do|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "do\n" +
                "  |\n"
                );
    }

    public void testEnterAfterIfStmt() {
        setDefaultsOptions();
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
    
    public void testEnterAfterIfStmtHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "if (true)\n"
                + "  stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "  stmt;\n"
                + "|\n"
                );
    }

    public void testEnterAfterIfElse() {
        setDefaultsOptions();
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

    public void testEnterAfterIfElseHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "if (true)\n"
                + "  stmt;\n"
                + "else|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "  stmt;\n"
                + "else\n"
                + "  |\n"
                );
    }
    
    public void testEnterAfterIfElseStmt() {
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
    
    
    public void testIdentMain() {
        setCppEditorKit(false);
        setDefaultsOptions();
        setLoadDocumentText(
            "int main() {|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int main() {\n" +
            "    \n");
    }

    public void testIdentMainHalf() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
            "int main() {|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int main() {\n" +
            "  \n");
    }

    public void testIdentMainHalf2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
            "int main()|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int main()\n" +
            "\n");
    }

    public void testIdentMainHalf3() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
            "int main()\n"+
            "{|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int main()\n" +
            "{\n" +
            "  \n");
    }

    public void testIZ101099() {
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setDefaultsOptions();
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
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        setLoadDocumentText(
            "int longmain(int a,|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "int longmain(int a,\n" +
            "             \n");
    }

    /**
     * test parameter aligning
     */
    public void testIdentCallParameters() {
        setCppEditorKit(false);
        setDefaultsOptions();
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
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        setLoadDocumentText(
            "a = longmain(a,|\n");
        indentNewLine();
        assertDocumentText("Incorrect identing of main",
            "a = longmain(a,\n" +
            "             \n");
    }

    public void testIdentNewLineLocalDeclararion() throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "tree\n" +
            "disp(int i){\n" +
            "  int i = |\n" +
            "}"
            );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect identing of New Line Local Declararion",
            "tree\n" +
            "disp(int i){\n" +
            "  int i = \n" +
            "  |\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement() throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = |\n" +
            "}"
            );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect identing of New Line Local Statement",
            "tree\n" +
            "disp(int i){\n" +
            "  i = \n" +
            "          |\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement2() throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,|)\n" +
            "}"
            );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect identing of New Line Local Statement",
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        |)\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement3() throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+|)\n" +
            "}"
            );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect identing of New Line Local Statement",
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+\n" +
            "          |)\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement4() throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+foo(a,|))\n" +
            "}"
            );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect identing of New Line Local Statement",
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+foo(a,\n" +
            "              |))\n" +
            "}"
            );
    }

    // IZ#135150:GNU style: wrong indent in 'if else' expression
    public void testIZ135150() throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "int\n" +
            "main()\n" +
            "{\n" +
            "  int i = 0;\n" +
            "  if (i == 0)\n" +
            "    i = 1;\n" +
            "  else\n" +
            "    {|\n"
            );
        indentNewLine();
        assertDocumentTextAndCaret("IZ#135150:GNU style: wrong indent in 'if else' expression",
            "int\n" +
            "main()\n" +
            "{\n" +
            "  int i = 0;\n" +
            "  if (i == 0)\n" +
            "    i = 1;\n" +
            "  else\n" +
            "    {\n" +
            "      |\n"
            );
    }
    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineIfCondition, true);
        setLoadDocumentText(
            "if (a &&|)");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "if (a &&\n"+
            "    )");
    }
    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineWhileCondition, true);
        setLoadDocumentText(
            "while(a &&|)");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "while(a &&\n"+
            "      )");
    }

    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_3() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                putBoolean(EditorOptions.alignMultilineFor, true);
        setLoadDocumentText(
            "for  (int a = 0;|)");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "for  (int a = 0;\n"+
            "      )");
    }

    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_4() {
        setCppEditorKit(false);
        setDefaultsOptions();
        setLoadDocumentText(
            "for  (int a = 0;|)");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "for  (int a = 0;\n"+
            "        )");
    }
    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_5() {
        setCppEditorKit(false);
        setDefaultsOptions();
        setLoadDocumentText(
            "if (a &&|)");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "if (a &&\n"+
            "        )");
    }

    public void testIZ161572() {
        setDefaultsOptions();
        setLoadDocumentText(
            "enum {\n" +
            "  t1 = 1,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "enum {\n" +
            "  t1 = 1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_1() {
        setDefaultsOptions();
        setLoadDocumentText(
            "enum A {\n" +
            "  t1 = 1,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "enum A {\n" +
            "  t1 = 1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "enum A {\n" +
            "  t1 = 1,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "enum A {\n" +
            "  t1 = 1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_3() {
        setDefaultsOptions();
        setLoadDocumentText(
            "enum A {\n" +
            "  t1,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "enum A {\n" +
            "  t1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_4() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A {\n" +
            "  int a,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "class A {\n" +
            "  int a,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_5() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A {\n" +
            "  int b;\n" +
            "  int a,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "class A {\n" +
            "  int b;\n" +
            "  int a,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_6() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A {\n" +
            "  int b(int p, int j){}\n" +
            "  int a,|\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "class A {\n" +
            "  int b(int p, int j){}\n" +
            "  int a,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_7() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A {\n" +
            "  int b(int p, int j){\n" +
            "      int a,|\n" +
            "  }\n" +
            "}");
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:161572 Wrong indent for multiline code",
            "class A {\n" +
            "  int b(int p, int j){\n" +
            "      int a,\n" +
            "              \n" +
            "  }\n" +
            "}");
    }

    public void testIZ168505() {
        setDefaultsOptions();
        setLoadDocumentText(
            "std::cout |<< \"Welcome ...\" << std::endl;\n"
            );
        indentNewLine();
        assertDocumentText("Incorrect identing IZ:168505 cout arrows should be better aligned, like in emacs",
            "std::cout \n" +
            "        << \"Welcome ...\" << std::endl;\n"
            );
    }
}
