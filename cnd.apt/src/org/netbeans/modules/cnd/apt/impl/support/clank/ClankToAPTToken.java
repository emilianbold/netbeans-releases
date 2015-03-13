/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.impl.support.clank;

import org.clang.basic.IdentifierInfo;
import org.clang.basic.tok;
import org.clang.lex.Token;
import org.clank.support.Casts;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Voskresensky
 */
/*package*/class ClankToAPTToken implements APTToken {

    private static final CharSequence COMMENT_TEXT_ID = CharSequences.create("/*COMMENT*/");

    private final Token orig;
    private final int aptTokenType;

    /*package*/ClankToAPTToken(Token token) {
        this.orig = token;
        this.aptTokenType = ClankToAPTUtils.convertClankToAPTTokenKind(token.getKind());
    }

    @Override
    public int getType() {
        return aptTokenType;
    }

    @Override
    public String getText() {
        if (APTLiteConstTextToken.isLiteConstTextType(aptTokenType)) {
            return APTLiteConstTextToken.toText(aptTokenType);
        }
        return getTextID().toString();
    }

    @Override
    public CharSequence getTextID() {
        if (APTLiteConstTextToken.isLiteConstTextType(aptTokenType)) {
            return APTLiteConstTextToken.toTextID(aptTokenType);
        } else if (orig.isLiteral()) {
            return CharSequences.create(Casts.toCharSequence(orig.getLiteralData()));
        } else if (orig.is(tok.TokenKind.comment)) {
            return COMMENT_TEXT_ID;
        } else if (orig.is(tok.TokenKind.raw_identifier)) {
            return CharSequences.create(Casts.toCharSequence(orig.getRawIdentifierData()));
        } else {
            IdentifierInfo identifierInfo = orig.getIdentifierInfo();
            assert identifierInfo != null : "No Text for " + orig;
            return CharSequences.create(Casts.toCharSequence(identifierInfo.getNameStart()));
        }
    }

    @Override
    public String toString() {
        return "ClankToAPTToken{" + "aptTokenType=" + APTUtils.getAPTTokenName(aptTokenType) + "\norig=" + orig + '}';
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public void setOffset(int o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEndOffset() {
        return 1;
    }

    @Override
    public void setEndOffset(int o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEndColumn() {
        return 1;
    }

    @Override
    public void setEndColumn(int c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEndLine() {
        return 1;
    }

    @Override
    public void setEndLine(int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTextID(CharSequence id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getProperty(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getColumn() {
        return 0;
    }

    @Override
    public void setColumn(int c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLine() {
        return 1;
    }

    @Override
    public void setLine(int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public void setFilename(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setText(String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setType(int t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
