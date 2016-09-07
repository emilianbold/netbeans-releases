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
// $ANTLR : "preproc.g" -> "Preprocessor.java"$

package org.netbeans.modules.cnd.antlr.preprocessor;

public interface PreprocessorTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_tokens = 4;
	int HEADER_ACTION = 5;
	int SUBRULE_BLOCK = 6;
	int ACTION = 7;
	int LITERAL_class = 8;
	int ID = 9;
	int LITERAL_extends = 10;
	int SEMI = 11;
	int TOKENS_SPEC = 12;
	int OPTIONS_START = 13;
	int ASSIGN_RHS = 14;
	int RCURLY = 15;
	int LITERAL_protected = 16;
	int LITERAL_private = 17;
	int LITERAL_public = 18;
	int BANG = 19;
	int ARG_ACTION = 20;
	int LITERAL_returns = 21;
	int RULE_BLOCK = 22;
	int LITERAL_throws = 23;
	int COMMA = 24;
	int LITERAL_exception = 25;
	int LITERAL_catch = 26;
	int ALT = 27;
	int ELEMENT = 28;
	int LPAREN = 29;
	int RPAREN = 30;
	int ID_OR_KEYWORD = 31;
	int CURLY_BLOCK_SCARF = 32;
	int WS = 33;
	int NEWLINE = 34;
	int COMMENT = 35;
	int SL_COMMENT = 36;
	int ML_COMMENT = 37;
	int CHAR_LITERAL = 38;
	int STRING_LITERAL = 39;
	int ESC = 40;
	int DIGIT = 41;
	int XDIGIT = 42;
}
