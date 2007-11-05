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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;

/**
 * Abstract token is base class of all token implementations used in the lexer module.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractToken<T extends TokenId> extends Token<T> implements CharSequence {
    
    private final T id; // 12 bytes (8-super + 4)

    private TokenList<T> tokenList; // 16 bytes
    
    private int rawOffset; // 20 bytes

    /**
     * @id non-null token id.
     */
    public AbstractToken(T id) {
        assert (id != null);
        this.id = id;
    }
    
    AbstractToken(T id, TokenList<T> tokenList, int rawOffset) {
        this.id = id;
        this.tokenList = tokenList;
        this.rawOffset = rawOffset;
    }
    
    public abstract int length();
    
    /**
     * Get identification of this token.
     *
     * @return non-null identification of this token.
     */
    @Override
    public final T id() {
        return id;
    }

    /**
     * Get text represented by this token.
     */
    @Override
    public CharSequence text() {
        if (tokenList != null) {
            if (tokenList.getClass() == EmbeddedTokenList.class) {
                EmbeddedTokenList<?> etl = (EmbeddedTokenList<?>)tokenList;
                return etl.embeddingContainer().updateStatus() ? this : null;
            }
            return this;
        } else {
            return null;
        }
    }

    /**
     * Get token list to which this token delegates its operation.
     */
    public final TokenList<T> tokenList() {
        return tokenList;
    }

    /**
     * Release this token from being attached to its parent token list.
     */
    public final void setTokenList(TokenList<T> tokenList) {
        this.tokenList = tokenList;
    }

    /**
     * Get raw offset of this token.
     * <br/>
     * Raw offset must be preprocessed before obtaining the real offset.
     */
    public final int rawOffset() {
        return rawOffset;
    }

    /**
     * Set raw offset of this token.
     *
     * @param rawOffset new raw offset.
     */
    public final void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }

    @Override
    public final boolean isFlyweight() {
        return (rawOffset == -1);
    }

    public final void makeFlyweight() {
        setRawOffset(-1);
    }
    
    @Override
    public PartType partType() {
        return PartType.COMPLETE;
    }

    @Override
    public boolean isCustomText() {
        return false;
    }

    @Override
    public final int offset(TokenHierarchy<?> tokenHierarchy) {
        if (rawOffset == -1) { // flyweight token
            return -1;
        }

        return (tokenList != null)
                ? tokenList.childTokenOffset(rawOffset)
                : rawOffset;
//        if (tokenHierarchy != null) {
//            return LexerApiPackageAccessor.get().tokenHierarchyOperation(
//                    tokenHierarchy).tokenOffset(this, tokenList, rawOffset);
//        } else {
//            return (tokenList != null)
//                ? tokenList.childTokenOffset(rawOffset)
//                : rawOffset;
//        }
    }
    
    @Override
    public boolean hasProperties() {
        return false;
    }
    
    @Override
    public Object getProperty(Object key) {
        return null;
    }

    // CharSequence methods
    /**
     * Implementation of <code>CharSequence.charAt()</code>
     */
    public final char charAt(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException(
                "index=" + index + ", length=" + length() // NOI18N
            );
        }
        if (tokenList == null) { // Should normally not happen
            // A bit strange to throw IOOBE but it's more practical since
            // TokenHierarchy's dump can overcome IOOBE and deliver a useful debug but not NPEs etc.
            throw new IndexOutOfBoundsException("index=" + index + ", length=" + length() +
                    " but tokenList==null for token " + dumpInfo(null));
        }
        return tokenList.childTokenCharAt(rawOffset, index);
    }

    public final CharSequence subSequence(int start, int end) {
        return CharSequenceUtilities.toString(this, start, end);
    }
    
    /**
     * This method is in fact <code>CharSequence.toString()</code> implementation.
     */
    @Override
    public String toString() {
        // To prevent NPEs when token.toString() would called without checking
        // (text() == null) there is an extra check for that.
        CharSequence text = text();
        return (text != null)
                ? CharSequenceUtilities.toString(this, 0, length())
                : "<null>";
    }

    /**
     * Dump various information about this token
     * into a string for debugging purporses.
     * <br>
     * A regular <code>toString()</code> usually just returns
     * a text of the token to satisfy acting of the token instance
     * as <code>CharSequence</code>.
     *
     * @param tokenHierarchy <code>null</code> should be passed
     *  (the parameter is reserved for future use when token hierarchy snapshots will be implemented).
     * @return dump of the thorough token information.
     */
    public String dumpInfo(TokenHierarchy<?> tokenHierarchy) {
        StringBuilder sb = new StringBuilder();
        CharSequence text = text();
        if (text != null) {
            sb.append('"');
            for (int i = 0; i < text.length(); i++) {
                try {
                    CharSequenceUtilities.debugChar(sb, text.charAt(i));
                } catch (IndexOutOfBoundsException e) {
                    // For debugging purposes it's better than to completely fail
                    sb.append("IOOBE at index=").append(i).append("!!!"); // NOI18N
                    break;
                }
            }
            sb.append('"');
        } else {
            sb.append("<null-text>"); // NOI18N
        }
        sb.append(' ');
        if (isFlyweight()) {
            sb.append("F(").append(length()).append(')');
        } else {
            int offset = offset(tokenHierarchy);
            sb.append('<').append(offset); // NOI18N
            sb.append(",").append(offset + length()).append('>'); // NOI18N
        }
        sb.append(' ').append(id != null ? id.name() + '[' + id.ordinal() + ']' : "<null-id>"); // NOI18N
        sb.append(" ").append(dumpInfoTokenType());
        return sb.toString();
    }
    
    protected String dumpInfoTokenType() {
        return "AbsT"; // NOI18N "AbstractToken"
    }
    
}
