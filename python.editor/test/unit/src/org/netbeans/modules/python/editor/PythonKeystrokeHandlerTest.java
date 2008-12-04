/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.gsf.api.Formatter;

/**
 *
 * @author Tor Norbye
 */
public class PythonKeystrokeHandlerTest extends PythonTestBase {
    static {
        System.setProperty("python.fromimport", "true");
    }

    public PythonKeystrokeHandlerTest(String testName) {
        super(testName);
    }

    private void match(String original) throws BadLocationException {
        super.assertMatches(original);
    }
    
    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }
    
    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    @Override
    public void deleteWord(String original, String expected) throws Exception {
        // Try deleting the word not just using the testcase but also surrounded by strings
        // to make sure there's no problem with lexer token directions
        super.deleteWord(original, expected);
        super.deleteWord(original+"foo", expected+"foo");
        super.deleteWord("foo"+original, "foo"+expected);
        super.deleteWord(original+"::", expected+"::");
        super.deleteWord(original+"::", expected+"::");
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        //return new PythonFormatter();
        return null;
    }

    public void testReturn() throws Exception {
        insertBreak("    def foo():\n        return^", "    def foo():\n        return\n    ^");
    }

    public void testReturn2() throws Exception {
        insertBreak("    def foo():\n        return^\n", "    def foo():\n        return\n    ^\n");
    }

    public void testReturn3() throws Exception {
        insertBreak("    def foo():\n        return true^\n", "    def foo():\n        return true\n    ^\n");
    }

    public void testRaise1() throws Exception {
        insertBreak("    def foo():\n        raise RuntimeError^\n", "    def foo():\n        raise RuntimeError\n    ^\n");
    }

    public void testDefParenSkip1() throws Exception {
        insertBreak("def foo(^):\n", "def foo():\n    ^\n");
    }

    public void testDefParenSkip2() throws Exception {
        insertBreak("class Foo(^):\n", "class Foo():\n    ^\n");
    }

    public void testDefParenSkip3() throws Exception {
        insertBreak("def foo(self^):\n", "def foo(self):\n    ^\n");
    }

    public void testDefParenSkip4() throws Exception {
        insertBreak("def foo(self,^):\n", "def foo(self,\n^):\n");
    }

    public void testDefParenSkip5() throws Exception {
        insertBreak("def foo(self^):", "def foo(self):\n    ^");
    }

    public void testContinued() throws Exception {
        insertBreak("    def foo():\n        raise RuntimeError \\^\n", "    def foo():\n        raise RuntimeError \\\n            ^\n");
    }

    public void testContinued2() throws Exception {
        insertBreak("foo\\^", "foo\\\n    ^");
    }

    public void testContinued3() throws Exception {
        insertBreak("foo\\^\n", "foo\\\n    ^\n");
    }

    public void testNotContinued1() throws Exception {
        insertBreak("fo^o\\\n", "fo\n^o\\\n");
    }

    public void testPassDedent() throws Exception {
        insertBreak("    if (true):\n        pass^\n", "    if (true):\n        pass\n    ^\n");
    }

    public void testPassDedent2() throws Exception {
        insertBreak("    if (true):\n        pass^", "    if (true):\n        pass\n    ^");
    }

    public void testReindentElse() throws Exception {
        insertChar("    if (true):\n        pass\n        else^", ':', "    if (true):\n        pass\n    else:^");
    }

    public void testReindentElse2() throws Exception {
        insertChar("    if (true):\n        pass\n        else^\n", ':', "    if (true):\n        pass\n    else:^\n");
    }

    public void testReindentElif() throws Exception {
        insertChar("    if (true):\n        pass\n        elif^", ':', "    if (true):\n        pass\n    elif:^");
    }

    public void testReindentElif2() throws Exception {
        insertChar("    if (true):\n        pass\n        elif^\n", ':', "    if (true):\n        pass\n    elif:^\n");
    }

    public void testReindentExcept() throws Exception {
        insertChar("    try:\n        pass\n        except Foo^", ':', "    try:\n        pass\n    except Foo:^");
    }

    public void testReindentExcept2() throws Exception {
        insertChar("    try:\n        pass\n        except Foo^\n", ':', "    try:\n        pass\n    except Foo:^\n");
    }

    public void testReindentExcept3() throws Exception {
        insertChar("    try:\n        pass\n        finally^\n", ':', "    try:\n        pass\n    finally:^\n");
    }

    public void testMultilineString() throws Exception {
        insertBreak("\"\"\"^\n", "\"\"\"\n^\n\"\"\"\n");
    }

    public void testMultilineString2() throws Exception {
        insertBreak("    \"\"\"^\n", "    \"\"\"\n    ^\n    \"\"\"\n");
    }

    public void testMultilineString3() throws Exception {
        insertBreak("\"\"\"^", "\"\"\"\n^\n\"\"\"");
    }

    public void testNoMultilineString() throws Exception {
        insertBreak("# \"\"\"^\n", "# \"\"\"\n^\n");
    }

    public void testNoMultilineString2() throws Exception {
        insertBreak("\"\"\"^\n\n\"\"\"\n", "\"\"\"\n^\n\n\"\"\"\n");
    }

// Not yet implemented
//    public void testSinglineString1() throws Exception {
//        // Not yet passing - should implement this
//        insertBreak("\"\"\"foo^\n\"\"\"", "\"\"\"foo\n\"\"\"\n^");
//    }
//
//    public void testSinglineString2() throws Exception {
//        insertBreak("\"\"\"foo^\n\"\"\"\n", "\"\"\"foo\n\"\"\"\n^\n");
//    }
//
//    public void testSinglineString3() throws Exception {
//        insertBreak("    \"\"\"foo^\n\"\"\"\n", "    \"\"\"foo\n\"\"\"\n    ^\n");
//    }

    public void testInsertColon1() throws Exception {
        insertChar("def foo^", '(', "def foo(^):");
    }

    public void testInsertColon2() throws Exception {
        insertChar("def foo^\n", '(', "def foo(^):\n");
    }

    public void testInsertColon3() throws Exception {
        insertChar("class A\n    def foo^", '(', "class A\n    def foo(self^):");
    }

    public void testInsertColon4() throws Exception {
        insertChar("class Abc^\n", '(', "class Abc(^):\n");
    }

    public void testInsertColon5() throws Exception {
        insertChar("class A\n    def foo()\n        bar\n\n  \n    def bar^\n", '(', "class A\n    def foo()\n        bar\n\n  \n    def bar(self^):\n");
    }

    public void testTypethroughComma1() throws Exception {
        insertChar("class Abc(^):\n", 'a', "class Abc(a^):\n");
    }

    public void testTypethroughComma2() throws Exception {
        insertChar("class Abc(a^):\n", ')', "class Abc(a)^:\n");
    }

    public void testTypethroughComma4() throws Exception {
        insertChar("class Abc(a)^:\n", ':', "class Abc(a):^\n");
    }

    public void testTypethroughComma5() throws Exception {
        insertChar("def foo(^):\n", ')', "def foo()^:\n");
    }

    public void testTypethroughComma6() throws Exception {
        insertChar("def foo()^:\n", ':', "def foo():^\n");
    }

    public void testNoTypethroughComma1() throws Exception {
        insertChar("ashex = _binascii.hexlify(data[:^-1])", ':', "ashex = _binascii.hexlify(data[::^-1])");
    }

    public void testNoTypethroughComma2() throws Exception {
        insertChar("ashex = _binascii.hexlify(data[^:-1])", ':', "ashex = _binascii.hexlify(data[:^:-1])");
    }

    public void testNoTypethroughComma3() throws Exception {
        insertChar("# def foo()^:\n", ':', "# def foo():^:\n");
    }

    public void testFromImport() throws Exception {
        insertChar("from sys^", ' ', "from sys import ^");
    }

    public void testFromImport2() throws Exception {
        insertChar("from sys^\n", ' ', "from sys import ^\n");
    }

    public void testNoFromImport() throws Exception {
        insertChar("from sys^\n", 's', "from syss^\n");
    }

    public void testNoFromImport2() throws Exception {
        insertChar("from sys^ import *\n", ' ', "from sys ^ import *\n");
    }

    public void testBreak1() throws Exception {
        insertBreak("test1:^", "test1:\n    ^");
    }

    public void testBreak2() throws Exception {
        insertBreak("test1:^\n", "test1:\n    ^\n");
    }

    public void testBreak3() throws Exception {
        insertBreak("test1:\n  foo^", "test1:\n  foo\n  ^");
    }

    public void testTripleQuotes1() throws Exception {
        insertChar("\"\"^",   '"', "\"\"\"^");
        insertChar("\"\"^\n", '"', "\"\"\"^\n");
    }

    public void testTripleQuotes2() throws Exception {
        insertChar("class A:\n    def foo():\n        \"\"^\n",
                '"',
                "class A:\n    def foo():\n        \"\"\"^\n");
    }

    public void testTripleQuotes3() throws Exception {
        insertChar("''^", '\'', "'''^");
        insertChar("''^\n", '\'', "'''^\n");
    }


    public void testBreak4() throws Exception {
        insertBreak("test1:\n  foo^\n", "test1:\n  foo\n  ^\n");
    }

    public void testBreak5() throws Exception {
        insertBreak("  test1: ^   foo", "  test1: \n  ^foo");
    }

    public void testBreak6() throws Exception {
        insertBreak("test1:  ^",   "test1:  \n    ^");
        insertBreak("test1:  ^\n", "test1:  \n    ^\n");
    }

    public void testBreak7() throws Exception {
        insertBreak("    test1:  ^",   "    test1:  \n        ^");
        insertBreak("    test1:  ^\n", "    test1:  \n        ^\n");
    }

    public void testBak1() throws Exception {
        // Ensure that a backspace with indent 4 will delete 3 more characters to
        // outdent one level
        deleteChar("        ^", "    ^");
    }

    public void testBak2() throws Exception {
        deleteChar("        ^test", "    ^test");
    }

    public void testBak3() throws Exception {
        // Make sure normal deletion works
        deleteChar("foo^", "fo^");
    }

    public void testLogicalRange1() throws Exception {
        String code = "print \"He^llo World!\"\n";
        String next = "print %<%\"He^llo World!\"%>%\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange2() throws Exception {
        String code = "print  %<%\"He^llo World!\"%>%\n";
        String next = "%<%print  \"He^llo World!\"%>%\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange3() throws Exception {
        String code = "class A:\n    def foo():\n        self.filename = file^name\n";
        String next = "class A:\n    def foo():\n        self.filename = %<%file^name%>%\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange4() throws Exception {
        String code = "class A:\n    def foo():\n        self.filename = %<%file^name%>%\n";
        String next = "class A:\n    def foo():\n        %<%self.filename = file^name%>%\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange5() throws Exception {
        String code = "class A:\n    def foo():\n        %<%self.filename = file^name%>%\n";
        String next = "class A:\n    %<%def foo():\n        self.filename = file^name%>%\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange6() throws Exception {
        String code = "class A:\n    %<%def foo():\n        self.filename = file^name%>%\n";
        String next = "%<%class A:\n    def foo():\n        self.filename = file^name%>%\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInComments() throws Exception {
        // End
        insertChar("# Hello^", '\'', "# Hello'^");
        insertChar("# Hello^", '"', "# Hello\"^");
        insertChar("# Hello^", '[', "# Hello[^");
        insertChar("# Hello^", '(', "# Hello(^");

        // Middle
        insertChar("# Hello^o", '\'', "# Hello'^o");
        insertChar("# Hello^o", '"', "# Hello\"^o");
        insertChar("# Hello^o", '[', "# Hello[^o");
        insertChar("# Hello^o", '(', "# Hello(^o");

        // Before newline
        insertChar("# Hello^\n", '\'', "# Hello'^\n");
        insertChar("# Hello^\n", '"', "# Hello\"^\n");
        insertChar("# Hello^\n", '[', "# Hello[^\n");
        insertChar("# Hello^\n", '(', "# Hello(^\n");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("x = \"^\"", '\'', "x = \"'^\"");
        insertChar("x = \"^\"", '[', "x = \"[^\"");
        insertChar("x = \"^\"", '(', "x = \"(^\"");
        insertChar("x = \"^)\"", ')', "x = \")^)\"");
        insertChar("x = '^'", '"', "x = '\"^'");
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

    public void testInsertBrokenQuote() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^)", '"',
                "System.out.prinlnt(\"pavel\"^)");
    }

    public void testInsertBrokenQuote2() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^\n", '"',
                "System.out.prinlnt(\"pavel\"^\n");
    }

    public void testInsertBrokenQuote3() throws Exception {
        insertChar("System.out.prinlnt(\"^\n", '"',
                "System.out.prinlnt(\"\"^\n");
    }

    public void testInsertBrokenQuote4() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^", '"',
                "System.out.prinlnt(\"pavel\"^");
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

//    public void testInsertBrace1() throws Exception {
//        insertBreak("foobar({^});", "foobar({\n    ^\n});");
//    }
//
//    public void testInsertBrace2() throws Exception {
//        insertBreak("foobar([^]);", "foobar([\n    ^\n]);");
//    }
//
//    public void testInsertBrace3() throws Exception {
//        insertBreak("x = {^}", "x = {\n    ^\n}");
//    }

    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }

//    public void testSplitStrings1() throws Exception {
//        insertBreak("  x = 'te^st'", "  x = 'te\\n\\\n^st'");
//    }
//
//    public void testSplitStrings1b() throws Exception {
//        insertBreak("  x = '^test'", "  x = '\\\n^test'");
//    }
//
//    public void testSplitStrings2() throws Exception {
//        insertBreak("  x = 'test^'", "  x = 'test\\n\\\n^'");
//    }
//
//    public void testSplitStrings3() throws Exception {
//        insertBreak("  x = \"te^st\"", "  x = \"te\\n\\\n^st\"");
//    }
//

    public void testBrackets1() throws Exception {
        insertChar("x = ^", '[', "x = [^]");
    }

    public void testBrackets2() throws Exception {
        insertChar("x = [^]", ']', "x = []^");
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
//
//
//    public void testFindMatching1() throws Exception {
//        match("if (true) ^{\n^}");
//    }
//
//    public void testFindMatching2() throws Exception {
//        match("x=^(true^)\ny=5");
//    }
//
//    public void testFindMatching3() throws Exception {
//        match("x=^(true || (false)^)\ny=5");
//    }
//
//    public void testFindMatching4() throws Exception {
//        match("function foo() ^{\nif (true) {\n}\n^}\n}");
//    }
//
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

    public void testBackspace9() throws Exception {
        deleteChar("def foo(^):\n", "def foo^\n");
    }

    public void testBackspaceComment() throws Exception {
        deleteChar("# x=[^]", "# x=^]");
    }

    public void testReplaceSelection1() throws Exception {
        insertChar("x = foo^", 'y', "x = y^", "foo");
    }

    public void testReplaceSelection2() throws Exception {
        insertChar("x = foo^", '"', "x = \"foo\"^", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("x = 'foo^bar'", '#', "x = '#^bar'", "foo");
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

    public void testdeleteWord() throws Exception {
        deleteWord("foo_bar_baz^", "foo_bar_^");
    }

    public void testdeleteWord111303() throws Exception {
        deleteWord("foo::bar^", "foo::^");
        deleteWord("Foo::Bar^", "Foo::^");
        deleteWord("Foo::Bar_Baz^", "Foo::Bar_^");
    }
    public void testdeleteWordx111305() throws Exception {
        deleteWord("foo_bar^", "foo_^");
        deleteWord("x.foo_bar^.y", "x.foo_^.y");
    }

    public void testdeleteWord2() throws Exception {
        deleteWord("foo_bar_baz ^", "foo_bar_baz^");
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testdeleteWord3() throws Exception {
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
}
