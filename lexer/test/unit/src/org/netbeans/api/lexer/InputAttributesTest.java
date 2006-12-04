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
