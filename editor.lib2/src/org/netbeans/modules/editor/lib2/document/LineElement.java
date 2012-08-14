/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
