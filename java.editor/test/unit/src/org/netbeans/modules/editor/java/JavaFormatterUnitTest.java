/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.java.JavaTokenContext;


/**
 * Test java bracket completion.
 *
 * @autor Miloslav Metelka
 */
public class JavaFormatterUnitTest extends JavaBaseDocumentUnitTestCase {
    
    public JavaFormatterUnitTest(String testMethodName) {
        super(testMethodName);
    }
    
    // ------- Tests for completion of right parenthesis ')' -------------
    
    /**
     *
     */
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
          + "    System.out.println(|\n"
          + "\n"
        );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
            "void m() {\n"
          + "    System.out.println(\n"
          + "            |\n"
          + "\n"
        );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnLineThree() {
        setLoadDocumentText(
            "void m() {\n"
          + "    System.out.println(\n"
          + "            \"haf\"|\n"
          + "\n"
        );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
            "void m() {\n"
          + "    System.out.println(\n"
          + "            \"haf\"\n"
          + "            |\n"
          + "\n"
        );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnAfterSemiColon() {
        setLoadDocumentText(
            "void m() {\n"
          + "    System.out.println(\n"
          + "            \"haf\");|\n"
          + "\n"
        );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
            "void m() {\n"
          + "    System.out.println(\n"
          + "            \"haf\");\n"
          + "    |\n"
          + "\n"
        );
        
    }
    
    public void testEnterInMultiLineClassDeclaration() {
        setLoadDocumentText(
            "public class C\n"
          + "        implements Runnable\n"
          + "        throws Exception {|\n"
          + "}\n"
        );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
            "public class C\n"
          + "        implements Runnable\n"
          + "        throws Exception {\n"
          + "    |\n"
          + "}\n"
        );
        
    }
    
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
            "if (int i = 0; i < 10; i++)|\n"
        );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
            "if (int i = 0; i < 10; i++)\n"
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

    // -------- Reformat tests -----------
    
    public void testReformatMultiLineSystemOutPrintln() {
        setLoadDocumentText(
            "void m() {\n"
          + "    System.out.println(\n"
          + "    \"haf\");\n"
          + "}\n"
        );
        reformat();
        assertDocumentText("Incorrect new-line indent",
            "void m() {\n"
          + "    System.out.println(\n"
          + "            \"haf\");\n"
          + "}\n"
        );
        
    }

    public void testReformatMultiLineClassDeclaration() {
        setLoadDocumentText(
            "public class C\n"
          + "implements Runnable\n"
          + "throws Exception {|\n"
          + "System.out.println(\"haf\");\n"
          + "}\n"
        );
        reformat();
        assertDocumentText("Incorrect new-line indent",
            "public class C\n"
          + "        implements Runnable\n"
          + "        throws Exception {\n"
          + "    System.out.println(\"haf\");\n"
          + "}\n"
        );
        
    }
    
    public void testReformatMultiArray(){
        setLoadDocumentText(
            "static int[][] CONVERT_TABLE={{1,2},{2,3},\n"
          + "{3,4},{4,5},{5,6},|\n"
          + "{6,7},{7,8},{8,9}};\n");
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
            "static int[][] CONVERT_TABLE={{1,2},{2,3},\n"
          + "        {3,4},{4,5},{5,6},\n"
          + "        {6,7},{7,8},{8,9}};\n");
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

    // ------- Private methods -------------
    
    private void indentNewLine() {
        Formatter f = getDocument().getFormatter();
        int offset = f.indentNewLine(getDocument(), getCaretOffset());
        getCaret().setDot(offset);
    }
    
    private void reformat() {
        Formatter f = getDocument().getFormatter();
        try {
            f.reformat(getDocument(), 0, getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
        }
    }
        
}
