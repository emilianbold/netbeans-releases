/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TokenHierarchyOperation;

/**
 * Shared information for all the token list changes
 * for a single token hierarchy event.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyEventInfo {

    private final TokenHierarchyOperation<?,?> tokenHierarchyOperation;

    private final TokenHierarchyEventType type;
    
    private TokenChange<? extends TokenId> tokenChange;

    private final int modificationOffset;

    private final int removedLength;

    private final CharSequence removedText;

    private final int insertedLength;
    
    private final int diffLengthOrZero;

    private OriginalText originalText;
    
    private int affectedStartOffset;
    
    private int affectedEndOffset;

    public TokenHierarchyEventInfo(TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation,
    TokenHierarchyEventType type, int modificationOffset, int removedLength, CharSequence removedText, int insertedLength) {
        // Initial checks
        if (modificationOffset < 0) {
            throw new IllegalArgumentException("modificationOffset=" + modificationOffset + " < 0"); // NOI18N
        }
        if (removedLength < 0) {
            throw new IllegalArgumentException("removedLength=" + removedLength + " < 0"); // NOI18N
        }
        if (insertedLength < 0) {
            throw new IllegalArgumentException("insertedLength=" + insertedLength + " < 0"); // NOI18N
        }

        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.type = type;
        this.modificationOffset = modificationOffset;
        this.removedLength = removedLength;
        this.removedText = removedText;
        this.insertedLength = insertedLength;
        this.diffLengthOrZero = Math.max(0, insertedLength - removedLength);
        this.affectedStartOffset = modificationOffset;
        this.affectedEndOffset = modificationOffset + diffLengthOrZero;
    }

    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }

    public TokenHierarchyEventType type() {
        return type;
    }
    
    public TokenChange<? extends TokenId> tokenChange() {
        return tokenChange;
    }
    
    public void setTokenChangeInfo(TokenChangeInfo<? extends TokenId> info) {
        this.tokenChange = LexerApiPackageAccessor.get().createTokenChange(info);
    }
    
    public int affectedStartOffset() {
        return affectedStartOffset;
    }
    
    public void setMinAffectedStartOffset(int affectedStartOffset) {
        if (affectedStartOffset < this.affectedStartOffset) {
            this.affectedStartOffset = affectedStartOffset;
        }
    }

    public int affectedEndOffset() {
        return affectedEndOffset;
    }
    
    public void setMaxAffectedEndOffset(int affectedEndOffset) {
        if (affectedEndOffset > this.affectedEndOffset) {
            this.affectedEndOffset = affectedEndOffset;
        }
    }

    public int modificationOffset() {
        return modificationOffset;
    }

    public int removedLength() {
        return removedLength;
    }

    public CharSequence removedText() {
        return removedText;
    }

    public int insertedLength() {
        return insertedLength;
    }
    
    /**
     * Get <code>Math.max(0, insertedLength() - removedLength())</code>.
     */
    public int diffLengthOrZero() {
        return diffLengthOrZero;
    }

    public OriginalText originalText() {
        if (originalText == null) {
            if (removedLength != 0 && removedText == null) {
                throw new IllegalStateException("Cannot obtain removed text for " // NOI18N
                        + tokenHierarchyOperation.mutableInputSource()
                        + " which breaks token snapshots operation and" // NOI18N
                        + " token text retaining after token's removal." // NOI18N
                        + " Valid removedText in TokenHierarchyControl.textModified()" // NOI18N
                        + " should be provided." // NOI18N
                        );
            }
            originalText = new OriginalText(
                    LexerSpiPackageAccessor.get().text(tokenHierarchyOperation.mutableTextInput()),
                    modificationOffset, removedText, insertedLength
                    );
        }
        return originalText;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("modOffset="); // NOI18N
        sb.append(modificationOffset());
        if (removedLength() > 0) {
            sb.append(", removedLength=");
            sb.append(removedLength());
        }
        if (insertedLength() > 0) {
            sb.append(", insertedLength=");
            sb.append(insertedLength()); // NOI18N
        }
        sb.append(", Affected(");
        sb.append(affectedStartOffset());
        sb.append(",");
        sb.append(affectedEndOffset());
        sb.append(')');
        return sb.toString();
    }

}
