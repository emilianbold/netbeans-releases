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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser;

import java.util.HashMap;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;
// $ANTLR 2.7.2: "EmbarcaderoJava.tree.g" -> "EmbarcaderoJavaTreeParser.java"$

public interface EmbarcaderoJavaTreeParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int BLOCK = 4;
	int MODIFIERS = 5;
	int OBJBLOCK = 6;
	int SLIST = 7;
	int END_SLIST = 8;
	int CTOR_DEF = 9;
	int METHOD_DEF = 10;
	int DESTRUCTOR_DEF = 11;
	int VARIABLE_DEF = 12;
	int INSTANCE_INIT = 13;
	int STATIC_INIT = 14;
	int TYPE = 15;
	int CLASS_DEF = 16;
	int INTERFACE_DEF = 17;
	int PACKAGE_DEF = 18;
	int ARRAY_DECLARATOR = 19;
	int EXTENDS_CLAUSE = 20;
	int IMPLEMENTS_CLAUSE = 21;
	int PARAMETERS = 22;
	int PARAMETER_DEF = 23;
	int LABELED_STAT = 24;
	int TYPECAST = 25;
	int INDEX_OP = 26;
	int POST_INC = 27;
	int POST_DEC = 28;
	int METHOD_CALL = 29;
	int EXPR = 30;
	int ARRAY_INIT = 31;
	int IMPORT = 32;
	int UNARY_MINUS = 33;
	int UNARY_PLUS = 34;
	int CASE_GROUP = 35;
	int ELIST = 36;
	int FOR_INIT = 37;
	int FOR_CONDITION = 38;
	int FOR_ITERATOR = 39;
	int EMPTY_STAT = 40;
	int FINAL = 41;
	int ABSTRACT = 42;
	int STRICTFP = 43;
	int SUPER_CTOR_CALL = 44;
	int CTOR_CALL = 45;
	int START_CLASS_BODY = 46;
	int END_CLASS_BODY = 47;
	int LITERAL_package = 48;
	int SEMI = 49;
	int LITERAL_import = 50;
	int LBRACK = 51;
	int RBRACK = 52;
	int LITERAL_void = 53;
	int LITERAL_boolean = 54;
	int LITERAL_byte = 55;
	int LITERAL_char = 56;
	int LITERAL_short = 57;
	int LITERAL_int = 58;
	int LITERAL_float = 59;
	int LITERAL_long = 60;
	int LITERAL_double = 61;
	int IDENT = 62;
	int DOT = 63;
	int STAR = 64;
	int LITERAL_private = 65;
	int LITERAL_public = 66;
	int LITERAL_protected = 67;
	int LITERAL_static = 68;
	int LITERAL_transient = 69;
	int LITERAL_native = 70;
	int LITERAL_synchronized = 71;
	int LITERAL_volatile = 72;
	int LITERAL_class = 73;
	int LITERAL_extends = 74;
	int LITERAL_interface = 75;
	int LCURLY = 76;
	int RCURLY = 77;
	int COMMA = 78;
	int LITERAL_implements = 79;
	int LPAREN = 80;
	int RPAREN = 81;
	int LITERAL_this = 82;
	int LITERAL_super = 83;
	int ASSIGN = 84;
	int LITERAL_throws = 85;
	int COLON = 86;
	int LITERAL_if = 87;
	int LITERAL_else = 88;
	int LITERAL_for = 89;
	int LITERAL_while = 90;
	int LITERAL_do = 91;
	int LITERAL_break = 92;
	int LITERAL_continue = 93;
	int LITERAL_return = 94;
	int LITERAL_switch = 95;
	int LITERAL_throw = 96;
	int LITERAL_case = 97;
	int LITERAL_default = 98;
	int LITERAL_try = 99;
	int LITERAL_catch = 100;
	int LITERAL_finally = 101;
	int PLUS_ASSIGN = 102;
	int MINUS_ASSIGN = 103;
	int STAR_ASSIGN = 104;
	int DIV_ASSIGN = 105;
	int MOD_ASSIGN = 106;
	int SR_ASSIGN = 107;
	int BSR_ASSIGN = 108;
	int SL_ASSIGN = 109;
	int BAND_ASSIGN = 110;
	int BXOR_ASSIGN = 111;
	int BOR_ASSIGN = 112;
	int QUESTION = 113;
	int LOR = 114;
	int LAND = 115;
	int BOR = 116;
	int BXOR = 117;
	int BAND = 118;
	int NOT_EQUAL = 119;
	int EQUAL = 120;
	int LT_ = 121;
	int GT = 122;
	int LE = 123;
	int GE = 124;
	int LITERAL_instanceof = 125;
	int SL = 126;
	int SR = 127;
	int BSR = 128;
	int PLUS = 129;
	int MINUS = 130;
	int DIV = 131;
	int MOD = 132;
	int INC = 133;
	int DEC = 134;
	int BNOT = 135;
	int LNOT = 136;
	int LITERAL_true = 137;
	int LITERAL_false = 138;
	int LITERAL_null = 139;
	int LITERAL_new = 140;
	int NUM_INT = 141;
	int CHAR_LITERAL = 142;
	int STRING_LITERAL = 143;
	int NUM_FLOAT = 144;
	int NUM_LONG = 145;
	int NUM_DOUBLE = 146;
	int WS = 147;
	int SL_COMMENT = 148;
	int ML_COMMENT = 149;
	int ESC = 150;
	int HEX_DIGIT = 151;
	int VOCAB = 152;
	int EXPONENT = 153;
	int FLOAT_SUFFIX = 154;
	int LITERAL_threadsafe = 155;
	int LITERAL_const = 156;
}
