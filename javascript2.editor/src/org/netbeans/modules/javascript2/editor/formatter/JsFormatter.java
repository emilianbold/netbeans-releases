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
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
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

    private static final Pattern SAFE_DELETE_PATTERN = Pattern.compile("\\s*"); // NOI18N

    @Override
    public int hangingIndentSize() {
        return -1; // Use IDE defaults
    }

    @Override
    public int indentSize() {
        return -1; // Use IDE defaults
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
                FormatTokenStream tokenStream = FormatTokenStream.create(LexUtilities.getJsTokenSequence(compilationInfo.getSnapshot()),
                        context.startOffset(), context.endOffset());
                FormatVisitor visitor = new FormatVisitor(tokenStream,
                        LexUtilities.getJsTokenSequence(compilationInfo.getSnapshot()));

                FunctionNode root = ((JsParserResult) compilationInfo).getRoot();
                if (root != null) {
                    root.accept(visitor);
                }

                int offsetDiff = 0;
                int indentationLevel = 0;

                List<FormatToken> tokens = tokenStream.getTokens();
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (FormatToken token : tokens) {
                        LOGGER.log(Level.FINE, token.toString());
                    }
                }

                for (int i = 0; i < tokens.size(); i++) {
                    FormatToken token = tokens.get(i);

                    switch (token.getKind()) {
                        case SOURCE_START:
                        case EOL:
                            // remove trailing spaces
                            FormatToken start = null;
                            for (int j = i - 1; j >= 0; j--) {
                                FormatToken nextToken = tokens.get(j);
                                if (!nextToken.isVirtual()
                                        && nextToken.getKind() != FormatToken.Kind.WHITESPACE) {
                                    break;
                                } else {
                                    start = tokens.get(j);
                                }
                            }
                            while (start != null
                                    && start.getKind() != FormatToken.Kind.EOL) {
                                if (!start.isVirtual()) {
                                    offsetDiff = remove(doc, start.getOffset(),
                                            start.getText().length(), offsetDiff);
                                }
                                start = start.next();
                            }
                            // do indentation
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
                            if (indentationEnd != null && indentationEnd.getKind() != FormatToken.Kind.EOL) {
                                int indentationSize = indentationLevel * IndentUtils.indentLevelSize(doc);
                                if (isContinuation(tokens, index)) {
                                    indentationSize += 4; // FIXME
                                }
                                offsetDiff = replace(doc, indentationStart.getOffset(),
                                        current.toString(), IndentUtils.createIndentString(doc, indentationSize),
                                        offsetDiff);
                            }
                            break;
                    }

                    indentationLevel = updateIndentationLevel(token, indentationLevel);
                }
            }
        });
    }

    public boolean isContinuation(List<FormatToken> tokens, int index) {
        FormatToken token = tokens.get(index);
        if (token.getKind() == FormatToken.Kind.SOURCE_START) {
            return false;
        }
        FormatToken next = token.next();
        if (next.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || next.getKind() == FormatToken.Kind.AFTER_PROPERTY) {
            return false;
        }

        FormatToken result = null;
        for (int i = index - 1; i >= 0; i--) {
            FormatToken previous = tokens.get(i);
            FormatToken.Kind kind = previous.getKind();
            if (kind == FormatToken.Kind.SOURCE_START
                    || kind == FormatToken.Kind.TEXT
                    || kind == FormatToken.Kind.AFTER_STATEMENT
                    || kind == FormatToken.Kind.AFTER_PROPERTY) {
                result = previous;
                break;
            }
        }
        if (result == null
                || result.getKind() == FormatToken.Kind.SOURCE_START
                || result.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || result.getKind() == FormatToken.Kind.AFTER_PROPERTY) {
            return false;
        }
        String text = result.getText().toString();
        return !(JsTokenId.BRACKET_LEFT_CURLY.fixedText().equals(text)
                || JsTokenId.BRACKET_RIGHT_CURLY.fixedText().equals(text)
                // this is just safeguard literal offsets should be fixed
                || JsTokenId.OPERATOR_SEMICOLON.fixedText().equals(text));

    }

    private int updateIndentationLevel(FormatToken token, int indentationLevel) {
        switch (token.getKind()) {
            case INDENTATION_INC:
                return indentationLevel + 1;
            case INDENTATION_DEC:
                return indentationLevel - 1;
        }
        return indentationLevel;
    }

    private int replace(BaseDocument doc, int offset, String oldString, String newString, int offsetDiff) {
        if (oldString.equals(newString)) {
            return offsetDiff;
        }

        try {
            assert SAFE_DELETE_PATTERN.matcher(oldString).matches()
                    : oldString;
            doc.remove(offset + offsetDiff, oldString.length());
            doc.insertString(offset + offsetDiff, newString, null);
            return offsetDiff + (newString.length() - oldString.length());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return offsetDiff;
    }

    private int remove(BaseDocument doc, int offset, int length, int offsetDiff) {
        try {
            assert SAFE_DELETE_PATTERN.matcher(doc.getText(offset + offsetDiff, length)).matches()
                    : doc.getText(offset + offsetDiff, length);
            doc.remove(offset + offsetDiff, length);
            return offsetDiff - length;
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return offsetDiff;
    }

    @Override
    public void reindent(Context context) {
        // TODO
    }

}
