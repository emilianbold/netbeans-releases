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
 * "TermStream.java"
 * TermStream.java 1.7 01/07/10
 */

package org.netbeans.lib.terminalemulator;

/**
 * TermStream is analogous to unix STREAMS. 
 * <br>
 * It is a full duplex processing and data transfer path between a raw
 * Term (The Data Terminal Equipment, DTE) and a client of the Term, usually
 * a process (The Data Communication Equipment, DCE).
 * <p>
 * TermStream's can be chained together. This is performed using
 * @see org.netbeans.lib.terminalemulator.Term#pushStream
 * <p>
 * Streams are usually used (in the context of terminals) to do echoing,
 * line buffering, CR/NL translation and so on. See
 * @see org.netbeans.lib.terminalemulator.LineDiscipline .
 * They can also be used for logging and debugging.
 */

public abstract class TermStream {
    protected TermStream toDTE;		// delegate putChar's to toDTE
    protected TermStream toDCE;		// delegate sendChar to from_keyboard

    void setToDCE(TermStream toDCE) {
	this.toDCE = toDCE;
    } 
    void setToDTE(TermStream toDTE) {
	this.toDTE = toDTE;
    }

    void setTerm(Term term) {
	this.term = term;
    } 
    protected Term getTerm() {
	return term;
    } 
    private Term term;


    // From world (DCE) to terminal (DTE) screen
    public abstract void flush();
    public abstract void putChar(char c);
    public abstract void putChars(char buf[], int offset, int count);

    // From terminal *keyboard) (DTE) to world (DCE).
    public abstract void sendChar(char c);
    public abstract void sendChars(char c[], int offset, int count);
}
