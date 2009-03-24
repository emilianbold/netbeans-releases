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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
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
public class PHPFormatter2 implements org.netbeans.modules.csl.api.Formatter {

    private static final Logger LOG = Logger.getLogger(PHPFormatter2.class.getName());
    private static final Set<PHPTokenId> IGNORE_BREAK_IN = new HashSet<PHPTokenId>(Arrays.asList(
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_DO));

    public PHPFormatter2() {
        LOG.fine("PHP Formatter: " + this); //NOI18N
    }

    public boolean needsParserResult() {
        return true;
    }

    public void reindent(Context context) {
        // Make sure we're not reindenting HTML content
        reindent(context, null, true);
    }

    public void reformat(Context context, ParserResult info) {
        prettyPrint(context);
        reindent(context, info, false);
    }

    public int indentSize() {
        return CodeStyle.get((Document) null).getIndentSize();
    }

    public int hangingIndentSize() {
        return CodeStyle.get((Document) null).getContinuationIndentSize();
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
                Source source = Source.create(file);
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        public void run(ResultIterator parameter) throws Exception {
                            PHPParseResult result = (PHPParseResult) parameter.getParserResult();
                            result.getProgram().accept(wsTransformer);
                        }
                    });
                } catch (ParseException ex) {
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

    private void reindent(final Context context, ParserResult info, final boolean indentOnly) {
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
            final int firstLine = Utilities.getLineOffset(doc, startOffset);
            final Map<Integer, Integer> indentLevels = new LinkedHashMap<Integer, Integer>();
            final IndentLevelCalculator indentCalc = new IndentLevelCalculator(doc, indentLevels);
            FileObject file = NavUtils.getFile(doc);
            Source source = Source.create(file);
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    public void run(ResultIterator parameter) throws Exception {
                        PHPParseResult result = (PHPParseResult) parameter.getParserResult();
                        result.getProgram().accept(indentCalc);
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

            doc.runAtomic(new Runnable() {

                public void run() {
                    int indentBias = 0;
                    try {
                        int numberOfLines = Utilities.getLineOffset(doc, doc.getLength() - 1);
                        Map<Integer, Integer> indentDeltaByLine = new LinkedHashMap<Integer, Integer>();

                        for (int point : indentLevels.keySet()) {
                            int indentDelta = indentLevels.get(point);
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
                            System.err.println("lineDelta[" + i + "]=" + lineDelta);

                            if (lineDelta != null) {
                                currentIndent += lineDelta;
                                assert currentIndent >= 0;
                            }

                            if (i == firstLine){
                                // TODO: do it also if there was HTML block in the middle
                                // and this is the first line after the HTML
                                indentBias = currentIndent - GsfUtilities.getLineIndent(doc, lineStart);
                            }

                            //TODO:
                            if (lineStart >= context.startOffset() && lineStart <= context.endOffset()){
                                int actualIndent = 0;

                                if (currentIndent > indentBias){
                                    actualIndent = currentIndent - indentBias;
                                }

                                GsfUtilities.setLineIndentation(doc, lineStart, actualIndent);
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
