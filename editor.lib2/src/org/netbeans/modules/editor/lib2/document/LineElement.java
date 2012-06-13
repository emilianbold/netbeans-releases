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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;

/**
 * Line element implementation.
 * <br>
 * It only holds the starting position.The ending position
 * is obtained by being connected to another line-element chain member
 * or by having a link to position.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

public final class LineElement implements Element, Position {
    
    /** Parent and root element */
    private final LineElementRoot root; // 8 + 4 = 12 bytes
    
    /** Position at the beginning of the line */
    private final Position startPos; // 12 + 4 = 16 bytes
    
    /** Next line or null if this is the last line. */
    private final Position endPos; // 16 + 4 = 20 bytes
    
    /**
     * Attributes of this line element
     */
    private Object attributes; // 20 + 4 = 24 bytes
    
    LineElement(LineElementRoot root, Position startPos, Position endPos) {
        assert(startPos != null);
        assert(endPos != null);

        this.root = root;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    @Override
    public Document getDocument() {
        return root.getDocument();
    }

    @Override
    public int getStartOffset() {
        return startPos.getOffset();
    }
    
    public Position getStartPosition() {
        return startPos;
    }

    @Override
    public int getOffset() {
        return getStartOffset();
    }
    
    @Override
    public int getEndOffset() {
        return endPos.getOffset();
    }
    
    public Position getEndPosition() {
        return endPos;
    }

    @Override
    public Element getParentElement() {
        return root;
    }

    @Override
    public String getName() {
        return AbstractDocument.ParagraphElementName;
    }

    @Override
    public AttributeSet getAttributes() {
        // Do not return null since Swing's view factories assume that this is non-null.
        return (attributes instanceof AttributeSet) ? (AttributeSet) attributes : SimpleAttributeSet.EMPTY;
    }
    
    public void setAttributes(AttributeSet attributes) {
        this.attributes = attributes;
    }
    
    public Object legacyGetAttributesObject() {
        return attributes;
    }
    
    public void legacySetAttributesObject(Object attributes) {
        this.attributes = attributes;
    }

    @Override
    public int getElementIndex(int offset) {
        return -1;
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public Element getElement(int index) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
    
    @Override
    public String toString() {
        return "getStartOffset()=" + getStartOffset() // NOI18N
            + ", getEndOffset()=" + getEndOffset(); // NOI18N
    }

}
