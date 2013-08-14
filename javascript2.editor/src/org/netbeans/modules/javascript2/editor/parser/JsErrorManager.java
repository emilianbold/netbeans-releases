/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import jdk.nashorn.internal.parser.Token;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.ErrorManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.nashorn.internal.runtime.ParserException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import static org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId.values;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class JsErrorManager extends ErrorManager {

    private static final Logger LOGGER = Logger.getLogger(JsErrorManager.class.getName());

    private static final int MAX_MESSAGE_LENGTH = 100;

    private static final boolean SHOW_BADGES_EMBEDDED = Boolean.getBoolean(JsErrorManager.class.getName() + ".showBadgesEmbedded");;

    private static final Comparator<SimpleError> POSITION_COMPARATOR = new Comparator<SimpleError>() {

        @Override
        public int compare(SimpleError o1, SimpleError o2) {
            if (o1.getPosition() < o2.getPosition()) {
                return -1;
            }
            if (o1.getPosition() > o2.getPosition()) {
                return 1;
            }
            return 0;
        }
    };

    // message pattern is for example "index.html:2:16 Exepcted ;"
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(".*:\\d+:\\d+ (.*)", Pattern.DOTALL); // NOI18N

    // used to replace pointers from mesage such as
    // Expected ( but found else
    // else
    // ^
    // with this pattern we replace last two lines and related new lines
    private static final Pattern REPLACE_POINTER_PATTERN = Pattern.compile("(\\n)+.*\\n\\s*\\^\\s*"); // NOI18N

    /** Keyword from the error.message which identifies missing char in the JS source. */
    private static final String EXPECTED = "Expected"; //NOI18N

    private final Snapshot snapshot;

    private final Language<JsTokenId> language;

    private List<ParserError> parserErrors;

    private List<JsParserError> convertedErrors;

    private final static Map<String, JsTokenId> JS_TEXT_TOKENS = new HashMap<String, JsTokenId>();

    static {
        for (JsTokenId jsTokenId : values()) {
            if (jsTokenId.fixedText() != null) {
                JS_TEXT_TOKENS.put(jsTokenId.fixedText(), jsTokenId);
            }
        }
    }

    public JsErrorManager(Snapshot snapshot, Language<JsTokenId> language) {
        this.snapshot = snapshot;
        this.language = language;
    }

    Error getMissingCurlyError() {
        if (parserErrors == null) {
            return null;
        }
        for (ParserError error : parserErrors) {
            if (error.message != null
                    && (error.message.contains("Expected }") || error.message.contains("but found }"))) { // NOI18N
                return new JsParserError(convert(error),
                        snapshot != null ? snapshot.getSource().getFileObject() : null,
                        Severity.ERROR, null, true, false, false);
            }
        }
        return null;
    }

    Error getMissingSemicolonError() {
        if (parserErrors == null) {
            return null;
        }
        for (ParserError error : parserErrors) {
            if (error.message != null
                    && error.message.contains("Expected ;")) { // NOI18N
                return new JsParserError(convert(error),
                        snapshot != null ? snapshot.getSource().getFileObject() : null,
                        Severity.ERROR, null, true, false, false);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return parserErrors == null;
    }

    @Override
    public void error(ParserException e) {
        addParserError(new ParserError(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e.getToken()));
    }

    @Override
    public void error(String message) {
        LOGGER.log(Level.FINE, "Error {0}", message);
        addParserError(new ParserError(message));
    }

    @Override
    public void warning(ParserException e) {
        LOGGER.log(Level.FINE, null, e);
    }

    @Override
    public void warning(String message) {
        LOGGER.log(Level.FINE, "Warning {0}", message);
    }

    public List<? extends Error> getErrors() {
        if (convertedErrors == null) {
            if (parserErrors == null) {
                convertedErrors = Collections.emptyList();
            } else {
                ArrayList<SimpleError> errors = new ArrayList<SimpleError>(parserErrors.size());
                for (ParserError error : parserErrors) {
                    errors.add(convert(error));
                }
                Collections.sort(errors, POSITION_COMPARATOR);
                convertedErrors = convert(snapshot, errors);
            }
        }
        return Collections.unmodifiableList(convertedErrors);
    }

    JsErrorManager fillErrors(JsErrorManager original) {
        assert this.snapshot == original.snapshot : this.snapshot + ":" + original.snapshot;
        assert this.language == original.language : this.language + ":" + original.language;

        if (original.parserErrors != null) {
            this.parserErrors = new ArrayList<ParserError>(original.parserErrors);
        } else {
            this.parserErrors = null;
        }
        this.convertedErrors = null;
        return this;
    }

    private void addParserError(ParserError error) {
        convertedErrors = null;
        if (parserErrors == null) {
            parserErrors = new ArrayList<ParserError>();
        }
        parserErrors.add(error);
    }

    private SimpleError convert(ParserError error) {
        String message = error.message;
        int offset = -1;
        Matcher matcher = ERROR_MESSAGE_PATTERN.matcher(message);
        if (matcher.matches()) {
            message = matcher.group(1);
        }
        message = REPLACE_POINTER_PATTERN.matcher(message).replaceAll(""); // NOI18N


        if (error.token > 0) {
            offset = Token.descPosition(error.token);
            if (Token.descType(error.token) == TokenType.EOF
                    && snapshot.getOriginalOffset(offset) == -1) {

                int realOffset = -1;
                TokenSequence<? extends JsTokenId> ts =
                        LexUtilities.getPositionedSequence(snapshot, offset, language);
                while (ts.movePrevious()) {
                    if (snapshot.getOriginalOffset(ts.offset()) > 0) {
                        realOffset = ts.offset() + ts.token().length() - 1;
                        break;
                    }
                }

                if (realOffset > 0) {
                    offset = realOffset;
                }
            }
        } else if (error.line == -1 && error.column == -1) {
            // is this still used ?
            String parts[] = error.message.split(":");
//            if (parts.length > 4) {
//                message = parts[4];
//                int index = message.indexOf('\n');
//                if (index > 0) {
//                    message = message.substring(0, index);
//                }
//
//            }
            if (parts.length > 3) {
                try {
                    offset = Integer.parseInt(parts[3]);
                } catch (NumberFormatException nfe) {
                    // do nothing
                }
            }
        }

        return new SimpleError(message, offset);
    }

    private static List<JsParserError> convert(Snapshot snapshot, List<SimpleError> errors) {
        // basically we are solwing showExplorerBadge attribute here
        List<JsParserError> ret = new ArrayList<JsParserError>(errors.size());
        final FileObject file = snapshot != null ? snapshot.getSource().getFileObject() : null;

        if (snapshot != null && JsParserResult.isEmbedded(snapshot)) {
            int nextCorrect = -1;
            boolean afterGeneratedIdentifier = false;
            for (SimpleError error : errors) {
                boolean showInEditor = true;
                // if the error is in embedded code we ignore it
                // as we don't know what the other language will add
                int pos = snapshot.getOriginalOffset(error.getPosition());
                if (pos >= 0 && nextCorrect <= error.getPosition()
                        && !JsEmbeddingProvider.containsGeneratedIdentifier(error.getMessage())) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(
                            snapshot, error.getPosition());
                    if (ts != null && ts.movePrevious()) {
                        // check also a previous token - is it generated ?
                        org.netbeans.api.lexer.Token<? extends JsTokenId> token =
                                LexUtilities.findPreviousNonWsNonComment(ts);
                        if (JsEmbeddingProvider.containsGeneratedIdentifier(token.text().toString())) {
                            // usually we may expect a group of errors
                            // so we disable them until next } .... \n
                            nextCorrect = findNextCorrectOffset(ts, error.getPosition());
                            showInEditor = false;
                            afterGeneratedIdentifier = true;
                        } else if (afterGeneratedIdentifier && error.getMessage().indexOf(EXPECTED) != -1) {
                            // errors after generated identifiers can display farther - see issue #229985
                            String expected = getExpected(error.getMessage());
                            if ("eof".equals(expected)) { //NOI18N
                                // unexpected end of script, probably missing at some earlier place : ; } etc.
                                showInEditor = false;
                            } else {
                                JsTokenId expectedToken = getJsTokenFromString(expected);
                                ts.movePrevious();
                                org.netbeans.api.lexer.Token<? extends JsTokenId> previousNonWsToken = LexUtilities.findPreviousNonWsNonComment(ts);
                                if (expectedToken != null && expectedToken == previousNonWsToken.id()) {
                                    // char is available, doesn't show the error
                                    showInEditor = false;
                                }
                            }
                        }
                    }
                } else {
                    showInEditor = false;
                }
                ret.add(new JsParserError(error, file, Severity.ERROR, null, true, SHOW_BADGES_EMBEDDED, showInEditor));
            }
        } else {
            for (SimpleError error : errors) {
                ret.add(new JsParserError(error, file, Severity.ERROR, null, true, true, true));
            }
        }
        return ret;
    }

    private static String getExpected(String errorMessage) {
        int expectedIndex = errorMessage.indexOf(EXPECTED);
        String afterExpected = errorMessage.substring(expectedIndex + 9);
        int indexOfSpace = afterExpected.indexOf(" "); //NOI18N
        return (indexOfSpace != -1) ? afterExpected.substring(0, indexOfSpace) : afterExpected;
    }

    public static JsTokenId getJsTokenFromString(String name) {
        return JS_TEXT_TOKENS.get(name);
    }

    private static int findNextCorrectOffset(TokenSequence<? extends JsTokenId> ts, int offset) {
        ts.move(offset);
        if (ts.moveNext()) {
            LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.BRACKET_LEFT_CURLY));
            LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.EOL));
        }
        return ts.offset();
    }

    static class SimpleError {

        private final String message;

        private final int position;

        public SimpleError(String message, int position) {
            this.message = message;
            this.position = position;
        }

        public String getMessage() {
            return message;
        }

        public int getPosition() {
            return position;
        }
    }

    private static class ParserError {

        protected final String message;

        protected final int line;

        protected final int column;

        protected final long token;

        public ParserError(String message, int line, int column, long token) {
            if (message.length() > MAX_MESSAGE_LENGTH) {
                this.message = message.substring(0, MAX_MESSAGE_LENGTH);
                LOGGER.log(Level.FINE, "Too long error message {0}", message);
            } else {
                this.message = message;
            }

            this.line = line;
            this.column = column;
            this.token = token;
        }

        public ParserError(String message, long token) {
            this(message, -1, -1, token);
        }

        public ParserError(String message) {
            this(message, -1, -1, -1);
        }
    }
}
