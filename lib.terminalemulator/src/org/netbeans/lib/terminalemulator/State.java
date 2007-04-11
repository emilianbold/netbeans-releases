/*
 *	The contents of this file are subject to the terms of the Common Development
 *	and Distribution License (the License). You may not use this file except in
 *	compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 *	or http://www.netbeans.org/cddl.txt.
 *	
 *	When distributing Covered Code, include this CDDL Header Notice in each file
 *	and include the License file at http://www.netbeans.org/cddl.txt.
 *	If applicable, add the following below the CDDL Header, with the fields
 *	enclosed by brackets [] replaced by your own identifying information:
 *	"Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is Terminal Emulator.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc..
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "State.java"
 * State.java 1.11 01/07/26
 */

package org.netbeans.lib.terminalemulator;

class State {
    public int rows;

    // Index of Line visible on top of the canvas (0-origin)
    public int firstx;
    public int firsty;

    // Cursor is in "cell" coordinates
    public BCoord cursor = new BCoord();

    public void adjust(int amount) {
	firstx += amount;
	if (firstx < 0)
	    firstx = 0;

	cursor.row += amount;
	if (cursor.row < 0)
	    cursor.row = 0;
    } 

    // Current attribute as defined by class Attr
    public int attr;

    // If 'true' characters replace what's under cursor (default)
    // If 'false' act as an insert operation.
    public boolean overstrike = true;


    /*
     * Cursor saving and restoration.
     * Saved values are not adjusted!
     */
    public void saveCursor() {
	saved_cursor = (BCoord) cursor.clone();
    }
    public void restoreCursor() {
	if (saved_cursor != null) {
	    cursor = saved_cursor;
	    saved_cursor = null;
	}
    }
    private BCoord saved_cursor = null;
} 
