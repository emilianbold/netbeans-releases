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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby;

import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;

/**
 * Test tokens dump of Ruby code input. Based on Java one by Mila Metelka.
 */
public class RubyTokenDumpTest extends NbTestCase {
    
    public RubyTokenDumpTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInput.rb.txt",
                RubyTokenId.language());
    }

    public void testHeredoc1() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/heredoc1.rb.txt",
                RubyTokenId.language());
    }
    
    public void testEmbeddedCode() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/embeddedcode.rb.txt",
                RubyTokenId.language());
    }    

    public void testScenario2() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/postgresql_adapter.rb.txt",
                RubyTokenId.language());
    }    

    public void testScenario3() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/freakout.rb.txt",
                RubyTokenId.language());
    }    

    public void testPercentExpressions() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/percent-expressions2.rb.txt",
                RubyTokenId.language());
    }    
}
