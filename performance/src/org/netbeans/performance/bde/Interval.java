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
