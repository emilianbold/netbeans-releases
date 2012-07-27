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

    private static boolean NEW_LINE_BREAKS = true;

    private static boolean ELSE_IF_SINGLE_LINE = true;

    private final Language<JsTokenId> language;

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
                int offsetDiff = 0;
                
                int initialIndent = CodeStyle.get(formatContext).getInitialIndent();
                int continuationIndent = CodeStyle.get(formatContext).getContinuationIndentSize();

                int indentationLevel = 0;

                List<FormatToken> tokens = tokenStream.getTokens();
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (FormatToken token : tokens) {
                        LOGGER.log(Level.FINE, token.toString());
                    }
                }

                for (int i = 0; i < tokens.size(); i++) {
                    FormatToken token = tokens.get(i);

                    // FIXME optimize performance
                    if (token.getOffset() >= 0) {
                        initialIndent = formatContext.getEmbeddingIndent(token.getOffset())
                                + CodeStyle.get(formatContext).getInitialIndent();
                    }
                    
                    switch (token.getKind()) {
                        case BEFORE_ASSIGNMENT_OPERATOR:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundAssignOps());
                            break;
                        case AFTER_ASSIGNMENT_OPERATOR:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundAssignOps());
                            break;
                        case BEFORE_BINARY_OPERATOR:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundBinaryOps());
                            break;
                        case AFTER_BINARY_OPERATOR:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundBinaryOps());
                            break;
                        case BEFORE_COMMA:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeComma());
                            break;
                        case AFTER_COMMA:
                        case VAR_AFTER_COMMA:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAfterComma());
                            break;
                        case AFTER_IF_KEYWORD:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeIfParen());
                            break;
                        case AFTER_WHILE_KEYWORD:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeWhileParen());
                            break;
                        case AFTER_FOR_KEYWORD:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeForParen());
                            break;
                        case AFTER_WITH_KEYWORD:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeWithParen());
                            break;
                        case AFTER_SWITCH_KEYWORD:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeSwitchParen());
                            break;
                        case AFTER_CATCH_KEYWORD:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeCatchParen());
                            break;
                        case BEFORE_WHILE_KEYWORD:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeWhile());
                            break;
                        case BEFORE_ELSE_KEYWORD:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeElse());
                            break;
                        case BEFORE_CATCH_KEYWORD:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeCatch());
                            break;
                        case BEFORE_FINALLY_KEYWORD:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeFinally());
                            break;
                        case BEFORE_SEMICOLON:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeSemi());
                            break;
                        case AFTER_SEMICOLON:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAfterSemi());
                            break;
                        case BEFORE_UNARY_OPERATOR:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundUnaryOps());
                            break;
                        case AFTER_UNARY_OPERATOR:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundUnaryOps());
                            break;
                        case BEFORE_TERNARY_OPERATOR:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundTernaryOps());
                            break;
                        case AFTER_TERNARY_OPERATOR:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceAroundTernaryOps());
                            break;
                        case BEFORE_FUNCTION_DECLARATION:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeMethodDeclParen());
                            break;
                        case BEFORE_FUNCTION_CALL:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeMethodCallParen());
                            break;
                        case AFTER_FUNCTION_DECLARATION_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinMethodDeclParens());
                            break;
                        case BEFORE_FUNCTION_DECLARATION_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinMethodDeclParens());
                            break;
                        case AFTER_FUNCTION_CALL_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinMethodCallParens());
                            break;
                        case BEFORE_FUNCTION_CALL_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinMethodCallParens());
                            break;
                        case AFTER_IF_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinIfParens());
                            break;
                        case BEFORE_IF_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinIfParens());
                            break;
                        case AFTER_WHILE_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinWhileParens());
                            break;
                        case BEFORE_WHILE_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinWhileParens());
                            break;
                        case AFTER_FOR_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinForParens());
                            break;
                        case BEFORE_FOR_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinForParens());
                            break;
                        case AFTER_WITH_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinWithParens());
                            break;
                        case BEFORE_WITH_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinWithParens());
                            break;
                        case AFTER_SWITCH_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinSwitchParens());
                            break;
                        case BEFORE_SWITCH_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinSwitchParens());
                            break;
                        case AFTER_CATCH_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinCatchParens());
                            break;
                        case BEFORE_CATCH_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinCatchParens());
                            break;
                        case AFTER_LEFT_PARENTHESIS:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinParens());
                            break;
                        case BEFORE_RIGHT_PARENTHESIS:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinParens());
                            break;
                        case BEFORE_IF_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeIfLeftBrace());
                            break;
                        case BEFORE_ELSE_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeElseLeftBrace());
                            break;
                        case BEFORE_WHILE_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeWhileLeftBrace());
                            break;
                        case BEFORE_FOR_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeForLeftBrace());
                            break;
                        case BEFORE_DO_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeDoLeftBrace());
                            break;
                        case BEFORE_TRY_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeTryLeftBrace());
                            break;
                        case BEFORE_CATCH_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeCatchLeftBrace());
                            break;
                        case BEFORE_FINALLY_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeFinallyLeftBrace());
                            break;
                        case BEFORE_SWITCH_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeSwitchLeftBrace());
                            break;
                        case BEFORE_FUNCTION_DECLARATION_BRACE:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceBeforeMethodDeclLeftBrace());
                            break;
                        case AFTER_ARRAY_LITERAL_BRACKET:
                            offsetDiff = handleSpaceAfter(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinArrayBrackets());
                            break;
                        case BEFORE_ARRAY_LITERAL_BRACKET:
                            offsetDiff = handleSpaceBefore(tokens, i, formatContext, offsetDiff,
                                    !CodeStyle.get(formatContext).spaceWithinArrayBrackets());
                            break;
                        case ELSE_IF_AFTER_BLOCK_START:
                            if (ELSE_IF_SINGLE_LINE) {
                                break;
                            }
                        case AFTER_CASE:
                        case AFTER_BLOCK_START:
                        case AFTER_STATEMENT:
                            if (!NEW_LINE_BREAKS) {
                                break;
                            }

                            // search for token which will be present after eol
                            FormatToken tokenAfterEol = token.next();
                            int startIndex = i;
                            while (tokenAfterEol != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL
                                    && tokenAfterEol.getKind() != FormatToken.Kind.TEXT) {
                                tokenAfterEol = tokenAfterEol.next();
                                startIndex++;
                            }

                            if (tokenAfterEol != null
                                    && tokenAfterEol.getKind() != FormatToken.Kind.EOL) {

                                // proceed the skipped tokens moving the main loop
                                for (FormatToken current = token; current != tokenAfterEol; current = current.next()) {
                                    indentationLevel = updateIndentationLevel(current, indentationLevel);
                                    i++;
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

                                // remove trailing spaces
                                int subsequentOffsetDiff =
                                        removeTrailingSpaces(tokens, startIndex, formatContext, tokenAfterEol, offsetDiff) - offsetDiff;

                                // insert eol
                                offsetDiff = formatContext.insert(tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(), "\n", offsetDiff); // NOI18N
                                // do the indentation
                                int indentationSize = initialIndent + indentationLevel * IndentUtils.indentLevelSize(doc);
                                offsetDiff = formatContext.indentLine(
                                        tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(),
                                        indentationSize, offsetDiff,
                                        Indentation.ALLOWED);
                                offsetDiff += subsequentOffsetDiff;
                            }
                            break;
                        case SOURCE_START:
                        case EOL:
                            // remove trailing spaces
                            offsetDiff = removeTrailingSpaces(tokens, i, formatContext, token, offsetDiff);

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
                                    indentationLevel = updateIndentationLevel(nextToken, indentationLevel);
                                }
                                i++;
                            }
                            if (indentationEnd != null
                                    && indentationEnd.getKind() != FormatToken.Kind.EOL) {
                                int indentationSize = initialIndent + indentationLevel * IndentUtils.indentLevelSize(doc);
                                if (isContinuation(tokens, index)) {
                                    indentationSize += continuationIndent;
                                }
                                offsetDiff = formatContext.indentLine(
                                        indentationStart.getOffset(), indentationSize, offsetDiff,
                                        checkIndentation(doc, token, formatContext, context, indentationSize));
                            }
                            break;
                    }

                    // update the indentation for the token
                    indentationLevel = updateIndentationLevel(token, indentationLevel);
                }
                LOGGER.log(Level.INFO, "Formatting changes: {0} ms", (System.nanoTime() - startTime) / 1000000);
            }
        });
    }

    private int removeTrailingSpaces(List<FormatToken> tokens, int index,
            FormatContext formatContext, FormatToken limit, int offsetDiff) {

        int value = offsetDiff;
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
                value = formatContext.remove(start.getOffset(),
                        start.getText().length(), value);
            }
            start = start.next();
        }
        return value;
    }

    private int handleSpaceAfter(List<FormatToken> tokens, int index,
            FormatContext formatContext, int offsetDiff, boolean remove) {

        FormatToken token = tokens.get(index);
        assert token.isVirtual();

        CodeStyle.WrapStyle style = getLineWrap(tokens, index, formatContext);
        if (style == CodeStyle.WrapStyle.WRAP_ALWAYS) {
            return offsetDiff;
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
                return offsetDiff;
            }
        }
        //XXX
        FormatToken start = getNextNonVirtual(token);

        if (start != null && end != null) {
            if (start.getKind() != FormatToken.Kind.WHITESPACE
                    && start.getKind() != FormatToken.Kind.EOL) {
                if (!remove) {
                    return formatContext.insert(start.getOffset(), " ", offsetDiff); // NOI18N
                }
            // TODO is this right ? not consistent with java and php
            } else if (!containsEol || style == CodeStyle.WrapStyle.WRAP_NEVER) {
                if (remove) {
                    return formatContext.remove(start.getOffset(),
                            end.getOffset()- start.getOffset(), offsetDiff);
                } else if (start.getText().length() != 1 || start.getKind() == FormatToken.Kind.EOL) {
                    return formatContext.replace(start.getOffset(),
                            start.getText().toString(), " ", offsetDiff); // NOI18N
                }
            }
        }

        return offsetDiff;
    }

    private int handleSpaceBefore(List<FormatToken> tokens, int index,
            FormatContext formatContext, int offsetDiff, boolean remove) {

        FormatToken token = tokens.get(index);
        assert token.isVirtual();

        CodeStyle.WrapStyle style = getLineWrap(tokens, index, formatContext);
        if (style == CodeStyle.WrapStyle.WRAP_ALWAYS) {
            return offsetDiff;
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
                    return formatContext.insert(start.getOffset(), " ", offsetDiff); // NOI18N
                }
            // TODO is this right ? not consistent with java and php
            } else if (!containsEol || style == CodeStyle.WrapStyle.WRAP_NEVER) {
                if (remove) {
                    return formatContext.remove(start.getOffset(),
                            theToken.getOffset() - start.getOffset(), offsetDiff);
                } else if (start.getText().length() != 1 || start.getKind() == FormatToken.Kind.EOL) {
                    return formatContext.replace(start.getOffset(),
                            start.getText().toString(), " ", offsetDiff); // NOI18N
                }
            }
        }
        return offsetDiff;
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

    private int updateIndentationLevel(FormatToken token, int indentationLevel) {
        switch (token.getKind()) {
            case ELSE_IF_INDENTATION_INC:
                if (ELSE_IF_SINGLE_LINE) {
                    break;
                }
            case INDENTATION_INC:
                return indentationLevel + 1;
            case ELSE_IF_INDENTATION_DEC:
                if (ELSE_IF_SINGLE_LINE) {
                    break;
                }
            case INDENTATION_DEC:
                return indentationLevel - 1;
        }
        return indentationLevel;
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
            return CodeStyle.WrapStyle.WRAP_ALWAYS;
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

    private static FormatToken getNextNonVirtual(FormatToken token) {
        FormatToken current = token.next();
        while (current != null && current.isVirtual()) {
            current = current.next();
        }
        return current;
    }

    private static FormatToken getPreviousNonVirtual(FormatToken token) {
        FormatToken current = token.previous();
        while (current != null && current.isVirtual()) {
            current = current.previous();
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
