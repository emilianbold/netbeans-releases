package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 *
 * $Id$
 */

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
// SAS: added this class to handle Binary input w/ FileInputStream

import java.io.IOException;
import java.io.InputStream;

public class ByteBuffer extends InputBuffer {

    // char source
    public transient InputStream input;


    /** Create a character buffer */
    public ByteBuffer(InputStream input_) {
        super();
        input = input_;
    }

    public void fill() throws CharStreamException {
        try {
                data = new char[INITIAL_BUFFER_SIZE];
                int pos = 0;
                char curChar;
                do {
                    if (pos == data.length) {
                        resizeData(0);
                    }
                    curChar = (char) input.read();
                    data[pos] = curChar;
                    pos++;
                } while (curChar != CharScanner.EOF_CHAR);
                p = 0;
	} catch (IOException io) {
            throw new CharStreamIOException(io);
        }
    }
}
