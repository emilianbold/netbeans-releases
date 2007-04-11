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
 * "WordDelineator.java"
 * WordDelineator.java 1.6 01/07/26
 */

package org.netbeans.lib.terminalemulator;

/*
 * Class used by Term to find the boundaries of a <i>word</i>, the region
 * of text that gets selected when you double-click.
 *<p>
 * Term has a default WordDelineator which can be changed by using this class
 * as an adapter and overriding either charClass() or findLeft() and
 * findRight() and assigning an object of the resulting class via
 * Term.setWordDelineator().
 */

public class WordDelineator {
    /**
     * Return the <i>character equivalence class</i> of 'c'.
     *<p>
     * This is used by findLeft() and findRight() which operate such that
     * a <i>word</i> is bounded by a change in character class.
     *<p>
     * A character equivalence class is characterised by a number, any number,
     * that is different from numbers for other character classes. For example,
     * this implementation, which is used as the default WordDelineator for
     * Term returns 1 for spaces and 0 for everything else.
     */
    protected int charClass(char c) {
	if (Character.isWhitespace(c))
	    return 1;
	else
	    return 0;
    }

    /**
     * Return index of char at the beginning of the word.
     */
    protected int findLeft(StringBuffer buf, int start) {
	int cclass = charClass(buf.charAt(start));

	// go left until a character of differing class is found
	int lx = start;
	while (lx > 0 && charClass(buf.charAt(lx-1)) == cclass) {
	    lx--;
	}
	return lx;
    } 

    /**
     * Return index of char past the word.
     */
    protected int findRight(StringBuffer buf, int start) {
	int cclass = charClass(buf.charAt(start));

	// go right until a character of a differing class is found.
	int rx = start;
	while (rx < buf.length() && charClass(buf.charAt(rx)) == cclass) {
	    rx++;
	}
	rx--;
	return rx;
    } 
}

