/*
 * RubyCommentLexerTest.java
 *
 * Created on February 26, 2007, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.ruby;

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

    public void test() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testComments.rb.txt",
                RubyCommentTokenId.language());
    }
}
