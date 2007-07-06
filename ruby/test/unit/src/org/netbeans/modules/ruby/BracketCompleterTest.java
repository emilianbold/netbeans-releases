/*
 * BracketCompleterTest.java
 * JUnit based test
 *
 * Created on July 2, 2007, 11:22 AM
 */

package org.netbeans.modules.ruby;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;

/**
 *
 * @author Tor Norbye
 */
public class BracketCompleterTest extends RubyTestBase {
    
    public BracketCompleterTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void match(String original) throws BadLocationException {
        BracketCompleter bc = new BracketCompleter();
        
        int caretPos = original.indexOf('^');
        
        original = original.substring(0, caretPos) + original.substring(caretPos+1);
        int matchingCaretPos = original.indexOf('^');
        assert caretPos < matchingCaretPos;
        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        OffsetRange range = bc.findMatching(doc, caretPos);
        
        assertNotSame("Didn't find matching token for " + LexUtilities.getToken(doc, caretPos).text().toString(), 
                OffsetRange.NONE, range);
        assertEquals("forward match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " +
                LexUtilities.getToken(doc, matchingCaretPos).text().toString(), 
                matchingCaretPos, range.getStart());
        
        // Perform reverse match
        range = bc.findMatching(doc, matchingCaretPos);
        
        assertNotSame(OffsetRange.NONE, range);
        assertEquals("reverse match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " + 
                LexUtilities.getToken(doc, caretPos).text().toString(), 
                caretPos, range.getStart());
    }
    
    private void insertBreak(String original, String expected) throws BadLocationException {
        BracketCompleter bc = new BracketCompleter();
        
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);
        int newOffset = bc.beforeBreak(doc, insertOffset, caret);
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.insertString(caret.getDot(), "\n", null);
            // Indent the new line
            Formatter formatter = new Formatter();
            FormattingPreferences preferences = new IndentPrefs(2,2);
            //ParserResult result = parse(fo);
            int indent = formatter.getLineIndent(doc, insertOffset+1, preferences);
            doc.getFormatter().changeRowIndent(doc, Utilities.getRowStart(doc, insertOffset+1), indent);

            //bc.afterBreak(doc, insertOffset, caret);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (newOffset != -1) {
                caret.setDot(newOffset);
            } else {
                caret.setDot(insertOffset+1+indent);
            }
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }

    private void insertChar(String original, char insertText, String expected) throws BadLocationException {
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BracketCompleter bc = new BracketCompleter();

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);

        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
        
            boolean handled = bc.beforeCharInserted(doc, insertOffset, caret, insertText);
            if (!handled) {
                doc.insertString(caret.getDot(), ""+insertText, null);
                caret.setDot(insertOffset+1);
                bc.afterCharInserted(doc, insertOffset, caret, insertText);
            }
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
        
    }

    private void deleteChar(String original, String expected) throws BadLocationException {
        int afterRemoveOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, afterRemoveOffset) + original.substring(afterRemoveOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BracketCompleter bc = new BracketCompleter();

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(afterRemoveOffset);
        int dot = afterRemoveOffset;
        char ch = doc.getChars(dot-1, 1)[0];

        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.remove(dot - 1, 1);
            caret.setDot(dot-1);
            boolean handled = bc.charBackspaced(doc, dot-1, caret, ch);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }
    
    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInComments() throws Exception {
        insertChar("# Hello^", '\'', "# Hello'^");
        insertChar("# Hello^", '"', "# Hello\"^");
        insertChar("# Hello^", '[', "# Hello[^");
        insertChar("# Hello^", '(', "# Hello(^");
    }

    // BROKEN because of the lexer bug (108889) - disabled for now
//    public void testNoMatchInStrings() throws Exception {
//        insertChar("x = \"^\"", '\'', "x = \"'^\"");
//        insertChar("x = \"^\"", '[', "x = \"[^\"");
//        insertChar("x = \"^\"", '(', "x = \"(^\"");
//        insertChar("x = \"^)\"", ')', "x = \")^)\"");
//        insertChar("x = '^'", '"', "x = '\"^'");
//        insertChar("x = \"\nf^\n\"", '\'', "x = \"\nf'^\n\"");
//        insertChar("x = \"\nf^\n\"", '[', "x = \"\nf[^\n\"");
//        insertChar("x = \"\nf^\n\"", '(', "x = \"\nf(^\n\"");
//        insertChar("x = '\nf^\n'", '"', "x = '\nf\"^\n'");
//    }
    
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

    public void testDocs() throws Exception {
        insertBreak("=begin^\n", "=begin\n^\n=end\n");
    }

//    public void testDocsEnd() throws Exception {
//        // For some reason it's broken at the end of the document; figure out why
//        insertBreak("=begin^", "=begin\n^\n=end");
//    }

    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertEnd2() throws Exception {
        insertBreak("class Foo^", "class Foo\n  ^\nend");
    }
    
    public void testInsertEnd3() throws Exception {
        insertBreak("class Foo^\nend", "class Foo\n  ^\nend");
    }

    public void testInsertIf1() throws Exception {
        insertBreak("    if true^", "    if true\n      ^\n    end");
    }

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

    public void testRegexp1() throws Exception {
        insertChar("x = ^", '/', "x = /^/");
    }

    public void testRegexp2() throws Exception {
        insertChar("x = /^/", '/', "x = //^");
    }

    public void testRegexp3() throws Exception {
        insertChar("x = /^/", 'a', "x = /a^/");
    }
    
    public void testRegexp4() throws Exception {
        insertChar("x = /\\^/", '/', "x = /\\/^/");
    }

    public void testSinglePercent1() throws Exception {
        insertChar("x = %q^", '(', "x = %q(^)");
    }

    public void testSinglePercent2() throws Exception {
        insertChar("x = %q(^)", ')', "x = %q()^");
    }
    
    // Broken!!
//    public void testSinglePercent3() throws Exception {
//        insertChar("x = %q(^)", '(', "x = %q((^))");
//    }

    // Broken!!
//    public void testSinglePercent4() throws Exception {
//        insertChar("x = %q((^))", ')', "x = %q(()^)");
//    }

    // Broken!
    // This test fails for an unknown reason. It only happens within the
    // test harness, not when I try to reproduce it by hand!
//    public void testSinglePercent5() throws Exception {
//        insertChar("x = %q((^))", 'a', "x = %q((a^))");
//    }
    
    public void testSinglePercent6() throws Exception {
        insertChar("x = %q^", '-', "x = %q-^-");
    }

    public void testSinglePercent7() throws Exception {
        insertChar("x = %q-^-", '-', "x = %q--^");
    }
    
    public void testSinglePercent8() throws Exception {
        insertChar("x = %q^", ' ', "x = %q ^ ");
    }

    // Broken!
//    public void testSinglePercent9() throws Exception {
//        insertChar("x = %q ^ ", ' ', "x = %q  ^");
//    }
    
    public void testSinglePercent10() throws Exception {
        insertChar("x = %q ^ ", 'x', "x = %q x^ ");
    }

    public void testSinglePercent11() throws Exception {
        insertChar("x = %q-\\^-", '-', "x = %q-\\-^-");
    }

    public void testHeredoc1() throws Exception {
        insertBreak("x=<<FOO^\n", "x=<<FOO\n^\nFOO\n");
    }

    // TODO: should I ensure that there's no indentation for heredocs?
    public void testHeredoc2() throws Exception {
        insertBreak("x=f(<<FOO,^\n", "x=f(<<FOO,\n  ^\nFOO\n");
    }
    
    public void testFindMatching1() throws Exception {
        match("^if true\n^end");
    }
    
    public void testFindMatching2() throws Exception {
        match("x=^(true^)\ny=5");
    }
    
    public void testFindMatching3() throws Exception {
        match("x=^(true || (false)^)\ny=5");
    }

    public void testFindMatching4() throws Exception {
        match("^def foo\nif true\nend\n^end\nend");
    }

    public void testFindMatching5() throws Exception {
        // Test heredocs
        match("x=f(^<<ABC,\"hello\")\nfoo\nbar\n^ABC\n");
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
    
    public void testNoContComment2() throws Exception {
        // No auto-# on new lines
        insertBreak("foo # ^", "foo # \n^");
    }
    
    // Not yet enabled
    public void testContComment() throws Exception {
        if (BracketCompleter.CONTINUE_COMMENTS) {
            insertBreak("# ^", "# \n# ^");
        } else {
            insertBreak("# ^", "# \n^");
        }
    }
    
    public void testDeleteContComment() throws Exception {
        deleteChar("# ^", "^");
        deleteChar("\n# ^", "\n^");
    }
    
    public void testNoDeleteContComment() throws Exception {
        deleteChar("#  ^", "# ^");
        deleteChar("#^", "^");
        deleteChar("puts '# ^'", "puts '#^'");
    }

    public void testFindMatching6() throws Exception {
        // Test heredocs
        match("x=f(^<<ABC,'hello',<<-DEF,'bye')\nfoo\nbar\n^ABC\nbaz\n  DEF\nwhatever");
    }

    public void testFindMatching7() throws Exception {
        // Test heredocs
        match("x=f(<<ABC,'hello',^<<-DEF,'bye')\nfoo\nbar\nABC\nbaz\n  ^DEF\nwhatever");
    }

    // TODO: Test
    // - backspace deletion
    // - entering incomplete output
    // automatic reindentation of "end", "else" etc.

    public void test108889() throws Exception {
        // Reproduce 108889: AIOOBE and AE during editing
        // NOTE: While the test currently throws an exception, when the 
        // exception is fixed the test won't actually pass; that's an expected
        // fail I will deal with later
        insertChar("x = %q((^))", 'a', "x = %q((a^))");
    }

}
