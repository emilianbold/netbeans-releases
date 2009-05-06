package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

// SAS: Added this class to genericise the input buffers for scanners
//      This allows a scanner to use a binary (FileInputStream) or
//      text (FileReader) stream of data; the generated scanner
//      subclass will define the input stream
//      There are two subclasses to this: CharBuffer and ByteBuffer

import java.io.*;

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
public class InputBuffer {
    // Number of active markers
    private int nMarkers = 0;

    // Additional offset used when markers are active
    private int markerOffset = 0;

    private int size = 0;
    
    // current position in the buffer
    private int p = 0;
    
    // buffer data
    public static final int INITIAL_BUFFER_SIZE = 2048;
    public static final int READ_BUFFER_SIZE = INITIAL_BUFFER_SIZE;
    private char[] data = new char[INITIAL_BUFFER_SIZE];

    public InputBuffer(char[] data) {
        this.data = data;
        this.size = data.length;
    }

    public InputBuffer(Reader input) { // SAS: for proper text i/o
        try{
            int numRead=0;
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
        p++;
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
        if ( (p+i-1) >= size ) {
            return CharScanner.EOF_CHAR;
        }
        
        return data[p+i-1];
    }

    /**Return an integer marker that can be used to rewind the buffer to
     * its current state.
     */
    public final int mark() {
        nMarkers++;
        return p;
    }

    /**Rewind the character buffer to a marker.
     * @param mark Marker returned previously from mark()
     */
    public final void rewind(int mark) {
        p = mark;
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
