/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/** C++ editor kit with appropriate document */ 
package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.MIMENames;
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
        
        private static void doCStyleComment(JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    doc.atomicLock();
                    try {
                        int startPos;
                        int endPos;
                        String endString = insertEndCommentString;
                        if (caret.isSelectionVisible()) {
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
                    } finally {
                        doc.atomicUnlock();
                    }                    
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
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
        
        private static void doCStyleUncomment(JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    doc.atomicLock();
                    try {
                        int startPos;
                        int endPos;
                        if (caret.isSelectionVisible()) {
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
                        CCSyntaxSupport sup = (CCSyntaxSupport)Utilities.getSyntaxSupport(target);
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
                                startLineEndPos = startLineEndPos < doc.getLength()-1 ? startLineEndPos + 1 : doc.getLength();
                            }
                            int endLineStartPos = Utilities.getRowStart(doc, commentBlockEndOffset);
                            int endLineEndPos = Utilities.getRowEnd(doc, endLineStartPos);
                            String endLineContent = doc.getText(endLineStartPos, endLineEndPos - endLineStartPos);
                            if (!END_BLOCK_COMMENT.equals(endLineContent.trim())) {
                                // not only "*/" on the line => remove only "*/" itself
                                endLineEndPos = commentBlockEndOffset;
                                endLineStartPos = endLineEndPos-END_BLOCK_COMMENT.length();
                            } else {
                                // remove full line with eol
                                endLineEndPos = endLineEndPos < doc.getLength()-1 ? endLineEndPos + 1 : doc.getLength();
                            }
                            // remove end line
                            doc.remove(endLineStartPos, endLineEndPos-endLineStartPos);
                            // remove start line
                            doc.remove(startLineStartPos, startLineEndPos-startLineStartPos);
                        }

                    } finally {
                        doc.atomicUnlock();
                    }                    
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
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

        private boolean allComments(JTextComponent target) {
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            TokenItem item = null;
            try {
                doc.atomicLock();
                try {
                    int startPos;
                    int endPos;
                    if (caret.isSelectionVisible()) {
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
                    CCSyntaxSupport sup = (CCSyntaxSupport)Utilities.getSyntaxSupport(target);
                    item = sup.getTokenChain(startPos, endPos);
                    while (item != null && item.getOffset() < endPos && 
                            (item.getTokenID() == CCTokenContext.WHITESPACE)) {
                        // all in comment means only whitespaces or block commens
                        item = item.getNext();
                    }
                } finally {
                    doc.atomicUnlock();
                }                    
            } catch (BadLocationException e) {
                target.getToolkit().beep();
            }
            return (item != null) && (item.getTokenID() == CCTokenContext.BLOCK_COMMENT);
        }
    }

}
