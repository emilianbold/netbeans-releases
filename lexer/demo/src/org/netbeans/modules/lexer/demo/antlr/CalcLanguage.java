
package org.netbeans.modules.lexer.demo.antlr;

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLanguage;
import org.netbeans.spi.lexer.MatcherFactory;
import org.netbeans.spi.lexer.util.IntegerCache;

public class CalcLanguage extends AbstractLanguage {

    /** Maximum constant corresponding to token identification or state. */
    static final int MAX_CONSTANT = 16;

    /** Array that allows conversion of <CODE>int</CODE> state to corresponding {@link java.lang.Integer} instance. */
    static final Integer[] CONSTANT_TO_INTEGER = IntegerCache.getTable(MAX_CONSTANT);

    /** Mime-type of this language. */
    public static final String MIME_TYPE = "text/x-java";

    /** Lazily initialized singleton instance of this language. */
    private static CalcLanguage INSTANCE;

    /** @return singleton instance of this language. */
    public static synchronized CalcLanguage get() {
        if (INSTANCE == null)
            INSTANCE = new CalcLanguage();

        return INSTANCE;
    }

    public static final int WHITESPACE_INT = 4;
    public static final int PLUS_INT = 5;
    public static final int MINUS_INT = 6;
    public static final int MUL_INT = 7;
    public static final int DIV_INT = 9;
    public static final int LPAREN_INT = 10;
    public static final int RPAREN_INT = 11;
    public static final int MUL3_INT = 8;
    public static final int CONSTANT_INT = 12;
    public static final int ERROR_INT = 16;


    public static final TokenId WHITESPACE = new TokenId("whitespace", WHITESPACE_INT, null, MatcherFactory.createTextCheckMatcher(" "));
    public static final TokenId PLUS = new TokenId("plus", PLUS_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("+"));
    public static final TokenId MINUS = new TokenId("minus", MINUS_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("-"));
    public static final TokenId MUL = new TokenId("mul", MUL_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("*"));
    public static final TokenId DIV = new TokenId("div", DIV_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("/"));
    public static final TokenId LPAREN = new TokenId("lparen", LPAREN_INT, new String[]{"separator"}, MatcherFactory.createTextCheckMatcher("("));
    public static final TokenId RPAREN = new TokenId("rparen", RPAREN_INT, new String[]{"separator"}, MatcherFactory.createTextCheckMatcher(")"));
    public static final TokenId MUL3 = new TokenId("mul3", MUL3_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("***"));
    public static final TokenId CONSTANT = new TokenId("constant", CONSTANT_INT, new String[]{"literal"});
    public static final TokenId ERROR = new TokenId("error", ERROR_INT);

    CalcLanguage() {
        super(MIME_TYPE);
    }

    public Lexer createLexer() {
        return new org.netbeans.modules.lexer.demo.antlr.CalcLexer(this);
    }

}

