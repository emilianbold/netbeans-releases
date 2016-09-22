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

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;

import org.netbeans.modules.cnd.antlr.collections.impl.Vector;

/**A Grammar holds a set of rules (which are stored
 * in a symbol table).  Most of the time a grammar
 * needs a code generator and an LLkAnalyzer too.
 */
public abstract class Grammar {
    protected Tool antlrTool;
    protected CodeGenerator generator;
    protected LLkGrammarAnalyzer theLLkAnalyzer;
    protected Hashtable symbols;
    protected boolean buildAST = false;
    protected boolean analyzerDebug = false;
    protected boolean interactive = false;
    protected boolean genASTClassMap = true;
    protected String superClass = null;

    /** The token manager associated with the grammar, if any.
     // The token manager is responsible for maintaining the set of valid tokens, and
     // is conceptually shared between the lexer and parser.  This may be either a
     // LexerGrammar or a ImportVocabTokenManager.
     */
    protected TokenManager tokenManager;

    /** The name of the export vocabulary...used to generate the output
     *  token types interchange file.
     */
    protected String exportVocab = null;

    /** The name of the import vocabulary.  "Initial conditions"
     */
    protected String importVocab = null;

    // Mapping from String keys to Token option values
    protected Hashtable options;
    // Vector of RuleSymbol entries
    protected Vector rules;

    protected Token preambleAction = new CommonToken(Token.INVALID_TYPE, "");
    protected String className = null;
    protected String fileName = null;
    protected Token classMemberAction = new CommonToken(Token.INVALID_TYPE, "");
    protected boolean hasSyntacticPredicate = false;
    protected boolean hasUserErrorHandling = false;

    // max lookahead that can be attempted for this parser.
    protected int maxk = 1;

    // options
    protected boolean traceRules = false;
    protected boolean debuggingOutput = false;
    protected boolean traceSyntacticPredicates = false;
    protected boolean defaultErrorHandler = true;

    protected String comment = null; // javadoc comment

    public Grammar(String className_, Tool tool_, String superClass) {
        className = className_;
        antlrTool = tool_;
        symbols = new Hashtable();
        options = new Hashtable();
        rules = new Vector(100);
        this.superClass = superClass;
    }

    /** Define a rule */
    public void define(RuleSymbol rs) {
        rules.appendElement(rs);
        // add the symbol to the rules hash table
        symbols.put(rs.getId(), rs);
    }

    /** Top-level call to generate the code for this grammar */
    public abstract void generate() throws IOException;

    protected String getClassName() {
        return className;
    }

    /* Does this grammar have a default error handler? */
    public boolean getDefaultErrorHandler() {
        return defaultErrorHandler;
    }

    public String getFilename() {
        return fileName;
    }

    /** Get an integer option.  Given the name of the option find its
     * associated integer value.  If the associated value is not an integer or
     * is not in the table, then throw an exception of type NumberFormatException.
     * @param key The name of the option
     * @return The value associated with the key.
     */
    public int getIntegerOption(String key) throws NumberFormatException {
        Token t = (Token)options.get(key);
        if (t == null || t.getType() != ANTLRTokenTypes.INT) {
            throw new NumberFormatException();
        }
        else {
            return Integer.parseInt(t.getText());
        }
    }

    /** Get an option.  Given the name of the option find its associated value.
     * @param key The name of the option
     * @return The value associated with the key, or null if the key has not been set.
     */
    public Token getOption(String key) {
        return (Token)options.get(key);
    }

    // Get name of class from which generated parser/lexer inherits
    protected abstract String getSuperClass();

    public GrammarSymbol getSymbol(String s) {
        return (GrammarSymbol)symbols.get(s);
    }

    public Enumeration getSymbols() {
        return symbols.elements();
    }

    /** Check the existence of an option in the table
     * @param key The name of the option
     * @return true if the option is in the table
     */
    public boolean hasOption(String key) {
        return options.containsKey(key);
    }

    /** Is a rule symbol defined? (not used for tokens) */
    public boolean isDefined(String s) {
        return symbols.containsKey(s);
    }

    /**Process command line arguments.  Implemented in subclasses */
    public abstract void processArguments(String[] args);

    public void setCodeGenerator(CodeGenerator gen) {
        generator = gen;
    }

    public void setFilename(String s) {
        fileName = s;
    }

    public void setGrammarAnalyzer(LLkGrammarAnalyzer a) {
        theLLkAnalyzer = a;
    }

    /** Set a generic option.
     * This associates a generic option key with a Token value.
     * No validation is performed by this method, although users of the value
     * (code generation and/or analysis) may require certain formats.
     * The value is stored as a token so that the location of an error
     * can be reported.
     * @param key The name of the option.
     * @param value The value to associate with the key.
     * @return true if the option was a valid generic grammar option, false o/w
     */
    public boolean setOption(String key, Token value) {
        options.put(key, value);
        String s = value.getText();
        int i;
        if (key.equals("k")) {
            try {
                maxk = getIntegerOption("k");
				if ( maxk<=0 ) {
					antlrTool.error("option 'k' must be greater than 0 (was " +
									value.getText() + ")",
									getFilename(),
									value.getLine(),
									value.getColumn());
					maxk = 1;
				}
            }
            catch (NumberFormatException e) {
                antlrTool.error("option 'k' must be an integer (was " + value.getText() + ")", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("codeGenMakeSwitchThreshold")) {
            try {
                i = getIntegerOption("codeGenMakeSwitchThreshold");
            }
            catch (NumberFormatException e) {
                antlrTool.error("option 'codeGenMakeSwitchThreshold' must be an integer", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("codeGenBitsetTestThreshold")) {
            try {
                i = getIntegerOption("codeGenBitsetTestThreshold");
            }
            catch (NumberFormatException e) {
                antlrTool.error("option 'codeGenBitsetTestThreshold' must be an integer", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("defaultErrorHandler")) {
            if (s.equals("true")) {
                defaultErrorHandler = true;
            }
            else if (s.equals("false")) {
                defaultErrorHandler = false;
            }
            else {
                antlrTool.error("Value for defaultErrorHandler must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("analyzerDebug")) {
            if (s.equals("true")) {
                analyzerDebug = true;
            }
            else if (s.equals("false")) {
                analyzerDebug = false;
            }
            else {
                antlrTool.error("option 'analyzerDebug' must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("codeGenDebug")) {
            if (s.equals("true")) {
                analyzerDebug = true;
            }
            else if (s.equals("false")) {
                analyzerDebug = false;
            }
            else {
                antlrTool.error("option 'codeGenDebug' must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("genASTClassMap")) {
            if (s.equals("true")) {
                genASTClassMap = true;
            }
            else if (s.equals("false")) {
                genASTClassMap = false;
            }
            else {
                antlrTool.error("option 'genASTClassMap' must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("classHeaderSuffix")) {
            return true;
        }
        if (key.equals("classHeaderPrefix")) {
            return true;
        }
        if (key.equals("namespaceAntlr")) {
            return true;
        }
        if (key.equals("namespaceStd")) {
            return true;
        }
        if (key.equals("genHashLines")) {
            return true;
        }
        if (key.equals("noConstructors")) {
            return true;
        }
        return false;
    }

    public void setTokenManager(TokenManager tokenManager_) {
        tokenManager = tokenManager_;
    }

    /** Print out the grammar without actions */
    public String toString() {
        StringBuffer buf = new StringBuffer(20000);
        Enumeration ids = rules.elements();
        while (ids.hasMoreElements()) {
            RuleSymbol rs = (RuleSymbol)ids.nextElement();
            if (!rs.id.equals("mnextToken")) {
                buf.append(rs.getBlock().toString());
                buf.append("\n\n");
            }
        }
        return buf.toString();
    }

}
