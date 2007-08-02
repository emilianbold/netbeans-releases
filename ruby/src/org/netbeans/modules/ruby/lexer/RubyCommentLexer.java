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

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.ErrorManager;
import org.openide.util.NbPreferences;


/**
 * Lexical analyzer for Ruby comments which identifies TODO markers
 * and highlights them specially.
 *
 * @todo Handle rdoc on/off directives (#++,#--). Since these occur on separate
 *   lines I can't handle it now.
 * @todo Highlight only RDoc reserved words, or all that fit the pattern? For
 *   now I'm highlighting :\w+: sequences. Possibly I should only highlight
 * @todo ___ shows up as an italic "_" - that aint right
 * @todo Tokenize Ruby-style symbols (:foo) and use the ruby color preferences?
 *
 * @author Tor Norbye
 */
public final class RubyCommentLexer implements Lexer<RubyCommentTokenId> {
    private static final int EOF = LexerInput.EOF;
    private static final String[] RDOC_DIRECTIVES =
        {
            "arg", "args", "yield", "yields", "notnew", "not-new", "not_new", "doc", "nodoc",
            "stopdoc", "startdoc", "enddoc", "main", "title", "section", "include"
        };
    private final LexerInput input;
    private final TokenFactory<RubyCommentTokenId> tokenFactory;
    private boolean inWord;
    private String[] markers;

    public RubyCommentLexer(LexerRestartInfo<RubyCommentTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }

    public Object state() {
        return null;
    }

    /**
     * Compute the set of markers to scan for in the user source code.
     * The code tries to look for the same markers used by the TODO module
     * in case the user has customized the set. (However, it is doing this
     * by peeking at the Preferences possibly left by the docscan module,
     * rather than having a contract API with it, based on
     * tasklist/docscan/src/org/netbeans/modules/tasklist/docscan/Settings.java)
     */
    private String[] getTodoMarkers() {
        if (markers == null) {
            final String MARKER_PREFIX = "Tag"; // NOI18N
            final int MARKER_PREFIX_LENGTH = MARKER_PREFIX.length();
            List<String> markerList = new ArrayList<String>();

            try {
                Preferences preferences =
                    NbPreferences.root().node("org/netbeans/modules/tasklist/docscan"); // NOI18N
                String[] keys = preferences.keys();

                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i];

                    if ((key != null) && key.startsWith(MARKER_PREFIX)) {
                        markerList.add(key.substring(MARKER_PREFIX_LENGTH));
                    }
                }
            } catch (BackingStoreException bse) {
                ErrorManager.getDefault().notify(bse);
            }

            if (markerList.size() > 0) {
                markerList.remove("@todo"); // Applies to javadoc, and these tags are now colorized separately
                markers = markerList.toArray(new String[markerList.size()]);
            } else {
                // Additional candidates: HACK, WORKAROUND, REMOVE, OLD
                markers = new String[] { "TODO", "FIXME", "XXX", "PENDING" }; // NOI18N
            }
        }

        return markers;
    }

    public Preferences getDocscanPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/tasklist/docscan");
    }

    public Token<RubyCommentTokenId> nextToken() {
        inWord = false;

    inputLoop:
        while (true) {
            int ch = input.read();

            switch (ch) {
            case EOF: {
                if (input.readLength() > 0) {
                    return token(RubyCommentTokenId.COMMENT_TEXT);
                } else {
                    return null;
                }
            }

            case '\\':
                // The next character is escaped...
                input.read();

                continue;

            case '\n':
                return token(RubyCommentTokenId.COMMENT_TEXT);

            case '#': { // Linked method

                // See if this is a method reference. It can be either "#method" or "Class#method".
                // If the input is something like " #" we need to chop it off to start at "#"; if
                // it's something like "foo Bar#baz" we need to chop it off at "Bar#baz", and
                // if it's something impossible like " foo#bar" we can ignore it completely (the class
                // must be uppercase).
                CharSequence s = input.readText();
                int classIndex = s.length()-1;
                assert s.charAt(classIndex) == '#';
                for (classIndex--; classIndex >= 0; classIndex--) {
                    char c = s.charAt(classIndex);
                    if (!Character.isJavaIdentifierPart(c) && c != '_') {
                        // The next character needs to be "#" or an uppercase character
                        assert classIndex < s.length()-1;
                        char next = s.charAt(classIndex+1);
                        if (!(next == '#' || Character.isUpperCase(next))) {
                            // This "#" is not in an Upper# sequence
                            // just continue processing input
                            continue inputLoop;
                        }
                        break;
                    }
                }
                // Make sure uppercase
                if (classIndex == -1) {
                    // It's the beginning of input - we're okay
                    char next = s.charAt(0);
                    if (!(next == '#' || Character.isUpperCase(next))) {
                        break;
                    }
                } else {
                    input.backup(input.readLength()-(classIndex+1));
                    return token(RubyCommentTokenId.COMMENT_TEXT);
                }

                int originalLength = input.readLength();

                // See if we have what looks like a method name:
                // method-only characters followed by whitespace, newlines or EOF:
                while (ch != EOF) {
                    ch = input.read();

                    if ((ch == '$') || !Character.isJavaIdentifierPart(ch)) {
                        input.backup(1);

                        break;
                    }
                }

                if (Character.isWhitespace(ch) || (ch == EOF) || (ch == '.') || (ch == ',') ||
                        (ch == ')') || (ch == '}')) {
                    if (input.readLength() > 2 && input.readLength() > originalLength) {
                        return token(RubyCommentTokenId.COMMENT_LINK);
                    }
                }

                break;
            }

            case 'f': // ftp:
            case 'm': // mailto:
            case 'w': // www.
            case 'h': { // http links. TODO: link:, ftp:, mailto:, and www.

                if (inWord) {
                    break;
                }

                int originalLength = input.readLength();
                boolean foundLinkBegin = false;

                if (ch == 'h') { // http:

                    if (input.read() == 't') {
                        if (input.read() == 't') {
                            if (input.read() == 'p') {
                                if (input.read() == ':') {
                                    foundLinkBegin = true;
                                } else {
                                    input.backup(4);
                                }
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                } else if (ch == 'f') { // ftp:

                    if (input.read() == 't') {
                        if (input.read() == 'p') {
                            if (input.read() == ':') {
                                foundLinkBegin = true;
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                } else if (ch == 'm') { // mailto:

                    if (input.read() == 'a') {
                        if (input.read() == 'i') {
                            if (input.read() == 'l') {
                                if (input.read() == 't') {
                                    if (input.read() == 'o') {
                                        if (input.read() == ':') {
                                            foundLinkBegin = true;
                                        } else {
                                            input.backup(6);
                                        }
                                    } else {
                                        input.backup(5);
                                    }
                                } else {
                                    input.backup(4);
                                }
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                } else if (ch == 'w') { // www.

                    if (input.read() == 'w') {
                        if (input.read() == 'w') {
                            if (input.read() == '.') {
                                foundLinkBegin = true;
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                }

                if (foundLinkBegin) {
                    while (ch != EOF) {
                        ch = input.read();

                        if ((ch == ']') || (ch == ')') || Character.isWhitespace(ch) ||
                                (ch == '\'') || (ch == '"')) {
                            input.backup(1);

                            break;
                        }
                    }

                    if (originalLength > 1) {
                        input.backup(input.readLengthEOF() - originalLength + 1);

                        return token(RubyCommentTokenId.COMMENT_TEXT);
                    }

                    if (input.readLength() > 2) {
                        return token(RubyCommentTokenId.COMMENT_LINK);
                    }
                }
                break;
            }

            case '_': // Italic text

                if (inWord) {
                    break;
                }

                if (input.readLength() > 1) {
                    input.backup(1);

                    return token(RubyCommentTokenId.COMMENT_TEXT);
                }

                while (ch != EOF) {
                    ch = input.read();

                    if (ch == '_') {
                        int next = input.read();
                        input.backup(1);

                        if (Character.isLetter(next) || (next == '_')) {
                            continue;
                        }

                        if (input.readLength() > 2) {
                            return token(RubyCommentTokenId.COMMENT_ITALIC);
                        }
                    } else if (!(Character.isLetter(ch) || (ch == '_'))) {
                        input.backup(1);
                        break;
                    }
                }

                break;

            case '*': // Bold text

                if (inWord) {
                    break;
                }

                if (input.readLength() > 1) {
                    input.backup(1);

                    return token(RubyCommentTokenId.COMMENT_TEXT);
                }

                while (ch != EOF) {
                    ch = input.read();

                    if ((ch == '*') && (input.readLength() > 2)) {
                        return token(RubyCommentTokenId.COMMENT_BOLD);
                    } else if (!(Character.isLetter(ch) || (ch == '_'))) {
                        input.backup(1);
                        break;
                    }
                }

                break;

            case '+': // Typewriter text

                if (inWord) {
                    break;
                }

                if (input.readLength() > 1) {
                    input.backup(1);

                    return token(RubyCommentTokenId.COMMENT_TEXT);
                }

                while (ch != EOF) {
                    ch = input.read();

                    if ((ch == '+') && (input.readLength() > 2)) {
                        return token(RubyCommentTokenId.COMMENT_HTMLTAG);
                    } else if (!(Character.isLetter(ch) || (ch == '_') || (ch == ':'))) { // ':' e.g. +::Module++
                        input.backup(1);
                        break;
                    }
                }

                break;

            case '<': { // Html tag - rdoc

                // Only accept things that look like tags: <foo> or </foo>, not
                // <<, < >, etc.
                int next = input.read();
                input.backup(1);

                if (!((next == '/') || Character.isLetter(next))) {
                    break;
                }

                if (input.readLength() > 1) {
                    input.backup(1);

                    return token(RubyCommentTokenId.COMMENT_TEXT);
                }

                while (ch != EOF) {
                    ch = input.read();

                    if (ch == '\n') {
                        break;
                    } else if (ch == '>') {
                        return token(RubyCommentTokenId.COMMENT_HTMLTAG);
                    }
                }

                break;
            }

            case ':': { // Possible rdoc tag, like :nodoc:

                if (input.readLength() > 1) {
                    input.backup(1);

                    return token(RubyCommentTokenId.COMMENT_TEXT);
                }

                int backup = 0;

                while (ch != EOF) {
                    ch = input.read();
                    backup++;

                    if ((ch == '\n') || (!Character.isLetter(ch) && (ch != '_') && (ch != '-'))) {
                        if ((ch == ':') && (input.readLength() > 2)) { // Don't recognize "::" since it's used a lot when mentioning modules
                                                                       // I should be able to use input.readText(1, ...) here but it doesn't work right

                            String seen = input.readText().toString();
                            String directive = seen.substring(1, seen.length() - 1);

                            for (String keyword : RDOC_DIRECTIVES) {
                                if (keyword.equals(directive)) {
                                    return token(RubyCommentTokenId.COMMENT_RDOC);
                                }
                            }
                        }

                        input.backup(backup);

                        break;
                    }
                }

                continue;
            }

            default: {
                if (!inWord) {
                    // See if we have a match from here on for any of the markers
                    String[] todoMarkers = getTodoMarkers();

                    for (int i = 0; i < todoMarkers.length; i++) {
                        if (todoMarkers[i].charAt(0) == ch) {
                            if (input.readLength() > 1) {
                                input.backup(1);

                                return token(RubyCommentTokenId.COMMENT_TEXT);
                            }

                            // Possible match!
                            // Read ahead while matching further characters, but if they
                            // stop matching, back up and try another
                            int backup = 0;
                            String marker = todoMarkers[i];

                            for (int c = 1, n = marker.length(); c < n; c++) {
                                backup++;

                                if (input.read() != marker.charAt(c)) {
                                    input.backup(backup);

                                    break;
                                }
                            }

                            if (backup == (marker.length() - 1)) { // Found it
                                                                   // Peek ahead and make sure this match is a whole word

                                boolean separate = !Character.isJavaIdentifierPart(input.read());
                                input.backup(1);

                                if (separate) {
                                    return tokenFactory.createToken(RubyCommentTokenId.COMMENT_TODO,
                                        input.readLength());
                                }
                            }
                        }
                    }
                }
            }
            }

            inWord = Character.isJavaIdentifierPart(ch);
        }
    }

    private Token<RubyCommentTokenId> token(RubyCommentTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }
}
