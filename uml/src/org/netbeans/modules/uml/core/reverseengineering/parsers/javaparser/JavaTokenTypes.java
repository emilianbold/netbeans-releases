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

package org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser;

import java.util.HashMap;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;
// $ANTLR 2.7.2: "java.g" -> "JavaLexer.java"$

public interface JavaTokenTypes {
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
}
