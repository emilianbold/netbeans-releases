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
package org.netbeans.performance.bde;

/** Describes in integer interval */
public final class Interval {
    
    private int start;
    private int end;
    private int step;

    /** Creates new Interval */
    public Interval(int start, int end) {
        this(start, end, 1);
    }

    /** Creates new Interval */
    public Interval(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }
    
    /** @return start */
    public int getStart() {
        return start;
    }
    
    /** @return end */
    public int getEnd() {
        return end;
    }
    
    /** @return step */
    public int getStep() {
        return step;
    }
}
