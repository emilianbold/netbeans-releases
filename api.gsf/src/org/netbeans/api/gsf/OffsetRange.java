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
package org.netbeans.api.gsf;


/**
 * An offset range provides a range (start, end) pair of offsets
 * that indicate a range in a character buffer. The range represented
 * is {@code [start,end>}, which means that the range includes the
 * character at index=start, and ends right before the character at end.
 * Put yet another way, the starting offset is inclusive, and the ending
 * offset is exclusive.
 *
 * @author Tor Norbye
 */
public class OffsetRange {
    public static final OffsetRange NONE = new OffsetRange(0, 0);
    private final int start;
    private final int end;

    /** Creates a new instance of OffsetRange */
    public OffsetRange(int start, int end) {
        assert start >= 0;
        assert end >= start;

        this.start = start;
        this.end = end;
    }

    /** Get the start offset of offset range */
    public int getStart() {
        return start;
    }

    /** Get the end offset of offset range */
    public int getEnd() {
        return end;
    }
    
    /** Get the length of the offset range */
    public int getLength() {
        return end-start;
    }

    public String toString() {
        if (this == NONE) {
            return "OffsetRange[NONE]";
        } else {
            return "OffsetRange[" + start + "," + end + ">"; // NOI18N
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        final OffsetRange test = (OffsetRange)o;

        if (this.start != test.start) {
            return false;
        }

        if (this.end != test.end) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int hash = 7;

        hash = (23 * hash) + this.start;
        hash = (23 * hash) + this.end;

        return hash;
    }

    /** Return true iff the given offset is within the bounds (or at the bounds) of the range */
    public boolean containsInclusive(int offset) {
        if (this == NONE) {
            return false;
        }

        return (offset >= start) && (offset <= end);
    }
}
