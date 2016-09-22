/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

// SAS: Added this class to genericise the input buffers for scanners
//      This allows a scanner to use a binary (FileInputStream) or
//      text (FileReader) stream of data; the generated scanner
//      subclass will define the input stream
//      There are two subclasses to this: CharBuffer and ByteBuffer

/**A Stream of characters fed to the lexer from a InputStream that can
 * be rewound via mark()/rewind() methods.
 * <p>
 * A dynamic array is used to buffer up all the input characters.  Normally,
 * "k" characters are stored in the buffer.  More characters may be stored during
 * guess mode (testing syntactic predicate), or when LT(i>k) is referenced.
 * Consumption of characters is deferred.  In other words, reading the next
 * character is not done by consume(), but deferred until needed by LA or LT.
 * <p>
 *
 * @see org.netbeans.modules.cnd.antlr.CharQueue
 */
public class InputBuffer {
    // Number of active markers
    private int nMarkers = 0;

    private int size = 0;
    
    // current position in the buffer
    private int position = 0;
    
    // buffer data
    public static final int INITIAL_BUFFER_SIZE = 8192*Integer.getInteger("antlr.input.buffer", 1).intValue(); // NOI18N
    public static final int READ_BUFFER_SIZE = INITIAL_BUFFER_SIZE;
    private char[] data;

    public InputBuffer(char[] data) {
        this.data = data;
        this.size = data.length;
    }

    public InputBuffer(Reader input) { // SAS: for proper text i/o
        data = new char[INITIAL_BUFFER_SIZE+READ_BUFFER_SIZE/2];
        try{
            int numRead = 0;
            int p = 0;
            do {
                if ( p + READ_BUFFER_SIZE > data.length ) { // overflow?
                    char[] newdata = new char[data.length * 2]; // resize
                    System.arraycopy(data, 0, newdata, 0, data.length);
                    data = newdata;
                }
                numRead = input.read(data, p, READ_BUFFER_SIZE);
                p += numRead;
            } while (numRead != -1);
            size = p + 1;
        } catch (IOException io) {
            System.err.println("tmp error: can't load input: " + io);
        }
    }
    
    public InputBuffer(InputStream input) {
        this(new InputStreamReader(input));
    }

    /** This method updates the state of the input buffer so that
     *  the text matched since the most recent mark() is no longer
     *  held by the buffer.  So, you either do a mark/rewind for
     *  failed predicate or mark/commit to keep on parsing without
     *  rewinding the input.
     */
    /*public final void commit() {
        nMarkers--;
    }*/

    /** Mark another character for deferred consumption */
    public final void consume() {
        position++;
    }

    /** Ensure that the input buffer is sufficiently full */
    //public abstract void fill(int amount) throws CharStreamException;
    //public abstract void fill() throws CharStreamException;

    // Not used
    /*public String getLAChars() {
        StringBuffer la = new StringBuffer();
        for (int i = markerOffset; i < queue.nbrEntries; i++)
            la.append(queue.elementAt(i));
        return la.toString();
    }

    public String getMarkedChars() {
        StringBuffer marked = new StringBuffer();
        for (int i = 0; i < markerOffset; i++)
            marked.append(queue.elementAt(i));
        return marked.toString();
    }*/
    
    /*public final boolean isMarked() {
        return (nMarkers != 0);
    }*/

    /** Get a lookahead character */
    public final char LA(int i) {
        if ( (position+i-1) >= size ) {
            return CharScanner.EOF_CHAR;
        }
        
        return data[position+i-1];
    }

    /**Return an integer marker that can be used to rewind the buffer to
     * its current state.
     */
    public final int mark() {
        nMarkers++;
        return position;
    }

    /**Rewind the character buffer to a marker.
     * @param mark Marker returned previously from mark()
     */
    public final void rewind(int mark) {
        position = mark;
        nMarkers--;
    }

    /** Reset the input buffer
     */
    /*public final void reset() {
        nMarkers = 0;
        markerOffset = 0;
        p=0;
        //data = null;
    }*/
}
