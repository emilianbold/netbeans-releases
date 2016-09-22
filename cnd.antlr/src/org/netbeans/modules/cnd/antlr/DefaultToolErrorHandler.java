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

import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

class DefaultToolErrorHandler implements ToolErrorHandler {
	DefaultToolErrorHandler(org.netbeans.modules.cnd.antlr.Tool tool) {
		antlrTool = tool;
	}
	private final org.netbeans.modules.cnd.antlr.Tool antlrTool;

	CharFormatter javaCharFormatter = new JavaCharFormatter();

	/** Dump token/character sets to a string array suitable for
	 * {@link org.netbeans.modules.cnd.antlr.Tool.warning(String[], String, int, int)
	 * @param output The array that will contain the token/character set dump,
	 *               one element per k (lookahead) value
	 * @param outputStartIndex The index into <code>output</code> that the
	 *                         dump should start at.
	 * @param lexicalAnalysis  true for lexical rule
	 * @param depth  The depth of the ambiguity
	 * @param sets  An array of bitsets containing the ambiguities
	 */
	private void dumpSets(String[] output,
						  int outputStartIndex,
						  Grammar grammar,
						  boolean lexicalAnalysis,
						  int depth,
						  Lookahead[] sets) {
		StringBuffer line = new StringBuffer(100);
		for (int i = 1; i <= depth; i++) {
			line.append("k==").append(i).append(':');
			if (lexicalAnalysis) {
				String bits = sets[i].fset.toStringWithRanges(",", javaCharFormatter);
				if (sets[i].containsEpsilon()) {
					line.append("<end-of-token>");
					if (bits.length() > 0) {
						line.append(',');
					}
				}
				line.append(bits);
			} else {
				line.append(sets[i].fset.toString(",", grammar.tokenManager.getVocabulary()));
			}
			output[outputStartIndex++] = line.toString();
			line.setLength(0);
		}
	}

	/** Issue a warning about ambiguity between a alternates
	 * @param blk  The block being analyzed
	 * @param lexicalAnalysis  true for lexical rule
	 * @param depth  The depth of the ambiguity
	 * @param sets  An array of bitsets containing the ambiguities
	 * @param altIdx1  The zero-based index of the first ambiguous alternative
	 * @param altIdx2  The zero-based index of the second ambiguous alternative
	 */
	public void warnAltAmbiguity(Grammar grammar,
								 AlternativeBlock blk,
								 boolean lexicalAnalysis,
								 int depth,
								 Lookahead[] sets,
								 int altIdx1,
								 int altIdx2)
	{
		final StringBuffer line = new StringBuffer(100);
		if (blk instanceof RuleBlock && ((RuleBlock)blk).isLexerAutoGenRule()) {
			Alternative ai = blk.getAlternativeAt(altIdx1);
			Alternative aj = blk.getAlternativeAt(altIdx2);
			RuleRefElement rri = (RuleRefElement)ai.head;
			RuleRefElement rrj = (RuleRefElement)aj.head;
			String ri = CodeGenerator.reverseLexerRuleName(rri.targetRule);
			String rj = CodeGenerator.reverseLexerRuleName(rrj.targetRule);
			line.append("lexical nondeterminism between rules ");
			line.append(ri).append(" and ").append(rj).append(" upon");
		}
		else {
			if (lexicalAnalysis) {
				line.append("lexical ");
			}
			line.append("nondeterminism between alts ");
			line.append(altIdx1 + 1).append(" and ");
			line.append(altIdx2 + 1).append(" of block upon");
		}
		final String [] output = new String [depth + 1];;
		output[0] = line.toString();
		dumpSets(output, 1, grammar, lexicalAnalysis, depth, sets);
		antlrTool.warning(output, grammar.getFilename(), blk.getLine(), blk.getColumn());

	}

	/** Issue a warning about ambiguity between an alternate and exit path.
	 * @param blk  The block being analyzed
	 * @param lexicalAnalysis  true for lexical rule
	 * @param depth  The depth of the ambiguity
	 * @param sets  An array of bitsets containing the ambiguities
	 * @param altIdx  The zero-based index of the ambiguous alternative
	 */
	public void warnAltExitAmbiguity(Grammar grammar,
									 BlockWithImpliedExitPath blk,
									 boolean lexicalAnalysis,
									 int depth,
									 Lookahead[] sets,
									 int altIdx
									 )
	{
		String [] output = new String[depth + 2];
		output[0] = (lexicalAnalysis ? "lexical " : "") + "nondeterminism upon";
		dumpSets(output, 1, grammar, lexicalAnalysis, depth, sets);
		output[depth + 1] = "between alt " + (altIdx + 1) + " and exit branch of block";
		antlrTool.warning(output, grammar.getFilename(), blk.getLine(), blk.getColumn());
	}
}
