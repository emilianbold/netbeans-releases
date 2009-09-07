/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.gsf;

import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.HtmlAutoCompletion;
import org.netbeans.modules.html.editor.Utils;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
public class HtmlKeystrokeHandler implements KeystrokeHandler {

    //not used. HTMLKit coveres this functionality
    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        HtmlAutoCompletion.charInserted((BaseDocument)doc, caretOffset, target.getCaret(), ch);
        if ('>' != ch) {
            return false;
        }
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument)doc, caretOffset, HTMLTokenId.language());
        if (ts == null) {
            return false;
        }
        ts.move(caretOffset);
        boolean found = false;
        while (ts.movePrevious()) {
            if (ts.token().id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                found = true;
                break;
            }
            if (ts.token().id() != HTMLTokenId.ARGUMENT &&
                ts.token().id() != HTMLTokenId.OPERATOR &&
                ts.token().id() != HTMLTokenId.VALUE &&
                ts.token().id() != HTMLTokenId.VALUE_CSS &&
                ts.token().id() != HTMLTokenId.VALUE_JAVASCRIPT &&
                ts.token().id() != HTMLTokenId.WS &&
                ts.token().id() != HTMLTokenId.TAG_CLOSE &&
                ts.token().id() != HTMLTokenId.TAG_OPEN) {
                break;
            }
        }
        if (!found) {
            return false;
        }
        int lineStart = Utilities.getRowFirstNonWhite((BaseDocument)doc, ts.offset());
        if (lineStart != ts.offset()) {
            return false;
        }
        final Indent indent = Indent.get(doc);
        indent.reindent(lineStart, caretOffset); //caled under Indent lock && atomic lock

        return false;
    }

    //not used. HTMLKit coveres this functionality
    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    //not used. HTMLKit coveres this functionality
    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument)doc, caretOffset, HTMLTokenId.language());
        if (ts == null) {
            return -1;
        }
        ts.move(caretOffset);
        String closingTagName = null;
        int end = -1;
        if (ts.moveNext() && ts.token().id() == HTMLTokenId.TAG_OPEN_SYMBOL &&
                ts.token().text().toString().equals("</")) {
            if (ts.moveNext() && ts.token().id() == HTMLTokenId.TAG_CLOSE) {
                closingTagName = ts.token().text().toString();
                end = ts.offset()+ts.token().text().length();
                ts.movePrevious();
                ts.movePrevious();
            }
        }
        if (closingTagName == null) {
            return  -1;
        }
        boolean foundOpening = false;
        if (ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL &&
                ts.token().text().toString().equals(">")) {
            while (ts.movePrevious()) {
                if (ts.token().id() == HTMLTokenId.TAG_OPEN) {
                    if (ts.token().text().toString().equals(closingTagName)) {
                        foundOpening = true;
                    }
                    break;
                }
            }
        }
        if (foundOpening) {
            final Indent indent = Indent.get(doc);
            doc.insertString(caretOffset, "\n", null); //NOI18N
            //move caret
            target.getCaret().setDot(caretOffset);
            //and indent the line
            indent.reindent(caretOffset + 1, end);
        }
        return -1;
    }

    //not used. HTMLBracesMatching coveres this functionality
    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        HtmlParserResult result = (HtmlParserResult)info;

        ArrayList<OffsetRange> ranges = new ArrayList<OffsetRange>(2);

        //include the text under the carat to the ranges.
        //I need to do it this lexical way since we do not
        //add the text nodes into the ast due to performance reasons
        Document doc = info.getSnapshot().getSource().getDocument(true);
        TokenHierarchy hierarchy = TokenHierarchy.get(doc);
        TokenSequence ts = Utils.getJoinedHtmlSequence(doc);
        if(ts == null) {
            return Collections.emptyList();
        }
        ts.move(caretOffset);
        if(ts.moveNext() || ts.movePrevious()) {
            Token token = ts.token();

            if(token.id() == HTMLTokenId.TEXT) {
                CharSequence text = token.text();
                if(text.toString().trim().length() > 0) { //filter only whitespace tokens
                    int from = ts.offset();
                    int to = from + token.text().length();

                    //properly compute end offset of joined tokens
                    List<Token> tokenParts = token.joinedParts();
                    if(tokenParts != null) {
                        //get last part token
                        Token last = tokenParts.get(tokenParts.size() - 1);
                        to = last.offset(hierarchy) + last.length();
                    }

                    //first add the range of trimmed text, then the whole text range
                    int trimmed_from = from;
                    for(int i = 0; i < text.length(); i++) {
                        char ch = text.charAt(i);
                        if(!Character.isWhitespace(ch)) {
                            trimmed_from = trimmed_from + i;
                            break;
                        }
                    }
                    int trimmed_to = to;
                    for(int i = text.length() - 1; i >= 0 ; i--) {
                        char ch = text.charAt(i);
                        if(!Character.isWhitespace(ch)) {
                            trimmed_to = to - ((text.length() - 1) - i);
                            break;
                        }
                    }

                    if(trimmed_from != from || trimmed_to != to) {
                        ranges.add(new OffsetRange(trimmed_from, trimmed_to));
                    }

                    ranges.add(new OffsetRange(from, to));
                }
            }
        }

        AstNode root = result.root();
        Snapshot snapshot = result.getSnapshot();

        if(root != null) {
            //find leaf at the position
            AstNode node = AstNodeUtils.findDescendant(root, snapshot.getEmbeddedOffset(caretOffset));
            if(node != null) {
                //go through the tree and add all parents with, eliminate duplicate nodes
                do {
                    int[] logicalRange = node.getLogicalRange();

                    int from = snapshot.getOriginalOffset(logicalRange[0]);
                    int to = snapshot.getOriginalOffset(logicalRange[1]);

                    if(from == -1 || to == -1 || from == to) {
                        continue;
                    }

                    OffsetRange last = ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
                    //skip duplicated ranges
                    if(last == null || !(last.getStart() == from && last.getEnd() == to)) {
                        ranges.add(new OffsetRange(from, to));
                    }
                } while ((node = node.parent()) != null);
            }
        }

        return ranges;
    }

    //TODO implement
    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
