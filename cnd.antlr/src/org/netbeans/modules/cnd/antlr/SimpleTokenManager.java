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

import org.netbeans.modules.cnd.antlr.collections.impl.Vector;

class SimpleTokenManager implements TokenManager, Cloneable {
    protected int maxToken = Token.MIN_USER_TYPE;
    // Token vocabulary is Vector of String's
    protected Vector vocabulary;
    // Hash table is a mapping from Strings to TokenSymbol
    private Hashtable table;
    // the ANTLR tool
    protected Tool antlrTool;
    // Name of the token manager
    protected String name;

    protected boolean readOnly = false;

    SimpleTokenManager(String name_, Tool tool_) {
        antlrTool = tool_;
        name = name_;
        // Don't make a bigger vector than we need, because it will show up in output sets.
        vocabulary = new Vector(1);
        table = new Hashtable();

        // define EOF symbol
        TokenSymbol ts = new TokenSymbol("EOF");
        ts.setTokenType(Token.EOF_TYPE);
        define(ts);

        // define <null-tree-lookahead> but only in the vocabulary vector
        vocabulary.ensureCapacity(Token.NULL_TREE_LOOKAHEAD);
        vocabulary.setElementAt("NULL_TREE_LOOKAHEAD", Token.NULL_TREE_LOOKAHEAD);
    }

    @Override
    public Object clone() {
        SimpleTokenManager tm;
        try {
            tm = (SimpleTokenManager)super.clone();
            tm.vocabulary = (Vector)this.vocabulary.clone();
            tm.table = (Hashtable)this.table.clone();
            tm.maxToken = this.maxToken;
            tm.antlrTool = this.antlrTool;
            tm.name = this.name;
        }
        catch (CloneNotSupportedException e) {
            antlrTool.fatalError("Cannot clone token manager");
            return null;
        }
        return tm;
    }

    /** define a token */
    public void define(TokenSymbol ts) {
        // Add the symbol to the vocabulary vector
        vocabulary.ensureCapacity(ts.getTokenType());
        vocabulary.setElementAt(ts.getId(), ts.getTokenType());
        // add the symbol to the hash table
        mapToTokenSymbol(ts.getId(), ts);
    }

    /** Simple token manager doesn't have a name -- must be set externally */
    public String getName() {
        return name;
    }

    /** Get a token symbol by index */
    public String getTokenStringAt(int idx) {
        return (String)vocabulary.elementAt(idx);
    }

    /** Get the TokenSymbol for a string */
    public TokenSymbol getTokenSymbol(String sym) {
        return (TokenSymbol)table.get(sym);
    }

    /** Get a token symbol by index */
    public TokenSymbol getTokenSymbolAt(int idx) {
        return getTokenSymbol(getTokenStringAt(idx));
    }

    /** Get an enumerator over the symbol table */
    public Enumeration getTokenSymbolElements() {
        return table.elements();
    }

    public Enumeration getTokenSymbolKeys() {
        return table.keys();
    }

    /** Get the token vocabulary (read-only).
     * @return A Vector of TokenSymbol
     */
    public Vector getVocabulary() {
        return vocabulary;
    }

    /** Simple token manager is not read-only */
    public boolean isReadOnly() {
        return false;
    }

    /** Map a label or string to an existing token symbol */
    public void mapToTokenSymbol(String name, TokenSymbol sym) {
        // System.out.println("mapToTokenSymbol("+name+","+sym+")");
        table.put(name, sym);
    }

    /** Get the highest token type in use */
    public int maxTokenType() {
        return maxToken - 1;
    }

    /** Get the next unused token type */
    public int nextTokenType() {
        return maxToken++;
    }

    /** Set the name of the token manager */
    public void setName(String name_) {
        name = name_;
    }

    public void setReadOnly(boolean ro) {
        readOnly = ro;
    }

    /** Is a token symbol defined? */
    public boolean tokenDefined(String symbol) {
        return table.containsKey(symbol);
    }
}
