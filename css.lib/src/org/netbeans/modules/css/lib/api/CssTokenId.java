/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * or only the GPL Version , indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.css.lib.api;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.css.lib.nblexer.CssLanguageHierarchy;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.css.lib.Css3Lexer;

/**
 * Token ids of CSS language
 *
 * @author Marek Fukala
 */
public enum CssTokenId implements TokenId {
    
     /* Defined categories:
     * -------------------
     * others
     * brace
     * separator
     * hash
     * operator
     * string
     * url
     * keyword
     * number
//     * rgb
     * identifier
//     * function
     * whitespace
     * comment
     */
   
    //see the Css3.g lexer definition to find out which of the tokens are only
    //token fragments (will not show up in the output tokens list)

    EOF(Css3Lexer.EOF, "others"),
    
    ERROR(org.antlr.runtime.Token.INVALID_TOKEN_TYPE, "error"),
    
    WS(Css3Lexer.WS, "whitespace"),
    CHARSET_SYM(Css3Lexer.CHARSET_SYM, "keyword"),
    STRING(Css3Lexer.STRING, "string"),
    SEMI(Css3Lexer.SEMI, "separator"),
    IMPORT_SYM(Css3Lexer.IMPORT_SYM, "keyword"),
    URI(Css3Lexer.URI, "uri"),
    COMMA(Css3Lexer.COMMA, "separator"),
    MEDIA_SYM(Css3Lexer.MEDIA_SYM, "keyword"),
    LBRACE(Css3Lexer.LBRACE, "brace"),
    RBRACE(Css3Lexer.RBRACE, "brace"),
    IDENT(Css3Lexer.IDENT, "identifier"),
    PAGE_SYM(Css3Lexer.PAGE_SYM, "keyword"),
    COLON(Css3Lexer.COLON, "separator"),
    SOLIDUS(Css3Lexer.SOLIDUS, "others"),
    PLUS(Css3Lexer.PLUS, "operator"),
    GREATER(Css3Lexer.GREATER, "operator"),
    TILDE(Css3Lexer.TILDE, "operator"),
    MINUS(Css3Lexer.MINUS, "operator"),
    STAR(Css3Lexer.STAR, "operator"),
    HASH(Css3Lexer.HASH, "hash"),
    DOT(Css3Lexer.DOT, "operator"),
    LBRACKET(Css3Lexer.LBRACKET, "brace"),
    OPEQ(Css3Lexer.OPEQ, "others"),
    INCLUDES(Css3Lexer.INCLUDES, "operator"),
    DASHMATCH(Css3Lexer.DASHMATCH, "operator"),
    RBRACKET(Css3Lexer.RBRACKET, "brace"),
    LPAREN(Css3Lexer.LPAREN, "brace"),
    RPAREN(Css3Lexer.RPAREN, "brace"),
    IMPORTANT_SYM(Css3Lexer.IMPORTANT_SYM, "keyword"),
    NUMBER(Css3Lexer.NUMBER, "number"),
    PERCENTAGE(Css3Lexer.PERCENTAGE, "number"),
    LENGTH(Css3Lexer.LENGTH, "number"),
    EMS(Css3Lexer.EMS, "number"),
    EXS(Css3Lexer.EXS, "number"),
    ANGLE(Css3Lexer.ANGLE, "number"),
    TIME(Css3Lexer.TIME, "number"),
    FREQ(Css3Lexer.FREQ, "number"),
    HEXCHAR(Css3Lexer.HEXCHAR, "number"),
    NONASCII(Css3Lexer.NONASCII, "others"),
    UNICODE(Css3Lexer.UNICODE, "others"),
    ESCAPE(Css3Lexer.ESCAPE, "others"),
    NMSTART(Css3Lexer.NMSTART, "others"),
    NMCHAR(Css3Lexer.NMCHAR, "others"),
    NAME(Css3Lexer.NAME, "others"),
    URL(Css3Lexer.URL, "url"),
    A(Css3Lexer.A, "others"),
    B(Css3Lexer.B, "others"),
    C(Css3Lexer.C, "others"),
    D(Css3Lexer.D, "others"),
    E(Css3Lexer.E, "others"),
    F(Css3Lexer.F, "others"),
    G(Css3Lexer.G, "others"),
    H(Css3Lexer.H, "others"),
    I(Css3Lexer.I, "others"),
    J(Css3Lexer.J, "others"),
    K(Css3Lexer.K, "others"),
    L(Css3Lexer.L, "others"),
    M(Css3Lexer.M, "others"),
    N(Css3Lexer.N, "others"),
    O(Css3Lexer.O, "others"),
    P(Css3Lexer.P, "others"),
    Q(Css3Lexer.Q, "others"),
    R(Css3Lexer.R, "others"),
    S(Css3Lexer.S, "others"),
    T(Css3Lexer.T, "others"),
    U(Css3Lexer.U, "others"),
    V(Css3Lexer.V, "others"),
    W(Css3Lexer.W, "others"),
    X(Css3Lexer.X, "others"),
    Y(Css3Lexer.Y, "others"),
    Z(Css3Lexer.Z, "others"),
    COMMENT(Css3Lexer.COMMENT, "comment"),
    
    //following two should possibly not be part of the grammar at all
    CDO(Css3Lexer.CDO, "others"), //<!--
    CDC(Css3Lexer.CDC, "others"), // -->
    
    
    INVALID(Css3Lexer.INVALID, "others"),
    DIMENSION(Css3Lexer.DIMENSION, "number"),
    NL(Css3Lexer.NL, "others"), //newline
    T__82(Css3Lexer.T__82, "others"),  //NOI18N
    
    GEN(Css3Lexer.GEN, "others");
    
    private static final Map<Integer, CssTokenId> codesMap = new HashMap<Integer, CssTokenId>();
    static {
        for(CssTokenId id : values()) {
            codesMap.put(id.code, id);
        }
    }

    public static  CssTokenId forTokenTypeCode(int tokenTypeCode) {
        return codesMap.get(tokenTypeCode);
    }
 
    private final String primaryCategory;
    private final int code;

    private static final Language<CssTokenId> language = new CssLanguageHierarchy().language();
    
    CssTokenId(int code, String primaryCategory) {
        this.primaryCategory = primaryCategory;
        this.code = code;
    }

    /** Gets a LanguageDescription describing a set of token ids
     * that comprise the given language.
     *
     * @return non-null LanguageDescription
     */
    public static Language<CssTokenId> language() {
        return language;
    }

    /**
     * Get name of primary token category into which this token belongs.
     * <br/>
     * Other token categories for this id can be defined in the language hierarchy.
     *
     * @return name of the primary token category into which this token belongs
     *  or null if there is no primary category for this token.
     */
    @Override
    public String primaryCategory() {
        return primaryCategory;
    }


}
