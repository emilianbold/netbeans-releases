/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.php.editor.PHPCodeCompletionTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 * @todo Test that if you insert x="" and then DELETE the ", it wipes out BOTH of them!
 * @todo Try typing in whole source files and other than tracking missing end and } closure
 *   statements the buffer should be identical - both in terms of quotes to the rhs not having
 *   accumulated as well as indentation being correct.
 * @todo
 *   // TODO: Test
 *   // - backspace deletion
 *   // - entering incomplete output
 *   // automatic reindentation of "end", "else" etc.
 *
 *
 *
 * @author Tor Norbye
 */
public class PHPBracketCompleterTest extends PHPCodeCompletionTestBase {

    public PHPBracketCompleterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    private static String wrapAsPhp(String s) {
        // XXX: remove \n
        return "<?php\n" + s + "\n?>";
    }

    @Override
    public void insertBreak(String original, String expected) throws Exception {
        insertBreak(original, expected, new HashMap<String, Object>(FmtOptions.getDefaults()));
    }

    public void insertBreak(String original, String expected, Map<String, Object> options) throws Exception {
        JEditorPane ta = getPane(original);
        Document doc = ta.getDocument();
        setOptionsForDocument(doc, options);
        super.insertBreak(wrapAsPhp(original), wrapAsPhp(expected));
    }

    private void setOptionsForDocument(Document doc, Map<String, Object> options) throws Exception {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (String option : options.keySet()) {
            Object value = options.get(option);
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer)value).intValue());
            }
            else if (value instanceof String) {
                prefs.put(option, (String)value);
            }
            else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean)value).toString());
            }
            else if (value instanceof CodeStyle.BracePlacement) {
                prefs.put(option, ((CodeStyle.BracePlacement)value).name());
            }
            else if (value instanceof CodeStyle.WrapStyle) {
                prefs.put(option, ((CodeStyle.WrapStyle)value).name());
            }
        }
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    @Override
    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws Exception {
        original = wrapAsPhp(original);
        expected = wrapAsPhp(expected);
        super.insertChar(original, insertText, expected, selection, codeTemplateMode);
    }

    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode, Map<String, Object> formatPrefs) throws Exception {
        String source = wrapAsPhp(original);
        String reformatted = wrapAsPhp(expected);
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        if (selection != null) {
            int start = original.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    original.indexOf(selection, start+1) == -1);
            ta.setSelectionStart(start);
            ta.setSelectionEnd(start+selection.length());
            assertEquals(selection, ta.getSelectedText());
        }

        BaseDocument doc = (BaseDocument) ta.getDocument();

        if (codeTemplateMode) {
            // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
            String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
            doc.putProperty(EDITING_TEMPLATE_DOC_PROPERTY, Boolean.TRUE);
        }

        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, null);

        if (formatter != null && formatPrefs != null) {
            setOptionsForDocument(doc, formatPrefs);
        }
        runKitAction(ta, DefaultEditorKit.defaultKeyTypedAction, ""+insertText);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    protected void testIndentInFile(String file) throws Exception {
        testIndentInFile(file, null, 0);
    }

    protected void testIndentInFile(String file, IndentPrefs preferences, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(preferences);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);


        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        prefs.putInt(FmtOptions.INITIAL_INDENT, initialIndent);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }

    @Override
    protected void deleteChar(String original, String expected) throws Exception {
        super.deleteChar(wrapAsPhp(original), wrapAsPhp(expected));
    }

    @Override
    protected void deleteWord(String original, String expected) throws Exception {
        super.deleteWord(wrapAsPhp(original), wrapAsPhp(expected));
    }

// XXX: currently failing, but should be fixed (probably ruby specific - needs to be investigated)
//    public void testContComment5() throws Exception {
//        // No auto-// on new lines
//        if (PHPBracketCompleter.CONTINUE_COMMENTS) {
//            insertBreak("      // ^", "      // \n      // ^");
//        } else {
//            insertBreak("      // ^", "      // \n      ^");
//        }
//    }
//
//
//    public void testContComment10() throws Exception {
//        insertBreak("//foo\n^// foobar", "//foo\n// ^\n// foobar");
//    }
//
//    public void testContComment12() throws Exception {
//        insertBreak("  code\n^// foobar", "  code\n\n  ^// foobar");
//    }
//
//    public void testContComment14() throws Exception {
//        insertBreak("def foo\n  code\n^// foobar\nend\n", "def foo\n  code\n\n  ^// foobar\nend\n");
//    }
//
//    public void testContComment17() throws Exception {
//        insertBreak("def foo\n  // cmnt1\n^  // cmnt2\nend\n", "def foo\n  // cmnt1\n  // ^\n  // cmnt2\nend\n");
//    }
//
//    public void testDeleteContComment() throws Exception {
//        deleteChar("// ^", "^");
//        deleteChar("\n// ^", "\n^");
//    }
//
//    public void testDeleteContComment2() throws Exception {
//        deleteChar("// ^  ", "^  ");
//        deleteChar("\n// ^  ", "\n^  ");
//    }
//
//    public void testNoDeleteContComment() throws Exception {
//        deleteChar("//  ^", "// ^");
//        deleteChar("//^", "^");
//        deleteChar("puts '// ^'", "puts '//^'");
//    }
//
//    public void testReplaceSelectionNotInTemplateMode2() throws Exception {
//        insertChar("x = \"foo^bar\"", '#', "x = \"#{^}bar\"", "foo", true);
//    }
//
//    public void testInsertIf2() throws Exception {
//        insertBreak("    if true\n    else", 20, "    if true\n    else\n      end", 27);
//    }
//
//    public void testHeredoc1() throws Exception {
//        insertBreak("x=<<FOO^\n", "x=<<FOO\n^\nFOO\n");
//    }
//
//    public void testHeredoc2() throws Exception {
//        insertBreak("x=f(<<FOO,^\n", "x=f(<<FOO,\n^\nFOO\n");
//    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testInsertBreakAfterClass2() throws Exception {
        insertBreak("class Foo {^\n    \n}", "class Foo {\n    ^\n    \n}");
    }

    public void testInsertBreakAfterClass() throws Exception {
        insertBreak("class Foo {^", "class Foo {\n    ^\n}");
    }

    public void testInsertBreakAfterFunction() throws Exception {
        insertBreak("function foo() {^", "function foo() {\n    ^\n}");
    }

    public void testInsertBreakAfterIf() throws Exception {
        insertBreak("if (1) {^", "if (1) {\n    ^\n}");
    }

    public void testInsertBreakAfterIfElse() throws Exception {
        insertBreak("if (1) {\n    \n} else {^", "if (1) {\n    \n} else {\n    ^\n}");
    }
    public void testInsertBreakAfterWhile() throws Exception {
        insertBreak("while (1) {^", "while (1) {\n    ^\n}");
    }
    public void testInsertBreakAfterCatch() throws Exception {
        insertBreak("try {\n    \n} catch (Exception $exc) {^",
                "try {\n    \n} catch (Exception $exc) {\n    ^\n}");
    }
    public void testInsertBreakAfterTry() throws Exception {
        insertBreak("try {^\n} catch (Exception $ex) {\n}",
                "try {\n    ^\n} catch (Exception $ex) {\n}");
    }
    public void testInsertBreakAfterForEach() throws Exception {
        insertBreak("foreach ($array_variable as $number_variable => $variable) {^",
                "foreach ($array_variable as $number_variable => $variable) {\n    ^\n}");
    }

    public void testInsertBreakInArray1() throws Exception {
        insertBreak("array(^)", "array(\n    ^\n)");
    }

    public void testInsertBreakInArray2() throws Exception {
        insertBreak("array(^\n)", "array(\n    ^\n)");
    }

    public void testInsertBreakInArray3() throws Exception {
        insertBreak("array(\n    'a',^\n)", "array(\n    'a',\n    ^\n)");
    }

    public void testInsertBreakInArray4() throws Exception {
        insertBreak("function a() {\n    array(\n        'a',^\n    )\n}", "function a() {\n    array(\n        'a',\n        ^\n    )\n}");
    }

    public void testInsertBreakInArray5() throws Exception {
        insertBreak("array(array(array(^)))", "array(array(array(\n    ^\n)))");
    }

    public void testInsertBreakInArray6() throws Exception {
        insertBreak("array(array(array(^\n)))", "array(array(array(\n    ^\n)))");
    }

    public void testInsertBreakInSwitch1() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':^\n}", "switch ($a) {\n    case 'a':\n        ^\n}");
    }

    public void testInsertBreakInSwitch2() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':\n        echo 'a';\n        break;^\n}", "switch ($a) {\n    case 'a':\n        echo 'a';\n        break;\n    ^\n}");
    }

    public void testInsertBreakInSwitch3() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':\n        echo 'a';\n        break 1;^\n}", "switch ($a) {\n    case 'a':\n        echo 'a';\n        break 1;\n    ^\n}");
    }

    public void testInsertBreakInSwitch8() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':\n        switch ($b) {\n            case 'b':\n                echo 'b';\n                break;^\n        }\n       \nbreak;^\n}", "switch ($a) {\n    case 'a':\n        switch ($b) {\n            case 'b':\n                echo 'b';\n                break;\n            ^\n        }\n       \nbreak;\n}");
    }

    public void testInsertBreakInFor() throws Exception {
        insertBreak("for (;;) {\n    break;^\n}", "for (;;) {\n    break;\n    ^\n}");
    }

    public void testInsertBreakInForeach() throws Exception {
        insertBreak("foreach ($arr as $val) {\n    break;^\n}", "foreach ($arr as $val) {\n    break;\n    ^\n}");
    }

    public void testInsertBreakInWhile() throws Exception {
        insertBreak("while (true) {\n    break;^\n}", "while (true) {\n    break;\n    ^\n}");
    }

    public void testInsertBreakInDo() throws Exception {
        insertBreak("do {\n    break;^\n} while (true)", "do {\n    break;\n    ^\n} while (true)");
    }

    public void testNoMatchInComments() throws Exception {
        insertChar("// Hello^", '"', "// Hello\"^");
        insertChar("// Hello^", '\'', "// Hello'^");
        insertChar("// Hello^", '[', "// Hello[^");
        insertChar("// Hello^", '(', "// Hello(^");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("x = \"^\"", '\'', "x = \"'^\"");
        insertChar("x = \"^\"", '[', "x = \"[^\"");
        insertChar("x = \"^\"", '(', "x = \"(^\"");
        insertChar("x = \"^)\"", ')', "x = \")^)\"");
        insertChar("x = '^'", '"', "x = '\"^'");
        insertChar("x = \"\nf^\n\"", '\'', "x = \"\nf'^\n\"");
        insertChar("x = \"\nf^\n\"", '[', "x = \"\nf[^\n\"");
        insertChar("x = \"\nf^\n\"", '(', "x = \"\nf(^\n\"");
        insertChar("x = '\nf^\n'", '"', "x = '\nf\"^\n'");
    }

    public void testSingleQuotes1() throws Exception {
        insertChar("x = ^", '\'', "x = '^'");
    }

    public void testSingleQuotes2() throws Exception {
        insertChar("x = '^'", '\'', "x = ''^");
    }

    public void testSingleQuotes3() throws Exception {
        insertChar("x = '^'", 'a', "x = 'a^'");
    }

    public void testSingleQuotes4() throws Exception {
        insertChar("x = '\\^'", '\'', "x = '\\'^'");
    }

    public void testSingleQuotes5() throws Exception {
        insertChar("x = '\\'^", '\'', "x = '\\''^");
    }

    public void testIssue209867_01() throws Exception {
        insertChar("$x = 'this is (long'^) string';", '\'', "$x = 'this is (long''^) string';");
    }

    public void testIssue209867_02() throws Exception {
        insertChar("$x = 'this is long'^ string';", '\'', "$x = 'this is long''^ string';");
    }

    public void testIssue209867_03() throws Exception {
        insertChar("$x = 'this is long^ string';", '\'', "$x = 'this is long'^ string';");
    }

    public void testIssue209867_04() throws Exception {
        insertChar("if ($x == ^) {}", '\'', "if ($x == '^') {}");
    }

    public void testIssue209867_05() throws Exception {
        insertChar("if ($x == '^) {}", '\'', "if ($x == ''^) {}");
    }

    public void testIssue209867_06() throws Exception {
        insertChar("$x = 'this is long string' . $foo . ^;", '\'', "$x = 'this is long string' . $foo . '^';");
    }

    public void testIssue209867_07() throws Exception {
        insertChar("$x = 'this is long string'^;", '\'', "$x = 'this is long string''^;");
    }

    public void testDoubleQuotes1() throws Exception {
        insertChar("x = ^", '"', "x = \"^\"");
    }

    public void testDoubleQuotes2() throws Exception {
        insertChar("x = \"^\"", '"', "x = \"\"^");
    }

    public void testDoubleQuotes3() throws Exception {
        insertChar("x = \"^\"", 'a', "x = \"a^\"");
    }

    public void testDobuleQuotes4() throws Exception {
        insertChar("x = \"\\^\"", '"', "x = \"\\\"^\"");
    }

    public void testDobuleQuotes5() throws Exception {
        insertChar("x = \"\\\"^", '"', "x = \"\\\"\"^");
    }

    public void testIssue153062() throws Exception {
        insertChar("//line comment\n^", '"', "//line comment\n\"^\"");
    }

    public void testIssue153062_2() throws Exception {
        insertChar("//line comment^", '"', "//line comment\"^");
    }

    public void testIssue162139() throws Exception {
        insertChar("^\\", '"', "\"^\\");
    }

    public void testIssue174891() throws Exception {
       insertNewline("\n" +
               "^/**\n" +
               "*/",
               "\n" +
               "\n" +
               "^/**\n" +
               "*/", null);
    }

    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertEnd2() throws Exception {
        insertBreak("class Foo {^", "class Foo {\n    ^\n}");
    }

    public void testInsertEnd3() throws Exception {
        insertBreak("class Foo {^\n}", "class Foo {\n    ^\n}");
    }

    public void testInsertEnd4() throws Exception {
        insertBreak("for(;;) {^", "for(;;) {\n    ^\n}");
    }

    public void testInsertEnd5() throws Exception {
        insertBreak("if ($something) {^", "if ($something) {\n    ^\n}");
    }

    public void testInsertEnd6() throws Exception {
        insertBreak("if ($something) {\n  \n} else {^", "if ($something) {\n  \n} else {\n    ^\n}");
    }

    public void testInsertIf1() throws Exception {
        insertBreak("if ($something)^", "if ($something)\n    ^");
    }

    public void testInsertIf2() throws Exception {
        insertBreak("if ($something)\n  echo 'Hi!';\nelse^", "if ($something)\n  echo 'Hi!';\nelse\n    ^");
    }

    public void testBrackets1() throws Exception {
        insertChar("x = ^", '[', "x = [^]");
    }

    public void testBrackets2() throws Exception {
        insertChar("x = [^]", ']', "x = []^");
    }

    public void testBracketsSpecialName() throws Exception {
        // "[]" and "[]=" are valid method names!
        insertChar("def ^", '[', "def [^]");
    }

    public void testBracketsSpecialName2() throws Exception {
        // "[]" and "[]=" are valid method names!
        insertChar("def [^]", ']', "def []^");
    }

    public void testBrackets3() throws Exception {
        insertChar("x = [^]", 'a', "x = [a^]");
    }

    public void testBrackets4() throws Exception {
        insertChar("x = [^]", '[', "x = [[^]]");
    }

    public void testBrackets5() throws Exception {
        insertChar("x = [[^]]", ']', "x = [[]^]");
    }

    public void testBrackets6() throws Exception {
        insertChar("x = [[]^]", ']', "x = [[]]^");
    }

    public void testParens1() throws Exception {
        insertChar("x = ^", '(', "x = (^)");
    }

    public void testParens2() throws Exception {
        insertChar("x = (^)", ')', "x = ()^");
    }

    public void testParens3() throws Exception {
        insertChar("x = (^)", 'a', "x = (a^)");
    }

    public void testParens4() throws Exception {
        insertChar("x = (^)", '(', "x = ((^))");
    }

    public void testParens5() throws Exception {
        insertChar("x = ((^))", ')', "x = (()^)");
    }

    public void testParens6() throws Exception {
        insertChar("x = (()^)", ')', "x = (())^");
    }

    public void testBackspace1() throws Exception {
        deleteChar("x^", "^");
    }

    public void testBackspace2() throws Exception {
        deleteChar("x^y", "^y");
    }

    public void testBackspace3() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace4() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace5() throws Exception {
        deleteChar("x=\"^\"", "x=^");
    }

    public void testBackspace6() throws Exception {
        deleteChar("x='^'", "x=^");
    }

    public void testBackspace7() throws Exception {
        deleteChar("x=(^)", "x=^");
    }

    public void testBackspace7b() throws Exception {
        deleteChar("x=[^]", "x=^");
    }

    public void testBackspace8() throws Exception {
        // See bug 111534
        deleteChar("x={^}", "x=^");
    }

    public void testContComment() throws Exception {
        if (PhpTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }

    public void testContComment4() throws Exception {
        insertBreak("// foo\n^", "// foo\n\n^");
    }

    public void testContComment6() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    public void testContComment7() throws Exception {
        insertBreak("   // foo^\n   // bar", "   // foo\n   // ^\n   // bar");
    }

    public void testContComment9() throws Exception {
        insertBreak("^// foobar", "\n^// foobar");
    }

    public void testContComment11() throws Exception {
        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
    }

    public void testContComment15() throws Exception {
        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
    }

    public void testContComment16() throws Exception {
        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
    }

    public void testNoContComment() throws Exception {
        // No auto-// on new lines
        insertBreak("foo // ^", "foo // \n^");
    }

    public void testFreakOutEditor1() throws Exception {
        String before = "x = method_call(50, <<TOKEN1, \"arg3\", <<TOKEN2, /startofregexp/^\nThis is part of the string\nTOKEN1\nrestofregexp/)";
        String  after = "x = method_call(50, <<TOKEN1, \"arg3\", <<TOKEN2, /startofregexp^\nThis is part of the string\nTOKEN1\nrestofregexp/)";
        deleteChar(before, after);
    }

    public void testReplaceSelection1() throws Exception {
        insertChar("x = foo^", 'y', "x = y^", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("x = 'foo^bar'", '#', "x = '#^bar'", "foo");
    }

    public void testReplaceSelection2() throws Exception {
        insertChar("x = foo^", '"', "x = \"foo\"^", "foo");
    }

    public void testReplaceSelection5() throws Exception {
        insertChar("'(^position:absolute;'", '{', "'{^position:absolute;'", "(");
    }

    public void testReplaceSelection6() throws Exception {
        insertChar("'position^:absolute;'", '{', "'pos{^:absolute;'", "ition");
    }

    public void testReplaceSelectionChangeType1() throws Exception {
        insertChar("x = \"foo\"^", '\'', "x = 'foo'^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType2() throws Exception {
        insertChar("x = \"foo\"^", '{', "x = {foo}^", "\"foo\"");
    }

    public void testReplaceSelectionNotInTemplateMode1() throws Exception {
        insertChar("x = foo^", '"', "x = \"^\"", "foo", true);
    }

    public void testReplaceCommentSelectionBold() throws Exception {
        insertChar("# foo^", '*', "# *foo*^", "foo");
    }

    public void testReplaceCommentSelectionTerminal() throws Exception {
        insertChar("# foo^", '+', "# +foo+^", "foo");
    }

    public void testReplaceCommentSelectionItalic() throws Exception {
        insertChar("# foo^", '_', "# _foo_^", "foo");
    }

    public void testReplaceCommentSelectionWords() throws Exception {
        // No replacement if it contains multiple lines
        insertChar("# foo bar^", '*', "# *^", "foo bar");
    }

    public void testReplaceCommentOther() throws Exception {
        // No replacement if it's not one of the three chars
        insertChar("# foo^", 'x', "# x^", "foo");
    }

    public void testDeleteWord() throws Exception {
        deleteWord("$foo_bar_baz^", "$foo_bar_^");
    }
    public void testDeleteWord111303() throws Exception {
        deleteWord("foo::bar^", "foo::^");
        deleteWord("Foo::Bar^", "Foo::^");
        deleteWord("Foo::Bar_Baz^", "Foo::Bar_^");
    }
    public void testDeleteWordx111305() throws Exception {
        deleteWord("foo_bar^", "foo_^");
        deleteWord("x.foo_bar^.y", "x.foo_^.y");
    }

    public void testDeleteWord2() throws Exception {
        deleteWord("foo_bar_baz ^", "foo_bar_baz^");
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testDeleteWord3() throws Exception {
        deleteWord("FooBarBaz^", "FooBar^");
    }

    public void testDeleteWord4_110998() throws Exception {
        deleteWord("Blah::Set^Foo", "Blah::^Foo");
    }

    public void testdeleteWord5() throws Exception {
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testdeleteWords() throws Exception {
        deleteWord("foo bar^", "foo ^");
    }


    public void testDeleteWord4_110998c() throws Exception {
        String before = "  snark^\n";
        String after = "  ^\n";
        deleteWord(before, after);
    }

    public void testDeleteWord4_110998b() throws Exception {
        String before = "" +
"  snark(%w(a b c))\n" +
"  snark(%W(a b c))\n" +
"  snark^\n" +
"  snark(%Q(a b c))\n" +
"  snark(%w(a b c))\n";
        String after = "" +
"  snark(%w(a b c))\n" +
"  snark(%W(a b c))\n" +
"  ^\n" +
"  snark(%Q(a b c))\n" +
"  snark(%w(a b c))\n";
        deleteWord(before, after);
    }

    public void testBackwardsDeletion() throws Exception {
        String s = "Foo::Bar = whatever('hello')  \n  nextline";
        PHPBracketCompleter bc = new PHPBracketCompleter();
        for (int i = s.length(); i >= 1; i--) {
            String shortened = s.substring(0, i);
            BaseDocument doc = getDocument(shortened);

            JTextArea ta = new JTextArea(doc);
            Caret caret = ta.getCaret();
            int dot = i;
            caret.setDot(dot);
            int begin = bc.getNextWordOffset(doc, dot, true);
            if (begin == -1) {
                begin = Utilities.getPreviousWord(ta, dot);
            }

            assert begin != -1 && begin < i;
        }
    }

    public void test108889() throws Exception {
        // Reproduce 108889: AIOOBE and AE during editing
        // NOTE: While the test currently throws an exception, when the
        // exception is fixed the test won't actually pass; that's an expected
        // fail I will deal with later
        insertChar("x = %q((^))", 'a', "x = %q((a^))");
    }

    public void testInsertBrace01() throws Exception {
        String testString = "if (true)" +
                "\n" +
                "    ^";
        String result  = "if (true)" +
                "\n" +
                "{^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace02() throws Exception {
        String testString = "    class Name\n" +
                "          ^";
        String result  = "    class Name\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace03() throws Exception {
        String testString =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "          ^";
        String result  =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace04() throws Exception {
        String testString =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "^";
        String result  =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace05() throws Exception {
        String testString =
                "    $a = 10;\n" +
                "    while ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "          ^";
        String result  =
                "    $a = 10;\n" +
                "    while ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace06() throws Exception {
        String testString =
                "    $a = 10;\n" +
                "    do\n" +
                "          ^";
        String result  =
                "    $a = 10;\n" +
                "    do\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace07() throws Exception {
        String testString =
                "    foreach($zzz as $zzzz)\n" +
                "          ^";
        String result  =
                "    foreach($zzz as $zzzz)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace08() throws Exception {
        String testString =
                "    for($i = 0; $i < 10; $i++)\n" +
                "          ^";
        String result  =
                "    for($i = 0; $i < 10; $i++)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testBracePlacement01() throws Exception {
        String testString = "class Name\n" +
                "    ^";
        String result  = "class Name\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar(testString, '{', result, null, false, options);
    }

    public void testAlternativeSyntaxFor_01()throws Exception {
        testIndentInFile("testfiles/indent/switch_09.php");
    }

    public void testIsseu191443() throws Exception {
        String testString = "$test = (string^) ahoj;";
        String result  = "$test = (string)^ ahoj;";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, ')', result, null, false, options);
    }

    public void testIssue198699_01() throws Exception {
        insertChar("a selected^ word", '"', "a \"selected\"^ word", "selected");
    }

    public void testIssue200729_01() throws Exception {
        insertBreak("function foo() {\n"
                + "    /*^\n"
                + "}",
                "function foo() {\n"
                + "    /*\n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue200729_02() throws Exception {
        insertBreak("function foo() {\n"
                + "    /**^\n"
                + "}",
                "function foo() {\n"
                + "    /**\n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue200729_03() throws Exception {
        insertBreak("function foo() {\n"
                + "    /**\n"
                + "     * ^\n"
                + "}",
                "function foo() {\n"
                + "    /**\n"
                + "     * \n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue200729_04() throws Exception {
        insertBreak("function foo() {\n"
                + "    /*\n"
                + "     * ^\n"
                + "}",
                "function foo() {\n"
                + "    /*\n"
                + "     * \n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue198708_01() throws Exception {
        insertChar("if ($a=($i+1^)", ')', "if ($a=($i+1)^)");
    }

    public void testIssue198708_02() throws Exception {
        insertChar("if (($a=($i+1^))", ')', "if (($a=($i+1)^))");
    }

    public void testIssue198708_03() throws Exception {
        insertChar("if ($a=($i+1^))", ')', "if ($a=($i+1)^)");
    }

    public void testIssue198708_04() throws Exception {
        insertChar("if (($a=($i+1^)))", ')', "if (($a=($i+1)^))");
    }

    public void testIssue209638() throws Exception {
        insertChar("$test = array(\n"
                + "    array(^)\n"
                + ");", ')', "$test = array(\n"
                + "    array()^\n"
                + ");");
    }

    public void testIssue212301_01() throws Exception {
        insertChar("$foo = 'bar';^", '/', "/^", "$foo = 'bar';");
    }

    public void testIssue212301_02() throws Exception {
        insertChar("$foo = 'bar'^;", '/', "/^;", "$foo = 'bar'");
    }

    public void testIssue212301_03() throws Exception {
        insertChar("$foo = 'bar'^;", '/', "$foo = /^;", "'bar'");
    }

    public void testIssue202644() throws Exception {
        insertBreak("function foo($bar) {^\n    echo($bar);\n}\n\nfunction bar($foo) {",
                "function foo($bar) {\n    ^\n    echo($bar);\n}\n\nfunction bar($foo) {");
    }

    public void testIssue185001() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertBreak("while (true)\n    {^", "while (true)\n    {\n    ^\n    }", options);
    }

    public void testIssue198810_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("if (true)\n    ^", '{', "if (true)\n{^", null, false, options);
    }

    public void testIssue198810_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("if (true)\n{    foo();\n    ^", '}', "if (true)\n{    foo();\n}^", null, false, options);
    }

    public void testIssue198810_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("if (true)\n    ^", '{', "if (true)\n    {^", null, false, options);
    }

    public void testIssue198810_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("if (true)\n    {    foo();\n    ^", '}', "if (true)\n    {    foo();\n    }^", null, false, options);
    }

    public void testIssue198810_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    ^", '{', "function foo()\n{\n    while ($bar)\n    {^", null, false, options);
    }

    public void testIssue198810_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        ^", '{', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {^", null, false, options);
    }

    public void testIssue198810_07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n            ^", '}', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }^", null, false, options);
    }

    public void testIssue198810_08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n        ^", '}', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n    }^", null, false, options);
    }

    public void testIssue198810_09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n    }\n    ^", '}', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n    }\n}^", null, false, options);
    }

    public void testIssue198810_10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n    ^", '{', "function foo()\n{\n    while ($bar)\n        {^", null, false, options);
    }

    public void testIssue198810_11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n        {\n        if (true)\n        ^", '{', "function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {^", null, false, options);
    }

    public void testIssue198810_12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            ^", '}', "function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }^", null, false, options);
    }

    public void testIssue198810_13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        ^", '}', "function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        }^", null, false, options);
    }

    public void testIssue198810_14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n    {\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        }\n    ^", '}', "function foo()\n    {\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        }\n    }^", null, false, options);
    }

    public void testIssue170779_01() throws Exception {
        String original = "switch($value) {\n    case^\n}";
        String expected = "switch($value) {\n    case ^\n}";
        insertChar(original, ' ', expected);
    }

    public void testIssue170779_02() throws Exception {
        String original = "switch ($value) {\n    case 1:\n        break;\n    case^\n}";
        String expected = "switch ($value) {\n    case 1:\n        break;\n    case ^\n}";
        insertChar(original, ' ', expected);
    }

    public void testIssue170779_03() throws Exception {
        String original = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n        case^\n}";
        String expected = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n    case ^\n}";
        insertChar(original, ' ', expected);
    }

    public void testIssue170779_04() throws Exception {
        String original = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n        default^\n}";
        String expected = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n    default:^\n}";
        insertChar(original, ':', expected);
    }

    public void testIssue223165() throws Exception {
        String original = "switch ($a) {\n    case 1: break;\n}if^";
        String expected = "switch ($a) {\n    case 1: break;\n}if ^";
        insertChar(original, ' ', expected);
    }

    public void testIssue223395_01() throws Exception {
        String original = "# first^\n# second";
        String expected = "# first\n# ^\n# second";
        insertBreak(original, expected);
    }

    public void testIssue223395_02() throws Exception {
        String original = "    # first^\n    # second";
        String expected = "    # first\n    # ^\n    # second";
        insertBreak(original, expected);
    }

    public void testStringConcatination_01() throws Exception {
        String original = "$f=^\"lorem ipsum $foo dolor sit amet\";";
        String expected = "$f=\n        ^\"lorem ipsum $foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_02() throws Exception {
        String original = "$f=\"^lorem ipsum $foo dolor sit amet\";";
        String expected = "$f=\"\"\n        . \"^lorem ipsum $foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_03() throws Exception {
        String original = "$f=\"lorem ip^sum $foo dolor sit amet\";";
        String expected = "$f=\"lorem ip\"\n        . \"^sum $foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_04() throws Exception {
        String original = "$f=\"lorem ipsum ^$foo dolor sit amet\";";
        String expected = "$f=\"lorem ipsum \"\n        . \"^$foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_05() throws Exception {
        String original = "$f=\"lorem ipsum $foo^ dolor sit amet\";";
        String expected = "$f=\"lorem ipsum $foo\"\n        . \"^ dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_06() throws Exception {
        String original = "$f=\"lorem ipsum $foo dol^or sit amet\";";
        String expected = "$f=\"lorem ipsum $foo dol\"\n        . \"^or sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_07() throws Exception {
        String original = "$f=\"lorem ipsum $foo dolor sit amet^\";";
        String expected = "$f=\"lorem ipsum $foo dolor sit amet\"\n        . \"^\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_08() throws Exception {
        String original = "$f=\"lorem ipsum $foo dolor sit amet\"^;";
        String expected = "$f=\"lorem ipsum $foo dolor sit amet\"\n        ^;";
        insertBreak(original, expected);
    }

    public void testStringConcatination_09() throws Exception {
        String original = "$b=^'lorem iprsum dolor';";
        String expected = "$b=\n        ^'lorem iprsum dolor';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_10() throws Exception {
        String original = "$b='^lorem iprsum dolor';";
        String expected = "$b=''\n        . '^lorem iprsum dolor';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_11() throws Exception {
        String original = "$b='lorem ipr^sum dolor';";
        String expected = "$b='lorem ipr'\n        . '^sum dolor';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_12() throws Exception {
        String original = "$b='lorem iprsum dolor^';";
        String expected = "$b='lorem iprsum dolor'\n        . '^';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_13() throws Exception {
        String original = "$b='lorem iprsum dolor'^;";
        String expected = "$b='lorem iprsum dolor'\n        ^;";
        insertBreak(original, expected);
    }

    public void testStringConcatination_14() throws Exception {
        String original = "$c=^\"PHP version\";";
        String expected = "$c=\n        ^\"PHP version\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_15() throws Exception {
        String original = "$c=\"^PHP version\";";
        String expected = "$c=\"\"\n        . \"^PHP version\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_16() throws Exception {
        String original = "$c=\"PHP ver^sion\";";
        String expected = "$c=\"PHP ver\"\n        . \"^sion\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_17() throws Exception {
        String original = "$c=\"PHP version^\";";
        String expected = "$c=\"PHP version\"\n        . \"^\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_18() throws Exception {
        String original = "$c=\"PHP version\"^;";
        String expected = "$c=\"PHP version\"\n        ^;";
        insertBreak(original, expected);
    }

    public void testStringConcatination_19() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\",^ \"a\");";
        String expected = "$checks[] = new G_Check(\"PHP version\",\n        ^\"a\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_20() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", ^\"a\");";
        String expected = "$checks[] = new G_Check(\"PHP version\", \n        ^\"a\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_21() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", \"^a\");";
        String expected = "$checks[] = new G_Check(\"PHP version\", \"\"\n        . \"^a\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_22() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", \"a^\");";
        String expected = "$checks[] = new G_Check(\"PHP version\", \"a\"\n        . \"^\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_23() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", \"a\"^);";
        String expected = "$checks[] = new G_Check(\"PHP version\", \"a\"\n        ^);";
        insertBreak(original, expected);
    }

    public void testIssue228860_01() throws Exception {
        String original = "echo <<<EOT\n<div>^</div>\nEOT;\n";
        String expected = "echo <<<EOT\n<div>\n    ^</div>\nEOT;\n";
        insertBreak(original, expected);
    }

    public void testIssue228860_02() throws Exception {
        String original = "echo <<<EOT\n<div></div>\nEOT;\n\n$foo = \"ba^r\";";
        String expected = "echo <<<EOT\n<div></div>\nEOT;\n\n$foo = \"ba\"\n        . \"^r\";";
        insertBreak(original, expected);
    }

    public void testIssue227105() throws Exception {
        String original = "switch(true)\n" +
                "{\n" +
                "    case 1: if(true) break;\n" +
                "    default:^\n" +
                "}";
        String expected = "switch(true)\n" +
                "{\n" +
                "    case 1: if(true) break;\n" +
                "    default:\n" +
                "        ^\n" +
                "}";
        insertBreak(original, expected);
    }

    public void testIssue229960() throws Exception {
        String original = "<?php\n# What is this?^\n#";
        String expected = "<?php\n# What is this?\n# ^\n#";
        insertBreak(original, expected);
    }

    public void testIssue229710() throws Exception {
        String original = "<?php\nfunction functionName($param) {\n    try {^\n}";
        String expected = "<?php\nfunction functionName($param) {\n    try {\n        ^\n    } catch (Exception $ex) {\n\n    }\n}";
        insertBreak(original, expected);
    }

}
