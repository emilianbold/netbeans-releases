/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsps.parserapi;

import java.io.File;

/**
 * Mark represents a point in the JSP input. 
 *
 * @author Anil K. Vijendran
 */
public final class Mark {
    int line, col;	// position within current stream
    String fileName;            // name of the current file

    public Mark(String filename, int line, int col) {
	this.line = line;
	this.col = col;
	this.fileName = filename;
    }

    // -------------------- Locator interface --------------------

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return col;
    }

    public String toString() {
	return getFile()+"("+line+","+col+")";
    }

    public String getFile() {
        return this.fileName;
    }
    
    public String toShortString() {
        return "("+line+","+col+")";
    }

    public boolean equals(Object other) {
	if (other instanceof Mark) {
	    Mark m = (Mark) other;
	    return this.line == m.line 
		&& this.col == m.col
                && new File(fileName).equals(new File(m.fileName));
	} 
	return false;
    }
}

