package org.netbeans.modules.cnd.antlr;

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
 * token is not done by consume(), but deferred until needed by LA or LT.
 * <p>
 *
 * @see org.netbeans.modules.cnd.antlr.Token
 * @see org.netbeans.modules.cnd.antlr.TokenQueue
 */

public class TokenBuffer {

    // Token source
    private final TokenStream input;

    // Number of active markers
    private int nMarkers = 0;
    
    private int windowStart = 0;

    // Additional offset used when markers are active
    private int markerOffset = 0;

    // Number of calls to consume() since last LA() or LT() call
    private int numToConsume = 0;

    // Circular queue
    private final TokenQueue queue;

    /** Create a token buffer */
    public TokenBuffer(TokenStream input) {
        this.input = input;
        this.queue = new TokenQueue(1);
    }
    
    public TokenBuffer(TokenStream input, int initialCapacity) {
        this(input);
    }

    /** Mark another token for deferred consumption */
    public final void consume() {
        numToConsume++;
    }

    /** Ensure that the token buffer is sufficiently full */
    private void fill(int amount) {
        syncConsume();
        // Fill the buffer sufficiently to hold needed tokens
        try {
            while (queue.nbrEntries < amount + markerOffset) {
                // Append the next token
                queue.append(input.nextToken());
            }
        }
        catch (TokenStreamException tse) {
                System.err.println("tmp error: can't load tokens: "+tse); //NOI18N
        }
    }

    /** Get a lookahead token value */
    public final int LA(int i) {
        fill(i+1);
        return queue.elementAt(markerOffset + i - 1).getType();
    }

    /** Get a lookahead token */
    public final Token LT(int i) {
        fill(i+1);
        return queue.elementAt(markerOffset + i - 1);
    }

    /**Return an integer marker that can be used to rewind the buffer to
     * its current state.
     */
    public final int mark() {
        syncConsume();
//System.out.println("Marking at " + markerOffset);
//try { for (int i = 1; i <= 2; i++) { System.out.println("LA("+i+")=="+LT(i).getText()); } } catch (ScannerException e) {}
        nMarkers++;
        return markerOffset+windowStart;
    }
    
    /** What token index are we at?  Assume mark() done at start.
     */
    public final int index() {
        syncConsume();
        return windowStart+markerOffset;
    }
    
    public final void seek(int position) {
        syncConsume();
        markerOffset = position-windowStart;
        assert markerOffset >= 0 : "Seek to unbuffered position"; //NOI18N
    }

    /**Rewind the token buffer to a marker.
     * @param mark Marker returned previously from mark()
     */
    public final void rewind(int mark) {
        syncConsume();
        markerOffset = mark-windowStart;
        assert markerOffset >= 0 : "Rewind to unbuffered position"; //NOI18N
        nMarkers--;
        assert nMarkers >= 0 : "Unbalanced mark-rewind"; //NOI18N
//        System.err.println("Rewinding to " + mark);
//        for (int i = 1; i <= 2; i++) { 
//            System.err.println("LA("+i+")="+LA(i));
//        }
    }

    /** Sync up deferred consumption */
    private void syncConsume() {
        while (numToConsume > 0) {
            if (nMarkers > 0) {
                // guess mode -- leave leading tokens and bump offset.
                markerOffset++;
            } else {
                // normal mode -- remove first token
                queue.removeFirst();
                windowStart++;
            }
            numToConsume--;
        }
    }
}
