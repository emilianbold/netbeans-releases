/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
class APTGnuCpp11FilterEx  implements APTLanguageFilter {
    
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    public APTGnuCpp11FilterEx() {
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

    private final class FilterStream implements TokenStream {
        private TokenStream orig;
        private Token shiftToken = null;

        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            if (shiftToken != null) {
                Token token = shiftToken;
                shiftToken = null;
                return new FilterToken((APTToken)token, APTTokenTypes.GREATERTHAN);
            }
            
            Token newToken = orig.nextToken();
            if (newToken.getType() == APTTokenTypes.SHIFTRIGHT) {
                shiftToken = newToken;
                return new FilterToken((APTToken)newToken, APTTokenTypes.GREATERTHAN);
            }
            return newToken;
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

