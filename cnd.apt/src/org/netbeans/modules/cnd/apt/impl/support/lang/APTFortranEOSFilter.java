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
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 * @author nk220367
 */
public class APTFortranEOSFilter implements APTLanguageFilter {
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    public APTFortranEOSFilter() {
    }

    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

    private final class FilterStream implements TokenStream {
        private TokenStream orig;
        private Token currentToken;
        boolean newLine = false;

        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        public Token nextToken() throws TokenStreamException {
            if(newLine) {
                newLine = false;
                return currentToken;
            }
            Token newToken = orig.nextToken();
            if (currentToken != null && newToken.getType() != APTTokenTypes.EOF && newToken.getLine() != currentToken.getLine()) {
                Token eos = new EOSToken((APTToken) currentToken);
                currentToken = newToken;
                newLine = true;
                return eos;
            }
            if (newToken.getType() == APTTokenTypes.SEMICOLON) {
                currentToken = newToken;
                return new FilterToken((APTToken)currentToken, APTTokenTypes.T_EOS);
            }
            currentToken = newToken;
            return currentToken;
        }
    }

    public static final class EOSToken implements APTToken {

        int offset;
        int endOffset;
        int column;
        int endColumn;
        int line;
        int endLine;
        String fileName;

        EOSToken(APTToken token) {
            offset = token.getOffset();
            endOffset = token.getEndOffset();
            column = token.getColumn();
            endColumn = token.getEndColumn();
            line = token.getLine();
            endLine = token.getEndLine();
            fileName = token.getFilename();
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int o) {
            offset = o;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(int o) {
            endOffset = o;
        }

        public int getEndColumn() {
            return endColumn;
        }

        public void setEndColumn(int c) {
            endColumn = c;
        }

        public int getEndLine() {
            return endLine;
        }

        public void setEndLine(int l) {
            endLine = l;
        }

        public String getText() {
            return "<EOS>"; // NOI18N
        }

        public CharSequence getTextID() {
            return "<EOS>"; // NOI18N
        }

        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int c) {
            column = c;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int l) {
            line = l;
        }

        public String getFilename() {
            return fileName;
        }

        public void setFilename(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setText(String t) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int getType() {
            return APTTokenTypes.T_EOS;
        }

        public void setType(int t) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

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

        public int getOffset() {
            return origToken.getOffset();
        }

        public void setOffset(int o) {
            origToken.setOffset(o);
        }

        public int getEndColumn() {
            return origToken.getEndColumn();
        }

        public void setEndColumn(int c) {
            origToken.setEndColumn(c);
        }

        public int getEndLine() {
            return origToken.getEndLine();
        }

        public void setEndLine(int l) {
            origToken.setEndLine(l);
        }

        public int getEndOffset() {
            return origToken.getEndOffset();
        }

        public void setEndOffset(int o) {
            origToken.setEndOffset(o);
        }

        public CharSequence getTextID() {
            return origToken.getTextID();
        }

        public void setTextID(CharSequence id) {
            origToken.setTextID(id);
        }

        public int getColumn() {
            return origToken.getColumn();
        }

        public void setColumn(int c) {
            origToken.setColumn(c);
        }

        public int getLine() {
            return origToken.getLine();
        }

        public void setLine(int l) {
            origToken.setLine(l);
        }

        public String getFilename() {
            return origToken.getFilename();
        }

        public void setFilename(String name) {
            origToken.setFilename(name);
        }

        public String getText() {
            return origToken.getText();
        }

        public void setText(String t) {
            origToken.setText(t);
        }

        public int getType() {
            return type;
        }

        public void setType(int t) {
            this.type = t;
        }

        @Override
        public String toString() {
            return "FilterToken: " + type + ((origToken == null) ? "null" : origToken.toString()); // NOI18N
        }
    }

}
