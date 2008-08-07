package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import antlr.collections.AST;
import antlr.collections.impl.BitSet;

public class NoViableAltException extends RecognitionException {
    public Token token = null;
    public AST node = null;	// handles parsing and treeparsing
    private BitSet expected = null;
    private String[] tokenNames;
    
    private static final boolean hideExpected = Boolean.getBoolean("antlr.exceptions.hideExpectedTokens");
    
    public NoViableAltException(AST t) {
        super("NoViableAlt", "<AST>", t.getLine(), t.getColumn());
        node = t;
    }

    public NoViableAltException(Token t, String fileName_) {
        super("NoViableAlt", fileName_, t.getLine(), t.getColumn());
        token = t;
    }
    
    public NoViableAltException(Token t, String fileName_, BitSet expected, String[] tokenNames) {
        this(t, fileName_);
        this.expected = expected;
        this.tokenNames = tokenNames;
    }

    public BitSet getExpected() {
        return expected;
    }
    
    /**
     * Returns a clean error message (no line number/column information)
     */
    @Override
    public String getMessage() {
        if (token != null) {
            String res = "unexpected token: " + token.getText();
            if (hideExpected) {
                return res;
            }
            if (tokenNames != null) {
                res += "(" + MismatchedTokenException.tokenName(tokenNames, token.getType()) + ")";
            }
            if (expected != null) {
                res += ", expected one of ";
                if (tokenNames != null) {
                    StringBuilder sb = new StringBuilder();
                    int[] elems = expected.toArray();
                        for (int i = 0; i < elems.length; i++) {
                            sb.append(", ");
                            sb.append(MismatchedTokenException.tokenName(tokenNames, elems[i]));
                    }
                    res += sb;
                } else {
                    res += expected;
                }
            }
            return res;
        }

        // must a tree parser error if token==null
        if (node == TreeParser.ASTNULL) {
            return "unexpected end of subtree";
        }
        return "unexpected AST node: " + node.toString();
    }

    @Override
    public String getTokenText() {
        return token.getText();
    }

}
