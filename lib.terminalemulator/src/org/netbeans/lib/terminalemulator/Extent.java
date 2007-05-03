/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "Extent.java"
 * Extent.java 1.5 01/07/26
 */

package org.netbeans.lib.terminalemulator;

public class Extent {
    public Coord begin;
    public Coord end;

    public Extent(Coord begin, Coord end) {
	this.begin = (Coord) begin.clone();
	this.end = (Coord) end.clone();
    } 

    /**
     * Override Object.toString
     */
    public String toString() {
	return "Extent[" + begin + " " + end + "]";	// NOI18N
    } 

    /**
     * Ensure that 'begin' is before 'end'.
     */
    public Extent order() {
	if (begin.compareTo(end) > 0) {
	    Coord tmp = begin;
	    begin = end;
	    end = tmp;
	}
	return this;
    }

    /*
     * Return true if selection intersects the given row/column
     */
    public boolean intersects(int arow, int col) {
	if (begin.row > arow)
	    return false;
	else if (end.row < arow)
	    return false;
	else if (begin.row == end.row)
	    return col >= begin.col && col <= end.col;
	else if (arow == begin.row)
	    return col >= begin.col;
	else if (arow == end.row)
	    return col <= end.col;
	else
	    return true;
    }
}
