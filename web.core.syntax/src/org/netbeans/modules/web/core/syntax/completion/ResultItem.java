/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.completion;

import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.text.Caret;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.java.JCExpression;
import org.netbeans.editor.ext.java.JavaCompletion;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private int offset = -1; //stores the substituteOffset
    
    public int getSubstituteOffset() {
        return offset;
    }
    
    void setSubstituteOffset(int offset) {
        this.offset = offset;
    }
    
    public abstract String getItemText();
    
    public abstract Component getPaintComponent(boolean isSelected);
    
    public abstract int getSortPriority();
    
    public CharSequence getSortText() {
        return getItemText();
    }
    
    public void processKeyEvent(KeyEvent evt) {
        // No special handling
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
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
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
        ResultItem.toAdd = "\n";
        Completion.get().hideAll();
        substituteText(component, substOffset, component.getCaret().getDot() - substOffset, false);
    }
    
}
