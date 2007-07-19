/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 * Lexical analyzer for Ruby quoted Strings
 *
 * @author Tor Norbye
 * @version 1.00
 */
public final class RubyStringLexer implements Lexer<RubyStringTokenId> {
    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<RubyStringTokenId> tokenFactory;
    private final boolean substituting;

    /**
     * A Lexer for ruby strings
     * @param substituting If true, handle substitution rules for double quoted strings, otherwise
     *    single quoted strings.
     */
    public RubyStringLexer(LexerRestartInfo<RubyStringTokenId> info, boolean substituting) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.substituting = substituting;
        assert (info.state() == null); // passed argument always null
    }

    public Object state() {
        return null;
    }

    public Token<RubyStringTokenId> nextToken() {
        return substituting ? nextTokenDoubleQuotes() : nextTokenSingleQuotes();
    }

    public Token<RubyStringTokenId> nextTokenSingleQuotes() {
        while (true) {
            int ch = input.read();

            switch (ch) {
            case EOF:

                if (input.readLength() > 0) {
                    return token(RubyStringTokenId.STRING_TEXT);
                } else {
                    return null;
                }

            case '\\':

                if (input.readLength() > 1) { // already read some text
                    input.backup(1);

                    return tokenFactory.createToken(RubyStringTokenId.STRING_TEXT,
                        input.readLength());
                }

                switch (ch = input.read()) {
                case '\\':
                case '\'':
                    return token(RubyStringTokenId.STRING_ESCAPE);

                case '0': // \0 etc. is a common construction when dealing with regexps.
                case '1': // It's not likely people will be confused about these
                case '2': // not getting escaped.
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    return token(RubyStringTokenId.STRING_TEXT);

                default:
                    return token(RubyStringTokenId.STRING_INVALID);
                }
            }
        }
    }

    public Token<RubyStringTokenId> nextTokenDoubleQuotes() {
        while (true) {
            int ch = input.read();

            switch (ch) {
            case EOF:

                if (input.readLength() > 0) {
                    return token(RubyStringTokenId.STRING_TEXT);
                } else {
                    return null;
                }

            // #{code} = Value of code
            case '#':

                int f = input.read();

                if (f == '{') {
                    //if (input.read() == '{') {
                    if (input.readLength() > 2) { // already read some text
                        input.backup(2);

                        return tokenFactory.createToken(RubyStringTokenId.STRING_TEXT,
                            input.readLength());
                    }

                    // Look for matching }...
                    // TODO: Figure out if I need to do anything else here,
                    // e.g. avoid escapes and such
                    int c;

                    while (true) {
                        c = input.read();

                        if ((c == EOF) || (c == '}')) {
                            break;
                        }
                    }

                    return token(RubyStringTokenId.EMBEDDED_RUBY);
                } else {
                    continue;
                }

            case '\\':

                if (input.readLength() > 1) { // already read some text
                    input.backup(1);

                    return tokenFactory.createToken(RubyStringTokenId.STRING_TEXT,
                        input.readLength());
                }

                switch (ch = input.read()) {
                // In general, \x = x
                // Thus, just special case out the exceptions

                // Hex escape: \xnn = Hex nn
                case 'x':

                // Octal escape: \nnn = Octal nnn
                case '0':
                case '1':
                case '2':
                case '3':

                    switch (input.read()) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':

                        switch (input.read()) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                            return token(RubyStringTokenId.STRING_ESCAPE); // valid octal escape
                        }

                        input.backup(1);

                        continue;
                    }

                    input.backup(1);

                    continue; // Just a \0 etc -> 0

                // \cx = Control-x
                case 'c': {
                    // If the next character is x, or -x, then it's a single sequence
                    int next = input.read();

                    if (next == 'x') {
                        return token(RubyStringTokenId.STRING_ESCAPE);
                    } else {
                        input.backup(1);
                    }

                    continue;
                }

                // \C-x = Control-x
                case 'C': {
                    int next = input.read();

                    if (next == '-') {
                        next = input.read();

                        if (next == 'x') {
                            return token(RubyStringTokenId.STRING_ESCAPE);
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }

                    continue;
                }

                // \M-x = Meta-x    
                case 'M': {
                    int next = input.read();

                    if (next == '-') {
                        next = input.read();

                        if (next == 'x') {
                            return token(RubyStringTokenId.STRING_ESCAPE);
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }

                    continue;
                }

                // TODO
                // Meta-control-x:  \M-\C-x
                //case 'M':
                //    return;
                default:

                    // There are lots of special escapes: \a, \b, \e, etc.
                    // but we don't need to actually substitute these, since
                    // lexically they have the same form as \x (which is = x),
                    // so treat these all the same:
                    return token(RubyStringTokenId.STRING_ESCAPE);
                }
            }
        }
    }

    private Token<RubyStringTokenId> token(RubyStringTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }
}
