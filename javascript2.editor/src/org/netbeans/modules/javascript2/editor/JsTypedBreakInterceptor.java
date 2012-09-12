/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.javascript2.editor.doc.JsDocumentationCompleter;
import org.netbeans.modules.javascript2.editor.lexer.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JsTypedBreakInterceptor implements TypedBreakInterceptor {

    /**
     * When true, continue comments if you press return in a line comment
     * (that does not also have code on the same line).
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("js.cont.comment"); // NOI18N

    private CommentGenerator commentGenerator = null;

    public boolean isInsertMatchingEnabled(BaseDocument doc) {
        // The editor options code is calling methods on BaseOptions instead of looking in the settings map :(
        // Boolean b = ((Boolean) Settings.getValue(doc.getKitClass(), SettingsNames.PAIR_CHARACTERS_COMPLETION));
        // return b == null || b.booleanValue();
        EditorOptions options = EditorOptions.get(JsTokenId.JAVASCRIPT_MIME_TYPE);
        if (options != null) {
            return options.getMatchBrackets();
        }

        return true;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.getDocument();
        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);
        int offset = context.getCaretOffset();
        boolean insertMatching = isInsertMatchingEnabled(doc);

        int lineBegin = Utilities.getRowStart(doc, offset);
        int lineEnd = Utilities.getRowEnd(doc, offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(tokenHierarchy, offset);

        if (ts == null) {
            return;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }

        Token<? extends JsTokenId> token = ts.token();
        JsTokenId id = token.id();

        // Insert a missing }
        boolean insertRightBrace = isEndMissing(doc, offset);

        if (!id.isError() && insertMatching && insertRightBrace) {
            int indent = GsfUtilities.getLineIndent(doc, offset);

            int afterLastNonWhite = Utilities.getRowLastNonWhite(doc, offset);

            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder();
            int carretOffset = 0;
            if (offset > afterLastNonWhite) {
                sb.append("\n"); // XXX On Windows, do \r\n?
                sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                carretOffset = sb.length();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(offset, Utilities.getRowEnd(doc, afterLastNonWhite)-offset);
                sb.append("\n"); // XXX On Windows, do \r\n?
                sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                carretOffset = sb.length();
                sb.append(restOfLine.trim());
                sb.append("\n");
                sb.append(IndentUtils.createIndentString(doc, indent));
                // FIXME can we avoid this ?
                doc.remove(offset, restOfLine.length());
            }

            if (insertRightBrace) {
                sb.append("}"); // NOI18N
            }

            context.setText(sb.toString(), 0, carretOffset);
            return;
        }

        if (id.isError()) {
            // See if it's a block comment opener
            String text = token.text().toString();
            if (text.startsWith("/*") && ts.offset() == Utilities.getRowFirstNonWhite(doc, offset)) {
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * "); // NOI18N
                int carretOffset = sb.length();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" */"); // NOI18N

                if (text.startsWith("/**")) {
                    // setup comment generator
                    commentGenerator = new CommentGenerator(offset + carretOffset, indent + 1);
                }
                context.setText(sb.toString(), 0, carretOffset);
                return;
            }
        }

        if (id == JsTokenId.STRING ||
                (id == JsTokenId.STRING_END) && offset < ts.offset()+ts.token().length()) {
            // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
            //int indent = GsfUtilities.getLineIndent(doc, offset);
            //int delimiterOffset = id == JsTokenId.STRING_END ? ts.offset() : ts.offset()-1;
            //char delimiter = doc.getText(delimiterOffset,1).charAt(0);
            //doc.insertString(offset, delimiter + " + " + delimiter, null);
            //caret.setDot(offset+3);
            //return offset + 5 + indent;
            String str = (id != JsTokenId.STRING || offset > ts.offset()) ? "\\n\\\n"  : "\\\n";
            context.setText(str, -1, str.length());
            return;
        }



        if (id == JsTokenId.REGEXP ||
                (id == JsTokenId.REGEXP_END) && offset < ts.offset()+ts.token().length()) {
            // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
            //int indent = GsfUtilities.getLineIndent(doc, offset);
            //doc.insertString(offset, "/ + /", null);
            //caret.setDot(offset+3);
            //return offset + 5 + indent;
            String str = (id != JsTokenId.REGEXP || offset > ts.offset()) ? "\\n\\\n"  : "\\\n";
            context.setText(str, -1, str.length());
            return;
        }

        // Special case: since I do hash completion, if you try to type
        //     y = Thread.start {
        //         code here
        //     }
        // you end up with
        //     y = Thread.start {|}
        // If you hit newline at this point, you end up with
        //     y = Thread.start {
        //     |}
        // which is not as helpful as it would be if we were not doing hash-matching
        // (in that case we'd notice the brace imbalance, and insert the closing
        // brace on the line below the insert position, and indent properly.
        // Catch this scenario and handle it properly.
        if ((id == JsTokenId.BRACKET_RIGHT_CURLY || id == JsTokenId.BRACKET_RIGHT_BRACKET) && offset > 0) {
            Token<? extends JsTokenId> prevToken = LexUtilities.getToken(doc, offset - 1);
            if (prevToken != null) {
                JsTokenId prevTokenId = prevToken.id();
                if (id == JsTokenId.BRACKET_RIGHT_CURLY && prevTokenId == JsTokenId.BRACKET_LEFT_CURLY ||
                        id == JsTokenId.BRACKET_RIGHT_BRACKET && prevTokenId == JsTokenId.BRACKET_LEFT_BRACKET) {
                    int indent = GsfUtilities.getLineIndent(doc, offset);
                    StringBuilder sb = new StringBuilder();
                    // XXX On Windows, do \r\n?
                    sb.append("\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                    int carretOffset = sb.length();
                    sb.append("\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, indent));

                    // should we reindent it automatically ?
                    context.setText(sb.toString(), 0, carretOffset);
                    return;
                }
            }
        }

        if (id == JsTokenId.WHITESPACE) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == JsTokenId.LINE_COMMENT) {
                        offset = begin;
                    }
                }
            }
        }

        if ((id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.DOC_COMMENT)
                && offset > ts.offset() && offset < ts.offset()+ts.token().length()) {
            // Continue *'s
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            int end = Utilities.getRowEnd(doc, offset)+1;
            if (begin == -1) {
                begin = end;
            }
            String line = doc.getText(begin, end-begin);
            boolean isBlockStart = line.startsWith("/*") || (begin != -1 && begin < ts.offset());
            if (isBlockStart || line.startsWith("*")) {
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder("\n");
                if (isBlockStart) {
                    indent++;
                }
                int carretPosition = 0;
                sb.append(IndentUtils.createIndentString(doc, indent));
                if (isBlockStart) {
                    // First comment should be propertly indented
                    sb.append("* "); //NOI18N
                    carretPosition = sb.length();

                    TokenSequence<? extends JsDocumentationTokenId> jsDocTS = LexUtilities.getJsDocumentationTokenSequence(tokenHierarchy, offset);
                    if (jsDocTS != null) {
                        if (!endsCommentProperly(jsDocTS)) {
                            // setup comment generator
                            commentGenerator = new CommentGenerator(offset + carretPosition, indent);
                            // append end of the comment
                            sb.append("\n").append(IndentUtils.createIndentString(doc, indent)).append("*/"); //NOI18N
                        }
                    }
                } else {
                    // Copy existing indentation inside the block
                    sb.append("*"); //NOI18N
                    int afterStar = isBlockStart ? begin+2 : begin+1;
                    line = doc.getText(afterStar, Utilities.getRowEnd(doc, afterStar)-afterStar);
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c == ' ' || c == '\t') { //NOI18N
                            sb.append(c);
                        } else {
                            break;
                        }
                    }
                    carretPosition = sb.length();
                }

                if (offset == begin && offset > 0) {
                    context.setText(sb.toString(), -1, sb.length());
                    return;
                }
                context.setText(sb.toString(), -1, carretPosition);
                return;
            }
        }

        boolean isComment = id == JsTokenId.LINE_COMMENT;
        if (id == JsTokenId.EOL) {
            if (ts.movePrevious() && ts.token().id() == JsTokenId.LINE_COMMENT) {
                //ts.moveNext();
                isComment = true;
            }
        }

        if (isComment) {
            // Only do this if the line only contains comments OR if there is content to the right on this line,
            // or if the next line is a comment!

            boolean continueComment = false;
            int begin = Utilities.getRowFirstNonWhite(doc, offset);

            // We should only continue comments if the previous line had a comment
            // (and a comment from the beginning, not a trailing comment)
            boolean previousLineWasComment = false;
            boolean nextLineIsComment = false;
            int rowStart = Utilities.getRowStart(doc, offset);
            if (rowStart > 0) {
                int prevBegin = Utilities.getRowFirstNonWhite(doc, rowStart - 1);
                if (prevBegin != -1) {
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, prevBegin);
                    if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                        previousLineWasComment = true;
                    }
                }
            }
            int rowEnd = Utilities.getRowEnd(doc, offset);
            if (rowEnd < doc.getLength()) {
                int nextBegin = Utilities.getRowFirstNonWhite(doc, rowEnd + 1);
                if (nextBegin != -1) {
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, nextBegin);
                    if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                        nextLineIsComment = true;
                    }
                }
            }

            // See if we have more input on this comment line (to the right
            // of the inserted newline); if so it's a "split" operation on
            // the comment
            if (previousLineWasComment || nextLineIsComment
                    || (offset > ts.offset() && offset < ts.offset() + ts.token().length())) {
                if (ts.offset() + token.length() > offset + 1) {
                    // See if the remaining text is just whitespace
                    String trailing = doc.getText(offset, Utilities.getRowEnd(doc, offset) - offset);
                    if (trailing.trim().length() != 0) {
                        continueComment = true;
                    }
                } else if (CONTINUE_COMMENTS) {
                    // See if the "continue comments" options is turned on, and this is a line that
                    // contains only a comment (after leading whitespace)
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, begin);
                    if (firstToken.id() == JsTokenId.LINE_COMMENT) {
                        continueComment = true;
                    }
                }
                if (!continueComment) {
                    // See if the next line is a comment; if so we want to continue
                    // comments editing the middle of the comment
                    int nextLine = Utilities.getRowEnd(doc, offset) + 1;
                    if (nextLine < doc.getLength()) {
                        int nextLineFirst = Utilities.getRowFirstNonWhite(doc, nextLine);
                        if (nextLineFirst != -1) {
                            Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, nextLineFirst);
                            if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                                continueComment = true;
                            }
                        }
                    }
                }
            }

            if (continueComment) {
                // Line comments should continue
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                if (offset != begin || offset <= 0) {
                    sb.append("\n");
                }
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append("//"); // NOI18N
                // Copy existing indentation
                int afterSlash = begin + 2;
                String line = doc.getText(afterSlash, Utilities.getRowEnd(doc, afterSlash) - afterSlash);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ' ' || c == '\t') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }

                if (offset == begin && offset > 0) {
                    int caretPosition = sb.length();
                    sb.append("\n");
                    context.setText(sb.toString(), -1, caretPosition);
                    return;
                }
                context.setText(sb.toString(), -1, sb.length());
                return;
            }
        }
        
        // Just indent the line properly
        int indentSize = getNextLineIndentation(doc, offset);
        if (indentSize > 0) {
            StringBuilder sb = new StringBuilder("\n"); // NOI18N
            sb.append(IndentUtils.createIndentString(doc, indentSize));
            context.setText(sb.toString(), -1, sb.length());
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        if (commentGenerator != null) {
            JsDocumentationCompleter.generateCompleteComment(
                    (BaseDocument) context.getDocument(),
                    commentGenerator.getOffset(),
                    commentGenerator.getIndent());
            commentGenerator = null;
        }
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void cancelled(Context context) {
    }

    /**
     * Computes the indentation of the next line (after the line break).
     * 
     * @param doc document
     * @param offset current offset
     * @return indentation size
     * @throws BadLocationException 
     */
    private static int getNextLineIndentation(BaseDocument doc, int offset) throws BadLocationException {
        int indent = GsfUtilities.getLineIndent(doc, offset);
        int currentOffset = offset;
        while (currentOffset > 0) {
            if (!Utilities.isRowEmpty(doc, currentOffset) && !Utilities.isRowWhite(doc, currentOffset)
                    && !LexUtilities.isCommentOnlyLine(doc, currentOffset)) {
                indent = GsfUtilities.getLineIndent(doc, currentOffset);
                int parenBalance = LexUtilities.getLineBalance(doc, currentOffset,
                        JsTokenId.BRACKET_LEFT_PAREN, JsTokenId.BRACKET_RIGHT_PAREN);
                if (parenBalance < 0) {
                    break;
                }
                int curlyBalance = LexUtilities.getLineBalance(doc, currentOffset,
                        JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                if (curlyBalance > 0) {
                    indent += IndentUtils.indentLevelSize(doc);
                }
                return indent;
            }
            currentOffset = Utilities.getRowStart(doc, currentOffset) - 1;
        }

        return indent;
    }

    /**
     * Returns <code>true</code> when ending } is missing.
     * 
     * @param doc document
     * @param offset current offset
     * @return <code>true</code> when ending } is missing
     * @throws BadLocationException 
     */
    private static boolean isEndMissing(BaseDocument doc, int offset) throws BadLocationException {

        // FIXME performance
        int curlyBalance = LexUtilities.getTokenBalance(doc,
                JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY, offset);

        if (curlyBalance <= 0) {
            return false;
        }

        int parenBalance = LexUtilities.getLineBalance(doc, 
                offset, JsTokenId.BRACKET_LEFT_PAREN, JsTokenId.BRACKET_RIGHT_PAREN);

        if ((curlyBalance == 1) && parenBalance >= 0) {
            // There is one more opening token on the line than a corresponding
            // closing token.  (If there's is more than one we don't try to help.)
            return curlyBalance > 0;
        }

        return false;
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
        @MimeRegistration(mimeType = JsDocumentationTokenId.MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    })
    public static class Factory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new JsTypedBreakInterceptor();
        }

    }

    private static boolean endsCommentProperly(TokenSequence ts) {
        while (ts.moveNext()) {
            if (ts.token().id() == JsDocumentationTokenId.EOL) {
                while (ts.moveNext() && ts.token().id() == JsDocumentationTokenId.WHITESPACE);
                if (!CharSequenceUtilities.startsWith(ts.token().text(), "*")) {
                    return false;
                }
            }
        }
        return ts.token().id() == JsDocumentationTokenId.COMMENT_END;
    }

    private static class CommentGenerator {

        private final int offset;
        private final int indent;

        public CommentGenerator(int offset, int indent) {
            this.offset = offset;
            this.indent = indent;
        }

        public int getIndent() {
            return indent;
        }

        public int getOffset() {
            return offset;
        }

    }
}
