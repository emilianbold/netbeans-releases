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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * Formatting and indentation for PHP
 *
 * @author Tor Norbye
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPFormatter2 implements org.netbeans.modules.gsf.api.Formatter {

    private static final Logger LOG = Logger.getLogger(PHPFormatter2.class.getName());
    private static final Set<PHPTokenId> IGNORE_BREAK_IN = new HashSet<PHPTokenId>(Arrays.asList(
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_DO));
    
    public PHPFormatter2() {
        LOG.fine("PHP Formatter: " + this); //NOI18N
    }
    
    public boolean needsParserResult() {
        return false;
    }

    public void reindent(Context context) {
        // Make sure we're not reindenting HTML content
        reindent(context, null, true);
    }

    public void reformat(Context context, CompilationInfo info) {
        prettyPrint(context);
        reindent(context, info, false);
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

    private boolean isInLiteral(BaseDocument doc, int offset) throws BadLocationException {
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
            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, pos);

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
                // No ruby token -- leave the formatting alone!
                // (Probably in an RHTML file on a line with no Ruby)
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

    private void prettyPrint(final Context context) {
        final BaseDocument doc = (BaseDocument) context.document();
        final String openingBraceStyle = CodeStyle.get(doc).getOpeningBraceStyle();

        if (FmtOptions.OBRACE_PRESERVE.equals(openingBraceStyle)){
            return;
        }

        doc.runAtomic(new Runnable() {

            public void run() {
                final WSTransformer wsTransformer = new WSTransformer(context);
                FileObject file = NavUtils.getFile(doc);
                try {
                    SourceModelFactory.getInstance().getModel(file).runUserActionTask(new CancellableTask<CompilationInfo>() {

                        public void cancel() {}

                        public void run(CompilationInfo parameter) throws Exception {
                            PHPParseResult result = (PHPParseResult) parameter.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);
                            result.getProgram().accept(wsTransformer);
                        }
                    }, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

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

    private void reindent(final Context context, CompilationInfo info, final boolean indentOnly) {
        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();
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
            

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset != doc.getLength());

            boolean includeEnd = endOffset == doc.getLength() || indentOnly;


            final Map<Integer, Integer> indentLevels = new LinkedHashMap<Integer, Integer>();
            final IndentLevelCalculator indentCalc = new IndentLevelCalculator(doc, indentLevels);
            FileObject file = NavUtils.getFile(doc);
            try {
                SourceModelFactory.getInstance().getModel(file).runUserActionTask(new CancellableTask<CompilationInfo>() {

                    public void cancel() {
                    }

                    public void run(CompilationInfo parameter) throws Exception {
                        PHPParseResult result = (PHPParseResult) parameter.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);
                        result.getProgram().accept(indentCalc);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        int numberOfLines = Utilities.getLineOffset(doc, doc.getLength() - 1);
                        Map<Integer, Integer> indentDeltaByLine = new LinkedHashMap<Integer, Integer>();

                        for (int point : indentLevels.keySet()) {
                            int indentDelta = indentLevels.get(point);
                            int lineNumber = Utilities.getLineOffset(doc, point);
                            Integer lineDelta = indentDeltaByLine.get(lineNumber);

                            indentDeltaByLine.put(lineNumber, lineDelta == null
                                    ? indentDelta : lineDelta + indentDelta);
                        }

                        for (int i = 1, currentIndent = 0; i < numberOfLines; i++) {
                            Integer lineDelta = indentDeltaByLine.get(i);

                            if (lineDelta != null) {
                                currentIndent += lineDelta;
                                assert currentIndent >= 0;
                            }

                            int lineStart = Utilities.getRowStartFromLineOffset(doc, i);
                            GsfUtilities.setLineIndentation(doc, lineStart, currentIndent);
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
