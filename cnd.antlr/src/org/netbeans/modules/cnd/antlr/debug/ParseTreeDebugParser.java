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
package org.netbeans.modules.cnd.antlr.debug;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.*;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

import java.util.Stack;

/** Override the standard matching and rule entry/exit routines
 *  to build parse trees.  This class is useful for 2.7.3 where
 *  you can specify a superclass like
 *
 *   class TinyCParser extends Parser(ParseTreeDebugParser);
 */
public class ParseTreeDebugParser extends LLkParser {
	/** Each new rule invocation must have it's own subtree.  Tokens
	 *  are added to the current root so we must have a stack of subtree roots.
	 */
	protected Stack currentParseTreeRoot = new Stack();

	/** Track most recently created parse subtree so that when parsing
	 *  is finished, we can get to the root.
	 */
	protected ParseTreeRule mostRecentParseTreeRoot = null;

	/** For every rule replacement with a production, we bump up count. */
	protected int numberOfDerivationSteps = 1; // n replacements plus step 0

	public ParseTreeDebugParser(int k_) {
		super(k_);
	}

	public ParseTreeDebugParser(TokenBuffer tokenBuf, int k_) {
		super(tokenBuf, k_);
	}

	public ParseTreeDebugParser(TokenStream lexer, int k_) {
		super(lexer,k_);
	}

	public ParseTree getParseTree() {
		return mostRecentParseTreeRoot;
	}

	public int getNumberOfDerivationSteps() {
		return numberOfDerivationSteps;
	}

    @Override
	public void match(int i) throws MismatchedTokenException {
		addCurrentTokenToParseTree();
		super.match(i);
	}

    @Override
	public void match(BitSet bitSet) throws MismatchedTokenException {
		addCurrentTokenToParseTree();
		super.match(bitSet);
	}

    @Override
	public void matchNot(int i) throws MismatchedTokenException {
		addCurrentTokenToParseTree();
		super.matchNot(i);
	}

	/** This adds LT(1) to the current parse subtree.  Note that the match()
	 *  routines add the node before checking for correct match.  This means
	 *  that, upon mismatched token, there will a token node in the tree
	 *  corresponding to where that token was expected.  For no viable
	 *  alternative errors, no node will be in the tree as nothing was
	 *  matched() (the lookahead failed to predict an alternative).
	 */
	protected void addCurrentTokenToParseTree() {
		if (guessing>0) {
			return;
		}
		ParseTreeRule root = (ParseTreeRule)currentParseTreeRoot.peek();
		ParseTreeToken tokenNode = null;
		if ( LA(1)==Token.EOF_TYPE ) {
			tokenNode = new ParseTreeToken(new org.netbeans.modules.cnd.antlr.CommonToken("EOF"));
		}
		else {
			tokenNode = new ParseTreeToken(LT(1));
		}
		root.addChild(tokenNode);
	}

	/** Create a rule node, add to current tree, and make it current root */
	public void traceIn(String s) {
		if (guessing>0) {
			return;
		}
		ParseTreeRule subRoot = new ParseTreeRule(s);
		if ( currentParseTreeRoot.size()>0 ) {
			ParseTreeRule oldRoot = (ParseTreeRule)currentParseTreeRoot.peek();
			oldRoot.addChild(subRoot);
		}
		currentParseTreeRoot.push(subRoot);
		numberOfDerivationSteps++;
	}

	/** Pop current root; back to adding to old root */
	public void traceOut(String s) {
		if (guessing>0) {
			return;
		}
		mostRecentParseTreeRoot = (ParseTreeRule)currentParseTreeRoot.pop();
	}

}
