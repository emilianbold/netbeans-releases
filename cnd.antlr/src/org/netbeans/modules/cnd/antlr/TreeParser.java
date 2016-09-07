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

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

public class TreeParser extends MatchExceptionState {
    /** The AST Null object; the parsing cursor is set to this when
     *  it is found to be null.  This way, we can test the
     *  token type of a node without having to have tests for null
     *  everywhere.
     */
    public static ASTNULLType ASTNULL = new ASTNULLType();

    /** Where did this rule leave off parsing; avoids a return parameter */
    protected AST _retTree;

    /** guessing nesting level; guessing==0 implies not guessing */
    // protected int guessing = 0;

    /** Nesting level of registered handlers */
    // protected int exceptionLevel = 0;

    protected TreeParserSharedInputState inputState;

    /** Table of token type to token names */
    protected String[] tokenNames;

    /** AST return value for a rule is squirreled away here */
    protected AST returnAST;

    /** AST support code; parser and treeparser delegate to this object */
    protected ASTFactory astFactory = new ASTFactory();

    /** Used to keep track of indentdepth for traceIn/Out */
    protected int traceDepth = 0;

    public TreeParser() {
        inputState = new TreeParserSharedInputState();
    }

    /** Get the AST return value squirreled away in the parser */
    public AST getAST() {
        return returnAST;
    }

    public ASTFactory getASTFactory() {
        return astFactory;
    }

    public String getTokenName(int num) {
        return tokenNames[num];
    }

    public String[] getTokenNames() {
        return tokenNames;
    }
    
    protected void match(AST t, int ttype) throws MismatchedTokenException {
        //System.out.println("match("+ttype+"); cursor is "+t);
        if (t == null || t == ASTNULL || t.getType() != ttype) {
            throw new MismatchedTokenException(getTokenNames(), t, ttype, false);
        }
    }

    /**Make sure current lookahead symbol matches the given set
     * Throw an exception upon mismatch, which is catch by either the
     * error handler or by the syntactic predicate.
     */
    public void match(AST t, BitSet b) throws MismatchedTokenException {
        if (t == null || t == ASTNULL || !b.member(t.getType())) {
            throw new MismatchedTokenException(getTokenNames(), t, b, false);
        }
    }

    protected void matchNot(AST t, int ttype) throws MismatchedTokenException {
        //System.out.println("match("+ttype+"); cursor is "+t);
        if (t == null || t == ASTNULL || t.getType() == ttype) {
            throw new MismatchedTokenException(getTokenNames(), t, ttype, true);
        }
    }

    /** @deprecated as of 2.7.2. This method calls System.exit() and writes
     *  directly to stderr, which is usually not appropriate when
     *  a parser is embedded into a larger application. Since the method is 
     *  <code>static</code>, it cannot be overridden to avoid these problems.
     *  ANTLR no longer uses this method internally or in generated code.
     */
    public static void panic() {
        System.err.println("TreeWalker: panic");
        Utils.error("");
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(RecognitionException ex) {
        System.err.println(ex.toString());
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(String s) {
        System.err.println("error: " + s);
    }

    /** Parser warning-reporting function can be overridden in subclass */
    public void reportWarning(String s) {
        System.err.println("warning: " + s);
    }

    /** Specify an object with support code (shared by
     *  Parser and TreeParser.  Normally, the programmer
     *  does not play with this, using setASTNodeType instead.
     */
    public void setASTFactory(ASTFactory f) {
        astFactory = f;
    }

    /** Specify the type of node to create during tree building.
     * 	@deprecated since 2.7.2
     */
    public void setASTNodeType(String nodeType) {
        setASTNodeClass(nodeType);
    }

    /** Specify the type of node to create during tree building */
    public void setASTNodeClass(String nodeType) {
        astFactory.setASTNodeClass(nodeType);
    }

    public void traceIndent() {
        for (int i = 0; i < traceDepth; i++)
            System.out.print(" ");
    }

    public void traceIn(String rname, AST t) {
        traceDepth += 1;
        traceIndent();
        System.out.println("> " + rname +
                           "(" + (t != null?t.toString():"null") + ")" +
                           ((inputState.guessing > 0)?" [guessing]":""));
    }

    public void traceOut(String rname, AST t) {
        traceIndent();
        System.out.println("< " + rname +
                           "(" + (t != null?t.toString():"null") + ")" +
                           ((inputState.guessing > 0)?" [guessing]":""));
        traceDepth--;
    }
}
