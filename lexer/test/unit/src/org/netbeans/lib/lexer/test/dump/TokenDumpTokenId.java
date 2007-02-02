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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Ids for parsing of the input file of a particular language
 * for the token dump check.
 * <br/>
 * The text of the special tokens is interlarded with dots
 * to eliminate the possibility that the particular special token will clash
 * with the target language.
 *
 * @author mmetelka
 */
public enum TokenDumpTokenId implements TokenId {
    
    /** Single line of text without newline. */
    TEXT(null),
    
    /** Unicode character as line containing ".\.u.XXXX." only where XXXX
     * is sequence of (lowercase or uppercase) hex digits.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the character.
     */
    UNICODE_CHAR("character"),
    
    /** Line containing ".\.b." only defines \b char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \b character.
     */
    BACKSPACE_CHAR("character"),
    /** Line containing ".\.f." only defines \f char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \f character.
     */
    FORM_FEED_CHAR("character"),
    /** Line containing ".\.b." only defines \b char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \b character.
     */
    CR_CHAR("character"),
    /** Line containing ".\.r." only defines \r char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \r character.
     */
    NEWLINE_CHAR("character"),
    /** Line containing ".\.t." only defines \t char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \t character.
     */
    TAB_CHAR("character"),
    
    /** EOF mark as line containing ".e.o.f." only.
     * It helps to separate tests and test lexer's behavior at the end of buffer.
     */
    EOF_VIRTUAL(null),
    
    /** Test name as line starting with ".t.e.s.t." to help debugging
     *  where a possible problem occurred. It should be used at begining
     *  or inside a test between virtual eofs.
     */
    TEST_NAME(null),
    
    /** Newline '\r', '\n' or '\r\n'.
     * <br/>
     * The test itself will replace this with '\n' because otherwise the output
     * of the token dump would contain the particular line separator depending on the platform
     * where the file would be checked out which would break the test.
     * <br/>
     * To test specific line separators the {@link #CR_CHAR} or {@link #NEWLINE_CHAR} may be used.
     */
    NEWLINE(null);
    
    private String primaryCategory;

    private TokenDumpTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    
    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<TokenDumpTokenId> lang = new LanguageHierarchy<TokenDumpTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-eof-mark";
        }

        @Override
        protected Collection<TokenDumpTokenId> createTokenIds() {
            return EnumSet.allOf(TokenDumpTokenId.class);
        }

        @Override
        protected Lexer<TokenDumpTokenId> createLexer(LexerRestartInfo<TokenDumpTokenId> info) {
            return new TokenDumpLexer(info);
        }
        
    }.language();
    
    public static Language<TokenDumpTokenId> language() {
        return lang;
    }
    
    private static Set<TokenDumpTokenId> charLiterals;
    
    public static boolean isCharLiteral(TokenDumpTokenId id) {
        Set<TokenDumpTokenId> catMembers = charLiterals;
        if (catMembers == null) {
            catMembers = language().tokenCategoryMembers("character");
            charLiterals = catMembers;
        }
        return catMembers.contains(id);
    }

    /**
     * Token property giving the unicode character value.
     */ 
    public static final String UNICODE_CHAR_TOKEN_PROPERTY = "unicode-char";
        
}
