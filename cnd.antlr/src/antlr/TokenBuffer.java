package antlr;

import java.util.List;
import java.util.ArrayList;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

/**A Stream of Token objects fed to the parser from a Tokenizer that can
 * be rewound via mark()/rewind() methods.
 * <p>
 * A dynamic array is used to buffer up all the input tokens.  Normally,
 * "k" tokens are stored in the buffer.  More tokens may be stored during
 * guess mode (testing syntactic predicate), or when LT(i>k) is referenced.
 * Consumption of tokens is deferred.  In other words, reading the next
 * token is not done by conume(), but deferred until needed by LA or LT.
 * <p>
 *
 * @see antlr.Token
 * @see antlr.TokenQueue
 */

public class TokenBuffer {

    // Number of active markers
    private int nMarkers = 0;

	/** The index into the tokens list of the current token (next token
     *  to consume).
     */
    private int p = 0;

    public static final int INITIAL_BUFFER_SIZE = 2048;
    
    /** Record every single token pulled from the source so we can reproduce
     *  chunks of it later.
     */
    private final List tokens = new ArrayList(INITIAL_BUFFER_SIZE);

    // type buffer data (created to improve performance of LA)
    private int size = 0;
    private int[] data = new int[INITIAL_BUFFER_SIZE];

    /** Create a token buffer */
    public TokenBuffer(TokenStream input) {
        // fill buffer
        try {
            int pos = 0;
            Token t = input.nextToken();
            while ( (t != null) && (t.getType() != Token.EOF_TYPE) ) {
                tokens.add(t);
                if (pos == data.length) resizeData();
                data[pos++] = t.getType();
                t = input.nextToken();
            }
            size = pos;
        }
        catch (TokenStreamException tse) {
                System.err.println("tmp error: can't load tokens: "+tse);
        }
    }

    /** Reset the input buffer to empty state */
    /*public final void reset() {
        nMarkers = 0;
        p = 0;
        size = 0;
        tokens.clear();
        data = null;
    }*/
    
    // double data size
    private final void resizeData() {
        int[] newdata = new int[data.length*2]; // resize
        System.arraycopy(data, 0, newdata, 0, data.length);
        data = newdata;
    }

    /** Mark another token for deferred consumption */
    public final void consume() {
        p++;
    }

    /** return the Tokenizer (needed by ParseView) */
    /*public TokenStream getInput() {
        return input;
    }*/

    /** Get a lookahead token value */
    public final int LA(int i) {
        int dataPos = p + i - 1;
        if ( dataPos >= size ) {
                return TokenImpl.EOF_TYPE;
        }
        return data[dataPos];
    }

    /** Get a lookahead token */
    public final Token LT(int i) {
        if ( (p+i-1) >= tokens.size() ) {
                return TokenImpl.EOF_TOKEN;
        }
        return (Token)tokens.get(p + i - 1);
    }

    /** Get token at absolute position (indexed from 0) */
    /*public final Token get(int i) {
        return (Token)tokens.get(i);
    }*/

	/**Return an integer marker that can be used to rewind the buffer to
     * its current state.
     */
    public final int mark() {
        //System.out.println("Marking at " + p);
        //try { for (int i = 1; i <= 2; i++) { System.out.println("LA("+i+")=="+LT(i).getText()); } } catch (ScannerException e) {}
        nMarkers++;
        return p;
    }

    /** What token index are we at?  Assume mark() done at start.
     */
    public final int index() {
        return p;
    }

    public final void seek(int position) {
        p = position;
    }

    /**Rewind the token buffer to a marker.
     * @param marker Marker returned previously from mark()
     */
    public final void rewind(int marker) {
        seek(marker);
        nMarkers--;
        //System.out.println("Rewinding to " + marker);
        //try { for (int i = 1; i <= 2; i++) { System.out.println("LA("+i+")=="+LT(i).getText()); } } catch (ScannerException e) {}
    }

}
