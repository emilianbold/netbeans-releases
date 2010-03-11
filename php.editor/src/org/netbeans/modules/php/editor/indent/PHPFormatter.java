/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
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

    @Override
    public boolean needsParserResult() {
        return true;
    }

    @Override
    public void reindent(Context context) {
        // Make sure we're not reindenting HTML content

        // hotfix for for broken new line indentation after merging with dkonecny's changes
        String mimeType = getMimeTypeAtOffset(context.document(), context.startOffset());

        if (!FileUtils.PHP_MIME_TYPE.equals(mimeType)){
            return;
        }
        // end of hotfix

        PHPNewLineIndenter indenter = new PHPNewLineIndenter(context);
        indenter.process();
    }

    @Override
    public void reformat(Context context, ParserResult info) {
        LOG.log(Level.FINE, "PHPFormatter snapshot: \n''{0}''\n", info.getSnapshot().getText().toString()); //NOI18N

        Map<Position, Integer> indentLevels = new LinkedHashMap<Position, Integer>();
        IndentLevelCalculator indentCalc = new IndentLevelCalculator(context.document(), indentLevels);
        PHPParseResult phpParseResult = ((PHPParseResult) info);
        phpParseResult.getProgram().accept(indentCalc);

        prettyPrint(context, info);
        astReformat(context, indentLevels);
    }

    @Override
    public int indentSize() {
        return CodeStyle.get((Document) null).getIndentSize();
    }

    @Override
    public int hangingIndentSize() {
        return CodeStyle.get((Document) null).getContinuationIndentSize();
    }

    private boolean lineUnformattable(BaseDocument doc, int offset) throws BadLocationException {
        Token<? extends PHPTokenId> firstTokenInLine = LexUtilities.getToken(doc, offset);
        if (firstTokenInLine.id() == PHPTokenId.PHP_LINE_COMMENT){
            // do not modify indent for line comments starting
            // right at the beginning of the line cos they were
            // most likely created using Ctrl+/
            // see issue #162586
            return true;
        }

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
                if (id == PHPTokenId.PHP_COMMENT ||
                    id == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE ||

// TODO: please review!! without this line PHP formatter clobers
// all indentation done by HTML formatter:
                    id == PHPTokenId.T_INLINE_HTML ||

                    id == PHPTokenId.PHP_HEREDOC_TAG || id == PHPTokenId.PHP_NOWDOC_TAG
                ) {
                    // No indentation for literal strings in Ruby, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }

                if (id == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING){

                    if (ts.movePrevious()){
                        if (ts.token().id() == PHPTokenId.PHP_HEREDOC_TAG
                                || ts.token().id() == PHPTokenId.PHP_NOWDOC_TAG){
                            return true;
                        }
                        ts.moveNext();
                    }

                    int startLine = Utilities.getLineOffset(doc, ts.offset());
                    int currentLine = Utilities.getLineOffset(doc, pos);

                    if (startLine < currentLine){
                        // multiline string
                        return true;
                    }
                }
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
                    id == PHPTokenId.PHP_HEREDOC_TAG || id == PHPTokenId.PHP_NOWDOC_TAG
                ) {
                    // No indentation for literal strings in Ruby, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isSectionBorderLine(BaseDocument doc, int lineStart) throws BadLocationException{
        int firstNonWS = Utilities.getFirstNonWhiteFwd(doc, lineStart);
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPositionedSequence(doc, firstNonWS);
        PHPTokenId id = ts.token().id();

        return id == PHPTokenId.PHP_CLOSETAG || id == PHPTokenId.PHP_OPENTAG;
    }

    private void prettyPrint(final Context context, final ParserResult info) {
        final BaseDocument doc = (BaseDocument) context.document();
//        final String openingBraceStyle = CodeStyle.get(doc).getOpeningBraceStyle();

//        if (FmtOptions.OBRACE_PRESERVE.equals(openingBraceStyle)){
//            return;
//        }

        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                final WSTransformer wsTransformer = new WSTransformer(context);
                PHPParseResult result = (PHPParseResult) info;
                result.getProgram().accept(wsTransformer);
                wsTransformer.tokenScan();

                List<WSTransformer.Replacement> replacements = wsTransformer.getReplacements();
                Collections.sort(replacements);
                Collections.reverse(replacements);

		WSTransformer.Replacement replacementToApply = null;
                for (WSTransformer.Replacement replacement : replacements) {
		    if (replacementToApply != null) {
			if (replacementToApply.offset() != replacement.offset()) {
			    int offset = replacementToApply.offset();

			    if (offset >= context.startOffset() && offset <= context.endOffset()) {
				try {
				    doc.insertString(offset, replacementToApply.newString(), null);
				    if (replacementToApply.length() > 0){
					doc.remove(offset - replacementToApply.length(), replacementToApply.length());
				    }
				} catch (BadLocationException ex) {
				    Exceptions.printStackTrace(ex);
				}
			    }

			    
			    replacementToApply = replacement;
			}
			else {
			    if ((replacementToApply.newString().length() < replacement.newString().length())
				    || (replacementToApply.newString().length() == replacement.newString().length()
				    && replacementToApply.getPriority() > replacement.getPriority())) {
				replacementToApply = replacement;
			    }
			}
		    }
		    else {
			replacementToApply = replacement;
		    }
                }
		if (replacementToApply != null) {
		    int offset = replacementToApply.offset();
		    if (offset >= context.startOffset() && offset <= context.endOffset()){
			try {
			    doc.insertString(offset, replacementToApply.newString(), null);
			    if (replacementToApply.length() > 0){
				doc.remove(offset - replacementToApply.length(), replacementToApply.length());
			    }
			} catch (BadLocationException ex) {
			    Exceptions.printStackTrace(ex);
			}
		    }
		}
            }
        });
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
        document.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE); //NOI18N

        try {
            final BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())
            final Map<Integer, Integer> suggestedLineIndents = (Map<Integer, Integer>)doc.getProperty("AbstractIndenter.lineIndents");
            final int startOffset = Utilities.getRowStart(doc, context.startOffset());
            final int firstLine = Utilities.getLineOffset(doc, startOffset);
            
            doc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    int indentBias = 0;
                    boolean indentBiasCalculated = (startOffset == 0);
                    try {
                        int initIndentSize = CodeStyle.get(doc).getInitialIndent();
                        int numberOfLines = Utilities.getLineOffset(doc, doc.getLength() - 1) + 1;
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

                        boolean templateEdit = doc.getProperty("code-template-insert-handler") != null; //NOI18N



                        for (int i = 0, currentIndent = 0; i < numberOfLines; i++) {
                            int lineStart = Utilities.getRowStartFromLineOffset(doc, i);
                            Integer lineDelta = indentDeltaByLine.get(i);

                            if (lineDelta != null) {
                                currentIndent += lineDelta;
                                if (currentIndent < 0 ) {
				    LOG.warning("currentIndent was < 0 in PHPFormatter.astReformat(). It shouldn't happen."); //I18N
				    currentIndent = 0;
				}
                            }

                            if (!lineUnformattable(doc, lineStart)) {
                                int htmlSuggestion = 0;

                                if (suggestedLineIndents != null) {
                                    Integer rawSuggestion = suggestedLineIndents.get(i);
                                    if (rawSuggestion != null) {
                                        htmlSuggestion = rawSuggestion.intValue();
                                    }
                                }

                                if (!indentBiasCalculated
                                        && (templateEdit && i >= firstLine
                                        || !templateEdit && i >= firstLine - 1)){

                                    indentBias = currentIndent - GsfUtilities.getLineIndent(doc, lineStart) - htmlSuggestion + initIndentSize;
                                    indentBiasCalculated = true;
                                }
                                
//                                System.err.println("lineDelta[" + i + "]=" + lineDelta);
//                                System.err.println("htmlSuggestion[" + i + "]=" + htmlSuggestion);
                                //TODO:
                                if (lineStart >= context.startOffset() && lineStart <= context.endOffset()) {
                                    int actualIndent = 0;
                                    int initialIndent = isSectionBorderLine(doc, lineStart) ? 0 : initIndentSize;

                                    if (currentIndent + htmlSuggestion  + initialIndent > indentBias) {
                                        actualIndent = currentIndent + htmlSuggestion + initialIndent - indentBias;
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
