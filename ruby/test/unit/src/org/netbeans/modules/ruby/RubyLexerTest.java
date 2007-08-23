/*
 * RubyLexerTest.java
 *
 * Created on November 28, 2006, 8:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.ruby;

import junit.framework.TestCase;
import org.netbeans.api.gsf.GsfTokenId;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;


/**
 *
 * @author Tor Norbye
 */
public class RubyLexerTest extends RubyTestBase {
    public RubyLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    @SuppressWarnings("unchecked")
    public void testComments() {
        String text = "# This is my comment";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LINE_COMMENT, text);
    }

    @SuppressWarnings("unchecked")
    public void testRubyEmbedding() {
        String text = "%r{foo#{code}bar}";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_BEGIN, "%r{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_LITERAL, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "code");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_LITERAL, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_LITERAL, "bar");
    }

    @SuppressWarnings("unchecked")
    public void testStatementModifiers() {
        String text = "foo if false}";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
        // Not RubyTokenId.IF - test that this if is just a statement modifier!
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.ANY_KEYWORD, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.ANY_KEYWORD, "false");
        
        // Make sure if when used not as a statement modifier is recognized
        text = "if false foo}";
        hi = TokenHierarchy.create(text, RubyTokenId.language());
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.ANY_KEYWORD, "false");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "foo");
    }
    
    public void testStrings() {
        String[] strings =
            new String[] { 
            "\"Hello\"",
            "'Hello'",
            "%(Hello)",
            "%q(Hello)",
            "% Hello "};
        for (int i = 0; i < strings.length; i++) {
            TokenHierarchy hi = TokenHierarchy.create(strings[i], RubyTokenId.language());
            TokenSequence ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            assertTrue(ts.token().id() == RubyTokenId.STRING_BEGIN || ts.token().id() == RubyTokenId.QUOTED_STRING_BEGIN);
            assertTrue(ts.moveNext());
            assertTrue(ts.token().id() == RubyTokenId.STRING_LITERAL || ts.token().id() == RubyTokenId.QUOTED_STRING_LITERAL);
            assertTrue(ts.moveNext());
            assertTrue(ts.token().id() == RubyTokenId.STRING_END || ts.token().id() == RubyTokenId.QUOTED_STRING_END);
        }
    }

    @SuppressWarnings("unchecked")
    public void test96485() {
        String text = "\"foo#{\"";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        //LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.ERROR, "\"");

        // Try related scenario for fields
        text = "\"foo#@\"";
        hi = TokenHierarchy.create(text, RubyTokenId.language());
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "\"");
    }
    
    @SuppressWarnings("unchecked")
    public void test101122() {
        String text = "\"\\n\\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.ERROR, "\\n\\n");
    }

    
    
    @SuppressWarnings("unchecked")
    public void testUnterminatedString() {
        String text = "\"Line1\nLine2\nLine3";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.ERROR, "Line1\n");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.CONSTANT, "Line2");
    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString2() {
        String text = "puts \"\n\n\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.IDENTIFIER, "puts");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.ERROR, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n\n");
    }    

    @SuppressWarnings("unchecked")
    public void testUnterminatedString3() {
        String text = "x = \"";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void test93990() {
        String text = "f(<<EOT,\"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz\")\n0123456789\nEOT\ny=5";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        // Just iterate through the sequence to make sure it's okay - this throws an exception because of bug 93990
        while (ts.moveNext()) {
            ;
        }
    }

    @SuppressWarnings("unchecked")
    public void test93990b() {

        String text = "x = f(<<EOT,<<EOY, \"another string\", 50)  # Comment _here\n_xFoo bar\nEOT\nhello\nEOY\ndone\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        // Just iterate through the sequence to make sure it's okay - this throws an exception because of bug 93990
        while (ts.moveNext()) {
            ;
        }
    }
    
    @SuppressWarnings("unchecked")
    public void test93990c() {
        // Multiline
        String text="    javax.swing.JOptionPane.showMessageDialog(nil, <<EOS)\n<html>Hello from <b><u>JRuby</u></b>.<br>\nButton '#{evt.getActionCommand()}' clicked.\nEOS\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        // Just iterate through the sequence to make sure it's okay - this throws an exception because of bug 93990
        while (ts.moveNext()) {
            ;
        }
    }

    @SuppressWarnings("unchecked")
    public void testHeredocInput() {
        // Make sure I can handle input AFTER a heredoc marker and properly tokenize it
        String text = "f(<<EOT,# Comment\nfoo\nEOT\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOT");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LINE_COMMENT, "# Comment\n");
       // LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOT\n");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testHeredocInput2() {
        String text = "f(<<EOT,<<EOY)\nfoo\nEOT\nbar\nEOY\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOT");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOY");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOT\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "bar\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOY\n");
        assertFalse(ts.moveNext());
    }
    
    @SuppressWarnings("unchecked")
    public void testHeredocInput3() { // Boiled down failure from postgresql_adapter.rb
        String text = "q(<<S,name)\nHELLO\nS\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "q");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<S");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "HELLO\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "S\n");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testHeredocEmbedded() {
        String text = "f(<<EOT,<<EOY)\nfoo#{hello}foo\n#{hello}\n\n\nEOT\nbar\nEOY\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOT");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOY");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "hello");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "}foo\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "hello");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "}\n\n\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOT\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "bar\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOY\n");
        assertFalse(ts.moveNext());
    }
    @SuppressWarnings("unchecked")
    public void testHeredocEmpty() {
        String text = "f(<<EOT\nEOT\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOT");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_END, "\nEOT\n");
        assertFalse(ts.moveNext());
    }
    
    @SuppressWarnings("unchecked")
    public void testHeredocError2() {
        String text = "f(<<EOT\nfoo";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOT");
        //LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.ERROR, "\nfoo");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testHeredocError3() {
        String text = "f(<<EOT";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<EOT");
        assertFalse(ts.moveNext());
    }
    
    @SuppressWarnings("unchecked")
    public void testHeredocsIndented() {
        String text = "f(<<-EOT,<<-EOY)\nfoo\n   EOT\nbar\n   EOY\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<-EOT");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<-EOY");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n");
        // XXX Is it correct that the string would include the indentation on the closing
        // delimiter line??
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo\n   ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOT\n");
        // XXX Is it correct that the string would include the indentation on the closing
        // delimiter line??
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "bar\n   ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOY\n");
        assertFalse(ts.moveNext());
    }
    
    @SuppressWarnings("unchecked")
    public void testHeredocsIndentedQuoted() {
        String text = "f(<<-\"EOT\",<<-\"EOY\")\nfoo\n   EOT\nbar\n   EOY\n";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<-\"EOT\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_BEGIN, "<<-\"EOY\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "foo\n   ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOT\n");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "bar\n   ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "EOY\n");
        assertFalse(ts.moveNext());
    }
    
    // 102082
    @SuppressWarnings("unchecked")
    public void testSymbol() {
        String text = ":\"foo\"";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.TYPE_SYMBOL, ":\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.TYPE_SYMBOL, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, GsfTokenId.TYPE_SYMBOL, "\"");
        assertFalse(ts.moveNext());
    }
    
    @SuppressWarnings("unchecked")
    public void testQuotesInEmbeddedCode() {
        // Simplified from sqlserver_adapter's add_limit_offset! method which failed miserably:
        // total_rows = @connection.select_all("SELECT count(*) as TotalRows from (#{sql.gsub(/\bSELECT(\s+DISTINCT)?\b/i, "SELECT#{$1} TOP 1000000000")}) tally")[0][:TotalRows].to_i
        // Quotes are allowed inside a string embedded
        String text = "\"fo#{\"hello\"}\"";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "fo");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "\"hello\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "\"");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testQuotesInEmbeddedCode2() {
        // Simplified from sqlserver_adapter's add_limit_offset! method which failed miserably:
        // total_rows = @connection.select_all("SELECT count(*) as TotalRows from (#{sql.gsub(/\bSELECT(\s+DISTINCT)?\b/i, "SELECT#{$1} TOP 1000000000")}) tally")[0][:TotalRows].to_i
        // Quotes are allowed inside a string embedded
        String text = "\"fo#{puts \"#notcomment\"}\"";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "fo");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "puts \"#notcomment\"");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, "\"");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testNestedEmbeddedCode() {
        // Simplified from sqlserver_adapter's add_limit_offset! method which failed miserably:
        // total_rows = @connection.select_all("SELECT count(*) as TotalRows from (#{sql.gsub(/\bSELECT(\s+DISTINCT)?\b/i, "SELECT#{$1} TOP 1000000000")}) tally")[0][:TotalRows].to_i
        String text = "x(%(#{y=#{z}}))";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "%(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "y=#{z}");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testParensInEmbedded() {
        // Simplified from activerecord-1.15.3/test/base_test.rb in test_array_to_xml_including_methods
        //   assert xml.include?(%(<topic-id type="integer">#{topics(:first).topic_id}</topic-id>)), xml
        String text = "x(%(#{y(1)}))";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_BEGIN, "%(");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.STRING_LITERAL, "#{");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.EMBEDDED_RUBY, "y(1)");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_LITERAL, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.QUOTED_STRING_END, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
        assertFalse(ts.moveNext());
    }

    // Not yet passing
//    @SuppressWarnings("unchecked")
//    public void testDefRegexp() {
//        // /Users/tor/netbeans/work/nbbuild/netbeans/ruby1/jruby-1.0/lib/ruby/1.8/webrick/httputils.rb
//        //     def _make_regex(str) /([#{Regexp.escape(str)}])/n end
//        String text = "def f(s) /df/ end";
//        TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
//        TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.DEF, "def");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "f");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.LPAREN, "(");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.IDENTIFIER, "s");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.RPAREN, ")");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_BEGIN, "/");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_LITERAL, "df");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.REGEXP_END, "/");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.WHITESPACE, " ");
//        LexerTestUtilities.assertNextTokenEquals(ts, RubyTokenId.END, "end");
//        assertFalse(ts.moveNext());
//    }
}    
