/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
