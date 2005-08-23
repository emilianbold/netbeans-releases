/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.util.swing;

import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * A pair of positions delimiting a text region in a swing document.
 * <br/>
 * At all times it should be satisfied that
 * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public class PositionRegion {
    
    /** Copmarator for position regions */
    private static Comparator comparator;
    
    /**
     * Get comparator for position regions comparing start offsets
     * of the two given regions.
     *
     * @return non-null comparator comparing the start offsets of the two given
     *  regions.
     */
    public static final Comparator getComparator() {
        if (comparator == null) {
            comparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((PositionRegion)o1).getStartOffset()
                        - ((PositionRegion)o2).getStartOffset();
                }
            };
        }
        return comparator;
    }

    /**
     * Check whether a list of position regions is sorted
     * according the start offsets of the regions.
     *
     * @param positionRegionList list of the regions to be compared.
     * @return true if the regions are sorted according to the starting offset
     *  of the given regions or false otherwise.
     */
    public static boolean isRegionsSorted(List/*<PositionRegion>*/ positionRegionList) {
        for (int i = positionRegionList.size() - 2; i >= 0; i--) {
            if (getComparator().compare(positionRegionList.get(i),
                    positionRegionList.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private Position startPosition;
    
    private Position endPosition;
    
    /**
     * Construct new position region.
     *
     * @param startPosition non-null start position of the region &lt;= end position.
     * @param endPosition non-null end position of the region &gt;= start position.
     */
    public PositionRegion(Position startPosition, Position endPosition) {
        assert (startPosition.getOffset() <= endPosition.getOffset())
            : "startPosition=" + startPosition.getOffset() + " > endPosition=" // NOI18N
                + endPosition;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    /**
     * Construct new position region based on the knowledge
     * of the document and starting and ending offset.
     */
    public PositionRegion(Document doc, int startOffset, int endOffset) throws BadLocationException {
        this(doc.createPosition(startOffset), doc.createPosition(endOffset));
    }
    
    /**
     * Get starting offset of this region.
     *
     * @return &gt;=0 starting offset of this region.
     */
    public final int getStartOffset() {
        return startPosition.getOffset();
    }
    
    /**
     * Get starting position of this region.
     *
     * @return non-null starting position of this region.
     */
    public final Position getStartPosition() {
        return startPosition;
    }
    
    /**
     * Get ending offset of this region.
     *
     * @return &gt;=0 ending offset of this region.
     */
    public final int getEndOffset() {
        return endPosition.getOffset();
    }
    
    /**
     * Get ending position of this region.
     *
     * @return non-null ending position of this region.
     */
    public final Position getEndPosition() {
        return endPosition;
    }
    
    /**
     * Get length of this region.
     *
     * @return &gt;=0 length of this region
     *  computed as <code>getEndOffset() - getStartOffset()</code>.
     */
    public final int getLength() {
        return getEndOffset() - getStartOffset();
    }

    /**
     * {@link MutablePositionRegion} uses this package private method
     * to set a new start position of this region.
     */
    void setStartPositionImpl(Position startPosition) {
        assert (startPosition.getOffset() <= endPosition.getOffset())
            : "startPosition=" + startPosition.getOffset() + " > endPosition=" // NOI18N
                + endPosition;
        this.startPosition = startPosition;
    }

    /**
     * {@link MutablePositionRegion} uses this package private method
     * to set a new start position of this region.
     */
    void setEndPositionImpl(Position endPosition) {
        assert (startPosition.getOffset() <= endPosition.getOffset())
            : "startPosition=" + startPosition.getOffset() + " > endPosition=" // NOI18N
                + endPosition;
        this.endPosition = endPosition;
    }
    
}
