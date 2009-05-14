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
 * C/C++ specific tests begin from testSystemInclude
 * @author Alexander Simon
 */
public class BracketCompletionTestCase extends EditorBase  {

    public BracketCompletionTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    // ------- Tests for completion of right parenthesis ')' -------------
    
    public void testRightParenSimpleMethodCall() {
        setDefaultsOptions();
        setLoadDocumentText("m()|)");
        assertTrue(isSkipRightParen());
    }
    
    public void testRightParenSwingInvokeLaterRunnable() {
        setDefaultsOptions();
        setLoadDocumentText("SwingUtilities.invokeLater(new Runnable()|))");
        assertTrue(isSkipRightParen());
    }

    public void testRightParenSwingInvokeLaterRunnableRun() {
        setDefaultsOptions();
        setLoadDocumentText(
            "SwingUtilities.invokeLater(new Runnable() {\n"
          + "    public void run()|)\n"
          + "})"  
            
        );
        assertTrue(isSkipRightParen());
    }
    
    public void testRightParenIfMethodCall() {
        setDefaultsOptions();
        setLoadDocumentText(
            " if (a()|) + 5 > 6) {\n"
          + " }"
        );
        assertTrue(isSkipRightParen());
    }

    public void testRightParenNoSkipNonBracketChar() {
        setDefaultsOptions();
        setLoadDocumentText("m()| ");
        assertFalse(isSkipRightParen());
    }

    public void testRightParenNoSkipDocEnd() {
        setDefaultsOptions();
        setLoadDocumentText("m()|");
        assertFalse(isSkipRightParen());
    }

    
    // ------- Tests for completion of right brace '}' -------------
    
    public void testAddRightBraceIfLeftBrace() {
        setDefaultsOptions();
        setLoadDocumentText("if (true) {|");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceWhiteSpace() {
        setDefaultsOptions();
        setLoadDocumentText("if (true) { \t|\n");
        assertTrue(isAddRightBrace());
    }
    
    public void testAddRightBraceIfLeftBraceLineComment() {
        setDefaultsOptions();
        setLoadDocumentText("if (true) { // line-comment|\n");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceBlockComment() {
        setDefaultsOptions();
        setLoadDocumentText("if (true) { /* block-comment */|\n");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceAlreadyPresent() {
        setDefaultsOptions();
        setLoadDocumentText(
            "if (true) {|\n"
          + "}"
        );
        assertFalse(isAddRightBrace());
    }

    public void testAddRightBraceCaretInComment() {
        setDefaultsOptions();
        setLoadDocumentText(
            "if (true) { /* in-block-comment |\n"
        );
        assertFalse(isAddRightBrace());
    }
    
    public void testSimpleAdditionOfOpeningParenthesisAfterWhile () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "while |"
        );
        typeChar('(', false);
        assertDocumentTextAndCaret ("Even a closing ')' should be added", 
            "while (|)"
        );
    }

    
    // ------- Tests for completion of quote (") -------------    
    public void testSimpleQuoteInEmptyDoc () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote In Empty Doc", 
            "\"|\""
        );
    }

    public void testSimpleQuoteAtBeginingOfDoc () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "|  "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote At Begining Of Doc", 
            "\"|\"  "
        );
    }

    public void testSimpleQuoteAtEndOfDoc () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  |"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote At End Of Doc", 
            "  \"|\""
        );
    }
    
    public void testSimpleQuoteInWhiteSpaceArea () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  |  "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote In White Space Area", 
            "  \"|\"  "
        );
    }
    
    public void testQuoteAtEOL () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  |\n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote At EOL", 
            "  \"|\"\n"
        );
    }
    
    public void testQuoteWithUnterminatedStringLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  \"unterminated string| \n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote With Unterminated String Literal", 
            "  \"unterminated string\"| \n"
        );
    }
    
    public void testQuoteAtEOLWithUnterminatedStringLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  \"unterminated string |\n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote At EOL With Unterminated String Literal", 
            "  \"unterminated string \"|\n"
        );
    }

    public void testQuoteInsideStringLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  \"stri|ng literal\" "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside String Literal", 
            "  \"stri\"|ng literal\" "
        );
    }

    public void testQuoteInsideEmptyParentheses () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(|) "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Empty Parentheses", 
            " printf(\"|\") "
        );
    }

    public void testQuoteInsideNonEmptyParentheses () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(|some text) "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Non Empty Parentheses", 
            " printf(\"|some text) "
        );
    }
    
    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParentheses () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(i+|) "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Non Empty Parentheses Before Closing Parentheses", 
            " printf(i+\"|\") "
        );
    }
    
    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedStringLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(\"unterminated string literal |); "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Non Empty Parentheses Before Closing Parentheses And Unterminated String Literal", 
            " printf(\"unterminated string literal \"|); "
        );
    }

    public void testQuoteBeforePlus () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(|+\"string literal\"); "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Plus", 
            " printf(\"|\"+\"string literal\"); "
        );
    }

    public void testQuoteBeforeComma () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s[] = new String[]{|,\"two\"};"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Comma", 
            "String s[] = new String[]{\"|\",\"two\"};"
        );
    }

    public void testQuoteBeforeBrace () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s[] = new String[]{\"one\",|};"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Brace", 
            "String s[] = new String[]{\"one\",\"|\"};"
        );
    }

    public void testQuoteBeforeSemicolon() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s = \"\" + |;"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Semicolon", 
            "String s = \"\" + \"|\";"
        );
    }

    public void testQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s = \"\" +| ;"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Semicolon With Whitespace", 
            "String s = \"\" +\"|\" ;"
        );
    }

    public void testQuoteAfterEscapeSequence() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "\\|"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Semicolon With Whitespace", 
            "\\\"|"
        );
    }
    
    /** issue #69524 */
    public void testQuoteEaten() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('"');
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Eaten", 
            "\"\"|"
        );
    }    

    /** issue #69935 */
    public void testQuoteInsideComments() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "/** |\n */"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Comments", 
            "/** \"|\n */"
        );
    }    
    
    /** issue #71880 */
    public void testQuoteAtTheEndOfLineCommentLine() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "// test line comment |\n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote At The End Of Line Comment Line", 
            "// test line comment \"|\n"
        );
    }    
    
    
    // ------- Tests for completion of single quote (') -------------        
    
    public void testSingleQuoteInEmptyDoc () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote In Empty Doc", 
            "'|'"
        );
    }

    public void testSingleQuoteAtBeginingOfDoc () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "|  "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At Begining Of Doc", 
            "'|'  "
        );
    }

    public void testSingleQuoteAtEndOfDoc () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  |"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At End Of Doc", 
            "  '|'"
        );
    }
    
    public void testSingleQuoteInWhiteSpaceArea () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  |  "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote In White Space Area", 
            "  '|'  "
        );
    }
    
    public void testSingleQuoteAtEOL () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  |\n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At EOL", 
            "  '|'\n"
        );
    }
    
    public void testSingleQuoteWithUnterminatedCharLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  '| \n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote With Unterminated Char Literal", 
            "  ''| \n"
        );
    }
    
    public void testSingleQuoteAtEOLWithUnterminatedCharLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  ' |\n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At EOL With Unterminated Char Literal", 
            "  ' '|\n"
        );
    }

    public void testSingleQuoteInsideCharLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "  '| ' "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Char Literal", 
            "  ''| ' "
        );
    }

    public void testSingleQuoteInsideEmptyParentheses () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(|) "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Empty Parentheses", 
            " printf('|') "
        );
    }

    public void testSingleQuoteInsideNonEmptyParentheses () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(|some text) "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Non Empty Parentheses", 
            " printf('|some text) "
        );
    }
    
    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParentheses () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(i+|) "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Non Empty Parentheses Before Closing Parentheses", 
            " printf(i+'|') "
        );
    }
    
    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedCharLiteral () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(' |); "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Non Empty Parentheses Before Closing Parentheses And Unterminated Char Literal", 
            " printf(' '|); "
        );
    }

    public void testSingleQuoteBeforePlus () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            " printf(|+\"string literal\"); "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Plus", 
            " printf('|'+\"string literal\"); "
        );
    }

    public void testSingleQuoteBeforeComma () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s[] = new String[]{|,\"two\"};"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Comma", 
            "String s[] = new String[]{'|',\"two\"};"
        );
    }

    public void testSingleQuoteBeforeBrace () throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s[] = new String[]{\"one\",|};"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Brace", 
            "String s[] = new String[]{\"one\",'|'};"
        );
    }

    public void testSingleQuoteBeforeSemicolon() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s = \"\" + |;"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Semicolon", 
            "String s = \"\" + '|';"
        );
    }

    public void testsingleQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "String s = \"\" +| ;"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Semicolon With Whitespace", 
            "String s = \"\" +'|' ;"
        );
    }

    public void testSingleQuoteAfterEscapeSequence() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "\\|"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Semicolon With Whitespace", 
            "\\'|"
        );
    }
    
    /** issue #69524 */
    public void testSingleQuoteEaten() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('\'');
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Eaten", 
            "''|"
        );
    }    
    
    /** issue #69935 */
    public void testSingleQuoteInsideComments() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "/* |\n */"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Comments", 
            "/* \'|\n */"
        );
    }    

    /** issue #71880 */
    public void testSingleQuoteAtTheEndOfLineCommentLine() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "// test line comment |\n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At The End Of Line Comment Line", 
            "// test line comment \'|\n"
        );
    }    

    public void testSystemInclude() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "#include |\n"
        );
        typeQuoteChar('<');
        assertDocumentTextAndCaret ("System Include", 
            "#include <|>\n"
        );
    }

    public void testUserInclude() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "#include |\n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("User Include", 
            "#include \"|\"\n"
        );
    }

    public void testArray() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "int a|\n"
        );
        typeQuoteChar('[');
        assertDocumentTextAndCaret ("Array", 
            "int a[|]\n"
        );
    }
    
    public void testRightBracePreprocessor() {
        setDefaultsOptions();
        setLoadDocumentText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "#else\n" +
            "    if (b){|\n" +
            "#endif\n" +
            "    }\n" +
            "}"
        );
        assertFalse(isAddRightBrace());
    }

    public void testRightBracePreprocessor2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){|\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "    }\n" +
            "}"
        );
        assertFalse(isAddRightBrace());
    }

    public void testRightBracePreprocessor3() {
        setDefaultsOptions();
        setLoadDocumentText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){|\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "//    }\n" +
            "}"
        );
        assertTrue(isAddRightBrace());
    }

    public void testRightBracePreprocessor4() {
        setDefaultsOptions();
        setLoadDocumentText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "    if (b){|\n" +
            "    }\n" +
            "}"
        );
        assertTrue(isAddRightBrace());
    }

    public void testRightBracePreprocessor5() {
        setDefaultsOptions();
        setLoadDocumentText(
            "void foo(){\n" +
            "#define PAREN {\n" +
            "    if (b){|\n" +
            "    }\n" +
            "}"
        );
        assertFalse(isAddRightBrace());
    }
    
    public void testIZ102091() throws Exception {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        setLoadDocumentText (
            "if(i)\n"+
            "    |"
        );
        typeChar('{', true);
        assertDocumentTextAndCaret ("IZ102091\n", 
            "if(i)\n"+
            "{|"
        );
    }
    
    public void testColonAfterPublic() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText (
            "class A{\n" +
            "    public|\n" +
            "}\n"
        );
        typeChar(':', true);
        assertDocumentText("Colon After Public", 
            "class A{\n" +
            "public:\n" +
            "}\n"
        );
    }
    
    public void testIdentFunctionName()  throws Exception {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
            "tree\n" +
            "        |"
            );
        typeChar('d',true);
        assertDocumentTextAndCaret("Incorrect identing of main",
            "tree\n" +
            "d|"
            );
    }
    
    // test line break
    
    public void testBreakLineInString1() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "char* a = \"|\"");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "char* a = \"\"\n" +
                "\"|\"");
    }    

    public void testBreakLineInString2() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "           char* a = \"\\|\"");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "           char* a = \"\\\n" +
                "|\"");
    }     

    public void testBreakLineInString2_1() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "           char* a = \"\\\\|\"");
        breakLine();
        // TODO: second line should be in the first column after fixing bug in indentation
        assertDocumentTextAndCaret("Incorrect identing of main",
                "           char* a = \"\\\\\"\n" +
                "           \"|\"");
    }

    public void testBreakLineInString3() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "             char* a = \"\\|");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "             char* a = \"\\\n" +
                "|");
    }    

    public void testBreakLineInString31() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "             char* a = \"\\|\n");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "             char* a = \"\\\n" +
                "|\n");
    }

    public void testBreakLineInString4() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "             char* a = \"\\|\"");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "             char* a = \"\\\n" +
                "|\"");
    }

    public void testBreakLineInString41() throws Exception {
        setDefaultsOptions();
        setLoadDocumentText(
                "             char* a = \"\\|\"\n");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "             char* a = \"\\\n" +
                "|\"\n");
    }

    public void testBreakLineAfterLCurly() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void foo() {|");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "void foo() {\n" +
                "    |\n" +
                "}");
    }
    
    public void testBreakLineAfterLCurly2() {
        setDefaultsOptions();
        setLoadDocumentText(
                "struct A {|");
        breakLine();
        assertDocumentTextAndCaret("Incorrect identing of main",
                "struct A {\n" +
                "    |\n" +
                "};");
    }    
}
