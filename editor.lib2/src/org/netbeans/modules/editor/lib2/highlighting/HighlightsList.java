/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.awt.Font;
import javax.swing.text.AttributeSet;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.editor.lib2.view.ViewUtils;

/**
 * List of highlights that can dynamically add/remove existing highlights fed from a highlights sequence.
 *
 * @author Miloslav Metelka
 */
public final class HighlightsList {
    
    /**
     * List of highlight items. First highlights item starts at startOffset.
     * <br/>
     * GapList is used due to its copyElements() method.
     */
    private HighlightItem[] highlightItems;
    
    private int startIndex;
    
    private int endIndex;
    
    /**
     * Start offset of the first highlight item.
     */
    private int startOffset;
    
    public HighlightsList(int startOffset) {
        this.highlightItems = new HighlightItem[4];
        this.startOffset = startOffset;
    }
    
    public HighlightsList(int startOffset, HighlightItem[] items) {
        this.highlightItems = items;
        this.endIndex = items.length;
        this.startOffset = startOffset;
    }
    
    public int startOffset() {
        return startOffset;
    }
    
    public void setStartOffset(int startOffset) {
        int firstItemEndOffset;
        assert (startOffset < (firstItemEndOffset = highlightItems[startIndex].getEndOffset())) :
                "startOffset=" + startOffset + " >= firstItemEndOffset=" + firstItemEndOffset; // NOI18N
        this.startOffset = startOffset;
    }
    
    public int endOffset() {
        return (endIndex - startIndex > 0)
                ? highlightItems[endIndex - 1].getEndOffset()
                : startOffset;
    }
    
    public int size() {
        return endIndex - startIndex;
    }
    
    public HighlightItem get(int index) {
        if (startIndex + index >= endIndex) {
            throw new IndexOutOfBoundsException("index=" + index + " >= size=" + size()); // NOI18N
        }
        return highlightItems[startIndex + index];
    }

    public void add(HighlightItem item) {
        if (endIndex == highlightItems.length) {
            if (startIndex == 0) {
                HighlightItem[] tmp = new HighlightItem[highlightItems.length << 1];
                System.arraycopy(highlightItems, 0, tmp, 0, highlightItems.length);
                highlightItems = tmp;
            } else { // Make startIndex == 0
                System.arraycopy(highlightItems, startIndex, highlightItems, 0, size());
                endIndex -= startIndex;
                startIndex = 0;
            }
        }
        highlightItems[endIndex++] = item;
    }

    /**
     * Create attribute set covering {@link #startOffset()} till maxEndOffset
     * or lower offset if font would differ for the particular attribute set
     * of an item.
     * <br/>
     * The list must cover cutEndOffset otherwise the behavior is undefined.
     *
     * @param defaultFont
     * @param maxEndOffset
     * @return 
     */
    public AttributeSet cutSameFont(Font defaultFont, int maxEndOffset) {
        assert (maxEndOffset <= endOffset()) : "maxEndOffset=" + maxEndOffset + " > endOffset()=" + endOffset(); // NOI18N
        HighlightItem item = highlightItems[startIndex];
        AttributeSet firstAttrs = item.getAttributes();
        int itemEndOffset = item.getEndOffset();
        if (maxEndOffset <= itemEndOffset) {
            if (maxEndOffset == itemEndOffset) {
                cutStartItems(1);
            }
            startOffset = maxEndOffset;
            return firstAttrs;
        }
        // Span two or more highlights
        Font firstFont = ViewUtils.getFont(firstAttrs, defaultFont);
        int index = 1;
        while (true) {
            item = highlightItems[startIndex + index];
            AttributeSet attrs = item.getAttributes();
            Font font = ViewUtils.getFont(attrs, defaultFont);
            if (!font.equals(firstFont)) { // Stop at itemEndOffset
                if (index == 1) { // Just single attribute set
                    cutStartItems(1);
                    startOffset = itemEndOffset; // end offset of first item
                    return firstAttrs;
                }
                // Index > 1
                return cutCompound(index, itemEndOffset); // end offset of first item
            }
            itemEndOffset = item.getEndOffset();
            if (maxEndOffset <= itemEndOffset) {
                if (maxEndOffset == itemEndOffset) {
                    return cutCompound(index + 1, itemEndOffset);
                }
                return cutCompoundNext(index, maxEndOffset, attrs);
            }
            index++;
        }
    }
    
    public AttributeSet cut(int endOffset) {
        assert (endOffset <= endOffset()) : "endOffset=" + endOffset + " > endOffset()=" + endOffset(); // NOI18N
        HighlightItem item = highlightItems[startIndex];
        AttributeSet attrs = item.getAttributes();
        int itemEndOffset = item.getEndOffset();
        if (endOffset <= itemEndOffset) {
            if (endOffset == itemEndOffset) {
                cutStartItems(1);
            }
            startOffset = endOffset;
            return attrs;
        }
        // Span two or more highlights
        int index = 1;
        while (true) {
            item = highlightItems[startIndex + index];
            itemEndOffset = item.getEndOffset();
            if (endOffset <= itemEndOffset) {
                if (endOffset == itemEndOffset) {
                    return cutCompound(index + 1, itemEndOffset);
                }
                return cutCompoundNext(index, endOffset, item.getAttributes());
            }
            index++;
        }
    }
    
    /**
     * Create attribute set covering single character at {@link #startOffset()}.
     * <br/>
     * The list must cover {@link #startOffset()} otherwise the behavior is undefined.
     *
     * @return attribute set.
     */
    public AttributeSet cutSingleChar() {
        HighlightItem item = highlightItems[startIndex];
        startOffset++;
        if (startOffset == item.getEndOffset()) {
            cutStartItems(1);
        }
        return item.getAttributes();
    }
    
    public void skip(int newStartOffset) {
        HighlightItem item = highlightItems[startIndex];
        int itemEndOffset = item.getEndOffset();
        if (newStartOffset <= itemEndOffset) {
            if (newStartOffset == itemEndOffset) {
                cutStartItems(1);
            }
        } else {
            int index = 1;
            while (true) {
                item = highlightItems[startIndex + index];
                itemEndOffset = item.getEndOffset();
                if (newStartOffset <= itemEndOffset) {
                    if (newStartOffset == itemEndOffset) {
                        cutStartItems(index + 1);
                    } else {
                        cutStartItems(index);
                    }
                    break;
                }
                index++;
            }
        }
        startOffset = newStartOffset;
    }

    private void cutStartItems(int count) {
        startIndex += count;
    }
    
    private CompoundAttributes cutCompound(int count, int lastItemEndOffset) {
        HighlightItem[] cutItems = new HighlightItem[count];
        System.arraycopy(highlightItems, startIndex, cutItems, 0, count);
        cutStartItems(count);
        CompoundAttributes cAttrs = new CompoundAttributes(startOffset, cutItems);
        startOffset = lastItemEndOffset;
        return cAttrs;
    }
    
    private CompoundAttributes cutCompoundNext(int count, int cutEndOffset, AttributeSet lastAttrs) {
        HighlightItem[] cutItems = new HighlightItem[count + 1];
        cutItems[count] = new HighlightItem(cutEndOffset, lastAttrs);
        System.arraycopy(highlightItems, startIndex, cutItems, 0, count);
        cutStartItems(count);
        CompoundAttributes cAttrs = new CompoundAttributes(startOffset, cutItems);
        startOffset = cutEndOffset;
        return cAttrs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        int size = size();
        int digitCount = ArrayUtilities.digitCount(size);
        int lastOffset = startOffset;
        for (int i = 0; i < size; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            HighlightItem item = get(i);
            sb.append(item.toString(lastOffset));
            sb.append('\n');
            lastOffset = item.getEndOffset();
        }
        return sb.toString();
    }

}
