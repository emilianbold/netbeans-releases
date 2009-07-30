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
package org.netbeans.modules.css.gsf;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Exceptions;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssBracketCompleter implements KeystrokeHandler {

    private static final char[][] PAIRS = new char[][]{{'{', '}'}, {'"', '"'}, {'\'', '\''}}; //NOI18N
    private char justAddedPair;
    private int justAddedPairOffset = -1;

    private int pairIndex(char ch) {
        for (int i = 0; i < PAIRS.length; i++) {
            char pair = PAIRS[i][0];
            if (pair == ch) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean beforeCharInserted(Document doc, int dot, JTextComponent target, char ch) throws BadLocationException {
        Caret caret = target.getCaret();

        if (justAddedPair == ch && justAddedPairOffset == dot) {
            //skip
            justAddedPair = 0;
            justAddedPairOffset = -1;
            caret.setDot(dot + 1);
            return true;
        }

        justAddedPair = 0;
        justAddedPairOffset = -1;

        if (ch == '}') {
            //handle curly bracket skipping
            //if there is a matching opening bracket and there is no opened unpaired bracket before
            //then just skip the typed char
            TokenSequence<CssTokenId> ts = LexerUtils.getCssTokenSequence(doc, dot);
            if (ts != null) {
                ts.move(dot);
                if (ts.moveNext()) {
                    //ts is already positioned
                    if (ts.token().id() == CssTokenId.RBRACE) {
                        //skip it
                        caret.setDot(dot + 1);
                        return true;
                    }
                }
            }
        }

        //test if we care about the typed character
        int pairIdx = pairIndex(ch);
        if (pairIdx == -1) {
            return false;
        }

        if (target.getSelectionStart() != dot) {
            /** @todo implement the adding quotes around selected text
             */
            return false;
        }


        if (ch == '\'' || ch == '"') {
            //handle quotations

            TokenSequence<CssTokenId> ts = LexerUtils.getCssTokenSequence(doc, dot);
            if (ts != null) {
                int diff = ts.move(dot);
                if (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == CssTokenId.STRING) {
                        //we are in or at a string
                        char front = t.text().charAt(diff);
                        if (front == ch) {
                            //do not insert, just move caret
                            caret.setDot(dot + 1);
                            return true;
                        } else {
                            //do nothing
                            return false;
                        }
                    }
                }

                //cover "text| and user types "
                //in such case just the quotation should be added

                //go back until we find " or ; { or } and test of the 
                //found quotation is a part of a string or not
                ts.move(dot);
                while (ts.movePrevious()) {
                    Token t = ts.token();
                    if (t.text().charAt(0) == ch) {
                        if (t.id() == CssTokenId.STRING || t.id() == CssTokenId.STRING1 || t.id() == CssTokenId.STRING2) {
                            //no unmatched quotation mark
                            break;
                        } else {
                            //found unmatched quotation mark - do nothing
                            return false;
                        }
                    }
                    if (t.id() == CssTokenId.LBRACE || t.id() == CssTokenId.RBRACE || t.id() == CssTokenId.SEMICOLON) {
                        //break the loop, not quotation found - we can complete
                        break;
                    }
                }
            }
        }

        justAddedPair = PAIRS[pairIdx][1];
        justAddedPairOffset = dot + 1;

        doc.insertString(dot, String.valueOf(PAIRS[pairIdx][0]), null);
        doc.insertString(dot + 1, String.valueOf(justAddedPair), null);
        caret.setDot(dot + 1);
        return true;

    }

    @Override
    public boolean afterCharInserted(Document doc, final int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        if ('}' != ch) {
            return false;
        }
        final int lineStart = Utilities.getRowFirstNonWhite((BaseDocument)doc, caretOffset);
        if (lineStart != caretOffset) {
            return false;
        }
        final Indent indent = Indent.get(doc);
        ((BaseDocument)doc).runAtomic(new Runnable() {
            public void run() {
                try {
                    indent.reindent(lineStart, caretOffset);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int dot, JTextComponent target, char ch) throws BadLocationException {
        if(justAddedPairOffset - 1 == dot) {
            //removed the paired char, remove the pair as well
            doc.remove(dot, 1);
        }

        justAddedPair = 0;
        justAddedPairOffset = -1;

        return false;

    }

    //this method is called within Indent.get(doc).lock() and unlock() section, no need for additional locking
    @Override
    public int beforeBreak(final Document doc, final int dot, final JTextComponent jtc) throws BadLocationException {
        if (dot == 0 || dot == doc.getLength()) { //check corners
            return -1;
        }
        String context = doc.getText(dot - 1, 2); //get char before and after

        if ("{}".equals(context)) { //NOI18N
            final Indent indent = Indent.get(doc);
            BaseDocument bdoc = (BaseDocument) doc;

            bdoc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        //smart indent
                        doc.insertString(dot, "\n", null); //NOI18N
                        //move caret
                        jtc.getCaret().setDot(dot);
                        //and indent the line
                        indent.reindent(dot - 1, dot + 2);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });


        }

        return -1;

    }

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        //XXX returning null or the default should cause GSF to use the IDE default matcher
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        ArrayList<OffsetRange> ranges = new ArrayList<OffsetRange>(2);

        SimpleNode root = ((CssParserResult) info).root();
        Snapshot snapshot = info.getSnapshot();

        if (root != null) {
            //find leaf at the position
            SimpleNode node = SimpleNodeUtil.findDescendant(root, snapshot.getEmbeddedOffset(caretOffset));
            if (node != null) {
                //go through the tree and add all parents with, eliminate duplicate nodes
                do {
                    int from = snapshot.getOriginalOffset(node.startOffset());
                    int to = snapshot.getOriginalOffset(node.endOffset());

                    if(from == -1 || to == -1) {
                        continue;
                    }

                    OffsetRange last = ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
                    //skip duplicated ranges
                    if (last == null || ((last.getEnd() - last.getStart()) < (to - from))) {
                        ranges.add(new OffsetRange(from, to));
                    }
                } while ((node = (SimpleNode) node.jjtGetParent()) != null);
            }
        }

//        //the bottom most element represents the whole parse tree, replace it by the document
//        //range since they doesn't need to be the same
//        if (!ranges.isEmpty()) {
//            ranges.set(ranges.size() - 1, new OffsetRange(0,
//                    info.getSnapshot().getSource().getDocument(true).getLength()));
//        }

        return ranges;
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }
}
