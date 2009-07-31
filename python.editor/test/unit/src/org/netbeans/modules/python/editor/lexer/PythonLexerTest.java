/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor.lexer;


import java.util.ConcurrentModificationException;
import javax.swing.text.BadLocationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 *
 * @author Tor Norbye
 */
public class PythonLexerTest extends NbTestCase {

    public PythonLexerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        Logger.getLogger(PythonLexer.class.getName()).setLevel(Level.FINEST);
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
                "# this is the first comment\n" +
                "SPAM = 1                 # and this is the second comment\n" +
                "                         # ... and now a third!\n" +
                "STRING = \"# This is not a comment.\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "# this is the first comment");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "SPAM");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, "                 ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "# and this is the second comment");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, "                         ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "# ... and now a third!");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "STRING");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "# This is not a comment.");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
    }

    public void test2() {
        String text =
                "#! /usr/bin/python\n" +
                "print \"Hello World!\"\t\n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "#! /usr/bin/python");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_KEYWORD, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "Hello World!");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, "\t");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        assertFalse(ts.moveNext());
    }

    public void test3() {
        String text =
                "#! /usr/bin/python\n" +
                "print \"Hello World!\"\t";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "#! /usr/bin/python");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_KEYWORD, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "Hello World!");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, "\t");
        assertFalse(ts.moveNext());
    }

    public void test4() throws BadLocationException {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class, PythonTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();

        // Newline in empty doc: this started happening around June 1st 2009
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        
        assertFalse(ts.moveNext());

        // Insert text into document
        String text =
                "#! /usr/bin/python\n" +
                "print \"Hello World!\"";
        doc.insertString(0, text, null);

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }

        ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "#! /usr/bin/python");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_KEYWORD, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "Hello World!");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);

        int offset = text.length() - 1;

        doc.remove(offset, 1);

        ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMENT, "#! /usr/bin/python");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_KEYWORD, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ERROR, "Hello World!\n");

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);
    }

    public void testSpecialChars() {
        String text =
                "x(3,2.0)\n" +
                "if (foo.bar[0]):\n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.INT_LITERAL, "3");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.FLOAT_LITERAL, "2.0");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "bar");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.LBRACKET, "[");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.RBRACKET, "]");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        assertFalse(ts.moveNext());
    }

    public void testStrings1() {
        String text =
                "x=UR'''this is\na\nstring\\nwith\\n\\nescapes\n '''";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "UR'''");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "this is\na\nstring\\nwith\\n\\nescapes\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "'''");
        assertFalse(ts.moveNext());
    }

    public void testStrings2() {
        String text =
                "x=UR'''this is\na\nstring\\nwith\\n\\nescapes\n ";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "UR'''");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ERROR, "this is\na\nstring\\nwith\\n\\nescapes\n ");
        assertFalse(ts.moveNext());
    }

    public void testStrings3() {
        String text =
                "x=UR\"\"\"this is\na\nstring\\nwith\\n\\nescapes\n \"\"\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "UR\"\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "this is\na\nstring\\nwith\\n\\nescapes\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"\"\"");
        assertFalse(ts.moveNext());
    }

    public void testStrings4() {
        String text =
                "x=R\"\"\"this is\na\nstring\\nwith\\n\\nescapes\n \"\"\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "R\"\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "this is\na\nstring\\nwith\\n\\nescapes\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"\"\"");
        assertFalse(ts.moveNext());
    }

    public void testStrings5() {
        String text =
                "x=u\"\"\"this is\na\nstring\\nwith\\n\\nescapes\n \"\"\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "u\"\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "this is\na\nstring\\nwith\\n\\nescapes\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"\"\"");
        assertFalse(ts.moveNext());
    }

    public void testDotPrefix() {
        String text =
                ".5";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.FLOAT_LITERAL, ".5");
        assertFalse(ts.moveNext());

        text = "x.y";
        hi = TokenHierarchy.create(text, PythonTokenId.language());
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "y");
        assertFalse(ts.moveNext());
    }

    public void testExponential() {
        String text =
                "1e100";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.FLOAT_LITERAL, "1e100");
        assertFalse(ts.moveNext());
    }

    public void testExponential2() {
        String text =
                "3.14e-10";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.FLOAT_LITERAL, "3.14e-10");
        assertFalse(ts.moveNext());
    }

    public void testImaginary() {
        String text =
                "3.14e-10j";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.FLOAT_LITERAL, "3.14e-10j");
        assertFalse(ts.moveNext());
    }

    public void testChars1() {
        String text =
                "x = \"\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
        assertFalse(ts.moveNext());
    }

    public void testChars2() {
        String text =
                "x = \"'\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ANY_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_LITERAL, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
        assertFalse(ts.moveNext());
    }

    public void testCornerCases() {
        String text =
                "\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        assertFalse(ts.moveNext());
    }

    public void testCornerCases2() {
        String text =
                "\"foo";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.ERROR, "foo");
        assertFalse(ts.moveNext());
    }

    public void testEmptyString1() {
        String text =
                "\"\"\"\"\"\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"\"\"");
        assertFalse(ts.moveNext());
    }

    public void testEmptyString2() {
        String text =
                "''''''";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "'''");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "'''");
        assertFalse(ts.moveNext());
    }

    public void testEmptyString3() {
        String text =
                "''";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "'");
        assertFalse(ts.moveNext());
    }

    public void testEmptyString4() {
        String text =
                "\"\"";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.STRING_END, "\"");
        assertFalse(ts.moveNext());
    }

    public void testDecorators1() {
        String text =
                "@foo\ndef bar:\n";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.DECORATOR, "@foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.DEF, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.IDENTIFIER, "bar");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.NEWLINE, "\n");
        assertFalse(ts.moveNext());
    }

    public void testDecorators2() {
        String text =
                "@,";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.DECORATOR, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonTokenId.COMMA, ",");
        assertFalse(ts.moveNext());
    }
}
