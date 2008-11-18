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

package org.netbeans.modules.groovy.editor.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * @author Martin Adamek
 */
public enum GroovyTokenId implements TokenId {

    // update also GroovyLexer.getTokenId(int) if you make changes here
    
    QUOTED_STRING_LITERAL(null, "string"),
    QUOTED_STRING_END(null, "string"),
    QUOTED_STRING_BEGIN(null, "string"),
    EMBEDDED_GROOVY(null, "default"),
    ERROR(null, "error"),
    IDENTIFIER(null, "identifier"),
    CLASS_VAR(null, "staticfield"),
    INSTANCE_VAR(null, "field"),
    GLOBAL_VAR(null, "static"),
    CONSTANT(null, "constant"),
    DOCUMENTATION(null, "comment"),
//    INT_LITERAL(null, "number"),
    REGEXP_LITERAL(null, "regexp"),
//    LONG_LITERAL(null, "number"),
//    FLOAT_LITERAL(null, "number"),
//    DOUBLE_LITERAL(null, "number"),
//    CHAR_LITERAL(null, "character"),
    STRING_LITERAL(null, "string"),
    WHITESPACE(null, "whitespace"),
    EOL(null, "whitespace"),
    LINE_COMMENT(null, "comment"),
    BLOCK_COMMENT(null, "comment"),
    TODO(null, "todo"),
    TYPE_SYMBOL(null, "typesymbol"),
    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    LBRACE("{", "separator"),
    RBRACE("}", "separator"),
    LBRACKET("[", "separator"),
    RBRACKET("]", "separator"),
    STRING_BEGIN(null, "string"),
    STRING_END(null, "string"),
    REGEXP_BEGIN(null, "regexp"), // or separator?
    REGEXP_END(null, "regexp"),
    CHAR_LITERAL_INCOMPLETE(null, "character"),
    STRING_LITERAL_INCOMPLETE(null, "string"),
    BLOCK_COMMENT_INCOMPLETE(null, "comment"),
    JAVADOC_COMMENT_INCOMPLETE(null, "comment"),
    INVALID_COMMENT_END("*/", "error"),
    FLOAT_LITERAL_INVALID(null, "number"),
    ANY_KEYWORD(null, "keyword"),
    ANY_OPERATOR(null, "operator"),

    ABSTRACT(null, "keyword"),
    ANNOTATION_ARRAY_INIT(null, "annotation"),
    ANNOTATION_DEF(null, "annotation"),
    ANNOTATION_FIELD_DEF(null, "annotation"),
    ANNOTATION_MEMBER_VALUE_PAIR(null, "annotation"),
    ANNOTATION(null, "annotation"),
    ANNOTATIONS(null, "annotation"),
    ARRAY_DECLARATOR(null, "default"),
    ASSIGN(null, "operator"),
    AT(null, "operator"),
    BAND_ASSIGN(null, "operator"),
    BAND(null, "operator"),
    BIG_SUFFIX(null, "number"),
    BLOCK(null, "default"),
    BNOT(null, "operator"),
    BOR_ASSIGN(null, "operator"),
    BOR(null, "operator"),
    BSR_ASSIGN(null, "operator"),
    BSR(null, "operator"),
    BXOR_ASSIGN(null, "operator"),
    BXOR(null, "operator"),
    CASE_GROUP(null, "default"),
    CLASS_DEF(null, "default"),
    CLOSED_BLOCK(null, "default"),
    CLOSURE_OP(null, "operator"),
    COLON(":", "operator"),
    COMMA(null, "operator"),
    COMPARE_TO(null, "operator"),
    CTOR_CALL(null, "default"),
    CTOR_IDENT(null, "default"),
    DEC(null, "operator"),
    DIGIT(null, "number"),
    DIV_ASSIGN(null, "operator"),
    DIV(null, "operator"),
    DOLLAR(null, "operator"),
    DOT(".", "operator"),
    DYNAMIC_MEMBER(null, "default"),
    ELIST(null, "default"),
    EMPTY_STAT(null, "default"),
    ENUM_CONSTANT_DEF(null, "default"),
    ENUM_DEF(null, "default"),
    EOF(null, "default"),
    EQUAL(null, "operator"),
    ESC(null, "default"),
    EXPONENT(null, "number"),
    EXPR(null, "default"),
    EXTENDS_CLAUSE(null, "default"),
    FINAL(null, "keyword"),
    FLOAT_SUFFIX(null, "number"),
    FOR_CONDITION(null, "default"),
    FOR_EACH_CLAUSE(null, "keyword"),
    FOR_IN_ITERABLE(null, "default"),
    FOR_INIT(null, "default"),
    FOR_ITERATOR(null, "default"),
    GE(null, "operator"),
    GT(null, "operator"),
    HEX_DIGIT(null, "number"),
    IMPLEMENTS_CLAUSE(null, "default"),
    IMPLICIT_PARAMETERS(null, "default"),
    IMPORT(null, "default"),
    INC(null, "operator"),
    INDEX_OP(null, "default"),
    INSTANCE_INIT(null, "default"),
    INTERFACE_DEF(null, "default"),
    LABELED_ARG(null, "default"),
    LABELED_STAT(null, "default"),
    LAND(null, "operator"),
    LE(null, "operator"),
    LETTER(null, "default"),
    LIST_CONSTRUCTOR(null, "default"),
    
    LITERAL_as(null, "keyword"),
    LITERAL_assert(null, "keyword"),
    LITERAL_boolean(null, "keyword"),
    LITERAL_break(null, "keyword"),
    LITERAL_byte(null, "keyword"),
    LITERAL_case(null, "keyword"),
    LITERAL_catch(null, "keyword"),
    LITERAL_class(null, "keyword"),
    LITERAL_continue(null, "keyword"),
    LITERAL_def(null, "keyword"),
    LITERAL_default(null, "keyword"),
    LITERAL_double(null, "keyword"),
    LITERAL_else(null, "keyword"),
    LITERAL_enum(null, "keyword"),
    LITERAL_extends(null, "keyword"),
    LITERAL_false(null, "keyword"),
    LITERAL_finally(null, "keyword"),
    LITERAL_float(null, "keyword"),
    LITERAL_for(null, "keyword"),
    LITERAL_char(null, "keyword"),
    LITERAL_if(null, "keyword"),
    LITERAL_implements(null, "keyword"),
    LITERAL_import(null, "keyword"),
    LITERAL_in(null, "keyword"),
    LITERAL_instanceof(null, "keyword"),
    LITERAL_int(null, "keyword"),
    LITERAL_interface(null, "keyword"),
    LITERAL_long(null, "keyword"),
    LITERAL_native(null, "keyword"),
    LITERAL_new(null, "keyword"),
    LITERAL_null(null, "keyword"),
    LITERAL_package(null, "keyword"),
    LITERAL_private(null, "keyword"),
    LITERAL_protected(null, "keyword"),
    LITERAL_public(null, "keyword"),
    LITERAL_return(null, "keyword"),
    LITERAL_short(null, "keyword"),
    LITERAL_static(null, "keyword"),
    LITERAL_super(null, "keyword"),
    LITERAL_switch(null, "keyword"),
    LITERAL_synchronized(null, "keyword"),
    LITERAL_this(null, "keyword"),
    LITERAL_threadsafe(null, "keyword"),
    LITERAL_throw(null, "keyword"),
    LITERAL_throws(null, "keyword"),
    LITERAL_transient(null, "keyword"),
    LITERAL_true(null, "keyword"),
    LITERAL_try(null, "keyword"),
    LITERAL_void(null, "keyword"),
    LITERAL_volatile(null, "keyword"),
    LITERAL_while(null, "keyword"),
    
    LNOT(null, "operator"),
    LOR(null, "operator"),
    LT(null, "operator"),
    MAP_CONSTRUCTOR(null, "default"),
    MEMBER_POINTER(null, "operator"),
    METHOD_CALL(null, "default"),
    METHOD_DEF(null, "default"),
    MINUS_ASSIGN(null, "operator"),
    MINUS(null, "operator"),
    MOD_ASSIGN(null, "operator"),
    MOD(null, "operator"),
    MODIFIERS(null, "default"),
    NLS(null, "default"),
    NOT_EQUAL(null, "operator"),
    NULL_TREE_LOOKAHEAD(null, "default"),
    NUM_BIG_DECIMAL(null, "number"),
    NUM_BIG_INT(null, "number"),
    NUM_DOUBLE(null, "number"),
    NUM_FLOAT(null, "number"),
    NUM_INT(null, "number"),
    NUM_LONG(null, "number"),
    OBJBLOCK(null, "default"),
    ONE_NL(null, "default"),
    OPTIONAL_DOT(null, "operator"),
    PACKAGE_DEF(null, "default"),
    PARAMETER_DEF(null, "default"),
    PARAMETERS(null, "default"),
    PLUS_ASSIGN(null, "operator"),
    PLUS(null, "operator"),
    POST_DEC(null, "default"),
    POST_INC(null, "default"),
    QUESTION(null, "operator"),
    RANGE_EXCLUSIVE(null, "operator"),
    RANGE_INCLUSIVE(null, "operator"),
    REGEX_FIND(null, "operator"),
    REGEX_MATCH(null, "operator"),
    REGEXP_SYMBOL(null, "default"),
    SELECT_SLOT(null, "default"),
    SEMI(null, "operator"),
    SH_COMMENT(null, "comment"),
    SL_ASSIGN(null, "operator"),
    SL_COMMENT(null, "comment"),
    SL(null, "operator"),
    SLIST(null, "default"),
    SPREAD_ARG(null, "default"),
    SPREAD_DOT(null, "operator"),
    SPREAD_MAP_ARG(null, "default"),
    SR_ASSIGN(null, "operator"),
    SR(null, "operator"),
    STAR_ASSIGN(null, "operator"),
    STAR_STAR_ASSIGN(null, "operator"),
    STAR_STAR(null, "operator"),
    STAR(null, "operator"),
    STATIC_IMPORT(null, "default"),
    STATIC_INIT(null, "default"),
    STRICTFP(null, "default"),
    STRING_CONSTRUCTOR(null, "string"),
    STRING_CTOR_END(null, "string"),
    STRING_CTOR_MIDDLE(null, "string"),
    STRING_CTOR_START(null, "string"),
    STRING_CH(null, "string"),
    STRING_NL(null, "string"),
    SUPER_CTOR_CALL(null, "default"),
    TRIPLE_DOT(null, "operator"),
    TYPE_ARGUMENT(null, "default"),
    TYPE_ARGUMENTS(null, "default"),
    TYPE_LOWER_BOUNDS(null, "default"),
    TYPE_PARAMETER(null, "default"),
    TYPE_PARAMETERS(null, "default"),
    TYPE_UPPER_BOUNDS(null, "default"),
    TYPE(null, "default"),
    TYPECAST(null, "default"),
    UNARY_MINUS(null, "default"),
    UNARY_PLUS(null, "default"),
    UNUSED_CONST(null, "default"),
    UNUSED_DO(null, "default"),
    UNUSED_GOTO(null, "default"),
    VARIABLE_DEF(null, "default"),
    VARIABLE_PARAMETER_DEF(null, "default"),
    VOCAB(null, "default"),
    WILDCARD_TYPE(null, "default"),
    
    // Non-unary operators which indicate a line continuation if used at the end of a line
    NONUNARY_OP(null, "operator");
    
    /**
     * MIME type for Groovy. Don't change this without also consulting the various XML files
     * that cannot reference this value directly.
     */
    public static final String GROOVY_MIME_TYPE = "text/x-groovy"; // NOI18N

    private final String primaryCategory;
    private final String fixedText;

    GroovyTokenId(String fixedText, String primaryCategory) {
        this.primaryCategory = primaryCategory;
        this.fixedText = fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    private static final Language<GroovyTokenId> language =
        new LanguageHierarchy<GroovyTokenId>() {

            protected String mimeType() {
                return GroovyTokenId.GROOVY_MIME_TYPE;
            }

            protected Collection<GroovyTokenId> createTokenIds() {
                return EnumSet.allOf(GroovyTokenId.class);
            }

            @Override
            protected Map<String, Collection<GroovyTokenId>> createTokenCategories() {
                Map<String, Collection<GroovyTokenId>> cats = new HashMap<String, Collection<GroovyTokenId>>();
                return cats;
            }

            protected Lexer<GroovyTokenId> createLexer(LexerRestartInfo<GroovyTokenId> info) {
                return new GroovyLexer(info);
            }

        }.language();

    public static Language<GroovyTokenId> language() {
        return language;
    }

}
