
package org.netbeans.modules.lexer.demo.handcoded.plain;

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLanguage;

public class PlainLanguage extends AbstractLanguage {

    /** Lazily initialized singleton instance of this language. */
    private static PlainLanguage INSTANCE;

    /** @return singleton instance of this language. */
    public static synchronized PlainLanguage get() {
        if (INSTANCE == null)
            INSTANCE = new PlainLanguage();

        return INSTANCE;
    }

    public static final int TEXT_INT = 1;


    public static final TokenId TEXT = new TokenId("text", TEXT_INT); // A line of text

    PlainLanguage() {
    }

    public Lexer createLexer() {
        return new PlainLexer();
    }

}
