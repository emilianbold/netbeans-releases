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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Description of the change in a token list.
 * <br/>
 * The change is expressed as a list of removed tokens
 * plus the current list and index and number of the tokens
 * added to the current list.
 * <br/>
 * Some of the information that needs to be exported into TokenChange
 * is synced in a tokenChangeInfo that this class manages.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenListChange<T extends TokenId> {
    
    private final TokenChangeInfo<T> tokenChangeInfo;
    
    /**
     * The list may store either tokens or branches as well.
     */
    private List<Object> addedTokensOrBranches;

    private LAState laState;

    private int offsetGapIndex;
    
    private int removedEndOffset;
    
    private int addedEndOffset;
    
    public TokenListChange(MutableTokenList<T> tokenList) {
        tokenChangeInfo = new TokenChangeInfo<T>(tokenList);
    }
    
    public TokenChangeInfo<T> tokenChangeInfo() {
        return tokenChangeInfo;
    }
    
    public MutableTokenList<T> tokenList() {
        return (MutableTokenList<T>)tokenChangeInfo.currentTokenList();
    }
    
    public LanguagePath languagePath() {
        return tokenList().languagePath();
    }

    public int index() {
        return tokenChangeInfo.index();
    }

    public void setIndex(int tokenIndex) {
        tokenChangeInfo.setIndex(tokenIndex);
    }
    
    public int offset() {
        return tokenChangeInfo.offset();
    }

    public void setOffset(int offset) {
        tokenChangeInfo.setOffset(offset);
    }
    
    public int offsetGapIndex() {
        return offsetGapIndex;
    }

    public void setOffsetGapIndex(int offsetGapIndex) {
        this.offsetGapIndex = offsetGapIndex;
    }

    public void addToken(AbstractToken<T> token, int lookahead, Object state) {
        if (addedTokensOrBranches == null) {
            addedTokensOrBranches = new ArrayList<Object>(2);
            laState = LAState.empty();
        }
        addedTokensOrBranches.add(token);
        laState = laState.add(lookahead, state);
    }
    
    public List<Object> addedTokensOrBranches() {
        return addedTokensOrBranches;
    }
    
    public int addedTokensOrBranchesCount() {
        return (addedTokensOrBranches != null) ? addedTokensOrBranches.size() : 0;
    }
    
    public void removeLastAddedToken() {
        int lastIndex = addedTokensOrBranches.size() - 1;
        addedTokensOrBranches.remove(lastIndex);
        laState.remove(lastIndex, 1);
    }
    
    public AbstractToken<T> addedToken(int index) {
        return LexerUtilsConstants.token(addedTokensOrBranches.get(0));
    }
    
    public void syncAddedTokenCount() {
        tokenChangeInfo.setAddedTokenCount(addedTokensOrBranches.size());
    }

    public void setRemovedTokens(Object[] removedTokensOrBranches) {
        tokenChangeInfo.setRemovedTokenList(new RemovedTokenList<T>(
                languagePath(), removedTokensOrBranches));
    }
    
    public int removedEndOffset() {
        return removedEndOffset;
    }
    
    public void setRemovedEndOffset(int removedEndOffset) {
        this.removedEndOffset = removedEndOffset;
    }
    
    public int addedEndOffset() {
        return addedEndOffset;
    }
    
    public void setAddedEndOffset(int addedEndOffset) {
        this.addedEndOffset = addedEndOffset;
    }
    
    public boolean isBoundsChange() {
        return tokenChangeInfo.isBoundsChange();
    }
    
    public void markBoundsChange() {
        tokenChangeInfo.markBoundsChange();
    }
    
    public LAState laState() {
        return laState;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append(languagePath().innerLanguage().mimeType());
        sb.append("\", index=");
        sb.append(index());
        sb.append(", offset=");
        sb.append(offset());
        if (isBoundsChange()) {
            sb.append(", boundsChange");
        }
        TokenList<T> removedTL = tokenChangeInfo.removedTokenList();
        if (removedTL != null && removedTL.tokenCount() > 0) {
            int digitCount = ArrayUtilities.digitCount(removedTL.tokenCount() - 1);
            for (int i = 0; i < removedTL.tokenCount(); i++) {
                sb.append('\n');
                ArrayUtilities.appendSpaces(sb, indent);
                sb.append("R[");
                ArrayUtilities.appendIndex(sb, i, digitCount);
                sb.append("]: ");
                LexerUtilsConstants.appendTokenInfo(sb, removedTL, i, null, false, 0);
            }
        }
        if (addedTokensOrBranches() != null) {
            int digitCount = ArrayUtilities.digitCount(addedTokensOrBranches().size() - 1);
            for (int i = 0; i < addedTokensOrBranches().size(); i++) {
                sb.append('\n');
                ArrayUtilities.appendSpaces(sb, indent);
                sb.append("A[");
                ArrayUtilities.appendIndex(sb, i, digitCount);
                sb.append("]: ");
                LexerUtilsConstants.appendTokenInfo(sb, addedTokensOrBranches.get(i),
                        laState.lookahead(i), laState.state(i), null, false, 0);
            }
        }
        return sb.toString();
    }
}