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
package org.netbeans.modules.java.source.save;

/**
 * Represents a difference, as used in <code>Diff</code>. A difference consists
 * of two pairs of starting and ending points, each pair representing either the
 * "from" or the "to" collection passed to <code>Diff</code>. If an ending point
 * is -1, then the difference was either a deletion or an addition. For example,
 * if <code>getDeletedEnd()</code> returns -1, then the difference represents an
 * addition.
 */
class Difference {
    public static final int NONE = -1;
    
    /**
     * The point at which the deletion starts.
     */
    private int delStart = NONE;
    
    /**
     * The point at which the deletion ends.
     */
    private int delEnd = NONE;
    
    /**
     * The point at which the addition starts.
     */
    private int addStart = NONE;
    
    /**
     * The point at which the addition ends.
     */
    private int addEnd = NONE;
    
    /**
     * Creates the difference for the given start and end points for the
     * deletion and addition.
     */
    public Difference(int delStart, int delEnd, int addStart, int addEnd) {
        this.delStart = delStart;
        this.delEnd   = delEnd;
        this.addStart = addStart;
        this.addEnd   = addEnd;
    }
    
    /**
     * The point at which the deletion starts, if any. A value equal to
     * <code>NONE</code> means this is an addition.
     */
    public int getDeletedStart() {
        return delStart;
    }
    
    /**
     * The point at which the deletion ends, if any. A value equal to
     * <code>NONE</code> means this is an addition.
     */
    public int getDeletedEnd() {
        return delEnd;
    }
    
    /**
     * The point at which the addition starts, if any. A value equal to
     * <code>NONE</code> means this must be an addition.
     */
    public int getAddedStart() {
        return addStart;
    }
    
    /**
     * The point at which the addition ends, if any. A value equal to
     * <code>NONE</code> means this must be an addition.
     */
    public int getAddedEnd() {
        return addEnd;
    }
    
    /**
     * Sets the point as deleted. The start and end points will be modified to
     * include the given line.
     */
    public void setDeleted(int line) {
        delStart = Math.min(line, delStart);
        delEnd   = Math.max(line, delEnd);
    }
    
    /**
     * Sets the point as added. The start and end points will be modified to
     * include the given line.
     */
    public void setAdded(int line) {
        addStart = Math.min(line, addStart);
        addEnd   = Math.max(line, addEnd);
    }
    
    /**
     * Compares this object to the other for equality. Both objects must be of
     * type Difference, with the same starting and ending points.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Difference) {
            Difference other = (Difference)obj;
            
            return (delStart == other.delStart &&
                    delEnd   == other.delEnd &&
                    addStart == other.addStart &&
                    addEnd   == other.addEnd);
        } else {
            return false;
        }
    }
    
    /**
     * Returns a string representation of this difference.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("del: [" + delStart + ", " + delEnd + "]");
        buf.append(" ");
        buf.append("add: [" + addStart + ", " + addEnd + "]");
        return buf.toString();
    }
    
}
