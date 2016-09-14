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

import java.io.IOException;

import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

/** Lexer-specific grammar subclass */
class LexerGrammar extends Grammar {
    // character set used by lexer
    protected BitSet charVocabulary;
    // true if the lexer generates literal testing code for nextToken
    protected boolean testLiterals = true;
    // true if the lexer generates case-sensitive LA(k) testing
    protected boolean caseSensitiveLiterals = true;
    /** true if the lexer generates case-sensitive literals testing */
    protected boolean caseSensitive = true;
    /** true if lexer is to ignore all unrecognized tokens */
    protected boolean filterMode = false;

    /** if filterMode is true, then filterRule can indicate an optional
     *  rule to use as the scarf language.  If null, programmer used
     *  plain "filter=true" not "filter=rule".
     */
    protected String filterRule = null;

    LexerGrammar(String className_, Tool tool_, String superClass) {
        super(className_, tool_, superClass);
		// by default, use 0..127 for ASCII char vocabulary
		BitSet cv = new BitSet();
		for (int i = 0; i <= 127; i++) {
			cv.add(i);
		}
		setCharVocabulary(cv);

        // Lexer usually has no default error handling
        defaultErrorHandler = false;
    }

    /** Top-level call to generate the code	 */
    public void generate() throws IOException {
        generator.gen(this);
    }

    public String getSuperClass() {
        // If debugging, use debugger version of scanner
        if (debuggingOutput)
            return "debug.DebuggingCharScanner";
        if (!MatchExceptionState.throwRecExceptions)
            return "CharScannerNoEx";
        return "CharScanner";
    }

    // Get the testLiterals option value
    public boolean getTestLiterals() {
        return testLiterals;
    }

    /**Process command line arguments.
     * -trace			have all rules call traceIn/traceOut
     * -traceLexer		have lexical rules call traceIn/traceOut
     * -debug			generate debugging output for parser debugger
     */
    public void processArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-trace")) {
                traceRules = true;
                antlrTool.setArgOK(i);
            }
            else if (args[i].equals("-traceLexer")) {
                traceRules = true;
                antlrTool.setArgOK(i);
            }
            else if (args[i].equals("-debug")) {
                debuggingOutput = true;
                antlrTool.setArgOK(i);
            }
        }
    }

    /** Set the character vocabulary used by the lexer */
    public void setCharVocabulary(BitSet b) {
        charVocabulary = b;
    }

    /** Set lexer options */
    public boolean setOption(String key, Token value) {
        String s = value.getText();
        if (key.equals("buildAST")) {
            antlrTool.warning("buildAST option is not valid for lexer", getFilename(), value.getLine(), value.getColumn());
            return true;
        }
        if (key.equals("testLiterals")) {
            if (s.equals("true")) {
                testLiterals = true;
            }
            else if (s.equals("false")) {
                testLiterals = false;
            }
            else {
                antlrTool.warning("testLiterals option must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("interactive")) {
            if (s.equals("true")) {
                interactive = true;
            }
            else if (s.equals("false")) {
                interactive = false;
            }
            else {
                antlrTool.error("interactive option must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("caseSensitive")) {
            if (s.equals("true")) {
                caseSensitive = true;
            }
            else if (s.equals("false")) {
                caseSensitive = false;
            }
            else {
                antlrTool.warning("caseSensitive option must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("caseSensitiveLiterals")) {
            if (s.equals("true")) {
                caseSensitiveLiterals = true;
            }
            else if (s.equals("false")) {
                caseSensitiveLiterals = false;
            }
            else {
                antlrTool.warning("caseSensitiveLiterals option must be true or false", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("filter")) {
            if (s.equals("true")) {
                filterMode = true;
            }
            else if (s.equals("false")) {
                filterMode = false;
            }
            else if (value.getType() == ANTLRTokenTypes.TOKEN_REF) {
                filterMode = true;
                filterRule = s;
            }
            else {
                antlrTool.warning("filter option must be true, false, or a lexer rule name", getFilename(), value.getLine(), value.getColumn());
            }
            return true;
        }
        if (key.equals("longestPossible")) {
            antlrTool.warning("longestPossible option has been deprecated; ignoring it...", getFilename(), value.getLine(), value.getColumn());
            return true;
        }
        if (key.equals("className")) {
            super.setOption(key, value);
            return true;
        }
        if (super.setOption(key, value)) {
            return true;
        }
        antlrTool.error("Invalid option: " + key, getFilename(), value.getLine(), value.getColumn());
        return false;
    }
}
