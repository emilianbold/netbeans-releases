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

import java.util.List;
import java.util.ArrayList;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

/**A Stream of Token objects fed to the parser from a Tokenizer that can
 * be rewound via mark()/rewind() methods.
 * <p>
 * A dynamic array is used to buffer up all the input tokens.  Normally,
 * "k" tokens are stored in the buffer.  More tokens may be stored during
 * guess mode (testing syntactic predicate), or when LT(i>k) is referenced.
 * Consumption of tokens is deferred.  In other words, reading the next
 * token is not done by consume(), but deferred until needed by LA or LT.
 * <p>
 *
 * @see org.netbeans.modules.cnd.antlr.Token
 * @see org.netbeans.modules.cnd.antlr.TokenQueue
 */

public class TokenBuffer {

    // Number of active markers
    private int nMarkers = 0;

	/** The index into the tokens list of the current token (next token
     *  to consume).
     */
    private int p = 0;

    public static final int INITIAL_BUFFER_SIZE = 2048;
    
    /** Record every single token pulled from the source so we can reproduce
     *  chunks of it later.
     */
    private final List<Token> tokens;

    // type buffer data (created to improve performance of LA)
    private final int size;
    private short[] data;

    /** Create a token buffer */
    public TokenBuffer(TokenStream input) {
        this(input, INITIAL_BUFFER_SIZE);
    }

    /** Create a token buffer */
    public TokenBuffer(TokenStream input, int initialCapacity) {
        tokens = new ArrayList<Token>(initialCapacity);
        data = new short[initialCapacity];
        // fill buffer
        int pos = 0;
        try {
            Token t = input.nextToken();
            int type;
            while ( (t != null) && ((type = t.getType()) != Token.EOF_TYPE) ) {
                tokens.add(t);
                if (pos == data.length) {
                    resizeData();
                }
                assert type < Short.MAX_VALUE;
                data[pos++] = (short) type;
                t = input.nextToken();
            }
        } catch (TokenStreamException tse) {
                System.err.println("tmp error: can't load tokens: "+tse);
        } catch (Throwable ex) {
                System.err.println(ex.getClass().getName() + ":" + ex.getMessage() + ":" + ex + ":" + ex.getCause()  + ":" + (ex.getStackTrace() == null ? "null stack" : "" + ex.getStackTrace().length) + " in onID "); // NOI18N
                ex.printStackTrace(System.err);
        }
        size = pos;
    }

    // double data size
    private void resizeData() {
        short[] newdata = new short[data.length*2]; // resize
        System.arraycopy(data, 0, newdata, 0, data.length);
        data = newdata;
    }

    /** Mark another token for deferred consumption */
    public final void consume() {
        p++;
    }

    /** Get a lookahead token value */
    public final int LA(int i) {
        int dataPos = p + i - 1;
        if ( dataPos >= size ) {
                return TokenImpl.EOF_TYPE;
        }
        return data[dataPos];
    }

    /** Get a lookahead token */
    public final Token LT(int i) {
        int dataPos = p + i - 1;
        if ( dataPos < 0 || dataPos >= size ) {
                return TokenImpl.EOF_TOKEN;
        }
        return tokens.get(dataPos);
    }

    public final int size() {
        return size;
    }
    
    /**Return an integer marker that can be used to rewind the buffer to
     * its current state.
     */
    public final int mark() {
        //System.out.println("Marking at " + p);
        //try { for (int i = 1; i <= 2; i++) { System.out.println("LA("+i+")=="+LT(i).getText()); } } catch (ScannerException e) {}
        nMarkers++;
        return p;
    }

    /** What token index are we at?  Assume mark() done at start.
     */
    public final int index() {
        return p;
    }

    public final void seek(int position) {
        p = position;
    }

    /**Rewind the token buffer to a marker.
     * @param marker Marker returned previously from mark()
     */
    public final void rewind(int marker) {
        seek(marker);
        nMarkers--;
        //System.out.println("Rewinding to " + marker);
        //try { for (int i = 1; i <= 2; i++) { System.out.println("LA("+i+")=="+LT(i).getText()); } } catch (ScannerException e) {}
    }

}
