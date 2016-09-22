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
package org.netbeans.modules.cnd.antlr.preprocessor;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.impl.IndexedVector;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import org.netbeans.modules.cnd.antlr.*;
import org.netbeans.modules.cnd.antlr.preprocessor.Grammar;

public class Hierarchy {
    protected Grammar LexerRoot = null;
    protected Grammar ParserRoot = null;
    protected Grammar TreeParserRoot = null;
    protected Hashtable symbols;	// table of grammars
    protected Hashtable files;	// table of grammar files read in
    protected org.netbeans.modules.cnd.antlr.Tool antlrTool;

    public Hierarchy(org.netbeans.modules.cnd.antlr.Tool tool) {
        this.antlrTool = tool;
        LexerRoot = new Grammar(tool, "Lexer", null, null);
        ParserRoot = new Grammar(tool, "Parser", null, null);
        TreeParserRoot = new Grammar(tool, "TreeParser", null, null);
        symbols = new Hashtable(10);
        files = new Hashtable(10);

        LexerRoot.setPredefined(true);
        ParserRoot.setPredefined(true);
        TreeParserRoot.setPredefined(true);

        symbols.put(LexerRoot.getName(), LexerRoot);
        symbols.put(ParserRoot.getName(), ParserRoot);
        symbols.put(TreeParserRoot.getName(), TreeParserRoot);
    }

    public void addGrammar(Grammar gr) {
        gr.setHierarchy(this);
        // add grammar to hierarchy
        symbols.put(gr.getName(), gr);
        // add grammar to file.
        GrammarFile f = getFile(gr.getFileName());
        f.addGrammar(gr);
    }

    public void addGrammarFile(GrammarFile gf) {
        files.put(gf.getName(), gf);
    }

    public void expandGrammarsInFile(String fileName) {
        GrammarFile f = getFile(fileName);
        for (Enumeration e = f.getGrammars().elements(); e.hasMoreElements();) {
            Grammar g = (Grammar)e.nextElement();
            g.expandInPlace();
        }
    }

    public Grammar findRoot(Grammar g) {
        if (g.getSuperGrammarName() == null) {		// at root
            return g;
        }
        // return root of super.
        Grammar sg = g.getSuperGrammar();
        if (sg == null) return g;		// return this grammar if super missing
        return findRoot(sg);
    }

    public GrammarFile getFile(String fileName) {
        return (GrammarFile)files.get(fileName);
    }

    public Grammar getGrammar(String gr) {
        return (Grammar)symbols.get(gr);
    }

    public static String optionsToString(IndexedVector options) {
        String s = "options {" + System.getProperty("line.separator");
        for (Enumeration e = options.elements(); e.hasMoreElements();) {
            s += (Option)e.nextElement() + System.getProperty("line.separator");
        }
        s += "}" +
            System.getProperty("line.separator") +
            System.getProperty("line.separator");
        return s;
    }

    public void readGrammarFile(String file) throws FileNotFoundException {
        Reader grStream = new BufferedReader(new FileReader(file));
        addGrammarFile(new GrammarFile(antlrTool, file));

        // Create the simplified grammar lexer/parser
        PreprocessorLexer ppLexer = new PreprocessorLexer(grStream);
        ppLexer.setFilename(file);
        Preprocessor pp = new Preprocessor(ppLexer);
		pp.setTool(antlrTool);
        pp.setFilename(file);

        // populate the hierarchy with class(es) read in
        try {
            pp.grammarFile(this, file);
        }
        catch (TokenStreamException io) {
            antlrTool.toolError("Token stream error reading grammar(s):\n" + io);
        }
        catch (ANTLRException se) {
            antlrTool.toolError("error reading grammar(s):\n" + se);
        }
    }

    /** Return true if hierarchy is complete, false if not */
    public boolean verifyThatHierarchyIsComplete() {
        boolean complete = true;
        // Make a pass to ensure all grammars are defined
        for (Enumeration e = symbols.elements(); e.hasMoreElements();) {
            Grammar c = (Grammar)e.nextElement();
            if (c.getSuperGrammarName() == null) {
                continue;		// at root: ignore predefined roots
            }
            Grammar superG = c.getSuperGrammar();
            if (superG == null) {
                antlrTool.toolError("grammar " + c.getSuperGrammarName() + " not defined");
                complete = false;
                symbols.remove(c.getName()); // super not defined, kill sub
            }
        }

        if (!complete) return false;

        // Make another pass to set the 'type' field of each grammar
        // This makes it easy later to ask a grammar what its type
        // is w/o having to search hierarchy.
        for (Enumeration e = symbols.elements(); e.hasMoreElements();) {
            Grammar c = (Grammar)e.nextElement();
            if (c.getSuperGrammarName() == null) {
                continue;		// ignore predefined roots
            }
            c.setType(findRoot(c).getName());
        }

        return true;
    }

    public org.netbeans.modules.cnd.antlr.Tool getTool() {
        return antlrTool;
    }

    public void setTool(org.netbeans.modules.cnd.antlr.Tool antlrTool) {
        this.antlrTool = antlrTool;
    }
}
