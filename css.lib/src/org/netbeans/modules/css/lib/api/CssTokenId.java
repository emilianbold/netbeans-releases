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

import java.util.Arrays;
import org.netbeans.modules.css.lib.nblexer.CssLanguageHierarchy;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

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

    EOF(-1, "others"),
    
    CHARSET_SYM(4, "keyword"),
    STRING(5, "string"),
    SEMI(6, "separator"),
    IMPORT_SYM(7, "keyword"),
    URI(8, "uri"),
    COMMA(9, "separator"),
    MEDIA_SYM(10, "keyword"),
    LBRACE(11, "brace"),
    RBRACE(12, "brace"),
    IDENT(13, "identifier"),
    PAGE_SYM(14, "keyword"),
    COLON(15, "separator"),
    SOLIDUS(16, "others"),
    PLUS(17, "operator"),
    GREATER(18, "operator"),
    TILDE(19, "operator"),
    MINUS(20, "operator"),
    STAR(21, "operator"),
    HASH(22, "hash"),
    DOT(23, "operator"),
    LBRACKET(24, "brace"),
    OPEQ(25, "others"),
    INCLUDES(26, "operator"),
    DASHMATCH(27, "operator"),
    RBRACKET(28, "brace"),
    LPAREN(29, "brace"),
    RPAREN(30, "brace"),
    IMPORTANT_SYM(31, "keyword"),
    NUMBER(32, "number"),
    PERCENTAGE(33, "number"),
    LENGTH(34, "number"),
    EMS(35, "number"),
    EXS(36, "number"),
    ANGLE(37, "number"),
    TIME(38, "number"),
    FREQ(39, "number"),
    HEXCHAR(40, "number"),
    NONASCII(41, "others"),
    UNICODE(42, "others"),
    ESCAPE(43, "others"),
    NMSTART(44, "others"),
    NMCHAR(45, "others"),
    NAME(46, "others"),
    URL(47, "url"),
    A(48, "others"),
    B(49, "others"),
    C(50, "others"),
    D(51, "others"),
    E(52, "others"),
    F(53, "others"),
    G(54, "others"),
    H(55, "others"),
    I(56, "others"),
    J(57, "others"),
    K(58, "others"),
    L(59, "others"),
    M(60, "others"),
    N(61, "others"),
    O(62, "others"),
    P(63, "others"),
    Q(64, "others"),
    R(65, "others"),
    S(66, "others"),
    T(67, "others"),
    U(68, "others"),
    V(69, "others"),
    W(70, "others"),
    X(71, "others"),
    Y(72, "others"),
    Z(73, "others"),
    COMMENT(74, "comment"),
    
    //following two should possibly not be part of the grammar at all
    CDO(75, "others"), //<!--
    CDC(76, "others"), // -->
    
    
    INVALID(77, "others"),
    WS(78, "whitespace"),
    DIMENSION(79, "number"),
    NL(80, "others"), //newline
    T__81(81, "others");  //NOI18N
    
    private static int[] codes = new int[values().length];
    static {
        for(int i = 0; i < values().length; i++) {
            codes[i] = values()[i].code;
        }
    }

    public static  CssTokenId forTokenTypeCode(int tokenTypeCode) {
        int index = Arrays.binarySearch(codes, tokenTypeCode);
        if(index < 0) {
            throw new IllegalArgumentException(String.format("Undefined token type code %s", tokenTypeCode)); //NOI18N
        }
        return values()[index];
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
