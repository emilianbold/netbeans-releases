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

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.text.*;
import javax.swing.Icon;

import org.netbeans.editor.*;
//import org.netbeans.editor.ext.*;
import org.netbeans.editor.Utilities;
import javax.swing.JLabel;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;

import org.netbeans.editor.ext.CompletionQuery.ResultItem;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 * This class carries result information required by NetBeans Editor module.
 *
 * @author  Petr Kuzel
 * @author  Sandeep Randhawa
 */
class XMLResultItem implements ResultItem, CompletionItem {
    
    private static final int XML_ITEMS_SORT_PRIORITY = 20;
    // text to be diplayed to user
    public String displayText;
    private String replacementText;
    // icon to be diplayed
    public javax.swing.Icon icon;
    public Color foreground = Color.black;
    public Color background = Color.white;
    public Color selectionForeground = Color.black;
    public Color selectionBackground = new Color(204, 204, 255);
    private static JLabel rubberStamp = new JLabel();
    private XMLCompletionResultItemPaintComponent component;
    private boolean shift = false;
    
    static {
        rubberStamp.setOpaque( true );
    }
    
    /**
     *
     * @param replacementText replacement text that is used as display name too
     */
    public XMLResultItem(String replacementText){
        this(replacementText, null);
    }
    
    /**
     * @param displayText text to display or null if replacementText is OK
     */
    public XMLResultItem(String replacementText, String displayText) {
        this.replacementText = replacementText;
        this.displayText = displayText != null ? displayText : replacementText;
    }
    
    /** Creates new XMLResultItem
     * @param displayText The string value that will be displayed in the completion window and will hence
     * be the replacement text if selected.
     * @param icon The icon that will be displayed for this element.
     * @param foreground The foreground color of the text
     * @param background The background color of the text
     * @param selectionForeground The foreground color of the selected text
     * @param selectionBackground The background color of the selected text
     */
    public XMLResultItem(String displayText, javax.swing.Icon icon, Color foreground, Color background, Color selectionForeground, Color selectionBackground) {
        this.displayText = displayText;
        this.icon = icon;
        this.foreground = foreground;
        this.background = background;
        this.selectionForeground = selectionForeground;
        this.selectionBackground = selectionBackground;
    }
    
    /**
     * Insert following text into document.
     */
    public String getReplacementText(int modifiers){
        return displayText;
    }
    
    
    protected Icon getIcon(){
        return icon;
    }
    
    /**
     * Actually replaces a piece of document by passes text.
     * @param component a document source
     * @param text a string to be inserted
     * @param offset the target offset
     * @param len a length that should be removed before inserting text
     */
    boolean replaceText( JTextComponent component, String text, int offset, int len) {
        BaseDocument doc = (BaseDocument)component.getDocument();
        doc.atomicLock();
        try {
            String currentText = doc.getText(offset, (doc.getLength() - offset) < text.length() ? (doc.getLength() - offset) : text.length()) ;
            if(!text.equals(currentText)) {
                doc.remove( offset, len );
                doc.insertString( offset, text, null);
            } else {
                int newCaretPos = component.getCaret().getDot() + text.length() - len;
                //#82242 workaround - the problem is that in some situations
                //1) result item is created and it remembers the remove length
                //2) document is changed
                //3) RI is substituted.
                //this situation shouldn't happen imho and is a problem of CC infrastructure
                component.setCaretPosition(newCaretPos < doc.getLength() ? newCaretPos : doc.getLength());
            }
            //reformat the line
            //((ExtFormatter)doc.getFormatter()).reformat(doc, Utilities.getRowStart(doc, offset), offset+text.length(), true);
        } catch( BadLocationException exc ) {
            return false;    //not sucessfull
            // } catch (IOException e) {
            //     return false;
        } finally {
            doc.atomicUnlock();
        }
        return true;
    }
    
    public boolean substituteCommonText( JTextComponent c, int offset, int len, int subLen ) {
        return replaceText( c, getReplacementText(0).substring( 0, subLen ), offset, len );
    }
    
    /**
     * Just translate <code>shift</code> to proper modifier
     */
    public final boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
        int modifier = shift ? java.awt.event.InputEvent.SHIFT_MASK : 0;
        return substituteText(c, offset, len, modifier);
    }
    
    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        return replaceText(c, getReplacementText(modifiers), offset, len);
    }
    
    /** @return Properly colored JLabel with text gotten from <CODE>getPaintText()</CODE>. */
    public java.awt.Component getPaintComponent( javax.swing.JList list, boolean isSelected, boolean cellHasFocus ) {
        // The space is prepended to avoid interpretation as HTML Label
        if (getIcon() != null) rubberStamp.setIcon(getIcon());
        
        rubberStamp.setText( displayText );
        if (isSelected) {
            rubberStamp.setBackground(selectionBackground);
            rubberStamp.setForeground(selectionForeground);
        } else {
            rubberStamp.setBackground(background);
            rubberStamp.setForeground(foreground);
        }
        return rubberStamp;
    }
    
    public final String getItemText() {
        return replacementText;
    }
    
    public String toString() {
        return getItemText();
    }
    
    Color getPaintColor() { return Color.BLUE; }
    
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////methods from CompletionItem interface////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    public CompletionTask createDocumentationTask() {
        return null; //no documentation supported for now
        //return new AsyncCompletionTask(new DocQuery(this));
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public void defaultAction(JTextComponent component) {
        int substOffset = getSubstituteOffset();
        if (substOffset == -1)
            substOffset = component.getCaretPosition();
        
        if(!shift) Completion.get().hideAll();
        substituteText(component, substOffset, component.getCaretPosition() - substOffset, shift);
    }
    
    static int substituteOffset = -1;
    
    public int getSubstituteOffset() {
        return substituteOffset;
    }
    
    public CharSequence getInsertPrefix() {
        return getItemText();
    }
    
    public Component getPaintComponent(boolean isSelected) {
        XMLCompletionResultItemPaintComponent component =
                new XMLCompletionResultItemPaintComponent.StringPaintComponent(getPaintColor());
        component.setSelected(isSelected);
        component.setString(getItemText());
        return component;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }
    
    public int getSortPriority() {
        return XML_ITEMS_SORT_PRIORITY;
    }
    
    public CharSequence getSortText() {
        return getItemText();
    }
    
    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }
    
    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }
    
    public void render(Graphics g, Font defaultFont,
            Color defaultColor, Color backgroundColor,
            int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((XMLCompletionResultItemPaintComponent)renderComponent).paintComponent(g);
    }
    
}
