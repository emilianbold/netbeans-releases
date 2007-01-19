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

package org.netbeans.spi.editor.highlighting;

import java.util.NoSuchElementException;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;

/**
 * An iterator through highlights in a <code>HighlightsContainer</code>.
 *
 * <p><b>Implementation:</b> Any <code>HighlightsSequence</code> obtained from any of the classes in
 * the Highlighting API will behave as so called <i>fast-fail</i> iterator. It
 * means that it will throw <code>ConcurrentModificationException</code> from
 * its methods if the underlying data (highlights) have changed since when the instance
 * of the <code>HighlightsSequence</code> was obtained.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface HighlightsSequence {

    /**
     * An empty <code>HighlightsSequence</code>.
     */
    public static final HighlightsSequence EMPTY = new HighlightsSequence() {
        public boolean moveNext() {
            return false;
        }

        public int getStartOffset() {
            throw new NoSuchElementException();
        }

        public int getEndOffset() {
            throw new NoSuchElementException();
        }

        public AttributeSet getAttributes() {
            throw new NoSuchElementException();
        }
    }; // End of EMPTY HighlightsSequence
    
    /**
     * Moves the internal pointer to the next highlight in this sequence (if there is any).
     * If this method returns <code>true</code> highlight's boundaries and attributes
     * can be retrieved by calling the getter methods.
     *
     * @return <code>true</code> If there is a highlight available and it is safe
     *         to call the getters.
     * @throws ConcurrentModificationException If the highlights this sequence is
     *         iterating through have been changed since the creation of the sequence.
     */
    boolean moveNext();
    
    /**
     * Gets the start offset of a current highlight.
     *
     * @return The offset in a document where the current highlight starts.
     * @throws ConcurrentModificationException If the highlights this sequence is
     * iterating through have been changed since the creation of the sequence.
     */
    int getStartOffset();
    
    /**
     * Gets the end offset of a current highlight.
     *
     * @return The offset in a document where the current highlight ends.
     * @throws ConcurrentModificationException If the highlights this sequence is
     * iterating through have been changed since the creation of the sequence.
     */
    int getEndOffset();
    
    /**
     * Gets the set of attributes that define how to render a current highlight.
     * 
     * <p>Since the <code>AttributeSet</code> can contain any attributes implementors
     * must be aware of whether the attributes returned from this method affect
     * metrics or not and set the <code>isFixedSize</code> parameter appropriately
     * when createing <code>HighlightsLayer</code>s.
     *
     * @return The set of text rendering attributes. Must not return <code>null</code>.
     * @throws ConcurrentModificationException If the highlights this sequence is
     * iterating through have been changed since the creation of the sequence.
     * 
     * @see org.netbeans.spi.editor.highlighting.HighlightsLayer
     */
    AttributeSet getAttributes();
}
