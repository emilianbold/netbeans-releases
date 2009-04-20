package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
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

import java.io.Reader;

// SAS: Move most functionality into InputBuffer -- just the file-specific
//      stuff is in here

public final class CharBuffer extends InputBuffer {
    public CharBuffer(char[] data) {
        super(data);
    }
    /** Create a character buffer */
    public CharBuffer(Reader input) { // SAS: for proper text i/o
        super(input);
    }
}
