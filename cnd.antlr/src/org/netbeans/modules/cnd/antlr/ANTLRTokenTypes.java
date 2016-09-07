/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
// $ANTLR 2.7.3rc3: "antlr.g" -> "ANTLRLexer.java"$

package org.netbeans.modules.cnd.antlr;

public interface ANTLRTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_tokens = 4;
	int LITERAL_header = 5;
	int STRING_LITERAL = 6;
	int ACTION = 7;
	int DOC_COMMENT = 8;
	int LITERAL_lexclass = 9;
	int LITERAL_class = 10;
	int LITERAL_extends = 11;
	int LITERAL_Lexer = 12;
	int LITERAL_TreeParser = 13;
	int OPTIONS = 14;
	int ASSIGN = 15;
	int SEMI = 16;
	int RCURLY = 17;
	int LITERAL_charVocabulary = 18;
	int CHAR_LITERAL = 19;
	int INT = 20;
	int OR = 21;
	int RANGE = 22;
	int TOKENS = 23;
	int TOKEN_REF = 24;
	int OPEN_ELEMENT_OPTION = 25;
	int CLOSE_ELEMENT_OPTION = 26;
	int LPAREN = 27;
	int RPAREN = 28;
	int LITERAL_Parser = 29;
	int LITERAL_protected = 30;
	int LITERAL_public = 31;
	int LITERAL_private = 32;
	int BANG = 33;
	int ARG_ACTION = 34;
	int LITERAL_returns = 35;
	int COLON = 36;
	int LITERAL_throws = 37;
	int COMMA = 38;
	int LITERAL_exception = 39;
	int LITERAL_catch = 40;
	int RULE_REF = 41;
	int NOT_OP = 42;
	int SEMPRED = 43;
	int TREE_BEGIN = 44;
	int QUESTION = 45;
	int STAR = 46;
	int PLUS = 47;
	int IMPLIES = 48;
	int CARET = 49;
	int WILDCARD = 50;
	int LITERAL_options = 51;
	int WS = 52;
	int COMMENT = 53;
	int SL_COMMENT = 54;
	int ML_COMMENT = 55;
	int ESC = 56;
	int DIGIT = 57;
	int XDIGIT = 58;
	int NESTED_ARG_ACTION = 59;
	int NESTED_ACTION = 60;
	int WS_LOOP = 61;
	int INTERNAL_RULE_REF = 62;
	int WS_OPT = 63;
}
