/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import jdk.nashorn.internal.ir.FunctionNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatter implements Formatter {

    // only for tests
    static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(JsFormatter.class.getName());

    private static final boolean ELSE_IF_SINGLE_LINE = true;

    private final Language<JsTokenId> language;

    private int lastOffsetDiff = 0;

    private final Set<FormatToken> processed = new HashSet<FormatToken>();

    public JsFormatter(Language<JsTokenId> language) {
        this.language = language;
    }

    @Override
    public int hangingIndentSize() {
        return CodeStyle.get((Document) null).getContinuationIndentSize();
    }

    @Override
    public int indentSize() {
        return CodeStyle.get((Document) null).getIndentSize();
    }

    @Override
    public boolean needsParserResult() {
        return true;
    }

    @Override
    public void reformat(final Context context, final ParserResult compilationInfo) {
        processed.clear();
        lastOffsetDiff = 0;
        final BaseDocument doc = (BaseDocument) context.document();
        final boolean templateEdit = doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;

        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                long startTime = System.nanoTime();

                FormatContext formatContext = new FormatContext(context, compilationInfo.getSnapshot(), language);

                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        compilationInfo.getSnapshot().getTokenHierarchy(), context.startOffset(), language);

                if (ts == null) {
                    return;
                }

                FormatTokenStream tokenStream = FormatTokenStream.create(
                        ts, 0/*context.startOffset()*/, context.endOffset());
                LOGGER.log(Level.FINE, "Format token stream creation: {0} ms",
                        (System.nanoTime() - startTime) / 1000000);

                startTime = System.nanoTime();
                FormatVisitor visitor = new FormatVisitor(tokenStream,
                        ts, context.endOffset());

                FunctionNode root = ((JsParserResult) compilationInfo).getRoot();
                if (root != null) {
                    root.accept(visitor);
                } else {
                    LOGGER.log(Level.FINE, "Format visitor not executed; no root node");
                }
                LOGGER.log(Level.FINE, "Format visitor: {0} ms",
                        (System.nanoTime() - startTime) / 1000000);

                startTime = System.nanoTime();

                int initialIndent = CodeStyle.get(formatContext).getInitialIndent();
                int continuationIndent = CodeStyle.get(formatContext).getContinuationIndentSize();

                List<FormatToken> tokens = tokenStream.getTokens();
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (FormatToken token : tokens) {
                        LOGGER.log(Level.FINE, token.toString());
                    }
                }

                // when the start offset != 0 this flag indicates when we
                // got after the start so we may change document now
                boolean started = false;
                boolean firstTokenFound = false;
                Stack<FormatContext.ContinuationBlock> continuations = new Stack<FormatContext.ContinuationBlock>();

                for (int i = 0; i < tokens.size(); i++) {
                    FormatToken token = tokens.get(i);
                    if (!started && !token.isVirtual() && token.getOffset() >= context.startOffset()) {
                        started = true;
                    }

                    if (processed.remove(token)) {
                        continue;
                    }

                    if (!token.isVirtual()) {
                        updateContinuationEnd(formatContext, token, continuations);

                        if (!firstTokenFound) {
                            firstTokenFound = true;
                            formatContext.setCurrentLineStart(token.getOffset());
                        }
                        // we do not store last things for potentially
                        // removed/replaced tokens
                        if (token.getKind() != FormatToken.Kind.WHITESPACE
                                && token.getKind() != FormatToken.Kind.EOL) {
                            lastOffsetDiff = formatContext.getOffsetDiff();
                        }
                        initialIndent = formatContext.getEmbeddingIndent(tokenStream, token)
                                + CodeStyle.get(formatContext).getInitialIndent();
                    }

                    if (started && (token.getKind() == FormatToken.Kind.BLOCK_COMMENT
                            || token.getKind() == FormatToken.Kind.DOC_COMMENT
                            || token.getKind() == FormatToken.Kind.LINE_COMMENT)) {
                        try {
                            int indent = context.lineIndent(context.lineStartOffset(
                                    formatContext.getDocumentOffset(token.getOffset()) + formatContext.getOffsetDiff()));
                            formatComment(token, formatContext, indent);
                        } catch (BadLocationException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    } else if (started && token.getKind().isSpaceMarker()) {
                        formatSpace(tokens, i, formatContext);
                    } else if (started && token.getKind().isLineWrapMarker()) {
                        formatLineWrap(tokens, i, formatContext, initialIndent,
                                continuationIndent, continuations);
                    } else if (token.getKind().isIndentationMarker()) {
                        updateIndentationLevel(token, formatContext);
                    } else if (token.getKind() == FormatToken.Kind.SOURCE_START
                            || token.getKind() == FormatToken.Kind.EOL) {
                        // XXX refactor eol token WRAP_IF_LONG handling
                        if (started && token.getKind() != FormatToken.Kind.SOURCE_START) {
                            // search for token which will be present just before eol
                            FormatToken tokenBeforeEol = null;
                            for (int j = i - 1; j >= 0; j--) {
                                tokenBeforeEol = tokens.get(j);
                                if (!tokenBeforeEol.isVirtual()) {
                                    break;
                                }
                            }
                            if (tokenBeforeEol.getKind() != FormatToken.Kind.SOURCE_START) {
                                int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                                        - formatContext.getCurrentLineStart() + lastOffsetDiff;

                                if (segmentLength >= CodeStyle.get(formatContext).getRightMargin()) {
                                    FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                                    if (lastWrap != null) {
                                        // wrap it
                                        wrapLine(formatContext, lastWrap, initialIndent, continuationIndent, continuations);
                                    }
                                }
                            }
                        }

                        if (started) {
                            // remove trailing spaces
                            removeTrailingSpaces(tokens, i, formatContext, token, templateEdit);
                        }
                        if (token.getKind() != FormatToken.Kind.SOURCE_START) {
                            formatContext.setCurrentLineStart(token.getOffset()
                                    + 1 + formatContext.getOffsetDiff());
                            formatContext.setLastLineWrap(null);
                        }

                        // following code handles the indentation
                        // do not do indentation for line comments starting
                        // at the beginning of the line to support comment/uncomment
                        FormatToken next = FormatTokenStream.getNextNonVirtual(token);
                        if (next != null && next.getKind() == FormatToken.Kind.LINE_COMMENT) {
                            continue;
                        }

                        FormatToken indentationStart = null;
                        FormatToken indentationEnd = null;
                        // we add tokens to processed to not to process them twice
                        for (int j = i + 1; j < tokens.size(); j++) {
                            FormatToken nextToken = tokens.get(j);
                            if (!nextToken.isVirtual()) {
                                if (nextToken.getOffset() >= context.startOffset()) {
                                    started = true;
                                }
                                if (nextToken.getKind() != FormatToken.Kind.WHITESPACE) {
                                    indentationEnd = nextToken;
                                    if (indentationStart == null) {
                                        indentationStart = nextToken;
                                    }
                                    break;
                                } else {
                                    if (indentationStart == null) {
                                        indentationStart = nextToken;
                                    }
                                }
                            } else {
                                updateIndentationLevel(nextToken, formatContext);
                            }
                            processed.add(nextToken);
                        }

                        // if it is code template formatting we want to do
                        // proper indentation even on a blank line
                        if (indentationEnd != null
                                && (indentationEnd.getKind() != FormatToken.Kind.EOL || templateEdit)) {
                            int indentationSize = initialIndent + formatContext.getIndentationLevel() * IndentUtils.indentLevelSize(doc);
                            int continuationLevel = formatContext.getContinuationLevel();
                            if (isContinuation(formatContext, token, false)) {
                                continuationLevel++;
                                updateContinuationStart(formatContext, token, continuations);
                            }
                            indentationSize += continuationIndent * continuationLevel;
                            if (started) {
                                formatContext.indentLine(
                                        indentationStart.getOffset(), indentationSize,
                                        checkIndentation(doc, token, indentationEnd, formatContext, context, indentationSize));
                            }
                        }
                    }
                }
                LOGGER.log(Level.FINE, "Formatting changes: {0} ms", (System.nanoTime() - startTime) / 1000000);
            }
        });
    }

    private static void removeTrailingSpaces(List<FormatToken> tokens, int index,
            FormatContext formatContext, FormatToken limit, boolean templateEdit) {

        // if it is code template we are doing indentation even on a blank line
        if (templateEdit && limit.getKind() == FormatToken.Kind.EOL) {
            return;
        }

        FormatToken start = null;
        for (int j = index - 1; j >= 0; j--) {
            FormatToken nextToken = tokens.get(j);
            if (!nextToken.isVirtual()
                    && nextToken.getKind() != FormatToken.Kind.WHITESPACE) {
                break;
            } else {
                start = tokens.get(j);
            }
        }
        while (start != null && start != limit) {
            if (!start.isVirtual()) {
                formatContext.remove(start.getOffset(),
                        start.getText().length());
            }
            start = start.next();
        }
    }

    private void wrapLine(FormatContext formatContext, FormatContext.LineWrap lastWrap,
            int initialIndent, int continuationIndent, Stack<FormatContext.ContinuationBlock> continuations) {
        // we dont have to remove trailing spaces as indentation will fix it
        formatContext.insertWithOffsetDiff(lastWrap.getToken().getOffset()
                + lastWrap.getToken().getText().length(), "\n", lastWrap.getOffsetDiff()); // NOI18N
        // there is + 1 for eol
        formatContext.setCurrentLineStart(lastWrap.getToken().getOffset()
                + lastWrap.getToken().getText().length() + 1 + lastWrap.getOffsetDiff());
        // do the indentation
        int indentationSize = initialIndent
                + lastWrap.getIndentationLevel() * IndentUtils.indentLevelSize(formatContext.getDocument());

        int continuationLevel = formatContext.getContinuationLevel();
        if (isContinuation(formatContext, lastWrap.getToken(), true)) {
            continuationLevel++;
            updateContinuationStart(formatContext, lastWrap.getToken(), continuations);
        }
        indentationSize += continuationIndent * continuationLevel;
        formatContext.indentLineWithOffsetDiff(
                lastWrap.getToken().getOffset() + lastWrap.getToken().getText().length() + 1,
                indentationSize, Indentation.ALLOWED, lastWrap.getOffsetDiff());
    }

    private void formatLineWrap(List<FormatToken> tokens, int index, FormatContext formatContext,
            int initialIndent, int continuationIndent, Stack<FormatContext.ContinuationBlock> continuations) {

        FormatToken token = tokens.get(index);
        CodeStyle.WrapStyle style = getLineWrap(token, formatContext);
        if (style == null) {
            return;
        }

        // search for token which will be present after eol
        FormatToken tokenAfterEol = token.next();
        int startIndex = index;
        while (tokenAfterEol != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL
                && tokenAfterEol.getKind() != FormatToken.Kind.TEXT) {
            tokenAfterEol = tokenAfterEol.next();
            startIndex++;
        }

        // search for token which will be present just before eol
        FormatToken tokenBeforeEol = null;
        for (int j = startIndex - 1; j >= 0; j--) {
            tokenBeforeEol = tokens.get(j);
            if (!tokenBeforeEol.isVirtual()
                    && tokenBeforeEol.getKind() != FormatToken.Kind.WHITESPACE) {
                break;
            }
        }

        // assert we can use the lastOffsetDiff and lastIndentationLevel
        assert tokenBeforeEol.getKind() != FormatToken.Kind.WHITESPACE
                && tokenBeforeEol.getKind() != FormatToken.Kind.EOL : tokenBeforeEol;

        if (style == CodeStyle.WrapStyle.WRAP_IF_LONG) {
            int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                    - formatContext.getCurrentLineStart() + lastOffsetDiff;

            if (segmentLength >= CodeStyle.get(formatContext).getRightMargin()) {
                FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                if (lastWrap != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL) {
                    // we dont have to remove trailing spaces as indentation will fix it
                    int offsetBeforeChanges = formatContext.getOffsetDiff();
                    // wrap it
                    wrapLine(formatContext, lastWrap, initialIndent, continuationIndent, continuations);
                    // we need to mark the current wrap
                    formatContext.setLastLineWrap(new FormatContext.LineWrap(
                            tokenBeforeEol, lastOffsetDiff + (formatContext.getOffsetDiff() - offsetBeforeChanges),
                            formatContext.getIndentationLevel(), formatContext.getContinuationLevel()));
                    return;
                }
                // we proceed with wrapping if there is no wrap other than current
                // and we are longer than whats allowed
            } else {
                formatContext.setLastLineWrap(new FormatContext.LineWrap(
                        tokenBeforeEol, lastOffsetDiff,
                        formatContext.getIndentationLevel(), formatContext.getContinuationLevel()));
                return;
            }
        }

        // contains the last eol in case of multiple empty lines
        // otherwise equal to tokenAfterEol
        FormatToken extendedTokenAfterEol = tokenAfterEol;
        for (FormatToken current = tokenAfterEol; current != null && (current.getKind() == FormatToken.Kind.EOL
                || current.getKind() == FormatToken.Kind.WHITESPACE
                || current.isVirtual()); current = current.next()) {
            if (current != tokenAfterEol && current.getKind() == FormatToken.Kind.EOL) {
                extendedTokenAfterEol = current;
            }
        }

        // statement like wrap is a bit special at least for now
        // we dont remove redundant eols for them
        if (tokenAfterEol != null
                // there is no eol
                && (tokenAfterEol.getKind() != FormatToken.Kind.EOL
                // or there are multiple lines and we are not after statement like token
                || extendedTokenAfterEol != tokenAfterEol && !isStatementWrap(token))) {

            // proceed the skipped tokens moving the main loop
            moveForward(token, extendedTokenAfterEol, formatContext, true);

            if (style != CodeStyle.WrapStyle.WRAP_NEVER) {
                if (tokenAfterEol.getKind() != FormatToken.Kind.EOL) {

                    // we have to check the line length and wrap if needed
                    // FIXME duplicated code
                    int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                            - formatContext.getCurrentLineStart() + lastOffsetDiff;

                    if (segmentLength >= CodeStyle.get(formatContext).getRightMargin()) {
                        FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                        if (lastWrap != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL) {
                            // wrap it
                            wrapLine(formatContext, lastWrap, initialIndent, continuationIndent, continuations);
                        }
                    }

                    // we dont have to remove trailing spaces as indentation will fix it
                    formatContext.insert(tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(), "\n"); // NOI18N
                    // there is + 1 for eol
                    formatContext.setCurrentLineStart(tokenBeforeEol.getOffset()
                        + tokenBeforeEol.getText().length() + 1);
                    formatContext.setLastLineWrap(null);
                    // do the indentation
                    int indentationSize = initialIndent
                            + formatContext.getIndentationLevel() * IndentUtils.indentLevelSize(formatContext.getDocument());

                    int continuationLevel = formatContext.getContinuationLevel();
                    if (isContinuation(formatContext, tokenBeforeEol, true)) {
                        continuationLevel++;
                        updateContinuationStart(formatContext, tokenBeforeEol, continuations);
                    }
                    indentationSize += continuationIndent * continuationLevel;
                    formatContext.indentLine(
                            tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(),
                            indentationSize, Indentation.ALLOWED);
                }

                if (extendedTokenAfterEol != tokenAfterEol) {

                    if (extendedTokenAfterEol != null) {
                        formatContext.remove(tokenAfterEol.getOffset(),
                                extendedTokenAfterEol.getOffset() - tokenAfterEol.getOffset());
                        // move to eol to do indentation in next cycle
                        // it is safe because we know the token to which we move is eol
                        processed.remove(extendedTokenAfterEol.previous());
                    } else {
                        FormatToken last = tokens.get(tokens.size() - 1);
                        while (last != null && last.isVirtual()) {
                            last = last.previous();
                        }
                        if (last != null) {
                            formatContext.remove(tokenAfterEol.getOffset(),
                                    last.getOffset() + last.getText().length() - tokenAfterEol.getOffset());
                        }
                    }
                }
            } else {
                int start = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length();

                FormatToken endToken = extendedTokenAfterEol;
                if (endToken == null) {
                    // end of file
                    endToken = tokens.get(tokens.size() - 1);
                    while (endToken != null && endToken.isVirtual()) {
                        endToken = endToken.previous();
                    }
                    if (endToken != null) {
                        formatContext.remove(start, endToken.getOffset() + endToken.getText().length() - start);
                    }
                } else if (endToken.getKind() != FormatToken.Kind.EOL) {
                    // no eol
                    FormatToken spaceStartToken = tokenBeforeEol.next();
                    if (spaceStartToken == null) {
                        spaceStartToken = tokenBeforeEol;
                    }

                    if (isSpace(spaceStartToken, formatContext, true, true)) {
                        formatContext.replace(start, endToken.getOffset() - start, " "); // NOI18N
                    } else {
                        formatContext.remove(start, endToken.getOffset() - start);
                    }
                } else if (tokenAfterEol != endToken) {
                    // multiple eols
                    formatContext.remove(start, endToken.getOffset() - start);
                }
            }
        }
    }

    private void formatSpace(List<FormatToken> tokens, int index, FormatContext formatContext) {
        FormatToken token = tokens.get(index);
        assert token.isVirtual();

        CodeStyle.WrapStyle style = getLineWrap(tokens, index, formatContext, true);
        // wrapping will take care of everything
        if (style == CodeStyle.WrapStyle.WRAP_ALWAYS) {
            return;
        }

        FormatToken lastEol = null;

        FormatToken start = null;
        for (FormatToken current = token.previous(); current != null;
                current = current.previous()) {

            if (!current.isVirtual()) {
                if (current.getKind() != FormatToken.Kind.WHITESPACE
                        && current.getKind() != FormatToken.Kind.EOL) {
                    start = current;
                    break;
                } else if (lastEol == null && current.getKind() == FormatToken.Kind.EOL) {
                    lastEol = current;
                }
            }
        }

        FormatToken end = null;
        for (FormatToken current = token.next(); current != null;
                current = current.next()) {

            if (!current.isVirtual()) {
                if (current.getKind() != FormatToken.Kind.WHITESPACE
                        && current.getKind() != FormatToken.Kind.EOL) {
                    end = current;
                    break;
                } else if (current.getKind() == FormatToken.Kind.EOL) {
                    lastEol = current;
                }
            }
        }

        // we mark space and WRAP_NEVER tokens as processed
        for (FormatToken current = start; current != null && current != end;
                current = current.next()) {
            if (current.isVirtual()
                    && !current.getKind().isIndentationMarker()
                    && getLineWrap(current, formatContext) != CodeStyle.WrapStyle.WRAP_IF_LONG) {
                processed.add(current);
            }
        }

        // FIXME if end is null we might be at EOF
        if (start != null && end != null) {
            boolean remove = !isSpace(token, formatContext, true, false);

            // we fetch the space or next token to start
            start = FormatTokenStream.getNextNonVirtual(start);

            if (start != null) {
                if (start.getKind() != FormatToken.Kind.WHITESPACE
                        && start.getKind() != FormatToken.Kind.EOL) {
                    assert start == end : start;
                    if (!remove) {
                        formatContext.insert(start.getOffset(), " "); // NOI18N
                    }
                } else {
                    if (lastEol != null) {
                        end = lastEol;
                    }
                    // if it should be removed or there is eol (in fact space)
                    // which will stay there
                    if (remove || end.getKind() == FormatToken.Kind.EOL) {
                        formatContext.remove(start.getOffset(),
                                end.getOffset() - start.getOffset());
                    } else {
                        formatContext.replace(start.getOffset(),
                                end.getOffset() - start.getOffset(), " "); // NOI18N
                    }
                }
            }
        }
    }

    private void formatComment(FormatToken comment, FormatContext formatContext, int indent) {
        // this assumes the firts line is already indented by EOL logic
        assert comment.getKind() == FormatToken.Kind.BLOCK_COMMENT
                || comment.getKind() == FormatToken.Kind.DOC_COMMENT
                || comment.getKind() == FormatToken.Kind.LINE_COMMENT;

        if (comment.getKind() == FormatToken.Kind.LINE_COMMENT) {
            return;
        }

        String text = comment.getText().toString();
        if (!text.contains("\n")) { // NOI18N
            return;
        }

        // mootools packager see issue #
        if (comment.getKind() == FormatToken.Kind.BLOCK_COMMENT
                && text.startsWith("/*\n---") && text.endsWith("...\n*/")) { // NOI18N
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            char single = text.charAt(i);
            if (single == '\n') { // NOI18N
                // following lines are + 1 indented
                formatContext.indentLine(comment.getOffset() + i + 1,
                        indent + 1, Indentation.ALLOWED); // NOI18N
            }
        }
    }

    private void updateContinuationStart(FormatContext formatContext, FormatToken token,
            Stack<FormatContext.ContinuationBlock> continuations) {

        FormatToken nextImportant = FormatTokenStream.getNextImportant(token);
        if (nextImportant != null && nextImportant.getKind() == FormatToken.Kind.TEXT) {
            if (JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(nextImportant.getText().toString())) {
                continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.CURLY, true));
                formatContext.incContinuationLevel();
                processed.add(nextImportant);
            } else if (JsTokenId.BRACKET_LEFT_BRACKET.fixedText().equals(nextImportant.getText().toString())) {
                continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.BRACKET, true));
                formatContext.incContinuationLevel();
                processed.add(nextImportant);
            } else if (JsTokenId.BRACKET_LEFT_PAREN.fixedText().equals(nextImportant.getText().toString())) {
                continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.PAREN, true));
                formatContext.incContinuationLevel();
                processed.add(nextImportant);
            } else if (JsTokenId.KEYWORD_FUNCTION.fixedText().equals(nextImportant.getText().toString())) {
                FormatToken curly = nextImportant;
                while (curly != null) {
                    if (!curly.isVirtual()) {
                        if (JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(curly.getText().toString())) {
                            // safety catch - something wrong
                            curly = null;
                            break;
                        }
                        if (JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(curly.getText().toString())) {
                            break;
                        }
                    }
                    curly = curly.next();
                }
                if (curly != null) {
                    continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.CURLY, true));
                    formatContext.incContinuationLevel();
                    processed.add(curly);
                }
            }
        }
    }


    private void updateContinuationEnd(FormatContext formatContext, FormatToken token,
            Stack<FormatContext.ContinuationBlock> continuations) {
        if (token.isVirtual() || continuations.isEmpty() || token.getKind() != FormatToken.Kind.TEXT) {
            return;
        }

        if (JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(token.getText().toString())) {
            continuations.push(new FormatContext.ContinuationBlock(
                    FormatContext.ContinuationBlock.Type.CURLY, false));
        } else if (JsTokenId.BRACKET_LEFT_BRACKET.fixedText().equals(token.getText().toString())) {
            continuations.push(new FormatContext.ContinuationBlock(
                    FormatContext.ContinuationBlock.Type.BRACKET, false));
        } else if (JsTokenId.BRACKET_LEFT_PAREN.fixedText().equals(token.getText().toString())) {
            continuations.push(new FormatContext.ContinuationBlock(
                    FormatContext.ContinuationBlock.Type.PAREN, false));
        } else if (JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(token.getText().toString())) {
            FormatContext.ContinuationBlock block = continuations.peek();
            if (block.getType() == FormatContext.ContinuationBlock.Type.CURLY) {
                continuations.pop();
                if (block.isChange()) {
                    formatContext.decContinuationLevel();
                }
            }
        } else if (JsTokenId.BRACKET_RIGHT_BRACKET.fixedText().equals(token.getText().toString())) {
            FormatContext.ContinuationBlock block = continuations.peek();
            if (block.getType() == FormatContext.ContinuationBlock.Type.BRACKET) {
                continuations.pop();
                if (block.isChange()) {
                    formatContext.decContinuationLevel();
                }
            }
        } else if (JsTokenId.BRACKET_RIGHT_PAREN.fixedText().equals(token.getText().toString())) {
            FormatContext.ContinuationBlock block = continuations.peek();
            if (block.getType() == FormatContext.ContinuationBlock.Type.PAREN) {
                continuations.pop();
                if (block.isChange()) {
                    formatContext.decContinuationLevel();
                }
            }
        }
    }

    private static boolean isContinuation(FormatContext formatContext,
            FormatToken token, boolean noRealEol) {

        assert noRealEol || token.getKind() == FormatToken.Kind.SOURCE_START
                || token.getKind() == FormatToken.Kind.EOL;

        if (token.getKind() == FormatToken.Kind.SOURCE_START) {
            return false;
        }

        FormatToken next = token.next();
        for (FormatToken current = next; current != null && current.isVirtual(); current = current.next()) {
            if (current.getKind() == FormatToken.Kind.AFTER_STATEMENT
                    || current.getKind() == FormatToken.Kind.AFTER_PROPERTY
                    || current.getKind() == FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM
                    || current.getKind() == FormatToken.Kind.AFTER_CASE
                    // do not suppose continuation when indentation is changed
                    || current.getKind().isIndentationMarker()) {
                return false;
            }
        }

        // this may happen when curly bracket is on new line
        FormatToken nonVirtualNext = FormatTokenStream.getNextNonVirtual(next);
        if (nonVirtualNext != null) {
            String nextText = nonVirtualNext.getText().toString();
            if (JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(nextText)) {
                FormatToken previous = nonVirtualNext.previous();
                if (previous == null || previous.getKind() != FormatToken.Kind.BEFORE_OBJECT) {
                    return false;
                }
            } else if (JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(nextText)
                    || JsTokenId.BRACKET_RIGHT_BRACKET.fixedText().equals(nextText)) {
                return false;
            }
        }

        // search backwards for important token
        FormatToken result = null;
        for (FormatToken previous = noRealEol ? token : token.previous(); previous != null;
                previous = previous.previous()) {

            FormatToken.Kind kind = previous.getKind();
            if (kind == FormatToken.Kind.SOURCE_START
                    || kind == FormatToken.Kind.TEXT
                    || kind == FormatToken.Kind.AFTER_STATEMENT
                    || kind == FormatToken.Kind.AFTER_PROPERTY
                    || kind == FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM
                    || kind == FormatToken.Kind.AFTER_CASE
                    // do not suppose continuation when indentation is changed
                    || kind.isIndentationMarker()) {
                result = previous;
                break;
            }
        }
        if (result == null
                || result.getKind() == FormatToken.Kind.SOURCE_START
                || result.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || result.getKind() == FormatToken.Kind.AFTER_PROPERTY
                || result.getKind() == FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM
                || result.getKind() == FormatToken.Kind.AFTER_CASE
                // do not suppose continuation when indentation is changed
                || result.getKind().isIndentationMarker()) {
            return false;
        }

        String text = result.getText().toString();
        return !(JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(text)
                || JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(text)
                || formatContext.isGenerated(result));

    }

    // FIXME can we movet his to FormatContext ?
    private Indentation checkIndentation(BaseDocument doc, FormatToken token, FormatToken indentationEnd,
            FormatContext formatContext, Context context, int indentationSize) {

        assert indentationEnd != null && !indentationEnd.isVirtual() : indentationEnd;
        assert token.getKind() == FormatToken.Kind.EOL || token.getKind() == FormatToken.Kind.SOURCE_START;
        // this: (token.getKind() != FormatToken.Kind.SOURCE_START
        // && formatContext.getDocumentOffset(token.getOffset()) >= 0)
        // handles the case when virtual source for embedded code contains
        // non existing eols we must not do indentation on these
        if ((token.getKind() != FormatToken.Kind.SOURCE_START && formatContext.getDocumentOffset(token.getOffset()) >= 0)
                || (context.startOffset() <= 0 && !formatContext.isEmbedded())) {

            // we don't want to touch lines starting with other language
            // it is a bit heuristic but we can't do much
            // see embeddedMultipleSections1.php
            if (formatContext.isGenerated(indentationEnd)) {
                return Indentation.FORBIDDEN;
            }
            return Indentation.ALLOWED;
        }

        // we are sure this is SOURCE_START - no source start indentation in embedded code
        if (formatContext.isEmbedded()) {
            return Indentation.FORBIDDEN;
        }

        try {
            // when we are formatting only selection we
            // have to handle the source start indentation properly
            int lineStartOffset = IndentUtils.lineStartOffset(doc, context.startOffset());
            if (isWhitespace(doc.getText(lineStartOffset, context.startOffset() - lineStartOffset))) {
                int currentIndentation = IndentUtils.lineIndent(doc, lineStartOffset);
                if (currentIndentation != indentationSize) {
                    // fix the indentation if possible
                    if (lineStartOffset + indentationSize >= context.startOffset()) {
                        return new Indentation(true, true);
                    }
                }
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Indentation.FORBIDDEN;
    }

    private static void updateIndentationLevel(FormatToken token, FormatContext formatContext) {
        switch (token.getKind()) {
            case ELSE_IF_INDENTATION_INC:
                if (ELSE_IF_SINGLE_LINE) {
                    break;
                }
            case INDENTATION_INC:
                formatContext.incIndentationLevel();
                break;
            case ELSE_IF_INDENTATION_DEC:
                if (ELSE_IF_SINGLE_LINE) {
                    break;
                }
            case INDENTATION_DEC:
                formatContext.decIndentationLevel();
                break;
            default:
                break;
        }
    }

    private static boolean isStatementWrap(FormatToken token) {
        return token.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || token.getKind() == FormatToken.Kind.AFTER_BLOCK_START
                || token.getKind() == FormatToken.Kind.AFTER_CASE
                || token.getKind() == FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START;
    }

    private static CodeStyle.WrapStyle getLineWrap(List<FormatToken> tokens, int index,
            FormatContext context, boolean skipWitespace) {
        FormatToken token = tokens.get(index);

        assert token.isVirtual();

        FormatToken next = token;
        while (next != null && (next.isVirtual() || skipWitespace && next.getKind() == FormatToken.Kind.WHITESPACE)) {
            CodeStyle.WrapStyle style = getLineWrap(next, context);
            if (style != null) {
                return style;
            }
            next = next.next();
        }
        return null;
    }

    private static CodeStyle.WrapStyle getLineWrap(FormatToken token, FormatContext context) {
        switch (token.getKind()) {
            case AFTER_STATEMENT:
                return CodeStyle.get(context).wrapStatement();
            case AFTER_BLOCK_START:
                // XXX option
                return CodeStyle.WrapStyle.WRAP_ALWAYS;
            case AFTER_CASE:
                // XXX option
                return CodeStyle.WrapStyle.WRAP_ALWAYS;
            case ELSE_IF_AFTER_BLOCK_START:
                if (ELSE_IF_SINGLE_LINE) {
                    return CodeStyle.WrapStyle.WRAP_NEVER;
                }
                // XXX option
                return CodeStyle.WrapStyle.WRAP_ALWAYS;
            case AFTER_VAR_DECLARATION:
                return CodeStyle.get(context).wrapVariables();
            case BEFORE_FUNCTION_DECLARATION_PARAMETER:
                return CodeStyle.get(context).wrapMethodParams();
            case BEFORE_FUNCTION_CALL_ARGUMENT:
                return CodeStyle.get(context).wrapMethodCallArgs();
            case AFTER_IF_START:
                return CodeStyle.get(context).wrapIfStatement();
            case AFTER_ELSE_START:
                return CodeStyle.get(context).wrapIfStatement();
            case AFTER_WHILE_START:
                return CodeStyle.get(context).wrapWhileStatement();
            case AFTER_DO_START:
                return CodeStyle.get(context).wrapDoWhileStatement();
            case AFTER_FOR_START:
                return CodeStyle.get(context).wrapForStatement();
            case AFTER_WITH_START:
                return CodeStyle.get(context).wrapWithStatement();
            case BEFORE_FOR_TEST:
            case BEFORE_FOR_MODIFY:
                return CodeStyle.get(context).wrapFor();
            case BEFORE_CHAIN_CALL_DOT:
                if (CodeStyle.get(context).wrapAfterDotInChainedMethodCalls()) {
                    return null;
                }
                return CodeStyle.get(context).wrapChainedMethodCalls();
            case AFTER_CHAIN_CALL_DOT:
                if (CodeStyle.get(context).wrapAfterDotInChainedMethodCalls()) {
                    return CodeStyle.get(context).wrapChainedMethodCalls();
                }
                return null;
            case AFTER_BINARY_OPERATOR_WRAP:
                if (CodeStyle.get(context).wrapAfterBinaryOps()) {
                    return CodeStyle.get(context).wrapBinaryOps();
                }
                return null;
            case BEFORE_BINARY_OPERATOR_WRAP:
                if (CodeStyle.get(context).wrapAfterBinaryOps()) {
                    return null;
                }
                return CodeStyle.get(context).wrapBinaryOps();
            case AFTER_ASSIGNMENT_OPERATOR_WRAP:
                return CodeStyle.get(context).wrapAssignOps();
            case AFTER_TERNARY_OPERATOR_WRAP:
                if (CodeStyle.get(context).wrapAfterTernaryOps()) {
                    return CodeStyle.get(context).wrapTernaryOps();
                }
                return null;
            case BEFORE_TERNARY_OPERATOR_WRAP:
                if (CodeStyle.get(context).wrapAfterTernaryOps()) {
                    return null;
                }
                return CodeStyle.get(context).wrapTernaryOps();
            case AFTER_OBJECT_START:
            case BEFORE_OBJECT_END:
                return CodeStyle.get(context).wrapObjects();
            case AFTER_PROPERTY:
                return CodeStyle.get(context).wrapProperties();
            case AFTER_ARRAY_LITERAL_START:
            case BEFORE_ARRAY_LITERAL_END:
                return CodeStyle.get(context).wrapArrayInit();
            case AFTER_ARRAY_LITERAL_ITEM:
                return CodeStyle.get(context).wrapArrayInitItems();
            default:
                return null;
        }
    }

    private static boolean isSpace(FormatToken token, FormatContext context,
            boolean skipWitespace, boolean skipEol) {

        if (!(token.isVirtual()
                || skipWitespace && token.getKind() == FormatToken.Kind.WHITESPACE
                || skipEol && token.getKind() == FormatToken.Kind.EOL)) {
            return false;
        }

        boolean hasSpaceMarker = false;
        boolean hasSpace = false;
        FormatToken next = token;
        while (next != null && (next.isVirtual()
                || skipWitespace && next.getKind() == FormatToken.Kind.WHITESPACE
                || skipWitespace && next.getKind() == FormatToken.Kind.EOL)) {
            if (next.getKind() != FormatToken.Kind.WHITESPACE
                    && next.getKind() != FormatToken.Kind.EOL) {
                if (isSpace(next, context)) {
                    return true;
                }
                if (next.getKind().isSpaceMarker()) {
                    hasSpaceMarker = true;
                }
            } else {
                hasSpace = true;
            }
            next = next.next();
        }
        return !hasSpaceMarker && hasSpace;
    }

    private static boolean isSpace(FormatToken token, FormatContext formatContext) {
        switch (token.getKind()) {
            case BEFORE_ASSIGNMENT_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundAssignOps();
            case AFTER_ASSIGNMENT_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundAssignOps();
            case BEFORE_PROPERTY_OPERATOR:
                return CodeStyle.get(formatContext).spaceBeforeColon();
            case AFTER_PROPERTY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAfterColon();
            case BEFORE_BINARY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundBinaryOps();
            case AFTER_BINARY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundBinaryOps();
            case BEFORE_COMMA:
                return CodeStyle.get(formatContext).spaceBeforeComma();
            case AFTER_COMMA:
                return CodeStyle.get(formatContext).spaceAfterComma();
            case AFTER_IF_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeIfParen();
            case AFTER_WHILE_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeWhileParen();
            case AFTER_FOR_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeForParen();
            case AFTER_WITH_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeWithParen();
            case AFTER_SWITCH_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeSwitchParen();
            case AFTER_CATCH_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeCatchParen();
            case BEFORE_WHILE_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeWhile();
            case BEFORE_ELSE_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeElse();
            case BEFORE_CATCH_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeCatch();
            case BEFORE_FINALLY_KEYWORD:
                return CodeStyle.get(formatContext).spaceBeforeFinally();
            case BEFORE_SEMICOLON:
                return CodeStyle.get(formatContext).spaceBeforeSemi();
            case AFTER_SEMICOLON:
                return CodeStyle.get(formatContext).spaceAfterSemi();
            case BEFORE_UNARY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundUnaryOps();
            case AFTER_UNARY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundUnaryOps();
            case BEFORE_TERNARY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundTernaryOps();
            case AFTER_TERNARY_OPERATOR:
                return CodeStyle.get(formatContext).spaceAroundTernaryOps();
            case BEFORE_FUNCTION_DECLARATION:
                return CodeStyle.get(formatContext).spaceBeforeMethodDeclParen();
            case BEFORE_FUNCTION_CALL:
                return CodeStyle.get(formatContext).spaceBeforeMethodCallParen();
            case AFTER_FUNCTION_DECLARATION_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinMethodDeclParens();
            case BEFORE_FUNCTION_DECLARATION_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinMethodDeclParens();
            case AFTER_FUNCTION_CALL_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinMethodCallParens();
            case BEFORE_FUNCTION_CALL_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinMethodCallParens();
            case AFTER_IF_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinIfParens();
            case BEFORE_IF_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinIfParens();
            case AFTER_WHILE_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinWhileParens();
            case BEFORE_WHILE_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinWhileParens();
            case AFTER_FOR_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinForParens();
            case BEFORE_FOR_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinForParens();
            case AFTER_WITH_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinWithParens();
            case BEFORE_WITH_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinWithParens();
            case AFTER_SWITCH_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinSwitchParens();
            case BEFORE_SWITCH_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinSwitchParens();
            case AFTER_CATCH_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinCatchParens();
            case BEFORE_CATCH_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinCatchParens();
            case AFTER_LEFT_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinParens();
            case BEFORE_RIGHT_PARENTHESIS:
                return CodeStyle.get(formatContext).spaceWithinParens();
            case AFTER_LEFT_BRACE:
                return CodeStyle.get(formatContext).spaceWithinBraces();
            case BEFORE_RIGHT_BRACE:
                return CodeStyle.get(formatContext).spaceWithinBraces();
            case BEFORE_IF_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeIfLeftBrace();
            case BEFORE_ELSE_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeElseLeftBrace();
            case BEFORE_WHILE_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeWhileLeftBrace();
            case BEFORE_FOR_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeForLeftBrace();
            case BEFORE_DO_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeDoLeftBrace();
            case BEFORE_TRY_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeTryLeftBrace();
            case BEFORE_CATCH_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeCatchLeftBrace();
            case BEFORE_FINALLY_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeFinallyLeftBrace();
            case BEFORE_SWITCH_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeSwitchLeftBrace();
            case BEFORE_WITH_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeWithLeftBrace();
            case BEFORE_FUNCTION_DECLARATION_BRACE:
                return CodeStyle.get(formatContext).spaceBeforeMethodDeclLeftBrace();
            case AFTER_ARRAY_LITERAL_BRACKET:
                return CodeStyle.get(formatContext).spaceWithinArrayBrackets();
            case BEFORE_ARRAY_LITERAL_BRACKET:
                return CodeStyle.get(formatContext).spaceWithinArrayBrackets();
            case AFTER_NEW_KEYWORD:
                // no option as false (removing space) would brake the code
                return true;
            case AFTER_VAR_KEYWORD:
                // no option as false (removing space) would brake the code
                return true;
            case BEFORE_DOT:
            case AFTER_DOT:
                return false;
            default:
                return false;
        }
    }

    private int getFormatStableStart(BaseDocument doc, Language<JsTokenId> language,
            int offset, boolean embedded) {

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                TokenHierarchy.get(doc), offset, language);
        if (ts == null) {
            return 0;
        }

        ts.move(offset);
        if (!ts.movePrevious()) {
            return 0;
        }

        // Look backwards to find a suitable context
        // which we will assume is properly indented and balanced
        do {
            Token<?extends JsTokenId> token = ts.token();
            TokenId id = token.id();

            // FIXME should we check for more tokens like {, if, else ...
            if (id == JsTokenId.KEYWORD_FUNCTION) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        if (embedded && !ts.movePrevious()) {
            // I may have moved to the front of an embedded JavaScript area, e.g. in
            // an attribute or in a <script> tag. If this is the end of the line,
            // go to the next line instead since the reindent code will go to the beginning
            // of the stable formatting start.
            int sequenceBegin = ts.offset();
            try {
                int lineTextEnd = Utilities.getRowLastNonWhite(doc, sequenceBegin);
                if (lineTextEnd == -1 || sequenceBegin > lineTextEnd) {
                    return Math.min(doc.getLength(), Utilities.getRowEnd(doc, sequenceBegin) + 1);
                }

            } catch (BadLocationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        return ts.offset();
    }

    private boolean isContinuation(BaseDocument doc, int offset, int bracketBalance) throws BadLocationException {

        offset = Utilities.getRowLastNonWhite(doc, offset);
        if (offset == -1) {
            return false;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, offset, language);
        Token<? extends JsTokenId> token = (ts != null ? ts.token() : null);

        if (ts != null && token != null) {
            int index = ts.index();
            JsTokenId previousId = null;
            if (ts.movePrevious()) {
                Token<? extends JsTokenId> previous = LexUtilities.findPreviousNonWsNonComment(ts);
                if (previous != null) {
                    previousId = previous.id();
                }

                ts.moveIndex(index);
                ts.moveNext();
            }

            JsTokenId id = token.id();

            boolean isContinuationOperator = isBinaryOperator(id, previousId);

            if (ts.offset() == offset && token.length() > 1 && token.text().toString().startsWith("\\")) {
                // Continued lines have different token types
                isContinuationOperator = true;
            }

            if (id == JsTokenId.OPERATOR_COMMA) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                isContinuationOperator = (bracketBalance == 0);
            }
            if (id == JsTokenId.BRACKET_LEFT_PAREN) {
                isContinuationOperator = true;
            }

            if (id == JsTokenId.OPERATOR_COLON) {
                TokenSequence<? extends JsTokenId> inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                Token<? extends JsTokenId> foundToken = LexUtilities.findPreviousIncluding(inner,
                        Arrays.asList(JsTokenId.KEYWORD_CASE, JsTokenId.KEYWORD_DEFAULT, JsTokenId.OPERATOR_COLON));
                if (foundToken != null && (foundToken.id() == JsTokenId.KEYWORD_CASE
                        || foundToken.id() == JsTokenId.KEYWORD_DEFAULT)) {
                    isContinuationOperator = false;
                } else {
                    isContinuationOperator = true;
                }
            }
            return isContinuationOperator;
        }

        return false;
    }

    // FIXME we do not reindent multiple regions
    @Override
    public void reindent(final Context context) {

        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        final IndentContext indentContext = new IndentContext(context);
        int indentationSize = IndentUtils.indentLevelSize(document);
        int continuationIndent = CodeStyle.get(indentContext).getContinuationIndentSize();

        try {
            final BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())

            startOffset = Utilities.getRowStart(doc, startOffset);
            int endLineOffset = Utilities.getRowStart(doc, endOffset);
            final boolean indentOnly = (startOffset == endLineOffset)
                    && (endOffset == context.caretOffset() || startOffset == context.caretOffset())
                    && (Utilities.isRowEmpty(doc, startOffset)
                    || Utilities.isRowWhite(doc, startOffset)
                    || Utilities.getFirstNonWhiteFwd(doc, startOffset) == context.caretOffset());
            if (indentOnly && indentContext.isEmbedded()) {
                // Make sure we're not messing with indentation in HTML
                Token<? extends JsTokenId> token = LexUtilities.getToken(doc, startOffset, language);
                if (token == null) {
                    return;
                }
            }

            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }

            
            final int lineStart = startOffset;//Utilities.getRowStart(doc, startOffset);
            int initialOffset = 0;
            int initialIndent = 0;
            if (startOffset > 0) {
                int prevOffset = Utilities.getRowStart(doc, startOffset-1);
                initialOffset = getFormatStableStart(doc, language, prevOffset, indentContext.isEmbedded());
                initialIndent = GsfUtilities.getLineIndent(doc, initialOffset);
            }

            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset + 1 != doc.getLength());

            boolean includeEnd = endOffset == doc.getLength() || indentOnly;

            // TODO - remove initialbalance etc.
            computeIndents(indentContext, initialIndent, indentationSize, continuationIndent, initialOffset, endOffset,
                    indentEmptyLines, includeEnd, indentOnly);

            doc.runAtomic(new Runnable() {
                public void run() {
                    try {
                        List<IndentContext.Indentation> indents = indentContext.getIndentations();
                        // Iterate in reverse order such that offsets are not affected by our edits
                        for (int i = indents.size() - 1; i >= 0; i--) {
                            IndentContext.Indentation indentation = indents.get(i);
                            int indent = indentation.getSize();
                            int lineBegin = indentation.getOffset();

                            if (lineBegin < lineStart) {
                                // We're now outside the region that the user wanted reformatting;
                                // these offsets were computed to get the correct continuation context etc.
                                // for the formatter
                                break;
                            }

                            if (lineBegin == lineStart && i > 0) {
                                // Look at the previous line, and see how it's indented
                                // in the buffer.  If it differs from the computed position,
                                // offset my computed position (thus, I'm only going to adjust
                                // the new line position relative to the existing editing.
                                // This avoids the situation where you're inserting a newline
                                // in the middle of "incorrectly" indented code (e.g. different
                                // size than the IDE is using) and the newline position ending
                                // up "out of sync"
                                IndentContext.Indentation prevIndentation = indents.get(i - 1);
                                int prevOffset = prevIndentation.getOffset();
                                int prevIndent = prevIndentation.getSize();
                                int actualPrevIndent = GsfUtilities.getLineIndent(doc, prevOffset);
                                // NOTE: in embedding this is usually true as we have some nonzero initial indent,
                                // I am just not sure if it is better to add indentOnly check (as I did) or
                                // remove blank lines condition completely?
                                if (actualPrevIndent != prevIndent) {
                                    // For blank lines, indentation may be 0, so don't adjust in that case
                                    if (indentOnly || !(Utilities.isRowEmpty(doc, prevOffset) || Utilities.isRowWhite(doc, prevOffset))) {
                                        indent = actualPrevIndent + (indent-prevIndent);
                                    }
                                }
                            }

                            // Adjust the indent at the given line (specified by offset) to the given indent
                            int currentIndent = GsfUtilities.getLineIndent(doc, lineBegin);

                            if (currentIndent != indent && indent >= 0) {
                                //org.netbeans.editor.Formatter editorFormatter = doc.getFormatter();
                                //editorFormatter.changeRowIndent(doc, lineBegin, indent);
                                context.modifyIndent(lineBegin, indent);
                            }
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            });
        } catch (BadLocationException ble) {
            LOGGER.log(Level.FINE, null, ble);
        }
    }

    private void computeIndents(IndentContext context, int initialIndent, int indentSize, int continuationIndent,
            int startOffset, int endOffset, boolean indentEmptyLines, boolean includeEnd, boolean indentOnly) {

        BaseDocument doc = context.getDocument();
        // PENDING:
        // The reformatting APIs in NetBeans should be lexer based. They are still
        // based on the old TokenID apis. Once we get a lexer version, convert this over.
        // I just need -something- in place until that is provided.

        try {
            // Algorithm:
            // Iterate over the range.
            // Accumulate a token balance ( {,(,[, and keywords like class, case, etc. increases the balance,
            //      },),] and "end" decreases it
            // If the line starts with an end marker, indent the line to the level AFTER the token
            // else indent the line to the level BEFORE the token (the level being the balance * indentationSize)
            // Compute the initial balance and indentation level and use that as a "base".
            // If the previous line is not "done" (ends with a comma or a binary operator like "+" etc.
            // add a "hanging indent" modifier.
            // At the end of the day, we're recording a set of line offsets and indents.
            // This can be used either to reformat the buffer, or indent a new line.

            // State:
            int offset = Utilities.getRowStart(doc, startOffset); // The line's offset
            int end = endOffset;


            // Pending - apply comment formatting too?

            // XXX Look up RHTML too
            //int indentSize = EditorOptions.get(RubyInstallation.RUBY_MIME_TYPE).getSpacesPerTab();
            //int hangingIndentSize = indentSize;


            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // The token balance at the offset
            int balance = 0;
            // The bracket balance at the offset ( parens, bracket, brace )
            int bracketBalance = 0;
            boolean continued = false;
//            boolean indentHtml = false;
//            if (embeddedJavaScript) {
//                indentHtml = codeStyle.indentHtml();
//            }

            //int originallockCommentIndention = 0;
            int adjustedBlockCommentIndention = 0;

            int endIndents;

            final int IN_CODE = 0;
            final int IN_LITERAL = 1;
            final int IN_BLOCK_COMMENT_START = 2;
            final int IN_BLOCK_COMMENT_MIDDLE = 3;

            // this cycle is written in offset but in fact it iretates over lines
            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line

                if (context.isEmbedded()) {
                    // now using JavaScript indent size to indent from <SCRIPT> tag; should it be HTML?
                    initialIndent = context.getEmbeddedIndent() + indentSize;
                }


                int lineType = IN_CODE;
                int pos = Utilities.getRowFirstNonWhite(doc, offset);
                TokenSequence<?extends JsTokenId> ts = null;

                if (pos != -1) {
                    // I can't look at the first position on the line, since
                    // for a string array that is indented, the indentation portion
                    // is recorded as a blank identifier
                    ts = LexUtilities.getPositionedSequence(doc, pos, false, language);

                    if (ts != null) {
                        JsTokenId id = ts.token().id();
                        int index = ts.index();
                        JsTokenId previousId = null;
                        if (ts.movePrevious()) {
                            Token<? extends JsTokenId> previous = LexUtilities.findPreviousNonWsNonComment(ts);
                            if (previous != null) {
                                previousId = previous.id();
                            }

                            ts.moveIndex(index);
                            ts.moveNext();
                        }
                        // We don't have multiline string literals in JavaScript!
                        if (id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.DOC_COMMENT) {
                            if (ts.offset() == pos) {
                                lineType = IN_BLOCK_COMMENT_START;
                                //originallockCommentIndention = GsfUtilities.getLineIndent(doc, offset);
                            } else {
                                lineType =  IN_BLOCK_COMMENT_MIDDLE;
                            }
                        } else if (isBinaryOperator(id, previousId)) {
                            // If a line starts with a non unary operator we can
                            // assume it's a continuation from a previous line
                            continued = true;
                        } else if (id == JsTokenId.STRING || id == JsTokenId.STRING_END ||
                                id == JsTokenId.REGEXP || id == JsTokenId.REGEXP_END) {
                            // You can get multiline literals in JavaScript by inserting a \ at the end
                            // of the line
                            lineType = IN_LITERAL;
                        }
                    } else {
                        // No ruby token -- leave the formatting alone!
                        // (Probably in an RHTML file on a line with no JavaScript)
                        lineType = IN_LITERAL;
                    }
                }

                int hangingIndent = continued ? (continuationIndent) : 0;

                if (lineType == IN_LITERAL) {
                    // Skip this line - leave formatting as it is prior to reformatting
                    indent = GsfUtilities.getLineIndent(doc, offset);

                    // No compound indent for JavaScript
                    //                    if (embeddedJavaScript && indentHtml && balance > 0) {
                    //                        indent += balance * indentSize;
                    //                    }
                } else if (lineType == IN_BLOCK_COMMENT_MIDDLE) {
                    if (doc.getText(pos,1).charAt(0) == '*') {
                        // *-lines get indented to be flushed with the * in /*, other lines
                        // get indented to be aligned with the presumably indented text content!
                        //indent = LexUtilities.getLineIndent(doc, ts.offset())+1;
                        indent = adjustedBlockCommentIndention+1;
                    } else {
                        // Leave indentation of comment blocks alone since they probably correspond
                        // to commented out code - we don't want to lose the indentation.
                        // Possibly, I could shift the code all relative to the first line
                        // in the commented out block... A possible later enhancement.
                        // This shifts by the starting line which is wrong - should use the first comment line
                        //indent = LexUtilities.getLineIndent(doc, offset)-originallockCommentIndention+adjustedBlockCommentIndention;
                        indent = GsfUtilities.getLineIndent(doc, offset);
                    }
                } else if ((!indentOnly || offset < context.getCaretLineStart() || offset > context.getCaretLineEnd()) && (endIndents = isEndIndent(context, offset)) > 0) {
                    indent = (balance-endIndents) * indentSize + hangingIndent + initialIndent;
                } else {
                    assert lineType == IN_CODE || lineType == IN_BLOCK_COMMENT_START;
                    indent = balance * indentSize + hangingIndent + initialIndent;

//                    System.out.println("### indent " + indent + " = " + balance + " * " + indentSize + " + " + hangingIndent + " + " + initialIndent);

                    if (lineType == IN_BLOCK_COMMENT_START) {
                        adjustedBlockCommentIndention = indent;
                    }
                }

                if (indent < 0) {
                    indent = 0;
                }

                int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

                // Insert whitespace on empty lines too -- needed for abbreviations expansion
                if (lineBegin != -1 || indentEmptyLines) {
                    // Don't do a hanging indent if we're already indenting beyond the parent level?

                    context.addIndentation(new IndentContext.Indentation(offset, indent, continued));
                }

                int endOfLine = Utilities.getRowEnd(doc, offset) + 1;

                if (lineBegin != -1) {
                    balance += getTokenBalance(context, ts, lineBegin, endOfLine, true, indentOnly);
                    int bracketDelta = getTokenBalance(context, ts, lineBegin, endOfLine, false, indentOnly);
                    bracketBalance += bracketDelta;
                    continued = isContinuation(doc, offset, bracketBalance);
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private int getTokenBalance(IndentContext context, TokenSequence<? extends JsTokenId> ts, int begin, int end, boolean includeKeywords, boolean indentOnly) {
        int balance = 0;
        BaseDocument doc = context.getDocument();

        if (ts == null) {
            try {
                // remember indent of previous html tag
                context.setEmbeddedIndent(Utilities.getRowIndent(doc, begin));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        int last = begin;
        do {
            Token<? extends JsTokenId> token = ts.token();
            if (token == null) {
                break;
            }
            JsTokenId id = token.id();

            if (includeKeywords) {
                int delta = getTokenBalanceDelta(context, id, ts, indentOnly);
                balance += delta;
            } else {
                balance += getBracketBalanceDelta(id);
            }
            last = ts.offset() + token.length();
        } while (ts.moveNext() && (ts.offset() < end));

        if (context.isEmbedded() && last < end) {
            // We're not done yet... find the next section...
            TokenSequence<? extends JsTokenId> ets = LexUtilities.getNextJsTokenSequence(doc, last+1, end, language);
            if (ets != null && ets.offset() > begin) {
                return balance + getTokenBalance(context, ets, ets.offset(), end, includeKeywords, indentOnly);
            }
        }

        return balance;
    }

    private int getBracketBalanceDelta(JsTokenId id) {
        if (id == JsTokenId.BRACKET_LEFT_PAREN || id == JsTokenId.BRACKET_LEFT_BRACKET) {
            return 1;
        } else if (id == JsTokenId.BRACKET_RIGHT_PAREN || id == JsTokenId.BRACKET_RIGHT_BRACKET) {
            return -1;
        }
        return 0;
    }

    private int getTokenBalanceDelta(IndentContext context, JsTokenId id, TokenSequence<? extends JsTokenId> ts, boolean indentOnly) {
        try {
            BaseDocument doc = context.getDocument();
            OffsetRange range = OffsetRange.NONE;
            if (id == JsTokenId.BRACKET_LEFT_BRACKET || id == JsTokenId.BRACKET_LEFT_CURLY) {
                // block with braces, just record it to stack and return 1
                context.getBlocks().push(new IndentContext.BlockDescription(
                        false, new OffsetRange(ts.offset(), ts.offset())));
                return 1;
            } else if (id == JsTokenId.KEYWORD_CASE || id == JsTokenId.KEYWORD_DEFAULT) {

                int index = ts.index();

                // find colon ':'
                LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.OPERATOR_COLON));

                // skip whitespaces, comments and newlines
                Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
                JsTokenId tokenId = token.id();

                if (tokenId == JsTokenId.KEYWORD_CASE || tokenId == JsTokenId.KEYWORD_DEFAULT) {
                    return 0;
                } else if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                    return -1;
                } else {
                    // look at the beginning of next line if there is case or default
                    LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.EOL));
                    LexUtilities.findNextNonWsNonComment(ts);
                    if (ts.token().id() == JsTokenId.KEYWORD_CASE || ts.token().id() == JsTokenId.KEYWORD_DEFAULT) {
                        return 0;
                    }
                }

                ts.moveIndex(index);
                ts.moveNext();

                return 1;
            } else if (id == JsTokenId.BRACKET_RIGHT_BRACKET || id == JsTokenId.BRACKET_RIGHT_CURLY) {
                /*
                 * End of braces block.
                 * If we are not on same line where block started, try to push
                 * all braceless blocks from stack and decrease indent for them,
                 * otherwise just decrese indent by 1.
                 * For example:
                 * if (true)
                 *   if (true)
                 *     if (true)
                 *       foo();     // we should decrease indent by 3 levels
                 *
                 * but:
                 * if (true)
                 *   if (true)
                 *     if (map[0]) // at ']' we should decrease only by 1
                 *       foo();
                 */
                int delta = -1;
                IndentContext.BlockDescription lastPop = context.getBlocks().empty() ? null : context.getBlocks().pop();
                if (lastPop != null && lastPop.getRange().getStart() <= (doc.getLength() + 1)
                        && Utilities.getLineOffset(doc, lastPop.getRange().getStart()) != Utilities.getLineOffset(doc, ts.offset())) {
                    int blocks = 0;
                    while (!context.getBlocks().empty() && context.getBlocks().pop().isBraceless()) {
                        blocks++;
                    }
                    delta -= blocks;
                }
                return delta;
            } else if ((range = LexUtilities.getMultilineRange(doc, ts)) != OffsetRange.NONE) {
                // we found braceless block, let's record it in the stack
                context.getBlocks().push(new IndentContext.BlockDescription(true, range));
            } else if (id == JsTokenId.EOL) {

                if (!indentOnly) {
                    TokenSequence<? extends JsTokenId> inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                    // skip whitespaces and newlines
                    Token<? extends JsTokenId> nextToken = null;
                    if (inner != null) {
                        nextToken = LexUtilities.findNextNonWsNonComment(inner);
                    }
                    TokenId tokenId = nextToken == null ? null : nextToken.id();
                    if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                        // if it is end of 'switch'
                        OffsetRange offsetRange = LexUtilities.findBwd(doc, inner, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                        if (offsetRange != OffsetRange.NONE) {
                            inner.movePrevious();
                            if (LexUtilities.skipParenthesis(inner, true)) {
                                Token<? extends JsTokenId> token = inner.token();
                                token = LexUtilities.findPreviousNonWsNonComment(inner);
                                if (token.id() == JsTokenId.KEYWORD_SWITCH) {
                                    return -1;
                                }
                            }
                        }
                    } else if (tokenId == JsTokenId.KEYWORD_CASE || tokenId == JsTokenId.KEYWORD_DEFAULT) {
                        inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                        Token<? extends JsTokenId> prevToken = LexUtilities.findPreviousNonWsNonComment(inner);
                        if (prevToken.id() != JsTokenId.BRACKET_LEFT_CURLY) {
                            // it must be case or default
                            inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                            LexUtilities.findPreviousIncluding(inner,
                                    Arrays.asList(JsTokenId.KEYWORD_CASE, JsTokenId.KEYWORD_DEFAULT));

                            int offset = inner.offset();
                            inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                            prevToken = LexUtilities.findPrevious(inner, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL));

                            int beginLine = Utilities.getLineOffset(doc, offset);
                            int eolLine = Utilities.getLineOffset(doc, ts.offset());

                            // we need to take care of case like this:
                            // case 'a':
                            //      test();
                            //      break;
                            //
                            //      //comment
                            //
                            //    case 'b':
                            //      test();
                            //      break;
                            // note the comment - we would get to this block twice
                            // (eol after break and eol after //comment)
                            // so indentation level change would be -2 instead of -1
                            if (prevToken.id() != JsTokenId.BLOCK_COMMENT
                                    && prevToken.id() != JsTokenId.DOC_COMMENT
                                    && prevToken.id() != JsTokenId.LINE_COMMENT) {
                                if (beginLine != eolLine) {
                                    return -1;
                                }
                            } else {
                                int commentLine = Utilities.getLineOffset(doc, inner.offset());
                                if (beginLine != eolLine && commentLine == beginLine) {
                                    return -1;
                                }
                            }
                        }
                    }
                }

                // other
                if (!context.getBlocks().empty()) {
                    if (context.getBlocks().peek().isBraceless()) {
                        // end of line after braceless block start
                        OffsetRange stackOffset = context.getBlocks().peek().getRange();
                        if (stackOffset.containsInclusive(ts.offset())) {
                            if (indentOnly) {
                                // enter pressed in braceless block
                                return 1;
                            }
                            // we are in the braceless block statement
                            int stackEndLine = Utilities.getLineOffset(doc, stackOffset.getEnd());
                            int offsetLine = Utilities.getLineOffset(doc, ts.offset());
                            if (stackEndLine == offsetLine) {
                                // if we are at the last line of braceless block statement
                                // increse indent by 1
                                return 1;
                            }
                        } else {
                            // we are not in braceless block statement,
                            // let's decrease indent for all braceless blocks in top of stack (if any)
                            int blocks = 0;
                            while (!context.getBlocks().empty() && context.getBlocks().peek().isBraceless()) {
                                blocks++;
                                context.getBlocks().pop();
                            }
                            return -blocks;
                        }
                    }
                }
            }
        } catch (BadLocationException ble) {
            LOGGER.log(Level.INFO, null, ble);
        }
        return 0;
    }

    private int isEndIndent(IndentContext context, int offset) throws BadLocationException {
        BaseDocument doc = context.getDocument();
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            Token<?extends JsTokenId> token = getFirstToken(context, offset);

            if (token == null) {
                return 0;
            }

            TokenId id = token.id();

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            if (id == JsTokenId.BRACKET_RIGHT_CURLY || id == JsTokenId.BRACKET_RIGHT_BRACKET
                    || id == JsTokenId.BRACKET_RIGHT_PAREN) {
                int indents = 1;

                // Check if there are multiple end markers here... if so increase indent level.
                // This should really do an iteration... for now just handling the most common
                // scenario in JavaScript where we have }) in object literals
                int lineEnd = Utilities.getRowEnd(doc, offset);
                int newOffset = offset;
                while (newOffset < lineEnd && token != null) {
                    newOffset = newOffset + token.length();
                    if (newOffset < doc.getLength()) {
                        token = LexUtilities.getToken(doc, newOffset, language);
                        if (token != null) {
                            id = token.id();
                            if (id == JsTokenId.WHITESPACE) {
                                continue;
                            } else {
                                break;
                            }
                        }
                    }
                }

                return indents;
            }
        }

        return 0;
    }

    private Token<? extends JsTokenId> getFirstToken(IndentContext context, int offset) throws BadLocationException {
        BaseDocument doc = context.getDocument();
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            if (context.isEmbedded()) {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        TokenHierarchy.get(doc), lineBegin, language);
                if (ts != null) {
                    ts.moveNext();
                    Token<? extends JsTokenId> token = ts.token();
                    while (token != null && token.id() == JsTokenId.WHITESPACE) {
                        if (!ts.moveNext()) {
                            return null;
                        }
                        token = ts.token();
                    }
                    return token;
                }
            } else {
                return LexUtilities.getToken(doc, lineBegin, language);
            }
        }

        return null;
    }

    private static boolean isBinaryOperator(JsTokenId id, JsTokenId previous) {
        switch (id) {
            case OPERATOR_GREATER:
            case OPERATOR_LOWER:
            case OPERATOR_EQUALS:
            case OPERATOR_EQUALS_EXACTLY:
            case OPERATOR_LOWER_EQUALS:
            case OPERATOR_GREATER_EQUALS:
            case OPERATOR_NOT_EQUALS:
            case OPERATOR_NOT_EQUALS_EXACTLY:
            case OPERATOR_AND:
            case OPERATOR_OR:
            case OPERATOR_MULTIPLICATION:
            case OPERATOR_DIVISION:
            case OPERATOR_BITWISE_AND:
            case OPERATOR_BITWISE_OR:
            case OPERATOR_BITWISE_XOR:
            case OPERATOR_MODULUS:
            case OPERATOR_LEFT_SHIFT_ARITHMETIC:
            case OPERATOR_RIGHT_SHIFT_ARITHMETIC:
            case OPERATOR_RIGHT_SHIFT:
            case OPERATOR_ASSIGNMENT:
            case OPERATOR_PLUS_ASSIGNMENT:
            case OPERATOR_MINUS_ASSIGNMENT:
            case OPERATOR_MULTIPLICATION_ASSIGNMENT:
            case OPERATOR_DIVISION_ASSIGNMENT:
            case OPERATOR_BITWISE_AND_ASSIGNMENT:
            case OPERATOR_BITWISE_OR_ASSIGNMENT:
            case OPERATOR_BITWISE_XOR_ASSIGNMENT:
            case OPERATOR_MODULUS_ASSIGNMENT:
            case OPERATOR_LEFT_SHIFT_ARITHMETIC_ASSIGNMENT:
            case OPERATOR_RIGHT_SHIFT_ARITHMETIC_ASSIGNMENT:
            case OPERATOR_RIGHT_SHIFT_ASSIGNMENT:
            case OPERATOR_DOT:
                return true;
            case OPERATOR_PLUS:
            case OPERATOR_MINUS:
                if (previous != null && (previous == JsTokenId.IDENTIFIER || previous == JsTokenId.NUMBER
                        || previous == JsTokenId.REGEXP_END || previous == JsTokenId.STRING_END
                        || previous == JsTokenId.BRACKET_RIGHT_BRACKET || previous == JsTokenId.BRACKET_RIGHT_CURLY
                        || previous == JsTokenId.BRACKET_RIGHT_PAREN)) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Iterates tokens from token to limit while properly updating indentation
     * level. Returns the new index in token sequence.
     *
     * @param token start token
     * @param index start index
     * @param limit end token
     * @param formatContext context to update
     * @return the new index
     */
    private void moveForward(FormatToken token, FormatToken limit,
            FormatContext formatContext, boolean allowComment) {

        for (FormatToken current = token; current != null && current != limit; current = current.next()) {
            assert current.isVirtual()
                    || current.getKind() == FormatToken.Kind.WHITESPACE
                    || current.getKind() == FormatToken.Kind.EOL
                    || allowComment
                        && (current.getKind() == FormatToken.Kind.BLOCK_COMMENT
                        || current.getKind() == FormatToken.Kind.LINE_COMMENT
                        || current.getKind() == FormatToken.Kind.DOC_COMMENT): current;

            processed.add(current);
            updateIndentationLevel(current, formatContext);
            if (current.getKind() == FormatToken.Kind.EOL) {
                formatContext.setCurrentLineStart(current.getOffset()
                        + 1 + formatContext.getOffsetDiff());
                formatContext.setLastLineWrap(null);
            }
        }
    }

    private static boolean isWhitespace(CharSequence charSequence) {
        for (int i = 0; i < charSequence.length(); i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static class Indentation {

        static final Indentation ALLOWED = new Indentation(true, false);

        static final Indentation FORBIDDEN = new Indentation(false, false);

        private final boolean allowed;

        private final boolean exceedLimits;

        public Indentation(boolean allowed, boolean exceedLimits) {
            this.allowed = allowed;
            this.exceedLimits = exceedLimits;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public boolean isExceedLimits() {
            return exceedLimits;
        }
    }
}
