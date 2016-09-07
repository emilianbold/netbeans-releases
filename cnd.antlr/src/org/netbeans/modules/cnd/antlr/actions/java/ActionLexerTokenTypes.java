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
// $ANTLR : "action.g" -> "ActionLexer.java"$

package org.netbeans.modules.cnd.antlr.actions.java;

public interface ActionLexerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int ACTION = 4;
	int STUFF = 5;
	int AST_ITEM = 6;
	int TEXT_ITEM = 7;
	int TREE = 8;
	int TREE_ELEMENT = 9;
	int AST_CONSTRUCTOR = 10;
	int AST_CTOR_ELEMENT = 11;
	int ID_ELEMENT = 12;
	int TEXT_ARG = 13;
	int TEXT_ARG_ELEMENT = 14;
	int TEXT_ARG_ID_ELEMENT = 15;
	int ARG = 16;
	int ID = 17;
	int VAR_ASSIGN = 18;
	int COMMENT = 19;
	int SL_COMMENT = 20;
	int ML_COMMENT = 21;
	int CHAR = 22;
	int STRING = 23;
	int ESC = 24;
	int DIGIT = 25;
	int INT = 26;
	int INT_OR_FLOAT = 27;
	int WS = 28;
}
