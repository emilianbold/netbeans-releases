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

package org.netbeans.modules.groovy.editor.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * @todo check ERROR_INT definition
 * 
 * @author Martin Adamek
 */
public class GroovyTokenId extends GsfTokenId {

    public static final int ERROR_INT = 221;
    
    public static final GsfTokenId QUOTED_STRING_LITERAL = new GsfTokenId("QUOTED_STRING_LITERAL", null, "string");
    public static final GsfTokenId QUOTED_STRING_END = new GsfTokenId("QUOTED_STRING_END", null, "string");
    public static final GsfTokenId QUOTED_STRING_BEGIN = new GsfTokenId("QUOTED_STRING_BEGIN", null, "string");
    public static final GsfTokenId EMBEDDED_GROOVY = new GsfTokenId("EMBEDDED_GROOVY", null, "default");
    
    public static TokenId NONUNARY_OP;

    // <editor-fold defaultstate="collapsed" desc="Tokens">
    public static final GsfTokenId ABSTRACT = new GsfTokenId("ABSTRACT", null, "keyword");
    public static final GsfTokenId ANNOTATION_ARRAY_INIT = new GsfTokenId("ANNOTATION_ARRAY_INIT", null, "annotation");
    public static final GsfTokenId ANNOTATION_DEF = new GsfTokenId("ANNOTATION_DEF", null, "annotation");
    public static final GsfTokenId ANNOTATION_FIELD_DEF = new GsfTokenId("ANNOTATION_FIELD_DEF", null, "annotation");
    public static final GsfTokenId ANNOTATION_MEMBER_VALUE_PAIR = new GsfTokenId("ANNOTATION_MEMBER_VALUE_PAIR", null, "annotation");
    public static final GsfTokenId ANNOTATION = new GsfTokenId("ANNOTATION", null, "annotation");
    public static final GsfTokenId ANNOTATIONS = new GsfTokenId("ANNOTATIONS", null, "annotation");
    public static final GsfTokenId ARRAY_DECLARATOR = new GsfTokenId("ARRAY_DECLARATOR", null, "default");
    public static final GsfTokenId ASSIGN = new GsfTokenId("ASSIGN", null, "operator");
    public static final GsfTokenId AT = new GsfTokenId("AT", null, "operator");
    public static final GsfTokenId BAND_ASSIGN = new GsfTokenId("BAND_ASSIGN", null, "operator");
    public static final GsfTokenId BAND = new GsfTokenId("BAND", null, "operator");
    public static final GsfTokenId BIG_SUFFIX = new GsfTokenId("BIG_SUFFIX", null, "number");
    public static final GsfTokenId BLOCK = new GsfTokenId("BLOCK", null, "default");
    public static final GsfTokenId BNOT = new GsfTokenId("BNOT", null, "operator");
    public static final GsfTokenId BOR_ASSIGN = new GsfTokenId("BOR_ASSIGN", null, "operator");
    public static final GsfTokenId BOR = new GsfTokenId("BOR", null, "operator");
    public static final GsfTokenId BSR_ASSIGN = new GsfTokenId("BSR_ASSIGN", null, "operator");
    public static final GsfTokenId BSR = new GsfTokenId("BSR", null, "operator");
    public static final GsfTokenId BXOR_ASSIGN = new GsfTokenId("BXOR_ASSIGN", null, "operator");
    public static final GsfTokenId BXOR = new GsfTokenId("BXOR", null, "operator");
    public static final GsfTokenId CASE_GROUP = new GsfTokenId("CASE_GROUP", null, "default");
    public static final GsfTokenId CLASS_DEF = new GsfTokenId("CLASS_DEF", null, "default");
    public static final GsfTokenId CLOSABLE_BLOCK = new GsfTokenId("CLOSABLE_BLOCK", null, "default");
    public static final GsfTokenId CLOSABLE_BLOCK_OP = new GsfTokenId("CLOSABLE_BLOCK_OP", null, "operator");
    public static final GsfTokenId CLOSURE_LIST = new GsfTokenId("CLOSURE_LIST", null, "operator");
    public static final GsfTokenId COLON = new GsfTokenId("COLON", null, "operator");
    public static final GsfTokenId COMMA = new GsfTokenId("COMMA", null, "operator");
    public static final GsfTokenId COMPARE_TO = new GsfTokenId("COMPARE_TO", null, "operator");
    public static final GsfTokenId CTOR_CALL = new GsfTokenId("CTOR_CALL", null, "default");
    public static final GsfTokenId CTOR_IDENT = new GsfTokenId("CTOR_IDENT", null, "default");
    public static final GsfTokenId DEC = new GsfTokenId("DEC", null, "operator");
    public static final GsfTokenId DIGIT = new GsfTokenId("DIGIT", null, "number");
    public static final GsfTokenId DIV_ASSIGN = new GsfTokenId("DIV_ASSIGN", null, "operator");
    public static final GsfTokenId DIV = new GsfTokenId("DIV", null, "operator");
    public static final GsfTokenId DOLLAR = new GsfTokenId("DOLLAR", null, "operator");
    public static final GsfTokenId DOT = new GsfTokenId("DOT", null, "operator");
    public static final GsfTokenId DYNAMIC_MEMBER = new GsfTokenId("DYNAMIC_MEMBER", null, "default");
    public static final GsfTokenId ELIST = new GsfTokenId("ELIST", null, "default");
    public static final GsfTokenId ELVIS_OPERATOR = new GsfTokenId("ELVIS_OPERATOR", null, "operator");
    public static final GsfTokenId EMPTY_STAT = new GsfTokenId("EMPTY_STAT", null, "default");
    public static final GsfTokenId ENUM_CONSTANT_DEF = new GsfTokenId("ENUM_CONSTANT_DEF", null, "default");
    public static final GsfTokenId ENUM_DEF = new GsfTokenId("ENUM_DEF", null, "default");
    public static final GsfTokenId EOF = new GsfTokenId("EOF", null, "default");
    public static final GsfTokenId EQUAL = new GsfTokenId("EQUAL", null, "operator");
    public static final GsfTokenId ESC = new GsfTokenId("ESC", null, "default");
    public static final GsfTokenId EXPONENT = new GsfTokenId("EXPONENT", null, "number");
    public static final GsfTokenId EXPR = new GsfTokenId("EXPR", null, "default");
    public static final GsfTokenId EXTENDS_CLAUSE = new GsfTokenId("EXTENDS_CLAUSE", null, "default");
    public static final GsfTokenId FINAL = new GsfTokenId("FINAL", null, "keyword");
    public static final GsfTokenId FLOAT_SUFFIX = new GsfTokenId("FLOAT_SUFFIX", null, "number");
    public static final GsfTokenId FOR_CONDITION = new GsfTokenId("FOR_CONDITION", null, "default");
    public static final GsfTokenId FOR_EACH_CLAUSE = new GsfTokenId("FOR_EACH_CLAUSE", null, "keyword");
    public static final GsfTokenId FOR_IN_ITERABLE = new GsfTokenId("FOR_IN_ITERABLE", null, "default");
    public static final GsfTokenId FOR_INIT = new GsfTokenId("FOR_INIT", null, "default");
    public static final GsfTokenId FOR_ITERATOR = new GsfTokenId("FOR_ITERATOR", null, "default");
    public static final GsfTokenId GE = new GsfTokenId("GE", null, "operator");
    public static final GsfTokenId GT = new GsfTokenId("GT", null, "operator");
    public static final GsfTokenId HEX_DIGIT = new GsfTokenId("HEX_DIGIT", null, "number");
    public static final GsfTokenId IDENT = new GsfTokenId("IDENT", null, "default");
    public static final GsfTokenId IMPLEMENTS_CLAUSE = new GsfTokenId("IMPLEMENTS_CLAUSE", null, "default");
    public static final GsfTokenId IMPLICIT_PARAMETERS = new GsfTokenId("IMPLICIT_PARAMETERS", null, "default");
    public static final GsfTokenId IMPORT = new GsfTokenId("IMPORT", null, "default");
    public static final GsfTokenId INC = new GsfTokenId("INC", null, "operator");
    public static final GsfTokenId INDEX_OP = new GsfTokenId("INDEX_OP", null, "default");
    public static final GsfTokenId INSTANCE_INIT = new GsfTokenId("INSTANCE_INIT", null, "default");
    public static final GsfTokenId INTERFACE_DEF = new GsfTokenId("INTERFACE_DEF", null, "default");
    public static final GsfTokenId LABELED_ARG = new GsfTokenId("LABELED_ARG", null, "default");
    public static final GsfTokenId LABELED_STAT = new GsfTokenId("LABELED_STAT", null, "default");
    public static final GsfTokenId LAND = new GsfTokenId("LAND", null, "operator");
    public static final GsfTokenId LBRACK = new GsfTokenId("LBRACK", null, "operator");
    public static final GsfTokenId LCURLY = new GsfTokenId("LCURLY", null, "operator");
    public static final GsfTokenId LE = new GsfTokenId("LE", null, "operator");
    public static final GsfTokenId LETTER = new GsfTokenId("LETTER", null, "default");
    public static final GsfTokenId LIST_CONSTRUCTOR = new GsfTokenId("LIST_CONSTRUCTOR", null, "default");
    public static final GsfTokenId LITERAL_as = new GsfTokenId("LITERAL_as", null, "keyword");
    public static final GsfTokenId LITERAL_assert = new GsfTokenId("LITERAL_assert", null, "keyword");
    public static final GsfTokenId LITERAL_boolean = new GsfTokenId("LITERAL_boolean", null, "keyword");
    public static final GsfTokenId LITERAL_break = new GsfTokenId("LITERAL_break", null, "keyword");
    public static final GsfTokenId LITERAL_byte = new GsfTokenId("LITERAL_byte", null, "keyword");
    public static final GsfTokenId LITERAL_case = new GsfTokenId("LITERAL_case", null, "keyword");
    public static final GsfTokenId LITERAL_catch = new GsfTokenId("LITERAL_catch", null, "keyword");
    public static final GsfTokenId LITERAL_class = new GsfTokenId("LITERAL_class", null, "keyword");
    public static final GsfTokenId LITERAL_continue = new GsfTokenId("LITERAL_continue", null, "keyword");
    public static final GsfTokenId LITERAL_def = new GsfTokenId("LITERAL_def", null, "keyword");
    public static final GsfTokenId LITERAL_default = new GsfTokenId("LITERAL_default", null, "keyword");
    public static final GsfTokenId LITERAL_double = new GsfTokenId("LITERAL_double", null, "keyword");
    public static final GsfTokenId LITERAL_else = new GsfTokenId("LITERAL_else", null, "keyword");
    public static final GsfTokenId LITERAL_enum = new GsfTokenId("LITERAL_enum", null, "keyword");
    public static final GsfTokenId LITERAL_extends = new GsfTokenId("LITERAL_extends", null, "keyword");
    public static final GsfTokenId LITERAL_false = new GsfTokenId("LITERAL_false", null, "keyword");
    public static final GsfTokenId LITERAL_finally = new GsfTokenId("LITERAL_finally", null, "keyword");
    public static final GsfTokenId LITERAL_float = new GsfTokenId("LITERAL_float", null, "keyword");
    public static final GsfTokenId LITERAL_for = new GsfTokenId("LITERAL_for", null, "keyword");
    public static final GsfTokenId LITERAL_char = new GsfTokenId("LITERAL_char", null, "keyword");
    public static final GsfTokenId LITERAL_if = new GsfTokenId("LITERAL_if", null, "keyword");
    public static final GsfTokenId LITERAL_implements = new GsfTokenId("LITERAL_implements", null, "keyword");
    public static final GsfTokenId LITERAL_import = new GsfTokenId("LITERAL_import", null, "keyword");
    public static final GsfTokenId LITERAL_in = new GsfTokenId("LITERAL_in", null, "keyword");
    public static final GsfTokenId LITERAL_instanceof = new GsfTokenId("LITERAL_instanceof", null, "keyword");
    public static final GsfTokenId LITERAL_int = new GsfTokenId("LITERAL_int", null, "keyword");
    public static final GsfTokenId LITERAL_interface = new GsfTokenId("LITERAL_interface", null, "keyword");
    public static final GsfTokenId LITERAL_long = new GsfTokenId("LITERAL_long", null, "keyword");
    public static final GsfTokenId LITERAL_native = new GsfTokenId("LITERAL_native", null, "keyword");
    public static final GsfTokenId LITERAL_new = new GsfTokenId("LITERAL_new", null, "keyword");
    public static final GsfTokenId LITERAL_null = new GsfTokenId("LITERAL_null", null, "keyword");
    public static final GsfTokenId LITERAL_package = new GsfTokenId("LITERAL_package", null, "keyword");
    public static final GsfTokenId LITERAL_private = new GsfTokenId("LITERAL_private", null, "keyword");
    public static final GsfTokenId LITERAL_protected = new GsfTokenId("LITERAL_protected", null, "keyword");
    public static final GsfTokenId LITERAL_public = new GsfTokenId("LITERAL_public", null, "keyword");
    public static final GsfTokenId LITERAL_return = new GsfTokenId("LITERAL_return", null, "keyword");
    public static final GsfTokenId LITERAL_short = new GsfTokenId("LITERAL_short", null, "keyword");
    public static final GsfTokenId LITERAL_static = new GsfTokenId("LITERAL_static", null, "keyword");
    public static final GsfTokenId LITERAL_super = new GsfTokenId("LITERAL_super", null, "keyword");
    public static final GsfTokenId LITERAL_switch = new GsfTokenId("LITERAL_switch", null, "keyword");
    public static final GsfTokenId LITERAL_synchronized = new GsfTokenId("LITERAL_synchronized", null, "keyword");
    public static final GsfTokenId LITERAL_this = new GsfTokenId("LITERAL_this", null, "keyword");
    public static final GsfTokenId LITERAL_threadsafe = new GsfTokenId("LITERAL_threadsafe", null, "keyword");
    public static final GsfTokenId LITERAL_throw = new GsfTokenId("LITERAL_throw", null, "keyword");
    public static final GsfTokenId LITERAL_throws = new GsfTokenId("LITERAL_throws", null, "keyword");
    public static final GsfTokenId LITERAL_transient = new GsfTokenId("LITERAL_transient", null, "keyword");
    public static final GsfTokenId LITERAL_true = new GsfTokenId("LITERAL_true", null, "keyword");
    public static final GsfTokenId LITERAL_try = new GsfTokenId("LITERAL_try", null, "keyword");
    public static final GsfTokenId LITERAL_void = new GsfTokenId("LITERAL_void", null, "keyword");
    public static final GsfTokenId LITERAL_volatile = new GsfTokenId("LITERAL_volatile", null, "keyword");
    public static final GsfTokenId LITERAL_while = new GsfTokenId("LITERAL_while", null, "keyword");
    public static final GsfTokenId LNOT = new GsfTokenId("LNOT", null, "operator");
    public static final GsfTokenId LOR = new GsfTokenId("LOR", null, "operator");
    public static final GsfTokenId LPAREN = new GsfTokenId("LPAREN", null, "operator");
    public static final GsfTokenId LT = new GsfTokenId("LT", null, "operator");
    public static final GsfTokenId MAP_CONSTRUCTOR = new GsfTokenId("MAP_CONSTRUCTOR", null, "default");
    public static final GsfTokenId MEMBER_POINTER = new GsfTokenId("MEMBER_POINTER", null, "operator");
    public static final GsfTokenId METHOD_CALL = new GsfTokenId("METHOD_CALL", null, "default");
    public static final GsfTokenId METHOD_DEF = new GsfTokenId("METHOD_DEF", null, "default");
    public static final GsfTokenId MINUS_ASSIGN = new GsfTokenId("MINUS_ASSIGN", null, "operator");
    public static final GsfTokenId MINUS = new GsfTokenId("MINUS", null, "operator");
    public static final GsfTokenId ML_COMMENT = new GsfTokenId("ML_COMMENT", null, "comment");
    public static final GsfTokenId MOD_ASSIGN = new GsfTokenId("MOD_ASSIGN", null, "operator");
    public static final GsfTokenId MOD = new GsfTokenId("MOD", null, "operator");
    public static final GsfTokenId MODIFIERS = new GsfTokenId("MODIFIERS", null, "default");
    public static final GsfTokenId NLS = new GsfTokenId("NLS", null, "default");
    public static final GsfTokenId NOT_EQUAL = new GsfTokenId("NOT_EQUAL", null, "operator");
    public static final GsfTokenId NULL_TREE_LOOKAHEAD = new GsfTokenId("NULL_TREE_LOOKAHEAD", null, "default");
    public static final GsfTokenId NUM_BIG_DECIMAL = new GsfTokenId("NUM_BIG_DECIMAL", null, "number");
    public static final GsfTokenId NUM_BIG_INT = new GsfTokenId("NUM_BIG_INT", null, "number");
    public static final GsfTokenId NUM_DOUBLE = new GsfTokenId("NUM_DOUBLE", null, "number");
    public static final GsfTokenId NUM_FLOAT = new GsfTokenId("NUM_FLOAT", null, "number");
    public static final GsfTokenId NUM_INT = new GsfTokenId("NUM_INT", null, "number");
    public static final GsfTokenId NUM_LONG = new GsfTokenId("NUM_LONG", null, "number");
    public static final GsfTokenId OBJBLOCK = new GsfTokenId("OBJBLOCK", null, "default");
    public static final GsfTokenId ONE_NL = new GsfTokenId("ONE_NL", null, "default");
    public static final GsfTokenId OPTIONAL_DOT = new GsfTokenId("OPTIONAL_DOT", null, "operator");
    public static final GsfTokenId PACKAGE_DEF = new GsfTokenId("PACKAGE_DEF", null, "default");
    public static final GsfTokenId PARAMETER_DEF = new GsfTokenId("PARAMETER_DEF", null, "default");
    public static final GsfTokenId PARAMETERS = new GsfTokenId("PARAMETERS", null, "default");
    public static final GsfTokenId PLUS_ASSIGN = new GsfTokenId("PLUS_ASSIGN", null, "operator");
    public static final GsfTokenId PLUS = new GsfTokenId("PLUS", null, "operator");
    public static final GsfTokenId POST_DEC = new GsfTokenId("POST_DEC", null, "default");
    public static final GsfTokenId POST_INC = new GsfTokenId("POST_INC", null, "default");
    public static final GsfTokenId QUESTION = new GsfTokenId("QUESTION", null, "operator");
    public static final GsfTokenId RANGE_EXCLUSIVE = new GsfTokenId("RANGE_EXCLUSIVE", null, "operator");
    public static final GsfTokenId RANGE_INCLUSIVE = new GsfTokenId("RANGE_INCLUSIVE", null, "operator");
    public static final GsfTokenId RBRACK = new GsfTokenId("RBRACK", null, "operator");
    public static final GsfTokenId RCURLY = new GsfTokenId("RCURLY", null, "operator");
    public static final GsfTokenId REGEX_FIND = new GsfTokenId("REGEX_FIND", null, "operator");
    public static final GsfTokenId REGEX_MATCH = new GsfTokenId("REGEX_MATCH", null, "operator");
    public static final GsfTokenId REGEXP_CTOR_END = new GsfTokenId("REGEXP_CTOR_END", null, "default");
    public static final GsfTokenId REGEXP_LITERAL = new GsfTokenId("REGEXP_LITERAL", null, "default");
    public static final GsfTokenId REGEXP_SYMBOL = new GsfTokenId("REGEXP_SYMBOL", null, "default");
    public static final GsfTokenId RPAREN = new GsfTokenId("RPAREN", null, "operator");
    public static final GsfTokenId SELECT_SLOT = new GsfTokenId("SELECT_SLOT", null, "default");
    public static final GsfTokenId SEMI = new GsfTokenId("SEMI", null, "operator");
    public static final GsfTokenId SH_COMMENT = new GsfTokenId("SH_COMMENT", null, "comment");
    public static final GsfTokenId SL_ASSIGN = new GsfTokenId("SL_ASSIGN", null, "operator");
    public static final GsfTokenId SL_COMMENT = new GsfTokenId("SL_COMMENT", null, "comment");
    public static final GsfTokenId SL = new GsfTokenId("SL", null, "operator");
    public static final GsfTokenId SLIST = new GsfTokenId("SLIST", null, "default");
    public static final GsfTokenId SPREAD_ARG = new GsfTokenId("SPREAD_ARG", null, "default");
    public static final GsfTokenId SPREAD_DOT = new GsfTokenId("SPREAD_DOT", null, "operator");
    public static final GsfTokenId SPREAD_MAP_ARG = new GsfTokenId("SPREAD_MAP_ARG", null, "default");
    public static final GsfTokenId SR_ASSIGN = new GsfTokenId("SR_ASSIGN", null, "operator");
    public static final GsfTokenId SR = new GsfTokenId("SR", null, "operator");
    public static final GsfTokenId STAR_ASSIGN = new GsfTokenId("STAR_ASSIGN", null, "operator");
    public static final GsfTokenId STAR_STAR_ASSIGN = new GsfTokenId("STAR_STAR_ASSIGN", null, "operator");
    public static final GsfTokenId STAR_STAR = new GsfTokenId("STAR_STAR", null, "operator");
    public static final GsfTokenId STAR = new GsfTokenId("STAR", null, "operator");
    public static final GsfTokenId STATIC_IMPORT = new GsfTokenId("STATIC_IMPORT", null, "default");
    public static final GsfTokenId STATIC_INIT = new GsfTokenId("STATIC_INIT", null, "default");
    public static final GsfTokenId STRICTFP = new GsfTokenId("STRICTFP", null, "default");
    public static final GsfTokenId STRING_CONSTRUCTOR = new GsfTokenId("STRING_CONSTRUCTOR", null, "string");
    public static final GsfTokenId STRING_CTOR_END = new GsfTokenId("STRING_CTOR_END", null, "string");
    public static final GsfTokenId STRING_CTOR_MIDDLE = new GsfTokenId("STRING_CTOR_MIDDLE", null, "string");
    public static final GsfTokenId STRING_CTOR_START = new GsfTokenId("STRING_CTOR_START", null, "string");
    public static final GsfTokenId STRING_CH = new GsfTokenId("STRING_CH", null, "string");
    public static final GsfTokenId STRING_LITERAL = new GsfTokenId("STRING_LITERAL", null, "string");
    public static final GsfTokenId STRING_NL = new GsfTokenId("STRING_NL", null, "string");
    public static final GsfTokenId SUPER_CTOR_CALL = new GsfTokenId("SUPER_CTOR_CALL", null, "default");
    public static final GsfTokenId TRIPLE_DOT = new GsfTokenId("TRIPLE_DOT", null, "operator");
    public static final GsfTokenId TYPE_ARGUMENT = new GsfTokenId("TYPE_ARGUMENT", null, "default");
    public static final GsfTokenId TYPE_ARGUMENTS = new GsfTokenId("TYPE_ARGUMENTS", null, "default");
    public static final GsfTokenId TYPE_LOWER_BOUNDS = new GsfTokenId("TYPE_LOWER_BOUNDS", null, "default");
    public static final GsfTokenId TYPE_PARAMETER = new GsfTokenId("TYPE_PARAMETER", null, "default");
    public static final GsfTokenId TYPE_PARAMETERS = new GsfTokenId("TYPE_PARAMETERS", null, "default");
    public static final GsfTokenId TYPE_UPPER_BOUNDS = new GsfTokenId("TYPE_UPPER_BOUNDS", null, "default");
    public static final GsfTokenId TYPE = new GsfTokenId("TYPE", null, "default");
    public static final GsfTokenId TYPECAST = new GsfTokenId("TYPECAST", null, "default");
    public static final GsfTokenId UNARY_MINUS = new GsfTokenId("UNARY_MINUS", null, "default");
    public static final GsfTokenId UNARY_PLUS = new GsfTokenId("UNARY_PLUS", null, "default");
    public static final GsfTokenId UNUSED_CONST = new GsfTokenId("UNUSED_CONST", null, "default");
    public static final GsfTokenId UNUSED_DO = new GsfTokenId("UNUSED_DO", null, "default");
    public static final GsfTokenId UNUSED_GOTO = new GsfTokenId("UNUSED_GOTO", null, "default");
    public static final GsfTokenId VARIABLE_DEF = new GsfTokenId("VARIABLE_DEF", null, "default");
    public static final GsfTokenId VARIABLE_PARAMETER_DEF = new GsfTokenId("VARIABLE_PARAMETER_DEF", null, "default");
    public static final GsfTokenId VOCAB = new GsfTokenId("VOCAB", null, "default");
    public static final GsfTokenId WILDCARD_TYPE = new GsfTokenId("WILDCARD_TYPE", null, "default");
    public static final GsfTokenId WS = new GsfTokenId("WS", null, "default");
    public static final GsfTokenId WHITESPACE = new GsfTokenId("WHITESPACE", null, "default");
    public static final GsfTokenId ERROR = new GsfTokenId("ERROR", null, "default");
    // </editor-fold>
    
    private static final Language<GsfTokenId> language =
        new LanguageHierarchy<GsfTokenId>() {

            protected String mimeType() {
                return "text/x-groovy"; // NOI18N
            }

            protected Collection<GsfTokenId> createTokenIds() {
                return getUsedTokens();
            }

            @Override
            protected Map<String, Collection<GsfTokenId>> createTokenCategories() {
                Map<String, Collection<GsfTokenId>> cats = new HashMap<String, Collection<GsfTokenId>>();
                return cats;
            }

            protected Lexer<GsfTokenId> createLexer(LexerRestartInfo<GsfTokenId> info) {
                return new GroovyLexer(info);
            }

        }.language();

    public GroovyTokenId(String name, String fixedText, String primaryCategory) {
        super(name, fixedText, primaryCategory);
    }

    public static Language<GsfTokenId> language() {
        return language;
    }

    public static List<GsfTokenId> getUsedTokens() {
        List<GsfTokenId> types = new ArrayList<GsfTokenId>();
        types.add(ABSTRACT);
        types.add(ANNOTATION_ARRAY_INIT);
        types.add(ANNOTATION_DEF);
        types.add(ANNOTATION_FIELD_DEF);
        types.add(ANNOTATION_MEMBER_VALUE_PAIR);
        types.add(ANNOTATION);
        types.add(ANNOTATIONS);
        types.add(ARRAY_DECLARATOR);
        types.add(ASSIGN);
        types.add(AT);
        types.add(BAND_ASSIGN);
        types.add(BAND);
        types.add(BIG_SUFFIX);
        types.add(BLOCK);
        types.add(BNOT);
        types.add(BOR_ASSIGN);
        types.add(BOR);
        types.add(BSR_ASSIGN);
        types.add(BSR);
        types.add(BXOR_ASSIGN);
        types.add(BXOR);
        types.add(CASE_GROUP);
        types.add(CLASS_DEF);
        types.add(CLOSABLE_BLOCK);
        types.add(CLOSABLE_BLOCK_OP);
        types.add(CLOSURE_LIST);
        types.add(COLON);
        types.add(COMMA);
        types.add(COMPARE_TO);
        types.add(CTOR_CALL);
        types.add(CTOR_IDENT);
        types.add(DEC);
        types.add(DIGIT);
        types.add(DIV_ASSIGN);
        types.add(DIV);
        types.add(DOLLAR);
        types.add(DOT);
        types.add(DYNAMIC_MEMBER);
        types.add(ELIST);
        types.add(ELVIS_OPERATOR);
        types.add(EMPTY_STAT);
        types.add(ENUM_CONSTANT_DEF);
        types.add(ENUM_DEF);
        types.add(EOF);
        types.add(EQUAL);
        types.add(ESC);
        types.add(EXPONENT);
        types.add(EXPR);
        types.add(EXTENDS_CLAUSE);
        types.add(FINAL);
        types.add(FLOAT_SUFFIX);
        types.add(FOR_CONDITION);
        types.add(FOR_EACH_CLAUSE);
        types.add(FOR_IN_ITERABLE);
        types.add(FOR_INIT);
        types.add(FOR_ITERATOR);
        types.add(GE);
        types.add(GT);
        types.add(HEX_DIGIT);
        types.add(IDENT);
        types.add(IMPLEMENTS_CLAUSE);
        types.add(IMPLICIT_PARAMETERS);
        types.add(IMPORT);
        types.add(INC);
        types.add(INDEX_OP);
        types.add(INSTANCE_INIT);
        types.add(INTERFACE_DEF);
        types.add(LABELED_ARG);
        types.add(LABELED_STAT);
        types.add(LAND);
        types.add(LBRACK);
        types.add(LCURLY);
        types.add(LE);
        types.add(LETTER);
        types.add(LIST_CONSTRUCTOR);
        types.add(LITERAL_as);
        types.add(LITERAL_assert);
        types.add(LITERAL_boolean);
        types.add(LITERAL_break);
        types.add(LITERAL_byte);
        types.add(LITERAL_case);
        types.add(LITERAL_catch);
        types.add(LITERAL_class);
        types.add(LITERAL_continue);
        types.add(LITERAL_def);
        types.add(LITERAL_default);
        types.add(LITERAL_double);
        types.add(LITERAL_else);
        types.add(LITERAL_enum);
        types.add(LITERAL_extends);
        types.add(LITERAL_false);
        types.add(LITERAL_finally);
        types.add(LITERAL_float);
        types.add(LITERAL_for);
        types.add(LITERAL_char);
        types.add(LITERAL_if);
        types.add(LITERAL_implements);
        types.add(LITERAL_import);
        types.add(LITERAL_in);
        types.add(LITERAL_instanceof);
        types.add(LITERAL_int);
        types.add(LITERAL_interface);
        types.add(LITERAL_long);
        types.add(LITERAL_native);
        types.add(LITERAL_new);
        types.add(LITERAL_null);
        types.add(LITERAL_package);
        types.add(LITERAL_private);
        types.add(LITERAL_protected);
        types.add(LITERAL_public);
        types.add(LITERAL_return);
        types.add(LITERAL_short);
        types.add(LITERAL_static);
        types.add(LITERAL_super);
        types.add(LITERAL_switch);
        types.add(LITERAL_synchronized);
        types.add(LITERAL_this);
        types.add(LITERAL_threadsafe);
        types.add(LITERAL_throw);
        types.add(LITERAL_throws);
        types.add(LITERAL_transient);
        types.add(LITERAL_true);
        types.add(LITERAL_try);
        types.add(LITERAL_void);
        types.add(LITERAL_volatile);
        types.add(LITERAL_while);
        types.add(LNOT);
        types.add(LOR);
        types.add(LPAREN);
        types.add(LT);
        types.add(MAP_CONSTRUCTOR);
        types.add(MEMBER_POINTER);
        types.add(METHOD_CALL);
        types.add(METHOD_DEF);
        types.add(MINUS_ASSIGN);
        types.add(MINUS);
        types.add(ML_COMMENT);
        types.add(MOD_ASSIGN);
        types.add(MOD);
        types.add(MODIFIERS);
        types.add(NLS);
        types.add(NOT_EQUAL);
        types.add(NULL_TREE_LOOKAHEAD);
        types.add(NUM_BIG_DECIMAL);
        types.add(NUM_BIG_INT);
        types.add(NUM_DOUBLE);
        types.add(NUM_FLOAT);
        types.add(NUM_INT);
        types.add(NUM_LONG);
        types.add(OBJBLOCK);
        types.add(ONE_NL);
        types.add(OPTIONAL_DOT);
        types.add(PACKAGE_DEF);
        types.add(PARAMETER_DEF);
        types.add(PARAMETERS);
        types.add(PLUS_ASSIGN);
        types.add(PLUS);
        types.add(POST_DEC);
        types.add(POST_INC);
        types.add(QUESTION);
        types.add(RANGE_EXCLUSIVE);
        types.add(RANGE_INCLUSIVE);
        types.add(RBRACK);
        types.add(RCURLY);
        types.add(REGEX_FIND);
        types.add(REGEX_MATCH);
        types.add(REGEXP_CTOR_END);
        types.add(REGEXP_LITERAL);
        types.add(REGEXP_SYMBOL);
        types.add(RPAREN);
        types.add(SELECT_SLOT);
        types.add(SEMI);
        types.add(SH_COMMENT);
        types.add(SL_ASSIGN);
        types.add(SL_COMMENT);
        types.add(SL);
        types.add(SLIST);
        types.add(SPREAD_ARG);
        types.add(SPREAD_DOT);
        types.add(SPREAD_MAP_ARG);
        types.add(SR_ASSIGN);
        types.add(SR);
        types.add(STAR_ASSIGN);
        types.add(STAR_STAR_ASSIGN);
        types.add(STAR_STAR);
        types.add(STAR);
        types.add(STATIC_IMPORT);
        types.add(STATIC_INIT);
        types.add(STRICTFP);
        types.add(STRING_CONSTRUCTOR);
        types.add(STRING_CTOR_END);
        types.add(STRING_CTOR_MIDDLE);
        types.add(STRING_CTOR_START);
        types.add(STRING_CH);
        types.add(STRING_LITERAL);
        types.add(STRING_NL);
        types.add(SUPER_CTOR_CALL);
        types.add(TRIPLE_DOT);
        types.add(TYPE_ARGUMENT);
        types.add(TYPE_ARGUMENTS);
        types.add(TYPE_LOWER_BOUNDS);
        types.add(TYPE_PARAMETER);
        types.add(TYPE_PARAMETERS);
        types.add(TYPE_UPPER_BOUNDS);
        types.add(TYPE);
        types.add(TYPECAST);
        types.add(UNARY_MINUS);
        types.add(UNARY_PLUS);
        types.add(UNUSED_CONST);
        types.add(UNUSED_DO);
        types.add(UNUSED_GOTO);
        types.add(VARIABLE_DEF);
        types.add(VARIABLE_PARAMETER_DEF);
        types.add(VOCAB);
        types.add(WILDCARD_TYPE);
        types.add(WS);
        types.add(WHITESPACE);
        types.add(ERROR);
        return types;
    }

    public static GsfTokenId getTokenId(int token) {
        switch (token) {
            case GroovyTokenTypes.ABSTRACT:
                    return GroovyTokenId.ABSTRACT;
            case GroovyTokenTypes.ANNOTATION_ARRAY_INIT:
                    return GroovyTokenId.ANNOTATION_ARRAY_INIT;
            case GroovyTokenTypes.ANNOTATION_DEF:
                    return GroovyTokenId.ANNOTATION_DEF;
            case GroovyTokenTypes.ANNOTATION_FIELD_DEF:
                    return GroovyTokenId.ANNOTATION_FIELD_DEF;
            case GroovyTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
                    return GroovyTokenId.ANNOTATION_MEMBER_VALUE_PAIR;
            case GroovyTokenTypes.ANNOTATION:
                    return GroovyTokenId.ANNOTATION;
            case GroovyTokenTypes.ANNOTATIONS:
                    return GroovyTokenId.ANNOTATIONS;
            case GroovyTokenTypes.ARRAY_DECLARATOR:
                    return GroovyTokenId.ARRAY_DECLARATOR;
            case GroovyTokenTypes.ASSIGN:
                    return GroovyTokenId.ASSIGN;
            case GroovyTokenTypes.AT:
                    return GroovyTokenId.AT;
            case GroovyTokenTypes.BAND_ASSIGN:
                    return GroovyTokenId.BAND_ASSIGN;
            case GroovyTokenTypes.BAND:
                    return GroovyTokenId.BAND;
            case GroovyTokenTypes.BIG_SUFFIX:
                    return GroovyTokenId.BIG_SUFFIX;
            case GroovyTokenTypes.BLOCK:
                    return GroovyTokenId.BLOCK;
            case GroovyTokenTypes.BNOT:
                    return GroovyTokenId.BNOT;
            case GroovyTokenTypes.BOR_ASSIGN:
                    return GroovyTokenId.BOR_ASSIGN;
            case GroovyTokenTypes.BOR:
                    return GroovyTokenId.BOR;
            case GroovyTokenTypes.BSR_ASSIGN:
                    return GroovyTokenId.BSR_ASSIGN;
            case GroovyTokenTypes.BSR:
                    return GroovyTokenId.BSR;
            case GroovyTokenTypes.BXOR_ASSIGN:
                    return GroovyTokenId.BXOR_ASSIGN;
            case GroovyTokenTypes.BXOR:
                    return GroovyTokenId.BXOR;
            case GroovyTokenTypes.CASE_GROUP:
                    return GroovyTokenId.CASE_GROUP;
            case GroovyTokenTypes.CLASS_DEF:
                    return GroovyTokenId.CLASS_DEF;
            case GroovyTokenTypes.CLOSABLE_BLOCK:
                    return GroovyTokenId.CLOSABLE_BLOCK;
            case GroovyTokenTypes.CLOSABLE_BLOCK_OP:
                    return GroovyTokenId.CLOSABLE_BLOCK_OP;
            case GroovyTokenTypes.CLOSURE_LIST:
                    return GroovyTokenId.CLOSURE_LIST;
            case GroovyTokenTypes.COLON:
                    return GroovyTokenId.COLON;
            case GroovyTokenTypes.COMMA:
                    return GroovyTokenId.COMMA;
            case GroovyTokenTypes.COMPARE_TO:
                    return GroovyTokenId.COMPARE_TO;
            case GroovyTokenTypes.CTOR_CALL:
                    return GroovyTokenId.CTOR_CALL;
            case GroovyTokenTypes.CTOR_IDENT:
                    return GroovyTokenId.CTOR_IDENT;
            case GroovyTokenTypes.DEC:
                    return GroovyTokenId.DEC;
            case GroovyTokenTypes.DIGIT:
                    return GroovyTokenId.DIGIT;
            case GroovyTokenTypes.DIV_ASSIGN:
                    return GroovyTokenId.DIV_ASSIGN;
            case GroovyTokenTypes.DIV:
                    return GroovyTokenId.DIV;
            case GroovyTokenTypes.DOLLAR:
                    return GroovyTokenId.DOLLAR;
            case GroovyTokenTypes.DOT:
                    return GroovyTokenId.DOT;
            case GroovyTokenTypes.DYNAMIC_MEMBER:
                    return GroovyTokenId.DYNAMIC_MEMBER;
            case GroovyTokenTypes.ELIST:
                    return GroovyTokenId.ELIST;
            case GroovyTokenTypes.ELVIS_OPERATOR:
                    return GroovyTokenId.ELVIS_OPERATOR;
            case GroovyTokenTypes.EMPTY_STAT:
                    return GroovyTokenId.EMPTY_STAT;
            case GroovyTokenTypes.ENUM_CONSTANT_DEF:
                    return GroovyTokenId.ENUM_CONSTANT_DEF;
            case GroovyTokenTypes.ENUM_DEF:
                    return GroovyTokenId.ENUM_DEF;
            case GroovyTokenTypes.EOF:
                    return GroovyTokenId.EOF;
            case GroovyTokenTypes.EQUAL:
                    return GroovyTokenId.EQUAL;
            case GroovyTokenTypes.ESC:
                    return GroovyTokenId.ESC;
            case GroovyTokenTypes.EXPONENT:
                    return GroovyTokenId.EXPONENT;
            case GroovyTokenTypes.EXPR:
                    return GroovyTokenId.EXPR;
            case GroovyTokenTypes.EXTENDS_CLAUSE:
                    return GroovyTokenId.EXTENDS_CLAUSE;
            case GroovyTokenTypes.FINAL:
                    return GroovyTokenId.FINAL;
            case GroovyTokenTypes.FLOAT_SUFFIX:
                    return GroovyTokenId.FLOAT_SUFFIX;
            case GroovyTokenTypes.FOR_CONDITION:
                    return GroovyTokenId.FOR_CONDITION;
            case GroovyTokenTypes.FOR_EACH_CLAUSE:
                    return GroovyTokenId.FOR_EACH_CLAUSE;
            case GroovyTokenTypes.FOR_IN_ITERABLE:
                    return GroovyTokenId.FOR_IN_ITERABLE;
            case GroovyTokenTypes.FOR_INIT:
                    return GroovyTokenId.FOR_INIT;
            case GroovyTokenTypes.FOR_ITERATOR:
                    return GroovyTokenId.FOR_ITERATOR;
            case GroovyTokenTypes.GE:
                    return GroovyTokenId.GE;
            case GroovyTokenTypes.GT:
                    return GroovyTokenId.GT;
            case GroovyTokenTypes.HEX_DIGIT:
                    return GroovyTokenId.HEX_DIGIT;
            case GroovyTokenTypes.IDENT:
                    return GroovyTokenId.IDENT;
            case GroovyTokenTypes.IMPLEMENTS_CLAUSE:
                    return GroovyTokenId.IMPLEMENTS_CLAUSE;
            case GroovyTokenTypes.IMPLICIT_PARAMETERS:
                    return GroovyTokenId.IMPLICIT_PARAMETERS;
            case GroovyTokenTypes.IMPORT:
                    return GroovyTokenId.IMPORT;
            case GroovyTokenTypes.INC:
                    return GroovyTokenId.INC;
            case GroovyTokenTypes.INDEX_OP:
                    return GroovyTokenId.INDEX_OP;
            case GroovyTokenTypes.INSTANCE_INIT:
                    return GroovyTokenId.INSTANCE_INIT;
            case GroovyTokenTypes.INTERFACE_DEF:
                    return GroovyTokenId.INTERFACE_DEF;
            case GroovyTokenTypes.LABELED_ARG:
                    return GroovyTokenId.LABELED_ARG;
            case GroovyTokenTypes.LABELED_STAT:
                    return GroovyTokenId.LABELED_STAT;
            case GroovyTokenTypes.LAND:
                    return GroovyTokenId.LAND;
            case GroovyTokenTypes.LBRACK:
                    return GroovyTokenId.LBRACK;
            case GroovyTokenTypes.LCURLY:
                    return GroovyTokenId.LCURLY;
            case GroovyTokenTypes.LE:
                    return GroovyTokenId.LE;
            case GroovyTokenTypes.LETTER:
                    return GroovyTokenId.LETTER;
            case GroovyTokenTypes.LIST_CONSTRUCTOR:
                    return GroovyTokenId.LIST_CONSTRUCTOR;
            case GroovyTokenTypes.LITERAL_as:
                    return GroovyTokenId.LITERAL_as;
            case GroovyTokenTypes.LITERAL_assert:
                    return GroovyTokenId.LITERAL_assert;
            case GroovyTokenTypes.LITERAL_boolean:
                    return GroovyTokenId.LITERAL_boolean;
            case GroovyTokenTypes.LITERAL_break:
                    return GroovyTokenId.LITERAL_break;
            case GroovyTokenTypes.LITERAL_byte:
                    return GroovyTokenId.LITERAL_byte;
            case GroovyTokenTypes.LITERAL_case:
                    return GroovyTokenId.LITERAL_case;
            case GroovyTokenTypes.LITERAL_catch:
                    return GroovyTokenId.LITERAL_catch;
            case GroovyTokenTypes.LITERAL_class:
                    return GroovyTokenId.LITERAL_class;
            case GroovyTokenTypes.LITERAL_continue:
                    return GroovyTokenId.LITERAL_continue;
            case GroovyTokenTypes.LITERAL_def:
                    return GroovyTokenId.LITERAL_def;
            case GroovyTokenTypes.LITERAL_default:
                    return GroovyTokenId.LITERAL_default;
            case GroovyTokenTypes.LITERAL_double:
                    return GroovyTokenId.LITERAL_double;
            case GroovyTokenTypes.LITERAL_else:
                    return GroovyTokenId.LITERAL_else;
            case GroovyTokenTypes.LITERAL_enum:
                    return GroovyTokenId.LITERAL_enum;
            case GroovyTokenTypes.LITERAL_extends:
                    return GroovyTokenId.LITERAL_extends;
            case GroovyTokenTypes.LITERAL_false:
                    return GroovyTokenId.LITERAL_false;
            case GroovyTokenTypes.LITERAL_finally:
                    return GroovyTokenId.LITERAL_finally;
            case GroovyTokenTypes.LITERAL_float:
                    return GroovyTokenId.LITERAL_float;
            case GroovyTokenTypes.LITERAL_for:
                    return GroovyTokenId.LITERAL_for;
            case GroovyTokenTypes.LITERAL_char:
                    return GroovyTokenId.LITERAL_char;
            case GroovyTokenTypes.LITERAL_if:
                    return GroovyTokenId.LITERAL_if;
            case GroovyTokenTypes.LITERAL_implements:
                    return GroovyTokenId.LITERAL_implements;
            case GroovyTokenTypes.LITERAL_import:
                    return GroovyTokenId.LITERAL_import;
            case GroovyTokenTypes.LITERAL_in:
                    return GroovyTokenId.LITERAL_in;
            case GroovyTokenTypes.LITERAL_instanceof:
                    return GroovyTokenId.LITERAL_instanceof;
            case GroovyTokenTypes.LITERAL_int:
                    return GroovyTokenId.LITERAL_int;
            case GroovyTokenTypes.LITERAL_interface:
                    return GroovyTokenId.LITERAL_interface;
            case GroovyTokenTypes.LITERAL_long:
                    return GroovyTokenId.LITERAL_long;
            case GroovyTokenTypes.LITERAL_native:
                    return GroovyTokenId.LITERAL_native;
            case GroovyTokenTypes.LITERAL_new:
                    return GroovyTokenId.LITERAL_new;
            case GroovyTokenTypes.LITERAL_null:
                    return GroovyTokenId.LITERAL_null;
            case GroovyTokenTypes.LITERAL_package:
                    return GroovyTokenId.LITERAL_package;
            case GroovyTokenTypes.LITERAL_private:
                    return GroovyTokenId.LITERAL_private;
            case GroovyTokenTypes.LITERAL_protected:
                    return GroovyTokenId.LITERAL_protected;
            case GroovyTokenTypes.LITERAL_public:
                    return GroovyTokenId.LITERAL_public;
            case GroovyTokenTypes.LITERAL_return:
                    return GroovyTokenId.LITERAL_return;
            case GroovyTokenTypes.LITERAL_short:
                    return GroovyTokenId.LITERAL_short;
            case GroovyTokenTypes.LITERAL_static:
                    return GroovyTokenId.LITERAL_static;
            case GroovyTokenTypes.LITERAL_super:
                    return GroovyTokenId.LITERAL_super;
            case GroovyTokenTypes.LITERAL_switch:
                    return GroovyTokenId.LITERAL_switch;
            case GroovyTokenTypes.LITERAL_synchronized:
                    return GroovyTokenId.LITERAL_synchronized;
            case GroovyTokenTypes.LITERAL_this:
                    return GroovyTokenId.LITERAL_this;
            case GroovyTokenTypes.LITERAL_threadsafe:
                    return GroovyTokenId.LITERAL_threadsafe;
            case GroovyTokenTypes.LITERAL_throw:
                    return GroovyTokenId.LITERAL_throw;
            case GroovyTokenTypes.LITERAL_throws:
                    return GroovyTokenId.LITERAL_throws;
            case GroovyTokenTypes.LITERAL_transient:
                    return GroovyTokenId.LITERAL_transient;
            case GroovyTokenTypes.LITERAL_true:
                    return GroovyTokenId.LITERAL_true;
            case GroovyTokenTypes.LITERAL_try:
                    return GroovyTokenId.LITERAL_try;
            case GroovyTokenTypes.LITERAL_void:
                    return GroovyTokenId.LITERAL_void;
            case GroovyTokenTypes.LITERAL_volatile:
                    return GroovyTokenId.LITERAL_volatile;
            case GroovyTokenTypes.LITERAL_while:
                    return GroovyTokenId.LITERAL_while;
            case GroovyTokenTypes.LNOT:
                    return GroovyTokenId.LNOT;
            case GroovyTokenTypes.LOR:
                    return GroovyTokenId.LOR;
            case GroovyTokenTypes.LPAREN:
                    return GroovyTokenId.LPAREN;
            case GroovyTokenTypes.LT:
                    return GroovyTokenId.LT;
            case GroovyTokenTypes.MAP_CONSTRUCTOR:
                    return GroovyTokenId.MAP_CONSTRUCTOR;
            case GroovyTokenTypes.MEMBER_POINTER:
                    return GroovyTokenId.MEMBER_POINTER;
            case GroovyTokenTypes.METHOD_CALL:
                    return GroovyTokenId.METHOD_CALL;
            case GroovyTokenTypes.METHOD_DEF:
                    return GroovyTokenId.METHOD_DEF;
            case GroovyTokenTypes.MINUS_ASSIGN:
                    return GroovyTokenId.MINUS_ASSIGN;
            case GroovyTokenTypes.MINUS:
                    return GroovyTokenId.MINUS;
            case GroovyTokenTypes.ML_COMMENT:
                    return GroovyTokenId.ML_COMMENT;
            case GroovyTokenTypes.MOD_ASSIGN:
                    return GroovyTokenId.MOD_ASSIGN;
            case GroovyTokenTypes.MOD:
                    return GroovyTokenId.MOD;
            case GroovyTokenTypes.MODIFIERS:
                    return GroovyTokenId.MODIFIERS;
            case GroovyTokenTypes.NLS:
                    return GroovyTokenId.NLS;
            case GroovyTokenTypes.NOT_EQUAL:
                    return GroovyTokenId.NOT_EQUAL;
            case GroovyTokenTypes.NULL_TREE_LOOKAHEAD:
                    return GroovyTokenId.NULL_TREE_LOOKAHEAD;
            case GroovyTokenTypes.NUM_BIG_DECIMAL:
                    return GroovyTokenId.NUM_BIG_DECIMAL;
            case GroovyTokenTypes.NUM_BIG_INT:
                    return GroovyTokenId.NUM_BIG_INT;
            case GroovyTokenTypes.NUM_DOUBLE:
                    return GroovyTokenId.NUM_DOUBLE;
            case GroovyTokenTypes.NUM_FLOAT:
                    return GroovyTokenId.NUM_FLOAT;
            case GroovyTokenTypes.NUM_INT:
                    return GroovyTokenId.NUM_INT;
            case GroovyTokenTypes.NUM_LONG:
                    return GroovyTokenId.NUM_LONG;
            case GroovyTokenTypes.OBJBLOCK:
                    return GroovyTokenId.OBJBLOCK;
            case GroovyTokenTypes.ONE_NL:
                    return GroovyTokenId.ONE_NL;
            case GroovyTokenTypes.OPTIONAL_DOT:
                    return GroovyTokenId.OPTIONAL_DOT;
            case GroovyTokenTypes.PACKAGE_DEF:
                    return GroovyTokenId.PACKAGE_DEF;
            case GroovyTokenTypes.PARAMETER_DEF:
                    return GroovyTokenId.PARAMETER_DEF;
            case GroovyTokenTypes.PARAMETERS:
                    return GroovyTokenId.PARAMETERS;
            case GroovyTokenTypes.PLUS_ASSIGN:
                    return GroovyTokenId.PLUS_ASSIGN;
            case GroovyTokenTypes.PLUS:
                    return GroovyTokenId.PLUS;
            case GroovyTokenTypes.POST_DEC:
                    return GroovyTokenId.POST_DEC;
            case GroovyTokenTypes.POST_INC:
                    return GroovyTokenId.POST_INC;
            case GroovyTokenTypes.QUESTION:
                    return GroovyTokenId.QUESTION;
            case GroovyTokenTypes.RANGE_EXCLUSIVE:
                    return GroovyTokenId.RANGE_EXCLUSIVE;
            case GroovyTokenTypes.RANGE_INCLUSIVE:
                    return GroovyTokenId.RANGE_INCLUSIVE;
            case GroovyTokenTypes.RBRACK:
                    return GroovyTokenId.RBRACK;
            case GroovyTokenTypes.RCURLY:
                    return GroovyTokenId.RCURLY;
            case GroovyTokenTypes.REGEX_FIND:
                    return GroovyTokenId.REGEX_FIND;
            case GroovyTokenTypes.REGEX_MATCH:
                    return GroovyTokenId.REGEX_MATCH;
            case GroovyTokenTypes.REGEXP_CTOR_END:
                    return GroovyTokenId.REGEXP_CTOR_END;
            case GroovyTokenTypes.REGEXP_LITERAL:
                    return GroovyTokenId.REGEXP_LITERAL;
            case GroovyTokenTypes.REGEXP_SYMBOL:
                    return GroovyTokenId.REGEXP_SYMBOL;
            case GroovyTokenTypes.RPAREN:
                    return GroovyTokenId.RPAREN;
            case GroovyTokenTypes.SELECT_SLOT:
                    return GroovyTokenId.SELECT_SLOT;
            case GroovyTokenTypes.SEMI:
                    return GroovyTokenId.SEMI;
            case GroovyTokenTypes.SH_COMMENT:
                    return GroovyTokenId.SH_COMMENT;
            case GroovyTokenTypes.SL_ASSIGN:
                    return GroovyTokenId.SL_ASSIGN;
            case GroovyTokenTypes.SL_COMMENT:
                    return GroovyTokenId.SL_COMMENT;
            case GroovyTokenTypes.SL:
                    return GroovyTokenId.SL;
            case GroovyTokenTypes.SLIST:
                    return GroovyTokenId.SLIST;
            case GroovyTokenTypes.SPREAD_ARG:
                    return GroovyTokenId.SPREAD_ARG;
            case GroovyTokenTypes.SPREAD_DOT:
                    return GroovyTokenId.SPREAD_DOT;
            case GroovyTokenTypes.SPREAD_MAP_ARG:
                    return GroovyTokenId.SPREAD_MAP_ARG;
            case GroovyTokenTypes.SR_ASSIGN:
                    return GroovyTokenId.SR_ASSIGN;
            case GroovyTokenTypes.SR:
                    return GroovyTokenId.SR;
            case GroovyTokenTypes.STAR_ASSIGN:
                    return GroovyTokenId.STAR_ASSIGN;
            case GroovyTokenTypes.STAR_STAR_ASSIGN:
                    return GroovyTokenId.STAR_STAR_ASSIGN;
            case GroovyTokenTypes.STAR_STAR:
                    return GroovyTokenId.STAR_STAR;
            case GroovyTokenTypes.STAR:
                    return GroovyTokenId.STAR;
            case GroovyTokenTypes.STATIC_IMPORT:
                    return GroovyTokenId.STATIC_IMPORT;
            case GroovyTokenTypes.STATIC_INIT:
                    return GroovyTokenId.STATIC_INIT;
            case GroovyTokenTypes.STRICTFP:
                    return GroovyTokenId.STRICTFP;
            case GroovyTokenTypes.STRING_CONSTRUCTOR:
                    return GroovyTokenId.STRING_CONSTRUCTOR;
            case GroovyTokenTypes.STRING_CTOR_END:
                    return GroovyTokenId.STRING_CTOR_END;
            case GroovyTokenTypes.STRING_CTOR_MIDDLE:
                    return GroovyTokenId.STRING_CTOR_MIDDLE;
            case GroovyTokenTypes.STRING_CTOR_START:
                    return GroovyTokenId.STRING_CTOR_START;
            case GroovyTokenTypes.STRING_CH:
                    return GroovyTokenId.STRING_CH;
            case GroovyTokenTypes.STRING_LITERAL:
                    return GroovyTokenId.STRING_LITERAL;
            case GroovyTokenTypes.STRING_NL:
                    return GroovyTokenId.STRING_NL;
            case GroovyTokenTypes.SUPER_CTOR_CALL:
                    return GroovyTokenId.SUPER_CTOR_CALL;
            case GroovyTokenTypes.TRIPLE_DOT:
                    return GroovyTokenId.TRIPLE_DOT;
            case GroovyTokenTypes.TYPE_ARGUMENT:
                    return GroovyTokenId.TYPE_ARGUMENT;
            case GroovyTokenTypes.TYPE_ARGUMENTS:
                    return GroovyTokenId.TYPE_ARGUMENTS;
            case GroovyTokenTypes.TYPE_LOWER_BOUNDS:
                    return GroovyTokenId.TYPE_LOWER_BOUNDS;
            case GroovyTokenTypes.TYPE_PARAMETER:
                    return GroovyTokenId.TYPE_PARAMETER;
            case GroovyTokenTypes.TYPE_PARAMETERS:
                    return GroovyTokenId.TYPE_PARAMETERS;
            case GroovyTokenTypes.TYPE_UPPER_BOUNDS:
                    return GroovyTokenId.TYPE_UPPER_BOUNDS;
            case GroovyTokenTypes.TYPE:
                    return GroovyTokenId.TYPE;
            case GroovyTokenTypes.TYPECAST:
                    return GroovyTokenId.TYPECAST;
            case GroovyTokenTypes.UNARY_MINUS:
                    return GroovyTokenId.UNARY_MINUS;
            case GroovyTokenTypes.UNARY_PLUS:
                    return GroovyTokenId.UNARY_PLUS;
            case GroovyTokenTypes.UNUSED_CONST:
                    return GroovyTokenId.UNUSED_CONST;
            case GroovyTokenTypes.UNUSED_DO:
                    return GroovyTokenId.UNUSED_DO;
            case GroovyTokenTypes.UNUSED_GOTO:
                    return GroovyTokenId.UNUSED_GOTO;
            case GroovyTokenTypes.VARIABLE_DEF:
                    return GroovyTokenId.VARIABLE_DEF;
            case GroovyTokenTypes.VARIABLE_PARAMETER_DEF:
                    return GroovyTokenId.VARIABLE_PARAMETER_DEF;
            case GroovyTokenTypes.VOCAB:
                    return GroovyTokenId.VOCAB;
            case GroovyTokenTypes.WILDCARD_TYPE:
                    return GroovyTokenId.WILDCARD_TYPE;
            case GroovyTokenTypes.WS:
                    return GroovyTokenId.WS;
            // added manually
            case ERROR_INT:
                    return GroovyTokenId.WS;
            default:
                return GroovyTokenId.IDENTIFIER;
        }
    }

}
