/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.apt.impl.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class APTBaseLanguageFilter implements APTLanguageFilter {
    private final Map<CharSequence,Integer/*ttype*/> filter;
    private final Set<Integer/*ttype*/> keywords = new HashSet<Integer>();

    // uncomment to use reduced memory
//    private static final int BUFFERED_COUNT = 256;
//    private static final Integer[] int2Int;
//    static {
//        int2Int = new Integer[BUFFERED_COUNT];
//        for (int i = 0; i < BUFFERED_COUNT; i++) {
//            int2Int[i] = null;
//        }
//    }

    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    protected APTBaseLanguageFilter(boolean caseInsensitive) {
        if (caseInsensitive) {
            filter = new TreeMap<CharSequence,Integer>(CharSequenceKey.ComparatorIgnoreCase);
        } else {
            filter = new HashMap<CharSequence,Integer>();
        }
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

//    // do necessary initializations in derived classes by calling
//    // filter() method to fill up the filter
//    protected abstract void initialize();

    /**
     * add token's key to be filtered
     * the token stream returned from getFilteredStream
     * will change the type of original token to new token type
     * if original token has the filtered textKey value
     */
    protected void filter(String text, int ttype) {
        // uncomment to use reduced memory
//        if (ttype < BUFFERED_COUNT) {
//            if (int2Int[ttype] == null) {
//                int2Int[ttype] = new Integer(ttype);
//            }
//        }
//        Integer val = (ttype < BUFFERED_COUNT) ? int2Int[ttype] : new Integer(ttype);
//        assert(val != null);
//        filter.put(textKey, val);
        filter.put(CharSequenceKey.create(text), Integer.valueOf(ttype));
        keywords.add(Integer.valueOf(ttype));
    }

    protected Token onID(Token token) {
        Integer newType = filter.get(((APTToken)token).getTextID());
        if (newType != null) {
            int ttype = newType.intValue();
            token = new FilterToken((APTToken)token, ttype);
        }
        return token;
    }

    protected Token onToken(Token token) {
        if (token.getType() == APTTokenTypes.ID) {
            token = onID(token);
        }
        return token;
    }

    private final class FilterStream implements TokenStream {
        private TokenStream orig;
        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            Token token = orig.nextToken();
            token = onToken(token);
            return token;
        }
    }

    public boolean isKeyword(Token token) {
        if (token.getType() == APTTokenTypes.ID) {
            Integer newType = filter.get(((APTToken)token).getTextID());
            if (newType != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isKeyword(int ttype) {
        return keywords.contains(Integer.valueOf(ttype));
    }
    /**
     * A wrapper token that changes original token type
     * and delegates the rest of the methods to original token.
     */
    public static final class FilterToken implements APTToken {

        private final APTToken origToken;
        private int type;

        public FilterToken(APTToken origToken, int type) {
            this.origToken = origToken;
            this.type = type;
        }

        public APTToken getOriginalToken() {
            return origToken;
        }

        @Override
        public int getOffset() {
            return origToken.getOffset();
        }

        @Override
        public void setOffset(int o) {
            origToken.setOffset(o);
        }

        @Override
        public int getEndColumn() {
            return origToken.getEndColumn();
        }

        @Override
        public void setEndColumn(int c) {
            origToken.setEndColumn(c);
        }

        @Override
        public int getEndLine() {
            return origToken.getEndLine();
        }

        @Override
        public void setEndLine(int l) {
            origToken.setEndLine(l);
        }

        @Override
        public int getEndOffset() {
            return origToken.getEndOffset();
        }

        @Override
        public void setEndOffset(int o) {
            origToken.setEndOffset(o);
        }

        @Override
        public CharSequence getTextID() {
            return origToken.getTextID();
        }

        @Override
        public void setTextID(CharSequence id) {
            origToken.setTextID(id);
        }

        @Override
        public int getColumn() {
            return origToken.getColumn();
        }

        @Override
        public void setColumn(int c) {
            origToken.setColumn(c);
        }

        @Override
        public int getLine() {
            return origToken.getLine();
        }

        @Override
        public void setLine(int l) {
            origToken.setLine(l);
        }

        @Override
        public String getFilename() {
            return origToken.getFilename();
        }

        @Override
        public void setFilename(String name) {
            origToken.setFilename(name);
        }

        @Override
        public String getText() {
            return origToken.getText();
        }

        @Override
        public void setText(String t) {
            origToken.setText(t);
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public void setType(int t) {
            this.type = t;
        }

        @Override
        public String toString() {
            return "FilterToken: " + type + ((origToken == null) ? "null" : origToken.toString()); // NOI18N
        }
    }

}
