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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/** C++ editor kit with appropriate document */
package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Language;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.editor.spi.cplusplus.CCSyntaxSupport;

public class CKit extends CCKit {

    @Override
    public String getContentType() {
        return MIMENames.C_MIME_TYPE;
    }

    /**
     * Create new instance of a C syntax coloring scanner.
     *
     * @param doc document to operate on
     */
    @Override
    public Syntax createSyntax(Document doc) {
        return new CSyntax();
    }
    
    @Override
    protected Language<CppTokenId> getLanguage() {
        return CppTokenId.languageC();
    }
    
    @Override
    protected Filter<CppTokenId> getFilter() {
        return CndLexerUtilities.getGccCFilter();
    }
    
    @Override
    protected Action getCommentAction() {
        return new CCommentAction(); 
    }
    
    @Override
    protected Action getUncommentAction() {
        return new CUncommentAction(); 
    }
    
    @Override
    protected Action getToggleCommentAction() {
        return new CToggleCommentAction(); 
    }
    
    private static String START_BLOCK_COMMENT = "/*"; // NOI18N
    private static String END_BLOCK_COMMENT = "*/"; // NOI18N

    private static String insertStartCommentString = START_BLOCK_COMMENT + "\n"; // NOI18N
    private static String insertEndCommentString = END_BLOCK_COMMENT + "\n"; // NOI18N
    
    private static final class CCommentAction extends CommentAction {
        private CCommentAction() {
            // fake string 
            super("//"); // NOI18N 
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            doCStyleComment(target);
        }
        
        private static void doCStyleComment(final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomic(new Runnable() {

                    public void run() {
                        Caret caret = target.getCaret();
                        try {
                            int startPos;
                            int endPos;
                            String endString = insertEndCommentString;
                            //if (caret.isSelectionVisible()) {
                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                    endPos--;
                                }

                                int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                                endPos = Utilities.getRowStart(doc, startPos, +lineCnt);
                            } else {
                                // selection not visible, surround only one line
                                startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                endPos = Utilities.getRowStart(doc, startPos, +1);
                                if (endPos == -1) {
                                    endPos = doc.getLength();
                                    endString = "\n" + insertEndCommentString; // NOI18N
                                }
                            }
                            // insert end line
                            doc.insertString(endPos, endString, null);
                            // then start line
                            doc.insertString(startPos, insertStartCommentString, null);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                });
            }        
        }        
    }
    
    private static final class CUncommentAction extends UncommentAction {
        private CUncommentAction() {
            // fake string 
            super("//"); // NOI18N 
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            doCStyleUncomment(target);
        }
        
        private static void doCStyleUncomment(final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomic(new Runnable() {

                    public void run() {
                        Caret caret = target.getCaret();
                        try {
                            int startPos;
                            int endPos;
                            //if (caret.isSelectionVisible()) {
                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = target.getSelectionStart();
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                    endPos--;
                                }
                            } else {
                                // selection not visible
                                endPos = startPos = target.getSelectionStart();
                            }
                            // get token inside selection
                            CCSyntaxSupport sup = (CCSyntaxSupport) Utilities.getSyntaxSupport(target);
                            TokenItem item = sup.getTokenChain(startPos, endPos);
                            while (item != null && item.getOffset() < endPos &&
                                    item.getTokenID() == CCTokenContext.WHITESPACE) {
                                item = item.getNext();
                            }
                            if (item != null && item.getTokenID() == CCTokenContext.BLOCK_COMMENT) {
                                int commentBlockStartOffset = item.getOffset();
                                int commentBlockEndOffset = commentBlockStartOffset + item.getImage().length();
                                int startLineStartPos = Utilities.getRowStart(doc, commentBlockStartOffset);
                                int startLineEndPos = Utilities.getRowEnd(doc, startLineStartPos);
                                String startLineContent = doc.getText(startLineStartPos, startLineEndPos - startLineStartPos);
                                if (!START_BLOCK_COMMENT.equals(startLineContent.trim())) {
                                    // not only "\*" on the line => remove only "\*" itself
                                    startLineStartPos = commentBlockStartOffset;
                                    startLineEndPos = startLineStartPos + START_BLOCK_COMMENT.length();
                                } else {
                                    // remove full line with eol
                                    startLineEndPos = startLineEndPos < doc.getLength() - 1 ? startLineEndPos + 1 : doc.getLength();
                                }
                                int endLineStartPos = Utilities.getRowStart(doc, commentBlockEndOffset);
                                int endLineEndPos = Utilities.getRowEnd(doc, endLineStartPos);
                                String endLineContent = doc.getText(endLineStartPos, endLineEndPos - endLineStartPos);
                                if (!END_BLOCK_COMMENT.equals(endLineContent.trim())) {
                                    // not only "*/" on the line => remove only "*/" itself
                                    endLineEndPos = commentBlockEndOffset;
                                    endLineStartPos = endLineEndPos - END_BLOCK_COMMENT.length();
                                } else {
                                    // remove full line with eol
                                    endLineEndPos = endLineEndPos < doc.getLength() - 1 ? endLineEndPos + 1 : doc.getLength();
                                }
                                // remove end line
                                doc.remove(endLineStartPos, endLineEndPos - endLineStartPos);
                                // remove start line
                                doc.remove(startLineStartPos, startLineEndPos - startLineStartPos);
                            }
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                });
            }
        }        
    }
    
    private static final class CToggleCommentAction extends ToggleCommentAction {
        private CToggleCommentAction() {
            // fake string 
            super("//"); // NOI18N 
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (allComments(target)) {
                CUncommentAction.doCStyleUncomment(target);
            } else {
                CCommentAction.doCStyleComment(target);
            }
        }

        private boolean allComments(final JTextComponent target) {           
            final BaseDocument doc = (BaseDocument)target.getDocument();
            final boolean res[] = new boolean[] { false };
            doc.runAtomic(new Runnable() {
                public void run() {
                    Caret caret = target.getCaret();
                    TokenItem item = null;
                    try {
                        int startPos;
                        int endPos;
                        //if (caret.isSelectionVisible()) {
                        if (Utilities.isSelectionShowing(caret)) {
                            startPos = target.getSelectionStart();
                            endPos = target.getSelectionEnd();
                            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                endPos--;
                            }
                        } else {
                            // selection not visible
                            endPos = startPos = target.getSelectionStart();
                        }
                        // get token inside selection
                        CCSyntaxSupport sup = (CCSyntaxSupport) Utilities.getSyntaxSupport(target);
                        item = sup.getTokenChain(startPos, endPos);
                        while (item != null && item.getOffset() < endPos &&
                                (item.getTokenID() == CCTokenContext.WHITESPACE)) {
                            // all in comment means only whitespaces or block commens
                            item = item.getNext();
                        }                        
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    }
                    res[0] = (item != null) && (item.getTokenID() == CCTokenContext.BLOCK_COMMENT);
                }
            });
            return res[0];
        }
    }

}
