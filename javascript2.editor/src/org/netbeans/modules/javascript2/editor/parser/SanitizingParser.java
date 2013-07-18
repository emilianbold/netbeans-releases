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

import java.util.Collections;
import jdk.nashorn.internal.ir.FunctionNode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public abstract class SanitizingParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(SanitizingParser.class.getName());

    private static final long MAX_FILE_SIZE_TO_PARSE = 1024 * 1024;

    private final Language<JsTokenId> language;

    private JsParserResult lastResult = null;

    public SanitizingParser(Language<JsTokenId> language) {
        this.language = language;
    }

    @Override
    public final void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, snapshot.getText().toString());
        }
        try {
            JsErrorManager errorManager = new JsErrorManager(snapshot, language);
            lastResult = parseSource(snapshot, event, Sanitize.NONE, errorManager);
            lastResult.setErrors(errorManager.getErrors());
        } catch (Exception ex) {
            LOGGER.log (Level.INFO, "Exception during parsing", ex);
            // TODO create empty result
            lastResult = new JsParserResult(snapshot, null);
        }
    }

    protected abstract String getDefaultScriptName();

    protected abstract FunctionNode parseSource(Snapshot snapshot, String name, String text, JsErrorManager errorManager) throws Exception;

    protected abstract String getMimeType();
    
    private JsParserResult parseSource(Snapshot snapshot, SourceModificationEvent event,
            Sanitize sanitizing, JsErrorManager errorManager) throws Exception {
        
        FileObject fo = snapshot.getSource().getFileObject();
        long startTime = System.nanoTime();
        String scriptName;
        Long size;
        boolean isEmbeded = !getMimeType().equals(snapshot.getMimePath().getPath());
        if (fo != null) {
            scriptName = snapshot.getSource().getFileObject().getNameExt();
            size = fo.getSize();
        } else {
            size = (long)snapshot.getText().length();
            scriptName = getDefaultScriptName();
        }
        if (size > MAX_FILE_SIZE_TO_PARSE && !isEmbeded) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "The file {0} was not parsed because the size is too big.", scriptName);
            }
            return new JsParserResult(snapshot, null);
        }
        int caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, event);
        
        JsParserResult result = parseContext(new Context(scriptName, snapshot, caretOffset),
                sanitizing, errorManager);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Parsing took: {0} ms; source: {1}",
                    new Object[]{(System.nanoTime() - startTime) / 1000000, scriptName});
        }
        return result;
    }
    
    JsParserResult parseContext(Context context, Sanitize sanitizing,
            JsErrorManager errorManager) throws Exception {
        return parseContext(context, sanitizing, errorManager, true);
    }
    
    private JsParserResult parseContext(Context context, Sanitize sanitizing,
            JsErrorManager errorManager, boolean copyErrors) throws Exception {
        
        boolean sanitized = false;
        if ((sanitizing != Sanitize.NONE) && (sanitizing != Sanitize.NEVER)) {
            boolean ok = sanitizeSource(context, sanitizing, errorManager);

            if (ok) {
                sanitized = true;
                assert context.getSanitizedSource() != null;
            } else {
                // Try next trick
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        
        JsErrorManager current = new JsErrorManager(context.getSnapshot(), language);
        FunctionNode node = parseSource(context.getSnapshot(), context.getName(),
                context.getSource(), current);

        if (copyErrors) {
            errorManager.fillErrors(current);
        }
        
        if (sanitizing != Sanitize.NEVER) {
            if (!sanitized) {
                if (current.getMissingCurlyError() != null) {
                    return parseContext(context, Sanitize.MISSING_CURLY, errorManager, false);
                }
                if (current.getMissingSemicolonError() != null) {
                    return parseContext(context, Sanitize.MISSING_SEMICOLON, errorManager, false);
                }
            }
            // TODO not very clever check
            if (node == null || !current.isEmpty()) {
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        return new JsParserResult(context.getSnapshot(), node);
    }

    private boolean sanitizeSource(Context context, Sanitize sanitizing, JsErrorManager errorManager) {
        if (sanitizing == Sanitize.MISSING_CURLY) {
            org.netbeans.modules.csl.api.Error error = errorManager.getMissingCurlyError();
            if (error != null) {
                int offset = error.getStartPosition();
                return sanitizeBrackets(sanitizing, context, offset, '{', '}'); // NOI18N
            }
        } else if (sanitizing == Sanitize.MISSING_SEMICOLON) {
            org.netbeans.modules.csl.api.Error error = errorManager.getMissingSemicolonError();
            if (error != null) {
                String source = context.getOriginalSource();

                boolean ok = false;
                StringBuilder builder = new StringBuilder(source);
                if (error.getStartPosition() >= source.length()) {
                    builder.append(';'); // NOI18N
                    ok = true;
                } else {
                    int replaceOffset = error.getStartPosition();
                    if (replaceOffset >= 0 && Character.isWhitespace(replaceOffset)) {
                        builder.delete(replaceOffset, replaceOffset + 1);
                        builder.insert(replaceOffset, ';'); // NOI18N
                        ok = true;
                    }
                }

                if (ok) {
                    context.setSanitizedSource(builder.toString());
                    context.setSanitization(sanitizing);
                    return true;
                }
            }
        } else if (sanitizing == Sanitize.SYNTAX_ERROR_CURRENT) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        context.getSnapshot(), 0, language);
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext()) {
                        int start = ts.offset();
                        if (start >= 0 && ts.moveNext()) {
                            int end = ts.offset();
                            StringBuilder builder = new StringBuilder(context.getOriginalSource());
                            erase(builder, start, end);
                            context.setSanitizedSource(builder.toString());
                            context.setSanitization(sanitizing);
                            return true;
                        }
                    }
                }
            }
        } else if (sanitizing == Sanitize.SYNTAX_ERROR_PREVIOUS) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizePrevious(sanitizing, context, offset, new TokenCondition() {

                    @Override
                    public boolean found(JsTokenId id) {
                        return id != JsTokenId.WHITESPACE
                                && id != JsTokenId.EOL
                                && id != JsTokenId.DOC_COMMENT
                                && id != JsTokenId.LINE_COMMENT
                                && id != JsTokenId.BLOCK_COMMENT;
                    }
                });
            }
        } else if (sanitizing == Sanitize.MISSING_PAREN) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizeBrackets(sanitizing, context, offset, '(', ')'); // NOI18N
            }
        } else if (sanitizing == Sanitize.ERROR_DOT) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizePrevious(sanitizing, context, offset, new TokenCondition() {

                    @Override
                    public boolean found(JsTokenId id) {
                        return id == JsTokenId.OPERATOR_DOT;
                    }
                });
            }
        } else if (sanitizing == Sanitize.ERROR_LINE) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizeLine(sanitizing, context, offset);
            }
        } else if (sanitizing == Sanitize.EDITED_LINE) {
            int offset = context.getCaretOffset();
            return sanitizeLine(sanitizing, context, offset);
        } else if (sanitizing == Sanitize.PREVIOUS_LINES) {
            StringBuilder result = new StringBuilder(context.getOriginalSource());
            for (org.netbeans.modules.csl.api.Error error : errorManager.getErrors()) {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        context.getSnapshot(), error.getStartPosition(), language);
                if (ts != null) {
                    ts.move(error.getStartPosition());
                    if (ts.movePrevious()) {
                        LexUtilities.findPreviousIncluding(ts, Collections.singletonList(JsTokenId.EOL));
                        if (!sanitizeLine(context.getOriginalSource(), result, ts.offset())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            context.setSanitizedSource(result.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }

    private boolean sanitizePrevious(Sanitize sanitizing, Context context, int offset, TokenCondition condition) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                context.getSnapshot(), 0, language);
        if (ts != null) {
            ts.move(offset);
            int start = -1;
            while (ts.movePrevious()) {
                if (condition.found(ts.token().id())) {
                    start = ts.offset();
                    break;
                }
            }
            if (start >= 0) {
                int end = offset;
                if (ts.moveNext()) {
                    end = ts.offset();
                }
                StringBuilder builder = new StringBuilder(context.getOriginalSource());
                erase(builder, start, end);
                context.setSanitizedSource(builder.toString());
                context.setSanitization(sanitizing);
                return true;
            }
        }
        return false;
    }

    private boolean sanitizeLine(Sanitize sanitizing, Context context, int offset) {
        if (offset > -1) {
            String source = context.getOriginalSource();

            StringBuilder builder = new StringBuilder(source);
            if (!sanitizeLine(source, builder, offset)) {
                return false;
            }
            context.setSanitizedSource(builder.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }

    private boolean sanitizeLine(String source, StringBuilder result, int offset) {
        if (offset > -1 && !source.isEmpty()) {
            int start = offset > 0 ? offset - 1 : offset;
            int end = start + 1;
            // fix until new line or }
            boolean incPosition = false;
            char c = source.charAt(start);
            while (start > 0 && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                c = source.charAt(--start);
                if (start <= 0) {
                    incPosition = false;
                } else {
                    incPosition = true;
                }
            }
            if (incPosition) {
                start++;
            }
            boolean decPosition = false;
            if (end < source.length()) {
                c = source.charAt(end);
                while (end < source.length() && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                    c = source.charAt(end++);
                    if (end >= source.length()) {
                        decPosition = false;
                    } else {
                        decPosition = true;
                    }
                }
            }
            if (decPosition) {
                end--;
            }

            erase(result, start, end);
            return true;
        }
        return false;
    }

    private boolean sanitizeBrackets(Sanitize sanitizing, Context context, int offset,
            char left, char right) {
        String source = context.getOriginalSource();
        int balance = 0;
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == left) {
                balance++;
            } else if (current == right) {
                balance--;
            }
        }
        if (balance != 0) {
            StringBuilder builder = new StringBuilder(source);
            if (balance < 0) {
                while (balance < 0) {
                    int index = builder.lastIndexOf(Character.toString(right));
                    if (index < 0) {
                        break;
                    }
                    erase(builder, index, index + 1);
                    balance++;
                }
            } else if (balance > 0) {
                if (offset >= source.length()) {
                    while (balance > 0) {
                        builder.append(right);
                        balance--;
                    }
                } else {
                    while (balance > 0 && offset - balance >= 0) {
                        // we try to insert them if there are enough whitespaces
                        char current = source.charAt(offset - balance);
                        if (Character.isWhitespace(current)) {
                            builder.replace(offset - balance,
                                    offset - balance + 1, Character.toString(right));
                            balance--;
                        } else {
                            return false;
                        }
                    }
                    if (balance > 0) {
                        return false;
                    }
                }
            }
            context.setSanitizedSource(builder.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }
    
    @Override
    public final Result getResult(Task task) throws ParseException {
        return lastResult;
    }

    @Override
    public final void addChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Adding changeListener: {0}", changeListener); //NOI18N)
    }

    @Override
    public final void removeChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Removing changeListener: {0}", changeListener); //NOI18N)
    }

    private static void erase(StringBuilder builder, int start, int end) {
        builder.delete(start, end);
        for (int i = start; i < end; i++) {
            builder.insert(i, ' ');
        }
    }
    
    /**
     * Parsing context
     */
    static class Context {

        private final String name;
        
        private final Snapshot snapshot;

        private final int caretOffset;
        
        private String source;

        private String sanitizedSource;

        private Sanitize sanitization;

        public Context(String name, Snapshot snapshot, int caretOffset) {
            this.name = name;
            this.snapshot = snapshot;
            this.caretOffset = caretOffset;
        }

        public String getName() {
            return name;
        }

        public Snapshot getSnapshot() {
            return snapshot;
        }

        public int getCaretOffset() {
            return snapshot.getEmbeddedOffset(caretOffset);
        }

        public String getSource() {
            if (sanitizedSource != null) {
                return sanitizedSource;
            }
            return getOriginalSource();
        }

        public String getOriginalSource() {
            if (source == null) {
                source = snapshot.getText().toString();
            }
            return source;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }

        public void setSanitizedSource(String sanitizedSource) {
            this.sanitizedSource = sanitizedSource;
        }

        public Sanitize getSanitization() {
            return sanitization;
        }

        public void setSanitization(Sanitize sanitization) {
            this.sanitization = sanitization;
        }

    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        },

        /** Perform no sanitization */
        NONE {

            @Override
            public Sanitize next() {
                return MISSING_CURLY;
            }
        },
        
        /** Attempt to fix missing } */
        MISSING_CURLY {

            @Override
            public Sanitize next() {
                return MISSING_SEMICOLON;
            }
        },

        /** Attempt to fix missing } */
        MISSING_SEMICOLON {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_CURRENT;
            }
        },
        
        /** Remove current error token */
        SYNTAX_ERROR_CURRENT {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_PREVIOUS;
            }
        },
        
        /** Remove token before error */
        SYNTAX_ERROR_PREVIOUS {

            @Override
            public Sanitize next() {
                return MISSING_PAREN;
            }
        },

        MISSING_PAREN {

            @Override
            public Sanitize next() {
                return EDITED_DOT;
            }
        },
        
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT {

            @Override
            public Sanitize next() {
                return ERROR_DOT;
            }
        },
        
        /** 
         * Try to remove the trailing . at the error position, or the prior
         * line.
         */
        ERROR_DOT {

            @Override
            public Sanitize next() {
                return ERROR_LINE;
            }
        },
        
        /** Try to cut out the error line */
        ERROR_LINE {

            @Override
            public Sanitize next() {
                return EDITED_LINE;
            }
        },
        
        /** Try to cut out the current edited line, if known */
        EDITED_LINE {

            @Override
            public Sanitize next() {
                return PREVIOUS_LINES;
            }
        },

        PREVIOUS_LINES {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        };

        
        public abstract Sanitize next();
    }

    private static abstract class TokenCondition {

        public abstract boolean found(JsTokenId id);

    }

}
