/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.languages.features;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;

/**
 *
 * @author Daniel Prusa
 */
public class ToggleCommentAction extends CommentAction {

    public ToggleCommentAction() {
        super(""); // NOI18N
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            BaseDocument doc = (BaseDocument)target.getDocument();
            Caret caret = target.getCaret();
            TokenHierarchy th = TokenHierarchy.get (doc);
            if (th == null) {
                return;
            }
            TokenSequence ts = th.tokenSequence();
            boolean isCommented = true;
            try {
                doc.atomicLock();
                if (caret.isSelectionVisible()) {
                    int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                    int endPos = target.getSelectionEnd();
                    if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                        endPos--;
                    }
                    int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                    List mimeTypes = new ArrayList(lineCnt);
                    int pos = startPos;
                    for (int x = lineCnt ; x > 0; x--) {
                        String mimeType = getRealMimeType(ts, pos);
                        mimeTypes.add(mimeType);
                        isCommented = isCommented && isCommentedLine(doc, mimeType, pos);
                        pos = Utilities.getRowStart(doc, pos, 1);
                    }

                    pos = startPos;
                    for (Iterator iter = mimeTypes.iterator(); iter.hasNext(); ) {
                        if (isCommented) {
                            uncommentLine(doc, (String)iter.next(), pos);
                        } else {
                            commentLine(doc, (String)iter.next(), pos);
                        }
                        pos = Utilities.getRowStart(doc, pos, 1);
                    }
                } else { // selection not visible
                    int pos = Utilities.getRowStart(doc, target.getSelectionStart());
                    String mt = getRealMimeType(ts, pos);
                    if (isCommentedLine(doc, mt, pos)) {
                        uncommentLine(doc, mt, pos);
                    } else {
                        commentLine(doc, mt, pos);
                    }
                }
            } catch (BadLocationException e) {
                target.getToolkit().beep();
            } finally {
                doc.atomicUnlock();
            }
        }
    }
    
    private String getRealMimeType(TokenSequence ts, int offset) {
        while (true) {
            ts.move(offset);
            if (!ts.moveNext())
                break;
            offset = ts.offset();
            TokenSequence ts2 = ts.embedded();
            if (ts2 == null) break;
            ts = ts2;
        }
        return ts.language().mimeType();
    }
    
    private void commentLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeature(Language.COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int end = Utilities.getRowEnd(doc, offset);
                doc.insertString(end, suffix, null);
            }
            doc.insertString(offset, prefix, null);
        }
    }
    
    private void uncommentLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeature(Language.COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int lastNonWhitePos = Utilities.getRowLastNonWhite(doc, offset);
                if (lastNonWhitePos != -1) {
                    int commentLen = suffix.length();
                    if (lastNonWhitePos - Utilities.getRowStart(doc, offset) >= commentLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, lastNonWhitePos - commentLen + 1, commentLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, suffix)) {
                            doc.remove(lastNonWhitePos - commentLen + 1, commentLen);
                        }
                    }
                }
            }
            int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
            if (firstNonWhitePos != -1) {
                int commentLen = prefix.length();
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= prefix.length()) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, commentLen);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, prefix)) {
                        doc.remove(firstNonWhitePos, commentLen);
                    }
                }
            }
        }
    }
    
    private boolean isCommentedLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        boolean suffixCommentOk = false;
        boolean prefixCommentOk = false;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeature(Language.COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return true;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int lastNonWhitePos = Utilities.getRowLastNonWhite(doc, offset);
                if (lastNonWhitePos != -1) {
                    int commentLen = suffix.length();
                    if (lastNonWhitePos - Utilities.getRowStart(doc, offset) >= commentLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, lastNonWhitePos - commentLen + 1, commentLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, suffix)) {
                            suffixCommentOk = true;
                        }
                    }
                }
            } else {
                suffixCommentOk = true;
            }
            int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
            if (firstNonWhitePos != -1) {
                int commentLen = prefix.length();
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= prefix.length()) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, commentLen);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, prefix)) {
                        prefixCommentOk = true;
                    }
                }
            }
            return prefixCommentOk && suffixCommentOk;
        } else {
            return true;
        }
    }
    
}
