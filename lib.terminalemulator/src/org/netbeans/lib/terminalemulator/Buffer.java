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
 * "Buffer.java"
 * Buffer.java 1.8 01/07/30
 */

package org.netbeans.lib.terminalemulator;

import java.util.Vector;

/**
 * The Buffer used by Term is _not_ related to javax.swing.text.Document.
 * <p>
 * The Swing Document is Element based while terms is Line based.
 * <br>
 * The Swing Document uses offsets for coordinates, while term uses cartesian
 * BCoords.
 * <p>
 */


class Buffer {

    /*
     * For some odd reason Vector.removeRange is protected, so 
     * we have to do this to gain access to it.
     */
    static class OurVector extends Vector {
	public void removeRange(int fromIndex, int toIndex) {
	    super.removeRange(fromIndex, toIndex);
	}
    }

    private OurVector lines = new OurVector();	// buffer

    public int nlines;		// number of lines in buffer
				// How is this different from lines.length?

    private int visible_cols;	// number of columns visible in view
    private int extra_cols;	// columns needed to support lines longer
				// than visible_cols. Only grows.

    public int visibleCols() {
	return visible_cols;
    } 

    public int totalCols() {
	return visible_cols + extra_cols;
    } 

    public Buffer(int visible_cols) {
	this.visible_cols = visible_cols;
    } 

    public void setVisibleCols(int visible_cols) {
	int delta = visible_cols - this.visible_cols;
	this.visible_cols = visible_cols;
	extra_cols -= delta;
	if (extra_cols < 0)
	    extra_cols = 0;
    }

    /*
     * Keep track of the largest column # to help set the extent of 
     * the horizontal scrollbar.
     */
    public void noteColumn(int col) {
	int new_extra = col - visible_cols;
	if (new_extra > extra_cols) {
	    extra_cols = new_extra;
	    // LATER hrange_listener.adjustHRange(extra_cols);
	}
    }

    /* DEBUG
    public static volatile boolean lock = false;
    
    private void ck_lock() {
	if (lock) {
	    System.out.println("Buffer ck_lock fail");	// NOI18N
	    printStats();
	    Thread.dumpStack();
	}
    }
    */

    Line lineAt(int brow) {
	try {
	    return (Line) lines.elementAt(brow);
	} catch(ArrayIndexOutOfBoundsException x) {
            //XXX swallowing this exception caused issue 40129.
            //I've put in a null-check on the return value in sel.paint()
            //as a hotfix.  Should find out why bad values are being passed
            //here.  Ivan?
            
	    /* DEBUG
	    System.out.println("Buffer.lineAt(" +brow+ ") -> null\n");// NOI18N
	    Thread.dumpStack();
	    */
	    return null;
	} 
    } 

    Line bottom() {
	return lineAt(nlines);
    } 

    public Line appendLine() {
	// DEBUG ck_lock();
	Line l = new Line();
	lines.add(l);
	nlines++;
	return l;
    }

    public Line addLineAt(int row) {
	// DEBUG ck_lock();
	Line l = new Line();
	lines.add(row, l);
	nlines++;
	return l;
    }

    /**
     * Remove 'n' lines starting at 'row'.
     * Return the number of characters deleted as a result.
     */
    public int removeLinesAt(int row, int n) {
	// DEBUG ck_lock();
	int nchars = 0;
	for (int r = row; r < row+n; r++)
	    nchars += lineAt(r).length() + 1;
	    
	lines.removeRange(row, row+n);
	nlines -= n;

	return nchars;
    }

    public void removeLineAt(int row) {
	// DEBUG ck_lock();
	lines.remove(row);
	nlines--;
    }

    public Line moveLineFromTo(int from, int to) {
	// DEBUG ck_lock();
	Line l = (Line) lines.remove(from);
	lines.add(to, l);
	return l;
    }

    /**
     * Visit the physical lines from 'begin', through 'end'.
     * <p>
     * If 'newlines' is set, the passed 'ecol' is set to the actual
     * number of columns in the view to signify that the newline is included.
     * This way of doing it helps with rendering of a whole-line selection.
     * Also Line knows about this and will tack on a "\n" when Line.text()
     * is asked for.
     */
    void visitLines(BCoord begin, BCoord end, boolean newlines,
		    LineVisitor visitor) {

	// In the general case a range is made up of three 
	// rectangles. The partial line at top, the partial line
	// at the bottom and the middle range of fully selected lines.

	Line l;
	if (begin.row == end.row) {
	    // range is on one line
	    l = lineAt(begin.row);
	    visitor.visit(l, begin.row, begin.col, end.col);

	} else {
	    boolean cont = false;

	    // range spans multiple lines
	    l = lineAt(begin.row);
	    if (newlines && !l.isWrapped())
		cont = visitor.visit(l, begin.row, begin.col, totalCols());
	    else
		cont = visitor.visit(l, begin.row, begin.col, l.length()-1);
	    if (!cont)
		return;

	    for (int r = begin.row+1; r < end.row; r++) {
		l = lineAt(r);
		if (newlines && !l.isWrapped())
		    cont = visitor.visit(l, r, 0, totalCols());
		else
		    cont = visitor.visit(l, r, 0, l.length()-1);
		if (!cont)
		    return;
	    } 

	    l = lineAt(end.row);
	    cont = visitor.visit(l, end.row, 0, end.col);
	    if (!cont)
		return;
	}
    }

    /*
     * Like visitLines() except in reverse.
     * <p>
     * Starts at 'end' and goes to 'begin'.
     */
    void reverseVisitLines(BCoord begin, BCoord end, boolean newlines,
		    LineVisitor visitor) {

	// very similar to visitLines

	Line l;
	if (begin.row == end.row) {
	    // range is on one line
	    l = lineAt(begin.row);
	    visitor.visit(l, begin.row, begin.col, end.col);

	} else {
	    boolean cont = false;

	    // range spans multiple lines
	    l = lineAt(end.row);
	    cont = visitor.visit(l, end.row, 0, end.col);
	    if (!cont)
		return;

	    for (int r = end.row-1; r > begin.row; r--) {
		l = lineAt(r);
		if (newlines && !l.isWrapped())
		    cont = visitor.visit(l, r, 0, totalCols());
		else
		    cont = visitor.visit(l, r, 0, l.length()-1);
		if (!cont)
		    return;
	    } 

	    l = lineAt(begin.row);
	    if (newlines && !l.isWrapped())
		cont = visitor.visit(l, begin.row, begin.col, totalCols());
	    else
		cont = visitor.visit(l, begin.row, begin.col, l.length()-1);
	    if (!cont)
		return;
	}
    }

    public BExtent find_word(WordDelineator word_delineator, BCoord coord) {
	/*
	 * Find the boundaries of a "word" at 'coord'.
	 */

	Line l = lineAt(coord.row);

	if (coord.col >= l.length())
	    return new BExtent(coord, coord);

	int lx = word_delineator.findLeft(l.stringBuffer(), coord.col);
	int rx = word_delineator.findRight(l.stringBuffer(), coord.col);

	return new BExtent(new BCoord(coord.row, lx),
			   new BCoord(coord.row, rx));
    } 

    /**
     * Back up the coordinate by one character and return new BCoord
     * <p>
     * Travels back over line boundaries
     * <br>
     * Returns null if 'c' is the first character of the buffer.
     */

    public BCoord backup(BCoord c) {
	if (c.col > 0)
	    return new BCoord(c.row, c.col-1);	// back one in line

	// Cursor is at beginning of line.
	// Need to find the end of previous line, but it might empty,
	// so we go one line back etc
	for (int prevrow = c.row-1; prevrow >= 0; prevrow--) {
	    Line l = lineAt(prevrow);
	    if (l.length() != 0)
		return new BCoord(prevrow, l.length()-1);
	}

	// prevrow == -1, at beginning of file; nowhere to back to
	return null;
    }

    /*
     * Advance the coordinate by one character and return a new coord.
     * <p>
     * Wraps around line boundaries.
     * <br>
     * Returns null if 'c' is at the last character of the buffer.
     */
    public BCoord advance(BCoord c) {
	int row = c.row;
	int col = c.col;

	col++;
	Line l = lineAt(row);
	if (col < l.length())
	    return new BCoord(row, col);

	// Need to wrap, but the next line might be empty ... so we 
	// keep going til either we find a non-empty line or the end
	// of the buffer.
	while (++row < nlines) {
	    l = lineAt(row);
	    if (l.length() != 0)
		return new BCoord(row, 0);
	}
	return null;
    }

    /*
     * Print interesting statistics and facts about this Term
     */
    public void printStats() {
	int nchars = 0;
	int ncapacity = 0;
	for (int lx = 0; lx < nlines; lx++) {
	    Line l = lineAt(lx);
	    ncapacity += l.capacity();
	    nchars += l.length();
	}
	System.out.println(
	    "  nlines " + nlines +	// NOI18N
	    "  nchars " + nchars + 	// NOI18N
	    "  ncapacity " + ncapacity );	// NOI18N
    }
}
