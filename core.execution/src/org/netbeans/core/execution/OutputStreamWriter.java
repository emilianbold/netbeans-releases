/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import java.io.Writer;
import java.io.PrintStream;
import java.io.IOException;

final class OutputStreamWriter extends Writer {

    private PrintStream out;

    /**
     * Create an OutputStreamWriter that uses the default character encoding.
     *
     * @param  out  An OutputStream
     */
    public OutputStreamWriter(PrintStream out) {
        super(out);
        if (out == null)
            throw new NullPointerException();
        this.out = out;
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException();
    }
    
    /**
     * Write a single character.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(int c) throws IOException {
        char cbuf[] = new char[1];
        cbuf[0] = (char) c;
        write(cbuf, 0, 1);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Buffer of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            if ((off == 0) && (len == cbuf.length)) {
                out.print(cbuf);
            } else {
                char[] chars = new char[len];
                System.arraycopy(cbuf, off, chars, 0, len);
                out.print(chars);
            }
        }
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  A String
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        char[] chars = new char[len];
        str.getChars(off, off + len, chars, 0);
        out.print(chars);
    }

    /**
     * Flush the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void flush() {
        synchronized (lock) {
            if (out == null)
                return;
            out.flush();
        }
    }

    /**
     * Close the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void close() {
        synchronized (lock) {
            if (out == null)
                return;
            flush();
            out.close();
            out = null;
        }
    }

}
