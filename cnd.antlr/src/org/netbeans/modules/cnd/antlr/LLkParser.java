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
package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

/**An LL(k) parser.
 *
 * @see org.netbeans.modules.cnd.antlr.Token
 * @see org.netbeans.modules.cnd.antlr.TokenBuffer
 */
public class LLkParser extends Parser {
	final int k;

	public LLkParser(int k_) {
		k = k_;
	}

	public LLkParser(TokenBuffer tokenBuf, int k_) {
		k = k_;
		setTokenBuffer(tokenBuf);
	}

	public LLkParser(TokenStream lexer, int k_) {
            k = k_;
            TokenBuffer tokenBuf = new TokenBuffer(lexer);
            setTokenBuffer(tokenBuf);
        }

        public LLkParser(TokenStream lexer, int k_, int initialBufferCapacity) {
		k = k_;
		TokenBuffer tokenBuf = new TokenBuffer(lexer, initialBufferCapacity);
		setTokenBuffer(tokenBuf);
	}

	/**Consume another token from the input stream.  Can only write sequentially!
	 * If you need 3 tokens ahead, you must consume() 3 times.
	 * <p>
	 * Note that it is possible to overwrite tokens that have not been matched.
	 * For example, calling consume() 3 times when k=2, means that the first token
	 * consumed will be overwritten with the 3rd.
	 */
        @Override
	public void consume() {
		input.consume();
	}

        @Override
	public int LA(int i) {
		return input.LA(i);
	}

        @Override
	public Token LT(int i) {
		return input.LT(i);
	}

	private void trace(String ee, String rname) {
		traceIndent();
		System.out.print(ee + rname + ((guessing > 0)?"; [guessing="+guessing+"]":"; "));
		for (int i = 1; i <= k; i++) {
			if (i != 1) {
				System.out.print(", ");
			}
			if ( LT(i)!=null ) {
				System.out.print("LA(" + i + ")==" + LT(i).getText());
			}
			else {
				System.out.print("LA(" + i + ")==null");
			}
		}
		System.out.println("");
	}

    @Override
	public void traceIn(String rname) {
		traceDepth += 1;
		trace("> ", rname);
	}

    @Override
	public void traceOut(String rname) {
		trace("< ", rname);
		traceDepth -= 1;
	}
    
        //vk++ for analyzing time spent in guessing
        protected void syntacticPredicateStarted(int id, int nestingLevel, int line) {
        }

        protected void syntacticPredicateFailed(int id, int nestingLevel) {
        }

        protected void syntacticPredicateSucceeded(int id, int nestingLevel) {
        }
        //vk--        
}
