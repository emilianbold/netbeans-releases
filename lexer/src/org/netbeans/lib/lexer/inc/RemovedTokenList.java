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
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;

/**
 * Token list implementation holding added or removed tokens from a list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class RemovedTokenList<T extends TokenId> implements TokenList<T> {
    
    private final LanguagePath languagePath;
    
    private Object[] tokensOrBranches;
    
    private int removedTokensStartOffset;
    
    public RemovedTokenList(LanguagePath languagePath, Object[] tokensOrBranches) {
        this.languagePath = languagePath;
        this.tokensOrBranches = tokensOrBranches;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public Object tokenOrEmbeddingContainer(int index) {
        return (index < tokensOrBranches.length) ? tokensOrBranches[index] : null;
    }

    public int lookahead(int index) {
        return -1;
    }

    public Object state(int index) {
        return null;
    }

    public int tokenOffset(int index) {
        Token<?> token = existingToken(index);
        if (token.isFlyweight()) {
            int offset = 0;
            while (--index >= 0) {
                token = existingToken(index);
                offset += token.length();
                if (!token.isFlyweight()) {
                    // Return from here instead of break; - see code after while()
                    return offset + token.offset(null);
                }
            }
            // might remove token sequence starting with flyweight
            return removedTokensStartOffset + offset;

        } else { // non-flyweight offset
            return token.offset(null);
        }
    }

    private Token<T> existingToken(int index) {
        return LexerUtilsConstants.token(tokensOrBranches[index]);
    }

    public synchronized AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
        tokensOrBranches[index] = nonFlyToken;
        return nonFlyToken;
    }

    public int tokenCount() {
        return tokenCountCurrent();
    }

    public int tokenCountCurrent() {
        return tokensOrBranches.length;
    }

    public int modCount() {
        return -1;
    }
    
    public int childTokenOffset(int rawOffset) {
        // Offsets of contained tokens are absolute
        return rawOffset;
    }
    
    public char childTokenCharAt(int rawOffset, int index) {
        throw new IllegalStateException("Querying of text for removed tokens not supported"); // NOI18N
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        throw new IllegalStateException("Branching of removed tokens not supported"); // NOI18N
    }
    
    public TokenList<?> root() {
        return this;
    }
    
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return null;
    }
    
    public InputAttributes inputAttributes() {
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

    public boolean isRemoved() {
        return true; // Collects tokens removed from TH
    }

    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }

    @Override
    public String toString() {
        return LexerUtilsConstants.appendTokenList(null, this).toString();
    }
    
}
