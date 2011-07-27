/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.syntax.gsf;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.web.indent.api.LexUtilities;

public class JspKeystrokeHandler implements KeystrokeHandler {

    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    //runs under document atomic lock
    //runs in AWT
    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        final BaseDocument bdoc = (BaseDocument) doc;
        if ('>' != ch) {
            return false;
        }

        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, caretOffset, JspTokenId.language());
        if (ts == null) {
            return false;
        }
        ts.move(caretOffset);
        boolean found = false;
        while (ts.movePrevious()) {
            if (ts.token().id() == JspTokenId.SYMBOL && (ts.token().text().toString().equals("<") ||
                    ts.token().text().toString().equals("</"))) {
                found = true;
                break;
            }
            if (ts.token().id() == JspTokenId.SYMBOL && ts.token().text().toString().equals(">")) {
                break;
            }
            if (ts.token().id() != JspTokenId.ATTRIBUTE &&
                    ts.token().id() != JspTokenId.ATTR_VALUE &&
                    ts.token().id() != JspTokenId.TAG &&
                    ts.token().id() != JspTokenId.ENDTAG &&
                    ts.token().id() != JspTokenId.SYMBOL &&
                    ts.token().id() != JspTokenId.EOL &&
                    ts.token().id() != JspTokenId.WHITESPACE) {
                break;
            }
        }

        if (found) {
            //ok, the user just type tag closing symbol, lets reindent the line
            //since the code runs under document atomic lock, we cannot lock the
            //indentation infrastructure directly. Instead of that create a new
            //AWT task and post it for later execution.
            final Position from = doc.createPosition(Utilities.getRowStart(bdoc, ts.offset()));
            final Position to = doc.createPosition(Utilities.getRowEnd(bdoc, ts.offset()));
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    final Indent indent = Indent.get(bdoc);
                    indent.lock();
                    try {
                        bdoc.runAtomic(new Runnable() {

                            public void run() {
                                try {
                                    indent.reindent(from.getOffset(), to.getOffset());
                                } catch (BadLocationException ex) {
                                    //ignore
                                }
                            }
                        });
                    } finally {
                        indent.unlock();
                    }
                }
            });
        }

        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        // TODO: below whitespace skipping does not work because whitespace
        // tokens between JSP tokens are actually HTML tokens and not JSP tokens.
        // Proper way is to iterate over document characters and skip all whitespaces
        // till you get to a text and then get token for the text.
        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, caretOffset, JspTokenId.language());
        if (ts == null) {
            return -1;
        }
        ts.move(caretOffset);
        String closingTagName = null;
        int end = -1;
        if (ts.moveNext() && ts.token().id() == JspTokenId.SYMBOL &&
                ts.token().text().toString().equals("</")) {
            if (ts.moveNext() && ts.token().id() == JspTokenId.ENDTAG) {
                closingTagName = ts.token().text().toString();
                end = ts.offset() + ts.token().text().length();
                ts.movePrevious();
                ts.movePrevious();
            }
        }
        if (closingTagName == null) {
            return -1;
        }
        boolean foundOpening = false;
        if (ts.token().id() == JspTokenId.SYMBOL &&
                ts.token().text().toString().equals(">")) {
            while (ts.movePrevious()) {
                if (ts.token().id() == JspTokenId.TAG) {
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

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        return new ArrayList<OffsetRange>();
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }
    
}
