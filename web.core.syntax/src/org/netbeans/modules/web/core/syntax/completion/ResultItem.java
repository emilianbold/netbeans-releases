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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.completion;

import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.*;


/**
 * Code completion result item base class
 *
 * @author  Dusan Balek, Marek Fukala
 */

public abstract class ResultItem implements CompletionQuery.ResultItem, CompletionItem {
    
    static String toAdd;
    public static final String COMPLETION_SUBSTITUTE_TEXT= "completion-substitute-text"; //NOI18N
    
    protected int selectionStartOffset = -1;
    protected int selectionEndOffset = -1;
    private int substituteOffset = -1; //stores the substituteOffset
    
    protected boolean shift = false;
    
    public int getSubstituteOffset() {
        return substituteOffset;
    }
    
    public void setSubstituteOffset(int offset) {
        this.substituteOffset = offset;
    }
    
    public abstract String getItemText();
    
    public abstract Component getPaintComponent(boolean isSelected);
    
    public abstract int getSortPriority();
    
    public CharSequence getSortText() {
        return getItemText();
    }
    
    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }
    
    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }
    
    //afaik called only from abbrevs result item
    public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getItemText();
        
        if (text != null) {
            if (toAdd != null && !toAdd.equals("\n")) // NOI18N
                text += toAdd;
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) return false;
                
                doc.remove(offset, len);
                doc.insertString(offset, text, null);
                if (selectionStartOffset >= 0) {
                    c.select(offset + selectionStartOffset,
                            offset + selectionEndOffset);
                }
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
        
        return true;
    }
    
    public Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret = getPaintComponent(isSelected);
        if (ret==null) return null;
        if (isSelected) {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        } else {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        ResultItemPaintComponent renderComponent = (ResultItemPaintComponent)getPaintComponent(false);
        return renderComponent.getPreferredWidth(g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((ResultItemPaintComponent)renderComponent).paintComponent(g);
    }
    
    public boolean instantSubstitution(JTextComponent c) {
        defaultAction(c);
        return true;
    }
    
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new JspCompletionProvider.DocQuery(this));
    }
    
    public abstract URL getHelpURL();
    
    /** Returns help for the item. It can be only url. If the item doesn't have a help
     *  than returns null. The class can overwrite this method and compounds the help realtime.
     */
    public abstract String getHelp();
    
    /** Returns whether the item has a help. */
    public abstract boolean hasHelp();
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public int getImportance() {
        return 0;
    }
    
    public void defaultAction(JTextComponent component) {
        int substOffset = getSubstituteOffset();
        if (substOffset == -1)
            substOffset = component.getCaret().getDot();
        
        if(!shift) Completion.get().hideAll();
        substituteText(component, substOffset, component.getCaret().getDot() - substOffset, shift);
    }
    
}
