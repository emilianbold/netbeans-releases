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
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author vv159170
 */
public class CsmIncludeCompletionItem implements CompletionItem {
       
    protected final static String QUOTE = "\""; // NOI18N
    protected final static String SYS_OPEN = "<"; // NOI18N
    protected final static String SYS_CLOSE = ">"; // NOI18N
    protected final static String SLASH = "/"; // NOI18N

    private final int substitutionOffset;
    private final int priority;
    private final String item;
    private final String parentFolder;
    private final boolean isUserInclude;
    private final boolean isFolder;
    
    private static final int FOLDER_PRIORITY = 30;
    private static final int FILE_PRIORITY = 10;
    private static final int SYS_VS_USR = 5;
    
    protected CsmIncludeCompletionItem(int substitutionOffset, int priority, 
            String parentFolder, String item,
            boolean usrInclude, boolean isFolder) {
        this.substitutionOffset = substitutionOffset;
        this.priority = priority;
        this.parentFolder = parentFolder == null ? "" : parentFolder;
        this.isUserInclude = usrInclude;
        this.isFolder = isFolder;
        assert item != null;
        this.item = item;
    }
    
    public static CsmIncludeCompletionItem createItem(int substitutionOffset, 
                                                    String relFileName, String dirPrefix,
                                                    boolean usrInclude,
                                                    boolean highPriority,
                                                    boolean isFolder) {
        int priority;
        if (isFolder) {
            if (highPriority) {
                priority = FOLDER_PRIORITY - SYS_VS_USR;
            } else {
                priority = FOLDER_PRIORITY + SYS_VS_USR;
            }
        } else {
            if (highPriority) {
                priority = FILE_PRIORITY - SYS_VS_USR;
            } else {
                priority = FILE_PRIORITY + SYS_VS_USR;
            }
        }
        String item = relFileName;
        return new CsmIncludeCompletionItem(substitutionOffset, priority, dirPrefix, item, usrInclude, isFolder);
    }
    
    public String getItemText() {
        return item;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, isFolder() ? SLASH : null);
        }
        if (this.isFolder()) {
            Completion.get().showCompletion();
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            switch (evt.getKeyChar()) {
                case '>':
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                    break;
//                case '<':
                case '"':
                case '/':
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                    JTextComponent component = (JTextComponent)evt.getSource();
                    BaseDocument doc = (BaseDocument)component.getDocument();
                    int caretOffset = component.getSelectionEnd();
                    doc.atomicLock();
                    try {
                        String toReplace = doc.getText(substitutionOffset, caretOffset - substitutionOffset);
                        if (toReplace.startsWith("\"")) {
                            Completion.get().hideCompletion();
                            break;
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        doc.atomicUnlock();
                    }
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    if ('/' == evt.getKeyChar()) {
                        Completion.get().showCompletion();
                    }
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

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.isUserInclude ? "[U ": "[S ");
        out.append(this.isFolder() ? "D] ": "F] ");
        out.append(this.getLeftHtmlText()).append(" : ");
        out.append(this.getRightHtmlText());
        return out.toString();
    }
    
    public int getSortPriority() {
        return this.priority;
    }

    public CharSequence getSortText() {
        return item;
    }

    public CharSequence getInsertPrefix() {
        return item;
    }        
    
    protected ImageIcon getIcon() {
        return CsmImageLoader.getIncludeImageIcon(isUserInclude,isFolder());
    }
    
    protected String getLeftHtmlText() {
        return this.item;
    }
    
    protected String getRightHtmlText() {
        return this.parentFolder;
    }
    
    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getItemText();
        if (text != null) {
            TokenItem token = null;
            NbCsmSyntaxSupport sup = (NbCsmSyntaxSupport) Utilities.getSyntaxSupport(c).get(NbCsmSyntaxSupport.class);
            if (sup != null) {
                token = sup.getTokenItem(offset);
            }
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
            if (toAdd != null) {
                text += toAdd;
            }
            String pref = "";
            String post = "";
            if (token != null) {
                switch (token.getTokenID().getNumericID()) {
                case CCTokenContext.WHITESPACE_ID:
                    pref = this.isUserInclude ? QUOTE : SYS_OPEN;
                    post = this.isUserInclude ? QUOTE : SYS_CLOSE;
                    break;
                case CCTokenContext.USR_INCLUDE_ID:
                case CCTokenContext.INCOMPLETE_USR_INCLUDE_ID:
                    pref = QUOTE;
                    post = QUOTE;
                    len = (token.getOffset() + token.getImage().length()) - offset;
                    break;
                case CCTokenContext.INCOMPLETE_SYS_INCLUDE_ID:
                case CCTokenContext.SYS_INCLUDE_ID:
                    pref = SYS_OPEN;
                    post = SYS_CLOSE;
                    len = (token.getOffset() + token.getImage().length()) - offset;
                    break;
                }
            }
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (textToReplace.startsWith(SYS_OPEN)) {
                    pref = SYS_OPEN;
//                    post = SYS_CLOSE;
                } else if (textToReplace.startsWith(QUOTE)) {
                    pref = QUOTE;
//                    post = QUOTE;
                }
                text = pref + text + post;
//                if (text.equals(textToReplace)) {
//                    if (semiPos > -1)
//                        doc.insertString(semiPos, ";", null); //NOI18N
//                    return;
//                }                
                Position position = doc.createPosition(offset);
//                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
//                if (semiPosition != null)
//                    doc.insertString(semiPosition.getOffset(), ";", null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
    }

    protected boolean isFolder() {
        return isFolder;
    }

}
