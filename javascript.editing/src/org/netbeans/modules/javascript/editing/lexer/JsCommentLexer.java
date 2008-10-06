/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.javascript.editing.lexer;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for JavaScript comments
 * It is supposed to support 3 standards: DOJO/JSDoc/ScriptDoc, note that
 * JSDoc/ScriptDoc are JavaDoc-like, while DOJO is not.
 * 
 * @todo this is initial version, copy of JavaDoc, so it works somehow nicely
 * with JSDoc/ScriptDoc
 * @todo introduce lexer per standard or lex all standards in one lexer?
 * @todo recognizes email address as doc tag
 *
 * @author Miloslav Metelka
 * @author Martin Adamek
 */

public class JsCommentLexer implements Lexer<JsCommentTokenId> {
    public static final String AT_RETURN = "@return";

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<JsCommentTokenId> tokenFactory;
    
    public JsCommentLexer(LexerRestartInfo<JsCommentTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
    }
    
    @SuppressWarnings("empty-statement")
    public Token<JsCommentTokenId> nextToken() {
        int ch = input.read();
        
        if (ch == EOF) {
            return null;
        }
        
        if (Character.isJavaIdentifierStart(ch)) {
            //TODO: EOF
            while (Character.isJavaIdentifierPart(input.read()))
                ;
            
            input.backup(1);
            return token(JsCommentTokenId.IDENT);
        }
        
        if ("@<.#{}".indexOf(ch) == (-1)) {
            //TODO: EOF
            ch = input.read();
            
            while (!Character.isJavaIdentifierStart(ch) && "@<.#{}".indexOf(ch) == (-1) && ch != EOF) {
                ch = input.read();
            }
            
            if (ch != EOF) {
                input.backup(1);
            }
            return token(JsCommentTokenId.OTHER_TEXT);
        }
        
        switch (ch) {
            case '@':
                while (true) {
                    ch = input.read();
                    
                    if (!Character.isLetter(ch)) {
                        input.backup(1);
                        return tokenFactory.createToken(JsCommentTokenId.COMMENT_TAG, input.readLength());
                    }
                }
            case '<':
                while (true) {
                    ch = input.read();
                    if (ch == '>' || ch == EOF) {
                        return token(JsCommentTokenId.HTML_TAG);
                    }
                }
            case '{':
                return token(JsCommentTokenId.LCURL);
            case '}':
                return token(JsCommentTokenId.RCURL);
            case '.':
                return token(JsCommentTokenId.DOT);
            case '#':
                return token(JsCommentTokenId.HASH);
        } // end of switch (ch)
        
        assert false;
        
        return null;
    }

    private Token<JsCommentTokenId> token(JsCommentTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

    /** 
     * Find the return and parameter types for the following function, according to the documentation.
     * This function will return a map of parameter names and the corresponding type string.
     * The type string can be null (for known parameters with unknown types), or a type string, or some
     * set of types separated by |.
     * The return value is using the special key AT_RETURN.
     */
    public static Map<String, String> findFunctionTypes(TokenSequence<? extends JsCommentTokenId> ts) {
        Map<String, String> result = new HashMap<String, String>();
        
        while (ts.moveNext()) {
            Token<? extends JsCommentTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == JsCommentTokenId.COMMENT_TAG) {
                CharSequence text = token.text();
                if (TokenUtilities.textEquals("@param", text) ||  // NOI18N
                        TokenUtilities.textEquals("@argument", text)) { // NOI18N
                    int index = ts.index();
                    String type = nextType(ts);
                    if (type == null) {
                        ts.moveIndex(index);
                        ts.moveNext();
                    }
                    String name = nextIdent(ts);
                    if (name != null) {
                        result.put(name, type);
                    } else {
                        ts.moveIndex(index);
                        ts.moveNext();
                    }
                } else if (TokenUtilities.textEquals("@type", text)) { // NOI18N)
                    String type = nextIdentGroup(ts);
                    if (type != null) {
                        result.put(AT_RETURN,type); // NOI18N
                    }
                } else if (TokenUtilities.textEquals(AT_RETURN,text) || // NOI18N
                        TokenUtilities.textEquals("@returns", text)) { // NOI18N
                    // There can be both @return and @type where one of them specifies
                    // the type so don't overwrite the map entry unconditionally
                    String type = nextType(ts);
                    if (type != null) {
                        result.put(AT_RETURN,type); // NOI18N
                    }
                } else if (TokenUtilities.textEquals("@namespace", text) || // NOI18N
                        TokenUtilities.textEquals("@extends", text) || // NOI18N
                        TokenUtilities.textEquals("@method", text) || // NOI18N
                        TokenUtilities.textEquals("@property", text) || // NOI18N
                        TokenUtilities.textEquals("@class", text)) { // NOI18N
                    String arg = nextIdentGroup(ts);
                    if (arg != null) {
                        result.put(text.toString(), arg);
                    }
                } else if (TokenUtilities.textEquals("@private", text) || // NOI18N
                        TokenUtilities.textEquals("@constructor", text) || // NOI18N
                        TokenUtilities.textEquals("@ignore", text) || // NOI18N
                        TokenUtilities.textEquals("@deprecated", text)) { // NOI18N
                    result.put(text.toString(), ""); // NOI18N
                }
            }
        }
        
        return result;
    }
    
    /**
     * Searches for closest (to current token) type definition in curly braces.
     * Skips sequence IGNORED - LCURL - IGNORED where IGNORED is token ignored by {@link #nextNonIgnored}
     * All IDENT tokens found in LCURL - RCURL range are returned separated by '|' character
     * @param ts token sequence to perform the search from current token
     * @return found IDENT token or null if no such token exists
     */
    public static String nextType(TokenSequence<? extends JsCommentTokenId> ts) {
        StringBuilder sb = new StringBuilder();
        // find next token which is not OTHER_TEXT
        Token<? extends JsCommentTokenId> nextToken = nextNonIgnored(ts);
        // if it is left curly brace try to find next IDENT token
        if (nextToken != null && nextToken.id() == JsCommentTokenId.LCURL) {
            boolean newToken = true;
            while (ts.moveNext() && ts.token().id() != JsCommentTokenId.RCURL) {
                TokenId tid = ts.token().id();
                if (tid == JsCommentTokenId.IDENT || tid == JsCommentTokenId.DOT) {
                    if (newToken) {
                        if (sb.length() > 0) { sb.append('|'); }
                    }
                    newToken = false;
                    sb.append(ts.token().text());
                } else if (tid == JsCommentTokenId.OTHER_TEXT && TokenUtilities.startsWith(ts.token().text(), "[]")) { // NOI18N
                    //sb.append("[]"); // NOI18N
                    rewriteAsArray(sb);
                    newToken = true;
                } else if (tid == JsCommentTokenId.HTML_TAG && TokenUtilities.startsWith(ts.token().text(), "<") && // NOI18N
                        sb.length() >= 5 && sb.charAt(sb.length()-1) == 'y' && sb.toString().endsWith("Array")) { // NOI18N
                    // Array<Foo>. We don't just insert all tags since many references put links to documentation around types
                    sb.append(ts.token().text());
                    newToken = true;
                } else {
                    newToken = true;
                }
            }
            if (ts.token() != null && ts.token().id() == JsCommentTokenId.RCURL) {
                return sb.toString();
            }
        }
        return null;
    }
    
    /**
     * Searches for closest (to current token) IDENT token.
     * Skips tokens ignored by {@link #nextNonIgnored} if there are any.
     * @param ts token sequence to perform the search from current token
     * @return found IDENT token or null if no such token exists
     */
    public static String nextIdent(TokenSequence<? extends JsCommentTokenId> ts) {
        // find next token which is not OTHER_TEXT
        Token<? extends JsCommentTokenId> nextToken = nextNonIgnored(ts);
        // if it is IDENT token return its text
        if (nextToken != null && nextToken.id() == JsCommentTokenId.IDENT) {
            return nextToken.text().toString();
        }
        return null;
    }

    /**
     * Find the next dot-joined group of idents.
     * Skips tokens ignored by {@link #nextNonIgnored} if there are any.
     * @param ts token sequence to perform the search from current token
     * @return found IDENT token group or null if no such token exists
     */
    public static String nextIdentGroup(TokenSequence<? extends JsCommentTokenId> ts) {
        // find next token which is not OTHER_TEXT
        Token<? extends JsCommentTokenId> nextToken = nextNonIgnored(ts);
        // if it is IDENT token return its text
        if (nextToken != null && nextToken.id() == JsCommentTokenId.IDENT) {
            // Peek to see if we have a dot next to it
            if (ts.moveNext()) {
                TokenId tid = ts.token().id();
                if (tid == JsCommentTokenId.DOT) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(nextToken.text());
                    
                    boolean goback = true;
                    while (ts.token().id() == JsCommentTokenId.DOT ||
                            ts.token().id() == JsCommentTokenId.IDENT) {
                        sb.append(ts.token().text());
                        if (!ts.moveNext()) {
                            goback = false;
                            break;
                        }
                    }
                    if (goback && TokenUtilities.startsWith(ts.token().text(), "[]")) { // NOI18N
                        //sb.append("[]");
                        rewriteAsArray(sb);
                    } else if (goback) {
                        ts.movePrevious();
                    }
                    return sb.toString();
                } else if (tid == JsCommentTokenId.HTML_TAG && TokenUtilities.startsWith(ts.token().text(), "<") && // NOI18N
                        TokenUtilities.endsWith(nextToken.text(), "Array")) { // NOI18N
                    return "Array" + ts.token().text().toString(); // NOI18N
                } else if (tid == JsCommentTokenId.OTHER_TEXT && TokenUtilities.startsWith(ts.token().text(), "[]")) { // NOI18N
                    // XXX This won't work for dotted types
                    return "Array<" + nextToken.text().toString() + ">"; // NOI18N
                }
                
                ts.movePrevious();
            }
            return nextToken.text().toString();
        }
        return null;
    }

    public static String getCompat(TokenSequence<? extends JsCommentTokenId> ts) {
        // find next token which is not OTHER_TEXT
        StringBuilder sb = new StringBuilder();
        nextIdent(ts);
        do {
            Token<? extends JsCommentTokenId> t = ts.token();
            if (t.id() == JsCommentTokenId.IDENT) {
                sb.append(t.text());
            } else {
                break;
            }
            if (ts.moveNext()) {
                t = ts.token();
                if (t.id() == JsCommentTokenId.OTHER_TEXT && t.length() == 1 &&
                        t.text().charAt(0) == '|') {
                    sb.append('|');
                } else {
                    break;
                }
            } else {
                break;
            }
        } while (ts.moveNext());

        return sb.toString();
    }
    
    /** If you have something like "String[]" we want to rewrite it as Array<String>.
     * The StringBuilder may already contain "String" when we see []. At this point
     * we want to rewrite the buffer to contain Array<String>. That's what this method
     * does: It finds the most recent type and converts it to an array.
     */
    private static void rewriteAsArray(StringBuilder sb) {
        int last = sb.length()-1;
        for (; last >= 0; last--) {
            char c = sb.charAt(last);
            if (c == '|') {
                break;
            }
        }
        last++;
        if (last == sb.length()) {
            // Error in documentation
            return;
        }
        
        String s = sb.substring(last);
        sb.setLength(last);
        sb.append("Array<"); // NOI18N
        sb.append(s);
        sb.append(">"); // NOI18N
    }
    
    /**
     * Searches for next token that is not OTHER_TEXT or RCURLY
     */
    private static Token<? extends JsCommentTokenId> nextNonIgnored(TokenSequence<? extends JsCommentTokenId> ts) {
        while (ts.moveNext() && (
                ts.token().id() == JsCommentTokenId.OTHER_TEXT ||
                ts.token().id() == JsCommentTokenId.RCURL)) {
        }
        return ts.token();
    }
    
}
