/*
 * RubyCommentLexerTest.java
 *
 * Created on February 26, 2007, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.ruby;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.ruby.lexer.RubyCommentTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Tor Norbye
 */
public class RubyCommentLexerTest extends NbTestCase {
    
    public RubyCommentLexerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @SuppressWarnings("unchecked")
    public void testClassLink() {
        String text = "Clz#mtd";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyCommentTokenId.COMMENT_LINK, "Clz#mtd");
    }

    @SuppressWarnings("unchecked")
    public void testClassLink2() {
        String text = "my Clz#mtd,";
        TokenHierarchy hi = TokenHierarchy.create(text, RubyCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, RubyCommentTokenId.COMMENT_TEXT, "my ");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyCommentTokenId.COMMENT_LINK, "Clz#mtd");
        LexerTestUtilities.assertNextTokenEquals(ts, RubyCommentTokenId.COMMENT_TEXT, ",");
    }

    public void test() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testComments.rb.txt",
                RubyCommentTokenId.language());
    }
}
