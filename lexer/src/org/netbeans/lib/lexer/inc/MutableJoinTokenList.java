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

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.JoinTokenListBase;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TokenListList;
import org.netbeans.lib.lexer.TokenOrEmbedding;


/**
 * Mutable join token list allows mutations by token list updater.
 * 
 * @author Miloslav Metelka
 */

class MutableJoinTokenList<T extends TokenId> extends JoinTokenList<T> implements MutableTokenList<T> {
    
    static <T extends TokenId> MutableJoinTokenList<T> create(TokenListList<T> tokenListList, int etlIndex) {
        MutableJoinTokenList<T> jtl;
        if (etlIndex >= 0) {
            EmbeddedTokenList<T> etl = tokenListList.get(etlIndex);
            int tokenListStartIndex = etlIndex - etl.joinInfo.tokenListIndex();
            jtl = new MutableJoinTokenList<T>(tokenListList, etl.joinInfo.base, tokenListStartIndex);
            // Position to this etl's join index
            jtl.setActiveTokenListIndex(etlIndex - tokenListStartIndex);
        } else { // No ETLs present in TokenListList
            assert (tokenListList.size() == 0);
            jtl = new MutableJoinTokenList<T>(tokenListList, new JoinTokenListBase(0), 0);
        }
        return jtl;
    }

    MutableJoinTokenList(TokenListList<T> tokenListList, JoinTokenListBase base, int tokenListStartIndex) {
        super(tokenListList, base, tokenListStartIndex);
    }

    public TokenOrEmbedding<T> tokenOrEmbeddingUnsync(int index) {
        return tokenOrEmbedding(index);
    }

    public boolean isFullyLexed() {
        return true;
    }

    public void replaceTokens(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo, boolean modInside) {
        base.incrementExtraModCount();
        ((JoinTokenListChange<T>) change).replaceTokens(eventInfo);
    }

    public LexerInputOperation<T> createLexerInputOperation(int tokenIndex, int relexOffset, Object relexState) {
        // Should never be called
        throw new IllegalStateException("Should never be called"); // NOI18N
    }

    public void setPrevActiveTokenListIndex() {
        activeTokenListIndex--;
        fetchActiveTokenListData();
    }
    
    @Override
    protected void updateStatus(EmbeddedTokenList<T> etl) {
        etl.embeddingContainer().updateStatusUnsync();
    }

    void moveIndexGap(int index) {
        base.moveIndexGap(tokenListList, tokenListStartIndex, index);
    }

    public void resetActiveAfterUpdate() { // Update the active token list after updating
        activeTokenListIndex = 0;
        fetchActiveTokenListData();
    }

}

