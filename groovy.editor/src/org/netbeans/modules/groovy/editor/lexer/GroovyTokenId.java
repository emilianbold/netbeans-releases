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
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * @author Martin Adamek
 */
public class GroovyTokenId implements TokenId {


    public static final GroovyTokenId QUOTED_STRING_LITERAL = new GroovyTokenId("QUOTED_STRING_LITERAL", null, "string");
    public static final GroovyTokenId QUOTED_STRING_END = new GroovyTokenId("QUOTED_STRING_END", null, "string");
    public static final GroovyTokenId QUOTED_STRING_BEGIN = new GroovyTokenId("QUOTED_STRING_BEGIN", null, "string");
    public static final GroovyTokenId EMBEDDED_GROOVY = new GroovyTokenId("EMBEDDED_GROOVY", null, "default");
    
    public static TokenId NONUNARY_OP;

    // Copied from GsfTokenId
    public static final GroovyTokenId ERROR = new GroovyTokenId("GSF_ERROR", null, "error");
    public static final GroovyTokenId IDENTIFIER = new GroovyTokenId("GSF_IDENTIFIER", null, "identifier");
    public static final GroovyTokenId CLASS_VAR = new GroovyTokenId("GSF_CLASS", null, "staticfield");
    public static final GroovyTokenId INSTANCE_VAR = new GroovyTokenId("GSF_INSTANCE", null, "field");
    public static final GroovyTokenId GLOBAL_VAR = new GroovyTokenId("GSF_GLOBAL", null, "static");
    public static final GroovyTokenId CONSTANT = new GroovyTokenId("GSF_CONSTANT", null, "constant");
    public static final GroovyTokenId DOCUMENTATION = new GroovyTokenId("GSF_DOCUMENTATION", null, "comment");
    public static final GroovyTokenId INT_LITERAL = new GroovyTokenId("GSF_INT_LITERAL", null, "number");
    public static final GroovyTokenId REGEXP_LITERAL = new GroovyTokenId("GSF_REGEXP_LITERAL", null, "regexp");
    public static final GroovyTokenId LONG_LITERAL = new GroovyTokenId("GSF_LONG_LITERAL", null, "number");
    public static final GroovyTokenId FLOAT_LITERAL = new GroovyTokenId("GSF_FLOAT_LITERAL", null, "number");
    public static final GroovyTokenId DOUBLE_LITERAL = new GroovyTokenId("GSF_DOUBLE_LITERAL", null, "number");
    public static final GroovyTokenId CHAR_LITERAL = new GroovyTokenId("GSF_CHAR_LITERAL", null, "character");
    public static final GroovyTokenId STRING_LITERAL = new GroovyTokenId("GSF_STRING_LITERAL", null, "string");
    public static final GroovyTokenId WHITESPACE = new GroovyTokenId("GSF_WHITESPACE", null, "whitespace");
    public static final GroovyTokenId LINE_COMMENT = new GroovyTokenId("GSF_LINE_COMMENT", null, "comment");
    public static final GroovyTokenId BLOCK_COMMENT = new GroovyTokenId("GSF_BLOCK_COMMENT", null, "comment");
    public static final GroovyTokenId TODO = new GroovyTokenId("GSF_TODO", null, "todo");
    public static final GroovyTokenId TYPE_SYMBOL = new GroovyTokenId("GSF_TYPESYMBOL", null, "typesymbol");
    public static final GroovyTokenId LPAREN = new GroovyTokenId("GSF_LPAREN", "(", "separator");
    public static final GroovyTokenId RPAREN = new GroovyTokenId("GSF_RPAREN", ")", "separator");
    public static final GroovyTokenId LBRACE = new GroovyTokenId("GSF_LBRACE", "{", "separator");
    public static final GroovyTokenId RBRACE = new GroovyTokenId("GSF_RBRACE", "}", "separator");
    public static final GroovyTokenId LBRACKET = new GroovyTokenId("GSF_LBRACKET", "[", "separator");
    public static final GroovyTokenId RBRACKET = new GroovyTokenId("GSF_RBRACKET", "]", "separator");
    public static final GroovyTokenId STRING_BEGIN = new GroovyTokenId("GSF_STRING_BEGIN", null, "string");
    public static final GroovyTokenId STRING_END = new GroovyTokenId("GSF_STRING_END", null, "string");
    public static final GroovyTokenId REGEXP_BEGIN = new GroovyTokenId("GSF_REGEXP_BEGIN", null, "regexp"); // or separator?
    public static final GroovyTokenId REGEXP_END = new GroovyTokenId("GSF_REGEXP_END", null, "regexp");
    public static final GroovyTokenId CHAR_LITERAL_INCOMPLETE = new GroovyTokenId("GSF_CHAR_LITERAL_INCOMPLETE", null, "character");
    public static final GroovyTokenId STRING_LITERAL_INCOMPLETE = new GroovyTokenId("GSF_STRING_LITERAL_INCOMPLETE", null, "string");
    public static final GroovyTokenId BLOCK_COMMENT_INCOMPLETE = new GroovyTokenId("GSF_BLOCK_COMMENT_INCOMPLETE", null, "comment");
    public static final GroovyTokenId JAVADOC_COMMENT_INCOMPLETE = new GroovyTokenId("GSF_JAVADOC_COMMENT_INCOMPLETE", null, "comment");
    public static final GroovyTokenId INVALID_COMMENT_END = new GroovyTokenId("GSF_INVALID_COMMENT_END", "*/", "error");
    public static final GroovyTokenId FLOAT_LITERAL_INVALID = new GroovyTokenId("GSF_FLOAT_LITERAL_INVALID", null, "number");
    public static final GroovyTokenId ANY_KEYWORD = new GroovyTokenId("GSF_ANY_KEYWORD", null, "keyword");
    public static final GroovyTokenId ANY_OPERATOR = new GroovyTokenId("GSF_ANY_OPERATOR", null, "operator");
    
    private final String name;
    private final String primaryCategory;
    private final String fixedText;
    private final int ordinal;
    protected static int nextOrdinal;

    public GroovyTokenId(String name, String fixedText, String primaryCategory) {
        this.name = name;
        this.primaryCategory = primaryCategory;
        this.fixedText = fixedText;
        synchronized (GroovyTokenId.class) {
            this.ordinal = nextOrdinal++;
        }
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }
    
    public String toString() {
        return getClass().getName() + ":" + name + ":" + ordinal;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Tokens">
    public static final GroovyTokenId ABSTRACT = new GroovyTokenId("ABSTRACT", null, "keyword");
    public static final GroovyTokenId ANNOTATION_ARRAY_INIT = new GroovyTokenId("ANNOTATION_ARRAY_INIT", null, "annotation");
    public static final GroovyTokenId ANNOTATION_DEF = new GroovyTokenId("ANNOTATION_DEF", null, "annotation");
    public static final GroovyTokenId ANNOTATION_FIELD_DEF = new GroovyTokenId("ANNOTATION_FIELD_DEF", null, "annotation");
    public static final GroovyTokenId ANNOTATION_MEMBER_VALUE_PAIR = new GroovyTokenId("ANNOTATION_MEMBER_VALUE_PAIR", null, "annotation");
    public static final GroovyTokenId ANNOTATION = new GroovyTokenId("ANNOTATION", null, "annotation");
    public static final GroovyTokenId ANNOTATIONS = new GroovyTokenId("ANNOTATIONS", null, "annotation");
    public static final GroovyTokenId ARRAY_DECLARATOR = new GroovyTokenId("ARRAY_DECLARATOR", null, "default");
    public static final GroovyTokenId ASSIGN = new GroovyTokenId("ASSIGN", null, "operator");
    public static final GroovyTokenId AT = new GroovyTokenId("AT", null, "operator");
    public static final GroovyTokenId BAND_ASSIGN = new GroovyTokenId("BAND_ASSIGN", null, "operator");
    public static final GroovyTokenId BAND = new GroovyTokenId("BAND", null, "operator");
    public static final GroovyTokenId BIG_SUFFIX = new GroovyTokenId("BIG_SUFFIX", null, "number");
    public static final GroovyTokenId BLOCK = new GroovyTokenId("BLOCK", null, "default");
    public static final GroovyTokenId BNOT = new GroovyTokenId("BNOT", null, "operator");
    public static final GroovyTokenId BOR_ASSIGN = new GroovyTokenId("BOR_ASSIGN", null, "operator");
    public static final GroovyTokenId BOR = new GroovyTokenId("BOR", null, "operator");
    public static final GroovyTokenId BSR_ASSIGN = new GroovyTokenId("BSR_ASSIGN", null, "operator");
    public static final GroovyTokenId BSR = new GroovyTokenId("BSR", null, "operator");
    public static final GroovyTokenId BXOR_ASSIGN = new GroovyTokenId("BXOR_ASSIGN", null, "operator");
    public static final GroovyTokenId BXOR = new GroovyTokenId("BXOR", null, "operator");
    public static final GroovyTokenId CASE_GROUP = new GroovyTokenId("CASE_GROUP", null, "default");
    public static final GroovyTokenId CLASS_DEF = new GroovyTokenId("CLASS_DEF", null, "default");
    public static final GroovyTokenId CLOSED_BLOCK = new GroovyTokenId("CLOSABLE_BLOCK", null, "default");
    public static final GroovyTokenId CLOSURE_OP = new GroovyTokenId("CLOSURE_LIST", null, "operator");
    public static final GroovyTokenId COLON = new GroovyTokenId("COLON", null, "operator");
    public static final GroovyTokenId COMMA = new GroovyTokenId("COMMA", null, "operator");
    public static final GroovyTokenId COMPARE_TO = new GroovyTokenId("COMPARE_TO", null, "operator");
    public static final GroovyTokenId CTOR_CALL = new GroovyTokenId("CTOR_CALL", null, "default");
    public static final GroovyTokenId CTOR_IDENT = new GroovyTokenId("CTOR_IDENT", null, "default");
    public static final GroovyTokenId DEC = new GroovyTokenId("DEC", null, "operator");
    public static final GroovyTokenId DIGIT = new GroovyTokenId("DIGIT", null, "number");
    public static final GroovyTokenId DIV_ASSIGN = new GroovyTokenId("DIV_ASSIGN", null, "operator");
    public static final GroovyTokenId DIV = new GroovyTokenId("DIV", null, "operator");
    public static final GroovyTokenId DOLLAR = new GroovyTokenId("DOLLAR", null, "operator");
    public static final GroovyTokenId DOT = new GroovyTokenId("DOT", null, "operator");
    public static final GroovyTokenId DYNAMIC_MEMBER = new GroovyTokenId("DYNAMIC_MEMBER", null, "default");
    public static final GroovyTokenId ELIST = new GroovyTokenId("ELIST", null, "default");
    public static final GroovyTokenId EMPTY_STAT = new GroovyTokenId("EMPTY_STAT", null, "default");
    public static final GroovyTokenId ENUM_CONSTANT_DEF = new GroovyTokenId("ENUM_CONSTANT_DEF", null, "default");
    public static final GroovyTokenId ENUM_DEF = new GroovyTokenId("ENUM_DEF", null, "default");
    public static final GroovyTokenId EOF = new GroovyTokenId("EOF", null, "default");
    public static final GroovyTokenId EQUAL = new GroovyTokenId("EQUAL", null, "operator");
    public static final GroovyTokenId ESC = new GroovyTokenId("ESC", null, "default");
    public static final GroovyTokenId EXPONENT = new GroovyTokenId("EXPONENT", null, "number");
    public static final GroovyTokenId EXPR = new GroovyTokenId("EXPR", null, "default");
    public static final GroovyTokenId EXTENDS_CLAUSE = new GroovyTokenId("EXTENDS_CLAUSE", null, "default");
    public static final GroovyTokenId FINAL = new GroovyTokenId("FINAL", null, "keyword");
    public static final GroovyTokenId FLOAT_SUFFIX = new GroovyTokenId("FLOAT_SUFFIX", null, "number");
    public static final GroovyTokenId FOR_CONDITION = new GroovyTokenId("FOR_CONDITION", null, "default");
    public static final GroovyTokenId FOR_EACH_CLAUSE = new GroovyTokenId("FOR_EACH_CLAUSE", null, "keyword");
    public static final GroovyTokenId FOR_IN_ITERABLE = new GroovyTokenId("FOR_IN_ITERABLE", null, "default");
    public static final GroovyTokenId FOR_INIT = new GroovyTokenId("FOR_INIT", null, "default");
    public static final GroovyTokenId FOR_ITERATOR = new GroovyTokenId("FOR_ITERATOR", null, "default");
    public static final GroovyTokenId GE = new GroovyTokenId("GE", null, "operator");
    public static final GroovyTokenId GT = new GroovyTokenId("GT", null, "operator");
    public static final GroovyTokenId HEX_DIGIT = new GroovyTokenId("HEX_DIGIT", null, "number");
    public static final GroovyTokenId IMPLEMENTS_CLAUSE = new GroovyTokenId("IMPLEMENTS_CLAUSE", null, "default");
    public static final GroovyTokenId IMPLICIT_PARAMETERS = new GroovyTokenId("IMPLICIT_PARAMETERS", null, "default");
    public static final GroovyTokenId IMPORT = new GroovyTokenId("IMPORT", null, "default");
    public static final GroovyTokenId INC = new GroovyTokenId("INC", null, "operator");
    public static final GroovyTokenId INDEX_OP = new GroovyTokenId("INDEX_OP", null, "default");
    public static final GroovyTokenId INSTANCE_INIT = new GroovyTokenId("INSTANCE_INIT", null, "default");
    public static final GroovyTokenId INTERFACE_DEF = new GroovyTokenId("INTERFACE_DEF", null, "default");
    public static final GroovyTokenId LABELED_ARG = new GroovyTokenId("LABELED_ARG", null, "default");
    public static final GroovyTokenId LABELED_STAT = new GroovyTokenId("LABELED_STAT", null, "default");
    public static final GroovyTokenId LAND = new GroovyTokenId("LAND", null, "operator");
    public static final GroovyTokenId LE = new GroovyTokenId("LE", null, "operator");
    public static final GroovyTokenId LETTER = new GroovyTokenId("LETTER", null, "default");
    public static final GroovyTokenId LIST_CONSTRUCTOR = new GroovyTokenId("LIST_CONSTRUCTOR", null, "default");
    public static final GroovyTokenId LITERAL_as = new GroovyTokenId("LITERAL_as", null, "keyword");
    public static final GroovyTokenId LITERAL_assert = new GroovyTokenId("LITERAL_assert", null, "keyword");
    public static final GroovyTokenId LITERAL_boolean = new GroovyTokenId("LITERAL_boolean", null, "keyword");
    public static final GroovyTokenId LITERAL_break = new GroovyTokenId("LITERAL_break", null, "keyword");
    public static final GroovyTokenId LITERAL_byte = new GroovyTokenId("LITERAL_byte", null, "keyword");
    public static final GroovyTokenId LITERAL_case = new GroovyTokenId("LITERAL_case", null, "keyword");
    public static final GroovyTokenId LITERAL_catch = new GroovyTokenId("LITERAL_catch", null, "keyword");
    public static final GroovyTokenId LITERAL_class = new GroovyTokenId("LITERAL_class", null, "keyword");
    public static final GroovyTokenId LITERAL_continue = new GroovyTokenId("LITERAL_continue", null, "keyword");
    public static final GroovyTokenId LITERAL_def = new GroovyTokenId("LITERAL_def", null, "keyword");
    public static final GroovyTokenId LITERAL_default = new GroovyTokenId("LITERAL_default", null, "keyword");
    public static final GroovyTokenId LITERAL_double = new GroovyTokenId("LITERAL_double", null, "keyword");
    public static final GroovyTokenId LITERAL_else = new GroovyTokenId("LITERAL_else", null, "keyword");
    public static final GroovyTokenId LITERAL_enum = new GroovyTokenId("LITERAL_enum", null, "keyword");
    public static final GroovyTokenId LITERAL_extends = new GroovyTokenId("LITERAL_extends", null, "keyword");
    public static final GroovyTokenId LITERAL_false = new GroovyTokenId("LITERAL_false", null, "keyword");
    public static final GroovyTokenId LITERAL_finally = new GroovyTokenId("LITERAL_finally", null, "keyword");
    public static final GroovyTokenId LITERAL_float = new GroovyTokenId("LITERAL_float", null, "keyword");
    public static final GroovyTokenId LITERAL_for = new GroovyTokenId("LITERAL_for", null, "keyword");
    public static final GroovyTokenId LITERAL_char = new GroovyTokenId("LITERAL_char", null, "keyword");
    public static final GroovyTokenId LITERAL_if = new GroovyTokenId("LITERAL_if", null, "keyword");
    public static final GroovyTokenId LITERAL_implements = new GroovyTokenId("LITERAL_implements", null, "keyword");
    public static final GroovyTokenId LITERAL_import = new GroovyTokenId("LITERAL_import", null, "keyword");
    public static final GroovyTokenId LITERAL_in = new GroovyTokenId("LITERAL_in", null, "keyword");
    public static final GroovyTokenId LITERAL_instanceof = new GroovyTokenId("LITERAL_instanceof", null, "keyword");
    public static final GroovyTokenId LITERAL_int = new GroovyTokenId("LITERAL_int", null, "keyword");
    public static final GroovyTokenId LITERAL_interface = new GroovyTokenId("LITERAL_interface", null, "keyword");
    public static final GroovyTokenId LITERAL_long = new GroovyTokenId("LITERAL_long", null, "keyword");
    public static final GroovyTokenId LITERAL_native = new GroovyTokenId("LITERAL_native", null, "keyword");
    public static final GroovyTokenId LITERAL_new = new GroovyTokenId("LITERAL_new", null, "keyword");
    public static final GroovyTokenId LITERAL_null = new GroovyTokenId("LITERAL_null", null, "keyword");
    public static final GroovyTokenId LITERAL_package = new GroovyTokenId("LITERAL_package", null, "keyword");
    public static final GroovyTokenId LITERAL_private = new GroovyTokenId("LITERAL_private", null, "keyword");
    public static final GroovyTokenId LITERAL_protected = new GroovyTokenId("LITERAL_protected", null, "keyword");
    public static final GroovyTokenId LITERAL_public = new GroovyTokenId("LITERAL_public", null, "keyword");
    public static final GroovyTokenId LITERAL_return = new GroovyTokenId("LITERAL_return", null, "keyword");
    public static final GroovyTokenId LITERAL_short = new GroovyTokenId("LITERAL_short", null, "keyword");
    public static final GroovyTokenId LITERAL_static = new GroovyTokenId("LITERAL_static", null, "keyword");
    public static final GroovyTokenId LITERAL_super = new GroovyTokenId("LITERAL_super", null, "keyword");
    public static final GroovyTokenId LITERAL_switch = new GroovyTokenId("LITERAL_switch", null, "keyword");
    public static final GroovyTokenId LITERAL_synchronized = new GroovyTokenId("LITERAL_synchronized", null, "keyword");
    public static final GroovyTokenId LITERAL_this = new GroovyTokenId("LITERAL_this", null, "keyword");
    public static final GroovyTokenId LITERAL_threadsafe = new GroovyTokenId("LITERAL_threadsafe", null, "keyword");
    public static final GroovyTokenId LITERAL_throw = new GroovyTokenId("LITERAL_throw", null, "keyword");
    public static final GroovyTokenId LITERAL_throws = new GroovyTokenId("LITERAL_throws", null, "keyword");
    public static final GroovyTokenId LITERAL_transient = new GroovyTokenId("LITERAL_transient", null, "keyword");
    public static final GroovyTokenId LITERAL_true = new GroovyTokenId("LITERAL_true", null, "keyword");
    public static final GroovyTokenId LITERAL_try = new GroovyTokenId("LITERAL_try", null, "keyword");
    public static final GroovyTokenId LITERAL_void = new GroovyTokenId("LITERAL_void", null, "keyword");
    public static final GroovyTokenId LITERAL_volatile = new GroovyTokenId("LITERAL_volatile", null, "keyword");
    public static final GroovyTokenId LITERAL_while = new GroovyTokenId("LITERAL_while", null, "keyword");
    public static final GroovyTokenId LNOT = new GroovyTokenId("LNOT", null, "operator");
    public static final GroovyTokenId LOR = new GroovyTokenId("LOR", null, "operator");
    public static final GroovyTokenId LT = new GroovyTokenId("LT", null, "operator");
    public static final GroovyTokenId MAP_CONSTRUCTOR = new GroovyTokenId("MAP_CONSTRUCTOR", null, "default");
    public static final GroovyTokenId MEMBER_POINTER = new GroovyTokenId("MEMBER_POINTER", null, "operator");
    public static final GroovyTokenId METHOD_CALL = new GroovyTokenId("METHOD_CALL", null, "default");
    public static final GroovyTokenId METHOD_DEF = new GroovyTokenId("METHOD_DEF", null, "default");
    public static final GroovyTokenId MINUS_ASSIGN = new GroovyTokenId("MINUS_ASSIGN", null, "operator");
    public static final GroovyTokenId MINUS = new GroovyTokenId("MINUS", null, "operator");
    public static final GroovyTokenId MOD_ASSIGN = new GroovyTokenId("MOD_ASSIGN", null, "operator");
    public static final GroovyTokenId MOD = new GroovyTokenId("MOD", null, "operator");
    public static final GroovyTokenId MODIFIERS = new GroovyTokenId("MODIFIERS", null, "default");
    public static final GroovyTokenId NLS = new GroovyTokenId("NLS", null, "default");
    public static final GroovyTokenId NOT_EQUAL = new GroovyTokenId("NOT_EQUAL", null, "operator");
    public static final GroovyTokenId NULL_TREE_LOOKAHEAD = new GroovyTokenId("NULL_TREE_LOOKAHEAD", null, "default");
    public static final GroovyTokenId NUM_BIG_DECIMAL = new GroovyTokenId("NUM_BIG_DECIMAL", null, "number");
    public static final GroovyTokenId NUM_BIG_INT = new GroovyTokenId("NUM_BIG_INT", null, "number");
    public static final GroovyTokenId NUM_DOUBLE = new GroovyTokenId("NUM_DOUBLE", null, "number");
    public static final GroovyTokenId NUM_FLOAT = new GroovyTokenId("NUM_FLOAT", null, "number");
    public static final GroovyTokenId NUM_INT = new GroovyTokenId("NUM_INT", null, "number");
    public static final GroovyTokenId NUM_LONG = new GroovyTokenId("NUM_LONG", null, "number");
    public static final GroovyTokenId OBJBLOCK = new GroovyTokenId("OBJBLOCK", null, "default");
    public static final GroovyTokenId ONE_NL = new GroovyTokenId("ONE_NL", null, "default");
    public static final GroovyTokenId OPTIONAL_DOT = new GroovyTokenId("OPTIONAL_DOT", null, "operator");
    public static final GroovyTokenId PACKAGE_DEF = new GroovyTokenId("PACKAGE_DEF", null, "default");
    public static final GroovyTokenId PARAMETER_DEF = new GroovyTokenId("PARAMETER_DEF", null, "default");
    public static final GroovyTokenId PARAMETERS = new GroovyTokenId("PARAMETERS", null, "default");
    public static final GroovyTokenId PLUS_ASSIGN = new GroovyTokenId("PLUS_ASSIGN", null, "operator");
    public static final GroovyTokenId PLUS = new GroovyTokenId("PLUS", null, "operator");
    public static final GroovyTokenId POST_DEC = new GroovyTokenId("POST_DEC", null, "default");
    public static final GroovyTokenId POST_INC = new GroovyTokenId("POST_INC", null, "default");
    public static final GroovyTokenId QUESTION = new GroovyTokenId("QUESTION", null, "operator");
    public static final GroovyTokenId RANGE_EXCLUSIVE = new GroovyTokenId("RANGE_EXCLUSIVE", null, "operator");
    public static final GroovyTokenId RANGE_INCLUSIVE = new GroovyTokenId("RANGE_INCLUSIVE", null, "operator");
    public static final GroovyTokenId REGEX_FIND = new GroovyTokenId("REGEX_FIND", null, "operator");
    public static final GroovyTokenId REGEX_MATCH = new GroovyTokenId("REGEX_MATCH", null, "operator");
    public static final GroovyTokenId REGEXP_SYMBOL = new GroovyTokenId("REGEXP_SYMBOL", null, "default");
    public static final GroovyTokenId SELECT_SLOT = new GroovyTokenId("SELECT_SLOT", null, "default");
    public static final GroovyTokenId SEMI = new GroovyTokenId("SEMI", null, "operator");
    public static final GroovyTokenId SH_COMMENT = new GroovyTokenId("SH_COMMENT", null, "comment");
    public static final GroovyTokenId SL_ASSIGN = new GroovyTokenId("SL_ASSIGN", null, "operator");
    public static final GroovyTokenId SL_COMMENT = new GroovyTokenId("SL_COMMENT", null, "comment");
    public static final GroovyTokenId SL = new GroovyTokenId("SL", null, "operator");
    public static final GroovyTokenId SLIST = new GroovyTokenId("SLIST", null, "default");
    public static final GroovyTokenId SPREAD_ARG = new GroovyTokenId("SPREAD_ARG", null, "default");
    public static final GroovyTokenId SPREAD_DOT = new GroovyTokenId("SPREAD_DOT", null, "operator");
    public static final GroovyTokenId SPREAD_MAP_ARG = new GroovyTokenId("SPREAD_MAP_ARG", null, "default");
    public static final GroovyTokenId SR_ASSIGN = new GroovyTokenId("SR_ASSIGN", null, "operator");
    public static final GroovyTokenId SR = new GroovyTokenId("SR", null, "operator");
    public static final GroovyTokenId STAR_ASSIGN = new GroovyTokenId("STAR_ASSIGN", null, "operator");
    public static final GroovyTokenId STAR_STAR_ASSIGN = new GroovyTokenId("STAR_STAR_ASSIGN", null, "operator");
    public static final GroovyTokenId STAR_STAR = new GroovyTokenId("STAR_STAR", null, "operator");
    public static final GroovyTokenId STAR = new GroovyTokenId("STAR", null, "operator");
    public static final GroovyTokenId STATIC_IMPORT = new GroovyTokenId("STATIC_IMPORT", null, "default");
    public static final GroovyTokenId STATIC_INIT = new GroovyTokenId("STATIC_INIT", null, "default");
    public static final GroovyTokenId STRICTFP = new GroovyTokenId("STRICTFP", null, "default");
    public static final GroovyTokenId STRING_CONSTRUCTOR = new GroovyTokenId("STRING_CONSTRUCTOR", null, "string");
    public static final GroovyTokenId STRING_CTOR_END = new GroovyTokenId("STRING_CTOR_END", null, "string");
    public static final GroovyTokenId STRING_CTOR_MIDDLE = new GroovyTokenId("STRING_CTOR_MIDDLE", null, "string");
    public static final GroovyTokenId STRING_CTOR_START = new GroovyTokenId("STRING_CTOR_START", null, "string");
    public static final GroovyTokenId STRING_CH = new GroovyTokenId("STRING_CH", null, "string");
    public static final GroovyTokenId STRING_NL = new GroovyTokenId("STRING_NL", null, "string");
    public static final GroovyTokenId SUPER_CTOR_CALL = new GroovyTokenId("SUPER_CTOR_CALL", null, "default");
    public static final GroovyTokenId TRIPLE_DOT = new GroovyTokenId("TRIPLE_DOT", null, "operator");
    public static final GroovyTokenId TYPE_ARGUMENT = new GroovyTokenId("TYPE_ARGUMENT", null, "default");
    public static final GroovyTokenId TYPE_ARGUMENTS = new GroovyTokenId("TYPE_ARGUMENTS", null, "default");
    public static final GroovyTokenId TYPE_LOWER_BOUNDS = new GroovyTokenId("TYPE_LOWER_BOUNDS", null, "default");
    public static final GroovyTokenId TYPE_PARAMETER = new GroovyTokenId("TYPE_PARAMETER", null, "default");
    public static final GroovyTokenId TYPE_PARAMETERS = new GroovyTokenId("TYPE_PARAMETERS", null, "default");
    public static final GroovyTokenId TYPE_UPPER_BOUNDS = new GroovyTokenId("TYPE_UPPER_BOUNDS", null, "default");
    public static final GroovyTokenId TYPE = new GroovyTokenId("TYPE", null, "default");
    public static final GroovyTokenId TYPECAST = new GroovyTokenId("TYPECAST", null, "default");
    public static final GroovyTokenId UNARY_MINUS = new GroovyTokenId("UNARY_MINUS", null, "default");
    public static final GroovyTokenId UNARY_PLUS = new GroovyTokenId("UNARY_PLUS", null, "default");
    public static final GroovyTokenId UNUSED_CONST = new GroovyTokenId("UNUSED_CONST", null, "default");
    public static final GroovyTokenId UNUSED_DO = new GroovyTokenId("UNUSED_DO", null, "default");
    public static final GroovyTokenId UNUSED_GOTO = new GroovyTokenId("UNUSED_GOTO", null, "default");
    public static final GroovyTokenId VARIABLE_DEF = new GroovyTokenId("VARIABLE_DEF", null, "default");
    public static final GroovyTokenId VARIABLE_PARAMETER_DEF = new GroovyTokenId("VARIABLE_PARAMETER_DEF", null, "default");
    public static final GroovyTokenId VOCAB = new GroovyTokenId("VOCAB", null, "default");
    public static final GroovyTokenId WILDCARD_TYPE = new GroovyTokenId("WILDCARD_TYPE", null, "default");
    
    
    // </editor-fold>
    
    private static final Language<GroovyTokenId> language =
        new LanguageHierarchy<GroovyTokenId>() {

            protected String mimeType() {
                return "text/x-groovy"; // NOI18N
            }

            protected Collection<GroovyTokenId> createTokenIds() {
                return getUsedTokens();
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

    public static List<GroovyTokenId> getUsedTokens() {
        List<GroovyTokenId> types = new ArrayList<GroovyTokenId>();
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
        types.add(CLOSED_BLOCK);
        types.add(CLOSURE_OP);
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
        types.add(IDENTIFIER);
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
        types.add(LBRACE);
        types.add(LBRACKET);
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
        types.add(BLOCK_COMMENT);
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
        types.add(RBRACE);
        types.add(RBRACKET);
        types.add(REGEX_FIND);
        types.add(REGEX_MATCH);
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
        types.add(WHITESPACE);
        types.add(ERROR);
        return types;
    }

    public static GroovyTokenId getTokenId(int token) {
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
                    return GroovyTokenId.CLOSED_BLOCK;
            case GroovyTokenTypes.CLOSURE_LIST:
                    return GroovyTokenId.CLOSURE_OP;
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
                    return GroovyTokenId.IDENTIFIER;
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
                    return GroovyTokenId.LBRACKET;
            case GroovyTokenTypes.LCURLY:
                    return GroovyTokenId.LBRACE;
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
                    return GroovyTokenId.BLOCK_COMMENT;
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
            case GroovyTokenTypes.RCURLY:
                    return GroovyTokenId.RBRACE;
            case GroovyTokenTypes.RBRACK:
                    return GroovyTokenId.RBRACKET;
            case GroovyTokenTypes.REGEX_FIND:
                    return GroovyTokenId.REGEX_FIND;
            case GroovyTokenTypes.REGEX_MATCH:
                    return GroovyTokenId.REGEX_MATCH;
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
                    return GroovyTokenId.WHITESPACE;
            default:
                return GroovyTokenId.IDENTIFIER;
        }
    }

}
