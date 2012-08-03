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

import com.oracle.nashorn.ir.FunctionNode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatter implements Formatter {

    private static final Logger LOGGER = Logger.getLogger(JsFormatter.class.getName());

    private static boolean ELSE_IF_SINGLE_LINE = true;

    private final Language<JsTokenId> language;

    int lastOffsetDiff = 0;

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
        final BaseDocument doc = (BaseDocument) context.document();

        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                long startTime = System.nanoTime();

                FormatContext formatContext = new FormatContext(context, compilationInfo.getSnapshot());
                
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        compilationInfo.getSnapshot().getTokenHierarchy(), context.startOffset(), language);
                
                FormatTokenStream tokenStream = FormatTokenStream.create(
                        ts, context.startOffset(), context.endOffset());
                LOGGER.log(Level.INFO, "Format token stream creation: {0} ms", (System.nanoTime() - startTime) / 1000000);

                startTime = System.nanoTime();
                FormatVisitor visitor = new FormatVisitor(tokenStream,
                        ts, context.endOffset());

                FunctionNode root = ((JsParserResult) compilationInfo).getRoot();
                if (root != null) {
                    root.accept(visitor);
                } else {
                    LOGGER.log(Level.INFO, "Format visitor not executed; no root node");
                }
                LOGGER.log(Level.INFO, "Format visitor: {0} ms", (System.nanoTime() - startTime) / 1000000);

                startTime = System.nanoTime();
                
                int initialIndent = CodeStyle.get(formatContext).getInitialIndent();
                int continuationIndent = CodeStyle.get(formatContext).getContinuationIndentSize();

                List<FormatToken> tokens = tokenStream.getTokens();
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (FormatToken token : tokens) {
                        LOGGER.log(Level.FINE, token.toString());
                    }
                }
                boolean firstTokenFound = false;

                for (int i = 0; i < tokens.size(); i++) {
                    FormatToken token = tokens.get(i);

                    // FIXME optimize performance
                    if (token.getOffset() >= 0) {
                        if (!firstTokenFound) {
                            firstTokenFound = true;
                            formatContext.setCurrentLineStart(token.getOffset());
                        }
                        lastOffsetDiff = formatContext.getOffsetDiff();
                        initialIndent = formatContext.getEmbeddingIndent(token.getOffset())
                                + CodeStyle.get(formatContext).getInitialIndent();
                    }
                    
                    switch (token.getKind()) {
                        case BEFORE_ASSIGNMENT_OPERATOR:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundAssignOps());
                            break;
                        case AFTER_ASSIGNMENT_OPERATOR:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundAssignOps());
                            break;
                        case BEFORE_BINARY_OPERATOR:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundBinaryOps());
                            break;
                        case AFTER_BINARY_OPERATOR:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundBinaryOps());
                            break;
                        case BEFORE_COMMA:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeComma());
                            break;
                        case AFTER_COMMA:
                        case VAR_AFTER_COMMA:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAfterComma());
                            break;
                        case AFTER_IF_KEYWORD:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeIfParen());
                            break;
                        case AFTER_WHILE_KEYWORD:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeWhileParen());
                            break;
                        case AFTER_FOR_KEYWORD:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeForParen());
                            break;
                        case AFTER_WITH_KEYWORD:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeWithParen());
                            break;
                        case AFTER_SWITCH_KEYWORD:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeSwitchParen());
                            break;
                        case AFTER_CATCH_KEYWORD:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeCatchParen());
                            break;
                        case BEFORE_WHILE_KEYWORD:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeWhile());
                            break;
                        case BEFORE_ELSE_KEYWORD:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeElse());
                            break;
                        case BEFORE_CATCH_KEYWORD:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeCatch());
                            break;
                        case BEFORE_FINALLY_KEYWORD:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeFinally());
                            break;
                        case BEFORE_SEMICOLON:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeSemi());
                            break;
                        case AFTER_SEMICOLON:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAfterSemi());
                            break;
                        case BEFORE_UNARY_OPERATOR:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundUnaryOps());
                            break;
                        case AFTER_UNARY_OPERATOR:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundUnaryOps());
                            break;
                        case BEFORE_TERNARY_OPERATOR:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundTernaryOps());
                            break;
                        case AFTER_TERNARY_OPERATOR:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceAroundTernaryOps());
                            break;
                        case BEFORE_FUNCTION_DECLARATION:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeMethodDeclParen());
                            break;
                        case BEFORE_FUNCTION_CALL:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeMethodCallParen());
                            break;
                        case AFTER_FUNCTION_DECLARATION_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinMethodDeclParens());
                            break;
                        case BEFORE_FUNCTION_DECLARATION_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinMethodDeclParens());
                            break;
                        case AFTER_FUNCTION_CALL_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinMethodCallParens());
                            break;
                        case BEFORE_FUNCTION_CALL_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinMethodCallParens());
                            break;
                        case AFTER_IF_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinIfParens());
                            break;
                        case BEFORE_IF_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinIfParens());
                            break;
                        case AFTER_WHILE_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinWhileParens());
                            break;
                        case BEFORE_WHILE_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinWhileParens());
                            break;
                        case AFTER_FOR_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinForParens());
                            break;
                        case BEFORE_FOR_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinForParens());
                            break;
                        case AFTER_WITH_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinWithParens());
                            break;
                        case BEFORE_WITH_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinWithParens());
                            break;
                        case AFTER_SWITCH_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinSwitchParens());
                            break;
                        case BEFORE_SWITCH_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinSwitchParens());
                            break;
                        case AFTER_CATCH_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinCatchParens());
                            break;
                        case BEFORE_CATCH_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinCatchParens());
                            break;
                        case AFTER_LEFT_PARENTHESIS:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinParens());
                            break;
                        case BEFORE_RIGHT_PARENTHESIS:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinParens());
                            break;
                        case BEFORE_IF_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeIfLeftBrace());
                            break;
                        case BEFORE_ELSE_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeElseLeftBrace());
                            break;
                        case BEFORE_WHILE_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeWhileLeftBrace());
                            break;
                        case BEFORE_FOR_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeForLeftBrace());
                            break;
                        case BEFORE_DO_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeDoLeftBrace());
                            break;
                        case BEFORE_TRY_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeTryLeftBrace());
                            break;
                        case BEFORE_CATCH_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeCatchLeftBrace());
                            break;
                        case BEFORE_FINALLY_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeFinallyLeftBrace());
                            break;
                        case BEFORE_SWITCH_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeSwitchLeftBrace());
                            break;
                        case BEFORE_FUNCTION_DECLARATION_BRACE:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceBeforeMethodDeclLeftBrace());
                            break;
                        case AFTER_ARRAY_LITERAL_BRACKET:
                            i = handleSpaceAfter(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinArrayBrackets());
                            break;
                        case BEFORE_ARRAY_LITERAL_BRACKET:
                            i = handleSpaceBefore(tokens, i, formatContext,
                                    !CodeStyle.get(formatContext).spaceWithinArrayBrackets());
                            break;
                        // line wrap and eol handling
                        case ELSE_IF_AFTER_BLOCK_START:
                            if (!ELSE_IF_SINGLE_LINE) {
                                i = handleLineWrap(tokens, i, formatContext, initialIndent);
                            }
                            break;
                        case AFTER_CASE:
                            i = handleLineWrap(tokens, i, formatContext, initialIndent);
                            break;
                        case AFTER_BLOCK_START:
                            i = handleLineWrap(tokens, i, formatContext, initialIndent);
                            break;
                        case AFTER_STATEMENT:
                            i = handleLineWrap(tokens, i, formatContext, initialIndent);
                            break;
                        case SOURCE_START:
                        case EOL:
                            // remove trailing spaces
                            removeTrailingSpaces(tokens, i, formatContext, token);

                            if (token.getKind() != FormatToken.Kind.SOURCE_START) {
                                formatContext.setCurrentLineStart(token.getOffset()
                                        + 1 + formatContext.getOffsetDiff());
                                formatContext.setLastLineWrap(null);
                            }

                            // following code handles the indentation
                            // do not do indentation for line comments starting
                            // at the beginning of the line to support comment/uncomment
                            FormatToken next = getNextNonVirtual(token);
                            if (next != null && next.getKind() == FormatToken.Kind.LINE_COMMENT) {
                                break;
                            }

                            FormatToken indentationStart = null;
                            FormatToken indentationEnd = null;
                            StringBuilder current = new StringBuilder();
                            int index = i;
                            // we move main loop here as well to not to process tokens twice
                            for (int j = i + 1; j < tokens.size(); j++) {
                                FormatToken nextToken = tokens.get(j);
                                if (!nextToken.isVirtual()) {
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
                                        current.append(nextToken.getText());
                                    }
                                } else {
                                    updateIndentationLevel(nextToken, formatContext);
                                }
                                i++;
                            }
                            if (indentationEnd != null
                                    && indentationEnd.getKind() != FormatToken.Kind.EOL) {
                                int indentationSize = initialIndent + formatContext.getIndentationLevel() * IndentUtils.indentLevelSize(doc);
                                if (isContinuation(tokens, index)) {
                                    indentationSize += continuationIndent;
                                }
                                formatContext.indentLine(
                                        indentationStart.getOffset(), indentationSize,
                                        checkIndentation(doc, token, formatContext, context, indentationSize));
                            }
                            break;
                    }

                    // update the indentation for the token
                    updateIndentationLevel(token, formatContext);
                }
                LOGGER.log(Level.INFO, "Formatting changes: {0} ms", (System.nanoTime() - startTime) / 1000000);
            }
        });
    }

    private void removeTrailingSpaces(List<FormatToken> tokens, int index,
            FormatContext formatContext, FormatToken limit) {

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

    private int handleLineWrap(List<FormatToken> tokens, int index, FormatContext formatContext,
            int initialIndent) {

        FormatToken token = tokens.get(index);
        int i = index;

        // search for token which will be present after eol
        FormatToken tokenAfterEol = token.next();
        int startIndex = i;
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

        CodeStyle.WrapStyle style = getLineWrap(token, formatContext);
        if (style == CodeStyle.WrapStyle.WRAP_IF_LONG) {
            int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                    - formatContext.getCurrentLineStart() + lastOffsetDiff;

            if (segmentLength >= CodeStyle.get(formatContext).getRightMargin()) {
                FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                if (lastWrap != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL) {
                    // we dont have to remove trailing spaces as indentation will fix it
                    int offsetBeforeChanges = formatContext.getOffsetDiff();
                    formatContext.insertWithOffsetDiff(lastWrap.getToken().getOffset() + lastWrap.getToken().getText().length(), "\n", lastWrap.getOffsetDiff()); // NOI18N
                    // there is + 1 for eol
                    formatContext.setCurrentLineStart(lastWrap.getToken().getOffset()
                            + lastWrap.getToken().getText().length() + 1 + lastWrap.getOffsetDiff());
                    // do the indentation
                    // FIXME continuation, initialIndent and level - check it is ok
                    int indentationSize = initialIndent
                            + formatContext.getIndentationLevel() * IndentUtils.indentLevelSize(formatContext.getDocument());
                    formatContext.indentLineWithOffsetDiff(
                            lastWrap.getToken().getOffset() + lastWrap.getToken().getText().length() + 1,
                            indentationSize, Indentation.ALLOWED, lastWrap.getOffsetDiff());
                    // we need to mark the current wrap
                    formatContext.setLastLineWrap(new FormatContext.LineWrap(
                            tokenBeforeEol, lastOffsetDiff + (formatContext.getOffsetDiff() - offsetBeforeChanges)));
                    return i;
                }
                // we proceed with wrapping if there is no wrap other than current
                // and we are longer than whats allowed
            } else {
                formatContext.setLastLineWrap(new FormatContext.LineWrap(
                        tokenBeforeEol, lastOffsetDiff));
                return i;
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
            i = moveForward(token, i, extendedTokenAfterEol, formatContext);

            if (style != CodeStyle.WrapStyle.WRAP_NEVER) {
                if (tokenAfterEol.getKind() != FormatToken.Kind.EOL) {
                    // we dont have to remove trailing spaces as indentation will fix it
                    formatContext.insert(tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(), "\n"); // NOI18N
                    // there is + 1 for eol
                    formatContext.setCurrentLineStart(tokenBeforeEol.getOffset()
                            + tokenBeforeEol.getText().length() + 1);
                    formatContext.setLastLineWrap(null);
                    // do the indentation
                    int indentationSize = initialIndent
                            + formatContext.getIndentationLevel() * IndentUtils.indentLevelSize(formatContext.getDocument());
                    formatContext.indentLine(
                            tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(),
                            indentationSize, Indentation.ALLOWED);
                }

                if (extendedTokenAfterEol != tokenAfterEol) {

                    if (extendedTokenAfterEol != null) {
                        formatContext.remove(tokenAfterEol.getOffset(),
                                extendedTokenAfterEol.getOffset() - tokenAfterEol.getOffset());
                        // move to eol to do indentation in next cycle
                        // it is safe because we need the token to which we move is eol
                        i--;
                    } else {
                        FormatToken last = tokens.get(tokens.size() - 1);
                        while (last != null && last.isVirtual()) {
                            last = last.previous();
                        }
                        formatContext.remove(tokenAfterEol.getOffset(),
                                last.getOffset() + last.getText().length() - tokenAfterEol.getOffset());
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
                    formatContext.remove(start, endToken.getOffset() + endToken.getText().length() - start);
                } else if (endToken.getKind() != FormatToken.Kind.EOL) {
                    // no eol
                    // XXX do it only when it is really needed
                    formatContext.replace(start, endToken.getOffset() - start, " "); // NOI18N
                } else if (tokenAfterEol != endToken) {
                    // multiple eols
                    formatContext.remove(start, endToken.getOffset() - start);
                } else {
                    return index;
                }
            }
        }
        return i;
    }

    private int handleSpaceAfter(List<FormatToken> tokens, int index, FormatContext formatContext,
            boolean remove) {

        FormatToken token = tokens.get(index);
        assert token.isVirtual();

        CodeStyle.WrapStyle style = getLineWrap(tokens, index, formatContext);
        if (style == CodeStyle.WrapStyle.WRAP_ALWAYS) {
            return index;
        }

        // now we have some AFTER token, find next non white token
        FormatToken end = null;
        boolean containsEol = false;

        for (FormatToken current = token.next(); current != null;
                current = current.next()) {

            if (!current.isVirtual()) {
                if (current.getKind() != FormatToken.Kind.WHITESPACE
                        && current.getKind() != FormatToken.Kind.EOL) {
                    end = current;
                    break;
                } else if (current.getKind() == FormatToken.Kind.EOL) {
                    containsEol = true;
                }
            }
        }

        // this avoids collision of after and before rules on same whitespace
        for (FormatToken virtual = end; virtual != null && virtual != token;
                virtual = virtual.previous()) {
            // the before marker should win anyway
            if (virtual.isBeforeMarker()) {
                return index;
            }
        }
        //XXX
        FormatToken start = getNextNonVirtual(token);

        if (start != null && end != null) {
            if (start.getKind() != FormatToken.Kind.WHITESPACE
                    && start.getKind() != FormatToken.Kind.EOL) {
                if (!remove) {
                    formatContext.insert(start.getOffset(), " "); // NOI18N
                }
            } else if (!containsEol) {
                if (remove) {
                    formatContext.remove(start.getOffset(),
                            end.getOffset() - start.getOffset());
                } else if ((end.getOffset() - start.getOffset()) != 1 || start.getKind() == FormatToken.Kind.EOL) {
                    formatContext.replace(start.getOffset(),
                            end.getOffset() - start.getOffset(), " "); // NOI18N
                }
                if (style == CodeStyle.WrapStyle.WRAP_NEVER) {
                    return moveForward(token, index, end, formatContext);
                }
            }
        }
        return index;
    }

    private int handleSpaceBefore(List<FormatToken> tokens, int index, FormatContext formatContext,
            boolean remove) {

        FormatToken token = tokens.get(index);
        assert token.isVirtual();

        CodeStyle.WrapStyle style = getLineWrap(tokens, index, formatContext);
        if (style == CodeStyle.WrapStyle.WRAP_ALWAYS) {
            return index;
        }

        // now we have some BEFORE token, find previous non white token
        FormatToken start = null;
        boolean containsEol = false;

        for (FormatToken current = token.previous(); current != null;
                current = current.previous()) {

            if (!current.isVirtual()) {
                if (current.getKind() != FormatToken.Kind.WHITESPACE
                        && current.getKind() != FormatToken.Kind.EOL) {
                    start = current;
                    break;
                } else if (current.getKind() == FormatToken.Kind.EOL) {
                    containsEol = true;
                }
            }
        }

        if (start != null) {
            // we fetch the space to start
            start = getNextNonVirtual(start);
            // and the token we are before
            FormatToken theToken = getNextNonVirtual(token);
            if (start.getKind() != FormatToken.Kind.WHITESPACE
                    && start.getKind() != FormatToken.Kind.EOL) {
                if (!remove) {
                    formatContext.insert(start.getOffset(), " "); // NOI18N
                }
            } else if (!containsEol) {
                if (remove) {
                    formatContext.remove(start.getOffset(),
                            theToken.getOffset() - start.getOffset());
                } else if (theToken.getOffset() - start.getOffset() != 1 || start.getKind() == FormatToken.Kind.EOL) {
                    formatContext.replace(start.getOffset(),
                            theToken.getOffset() - start.getOffset(), " "); // NOI18N
                }
                if (style == CodeStyle.WrapStyle.WRAP_NEVER) {
                    return moveForward(token, index, theToken, formatContext);
                }
            }
        }
        return index;
    }

    private boolean isContinuation(List<FormatToken> tokens, int index) {
        FormatToken token = tokens.get(index);

        assert token.getKind() == FormatToken.Kind.SOURCE_START
                || token.getKind() == FormatToken.Kind.EOL;

        if (token.getKind() == FormatToken.Kind.SOURCE_START) {
            return false;
        }

        FormatToken next = token.next();
        if (next.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || next.getKind() == FormatToken.Kind.AFTER_PROPERTY
                || next.getKind() == FormatToken.Kind.AFTER_CASE
                // do not suppose continuation when indentation is changed
                || next.isIndentationMarker()) {
            return false;
        }

        // this may happen when curly bracket is on new line
        FormatToken nonVirtualNext = getNextNonVirtual(next);
        if (nonVirtualNext != null && nonVirtualNext.getText() != null) {
            String nextText = nonVirtualNext.getText().toString();
            if(JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(nextText)) {
                FormatToken previous = nonVirtualNext.previous();
                if (previous == null || previous.getKind() != FormatToken.Kind.BEFORE_OBJECT) {
                    return false;
                }
            } else if (JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(nextText)) {
                return false;
            }
        }

        // search backwards for important token
        FormatToken result = null;
        for (FormatToken previous = token.previous(); previous != null;
                previous = previous.previous()) {

            FormatToken.Kind kind = previous.getKind();
            if (kind == FormatToken.Kind.SOURCE_START
                    || kind == FormatToken.Kind.TEXT
                    || kind == FormatToken.Kind.AFTER_STATEMENT
                    || kind == FormatToken.Kind.AFTER_PROPERTY
                    || kind == FormatToken.Kind.AFTER_CASE
                    // do not suppose continuation when indentation is changed
                    || previous.isIndentationMarker()) {
                result = previous;
                break;
            }
        }
        if (result == null
                || result.getKind() == FormatToken.Kind.SOURCE_START
                || result.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || result.getKind() == FormatToken.Kind.AFTER_PROPERTY
                || result.getKind() == FormatToken.Kind.AFTER_CASE
                // do not suppose continuation when indentation is changed
                || result.isIndentationMarker()) {
            return false;
        }

        String text = result.getText().toString();
        return !(JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(text)
                || JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(text)
                // this is just safeguard literal offsets should be fixed
                || JsTokenId.OPERATOR_SEMICOLON.fixedText().equals(text));

    }

    private Indentation checkIndentation(BaseDocument doc, FormatToken token,
            FormatContext formatContext, Context context, int indentationSize) {

        assert token.getKind() == FormatToken.Kind.EOL || token.getKind() == FormatToken.Kind.SOURCE_START;
        if (token.getKind() != FormatToken.Kind.SOURCE_START
                || (context.startOffset() <= 0 && !formatContext.isEmbedded())) {
            return Indentation.ALLOWED;
        }

        // no source start indentation in embedded code
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
        }
    }

    private static boolean isStatementWrap(FormatToken token) {
        return token.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || token.getKind() == FormatToken.Kind.AFTER_BLOCK_START
                || token.getKind() == FormatToken.Kind.AFTER_CASE
                || token.getKind() == FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START;
    }

    private static CodeStyle.WrapStyle getLineWrap(List<FormatToken> tokens, int index, FormatContext context) {
        FormatToken token = tokens.get(index);
        
        assert token.isVirtual();

        FormatToken next = token;
        while (next != null && next.isVirtual()) {
            CodeStyle.WrapStyle style = getLineWrap(next, context);
            if (style != null) {
                return style;
            }
            next = next.next();
        }
        return null;
    }

    private static CodeStyle.WrapStyle getLineWrap(FormatToken token, FormatContext context) {
        if (token.getKind() == FormatToken.Kind.AFTER_STATEMENT) {
            // XXX option
            return CodeStyle.get(context).wrapStatement();
        }
        if (token.getKind() == FormatToken.Kind.AFTER_BLOCK_START) {
            // XXX option
            return CodeStyle.WrapStyle.WRAP_ALWAYS;
        }
        if (token.getKind() == FormatToken.Kind.AFTER_CASE) {
            // XXX option
            return CodeStyle.WrapStyle.WRAP_ALWAYS;
        }
        if (token.getKind() == FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START) {
            if (ELSE_IF_SINGLE_LINE) {
                return CodeStyle.WrapStyle.WRAP_NEVER;
            }
            // XXX option
            return CodeStyle.WrapStyle.WRAP_ALWAYS;
        }
        return null;
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
    private static int moveForward(FormatToken token, int index, FormatToken limit,
            FormatContext formatContext) {

        int i = index;
        for (FormatToken current = token; current != null && current != limit; current = current.next()) {
            assert current.isVirtual()
                    || current.getKind() == FormatToken.Kind.WHITESPACE
                    || current.getKind() == FormatToken.Kind.EOL : current;
            updateIndentationLevel(current, formatContext);
            i++;
        }
        return i;
    }

    private static FormatToken getNextNonVirtual(FormatToken token) {
        FormatToken current = token.next();
        while (current != null && current.isVirtual()) {
            current = current.next();
        }
        return current;
    }

    private static boolean isWhitespace(CharSequence charSequence) {
        for (int i = 0; i < charSequence.length(); i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void reindent(Context context) {
        // TODO
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
