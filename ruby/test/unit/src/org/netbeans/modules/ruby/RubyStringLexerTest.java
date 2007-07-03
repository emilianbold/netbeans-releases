/*
 * RubyCommentLexerTest.java
 *
 * Created on February 26, 2007, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.ruby;

import org.netbeans.modules.ruby.lexer.RubyStringTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Tor Norbye
 */
public class RubyStringLexerTest extends NbTestCase {
    
    public RubyStringLexerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void test() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testStrings.rb.txt",
                RubyStringTokenId.languageDouble());
    }
}
