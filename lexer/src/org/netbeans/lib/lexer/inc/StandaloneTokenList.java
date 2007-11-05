/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.lib.lexer.inc;

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;


/**
 * Single token list maintains a text for a single token.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class StandaloneTokenList<T extends TokenId> implements TokenList<T> {

    private char[] tokenText;

    private LanguagePath languagePath;
    
    public StandaloneTokenList(LanguagePath languagePath, char[] tokenText) {
        this.languagePath = languagePath;
        this.tokenText = tokenText;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public Object tokenOrEmbeddingContainer(int index) {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }

    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }

    public int lookahead(int index) {
        return -1;
    }

    public Object state(int index) {
        return null;
    }

    public int tokenOffset(int index) {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }
    
    public int tokenCount() {
        return 1;
    }

    public int tokenCountCurrent() {
        return 1;
    }

    public int modCount() {
        return -1;
    }
    
    public int childTokenOffset(int rawOffset) {
        // Offset of the standalone token is absolute
        return rawOffset;
    }
    
    public char childTokenCharAt(int rawOffset, int index) {
        return tokenText[index];
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        throw new IllegalStateException("Branching of standalone tokens not supported"); // NOI18N
    }
    
    public TokenList<?> root() {
        return this;
    }
    
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return null;
    }
    
    public InputAttributes inputAttributes() {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }
    
    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }

    public int startOffset() {
        if (tokenCountCurrent() > 0 || tokenCount() > 0)
            return tokenOffset(0);
        return 0;
    }

    public int endOffset() {
        int cntM1 = tokenCount() - 1;
        if (cntM1 >= 0)
            return tokenOffset(cntM1) + LexerUtilsConstants.token(this, cntM1).length();
        return 0;
    }

}
