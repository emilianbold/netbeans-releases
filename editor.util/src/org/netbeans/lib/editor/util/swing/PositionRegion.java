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

    public static boolean isRegionsSorted(List/*<PositionRegion>*/ positionRegionList) {
        for (int i = positionRegionList.size() - 2; i >= 0; i--) {
            if (getComparator().compare(positionRegionList.get(i),
                    positionRegionList.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private final Position startPosition;
    
    private final Position endPosition;
    
    public PositionRegion(Position startPosition, Position endPosition) {
        assert (startPosition.getOffset() <= endPosition.getOffset())
            : "startPosition=" + startPosition.getOffset() + " > endPosition=" // NOI18N
                + endPosition;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    public PositionRegion(Document doc, int startOffset, int endOffset) throws BadLocationException {
        this(doc.createPosition(startOffset), doc.createPosition(endOffset));
    }
    
    public final int getStartOffset() {
        return startPosition.getOffset();
    }
    
    public final Position getStartPosition() {
        return startPosition;
    }

    public final int getEndOffset() {
        return endPosition.getOffset();
    }
    
    public final Position getEndPosition() {
        return endPosition;
    }
    
}
