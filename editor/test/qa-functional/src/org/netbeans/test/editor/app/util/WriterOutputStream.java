/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.util;

import java.io.*;

/**
 *
 * @author  jlahoda
 * @version 
 */
public class WriterOutputStream extends java.io.OutputStream {

    /** Creates new WriterOutputStream */
    private Writer writer;
    
    public WriterOutputStream(Writer awriter) {
        writer = awriter;
    }
    
    public void write(int b) throws java.io.IOException {
        writer.write(b);
    }
    
    public void flush() throws java.io.IOException {
        writer.flush();
    }
    
    public void close() throws java.io.IOException {
        writer.flush();
    }

}
