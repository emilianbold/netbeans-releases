package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 *
 * $Id$
 */

// SAS: Added this class to genericise the input buffers for scanners
//      This allows a scanner to use a binary (FileInputStream) or
//      text (FileReader) stream of data; the generated scanner
//      subclass will define the input stream
//      There are two subclasses to this: CharBuffer and ByteBuffer

import java.io.IOException;

/**A Stream of characters fed to the lexer from a InputStream that can
 * be rewound via mark()/rewind() methods.
 * <p>
 * A dynamic array is used to buffer up all the input characters.  Normally,
 * "k" characters are stored in the buffer.  More characters may be stored during
 * guess mode (testing syntactic predicate), or when LT(i>k) is referenced.
 * Consumption of characters is deferred.  In other words, reading the next
 * character is not done by conume(), but deferred until needed by LA or LT.
 * <p>
 *
 * @see antlr.CharQueue
 */
public abstract class InputBuffer {
    // Number of active markers
    protected int nMarkers = 0;

    // Additional offset used when markers are active
    protected int markerOffset = 0;

    // current position in the buffer
    protected int p = -1;
    
    // buffer data
    protected char[] data = null;
    public static final int INITIAL_BUFFER_SIZE = 2048;

    /** Create an input buffer */
    public InputBuffer() {
    }

    /** This method updates the state of the input buffer so that
     *  the text matched since the most recent mark() is no longer
     *  held by the buffer.  So, you either do a mark/rewind for
     *  failed predicate or mark/commit to keep on parsing without
     *  rewinding the input.
     */
    public void commit() {
        nMarkers--;
    }

    /** Mark another character for deferred consumption */
    public void consume() {
        p++;
    }

    /** Ensure that the input buffer is sufficiently full */
    //public abstract void fill(int amount) throws CharStreamException;
    public abstract void fill() throws CharStreamException;

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
    
    // if sizeIncrease == 0 then double size
    protected void resizeData(int sizeIncrease) {
        int newLen = (sizeIncrease == 0) ? data.length*2 : data.length + sizeIncrease;
        char[] newdata = new char[newLen]; // resize
        System.arraycopy(data, 0, newdata, 0, data.length);
        data = newdata;
    }

    public boolean isMarked() {
        return (nMarkers != 0);
    }

    /** Get a lookahead character */
    public char LA(int i) throws CharStreamException {
        // fill buffer at the first LA call
        if (p == -1) {
            fill();
        }
        
        // actually this should never happen
        if ( (p+i-1) >= data.length ) {
            return CharScanner.EOF_CHAR;
        }
        
        return data[p + i - 1];
    }

    /**Return an integer marker that can be used to rewind the buffer to
     * its current state.
     */
    public int mark() {
        nMarkers++;
        return p;
    }

    /**Rewind the character buffer to a marker.
     * @param mark Marker returned previously from mark()
     */
    public void rewind(int mark) {
        p = mark;
        nMarkers--;
    }

    /** Reset the input buffer
     */
    public void reset() {
        nMarkers = 0;
        markerOffset = 0;
        p=-1;
        data = null;
    }
}
