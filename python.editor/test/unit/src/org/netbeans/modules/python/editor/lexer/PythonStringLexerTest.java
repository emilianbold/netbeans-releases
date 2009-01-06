/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor.lexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Tor Norbye
 */
public class PythonStringLexerTest extends NbTestCase {

    public PythonStringLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        Logger.getLogger(PythonStringLexer.class.getName()).setLevel(Level.FINEST);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    protected Level logLevel() {
        // enabling logging
        return Level.INFO; // uncomment this to have logging from PyhonLexer
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    public void test1() {
        String text =
            "\nThis is the \"example\" module.\n\nThe example module supplies one function, factorial().  For example,\n\n>>> factorial(5)120\n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\nThis is the \"example\" module.\n\nThe example module supplies one function, factorial().  For example,\n\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.EMBEDDED_PYTHON, ">>> factorial(5)120");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\n");
        assertFalse(ts.moveNext());
    }

    public void test2() {
        String text =
            "\n>>\n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\n>>\n");
        assertFalse(ts.moveNext());
    }

    public void test3() {
        String text =
            "\n>>>\n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, ">>>\n");
        assertFalse(ts.moveNext());
    }

    public void test4() {
        String text =
            "\n>>> \n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, ">>> \n");
        assertFalse(ts.moveNext());
    }

    public void test5() {
        String text =
            "\n>>>";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, ">>>");
        assertFalse(ts.moveNext());
    }

    public void test6() {
        String text =
            "\n>>>>";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, ">>>>");
        assertFalse(ts.moveNext());
    }

    public void test8() {
        String text =
            "\\";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_INVALID, "\\");
        assertFalse(ts.moveNext());
    }

    public void test9() {
        String text =
            "a\\nb";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonStringTokenId.language);
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_ESCAPE, "\\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonStringTokenId.STRING_TEXT, "b");
        assertFalse(ts.moveNext());
    }
}
