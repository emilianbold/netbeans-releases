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
 * "NullTermStream.java"
 * NullTermStream.java 1.6 01/07/10
 */

package org.netbeans.lib.terminalemulator;

/**
 * Null stream module for debugging/testing purposes.
 */

public class NullTermStream extends TermStream {
    public void flush() {
	toDTE.flush();
    }
    public void putChar(char c) {
	toDTE.putChar(c);
    }
    public void putChars(char buf[], int offset, int count) {
	toDTE.putChars(buf, offset, count);
    }
    public void sendChar(char c) {
	toDCE.sendChar(c);
    }
    public void sendChars(char buf[], int offset, int count) {
	toDCE.sendChars(buf, offset, count);
    }
};
