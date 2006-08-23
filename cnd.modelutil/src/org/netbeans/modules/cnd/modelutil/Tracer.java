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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelutil;

import java.io.PrintStream;

/**
 * Utility class that prints with indentation.
 *
 * Each indent() call makes it adding 4 spaces in the beginning of each string
 * unindent() decreases the amount of leading spaces by 4
 *
 * @author Vladimir Kvashin
 */
public class Tracer {
    
    private int step = 4;
    private PrintStream pstream;
    private StringBuffer indentBuffer = new StringBuffer();
    
    public Tracer() {
	this(System.err);
    }
    
    public Tracer(PrintStream pstream) {
	this.pstream = pstream;
    }
    
    public Tracer(PrintStream pstream, int step) {
	this.pstream = pstream;
	this.step = step;
    }
    
    public void indent() {
	setupIndentBuffer(indentBuffer.length() + step);
    }
    
    public void unindent() {
	setupIndentBuffer(indentBuffer.length() - step);
    }
    
    private void setupIndentBuffer(int len) {
	if( len <= 0 ) {
	    indentBuffer.setLength(0);
	} else {
	    indentBuffer.setLength(len);
	    for( int i = 0; i < len; i++ ) {
		indentBuffer.setCharAt(i,  ' ');
	    }
	}
    }
    
    public void trace(Object arg) {
	System.err.println(indentBuffer.toString() + arg);
    }
    
}
