/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.editor.indent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import javax.swing.text.Position;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * Formatting and indentation for PHP
 * @author Tor Norbye
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPFormatter implements Formatter {

    private static final Logger LOG = Logger.getLogger(PHPFormatter.class.getName());
    private static final Set<PHPTokenId> IGNORE_BREAK_IN = new HashSet<PHPTokenId>(Arrays.asList(
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_DO));

    public PHPFormatter() {
        LOG.fine("PHP Formatter: " + this); //NOI18N
    }

    public boolean needsParserResult() {
        return true;
    }

    public void reindent(Context context) {
        // Make sure we're not reindenting HTML content

        // hotfix for for broken new line indentation after merging with dkonecny's changes
        String mimeType = getMimeTypeAtOffset(context.document(), context.startOffset());

        if (!PHPLanguage.PHP_MIME_TYPE.equals(mimeType)){
            return;
        }
        // end of hotfix

        reindent(context, null, true);
    }

    public void reformat(Context context, ParserResult info) {
        LOG.log(Level.FINE, "PHPFormatter snapshot: \n''{0}''\n", info.getSnapshot().getText().toString()); //NOI18N

        Map<Position, Integer> indentLevels = new LinkedHashMap<Position, Integer>();
        IndentLevelCalculator indentCalc = new IndentLevelCalculator(context.document(), indentLevels);
        PHPParseResult phpParseResult = ((PHPParseResult) info);
        phpParseResult.getProgram().accept(indentCalc);
        
        prettyPrint(context, info);
        astReformat(context, indentLevels);
    }

    public int indentSize() {
        return CodeStyle.get((Document) null).getIndentSize();
    }

    public int hangingIndentSize() {
        return CodeStyle.get((Document) null).getContinuationIndentSize();
    }

    /** Compute the initial balance of brackets at the given offset. */
    private int getFormatStableStart(BaseDocument doc, int offset) {
        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return 0;
        }

        ts.move(offset);

        if (!ts.movePrevious()) {
            return 0;
        }

        // Look backwards to find a suitable context - a class, module or method definition
        // which we will assume is properly indented and balanced
        do {
            Token<?extends PHPTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == PHPTokenId.PHP_OPENTAG || id == PHPTokenId.PHP_CLASS || id == PHPTokenId.PHP_FUNCTION) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        return ts.offset();
    }

    public static int getTokenBalanceDelta(BaseDocument doc, Token<? extends PHPTokenId> token, TokenSequence<? extends PHPTokenId> ts, boolean includeKeywords) {
        if (token.id() == PHPTokenId.PHP_VARIABLE) {
            // In some cases, the [ shows up as an identifier, for example in this expression:
            //  for k, v in sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
            if (token.length() == 1) {
                char c = token.text().charAt(0);
                if (c == '[') {
                    return 1;
                } else if (c == ']') {
                    // I've seen "]" come instead of a RBRACKET too - for example in RHTML:
                    // <%if session[:user]%>
                    return -1;
                }
            }
        } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
            return 1;
        } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
            return -1;
        } else if (token.id() == PHPTokenId.PHP_CASE || token.id() == PHPTokenId.PHP_DEFAULT) {
            return 1;
        } else if (token.id() == PHPTokenId.PHP_BREAK) {
            return getIndentAfterBreak(doc, ts);
        } else if (token.id() == PHPTokenId.PHP_TOKEN) {
            if (LexUtilities.textEquals(token.text(), '(') || LexUtilities.textEquals(token.text(), '[')) {
                return 1;
            } else if (LexUtilities.textEquals(token.text(), ')') || LexUtilities.textEquals(token.text(), ']')) {
                return -1;
            }
        } else if (includeKeywords) {
            if (LexUtilities.isIndentBeginToken(token.id())) {
                return 1;
            } else if (LexUtilities.isIndentEndToken(token.id())) {
                return -1;
            }
        }

        return 0;
    }

    // return indent if we are not in switch/case, otherwise return 0
    private static int getIndentAfterBreak(BaseDocument doc, TokenSequence<? extends PHPTokenId> ts) {
        // we are inside a block
        final int index = ts.index();
        final int breakOffset = ts.offset();
        int indent = 0;
        int balance = 0;
        while (ts.movePrevious()) {
            Token<? extends PHPTokenId> token = ts.token();
            if (token.id() == PHPTokenId.PHP_CURLY_OPEN || LexUtilities.textEquals(token.text(), '(') || LexUtilities.textEquals(token.text(), '[')) {
                // out of the block
                balance--;
            } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE || LexUtilities.textEquals(token.text(), ')') || LexUtilities.textEquals(token.text(), ']')) {
                // some block => ignore it
                balance++;
            } else if (balance == -1 && IGNORE_BREAK_IN.contains(token.id())) {
                // out of the block
                indent = 0;
                break;
            } else if (balance == 0 && token.id() == PHPTokenId.PHP_CASE) {
                // in the same block
                CodeStyle codeStyle = CodeStyle.get(doc);
                int tplIndentSize = codeStyle.getIndentSize();
                if (tplIndentSize > 0) {
                    try {
                        int caseIndent = Utilities.getRowIndent(doc, ts.offset());
                        int breakIndent = Utilities.getRowIndent(doc, breakOffset);
                        indent = (caseIndent - breakIndent) / tplIndentSize;
                    } catch (BadLocationException ignored) {
                        LOG.log(Level.FINE, "Incorrect offset?!", ignored);
                    }
                }
                break;
            }
        }
        // return to the original token
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    // TODO RHTML - there can be many discontiguous sections, I've gotta process all of them on the given line
    public static int getTokenBalance(BaseDocument doc, int begin, int end, boolean includeKeywords) {
        int balance = 0;

        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, begin);
        if (ts == null) {
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        do {
            Token<?extends PHPTokenId> token = ts.token();
            balance += getTokenBalanceDelta(doc, token, ts, includeKeywords);
        } while (ts.moveNext() && (ts.offset() < end));

        return balance;
    }

    private boolean lineUnformattable(BaseDocument doc, int offset) throws BadLocationException {
        // TODO: Handle arrays better
        // %w(January February March April May June July
        //    August September October November December)
        // I should indent to the same level

        // Can't reformat these at the moment because reindenting a line
        // that is a continued string array causes incremental lexing errors
        // (which further screw up formatting)
        int pos = Utilities.getRowFirstNonWhite(doc, offset);
        //int pos = offset;

        if (pos != -1) {
            // I can't look at the first position on the line, since
            // for a string array that is indented, the indentation portion
            // is recorded as a blank identifier
            Token<? extends PHPTokenId> token = LexUtilities.getToken(doc, pos);

            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPositionedSequence(doc, pos);

            if (ts != null) {
                token = ts.token();
            }

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if (id == PHPTokenId.PHP_COMMENT || id == PHPTokenId.PHP_COMMENT_START || id == PHPTokenId.PHP_COMMENT_END ||
                    id == PHPTokenId.PHPDOC_COMMENT || id == PHPTokenId.PHPDOC_COMMENT_START || id == PHPTokenId.PHPDOC_COMMENT_END ||
                    id == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE ||

// TODO: please review!! without this line PHP formatter clobers
// all indentation done by HTML formatter:
                    id == PHPTokenId.T_INLINE_HTML ||

                    id == PHPTokenId.PHP_HEREDOC_TAG
                ) {
                    // No indentation for literal strings in Ruby, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }

                if (id == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING){
                    int startLine = Utilities.getLineOffset(doc, ts.offset());
                    int currentLine = Utilities.getLineOffset(doc, pos);

                    if (startLine < currentLine){
                        // multiline string
                        return true;
                    }
                }
// XXX: resurrect support for heredoc
//                if (id == PHPTokenId.STRING_END || id == PHPTokenId.QUOTED_STRING_END) {
//                    // Possibly a heredoc
//                    TokenSequence<? extends PHPTokenId> ts = LexUtilities.getRubyTokenSequence(doc, pos);
//                    ts.move(pos);
//                    OffsetRange range = LexUtilities.findHeredocBegin(ts, token);
//                    if (range != OffsetRange.NONE) {
//                        String text = doc.getText(range.getStart(), range.getLength());
//                        if (text.startsWith("<<-")) { // NOI18N
//                            return false;
//                        } else {
//                            return true;
//                        }
//                    }
//                }
            } else {
                // No PHP token -- leave the formatting alone!
                return true;
            }
        } else {
            // Empty line inside a string, documentation etc. literal?
            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, offset);

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if (id == PHPTokenId.PHP_COMMENT || id == PHPTokenId.PHP_COMMENT_START || id == PHPTokenId.PHP_COMMENT_END ||
                    id == PHPTokenId.PHPDOC_COMMENT || id == PHPTokenId.PHPDOC_COMMENT_START || id == PHPTokenId.PHPDOC_COMMENT_END ||
                    id == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING ||
                    id == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE ||
                    id == PHPTokenId.PHP_HEREDOC_TAG
                ) {
                    // No indentation for literal strings in Ruby, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the first token on the given line. Similar to LexUtilities.getToken(doc, lineBegin)
     * except (a) it computes the line begin from the offset itself, and more importantly,
     * (b) it handles RHTML tokens specially; e.g. if a line begins with
     * {@code
     *    <% if %>
     * }
     * then the "if" embedded token will be returned rather than the RHTML delimiter, or even
     * the whitespace token (which is the first Ruby token in the embedded sequence).
     *
     * </pre>
     */
    private Token<? extends PHPTokenId> getFirstToken(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            return LexUtilities.getToken(doc, lineBegin);
        }

        return null;
    }

    private boolean isEndIndent(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            Token<?extends PHPTokenId> token = getFirstToken(doc, offset);

            if (token == null) {
                return false;
            }

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            return LexUtilities.isIndentEndToken(token.id()) ||
                LexUtilities.textEquals(token.text(), ')') || LexUtilities.textEquals(token.text(), ']') ||
                token.id() == PHPTokenId.PHP_CURLY_CLOSE;
        }

        return false;
    }

    private boolean isLineContinued(BaseDocument doc, int offset, int bracketBalance) throws BadLocationException {
        offset = Utilities.getRowLastNonWhite(doc, offset);
        if (offset == -1) {
            return false;
        }

        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return false;
        }

        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends PHPTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            if (ts.offset() == offset && token.length() > 1 && token.text().toString().startsWith("\\")) {
                // Continued lines have different token types
                return true;
            }

            if (token.length() == 1 && id == PHPTokenId.PHP_TOKEN && token.text().toString().equals(",")) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                if (bracketBalance == 0) {
                    return true;
                }
            }

            if (id == PHPTokenId.PHP_TOKEN) {
                if (CharSequenceUtilities.textEquals(token.text(), "or") // NOI18N
                    || CharSequenceUtilities.textEquals(token.text(), "and")
                ) {
                    return true;
                }
            }
        }

        return false;
    }

 private void prettyPrint(final Context context, final ParserResult info) {
        final BaseDocument doc = (BaseDocument) context.document();
        final String openingBraceStyle = CodeStyle.get(doc).getOpeningBraceStyle();

        if (FmtOptions.OBRACE_PRESERVE.equals(openingBraceStyle)){
            return;
        }

        doc.runAtomic(new Runnable() {

            public void run() {
                final WSTransformer wsTransformer = new WSTransformer(context);
                PHPParseResult result = (PHPParseResult) info;
                result.getProgram().accept(wsTransformer);
               
                for (WSTransformer.Replacement replacement : wsTransformer.getReplacements()){
                    if (replacement.offset() < context.startOffset()
                            || replacement.offset() > context.endOffset()){
                        continue;
                    }

                    try {
                        doc.insertString(replacement.offset(), replacement.newString(), null);

                        if (replacement.length() > 0){
                            doc.remove(replacement.offset() - replacement.length(), replacement.length());
                        }

                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }
    private void reindent(final Context context, ParserResult info, final boolean indentOnly) {
        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

//        System.out.println("~~~ PHP Formatter: " + (indentOnly ? "renidenting" : "reformatting")
//                + " <" + startOffset + ", " + endOffset + ">");

        // a workaround for issue #131929
        document.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE); //NOI18N

        try {
            final BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())

            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }

            startOffset = Utilities.getRowStart(doc, startOffset);
            final int lineStart = startOffset;//Utilities.getRowStart(doc, startOffset);
            int initialOffset = 0;
            int initialIndent = 0;
            if (startOffset > 0) {
                int prevOffset = Utilities.getRowStart(doc, startOffset-1);
                initialOffset = getFormatStableStart(doc, prevOffset);
                initialIndent = GsfUtilities.getLineIndent(doc, initialOffset);
            }

//            System.out.println("~~~ initialIndent=" + initialIndent + ", initialOffset=" + initialOffset + ", startOffset=" + startOffset);

            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.
            final List<Integer> offsets = new ArrayList<Integer>();
            final List<Integer> indents = new ArrayList<Integer>();

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset != doc.getLength());

            boolean includeEnd = endOffset == doc.getLength() || indentOnly;

            // TODO - remove initialbalance etc.
            computeIndents(doc, initialIndent, initialOffset, endOffset, info,
                    offsets, indents, indentEmptyLines, includeEnd, indentOnly);

//            System.out.println("~~~ indents=" + indents.size() + ", offsets=" + offsets.size());

            doc.runAtomic(new Runnable() {
                public void run() {
                    try {
                        // Iterate in reverse order such that offsets are not affected by our edits
                        assert indents.size() == offsets.size();
                        for (int i = indents.size() - 1; i >= 0; i--) {
                            int indent = indents.get(i);
                            int lineBegin = offsets.get(i);

        //                    System.out.println("~~~ [" + i + "]: indent=" + indent + ", offset=" + lineBegin);

                            if (lineBegin < lineStart) {
                                // We're now outside the region that the user wanted reformatting;
                                // these offsets were computed to get the correct continuation context etc.
                                // for the formatter
        //                        System.out.println("~~~ END ?! lineBegin=" + lineBegin + " < lineStart=" + lineStart);
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
                                int prevOffset = offsets.get(i-1);
                                int prevIndent = indents.get(i-1);
                                int actualPrevIndent = GsfUtilities.getLineIndent(doc, prevOffset);
                                if (actualPrevIndent != prevIndent) {
                                    // For blank lines, indentation may be 0, so don't adjust in that case
                                    if (indentOnly || !(Utilities.isRowEmpty(doc, prevOffset) || Utilities.isRowWhite(doc, prevOffset))) {
                                        indent = actualPrevIndent + (indent-prevIndent);
                                    }
                                }
                            }

                            // Adjust the indent at the given line (specified by offset) to the given indent
                            int currentIndent = GsfUtilities.getLineIndent(doc, lineBegin);

        //                    System.out.println("~~~ [" + i + "]: currentIndent=" + currentIndent + ", indent=" + indent);

                            if (currentIndent != indent && indent >= 0) {
                                context.modifyIndent(lineBegin, indent);
                            }
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            });
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    public void computeIndents(BaseDocument doc, int initialIndent, int startOffset, int endOffset, ParserResult info,
            List<Integer> offsets,
            List<Integer> indents,
            boolean indentEmptyLines, boolean includeEnd, boolean indentOnly
        ) {
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

            CodeStyle codeStyle = CodeStyle.get(doc);
            int iSize = codeStyle.getIndentSize();
            int hiSize = codeStyle.getContinuationIndentSize();

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

            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line

                int hangingIndent = continued ? (hiSize) : 0;

                /**
                 * As most of this code was copy&pasted from JavaScript/Ruby formatter
                 * I'm copy pasting also intialization of initialIndent as done in Ruby
                 * formatter.
                 * TODO: PHP does not seem to have isEmbeddedDoc. is that OK?
                 */
                if (/*isEmbeddedDoc &&*/ !indentOnly) {
                    // Pick up the indentation level assigned by the HTML indenter; gets HTML structure
                    Map<Integer, Integer> suggestedLineIndents = (Map<Integer, Integer>)doc.getProperty("AbstractIndenter.lineIndents");
                    if (suggestedLineIndents != null) {
                        Integer ind = suggestedLineIndents.get(Utilities.getLineOffset(doc, offset));
                        if (ind != null) {
                            initialIndent = ind.intValue();
                        } else {
                            initialIndent = 0; //getOffsetFromPrevLine(doc, offset);
                        }
                    } else {
                        initialIndent = 0; //getOffsetFromPrevLine(doc, offset);
                    }
                }

                if (lineUnformattable(doc, offset)) {
                    // Skip this line - leave formatting as it is prior to reformatting
                    indent = GsfUtilities.getLineIndent(doc, offset);
                } else if (isEndIndent(doc, offset)) {
                    indent = (balance-1) * iSize + hangingIndent + initialIndent;
                } else {
                    indent = balance * iSize + hangingIndent + initialIndent;
                }

                if (indent < 0) {
                    indent = 0;
                }

                int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

                // Insert whitespace on empty lines too -- needed for abbreviations expansion
                if (lineBegin != -1 || indentEmptyLines) {
                    // Don't do a hanging indent if we're already indenting beyond the parent level?

                    indents.add(Integer.valueOf(indent));
                    offsets.add(Integer.valueOf(offset));
                }

                int endOfLine = Utilities.getRowEnd(doc, offset) + 1;

                if (lineBegin != -1) {
                    balance += getTokenBalance(doc, lineBegin, endOfLine, true);
                    bracketBalance += getTokenBalance(doc, lineBegin, endOfLine, false);
                    continued = isLineContinued(doc, offset, bracketBalance);
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private static String getMimeTypeAtOffset(Document doc, int offset){
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsl = th.embeddedTokenSequences(offset, false);
        if (tsl != null && tsl.size() > 0) {
            TokenSequence<?> tokenSequence = tsl.get(tsl.size() - 1);
            return tokenSequence.language().mimeType();
        }

        return null;
    }

    private void astReformat(final Context context, final Map<Position, Integer> indentLevels) {
        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();
        document.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE); //NOI18N

        try {
            final BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())
            final Map<Integer, Integer> suggestedLineIndents = (Map<Integer, Integer>)doc.getProperty("AbstractIndenter.lineIndents");

            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }

            startOffset = Utilities.getRowStart(doc, startOffset);
            final int firstLine = Utilities.getLineOffset(doc, startOffset);
            
            doc.runAtomic(new Runnable() {

                public void run() {
                    int indentBias = 0;
                    try {
                        int numberOfLines = Utilities.getLineOffset(doc, doc.getLength() - 1);
                        Map<Integer, Integer> indentDeltaByLine = new LinkedHashMap<Integer, Integer>();

                        for (Position pos : indentLevels.keySet()) {
                            int indentDelta = indentLevels.get(pos);
                            int point = pos.getOffset();
                            int lineNumber = Utilities.getLineOffset(doc, point);
                            int rowStart = Utilities.getRowStart(doc, point);
                            int firstNonWSBefore = Utilities.getFirstNonWhiteBwd(doc, point);

                            if (firstNonWSBefore >= rowStart){
                                lineNumber ++;
                            }

                            Integer lineDelta = indentDeltaByLine.get(lineNumber);
                            indentDeltaByLine.put(lineNumber, lineDelta == null
                                    ? indentDelta : lineDelta + indentDelta);
                        }

                        for (int i = 0, currentIndent = 0; i < numberOfLines; i++) {
                            int lineStart = Utilities.getRowStartFromLineOffset(doc, i);
                            Integer lineDelta = indentDeltaByLine.get(i);

                            if (lineDelta != null) {
                                currentIndent += lineDelta;
                                assert currentIndent >= 0 : "currentIndent < 0";
                            }

                            if (!lineUnformattable(doc, lineStart)) {
                                int htmlSuggestion = 0;

                                if (suggestedLineIndents != null) {
                                    Integer rawSuggestion = suggestedLineIndents.get(i);
                                    if (rawSuggestion != null) {
                                        htmlSuggestion = rawSuggestion.intValue();
                                    }
                                }

                                if (i == firstLine) {
                                    indentBias = currentIndent - GsfUtilities.getLineIndent(doc, lineStart) - htmlSuggestion;
                                }

//                                System.err.println("lineDelta[" + i + "]=" + lineDelta);
//                                System.err.println("htmlSuggestion[" + i + "]=" + htmlSuggestion);
                                //TODO:
                                if (lineStart >= context.startOffset() && lineStart <= context.endOffset()) {
                                    int actualIndent = 0;

                                    if (currentIndent + htmlSuggestion > indentBias) {
                                        actualIndent = currentIndent + htmlSuggestion - indentBias;
                                    }

                                    GsfUtilities.setLineIndentation(doc, lineStart, actualIndent);
                                }
                            }
                        }

                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
}
