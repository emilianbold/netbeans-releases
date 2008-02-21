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
package org.netbeans.modules.css2.gsf;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.fpi.gsf.BracketCompletion;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.modules.css2.editor.LexerUtils;
import org.netbeans.modules.css2.lexer.api.CSSTokenId;

/**
 *
 * @author marek
 */
public class CssBracketCompleter implements BracketCompletion {

    private static final char[][] PAIRS = new char[][]{{'{', '}'}, {'"', '"'}, {'\'', '\''}};

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
                System.out.println("diff " + diff);
                if(ts.moveNext() || ts.movePrevious()) {
                    Token t = ts.token();
                    if(t.id() == CSSTokenId.STRING) {
                        //there is already a string, do nothing
                        return false;
                    }
                }
            }

            int pairIdx = pairIndex(ch);
            
            doc.insertString(dot, String.valueOf(PAIRS[pairIdx][0]), null);
            doc.insertString(dot + 1, String.valueOf(PAIRS[pairIdx][1]), null);
            caret.setDot(dot + 1);
            return true;

        }

        return false;
    }

    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {

        int index = pairIndex(ch);
        if (index == -1) {
            //not interested
            return false;
        }


        TokenSequence<CSSTokenId> ts = LexerUtils.getCssTokenSequence(doc, caretOffset);
        if (ts == null) {
            //no css
            return false;
        }

        int diff = ts.move(caretOffset);
        System.out.println("diff = " + diff);

        if (ts.moveNext() || ts.movePrevious()) {
            Token t = ts.token();

            if (t.id() == CSSTokenId.STRING) {
            }
        }


        return false;

    }

    public int beforeBreak(Document doc, int caretOffset, JTextComponent caret) throws BadLocationException {
        return -1;
    }

    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    public List<OffsetRange> findLogicalRanges(CompilationInfo info, int caretOffset) {
        return Collections.emptyList();
    }

    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }
}
