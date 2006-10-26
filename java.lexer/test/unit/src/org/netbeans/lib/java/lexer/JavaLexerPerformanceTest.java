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
import java.nio.CharBuffer;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * This is a test that scans the source that is a copy of javax.swing.JComponent
 * and reports the times of creation of all the tokens. It exists
 * mainly because it's easy to run profiler over it.
 *
 * @author mmetelka
 */
public class JavaLexerPerformanceTest extends NbTestCase {
    
    public JavaLexerPerformanceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        // Disable for performance testing
        // LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testString() throws Exception {
        String text = readJComponentFile();
        TokenHierarchy hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        // Initial pass - force all the tokens to be initialized
        while (ts.moveNext()) { }

        // Create the token hierarchy again and measure time
        hi = TokenHierarchy.create(text, JavaTokenId.language());
        ts = hi.tokenSequence();
        long tm = System.currentTimeMillis();
        // Force all the tokens to be initialized
        while (ts.moveNext()) { }
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over String: all tokens created in " + tm + " ms.");
    }
    
    public void testDocument() throws Exception {
        String text = readJComponentFile();
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy hi = TokenHierarchy.get(doc);
        TokenSequence ts = hi.tokenSequence();
        while (ts.moveNext()) { }
        
        // Create the document and token hierarchy again and measure time
        doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, JavaTokenId.language());
        hi = TokenHierarchy.get(doc);
        ts = hi.tokenSequence();
        long tm = System.currentTimeMillis();
        // Force all the tokens to be initialized
        while (ts.moveNext()) { }
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over Swing Document: all tokens created in " + tm + " ms.");

    }
    
    private String readJComponentFile() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/JComponent.java.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        return cb.toString();
    }
    
}
