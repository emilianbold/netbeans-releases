
package org.netbeans.modules.lexer.demo.javacc;

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.lexer.lang.MatcherFactory;
import org.netbeans.modules.lexer.util.IntegerCache;
import org.netbeans.spi.lexer.AbstractLanguage;

public class CalcLanguage extends AbstractLanguage {

    public static final String MIME_TYPE = "text/x-calc";
    static final int CONSTANTS_MAX_INT = 19;
    static final Integer[] STATE_TABLE = IntegerCache.getTable(CONSTANTS_MAX_INT);

    public static final int WHITESPACE_INT = 1;
    public static final int ML_COMMENT_INT = 3;
    public static final int PLUS_INT = 6;
    public static final int MINUS_INT = 7;
    public static final int MUL_INT = 8;
    public static final int DIV_INT = 9;
    public static final int LPAREN_INT = 12;
    public static final int RPAREN_INT = 13;
    public static final int MUL3_INT = 10;
    public static final int PLUS5_INT = 11;
    public static final int CONSTANT_INT = 14;
    public static final int ML_COMMENT_END_INT = 18;
    public static final int ERROR_INT = 19;
    public static final int INCOMPLETE_ML_COMMENT_INT = 4;


    public static final TokenId WHITESPACE = new TokenId("whitespace", WHITESPACE_INT);
    public static final TokenId ML_COMMENT = new TokenId("ml-comment", ML_COMMENT_INT, new String[]{"comment"});
    public static final TokenId PLUS = new TokenId("plus", PLUS_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("+"));
    public static final TokenId MINUS = new TokenId("minus", MINUS_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("-"));
    public static final TokenId MUL = new TokenId("mul", MUL_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("*"));
    public static final TokenId DIV = new TokenId("div", DIV_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("/"));
    public static final TokenId LPAREN = new TokenId("lparen", LPAREN_INT, new String[]{"separator"}, MatcherFactory.createTextCheckMatcher("("));
    public static final TokenId RPAREN = new TokenId("rparen", RPAREN_INT, new String[]{"separator"}, MatcherFactory.createTextCheckMatcher(")"));
    public static final TokenId MUL3 = new TokenId("mul3", MUL3_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("***")); // Special token for testing extra lookahead and lookback
    public static final TokenId PLUS5 = new TokenId("plus5", PLUS5_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("+++++")); // Special token for testing extra lookahead and lookback
    public static final TokenId CONSTANT = new TokenId("constant", CONSTANT_INT, new String[]{"literal"});
    public static final TokenId ML_COMMENT_END = new TokenId("ml-comment-end", ML_COMMENT_END_INT, new String[]{"error"}, MatcherFactory.createTextCheckMatcher("*/"));
    public static final TokenId ERROR = new TokenId("error", ERROR_INT, new String[]{"error"});
    public static final TokenId INCOMPLETE_ML_COMMENT = new TokenId("incomplete-ml-comment", INCOMPLETE_ML_COMMENT_INT, new String[]{"comment", "incomplete"});

    CalcLanguage() {
        super(MIME_TYPE);
    }

    public Lexer createLexer() {
        return new org.netbeans.modules.lexer.demo.javacc.CalcLexer(this);
    }

}

