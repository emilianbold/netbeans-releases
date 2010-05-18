// $ANTLR 2.7.2: "java15.g" -> "JavaLexer.java"$

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;

public interface JavaTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int BLOCK = 4;
	int MODIFIERS = 5;
	int OBJBLOCK = 6;
	int SLIST = 7;
	int CTOR_DEF = 8;
	int METHOD_DEF = 9;
	int VARIABLE_DEF = 10;
	int INSTANCE_INIT = 11;
	int STATIC_INIT = 12;
	int TYPE = 13;
	int CLASS_DEF = 14;
	int INTERFACE_DEF = 15;
	int PACKAGE_DEF = 16;
	int ARRAY_DECLARATOR = 17;
	int EXTENDS_CLAUSE = 18;
	int IMPLEMENTS_CLAUSE = 19;
	int PARAMETERS = 20;
	int PARAMETER_DEF = 21;
	int LABELED_STAT = 22;
	int TYPECAST = 23;
	int INDEX_OP = 24;
	int POST_INC = 25;
	int POST_DEC = 26;
	int METHOD_CALL = 27;
	int EXPR = 28;
	int ARRAY_INIT = 29;
	int IMPORT = 30;
	int UNARY_MINUS = 31;
	int UNARY_PLUS = 32;
	int CASE_GROUP = 33;
	int ELIST = 34;
	int FOR_INIT = 35;
	int FOR_CONDITION = 36;
	int FOR_ITERATOR = 37;
	int EMPTY_STAT = 38;
	int FINAL = 39;
	int ABSTRACT = 40;
	int STRICTFP = 41;
	int SUPER_CTOR_CALL = 42;
	int CTOR_CALL = 43;
	int VARIABLE_PARAMETER_DEF = 44;
	int STATIC_IMPORT = 45;
	int ENUM_DEF = 46;
	int ENUM_CONSTANT_DEF = 47;
	int FOR_EACH_CLAUSE = 48;
	int ANNOTATION_DEF = 49;
	int ANNOTATIONS = 50;
	int ANNOTATION = 51;
	int ANNOTATION_MEMBER_VALUE_PAIR = 52;
	int ANNOTATION_FIELD_DEF = 53;
	int ANNOTATION_ARRAY_INIT = 54;
	int TYPE_ARGUMENTS = 55;
	int TYPE_ARGUMENT = 56;
	int TYPE_PARAMETERS = 57;
	int TYPE_PARAMETER = 58;
	int WILDCARD_TYPE = 59;
	int TYPE_UPPER_BOUNDS = 60;
	int TYPE_LOWER_BOUNDS = 61;
	int GENERIC_TYPE = 62;
	int START_CLASS_BODY = 63;
	int END_CLASS_BODY = 64;
	int LITERAL_package = 65;
	int SEMI = 66;
	int LITERAL_import = 67;
	int LITERAL_static = 68;
	int LBRACK = 69;
	int RBRACK = 70;
	int QUESTION = 71;
	int LITERAL_extends = 72;
	int LITERAL_super = 73;
	int LT = 74;
	int COMMA = 75;
	int GT = 76;
	int SR = 77;
	int BSR = 78;
	int LITERAL_void = 79;
	int LITERAL_boolean = 80;
	int LITERAL_byte = 81;
	int LITERAL_char = 82;
	int LITERAL_short = 83;
	int LITERAL_int = 84;
	int LITERAL_float = 85;
	int LITERAL_long = 86;
	int LITERAL_double = 87;
	int IDENT = 88;
	int DOT = 89;
	int STAR = 90;
	int LITERAL_private = 91;
	int LITERAL_public = 92;
	int LITERAL_protected = 93;
	int LITERAL_transient = 94;
	int LITERAL_native = 95;
	int LITERAL_threadsafe = 96;
	int LITERAL_synchronized = 97;
	int LITERAL_volatile = 98;
	int AT = 99;
	int LPAREN = 100;
	int RPAREN = 101;
	int ASSIGN = 102;
	int LCURLY = 103;
	int RCURLY = 104;
	int LITERAL_class = 105;
	int LITERAL_interface = 106;
	int LITERAL_enum = 107;
	int BAND = 108;
	int LITERAL_default = 109;
	int LITERAL_implements = 110;
	int LITERAL_this = 111;
	int LITERAL_throws = 112;
	int TRIPLE_DOT = 113;
	int COLON = 114;
	int LITERAL_if = 115;
	int LITERAL_else = 116;
	int LITERAL_while = 117;
	int LITERAL_do = 118;
	int LITERAL_break = 119;
	int LITERAL_continue = 120;
	int LITERAL_return = 121;
	int LITERAL_switch = 122;
	int LITERAL_throw = 123;
	int LITERAL_assert = 124;
	int LITERAL_for = 125;
	int LITERAL_case = 126;
	int LITERAL_try = 127;
	int LITERAL_finally = 128;
	int LITERAL_catch = 129;
	int PLUS_ASSIGN = 130;
	int MINUS_ASSIGN = 131;
	int STAR_ASSIGN = 132;
	int DIV_ASSIGN = 133;
	int MOD_ASSIGN = 134;
	int SR_ASSIGN = 135;
	int BSR_ASSIGN = 136;
	int SL_ASSIGN = 137;
	int BAND_ASSIGN = 138;
	int BXOR_ASSIGN = 139;
	int BOR_ASSIGN = 140;
	int LOR = 141;
	int LAND = 142;
	int BOR = 143;
	int BXOR = 144;
	int NOT_EQUAL = 145;
	int EQUAL = 146;
	int LE = 147;
	int GE = 148;
	int LITERAL_instanceof = 149;
	int SL = 150;
	int PLUS = 151;
	int MINUS = 152;
	int DIV = 153;
	int MOD = 154;
	int INC = 155;
	int DEC = 156;
	int BNOT = 157;
	int LNOT = 158;
	int LITERAL_true = 159;
	int LITERAL_false = 160;
	int LITERAL_null = 161;
	int LITERAL_new = 162;
	int NUM_INT = 163;
	int CHAR_LITERAL = 164;
	int STRING_LITERAL = 165;
	int NUM_FLOAT = 166;
	int NUM_LONG = 167;
	int NUM_DOUBLE = 168;
	int WS = 169;
	int SL_COMMENT = 170;
	int ML_COMMENT = 171;
	int ESC = 172;
	int HEX_DIGIT = 173;
	int VOCAB = 174;
	int IDENT_LETTER = 175;
	int EXPONENT = 176;
	int FLOAT_SUFFIX = 177;
}
