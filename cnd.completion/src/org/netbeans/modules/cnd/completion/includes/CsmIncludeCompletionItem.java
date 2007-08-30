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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author vv159170
 */
public class CsmIncludeCompletionItem implements CompletionItem {
       
    protected int substitutionOffset;
    
    protected CsmIncludeCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            switch (evt.getKeyChar()) {
                case ';':
                case ',':
                case '(':
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                case '.':
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    if (evt.getKeyChar() == '.')
                        Completion.get().showCompletion();
                    evt.consume();
                    break;
            }
        }
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
    }
    
    protected String getLeftHtmlText() {
        return null;
    }
    
    protected String getRightHtmlText() {
        return null;
    }

    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getInsertPrefix().toString();
        if (text != null) {
//            int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
//            if (semiPos > -2)
//                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
//            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
//                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
//                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
//                    text += toAdd;
//                    toAdd = null;
//                }
//                boolean added = false;
//                while(toAdd != null && toAdd.length() > 0) {
//                    String tokenText = sequence.token().text().toString();
//                    if (tokenText.startsWith(toAdd)) {
//                        len = sequence.offset() - offset + toAdd.length();
//                        text += toAdd;
//                        toAdd = null;
//                    } else if (toAdd.startsWith(tokenText)) {
//                        sequence.moveNext();
//                        len = sequence.offset() - offset;
//                        text += toAdd.substring(0, tokenText.length());
//                        toAdd = toAdd.substring(tokenText.length());
//                        added = true;
//                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
//                        if (!sequence.moveNext()) {
//                            text += toAdd;
//                            toAdd = null;
//                        }
//                    } else {
//                        if (!added)
//                            text += toAdd;
//                        toAdd = null;
//                    }
//                }
//            }
//            // Update the text
//            doc.atomicLock();
//            try {
//                String textToReplace = doc.getText(offset, len);
//                if (text.equals(textToReplace)) {
//                    if (semiPos > -1)
//                        doc.insertString(semiPos, ";", null); //NOI18N
//                    return;
//                }                
//                Position position = doc.createPosition(offset);
//                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
//                doc.remove(offset, len);
//                doc.insertString(position.getOffset(), text, null);
//                if (semiPosition != null)
//                    doc.insertString(semiPosition.getOffset(), ";", null);
//            } catch (BadLocationException e) {
//                // Can't update
//            } finally {
//                doc.atomicUnlock();
//            }
        }
    }
    
    public int getSortPriority() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CharSequence getSortText() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CharSequence getInsertPrefix() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
