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

import com.oracle.nashorn.ir.Block;
import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.LiteralNode;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.ObjectNode;
import com.oracle.nashorn.ir.PropertyKeyNode;
import com.oracle.nashorn.ir.PropertyNode;
import com.oracle.nashorn.parser.Token;
import com.oracle.nashorn.parser.TokenType;
import com.oracle.nashorn.runtime.Source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.editor.jsdoc.JsDocParser;
import org.netbeans.modules.javascript2.editor.lexer.CommonTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.JsComment;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author Petr Hejl
 */
public class JsonParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(JsParser.class.getName());

    private JsParserResult lastResult = null;

    public JsonParser() {
        super();
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        long startTime = System.currentTimeMillis();
        try {
            JsErrorManager errorManager = new JsErrorManager(snapshot.getSource().getFileObject());
            lastResult = parseSource(snapshot, event, Sanitize.NEVER, errorManager);
            lastResult.setErrors(errorManager.getErrors());
        } catch (Exception ex) {
            LOGGER.log (Level.INFO, "Exception during parsing: {0}", ex);
            // TODO create empty result
            lastResult = new JsParserResult(snapshot, null, Collections.<Integer, JsComment>emptyMap());
        }
        long endTime = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Parsing took: {0} ms source: {1}", new Object[]{endTime - startTime, snapshot.getSource().getFileObject()}); //NOI18N
    }

    private JsParserResult parseSource(Snapshot snapshot, SourceModificationEvent event,
            Sanitize sanitizing, JsErrorManager errorManager) throws Exception {
        
        long startTime = System.nanoTime();
        String scriptName;
        if (snapshot.getSource().getFileObject() != null) {
            scriptName = snapshot.getSource().getFileObject().getNameExt();
        } else {
            scriptName = "javascript.js"; // NOI18N
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
        
        JsErrorManager current = new JsErrorManager(context.getSnapshot().getSource().getFileObject());
        com.oracle.nashorn.ir.FunctionNode node = parseSource(context.getSnapshot(),
                context.getName(), context.getSource(), current);

        if (copyErrors) {
            errorManager.fill(current);
        }
        
        if (sanitizing != Sanitize.NEVER) {
            if (!sanitized && current.getMissingCurlyError() != null) {
                return parseContext(context, Sanitize.MISSING_CURLY, errorManager, false);
            // TODO not very clever check
            } if (node == null || !current.isEmpty()) {
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        
        // process comment elements
        Map<Integer, ? extends JsComment> comments = Collections.<Integer, JsComment>emptyMap();
        if (context.getSnapshot() != null) {
            try {
                long startTime = System.nanoTime();
                comments = JsDocParser.parse(context.getSnapshot());
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Parsing of comments took: {0} ms source: {1}",
                            new Object[]{(System.nanoTime() - startTime) / 1000000, context.getName()});
                }
            } catch (Exception ex) {
                // if anything wrong happen during parsing comments
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return new JsParserResult(context.getSnapshot(), node, comments);
    }
    
    private com.oracle.nashorn.ir.FunctionNode parseSource(Snapshot snapshot, String name, String text, JsErrorManager errorManager) throws Exception {
        com.oracle.nashorn.runtime.Source source = new com.oracle.nashorn.runtime.Source(name, text);
        
        // dummy compiler setup
        com.oracle.nashorn.runtime.options.Options options = new com.oracle.nashorn.runtime.options.Options("nashorn");
        options.process(new String[] {
            "--parse-only=true", // NOI18N
            "--empty-statements=true", // NOI18N
            "--debug-lines=false"}); // NOI18N

        errorManager.setLimit(0);
        com.oracle.nashorn.runtime.Context contextN = new com.oracle.nashorn.runtime.Context(options, errorManager);
        com.oracle.nashorn.runtime.Context.setContext(contextN);
        com.oracle.nashorn.codegen.Compiler compiler = new com.oracle.nashorn.codegen.Compiler(source, contextN);
        
        com.oracle.nashorn.ir.FunctionNode node = null;
        TokenSequence<? extends CommonTokenId> ts = snapshot.getTokenHierarchy().tokenSequence(CommonTokenId.jsonLanguage());
        if (ts != null) {
            ObjectNode object = parseObject(source, ts, errorManager);
            node = new FunctionNode(source, 0, text.length(), compiler, null, null, "runScript"); // NOI18N
            node.setKind(FunctionNode.Kind.SCRIPT);
            node.setStatements(Collections.<Node>singletonList(object));
        }
        return node;
    }
    
    private ObjectNode parseObject(Source source, TokenSequence<? extends CommonTokenId> ts,
            JsErrorManager errorManager) {

        int start = expect(source, CommonTokenId.BRACKET_LEFT_CURLY, ts, errorManager);
        List<Node> members = Collections.emptyList();
        if (nextToken(ts)) {
            CommonTokenId id = ts.token().id();
            ts.movePrevious();
            if (id != CommonTokenId.BRACKET_RIGHT_CURLY) {
                members = parseMembers(source, ts, errorManager);
            }
        }
        
        int end = expect(source, CommonTokenId.BRACKET_RIGHT_CURLY, ts, errorManager);
        Block context = new Block(source, Token.toDesc(TokenType.LBRACE, start, 1), end, null, null);
        return new ObjectNode(source, Token.toDesc(TokenType.LBRACE, start, 1), end, context, members);
    }
    
    private List<Node> parseMembers(Source source, TokenSequence<? extends CommonTokenId> ts,
            JsErrorManager errorManager) {
        
        List<Node> ret = new ArrayList<Node>();
        ret.add(parsePair(source, ts, errorManager));
        if (nextToken(ts)) {
            if (ts.token().id() == CommonTokenId.OPERATOR_COMMA) {
                ret.addAll(parseMembers(source, ts, errorManager));
            } else {
                ts.movePrevious();
            }
        }
        return ret;
    }
    
    private PropertyNode parsePair(Source source, TokenSequence<? extends CommonTokenId> ts,
            JsErrorManager errorManager) {
        int start = expect(source, CommonTokenId.STRING_BEGIN, ts, errorManager);
        expect(source, CommonTokenId.STRING, ts, errorManager);
        String val = ts.token().text().toString();
        int end = expect(source, CommonTokenId.STRING_END, ts, errorManager);
        
        PropertyKeyNode key = LiteralNode.newInstance(source, Token.toDesc(TokenType.STRING, start + 1, val.length()), end, val);
        expect(source, CommonTokenId.OPERATOR_COLON, ts, errorManager);
        
        Node value = parseValue(source, ts, errorManager);
        return new PropertyNode(source, start, value != null ? value.getFinish() : -1, key, value);
    }
    
    private Node parseValue(Source source, TokenSequence<? extends CommonTokenId> ts,
                JsErrorManager errorManager) {
        if (!nextToken(ts)) {
            errorManager.error(source.getName() + ":" + "0:0:" + ts.offset()
                    + ": Expected value but eof found");
            return null;
        }
        CommonTokenId id = ts.token().id();
        
        if (id == CommonTokenId.KEYWORD_TRUE) {
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.TRUE, ts.offset(), id.fixedText().length()), ts.offset() + id.fixedText().length(), true);
        } else if (id == CommonTokenId.KEYWORD_FALSE) {
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.FALSE, ts.offset(), id.fixedText().length()), ts.offset() + id.fixedText().length(), false);
        } else if (id == CommonTokenId.KEYWORD_NULL) {
            return LiteralNode.newInstance(source, Token.toDesc(TokenType.NULL, ts.offset(), id.fixedText().length()), ts.offset() + id.fixedText().length());
        } else if (id == CommonTokenId.STRING_BEGIN) {
            int start = expect(source, CommonTokenId.STRING, ts, errorManager);
            String val = ts.token().text().toString();
            int end = expect(source, CommonTokenId.STRING_END, ts, errorManager);
            return LiteralNode.newInstance(source, Token.toDesc(TokenType.STRING, start + 1, val.length()), end, val);
        } else if (id == CommonTokenId.NUMBER) {
            return LiteralNode.newInstance(source, Token.toDesc(TokenType.FLOATING, ts.offset(), ts.token().text().length()), ts.offset() + ts.token().text().length(),
                    Double.valueOf(ts.token().text().toString()));
        } else if (id == CommonTokenId.BRACKET_LEFT_CURLY) {
            ts.movePrevious();
            return parseObject(source, ts, errorManager);
        }
        errorManager.error(source.getName() + ":" + "0:0:" + ts.offset()
                + ": Expected value but " + ts.token().text().toString() + " found");          
        return null;
    }

    private int expect(Source source, CommonTokenId expected,
            TokenSequence<? extends CommonTokenId> ts, JsErrorManager errorManager) {
        if (!nextToken(ts)) {
            errorManager.error(source.getName() + ":" + "0:0:" + (source.getLength() - 1)
                    + ": Expected " + (expected.fixedText() != null ? expected.fixedText() : expected.toString()) + " but eof found");
            return -1;
        }
        CommonTokenId id = ts.token().id();
        if (id != expected) {
            errorManager.error(source.getName() + ":" + "0:0:" + ts.offset()
                    + ": Expected " + (expected.fixedText() != null ? expected.fixedText() : expected.toString())  + " but " + ts.token().text().toString() + " found");          
            return -1;
        }
        return ts.offset();
    }
    
    private boolean nextToken(TokenSequence<? extends CommonTokenId> ts) {
        boolean ret = false;
        while (ret = ts.moveNext()) {
            if (ts.token().id() != CommonTokenId.WHITESPACE && ts.token().id() != CommonTokenId.EOL) {
                break;
            }
        }
        return ret;
    }
    
    private boolean sanitizeSource(Context context, Sanitize sanitizing, JsErrorManager errorManager) {
        if (sanitizing == Sanitize.MISSING_CURLY) {
            org.netbeans.modules.csl.api.Error error = errorManager.getMissingCurlyError();
            if (error != null) {
                String source = context.getOriginalSource();
                int balance = 0;
                for (int i = 0; i < source.length(); i++) {
                    char current = source.charAt(i);
                    if (current == '{') { // NOI18N
                        balance++;
                    } else if (current == '}') { // NOI18N
                        balance--;
                    }
                }
                if (balance != 0) {
                    StringBuilder builder = new StringBuilder(source);
                    if (balance < 0) {
                        while (balance < 0) {
                            int index = builder.lastIndexOf("}"); // NOI18N
                            if (index < 0) {
                                break;
                            }
                            erase(builder, index, index + 1);
                            balance++;
                        }
                    } else if (balance > 0 && error.getStartPosition() >= source.length()) {
                        while (balance > 0) {
                            builder.append('}'); // NOI18N
                            balance--;
                        }
                    }
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
                TokenSequence<? extends CommonTokenId> ts = LexUtilities.getJsTokenSequence(
                        context.getSnapshot(), 0);
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
                TokenSequence<? extends CommonTokenId> ts = LexUtilities.getJsTokenSequence(
                        context.getSnapshot(), 0);
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext()) {
                        int start = -1;
                        while (ts.movePrevious()) {
                            if (ts.token().id() != CommonTokenId.WHITESPACE
                                    && ts.token().id() != CommonTokenId.EOL
                                    && ts.token().id() != CommonTokenId.DOC_COMMENT
                                    && ts.token().id() != CommonTokenId.LINE_COMMENT
                                    && ts.token().id() != CommonTokenId.BLOCK_COMMENT) {

                                start = ts.offset();
                                break;
                            }
                        }
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
        } else if (sanitizing == Sanitize.ERROR_LINE) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                sanitizeLine(sanitizing, context, offset);
            }
        } else if (sanitizing == Sanitize.EDITED_LINE) {
            int offset = context.getCaretOffset();
            sanitizeLine(sanitizing, context, offset);
        }
        return false;
    }

    private boolean sanitizeLine(Sanitize sanitizing, Context context, int offset) {
        if (offset > -1) {
            String source = context.getOriginalSource();
            int start = offset - 1;
            int end = offset;
            // fix until new line or }
            char c = source.charAt(start);
            while (start > 0 && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                c = source.charAt(--start);
            }
            start++;
            if (end < source.length()) {
                c = source.charAt(end);
                while (end < source.length() && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                    c = source.charAt(end++);
                }
            }

            StringBuilder builder = new StringBuilder(context.getOriginalSource());
            erase(builder, start, end);
            context.setSanitizedSource(builder.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }
    
    @Override
    public Result getResult(Task task) throws ParseException {
        return lastResult;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Adding changeListener: {0}", changeListener); //NOI18N)
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
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
            return caretOffset;
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
                return SYNTAX_ERROR_PREVIOUS_LINE;
            }
        },
        
        /** remove line with error */
        SYNTAX_ERROR_PREVIOUS_LINE {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_BLOCK;
            }
        },
        
        /** try to delete the whole block, where is the error*/
        SYNTAX_ERROR_BLOCK {

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
         * Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line
         */
        ERROR_DOT {

            @Override
            public Sanitize next() {
                return BLOCK_START;
            }
        },
        /** 
         * Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START {

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
                return NEVER;
            }
        };

        
        public abstract Sanitize next();
    }

}
