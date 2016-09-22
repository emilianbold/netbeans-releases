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

import java.io.*;

/** Static implementation of the TokenManager, used for importVocab option  */
class ImportVocabTokenManager extends SimpleTokenManager implements Cloneable {
    private String filename;
    protected Grammar grammar;

    // FIXME: it would be nice if the path to the original grammar file was
    // also searched.
    ImportVocabTokenManager(Grammar grammar, String filename_, String name_, Tool tool_) {
        // initialize
        super(name_, tool_);

        this.grammar = grammar;
        this.filename = filename_;

        // Figure out exactly where the file lives.  Check $PWD first,
        // and then search in -o <output_dir>.
        //
        File grammarFile = new File(filename);

        if (!grammarFile.exists()) {
            grammarFile = new File(antlrTool.getOutputDirectory(), filename);

            if (!grammarFile.exists()) {
                antlrTool.fatalError("Cannot find importVocab file '" + filename + "'");
            }
        }

        setReadOnly(true);

        // Read a file with lines of the form ID=number
        try {
            Reader fileIn = new BufferedReader(new FileReader(grammarFile));
            ANTLRTokdefLexer tokdefLexer = new ANTLRTokdefLexer(fileIn);
            ANTLRTokdefParser tokdefParser = new ANTLRTokdefParser(tokdefLexer);
            tokdefParser.setTool(antlrTool);
            tokdefParser.setFilename(filename);
            tokdefParser.file(this);
        }
        catch (FileNotFoundException fnf) {
            antlrTool.fatalError("Cannot find importVocab file '" + filename + "'");
        }
        catch (RecognitionException ex) {
            antlrTool.fatalError("Error parsing importVocab file '" + filename + "': " + ex.toString());
        }
        catch (TokenStreamException ex) {
            antlrTool.fatalError("Error reading importVocab file '" + filename + "'");
        }
    }

    public Object clone() {
        ImportVocabTokenManager tm;
        tm = (ImportVocabTokenManager)super.clone();
        tm.filename = this.filename;
        tm.grammar = this.grammar;
        return tm;
    }

    /** define a token. */
    public void define(TokenSymbol ts) {
        super.define(ts);
    }

    /** define a token.  Intended for use only when reading the importVocab file. */
    public void define(String s, int ttype) {
        TokenSymbol ts = null;
        if (s.startsWith("\"")) {
            ts = new StringLiteralSymbol(s);
        }
        else {
            ts = new TokenSymbol(s);
        }
        ts.setTokenType(ttype);
        super.define(ts);
        maxToken = (ttype + 1) > maxToken ? (ttype + 1) : maxToken;	// record maximum token type
    }

    /** importVocab token manager is read-only if output would be same as input */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** Get the next unused token type. */
    public int nextTokenType() {
        return super.nextTokenType();
    }
}
