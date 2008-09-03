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

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
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

            TokenSequence<CSSTokenId> ts = LexerUtils.getCssTokenSequence(doc, dot);
            if (ts != null) {
                int diff = ts.move(dot);
                if (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == CSSTokenId.STRING) {
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
                        if (t.id() == CSSTokenId.STRING || t.id() == CSSTokenId.STRING1 || t.id() == CSSTokenId.STRING2) {
                            //no unmatched quotation mark
                            break;
                        } else {
                            //found unmatched quotation mark - do nothing
                            return false;
                        }
                    }
                    if (t.id() == CSSTokenId.LBRACE || t.id() == CSSTokenId.RBRACE || t.id() == CSSTokenId.SEMICOLON) {
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

    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    public boolean charBackspaced(Document doc, int dot, JTextComponent target, char ch) throws BadLocationException {
        justAddedPair = 0;
        justAddedPairOffset = -1;
        
        return false;

    }

    //this method is called within Indent.get(doc).lock() and unlock() section, no need for additional locking
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

    public OffsetRange findMatching(Document doc, int caretOffset) {
        //XXX returning null or the default should cause GSF to use the IDE default matcher
        return OffsetRange.NONE;
    }

    public List<OffsetRange> findLogicalRanges(CompilationInfo info, int caretOffset) {
        return Collections.emptyList();
    }

    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }
    
}
