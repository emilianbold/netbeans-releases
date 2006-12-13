/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test.dump;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenDumpTest extends NbTestCase {
    
    public TokenDumpTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenDump() throws Exception {
        // The testfiles/TokenDumpTestFile.txt in the code below should be located under
        // $module/test/unit/data directory e.g. $cvs/lexer/test/unit/data/testfiles/TokenDumpTestFile.txt
        // ("Files" view by Ctrl+2 can be used
        // to locate/create appropriate dirs/files).
        // The TokenDumpTestFile.txt.tokens.txt will be created by the test
        // if it does not exist (see also javadoc of LexerTestUtilities.checkTokenDump()).
        LexerTestUtilities.checkTokenDump(this,
                "testfiles/TokenDumpTestFile.txt",
                TextAsSingleTokenTokenId.language());
    }

}
