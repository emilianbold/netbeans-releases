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
