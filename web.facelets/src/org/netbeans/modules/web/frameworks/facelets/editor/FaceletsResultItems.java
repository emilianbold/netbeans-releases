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

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsResultItems {
    
    
    public abstract static class Item implements CompletionItem {
        String text;
        int offset;
        int remLength;
        
        // is the SHIFT key pressed
        boolean shift = false;
        
        public Item(String text, int offset, int remLength){
            this.text = text;
            this.offset = offset;
            this.remLength = remLength;
        }
        
        public void defaultAction(JTextComponent component) {
            if(!shift) Completion.get().hideAll();
            replaceText(component, text);
        }
        
        public void processKeyEvent(KeyEvent event) {
            shift = (event.getKeyCode() == KeyEvent.VK_ENTER && event.getID() == KeyEvent.KEY_PRESSED && event.isShiftDown());
        }
        
        public int getPreferredWidth(Graphics graph, Font defaultFont) {
            return (int)(graph.getFontMetrics(defaultFont).getStringBounds(" " + text + " ", graph).getWidth());
        }
        
        abstract public void render(Graphics graph, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected);
              
        public CompletionTask createDocumentationTask() {
            return null;
        }
        
        public CompletionTask createToolTipTask() {
            return null;
        }
        
        public boolean instantSubstitution(JTextComponent component) {
            defaultAction(component);
            return true;
        }
        
        public int getSortPriority() {
            return 20;
        }
        
        public CharSequence getSortText() {
            return text;
        }
        
        public CharSequence getInsertPrefix() {
            return text;
        }
        
        boolean replaceText( JTextComponent component, String text ) {
            BaseDocument doc = (BaseDocument)component.getDocument();
            doc.atomicLock();
            try {
                //test whether we are trying to insert sg. what is already present in the text
                String currentText = doc.getText(offset, (doc.getLength() - offset) < text.length() ? (doc.getLength() - offset) : text.length()) ;
                if(!text.equals(currentText)) {
                    //remove common part
                    doc.remove( offset, remLength );
                    doc.insertString( offset, text, null);
                } else {
                    int newCaretPos = component.getCaret().getDot() + text.length() - remLength;
                    component.setCaretPosition(newCaretPos < doc.getLength() ? newCaretPos : doc.getLength());
                }
            } catch( BadLocationException exc ) {
                return false;    //not sucessfull
            } finally {
                doc.atomicUnlock();
            }
            return true;
        }
    }

    public static class AttributeItem extends Item{
        
        public AttributeItem(String text, int offset, int remLength){
            super(text, offset, remLength);
        }

        public void render(Graphics graph, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            if (selected)
                graph.setColor(Color.BLACK);
            else
                graph.setColor(Color.GREEN.darker());
            FontMetrics fMetrics = graph.getFontMetrics(defaultFont);
            graph.drawString(text, fMetrics.charWidth(' '), fMetrics.getAscent());
        }
        
        public void defaultAction(JTextComponent component) {
            if(!shift) Completion.get().hideAll();
            replaceText(component, text+"=\"\"");
            Caret caret = component.getCaret();
            caret.setDot( caret.getDot() - 1 );
        }
    }
    
    public static class TagValueAttributeItem extends Item {
        String prefix;
        String tagName;
        String uri;
        
        public TagValueAttributeItem(String prefix, String tagName, String uri, int offset, int remLength){
            super(prefix+":"+tagName, offset, remLength);
            this.prefix = prefix;
            this.tagName = tagName;
            this.uri = uri;
        }

        public void render(Graphics graph, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            graph.setColor(Color.BLUE.darker());
            FontMetrics fMetrics = graph.getFontMetrics(defaultFont);
            graph.drawString(prefix + ":" + tagName, fMetrics.charWidth(' '), fMetrics.getAscent());
        }
        
        public void defaultAction(JTextComponent component) {
            super.defaultAction(component);
            (new FaceletsAutoTagImporter()).importLibrary(component.getDocument(), prefix, uri);
        }
    }
    
}
