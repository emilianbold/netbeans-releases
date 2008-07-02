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

package org.netbeans.lib.lexer.batch;

import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Filtering token list constructed over character array with an independent
 * start offset value.
 * <br>
 * It is constructed for batch inputs and it implements
 * a token list but it only implements translation of raw offsets
 * into real offsets and retrieving of the characters of token bodies.
 * <br>
 * Other operations are delegated to an original
 * token list that really holds the tokens.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SkimTokenList<T extends TokenId> implements TokenList<T> {
    
    private CopyTextTokenList<T> tokenList;
    
    private int startOffset;
    
    private char[] text;
    
    
    public SkimTokenList(CopyTextTokenList<T> tokenList, int startOffset, char[] text) {
        this.tokenList = tokenList;
        this.startOffset = startOffset;
        this.text = text;
    }

    public CopyTextTokenList<T> getTokenList() {
        return tokenList;
    }
    
    public int startOffset() {
        return tokenList.startOffset();
    }
    
    public int endOffset() {
        return tokenList.endOffset();
    }
    
    public boolean isRemoved() {
        return tokenList.isRemoved();
    }

    char[] getText() {
        return text;
    }
    
    void setText(char[] text) {
        this.text = text;
    }

    public int childTokenOffset(int rawOffset) {
        int offsetShift = (rawOffset >> 16);
        return startOffset + (rawOffset & 0xFFFF) + offsetShift;
    }

    public char childTokenCharAt(int rawOffset, int index) {
        return text[((rawOffset + index) & 0xFFFF)];
    }

    public int modCount() {
        return 0;
    }

    public Object tokenOrEmbeddingContainer(int index) {
        return tokenList.tokenOrEmbeddingContainer(index);
    }
    
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        return tokenList.replaceFlyToken(index, flyToken, offset);
    }
    

    public int lookahead(int index) {
        return tokenList.lookahead(index);
    }

    public Object state(int index) {
        return tokenList.state(index);
    }

    public int tokenOffset(int index) {
        return tokenList.tokenOffset(index);
    }

    public int tokenCount() {
        return tokenList.tokenCount();
    }
    
    public int tokenCountCurrent() {
        return tokenList.tokenCountCurrent();
    }

    public TokenList<?> root() {
        return tokenList.root();
    }

    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenList.tokenHierarchyOperation();
    }
    
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        tokenList.wrapToken(index, embeddingContainer);
    }

    public InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }
    
    public boolean isContinuous() {
        return tokenList.isContinuous();
    }

    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }

}
