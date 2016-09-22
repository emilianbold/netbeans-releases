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

public interface ANTLRGrammarParseBehavior {
    public void abortGrammar();

    public void beginAlt(boolean doAST_);

    public void beginChildList();

    // Exception handling
    public void beginExceptionGroup();

    public void beginExceptionSpec(Token label);

    public void beginSubRule(Token label, Token start, boolean not);

    // Trees
    public void beginTree(Token tok) throws SemanticException;

    public void defineRuleName(Token r, String access, boolean ruleAST, String docComment) throws SemanticException;

    public void defineToken(Token tokname, Token tokliteral);

    public void endAlt();

    public void endChildList();

    public void endExceptionGroup();

    public void endExceptionSpec();

    public void endGrammar();

    public void endOptions();

    public void endRule(String r);

    public void endSubRule();

    public void endTree();

    public void hasError();

    public void noASTSubRule();

    public void oneOrMoreSubRule();

    public void optionalSubRule();

    public void refAction(Token action);

    public void refArgAction(Token action);

    public void setUserExceptions(String thr);

    public void refCharLiteral(Token lit, Token label, boolean inverted, int autoGenType, boolean lastInRule);

    public void refCharRange(Token t1, Token t2, Token label, int autoGenType, boolean lastInRule);

    public void refElementOption(Token option, Token value);

    public void refTokensSpecElementOption(Token tok, Token option, Token value);

    public void refExceptionHandler(Token exTypeAndName, Token action);

    public void refHeaderAction(Token name, Token act);

    public void refInitAction(Token action);

    public void refMemberAction(Token act);

    public void refPreambleAction(Token act);

    public void refReturnAction(Token returnAction);

    public void refRule(Token idAssign, Token r, Token label, Token arg, int autoGenType);

    public void refSemPred(Token pred);

    public void refStringLiteral(Token lit, Token label, int autoGenType, boolean lastInRule);

    public void refToken(Token assignId, Token t, Token label, Token args,
                         boolean inverted, int autoGenType, boolean lastInRule);

    public void refTokenRange(Token t1, Token t2, Token label, int autoGenType, boolean lastInRule);

    // Tree specifiers
    public void refTreeSpecifier(Token treeSpec);

    public void refWildcard(Token t, Token label, int autoGenType);

    public void setArgOfRuleRef(Token argaction);

    public void setCharVocabulary(BitSet b);

    // Options
    public void setFileOption(Token key, Token value, String filename);

    public void setGrammarOption(Token key, Token value);

    public void setRuleOption(Token key, Token value);

    public void setSubruleOption(Token key, Token value);

    public void startLexer(String file, Token name, String superClass, String doc);

    // Flow control for grammars
    public void startParser(String file, Token name, String superClass, String doc);

    public void startTreeWalker(String file, Token name, String superClass, String doc);

    public void synPred();

    public void zeroOrMoreSubRule();
}
