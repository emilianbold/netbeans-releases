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

package org.netbeans.api.lexer;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.simple.*;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class InputAttributesTest extends TestCase {

    public InputAttributesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    LanguagePath simpleLP = LanguagePath.get(SimpleTokenId.language());
    LanguagePath jdLP = LanguagePath.get(SimpleJavadocTokenId.language());
    LanguagePath nestedJDLP  = LanguagePath.get(simpleLP, SimpleJavadocTokenId.language());

    public void testGetSetValue() {
        InputAttributes attrs = new InputAttributes();
        assertNull(attrs.getValue(simpleLP, "version"));
        attrs.setValue(simpleLP, "version", Integer.valueOf(1), false);
        assertEquals(attrs.getValue(simpleLP, "version"), Integer.valueOf(1));
        
        attrs = new InputAttributes();
        attrs.setValue(simpleLP, "version", Integer.valueOf(1), true);
        assertEquals(attrs.getValue(simpleLP, "version"), Integer.valueOf(1));
    }

    public void testInheritance() {
        InputAttributes attrs = new InputAttributes();
        attrs.setValue(jdLP, "version", Integer.valueOf(1), false);
        assertNull(attrs.getValue(nestedJDLP, "version"));
        
        attrs = new InputAttributes();
        attrs.setValue(jdLP, "version", Integer.valueOf(1), true);
        assertEquals(attrs.getValue(nestedJDLP, "version"), Integer.valueOf(1));
    }

    public void testLexerInputAttributes() {
        String text = "public static private";

        // Default version recognizes "static" keyword
        TokenHierarchy<?> hi = TokenHierarchy.create(text, SimpleTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.STATIC, "static", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PRIVATE, "private", 14);
        assertFalse(ts.moveNext());

        // Version 1 recognizes "static" as identifier
        InputAttributes attrs = new InputAttributes();
        attrs.setValue(SimpleTokenId.language(), "version", Integer.valueOf(1), false);
        hi = TokenHierarchy.create(text, false, SimpleTokenId.language(), null, attrs);
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "static", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PRIVATE, "private", 14);
        assertFalse(ts.moveNext());
    }
    
}
