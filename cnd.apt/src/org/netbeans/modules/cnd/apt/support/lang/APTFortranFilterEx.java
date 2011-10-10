/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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


package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 * @author nk220367
 */
final class APTFortranFilterEx implements APTLanguageFilter {
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    public APTFortranFilterEx() {
    }

    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

    private final class FilterStream implements TokenStream {
        private TokenStream orig;
        private Token nextToken = null;
        boolean newLine = false;

        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        public Token nextToken() throws TokenStreamException {
            if (nextToken != null) {
                Token ret = nextToken;
                nextToken = null;
                return ret;
            }
            
            Token newToken = orig.nextToken();
            if (newToken.getType() == APTTokenTypes.T_ASTERISK) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.T_ASTERISK) {
                    nextToken = null;
                    return new FilterToken((APTToken)newToken, APTTokenTypes.T_POWER);
                }
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
