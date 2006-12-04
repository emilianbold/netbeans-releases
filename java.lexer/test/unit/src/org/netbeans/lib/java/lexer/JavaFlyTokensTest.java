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

package org.netbeans.lib.java.lexer;

import java.io.File;
import java.io.FileReader;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test how many flyweight tokens gets created over a typical java source
 * (copy of javax.swing.JComponent is used).
 *
 * @author mmetelka
 */
public class JavaFlyTokensTest extends NbTestCase {
    
    public JavaFlyTokensTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/JComponent.java.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        
        System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                + "\nTotal tokens: " + ts.tokenCount()
                + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                + "\nTotal text length: " + fileLen
                + "\nDistribution: " + LexerTestUtilities.flyweightDistribution(ts)
        );

        assertEquals(LexerTestUtilities.flyweightTokenCount(ts), 13884);
        assertEquals(LexerTestUtilities.flyweightTextLength(ts), 21741);
        assertEquals(ts.tokenCount(), 21481);
        
    }
    
}
