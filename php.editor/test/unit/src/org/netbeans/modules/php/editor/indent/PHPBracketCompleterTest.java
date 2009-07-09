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

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

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
public class PHPBracketCompleterTest extends PHPTestBase {
    
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
        return "<?\n" + s + "\n?>";
    }
    
    @Override
    public void insertBreak(String original, String expected) throws Exception {
        super.insertBreak(wrapAsPhp(original), wrapAsPhp(expected));
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

    @Override
    protected void deleteChar(String original, String expected) throws Exception {
        super.deleteChar(wrapAsPhp(original), wrapAsPhp(expected));
    }

    @Override
    protected void deleteWord(String original, String expected) throws Exception {
        super.deleteWord(wrapAsPhp(original), wrapAsPhp(expected));
    }

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

    // XXX out of scope for NB 6.5
//    public void testInsertBreakInArray5() throws Exception {
//        insertBreak("array(array(array(^)))", "array(array(array(\n^)))");
//    }

    // this would be ideal
//    public void testInsertBreakInArray6() throws Exception {
//        insertBreak("array(array(array(^\n)))", "array(array(array(\n    ^\n)))");
//    }

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

// XXX: ruby specific
//    public void testDocs() throws Exception {
//        insertBreak("=begin^\n", "=begin\n^\n=end\n");
//    }
//
//    public void testDocsEnd() throws Exception {
//        insertBreak("=begin^", "=begin\n^\n=end");
//    }
//
//    public void testDocsEnd2() throws Exception {
//        insertBreak("def foo\nend\n=begin^", "def foo\nend\n=begin\n^\n=end");
//    }

// XXX: currently failing, but should be fixed
//    
//    public void testInsertEnd1() throws Exception {
//        insertBreak("x^", "x\n^");
//    }
//
//    public void testInsertEnd2() throws Exception {
//        insertBreak("class Foo {^", "class Foo {\n  ^\n}");
//    }
//    
//    public void testInsertEnd3() throws Exception {
//        insertBreak("class Foo {^\n}", "class Foo {\n  ^\n}");
//    }
//
//    public void testInsertEnd4() throws Exception {
//        insertBreak("for(;;) {^", "for(;;) {\n  ^\n}");
//    }
//
//    public void testInsertEnd5() throws Exception {
//        insertBreak("if ($something) {^", "if ($something) {\n  ^\n}");
//    }
//    
//    public void testInsertEnd6() throws Exception {
//        insertBreak("if ($something) {\n  \n} else {^", "if ($something) {\n  \n} else {\n  ^\n}");
//    }
//    
//    
//    public void testInsertIf1() throws Exception {
//        insertBreak("if ($something)^", "if ($something)\n  ^");
//    }
//
//    public void testInsertIf2() throws Exception {
//        insertBreak("if ($something)\n  echo 'Hi!';\nelse^", "if ($something)\n  echo 'Hi!';\nelse\n  ^");
//    }

    // This doesn't work
//    public void testInsertIf2() throws Exception {
//        insertBreak("    if true\n    else", 20, "    if true\n    else\n      end", 27);
//    }

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
// XXX: ruby specific
//    public void testRegexp1() throws Exception {
//        insertChar("x = ^", '/', "x = /^/");
//    }
//
//    public void testRegexp2() throws Exception {
//        insertChar("x = /^/", '/', "x = //^");
//    }
//
//    public void testRegexp3() throws Exception {
//        insertChar("x = /^/", 'a', "x = /a^/");
//    }
//    
//    public void testRegexp4() throws Exception {
//        insertChar("x = /\\^/", '/', "x = /\\/^/");
//    }
//
//    public void testRegexp5() throws Exception {
//        insertChar("    regexp = /fofo^\n      # Subsequently, you can make calls to it by name with <tt>yield</tt> in", '/',
//                "    regexp = /fofo/^\n      # Subsequently, you can make calls to it by name with <tt>yield</tt> in");
//    }
//
//    public void testRegexp6() throws Exception {
//        insertChar("    regexp = /fofo^\n", '/',
//                "    regexp = /fofo/^\n");
//    }
//
//    public void testRegexp7() throws Exception {
//        insertChar("x = ^\n", '/', "x = /^/\n");
//    }
//
//    public void testRegexp8() throws Exception {
//        insertChar("x = /^/\n", '/', "x = //^\n");
//    }
//
//    public void testRegexp9() throws Exception {
//        insertChar("x = /^/\n", 'a', "x = /a^/\n");
//    }
//    
//    public void testRegexp10() throws Exception {
//        insertChar("x = /\\^/\n", '/', "x = /\\/^/\n");
//    }
//    
//    public void testRegexp11() throws Exception {
//        insertChar("/foo^", '/',
//                "/foo/^");
//    }
//
//    public void testNotRegexp1() throws Exception {
//        insertChar("x = 10 ^", '/', "x = 10 /^");
//    }
//
//    public void testNotRegexp2() throws Exception {
//        insertChar("x = 3.14 ^", '/', "x = 3.14 /^");
//    }
//
//    // This test doesn't work; the lexer identifies x = y / as the
//    // beginning of a regular expression. Without the space it DOES
//    // work (see regexp4)
//    //public void testNotRegexp3() throws Exception {
//    //    insertChar("x = y ^", '/', "x = y /^");
//    //}
//
//    public void testNotRegexp4() throws Exception {
//        insertChar("x = y^", '/', "x = y/^");
//    }
//
//    public void testRegexpPercent1() throws Exception {
//        insertChar("x = %r^", '(', "x = %r(^)");
//    }
//
//    public void testRegexpPercent2() throws Exception {
//        insertChar("x = %r(^)", ')', "x = %r()^");
//    }
//    
//    
//    public void testSinglePercent1() throws Exception {
//        insertChar("x = %q^", '(', "x = %q(^)");
//    }
//
//    public void testSinglePercent2() throws Exception {
//        insertChar("x = %q(^)", ')', "x = %q()^");
//    }
//    
//    // Broken!!
//    // I've gotta handle proper parenthesis nesting here... e.g.
//    // %q(())
////    public void testSinglePercent3() throws Exception {
////        insertChar("x = %q(^)", '(', "x = %q((^))");
////    }
//
//    // Broken!!
////    public void testSinglePercent4() throws Exception {
////        insertChar("x = %q((^))", ')', "x = %q(()^)");
////    }
//
//    public void testSinglePercent5() throws Exception {
//        insertChar("x = %q((^))", 'a', "x = %q((a^))");
//    }
//    
//    public void testSinglePercent6() throws Exception {
//        insertChar("x = %q^", '-', "x = %q-^-");
//    }
//
//    public void testSinglePercent7() throws Exception {
//        insertChar("x = %q-^-", '-', "x = %q--^");
//    }
//    
//    public void testSinglePercent8() throws Exception {
//        insertChar("x = %q^", ' ', "x = %q ^ ");
//    }
//
//    // Broken!
////    public void testSinglePercent9() throws Exception {
////        insertChar("x = %q ^ ", ' ', "x = %q  ^");
////    }
//    
//    public void testSinglePercent10() throws Exception {
//        insertChar("x = %q ^ ", 'x', "x = %q x^ ");
//    }
//
//    public void testSinglePercent11() throws Exception {
//        insertChar("x = %q-\\^-", '-', "x = %q-\\-^-");
//    }

// XXX: currently failing, but should be fixed
//    public void testHeredoc1() throws Exception {
//        insertBreak("x=<<FOO^\n", "x=<<FOO\n^\nFOO\n");
//    }
//
//    public void testHeredoc2() throws Exception {
//        insertBreak("x=f(<<FOO,^\n", "x=f(<<FOO,\n^\nFOO\n");
//    }
    
// XXX: ruby specific, we don't support this kind of markings; perhaps should be part of mark occurences
//    public void testFindMatching1() throws Exception {
//        match("^if true\n^end");
//    }
//    
//    public void testFindMatching4() throws Exception {
//        match("^def foo\nif true\nend\n^end\nend");
//    }
//
//    public void testFindMatching5() throws Exception {
//        // Test heredocs
//        match("x=f(^<<ABC,\"hello\")\nfoo\nbar\n^ABC\n");
//    }
//
//    public void testFindMatching6() throws Exception {
//        // Test heredocs
//        match("x=f(^<<ABC,'hello',<<-DEF,'bye')\nfoo\nbar\n^ABC\nbaz\n  DEF\nwhatever");
//    }
//
//    public void testFindMatching7() throws Exception {
//        // Test heredocs
//        match("x=f(<<ABC,'hello',^<<-DEF,'bye')\nfoo\nbar\nABC\nbaz\n  ^DEF\nwhatever");
//    }
    
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
    

// XXX: ruby specific; BUT maybe there is an equivalent in PHP, see {$ } syntax in quoted strings
//    public void testPercentBackspace() throws Exception {
//        deleteChar("x=\"#{^}\"", "x=\"#^\"");
//    }
//
//    public void testPercentBackspace2() throws Exception {
//        deleteChar("x=\"#{a^}\"", "x=\"#{^}\"");
//    }
//
//    public void testPercentBackspace3() throws Exception {
//        deleteChar("x=\"a#{^}b\"", "x=\"a#^b\"");
//    }
//    
//    public void testPercentBackspace4() throws Exception {
//        deleteChar("x=/#{^}/", "x=/#^/");
//    }
//
//    public void testPercentBackspace5() throws Exception {
//        deleteChar("x=/#{a^}/", "x=/#{^}/");
//    }
//
//    public void testPercentBackspace6() throws Exception {
//        deleteChar("x=/a#{^}b/", "x=/a#^b/");
//    }
    
    public void testContComment() throws Exception {
        if (PHPBracketCompleter.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }
    
// XXX: currently failing, but should be fixed
//    public void testContComment4() throws Exception {
//        insertBreak("// foo\n^", "// foo\n\n^");
//    }
//
//    public void testContComment5() throws Exception {
//        // No auto-// on new lines
//        if (PHPBracketCompleter.CONTINUE_COMMENTS) {
//            insertBreak("      // ^", "      // \n      // ^");
//        } else {
//            insertBreak("      // ^", "      // \n      ^");
//        }
//    }
//    
//    public void testContComment6() throws Exception {
//        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
//    }
//    
//    public void testContComment7() throws Exception {
//        insertBreak("   // foo^\n   // bar", "   // foo\n   // ^\n   // bar");
//    }
//    
//    public void testContComment8() throws Exception {
//        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
//    }
//
//    
//    public void testContComment9() throws Exception {
//        insertBreak("^// foobar", "\n^// foobar");
//    }
//
//    public void testContComment10() throws Exception {
//        insertBreak("//foo\n^// foobar", "//foo\n// ^\n// foobar");
//    }
//
//    public void testContComment11() throws Exception {
//        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
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
//    public void testContComment15() throws Exception {
//        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
//    }
//
//    public void testContComment16() throws Exception {
//        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
//    }
//
//    public void testContComment17() throws Exception {
//        insertBreak("def foo\n  // cmnt1\n^  // cmnt2\nend\n", "def foo\n  // cmnt1\n  // ^\n  // cmnt2\nend\n");
//    }
//    
//    public void testNoContComment() throws Exception {
//        // No auto-// on new lines
//        insertBreak("foo // ^", "foo // \n^");
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

//    public void testFreakOutEditor1() throws Exception {
//        String before = "x = method_call(50, <<TOKEN1, \"arg3\", <<TOKEN2, /startofregexp/^\nThis is part of the string\nTOKEN1\nrestofregexp/)";
//        String  after = "x = method_call(50, <<TOKEN1, \"arg3\", <<TOKEN2, /startofregexp^\nThis is part of the string\nTOKEN1\nrestofregexp/)";
//        deleteChar(before, after);
//    }
//    

// XXX: ruby specific
//    public void testInsertPercentInString() throws Exception {
//        insertChar("x = \"foo ^\"", '#', "x = \"foo #{^}\"");
//    }
//
//    public void testInsertPercentInString2() throws Exception {
//        // Make sure type-through works
//        insertChar("x = \"foo #{^}\"", '}', "x = \"foo #{}^\"");
//    }
//
//    public void testInsertPercentInString3() throws Exception {
//        insertChar("x = \"foo #{^}\"", '{', "x = \"foo #{^}\"");
//    }
//
//    public void testInsertPercentInString4() throws Exception {
//        insertChar("x = \"foo #{^a}\"", '}', "x = \"foo #{}^a}\"");
//    }
//
//    public void testInsertPercentInString5() throws Exception {
//        insertChar("x = \"foo {^}\"", '}', "x = \"foo {}^}\"");
//    }
//
//    public void testInsertPercentInString6() throws Exception {
//        insertChar("x = \"foo {^}\"", '{', "x = \"foo {{^}\"");
//    }
//
//    public void testNoInsertPercentInString() throws Exception {
//        insertChar("x = 'foo ^'", '#', "x = 'foo #^'");
//    }
//
//    public void testNoInsertPercentElsewhere() throws Exception {
//        insertChar("x = ^", '#', "x = #^");
//    }
//    
//    public void testInsertPercentInRegexp() throws Exception {
//        insertChar("x = /foo ^/", '#', "x = /foo #{^}/");
//    }
//
//    public void testInsertPercentInRegexp2() throws Exception {
//        // Make sure type-through works
//        insertChar("x = /foo #{^}/", '}', "x = /foo #{}^/");
//    }
//
//    public void testInsertPercentInRegexp3() throws Exception {
//        insertChar("x = /foo #{^}/", '{', "x = /foo #{^}/");
//    }
//
//    public void testInsertPercentInRegexp4() throws Exception {
//        insertChar("x = /foo #{^a}/", '}', "x = /foo #{}^a}/");
//    }
//
//    public void testInsertPercentInRegexp5() throws Exception {
//        insertChar("x = /foo {^}/", '}', "x = /foo {}^}/");
//    }
//
//    public void testInsertPercentInRegexp6() throws Exception {
//        insertChar("x = /foo {^}/", '{', "x = /foo {{^}/");
//    }

    public void testReplaceSelection1() throws Exception {
        insertChar("x = foo^", 'y', "x = y^", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("x = 'foo^bar'", '#', "x = '#^bar'", "foo");
    }
// XXX: currently failing, but should be fixed
//    public void testReplaceSelection2() throws Exception {
//        insertChar("x = foo^", '"', "x = \"foo\"^", "foo");
//    }
//    
//    public void testReplaceSelection5() throws Exception {
//        insertChar("'(^position:absolute;'", '{', "'{^position:absolute;'", "(");
//    }
//
//    public void testReplaceSelection6() throws Exception {
//        insertChar("'position^:absolute;'", '{', "'pos{^:absolute;'", "ition");
//    }

// XXX: ruby specific
//    public void testReplaceSelection3() throws Exception {
//        insertChar("x = \"foo^bar\"", '#', "x = \"#{foo}^bar\"", "foo");
//    }
    
    public void testReplaceSelectionChangeType1() throws Exception {
        insertChar("x = \"foo\"^", '\'', "x = 'foo'^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType2() throws Exception {
        insertChar("x = \"foo\"^", '{', "x = {foo}^", "\"foo\"");
    }

    public void testReplaceSelectionNotInTemplateMode1() throws Exception {
        insertChar("x = foo^", '"', "x = \"^\"", "foo", true);
    }
    
    // Functionality works but test is broken
    //public void testReplaceSelectionNotInTemplateMode2() throws Exception {
    //    insertChar("x = \"foo^bar\"", '#', "x = \"#{^}bar\"", "foo", true);
    //}
    
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
// XXX: failing, but should be fixed; some usecase could be ruby specific
//    public void testDeleteWord111303() throws Exception {
//        deleteWord("foo::bar^", "foo::^");
//        deleteWord("Foo::Bar^", "Foo::^");
//        deleteWord("Foo::Bar_Baz^", "Foo::Bar_^");
//    }
//    public void testDeleteWordx111305() throws Exception {
//        deleteWord("foo_bar^", "foo_^");
//        deleteWord("x.foo_bar^.y", "x.foo_^.y");
//    }
//
//    public void testDeleteWord2() throws Exception {
//        deleteWord("foo_bar_baz ^", "foo_bar_baz^");
//        deleteWord("foo_bar_^", "foo_^");
//    }
//
//    public void testDeleteWord3() throws Exception {
//        deleteWord("FooBarBaz^", "FooBar^");
//    }
//    
//    public void testDeleteWord4_110998() throws Exception {
//        deleteWord("Blah::Set^Foo", "Blah::^Foo");
//    }
//
//    public void testdeleteWord5() throws Exception {
//        deleteWord("foo_bar_^", "foo_^");
//    }
//
//    public void testdeleteWords() throws Exception {
//        deleteWord("foo bar^", "foo ^");
//    }
//
//
//    public void testDeleteWord4_110998c() throws Exception {
//        String before = "  snark^\n";
//        String after = "  ^\n";
//        deleteWord(before, after);
//    }
//    
//    public void testDeleteWord4_110998b() throws Exception {
//        String before = "" +
//"  snark(%w(a b c))\n" +
//"  snark(%W(a b c))\n" +
//"  snark^\n" +
//"  snark(%Q(a b c))\n" +
//"  snark(%w(a b c))\n";
//        String after = "" +
//"  snark(%w(a b c))\n" +
//"  snark(%W(a b c))\n" +
//"  ^\n" +
//"  snark(%Q(a b c))\n" +
//"  snark(%w(a b c))\n";
//        deleteWord(before, after);
//    }
    
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
    
//    public void testLogicalRange1() throws Exception {
//        String code = "if (true)\n  fo^o\nend";
//        String next = "if (true)\n  %<%fo^o%>%\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRange2() throws Exception {
//        String code = "if (true)\n  %<%fo^o%>%\nend";
//        String next = "%<%if (true)\n  fo^o\nend%>%";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRange3() throws Exception {
//        String code = "def foo\nif (true)\n  %<%fo^o%>%\nend\nend";
//        String next = "def foo\n%<%if (true)\n  fo^o\nend%>%\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRange4() throws Exception {
//        String code = "class Foo\ndef foo\nif (true)\n  %<%fo^o%>%\nend\nend\nend";
//        String next = "class Foo\ndef foo\n%<%if (true)\n  fo^o\nend%>%\nend\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRange5() throws Exception {
//        String code = "class Foo\ndef foo\n%<%if (true)\n  fo^o\nend%>%\nend\nend";
//        String next = "class Foo\n%<%def foo\nif (true)\n  fo^o\nend\nend%>%\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRange6() throws Exception {
//        String code = "class Foo\n%<%def fo^o\nif (true)\n  foo\nend\nend%>%\nend";
//        String next = "%<%class Foo\ndef fo^o\nif (true)\n  foo\nend\nend\nend%>%";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeComment1() throws Exception {
//        String code = "foo\n  # Foo Bar\n  # Foo^y Baary\n  # Bye\ndef foo\nend\n";
//        String next = "foo\n  # Foo Bar\n  %<%# Foo^y Baary%>%\n  # Bye\ndef foo\nend\n";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeComment2() throws Exception {
//        String code = "foo\n  # Foo Bar\n  %<%# Foo^y Baary%>%\n  # Bye\ndef foo\nend\n";
//        String next = "foo\n  %<%# Foo Bar\n  # Foo^y Baary\n  # Bye%>%\ndef foo\nend\n";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeComment3() throws Exception {
//        String code = "foo\n  # Foo Bar\n\n  %<%# Foo^y Baary%>%\n  # Bye\ndef foo\nend\n";
//        String next = "foo\n  # Foo Bar\n\n  %<%# Foo^y Baary\n  # Bye%>%\ndef foo\nend\n";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//    public void testLogicalRangeComment5() throws Exception {
//        String code = "foo\n  foo # Foo Bar\n  %<%# Foo^y Baary%>%\n  # Bye\ndef foo\nend\n";
//        String next = "foo\n  foo # Foo Bar\n  %<%# Foo^y Baary\n  # Bye%>%\ndef foo\nend\n";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//    
//    public void testLogicalRangeStrings1() throws Exception {
//        String code = "x = 'foo b^ar baz', y = \"whatever\"";
//        String next = "x = %<%'foo b^ar baz'%>%, y = \"whatever\"";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeStrings2() throws Exception {
//        String code = "x = %q-foo b^ar baz-, y = \"whatever\"";
//        String next = "x = %<%%q-foo b^ar baz-%>%, y = \"whatever\"";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeStrings3() throws Exception {
//        String code = "def foo\nif (true)\nx = %<%'foo b^ar baz'%>%\nend\nend";
//        String next = "def foo\nif (true)\n%<%x = 'foo b^ar baz'%>%\nend\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeStrings4() throws Exception {
//        String code = "def foo\nif (true)\n%<%x = 'foo b^ar baz'%>%\nend\nend";
//        String next = "def foo\n%<%if (true)\nx = 'foo b^ar baz'\nend%>%\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeStrings5() throws Exception {
//        String code = "def test\n 'return^ me'\nend";
//        String next = "def test\n %<%'return^ me'%>%\nend";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeStrings6() throws Exception {
//        String code = "def test\n %<%'return^ me'%>%\nend";
//        String next = "%<%def test\n 'return^ me'\nend%>%";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }
//
//    public void testLogicalRangeRegexps1() throws Exception {
//        String code = "x = /foo b^ar baz/, y = \"whatever\"";
//        // Uhm - is it good that we're selecting the -inside- of the regexp?
//        String next = "x = /%<%foo b^ar baz%>%/, y = \"whatever\"";
//        assertLogicalRange(code, true, next);
//        assertLogicalRange(next, false, code);
//    }

// XXX: ruby specific
//    public void testPipes1() throws Exception {
//        insertChar("5.each { ^", '|', "5.each { |^|");
//    }
//
//    public void testPipes2() throws Exception {
//        insertChar("5.each { ^}", '|', "5.each { |^|}");
//    }
//    
//    public void testPipes3() throws Exception {
//        insertChar("5.each { |^|}", '|', "5.each { ||^}");
//    }
//    
//    public void testPipes4() throws Exception {
//        insertChar("5.each { |foo^|}", '|', "5.each { |foo|^}");
//    }
//
//    public void testNegativePipes1() throws Exception {
//        insertChar("'^'", '|', "'|^'");
//    }
//
//    public void testNegativePipes2() throws Exception {
//        insertChar("/^/", '|', "/|^/");
//    }
//
//    public void testNegativePipes3() throws Exception {
//        insertChar("#^", '|', "#|^");
//    }
//
//    public void testNegativePipes4() throws Exception {
//        insertChar("\"^\"", '|', "\"|^\"");
//    }
//
//    public void testNegativePipes5() throws Exception {
//        insertChar("5.each { |f^oo|}", '|', "5.each { |f|^oo|}");
//    }
//
//    public void testNegativePipes6() throws Exception {
//        insertChar("5.each { |^|foo|}", '|', "5.each { ||^foo|}");
//    }
//
//    public void testNegativePipes7() throws Exception {
//        insertChar("x = true ^", '|', "x = true |^");
//    }
//
//    public void testNegativePipes8() throws Exception {
//        insertChar("x = true |^", '|', "x = true ||^");
//    }
//
//    public void testBackspacePipes() throws Exception {
//        deleteChar("x=|^|", "x=^");
//    }
//
//    public void testBackspacePipes2() throws Exception {
//        deleteChar("x=|^x", "x=^x");
//    }
//    
//    public void testBackspacePipes3() throws Exception {
//        deleteChar("x=|^", "x=^");
//    }
}
