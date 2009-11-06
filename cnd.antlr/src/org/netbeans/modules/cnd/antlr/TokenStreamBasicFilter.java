package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.ASdebug.ASDebugStream;
import org.netbeans.modules.cnd.antlr.ASdebug.IASDebugStream;
import org.netbeans.modules.cnd.antlr.ASdebug.TokenOffsetInfo;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

/** This object is a TokenStream that passes through all
 *  tokens except for those that you tell it to discard.
 *  There is no buffering of the tokens.
 */
public class TokenStreamBasicFilter implements TokenStream,IASDebugStream {
    /** The set of token types to discard */
    protected BitSet discardMask;

    /** The input stream */
    protected TokenStream input;

    public TokenStreamBasicFilter(TokenStream input) {
        this.input = input;
        discardMask = new BitSet();
    }

    public void discard(int ttype) {
        discardMask.add(ttype);
    }

    public void discard(BitSet mask) {
        discardMask = mask;
    }

    public Token nextToken() throws TokenStreamException {
        Token tok = input.nextToken();
        while (tok != null && discardMask.member(tok.getType())) {
            tok = input.nextToken();
        }
        return tok;
    }

    public String getEntireText()
    {
        return ASDebugStream.getEntireText(this.input);
    }

    public TokenOffsetInfo getOffsetInfo(Token token)
    {
        return ASDebugStream.getOffsetInfo(this.input, token);
    }
}
