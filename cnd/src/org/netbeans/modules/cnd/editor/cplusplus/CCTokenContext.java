/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.HashMap;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
 * CC token-context defines token-ids and token-categories
 * used in CC language.
 */
public class CCTokenContext extends TokenContext {

    // Token category-ids
    public static final int KEYWORDS_ID           = 1;
    public static final int OPERATORS_ID          = KEYWORDS_ID + 1;
    public static final int NUMERIC_LITERALS_ID   = OPERATORS_ID + 1;
    public static final int ERRORS_ID             = NUMERIC_LITERALS_ID + 1;
    // XXX: need to use CPP_ID somewhere to get special highlighting
    public static final int CPP_ID		  = ERRORS_ID + 1;
    
    // Numeric-ids for token-ids
    public static final int WHITESPACE_ID         = CPP_ID + 1;
    public static final int IDENTIFIER_ID         = WHITESPACE_ID + 1;
    public static final int LINE_COMMENT_ID       = IDENTIFIER_ID + 1;
    public static final int BLOCK_COMMENT_ID      = LINE_COMMENT_ID + 1;
    public static final int CHAR_LITERAL_ID       = BLOCK_COMMENT_ID + 1;
    public static final int STRING_LITERAL_ID     = CHAR_LITERAL_ID + 1;
    public static final int INT_LITERAL_ID        = STRING_LITERAL_ID + 1;
    public static final int LONG_LITERAL_ID       = INT_LITERAL_ID + 1;
    public static final int HEX_LITERAL_ID        = LONG_LITERAL_ID + 1;
    public static final int OCTAL_LITERAL_ID      = HEX_LITERAL_ID + 1;
    public static final int FLOAT_LITERAL_ID      = OCTAL_LITERAL_ID + 1;
    public static final int DOUBLE_LITERAL_ID     = FLOAT_LITERAL_ID + 1;
    public static final int BACKSLASH_ID          = DOUBLE_LITERAL_ID + 1;
    public static final int LINE_CONTINUATION_ID  = BACKSLASH_ID + 1; // not used


    // Operator numeric-ids
    public static final int HASH_ID = LINE_CONTINUATION_ID + 1; // #
    public static final int DOUBLE_HASH_ID = HASH_ID + 1; // ##
    public static final int EQ_ID = DOUBLE_HASH_ID + 1;   // =
    public static final int LT_ID = EQ_ID + 1;            // <
    public static final int GT_ID = LT_ID + 1;            // >
    public static final int LSHIFT_ID = GT_ID + 1;        // <<
    public static final int RSSHIFT_ID = LSHIFT_ID + 1;   // >>
    public static final int PLUS_ID = RSSHIFT_ID + 1;     // +
    public static final int MINUS_ID = PLUS_ID + 1;       // -
    public static final int MUL_ID = MINUS_ID + 1;        // *
    public static final int DIV_ID = MUL_ID + 1;          // /
    public static final int AND_ID = DIV_ID + 1;          // &
    public static final int OR_ID = AND_ID + 1;           // |
    public static final int XOR_ID = OR_ID + 1;           // ^
    public static final int MOD_ID = XOR_ID + 1;          // %
    public static final int NOT_ID = MOD_ID + 1;          // !
    public static final int NEG_ID = NOT_ID + 1;          // ~
    public static final int EQ_EQ_ID = NEG_ID + 1;        // ==
    public static final int LT_EQ_ID = EQ_EQ_ID + 1;      // <=
    public static final int GT_EQ_ID = LT_EQ_ID + 1;         // >=
    public static final int LSHIFT_EQ_ID = GT_EQ_ID + 1;  // <<=
    public static final int RSSHIFT_EQ_ID = LSHIFT_EQ_ID + 1; // >>=
    public static final int PLUS_EQ_ID = RSSHIFT_EQ_ID + 1; // +=
    public static final int MINUS_EQ_ID = PLUS_EQ_ID + 1; // -=
    public static final int ARROW_ID = MINUS_EQ_ID + 1; // ->
    public static final int ARROWMBR_ID = ARROW_ID + 1;  // ->*
    public static final int MUL_EQ_ID = ARROWMBR_ID + 1;  // *=
    public static final int DIV_EQ_ID = MUL_EQ_ID + 1;    // /=
    public static final int AND_EQ_ID = DIV_EQ_ID + 1;    // &=
    public static final int OR_EQ_ID = AND_EQ_ID + 1;     // |=
    public static final int XOR_EQ_ID = OR_EQ_ID + 1;     // ^=
    public static final int MOD_EQ_ID = XOR_EQ_ID + 1;    // %=
    public static final int NOT_EQ_ID = MOD_EQ_ID + 1;    // !=
    public static final int DOT_ID = NOT_EQ_ID + 1;       // .
    public static final int DOTMBR_ID = DOT_ID + 1;       // .*
    public static final int COMMA_ID = DOTMBR_ID + 1;        // ,
    public static final int COLON_ID = COMMA_ID + 1;      // :
    public static final int SCOPE_ID = COLON_ID + 1;      // ::
    public static final int SEMICOLON_ID = SCOPE_ID + 1;  // ;
    public static final int QUESTION_ID = SEMICOLON_ID + 1; // ?
    public static final int LPAREN_ID = QUESTION_ID + 1;  // (
    public static final int RPAREN_ID = LPAREN_ID + 1;    // )
    public static final int LBRACKET_ID = RPAREN_ID + 1;  // [
    public static final int RBRACKET_ID = LBRACKET_ID + 1; // ]
    public static final int LBRACE_ID = RBRACKET_ID + 1;  // {
    public static final int RBRACE_ID = LBRACE_ID + 1;    // }
    public static final int PLUS_PLUS_ID = RBRACE_ID + 1; // ++
    public static final int MINUS_MINUS_ID = PLUS_PLUS_ID + 1; // --
    public static final int AND_AND_ID = MINUS_MINUS_ID + 1; // &&
    public static final int OR_OR_ID = AND_AND_ID + 1;    // ||

    // Data type keyword numeric-ids
    public static final int ASM_ID = OR_OR_ID + 1;
    public static final int AUTO_ID = ASM_ID + 1;
    public static final int BOOLEAN_ID = AUTO_ID + 1;
    public static final int CHAR_ID = BOOLEAN_ID + 1;
    public static final int DOUBLE_ID = CHAR_ID + 1;
    public static final int ENUM_ID = DOUBLE_ID + 1;
    public static final int EXPORT_ID = ENUM_ID + 1;
    public static final int FLOAT_ID = EXPORT_ID + 1;
    public static final int INLINE_ID = FLOAT_ID + 1;
    public static final int INT_ID = INLINE_ID + 1;
    public static final int LONG_ID = INT_ID + 1;
    public static final int MUTABLE_ID = LONG_ID + 1;
    public static final int REGISTER_ID = MUTABLE_ID + 1;
    public static final int SHORT_ID = REGISTER_ID + 1;
    public static final int SIGNED_ID = SHORT_ID + 1;
    public static final int STRUCT_ID = SIGNED_ID + 1;
    public static final int TYPEDEF_ID = STRUCT_ID + 1;
    public static final int TYPEID_ID = TYPEDEF_ID + 1;
    public static final int TYPENAME_ID = TYPEID_ID + 1;
    public static final int TYPEOF_ID = TYPENAME_ID + 1;
    public static final int UNSIGNED_ID = TYPEOF_ID + 1;

    // Void type keyword numeric-id
    public static final int VOID_ID = UNSIGNED_ID + 1;
    public static final int WCHAR_T_ID = VOID_ID + 1;
    public static final int UNION_ID = WCHAR_T_ID + 1;

    // Other keywords numeric-ids
    public static final int BREAK_ID = UNION_ID + 1;
    public static final int CASE_ID = BREAK_ID + 1;
    public static final int CATCH_ID = CASE_ID + 1;
    public static final int CLASS_ID = CATCH_ID + 1;
    public static final int CONST_ID = CLASS_ID + 1;
    public static final int CONTINUE_ID = CONST_ID + 1;
    public static final int DEFAULT_ID = CONTINUE_ID + 1;
    public static final int DELETE_ID = DEFAULT_ID + 1;
    public static final int DO_ID = DELETE_ID + 1;
    public static final int ELSE_ID = DO_ID + 1;
    public static final int EXPLICIT_ID = ELSE_ID + 1;
    public static final int EXTERN_ID = EXPLICIT_ID + 1;  
    public static final int FALSE_ID = EXTERN_ID + 1;
    public static final int FOR_ID = FALSE_ID + 1;
    public static final int FRIEND_ID = FOR_ID + 1;
    public static final int GOTO_ID = FOR_ID + 1;
    public static final int IF_ID = GOTO_ID + 1;
    public static final int NAMESPACE_ID = IF_ID + 1;
    public static final int NEW_ID = NAMESPACE_ID + 1;
    public static final int NULL_ID = NEW_ID + 1;
    public static final int OPERATOR_ID = NULL_ID + 1;
    public static final int PRIVATE_ID = OPERATOR_ID + 1;
    public static final int PROTECTED_ID = PRIVATE_ID + 1;
    public static final int PUBLIC_ID = PROTECTED_ID + 1;
    public static final int RETURN_ID = PUBLIC_ID + 1;
    public static final int SIZEOF_ID = RETURN_ID + 1;
    public static final int STATIC_ID = SIZEOF_ID + 1;
    public static final int SWITCH_ID = STATIC_ID + 1;
    public static final int TEMPLATE_ID = SWITCH_ID + 1;
    public static final int THIS_ID = TEMPLATE_ID + 1;
    public static final int THROW_ID = THIS_ID + 1;
    public static final int TRUE_ID = THROW_ID + 1;
    public static final int TRY_ID = TRUE_ID + 1;
    public static final int USING_ID = TRY_ID + 1;
    public static final int VIRTUAL_ID = USING_ID + 1;
    public static final int VOLATILE_ID = VIRTUAL_ID + 1;
    public static final int WHILE_ID = VOLATILE_ID + 1;

    public static final int DYNAMIC_CAST_ID = WHILE_ID + 1;
    public static final int STATIC_CAST_ID = DYNAMIC_CAST_ID + 1;
    public static final int REINTERPRET_CAST_ID = STATIC_CAST_ID + 1;
    public static final int CONST_CAST_ID = REINTERPRET_CAST_ID + 1;
    
    // New C keywords
    public static final int RESTRICT_ID = CONST_CAST_ID + 1;
    public static final int _BOOL_ID = RESTRICT_ID + 1;
    public static final int _COMPLEX_ID = _BOOL_ID + 1;
    public static final int _IMAGINARY_ID = _COMPLEX_ID + 1;

    // Preprocessor directives
    public static final int CPPDEFINE_ID = _IMAGINARY_ID + 1;
    public static final int CPPELIF_ID = CPPDEFINE_ID + 1;
    public static final int CPPELSE_ID = CPPELIF_ID + 1;
    public static final int CPPENDIF_ID = CPPELSE_ID + 1;
    public static final int CPPERROR_ID = CPPENDIF_ID + 1;
    public static final int CPPIF_ID = CPPERROR_ID + 1;
    public static final int CPPIFDEF_ID = CPPIF_ID + 1;
    public static final int CPPIFNDEF_ID = CPPIFDEF_ID + 1;
    public static final int CPPINCLUDE_ID = CPPIFNDEF_ID + 1;
    public static final int CPPINCLUDE_NEXT_ID = CPPINCLUDE_ID + 1;
    public static final int CPPLINE_ID = CPPINCLUDE_NEXT_ID + 1;
    public static final int CPPPRAGMA_ID = CPPLINE_ID + 1;
    public static final int CPPUNDEF_ID = CPPPRAGMA_ID + 1;
    public static final int CPPWARNING_ID = CPPUNDEF_ID + 1;
    public static final int CPPIDENTIFIER_ID = CPPWARNING_ID + 1; // not recognized # id

    // include directives
    public static final int SYS_INCLUDE_ID = CPPIDENTIFIER_ID + 1; // <filename>
    public static final int USR_INCLUDE_ID = SYS_INCLUDE_ID + 1; // "filename"
    
    // Incomplete tokens
    public static final int INCOMPLETE_STRING_LITERAL_ID = USR_INCLUDE_ID + 1;
    public static final int INCOMPLETE_CHAR_LITERAL_ID = INCOMPLETE_STRING_LITERAL_ID + 1;
    public static final int INCOMPLETE_HEX_LITERAL_ID = INCOMPLETE_CHAR_LITERAL_ID + 1;
    public static final int INVALID_CHAR_ID = INCOMPLETE_HEX_LITERAL_ID + 1;
    public static final int INVALID_OPERATOR_ID = INVALID_CHAR_ID + 1;
    public static final int INVALID_OCTAL_LITERAL_ID = INVALID_OPERATOR_ID + 1;
    public static final int INVALID_COMMENT_END_ID = INVALID_OCTAL_LITERAL_ID + 1;
    public static final int INVALID_BACKSLASH_ID = INVALID_COMMENT_END_ID + 1;
    public static final int INCOMPLETE_SYS_INCLUDE_ID = INVALID_BACKSLASH_ID + 1; 
    public static final int INCOMPLETE_USR_INCLUDE_ID = INCOMPLETE_SYS_INCLUDE_ID + 1; 

    // Token-categories
    /** All the keywords belong to this category. */
    public static final BaseTokenCategory KEYWORDS
    = new BaseTokenCategory("keywords", KEYWORDS_ID);  // NOI18N

    /** All the operators belong to this category. */
    public static final BaseTokenCategory OPERATORS
    = new BaseTokenCategory("operators", OPERATORS_ID);  // NOI18N

    /** All the numeric literals belong to this category. */
    public static final BaseTokenCategory NUMERIC_LITERALS
    = new BaseTokenCategory("numeric-literals", NUMERIC_LITERALS_ID); // NOI18N

    /** All the errorneous constructions and incomplete tokens
     * belong to this category.
     */
    public static final BaseTokenCategory ERRORS
    = new BaseTokenCategory("errors", ERRORS_ID);  // NOI18N

    /* All C preprocessor directive keywords belong to this category */
    public static final BaseTokenCategory CPP
    = new BaseTokenCategory("preprocessor", CPP_ID);  // NOI18N


    // Token-ids
    public static final BaseTokenID WHITESPACE
    = new BaseTokenID("whitespace", WHITESPACE_ID);  // NOI18N

    public static final BaseTokenID BACKSLASH
    = new BaseTokenID("backslash", BACKSLASH_ID);  // NOI18N
    
    public static final BaseTokenID LINE_CONTINUATION
	= new BaseTokenID("line-continuation", LINE_CONTINUATION_ID); // NOI18N

    public static final BaseTokenID IDENTIFIER
    = new BaseTokenID("identifier", IDENTIFIER_ID);  // NOI18N

    /** Comment with the '//' prefix */
    public static final BaseTokenID LINE_COMMENT
    = new BaseTokenID("line-comment", LINE_COMMENT_ID);  // NOI18N

    /** Block comment */
    public static final BaseTokenID BLOCK_COMMENT
    = new BaseTokenID("block-comment", BLOCK_COMMENT_ID);  // NOI18N

    /** Character literal e.g. 'c' */
    public static final BaseTokenID CHAR_LITERAL
    = new BaseTokenID("char-literal", CHAR_LITERAL_ID);  // NOI18N

    /** CC string literal e.g. "hello" */
    public static final BaseTokenID STRING_LITERAL
    = new BaseTokenID("string-literal", STRING_LITERAL_ID);  // NOI18N

    /** CC integer literal e.g. 1234 */
    public static final BaseTokenID INT_LITERAL
    = new BaseTokenID("int-literal", INT_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** CC long literal e.g. 12L */
    public static final BaseTokenID LONG_LITERAL
    = new BaseTokenID("long-literal", LONG_LITERAL_ID, NUMERIC_LITERALS);// NOI18N

    /** CC hexadecimal literal e.g. 0x5a */
    public static final BaseTokenID HEX_LITERAL
    = new BaseTokenID("hex-literal", HEX_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** CC octal literal e.g. 0123 */
    public static final BaseTokenID OCTAL_LITERAL
	= new BaseTokenID("octal-literal",  // NOI18N
			     OCTAL_LITERAL_ID, NUMERIC_LITERALS);
    /** CC float literal e.g. 1.5e+20f */
    public static final BaseTokenID FLOAT_LITERAL
	= new BaseTokenID("float-literal",  // NOI18N
			     FLOAT_LITERAL_ID, NUMERIC_LITERALS);

    /** CC double literal e.g. 1.5e+20 */
    public static final BaseTokenID DOUBLE_LITERAL
	= new BaseTokenID("double-literal",  // NOI18N
			     DOUBLE_LITERAL_ID, NUMERIC_LITERALS);

    /** PP system include <filename> */
    public static final BaseTokenID SYS_INCLUDE
    = new BaseTokenID("sys-include", SYS_INCLUDE_ID);  // NOI18N

    /** PP user include "filename" */
    public static final BaseTokenID USR_INCLUDE
    = new BaseTokenID("user-include", USR_INCLUDE_ID);  // NOI18N
    
    // Operators
    public static final BaseImageTokenID HASH
    = new BaseImageTokenID("hash", HASH_ID, CPP, "#"); // NOI18N

    public static final BaseImageTokenID DOUBLE_HASH
    = new BaseImageTokenID("double-hash", DOUBLE_HASH_ID, CPP, "##"); // NOI18N     
        
    public static final BaseImageTokenID EQ
    = new BaseImageTokenID("eq", EQ_ID, OPERATORS, "=");  // NOI18N

    public static final BaseImageTokenID LT
    = new BaseImageTokenID("lt", LT_ID, OPERATORS, "<");  // NOI18N

    public static final BaseImageTokenID GT
    = new BaseImageTokenID("gt", GT_ID, OPERATORS, ">");  // NOI18N

    public static final BaseImageTokenID LSHIFT
    = new BaseImageTokenID("lshift", LSHIFT_ID, OPERATORS, "<<");  // NOI18N

    public static final BaseImageTokenID RSSHIFT
    = new BaseImageTokenID("rsshift", RSSHIFT_ID, OPERATORS, ">>");  // NOI18N

    public static final BaseImageTokenID PLUS
    = new BaseImageTokenID("plus", PLUS_ID, OPERATORS, "+");  // NOI18N

    public static final BaseImageTokenID MINUS
    = new BaseImageTokenID("minus", MINUS_ID, OPERATORS, "-");  // NOI18N

    public static final BaseImageTokenID MUL
    = new BaseImageTokenID("mul", MUL_ID, OPERATORS, "*");  // NOI18N

    public static final BaseImageTokenID DIV
    = new BaseImageTokenID("div", DIV_ID, OPERATORS, "/");  // NOI18N

    public static final BaseImageTokenID AND
    = new BaseImageTokenID("and", AND_ID, OPERATORS, "&");  // NOI18N

    public static final BaseImageTokenID OR
    = new BaseImageTokenID("or", OR_ID, OPERATORS, "|");  // NOI18N

    public static final BaseImageTokenID XOR
    = new BaseImageTokenID("xor", XOR_ID, OPERATORS, "^");  // NOI18N

    public static final BaseImageTokenID MOD
    = new BaseImageTokenID("mod", MOD_ID, OPERATORS, "%");  // NOI18N

    public static final BaseImageTokenID NOT
    = new BaseImageTokenID("not", NOT_ID, OPERATORS, "!");  // NOI18N

    public static final BaseImageTokenID NEG
    = new BaseImageTokenID("neg", NEG_ID, OPERATORS, "~");  // NOI18N


    public static final BaseImageTokenID EQ_EQ
    = new BaseImageTokenID("eq-eq", EQ_EQ_ID, OPERATORS, "==");  // NOI18N

    public static final BaseImageTokenID LT_EQ
    = new BaseImageTokenID("le", LT_EQ_ID, OPERATORS, "<=");  // NOI18N

    public static final BaseImageTokenID GT_EQ
    = new BaseImageTokenID("ge", GT_EQ_ID, OPERATORS, ">=");  // NOI18N

    public static final BaseImageTokenID LSHIFT_EQ
    = new BaseImageTokenID("lshift-eq", LSHIFT_EQ_ID, OPERATORS, "<<=");// NOI18N

    public static final BaseImageTokenID RSSHIFT_EQ
    = new BaseImageTokenID("rsshift-eq", RSSHIFT_EQ_ID, OPERATORS, ">>=");// NOI18N

    public static final BaseImageTokenID PLUS_EQ
    = new BaseImageTokenID("plus-eq", PLUS_EQ_ID, OPERATORS, "+=");  // NOI18N

    public static final BaseImageTokenID MINUS_EQ
    = new BaseImageTokenID("minus-eq", MINUS_EQ_ID, OPERATORS, "-=");  // NOI18N

    public static final BaseImageTokenID ARROW
    = new BaseImageTokenID("arrow", ARROW_ID, OPERATORS, "->");  // NOI18N

    public static final BaseImageTokenID ARROWMBR
    = new BaseImageTokenID("arrow-member", ARROWMBR_ID, OPERATORS, "->*");  // NOI18N
    
    public static final BaseImageTokenID MUL_EQ
    = new BaseImageTokenID("mul-eq", MUL_EQ_ID, OPERATORS, "*=");  // NOI18N

    public static final BaseImageTokenID DIV_EQ
    = new BaseImageTokenID("div-eq", DIV_EQ_ID, OPERATORS, "/=");  // NOI18N

    public static final BaseImageTokenID AND_EQ
    = new BaseImageTokenID("and-eq", AND_EQ_ID, OPERATORS, "&=");  // NOI18N

    public static final BaseImageTokenID OR_EQ
    = new BaseImageTokenID("or-eq", OR_EQ_ID, OPERATORS, "|=");  // NOI18N

    public static final BaseImageTokenID XOR_EQ
    = new BaseImageTokenID("xor-eq", XOR_EQ_ID, OPERATORS, "^=");  // NOI18N

    public static final BaseImageTokenID MOD_EQ
    = new BaseImageTokenID("mod-eq", MOD_EQ_ID, OPERATORS, "%=");  // NOI18N

    public static final BaseImageTokenID NOT_EQ
    = new BaseImageTokenID("not-eq", NOT_EQ_ID, OPERATORS, "!=");  // NOI18N

    public static final BaseImageTokenID DOT
    = new BaseImageTokenID("dot", DOT_ID, OPERATORS, ".");  // NOI18N
    
    public static final BaseImageTokenID DOTMBR
    = new BaseImageTokenID("dot-member", DOTMBR_ID, OPERATORS, ".*");  // NOI18N

    public static final BaseImageTokenID COMMA
    = new BaseImageTokenID("comma", COMMA_ID, OPERATORS, ",");  // NOI18N

    public static final BaseImageTokenID COLON
    = new BaseImageTokenID("colon", COLON_ID, OPERATORS, ":");  // NOI18N

    public static final BaseImageTokenID SCOPE
    = new BaseImageTokenID("scope", SCOPE_ID, OPERATORS, "::");  // NOI18N
    
    public static final BaseImageTokenID SEMICOLON
    = new BaseImageTokenID("semicolon", SEMICOLON_ID, OPERATORS, ";"); // NOI18N

    public static final BaseImageTokenID QUESTION
    = new BaseImageTokenID("question", QUESTION_ID, OPERATORS, "?");  // NOI18N

    public static final BaseImageTokenID LPAREN
    = new BaseImageTokenID("lparen", LPAREN_ID, OPERATORS, "(");  // NOI18N

    public static final BaseImageTokenID RPAREN
    = new BaseImageTokenID("rparen", RPAREN_ID, OPERATORS, ")");  // NOI18N

    public static final BaseImageTokenID LBRACKET
    = new BaseImageTokenID("lbracket", LBRACKET_ID, OPERATORS, "[");  // NOI18N

    public static final BaseImageTokenID RBRACKET
    = new BaseImageTokenID("rbracket", RBRACKET_ID, OPERATORS, "]");  // NOI18N

    public static final BaseImageTokenID LBRACE
    = new BaseImageTokenID("lbrace", LBRACE_ID, OPERATORS, "{");  // NOI18N

    public static final BaseImageTokenID RBRACE
    = new BaseImageTokenID("rbrace", RBRACE_ID, OPERATORS, "}");  // NOI18N

    public static final BaseImageTokenID PLUS_PLUS
    = new BaseImageTokenID("plus-plus", PLUS_PLUS_ID, OPERATORS, "++"); // NOI18N

    public static final BaseImageTokenID MINUS_MINUS
	= new BaseImageTokenID("minus-minus",  // NOI18N
			       MINUS_MINUS_ID, OPERATORS, "--"); // NOI18N

    public static final BaseImageTokenID AND_AND
    = new BaseImageTokenID("and-and", AND_AND_ID, OPERATORS, "&&");  // NOI18N

    public static final BaseImageTokenID OR_OR
    = new BaseImageTokenID("or-or", OR_OR_ID, OPERATORS, "||");  // NOI18N


    // Data types
    public static final BaseImageTokenID ASM
    = new BaseImageTokenID("asm", ASM_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID AUTO
    = new BaseImageTokenID("auto", AUTO_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID BOOLEAN
    = new BaseImageTokenID("bool", BOOLEAN_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID CHAR
    = new BaseImageTokenID("char", CHAR_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID DOUBLE
    = new BaseImageTokenID("double", DOUBLE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID ENUM
    = new BaseImageTokenID("enum", ENUM_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID EXPORT
    = new BaseImageTokenID("export", EXPORT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID FLOAT
    = new BaseImageTokenID("float", FLOAT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID INT
    = new BaseImageTokenID("int", INT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID LONG
    = new BaseImageTokenID("long", LONG_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID MUTABLE
    = new BaseImageTokenID("mutable", MUTABLE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID REGISTER
    = new BaseImageTokenID("register", REGISTER_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID SHORT
    = new BaseImageTokenID("short", SHORT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID SIGNED
    = new BaseImageTokenID("signed", SIGNED_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID STRUCT
    = new BaseImageTokenID("struct", STRUCT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID TYPEDEF
    = new BaseImageTokenID("typedef", TYPEDEF_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID UNSIGNED
    = new BaseImageTokenID("unsigned", UNSIGNED_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID VOID
    = new BaseImageTokenID("void", VOID_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID WCHAR_T
    = new BaseImageTokenID("wchar_t", WCHAR_T_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID UNION
    = new BaseImageTokenID("union", UNION_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID BREAK
    = new BaseImageTokenID("break", BREAK_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID CASE
    = new BaseImageTokenID("case", CASE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID CATCH
    = new BaseImageTokenID("catch", CATCH_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID CLASS
    = new BaseImageTokenID("class", CLASS_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID CONST
    = new BaseImageTokenID("const", CONST_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID CONTINUE
    = new BaseImageTokenID("continue", CONTINUE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID DEFAULT
    = new BaseImageTokenID("default", DEFAULT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID DELETE
    = new BaseImageTokenID("delete", DELETE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID DO
    = new BaseImageTokenID("do", DO_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID ELSE
    = new BaseImageTokenID("else", ELSE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID EXPLICIT
    = new BaseImageTokenID("explicit", EXPLICIT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID EXTERN
    = new BaseImageTokenID("extern", EXTERN_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID FALSE
    = new BaseImageTokenID("false", FALSE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID FOR
    = new BaseImageTokenID("for", FOR_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID FRIEND
    = new BaseImageTokenID("friend", FRIEND_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID GOTO
    = new BaseImageTokenID("goto", GOTO_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID IF
    = new BaseImageTokenID("if", IF_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID INLINE
    = new BaseImageTokenID("inline", INLINE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID NAMESPACE
    = new BaseImageTokenID("namespace", NAMESPACE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID NEW
    = new BaseImageTokenID("new", NEW_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID NULL
    = new BaseImageTokenID("null", NULL_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID OPERATOR
    = new BaseImageTokenID("operator", OPERATOR_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID PRIVATE
    = new BaseImageTokenID("private", PRIVATE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID PROTECTED
    = new BaseImageTokenID("protected", PROTECTED_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID PUBLIC
    = new BaseImageTokenID("public", PUBLIC_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID RETURN
    = new BaseImageTokenID("return", RETURN_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID SIZEOF
    = new BaseImageTokenID("sizeof", SIZEOF_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID STATIC
    = new BaseImageTokenID("static", STATIC_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID SWITCH
    = new BaseImageTokenID("switch", SWITCH_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID THIS
    = new BaseImageTokenID("this", THIS_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID THROW
    = new BaseImageTokenID("throw", THROW_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID TRUE
    = new BaseImageTokenID("true", TRUE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID TRY
    = new BaseImageTokenID("try", TRY_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID TYPEID
    = new BaseImageTokenID("typeid", TYPEID_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID TYPEOF
    = new BaseImageTokenID("typeof", TYPEOF_ID, KEYWORDS);  // NOI18N    
    
    public static final BaseImageTokenID TYPENAME
    = new BaseImageTokenID("typename", TYPENAME_ID, KEYWORDS);  // NOI18N 

    public static final BaseImageTokenID TEMPLATE
    = new BaseImageTokenID("template", TEMPLATE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID USING
    = new BaseImageTokenID("using", USING_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID VIRTUAL
    = new BaseImageTokenID("virtual", VIRTUAL_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID VOLATILE
    = new BaseImageTokenID("volatile", VOLATILE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID WHILE
    = new BaseImageTokenID("while", WHILE_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID DYNAMIC_CAST
    = new BaseImageTokenID("dynamic_cast", DYNAMIC_CAST_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID STATIC_CAST
    = new BaseImageTokenID("static_cast", STATIC_CAST_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID REINTERPRET_CAST
	= new BaseImageTokenID("reinterpret_cast",  // NOI18N
				    REINTERPRET_CAST_ID, KEYWORDS);

    public static final BaseImageTokenID CONST_CAST
    = new BaseImageTokenID("const_cast", CONST_CAST_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID RESTRICT
    = new BaseImageTokenID("restrict", RESTRICT_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID _BOOL
    = new BaseImageTokenID("_Bool", _BOOL_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID _COMPLEX
    = new BaseImageTokenID("_Complex", _COMPLEX_ID, KEYWORDS);  // NOI18N

    public static final BaseImageTokenID _IMAGINARY
    = new BaseImageTokenID("_Imaginary", _IMAGINARY_ID, KEYWORDS);  // NOI18N

    // Preprocessor directives
    public static final BaseImageTokenID CPPDEFINE
    = new BaseImageTokenID("#define", CPPDEFINE_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPELIF
    = new BaseImageTokenID("#elif", CPPELIF_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPELSE
    = new BaseImageTokenID("#else", CPPELSE_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPENDIF
    = new BaseImageTokenID("#endif", CPPENDIF_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPERROR
    = new BaseImageTokenID("#error", CPPERROR_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPIF
    = new BaseImageTokenID("#if", CPPIF_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPIFDEF
    = new BaseImageTokenID("#ifdef", CPPIFDEF_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPIFNDEF
    = new BaseImageTokenID("#ifndef", CPPIFNDEF_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPINCLUDE
    = new BaseImageTokenID("#include", CPPINCLUDE_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPINCLUDE_NEXT
    = new BaseImageTokenID("#include_next", CPPINCLUDE_NEXT_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPLINE
    = new BaseImageTokenID("#line", CPPLINE_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPPRAGMA
    = new BaseImageTokenID("#pragma", CPPPRAGMA_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPUNDEF
    = new BaseImageTokenID("#undef", CPPUNDEF_ID, CPP);  // NOI18N

    public static final BaseImageTokenID CPPWARNING
    = new BaseImageTokenID("#warning", CPPWARNING_ID, CPP); // NOI18N

    public static final BaseImageTokenID CPPIDENTIFIER
    = new BaseImageTokenID("#preproc-identifier", CPPIDENTIFIER_ID, CPP); // NOI18N
    
    // Incomplete and error token-ids
    public static final BaseTokenID INCOMPLETE_STRING_LITERAL
	= new BaseTokenID("incomplete-string-literal",  // NOI18N
			     INCOMPLETE_STRING_LITERAL_ID, ERRORS);

    public static final BaseTokenID INCOMPLETE_CHAR_LITERAL
	= new BaseTokenID("incomplete-char-literal",  // NOI18N
			     INCOMPLETE_CHAR_LITERAL_ID, ERRORS);

    public static final BaseTokenID INCOMPLETE_HEX_LITERAL
	= new BaseTokenID("incomplete-hex-literal", // NOI18N
			     INCOMPLETE_HEX_LITERAL_ID, ERRORS); 

    public static final BaseTokenID INVALID_CHAR
    = new BaseTokenID("invalid-char", INVALID_CHAR_ID, ERRORS);  // NOI18N

    public static final BaseTokenID INVALID_OPERATOR
    = new BaseTokenID("invalid-operator", INVALID_OPERATOR_ID, ERRORS);// NOI18N

    public static final BaseTokenID INVALID_OCTAL_LITERAL
	= new BaseTokenID("invalid-octal-literal",  // NOI18N
			     INVALID_OCTAL_LITERAL_ID, ERRORS);

    public static final BaseTokenID INVALID_COMMENT_END
	= new BaseTokenID("invalid-comment-end",  // NOI18N
			     INVALID_COMMENT_END_ID, ERRORS);
    
    public static final BaseTokenID INVALID_BACKSLASH
    = new BaseTokenID("invalid-backslash", INVALID_BACKSLASH_ID, ERRORS);  // NOI18N

    public static final BaseTokenID INCOMPLETE_SYS_INCLUDE
    = new BaseTokenID("incomplete-sys-include", INCOMPLETE_SYS_INCLUDE_ID, ERRORS); // NOI18N

    public static final BaseTokenID INCOMPLETE_USR_INCLUDE
    = new BaseTokenID("incomplete-user-include", INCOMPLETE_USR_INCLUDE_ID, ERRORS); // NOI18N
    
    // Context instance declaration
    public static final CCTokenContext context = new CCTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private static final HashMap str2kwd = new HashMap();

  /*    static {
        BaseImageTokenID[] kwds = new BaseImageTokenID[] {
            BREAK, CASE, CATCH, CLASS, CONST, CONST_CAST, CONTINUE, 
	      DEFAULT, DELETE, DO, DYNAMIC_CAST,
            ELSE, EXPLICIT, EXPORT, EXTERN, FALSE, FOR, FRIEND, GOTO, IF, 
            NAMESPACE, NEW, NULL, OPERATOR, PRIVATE, 
            PROTECTED, PUBLIC, REINTERPRET_CAST, RETURN, 
	      SIZEOF, STATIC, STATIC_CAST, SWITCH, 
	      TEMPLATE, THIS, THROW, TRUE, TRY, TYPEID, TYPENAME,  
	      USING, VIRTUAL, VOLATILE, WHILE
        };

        for (int i = kwds.length - 1; i >= 0; i--) {
            str2kwd.put(kwds[i].getImage(), kwds[i]);
        }
    }*/

    /** Checks whether the given token-id is a type-keyword.
    * @return true when the keyword is a data type.
    */
    public static boolean isType(TokenID keywordTokenID) {
        int numID = (keywordTokenID != null) ? keywordTokenID.getNumericID() : -1;
        return (numID >= BOOLEAN_ID && numID <= VOID_ID);
    }

    /** Checks whether the given string is a type-keyword. */
    public static boolean isType(String s) {
        return isType((TokenID)str2kwd.get(s));
    }

    /** Checks whether the given token-id is a data-type-keyword or void-keyword.
    * @return true when the keyword is a data-type-keyword or void-keyword.
    */
    public static boolean isTypeOrVoid(TokenID keywordTokenID) {
        int numID = (keywordTokenID != null) ? keywordTokenID.getNumericID() : -1;
        return (numID >= BOOLEAN_ID && numID <= VOID_ID);
    }

    /** Checks whether the given string is a data-type-keyword or void-keyword. */
    public static boolean isTypeOrVoid(String s) {
        return isTypeOrVoid((TokenID)str2kwd.get(s));
    }

    /** Get the keyword token-id from string */
    public static TokenID getKeyword(String s) {
        return (TokenID)str2kwd.get(s);
    }

    private CCTokenContext() {
        super("cc-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { //NOI18N
                e.printStackTrace();
            }
        }

    }

}


