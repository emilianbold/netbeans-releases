package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.Stack;
import org.netbeans.modules.cnd.antlr.collections.impl.LList;

/** A token stream MUX (multiplexor) knows about n token streams
 *  and can multiplex them onto the same channel for use by token
 *  stream consumer like a parser.  This is a way to have multiple
 *  lexers break up the same input stream for a single parser.
 *	Or, you can have multiple instances of the same lexer handle
 *  multiple input streams; this works great for includes.
 */
public class TokenStreamSelector implements TokenStream {
    /** The currently-selected token stream input */
    protected TokenStream input = null;

    /** Used to track stack of input streams */
    protected Stack streamStack = new LList();

    public TokenStreamSelector() {
    }

    /** Return the stream from tokens are being pulled at
     *  the moment.
     */
    /*public TokenStream getCurrentStream() {
        return input;
    }*/

    public final Token nextToken() throws TokenStreamException {
        return input.nextToken();
    }

    public final TokenStream pop() {
        TokenStream stream = (TokenStream)streamStack.pop();
        select(stream);
        return stream;
    }

    public final void push(TokenStream stream) {
        streamStack.push(input); // save current stream
        select(stream);
    }
    
    public final boolean isEmpty() {
        return streamStack.height() == 0;
    }

    /** Set the stream without pushing old stream */
    public final void select(TokenStream stream) {
        input = stream;
    }
}
