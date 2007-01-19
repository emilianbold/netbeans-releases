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

import java.util.EventObject;

/**
 * An event object notifying about a change in highlighting of certain area
 * of a document. The area where the highlighting has changed is specified by
 * its starting and ending offsets. Whoever receives this event should consider
 * re-requesting the new list of highlighted areas from the 
 * <code>HighlightsContainer</code> that fired the event.
 *
 * @author Vita Stejskal
 */
public final class HighlightsChangeEvent extends EventObject {
    
    private int startOffset;
    private int endOffset;
    
    /** 
     * Creates a new instance of <code>HighlightsChangeEvent</code>. The
     * <code>startOffset</code> and <code>endOffset</code> parameters specify
     * the area of a document where highlighting has changed. It's possible to
     * use <code>Integer.MAX_VALUE</code> for the <code>endOffset</code> parameter
     * meaning that the end of the change is unknown or the change spans up to
     * the end of a document.
     *
     * @param source         The highlight layer that fired this event.
     * @param startOffset    The beginning of the area that has changed.
     * @param endOffset      The end of the changed area.
     */
    public HighlightsChangeEvent(HighlightsContainer source, int startOffset, int endOffset) {
        super(source);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Gets the beginning of an area in the document where highlighting has
     * changed.
     *
     * @return The starting offset of the chaged area. Should always be greater than
     *         or equal to zero.
     */
    public int getStartOffset() {
        return startOffset;
    }
    
    /**
     * Gets the end of an area in the document where highlighting has
     * changed.
     *
     * @return The ending offset of the chaged area. May return <code>Integer.MAX_VALUE</code>
     *         if the ending position is unknown.
     */
    public int getEndOffset() {
        return endOffset;
    }
}
