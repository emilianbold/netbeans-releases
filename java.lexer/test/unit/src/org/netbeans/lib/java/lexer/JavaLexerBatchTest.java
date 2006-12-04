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

import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class JavaLexerBatchTest extends TestCase {

    public JavaLexerBatchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testComments() {
        String text = "/*ml-comment*//**//***//**\n*javadoc-comment*//* a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BLOCK_COMMENT, "/*ml-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BLOCK_COMMENT, "/**/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.JAVADOC_COMMENT, "/***/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.JAVADOC_COMMENT, "/**\n*javadoc-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BLOCK_COMMENT_INCOMPLETE, "/* a");
    }
    
    public void testIdentifiers() {
        String text = "a ab aB2 2a x\nyZ\r\nz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "ab");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "aB2");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "yZ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\r\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "z");
    }
    
    public void testCharLiterals() {
        String text = "'' 'a''' '\\'' '\\\\' '\\\\\\'' '\\n' 'a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'a'");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\\\'");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\\\\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\n'");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL_INCOMPLETE, "'a");
    }
    
    public void testStringLiterals() {
        String text = "\"\" \"a\"\"\" \"\\\"\" \"\\\\\" \"\\\\\\\"\" \"\\n\" \"a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\\\\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\\\\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL_INCOMPLETE, "\"a");
    }
    
    public void testNumberLiterals() {
        String text = "0 00 09 1 12 0L 1l 12L 0x1 0xf 0XdE 0Xbcy" + 
                " 09.5 1.5f 2.5d 6d 7e3 6.1E-7f 0xa.5dp+12d .3";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "00");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "0L");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "1l");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "12L");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0x1");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0xf");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0XdE");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0Xbc");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "09.5");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FLOAT_LITERAL, "1.5f");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "2.5d");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "6d");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "7e3");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FLOAT_LITERAL, "6.1E-7f");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "0xa.5dp+12d");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, ".3");
    }
    
    public void testOperators() {
        String text = "^ ^= % %= * *= / /= = ==";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CARET, "^");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CARETEQ, "^=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PERCENT, "%");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PERCENTEQ, "%=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STAREQ, "*=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SLASHEQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.EQ, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.EQEQ, "==");
    }

    public void testKeywords() {
        String text = "abstract assert boolean break byte case catch char class const continue " +
            "default do double else enum extends final finally float for goto if " +
            "implements import instanceof int interface long native new package " +
            "private protected public return short static strictfp super switch " +
            "synchronized this throw throws transient try void volatile while " +
            "null true false";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ABSTRACT, "abstract");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ASSERT, "assert");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BOOLEAN, "boolean");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BREAK, "break");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BYTE, "byte");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CASE, "case");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CATCH, "catch");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR, "char");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CLASS, "class");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CONST, "const");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CONTINUE, "continue");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DEFAULT, "default");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DO, "do");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ENUM, "enum");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.EXTENDS, "extends");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FINAL, "final");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FINALLY, "finally");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FLOAT, "float");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FOR, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.GOTO, "goto");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IMPLEMENTS, "implements");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IMPORT, "import");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INSTANCEOF, "instanceof");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INTERFACE, "interface");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG, "long");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.NATIVE, "native");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.NEW, "new");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PACKAGE, "package");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PRIVATE, "private");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PROTECTED, "protected");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PUBLIC, "public");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.RETURN, "return");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SHORT, "short");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STATIC, "static");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRICTFP, "strictfp");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SUPER, "super");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SWITCH, "switch");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SYNCHRONIZED, "synchronized");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.THIS, "this");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.THROW, "throw");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.THROWS, "throws");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.TRANSIENT, "transient");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.TRY, "try");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.VOID, "void");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.VOLATILE, "volatile");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHILE, "while");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.NULL, "null");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.TRUE, "true");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FALSE, "false");
    }

    public void testNonKeywords() {
        String text = "abstracta assertx b br car dou doubl finall im i ifa inti throwsx";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "abstracta");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "assertx");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "throwsx");
    }
    
    public void testEmbedding() {
        String text = "ddx \"d\\t\\br\" /** @see X */";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "ddx");
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        assertEquals(3, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"d\\t\\br\"");
        assertEquals(4, ts.offset());
        
        TokenSequence<? extends TokenId> es = ts.embedded();
        
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.TEXT, "d");
        assertEquals(5, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.TAB, "\\t");
        assertEquals(6, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.BACKSPACE, "\\b");
        assertEquals(8, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.TEXT, "r");
        assertEquals(10, es.offset());
        
        assertFalse(es.moveNext());
        
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        assertEquals(12, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.JAVADOC_COMMENT, "/** @see X */");
        assertEquals(13, ts.offset());
        
        TokenSequence<? extends TokenId> ds = ts.embedded();
        
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.OTHER_TEXT, " ");
        assertEquals(16, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.TAG, "@see");
        assertEquals(17, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.OTHER_TEXT, " ");
        assertEquals(21, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.IDENT, "X");
        assertEquals(22, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.OTHER_TEXT, " ");
        assertEquals(23, ds.offset());
        
        assertFalse(ds.moveNext());
        
        assertFalse(ts.moveNext());
    }
}
